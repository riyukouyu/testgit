package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublicServ;
import cn.sunline.ltts.busi.dp.domain.DpSaveEntity;
import cn.sunline.ltts.busi.dp.froz.DpFrozTools;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDpsg;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDpsgDao;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.FxautrTranData;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaStaPublic;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRDPTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;

/**
 * 传统定期到期处理 传统定期到期把本金和利息转入电子账户
 * 
 */

public class fxactrDataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Fxactr.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Fxactr.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.FxautrTranData> {

	private static final BizLog bizlog = BizLogUtil
			.getBizLog(fxautrDataProcessor.class);
	private String trandt = DateTools2.getDateInfo().getSystdt();// 交易日期

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param job
	 *            批次作业ID
	 * @param index
	 *            批次作业第几笔数据(从1开始)
	 * @param dataItem
	 *            批次数据项
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(
			String jobId,
			int index,
			cn.sunline.ltts.busi.dp.type.DpDayEndType.FxautrTranData dataItem,
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Fxactr.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Fxactr.Property property) {

		bizlog.debug("**********账号[" + dataItem.getAcctno()
				+ "]传统定期到期处理开始*******************");


		if (!DpFrozTools.isFroz(E_FROZOW.AUACCT, dataItem.getCustac(), E_YES___.YES,
				E_YES___.NO, E_YES___.YES)) {

			// 获取对应活期账号
			//String huoqino = CaTools.getAcctno(dataItem.getCustac(),dataItem.getCrcycd());
			String huoqino = SysUtil.getInstance(IoCaStaPublic.class).CaTools_getAcctno(dataItem.getCustac(),dataItem.getCrcycd());
			// 签约表
			KnaDpsg tabKnaDpsg = KnaDpsgDao.selectOne_odb1(
					dataItem.getAcctno(), E_SIGNTP.TRDP, false);

			// 如果转存类型为转入活期账户才执行
			if (tabKnaDpsg.getTrdptp() == E_TRDPTP.TRDPCR) {

				KubInrt inrt = KubInrtDao.selectOne_odb1(
						dataItem.getAcctno(), false);
				if (CommUtil.isNull(inrt))
					throw ApError.BusiAplt.E0000("账户利率信息为空");

				// 每一笔交易重新生成一笔流水，用来进行平衡性检查
				 MsSystemSeq.getTrxnSeq();

				// 支取服务
				DpSaveEntity dpSave = new DpSaveEntity();
				dpSave.setAcctno(dataItem.getAcctno());// 负债账号
				dpSave.setCrcycd(dataItem.getCrcycd());// 币种
				dpSave.setTranam(dataItem.getOnlnbl());// 交易金额
				dpSave.setCardno(dataItem.getCardno());// 卡号
				dpSave.setCustac(dataItem.getCustac());// 电子账户
				dpSave.setAcseno(dataItem.getAcseno());// 电子账户子号
				dpSave.setToacct(huoqino);// 转账账号
				dpSave.setOpacna(dataItem.getAcctna());// 转账户名
				DpPublicServ.drawAcctDp(dpSave);// 返回支取金额利息

				bizlog.debug("**********账号[" + dataItem.getAcctno()
						+ "] 支取的利息为：" + dpSave.getInstam()
						+ "*******************");

				// 存入服务
				DpSaveEntity entity = new DpSaveEntity();
				entity.setAcctno(huoqino);
				entity.setCrcycd(dataItem.getCrcycd());
				entity.setTranam(dataItem.getOnlnbl().add(dpSave.getInstam()));// 应该加上本金产生的利息，支取服务还没进行改造，会将利息返回。
				entity.setCardno(dataItem.getCardno());
				entity.setCustac(dataItem.getCustac());
				entity.setAcseno(dataItem.getAcseno());
				entity.setToacct(dataItem.getAcctno());
				entity.setOpacna(dataItem.getAcctna());
				DpPublicServ.postAcctDp(entity);

				// 检查平衡
				String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
				String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
				IoCheckBalance ioCheckBalanceSrv = SysUtil
						.getInstance(IoCheckBalance.class);
				ioCheckBalanceSrv.checkBalance(trandt, transq,null);

				bizlog.debug("**********账号[" + dataItem.getAcctno()
						+ "]传统定期到期处理结束*******************");
			}
		}
	}

	/**
	 * 获取数据遍历器。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.FxautrTranData> getBatchDataWalker(
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Fxactr.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Fxactr.Property property) {

		Params params = new Params();
		params.add("trandt", trandt);
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		return new CursorBatchDataWalker<FxautrTranData>(
				DpDayEndDao.namedsql_selFxactrDatas, params);

	}

}
