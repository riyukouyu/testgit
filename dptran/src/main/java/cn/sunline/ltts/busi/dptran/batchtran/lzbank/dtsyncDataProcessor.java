
package cn.sunline.ltts.busi.dptran.batchtran.lzbank;

import cn.sunline.edsp.base.lang.*;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;

import cn.sunline.edsp.base.file.FileDataExecutor;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_HXSTAT;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCkfi;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCkfiDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.DBTools;
import java.io.File;
import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.FileUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;

/**
 * 数据同步（对平）
 * 
 * @author
 * @Date
 */

public class dtsyncDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.edsp.busi.dptran.batchtran.lzbank.intf.Dtsync.Input, cn.sunline.edsp.busi.dptran.batchtran.lzbank.intf.Dtsync.Property> {
	private static final String fileSepa1 = "\\|";// 文件分隔符
	private static String encoding;
	private static BizLog biz = BizLogUtil.getBizLog(dtsyncDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.edsp.busi.dptran.batchtran.lzbank.intf.Dtsync.Input input, cn.sunline.edsp.busi.dptran.batchtran.lzbank.intf.Dtsync.Property property) {
		String filedt = new BigDecimal(CommTools.getBaseRunEnvs().getComputer_date()).subtract(new BigDecimal(1)).toString();
		String filePath = null;
		String fileOK = null;
		String fileName = null;
		KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "file", "dtsync", "SYNC", true);
		encoding = para.getParm_value5();
		filePath = para.getParm_value1() + filedt + "/" + para.getParm_value2() + para.getParm_value3();
		fileOK = filePath + ".ok";
		fileName = para.getParm_value2() + para.getParm_value3();
		KnlCkfi knlckfi = KnlCkfiDao.selectOne_odb3(filedt, fileName, false);
		if (CommUtil.isNotNull(knlckfi) && knlckfi.getIsmkfi() == E_YES___.YES) {
			return;
		}
		File fileok = new File(fileOK);
		if (!fileok.exists() || !fileok.isFile()) {
			return;
		}
		biz.debug("<<<<<<<<<<获取文件路径>>>>>>>>>>>>>>>>>>" + fileName);
		final ByRef<Long> tolnnm = new ByRef<Long>(0L); // 应处理总笔数
		final ByRef<Long> counts = new ByRef<Long>(0L); // 实际处理总笔数
		biz.debug("<<<<<<<<<<开始读取文件>>>>>>>>>>>>>>>>>>");
		//读取数据同步文件
		try {
			FileUtil.readFile(filePath, new FileDataExecutor() {
				@Override
				public void process(int arg0, String arg1) {
					//行信息
					try {
						String[] line = arg1.split(fileSepa1);
						if (arg0 == 1) {
							//读取文件头
							if (line.length != 2) {
								throw DpModuleError.DpstComm.E9999("数据同步文件第1行字段数量不符，文件异常。");
							}
							tolnnm.value = Long.parseLong(line[0]);//应处理总笔数
						}
						if (arg0 > 1) {
							String mntrsq = line[0];//交易流水号
							String trandt = line[1];//交易日期
							E_HXSTAT hxstat = CommUtil.toEnum(E_HXSTAT.class, line[2]);//对账状态
							//读取对平数据，并登记
							KnlIoblCups entity = KnlIoblCupsDao.selectOne_odb2(mntrsq, trandt, true);
							if (hxstat != E_HXSTAT.SUCC) {
								return;
							}
							if (CommUtil.isNotNull(entity)) {
								entity.setHxstat(hxstat);
								KnlIoblCupsDao.updateOne_odb2(entity);
								counts.value++;
							}
						}
						DBTools.commit();
					}
					catch (Exception e) {
						counts.setValue(-1L);
					}
				}
			}, encoding);
		}
		catch (Exception e) {
			biz.debug("<<<<<<<<<<<<<<<<<数据同步文件读取异常>>>>>>>>>>>>>>>>>");
			if (CommUtil.isNull(knlckfi)) {
				KnlCkfi entity = SysUtil.getInstance(KnlCkfi.class);
				entity.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
				entity.setTrandt(CommTools.getBaseRunEnvs().getComputer_date());// 交易日期
				entity.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 渠道流水
				entity.setServdt(CommTools.getBaseRunEnvs().getComputer_date());// 渠道日期
				entity.setChckdt(filedt);// 对账日期
				entity.setIsmkfi(E_YES___.NO);// 文件是否读取
				entity.setFimkdt(filedt);// 文件生成日期
				entity.setFipath(para.getParm_value1() + filedt);// 文件路径
				entity.setFiname(para.getParm_value2() + para.getParm_value3());// 文件名称
				KnlCkfiDao.insert(entity);
			}
			biz.method("<<<<<<<<<<<<<<<<<数据同步文件读取异常>>>>>>>>>>>>>>>>>");
			return;
		}
		biz.debug("<<<<<<<<<<文件读取结束>>>>>>>>>>>>>>>>>>");
		// 判断实际处理与文件总是否相符
		if (CommUtil.compare(tolnnm.value, counts.value) != 0) {
			biz.debug("<<<<<<<<<<读取对账数据同步总记录数[\" " + tolnnm.value + " \"]与实际处理记录数[\" " + counts.value + " \"]不符！>>>>>>>>>>>>>>>>>>");
			return;
		}
		/**
		 * 文件读取记录状态
		 */
		if (CommUtil.isNull(knlckfi)) {
			KnlCkfi entity = SysUtil.getInstance(KnlCkfi.class);
			entity.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
			entity.setTrandt(CommTools.getBaseRunEnvs().getComputer_date());// 交易日期
			entity.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 渠道流水
			entity.setServdt(CommTools.getBaseRunEnvs().getComputer_date());// 渠道日期
			entity.setChckdt(filedt);// 对账日期
			entity.setIsmkfi(E_YES___.YES);// 文件是否读取
			entity.setFimkdt(filedt);// 文件生成日期
			entity.setFipath(para.getParm_value1() + filedt);// 文件路径
			entity.setFiname(para.getParm_value2() + para.getParm_value3());// 文件名称
			KnlCkfiDao.insert(entity);
		}
		else {
			knlckfi.setIsmkfi(E_YES___.YES);// 文件是否生成
			KnlCkfiDao.updateOne_odb2(knlckfi);
		}
	}
}
