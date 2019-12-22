package cn.sunline.ltts.busi.dptran.batchtran.sjpt;

import java.io.File;
import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.dp.namedsql.sjpt.sjptdtDao;
import cn.sunline.ltts.busi.dp.type.sjpt.sjptdt.rbintamtInfo;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_OFCHAR;
	 /**
	  * 存款结息表
	  * 存款结息表
	  *
	  */

public class rbintamtDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbintamt.Input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbintamt.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(rbintamtDataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbintamt.Input input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbintamt.Property property) {
		 // 交易数据准备
		 // 获取存款结息文件相关数据
		 KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.rbintamt", "rbintamtFile", "rbintamtData", "%", true);
		 
		 /*KnpParameter sqNoKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.sqno", "sqno", "number", "%", true);
		 String pmval1 = sqNoKnpParameter.getParm_value1();//当前值
*/		// 产生文件的日期目录
		final String trandt = DateTools2.getDateInfo().getLastdt();//获取上次交易日期
		// 产生文件的日期目录
		String filePath = tblKnpParameter.getParm_value1() + File.separator + trandt;
		//String filePath = "E:\\123\\";
		String fileName = tblKnpParameter.getParm_value2() + E_OFCHAR.UND + trandt + E_OFCHAR.UND + tblKnpParameter.getParm_value3() + E_OFCHAR.UND + "0001" + tblKnpParameter.getParm_value4();
		 
		bizlog.debug("贷款还款计划明细文件:[" + filePath + fileName + "]");
		 // 获取是否产生文件标志	
		 final LttsFileWriter file = new LttsFileWriter(filePath, fileName);
		 Params params = new Params();
		 params.add("trandt", trandt);
		 file.open();
		 try {
			final StringBuilder ss = new StringBuilder("0");
			DaoUtil.selectList(sjptdtDao.namedsql_selRbintamt, params, new CursorHandler<rbintamtInfo>() {
				@Override
				public boolean handle(int arg0, rbintamtInfo arg1) {
					StringBuffer lnreq = new StringBuffer();
					//数据日期
					String trandt = arg1.getTrandt();
					lnreq.append(trandt).append(E_OFCHAR.VER);
					//账号
					String acctno = arg1.getAcctno();
					lnreq.append(acctno).append(E_OFCHAR.VER);
					//产品代码
					String prodcd = arg1.getProdcd();
					lnreq.append(prodcd).append(E_OFCHAR.VER);
					//结息前本金
					BigDecimal actbal = arg1.getActbal();
					lnreq.append(actbal).append(E_OFCHAR.VER);
					// 当前账余额
					BigDecimal onlnbl = arg1.getOnlnbl();
					lnreq.append(onlnbl).append(E_OFCHAR.VER);		//结息后本金
					// 利息
					BigDecimal intest = arg1.getIntest();
					//利息
					lnreq.append(intest).append(E_OFCHAR.VER);
					//利息税
					BigDecimal cutmis = arg1.getCutmis();
					lnreq.append(cutmis).append(E_OFCHAR.VER);
					//本金发生额
					//BigDecimal cutmam = arg1.getCutmam();
					lnreq.append(intest).append(E_OFCHAR.VER);
					//核心流水号
					String transq = arg1.getTransq();
					lnreq.append(transq).append(E_OFCHAR.VER);
					//利息税率
					BigDecimal taxrat = arg1.getTaxrat();
					lnreq.append(taxrat).append(E_OFCHAR.VER);
					//应税利息
					BigDecimal taxint = arg1.getTaxint();
					lnreq.append(taxint).append(E_OFCHAR.VER);
					file.write(lnreq.toString());
					if (ss.toString().length() > 0) {
						ss.delete(0, ss.toString().length());//数据重置
					}
					ss.append(arg0);
					return true;
				}
			});
			file.write("END" + E_OFCHAR.VER + ss.toString() + E_OFCHAR.VER);
		 }finally {
			file.close();
		 }
	}

}


