package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldfDao;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubBrch;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class adcpld {
	private static final BizLog bizlog = BizLogUtil.getBizLog(adcpld.class);

	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：新增优惠计划定义
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void adcpld( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adcpld.Input input){
		bizlog.method("adcpld begin >>>>>>");
		String diplcd = "YHJH" + BusiTools.getSequence("diplcd_seq", 4); // 优惠计划代码
		String planna = input.getPlanna(); // 优惠计划代码名称
		String brchno = input.getBrchno(); // 机构号
		String efctdt = input.getEfctdt(); // 生效日期
		String inefdt = input.getInefdt(); // 失效日期
		String explan = input.getExplan(); // 备注
//		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller(); //交易柜员
//		String transq = CommTools.getBaseRunEnvs().getTrxn_seq(); //流水号
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期

		// 优惠计划代码
		if (CommUtil.isNull(diplcd)) {
			throw FeError.Chrg.BNASF303();
		}

		// 优惠计划代码名称
		if (CommUtil.isNull(planna)) {
			throw FeError.Chrg.BNASF304();
		}

		// 机构号
		if (CommUtil.isNull(brchno)) {
			throw FeError.Chrg.BNASF131();
		}

		// 生效日期
		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}

		// 失效日期
		if (CommUtil.isNull(inefdt)) {
			throw FeError.Chrg.BNASF212();
		}

		// 备注
//		if (CommUtil.isNull(explan)) {
//			throw FeError.Chrg.E1014("备注");
//		}

		// 失效日期必须大于生效日期
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}

		// 生效日期必须大于当前系统日期
		if (DateUtil.compareDate(efctdt, sTime) <= 0) {
			throw FeError.Chrg.BNASF204();
		}

		// 判断机构号信息是否存在
		IoPbKubBrch infoBrch = FeDiscountDao.selKubBrchByBrhcno(brchno, false);
		if (CommUtil.isNull(infoBrch)) {
			throw FeError.Chrg.BNASF133();
		}

		KcpFavoPldf tblKcpfavopldf = SysUtil.getInstance(KcpFavoPldf.class);// 优惠计划定义表

		tblKcpfavopldf.setDiplcd(diplcd); // 优惠计划
		tblKcpfavopldf.setPlanna(planna); // 优惠计划名称
		tblKcpfavopldf.setBrchno(brchno); // 机构号
		tblKcpfavopldf.setEfctdt(efctdt); // 生效日期
		tblKcpfavopldf.setInefdt(inefdt); // 失效日期
		tblKcpfavopldf.setExplan(explan); // 备注

		KcpFavoPldfDao.insert(tblKcpfavopldf);// 登记优惠计划定义表
		
		//增加审计
		ApDataAudit.regLogOnInsertParameter(tblKcpfavopldf);
		
	}
}
