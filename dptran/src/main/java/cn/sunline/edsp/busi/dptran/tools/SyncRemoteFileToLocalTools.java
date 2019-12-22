package cn.sunline.edsp.busi.dptran.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.engine.BatchConfigConstant;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.FileUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApRemoteFileList;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.Knb_file_infoDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.knb_file_info;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

/**
 * <p>
 * 文件功能说明：
 * </p>
 * 
 * @Author songlw
 *         <p>
 *         <li>2019年12月9日-下午8:27:19</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2019年12月9日：同步远程文件</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class SyncRemoteFileToLocalTools {
	// 初始化日志信息
	private static final BizLog bizLog = BizLogUtil.getBizLog(SyncRemoteFileToLocalTools.class);
	private static final String FILE_OK = ".OK";

	/**
	 * @Author songlw
	 *         <p>
	 *         <li>2019年12月11日-上午10:00:33</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param remoteDir
	 * @param localDir
	 * @param uploadDir
	 * @param batchTranGroup
	 */
	public static void syncRemoteFile2Local(String remoteDir, String localDir, String uploadDir, String batchTranGroup, String taskId) {
		// 本地无local_dir_code 目录则自动创建，创建失败需要抛出
		File dirFile = new File(localDir);
		if (!dirFile.exists()) {
			if (!dirFile.mkdirs())
				throw DpModuleError.DpTrans.TS020083(localDir);
		}

		// 获取远程 *.OK 列表中本地不存在的清单
		ApRemoteFileList apRemoteFileList = getRemoteFileList(remoteDir, FILE_OK);

		for (String fileNames : apRemoteFileList.getFile_name()) {
			File file = new File(getFileFullPath(localDir, fileNames.substring(0, fileNames.lastIndexOf(FILE_OK))));
			if (!file.exists()) {
				bizLog.method("copyfile begin >>>>>>>>>>>>>>>>>>>>");

				// 获取服务器文件路径
				String remoteFileName = getFileFullPath(remoteDir, file.getName());

				// 获取本地文件路径
				String localFileName = getFileFullPath(localDir, file.getName());
				bizLog.parm("localFileName [%s],  remoteFileName  [%s]", localFileName, remoteFileName);

				// 文件复制
				try {
					FileUtil.copyFile(remoteFileName, localFileName);
				}
				catch (Exception e) {
					bizLog.error("文件复制失败，remoteFileName[%s],localFileName[%s], 错误原因[%s]", remoteFileName, localFileName, e.getMessage());
				}
				bizLog.method("copyfile end <<<<<<<<<<<<<<<<<<<<");
				// 初始化文件信息表
				knb_file_info tblFileInfo = SysUtil.getInstance(knb_file_info.class);
				String batchId = BatchUtil.getTaskId();
				tblFileInfo.setBtchno(taskId);
				tblFileInfo.setBusseq(batchId);
				tblFileInfo.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());
				tblFileInfo.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
				tblFileInfo.setDownph(remoteDir);
				tblFileInfo.setDownna(file.getName());
				tblFileInfo.setUpfeph(uploadDir);
				tblFileInfo.setUpfena(file.getName().concat("_RET"));
				tblFileInfo.setLocaph(localDir);
				tblFileInfo.setBtfest(E_BTFEST.DOWNSUCC);

				// 写入数据
				Knb_file_infoDao.insert(tblFileInfo);

				// 调用批量组
				DataArea dataArea = DataArea.buildWithEmpty();
				dataArea.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE, CommTools.getBaseRunEnvs().getTrxn_date());
				dataArea.getCommReq().setString("trxn_branch", CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
				dataArea.getCommReq().setString("corpno", CommTools.getBaseRunEnvs().getBusi_org_id()); // 交易法人
				dataArea.getInput().setString("filesq", taskId);

				bizLog.debug("--------------------调起批量开始--------------------");
				BatchUtil.submitAndRunBatchTranGroup(batchId, batchTranGroup, dataArea);
				bizLog.debug("--------------------调起批量结束--------------------");
			}
		}
	}

	/**
	 * @Author songlw
	 *         <p>
	 *         <li>2019年10月22日-下午10:08:30</li>
	 *         <li>功能说明：获取远程路径</li>
	 *         </p>
	 * @param remoteDir
	 * @param fileRegs
	 * @return
	 */
	public static ApRemoteFileList getRemoteFileList(String remoteDir, String fileRegs) {
		bizLog.method("getRemoteFileList begin >>>>>>>>>>>>>>>>>>>>");
		bizLog.parm(" remoteDir[%s],  fileRegs [%s]", remoteDir, fileRegs);

		ApRemoteFileList apRemoteFileList = SysUtil.getInstance(ApRemoteFileList.class);
		List<String> listNames = new ArrayList<String>();

		File file = new File(remoteDir);
		File[] tempList = file.listFiles();
		if (CommUtil.isNotNull(tempList)) {
			for (int i = 0; i < tempList.length; i++) {
				if (tempList[i].isFile() && tempList[i].getName().endsWith(fileRegs)) {
					bizLog.debug("getRemoteFileList >>>>>>>>>>>>>>>>>>>> 远程路径[%s]下文件[%s]", remoteDir, tempList[i].getName());
					String fllength = tempList[i].getName().substring(tempList[i].getName().indexOf('.', 1));
					int num = fllength.length();//得到后缀名长度
					String fileOtherName = tempList[i].getName().substring(0, tempList[i].getName().length() - num).concat(".OK");//得到文件名。去掉了后缀
					listNames.add(fileOtherName);
				}
			}
			apRemoteFileList.getFile_name().addAll(listNames);
		}
		bizLog.method("getRemoteFileList end >>>>>>>>>>>>>>>>>>>>");
		return apRemoteFileList;
	}

	/**
	 * @Author songlw
	 *         <p>
	 *         <li>2019年10月22日-下午9:42:13</li>
	 *         <li>功能说明：获取路径</li>
	 *         </p>
	 * @param rootDir
	 * @param fileName
	 * @return
	 */
	public static String getFileFullPath(String rootDir, String fileName) {
		bizLog.method("getFileFullPath begin >>>>>>>>>>>>>>>>>>>>");
		bizLog.parm(" rootDir[%s],  fileName [%s]", rootDir, fileName);

		String sFullPath = FileUtil.getFullPath(rootDir, fileName);

		bizLog.parm("sFullPath [%s]", sFullPath);
		bizLog.method("getFileFullPath end >>>>>>>>>>>>>>>>>>>>");
		return sFullPath;
	}

}
