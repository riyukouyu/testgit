package cn.sunline.ltts.busi.dptran.batchtran;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.base.util.security.encrypt.MD5EncryptUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit;
import cn.sunline.ltts.busi.aplt.para.ApBatchFileParams;
import cn.sunline.ltts.busi.aplt.tools.FileBatchTools;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.dp.namedsql.ProSalSqlDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcct;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAddt;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrch;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfir;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawPlan;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntr;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbSync;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbSyncDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbSyncTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbSyncTempDao;
import cn.sunline.ltts.busi.dp.type.DpProdProsalType.KupDppbDrawProsInfo;
import cn.sunline.ltts.busi.dp.type.DpProdProsalType.KupDppbMatuProsInfo;
import cn.sunline.ltts.busi.dp.type.DpProdProsalType.KupDppbPostProsInfo;
import cn.sunline.ltts.busi.dp.type.DpProdProsalType.KupDppbProsInfo;
import cn.sunline.ltts.busi.dp.type.DpProdProsalType.KupDppbPtplProsInfo;
import cn.sunline.ltts.busi.dp.type.DpProdProsalType.KupDppdCustProsInfo;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatch;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKupDppbTermFile;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SYNCST;

//import com.alibaba.fastjson.JSON;

/**
 * 装配产品同步销售工厂
 * 
 */

