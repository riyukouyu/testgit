package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.fe.namedsql.FeFormulaDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgCmdt;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrcmdt {
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrcmdt.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：计费公式明细表查询
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */
	public static void qryTranInfo( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcmdt.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcmdt.Property property,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcmdt.Output output){
		property.setOrgnbr(CommTools.getBaseRunEnvs().getTrxn_branch());// 交易机构号
		property.setStart(CommTools.getBaseRunEnvs().getPage_start());// 页码
		property.setCount(CommTools.getBaseRunEnvs().getPage_size());// 页容量
	}
	public static void qrcmdt( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcmdt.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcmdt.Property property,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcmdt.Output output){
		
		bizlog.method("<<<<<< qrcmdt begin >>>>>>");
	    
		String chrgfm = input.getChrgfm();
	    String brchno = null;
		long pageno = CommTools.getBaseRunEnvs().getPage_start();//页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();//页容量
		long totlCount = 0;
		
		if(CommUtil.isNull(brchno)){
			//汇总有权限的查询机构
			IoBrchInfo brchinfo = SysUtil.getInstance(IoBrchInfo.class);
			brchinfo.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
			property.getBrchinfo().add(brchinfo);
			property.getBrchinfo().addAll(property.getUpBrchinfo());
			property.getBrchinfo().addAll(property.getDoBrchinfo());
			
			StringBuilder sb = new StringBuilder();
			Options<IoBrchInfo> brInfo = property.getBrchinfo();
			for (int i = 0; i < brInfo.size(); i++) {
				if(i == brInfo.size() - 1){
					sb.append("'" + String.valueOf(brInfo.get(i).getBrchno()) + "'");
				}else{
					sb.append("'" + String.valueOf(brInfo.get(i).getBrchno() + "',"));
				}
			}
			brchno = sb.toString();
		}
		
		bizlog.debug("<<<<<<机构号Brchno=" + brchno + ">>>>>>");
		
		//查询计费公式明细
		Page<IoCgCmdt> lstkcp_chrg_fmdt = FeFormulaDao.selall_kcp_chrg_fmdt(chrgfm, brchno, null, null, null, (pageno-1)*pgsize, pgsize, totlCount, false);
		Options<IoCgCmdt> optkcp_chrg_fmdt = new DefaultOptions<IoCgCmdt>();// 初始化输出对象
		optkcp_chrg_fmdt.addAll(lstkcp_chrg_fmdt.getRecords());
		
		//输出
		output.setPinfos(optkcp_chrg_fmdt);
		CommTools.getBaseRunEnvs().setTotal_count(lstkcp_chrg_fmdt.getRecordCount());// 记录总数

	}
}
