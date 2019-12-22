package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.io.File;
import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlLedgerSum;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glledg.Input;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glledg.Property;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.GlError;

/**
 * 生成分户账余额文件供总账
 * 
 */

public class glledgDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glledg.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glledg.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(glledgDataProcessor.class);

	private String path;// 文件路径
	private String trandt;
	private int cnt;
	private BigDecimal tranam;
	private String fileName;
		
	@Override
	public void beforeTranProcess(String taskId, Input input, Property property) {
		super.beforeTranProcess(taskId, input, property);
		trandt = CommTools.getBaseRunEnvs().getLast_date();
		KnpParameter KnpParameter = KnpParameterDao.selectOne_odb1("GL_FILE", "LEDGERBAL", "%", "%", false);
		if(CommUtil.isNull(KnpParameter)) {
			throw ApError.BusiAplt.E0055("GL_FILE","LEDGERBAL");
		}
		path = KnpParameter.getParm_value1() + trandt + "/" + KnpParameter.getParm_value2() + "/";
		fileName = KnpParameter.getParm_value2() + trandt  + FILE_TXT;
	}

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glledg.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glledg.Property property) {
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		bizlog.debug("开始生成分户余额文件，日期[%s],文件路径[%s]，文件名[%s]", trandt, path, fileName);
		cnt = InDayEndSqlsDao.selLedgerBalCount(trandt,corpno,false);
		tranam = InDayEndSqlsDao.selLedgerBalTranam(trandt,corpno,false);
		
		final LttsFileWriter file = new LttsFileWriter(path, fileName);
		file.open();
		try {
			if(cnt == 0) {
				file.write("{}");
			} else {
				file.write("{" + "\"head_total_count\":" + cnt + "," + "\"head_total_amt\":" + tranam + "}");
				Params params = new Params();
				params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
				params.add("acctdt", trandt);
				bizlog.debug("开始写入分户账余额数据");
				DaoUtil.selectList(InDayEndSqlsDao.namedsql_selLedgerBalList, params, new CursorHandler<GlLedgerSum>() {
					@Override
					public boolean handle(int index, GlLedgerSum entity) {
						String line = JsonUtil.format(entity);
						file.write(line);
						return true;
					}
				});
			}
		} finally {
			file.close();
		}
	}

	@Override
	public void afterTranProcess(String taskId, Input input, Property property) {
		super.afterTranProcess(taskId, input, property);
		if (cnt > 0) {
			String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
			InDayEndSqlsDao.insertGlCheckRecord(trandt,corpno);
		}
		genOkFile();
		// TODO 上传文件
	}

	private static final String FILE_OK = ".ok";
	private static final String FILE_TXT = ".txt";
	private void genOkFile() {
		File okFile = new File(path + fileName + FILE_OK);
		if (!okFile.exists()) {
			try {
				if (okFile.createNewFile()) {
					throw GlError.GL.E0105();
				}
			}
			catch (Exception e) {
				throw GlError.GL.E0105();
			}
		}
	}

}
