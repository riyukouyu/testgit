package cn.sunline.ltts.busi.dptran.trans.close;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgChrgSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccChngbrSvc;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSignDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.ChkCuad;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType;
import cn.sunline.ltts.busi.wa.type.WaAcctType.IoWaKnaRelt;


public class qrclam {

	public static void prcQryCloseBefore(
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Output output) {
		
		//输入接口校验
		String cardno = input.getCardno();
		if (CommUtil.isNull(cardno)) {
			throw DpModuleError.DpstComm.BNAS0311();
		}
		
		// 调用方法检查电子账户销户需要满足的状态和状态字并获取电子账号ID
		IoCaKnaCust cplKnaCust = clsoeAcctStautsCheck.acctStautsCheck(cardno);
		String sCustac = cplKnaCust.getCustac();// 电子账号ID
		String custna = cplKnaCust.getCustna();
		
		property.setCustna(custna);
		
		//查询出电子账户的账户分类
		E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
				.qryAccatpByCustac(sCustac);
		
		//add by songkl 20161219 新增钱包校验
		//查询是否开通亲情钱包，若开通亲情钱包则允许销户
		List<IoWaKnaRelt> tblknarelt = DpAcctQryDao.selknareltbycustac(sCustac, false);
		if(CommUtil.isNotNull(tblknarelt)){
			throw DpModuleError.DpstAcct.BNAS0445();
		}
		
		//add by xiongzhao 20170106 新增签约校验
		KnaAcct tblKnaAcct = CapitalTransDeal.getSettKnaAcctAc(sCustac);
		// 获取转存签约明细信息
		IoCaKnaSignDetl cplkna_sign_detl = ActoacDao.selKnaSignDetl(tblKnaAcct.getAcctno(),
				E_SIGNTP.ZNCXL, E_SIGNST.QY, false);
		if (CommUtil.isNotNull(cplkna_sign_detl)) {
			throw DpModuleError.DpstAcct.BNAS0996();
		}
		
		//将查询出的电子账号ID映射到属性区
		property.setCustac(sCustac);
		//将账户分类映射到属性区
		property.setAccatp(accatp);
	}

	public static void prcQryClAfter(
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Output output) {
		//检查属性是否不为空
		//电子账号
		if (CommUtil.isNull(property.getCustac())) {
			throw DpModuleError.DpstProd.BNAS0935();
		}
		//存款余额利息试算输出
		if (CommUtil.isNull(property.getChkot())) {
			throw DpModuleError.DpstComm.BNAS1035();
		}
		
		// 检查电子账户是否只开过户
		E_YES___ obopfg = SysUtil.getInstance(IoAccChngbrSvc.class)
				.judgeOpenJust(property.getCustac());
		
		output.setObopfg(obopfg);// 是否只开过户标志
		output.setTotlam(property.getChkot().getTotlam());//销户汇总金额
		output.setTotlst(property.getChkot().getIntrvl());//销户汇总利息
		output.setTotlcp(property.getChkot().getTotlam().subtract(property.getChkot().getIntrvl()));//销户汇总本金
	}

	/**
	 * 
	 * @Title: chkRedpack
	 * @Description:(销户前检查是否有未落地红包)
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2016年11月25日 上午11:01:25
	 * @version V2.3.0
	 */
	public static void chkRedpack(
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Output output) {
		String custid = "";
		ChkCuad cplKnaCuad = ActoacDao.selKnaCuad(property.getCustac(), false);
		if (CommUtil.isNotNull(cplKnaCuad)) {
			custid = cplKnaCuad.getCustid();
		}
		CapitalTransDeal.chkRedpack(input.getCardno(), custid);
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
	public static void chkNotChrgFee( final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Output output){
		PbEnumType.E_YES___ isfee = SysUtil.getInstance(IoCgChrgSvcType.class).CgChageRgstNotChargeByCustac(property.getCustac());
		if(isfee == PbEnumType.E_YES___.YES){
			throw DpModuleError.DpstAcct.BNAS0854();
		}
	}
	
	/**
	 * 
	 * @Title: chkinspfg 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param input
	 * @param property
	 * @param output
	 * @author songkailei
	 * @date 2017年1月12日 下午4:13:49 
	 * @version V2.3.0
	 */
	public static void chkinspfg( final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrclam.Output output){
		
		E_INSPFG inspfg = property.getInspfg();
		if(E_INSPFG.INVO == inspfg){
			throw DpModuleError.DpstAcct.BNAS0770();
		}
		
		if(E_INSPFG.SUSP == inspfg){
			throw DpModuleError.DpstComm.BNAS0706();
		}
	}
}
