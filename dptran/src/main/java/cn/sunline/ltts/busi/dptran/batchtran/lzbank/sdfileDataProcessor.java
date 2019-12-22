package cn.sunline.ltts.busi.dptran.batchtran.lzbank;

import java.math.BigDecimal;

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
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.ca.namedsql.CaBatchTransDao;
import cn.sunline.ltts.busi.ca.type.CaCustInfo.CoresdList;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCkfi;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCkfiDao;
import cn.sunline.ltts.busi.dp.type.DpTransfer.DpCount;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * 收单对账文件生成
 * 
 * @author
 * @Date
 */

public class sdfileDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.edsp.busi.dptran.batchtran.lzbank.intf.Sdfile.Input, cn.sunline.edsp.busi.dptran.batchtran.lzbank.intf.Sdfile.Property> {
	private static final String fileSepa1 = "~";// 文件分隔符
	private static BizLog biz = BizLogUtil.getBizLog(sdfileDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.edsp.busi.dptran.batchtran.lzbank.intf.Sdfile.Input input, cn.sunline.edsp.busi.dptran.batchtran.lzbank.intf.Sdfile.Property property) {
		biz.method("<<<<<<<<<<<<<<<<<收单对账文件开始>>>>>>>>>>>>>>>>>");
		String lastdt = new BigDecimal(CommTools.getBaseRunEnvs().getComputer_date()).subtract(new BigDecimal(1)).toString();
		String path = null;
		String fileName = null;
		String namedsql = null;
		Params params = new Params();
		params.put("trandt", lastdt);
		KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "file", "sdfile", E_VAFITY.UNION.getId(), true);
		// 获取文件参数
		DpCount maps = CaBatchTransDao.selKnlIoblCupsAll(lastdt, false);// 获取总计及总金额
		DpCount rmaps = CaBatchTransDao.selKnlIoblCupsReAll(lastdt, false);// 获取反向交易总计及总金额
		maps.setCounts(maps.getCounts() + rmaps.getCounts());
		path = para.getParm_value1() + lastdt;
		String encoding = para.getParm_value5();// 文件编码
		fileName = para.getParm_value2() + para.getParm_value3();
		KnlCkfi knlckfi = KnlCkfiDao.selectOne_odb3(lastdt, fileName, false);
		//为空或者不为空但是文件是否生成状态为NO
		if (CommUtil.isNotNull(knlckfi) && knlckfi.getIsmkfi() == E_YES___.YES) {
			return;
		}
		//对账文件的生成
		String fileNameOK = para.getParm_value4();
		biz.debug("<<<<<<<<<<<<<<<<<对账文件名>>>>>>>>>>>>>>>>>" + fileName);
		namedsql = CaBatchTransDao.namedsql_selKnlIoblCups;
		final LttsFileWriter file = new LttsFileWriter(path, fileName, encoding);
		file.open();
		final LttsFileWriter fileOk = new LttsFileWriter(path, fileNameOK, encoding);
		fileOk.open();
		biz.debug("<<<<<<<<<<<<<<<<<开始写入文件>>>>>>>>>>>>>>>>>");
		StringBuffer lineInfo1 = SysUtil.getInstance(StringBuffer.class);
		lineInfo1.append(maps.getCounts()).append(fileSepa1)// 总计
				.append(maps.getTranam()).append(fileSepa1);// 总金额
		file.write(lineInfo1.toString());
		try {
			DaoUtil.selectList(namedsql, params, new CursorHandler<CoresdList>() {
				@Override
				public boolean handle(int arg0, CoresdList arg1) {
					StringBuffer lineInfo = SysUtil.getInstance(StringBuffer.class);
					lineInfo.append(arg1.getFlowid() == null ? "" : arg1.getFlowid()).append(fileSepa1);// 自增id
					lineInfo.append(arg1.getServtp() == null ? "" : arg1.getServtp()).append(fileSepa1);// 支付渠道
					lineInfo.append(arg1.getUniseq() == null ? "" : arg1.getUniseq()).append(fileSepa1);// 银联流水
					lineInfo.append(arg1.getUnkpdt() == null ? "" : arg1.getUnkpdt()).append(fileSepa1);// 银联清算日期
					lineInfo.append(arg1.getMesstp() == null ? "" : arg1.getMesstp()).append(fileSepa1);// 银联报文类型
					lineInfo.append(arg1.getTranam() == null ? "" : arg1.getTranam()).append(fileSepa1);// 交易金额
					lineInfo.append(arg1.getRefeno() == null ? "" : arg1.getRefeno()).append(fileSepa1);// 参考号
					lineInfo.append(arg1.getOrdeno() == null ? "" : arg1.getOrdeno()).append(fileSepa1);// 订单号
					lineInfo.append(arg1.getCardno() == null ? "" : arg1.getCardno()).append(fileSepa1);// 交易卡号
					lineInfo.append(arg1.getInmeid() == null ? "" : arg1.getInmeid()).append(fileSepa1);// 内部商户号
					lineInfo.append(arg1.getInmena() == null ? "" : arg1.getInmena()).append(fileSepa1);// 内部商户名称
					lineInfo.append("").append(fileSepa1);// 内部商户名称
					lineInfo.append(arg1.getTranst() == null ? "" : arg1.getTranst()).append(fileSepa1);// 核心交易状态
					lineInfo.append(arg1.getMntrsq() == null ? "" : arg1.getMntrsq()).append(fileSepa1);// 核心流水
					lineInfo.append(arg1.getTrandt() == null ? "" : arg1.getTrandt()).append(fileSepa1);// 核心交易日期
					lineInfo.append(arg1.getMntrtm() == null ? "" : arg1.getMntrtm()).append(fileSepa1);// 核心交易时间
					lineInfo.append(arg1.getPrepsq() == null ? "" : arg1.getPrepsq()).append(fileSepa1);// posp流水
					lineInfo.append(arg1.getPrepdt() == null ? "" : arg1.getPrepdt()).append(fileSepa1);// posp日期
					lineInfo.append(arg1.getPreptm() == null ? "" : arg1.getPreptm()).append(fileSepa1);// posp时间
					lineInfo.append(arg1.getSbrand() == null ? "" : arg1.getSbrand()).append(fileSepa1);// posp品牌名称
					lineInfo.append(arg1.getBusitp() == null ? "" : arg1.getBusitp()).append(fileSepa1);// 业务类型
					lineInfo.append(arg1.getTeleno() == null ? "" : arg1.getTeleno()).append(fileSepa1);// 手机号
					lineInfo.append(arg1.getTmteleno() == null ? "" : arg1.getTmteleno()).append(fileSepa1);//脱敏手机号
					lineInfo.append(arg1.getCardtp() == null ? "" : arg1.getCardtp()).append(fileSepa1);// 卡类型
					lineInfo.append(arg1.getMerate() == null ? "" : arg1.getMerate()).append(fileSepa1);// 商户费率
					lineInfo.append(arg1.getJnanam() == null ? "" : arg1.getJnanam()).append(fileSepa1);// 交易手续费
					lineInfo.append(arg1.getVoucfe() == null ? "" : arg1.getVoucfe()).append(fileSepa1);// 优惠卷抵扣
					lineInfo.append(arg1.getToanam() == null ? "" : arg1.getToanam()).append(fileSepa1);// 商户存入金额
					lineInfo.append(arg1.getFinsty() == null ? "" : arg1.getFinsty()).append(fileSepa1);// 结算方式
					lineInfo.append(arg1.getAcfist() == null ? "" : arg1.getAcfist()).append(fileSepa1);// 实际结算方式
					lineInfo.append(arg1.getChrgam() == null ? "" : arg1.getChrgam()).append(fileSepa1);// 银联成本
					lineInfo.append(arg1.getAd01am() == null ? "" : arg1.getAd01am()).append(fileSepa1);// 服务商分润金额
					lineInfo.append(arg1.getReflid() == null ? "" : arg1.getReflid()).append(fileSepa1);// 原自增id
					lineInfo.append(arg1.getSignim() == null ? "" : arg1.getSignim()).append(fileSepa1);// 签购单地址
					lineInfo.append(arg1.getFiel0() == null ? "" : arg1.getFiel0()).append(fileSepa1);// 0域
					lineInfo.append(arg1.getFiel03() == null ? "" : arg1.getFiel03()).append(fileSepa1);// 3域
					lineInfo.append(arg1.getBscdam() == null ? "" : arg1.getBscdam()).append(fileSepa1);//发卡基础金额
					lineInfo.append(arg1.getSvntam() == null ? "" : arg1.getSvntam()).append(fileSepa1);// 银联网络服务费金额
					lineInfo.append(arg1.getUnbdam() == null ? "" : arg1.getUnbdam());// 银联品牌服务费金额
					file.write(lineInfo.toString());
					return true;
				}
			});
			fileOk.write("OK");

		}
		catch (Exception e) {
			biz.debug("<<<<<<<<<<<<<<<<<收单对账文件生成异常开始>>>>>>>>>>>>>>>>>");
			if (CommUtil.isNull(knlckfi)) {
				KnlCkfi entity = SysUtil.getInstance(KnlCkfi.class);
				entity.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
				entity.setTrandt(CommTools.getBaseRunEnvs().getComputer_date());// 交易日期
				entity.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 渠道流水
				entity.setServdt(CommTools.getBaseRunEnvs().getComputer_date());// 渠道日期
				entity.setFilety(E_VAFITY.UNION);// 文件类型
				entity.setChckdt(lastdt);// 对账日期
				entity.setIsmkfi(E_YES___.NO);// 文件是否生成
				entity.setFimkdt(lastdt);// 文件生成日期
				entity.setFipath(path);// 文件路径
				entity.setFiname(fileName);// 文件名称
				KnlCkfiDao.insert(entity);
			}
			biz.debug("<<<<<<<<<<<<<<<<<收单对账文件生成异常开始>>>>>>>>>>>>>>>>>");
			return;
		}
		finally {
			biz.debug("<<<<<<<<<<<<<<<<<写入文件完成>>>>>>>>>>>>>>>>>");
			file.close();
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
			entity.setFilety(E_VAFITY.UNION);// 文件类型
			entity.setChckdt(lastdt);// 对账日期
			entity.setIsmkfi(E_YES___.YES);// 文件是否生成
			entity.setFimkdt(lastdt);// 文件生成日期
			entity.setFipath(path);// 文件路径
			entity.setFiname(fileName);// 文件名称
			KnlCkfiDao.insert(entity);
		}
		else {
			knlckfi.setIsmkfi(E_YES___.YES);// 文件是否生成
			KnlCkfiDao.updateOne_odb2(knlckfi);
		}
		biz.method("<<<<<<<<<<<<<<<<<收单对账文件结束>>>>>>>>>>>>>>>>>");
	}

}
