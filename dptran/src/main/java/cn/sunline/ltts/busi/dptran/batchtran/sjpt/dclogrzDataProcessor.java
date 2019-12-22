package cn.sunline.ltts.busi.dptran.batchtran.sjpt;

import java.io.File;
import java.math.BigDecimal;

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
import cn.sunline.ltts.busi.dp.type.sjpt.sjptdt.dclogrzInfo;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_OFCHAR;
	 /**
	  * 会计分录流水表
	  * 会计分录流水表-每日增量，科目发生额表对应的明细记录
	  *	
	  */

public class dclogrzDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Dclogrz.Input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Dclogrz.Property> {
	
	private static final BizLog bizlog = BizLogUtil
			.getBizLog(dclogrzDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Dclogrz.Input input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Dclogrz.Property property) {
		//从公共参数表中获取需要写出的文件路径信息
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.dclogrz", "dclogrzFile", "dclogrzData", "%", true);
		/*KnpParameter sqNoKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.sqno", "sqno", "number", "%", true);
		String pmval1 = sqNoKnpParameter.getParm_value1();//当前值
*/		// 产生文件的日期目录
		final String trandt = DateTools2.getDateInfo().getLastdt();//获取上次交易日期
		// 产生文件的日期目录
		String filePath = tblKnpParameter.getParm_value1() + File.separator + trandt;
		//String filePath = "E:\\789\\";
		String fileName = tblKnpParameter.getParm_value2() + E_OFCHAR.UND + trandt + E_OFCHAR.UND + tblKnpParameter.getParm_value3() + E_OFCHAR.UND + "0001" + tblKnpParameter.getParm_value4();
		 
		bizlog.debug("贷款还款计划明细文件:[" + filePath + fileName + "]");
		 
		// 获取是否产生文件标志
		final LttsFileWriter file = new LttsFileWriter(filePath, fileName);
		
		Params params = new Params();
		params.add("trandt", trandt); 
		
		if(true){
	    	file.open();
	    	try {
	    		final StringBuilder end = new StringBuilder("0");
	    		DaoUtil.selectList(sjptdtDao.namedsql_selDcLogRz, params, new CursorHandler<dclogrzInfo>() {

					@Override
					public boolean handle(int arg0, dclogrzInfo arg1) {
						
						StringBuffer lnreq = new StringBuffer();
						//账务机构
						String acctbr = arg1.getAcctbr();
						lnreq.append(acctbr).append(E_OFCHAR.VER);
						
						//币种
						String crcycd = arg1.getCrcycd();
						lnreq.append(crcycd).append(E_OFCHAR.VER);
						
						//数据日期
						lnreq.append(trandt).append(E_OFCHAR.VER);
						
						//科目号
						String gl_code = arg1.getGl_code();
						lnreq.append(gl_code).append(E_OFCHAR.VER);
						
						//借贷标志
						String amntcd = arg1.getAmntcd().getValue();
						String fileid = arg1.getFileid();
						//交易金额
						BigDecimal tranam = arg1.getTranam();
						if (CommUtil.isNotNull(fileid)) {
							if (fileid.trim().equals("loanjx")) { //贷款结息,避免虚增发生额情况，柳行特色
								if (amntcd.equals("C")) {
									amntcd = "D";
									tranam = tranam.negate();
								}
							}
						}
						BigDecimal	money = new BigDecimal(0);
						if (amntcd.equals("D") || amntcd.equals("R")) {
							lnreq.append(tranam).append(E_OFCHAR.VER);//借方发生额
							lnreq.append(money).append(E_OFCHAR.VER);//贷方发生额
						}else{
							lnreq.append(money).append(E_OFCHAR.VER);//借方发生额
							lnreq.append(tranam).append(E_OFCHAR.VER);//贷方发生额
						}
						
						lnreq.append(fileid).append(E_OFCHAR.VER);//交易码
						lnreq.append(amntcd).append(E_OFCHAR.VER);
						
						//主交易流水
						String mntrsq = arg1.getMntrsq();
						lnreq.append(mntrsq).append(E_OFCHAR.VER);
						
						lnreq.append("1").append(E_OFCHAR.VER);//清算标志
						
						lnreq.append(acctbr).append(E_OFCHAR.VER);
						
						//记录次序号
						Long sortno = arg1.getSortno();
						lnreq.append(sortno).append(E_OFCHAR.VER);
						file.write(lnreq.toString());	
						if (end.toString().length() > 0) {
							end.delete(0, end.toString().length());//数据重置
						}
						end.append(arg0);
						return true;
					}
				});	
	    		//file.write("END" + E_OFCHAR.VER + end.toString() + E_OFCHAR.VER);
				/*String okFileName = tblKnpParameter.getParm_value2() + ".ok";
				LttsFileWriter fileOk = new LttsFileWriter(path1, okFileName);
				fileOk.open();
				try{
					fileOk.write(trandt);
				}finally{
					fileOk.close();
				}*/
			}finally {
				file.close();
			}
	    }
	 
	   bizlog.debug("数据平台供述-会计分录流水 ：" + fileName + "文件产生完成");
	}

}


