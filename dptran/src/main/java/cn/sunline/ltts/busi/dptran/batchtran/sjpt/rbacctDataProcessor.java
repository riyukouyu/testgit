package cn.sunline.ltts.busi.dptran.batchtran.sjpt;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.FileUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.edsp.base.file.FileDataExecutor;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.dp.namedsql.sjpt.sjptdtDao;
import cn.sunline.ltts.busi.dp.type.sjpt.sjptdt.rbacctInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_QIXIANDW;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRRTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_OFCHAR;
	 /**
	  * 存款账户信息
	  * 存款账户信息
	  *
	  */

public class rbacctDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbacct.Input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbacct.Property> {
	private static final BizLog bizlog = BizLogUtil
			.getBizLog(rbacctDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbacct.Input input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbacct.Property property) {
		//从公共参数表中获取需要写出的文件路径信息
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.rbacct", "rbacctFile", "rbacctData", "%", true);
		 
		/*KnpParameter sqNoKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.sqno", "sqno", "number", "%", true);
		String pmval1 = sqNoKnpParameter.getParm_value1();//当前值
*/		// 产生文件的日期目录
		//final String trandt = DateTools2.getDateInfo().getLastdt();//获取上次交易日期
		ApSysDateStru ap = DateTools2.getDateInfo();
		final String trandt = ap.getLastdt();
		String bflsdt = ap.getBflsdt();		
		// 产生文件的日期目录
		String filePath = tblKnpParameter.getParm_value1() + File.separator + trandt;
		//String filePath = "E:\\123\\";
		String fileName = tblKnpParameter.getParm_value2() + E_OFCHAR.UND + trandt + E_OFCHAR.UND + tblKnpParameter.getParm_value3() + E_OFCHAR.UND + "0001" + tblKnpParameter.getParm_value4();
		 
		bizlog.debug("贷款还款计划明细文件:[" + filePath + fileName + "]");
		 
		// 获取是否产生文件标志
		final LttsFileWriter file = new LttsFileWriter(filePath, fileName);
		
		Params params = new Params();
		params.add("trandt", trandt); 
		params.add("bflsdt", bflsdt); 
		if(true){
	    	file.open();
	    	try {
	    		final StringBuilder end = new StringBuilder("0");
	    		DaoUtil.selectList(sjptdtDao.namedsql_selRbAcct, params, new CursorHandler<cn.sunline.ltts.busi.dp.type.sjpt.sjptdt.rbacctInfo>() {

					@Override
					public boolean handle(int arg0, rbacctInfo arg1) {
						StringBuffer lnreq = new StringBuffer();
						lnreq.append(trandt).append(E_OFCHAR.VER);						//数据日期
						String acctno				 = arg1.getAcctno();				// 账号
						lnreq.append(acctno).append(E_OFCHAR.VER);
						String subsac 				 = arg1.getSubsac() ;				// 序号
						lnreq.append(subsac).append(E_OFCHAR.VER);
						String acctst				 = arg1.getAcctst()	;				// 账户状态
						lnreq.append(acctst).append(E_OFCHAR.VER);
						String opendt				 = arg1.getOpendt();				// 开户日期
						lnreq.append(opendt).append(E_OFCHAR.VER);
						String closdt				 = arg1.getClosdt();				// 销户日期
						lnreq.append(closdt).append(E_OFCHAR.VER);
						String spectp				 = arg1.getSpectp().getValue()	;	// 负债账户性质
						lnreq.append(spectp).append(E_OFCHAR.VER);
						String matudt				 = arg1.getMatudt()	;				// 存单到期日.
						lnreq.append(matudt).append(E_OFCHAR.VER);
						String pddpfg				 = arg1.getPddpfg();				// 定活标识
						lnreq.append(pddpfg).append(E_OFCHAR.VER);
						String finfla		 = arg1.getFinfla();						// 财政性存款标识
						lnreq.append(finfla).append(E_OFCHAR.VER);
						String unfpri	 	 = arg1.getUnfpri();						// 对公对私标识
						lnreq.append(unfpri).append(E_OFCHAR.VER);
						String brchno				 = arg1.getBrchno()	;				// 机构号
						lnreq.append(brchno).append(E_OFCHAR.VER);
						String custno				 = arg1.getCustno()	;				// 客户号
						lnreq.append(custno).append(E_OFCHAR.VER);
						BigDecimal onlnbl			 = arg1.getOnlnbl()	;				// 余额
						lnreq.append(onlnbl).append(E_OFCHAR.VER);
						String crcycd				 = arg1.getCrcycd()	;				// 币种
						lnreq.append(crcycd).append(E_OFCHAR.VER);
						BigDecimal hdmxmy			 = arg1.getHdmxmy()	;				// 账户限制
						lnreq.append(hdmxmy).append(E_OFCHAR.VER);
						String opensv				 = arg1.getOpensv();
						lnreq.append(opensv).append(E_OFCHAR.VER);						// 来源系统ID
						String capamt			 = arg1.getCapamt();					// 资本金帐户已投入资本金数额
						lnreq.append(capamt).append(E_OFCHAR.VER);
						String acctcd				 = arg1.getAcctcd()	;				// 科目代码
						lnreq.append(acctcd).append(E_OFCHAR.VER);
						//String matudt				 = arg1.getMatudt()	;				// 实际到期日
						lnreq.append(matudt).append(E_OFCHAR.VER);
						String opentm				 = arg1.getOpentm();				// 开户时间
						lnreq.append(opentm).append(E_OFCHAR.VER);
						String clostm			 	= arg1.getClostm();					// 销户时间
						lnreq.append(clostm).append(E_OFCHAR.VER);
						String prodcd				 = arg1.getProdcd();				// 产品代码
						lnreq.append(prodcd).append(E_OFCHAR.VER);
						String isfdep	 = arg1.getIsfdep();							// 是否为理财存款
						lnreq.append(isfdep).append(E_OFCHAR.VER);
						String bgindt				 = arg1.getBgindt()	;				// 起始计息日期
						lnreq.append(bgindt).append(E_OFCHAR.VER);
						String txbefr				 = arg1.getTxbefr()	;				// 结息周期
						String txbefrValue = "";
						String txbefrF = "1";				//frequency_int 结息频率 按月活一个月内结息为1，按季结息为1，按年为1，按半年为2
						if (CommUtil.isNotNull(txbefr)) {
							int iFlag = 0;
					        for (int i = 0; i < txbefr.length(); i++) {
					            if (!Character.isDigit(txbefr.charAt(i))) {
					                iFlag = i;
					                break;
					            }
					        }
					        String sQiXianDW = txbefr.substring(iFlag, iFlag + 1);
					        if (sQiXianDW.equals(E_QIXIANDW.Day.getValue())) {
					        	txbefrValue = "B04";
							}else if (sQiXianDW.equals(E_QIXIANDW.Month.getValue())) {
								txbefrValue = "B03";
							}else if (sQiXianDW.equals(E_QIXIANDW.Quart.getValue())) {
								txbefrValue = "B02";
							}else if (sQiXianDW.equals(E_QIXIANDW.HalfYear.getValue())) {
								txbefrValue = "B05";
								txbefrF = "2";
							}else if (sQiXianDW.equals(E_QIXIANDW.Year.getValue())) {
								txbefrValue = "B01";
							}else{
								txbefrValue = "C";
							}
						}
						lnreq.append(txbefrValue).append(E_OFCHAR.VER);
						String restyp		 = arg1.getRestyp();						// 存款准备金交存方式
						lnreq.append(restyp).append(E_OFCHAR.VER);
						String sworty			 = arg1.getSworty();					// 存款经营类型
						lnreq.append(sworty).append(E_OFCHAR.VER);
						String incdtp				 = arg1.getIncdtp();				// 利率是否固定
						lnreq.append(incdtp).append(E_OFCHAR.VER);
						String depttm				 = arg1.getDepttm();				// 存款合同期限
						lnreq.append(depttm).append(E_OFCHAR.VER);
						//需要加工,跟行方的数据完全不同，先写空
						E_IRRTTP inprwy  = arg1.getInprwy();
						String inprwy1 = "RR01";
						if (CommUtil.isNotNull(inprwy)) {
							if (inprwy.getValue().equals("1")) {//不重定价
								inprwy1 = "";
							}
						}
						lnreq.append(inprwy1).append(E_OFCHAR.VER);						// 利率重定价方式
						//lnreq.append("RR01").append(E_OFCHAR.VER);
						/*String inadlv				 = arg1.getInadlv();				// 利率调整频率
						lnreq.append(inadlv).append(E_OFCHAR.VER);*/
						String custac				 = arg1.getCustac();				// 主账户账号
						lnreq.append(custac).append(E_OFCHAR.VER);
						String geninm	 = arg1.getGeninm();							// 计息方式
						lnreq.append(geninm).append(E_OFCHAR.VER);
						String genter				 = arg1.getGenter();				// 计息期限
						lnreq.append(genter).append(E_OFCHAR.VER);
						String debttp				 = arg1.getDebttp();				// 存款产品类别
						lnreq.append(debttp).append(E_OFCHAR.VER);
						String autosf = arg1.getAutosf();
						lnreq.append(autosf).append(E_OFCHAR.VER);						// 是否自动转存
						String trsvtp = arg1.getTrsvtp();
						lnreq.append(trsvtp).append(E_OFCHAR.VER);						// 自动转存类型
						lnreq.append(arg1.getAccttp()).append(E_OFCHAR.VER);			// 结算账户标识
						lnreq.append(arg1.getCsextg()).append(E_OFCHAR.VER);			// 钞汇标志
						String acctna				 = arg1.getAcctna();				// 账户名称
						lnreq.append(acctna).append(E_OFCHAR.VER);
						String purpose				 = arg1.getPurpose();				// 账户用途
						lnreq.append(purpose).append(E_OFCHAR.VER);
						String trantm				 = arg1.getTrantm();				// 最后一次交易时间
						lnreq.append(trantm).append(E_OFCHAR.VER);
						BigDecimal cuusin			 = arg1.getCuusin();				// 实际利率
						lnreq.append(cuusin).append(E_OFCHAR.VER);
						BigDecimal bsintr			 = arg1.getBsintr();				// 基准利率
						lnreq.append(bsintr).append(E_OFCHAR.VER);
						String acccts				 = arg1.getAcccts();				// 核心账户状态
						lnreq.append(acccts).append(E_OFCHAR.VER);
						String acstad		 = arg1.getAcstad();						// 账户状态变更时间
						lnreq.append(acstad).append(E_OFCHAR.VER);
						BigDecimal planin			 = arg1.getPlanin();				// 应计利息
						lnreq.append(planin).append(E_OFCHAR.VER);
						BigDecimal taxrat		     = arg1.getTaxrat();				// 利息税率
						lnreq.append(taxrat).append(E_OFCHAR.VER);
						BigDecimal acclim			 = arg1.getAcclim();					// 协定账户额度
						lnreq.append(acclim).append(E_OFCHAR.VER);
						String accrat			 = arg1.getAccrat();					// 协定利率
						lnreq.append(accrat).append(E_OFCHAR.VER);
						String intrcd				 = arg1.getIntrcd();				// 利率代码
						lnreq.append(intrcd).append(E_OFCHAR.VER);
						//String txbefr				 = arg1.getTxbefr();				// 结息频率
						//lnreq.append(txbefrValue).append(E_OFCHAR.VER);
						lnreq.append(txbefrF).append(E_OFCHAR.VER);
						String accmad	 			= arg1.getAccmad();					// 协议到期日
						lnreq.append(accmad).append(E_OFCHAR.VER);
						String termty			 = arg1.getTermty();					// 期限类型
						lnreq.append(termty).append(E_OFCHAR.VER);
						//String acctno				 = arg1.getAcctno();				// 结息账户账号
						lnreq.append(acctno).append(E_OFCHAR.VER);
						BigDecimal openam				 = arg1.getOpenam();				// 开户金额
						lnreq.append(openam).append(E_OFCHAR.VER);
						String txbebs				 = arg1.getTxbebs();				// 计息方式代码
						lnreq.append(txbebs).append(E_OFCHAR.VER);
						BigDecimal accint		 = arg1.getAccint();						// 当日利息
						lnreq.append(accint).append(E_OFCHAR.VER);
						Long drmxtm			 		 = arg1.getDrmxtm();			 	// 部提次数
						lnreq.append(drmxtm).append(E_OFCHAR.VER);
						String openus 				 = arg1.getOpenus(); 				// 开户柜员
						lnreq.append(openus).append(E_OFCHAR.VER);
						String acchad 	 = arg1.getAcchad(); 							// 动帐日期
						lnreq.append(acchad).append(E_OFCHAR.VER);
						String demada 	 = arg1.getDemada(); 							// 延期到期日
						lnreq.append(demada).append(E_OFCHAR.VER);
						String unpamo 		 = arg1.getUnpamo(); 						// 已结未付
						lnreq.append(unpamo).append(E_OFCHAR.VER);
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
			}finally {
				file.close();
			}
	    }
	 
	   bizlog.debug("数据平台供述-贷款产品参数 ：" + fileName + "文件产生完成");
	   List<String> fileDataList = new ArrayList<String>();
		int size = fileDataList.size();
		
//		File oldfile = new File(filePath + File.separator + fileName); //已生成的文件
      
       fileDataList = readFile(new File(filePath + File.separator + fileName));
           if(fileDataList.size()>0){
               String lastLine = fileDataList.get(fileDataList.size()-1);
               if(lastLine.contains("END")){
                   fileDataList.remove(fileDataList.size()-1);  //最后一行删除
               }
               size = fileDataList.size();
           }
           LttsFileWriter fileWriter = null;  
           //BufferedReader bre = null;
		try {

		//bre = new BufferedReader(new FileReader(oldfile)); //读取已存在文件到reader缓存中
       fileWriter = new LttsFileWriter(filePath + File.separator, fileName);
       for (String oldLine : fileDataList){   //将每一行分别写到writer中，完成已存在文件的读写
           fileWriter.write(oldLine);
           fileWriter.write(System.getProperty("line.separator"));
       }
			List<rbacctInfo> list = sjptdtDao.selRbacct(trandt, bflsdt, false);
			for (rbacctInfo arg1 : list) {
				StringBuffer lnreq = new StringBuffer();
				lnreq.append(trandt).append(E_OFCHAR.VER);						//数据日期
				String acctno				 = arg1.getAcctno();				// 账号
				lnreq.append(acctno).append(E_OFCHAR.VER);
				String subsac 				 = arg1.getSubsac() ;				// 序号
				lnreq.append(subsac).append(E_OFCHAR.VER);
				String acctst				 = arg1.getAcctst()	;				// 账户状态
				lnreq.append(acctst).append(E_OFCHAR.VER);
				String opendt				 = arg1.getOpendt();				// 开户日期
				lnreq.append(opendt).append(E_OFCHAR.VER);
				String closdt				 = arg1.getClosdt();				// 销户日期
				lnreq.append(closdt).append(E_OFCHAR.VER);
				String spectp				 = arg1.getSpectp().getValue()	;	// 负债账户性质
				lnreq.append(spectp).append(E_OFCHAR.VER);
				String matudt				 = arg1.getMatudt()	;				// 存单到期日.
				lnreq.append(matudt).append(E_OFCHAR.VER);
				String pddpfg				 = arg1.getPddpfg();				// 定活标识
				lnreq.append(pddpfg).append(E_OFCHAR.VER);
				String finfla		 = arg1.getFinfla();						// 财政性存款标识
				lnreq.append(finfla).append(E_OFCHAR.VER);
				String unfpri	 	 = arg1.getUnfpri();						// 对公对私标识
				lnreq.append(unfpri).append(E_OFCHAR.VER);
				String brchno				 = arg1.getBrchno()	;				// 机构号
				lnreq.append(brchno).append(E_OFCHAR.VER);
				String custno				 = arg1.getCustno()	;				// 客户号
				lnreq.append(custno).append(E_OFCHAR.VER);
				BigDecimal onlnbl			 = arg1.getOnlnbl()	;				// 余额
				lnreq.append(onlnbl).append(E_OFCHAR.VER);
				String crcycd				 = arg1.getCrcycd()	;				// 币种
				lnreq.append(crcycd).append(E_OFCHAR.VER);
				BigDecimal hdmxmy			 = arg1.getHdmxmy()	;				// 账户限制
				lnreq.append(hdmxmy).append(E_OFCHAR.VER);
				String opensv				 = arg1.getOpensv();
				lnreq.append(opensv).append(E_OFCHAR.VER);						// 来源系统ID
				String capamt			 = arg1.getCapamt();					// 资本金帐户已投入资本金数额
				lnreq.append(capamt).append(E_OFCHAR.VER);
				String acctcd				 = arg1.getAcctcd()	;				// 科目代码
				lnreq.append(acctcd).append(E_OFCHAR.VER);
				//String matudt				 = arg1.getMatudt()	;				// 实际到期日
				lnreq.append(matudt).append(E_OFCHAR.VER);
				String opentm				 = arg1.getOpentm();				// 开户时间
				lnreq.append(opentm).append(E_OFCHAR.VER);
				String clostm			 	= arg1.getClostm();					// 销户时间
				lnreq.append(clostm).append(E_OFCHAR.VER);
				String prodcd				 = arg1.getProdcd();				// 产品代码
				lnreq.append(prodcd).append(E_OFCHAR.VER);
				String isfdep	 = arg1.getIsfdep();							// 是否为理财存款
				lnreq.append(isfdep).append(E_OFCHAR.VER);
				String bgindt				 = arg1.getBgindt()	;				// 起始计息日期
				lnreq.append(bgindt).append(E_OFCHAR.VER);
				String txbefr				 = arg1.getTxbefr()	;				// 结息周期
				String txbefrValue = "";
				String txbefrF = "1";				//frequency_int 结息频率 按月活一个月内结息为1，按季结息为1，按年为1，按半年为2
				if (CommUtil.isNotNull(txbefr)) {
					int iFlag = 0;
			        for (int i = 0; i < txbefr.length(); i++) {
			            if (!Character.isDigit(txbefr.charAt(i))) {
			                iFlag = i;
			                break;
			            }
			        }
			        String sQiXianDW = txbefr.substring(iFlag, iFlag + 1);
			        if (sQiXianDW.equals(E_QIXIANDW.Day.getValue())) {
			        	txbefrValue = "B04";
					}else if (sQiXianDW.equals(E_QIXIANDW.Month.getValue())) {
						txbefrValue = "B03";
					}else if (sQiXianDW.equals(E_QIXIANDW.Quart.getValue())) {
						txbefrValue = "B02";
					}else if (sQiXianDW.equals(E_QIXIANDW.HalfYear.getValue())) {
						txbefrValue = "B05";
						txbefrF = "2";
					}else if (sQiXianDW.equals(E_QIXIANDW.Year.getValue())) {
						txbefrValue = "B01";
					}else{
						txbefrValue = "C";
					}
				}else{
					txbefrValue = "A";
				}
				lnreq.append(txbefrValue).append(E_OFCHAR.VER);
				String restyp		 = arg1.getRestyp();						// 存款准备金交存方式
				lnreq.append(restyp).append(E_OFCHAR.VER);
				String sworty			 = arg1.getSworty();					// 存款经营类型
				lnreq.append(sworty).append(E_OFCHAR.VER);
				String incdtp				 = arg1.getIncdtp();				// 利率是否固定
				lnreq.append(incdtp).append(E_OFCHAR.VER);
				String depttm				 = arg1.getDepttm();				// 存款合同期限
				lnreq.append(depttm).append(E_OFCHAR.VER);
				//需要加工,跟行方的数据完全不同，先写空
				E_IRRTTP inprwy  = arg1.getInprwy();
				String inprwy1 = "RR01";
				if (CommUtil.isNotNull(inprwy)) {
					if (inprwy.getValue().equals("1")) {//不重定价
						inprwy1 = "";
					}
				}
				lnreq.append(inprwy1).append(E_OFCHAR.VER);						// 利率重定价方式
				//lnreq.append("RR01").append(E_OFCHAR.VER);
				/*String inadlv				 = arg1.getInadlv();				// 利率调整频率
				lnreq.append(inadlv).append(E_OFCHAR.VER);*/
				String custac				 = arg1.getCustac();				// 主账户账号
				lnreq.append(custac).append(E_OFCHAR.VER);
				String geninm	 = arg1.getGeninm();							// 计息方式
				lnreq.append(geninm).append(E_OFCHAR.VER);
				String genter				 = arg1.getGenter();				// 计息期限
				lnreq.append(genter).append(E_OFCHAR.VER);
				String debttp				 = arg1.getDebttp();				// 存款产品类别
				lnreq.append(debttp).append(E_OFCHAR.VER);
				String autosf = arg1.getAutosf();
				lnreq.append(autosf).append(E_OFCHAR.VER);						// 是否自动转存
				String trsvtp = arg1.getTrsvtp();
				lnreq.append(trsvtp).append(E_OFCHAR.VER);						// 自动转存类型
				lnreq.append(arg1.getAccttp()).append(E_OFCHAR.VER);			// 结算账户标识
				lnreq.append(arg1.getCsextg()).append(E_OFCHAR.VER);			// 钞汇标志
				String acctna				 = arg1.getAcctna();				// 账户名称
				lnreq.append(acctna).append(E_OFCHAR.VER);
				String purpose				 = arg1.getPurpose();				// 账户用途
				lnreq.append(purpose).append(E_OFCHAR.VER);
				String trantm				 = arg1.getTrantm();				// 最后一次交易时间
				lnreq.append(trantm).append(E_OFCHAR.VER);
				BigDecimal cuusin			 = arg1.getCuusin();				// 实际利率
				lnreq.append(cuusin).append(E_OFCHAR.VER);
				BigDecimal bsintr			 = arg1.getBsintr();				// 基准利率
				lnreq.append(bsintr).append(E_OFCHAR.VER);
				String acccts				 = arg1.getAcccts();				// 核心账户状态
				lnreq.append(acccts).append(E_OFCHAR.VER);
				String acstad		 = arg1.getAcstad();						// 账户状态变更时间
				lnreq.append(acstad).append(E_OFCHAR.VER);
				BigDecimal planin			 = arg1.getPlanin();				// 应计利息
				lnreq.append(planin).append(E_OFCHAR.VER);
				BigDecimal taxrat		     = arg1.getTaxrat();				// 利息税率
				lnreq.append(taxrat).append(E_OFCHAR.VER);
				BigDecimal acclim			 = arg1.getAcclim();					// 协定账户额度
				lnreq.append(acclim).append(E_OFCHAR.VER);
				String accrat			 = arg1.getAccrat();					// 协定利率
				lnreq.append(accrat).append(E_OFCHAR.VER);
				String intrcd				 = arg1.getIntrcd();				// 利率代码
				lnreq.append(intrcd).append(E_OFCHAR.VER);
				//String txbefr				 = arg1.getTxbefr();				// 结息频率
				//lnreq.append(txbefrValue).append(E_OFCHAR.VER);
				lnreq.append(txbefrF).append(E_OFCHAR.VER);
				String accmad	 			= arg1.getAccmad();					// 协议到期日
				lnreq.append(accmad).append(E_OFCHAR.VER);
				String termty			 = arg1.getTermty();					// 期限类型
				lnreq.append(termty).append(E_OFCHAR.VER);
				//String acctno				 = arg1.getAcctno();				// 结息账户账号
				lnreq.append(acctno).append(E_OFCHAR.VER);
				BigDecimal openam				 = arg1.getOpenam();				// 开户金额
				lnreq.append(openam).append(E_OFCHAR.VER);
				String txbebs				 = arg1.getTxbebs();				// 计息方式代码
				lnreq.append(txbebs).append(E_OFCHAR.VER);
				BigDecimal accint		 = arg1.getAccint();						// 当日利息
				lnreq.append(accint).append(E_OFCHAR.VER);
				Long drmxtm			 		 = arg1.getDrmxtm();			 	// 部提次数
				lnreq.append(drmxtm).append(E_OFCHAR.VER);
				String openus 				 = arg1.getOpenus(); 				// 开户柜员
				lnreq.append(openus).append(E_OFCHAR.VER);
				String acchad 	 = arg1.getAcchad(); 							// 动帐日期
				lnreq.append(acchad).append(E_OFCHAR.VER);
				String demada 	 = arg1.getDemada(); 							// 延期到期日
				lnreq.append(demada).append(E_OFCHAR.VER);
				String unpamo 		 = arg1.getUnpamo(); 						// 已结未付
				lnreq.append(unpamo).append(E_OFCHAR.VER);

				fileWriter.write(lnreq.toString());
				fileWriter.write(System.getProperty("line.separator"));
				size ++;
			}
			fileWriter.write("END" + E_OFCHAR.VER + Integer.toString(size) + E_OFCHAR.VER);
			
		} catch (Exception e) {
			//Log.error(e.toString());
			DpModuleError.DpstComm.E9999(e.toString()); 
		 
	} finally {
		fileWriter.close();
	}
	 }

	private List<String> readFile(File file) {

		final List<String> lines = new ArrayList<String>();
		FileUtil.readFile(file.getAbsolutePath(), new FileDataExecutor() {

			@Override
			public void process(int index, String line) {
				lines.add(line);
			}

		});
		return lines;
	
	}
}


