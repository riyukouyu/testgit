package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.engine.sequence.SequenceManager;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.clwj.msap.core.parameter.MsGlobalMultiParm;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.clwj.msap.core.type.MsCoreComplexType.MsParmInfo;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsq;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqDao;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.type.InDayEndTypes.DcnClearData;
import cn.sunline.ltts.busi.in.type.InQueryTypes.BalanceOfCmda;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glcler.Input;
import cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glcler.Property;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INPTSR;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.PbEnumType;

/**
 * 非实时余额内部户跨DCN清算
 * 
 */

public class glclerDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glcler.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glcler.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(glclerDataProcessor.class);

	@Override
	public void beforeTranProcess(String taskId, Input input, Property property) {
		super.beforeTranProcess(taskId, input, property);

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
	public void process(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glcler.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Glcler.Property property) {
//		if(DcnUtil.isAdminDcn(DcnUtil.getCurrDCN())) {
//			return;
//		}
		String trandt = CommTools.getBaseRunEnvs().getLast_date();
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		
		List<DcnClearData> dcnClearDataList = InDayEndSqlsDao.selDcnClearData(trandt, corpno, false);
		IoInAccount ioInAccountServ = SysUtil.getInstance(IoInAccount.class);
		MsParmInfo  knpGlbl = MsGlobalMultiParm.getGlobalParm("system.clear","dcn");
		for(DcnClearData entity : dcnClearDataList) {
			 MsSystemSeq.getTrxnSeq();
			IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
		
			iaAcdrInfo.setAcbrch(entity.getAcctbr());
			iaAcdrInfo.setBusino(entity.getBusino());
			iaAcdrInfo.setAcctno(entity.getAcctno());
			iaAcdrInfo.setAcctna(entity.getAcctna());
			iaAcdrInfo.setCrcycd(entity.getCrcycd());
			iaAcdrInfo.setToacct(knpGlbl.getParm_value1());
			iaAcdrInfo.setToacna(knpGlbl.getParm_value2());
			iaAcdrInfo.setTranam(entity.getTranam());
			iaAcdrInfo.setInptsr(E_INPTSR.GL00);
			CommTools.getBaseRunEnvs().setTrxn_date(entity.getTrandt());
			E_AMNTCD amntcd = entity.getAmntcd();
			if(amntcd == E_AMNTCD.DR) {
				ioInAccountServ.ioInAccr(iaAcdrInfo);//反方向记
			} else if(amntcd == E_AMNTCD.CR) {
				ioInAccountServ.ioInAcdr(iaAcdrInfo);
			}
			String transq = CommTools.getBaseRunEnvs().getTrxn_seq();
			dealClearAcsq(entity,transq,knpGlbl.getParm_value1());
		}
		genDcnClearFile(dcnClearDataList);
	}

	private void genDcnClearFile(List<DcnClearData> dcnClearDataList) {
		String trandt = CommTools.getBaseRunEnvs().getLast_date();
		//String dcn = DcnUtil.getCurrDCN();
		String txt = ".txt"; 
		String ok = ".ok";
		KnpParameter para = KnpParameterDao.selectOne_odb1("system.clear.file", "path","%", "%", true);
		String fileNameTxt = para.getParm_value2()+ txt;
		String fileNameOk = para.getParm_value2() + ok;
		String filePath = para.getParm_value1() + trandt + "/";
		
		LttsFileWriter file = new LttsFileWriter(filePath, fileNameTxt);
		file.open();
		try {
			for(DcnClearData entity : dcnClearDataList) {
				String line = JsonUtil.format(entity);
				file.write(line);
			}
			genOkFile(filePath + fileNameOk);
		} finally {
			file.close();
		}
		
	}

	private void genOkFile(String fileName) {
		File okFile = new File(fileName);
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

	private void dealClearAcsq(DcnClearData entity, String transq, String dtitcd) {
		List<BalanceOfCmda> cmdas = null;
		try {
			cmdas = InacSqlsDao
					.CheckBalanceOfGlvcByTransq(entity.getTrandt(), transq, true);
		} catch (Exception e) {
			throw InError.comm.E0003("交易平衡性数据查询失败，其他错误");
		}
		if(CommUtil.isNull(cmdas)) {
			return;
		}
		KnsAcsq tblAcct_new = SysUtil.getInstance(KnsAcsq.class);
		tblAcct_new.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblAcct_new.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
		//tblAcct_new.setSortno(Long.parseLong(SequenceManager.nextval("KnsAcsq")));
		tblAcct_new.setSortno(Long.parseLong(CoreUtil.nextValue("KnsAcsq")));
		tblAcct_new.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());
		tblAcct_new.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		tblAcct_new.setAtsqtp(PbEnumType.E_ATSQTP.ACCOUNT);
		tblAcct_new.setAtowtp(PbEnumType.E_ATOWTP.IN);
		tblAcct_new.setBltype(BaseEnumType.E_BLTYPE.BALANCE);

		tblAcct_new.setDtitcd(dtitcd);
		tblAcct_new.setAcctbr(SysUtil.getRemoteInstance(IoSrvPbBranch.class)
				.getAcctBranch(CommTools.getBaseRunEnvs().getTrxn_branch())
				.getBrchno());

		BalanceOfCmda cmda = cmdas.get(0);
		tblAcct_new.setCrcycd(cmda.getCrcycd());
		BigDecimal tranam = cmda.getTranam();
		if (CommUtil.compare(tranam, BigDecimal.ZERO) > 0) {
			// 贷方多，记借方
			tblAcct_new.setTranam(tranam);
			tblAcct_new.setAmntcd(BaseEnumType.E_AMNTCD.DR);
		} else {
			// 借方多，记贷方
			tblAcct_new.setTranam(tranam.negate());
			tblAcct_new.setAmntcd(BaseEnumType.E_AMNTCD.CR);
		}

		tblAcct_new.setDasyst(BaseEnumType.E_DASYST.WAIT);
		tblAcct_new.setIoflag(E_IOFLAG.IN);
		tblAcct_new.setProdcd(ApUtil.DEFAULT_PROD_CODE);
		//tblAcct_new.setCrdcnfg(CommTools.getBaseRunEnvs().getXdcnfg());
		
		KnsAcsqDao.insert(tblAcct_new);
	}
	
	@Override
	public void afterTranProcess(String taskId, Input input, Property property) {
		super.afterTranProcess(taskId, input, property);

	}

}
