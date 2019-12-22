package cn.sunline.ltts.busi.dptran.trans;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;

public class dpfxin {

	/**
	 * 交易后处理
	 */
	public static void chkTransAfter(
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxin.Input Input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxin.Property Property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxin.Output Output) {
		
		//平衡性检查
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		
		IoCheckBalance ioCheckBalance = SysUtil.getInstance(IoCheckBalance.class);
		ioCheckBalance.checkBalance(trandt, transq,null);
	}

	/**
	 * 交易前处理
	 */
	public static void chkTransBefore(
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxin.Input Input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxin.Property Property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxin.Output Output) {
		
		//处理368及788两款定存产品每个客户只能购买一款
		String prodcdOne = "010010003";
		String prodcdTwo = "010010004";
		int saveCount = 1; //限制份数
		
		String custac = Input.getCustac(); //电子账号
		String custno = null; //客户号
		//kna_cust tblKna_cust = null;
		IoCaKnaCust tblKna_cust = SysUtil.getInstance(IoCaKnaCust.class);
		if(CommUtil.isNull(Input.getTranam())){
			throw DpModuleError.DpstAcct.BNAS0623();
		}
		
		KupDppb tblKupDppb1 = KupDppbDao.selectOne_odb1(prodcdOne, true);
		KupDppb tblKupDppb2 = KupDppbDao.selectOne_odb1(prodcdTwo, true);
		
		//电子账号
		if(CommUtil.isNull(custac)){
			throw DpModuleError.DpstComm.BNAS0955();
		}else{
			//tblKna_cust = Kna_custDao.selectOne_odb1(custac, false);
			tblKna_cust = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(custac, false);
			if(CommUtil.isNull(tblKna_cust)){
				throw DpModuleError.DpstComm.BNAS0754();
			}
			if(E_ACCTST.NORMAL != tblKna_cust.getAcctst()){
				throw DpModuleError.DpstAcct.BNAS0905(tblKna_cust.getAcctst().getLongName());
			}
			
			custno = tblKna_cust.getCustno(); //客户号
		}
		
		List<KnaFxac> lstKnaFxac = DpAcctQryDao.selKnaFxacCountByCustno(prodcdOne, prodcdTwo, custno, false);
		
		if(CommUtil.isNotNull(lstKnaFxac) && lstKnaFxac.size() >= saveCount){
			KnaFxac tblKnaFxac = lstKnaFxac.get(0);
			String prodtx = null;
			if(CommUtil.equals(tblKnaFxac.getProdcd(), prodcdOne)){
				prodtx = tblKupDppb1.getProdtx();
			}else if(CommUtil.equals(tblKnaFxac.getProdcd(), prodcdTwo)){
				prodtx = tblKupDppb2.getProdtx();
			}
			
			throw DpModuleError.DpstAcct.BNAS0247(prodtx);
		}
	}
}
