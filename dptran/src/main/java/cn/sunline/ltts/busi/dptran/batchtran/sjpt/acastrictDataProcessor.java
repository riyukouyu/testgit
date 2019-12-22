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
import cn.sunline.ltts.busi.dp.type.sjpt.sjptdt.acastrictInfo;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_OFCHAR;
	 /**
	  * 账户限制
	  * 账户限制
	  *
	  */

public class acastrictDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Acastrict.Input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Acastrict.Property> {
	private static final BizLog bizlog = BizLogUtil
			.getBizLog(acastrictDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Acastrict.Input input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Acastrict.Property property) {
		//从公共参数表中获取需要写出的文件路径信息
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.acastrict", "acastrictFile", "acastrictData", "%", true);
		 
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
		
		if (true) {
			file.open();
			try {
				final StringBuilder end = new StringBuilder("0");
				DaoUtil.selectList(sjptdtDao.namedsql_selAcastrict, params, new CursorHandler<acastrictInfo>() {
					@Override
					public boolean handle(int arg0, acastrictInfo arg1) {
						StringBuffer lnreq = new StringBuffer();
						lnreq.append(trandt).append(E_OFCHAR.VER);
						String as_type	= arg1.getAs_type();	//	限制类型
						lnreq.append(as_type).append(E_OFCHAR.VER);
						String custac   = arg1.getCustac();    //   限制号码
						lnreq.append(custac).append(E_OFCHAR.VER);
						String seq_no   = arg1.getSeq_no();    //   序号	
						lnreq.append(seq_no).append(E_OFCHAR.VER);
						String crcycd   = arg1.getCrcycd();    //   币种		
						lnreq.append(crcycd).append(E_OFCHAR.VER);
						if (CommUtil.isNotNull(arg1.getCsextg())) {
							String csextg   = arg1.getCsextg().getValue();    //   钞汇标志	
							lnreq.append(csextg).append(E_OFCHAR.VER);
						}else{
							lnreq.append("").append(E_OFCHAR.VER);
						}
						if (CommUtil.isNotNull(arg1.getFrlmtp())) {
							String frlmtp   = arg1.getFrlmtp().getValue();    //   限制方式	
							lnreq.append(frlmtp).append(E_OFCHAR.VER);
						}else{
							lnreq.append("").append(E_OFCHAR.VER);
						}
						String frreas	= arg1.getFrreas();	//	限制原因
						lnreq.append(frreas).append(E_OFCHAR.VER);
						/*if (CommUtil.isNotNull(arg1.getServtp())) {
							String servtp   = arg1.getServtp();    
							lnreq.append(servtp).append(E_OFCHAR.VER);
						}else{
							lnreq.append("").append(E_OFCHAR.VER);
						}*/
						lnreq.append("Y5").append(E_OFCHAR.VER);	//   渠道号	
						file.write(lnreq.toString());	
						if (end.toString().length() > 0) {
							end.delete(0, end.toString().length());//数据重置
						}
						end.append(arg0);
						return true;
					}
				});
				file.write("END" + E_OFCHAR.VER + end.toString() + E_OFCHAR.VER);
				/*String okFileName = tblKnpParameter.getParm_value2() + ".ok";
				LttsFileWriter fileOk = new LttsFileWriter(path1, okFileName);
				fileOk.open();
				try{
					fileOk.write(trandt);
				}finally{
					fileOk.close();
				}*/
			}finally{
				file.close();
			}
		}
		
	}

}


