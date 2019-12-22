package cn.sunline.ltts.busi.dptran.batchtran.sjpt;

import java.io.File;

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
import cn.sunline.ltts.busi.dp.type.sjpt.sjptdt.acimovergInfo;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_OFCHAR;
	 /**
	  * 不动户久悬户登记簿
	  * 不动户久悬户登记簿
	  *
	  */

public class acimovergDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Acimoverg.Input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Acimoverg.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(acimovergDataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Acimoverg.Input input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Acimoverg.Property property) {
			// 获取不动户久悬户登记簿文件相关数据
			KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.acimoverg","acimovergFile", "acimovergData", "%", true);
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
				DaoUtil.selectList(sjptdtDao.namedsql_selAcimoverg, params, new CursorHandler<acimovergInfo>() {
							@Override
							public boolean handle(int arg0, acimovergInfo arg1) {
								StringBuffer lnreq = new StringBuffer();
								//数据日期
								//String datadt = arg1.getDatadt();
								lnreq.append(trandt).append(E_OFCHAR.VER);
								//账号
								String cardno = arg1.getCardno();
								lnreq.append(cardno).append(E_OFCHAR.VER);
								//序号
								String seq_no = arg1.getSeq_no();
								lnreq.append(seq_no).append(E_OFCHAR.VER);
								//机构号 
								String brchno = arg1.getBrchno();            
								lnreq.append(brchno).append(E_OFCHAR.VER);
								//标识
								String procrs = arg1.getProcrs();
								lnreq.append(procrs).append(E_OFCHAR.VER);
								//账户状态 
								String acctst = arg1.getAcctst();
								lnreq.append(acctst).append(E_OFCHAR.VER);
								//操作柜员
								String userid = arg1.getUserid();
								lnreq.append(userid).append(E_OFCHAR.VER);
								 
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


