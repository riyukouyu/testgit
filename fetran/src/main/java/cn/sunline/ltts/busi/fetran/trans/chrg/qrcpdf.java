package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSmdf;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrcpdf {
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrcpdf.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：超额优惠定义
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */
	public static void qryTranInfo( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpdf.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpdf.Property property,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpdf.Output output){
		property.setOrgnbr(CommTools.getBaseRunEnvs().getTrxn_branch());// 交易机构号
		property.setStart(CommTools.getBaseRunEnvs().getPage_start());// 页码
		property.setCount(CommTools.getBaseRunEnvs().getPage_size());// 页容量
	}
	public static void qrcpdf( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpdf.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpdf.Property property,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpdf.Output output){
		
		bizlog.method("<<<<<< qrcpdf begin >>>>>>");
		
		String smfacd = input.getSmfacd();
		String brchno = input.getBrchno();
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
			
		Page<KcpFavoSmdf> lstkcp_favo_smdf = FeDiscountDao.selall_kcp_favo_smdf(smfacd, brchno,(pageno - 1) * pgsize, pgsize, totlCount, false);
		Options<KcpFavoSmdf> optkcp_favo_smdf = new DefaultOptions<KcpFavoSmdf>();// 初始化输出对象
		optkcp_favo_smdf.addAll(lstkcp_favo_smdf.getRecords());
		output.setPinfos(optkcp_favo_smdf);
		
		CommTools.getBaseRunEnvs().setTotal_count(lstkcp_favo_smdf.getRecordCount());// 记录总数

	}
}
