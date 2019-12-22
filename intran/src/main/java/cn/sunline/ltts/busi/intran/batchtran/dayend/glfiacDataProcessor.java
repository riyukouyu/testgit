package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.KnbCbtlBatch;
import cn.sunline.ltts.busi.in.tables.In.KnbCbtlBatchDao;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;

/**
 * 日终总账核算供数
 * 
 */

public class glfiacDataProcessor
		extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.intf.Glfiac.Input, cn.sunline.ltts.busi.intran.batchtran.intf.Glfiac.Property> {
	private static final BizLog bizlog = BizLogUtil
			.getBizLog(glfiacDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(
			cn.sunline.ltts.busi.intran.batchtran.intf.Glfiac.Input input,
			cn.sunline.ltts.busi.intran.batchtran.intf.Glfiac.Property property) {
		bizlog.debug("---------开始进行计提利息汇总-------");
		// 日期为日切前日期
		final String trandt = DateTools2.getDateInfo().getLastdt();

		// 借贷合计金额比较
		BigDecimal amtsumd =InDayEndSqlsDao.selGlFaAmt(trandt, E_AMNTCD.DR,false);// 借方金额合计
		
		if(CommUtil.isNull(amtsumd)){
			amtsumd = BigDecimal.ZERO;
		}
		
		BigDecimal amtsumc = InDayEndSqlsDao.selGlFaAmt(trandt, E_AMNTCD.CR,false); // 贷方金额合计
		
		if(CommUtil.isNull(amtsumc)){
			amtsumc = BigDecimal.ZERO;
		}

		bizlog.debug("---------借方金额汇总-------" + amtsumd);
		bizlog.debug("---------贷方金额汇总-------" + amtsumc);
		if (CommUtil.compare(amtsumd, amtsumc) != 0) {

			throw ApError.BusiAplt.E0000("借方汇总金额：" + amtsumd + " 与贷方汇总金额："
					+ amtsumc + " 不相等！");
		}
		
		BigDecimal totlam = InDayEndSqlsDao.selSumGlFaAmt(trandt, false); // 查询总发生额
		
		if(CommUtil.isNull(totlam)){
			totlam = BigDecimal.ZERO;
		}
		
		//BigDecimal totlam =  amtsumd.add(amtsumc);//交易总金额
		// 生成数据批次号
		final String taskid = trandt + "01";
		final String sysdat = DateTools2.getSystemDate();// 自然日期
		// 将日终总账核算供数数据写入写文件临时表
		KnpParameter tbl_KnpParameter = SysUtil.getInstance(KnpParameter.class);

		tbl_KnpParameter = KnpParameterDao.selectOne_odb1("GlParm.Detail", "%", "%", "%",
				false);
		String sql = "";
		Params params = new Params();

		params.add("trandt", trandt);
		params.add("taskid", taskid);
		if (CommUtil.isNotNull(tbl_KnpParameter)
				&& CommUtil.equals("1", tbl_KnpParameter.getParm_value1())) {

			sql = InDayEndSqlsDao.namedsql_selGldayEndFaDetail;
		} else {

			sql = InDayEndSqlsDao.namedsql_selGldayEndFa;
		}

		DaoUtil.selectList(sql, params, new CursorHandler<KnbCbtlBatch>() {

			public boolean handle(int index, KnbCbtlBatch entity) {

				KnbCbtlBatch knblttl = SysUtil.getInstance(KnbCbtlBatch.class);
				knblttl = entity;
				knblttl.setFiledate(sysdat);
				knblttl.setTrandt(trandt);
				knblttl.setTaskid(taskid);
				if (CommUtil.compare(knblttl.getTranam(), BigDecimal.ZERO) != 0) {

					KnbCbtlBatchDao.insert(knblttl);
				} else {

					bizlog.debug("---------记录数" + index + "产品"
							+ entity.getProdcd() + "交易信息"
							+ entity.getTrantype() + "---金额为零,过滤----");
				}

				return true;

			}
		});

		//取汇总核算表中的交易总金额
		BigDecimal cbtlam  = InDayEndSqlsDao.selKnbCbtlBatchAm(trandt, false);
		if(CommUtil.isNull(cbtlam)){
			cbtlam=BigDecimal.ZERO;
		}
		if(CommUtil.compare(cbtlam, totlam)!=0){
			
			throw ApError.BusiAplt.E0000("账务明细总金额：" + totlam + " 与核算明细表总金额："
					+ cbtlam + " 不相等！");			
		}
		
	}

}
