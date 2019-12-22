package cn.sunline.ltts.busi.dptran.batchtran.sjpt;

import java.io.File;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.dp.namedsql.sjpt.sjptdtDao;
import cn.sunline.ltts.busi.dp.type.sjpt.sjptdt.deparmInfo;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_OFCHAR;
	 /**
	  * 存款产品参数
	  * 存款产品参数
	  *
	  */

public class deparmDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Deparm.Input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Deparm.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(deparmDataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Deparm.Input input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Deparm.Property property) {
		 	// 获取存款产品参数文件相关数据
			KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.deparm", "deparmFile", "deparmData", "%", true);
			/*KnpParameter sqNoKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.sqno", "sqno", "number", "%", true);
			String pmval1 = sqNoKnpParameter.getParm_value1();//当前值
*/			// 产生文件的日期目录
			final String trandt = DateTools2.getDateInfo().getLastdt();//获取上次交易日期
			// 产生文件的日期目录
			String filePath = tblKnpParameter.getParm_value1() + File.separator + trandt;
			//String filePath = "E:\\123\\";
			String fileName = tblKnpParameter.getParm_value2() + E_OFCHAR.UND + trandt + E_OFCHAR.UND + tblKnpParameter.getParm_value3() + E_OFCHAR.UND + "0001" + tblKnpParameter.getParm_value4();
			 
			bizlog.debug("贷款还款计划明细文件:[" + filePath + fileName + "]");
			// 获取是否产生文件标志
			final LttsFileWriter file = new LttsFileWriter(filePath, fileName);
			Params params = new Params();
//			params.add("trandt", trandt);
			file.open();
			try {
				final StringBuilder ss = new StringBuilder("0");
				DaoUtil.selectList(sjptdtDao.namedsql_selDeparm, params, new CursorHandler<deparmInfo>() {
							@Override
							public boolean handle(int arg0, deparmInfo arg1) {
								StringBuffer lnreq = new StringBuffer();
								// 产品号
								String prodcd = arg1.getProdcd();
								lnreq.append(prodcd).append(E_OFCHAR.VER);
								// 产品名称
								String prodtx = arg1.getProdtx();
								lnreq.append(prodtx).append(E_OFCHAR.VER);
								// 产品简称
								String pdmktx = arg1.getPdmktx();
								if (CommUtil.isNull(pdmktx)) {
									pdmktx = prodtx;
								}
								lnreq.append(pdmktx).append(E_OFCHAR.VER);
								// 启用日期
								String efctdt = arg1.getEfctdt();
								lnreq.append(efctdt).append(E_OFCHAR.VER);
								// 止用日期
								String inefdt = arg1.getInefdt();
								lnreq.append(inefdt).append(E_OFCHAR.VER);
								// 币种
								String crcycd = arg1.getCrcycd();
								lnreq.append(crcycd).append(E_OFCHAR.VER);
								// 业务细类
								String debttp = arg1.getDebttp();
								lnreq.append(debttp).append(E_OFCHAR.VER);
								// 利息税编号
								//String taxecd = arg1.getTaxecd();
								lnreq.append("TAX001").append(E_OFCHAR.VER);
								// 计息天数类型
								String txbebs = arg1.getTxbebs();
								lnreq.append(txbebs).append(E_OFCHAR.VER);
								// 会计处理代码
								String glcode = arg1.getGl_code();
								lnreq.append(glcode).append(E_OFCHAR.VER);
								// 数据日期
								lnreq.append(trandt).append(E_OFCHAR.VER);
								file.write(lnreq.toString());
								if (ss.toString().length() > 0) {
									ss.delete(0, ss.toString().length());//数据重置
								}
								ss.append(arg0);
								return true;
							}
						});
				file.write("END" + E_OFCHAR.VER + ss.toString() + E_OFCHAR.VER);
			} finally {
				file.close();
			}
	}
}


