package cn.sunline.ltts.busi.dptran.batchtran.sjpt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import cn.sunline.ltts.busi.dp.namedsql.sjpt.sjptdtDao;
import cn.sunline.ltts.busi.dp.type.sjpt.sjptdt.rbtranhisInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_OFCHAR;

/**
 * 存款金融交易明细 
 * 
 */

public class rbtranhisDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbtranhis.Input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbtranhis.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(rbintamtDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(
			cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbtranhis.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbtranhis.Property property) {
		// 获取存款金融交易明细文件相关数据
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.rbtranhis", "rbtranhisFile", "rbtranhisData", "%", true);

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
			DaoUtil.selectList(sjptdtDao.namedsql_selRbtranhis, params, new CursorHandler<rbtranhisInfo>() {
					@Override
					public boolean handle(int arg0, rbtranhisInfo arg1) {
						StringBuffer lnreq = new StringBuffer();
						String	transq = arg1.getTransq();		//	交易流水号
						lnreq.append(transq).append(E_OFCHAR.VER);
						String	trandtt = arg1.getTrandt();     //   交易日期
						if (CommUtil.isNotNull(trandtt)) {
							trandtt = trandtt.substring(0, trandtt.indexOf("."));
						}
						lnreq.append(trandtt).append(E_OFCHAR.VER);
						String	atowtp = arg1.getAtowtp();     //   现金交易标识		
						lnreq.append(atowtp).append(E_OFCHAR.VER);
						String	toacna = arg1.getToacna();		//	交易对手名称	
						lnreq.append(toacna).append(E_OFCHAR.VER);
						String	strktp = arg1.getStrktp();     //   冲正标识	
						lnreq.append(strktp).append(E_OFCHAR.VER);
						String	soumod = arg1.getSoumod();     //   来源模块	
						lnreq.append(soumod).append(E_OFCHAR.VER);
						String	agenta = arg1.getAgenta();     //   代办人名称	
						lnreq.append(agenta).append(E_OFCHAR.VER);
						String	agentc = arg1.getAgentc();     //   代办人证件类型
						lnreq.append(agentc).append(E_OFCHAR.VER);
						String	agentn = arg1.getAgentn();     //   代办人证件号码	
						lnreq.append(agentn).append(E_OFCHAR.VER);
						String	agenna = arg1.getAgenna();     //   代办人国籍	
						lnreq.append(agenna).append(E_OFCHAR.VER);
						//String	datada = arg1.getDatada();     //   数据日期	
						lnreq.append(trandt).append(E_OFCHAR.VER);
						String	invoft = arg1.getInvoft();     //   涉外收支交易代码
						lnreq.append(invoft).append(E_OFCHAR.VER);
						String	coubrt = arg1.getCoubrt();     //   对方行号类型	
						lnreq.append(coubrt).append(E_OFCHAR.VER);
						String	tranbr = arg1.getTranbr();     //   机构号		
						lnreq.append(tranbr).append(E_OFCHAR.VER);
						String	opbrch = arg1.getOpbrch();     //   交易对手行号	
						lnreq.append(opbrch).append(E_OFCHAR.VER);
						String	crcycd = arg1.getCrcycd();     //   币种		
						lnreq.append(crcycd).append(E_OFCHAR.VER);
						String	custno = arg1.getCustno();     //   客户号		
						lnreq.append(custno).append(E_OFCHAR.VER);
						BigDecimal	tranam = arg1.getTranam();     //   交易金额	
						lnreq.append(tranam).append(E_OFCHAR.VER);
						String	toacct = arg1.getToacct();     //   对方账户号	
						lnreq.append(toacct).append(E_OFCHAR.VER);
						String	acctno = arg1.getAcctno();     //   账号
						lnreq.append(acctno).append(E_OFCHAR.VER);
						String	oppodc = arg1.getOppodc();     //   对方所在地区	
						lnreq.append(oppodc).append(E_OFCHAR.VER);
						String	bankna = arg1.getBankna();     //   对方银行名称	
						lnreq.append(bankna).append(E_OFCHAR.VER);
						String	amntcd = arg1.getAmntcd();     //   资金收付标识	
						lnreq.append(amntcd).append(E_OFCHAR.VER);
						String	drtion = arg1.getDrtion();     //   交易去向
						lnreq.append(drtion).append(E_OFCHAR.VER);
						String	remark = arg1.getRemark();     //   交易目的	
						lnreq.append(remark).append(E_OFCHAR.VER);
						String	inlind = arg1.getInlind();     //   境内外标识	
						lnreq.append(inlind).append(E_OFCHAR.VER);
						String	partct = arg1.getPartct();     //   交易对手客户类型
						lnreq.append(partct).append(E_OFCHAR.VER);
						String	trantd = arg1.getTrantd();     //   交易方式明细	
						lnreq.append(trantd).append(E_OFCHAR.VER);
						String	smryds = arg1.getSmryds();     //   资金来源或用途	
						lnreq.append(smryds).append(E_OFCHAR.VER);
						String	place = arg1.getPlace();       //   交易发生地		
						lnreq.append(place).append(E_OFCHAR.VER);
						String	partty = arg1.getPartty();     //   交易对手证件类型
						lnreq.append(partty).append(E_OFCHAR.VER);
						String	partid = arg1.getPartid();     //   交易对手证件号码
						lnreq.append(partid).append(E_OFCHAR.VER);
						String	bltype = arg1.getBltype();     //   交易种类		
						lnreq.append(bltype).append(E_OFCHAR.VER);
						String	partat = arg1.getPartat();     //   交易对手账户类型
						lnreq.append(partat).append(E_OFCHAR.VER);
						String	cifcty = arg1.getCifcty();     //   客户类型	
						lnreq.append(cifcty).append(E_OFCHAR.VER);
						String	sortno = arg1.getSortno();     //   交易序号	
						lnreq.append(sortno).append(E_OFCHAR.VER);
						String	custna = arg1.getCustna();     //   客户名称	
						lnreq.append(custna).append(E_OFCHAR.VER);
						String	cardno = arg1.getCardno();     //   账户号码	
						lnreq.append(cardno).append(E_OFCHAR.VER);
						String	flgflg = arg1.getFlgflg();     //   交易标志
						lnreq.append(flgflg).append(E_OFCHAR.VER);
						String	subsac = arg1.getSubsac();     //   账户序号	
						lnreq.append(subsac).append(E_OFCHAR.VER);
						//String	servtp = arg1.getServtp();     //   交易渠道	
						lnreq.append("Y5").append(E_OFCHAR.VER);
						String	intrcd = arg1.getIntrcd();     //   交易代码	
						lnreq.append(intrcd).append(E_OFCHAR.VER);
						String	termin = arg1.getTermin();     //   终端号		
						lnreq.append(termin).append(E_OFCHAR.VER);
						String	mcccod = arg1.getMcccod();     //   商户类型	
						lnreq.append(mcccod).append(E_OFCHAR.VER);
						String	merchn = arg1.getMerchn();     //   商户号		
						lnreq.append(merchn).append(E_OFCHAR.VER);
						String	groupt = arg1.getGroupt();     //   组合交易代码	
						lnreq.append(groupt).append(E_OFCHAR.VER);
						String	servsq = arg1.getServsq();     //   渠道流水号		
						lnreq.append(servsq).append(E_OFCHAR.VER);
						String	origtq = arg1.getOrigtq();     //   原交易流水号	
						lnreq.append(origtq).append(E_OFCHAR.VER);
						String	dcbtno = arg1.getDcbtno();     //   凭证号			
						lnreq.append(dcbtno).append(E_OFCHAR.VER);
						String	dcmttp = arg1.getDcmttp();     //   凭证类型		
						lnreq.append(dcmttp).append(E_OFCHAR.VER);
						String	prefip = arg1.getPrefip();     //   前缀			
						lnreq.append(prefip).append(E_OFCHAR.VER);
						String	userid = arg1.getUserid();     //   操作柜员		
						lnreq.append(userid).append(E_OFCHAR.VER);
						String	otranc = arg1.getOtranc();     //   原渠道号		
						lnreq.append(otranc).append(E_OFCHAR.VER);
						BigDecimal	acctbl = arg1.getAcctbl();     //   账户余额	
						lnreq.append(acctbl).append(E_OFCHAR.VER);
						//String	tranam = arg1.getTranam();     //   本金		
						lnreq.append(tranam).append(E_OFCHAR.VER);
						String	trandc = arg1.getTrandc();     //   交易说明代码	
						lnreq.append(trandc).append(E_OFCHAR.VER);
						String	authus = arg1.getAuthus();     //   授权柜员		
						lnreq.append(authus).append(E_OFCHAR.VER);
						String	corrtg = arg1.getCorrtg();     //   撤销标识		
						lnreq.append(corrtg).append(E_OFCHAR.VER);
						lnreq.append("").append(E_OFCHAR.VER);	//		支票类型			
						lnreq.append("").append(E_OFCHAR.VER);  //		支票前缀			
						lnreq.append("").append(E_OFCHAR.VER);  //		支票号码			
						lnreq.append("").append(E_OFCHAR.VER);  //		签发日期			
						lnreq.append("").append(E_OFCHAR.VER);  //		结售汇汇率			
						lnreq.append("").append(E_OFCHAR.VER);  //		结售汇标志			
						lnreq.append("").append(E_OFCHAR.VER);  //		结汇用途			
						lnreq.append("").append(E_OFCHAR.VER);  //		结汇详细用途		
						lnreq.append("").append(E_OFCHAR.VER);  //		借据凭证号			
						lnreq.append("").append(E_OFCHAR.VER);  //		财政虚拟子户入账标示
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
//            BufferedReader bre = null;
		try {

//		bre = new BufferedReader(new FileReader(oldfile)); //读取已存在文件到reader缓存中
        fileWriter = new LttsFileWriter(filePath + File.separator, fileName);
        for (String oldLine : fileDataList){   //将每一行分别写到writer中，完成已存在文件的读写
            fileWriter.write(oldLine);
            fileWriter.write(System.getProperty("line.separator"));
        }
			List<rbtranhisInfo> list = sjptdtDao.selRbTranhis(trandt, false);
			for (rbtranhisInfo arg1 : list) {
				StringBuffer lnreq = new StringBuffer();
				String	transq = arg1.getTransq();		//	交易流水号
				lnreq.append(transq).append(E_OFCHAR.VER);
				String	trandtt = arg1.getTrandt();     //   交易日期
				if (CommUtil.isNotNull(trandtt)) {
					trandtt = trandtt.substring(0, trandtt.indexOf("."));
				}
				lnreq.append(trandtt).append(E_OFCHAR.VER);
				String	atowtp = arg1.getAtowtp();     //   现金交易标识		
				lnreq.append(atowtp).append(E_OFCHAR.VER);
				String	toacna = arg1.getToacna();		//	交易对手名称	
				lnreq.append(toacna).append(E_OFCHAR.VER);
				String	strktp = arg1.getStrktp();     //   冲正标识	
				lnreq.append(strktp).append(E_OFCHAR.VER);
				String	soumod = arg1.getSoumod();     //   来源模块	
				lnreq.append(soumod).append(E_OFCHAR.VER);
				String	agenta = arg1.getAgenta();     //   代办人名称	
				lnreq.append(agenta).append(E_OFCHAR.VER);
				String	agentc = arg1.getAgentc();     //   代办人证件类型
				lnreq.append(agentc).append(E_OFCHAR.VER);
				String	agentn = arg1.getAgentn();     //   代办人证件号码	
				lnreq.append(agentn).append(E_OFCHAR.VER);
				String	agenna = arg1.getAgenna();     //   代办人国籍	
				lnreq.append(agenna).append(E_OFCHAR.VER);
				//String	datada = arg1.getDatada();     //   数据日期	
				lnreq.append(trandt).append(E_OFCHAR.VER);
				String	invoft = arg1.getInvoft();     //   涉外收支交易代码
				lnreq.append(invoft).append(E_OFCHAR.VER);
				String	coubrt = arg1.getCoubrt();     //   对方行号类型	
				lnreq.append(coubrt).append(E_OFCHAR.VER);
				String	tranbr = arg1.getTranbr();     //   机构号		
				lnreq.append(tranbr).append(E_OFCHAR.VER);
				String	opbrch = arg1.getOpbrch();     //   交易对手行号	
				lnreq.append(opbrch).append(E_OFCHAR.VER);
				String	crcycd = arg1.getCrcycd();     //   币种		
				lnreq.append(crcycd).append(E_OFCHAR.VER);
				String	custno = arg1.getCustno();     //   客户号		
				lnreq.append(custno).append(E_OFCHAR.VER);
				BigDecimal	tranam = arg1.getTranam();     //   交易金额	
				lnreq.append(tranam).append(E_OFCHAR.VER);
				String	toacct = arg1.getToacct();     //   对方账户号	
				lnreq.append(toacct).append(E_OFCHAR.VER);
				String	acctno = arg1.getAcctno();     //   账号
				lnreq.append(acctno).append(E_OFCHAR.VER);
				String	oppodc = arg1.getOppodc();     //   对方所在地区	
				lnreq.append(oppodc).append(E_OFCHAR.VER);
				String	bankna = arg1.getBankna();     //   对方银行名称	
				lnreq.append(bankna).append(E_OFCHAR.VER);
				String	amntcd = arg1.getAmntcd();     //   资金收付标识	
				lnreq.append(amntcd).append(E_OFCHAR.VER);
				String	drtion = arg1.getDrtion();     //   交易去向
				lnreq.append(drtion).append(E_OFCHAR.VER);
				String	remark = arg1.getRemark();     //   交易目的	
				lnreq.append(remark).append(E_OFCHAR.VER);
				String	inlind = arg1.getInlind();     //   境内外标识	
				lnreq.append(inlind).append(E_OFCHAR.VER);
				String	partct = arg1.getPartct();     //   交易对手客户类型
				lnreq.append(partct).append(E_OFCHAR.VER);
				String	trantd = arg1.getTrantd();     //   交易方式明细	
				lnreq.append(trantd).append(E_OFCHAR.VER);
				String	smryds = arg1.getSmryds();     //   资金来源或用途	
				lnreq.append(smryds).append(E_OFCHAR.VER);
				String	place = arg1.getPlace();       //   交易发生地		
				lnreq.append(place).append(E_OFCHAR.VER);
				String	partty = arg1.getPartty();     //   交易对手证件类型
				lnreq.append(partty).append(E_OFCHAR.VER);
				String	partid = arg1.getPartid();     //   交易对手证件号码
				lnreq.append(partid).append(E_OFCHAR.VER);
				String	bltype = arg1.getBltype();     //   交易种类		
				lnreq.append(bltype).append(E_OFCHAR.VER);
				String	partat = arg1.getPartat();     //   交易对手账户类型
				lnreq.append(partat).append(E_OFCHAR.VER);
				String	cifcty = arg1.getCifcty();     //   客户类型	
				lnreq.append(cifcty).append(E_OFCHAR.VER);
				String	sortno = arg1.getSortno();     //   交易序号	
				lnreq.append(sortno).append(E_OFCHAR.VER);
				String	custna = arg1.getCustna();     //   客户名称	
				lnreq.append(custna).append(E_OFCHAR.VER);
				String	cardno = arg1.getCardno();     //   账户号码	
				lnreq.append(cardno).append(E_OFCHAR.VER);
				String	flgflg = arg1.getFlgflg();     //   交易标志
				lnreq.append(flgflg).append(E_OFCHAR.VER);
				String	subsac = arg1.getSubsac();     //   账户序号	
				lnreq.append(subsac).append(E_OFCHAR.VER);
				//String	servtp = arg1.getServtp();     //   交易渠道	
				lnreq.append("Y5").append(E_OFCHAR.VER);
				String	intrcd = arg1.getIntrcd();     //   交易代码	
				lnreq.append(intrcd).append(E_OFCHAR.VER);
				String	termin = arg1.getTermin();     //   终端号		
				lnreq.append(termin).append(E_OFCHAR.VER);
				String	mcccod = arg1.getMcccod();     //   商户类型	
				lnreq.append(mcccod).append(E_OFCHAR.VER);
				String	merchn = arg1.getMerchn();     //   商户号		
				lnreq.append(merchn).append(E_OFCHAR.VER);
				String	groupt = arg1.getGroupt();     //   组合交易代码	
				lnreq.append(groupt).append(E_OFCHAR.VER);
				String	servsq = arg1.getServsq();     //   渠道流水号		
				lnreq.append(servsq).append(E_OFCHAR.VER);
				String	origtq = arg1.getOrigtq();     //   原交易流水号	
				lnreq.append(origtq).append(E_OFCHAR.VER);
				String	dcbtno = arg1.getDcbtno();     //   凭证号			
				lnreq.append(dcbtno).append(E_OFCHAR.VER);
				String	dcmttp = arg1.getDcmttp();     //   凭证类型		
				lnreq.append(dcmttp).append(E_OFCHAR.VER);
				String	prefip = arg1.getPrefip();     //   前缀			
				lnreq.append(prefip).append(E_OFCHAR.VER);
				String	userid = arg1.getUserid();     //   操作柜员		
				lnreq.append(userid).append(E_OFCHAR.VER);
				String	otranc = arg1.getOtranc();     //   原渠道号		
				lnreq.append(otranc).append(E_OFCHAR.VER);
				BigDecimal	acctbl = arg1.getAcctbl();     //   账户余额	
				lnreq.append(acctbl).append(E_OFCHAR.VER);
				//String	tranam = arg1.getTranam();     //   本金		
				lnreq.append(tranam).append(E_OFCHAR.VER);
				String	trandc = arg1.getTrandc();     //   交易说明代码	
				lnreq.append(trandc).append(E_OFCHAR.VER);
				String	authus = arg1.getAuthus();     //   授权柜员		
				lnreq.append(authus).append(E_OFCHAR.VER);
				String	corrtg = arg1.getCorrtg();     //   撤销标识		
				lnreq.append(corrtg).append(E_OFCHAR.VER);
				lnreq.append("").append(E_OFCHAR.VER);	//		支票类型			
				lnreq.append("").append(E_OFCHAR.VER);  //		支票前缀			
				lnreq.append("").append(E_OFCHAR.VER);  //		支票号码			
				lnreq.append("").append(E_OFCHAR.VER);  //		签发日期			
				lnreq.append("").append(E_OFCHAR.VER);  //		结售汇汇率			
				lnreq.append("").append(E_OFCHAR.VER);  //		结售汇标志			
				lnreq.append("").append(E_OFCHAR.VER);  //		结汇用途			
				lnreq.append("").append(E_OFCHAR.VER);  //		结汇详细用途		
				lnreq.append("").append(E_OFCHAR.VER);  //		借据凭证号			
				lnreq.append("").append(E_OFCHAR.VER);  //		财政虚拟子户入账标示

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
