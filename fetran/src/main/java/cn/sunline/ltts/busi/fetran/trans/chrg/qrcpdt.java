package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgCpdt;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrcpdt {
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrcpdt.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：查询超额优惠明细
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */

	public static void qryTranInfo( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpdt.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpdt.Property property,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpdt.Output output){
		property.setOrgnbr(CommTools.getBaseRunEnvs().getTrxn_branch());// 交易机构号
		property.setStart(CommTools.getBaseRunEnvs().getPage_start());// 页码
		property.setCount(CommTools.getBaseRunEnvs().getPage_size());// 页容量
	}
	public static void qrcpdt( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpdt.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpdt.Property property,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpdt.Output output){
		
		bizlog.method("<<<<<< qrcpdt begin >>>>>>");
		
		String smfacd = input.getSmfacd(); //超额优惠代码
		String brchno = input.getBrchno();

//		BigDecimal smstrt = input.getSmstrt(); //累积起点
		long pageno = CommTools.getBaseRunEnvs().getPage_start();//页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();

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

		Page<IoCgCpdt> lstkcp_favo_smdl = FeDiscountDao.selall_kcp_favo_smdl(smfacd, brchno, (pageno - 1) * pgsize, pgsize, totlCount, false);
		Options<IoCgCpdt> optkcp_favo_smdl = new DefaultOptions<IoCgCpdt>();// 初始化输出对象
		optkcp_favo_smdl.addAll(lstkcp_favo_smdl.getRecords());
		output.setPinfos(optkcp_favo_smdl);
		
		CommTools.getBaseRunEnvs().setTotal_count(lstkcp_favo_smdl.getRecordCount());// 记录总数

		
		
	}
}
