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
import cn.sunline.ltts.busi.dp.type.sjpt.sjptdt.cointratesInfo;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_OFCHAR;
	 /**
	  * 利率明细
	  * 利率明细
	  *
	  */

public class cointratesDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Cointrates.Input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Cointrates.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(cointratesDataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Cointrates.Input input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Cointrates.Property property) {
			// 获取存款产品参数文件相关数据
			KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.cointrates", "cointratesFile", "cointratesData", "%", true);
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
				DaoUtil.selectList(sjptdtDao.namedsql_selCointrates, params, new CursorHandler<cointratesInfo>() {
							@Override
							public boolean handle(int arg0, cointratesInfo arg1) {
								StringBuffer lnreq = new StringBuffer();
								//利率代码
								String intrcd = arg1.getIntrcd();            
								lnreq.append(intrcd).append(E_OFCHAR.VER);
								//数据日期
								/*String tmstmp = arg1.getTmstmp();
								if (tmstmp != null && !"".equals(tmstmp)) {
									tmstmp = tmstmp.substring(0,8);
								}*/
								lnreq.append(trandt).append(E_OFCHAR.VER);
								//业务类别
								String intrkd = arg1.getIntrkd();           
								lnreq.append(intrkd).append(E_OFCHAR.VER);
								//期限
								String rfirtm = arg1.getRfirtm();
								lnreq.append(rfirtm).append(E_OFCHAR.VER);
								//最小金额
								lnreq.append("").append(E_OFCHAR.VER);
								//生效日期
								String efctdt = arg1.getEfctdt();           
								lnreq.append(efctdt).append(E_OFCHAR.VER);
								//利率值
								BigDecimal baseir = arg1.getBaseir();
								lnreq.append(baseir).append(E_OFCHAR.VER);
								// 协定金额
								lnreq.append("").append(E_OFCHAR.VER);
								//浮动点数上限
								BigDecimal flmxsc = arg1.getFlmxsc();            
								lnreq.append(flmxsc).append(E_OFCHAR.VER);
								//浮动点数下限
								BigDecimal flmnsc = arg1.getFlmnsc();            
								lnreq.append(flmnsc).append(E_OFCHAR.VER);
								//序号
								String rfirsq = arg1.getRfirsq();           
								lnreq.append(rfirsq).append(E_OFCHAR.VER);
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


