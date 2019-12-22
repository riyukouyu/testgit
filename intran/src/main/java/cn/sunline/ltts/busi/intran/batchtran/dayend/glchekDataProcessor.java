package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.io.File;
import java.util.List;

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
import cn.sunline.ltts.busi.in.tables.In.GlCheckRecord;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glchek.Input;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glchek.Property;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.GlError;

/**
 * 生成对账文件供总账
 * 
 */

public class glchekDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glchek.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glchek.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(glchekDataProcessor.class);

	private String path;// 文件路径
	private String trandt;
	private String fileName;

	@Override
	public void beforeTranProcess(String taskId, Input input, Property property) {
		super.beforeTranProcess(taskId, input, property);
		trandt = CommTools.getBaseRunEnvs().getLast_date();
		KnpParameter KnpParameter = KnpParameterDao.selectOne_odb1("GL_FILE", "CHECKRECORD", "%", "%", false);
		if(CommUtil.isNull(KnpParameter)) {
			throw ApError.BusiAplt.E0055("GL_FILE","CHECKRECORD");
		}
		path = KnpParameter.getParm_value1() + trandt + "/" + KnpParameter.getParm_value2() + "/";
		fileName = KnpParameter.getParm_value2() + trandt  + ".txt";
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
	public void process(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glchek.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glchek.Property property) {

		bizlog.debug("开始汇总对账文件生产，日期[%s],文件路径[%s]，文件名[%s]", trandt, path, fileName);

		final LttsFileWriter file = new LttsFileWriter(path, fileName);
		file.open();
		try {
			Params params = new Params();
			params.add("trandt", trandt);
			params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id()); 
			bizlog.debug("开始写总账对账数据");
			List<GlCheckRecord> selGlCheckRecordList = InDayEndSqlsDao.selGlCheckRecordList(trandt, CommTools.getBaseRunEnvs().getBusi_org_id(), false);
			if(CommUtil.isNull(selGlCheckRecordList) || selGlCheckRecordList.size() == 0) {
				file.write("{}");
			} else {
				DaoUtil.selectList(InDayEndSqlsDao.namedsql_selGlCheckRecordList, params, new CursorHandler<GlCheckRecord>() {
					@Override
					public boolean handle(int index, GlCheckRecord entity) {
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
		genOkFile();
	}

	private static final String FILE_OK = ".ok";
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
