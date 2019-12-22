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
import cn.sunline.ltts.busi.dp.type.sjpt.sjptdt.acfrostInfo;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_OFCHAR;
	 /**
	  * 电子账户冻结信息
	  * 电子账户冻结信息
	  *
	  */

public class acfrostDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Acfrost.Input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Acfrost.Property> {
	private static final BizLog bizlog = BizLogUtil
			.getBizLog(acfrostDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Acfrost.Input input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Acfrost.Property property) {
		//从公共参数表中获取需要写出的文件路径信息
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.acfrost", "acfrostFile", "acfrostData", "%", true);
		 
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
				DaoUtil.selectList(sjptdtDao.namedsql_selAcFrost, params, new CursorHandler<acfrostInfo>() {

					@Override
					public boolean handle(int arg0, acfrostInfo arg1) {
						StringBuffer lnreq = new StringBuffer();
						String custac = arg1.getCustac();	 	//账号	
						lnreq.append(custac).append(E_OFCHAR.VER);
						Long frozsq = arg1.getFrozsq();   		//序号	
						lnreq.append(frozsq).append(E_OFCHAR.VER);
						String cardno = arg1.getCardno();   	//账户号码
						lnreq.append(cardno).append(E_OFCHAR.VER);
						//String frozdt = arg1.getFrozdt();   	//数据日期
						lnreq.append(trandt).append(E_OFCHAR.VER);
						String frbgdt = arg1.getFrbgdt();   	//限制日期
						lnreq.append(frbgdt).append(E_OFCHAR.VER);
						String unfrdt = arg1.getUnfrdt();   	//解除日期
						lnreq.append(unfrdt).append(E_OFCHAR.VER);
						BigDecimal frozbl = arg1.getFrozbl();   //限制金额
						lnreq.append(frozbl).append(E_OFCHAR.VER);
						String froztp = arg1.getFroztp();   //限制类型
						lnreq.append(froztp).append(E_OFCHAR.VER);
						String custno = arg1.getCustno();   	//客户号	
						lnreq.append(custno).append(E_OFCHAR.VER);
						String oastty = arg1.getOastty();		//原限制类型
						lnreq.append(oastty).append(E_OFCHAR.VER);
						String tranus = arg1.getTranus();		//经办人
						lnreq.append(tranus).append(E_OFCHAR.VER);
						/*if (CommUtil.isNotNull(arg1.getServtp())) {
							String servtp = arg1.getServtp();		
							lnreq.append(servtp).append(E_OFCHAR.VER);
						}else{
							lnreq.append("").append(E_OFCHAR.VER);
						}*/
						lnreq.append("Y5").append(E_OFCHAR.VER);	//交易渠道
						String tranbr = arg1.getTranbr();		//交易机构
						lnreq.append(tranbr).append(E_OFCHAR.VER);
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


