package cn.sunline.ltts.busi.dptran.trans.close;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.ChkCuad;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.PbEnumType;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class ckclac {

	
	private static BizLog log = BizLogUtil.getBizLog(ckclac.class);
	
	public static void DealTransBefore( final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Output output){
		
		String cardno = input.getCardno();
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		
		log.debug("*********************************************");
		
		
		
		IoCaKnaAcdc acdc = caqry.getKnaAcdcOdb2(cardno, false);
		if(CommUtil.isNull(acdc)){
			throw DpModuleError.DpstComm.BNAS0754();
		}
		
		if (acdc.getStatus() == E_DPACST.CLOSE) {
			throw DpModuleError.DpstComm.BNAS0164();
		}
		
		IoCaKnaCust cust = caqry.getKnaCustByCustacOdb1(acdc.getCustac(), false);
		if (CommUtil.isNull(cust)) {
			throw DpModuleError.DpstComm.BNAS0750();
		}
		/**
		if(CommUtil.compare(cust.getCorpno(), CommTools.getBaseRunEnvs().getBusi_org_id()) != 0){
			throw DpModuleError.DpstComm.E9999("不能跨法人交易");
		}
		**/
		property.setCustac(acdc.getCustac());
		property.setCustna(cust.getCustna());
		output.setBrchno(cust.getBrchno());
		output.setCustna(cust.getCustna());
	}
	
	public static void DealTransAfter( final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Output output){
		output.setAccttp(property.getAccttp());
		output.setClosam(property.getChkot().getTotlam());
		output.setPdlist(property.getChkot().getPdlist());
	}

	/**
	 * @Title: chkRedpack 
	 * @Description:销户前检查是否有未领取红包  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年11月25日 上午10:38:12 
	 * @version V2.3.0
	 */
	public static void chkRedpack( final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Output output){
		String custid = "";
		ChkCuad cplKnaCuad = ActoacDao.selKnaCuad(property.getCustac(), false);
		if (CommUtil.isNotNull(cplKnaCuad)) {
			custid = cplKnaCuad.getCustid();
		}
		CapitalTransDeal.chkRedpack(input.getCardno(), custid);
	}

	public static void InspfgAfter( final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Output output){
		
		E_INSPFG inspfg = property.getInspfg();
		if(inspfg == E_INSPFG.INVO){
			throw DpModuleError.DpstAcct.BNAS1910();
		}
				
		
	}
	
	/**
	 * @Title: chkNotChrgFee 
	 * @Description: 是否有未收讫费用检查  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年12月22日 下午2:02:04 
	 * @version V2.3.0
	 */
	public static void chkNotChrgFee( final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Input input,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Property property,  final cn.sunline.ltts.busi.dptran.trans.close.intf.Ckclac.Output output){
		PbEnumType.E_YES___ isfee = SysUtil.getInstance(IoCgChrgSvcType.class).CgChageRgstNotChargeByCustac(property.getCustac());
		if(isfee == PbEnumType.E_YES___.YES){
			throw DpModuleError.DpstAcct.BNAS0854();
		}
	}
}
