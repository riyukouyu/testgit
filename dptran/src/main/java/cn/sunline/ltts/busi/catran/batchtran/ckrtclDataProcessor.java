package cn.sunline.ltts.busi.catran.batchtran;

import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.namedsql.CaBatchTransDao;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbProm;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbPromDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STATUS;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_IDCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MPCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PROMST;
import cn.sunline.ltts.busi.sys.type.WaEnumType.E_RELTST;
import cn.sunline.ltts.busi.wa.type.WaAcctType.IoWaKnaRelt;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 身份核查未返回失败处理
 * 
 */

public class ckrtclDataProcessor
		extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.catran.batchtran.intf.Ckrtcl.Input, cn.sunline.ltts.busi.catran.batchtran.intf.Ckrtcl.Property> {

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	private static final BizLog bizlog = BizLogUtil
			.getBizLog(miscelDataProcessor.class);

	@Override
	public void process(
			cn.sunline.ltts.busi.catran.batchtran.intf.Ckrtcl.Input input,
			cn.sunline.ltts.busi.catran.batchtran.intf.Ckrtcl.Property property) {
		
		// 打印当前交易日期
		bizlog.debug("交易开始当前交易日期：[" + CommTools.getBaseRunEnvs().getTrxn_date() + "]");
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
		String timetm = DateTools2.getCurrentTimestamp();// 交易日期
		
		// 获取身份核查未返回超时处理参数
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("Ckrtcl", "%", "%", "%", true);
		
		// 电子账户身份核查结果5天未返回核查记录作失败处理
		
		// 1.电子账户开户，启用身份核查结果返回超时处理
		// 查询出电子账户表身份核查结果为待核查的记录 modifyed by xieqq add corpno select. 20170630
		List<KnaCust> tblKnaCust = CaBatchTransDao.selKnaCustIdckrtInfos(corpno,E_IDCKRT.CHECKING, E_MPCKRT.CHECKING, false);
		
		// 如果查询结果不为空进行处理操作
		if (CommUtil.isNotNull(tblKnaCust) && tblKnaCust.size() > 0) {
			
			// 取出每一条数据进行处理
			for (KnaCust knacust:tblKnaCust) {
				
				// 查询电子账户状态
				E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(knacust.getCustac());
			
				// 超时处理日期取开户日期+5天
				String sTrandt = DateTools2.calDateByTerm(knacust.getOpendt(), tblKnpParameter.getParm_value1());
				
				if (cuacst == E_CUACST.PREOPEN) {
					// 与当前系统日期进行比较
					if (CommUtil.compare(sTrandt, trandt) <= 0) {

						knacust.setIdckrt(E_IDCKRT.FAILD);// 身份核查结果
						knacust.setAcctst(E_ACCTST.CLOSE);// 账户状态
						
						// 更新电子账户表
						KnaCustDao.updateOne_odb1(knacust);
						
						//更新电子账户账户别名，绑定手机号 modifyed by xieqq add corpno update. 20170630
						CaDao.updKnaAcalStatusByCustac(E_ACALST.INVALID, knacust.getCustac(),corpno,timetm);
						
						//更新客户信息关联状态为关闭
						int count = CaDao.selKnareltiInfos(knacust.getCustno(), false);
						if (count == 0) {
							CaDao.updCifCustAccsStatusByCustac(E_STATUS.CLOSE, knacust.getCustno());
						}

						// 登记电子账户状态
						IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
						cplDimeInfo.setCustac(knacust.getCustac());
						SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
					}
				} else if(cuacst == E_CUACST.NOENABLE){
					// 与当前系统日期进行比较
					if (CommUtil.compare(sTrandt, trandt) <= 0) {
						
						knacust.setIdckrt(E_IDCKRT.FAILD);// 身份核查结果
						KnaCustDao.updateOne_odb1(knacust);
					}

				} else {
					bizlog.error("其余状态不用做核查超时处理！", sTrandt);
				}
			}
		}
		
		// 2.电子账户升级身份核查结果返回超时处理modifyed by xieqq add corpno as select.
		List<KnbProm> tblKnbProm = CaBatchTransDao.selKnbPromIdckrtInfos(E_IDCKRT.CHECKING, E_MPCKRT.CHECKING, E_PROMST.ACCEPTED,corpno,false);
		
		// 如果查询结果不为空进行处理操作
		if (CommUtil.isNotNull(tblKnbProm) && tblKnbProm.size() > 0) {
			
			// 取出每一条数据进行处理
			for (KnbProm knbprom:tblKnbProm) {
				// 超时处理日期取申请日期+5天
				String sTrandt = DateTools2.calDateByTerm(knbprom.getPromdt(), tblKnpParameter.getParm_value1());
				// 与当前系统日期进行比较
				if (CommUtil.compare(sTrandt, trandt) <= 0) {
					
					knbprom.setIdckrt(E_IDCKRT.FAILD);// 身份核查结果
					knbprom.setPromst(E_PROMST.FAILURE);// 升级状态
					
					// 更新升级登记簿表
					KnbPromDao.updateOne_odb4(knbprom);
				}
			}
		}
		
		// 3.亲情钱包开立身份核查结果返回超时处理
		List<IoWaKnaRelt> cplKnaRelt = CaBatchTransDao.selKnaReltIdckrtInfos(E_IDCKRT.CHECKING, false);
		
		// 如果查询结果不为空进行处理操作
		if (CommUtil.isNotNull(cplKnaRelt) && cplKnaRelt.size() > 0) {
			
			// 取出每一条数据进行处理
			for (IoWaKnaRelt tblKnaRelt:cplKnaRelt) {
				// 超时处理日期取开户日期+5天
				String sTrandt = DateTools2.calDateByTerm(tblKnaRelt.getCreadt(), tblKnpParameter.getParm_value1());
				// 与当前系统日期进行比较
				if (CommUtil.compare(sTrandt, trandt) <= 0) {
					
					// 更新亲情账户关系关联表
					CaDao.updKnaReltInfo(E_IDCKRT.FAILD, E_RELTST.CLOSE, tblKnaRelt.getReltid(),timetm);
					
					// 更新客户关联关系
//					SysUtil.getInstance(IoWaSrvWalletAccountType.class).ioWaCloseCust(tblKnaRelt.getElacct());
					
				}
			}
		}
	}

}
