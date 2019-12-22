package cn.sunline.ltts.busi.dptran.batchtran.sjpt;

import java.io.File;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_OFCHAR;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
	 /**
	  * 数据平台供数-渠道和交易码参数表信息
	  * 数据平台供数-渠道和交易码参数表信息
	  *
	  */

public class trzupaDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Trzupa.Input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Trzupa.Property> {
	private static final BizLog bizlog = BizLogUtil
			.getBizLog(trzupaDataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Trzupa.Input input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Trzupa.Property property) {
		//从公共参数表中获取需要写出的文件路径信息
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.trzupa", "trzupaFile", "trzupaData", "%", true);
		// 产生文件的日期目录
		final String trandt = DateTools2.getDateInfo().getLastdt();//获取上次交易日期
		// 产生文件的日期目录
		String filePath = tblKnpParameter.getParm_value1() + File.separator + trandt;
		//String filePath = "E:\\789\\";
		String fileName = tblKnpParameter.getParm_value2() + E_OFCHAR.UND + trandt + E_OFCHAR.UND + tblKnpParameter.getParm_value3() + E_OFCHAR.UND + "0001" + tblKnpParameter.getParm_value4();
		 
		bizlog.debug("数据平台供数-渠道和交易码参数表文件信息:[" + filePath + fileName + "]");
		
		// 获取是否产生文件标志
		LttsFileWriter file = new LttsFileWriter(filePath, fileName);
		try {
			StringBuffer lnreq = new StringBuffer();
			lnreq.append("qbtqhk").append(E_OFCHAR.VER).append("钱包还款").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("parepy").append(E_OFCHAR.VER).append("平安普惠还款交易").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("pahgdl").append(E_OFCHAR.VER).append("平安普惠提前理赔回购").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("padkdl").append(E_OFCHAR.VER).append("平安普惠代扣处理").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("bxloan").append(E_OFCHAR.VER).append("有氧现金贷放款").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("lnqbhk").append(E_OFCHAR.VER).append("钱包批量文件还款").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("bapary").append(E_OFCHAR.VER).append("普惠文件还款").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("oprpay").append(E_OFCHAR.VER).append("贷款欠款归还").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("maturp").append(E_OFCHAR.VER).append("贷款到期还款").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("loanjx").append(E_OFCHAR.VER).append("贷款结息").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("lntrsf").append(E_OFCHAR.VER).append("贷款形态转移").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("opaccd").append(E_OFCHAR.VER).append("电子账户开户").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("lsamin").append(E_OFCHAR.VER).append("大小额来账").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("lsamot").append(E_OFCHAR.VER).append("大小额往账").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("lsamre").append(E_OFCHAR.VER).append("大小额冲账").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("sfcdin").append(E_OFCHAR.VER).append("云闪付本行卡转入电子账户").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("sfcdot").append(E_OFCHAR.VER).append("云闪付电子账户转出到本行卡").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("cupsou").append(E_OFCHAR.VER).append("云闪付银联CUPS转出").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("otcdin").append(E_OFCHAR.VER).append("云闪付他行卡转入电子账户").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("otcdot").append(E_OFCHAR.VER).append("云闪付电子账户转出到他行卡").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("api111").append(E_OFCHAR.VER).append("通用冲账").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("cupsin").append(E_OFCHAR.VER).append("云闪付银联CUPS转入").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("cupscx").append(E_OFCHAR.VER).append("云闪付银联CUPS消费撤销及退货").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("instpy").append(E_OFCHAR.VER).append("存款日终结息").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("gl15").append(E_OFCHAR.VER).append("存款差额计提").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("gl16").append(E_OFCHAR.VER).append("贷款差额计提").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("gl21").append(E_OFCHAR.VER).append("贷款表外差额计提").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("gl19").append(E_OFCHAR.VER).append("营改增价税分离流水入账").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("gl17").append(E_OFCHAR.VER).append("拨备计提").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("huabln01").append(E_OFCHAR.VER).append("花呗放款").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("huabhs01").append(E_OFCHAR.VER).append("花呗回收").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("xhdln01").append(E_OFCHAR.VER).append("鑫合贷放款").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("xhdhs01").append(E_OFCHAR.VER).append("鑫合贷回收").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("").append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreq.append("END").append(E_OFCHAR.VER).append("33").append(E_OFCHAR.VER);//.append(System.getProperty("line.separator"))
			file.open();
			file.write(lnreq.toString());
		}finally {
			file.close();
		}
		//从公共参数表中获取需要写出的文件路径信息
		KnpParameter channl = KnpParameterDao.selectOne_odb1("Sjpt.chanpa", "chanpaFile", "chanpaData", "%", true);
		String channlPath = channl.getParm_value1() + File.separator + trandt;
		//String channlPath = "E:\\789\\";
		String channlName = channl.getParm_value2() + E_OFCHAR.UND + trandt + E_OFCHAR.UND + channl.getParm_value3() + E_OFCHAR.UND + "0001" + channl.getParm_value4();
		// 获取是否产生文件标志
		LttsFileWriter channlFile = new LttsFileWriter(channlPath, channlName);
		bizlog.debug("数据平台供数-渠道和交易码参数表文件信息:[" + filePath + fileName + "]");
		try {
			StringBuffer lnreqChannl = new StringBuffer();
			lnreqChannl.append("Y1").append(E_OFCHAR.VER).append("互金信贷系统").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreqChannl.append("Y2").append(E_OFCHAR.VER).append("云闪付").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreqChannl.append("Y3").append(E_OFCHAR.VER).append("普惠").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreqChannl.append("Y4").append(E_OFCHAR.VER).append("钱包").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreqChannl.append("Y5").append(E_OFCHAR.VER).append("互金核心").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreqChannl.append("Y6").append(E_OFCHAR.VER).append("小安时代").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreqChannl.append("Y7").append(E_OFCHAR.VER).append("小微快贷").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreqChannl.append("Y8").append(E_OFCHAR.VER).append("花呗").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreqChannl.append("Y9").append(E_OFCHAR.VER).append("鑫合贷").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreqChannl.append("Z1").append(E_OFCHAR.VER).append("广西金投").append(E_OFCHAR.VER).append(trandt).append(E_OFCHAR.VER).append("icore").append(E_OFCHAR.VER).append(System.getProperty("line.separator"));
			lnreqChannl.append("END").append(E_OFCHAR.VER).append("10").append(E_OFCHAR.VER);//.append(System.getProperty("line.separator"))
			channlFile.open();
			channlFile.write(lnreqChannl.toString());
		}finally {
			channlFile.close();
		}
		
	}

}


