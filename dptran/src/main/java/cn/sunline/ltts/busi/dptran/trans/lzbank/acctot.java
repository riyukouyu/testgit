package cn.sunline.ltts.busi.dptran.trans.lzbank;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_PASTAT;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_INTYPE;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdm;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmContro;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmControDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmDao;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * 银联CUPS转入
 * 
 * @param input
 * @param property
 * @param output
 */
public class acctot {

	public static void beforeCheckDeal(final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctot.Input input,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctot.Property property,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctot.Output output) {
		if (CommUtil.isNull(input.getPrepsq())) {// 银联前置流水
			throw DpModuleError.DpstComm.BNAS1914();
		}

		// 金额
		if (CommUtil.isNull(input.getPrepdt())) {// 银联前置日期
			throw DpModuleError.DpstComm.BNAS1916();
		}

		if (CommUtil.isNull(input.getCrcycd())) {// 币种
			throw DpModuleError.DpstComm.BNAS0195();
		}

		if (CommUtil.isNull(input.getUnkpdt())) {// 银联清算日期
			throw DpModuleError.DpstComm.BNAS1917();
		}

		if (CommUtil.isNull(input.getAcctid())) {// 服务商id和商户id
			throw DpModuleError.DpTrans.TS020013();
		}

		if (CommUtil.isNull(input.getUniseq())) {// 银联流水
			throw DpModuleError.DpstComm.BNAS1921();
		}

		if (CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0) { // 校验交易金额
			throw DpModuleError.DpstComm.BNAS0627();
		}
		if (CommUtil.isNotNull(input.getFinsty())
				&& CommUtil.equals(input.getFinsty().getValue(), E_INTYPE.D0.getValue())) {
			if (CommUtil.isNull(input.getRetrsq()) || CommUtil.isNull(input.getRetrdt())) {
			if (CommUtil.isNull(input.getRetrsq()) || CommUtil.isNull(input.getRetrdt())) {
				throw DpModuleError.DpTrans.TS020019();
			}
			KnlIoblEdmContro knlIoblEdmContro = KnlIoblEdmControDao.selectOneWithLock_edmOdb01(input.getRetrsq(),
					input.getRetrdt(), false);

			if (CommUtil.isNull(knlIoblEdmContro)) {
				throw DpModuleError.DpTrans.TS020020();
			}
			// 如果有记录直接返回
			if (!CommUtil.equals(knlIoblEdmContro.getEdmflg().getValue(), E_PASTAT.UNTREAT.getValue())) {
				output.setMntrdt(knlIoblEdmContro.getTrandt());
				output.setMntrsq(knlIoblEdmContro.getMntrsq());
				output.setMntrtm(knlIoblEdmContro.getTmstmp());
				property.setIsexit(E_YES___.YES);
				return;
			}

			knlIoblEdmContro.setEdmflg(E_PASTAT.PROC);// 是否代发
			knlIoblEdmContro.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			knlIoblEdmContro.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());

			KnlIoblEdmControDao.updateOne_edmOdb01(knlIoblEdmContro);
		}
		}

		KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(input.getAcctid(), false);
		if (CommUtil.isNull(tblKnaSbad)) {
			throw DpModuleError.DpAcct.AT020054(input.getAcctid());
		}
		KnaAcct knaAcct = KnaAcctDao.selectOne_odb1(tblKnaSbad.getAcctno(), false);
		if (CommUtil.isNull(knaAcct)) {// 品牌子户号对应不存在
			throw DpModuleError.DpTrans.TS020014();
		}
		
		property.setBrchno(knaAcct.getBrchno());
		property.setOtacct(knaAcct.getAcctno());
		property.setOtacna(knaAcct.getAcctna());
		property.setCustac(knaAcct.getCustac());
		

		/**
		 * 重复请求校验
		 */
//		KnlIoblEdm knlIoblEdm = KnlIoblEdmDao.selectOne_odb02(input.getPrepsq(), input.getPrepdt(), false);
//		if (CommUtil.isNotNull(knlIoblEdm)) {
//			output.setMntrdt(knlIoblEdm.getTrandt());
//			output.setMntrsq(knlIoblEdm.getMntrsq());
//			output.setMntrtm(knlIoblEdm.getTmstmp());
//			property.setIsexit(E_YES___.NO);
//			return;
//		}
		property.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));

		// 业务编码查询赋值
//		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("DP.ACCTOT", "busino", "%", "%", false);
//		if (CommUtil.isNull(knpParameter)) {
//			throw DpModuleError.DpTrans.TS020021();
//		}
//		// 待清算业务编码
//		property.setBusino(knpParameter.getParm_value1());

	}

	public static void afterDeal(final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctot.Input input,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctot.Property property,
			final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Acctot.Output output) {

		// 后期是否需要登记原支付前置流水和日期
		KnlIoblEdm knlIoblEdm = SysUtil.getInstance(KnlIoblEdm.class);
		knlIoblEdm.setAcctid(input.getAcctid());
		knlIoblEdm.setFinsty(input.getFinsty());	
		knlIoblEdm.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		knlIoblEdm.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		knlIoblEdm.setPrepdt(input.getPrepdt());
		knlIoblEdm.setPrepsq(input.getPrepsq());
		knlIoblEdm.setTranam(input.getTranam());
//		knlIoblEdm.setTranst(E_CUPSST.CLWC);
		knlIoblEdm.setRetrsq(input.getRetrsq());
		knlIoblEdm.setRetrdt(input.getRetrdt());
		
		KnlIoblEdmDao.insert(knlIoblEdm);
		// 更新防重登记簿

		// 平衡性检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(),
				CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._35);

		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrtm(BusiTools.getBusiRunEnvs().getComputer_time());

	}
}
