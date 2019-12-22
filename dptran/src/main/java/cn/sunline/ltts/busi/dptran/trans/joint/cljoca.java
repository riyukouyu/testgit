package cn.sunline.ltts.busi.dptran.trans.joint;

import java.util.List;

import cn.sunline.clwj.msap.core.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaJointCler;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnaJointClerDao;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnbJointIobl;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnbJointIoblDao;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnbPlatInfo;
import cn.sunline.ltts.busi.dp.tables.DpJointAccount.KnbPlatInfoDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CLERST;;

public class cljoca {

	public static void beforeMethod(
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Cljoca.Input input,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Cljoca.Property property,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Cljoca.Output output) {
		
		if (CommUtil.isNull(input.getPlatno())) {
			throw DpModuleError.DpstComm.E9027("平台编号");
		}

		KnbPlatInfo knbPlatInfo = KnbPlatInfoDao.selectOne_odb1(input.getPlatno(), false);
		if (CommUtil.isNull(knbPlatInfo)) {
			throw DpModuleError.DpstProd.E0010("平台信息不存在！");
		}
		
		property.setPllqbu(knbPlatInfo.getPllqbu());
		property.setPlowbu(knbPlatInfo.getPlowbu());
		CommTools.getBaseRunEnvs().getTrxn_branch();
		
	}

	public static void afterMethod(
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Cljoca.Input input,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Cljoca.Property property,
			final cn.sunline.ltts.busi.dptran.trans.joint.intf.Cljoca.Output output) {
		
		RunEnvsComm runEnvs = CommTools.getBaseRunEnvs();
		
		//更新消费明细状态为已清算（knb_joint_iobl
		List<KnbJointIobl> knbJointIoblList = KnbJointIoblDao.selectAll_odb2(input.getClerdt(), false);
		for (KnbJointIobl knbJointIobl : knbJointIoblList) {
			knbJointIobl.setClerdt(runEnvs.getTrxn_date());
			knbJointIobl.setClersq(runEnvs.getTrxn_seq());
			knbJointIobl.setClerst(E_CLERST._2);
			KnbJointIoblDao.updateOne_odb1(knbJointIobl);
		}
		
		//登记清算明细
		KnaJointCler knaJointCler = SysUtil.getInstance(KnaJointCler.class);
		knaJointCler.setClerdt(runEnvs.getTrxn_date());
		knaJointCler.setClersq(runEnvs.getTrxn_seq());
		knaJointCler.setPlatno(input.getPlatno());
		knaJointCler.setTranam(input.getTranam());
		knaJointCler.setDracct(property.getPllqbu());
		knaJointCler.setDracna("平台清算户");
		knaJointCler.setCracct(property.getPlowbu());
		knaJointCler.setCracna("平台自有资金户");
		knaJointCler.setTrandt(input.getClerdt());
		knaJointCler.setTranst(E_TRANST.SUCCESS);
		KnaJointClerDao.insert(knaJointCler);
	}
}
