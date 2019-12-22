package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdfDao;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_CGSMTP;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_PDUNIT;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_SMBDTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class adcpdf {
	private static final BizLog bizlog = BizLogUtil.getBizLog(adcpdf.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：超额累积优惠定义新增
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void adcpdf( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcpdf.Input input,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcpdf.Property property,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcpdf.Output output){
		bizlog.method("adcpdf begin >>>>>>");
		String smfacd = "CEYH" + BusiTools.getSequence("smfacd_seq", 4); //超额优惠代码
		String smfana = input.getSmfana(); //超额优惠代码名称
		String brchno = input.getBrchno(); //机构号
		E_SMBDTP smbdtp = input.getSmbdtp(); //累积主体类型
		E_CGSMTP cgsmtp = input.getCgsmtp(); //收费累计类型
		String efctdt = input.getEfctdt(); //失效日期
		String inefdt = input.getInefdt(); //生效日期
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期 
		E_PDUNIT pdunit = input.getPdunit();//周期
		
//		SysUtil.getInstance(IoBrchSvcType.class).getBrchInfo(brchno);
//		if(CommUtil.isNull(smfacd)){
//			throw FeError.Chrg.E1014("超额优惠代码");
//		}
		if(CommUtil.isNull(smfana)){
			throw FeError.Chrg.BNASF029();
		}
		if(CommUtil.isNull(brchno)){
			throw FeError.Chrg.BNASF131();
		}
		if(CommUtil.isNull(smbdtp)){
			throw FeError.Chrg.BNASF187();
		}
		if(CommUtil.isNull(cgsmtp)){
			throw FeError.Chrg.BNASF115();
		}
		if(CommUtil.isNull(efctdt)){
			throw FeError.Chrg.BNASF207();
		}
		if(CommUtil.isNull(inefdt)){
			throw FeError.Chrg.BNASF212();
		}
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}
		if (DateUtil.compareDate(efctdt, sTime) <= 0) {
			throw FeError.Chrg.BNASF204();
		}
		if(CommUtil.isNull(pdunit)){
			throw FeError.Chrg.BNASF339();
		}
		
	   //判断传入机构是否存在
	   IoSrvPbBranch brchSvrType = SysUtil.getInstance(IoSrvPbBranch.class);
	   try{
		   IoBrchInfo brchinfo = brchSvrType.getBranch(brchno);
	   }catch(Exception e){
		   throw FeError.Chrg.E9999(e.getMessage());
	   }
		
		//省县两级参数管理员均有操作权限，县级行社参数管理员只允许新增本行社
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), BusiTools.getBusiRunEnvs().getCentbr()) &&
				!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), input.getBrchno())) {
			throw FeError.Chrg.BNASF158();
		}
		
		KcpFavoSmdf tblFavosmdf = SysUtil.getInstance(KcpFavoSmdf.class);
		
		tblFavosmdf.setSmfacd(smfacd); //超额优惠代码
		tblFavosmdf.setSmfana(smfana); //超额优惠代码名称
		tblFavosmdf.setSmbdtp(input.getSmbdtp()); //累积主体类型
		tblFavosmdf.setPdunit(input.getPdunit()); //周期
		tblFavosmdf.setCgsmtp(input.getCgsmtp()); //累积类型
		tblFavosmdf.setEfctdt(efctdt); //生效日期
		tblFavosmdf.setInefdt(inefdt); //失效日期
		tblFavosmdf.setBrchno(brchno); //机构号
		KcpFavoSmdfDao.insert(tblFavosmdf);
		
		//增加审计
		ApDataAudit.regLogOnInsertParameter(tblFavosmdf);
		
		output.setSmfacd(smfacd);
	}
	
}
