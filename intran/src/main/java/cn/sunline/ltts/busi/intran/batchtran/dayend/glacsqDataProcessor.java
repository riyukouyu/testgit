package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.io.File;
import java.math.BigDecimal;
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
import cn.sunline.ltts.busi.in.type.InDayEndTypes.GlKnsqData;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glacsq.Input;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glacsq.Property;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.pb.namedsql.BrchDao;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.GlError;

/**
 * 按机构生成会计流水文件供总账
 * 
 */

public class glacsqDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glacsq.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glacsq.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(glacsqDataProcessor.class);

	private String path;
	private String trandt;
	private String fileNamePrefix;
	
	@Override
	public void beforeTranProcess(String taskId, Input input, Property property) {
		super.beforeTranProcess(taskId, input, property);
		trandt = CommTools.getBaseRunEnvs().getLast_date();
		KnpParameter KnpParameter = KnpParameterDao.selectOne_odb1("GL_FILE", "ACCOUNTING", "%", "%", false);
		if(CommUtil.isNull(KnpParameter)) {
			throw ApError.BusiAplt.E0055("GL_FILE","ACCOUNTING");
		}
		path = KnpParameter.getParm_value1() + trandt + "/" + KnpParameter.getParm_value2() + "/";
		fileNamePrefix = KnpParameter.getParm_value2();
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
	public void process(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glacsq.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glacsq.Property property) {
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		List<IoBrchInfo> brchList = BrchDao.selRealBrchList(corpno, false);
		String brchno = null;
		int index = 0;
		String FILE_TXT = ".txt";
		for (IoBrchInfo brch : brchList) {
			brchno = brch.getBrchno();
			String fileName = fileNamePrefix + trandt + brchno + (++index) +  FILE_TXT;

			bizlog.debug("开始生成会计流水文件，日期[%s],文件路径[%s]，文件名[%s]", trandt, path, fileName);
			int cnt = InDayEndSqlsDao.selKnsAcsqListCount(trandt, brchno, false);
			BigDecimal tranam = InDayEndSqlsDao.selKnsAcsqListTranam(trandt, brchno, false);
			
			final LttsFileWriter file = new LttsFileWriter(path, fileName);
			file.open();
			try {
				if(0 == cnt) {
					file.write("{}");
				}else{
					file.write("{" + "\"head_total_count\":" + cnt + "," + "\"head_total_amt\":" + tranam + "}");
					Params params = new Params();
					params.add("trandt", trandt);
					params.add("brchno", brchno);
					DaoUtil.selectList(InDayEndSqlsDao.namedsql_selKnsAcsqList, params, new CursorHandler<GlKnsqData>() {
						@Override
						public boolean handle(int index, GlKnsqData entity) {
							String line = JsonUtil.format(entity);
							file.write(line);
							return true;
						}
					});
				}
			} finally {
				file.close();
			}
			genOkFile(fileName);
		}

	}

	@Override
	public void afterTranProcess(String taskId, Input input, Property property) {
		super.afterTranProcess(taskId, input, property);
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		List<GlCheckRecord> list = InDayEndSqlsDao.selGlAcsqSum(trandt, corpno, false);
		if (CommUtil.isNotNull(list)) {
			DaoUtil.insertBatch(GlCheckRecord.class, list);
		}
	}

	private static final String FILE_OK = ".ok";
	private void genOkFile(String fileName) {
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
