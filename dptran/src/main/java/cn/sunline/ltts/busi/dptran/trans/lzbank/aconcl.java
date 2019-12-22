package cn.sunline.ltts.busi.dptran.trans.lzbank;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.in.tables.In.KnsAcsqColl;
import cn.sunline.ltts.busi.in.tables.In.KnsAcsqCollDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.InEnumType;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CLCTST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CLERST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INPTSR;


public class aconcl {

	public static void dealCler( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Aconcl.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Aconcl.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Aconcl.Output output){
		
		String bathid = input.getBathid();//批次号
		BaseEnumType.E_CLACTP clactp = input.getClactp();//系统内账号类型
		
		String acctno = input.getAcctno();//账号
		String toacct = input.getToacct();//对方账号
		BigDecimal dranam = input.getDranam();//借方金额
		BigDecimal cranam = input.getCranam();//贷方金额
		String crcycd = input.getCrcycd().getValue();//币种
		String brchno = input.getBrchno();//机构
		E_CLCTST corest = input.getCorest();//传统核心记账状态
		
		/**
		 * 0，非空校验
		 */
		if(CommUtil.isNull(acctno)){
			throw DpModuleError.DpstComm.E9999("内部账号不能为空！");
		}
		if(CommUtil.isNull(toacct)){
			throw DpModuleError.DpstComm.E9999("对方账号不能为空！");
		}
		if(CommUtil.isNull(dranam)){
			throw DpModuleError.DpstComm.E9999("借方金额不能为空！");
		}
		if(CommUtil.isNull(cranam)){
			throw DpModuleError.DpstComm.E9999("贷方金额不能为空！");
		}
		if(CommUtil.isNull(crcycd)){
			throw DpModuleError.DpstComm.E9999("币种不能为空！");
		}
		if(CommUtil.isNull(brchno)){
			throw DpModuleError.DpstComm.E9999("机构不能为空！");
		}
//		if(CommUtil.isNull(corest)){
//			throw DpModuleError.DpstComm.E9999("传统核心记账状态不能为空！");
//		}
		
		/**
		 * 1，校验汇总明细
		 */
		KnsAcsqColl tblKnsAcsqColl =  KnsAcsqCollDao.selectOne_odb2(bathid, brchno, crcycd, clactp, false);
		if(CommUtil.isNull(tblKnsAcsqColl)){
			throw DpModuleError.DpstComm.E9999("汇总记录["+bathid+"]["+brchno+"]["+crcycd+"]["+clactp+"]不存在！");
		}
		if(InEnumType.E_CLERST._2 == tblKnsAcsqColl.getStatus()){
			throw DpModuleError.DpstComm.E9999("汇总记录["+bathid+"]["+brchno+"]["+crcycd+"]["+clactp+"]已经成功清算过了！");
		}
		if(CommUtil.compare(tblKnsAcsqColl.getAcctno(), acctno)!=0){
			throw DpModuleError.DpstComm.E9999("内部账号与记录不一致");
		}
		if(CommUtil.compare(tblKnsAcsqColl.getToacct(), toacct)!=0){
			throw DpModuleError.DpstComm.E9999("对方账号与记录不一致");
		}
		if(CommUtil.compare(tblKnsAcsqColl.getDranam(), dranam)!=0){
			throw DpModuleError.DpstComm.E9999("借方金额与记录不一致");
		}
		if(CommUtil.compare(tblKnsAcsqColl.getCrcycd(), crcycd)!=0){
			throw DpModuleError.DpstComm.E9999("币种与记录不一致");
		}
		if(CommUtil.compare(tblKnsAcsqColl.getBrchno(), brchno)!=0){
			throw DpModuleError.DpstComm.E9999("机构与记录不一致");
		}
		
		/***
		 *,2，校验内部账户
		 */
		GlKnaAcct glacct = GlKnaAcctDao.selectOne_odb1(acctno, false);
		GlKnaAcct glacctTo = GlKnaAcctDao.selectOne_odb1(toacct, false);
		if(CommUtil.isNull(glacct)){
			throw DpModuleError.DpstComm.E9999("内部账户["+acctno+"]不存在！");
		}
		if(CommUtil.isNull(glacctTo)){
			throw DpModuleError.DpstComm.E9999("内部账户["+toacct+"]不存在！");
		}
		
		/**
		 * 3,汇总来账金额小于汇总往账金额
		 */
		if(CommUtil.compare(dranam, BigDecimal.ZERO) >0 ){
			//调用内部户借方记账服务
			IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
			iaAcdrInfo.setTrantp(E_TRANTP.TR);
			iaAcdrInfo.setInptsr(E_INPTSR.GL00);
			iaAcdrInfo.setAcctno(glacct.getAcctno());
			iaAcdrInfo.setTranam(dranam);// 记账金额
			iaAcdrInfo.setCrcycd(crcycd);// 币种
			iaAcdrInfo.setAcbrch(BusiTools.getBusiRunEnvs().getCentbr());	
			iaAcdrInfo.setToacct(glacctTo.getAcctno());//对方账号
			iaAcdrInfo.setToacna(glacctTo.getAcctna());//对方户名
			iaAcdrInfo.setDscrtx("汇总来账金额小于汇总往账金额清算");
			SysUtil.getInstance(IoInAccount.class).ioInAcdr(
					iaAcdrInfo);
			
			//系统间往来对应贷方记账
			IaAcdrInfo iaAcdrInfo2 = SysUtil.getInstance(IaAcdrInfo.class);
			iaAcdrInfo2.setTrantp(E_TRANTP.TR);
			iaAcdrInfo2.setInptsr(E_INPTSR.GL00);
			iaAcdrInfo2.setAcctno(glacctTo.getAcctno());
			iaAcdrInfo2.setTranam(dranam); // 记账金额
			iaAcdrInfo2.setAcbrch(brchno);
			iaAcdrInfo2.setCrcycd(crcycd);
			iaAcdrInfo2.setToacct(glacct.getAcctno());
			iaAcdrInfo2.setToacna(glacct.getAcctna());
			iaAcdrInfo2.setDscrtx("汇总来账金额小于汇总往账金额清算");
			SysUtil.getInstance(IoInAccount.class).ioInAccr(
					iaAcdrInfo2);// 内部户贷方服
		} 
		
		
		/**
		 * 4,汇总来账金额大于汇总往账金额
		 */
		if(CommUtil.compare(cranam, BigDecimal.ZERO) >0 ){
			// 调用内部户贷方记账服务
			IaAcdrInfo iaAcdrInfo2 = SysUtil.getInstance(IaAcdrInfo.class);
			iaAcdrInfo2.setTrantp(E_TRANTP.TR);
			iaAcdrInfo2.setInptsr(E_INPTSR.GL00);
			iaAcdrInfo2.setAcctno(glacct.getAcctno());
			iaAcdrInfo2.setTranam(cranam); // 记账金额
			iaAcdrInfo2.setAcbrch(BusiTools.getBusiRunEnvs().getCentbr());
			iaAcdrInfo2.setCrcycd(crcycd);
			iaAcdrInfo2.setToacct(glacctTo.getAcctno());
			iaAcdrInfo2.setToacna(glacctTo.getAcctna());
			iaAcdrInfo2.setDscrtx("汇总来账金额大于汇总往账金额清算");
			SysUtil.getInstance(IoInAccount.class).ioInAccr(iaAcdrInfo2);
			
			//系统间往来对应借方记账
			IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
			iaAcdrInfo.setTrantp(E_TRANTP.TR);
			iaAcdrInfo.setInptsr(E_INPTSR.GL00);
			iaAcdrInfo.setAcctno(glacctTo.getAcctno());
			iaAcdrInfo.setTranam(cranam);// 记账金额
			iaAcdrInfo.setCrcycd(crcycd);// 币种
			iaAcdrInfo.setAcbrch(brchno);//账户机构	
			iaAcdrInfo.setDscrtx("汇总来账金额大于汇总往账金额清算");
			iaAcdrInfo.setToacct(glacct.getAcctno());//对方账号
			iaAcdrInfo.setToacna(glacct.getAcctna());//对方户名
			SysUtil.getInstance(IoInAccount.class).ioInAcdr(
					iaAcdrInfo);
		}
		
		/**
		 * 5,清算记账成功，更新清算汇总明细状态
		 */
		tblKnsAcsqColl.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
		tblKnsAcsqColl.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblKnsAcsqColl.setStatus(E_CLERST._2);// 已清算
		tblKnsAcsqColl.setCorest(corest);//传统核心记账状态
		KnsAcsqCollDao.update_odb1(tblKnsAcsqColl);	
		
		/**
		 * 6,输出赋值
		 */
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//主交易流水
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());//主交易日期
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());//主交易时间
	}
	
}
