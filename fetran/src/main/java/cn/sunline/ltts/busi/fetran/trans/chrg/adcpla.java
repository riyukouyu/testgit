package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPldfDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPljo;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoPljoDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgCplj;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubBrch;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 
 * @ClassName: adcpla 
 * @Description: 新增优惠计划（新增一条优惠计划定义和对应的多条优惠计划明细） 
 * @author chengen
 * @date 2016年7月30日 上午10:17:43 
 *
 */
public class adcpla {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(adcpla.class);
public static void adcpla( final cn.sunline.ltts.busi.fetran.trans.intf.Adcpla.Input input,  final cn.sunline.ltts.busi.fetran.trans.intf.Adcpla.Property property,  final cn.sunline.ltts.busi.fetran.trans.intf.Adcpla.Output output){
	bizlog.method("adcpla begin >>>>>>");
	String diplcd = "YHJH" + BusiTools.getSequence("diplcd_seq", 4); // 优惠计划代码
	String planna = input.getPlanna(); // 优惠计划代码名称
	String brchno = input.getBrchno(); // 机构号
	String efctdt = input.getEfctdt(); // 生效日期
	String inefdt = input.getInefdt(); // 失效日期
	String explan = input.getExplan(); // 备注
	String sTime = CommTools.getBaseRunEnvs().getTrxn_date();// 系统日期
	
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
//	if (CommUtil.isNull(explan)) {
//		throw FeError.Chrg.E1014("备注");
//	}

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

//	// 失效日期必须大于生效日期
//	if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
//		throw FeError.Chrg.E9999("失效日期必须大于生效日期");
//	}
//
//	// 生效日期必须大于当前系统日期
//	if (DateUtil.compareDate(efctdt, sTime) <= 0) {
//		throw FeError.Chrg.E9999("生效日期必须大于当前系统日期");
//	}
	
	//省县两级参数管理员均有操作权限，县级行社参数管理员只允许新增本行社
	if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), BusiTools.getBusiRunEnvs().getCentbr()) &&
			!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), input.getBrchno())) {
		throw FeError.Chrg.BNASF158();
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
	
	if (CommUtil.isNull(input.getLjinfo())) {
		throw FeError.Chrg.BNASF309();
	}	
	
	for (IoCgCplj kcpFavoPljo: input.getLjinfo()) {
		
		String seqnum = BusiTools.getSequence("diplseqnum_seq", 10);
		
		if (CommUtil.isNull(kcpFavoPljo.getDimecg())) {
			throw FeError.Chrg.BNASF120();
		}
		
		if (CommUtil.isNull(FeDimeDao.selone_kcp_dime_cg(kcpFavoPljo.getDimecg(), false))) {
			throw FeError.Chrg.BNASF250();
		}
		if (CommUtil.isNull(kcpFavoPljo.getRelvfg())) {
			throw FeError.Chrg.BNASF117();
		}
		
		if (CommUtil.isNotNull(KcpFavoPljoDao.selectAll_odb3(diplcd, kcpFavoPljo.getDimecg(), false))) {
			throw FeError.Chrg.BNASF118();
		}
		if (CommUtil.isNull(FeDimeDao.selone_evl_dime(kcpFavoPljo.getFadmvl(), kcpFavoPljo.getDimecg(), false))
				&& CommUtil.isNotNull(kcpFavoPljo.getFadmvl())) {
			throw FeError.Chrg.BNASF252();
		}
		if (CommUtil.compare(BigDecimal.ZERO, kcpFavoPljo.getIldmdn()) != 0
				&& CommUtil.compare(BigDecimal.ZERO, kcpFavoPljo.getIldmup()) != 0
				&& CommUtil.compare(kcpFavoPljo.getIldmdn(), kcpFavoPljo.getIldmup()) >= 0) {
			throw FeError.Chrg.BNASF122();
		}
		
		if (CommUtil.isNull(kcpFavoPljo.getIldmdn())
				|| CommUtil.isNull(kcpFavoPljo.getIldmup())) {
			throw FeError.Chrg.BNASF121();
		}
		
		if ((CommUtil.compare(BigDecimal.ZERO, kcpFavoPljo.getIldmdn()) >= 0
				|| CommUtil.compare(BigDecimal.ZERO, kcpFavoPljo.getIldmup()) >= 0)
						&& CommUtil.isNull(kcpFavoPljo.getFadmvl())) {
			throw FeError.Chrg.BNASF311();
		}
		
		if((CommUtil.compare(BigDecimal.ZERO, kcpFavoPljo.getIldmdn()) < 0
				|| CommUtil.compare(BigDecimal.ZERO, kcpFavoPljo.getIldmup()) < 0)
						&& CommUtil.isNotNull(kcpFavoPljo.getFadmvl())) {
					throw FeError.Chrg.BNASF310();
			}	
		
		KcpFavoPljo tblKcpfavopljo = SysUtil.getInstance(KcpFavoPljo.class);
		tblKcpfavopljo.setDiplcd(diplcd);
		tblKcpfavopljo.setSeqnum(seqnum);
		tblKcpfavopljo.setDimecg(kcpFavoPljo.getDimecg());
		tblKcpfavopljo.setRelvfg(kcpFavoPljo.getRelvfg());
		tblKcpfavopljo.setIldmup(kcpFavoPljo.getIldmup());
		tblKcpfavopljo.setIldmdn(kcpFavoPljo.getIldmdn());
		tblKcpfavopljo.setFadmvl(kcpFavoPljo.getFadmvl());
		KcpFavoPljoDao.insert(tblKcpfavopljo);
		
		//增加审计
		ApDataAudit.regLogOnInsertParameter(tblKcpfavopljo);
		
		output.setDiplcd(diplcd);

	}
	
}

}
