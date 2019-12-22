package cn.sunline.ltts.busi.dp.serviceimpl;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.namedsql.DpJointDao;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaAcctJoint;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaAcctJointDao;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaAcctJointLimit;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaAcctJointLimitDao;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnbJointIobl;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnbJointIoblDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpJointSvc.AddJointAccount.Output;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpJointType.IoDpKnaAcctJointInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpJointType.IoDpKnaAcctJointLimitInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMONDR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_JOTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CLERST;

/**
 * 联名账户相关交易
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoDpJointSvcImpl", longname = "联名账户相关交易", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoDpJointSvcImpl implements
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpJointSvc {
	/**
	 * 新增联合账户
	 * 
	 */
	@Override
	public void addJointAccount(IoDpKnaAcctJointInfo jointa, Output output) {
		// TODO Auto-generated method stub

		RunEnvsComm runEnvs = CommTools.getBaseRunEnvs();
		String joinac = genJointNo(); //TODO CommTools.gen 账号生成
		KnaAcctJoint knaAcctJoint = SysUtil.getInstance(KnaAcctJoint.class);
		knaAcctJoint.setJoinac(joinac);
		knaAcctJoint.setJoinna(jointa.getJoinna());
		knaAcctJoint.setPlatno(jointa.getPlatno());
		knaAcctJoint.setBrchno(runEnvs.getTrxn_branch());
		knaAcctJoint.setCustac(jointa.getCustac());
		knaAcctJoint.setCustno(jointa.getCustno());
		knaAcctJoint.setOpendt(runEnvs.getTrxn_date());
		knaAcctJoint.setOpensq(runEnvs.getTrxn_seq());
		knaAcctJoint.setOpense(CommTools.getBaseRunEnvs().getChannel_id());
		// knaAcctJoint.setLastbl();
		// knaAcctJoint.setUpbldt();
		// knaAcctJoint.setOnlnbl();
		// knaAcctJoint.setClosdt();
		// knaAcctJoint.setClossq();
		 knaAcctJoint.setLastdt(runEnvs.getTrxn_date());
		 knaAcctJoint.setLastsq(runEnvs.getTrxn_seq());
		// knaAcctJoint.setDetlsq();
		// knaAcctJoint.setObgaon();
		// knaAcctJoint.setObgatw();
		KnaAcctJointDao.insert(knaAcctJoint);

		// 返回生成的联名卡账号
		output.setJoinac(joinac);
		output.setFcflag(E_FCFLAG.CURRENT);
		output.setProdtp(E_PRODTP.DEPO); // 是否添加新的产品类型
		output.setCrcycd(BusiTools.getDefineCurrency());

	}

	@Override
	public void addJointLimit(
			IoDpKnaAcctJointLimitInfo joinli,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpJointSvc.AddJointLimit.Output output) {
		String joinac = joinli.getJoinac();
		KnaAcctJointLimit knaAcctJointLimit = KnaAcctJointLimitDao
				.selectOne_odb1(joinac, false);
		if (CommUtil.isNotNull(knaAcctJointLimit)) {
			throw DpModuleError.DpstProd.E0010("联名账户额度信息已存在！");
		}
		knaAcctJointLimit = SysUtil.getInstance(KnaAcctJointLimit.class);
		knaAcctJointLimit.setJoinac(joinac);
		knaAcctJointLimit.setSginam(joinli.getSginam());
		knaAcctJointLimit.setSgouam(joinli.getSgouam());
		knaAcctJointLimit.setSgspam(joinli.getSgspam());
		knaAcctJointLimit.setObgaon(joinli.getObgaon());
		knaAcctJointLimit.setObgatw(joinli.getObgatw());
		KnaAcctJointLimitDao.insert(knaAcctJointLimit);
	}

	public void updJointAcctBalance(
			final cn.sunline.ltts.busi.iobus.type.dp.IoDpJointType.IoDpJointAcctBalanceIn joinin,
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpJointSvc.UpdJointAcctBalance.Output output) {
		RunEnvsComm runEnvs = CommTools.getBaseRunEnvs();
		String joinac = joinin.getJoinac();
		BigDecimal tranam = joinin.getTranam();
		//更新联名账户信息表
		KnaAcctJoint knaAcctJoint = KnaAcctJointDao.selectOne_odb1(joinac, false);
		if (CommUtil.isNull(knaAcctJoint)) {
			throw DpModuleError.DpstProd.E0010("联名账户信息不存在！");
		}
		BigDecimal lastbl = knaAcctJoint.getOnlnbl();
		BigDecimal onlnbl = lastbl;
		if (joinin.getAmondr() == E_AMONDR.UP) {
			onlnbl = onlnbl.add(tranam);
		} else {
			onlnbl = onlnbl.subtract(tranam);
		}
		
		String detlsq = genDetlsq();   //TODO 序号生成
		
		knaAcctJoint.setLastbl(lastbl);
		knaAcctJoint.setUpbldt(knaAcctJoint.getLastdt());
		knaAcctJoint.setOnlnbl(onlnbl);
		knaAcctJoint.setLastdt(runEnvs.getTrxn_date());
		knaAcctJoint.setLastsq(runEnvs.getTrxn_seq());
		knaAcctJoint.setDetlsq(detlsq);
		KnaAcctJointDao.updateOne_odb1(knaAcctJoint);
		
		//联名账户交易明细登记簿
		KnbJointIobl knbJointIobl = SysUtil.getInstance(KnbJointIobl.class);
		knbJointIobl.setTrandt(runEnvs.getTrxn_date());
		knbJointIobl.setTransq(runEnvs.getTrxn_seq());
		knbJointIobl.setDetlsq(detlsq);       
		knbJointIobl.setJoinac(joinac);
		knbJointIobl.setJoinna(knaAcctJoint.getJoinna());
		knbJointIobl.setPlatno(knaAcctJoint.getPlatno());
		knbJointIobl.setTranbr(runEnvs.getTrxn_branch());
		knbJointIobl.setCustac(joinin.getCustac());
		knbJointIobl.setTranam(tranam);
		knbJointIobl.setAcctbl(onlnbl);
		knbJointIobl.setTrantp(joinin.getJotrtp());
		knbJointIobl.setTranst(E_TRANST.SUCCESS);
		knbJointIobl.setTranse("");
		knbJointIobl.setTrdesc("");
		knbJointIobl.setClerst(E_CLERST._1);
//		knbJointIobl.setObgaon();
//		knbJointIobl.setObgatw();
		KnbJointIoblDao.insert(knbJointIobl);
		
		output.setAcctbl(onlnbl);
	}

	@Override
	public void selAggregateAmountByTrandt(
			String platno,
			String trandt,
			E_JOTRTP jotrtp,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpJointSvc.SelAggregateAmountByTrandt.Output output) {
		// TODO Auto-generated method stub
		BigDecimal aggram = DpJointDao.selAggregateAmountByTrandt(BusiTools.getTranCorpno(), platno, trandt, false);
		output.setAggram(aggram);
	}
	
	public static String genJointNo() {
		RunEnvsComm runEnvs = CommTools.getBaseRunEnvs();
		return runEnvs.getBusi_org_id() + runEnvs.getTrxn_date()
				+ BusiTools.getSequence("JointNo", 10, "0");
	}
	
	public static String genDetlsq() {
		return BusiTools.getSequence("detlsq", 10, "0");
	}
}
