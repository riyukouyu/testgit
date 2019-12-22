
package cn.sunline.edsp.busi.dptran.batchtran;

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
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.adp.core.exception.AdpBusinessException;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.DBTools;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.ApKnpParameter;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApRemoteFileList;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

/**
 * 定时扫描代发回盘文件
 * 
 * @author
 * @Date
 */

public class spfileDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.edsp.busi.dptran.batchtran.intf.Spfile.Input, cn.sunline.edsp.busi.dptran.batchtran.intf.Spfile.Property> {
	// 初始化日志信息
	private static final BizLog bizLog = BizLogUtil.getBizLog(spfileDataProcessor.class);
	private static final String FILE_OK = ".OK";

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.edsp.busi.dptran.batchtran.intf.Spfile.Input input,
			cn.sunline.edsp.busi.dptran.batchtran.intf.Spfile.Property property) {
		try {
			this.syncRemoteFile2Local();
		} catch (AdpBusinessException e) {
			bizLog.debug("LttsBusinessException e[5s]", e);
			DBTools.rollback();
		}
	}

	/**
	 * @Author songlw
	 *         <p>
	 *         <li>2019年10月22日-下午9:32:41</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 */
	public void syncRemoteFile2Local() {
		bizLog.error(" syncRemoteFile2Local begin >>>>>>>>>>>>>>>>");

		// 远程服务器是系统缺省的文件服务器
		String remoteDir = ApKnpParameter.getKnpParameter("DP.FILEPATH", "SPFILE").getParm_value1();
		bizLog.debug("----------------remoteDir[%s]----------------", remoteDir);
		String localPath = ApKnpParameter.getKnpParameter("DP.FILEPATH", "SPFILE").getParm_value2();
		bizLog.debug("----------------localPath[%s]----------------", localPath);
		String localDir = localPath;

		// 本地无local_dir_code 目录则自动创建，创建失败需要抛出
		File dirFile = new File(localDir);
		if (!dirFile.exists()) {
			if (!dirFile.mkdirs())
				throw GlError.GL.E0104(localDir);
		}

		// 获取远程 *.ok 列表中本地不存在的清单
		ApRemoteFileList apRemoteFileList = getRemoteFileList(remoteDir, FILE_OK);

		for (String fileNames : apRemoteFileList.getFile_name()) {
			File file = new File(getFileFullPath(localDir, fileNames.substring(0, fileNames.lastIndexOf(FILE_OK)).concat(".txt")));
			if (!file.exists()) {
				bizLog.method("copyfile begin >>>>>>>>>>>>>>>>>>>>");

				// 获取服务器文件路径
				String remoteFileName = getFileFullPath(remoteDir, file.getName());

				// 获取本地文件路径
				String localFileName = getFileFullPath(localPath, file.getName());
				bizLog.parm("localFileName [%s],  remoteFileName  [%s]", localFileName, remoteFileName);

				// 文件复制
				try {
					FileUtil.copyFile(remoteFileName, localFileName);
				}
				catch (Exception e) {
					bizLog.error("文件复制失败，remoteFileName[%s],localFileName[%s], 错误原因[%s]", remoteFileName, localFileName, e.getMessage());
				}
				bizLog.method("copyfile end <<<<<<<<<<<<<<<<<<<<");
				String taskId = BatchUtil.getTaskId();
				// 初始化文件信息表
				kapb_wjplxxb tblWjplxxb = SysUtil.getInstance(kapb_wjplxxb.class);

				tblWjplxxb.setBtchno(taskId);
				tblWjplxxb.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());
				tblWjplxxb.setUpfeph(localPath);
				tblWjplxxb.setUpfena(file.getName());
				tblWjplxxb.setDownph(remoteDir);
				tblWjplxxb.setDownna(file.getName());
				tblWjplxxb.setBtfest(E_BTFEST.DOWNSUCC);

				Kapb_wjplxxbDao.insert(tblWjplxxb);
				// 调用批量组
				DataArea dataArea = DataArea.buildWithEmpty();
				dataArea.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE,
						CommTools.getBaseRunEnvs().getTrxn_date());
				dataArea.getCommReq().setString("trxn_branch", CommTools.getBaseRunEnvs().getTrxn_branch()); // 交易机构
				dataArea.getCommReq().setString("corpno", CommTools.getBaseRunEnvs().getBusi_org_id()); // 交易法人

				dataArea.getInput().setString("filesq", taskId);
				bizLog.debug("--------------------调起批量ST1004开始--------------------");
				BatchUtil.submitAndRunBatchTranGroup(taskId, "ST1004", dataArea);
				bizLog.debug("--------------------调起批量ST1004结束--------------------");
			}
		}
		bizLog.error(" ApFile.syncRemoteFile2Local end <<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author songlw
	 *         <p>
	 *         <li>2019年10月22日-下午10:08:30</li>
	 *         <li>功能说明：使用list的循环</li>
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
					bizLog.debug("getRemoteFileList >>>>>>>>>>>>>>>>>>>> 远程路径[%s]下文件[%s]", remoteDir,
							tempList[i].getName().concat(".txt"));
					String fllength	=tempList[i].getName().substring(tempList[i].getName().indexOf('.',1));
					int num=fllength.length();//得到后缀名长度
				    String fileOtherName=tempList[i].getName().substring(0, tempList[i].getName().length()-num).concat(".OK");//得到文件名。去掉了后缀
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
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param rootDir
	 * @param fileName
	 * @return
	 */
	public String getFileFullPath(String rootDir, String fileName) {
		bizLog.method("getFileFullPath begin >>>>>>>>>>>>>>>>>>>>");
		bizLog.parm(" rootDir[%s],  fileName [%s]", rootDir, fileName);

		String sFullPath = FileUtil.getFullPath(rootDir, fileName);

		bizLog.parm("sFullPath [%s]", sFullPath);
		bizLog.method("getFileFullPath end >>>>>>>>>>>>>>>>>>>>");
		return sFullPath;
	}
}
