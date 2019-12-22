package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.type.FeConfigTranType.CgKcpFavoPldf;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

public class qrcpld {

	private static final BizLog bizlog = BizLogUtil.getBizLog(qrcpld.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：查询优惠计划定义
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */
	public static void qryTranInfo(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpld.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpld.Property property,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpld.Output output) {
		property.setOrgnbr(CommTools.getBaseRunEnvs().getTrxn_branch());// 交易机构号
		property.setStart(CommTools.getBaseRunEnvs().getPage_start());// 页码
		property.setCount(CommTools.getBaseRunEnvs().getPage_size());// 页容量
	}

	public static void qrcpld(
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpld.Input input,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpld.Property property,
			final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpld.Output output) {
		String diplcd = input.getDiplcd(); // 优惠计划代码
		String brchno = input.getBrchno();// 机构号
		long pageno = CommTools.getBaseRunEnvs().getPage_start();// 页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();// 页容量

		long totlCount = 0;

		if (CommUtil.isNull(brchno)) {
			// 汇总有权限的查询机构
			IoBrchInfo brchinfo = SysUtil.getInstance(IoBrchInfo.class);
			brchinfo.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
			property.getBrchinfo().add(brchinfo);
			property.getBrchinfo().addAll(property.getUpBrchinfo());
			property.getBrchinfo().addAll(property.getDoBrchinfo());

			StringBuilder sb = new StringBuilder();
			Options<IoBrchInfo> brInfo = property.getBrchinfo();
			for (int i = 0; i < brInfo.size(); i++) {
				if (i == brInfo.size() - 1) {
					sb.append("'" + String.valueOf(brInfo.get(i).getBrchno())
							+ "'");
				} else {
					sb.append("'"
							+ String.valueOf(brInfo.get(i).getBrchno() + "',"));
				}
			}
			brchno = sb.toString();
		}
		bizlog.debug("<<<<<<机构号Brchno=" + brchno + ">>>>>>");

		Page<CgKcpFavoPldf> lstkcp_favo_pldf = FeDiscountDao
				.selall_kcp_favo_pldf(diplcd, brchno, (pageno - 1) * pgsize,
						pgsize, totlCount, false);
		Options<CgKcpFavoPldf> optkcp_favo_pldf = new DefaultOptions<CgKcpFavoPldf>();// 初始化输出对象
		optkcp_favo_pldf.addAll(lstkcp_favo_pldf.getRecords());
		output.setPinfos(optkcp_favo_pldf);

		CommTools.getBaseRunEnvs().setTotal_count(lstkcp_favo_pldf.getRecordCount());// 记录总数

	}
}
