package cn.sunline.ltts.busi.dptran.batchtran;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.base.util.security.encrypt.MD5EncryptUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.targetList;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbStat;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbStatDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBill;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatch;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.cf.IoCuTable.IoCucifCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PROCST;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

	 /**
	  * 电子账户对账单
	  *
	  */

public class chkbilDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.intf.Chkbil.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Chkbil.Property, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbStat> {
	  
	  	private static BizLog bizlog = BizLogUtil.getBizLog(AbstractBatchDataProcessor.class);
	
		private static String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		private static String fileph = KnpParameterDao.selectOne_odb1("CHKB", "dpfile","01", "%", true).getParm_value1() + E_FILETP.NA000001 + File.separator + trandt + File.separator;//附件路径
		private static String sdfile = KnpParameterDao.selectOne_odb1("CHKB", "dpfile","01", "%", true).getParm_value1() + E_FILETP.NA000002 + File.separator + trandt + File.separator;//写文件路径
		
		private static String fengefu = "|||";// 分隔符
		private static String tpltno = "EXXXNASZDYJ00001"; //模板标识
		private static String tpltvs = "001"; //模板版本号
		private static BizLog log = BizLogUtil.getBizLog(chkbilDataProcessor.class);
		private static String md5 = ""; 	//MD5值
		private static String snpath = File.separator + E_FILETP.NA000001 + File.separator + trandt + File.separator;//子文件相对路径
		private static String path2 = File.separator + E_FILETP.NA000002 + File.separator + trandt + File.separator;//文件相对路径
	    DefaultOptions<BatchFileSubmit> ls = new DefaultOptions<BatchFileSubmit>();
	    BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
	    Map<String,String> map = new HashMap<String,String>();
	  
	  /**
		 * 批次数据项处理逻辑。
		 * 
		 * @param job 批次作业ID
		 * @param index  批次作业第几笔数据(从1开始)
		 * @param dataItem 批次数据项
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
	 * @throws Exception 
	 * @throws WriteException 
		 */
	    
	    //写文件头
	    private static WritableWorkbook getWeb(OutputStream out, int cnt){
	    	
	    	WritableWorkbook web = null;
	    	WritableSheet ws = null;
	    	try {
	    		web = Workbook.createWorkbook(out);//创建文件
	    		ws = web.createSheet("账单", 0); //创建工作簿

	    		WritableCellFormat w = new WritableCellFormat();
	    		w.setAlignment(Alignment.CENTRE);//定义格式

	    		//设置列的宽度
	    		ws.setColumnView(0, 30);
	    		ws.setColumnView(1, 30);
	    		ws.setColumnView(2, 30);
	    		ws.setColumnView(3, 30);
	    		ws.setColumnView(4, 30);
	    		ws.setColumnView(5, 30);
	    		ws.setColumnView(6, 30);
	    		ws.setColumnView(7, 30);
	    		ws.setColumnView(8, 30);
	    		ws.setColumnView(9, 30);
	    		ws.setColumnView(10, 30);
	    		ws.setColumnView(11, 30);
	    		ws.setColumnView(12, 30);
	    		ws.setColumnView(13, 30);

	    		WritableFont wf = new WritableFont(WritableFont.ARIAL,12,WritableFont.BOLD,true);
	    		WritableCellFormat wcf = new WritableCellFormat(wf);
	    		wcf.setAlignment(Alignment.CENTRE);

	    		int col = 0;//列号
	    		Label label1 = new Label(col++, 0, "卡号", wcf);
	    		Label label2 = new Label(col++, 0, "户名", wcf);
	    		Label label3 = new Label(col++, 0, "起始日期", wcf);
	    		Label label4 = new Label(col++, 0, "终止日期", wcf);
	    		Label label5 = new Label(col++, 0, "交易日期", wcf);
	    		Label label6 = new Label(col++, 0, "交易时间", wcf);
	    		Label label7 = new Label(col++, 0, "借/贷", wcf);
	    		Label label8 = new Label(col++, 0, "币种", wcf);
	    		Label label9 = new Label(col++, 0, "交易金额", wcf);
	    		Label label10 = new Label(col++, 0, "账户余额", wcf);
	    		Label label11 = new Label(col++, 0, "对方账号", wcf);
	    		Label label12 = new Label(col++, 0, "对方户名", wcf);
	    		Label label13 = new Label(col++, 0, "摘要", wcf);
	    		Label label14 = new Label(col++, 0, "备注", wcf);

	    		ws.addCell(label1);
	    		ws.addCell(label2);
	    		ws.addCell(label3);
	    		ws.addCell(label4);
	    		ws.addCell(label5);
	    		ws.addCell(label6);
	    		ws.addCell(label7);
	    		ws.addCell(label8);
	    		ws.addCell(label9);
	    		ws.addCell(label10);
	    		ws.addCell(label11);
	    		ws.addCell(label12);
	    		ws.addCell(label13);
	    		ws.addCell(label14);


	    		
	    		
	    		if(cnt == 0){
//	    			log.debug("**********文件头关闭**********");
	    			web.write();
	    			out.flush();
	    			web.close();
	    			out.close();
	    		}
	    		
	    	} catch (RowsExceededException e) {
	    		log.debug("写文件头错误:[%s]", e.getLocalizedMessage());
	    		throw CaError.Eacct.E0001(e.getLocalizedMessage());
	    	} catch (WriteException e) {
	    		log.debug("工作簿写入失败:[%s]", e.getLocalizedMessage());
	    		throw CaError.Eacct.E0001(e.getLocalizedMessage());
	    	} catch (Exception e) {
	    		log.debug("文件打开失败:[%s]", e.getLocalizedMessage());
	    		throw CaError.Eacct.E0001(e.getLocalizedMessage());
	    	}


	    	return web;
	    }
	    
