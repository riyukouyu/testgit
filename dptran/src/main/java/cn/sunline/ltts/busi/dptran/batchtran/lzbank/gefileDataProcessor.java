package cn.sunline.ltts.busi.dptran.batchtran.lzbank;

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
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_VAFITY;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.ca.namedsql.CaBatchTransDao;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrDetl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCkfi;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCkfiDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdm;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmContro;
import cn.sunline.ltts.busi.dp.type.DpTransfer.DpCount;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * 
 * @author sunzy
 *
 */
public class gefileDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Gefile.Input, cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Gefile.Property> {
	private static final String fileSepa1 = "~";// 文件分隔符
	private static BizLog biz = BizLogUtil.getBizLog(gefileDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Gefile.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Gefile.Property property) {

		String servsq = input.getServsq();// 渠道流水
		String servdt = input.getServdt();// 渠道日期
		E_VAFITY rqfity = input.getFilety(); // 对账文件类型
		String trandt = input.getChckdt(); // 对账日期
		String path = null;
		String fileName = null;
		String namedsql = null;
		// 查询条件
		Params params = new Params();
		params.put("trandt", trandt);
		if (rqfity == E_VAFITY.CTHX) { // 传统核心对账
			genCoreFile(params, trandt, namedsql);
		} else if (rqfity == E_VAFITY.DE) { // 大额对账
			genLargeAmountFile(params, trandt);
		} else if (rqfity == E_VAFITY.SE) {// 小额渠道
			genSmallAmountFile(params, trandt);
		} else if (rqfity == E_VAFITY.UNION) {// 银联收单对账文件
			genUnionPayFile(params, trandt, rqfity);
		} else if (rqfity == E_VAFITY.EM) {// 银联代发对账
			genUnionAllChannel(params, trandt, rqfity);
		}

		/**
		 * 修改文件生成记录状态
		 */
		KnlCkfi tblKnlCkfi = KnlCkfiDao.selectOne_odb2(servsq, servdt, false);
		if (CommUtil.isNull(tblKnlCkfi)) {
			KnlCkfi entity = SysUtil.getInstance(KnlCkfi.class);
			entity.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
			entity.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
			entity.setServsq(servsq);// 渠道流水
			entity.setServdt(servdt);// 渠道日期
			entity.setTaskid("");// 文件批量ID
			entity.setFilety(rqfity);// 文件类型
			entity.setChckdt(trandt);// 对账日期
			entity.setIsmkfi(E_YES___.YES);// 文件是否生成
			entity.setFimkdt(DateTools2.getDateInfo().getSystdt());// 文件生成日期
			entity.setFipath(path);// 文件路径
			entity.setFiname(fileName);// 文件名称
			KnlCkfiDao.insert(entity);
		} else {
			tblKnlCkfi.setIsmkfi(E_YES___.YES);// 文件是否生成
			tblKnlCkfi.setFimkdt(DateTools2.getDateInfo().getSystdt());// 文件生成日期
			tblKnlCkfi.setFipath(path);// 文件路径
			tblKnlCkfi.setFiname(fileName);// 文件名称
			KnlCkfiDao.updateOne_odb2(tblKnlCkfi);
		}
	}

