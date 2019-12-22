package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.io.File;
import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
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
import cn.sunline.ltts.busi.in.tables.In.GlCheckRecordDao;
import cn.sunline.ltts.busi.in.type.InDayEndTypes.GlAccrueData;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glaccr.Input;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glaccr.Property;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCRUETYPE;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;

/**
 * 生成计提取文件供总账
 * 
 */

public class glaccrDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glaccr.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glaccr.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(glaccrDataProcessor.class);

	private String path;// 文件路径
	private int cntDeposit, cntLoan, cntVat, cntQua;
	private BigDecimal tranamDeposit, tranamLoan, tranamVat, tranamQua;
	private String trandt;
	private String fileName;
	@Override
	public void beforeTranProcess(String taskId, Input input, Property property) {
		super.beforeTranProcess(taskId, input, property);
		trandt = CommTools.getBaseRunEnvs().getLast_date();
		KnpParameter KnpParameter = KnpParameterDao.selectOne_odb1("GL_FILE", "ACCURE", "%", "%", false);
		if(CommUtil.isNull(KnpParameter)) {
			throw ApError.BusiAplt.E0055("GL_FILE","ACCURE");
		}
		path = KnpParameter.getParm_value1() + trandt + "/" + KnpParameter.getParm_value2() + "/";
		fileName = KnpParameter.getParm_value2() + trandt +  FILE_TXT;
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
	public void process(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glaccr.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glaccr.Property property) {
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		bizlog.debug("开始生产计提文件，日期[%s],文件路径[%s]，文件名[%s]", trandt, path, fileName);
		cntDeposit = InDayEndSqlsDao.selDepositAccrueListCount(trandt, corpno, false);
		tranamDeposit = InDayEndSqlsDao.selDepositAccrueListTranam(trandt, corpno, false);
		if (CommUtil.isNull(tranamDeposit)) {
			tranamDeposit = BigDecimal.ZERO;
		}
		cntLoan = InDayEndSqlsDao.selLoanAccrueListCount(trandt, corpno, false);
		tranamLoan = InDayEndSqlsDao.selLoanAccrueListTranam(trandt, corpno, false);
		if (CommUtil.isNull(tranamLoan)) {
			tranamLoan = BigDecimal.ZERO;
		}
		
		cntVat = InDayEndSqlsDao.selTxBusiAddeListCount(trandt, corpno, false);
		tranamVat = InDayEndSqlsDao.selTxBusiAddeListTranam(trandt, corpno, false);
		if (CommUtil.isNull(tranamVat)) {
			tranamVat = BigDecimal.ZERO;
		}
		
        cntQua = InDayEndSqlsDao.selLoanQuaClaListCount(trandt, corpno, false);
        tranamQua = InDayEndSqlsDao.selLoanQuaClaListTranam(trandt, corpno, false);
        if (CommUtil.isNull(tranamQua)) {
            tranamQua = BigDecimal.ZERO;
        }
        
		int cnt = cntDeposit + cntLoan + cntVat + cntQua;
		BigDecimal tranam = tranamDeposit.add(tranamLoan).add(tranamVat).add(tranamQua);
		
		final LttsFileWriter file = new LttsFileWriter(path, fileName);
		file.open();
		try {
			if(cnt == 0) {
				file.write("{}");
			} else {
				file.write("{" + "\"head_total_count\":" + cnt + "," + "\"head_total_amt\":" + tranam + "}");
				Params params = new Params();
				params.add("cabrdt", trandt);
				params.add("corpno", corpno);
				bizlog.debug("开始写入贷款计提数据");
				DaoUtil.selectList(InDayEndSqlsDao.namedsql_selLoanAccrueList, params, new CursorHandler<GlAccrueData>() {
					@Override
					public boolean handle(int index, GlAccrueData entity) {
						//entity.setAccrue_type(E_ACCRUETYPE.LOAN_INTEREST_RECEIVABLE);
						//entity.setBal_attributes(E_BLTYPE.CAIN);
						String line = JsonUtil.format(entity);
						file.write(line);
						return true;
					}
				});
				bizlog.debug("开始写入存款款计提数据");
				DaoUtil.selectList(InDayEndSqlsDao.namedsql_selDepositAccrueList, params, new CursorHandler<GlAccrueData>() {
					@Override
					public boolean handle(int index, GlAccrueData entity) {
						entity.setAccrue_type(E_ACCRUETYPE.DEPOSIT_INTEREST_PAYBLE);
						entity.setBal_attributes(E_BLTYPE.CAIN);
						String line = JsonUtil.format(entity);
						file.write(line);
						return true;
					}
				});
				bizlog.debug("开始写入营改增登记数据");
				DaoUtil.selectList(InDayEndSqlsDao.namedsql_selTxBusiAddeList, params, new CursorHandler<GlAccrueData>() {
					@Override
					public boolean handle(int index, GlAccrueData entity) {
						entity.setAccrue_type(E_ACCRUETYPE.TAX_SEPARATION);
						entity.setBal_attributes(E_BLTYPE.VAT);
						String line = JsonUtil.format(entity);
						file.write(line);
						return true;
					}
				});
                bizlog.debug("开始写入贷款损失计提数据");
                DaoUtil.selectList(InDayEndSqlsDao.namedsql_selLoanQuaClaList, params, new CursorHandler<GlAccrueData>() {
                    @Override
                    public boolean handle(int index, GlAccrueData entity) {
                        entity.setAccrue_type(E_ACCRUETYPE.LOAN_LOST_PROVISION);
                        entity.setBal_attributes(E_BLTYPE.QUA);
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
		GlCheckRecord entity = SysUtil.getInstance(GlCheckRecord.class);
		entity.setTrxn_date(trandt);
		entity.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		entity.setFile_type("E");// C代表会计流水文件 E代表计提文件 G代表核心分户账文件
		if (cntLoan > 0) {
			entity.setRecord_number(cntLoan);
			entity.setTotal_amt(tranamLoan);
			entity.setAccounting_subject(E_ATOWTP.LN);
			GlCheckRecordDao.insert(entity);
		}
		if (cntDeposit > 0) {
			entity.setRecord_number(cntDeposit);
			entity.setTotal_amt(tranamDeposit);
			entity.setAccounting_subject(E_ATOWTP.DP);
			GlCheckRecordDao.insert(entity);
		}
		if (cntVat > 0) {
			entity.setRecord_number(cntVat);
			entity.setTotal_amt(tranamVat);
			entity.setAccounting_subject(E_ATOWTP.TX);
			GlCheckRecordDao.insert(entity);
		}
        if (cntQua > 0) {
            entity.setRecord_number(cntQua);
            entity.setTotal_amt(tranamQua);
            entity.setAccounting_subject(E_ATOWTP.LC);
            GlCheckRecordDao.insert(entity);
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
