package cn.sunline.ltts.busi.dptran.batchtran.lzbank;

import cn.sunline.edsp.base.lang.*;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;

import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_VAFITY;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.ca.namedsql.CaBatchTransDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCkfi;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCkfiDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdm;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmContro;
import cn.sunline.ltts.busi.dp.type.DpTransfer.DpCount;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;

/**
 * 代发对账文件生成
 * 
 * @author
 * @Date
 */

public class dffileDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.edsp.busi.dptran.batchtran.lzbank.intf.Dffile.Input, cn.sunline.edsp.busi.dptran.batchtran.lzbank.intf.Dffile.Property> {
	private static final String fileSepa1 = "~";// 文件分隔符
	private static BizLog biz = BizLogUtil.getBizLog(dffileDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.edsp.busi.dptran.batchtran.lzbank.intf.Dffile.Input input, cn.sunline.edsp.busi.dptran.batchtran.lzbank.intf.Dffile.Property property) {
		biz.method("<<<<<<<<<<<<<<<<<代发对账文件开始>>>>>>>>>>>>>>>>>");
		String lastdt = new BigDecimal(CommTools.getBaseRunEnvs().getComputer_date()).subtract(new BigDecimal(1)).toString();
		String path = null;
		String fileName = null;
		String namedsql = null;
		String namersql = null;
		Params params = new Params();
		params.put("trandt", lastdt);
		KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "file", "dffile", E_VAFITY.EM.getId(), true);
		// 获取文件参数
		DpCount maps = CaBatchTransDao.selKnlIoblEdmAll(lastdt, false);// 获取总计及总金额
		path = para.getParm_value1() + lastdt;
		String encoding = para.getParm_value5();// 文件编码
		fileName = para.getParm_value2() + para.getParm_value3();
		String fileNameRele = para.getParm_value2() + "_rele" + para.getParm_value3();
		KnlCkfi knlckfi = KnlCkfiDao.selectOne_odb4(lastdt, fileName, fileNameRele, false);
		if (CommUtil.isNotNull(knlckfi) && knlckfi.getIsmkfi() == E_YES___.YES) {
			return;
		}
		String fileNameOK = para.getParm_value4();
		biz.debug("<<<<<<<<<<<<<<<<<对账文件名>>>>>>>>>>>>>>>>>" + fileName);
		namedsql = CaBatchTransDao.namedsql_selKnlIoblEdm;
		final LttsFileWriter file = new LttsFileWriter(path, fileName, encoding);
		final LttsFileWriter fileOk = new LttsFileWriter(path, fileNameOK, encoding);
		fileOk.open();
		file.open();
		biz.debug("<<<<<<<<<<<<<<<<<开始写入文件>>>>>>>>>>>>>>>>>");
		StringBuffer lineInfo1 = SysUtil.getInstance(StringBuffer.class);
		lineInfo1.append(maps.getCounts()).append(fileSepa1)// 总计
				.append(maps.getTranam()).append(fileSepa1);// 总金额
		final LttsFileWriter fileRele = new LttsFileWriter(path, fileNameRele, encoding);
		file.write(lineInfo1.toString());
		try {
			DaoUtil.selectList(namedsql, params, new CursorHandler<KnlIoblEdm>() {
				@Override
				public boolean handle(int arg0, KnlIoblEdm arg1) {
					StringBuffer lineInfo = SysUtil.getInstance(StringBuffer.class);
					lineInfo.append(arg1.getOrgaid() == null ? "" : arg1.getOrgaid()).append(fileSepa1);// 机构号
					lineInfo.append(arg1.getServtp() == null ? "" : arg1.getServtp()).append(fileSepa1);// 接出渠道
					lineInfo.append(arg1.getTranam() == null ? "" : arg1.getTranam()).append(fileSepa1);// 交易金额
					lineInfo.append(arg1.getAcctid() == null ? "" : arg1.getAcctid()).append(fileSepa1);// 服务商id/商户id
					lineInfo.append(arg1.getTranst() == null ? "" : arg1.getTranst()).append(fileSepa1);// 核心交易状态
					lineInfo.append(arg1.getSabkna() == null ? "" : arg1.getSabkna()).append(fileSepa1);// 结算卡银行名称
					lineInfo.append(arg1.getSacdno() == null ? "" : arg1.getSacdno()).append(fileSepa1);// 结算卡账号
					lineInfo.append(arg1.getTmsacdno() == null ? "" : arg1.getTmsacdno()).append(fileSepa1);// 脱敏结算卡账号
					lineInfo.append(arg1.getSacdna() == null ? "" : arg1.getSacdna()).append(fileSepa1);// 结算卡账号名称
					lineInfo.append(arg1.getTmsacdna() == null ? "" : arg1.getTmsacdna()).append(fileSepa1);// 脱敏结算卡账号名称
					lineInfo.append(arg1.getMntrsq() == null ? "" : arg1.getMntrsq()).append(fileSepa1);// 代发流水
					lineInfo.append(arg1.getTrandt() == null ? "" : arg1.getTrandt()).append(fileSepa1);// 代发交易日期
					lineInfo.append(arg1.getMntrtm() == null ? "" : arg1.getMntrtm()).append(fileSepa1);// 代发交易时间
					lineInfo.append(arg1.getRtcode() == null ? "" : arg1.getRtcode()).append(fileSepa1);// 应答码
					lineInfo.append(arg1.getRetmsg() == null ? "" : arg1.getRetmsg()).append(fileSepa1);// 应答描述
					lineInfo.append(arg1.getBankno() == null ? "" : arg1.getBankno());// 联行号
					file.write(lineInfo.toString());
					return true;
				}
			});
			//第二个文件生成分隔符
			namersql = CaBatchTransDao.namedsql_selKnlIoblEdmContro;
			fileRele.open();
			DpCount mapt = CaBatchTransDao.selKnlIoblEdmControALL(lastdt, false);// 获取总计
			StringBuffer lineInfo2 = SysUtil.getInstance(StringBuffer.class);
			lineInfo2.append(mapt.getCounts()).append(fileSepa1);// 总计	
			fileRele.write(lineInfo2.toString());
			DaoUtil.selectList(namersql, params, new CursorHandler<KnlIoblEdmContro>() {
				@Override
				public boolean handle(int arg0, KnlIoblEdmContro arg1) {
					StringBuffer lineInfo = SysUtil.getInstance(StringBuffer.class);
					lineInfo.append(arg1.getRetrsq() == null ? "" : arg1.getRetrsq()).append(fileSepa1);// 收单交易流水
					lineInfo.append(arg1.getRetrdt() == null ? "" : arg1.getRetrdt()).append(fileSepa1);// 收单交易日期
					lineInfo.append(arg1.getMntrsq() == null ? "" : arg1.getMntrsq()).append(fileSepa1);// 代发交易流水
					lineInfo.append(arg1.getTrandt() == null ? "" : arg1.getTrandt());// 代发交易日期
					fileRele.write(lineInfo.toString());
					return true;
				}
			});
			fileOk.write("OK");
		}
		catch (Exception e) {
			biz.debug("<<<<<<<<<<<<<<<<<代发对账文件生成异常>>>>>>>>>>>>>>>>>");
			if (CommUtil.isNull(knlckfi)) {
				KnlCkfi entity = SysUtil.getInstance(KnlCkfi.class);
				entity.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
				entity.setTrandt(CommTools.getBaseRunEnvs().getComputer_date());// 交易日期
				entity.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 渠道流水
				entity.setServdt(CommTools.getBaseRunEnvs().getComputer_date());// 渠道日期
				entity.setFilety(E_VAFITY.EM);// 文件类型
				entity.setChckdt(lastdt);// 对账日期
				entity.setIsmkfi(E_YES___.NO);// 文件是否生成
				entity.setFimkdt(lastdt);// 文件生成日期
				entity.setFipath(path);// 文件路径
				entity.setFiname(fileName);// 文件名称
				entity.setFlname(fileNameRele);// 文件名称
				KnlCkfiDao.insert(entity);
			}
			fileOk.close();
			fileOk.remove();
			return;
		}
		finally {
			file.close();
			fileRele.close();
			fileOk.close();
		}

		/**
		 * 文件生成记录状态
		 */
		if (CommUtil.isNull(knlckfi)) {
			KnlCkfi entity = SysUtil.getInstance(KnlCkfi.class);
			entity.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
			entity.setTrandt(CommTools.getBaseRunEnvs().getComputer_date());// 交易日期
			entity.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 渠道流水
			entity.setServdt(CommTools.getBaseRunEnvs().getComputer_date());// 渠道日期
			entity.setFilety(E_VAFITY.EM);// 文件类型
			entity.setChckdt(lastdt);// 对账日期
			entity.setIsmkfi(E_YES___.YES);// 文件是否生成
			entity.setFimkdt(lastdt);// 文件生成日期
			entity.setFipath(path);// 文件路径
			entity.setFiname(fileName);// 文件名称
			entity.setFlname(fileNameRele);// 文件名称
			KnlCkfiDao.insert(entity);
		}
		else {
			knlckfi.setIsmkfi(E_YES___.YES);// 文件是否生成
			KnlCkfiDao.updateOne_odb2(knlckfi);
		}
		biz.method("<<<<<<<<<<<<<<<<<代发对账文件结束>>>>>>>>>>>>>>>>>");
	}

}