		@Override
		public void  process(String jobId, int index, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbStat dataItem, cn.sunline.ltts.busi.dptran.batchtran.intf.Chkbil.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Chkbil.Property property) {
			
			final String path1 = fileph;
			Params params = new Params();
			String namedSqlId = "";// 查询数据集的命名sql
			namedSqlId = DpAcctQryDao.namedsql_selknlbillchk;
			
						
			
			final KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("CHKB", "filetp", "%", "%", false);
			if(CommUtil.isNull(tblKnpParameter)){
				throw CaError.Eacct.E0001("xls文件格式参数未配置");
			}

			File file = new File(path1);
			file.mkdirs();

			final String cardno = dataItem.getCardno();
			final String acctna = dataItem.getAcctna();
			final String begndt = dataItem.getBegndt();
			final String endddt = dataItem.getEndddt();
			//查询电子账号
			IoCaKnaAcdc tblacdc = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb2(cardno, false);
			
			if(CommUtil.isNull(tblacdc)){
				return;
			}
			//查询负债账号
			KnaAcct tbkacct = CapitalTransDeal.getSettKnaAcctAc(tblacdc.getCustac());
			
			if(CommUtil.isNull(tbkacct)){
				return;
			}
			
			//add 2017/01/21 新增账单期间有过升级操作的账户的支持
			//查询账户下所有结算负债子账户
			List<KnaAcct> tblacct = null;
			List<String> acctnos = new ArrayList<>();
			int cntl = 0;
			tblacct = ActoacDao.selknaAcctclose(E_ACSETP.SA, tbkacct.getCustac(),false);
			
			if(tblacct.size() > 1){
				for(KnaAcct acct : tblacct){
					acctnos.add(acct.getAcctno());
				}
			}
			
			//账户没有升级记录
			if(tblacct.size() == 1){
				params.add("acctno", tbkacct.getAcctno());
				params.add("begndt", begndt);
				params.add("endddt", endddt);
				
				namedSqlId = DpAcctQryDao.namedsql_selknlbillchk;
				cntl = DpAcctQryDao.selknlbillchkcnt(tbkacct.getAcctno(), begndt, endddt, true);
				log.debug("***********************" +  cntl + "**************************");			
			}
			
			//账户存在升级记录
			if(CommUtil.isNotNull(acctnos) && acctnos.size() > 1){
				//list转Sting时会带[]，放入params前先去掉[]
			    StringBuffer acctnostr = new StringBuffer();
			       for (int i = 0; i < acctnos.size(); i++) {
			           if (i == acctnos.size() - 1) {
			               acctnostr.append("'" + String.valueOf(acctnos.get(i)) + "'");
			           } else {
			               acctnostr.append("'" + String.valueOf(acctnos.get(i) + "'" + ","));
			           }
			       }
//				String acctnostr = acctnos.toString().substring(1, acctnos.toString().length() - 1);
				System.out.println(acctnostr);
				params.add("acctno", acctnostr);
				params.add("begndt", begndt);
				params.add("endddt", endddt);
				
				namedSqlId = DpAcctQryDao.namedsql_selknlbillchkwithup;
				cntl = DpAcctQryDao.selknlbillchkwithupcnt(acctnostr.toString(), begndt, endddt, true);
				log.debug("***********************已升级" +  cntl + "**************************");
			}
			
			
//			final int cnt = DpAcctQryDao.selknlbillchkcnt(tbkacct.getAcctno(), begndt, endddt, true);
			final int cnt = cntl;			 
			log.debug("***********************" +  cnt + "**************************");			 
//			params.add("acctno", tbkacct.getAcctno());
//			params.add("begndt", begndt);
//			params.add("endddt", endddt);

			final String filesq = dataItem.getFilesq();
			try {
			DaoUtil.selectList(namedSqlId, params,
				new CursorHandler<KnlBill>() {
					
					int seq = 1;
					int count = 0;

					String filename1 = tblKnpParameter.getParm_value1() + cardno + tblKnpParameter.getParm_value2() + filesq + tblKnpParameter.getParm_value2() + seq + tblKnpParameter.getParm_value3();
					String pathname = path1.concat(File.separator).concat(filename1);
					OutputStream out = new FileOutputStream(pathname);
					WritableWorkbook web = getWeb(out,cnt);
					WritableSheet ws = web.getSheet(0);
					
					WritableCellFormat w = new WritableCellFormat(new WritableFont(WritableFont.ARIAL,12,WritableFont.NO_BOLD,true));
					
					@Override
					public boolean handle(int index, KnlBill entity) {
						int m = 0;
						try{
							
							
							//填充Excel表数据
							Label labela1 = new Label(m++, count+1, cardno, w);//卡号
							Label labela2 = new Label(m++, count+1, acctna, w);//户名
							Label labela3 = new Label(m++, count+1, begndt, w);//起始日期
							Label labela4 = new Label(m++, count+1, endddt, w);//终止日期
							Label labela5 = new Label(m++, count+1, entity.getTrandt(), w);//交易日期
							Label labela6 = new Label(m++, count+1, entity.getTrantm(), w);//交易时间
							Label labela7 = new Label(m++, count+1, entity.getAmntcd().getLongName(), w);//借贷标识
							Label labela8 = new Label(m++, count+1, entity.getTrancy(), w);//币种
							Label labela9 = new Label(m++, count+1, entity.getTranam().toString(), w);//交易金额
							Label labela10 = new Label(m++, count+1, entity.getAcctbl().toString(), w);//账户余额
							Label labela11 = new Label(m++, count+1, entity.getOpcuac(), w);//对方账号
							Label labela12 = new Label(m++, count+1, entity.getOpacna(), w);//对方户名
							Label labela13 = new Label(m++, count+1, entity.getSmryds(), w);//摘要
							Label labela14 = new Label(m++, count+1, entity.getRemark(), w);//备注
						
							ws.addCell(labela1);
							ws.addCell(labela2);
							ws.addCell(labela3);
							ws.addCell(labela4);
							ws.addCell(labela5);
							ws.addCell(labela6);
							ws.addCell(labela7);
							ws.addCell(labela8);
							ws.addCell(labela9);
							ws.addCell(labela10);
							ws.addCell(labela11);
							ws.addCell(labela12);
							ws.addCell(labela13);
							ws.addCell(labela14);

							
//							log.debug(ws.getCell(0, 0).getContents());
//							log.debug("**********" + entity.getTrandt() +"*****" + cardno + "**********************");
//							log.debug("**********index:[%s], cnt:[%s]",index,cnt);
							
							count = count + 1;

							if(index == cnt){
								log.debug("**********文件关闭**********");
								web.write(); 
								out.flush();
								web.close();
								out.close();
							}else if(count % ConvertUtil.toInteger(tblKnpParameter.getParm_value4()) == 0){
								log.debug("**********文件关闭**********");
								web.write(); 
								out.flush();
								web.close();
								out.close();
																 
								count = 0;
								seq = seq + 1;
								filename1 = tblKnpParameter.getParm_value1() + cardno + tblKnpParameter.getParm_value2() + filesq + tblKnpParameter.getParm_value2() + seq + tblKnpParameter.getParm_value3();
								pathname = path1.concat(File.separator).concat(filename1);
								out = new FileOutputStream(pathname);
								web = getWeb(out,cnt);
								ws = web.getSheet(0);
								w = new WritableCellFormat(new WritableFont(WritableFont.ARIAL,12,WritableFont.NO_BOLD,true));
							}
							 
							 
						}catch(RowsExceededException e){
							bizlog.debug("[%s]", e);
//							e.printStackTrace();
							try {
								web.write(); 
								out.flush();
								web.close();
								out.close();
							} catch (WriteException e1) {
								log.debug("写文件体错误:[%s]", e.getLocalizedMessage());
					    		throw CaError.Eacct.E0001(e.getLocalizedMessage());
							} catch (Exception e1) {
								log.debug("关闭文件错误:[%s]", e.getLocalizedMessage());
					    		throw CaError.Eacct.E0001(e.getLocalizedMessage());
							}

						} catch (WriteException e) {
							log.debug("写文件体错误:[%s]", e.getLocalizedMessage());
				    		throw CaError.Eacct.E0001(e.getLocalizedMessage());
						} catch (Exception e) {
							log.debug("关闭文件错误:[%s]", e.getLocalizedMessage());
				    		throw CaError.Eacct.E0001(e.getLocalizedMessage());
						}
						return true;
					}
				});
			} catch (FileNotFoundException e) {
				log.debug("文件未找到:[%s]", e.getLocalizedMessage());
	    		throw CaError.Eacct.E0001(e.getLocalizedMessage());
			} catch (Exception e) {
				throw CaError.Eacct.E0001(e.getLocalizedMessage());
			} finally {
				//更新处理状态
				dataItem.setChanel("EMAIL");
				dataItem.setTpltno(tpltno);
				dataItem.setTpltvs(tpltvs);
				dataItem.setDealst(E_PROCST.SUCCESS);
				dataItem.setMnpath(sdfile);
				dataItem.setFlpath(path1);
				dataItem.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
				
				KnbStatDao.updateOne_knb_stat_odb1(dataItem);
			}
						

		}
		@Override
		public void afterTranProcess(String taskId, cn.sunline.ltts.busi.dptran.batchtran.intf.Chkbil.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Chkbil.Property property) {
				//写总文件
			 	
			String path1 = sdfile;
	        bizlog.debug("文件产生路径 path:[" + path1 + "]");
	        
	        final KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("CHKB", "filetp", "%", "%", false);
			if(CommUtil.isNull(tblKnpParameter)){
				throw CaError.Eacct.E0001("xls文件格式参数未配置");
			}
	        
	        // 获取文件名
	        String filename1 = tblKnpParameter.getParm_value1() + UUID.randomUUID().toString().replaceAll("\\-", "") +".ok";
	        bizlog.debug("文件名称 filename:[" + filename1 + "]");
	        
			
	        final LttsFileWriter file = new LttsFileWriter(path1, filename1, "UTF-8");
            
	        Params params = new Params();
            String namedSqlId = "";//查询数据集的命名sql
            params.add("dealst", E_PROCST.SUCCESS);
            params.add("trandt", trandt);
            params.add("filesq", input.getFilesq());
            
            namedSqlId = DpAcctQryDao.namedsql_selknbstatAll;
            
            file.open();
            try{
            	//写文件头
            	StringBuffer file_Info = SysUtil.getInstance(StringBuffer.class);// 拼接字符串
            	file_Info.append("EMAIL");  //渠道类型
            	file_Info.append(fengefu).append(tpltno);  //模板标识
            	file_Info.append(fengefu).append(tpltvs);  //模版版本号
            	file_Info.append(fengefu).append("93");  //系统代码
            	
            	// 打印文件
				file.write(file_Info.toString());
            	
            	DaoUtil.selectList(namedSqlId, params, new CursorHandler<KnbStat>() {

					@Override
					public boolean handle(int index, KnbStat entity) {
						
						// 写文件
						StringBuffer file_Info = SysUtil.getInstance(StringBuffer.class);// 拼接字符串
						
						
						String cardno = entity.getCardno();//卡号，用于拼接路径
						String begndt = entity.getBegndt();
						String endddt = entity.getEndddt();
						//根据卡号查客户号
						
						
						IoCucifCust  tblknaAcdc = DpAcctDao.selCifcustbyCardno(cardno, true);
					    int cnt = DpAcctQryDao.selknlbillchkcnt(cardno, begndt, endddt, true);//获取账单总条数
						int seq = 1;//文件后缀
						int counts = cnt/ConvertUtil.toInteger(tblKnpParameter.getParm_value4());
						int rem = cnt%ConvertUtil.toInteger(tblKnpParameter.getParm_value4());
						//判断该账号生成了几个账单表
						//若未查到数据，则生成一个空文件
						if(counts == 0 && rem == 0){
							counts = counts + 1;
						}
						//若余数不为0，则多生成一个文件
						if(rem != 0){
							counts = counts + 1;
						}
					    String custno = tblknaAcdc.getCustno(); //客户号
						
						
						
						String str = cardno.substring(cardno.length()-4, cardno.length());
						Integer month = DateTools2.getMonth(DateTools2.covStringToDate(begndt)) + 1;//月份
						
						map.put("X1", str);
						map.put("X2", month.toString());
						
						String parms = JsonUtil.format(map); // 参数
						
						//String filepath = snpath + tblKnpParameter.getParm_value1() + cardno + tblKnpParameter.getParm_value2() + entity.getFilesq() + tblKnpParameter.getParm_value3();
						
						String brchno = "";                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
						String maildr = (CommUtil.isNotNull(entity.getMaildr()) ? entity.getMaildr() : ""); //发送目标
						String themes = "电子账户对账单"; //邮件主题
						DefaultOptions<String> filelist = new DefaultOptions<String>();
						
						for(seq=1; seq <= counts; seq++){
							String filepath = snpath + tblKnpParameter.getParm_value1() + cardno + tblKnpParameter.getParm_value2() + entity.getFilesq() + tblKnpParameter.getParm_value2() + seq + tblKnpParameter.getParm_value3();
							
							Map<String,String> map1 = new HashMap<String,String>();
							map1.put("filePath", filepath);
							String attach = JsonUtil.format(map1); //附件列表
							
							filelist.add(attach);
						}
//						Map<String,String> map1 = new HashMap<String,String>();
//						map1.put("filePath", filepath);
//						String attach = JsonUtil.toJson(map1); //附件列表
//						
//						DefaultOptions<String> filelist = new DefaultOptions<String>();
//						filelist.add(attach);
						
						
						if(CommUtil.isNotNull(custno)){
							
//							IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//							queryCust.setCustno(custno);
//							IoSrvCfPerson.IoGetCifCust.Output lst = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//							SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, lst);
							
							
//							brchno = (CommUtil.isNotNull(lst.getBrchno()) ? lst.getBrchno() : "");
							
						}
						for(int i = 0;i < filelist.size(); i++){
							file_Info = SysUtil.getInstance(StringBuffer.class);// 拼接字符串
							DefaultOptions<String> filelist1 = new DefaultOptions<String>();
							filelist1.add(filelist.get(i));
							file_Info.append(brchno);
							file_Info.append(fengefu).append(maildr);
							file_Info.append(fengefu).append(parms);
							file_Info.append(fengefu).append(themes);
							file_Info.append(fengefu).append(filelist1);
							file_Info.append(fengefu).append(custno);
							
							// 打印文件
							file.write(file_Info.toString());
							
						}
						return true;
					}
				});
            	
            }finally{
            	file.close();
            }
            
            
            
            try {
	        	 md5 = MD5EncryptUtil.getFileMD5String(new File(path1.concat(File.separator).concat(filename1)));
			} catch (Exception e) {
				throw ApError.BusiAplt.E0042(filename1);
			}
	        
	        batch.setFilenm(filename1);
	        batch.setFlpath(path2);
	        batch.setFilemd(md5);
	        
	        ls.add(batch);
		
	        //发消息
	        E_SYSCCD srocue = E_SYSCCD.NAS;//原系统
	        E_SYSCCD target = E_SYSCCD.MMP; //待确定
			E_FILETP filetp = E_FILETP.NA000002; 
//			String status=E_FILEST.SUCC.getValue();
//			String descri = "电子账户对账单";
//			DefaultOptions<E_SYSCCD> ls = new DefaultOptions<>();
//			ls.add(target);
			
			targetList targets = SysUtil.getInstance(targetList.class);
			targets.setTarget(target);
			
			DefaultOptions<targetList> ls =new DefaultOptions<targetList>();
			ls.add(targets);
			
			SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBackSynNotice(srocue, trandt, filetp, filename1, md5, ls);
	        
	}
	
	
	
	/**
	 * 获取数据遍历器。
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbStat> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.intf.Chkbil.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Chkbil.Property property) {
		Params params = new Params();
		params.add("dealst", E_PROCST.SUSPEND);
		params.add("filesq", input.getFilesq());
		params.add("trandt", CommTools.getBaseRunEnvs().getTrxn_date());
		
		return new CursorBatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbStat>(DpAcctQryDao.namedsql_selknbstatAll, params);
	}
	

}