public class prosalDataProcessor
		extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.intf.Prosal.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Prosal.Property> {

	private static final BizLog bizlog = BizLogUtil
			.getBizLog(prosalDataProcessor.class);
	private static String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
	// 存放目录更改
	private static String filePath = ""; // 文件路径
	private static String idx = ""; // 文件名后缀

	private static E_FILETP filetp = E_FILETP.DP021400;
	private static String fengefu = "|@|";
	private static String filesq = "NASPSS" + trandt;// 统一生成一个流水号

	private static String md5 = ""; // MD5值
	private static String path2 = "";// 相对路径，与数据子系统协调后确定
	DefaultOptions<BatchFileSubmit> ls = new DefaultOptions<BatchFileSubmit>();

	// BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
	// Map<String,String> map = new HashMap<String,String>();

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
			cn.sunline.ltts.busi.dptran.batchtran.intf.Prosal.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.intf.Prosal.Property property) {
		//判断是否有产品需要同步，没有就直接结束

		final int cnt1 = ProSalSqlDao.selKupDppbCnt(CommTools.getBaseRunEnvs().getBusi_org_id(),false);
		if (0 == cnt1) {
			return;
		}
		dealparams();
		writeKupDppbDfir();
		writeKuppDppbFile();
		writeKupDppbCust();
		// writelKupDppbBrch();
		writeKupDppbTerm();
		writeKupDppbPost();
		writeKupDppbDraw();
		writeKupDppbMatu();
		writeKupDppbPostPlan();
		writeKupDppbDrawPlan();
		writeKupDppbIntr();
		writeKupDppbAcct();
		writeKupDppbActp();
		writeKupDppbAddt();

		// 对已同步成功的同步状态改为已成功
		if (ls != null) {
			java.util.List<KupDppbSyncTemp> temp2 = ProSalSqlDao
					.selAllKupDppbSyncTemp(false);
			for (KupDppbSyncTemp temp1 : temp2) {
//				KupDppbSync sync = SysUtil.getInstance(KupDppbSync.class);
				KupDppbSync sync = KupDppbSyncDao.selectOne_odb1(temp1.getProdcd(), true);
				sync.setProdcd(temp1.getProdcd());
				sync.setCaredt(temp1.getCaredt());
				sync.setSyncst(temp1.getSyncst());
				sync.setSeqnum(idx);
				sync.setTrandt(trandt);
				KupDppbSyncDao.update_odb(sync);
			}
			ProSalSqlDao.delKupDppbSyncTemp();
		}

		/*
		 * M：本地测试，暂时注释。
		E_SYSCCD target = E_SYSCCD.PSS;
		String status = E_FILEST.SUCC.getValue();
		String descri = "产品工厂产品同步";
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBackNotice(
				status, descri, target, filetp, ls);
		 */

	}

	private void dealparams() {
		
		String str = "0"; // 用于补位

		KnpParameter ts = KnpParameterDao.selectOne_odb1("PROS", "dpfile", "01", "%",
				true);

		//生成文件路径
		filePath = ts.getParm_value1() + trandt
				+ File.separator;

		//外调返回路径
		KnpParameter para = KnpParameterDao.selectOne_odb1("Batch.File", "%", "%", "%", true);
		
		path2=FileBatchTools.subPathByString(para.getParm_value1(), filePath);
				
		bizlog.debug("方法值为：" + filePath + "=============");

		// 若参数表中val4为空或者小于当前交易日期，将idx值初始化为1，并+1后更新参数表，将当前日期替换val4
		if (CommUtil.isNull(ts.getParm_value4())
				|| CommUtil.compare(trandt, ts.getParm_value4()) > 0) {

			idx = "1";

			Integer seqno = (ConvertUtil.toInteger(idx) + 1);
			ts.setParm_value3(seqno.toString());
			ts.setParm_value4(trandt);
			KnpParameterDao.updateOne_odb1(ts);
		}

		// 若参数表中的val4等于当前交易日期，则取val3为idx值，然后加1更新参数表
		if (CommUtil.equals(trandt, ts.getParm_value4())) {

			if (CommUtil.isNull(ts.getParm_value3()) || CommUtil.equals("0", ts.getParm_value3())) {
				idx = "1";
			} else {
				idx = ts.getParm_value3();
			}
			Integer seqno = (ConvertUtil.toInteger(idx) + 1);
			ts.setParm_value3(seqno.toString());
			KnpParameterDao.updateOne_odb1(ts);
		}

		// 若后缀小于3为，则补到3位
		idx = CommUtil.lpad(idx, 3, str);

	}

	private void writeKuppDppbFile() {

		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		Map<String, String> map = new HashMap<String, String>();

		// 获取文件名
		String filename1 = "PSS_DP_KUPDPPB_" + trandt + "_" + idx + ".txt";
		bizlog.debug("文件名称 filename:[" + filename1 + "]");

		// 获取全路径
		String path1 = filePath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");

		// 获取是否产生文件标志
		String isCreateFlg1 = "Y";
		bizlog.debug("文件产生标志 :[" + isCreateFlg1 + "]");

		if (CommUtil.equals(isCreateFlg1, "Y")) {
			final LttsFileWriter file = new LttsFileWriter(path1, filename1,
					"UTF-8");
			// List<cif_cust> entities = null;
			Params params = new Params();
			String namedSqlId = "";// 查询数据集的命名sql
			params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
			namedSqlId = ProSalSqlDao.namedsql_selAllKupDppb;

			file.open();
			try {
				final int cnt = ProSalSqlDao.selKupDppbCnt(CommTools.getBaseRunEnvs().getBusi_org_id(),false);
				if (0 == cnt) {
					// 插入文件第一行，总记录数
					file.writeLastLine(Integer.toString(cnt) + "/" + "n");

				} else {
					// 插入文件第一行，总记录数
					file.write(Integer.toString(cnt) + "/" + "n");

					DaoUtil.selectList(namedSqlId, params,
							new CursorHandler<KupDppbProsInfo>() {

								@Override
								public boolean handle(int index, KupDppbProsInfo entity) {
									// 写文件
									StringBuffer file_Info = SysUtil
											.getInstance(StringBuffer.class);// 拼接字符串
									String prodcd = (CommUtil.isNotNull(entity
											.getProdcd()) ? entity.getProdcd()
											: "");
								//	String prodtx = (CommUtil.isNotNull(entity
									//		.getProdtx()) ? entity.getProdtx()
									//		.toString() : "");
									String pddpfg = (CommUtil.isNotNull(entity
											.getPddpfg()) ? entity.getPddpfg()
											.toString() : "");
									String prodtp = (CommUtil.isNotNull(entity
											.getProdtp()) ? entity.getProdtp()
											.toString() : "");
									String pdcrcy = (CommUtil.isNotNull(entity
											.getPdcrcy()) ? entity.getPdcrcy()
											.toString() : "");
									String debttp = (CommUtil.isNotNull(entity
											.getDebttp()) ? entity.getDebttp()
											.toString() : "");
									String mginfg = (CommUtil.isNotNull(entity
											.getMginfg()) ? entity.getMginfg()
											.toString() : "");
									String mgindy = (CommUtil.isNotNull(entity
											.getMgindy()) ? entity.getMgindy()
											.toString() : "");

									// 字符串拼接
									file_Info.append("PRODCD=").append(prodcd);
									file_Info.append(fengefu).append("PDDPFG=")
											.append("[").append(pddpfg)
											.append("]").append(fengefu)
											.append("PRODTP=").append("[")
											.append(prodtp).append("]");
									file_Info.append(fengefu).append("PDCRCY=")
											.append("[").append(pdcrcy)
											.append("]");
									file_Info.append(fengefu).append("DEBTTP=")
											.append("[").append(debttp)
											.append("]").append(fengefu).append("MGINFG=").append("[")  //以下后面加的
											.append(mginfg).append("]").append(fengefu).append("MGINDY=")
											.append(mgindy).append("/n");

									// 打印文件
									file.write(file_Info.toString());
									return true;

								}
							});
				}
			} finally {
				file.close();
			}

			md5 = MD5EncryptUtil.getFileMD5String(new File(path1.concat(
					File.separator).concat(filename1)));

			map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
			map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

			batch.setFilenm(filename1);
			batch.setFlpath(path2);
			batch.setFilemd(md5);
//			batch.setParams(JSON.toJSONString(map));

			ls.add(batch);
			if (ls != null) {
				java.util.List<KupDppbSync> list1 = ProSalSqlDao
						.selAllKupDppbSync(false);
				for (KupDppbSync info : list1) {
					KupDppbSyncTemp temp = SysUtil.getInstance(KupDppbSyncTemp.class);
					temp.setProdcd(info.getProdcd());
					temp.setCaredt(info.getCaredt());
					temp.setSyncst(E_SYNCST.SUCCESS);
					KupDppbSyncTempDao.insert(temp);
				}
			}

		}
	}

	private void writeKupDppbCust() {

		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		Map<String, String> map = new HashMap<String, String>();

		// 获取文件名
		String filename1 = "PSS_DP_KUPCUST_" + trandt + "_" + idx + ".txt";

		String path1 = filePath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");

		final LttsFileWriter file = new LttsFileWriter(path1, filename1,
				"UTF-8");
		// List<cif_cust> entities = null;

		Params params = new Params();
		String namedSqlId = "";// 查询数据集的命名sql
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		namedSqlId = ProSalSqlDao.namedsql_selAllKupDppbCust;

		file.open();
		try {
			final int cnt = ProSalSqlDao.selKupDppbCustCnt(CommTools.getBaseRunEnvs().getBusi_org_id(),false);
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt) + "/" + "n");
			} else {
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt) + "/" + "n");
				DaoUtil.selectList(namedSqlId, params,
						new CursorHandler<KupDppdCustProsInfo>() {

							@Override
							public boolean handle(int index,
									KupDppdCustProsInfo entity) {
								// 写文件
								StringBuffer file_Info = SysUtil
										.getInstance(StringBuffer.class);// 拼接字符串
								String prodcd = (CommUtil.isNotNull(entity
										.getProdcd()) ? entity.getProdcd() : "");
								String madtby = (CommUtil.isNotNull(entity
										.getMadtby()) ? entity.getMadtby()
										.toString() : "");
								String onlyfg = (CommUtil.isNotNull(entity
										.getOnlyfg()) ? entity.getOnlyfg()
										.toString() : "");
								String srdpam = (CommUtil.isNotNull(entity
										.getSrdpam()) ? entity.getSrdpam()
										.toString() : "");
								String stepvl = (CommUtil.isNotNull(entity
										.getStepvl()) ? entity.getStepvl()
										.toString() : "");

								// 字符串拼接
								file_Info.append("PRODCD=").append(prodcd)
										.append(fengefu).append("MADTBY=")
										.append("[").append(madtby).append("]")
										.append(fengefu);
								file_Info.append("ONLYFG=").append("[")
										.append(onlyfg).append("]")
										.append(fengefu).append("SRDPAM=")
										.append(srdpam).append(fengefu)
										.append("STEPVL=").append(stepvl);// .append(fengefu);
								file_Info.append("/n");
								// 打印文件
								file.write(file_Info.toString());
								return true;
							}
						});
			}

		} finally {
			file.close();
		}

		try {
			md5 = MD5EncryptUtil.getFileMD5String(new File(path1
					.concat(File.separator).concat(filename1)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(filename1);
		}

		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

		batch.setFilenm(filename1);
		batch.setFlpath(path2);
		batch.setFilemd(md5);
//		batch.setParams(JSON.toJSONString(map));

		ls.add(batch);

	}

	private void writelKupDppbBrch() {

		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		Map<String, String> map = new HashMap<String, String>();

		// 获取文件名
		String filename1 = "KupDppbBrch.txt";

		String path1 = filePath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");

		final LttsFileWriter file = new LttsFileWriter(path1, filename1,
				"UTF-8");
		// List<cif_cust> entities = null;
		Params params = new Params();
		String namedSqlId = "";// 查询数据集的命名sql

		namedSqlId = ProSalSqlDao.namedsql_selAllKupDppbBrch;

		file.open();
		try {
			final int cnt = ProSalSqlDao.selKupDppbBrchCnt(false);
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt) + "/" + "n");
			} else {
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt) + "/" + "n");
				DaoUtil.selectList(namedSqlId, params,
						new CursorHandler<KupDppbBrch>() {

							@Override
							public boolean handle(int index,
									KupDppbBrch entity) {
								// 写文件
								StringBuffer file_Info = SysUtil
										.getInstance(StringBuffer.class);// 拼接字符串
								String prodcd = (CommUtil.isNotNull(entity
										.getProdcd()) ? entity.getProdcd() : "");
								String crcycd = (CommUtil.isNotNull(entity
										.getCrcycd()) ? entity.getCrcycd()
										.toString() : "");
								String brchno = (CommUtil.isNotNull(entity
										.getBrchno()) ? entity.getBrchno() : "");
								String efctdt = (CommUtil.isNotNull(entity
										.getEfctdt()) ? entity.getEfctdt() : "");
								String inefdt = (CommUtil.isNotNull(entity
										.getInefdt()) ? entity.getInefdt() : "");
								String corpno = (CommUtil.isNotNull(entity
										.getCorpno()) ? entity.getCorpno() : "");

								file_Info.append("PRODCD=").append(prodcd)
										.append(fengefu).append("CRCYCD=")
										.append("[").append(crcycd).append("]")
										.append(fengefu).append("BRCHNO=")
										.append(brchno);
								file_Info.append(fengefu).append("EFCTDT=")
										.append(efctdt).append(fengefu)
										.append("INEFDT=").append(inefdt);
								file_Info.append(fengefu).append("CORPNO=")
										.append(corpno);
								file_Info.append("/n");
								// 打印文件
								file.write(file_Info.toString());
								return true;
							}
						});
			}
		} finally {
			file.close();
		}

		try {
			md5 = MD5EncryptUtil.getFileMD5String(new File(path1
					.concat(File.separator).concat(filename1)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(filename1);
		}

		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

		batch.setFilenm(filename1);
		batch.setFlpath(path2);
		batch.setFilemd(md5);
//		batch.setParams(JSON.toJSONString(map));

		ls.add(batch);

	}

	private void writeKupDppbTerm() {

		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		Map<String, String> map = new HashMap<String, String>();

		// 获取文件名
		String filename1 = "PSS_DP_KUPTERM_" + trandt + "_" + idx + ".txt";

		// 获取全路径
		String path1 = filePath;

		final LttsFileWriter file = new LttsFileWriter(path1, filename1,
				"UTF-8");
		// List<cif_cust> entities = null;

		Params params = new Params();
		String namedSqlId = "";// 查询数据集的命名sql
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		namedSqlId = ProSalSqlDao.namedsql_selAllKupDppbTerm;

		file.open();
		try {
			final int cnt = ProSalSqlDao.selKupDppbTermCnt(CommTools.getBaseRunEnvs().getBusi_org_id(),false);
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt) + "/" + "n");
			} else {
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt) + "/" + "n");

				DaoUtil.selectList(namedSqlId, params,
						new CursorHandler<IoDpKupDppbTermFile>() {

							@Override
							public boolean handle(int index,
									IoDpKupDppbTermFile entity) {

								// 写文件
								StringBuffer file_Info = SysUtil
										.getInstance(StringBuffer.class);// 拼接字符串
								String prodcd = (CommUtil.isNotNull(entity
										.getProdcd()) ? entity.getProdcd() : "");
								String depttm = (CommUtil.isNotNull(entity
										.getDepttm()) ? entity.getDepttm()
										.toString() : "");
								String deptdy1 = (CommUtil.isNotNull(entity
										.getDeptdy1())
										&& entity.getDeptdy1() != 0 ? entity
										.getDeptdy1().toString() : "");
								String deptdy2 = (CommUtil.isNotNull(entity
										.getDeptdy2())
										&& entity.getDeptdy2() != 0 ? entity
										.getDeptdy2().toString() : "");
								String deptdy3 = (CommUtil.isNotNull(entity
										.getDeptdy3())
										&& entity.getDeptdy3() != 0 ? entity
										.getDeptdy3().toString() : "");
								String deptdy4 = (CommUtil.isNotNull(entity
										.getDeptdy4())
										&& entity.getDeptdy4() != 0 ? entity
										.getDeptdy4().toString() : "");

								file_Info.append("PRODCD=").append(prodcd);
								file_Info.append(fengefu).append("DEPTTM=")
										.append(depttm);
								file_Info.append(fengefu).append("DEPTDY1=")
										.append(deptdy1);
								file_Info.append(fengefu).append("DEPTDY2=")
										.append(deptdy2);
								file_Info.append(fengefu).append("DEPTDY3=")
										.append(deptdy3);
								file_Info.append(fengefu).append("DEPTDY4=")
										.append(deptdy4);
								file_Info.append("/n");

								// 打印文件
								file.write(file_Info.toString());
								return true;
							}
						});
			}

		} finally {
			file.close();
		}

		try {
			md5 = MD5EncryptUtil.getFileMD5String(new File(path1
					.concat(File.separator).concat(filename1)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(filename1);
		}

		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

		batch.setFilenm(filename1);
		batch.setFlpath(path2);
		batch.setFilemd(md5);
//		batch.setParams(JSON.toJSONString(map));

		ls.add(batch);

	}

	private void writeKupDppbPost() {

		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		Map<String, String> map = new HashMap<String, String>();

		// 获取文件名
		String filename1 = "PSS_DP_KUPPOST_" + trandt + "_" + idx + ".txt";

		String path1 = filePath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");

		final LttsFileWriter file = new LttsFileWriter(path1, filename1,
				"UTF-8");
		// List<cif_cust> entities = null;

		Params params = new Params();
		String namedSqlId = "";// 查询数据集的命名sql
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		namedSqlId = ProSalSqlDao.namedsql_selAllKupDppbPost;

		file.open();
		try {
			final int cnt = ProSalSqlDao.selKupDppbPostCnt(CommTools.getBaseRunEnvs().getBusi_org_id(),false);
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt) + "/" + "n");
			} else {
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt) + "/" + "n");
				DaoUtil.selectList(namedSqlId, params,
						new CursorHandler<KupDppbPostProsInfo>() {

							@Override
							public boolean handle(int index,
									KupDppbPostProsInfo entity) {
								// 写文件
								StringBuffer file_Info = SysUtil
										.getInstance(StringBuffer.class);// 拼接字符串
								String prodcd = (CommUtil.isNotNull(entity
										.getProdcd()) ? entity.getProdcd() : "");
								String posttp = (CommUtil.isNotNull(entity
										.getPosttp()) ? entity.getPosttp()
										.toString() : "");
								String postwy = (CommUtil.isNotNull(entity
										.getPostwy()) ? entity.getPostwy()
										.toString() : "");
								String amntwy = (CommUtil.isNotNull(entity
										.getAmntwy()) ? entity.getAmntwy()
										.toString() : "");
								String miniam = (CommUtil.isNotNull(entity
										.getMiniam()) ? entity.getMiniam()
										.toString() : "");
								String maxiam = (CommUtil.isNotNull(entity
										.getMaxiam()) ? entity.getMaxiam()
										.toString() : "");
								String timewy = (CommUtil.isNotNull(entity
										.getTimewy()) ? entity.getTimewy()
										.toString() : "");
								String minitm = (CommUtil.isNotNull(entity
										.getMinitm()) ? entity.getMinitm()
										.toString() : "");
								String maxitm = (CommUtil.isNotNull(entity
										.getMaxitm()) ? entity.getMaxitm()
										.toString() : "");
								String detlfg = (CommUtil.isNotNull(entity
										.getDetlfg()) ? entity.getDetlfg()
										.toString() : "");
								String maxibl = (CommUtil.isNotNull(entity
										.getMaxibl()) ? entity.getMaxibl()
										.toString() : "");

								file_Info.append("PRODCD=").append(prodcd)
										.append(fengefu).append("POSTTP=")
										.append("[").append(posttp).append("]");
								file_Info.append(fengefu).append("POSTWY=")
										.append("[").append(postwy).append("]")
										.append(fengefu).append("AMNTWY=")
										.append("[").append(amntwy).append("]")
										.append(fengefu).append("MINIAM=")
										.append(miniam);
								file_Info.append(fengefu).append("MAXIAM=")
										.append(maxiam).append(fengefu)
										.append("TIMEWY=").append("[")
										.append(timewy).append("]");
								file_Info.append(fengefu).append("MINITM=")
										.append(minitm).append(fengefu)
										.append("MAXITM=").append(maxitm)
										.append(fengefu).append("DETLFG=")
										.append("[").append(detlfg).append("]");
								file_Info.append(fengefu).append("MAXIBL=")
										.append(maxibl).append("/n");

								// 打印文件
								file.write(file_Info.toString());
								return true;
							}
						});
			}

		} finally {
			file.close();
		}

		try {
			md5 = MD5EncryptUtil.getFileMD5String(new File(path1
					.concat(File.separator).concat(filename1)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(filename1);
		}

		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

		batch.setFilenm(filename1);
		batch.setFlpath(path2);
		batch.setFilemd(md5);
//		batch.setParams(JSON.toJSONString(map));

		ls.add(batch);

	}

	private void writeKupDppbDraw() {

		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		Map<String, String> map = new HashMap<String, String>();

		// 获取文件名
		String filename1 = "PSS_DP_KUPDRAW_" + trandt + "_" + idx + ".txt";

		String path1 = filePath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");

		final LttsFileWriter file = new LttsFileWriter(path1, filename1,
				"UTF-8");
		// List<cif_cust> entities = null;

		Params params = new Params();
		String namedSqlId = "";// 查询数据集的命名sql
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		namedSqlId = ProSalSqlDao.namedsql_selAllKupDppbDraw;

		file.open();
		try {
			final int cnt = ProSalSqlDao.selKupDppbDrawCnt(CommTools.getBaseRunEnvs().getBusi_org_id(),false);
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt) + "/" + "n");
			} else {
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt) + "/" + "n");
				DaoUtil.selectList(namedSqlId, params,
						new CursorHandler<KupDppbDrawProsInfo>() {

							@Override
							public boolean handle(int index,
									KupDppbDrawProsInfo entity) {
								// 写文件
								StringBuffer file_Info = SysUtil
										.getInstance(StringBuffer.class);// 拼接字符串
								String prodcd = (CommUtil.isNotNull(entity
										.getProdcd()) ? entity.getProdcd() : "");
								String drawtp = (CommUtil.isNotNull(entity
										.getDrawtp()) ? entity.getDrawtp()
										.toString() : "");
								String ctrlwy = (CommUtil.isNotNull(entity
										.getCtrlwy()) ? entity.getCtrlwy()
										.toString() : "");
								String dramwy = (CommUtil.isNotNull(entity
										.getDramwy()) ? entity.getDramwy()
										.toString() : "");
								String drmiam = (CommUtil.isNotNull(entity
										.getDrmiam()) ? entity.getDrmiam()
										.toString() : "");
								String drmxam = (CommUtil.isNotNull(entity
										.getDrmxam()) ? entity.getDrmxam()
										.toString() : "");
								String drtmwy = (CommUtil.isNotNull(entity
										.getDrtmwy()) ? entity.getDrtmwy()
										.toString() : "");
								String drmitm = (CommUtil.isNotNull(entity
										.getDrmitm()) ? entity.getDrmitm()
										.toString() : "");
								String drmxtm = (CommUtil.isNotNull(entity
										.getDrmxtm()) ? entity.getDrmxtm()
										.toString() : "");
								String drrule = (CommUtil.isNotNull(entity
										.getDrrule()) ? entity.getDrrule()
										.toString() : "");
								String ismibl = (CommUtil.isNotNull(entity
										.getIsmibl()) ? entity.getIsmibl()
										.toString() : "");
								String minibl = (CommUtil.isNotNull(entity
										.getMinibl()) ? entity.getMinibl()
										.toString() : "");

								file_Info.append("PRODCD=").append(prodcd)
										.append(fengefu).append("DRAWTP=")
										.append("[").append(drawtp).append("]")
										.append(fengefu);
								file_Info.append("CTRLWY=").append("[")
										.append(ctrlwy).append("]")
										.append(fengefu);
								file_Info.append("DRAMWY=").append("[")
										.append(dramwy).append("]")
										.append(fengefu).append("DRMIAM=")
										.append(drmiam).append(fengefu)
										.append("DRMXAM=").append(drmxam)
										.append(fengefu);
								file_Info.append("DRTMWY=").append("[")
										.append(drtmwy).append("]")
										.append(fengefu).append("DRMITM=")
										.append(drmitm).append(fengefu)
										.append("DRMXTM=").append(drmxtm)
										.append(fengefu).append("DRRULE=")
										.append("[").append(drrule).append("]");
								file_Info.append(fengefu).append("ISMIBL=")
										.append("[").append(ismibl).append("]")
										.append(fengefu).append("MINIBL=")
										.append(minibl);
								file_Info.append("/n");
								// 打印文件
								file.write(file_Info.toString());
								return true;
							}
						});
			}

		} finally {
			file.close();
		}

		try {
			md5 = MD5EncryptUtil.getFileMD5String(new File(path1
					.concat(File.separator).concat(filename1)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(filename1);
		}

		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

		batch.setFilenm(filename1);
		batch.setFlpath(path2);
		batch.setFilemd(md5);
//		batch.setParams(JSON.toJSONString(map));

		ls.add(batch);

	}

	private void writeKupDppbMatu() {

		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		Map<String, String> map = new HashMap<String, String>();

		// 获取文件名
		String filename1 = "PSS_DP_KUPMATU_" + trandt + "_" + idx + ".txt";

		String path1 = filePath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");

		final LttsFileWriter file = new LttsFileWriter(path1, filename1,
				"UTF-8");
		// List<cif_cust> entities = null;

		Params params = new Params();
		String namedSqlId = "";// 查询数据集的命名sql
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		namedSqlId = ProSalSqlDao.namedsql_selAllKupDppbMatu;

		file.open();
		try {
			final int cnt = ProSalSqlDao.selKupDppbMatuCnt(CommTools.getBaseRunEnvs().getBusi_org_id(),false);
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt) + "/" + "n");
			} else {
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt) + "/" + "n");
				DaoUtil.selectList(namedSqlId, params,
						new CursorHandler<KupDppbMatuProsInfo>() {

							@Override
							public boolean handle(int index,
									KupDppbMatuProsInfo entity) {
								// 写文件
								StringBuffer file_Info = SysUtil
										.getInstance(StringBuffer.class);// 拼接字符串
								String prodcd = (CommUtil.isNotNull(entity
										.getProdcd()) ? entity.getProdcd() : "");
								String festdl = (CommUtil.isNotNull(entity
										.getFestdl()) ? entity.getFestdl()
										.toString() : "");
								String delyfg = (CommUtil.isNotNull(entity
										.getDelyfg()) ? entity.getDelyfg()
										.toString() : "");
								String matupd = (CommUtil.isNotNull(entity
										.getMatupd()) ? entity.getMatupd()
										.toString() : "");
								String trdpfg = (CommUtil.isNotNull(entity
										.getTrdpfg()) ? entity.getTrdpfg()
										.toString() : "");
								String trpdfg = (CommUtil.isNotNull(entity
										.getTrpdfg()) ? entity.getTrpdfg()
										.toString() : "");
								String trintm = (CommUtil.isNotNull(entity
										.getTrintm()) ? entity.getTrintm()
										.toString() : "");
								String trprod = (CommUtil.isNotNull(entity
										.getTrprod()) ? entity.getTrprod()
										.toString() : "");
								String trinwy = (CommUtil.isNotNull(entity
										.getTrinwy()) ? entity.getTrinwy()
										.toString() : "");
								String trsvtp = (CommUtil.isNotNull(entity
										.getTrsvtp()) ? entity.getTrsvtp()
										.toString() : "");

								file_Info.append("PRODCD=").append(prodcd)
										.append(fengefu).append("FESTDL=")
										.append("[").append(festdl).append("]")
										.append(fengefu);
								file_Info.append("DELYFG=").append("[")
										.append(delyfg).append("]")
										.append(fengefu).append("MATUPD=")
										.append(matupd).append(fengefu)
										.append("TRDPFG=").append("[")
										.append(trdpfg).append("]")
										.append(fengefu);
								file_Info.append("TRPDFG=").append("[")
										.append(trpdfg).append("]")
										.append(fengefu).append("TRINTM=")
										.append(trintm).append(fengefu)
										.append("TRPROD=").append(trprod)
										.append(fengefu);
								file_Info.append("TRINWY=").append("[")
										.append(trinwy).append("]")
										.append(fengefu).append("TRSVTP=")
										.append("[").append(trsvtp).append("]");
								file_Info.append("/n");
								// 打印文件
								file.write(file_Info.toString());
								return true;
							}
						});
			}

		} finally {
			file.close();
		}

		try {
			md5 = MD5EncryptUtil.getFileMD5String(new File(path1
					.concat(File.separator).concat(filename1)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(filename1);
		}

		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

		batch.setFilenm(filename1);
		batch.setFlpath(path2);
		batch.setFilemd(md5);
//		batch.setParams(JSON.toJSONString(map));

		ls.add(batch);
	}

	private void writeKupDppbDfir() {

		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		Map<String, String> map = new HashMap<String, String>();

		// 获取文件名
		String filename1 = "PSS_DP_KUPDFIR_" + trandt + "_" + idx + ".txt";

		String path1 = filePath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");

		final LttsFileWriter file = new LttsFileWriter(path1, filename1,
				"UTF-8");
		// List<cif_cust> entities = null;

		Params params = new Params();
		String namedSqlId = "";// 查询数据集的命名sql
		params.add("corpno", CommTools.getBaseRunEnvs().getCenter_org_id());
		
		namedSqlId = ProSalSqlDao.namedsql_selAllKupDppbDfir;

		file.open();
		try {
			final int cnt = ProSalSqlDao.selKupDppbDfirCnt(CommTools.getBaseRunEnvs().getCenter_org_id(),false);
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt) + "/" + "n");
			} else {
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt) + "/" + "n");
				DaoUtil.selectList(namedSqlId, params,
						new CursorHandler<KupDppbDfir>() {

							@Override
							public boolean handle(int index,
									KupDppbDfir entity) {
								// 写文件
								StringBuffer file_Info = SysUtil
										.getInstance(StringBuffer.class);// 拼接字符串
								String prodcd = (CommUtil.isNotNull(entity
										.getProdcd()) ? entity.getProdcd() : "");
								String teartp = (CommUtil.isNotNull(entity
										.getTeartp()) ? entity.getTeartp()
										.toString() : "");
								String incdtp = (CommUtil.isNotNull(entity
										.getIncdtp()) ? entity.getIncdtp()
										.toString() : "");
								String drintx = (CommUtil.isNotNull(entity
										.getDrintx()) ? entity.getDrintx()
										.toString() : "");
								// String intrtp =
								// (CommUtil.isNotNull(entity.getIntrtp()) ?
								// entity.getIntrtp().toString() : "");
								String insrwy = (CommUtil.isNotNull(entity
										.getInsrwy()) ? entity.getInsrwy()
										.toString() : "");
								String inclfg = (CommUtil.isNotNull(entity
										.getInclfg()) ? entity.getInclfg()
										.toString() : "");
								String intrwy = (CommUtil.isNotNull(entity
										.getIntrwy()) ? entity.getIntrwy()
										.toString() : "");
								String bsinam = (CommUtil.isNotNull(entity
										.getBsinam()) ? entity.getBsinam()
										.toString() : "");
								String bsindt = (CommUtil.isNotNull(entity
										.getBsindt()) ? entity.getBsindt()
										.toString() : "");
								String inedsc = (CommUtil.isNotNull(entity
										.getInedsc()) ? entity.getInedsc()
										.toString() : "");
								String bsincd = (CommUtil.isNotNull(entity
										.getBsincd()) ? entity.getBsincd()
										.toString() : "");
								String bsinrl = (CommUtil.isNotNull(entity
										.getBsinrl()) ? entity.getBsinrl()
										.toString() : "");
								String drdein = (CommUtil.isNotNull(entity
										.getDrdein()) ? entity.getDrdein()
										.toString() : "");
								String inadtp = (CommUtil.isNotNull(entity
										.getInadtp()) ? entity.getInadtp()
										.toString() : "");
								String intrdt = (CommUtil.isNotNull(entity
										.getIntrdt()) ? entity.getIntrdt()
										.toString() : "");
								String levety = (CommUtil.isNotNull(entity
										.getLevety()) ? entity.getLevety()
										.toString() : "");
								file_Info.append("PRODCD=").append(prodcd);
								file_Info.append(fengefu).append("TEARTP")
										.append(teartp).append("=").append("[")
										.append(teartp).append("]")
										.append(fengefu).append("INCDTP")
										.append(teartp).append("=").append("[")
										.append(incdtp).append("]");
								file_Info.append(fengefu).append("DRINTX")
										.append(teartp).append("=")
										.append(drintx);// .append("INTRTP").append(teartp).append("=").append("[").append(intrtp).append("]");
								file_Info.append(fengefu).append("INSRWY")
										.append(teartp).append("=").append("[")
										.append(insrwy).append("]");
								file_Info.append(fengefu).append("INCLFG")
										.append(teartp).append("=").append("[")
										.append(inclfg).append("]")
										.append(fengefu).append("INTRWY")
										.append(teartp).append("=").append("[")
										.append(intrwy).append("]")
										.append(fengefu).append("BSINAM")
										.append(teartp).append("=").append("[")
										.append(bsinam).append("]");
								file_Info.append(fengefu).append("BSINDT")
										.append(teartp).append("=").append("[")
										.append(bsindt).append("]")
										.append(fengefu).append("INEDSC")
										.append(teartp).append("=").append("[")
										.append(inedsc).append("]")
										.append(fengefu).append("BSINCD")
										.append(teartp).append("=")
										.append(bsincd);
								file_Info.append(fengefu).append("BSINRL")
										.append(teartp).append("=").append("[")
										.append(bsinrl).append("]")
										.append(fengefu);
								file_Info.append("DRDEIN").append(teartp)
										.append("=").append("[").append(drdein)
										.append("]").append("/n"); // 加了 DRDEIN=
								file_Info.append(fengefu).append("INADTP")			//以下后面加的
								         .append(teartp).append("=").append("[")
								         .append(inadtp).append("]")
								         .append(fengefu).append("INTRDT")
								         .append(teartp).append("=").append("[")
								         .append(intrdt).append("]")
								         .append(fengefu).append("LEVETY")
								         .append(teartp).append("=").append("[")
								         .append(levety).append("]");
								// 打印文件
								file.write(file_Info.toString());
								return true;
							}
						});
			}
		} finally {
			file.close();
		}

		try {
			md5 = MD5EncryptUtil.getFileMD5String(new File(path1
					.concat(File.separator).concat(filename1)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(filename1);
		}

		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

		batch.setFilenm(filename1);
		batch.setFlpath(path2);
		batch.setFilemd(md5);
//		batch.setParams(JSON.toJSONString(map));

		ls.add(batch);

	}

	private void writeKupDppbPostPlan() {

		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		Map<String, String> map = new HashMap<String, String>();

		// 获取文件名
		String filename1 = "PSS_DP_KUPPOPL_" + trandt + "_" + idx + ".txt";

		String path1 = filePath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");

		final LttsFileWriter file = new LttsFileWriter(path1, filename1,
				"UTF-8");
		// List<cif_cust> entities = null;

		Params params = new Params();
		String namedSqlId = "";// 查询数据集的命名sql
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		namedSqlId = ProSalSqlDao.namedsql_selAllKupDppbPostPlan;

		file.open();
		try {
			final int cnt = ProSalSqlDao.selKupDppbPostPlanCnt(CommTools.getBaseRunEnvs().getBusi_org_id(),false);
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt) + "/" + "n");
			} else {
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt) + "/" + "n");
				DaoUtil.selectList(namedSqlId, params,
						new CursorHandler<KupDppbPtplProsInfo>() {

							@Override
							public boolean handle(int index,
									KupDppbPtplProsInfo entity) {
								// 写文件
								StringBuffer file_Info = SysUtil
										.getInstance(StringBuffer.class);// 拼接字符串
								String prodcd = (CommUtil.isNotNull(entity
										.getProdcd()) ? entity.getProdcd() : "");
								String gentwy = (CommUtil.isNotNull(entity
										.getGentwy()) ? entity.getGentwy()
										.toString() : "");
								String planpd = (CommUtil.isNotNull(entity
										.getPlanpd()) ? entity.getPlanpd()
										.toString() : "");
								String svlepd = (CommUtil.isNotNull(entity
										.getSvlepd()) ? entity.getSvlepd()
										.toString() : "");
								String svlewy = (CommUtil.isNotNull(entity
										.getSvlewy()) ? entity.getSvlewy()
										.toString() : "");
								String maxisp = (CommUtil.isNotNull(entity
										.getMaxisp()) ? entity.getMaxisp()
										.toString() : "");
								String dfltsd = (CommUtil.isNotNull(entity
										.getDfltsd()) ? entity.getDfltsd()
										.toString() : "");
								String svletm = (CommUtil.isNotNull(entity
										.getSvletm()) ? entity.getSvletm()
										.toString() : "");
								String dfltwy = (CommUtil.isNotNull(entity
										.getDfltwy()) ? entity.getDfltwy()
										.toString() : "");
								String pscrwy = (CommUtil.isNotNull(entity
										.getPscrwy()) ? entity.getPscrwy()
										.toString() : "");
								String psamtp = (CommUtil.isNotNull(entity
										.getPsamtp()) ? entity.getPsamtp()
										.toString() : ""); // 后面加的

								file_Info.append("PRODCD=").append(prodcd);
								file_Info.append(fengefu).append("GENTWY=")
										.append("[").append(gentwy).append("]")
										.append(fengefu).append("PLANPD=")
										.append(planpd).append(fengefu)
										.append("SVLEPD=").append(svlepd);
								file_Info.append(fengefu).append("SVLEWY=")
										.append("[").append(svlewy).append("]")
										.append(fengefu).append("MAXISP=")
										.append(maxisp).append(fengefu)
										.append("DFLTSD=").append("[")
										.append(dfltsd).append("]");
								file_Info.append(fengefu).append("SVLETM=")
										.append(svletm).append(fengefu)
										.append("DFLTWY=").append("[")
										.append(dfltwy).append("]")
										.append(fengefu).append("PSCRWY=")
										.append("[").append(pscrwy).append("]");
								file_Info.append(fengefu).append("PSAMTP=")
										.append("[").append(psamtp).append("]")
										.append("/n");
								// 打印文件
								file.write(file_Info.toString());
								return true;
							}
						});
			}

		} finally {
			file.close();
		}

		try {
			md5 = MD5EncryptUtil.getFileMD5String(new File(path1
					.concat(File.separator).concat(filename1)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(filename1);
		}

		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

		batch.setFilenm(filename1);
		batch.setFlpath(path2);
		batch.setFilemd(md5);
//		batch.setParams(JSON.toJSONString(map));

		ls.add(batch);

	}

	private void writeKupDppbDrawPlan() {

		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		Map<String, String> map = new HashMap<String, String>();

		// 获取文件名
		String filename1 = "PSS_DP_KUPDRPL_" + trandt + "_" + idx + ".txt";

		String path1 = filePath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");

		final LttsFileWriter file = new LttsFileWriter(path1, filename1,
				"UTF-8");
		// List<cif_cust> entities = null;

		Params params = new Params();
		String namedSqlId = "";// 查询数据集的命名sql
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		namedSqlId = ProSalSqlDao.namedsql_selAllKupDppbDrawPlan;

		file.open();
		try {
			final int cnt = ProSalSqlDao.selKupDppbDrawPlanCnt(CommTools.getBaseRunEnvs().getBusi_org_id(),false);
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt) + "/" + "n");
			} else {
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt) + "/" + "n");
				DaoUtil.selectList(namedSqlId, params,
						new CursorHandler<KupDppbDrawPlan>() {

							@Override
							public boolean handle(int index,
									KupDppbDrawPlan entity) {
								// 写文件
								StringBuffer file_Info = SysUtil
										.getInstance(StringBuffer.class);// 拼接字符串
								String prodcd = (CommUtil.isNotNull(entity
										.getProdcd()) ? entity.getProdcd() : "");
								String dradwy = (CommUtil.isNotNull(entity
										.getDradwy()) ? entity.getDradwy()
										.toString() : "");
								String gendpd = (CommUtil.isNotNull(entity
										.getGendpd()) ? entity.getGendpd()
										.toString() : "");
								String drcrwy = (CommUtil.isNotNull(entity
										.getDrcrwy()) ? entity.getDrcrwy()
										.toString() : "");
								String drdfsd = (CommUtil.isNotNull(entity
										.getDrdfsd()) ? entity.getDrdfsd()
										.toString() : "");
								String drdfwy = (CommUtil.isNotNull(entity
										.getDrdfwy()) ? entity.getDrdfwy()
										.toString() : "");
								String beinfg = (CommUtil.isNotNull(entity
										.getBeinfg()) ? entity.getBeinfg()
										.toString() : "");

								file_Info.append("PRODCD=").append(prodcd)
										.append(fengefu);
								file_Info.append("DRADWY=").append("[")
										.append(dradwy).append("]")
										.append(fengefu).append("GENDPD=")
										.append(gendpd);
								file_Info.append(fengefu).append("DRCRWY=")
										.append("[").append(drcrwy).append("]");
								file_Info.append(fengefu).append("DRDFSD=")
										.append("[").append(drdfsd).append("]")
										.append(fengefu).append("DRDFWY=")
										.append("[").append(drdfwy).append("]");
								file_Info.append(fengefu).append("BEINFG=")
										.append("[").append(beinfg).append("]");
								file_Info.append("/n");
								// 打印文件
								file.write(file_Info.toString());
								return true;
							}
						});
			}

		} finally {
			file.close();
		}

		try {
			md5 = MD5EncryptUtil.getFileMD5String(new File(path1
					.concat(File.separator).concat(filename1)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(filename1);
		}

		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

		batch.setFilenm(filename1);
		batch.setFlpath(path2);
		batch.setFilemd(md5);
//		batch.setParams(JSON.toJSONString(map));

		ls.add(batch);

	}

	private void writeKupDppbIntr() {

		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		Map<String, String> map = new HashMap<String, String>();

		// 获取文件名
		String filename1 = "PSS_DP_KUPINTR_" + trandt + "_" + idx + ".txt";

		String path1 = filePath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");

		final LttsFileWriter file = new LttsFileWriter(path1, filename1,
				"UTF-8");
		// List<cif_cust> entities = null;

		Params params = new Params();
		String namedSqlId = "";// 查询数据集的命名sql
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		namedSqlId = ProSalSqlDao.namedsql_selAllKupDppbIntr;

		file.open();
		try {
			final int cnt = ProSalSqlDao.selKupDppbIntrCnt(CommTools.getBaseRunEnvs().getBusi_org_id(),false);
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt) + "/" + "n");
			} else {
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt) + "/" + "n");
				DaoUtil.selectList(namedSqlId, params,
						new CursorHandler<KupDppbIntr>() {

							@Override
							public boolean handle(int index,
									KupDppbIntr entity) {
								// 写文件
								StringBuffer file_Info = SysUtil
										.getInstance(StringBuffer.class);// 拼接字符串
								String prodcd = (CommUtil.isNotNull(entity
										.getProdcd()) ? entity.getProdcd() : "");
								String intrtp = (CommUtil.isNotNull(entity
										.getIntrtp()) ? entity.getIntrtp()
										.toString() : "");
								String inbefg = (CommUtil.isNotNull(entity
										.getInbefg()) ? entity.getInbefg()
										.toString() : "");
								String txbefg = (CommUtil.isNotNull(entity
										.getTxbefg()) ? entity.getTxbefg()
										.toString() : "");
								String txbebs = (CommUtil.isNotNull(entity
										.getTxbebs()) ? entity.getTxbebs()
										.toString() : "");
								String tebehz = (CommUtil.isNotNull(entity
										.getTebehz()) ? entity.getTebehz()
										.toString() : "");
								String hutxfg = (CommUtil.isNotNull(entity
										.getHutxfg()) ? entity.getHutxfg()
										.toString() : "");
								String txbefr = (CommUtil.isNotNull(entity
										.getTxbefr()) ? entity.getTxbefr()
										.toString() : "");
								String intrcd = (CommUtil.isNotNull(entity
										.getIntrcd()) ? entity.getIntrcd()
										.toString() : "");
								String inwytp = (CommUtil.isNotNull(entity
										.getInwytp()) ? entity.getInwytp()
										.toString() : "");
								String intrwy = (CommUtil.isNotNull(entity
										.getIntrwy()) ? entity.getIntrwy()
										.toString() : "");
								String incdtp = (CommUtil.isNotNull(entity
										.getIncdtp()) ? entity.getIncdtp()
										.toString() : "");
								String lyinwy = (CommUtil.isNotNull(entity
										.getLyinwy()) ? entity.getLyinwy()
										.toString() : "");
								String inammd = (CommUtil.isNotNull(entity
										.getInammd()) ? entity.getInammd()
										.toString() : "");
								String bldyca = (CommUtil.isNotNull(entity
										.getBldyca()) ? entity.getBldyca()
										.toString() : "");
								String inprwy = (CommUtil.isNotNull(entity
										.getInprwy()) ? entity.getInprwy()
										.toString() : "");
								String inadlv = (CommUtil.isNotNull(entity
										.getInadlv()) ? entity.getInadlv()
										.toString() : "");
								String reprwy = (CommUtil.isNotNull(entity
										.getReprwy()) ? entity.getReprwy()
										.toString() : "");
								String taxecd = (CommUtil.isNotNull(entity
										.getTaxecd()) ? entity.getTaxecd()
										.toString() : "");
								String isrgdt = (CommUtil.isNotNull(entity
										.getIsrgdt()) ? entity.getIsrgdt()
										.toString() : "");
								String lydttp = (CommUtil.isNotNull(entity
										.getLydttp()) ? entity.getLydttp()
										.toString() : "");
								String intrdt = (CommUtil.isNotNull(entity
										.getIntrdt()) ? entity.getIntrdt()
										.toString() : "");
								String levety = (CommUtil.isNotNull(entity
										.getLevety()) ? entity.getLevety()
										.toString() : "");
								file_Info.append("PRODCD=").append(prodcd)
										.append(fengefu).append("INTRTP=")
										.append("[").append(intrtp).append("]");
								file_Info.append(fengefu).append("INBEFG=")
										.append("[").append(inbefg).append("]")
										.append(fengefu).append("TXBEFG=")
										.append("[").append(txbefg).append("]")
										.append(fengefu).append("TXBEBS=")
										.append("[").append(txbebs).append("]");
								file_Info.append(fengefu).append("TEBEHZ=")
										.append(tebehz).append(fengefu)
										.append("HUTXFG=").append("[")
										.append(hutxfg).append("]")
										.append(fengefu).append("TXBEFR=")
										.append(txbefr);
								file_Info.append(fengefu).append("INTRCD=")
										.append(intrcd).append(fengefu)
										.append("INWYTP=").append("[")
										.append(inwytp).append("]")
										.append(fengefu).append("INTRWY=")
										.append("[").append(intrwy).append("]");
								file_Info.append(fengefu).append("INCDTP=")
										.append("[").append(incdtp).append("]")
										.append(fengefu).append("LYINWY=")
										.append("[").append(lyinwy).append("]")
										.append(fengefu).append("INAMMD=")
										.append("[").append(inammd).append("]");
								file_Info.append(fengefu).append("BLDYCA=")
										.append("[").append(bldyca).append("]")
										.append(fengefu).append("INPRWY=")
										.append("[").append(inprwy).append("]")
										.append(fengefu).append("INADLV=")
										.append(inadlv);
								file_Info.append(fengefu).append("REPRWY=")
										.append("[").append(reprwy).append("]");
								file_Info.append(fengefu).append("TAXECD=")
										.append(taxecd).append(fengefu)
										.append("ISRGDT=").append("[")
										.append(isrgdt).append("]")
										.append(fengefu).append("LYDTTP=")
										.append("[").append(lydttp).append("]")
										.append(fengefu).append("INTRDT=")        //以下是后面加的
										.append("[").append(intrdt).append("]")
										.append(fengefu).append("LEVETY=")
										.append("[").append(levety).append("]");
								file_Info.append("/n");

								// 打印文件
								file.write(file_Info.toString());

								return true;
							}
						});
			}

		} finally {
			file.close();
		}

		try {
			md5 = MD5EncryptUtil.getFileMD5String(new File(path1
					.concat(File.separator).concat(filename1)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(filename1);
		}

		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

		batch.setFilenm(filename1);
		batch.setFlpath(path2);
		batch.setFilemd(md5);
//		batch.setParams(JSON.toJSONString(map));

		ls.add(batch);

	}

	private void writeKupDppbAcct() {

		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		Map<String, String> map = new HashMap<String, String>();

		// 获取文件名
		String filename1 = "PSS_DP_KUPACCT_" + trandt + "_" + idx + ".txt";

		String path1 = filePath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");

		final LttsFileWriter file = new LttsFileWriter(path1, filename1,
				"UTF-8");
		// List<cif_cust> entities = null;

		Params params = new Params();
		String namedSqlId = "";// 查询数据集的命名sql
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		namedSqlId = ProSalSqlDao.namedsql_selAllKupDppbAcct;

		file.open();
		try {
			final int cnt = ProSalSqlDao.selKupDppbAcctCnt(CommTools.getBaseRunEnvs().getBusi_org_id(),false);
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt) + "/" + "n");
			} else {
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt) + "/" + "n");
				DaoUtil.selectList(namedSqlId, params,
						new CursorHandler<KupDppbAcct>() {

							@Override
							public boolean handle(int index,
									KupDppbAcct entity) {
								// 写文件
								StringBuffer file_Info = SysUtil
										.getInstance(StringBuffer.class);// 拼接字符串
								String prodcd = (CommUtil.isNotNull(entity
										.getProdcd()) ? entity.getProdcd() : "");
								String acctcd = (CommUtil.isNotNull(entity
										.getAcctcd()) ? entity.getAcctcd()
										.toString() : "");

								file_Info.append("PRODCD=").append(prodcd)
										.append(fengefu).append("ACCTCD=")
										.append(acctcd);
								file_Info.append("/n");
								// 打印文件
								file.write(file_Info.toString());
								return true;
							}
						});
			}

		} finally {
			file.close();
		}

		try {
			md5 = MD5EncryptUtil.getFileMD5String(new File(path1
					.concat(File.separator).concat(filename1)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(filename1);
		}

		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

		batch.setFilenm(filename1);
		batch.setFlpath(path2);
		batch.setFilemd(md5);
//		batch.setParams(JSON.toJSONString(map));

		ls.add(batch);

	}

	private void writeKupDppbActp() {

		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		Map<String, String> map = new HashMap<String, String>();

		// 获取文件名
		String filename1 = "PSS_DP_KUPACTP_" + trandt + "_" + idx + ".txt";

		String path1 = filePath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");

		final LttsFileWriter file = new LttsFileWriter(path1, filename1,
				"UTF-8");
		// List<cif_cust> entities = null;

		Params params = new Params();
		String namedSqlId = "";// 查询数据集的命名sql
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		namedSqlId = ProSalSqlDao.namedsql_selAllKupDppbActp;

		file.open();
		try {
			final int cnt = ProSalSqlDao.selKupDppbActpCnt(CommTools.getBaseRunEnvs().getBusi_org_id(),false);
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt) + "/" + "n");
			} else {
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt) + "/" + "n");
				DaoUtil.selectList(namedSqlId, params,
						new CursorHandler<KupDppbActp>() {

							@Override
							public boolean handle(int index,
									KupDppbActp entity) {
								// 写文件
								StringBuffer file_Info = SysUtil
										.getInstance(StringBuffer.class);// 拼接字符串
								String prodcd = (CommUtil.isNotNull(entity
										.getProdcd()) ? entity.getProdcd() : "");
								String cacttp = (CommUtil.isNotNull(entity
										.getCacttp()) ? entity.getCacttp()
										.toString() : "");
								String acolfg = (CommUtil.isNotNull(entity
										.getAcolfg()) ? entity.getAcolfg()
										.toString() : "");

								file_Info.append("PRODCD=").append(prodcd)
										.append(fengefu).append("CACTTP=")
										.append("[").append(cacttp).append("]")
										.append(fengefu).append("ACOLFG=")
										.append("[").append(acolfg).append("]");
								file_Info.append("/n");
								// 打印文件
								file.write(file_Info.toString());
								return true;
							}
						});
			}

		} finally {
			file.close();
		}

		try {
			md5 = MD5EncryptUtil.getFileMD5String(new File(path1
					.concat(File.separator).concat(filename1)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(filename1);
		}

		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

		batch.setFilenm(filename1);
		batch.setFlpath(path2);
		batch.setFilemd(md5);
//		batch.setParams(JSON.toJSONString(map));

		ls.add(batch);

	}

	private void writeKupDppbAddt() {

		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
		Map<String, String> map = new HashMap<String, String>();

		// 获取文件名
		String filename1 = "PSS_DP_KUPADDT_" + trandt + "_" + idx + ".txt";

		String path1 = filePath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");

		final LttsFileWriter file = new LttsFileWriter(path1, filename1,
				"UTF-8");
		// List<cif_cust> entities = null;

		Params params = new Params();
		String namedSqlId = "";// 查询数据集的命名sql
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		namedSqlId = ProSalSqlDao.namedsql_selAllKupDppbAddt;

		// knp_bach back = SysUtil.getInstance(knp_bach.class);

		String filesq = MsSystemSeq.getTrxnSeq();

		file.open();
		try {
			final int cnt = ProSalSqlDao.selKupDppbAddtCnt(CommTools.getBaseRunEnvs().getBusi_org_id(),false);
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt) + "/" + "n");
			} else {
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt) + "/" + "n");
				DaoUtil.selectList(namedSqlId, params,
						new CursorHandler<KupDppbAddt>() {

							@Override
							public boolean handle(int index,
									KupDppbAddt entity) {
								// 写文件
								StringBuffer file_Info = SysUtil
										.getInstance(StringBuffer.class);// 拼接字符串
								String prodcd = (CommUtil.isNotNull(entity
										.getProdcd()) ? entity.getProdcd() : "");
								String accatp = (CommUtil.isNotNull(entity
										.getAccatp()) ? entity.getAccatp()
										.toString() : "");

								file_Info.append("PRODCD=").append(prodcd)
										.append(fengefu).append("ACCATP=")
										.append("[").append(accatp).append("]");
								file_Info.append("/n");
								// 打印文件
								file.write(file_Info.toString());
								return true;
							}
						});
			}

		} finally {
			file.close();
		}

		try {
			md5 = MD5EncryptUtil.getFileMD5String(new File(path1
					.concat(File.separator).concat(filename1)));
		} catch (Exception e) {
			throw ApError.BusiAplt.E0042(filename1);
		}

		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

		batch.setFilenm(filename1);
		batch.setFlpath(path2);
		batch.setFilemd(md5);
//		batch.setParams(JSON.toJSONString(map));

		ls.add(batch);

	}

}
