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
import cn.sunline.ltts.busi.dp.type.sjpt.sjptdt.cointrateInfo;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_OFCHAR;
	 /**
	  * 利率
	  * 利率
	  *
	  */

public class cointrateDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Cointrate.Input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Cointrate.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(acimovergDataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Cointrate.Input input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Cointrate.Property property) {
			// 获取利率文件相关数据
			KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.cointrate", "cointrateFile", "cointrateData", "%", true);
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
			params.add("trandt", trandt);
			file.open();
			try {
				final StringBuilder ss = new StringBuilder("0");
				DaoUtil.selectList(sjptdtDao.namedsql_selCointrate, params, new CursorHandler<cointrateInfo>() {
							@Override
							public boolean handle(int arg0, cointrateInfo arg1) {
								StringBuffer lnreq = new StringBuffer();
								//利率代码
								String intrcd = arg1.getIntrcd();
								lnreq.append(intrcd).append(E_OFCHAR.VER);
								//数据时间
								lnreq.append(trandt).append(E_OFCHAR.VER);
								//基础利率
								BigDecimal baseir = arg1.getBaseir();
								lnreq.append(baseir).append(E_OFCHAR.VER);
								//币种
								String crcycd = arg1.getCrcycd();
								lnreq.append(crcycd).append(E_OFCHAR.VER);
								//利率名称-基准利率描述
								String intrna = arg1.getIntrna();
								lnreq.append(intrna).append(E_OFCHAR.VER);
								//生效日期
								String efctdt = arg1.getEfctdt();
								lnreq.append(efctdt).append(E_OFCHAR.VER);
								//协定金额
								BigDecimal amtcop = arg1.getAmtcop();
								lnreq.append(amtcop).append(E_OFCHAR.VER);
								file.write(lnreq.toString());
								if (ss.toString().length() > 0) {
									ss.delete(0, ss.toString().length());//数据重置
								}
								ss.append(arg0);
								return true;
							}
						});
				file.write("END" + E_OFCHAR.VER + ss.toString() + E_OFCHAR.VER);
				/*String okFileName = tblKnpParameter.getParm_value2() + ".ok";
				LttsFileWriter fileOk = new LttsFileWriter(filePath, okFileName);
				fileOk.open();
				try{
					fileOk.write(trandt);
				}finally{
					fileOk.close();
				}	*/
			} finally {
				file.close();
			}
	
	}

}


