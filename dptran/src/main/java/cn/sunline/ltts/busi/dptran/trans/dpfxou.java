package cn.sunline.ltts.busi.dptran.trans;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwne;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwneDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaStaPublic;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class dpfxou {
	private final static BizLog bizlog = BizLogUtil.getBizLog(dpfxou.class);

	public static void prcTransBefore(
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxou.Input Input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxou.Property Property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxou.Output Output) {

		String custac = Input.getCustac(); // 电子账号
		String acctno = Input.getAcctno(); // 定期子账号
		BigDecimal bigTranam = Input.getTranam(); // 交易金额
		
		
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaStaPublic casta = SysUtil.getInstance(IoCaStaPublic.class);
		
		// 电子账号
		if (CommUtil.isNull(custac)) {
			throw DpModuleError.DpstProd.BNAS0926();
		}

		// 定期子账号
		if (CommUtil.isNull(acctno)) {
			throw DpModuleError.DpstAcct.BNAS0837();
		}

		// 交易金额
		if(CommUtil.isNull(bigTranam)){
			throw DpModuleError.DpstAcct.BNAS0623();
		}else{			
			if (CommUtil.compare(bigTranam, BigDecimal.ZERO) <= 0) {
				throw DpModuleError.DpstComm.BNAS0627();
			}
		}

		// 查询定期账号信息
		//kna_accs tblKna_accs1 = Kna_accsDao.selectOne_odb2(acctno, false);
		IoCaKnaAccs tblKna_accs1 = caqry.getKnaAccsOdb2(acctno, false);
		if (CommUtil.isNull(tblKna_accs1)) {
			throw DpModuleError.DpstAcct.BNAS0838(acctno);
		}

		if (!CommUtil.equals(custac, tblKna_accs1.getCustac())) {
			throw DpModuleError.DpstAcct.BNAS0945(custac,acctno);
		}
		
		//查询是否子账户冻结   by  zhx
		KnbFrozOwne frozowne  =  KnbFrozOwneDao.selectOne_odb1(E_FROZOW.ACCTNO, acctno, false);
		if (CommUtil.isNotNull(frozowne)) {
			throw CaError.Eacct.BNAS0435();
		}

		// 查询电子账户信息
		//kna_cust tblKna_cust = Kna_custDao.selectOne_odb1(custac, false);
		IoCaKnaCust tblKna_cust = caqry.getKnaCustByCustacOdb1(custac, false);
		if (CommUtil.isNull(tblKna_cust)) {
			throw DpModuleError.DpstAcct.BNAS0946(custac);
		} else {
			if (E_ACCTST.NORMAL != tblKna_cust.getAcctst()) {
				throw DpModuleError.DpstAcct.BNAS0947( custac,
						tblKna_cust.getAcctst().getLongName());
			}
		}

		// 查询电子账户默认活期账户
		//kna_accs tblKna_accs = CaTools.getAcctAccs(custac,tblKna_accs1.getCrcycd());
		IoCaKnaAccs tblKna_accs = casta.CaTools_getAcctAccs(custac,tblKna_accs1.getCrcycd());
		
		Property.setCrcycd(tblKna_accs1.getCrcycd()); // 币种
		Property.setAcctna(tblKna_cust.getCustna()); // 客户名称
		Property.setSubsac(tblKna_accs1.getSubsac()); // 定期账号对应的子户号
		Property.setToacct(tblKna_accs.getAcctno()); // 默认活期账号，对方账号
		Property.setTosbac(tblKna_accs.getSubsac()); // 默认活期账号对应的子户号
		Property.setOpbrch(tblKna_cust.getBrchno());//开户机构
	}

	public static void chkTransAfter(
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxou.Input Input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxou.Property Property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxou.Output Output) {

		// 平衡性检查
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();

		IoCheckBalance ioCheckBalance = SysUtil
				.getInstance(IoCheckBalance.class);
		ioCheckBalance.checkBalance(trandt, transq,null);
	}

	public static void prcDpAcctTranam( final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxou.Input Input,  final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxou.Property Property,  final cn.sunline.ltts.busi.dptran.trans.intf.Dpfxou.Output Output){
		bizlog.debug("交易金额========="+Input.getTranam());
		bizlog.debug("支取利息========="+Property.getInstam());
		//处理存入金额，加上支取利息部分
		BigDecimal dpinam = BigDecimal.ZERO;
		//存入金额=支取金额+利息
		dpinam = dpinam.add(Input.getTranam()).add(Property.getInstam());
		Property.setDpinam(dpinam);
		
		bizlog.debug("处理后存入金额============="+Property.getDpinam());
	}
}