	/**
	 * 生成核心对账文件
	 * 
	 * @param path
	 * @param params
	 * @param trandt
	 * @param fileName
	 * @param namedsql
	 */
	private static void genCoreFile(Params params, String trandt, String namedsql) {
		// 获取文件参数
		KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "file", "gefile", "CTHX", true);
		String path = para.getParm_value1();
		String encoding = para.getParm_value5();// 文件编码
		String servtp = "IM";// 文件编码
		params.put("servtp", servtp);
		String fileName = para.getParm_value2() + trandt + para.getParm_value3();
		biz.debug("<<<<<<<<<<<<<<<<<对账文件名>>>>>>>>>>>>>>>>>" + fileName);
		DpCount maps = CaBatchTransDao.selKnlIoblAll(trandt, servtp, false);
		final LttsFileWriter file = new LttsFileWriter(path, fileName, encoding);
		file.open();
		biz.debug("<<<<<<<<<<<<<<<<<开始写入文件>>>>>>>>>>>>>>>>>");
		StringBuffer lineInfo1 = SysUtil.getInstance(StringBuffer.class);
		lineInfo1.append(maps.getCounts()).append(fileSepa1)// 总计
				.append(maps.getTranam()).append(fileSepa1);// 总金额
		file.write(lineInfo1.toString());
		try {
			DaoUtil.selectList(CaBatchTransDao.namedsql_selKnlIobl, params, new CursorHandler<KnlIobl>() {
				@Override
				public boolean handle(int arg0, KnlIobl arg1) {
					StringBuffer lineInfo = SysUtil.getInstance(StringBuffer.class);
					lineInfo.append(arg1.getCapitp()).append(fileSepa1);// 转账交易类型
					lineInfo.append(arg1.getToacct()).append(fileSepa1);// 转出方账号/卡号
					lineInfo.append("").append(fileSepa1);// 转出方子账号类型
					lineInfo.append(arg1.getToacno()).append(fileSepa1);// 转出方子账号
					lineInfo.append("").append(fileSepa1);// 转出方户名
					lineInfo.append(arg1.getTobrch()).append(fileSepa1);// 转出方账号所属机构
					lineInfo.append(arg1.getCrcycd()).append(fileSepa1);// 币种
					lineInfo.append("").append(fileSepa1);// 钞汇标志
					lineInfo.append(arg1.getTranam()).append(fileSepa1);// 交易金额
					lineInfo.append("").append(fileSepa1);// 摘要码
					lineInfo.append(arg1.getCardno()).append(fileSepa1);// 转入方账号/卡号
					lineInfo.append("").append(fileSepa1);// 转入方子账号类型
					lineInfo.append(arg1.getAcctno()).append(fileSepa1);// 转入方子账号
					lineInfo.append("").append(fileSepa1);// 转入方户名
					lineInfo.append(arg1.getBrchno()).append(fileSepa1);// 转入方账号所属机构
					lineInfo.append(arg1.getTlcgam()).append(fileSepa1);// 收费总额
					lineInfo.append(arg1.getChckdt()).append(fileSepa1);// 对账日期
					lineInfo.append(arg1.getKeepdt()).append(fileSepa1);// 清算日期
					lineInfo.append("").append(fileSepa1);// 备用字段1
					lineInfo.append("").append(fileSepa1);// 备用字段2
					lineInfo.append(arg1.getServdt()).append(fileSepa1);// 支付前置日期
					lineInfo.append(arg1.getServsq()).append(fileSepa1);// 支付前置流水号
					lineInfo.append(arg1.getTransq()).append(fileSepa1);// 核心流水
					lineInfo.append(arg1.getTrandt()).append(fileSepa1);// 交易日期
					lineInfo.append("TCORE").append(fileSepa1);// 支付交易渠道 TCORE-传统核心对账数据
					file.write(lineInfo.toString());
					return true;
				}
			});
			DaoUtil.selectList(CaBatchTransDao.namedsql_selKnbRptrDetl, params, new CursorHandler<KnbRptrDetl>() {
				@Override
				public boolean handle(int arg0, KnbRptrDetl arg1) {
					StringBuffer lineInfo = SysUtil.getInstance(StringBuffer.class);
					lineInfo.append("").append(fileSepa1);// 转账交易类型
					lineInfo.append(arg1.getDebact()).append(fileSepa1);// 借方账号-转出方账号/卡号
					lineInfo.append(arg1.getDecstp() == null ? "" : arg1.getDecstp()).append(fileSepa1);// 借方子账户类型-转出方子账号类型
					lineInfo.append(arg1.getDeacct() == null ? "" : arg1.getDeacct()).append(fileSepa1);// 借方子账号-转出方子账号
					lineInfo.append("").append(fileSepa1);// 借方户名-转出方户名
					lineInfo.append(arg1.getDeborg()).append(fileSepa1);// 借方机构-转出方账号所属机构
					lineInfo.append(arg1.getCrcycd()).append(fileSepa1);// 货币代号
					lineInfo.append("").append(fileSepa1);// 钞汇标志
					lineInfo.append(arg1.getTranam()).append(fileSepa1);// 交易金额
					lineInfo.append("").append(fileSepa1);// 摘要码
					lineInfo.append(arg1.getCrdact()).append(fileSepa1);// 贷方账号-转入方账号/卡号
					lineInfo.append(arg1.getCrcstp() == null ? "" : arg1.getCrcstp()).append(fileSepa1);// 贷方子账户类型-转入方子账号类型
					lineInfo.append(arg1.getCracct() == null ? "" : arg1.getCracct()).append(fileSepa1);// 贷方子账号-转入方子账号
					lineInfo.append("").append(fileSepa1);// 贷方户名-转入方户名
					lineInfo.append(arg1.getCrdorg()).append(fileSepa1);// 贷方机构-转入方账号所属机构
					lineInfo.append("").append(fileSepa1);// 收费总额
					lineInfo.append(arg1.getChckdt()).append(fileSepa1);// 对账日期-对账日期
					lineInfo.append(arg1.getKeepdt()).append(fileSepa1);// 清算日期-清算日期
					lineInfo.append(arg1.getStady1()).append(fileSepa1);// 备用字段1
					lineInfo.append(arg1.getStady2()).append(fileSepa1);// 备用字段2
					lineInfo.append(arg1.getSourdt()).append(fileSepa1);// 来源方交易日期-支付前置日期
					lineInfo.append(arg1.getSoursq()).append(fileSepa1);// 来源方交易流水-支付前置流水号
					lineInfo.append(arg1.getTransq()).append(fileSepa1);// 主交易流水-核心流水
					lineInfo.append(arg1.getTrandt()).append(fileSepa1);// 交易日期
					lineInfo.append("GIFTMONY").append(fileSepa1);// 支付交易渠道 GIFTMONY-红包对账数据
					file.write(lineInfo.toString());
					return true;
				}
			});
		} finally {
			biz.debug("<<<<<<<<<<<<<<<<<文件写入完成>>>>>>>>>>>>>>>>>");
			file.close();
		}
	}

	/**
	 * 生成大额对账文件
	 * 
	 * @param path
	 * @param params
	 * @param trandt
	 * @param fileName
	 * @param namedsql
	 */
	private static void genLargeAmountFile(Params params, String trandt) {
		// 获取文件参数
		KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "file", "gefile", "DE", true);
		String subsys = para.getParm_value4();// 0-大额渠道
		String encoding = para.getParm_value5();// 文件编码
		params.put("subsys", subsys);
		String path = para.getParm_value1();
		String fileName = para.getParm_value2() + trandt + para.getParm_value3();
		biz.debug("<<<<<<<<<<<<<<<<<对账文件名>>>>>>>>>>>>>>>>>" + fileName);
		String namedsql = CaBatchTransDao.namedsql_selKnlCnapot;
		final LttsFileWriter file = new LttsFileWriter(path, fileName, encoding);
		DpCount maps = CaBatchTransDao.selKnlCnapotAll(trandt, subsys, false);
		file.open();
		biz.debug("<<<<<<<<<<<<<<<<<开始写入文件>>>>>>>>>>>>>>>>>");
		StringBuffer lineInfo1 = SysUtil.getInstance(StringBuffer.class);
		lineInfo1.append(maps.getCounts()).append(fileSepa1)// 总计
				.append(maps.getTranam()).append(fileSepa1);// 总金额
		file.write(lineInfo1.toString());
		try {
			DaoUtil.selectList(namedsql, params, new CursorHandler<KnlCnapot>() {
				@Override
				public boolean handle(int arg0, KnlCnapot arg1) {
					StringBuffer lineInfo = SysUtil.getInstance(StringBuffer.class);
					lineInfo.append(arg1.getSubsys()).append(fileSepa1);// 清算渠道
					lineInfo.append(arg1.getMsetdt()).append(fileSepa1);// 委托日期
					lineInfo.append(arg1.getMsetsq()).append(fileSepa1);// 交易序号
					lineInfo.append(arg1.getCrdbtg()).append(fileSepa1);// 借贷标志
					lineInfo.append("").append(fileSepa1);// 业务代码
					lineInfo.append("").append(fileSepa1);// 业务种类
					lineInfo.append(arg1.getMesgtp()).append(fileSepa1);// 报文编号
					lineInfo.append(arg1.getIotype()).append(fileSepa1);// 往来标志
					lineInfo.append(arg1.getCrcycd()).append(fileSepa1);// 币种
					lineInfo.append(arg1.getCstrfg()).append(fileSepa1);// 现转标志
					lineInfo.append(arg1.getCsextg()).append(fileSepa1);// 钞汇属性
					lineInfo.append(arg1.getPyercd()).append(fileSepa1);// 发起行行号
					lineInfo.append(arg1.getPyeecd()).append(fileSepa1);// 接收行行号
					lineInfo.append(arg1.getPyerac()).append(fileSepa1);// 付款人账号
					lineInfo.append(arg1.getPyerna()).append(fileSepa1);// 付款人名称
					lineInfo.append(arg1.getPyeeac()).append(fileSepa1);// 收款人账号
					lineInfo.append(arg1.getPyeena()).append(fileSepa1);// 收款人名称
					lineInfo.append(arg1.getPriotp()).append(fileSepa1);// 加急标志
					lineInfo.append(arg1.getAfeetg()).append(fileSepa1);// 收费标志
					lineInfo.append(arg1.getTranam()).append(fileSepa1);// 发生额
					lineInfo.append(arg1.getAfeeam()).append(fileSepa1);// 手续费
					lineInfo.append(arg1.getFeeam1()).append(fileSepa1);// 汇划费
					lineInfo.append(arg1.getChfcnb()).append(fileSepa1);// 对账分类编号
					lineInfo.append(arg1.getServdt()).append(fileSepa1);// 支付前置日期
					lineInfo.append(arg1.getServsq()).append(fileSepa1);// 支付前置流水号
					lineInfo.append(arg1.getBrchno()).append(fileSepa1);// 交易机构号
					lineInfo.append(arg1.getUserid()).append(fileSepa1);// 录入柜员
					lineInfo.append(arg1.getCkbkus()).append(fileSepa1);// 复合柜员
					lineInfo.append(arg1.getAuthus()).append(fileSepa1);// 授权柜员
					lineInfo.append(arg1.getKeepdt()).append(fileSepa1);// 清算日期
					lineInfo.append(arg1.getNpcpdt()).append(fileSepa1);// 轧差日期
					lineInfo.append(arg1.getNpcpbt()).append(fileSepa1);// 轧差场次
					lineInfo.append(arg1.getRemark1()).append(fileSepa1);// 备用字段1
					lineInfo.append(arg1.getRemark2()).append(fileSepa1);// 备用字段2
					lineInfo.append("").append(fileSepa1);// 摘要码
					lineInfo.append(arg1.getTransq()).append(fileSepa1);// 核心流水
					lineInfo.append(arg1.getTrandt()).append(fileSepa1);// 核心日期
					file.write(lineInfo.toString());
					return true;
				}
			});
		} finally {
			biz.debug("<<<<<<<<<<<<<<<<<文件写入完成>>>>>>>>>>>>>>>>>");
			file.close();
		}
	}

	/**
	 * 生成小额对账文件
	 * 
	 * @param path
	 * @param params
	 * @param trandt
	 * @param fileName
	 * @param namedsql
	 */
	private static void genSmallAmountFile(Params params, String trandt) {
		KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "file", "gefile", "SE", true);
		String subsys = para.getParm_value4();// 1-小额渠道
		String encoding = para.getParm_value5();// 文件编码
		params.put("subsys", subsys);
		DpCount maps = CaBatchTransDao.selKnlCnapotAll(trandt, subsys, false);// 查询总计及总金额
		// 获取文件参数
		String path = para.getParm_value1();
		String fileName = para.getParm_value2() + trandt + para.getParm_value3();
		biz.debug("<<<<<<<<<<<<<<<<<对账文件名>>>>>>>>>>>>>>>>>" + fileName);
		String namedsql = CaBatchTransDao.namedsql_selKnlCnapot;
		final LttsFileWriter file = new LttsFileWriter(path, fileName, encoding);
		file.open();
		biz.debug("<<<<<<<<<<<<<<<<<开始写入文件>>>>>>>>>>>>>>>>>");
		StringBuffer lineInfo1 = SysUtil.getInstance(StringBuffer.class);
		lineInfo1.append(maps.getCounts()).append(fileSepa1)// 总计
				.append(maps.getTranam()).append(fileSepa1);// 总金额
		file.write(lineInfo1.toString());
		try {
			DaoUtil.selectList(namedsql, params, new CursorHandler<KnlCnapot>() {
				@Override
				public boolean handle(int arg0, KnlCnapot arg1) {
					StringBuffer lineInfo = SysUtil.getInstance(StringBuffer.class);
					lineInfo.append(arg1.getSubsys()).append(fileSepa1);// 清算渠道
					lineInfo.append(arg1.getMsetdt()).append(fileSepa1);// 委托日期
					lineInfo.append(arg1.getMsetsq()).append(fileSepa1);// 交易序号
					lineInfo.append(arg1.getCrdbtg()).append(fileSepa1);// 借贷标志
					lineInfo.append("").append(fileSepa1);// 业务代码
					lineInfo.append("").append(fileSepa1);// 业务种类
					lineInfo.append(arg1.getMesgtp()).append(fileSepa1);// 报文编号
					lineInfo.append(arg1.getIotype()).append(fileSepa1);// 往来标志
					lineInfo.append(arg1.getCrcycd()).append(fileSepa1);// 币种
					lineInfo.append(arg1.getCstrfg()).append(fileSepa1);// 现转标志
					lineInfo.append(arg1.getCsextg()).append(fileSepa1);// 钞汇属性
					lineInfo.append(arg1.getPyercd()).append(fileSepa1);// 发起行行号
					lineInfo.append(arg1.getPyeecd()).append(fileSepa1);// 接收行行号
					lineInfo.append(arg1.getPyerac()).append(fileSepa1);// 付款人账号
					lineInfo.append(arg1.getPyerna()).append(fileSepa1);// 付款人名称
					lineInfo.append(arg1.getPyeeac()).append(fileSepa1);// 收款人账号
					lineInfo.append(arg1.getPyeena()).append(fileSepa1);// 收款人名称
					lineInfo.append(arg1.getPriotp()).append(fileSepa1);// 加急标志
					lineInfo.append(arg1.getAfeetg()).append(fileSepa1);// 收费标志
					lineInfo.append(arg1.getTranam()).append(fileSepa1);// 发生额
					lineInfo.append(arg1.getAfeeam()).append(fileSepa1);// 手续费
					lineInfo.append(arg1.getFeeam1()).append(fileSepa1);// 汇划费
					lineInfo.append(arg1.getChfcnb()).append(fileSepa1);// 对账分类编号
					lineInfo.append(arg1.getServdt()).append(fileSepa1);// 支付前置日期
					lineInfo.append(arg1.getServsq()).append(fileSepa1);// 支付前置流水号
					lineInfo.append(arg1.getBrchno()).append(fileSepa1);// 交易机构号
					lineInfo.append(arg1.getUserid()).append(fileSepa1);// 录入柜员
					lineInfo.append(arg1.getCkbkus()).append(fileSepa1);// 复合柜员
					lineInfo.append(arg1.getAuthus()).append(fileSepa1);// 授权柜员
					lineInfo.append(arg1.getKeepdt()).append(fileSepa1);// 清算日期
					lineInfo.append(arg1.getNpcpdt()).append(fileSepa1);// 轧差日期
					lineInfo.append(arg1.getNpcpbt()).append(fileSepa1);// 轧差场次
					lineInfo.append(arg1.getRemark1()).append(fileSepa1);// 备用字段1
					lineInfo.append(arg1.getRemark2()).append(fileSepa1);// 备用字段2
					lineInfo.append("").append(fileSepa1);// 摘要码
					lineInfo.append(arg1.getTransq()).append(fileSepa1);// 核心流水
					lineInfo.append(arg1.getTrandt()).append(fileSepa1);// 核心日期
					file.write(lineInfo.toString());
					return true;
				}
			});
		} finally {
			biz.debug("<<<<<<<<<<<<<<<<<文件写入完成>>>>>>>>>>>>>>>>>");
			file.close();
		}
	}

	/**
	 * 生成银联Cups对账文件
	 * 
	 * @param path
	 * @param params
	 * @param trandt
	 * @param fileName
	 * @param namedsql
	 */
	private static void genUnionPayFile(Params params, String trandt, E_VAFITY rqfity) {
		KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "file", "gefile", rqfity.getId(), true);
		// 获取文件参数
		DpCount maps = CaBatchTransDao.selKnlIoblCupsAll(trandt, false);// 获取总计及总金额
		String path = para.getParm_value1() + trandt;
		String encoding = para.getParm_value5();// 文件编码
		String fileName = para.getParm_value2() + para.getParm_value3();
		String fileNameOK = para.getParm_value4();
		biz.debug("<<<<<<<<<<<<<<<<<对账文件名>>>>>>>>>>>>>>>>>" + fileName);
		String namedsql = CaBatchTransDao.namedsql_selKnlIoblCups;
		// params.put("rqfity", rqfity.getValue());
		final LttsFileWriter file = new LttsFileWriter(path, fileName, encoding);
		file.open();
		//ok文件
		final LttsFileWriter fileOk = new LttsFileWriter(path, fileNameOK, encoding);
		fileOk.open();
		biz.debug("<<<<<<<<<<<<<<<<<开始写入文件>>>>>>>>>>>>>>>>>");
		StringBuffer lineInfo1 = SysUtil.getInstance(StringBuffer.class);
		lineInfo1.append(maps.getCounts()).append(fileSepa1)// 总计
				.append(maps.getTranam()).append(fileSepa1);// 总金额
		file.write(lineInfo1.toString());
		try {
			DaoUtil.selectList(namedsql, params, new CursorHandler<KnlIoblCups>() {
				@Override
				public boolean handle(int arg0, KnlIoblCups arg1) {
					StringBuffer lineInfo = SysUtil.getInstance(StringBuffer.class);
					lineInfo.append(arg1.getFlowid()).append(fileSepa1);// 自增id
					lineInfo.append(arg1.getServtp()).append(fileSepa1);// 支付渠道
					lineInfo.append(arg1.getUniseq()).append(fileSepa1);// 银联流水
					lineInfo.append(arg1.getUnkpdt()).append(fileSepa1);// 银联清算日期
					lineInfo.append(arg1.getMesstp()).append(fileSepa1);// 银联报文类型
					lineInfo.append(arg1.getTranam()).append(fileSepa1);// 交易金额
					lineInfo.append(arg1.getRefeno()).append(fileSepa1);// 参考号
					lineInfo.append(arg1.getOrdeno()).append(fileSepa1);// 订单号
					lineInfo.append(arg1.getCardno()).append(fileSepa1);// 交易卡号
					lineInfo.append(arg1.getInmeid()).append(fileSepa1);// 内部商户号
					lineInfo.append(arg1.getInmena()).append(fileSepa1);// 内部商户名称
					lineInfo.append(arg1.getTranst()).append(fileSepa1);// 核心交易状态
					lineInfo.append(arg1.getMntrsq()).append(fileSepa1);// 核心流水
					lineInfo.append(arg1.getTrandt()).append(fileSepa1);// 核心交易日期
					lineInfo.append(arg1.getMntrtm()).append(fileSepa1);// 核心交易时间
					lineInfo.append(arg1.getPrepsq()).append(fileSepa1);// posp流水
					lineInfo.append(arg1.getPrepdt()).append(fileSepa1);// posp日期
					lineInfo.append(arg1.getPreptm()).append(fileSepa1);// posp时间
					lineInfo.append(arg1.getSbrand()).append(fileSepa1);// posp品牌名称
					lineInfo.append(arg1.getBusitp()).append(fileSepa1);// 业务类型
					lineInfo.append(arg1.getTeleno()).append(fileSepa1);// 手机号
					lineInfo.append(arg1.getCardtp()).append(fileSepa1);// 卡类型
					lineInfo.append(arg1.getMerate()).append(fileSepa1);// 客户费率
					lineInfo.append(arg1.getJnanam()).append(fileSepa1);// 交易手续费
					lineInfo.append(arg1.getVoucfe()).append(fileSepa1);// 优惠卷抵扣
					lineInfo.append(arg1.getToanam()).append(fileSepa1);// 商户存入金额
					lineInfo.append(arg1.getFinsty()).append(fileSepa1);// 结算方式
					lineInfo.append(arg1.getAcfist()).append(fileSepa1);// 实际结算方式
					lineInfo.append(arg1.getChrgam()).append(fileSepa1);// 银联成本
					lineInfo.append(arg1.getAd01am()).append(fileSepa1);// 服务商分润金额
					lineInfo.append(arg1.getReflid()).append(fileSepa1);// 原自增id

					file.write(lineInfo.toString());
					return true;
				}
			});
			fileOk.write("OK");
		} finally {
			biz.debug("<<<<<<<<<<<<<<<<<写入文件完成>>>>>>>>>>>>>>>>>");
			file.close();
			fileOk.close();
		}
	}

	/**
	 * 生成银联cups对账文件
	 * 
	 * @param path
	 * @param params
	 * @param trandt
	 * @param fileName
	 * @param namedsql
	 */
	private static void genUnionPayCard(Params params, String trandt, E_VAFITY rqfity) {
		KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "file", "gefile", "YLCP", true);
		// 获取文件参数
		DpCount maps = CaBatchTransDao.selKnlIoblCupsAll(trandt, false);// 获取总计及总金额
		String path = para.getParm_value1();
		String encoding = para.getParm_value5();// 文件编码
		String fileName = para.getParm_value2() + trandt + para.getParm_value3();
		biz.debug("<<<<<<<<<<<<<<<<<对账文件名>>>>>>>>>>>>>>>>>" + fileName);
		String namedsql = CaBatchTransDao.namedsql_selKnlIoblCups;
		params.put("djtype", rqfity);
		final LttsFileWriter file = new LttsFileWriter(path, fileName, encoding);
		file.open();
		biz.debug("<<<<<<<<<<<<<<<<<开始写入文件>>>>>>>>>>>>>>>>>");
		StringBuffer lineInfo1 = SysUtil.getInstance(StringBuffer.class);
		lineInfo1.append(maps.getCounts()).append(fileSepa1)// 总计
				.append(maps.getTranam()).append(fileSepa1);// 总金额
		file.write(lineInfo1.toString());
		try {
			DaoUtil.selectList(namedsql, params, new CursorHandler<KnlIoblCups>() {
				@Override
				public boolean handle(int arg0, KnlIoblCups arg1) {
					StringBuffer lineInfo = SysUtil.getInstance(StringBuffer.class);
					lineInfo.append(arg1.getOrgaid()).append(fileSepa1);// 机构号
					lineInfo.append(arg1.getMesstp()).append(fileSepa1);// 报文类型
					lineInfo.append(arg1.getUniseq()).append(fileSepa1);// 银联流水
					lineInfo.append(arg1.getTranam()).append(fileSepa1);// 交易金额
					lineInfo.append(arg1.getRefeno()).append(fileSepa1);// 参考号或订单号
					lineInfo.append(arg1.getCardno()).append(fileSepa1);// 交易卡号
					// lineInfo.append(arg1.getOtacct()).append(fileSepa1);//转出账号
					// lineInfo.append(arg1.getOtacna()).append(fileSepa1);//转出账号户名
					// lineInfo.append(arg1.getInacct()).append(fileSepa1);// 转入账号
					// lineInfo.append(arg1.getInacna()).append(fileSepa1);// 转入账号户名
					lineInfo.append(arg1.getMntrsq()).append(fileSepa1);// 核心流水
					lineInfo.append(arg1.getTrandt());// 核心交易日期
					// lineInfo.append(arg1.getPrepsq()).append(fileSepa1);//银联前置流水
					// lineInfo.append(arg1.getCardno()).append(fileSepa1);//交易卡号
					// lineInfo.append(arg1.getDevcno()).append(fileSepa1);//设备号
					// lineInfo.append(arg1.getAmntcd()).append(fileSepa1);//借贷标志
					// lineInfo.append(arg1.getTranam()).append(fileSepa1);//交易金额
					// lineInfo.append(arg1.getPrdate()).append(fileSepa1);//银联前置日期
					// lineInfo.append(arg1.getCrcycd()).append(fileSepa1);//币种
					// lineInfo.append(arg1.getCnkpdt()).append(fileSepa1);//传统核心清算日期
					// lineInfo.append(arg1.getUnkpdt()).append(fileSepa1);//银联清算日期
					// lineInfo.append(arg1.getOtacct()).append(fileSepa1);//转出账号
					// lineInfo.append(arg1.getOtacna()).append(fileSepa1);//转出账号户名
					// lineInfo.append(arg1.getOtbrch()).append(fileSepa1);//转出机构号
					// lineInfo.append(arg1.getInacct()).append(fileSepa1);//转入账号
					// lineInfo.append(arg1.getInacna()).append(fileSepa1);//转入账号户名
					// lineInfo.append(arg1.getInbrch()).append(fileSepa1);//转入机构号
					// lineInfo.append(arg1.getTrbrch()).append(fileSepa1);//受理机构号
					// lineInfo.append(arg1.getPrdate()).append(fileSepa1);//前置交易日期
					// lineInfo.append(arg1.getPrbrmk()).append(fileSepa1);//代理机构标志码
					// lineInfo.append(arg1.getTrbrmk()).append(fileSepa1);//发送机构标志码
					// lineInfo.append(arg1.getTrcode()).append(fileSepa1);//银联交易码
					// lineInfo.append(arg1.getStand1()).append(fileSepa1);//32域
					// lineInfo.append(arg1.getStand2()).append(fileSepa1);//33域
					// lineInfo.append(arg1.getRetrdt()).append(fileSepa1);//设备交易日期时间
					// lineInfo.append(arg1.getResssq()).append(fileSepa1);//原系统跟踪号
					// lineInfo.append(arg1.getReprsq()).append(fileSepa1);//原前置流水
					// lineInfo.append(arg1.getServsq()).append(fileSepa1);//全渠道流水号
					// lineInfo.append(arg1.getChckno()).append(fileSepa1);//对账分类编号
					// lineInfo.append(arg1.getChrgam()).append(fileSepa1);//手续费金额
					// lineInfo.append(arg1.getBusino()).append(fileSepa1);//商户代码
					// lineInfo.append(arg1.getBusitp()).append(fileSepa1);//商户类型
					// lineInfo.append(arg1.getAuthno()).append(fileSepa1);//预授权标识码
					// lineInfo.append(arg1.getMesstp()).append(fileSepa1);//报文类型
					// lineInfo.append(arg1.getProccd()).append(fileSepa1);//处理码
					// lineInfo.append(arg1.getSpared()).append(fileSepa1);//备用
					// lineInfo.append(arg1.getFrondt()).append(fileSepa1);//支付前置日期
					// lineInfo.append(arg1.getFronsq()).append(fileSepa1);//支付前置流水号
					// lineInfo.append(arg1.getDjtype()).append(fileSepa1);//登记方式
					// lineInfo.append(arg1.getPrepty()).append(fileSepa1);//银联交易类型

					file.write(lineInfo.toString());
					return true;
				}
			});
		} finally {
			biz.debug("<<<<<<<<<<<<<<<<<写入文件完成>>>>>>>>>>>>>>>>>");
			file.close();
		}
	}

	/**
	 * 生成银联全渠道对账文件
	 * 
	 * @param path
	 * @param params
	 * @param trandt
	 * @param fileName
	 * @param namedsql
	 */
	private static void genUnionAllChannel(Params params, String trandt, E_VAFITY rqfity) {
		KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "file", "gefile", rqfity.getId(), true);
		// 获取文件参数
		DpCount maps = CaBatchTransDao.selKnlIoblEdmAll(trandt, false);// 获取总计及总金额
		String path = para.getParm_value1() + trandt;
		String encoding = para.getParm_value5();// 文件编码
		String fileName = para.getParm_value2() + para.getParm_value3();
		String fileNameOK = para.getParm_value4();
		biz.debug("<<<<<<<<<<<<<<<<<对账文件名>>>>>>>>>>>>>>>>>" + fileName);
		String namedsql = CaBatchTransDao.namedsql_selKnlIoblEdm;
		// params.put("rqfity", rqfity);
		final LttsFileWriter file = new LttsFileWriter(path, fileName, encoding);
		//ok文件
		final LttsFileWriter fileOk = new LttsFileWriter(path, fileNameOK, encoding);

		fileOk.open();
		file.open();
		biz.debug("<<<<<<<<<<<<<<<<<开始写入文件>>>>>>>>>>>>>>>>>");
		StringBuffer lineInfo1 = SysUtil.getInstance(StringBuffer.class);
		lineInfo1.append(maps.getCounts()).append(fileSepa1)// 总计
				.append(maps.getTranam()).append(fileSepa1);// 总金额
		file.write(lineInfo1.toString());
		try {
			DaoUtil.selectList(namedsql, params, new CursorHandler<KnlIoblEdm>() {
				@Override
				public boolean handle(int arg0, KnlIoblEdm arg1) {
					StringBuffer lineInfo = SysUtil.getInstance(StringBuffer.class);
					lineInfo.append(arg1.getOrgaid()).append(fileSepa1);// 机构号
					lineInfo.append(arg1.getServtp()).append(fileSepa1);// 接出渠道
					lineInfo.append(arg1.getTranam()).append(fileSepa1);// 交易金额
					lineInfo.append(arg1.getAcctid()).append(fileSepa1);// 服务商id/商户id
					lineInfo.append(arg1.getTranst()).append(fileSepa1);// 核心交易状态
					lineInfo.append(arg1.getSabkna()).append(fileSepa1);// 结算卡银行名称
					lineInfo.append(arg1.getSacdno()).append(fileSepa1);// 结算卡账号
					lineInfo.append(arg1.getSacdna()).append(fileSepa1);// 结算卡账号名称
					lineInfo.append(arg1.getMntrsq()).append(fileSepa1);// 代发流水
					lineInfo.append(arg1.getTrandt()).append(fileSepa1);// 代发交易日期
					lineInfo.append(arg1.getMntrtm()).append(fileSepa1);// 代发交易时间
					lineInfo.append(arg1.getRtcode()).append(fileSepa1);// 应答码
					lineInfo.append(arg1.getRetmsg()).append(fileSepa1);// 应答描述
					lineInfo.append(arg1.getBankno());// 联行号
					file.write(lineInfo.toString());
					return true;
				}
			});
		} finally {
			biz.debug("<<<<<<<<<<<<<<<<<写入文件完成>>>>>>>>>>>>>>>>>");
			file.close();

			String fileNameRele = para.getParm_value2() + "_rele" + para.getParm_value3();

			final LttsFileWriter fileRele = new LttsFileWriter(path, fileNameRele, encoding);
			fileRele.open();

			try {
				namedsql = CaBatchTransDao.namedsql_selKnlIoblEdmContro;

				DaoUtil.selectList(namedsql, params, new CursorHandler<KnlIoblEdmContro>() {
					@Override
					public boolean handle(int arg0, KnlIoblEdmContro arg1) {
						StringBuffer lineInfo = SysUtil.getInstance(StringBuffer.class);
						lineInfo.append(arg1.getRetrsq()).append(fileSepa1);// 收单交易流水
						lineInfo.append(arg1.getRetrdt()).append(fileSepa1);// 收单交易日期
						lineInfo.append(arg1.getMntrsq()).append(fileSepa1);// 代发交易流水
						lineInfo.append(arg1.getTrandt());// 代发交易日期

						fileRele.write(lineInfo.toString());
						return true;
					}
				});
				fileOk.write("OK");
			} finally {
				fileRele.close();
				fileOk.close();
			}
		}
	}

	/**
	 * 生成超级网银对账文件
	 * 
	 * @param path
	 * @param params
	 * @param trandt
	 * @param fileName
	 * @param namedsql
	 */
	private static void genInternetBank(Params params, String trandt, String namedsql) {
		// 超级网银对账
		// 获取文件参数
		KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "file", "gefile", "SP", true);
		String path = para.getParm_value1();
		String encoding = para.getParm_value5();// 文件编码
		String servtp = "SI";// 文件编码
		params.put("servtp", servtp);
		String fileName = para.getParm_value2() + trandt + para.getParm_value3();
		biz.debug("<<<<<<<<<<<<<<<<<对账文件名>>>>>>>>>>>>>>>>>" + fileName);
		DpCount maps = CaBatchTransDao.selKnlIoblAll(trandt, servtp, false);
		final LttsFileWriter file = new LttsFileWriter(path, fileName, encoding);
		file.open();
		biz.debug("<<<<<<<<<<<<<<<<<开始写入文件>>>>>>>>>>>>>>>>>");
		StringBuffer lineInfo1 = SysUtil.getInstance(StringBuffer.class);
		lineInfo1.append(maps.getCounts()).append(fileSepa1)// 总计
				.append(maps.getTranam()).append(fileSepa1);// 总金额
		file.write(lineInfo1.toString());
		try {
			DaoUtil.selectList(CaBatchTransDao.namedsql_selKnlIobl, params, new CursorHandler<KnlIobl>() {
				@Override
				public boolean handle(int arg0, KnlIobl arg1) {
					StringBuffer lineInfo = SysUtil.getInstance(StringBuffer.class);
					lineInfo.append(arg1.getCapitp()).append(fileSepa1);// 转账交易类型
					lineInfo.append(arg1.getToacct()).append(fileSepa1);// 转出方账号/卡号
					lineInfo.append("").append(fileSepa1);// 转出方子账号类型
					lineInfo.append(arg1.getToacno()).append(fileSepa1);// 转出方子账号
					lineInfo.append("").append(fileSepa1);// 转出方户名
					lineInfo.append(arg1.getTobrch()).append(fileSepa1);// 转出方账号所属机构
					lineInfo.append(arg1.getCrcycd()).append(fileSepa1);// 币种
					lineInfo.append("").append(fileSepa1);// 钞汇标志
					lineInfo.append(arg1.getTranam()).append(fileSepa1);// 交易金额
					lineInfo.append("").append(fileSepa1);// 摘要码
					lineInfo.append(arg1.getCardno()).append(fileSepa1);// 转入方账号/卡号
					lineInfo.append("").append(fileSepa1);// 转入方子账号类型
					lineInfo.append(arg1.getAcctno()).append(fileSepa1);// 转入方子账号
					lineInfo.append("").append(fileSepa1);// 转入方户名
					lineInfo.append(arg1.getBrchno()).append(fileSepa1);// 转入方账号所属机构
					lineInfo.append(arg1.getTlcgam()).append(fileSepa1);// 收费总额
					lineInfo.append(arg1.getChckdt()).append(fileSepa1);// 对账日期
					lineInfo.append(arg1.getKeepdt()).append(fileSepa1);// 清算日期
					lineInfo.append("").append(fileSepa1);// 备用字段1
					lineInfo.append("").append(fileSepa1);// 备用字段2
					lineInfo.append(arg1.getServdt()).append(fileSepa1);// 支付前置日期
					lineInfo.append(arg1.getServsq()).append(fileSepa1);// 支付前置流水号
					lineInfo.append(arg1.getTransq()).append(fileSepa1);// 核心流水
					lineInfo.append(arg1.getTrandt()).append(fileSepa1);// 交易日期
					// lineInfo.append("TCORE").append(fileSepa1);//支付交易渠道 TCORE-传统核心对账数据
					file.write(lineInfo.toString());
					return true;
				}
			});
		} finally {
			biz.debug("<<<<<<<<<<<<<<<<<文件写入完成>>>>>>>>>>>>>>>>>");
			file.close();
		}
	}

}
