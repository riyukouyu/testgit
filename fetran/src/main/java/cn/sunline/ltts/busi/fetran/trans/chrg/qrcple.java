package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgCple;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

public class qrcple {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrcple.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：优惠计划解析
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */

	public static void qryTranInfo( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcple.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcple.Property property,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcple.Output output){
		property.setOrgnbr(CommTools.getBaseRunEnvs().getTrxn_branch());// 交易机构号
		property.setStart(CommTools.getBaseRunEnvs().getPage_start());// 页码
		property.setCount(CommTools.getBaseRunEnvs().getPage_size());// 页容量
	}

	public static void qrcple( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcple.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcple.Property property,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcple.Output output){
		String chrgcd = input.getChrgcd(); // 费种代码
		String diplcd = input.getDiplcd();// 优惠计划代码

		long pageno = CommTools.getBaseRunEnvs().getPage_start();// 页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();// 页容量
		String brchno = input.getBrchno();

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

		Page<IoCgCple> lstkcp_favo_plex = FeDiscountDao .selall_kcp_favo_plex(chrgcd, diplcd, brchno, (pageno - 1) * pgsize, pgsize, totlCount, false);
		Options<IoCgCple> optkcp_favo_plex = new DefaultOptions<IoCgCple>();// 初始化输出对象
		optkcp_favo_plex.addAll(lstkcp_favo_plex.getRecords());
		output.setPinfos(optkcp_favo_plex);
		
		CommTools.getBaseRunEnvs().setTotal_count(lstkcp_favo_plex.getRecordCount());// 记录总数

	}
}
