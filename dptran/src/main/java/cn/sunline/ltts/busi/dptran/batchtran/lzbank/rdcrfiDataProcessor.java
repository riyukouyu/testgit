package cn.sunline.ltts.busi.dptran.batchtran.lzbank;

import java.io.File;
import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.FileUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.DBTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.file.FileDataExecutor;
import cn.sunline.edsp.base.lang.ByRef;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapotError;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapotErrorDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsError;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsErrorDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblError;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblErrorDao;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.FdError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CPRSST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUPSST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOTYPE;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRMPTP;
	 /**
	  * 对账差错文件读取
	  *
	  */

public class rdcrfiDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Rdcrfi.Input, cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Rdcrfi.Property> {
	
	private static final String fileSepa1 = "~";//文件分隔符
	private static final String encoding = "GBK";//文件编码
	String trandt = DateTools2.getDateInfo().getSystdt();//系统日期
	private BigDecimal sumamt = BigDecimal.ZERO; // 实际处理总金额

	 private static BizLog biz = BizLogUtil.getBizLog(rdcrfiDataProcessor.class);
	
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Rdcrfi.Input input, cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Rdcrfi.Property property) {
		String filedt = input.getTrandt();
		if(CommUtil.isNull(filedt)){
		    filedt=DateTools2.getDateInfo().getSystdt();
		}
		//读取大额对账差错文件
//		readLgamChkFile(filedt);
		
		//读取小额对账差错文件
//		readsmamChkFile(filedt);
		
		//读取银联对账差错文件
		readcupsChkFile(filedt);
		
		/**
		 * add by liuz
		 */
		
		//读取银联全渠道对账差错文件
		readAlchChkFile(filedt);
		
		//读取银联无卡对账差错文件
		readnocdChkFile(filedt);
		
		/**
		 * end
		 */
		//读取传统核心对账差错文件
//		readSfcdChkFile(filedt);
		
		//读取超级网银对账差错文件
//		readSuprChkFile(filedt);
		 
	}
	 
	 /**
	  * add by liuz
	 * 读取银联全渠道对账差错文件 
	 * @param filedt
	 */
	 private void readAlchChkFile(String filedt) {
		 String fileName = null;
		    KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "readfl", "rdcrfi", "ALCH", true);
		    
		    //读取开关控制校验
	  		if(CommUtil.compare("0", para.getParm_value5())==0){
	  			biz.debug("<<<<<<<<<<读取银联全渠道对账差错文件,跳过处理>>>>>>>>>>>>>>>>>>");
	  			return;
	  		}
		    
		    //如果当前系统日期与参数表中日期相等，表示已读取过文件
	        if(CommUtil.equals(trandt, para.getParm_value4())&&CommUtil.isNotNull(para.getParm_value4())){
	            return;
	        }
	        fileName=para.getParm_value1()+para.getParm_value2()+filedt+para.getParm_value3();
	        File file = new File(fileName);
	        if(!file.exists()){
	            return;
	        }
	        biz.debug("<<<<<<<<<<获取文件路径>>>>>>>>>>>>>>>>>>"+fileName);
	        //读取银联差错文件
	        final ByRef<Long> tolnnm = new ByRef<Long>(0L); // 应处理总笔数
	        final ByRef<Long> counts = new ByRef<Long>(0L); // 实际处理总笔数
	        final ByRef<BigDecimal> tolnam = new ByRef<BigDecimal>(); // 应处理总金额
	        sumamt = BigDecimal.ZERO; // 实际处理总金额
	        biz.debug("<<<<<<<<<<开始读取文件>>>>>>>>>>>>>>>>>>");
	        //读取银联差错文件
	        FileUtil.readFile(fileName, new FileDataExecutor() {
	            
	            @Override
	            public void process(int arg0, String arg1) {
	                 MsSystemSeq.getTrxnSeq();//重置流水
	                try {
	                    //行信息
	                    String[] line = arg1.split(fileSepa1);
	                    
	                    if(arg0 == 1){
	                        //读取文件头
	                        if (line.length != 2) {
	                            throw DpModuleError.DpstComm.E9999("银联全渠道对账文件第1行字段数量不符，文件异常。");
	                        }
	                        tolnnm.value = Long.parseLong(line[0]);//应处理总笔数
	                        tolnam.value = new BigDecimal(line[1]);//应处理总金额
	                        return;
	                    }
	                    if(arg0 > 1){
	                    	String cardno = line[0];//交易卡号
	                    	String amntcd = line[2];//借贷标志
	                    	String tranam = line[3];//交易金额
	                    	String ditype = line[21];//登记方式
	                    	
	                    	String cycycd = line[4];//币种
	                    	String trcrch = line[25];//机构
	                  
	                    	//验空
						    if(CommUtil.isNull(cardno)){
						    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第1列交易卡号不能为空！");
						    }
						    if(CommUtil.isNull(amntcd)){
						    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第3列借贷标志不能为空！");
						    }
						    if(CommUtil.isNull(tranam)){
						    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第4列交易金额不能为空！");
						    }
						    if(CommUtil.isNull(ditype)){
						    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第21列登记方式不能为空！");
						    }
						    
						    //交易机构 和 币种处理
						    cycycd = CommUtil.isNull(cycycd)?"CNY":cycycd;
						    trcrch = SysUtil.getRemoteInstance(IoSrvPbBranch.class)
						    		.getRootBranch(CommUtil.isNull(cycycd)?"CNY":cycycd, E_BRMPTP.B);
	                    	
	                        //读取差错明细，并登记
	                        KnlIoblCupsError entity = SysUtil.getInstance(KnlIoblCupsError.class);
	                        entity.setRdtrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//读取流水
	                        entity.setRdtrdt(CommTools.getBaseRunEnvs().getTrxn_date());//读取日期
	                        entity.setCardno(cardno);//交易卡号
	                        entity.setDevcno(line[1]);//设备号
	                        entity.setAmntcd(CommUtil.toEnum(E_AMNTCD.class, amntcd));//借贷标志
	                        entity.setTranam(new BigDecimal(tranam));//金额
	                        entity.setCrcycd(cycycd);//币种
	                        entity.setOtacct(line[5]);//转出账号
	                        entity.setOtacna(line[6]);//转出账号户名
	                        entity.setInacct(line[7]);//转入账号
	                        entity.setInacna(line[8]);//转入账号户名
	                        entity.setRetrdt(line[9]);//设备交易日期时间
	                        entity.setChrgam(new BigDecimal(line[10]));//手续费金额
	                        entity.setBusino(line[11]);//商户代码
	                        entity.setBusitp(line[12]);//商户类型
	                        entity.setAuthno(line[13]);//预授权标识码
	                        entity.setMesstp(line[14]);//报文类型
	                        entity.setProccd(line[15]);//处理码
	                        entity.setSpared(line[16]);//备用
	                        entity.setFrondt(line[17]);//渠道请求日期
	                        entity.setFronsq(line[18]);//渠道请求流水号
	                        entity.setMntrsq(line[19]);//核心流水
	                        entity.setTrandt(line[20]);//交易日期
	                        entity.setDjtype(CommUtil.toEnum(BaseEnumType.E_DJTYPE.class, ditype));//登记方式
	                        entity.setPrepty(CommUtil.toEnum(BaseEnumType.E_PREPTY.class, line[22]));//银联交易类型
	                        entity.setErroty(CommUtil.toEnum(E_CPRSST.class, line[23]));//差错类型
	                        entity.setErromk(line[24]);//差错描述
	                        entity.setTrbrch(trcrch);//交易机构
	                        entity.setTranst(E_CUPSST.DCL);//6-待处理
	                        KnlIoblCupsErrorDao.insert(entity);
	                        counts.value++;
	                        sumamt = sumamt.add(entity.getTranam());
	                    }
	                } catch (Exception e) {
	                    throw DpModuleError.DpstComm.E9999("读取银联全渠道差错文件第" + arg0 + "条记录处理异常！",e); 
	                }
	                
	            }
	        },"UTF-8");
	        biz.debug("<<<<<<<<<<文件读取结束>>>>>>>>>>>>>>>>>>");
	        // 判断实际处理与文件总是否相符
	        if (CommUtil.compare(tolnnm.value, counts.value) != 0) {
	            throw FdError.FundAcct.E0001("读取银联全渠道差错文件总记录数[" + tolnnm.value+ "]与实际处理记录数[" + counts.value + "]不符！");
	        }
	        if (CommUtil.compare(tolnam.value, sumamt) != 0) {
	            throw FdError.FundAcct.E0001("读取银联全渠道差错文件总金额[" + tolnam.value+ "]与实际处理总金额[" + sumamt + "]不符！");
	        }        
	        //更改读取日期标志 
	        para.setParm_value4(trandt);
	        KnpParameterDao.updateOne_odb1(para);
	        DBTools.commit();
	}
	 /**
	  * end
	  */

	/**
	  * add by liuz
	 * 读取银联无卡对账差错文件 
	 * @param filedt
	 */
	 
	 private void readnocdChkFile(String filedt) {
		    String fileName = null;
		    KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "readfl", "rdcrfi", "YLWK", true);
		    
		    //读取开关控制校验
	  		if(CommUtil.compare("0", para.getParm_value5())==0){
	  			biz.debug("<<<<<<<<<<读取银联无卡对账差错文件,跳过处理>>>>>>>>>>>>>>>>>>");
	  			return;
	  		}
		    
		    //如果当前系统日期与参数表中日期相等，表示已读取过文件
	        if(CommUtil.equals(trandt, para.getParm_value4())&&CommUtil.isNotNull(para.getParm_value4())){
	            return;
	        }
	        fileName=para.getParm_value1()+para.getParm_value2()+filedt+para.getParm_value3();
	        File file = new File(fileName);
	        if(!file.exists()){
	            return;
	        }
	        biz.debug("<<<<<<<<<<获取文件路径>>>>>>>>>>>>>>>>>>"+fileName);
	        //读取银联差错文件
	        final ByRef<Long> tolnnm = new ByRef<Long>(0L); // 应处理总笔数
	        final ByRef<Long> counts = new ByRef<Long>(0L); // 实际处理总笔数
	        final ByRef<BigDecimal> tolnam = new ByRef<BigDecimal>(); // 应处理总金额
	        sumamt = BigDecimal.ZERO; // 实际处理总金额
	        biz.debug("<<<<<<<<<<开始读取文件>>>>>>>>>>>>>>>>>>");
	        //读取银联差错文件
	        FileUtil.readFile(fileName, new FileDataExecutor() {
	            
	            @Override
	            public void process(int arg0, String arg1) {
	                 MsSystemSeq.getTrxnSeq();//重置流水
	                try {
	                    //行信息
	                    String[] line = arg1.split(fileSepa1);
	                    
	                    if(arg0 == 1){
	                        //读取文件头
	                        if (line.length != 2) {
	                            throw DpModuleError.DpstComm.E9999("银联无卡对账文件第1行字段数量不符，文件异常。");
	                        }
	                        tolnnm.value = Long.parseLong(line[0]);//应处理总笔数
	                        tolnam.value = new BigDecimal(line[1]);//应处理总金额
	                        return;
	                    }
	                    if(arg0 > 1){
	                    	String cardno = line[1];//交易卡号
	                    	String amntcd = line[3];//借贷标志
	                    	String tranam = line[4];//交易金额
	                    	String ditype = line[39];//登记方式
	                    	
	                    	String cycycd = line[6];//币种
	                    	String trcrch = line[15];//机构
	                  
	                    	//验空
						    if(CommUtil.isNull(cardno)){
						    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第2列交易卡号不能为空！");
						    }
						    if(CommUtil.isNull(amntcd)){
						    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第4列借贷标志不能为空！");
						    }
						    if(CommUtil.isNull(tranam)){
						    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第5列交易金额不能为空！");
						    }
						    if(CommUtil.isNull(ditype)){
						    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第40列登记方式不能为空！");
						    }
						    
						    //交易机构 和 币种处理
						    cycycd = CommUtil.isNull(cycycd)?"CNY":cycycd;
						    trcrch = SysUtil.getRemoteInstance(IoSrvPbBranch.class)
						    		.getRootBranch(CommUtil.isNull(cycycd)?"CNY":cycycd, E_BRMPTP.B);
	                    	
	                        //读取差错明细，并登记
	                        KnlIoblCupsError entity = SysUtil.getInstance(KnlIoblCupsError.class);
	                        entity.setRdtrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//读取流水
	                        entity.setRdtrdt(CommTools.getBaseRunEnvs().getTrxn_date());//读取日期
	                        entity.setPrepsq(line[0]);//银联前置流水
	                        entity.setCardno(cardno);//交易卡号
	                        entity.setDevcno(line[2]);//设备号
	                        entity.setAmntcd(CommUtil.toEnum(E_AMNTCD.class, amntcd));//借贷标志
	                        entity.setTranam(new BigDecimal(tranam));//金额
	                        entity.setPrepdt(line[5]);//银联前置日期
	                        entity.setCrcycd(cycycd);//币种
	                        entity.setCnkpdt(line[7]);//传统核心清算日期
	                        entity.setUnkpdt(line[8]);//银联清算日期
	                        entity.setOtacct(line[9]);//转出账号
	                        entity.setOtacna(line[10]);//转出账号户名
	                        entity.setOtbrch(line[11]);//转出机构号
	                        entity.setInacct(line[12]);//转入账号
	                        entity.setInacna(line[13]);//转入账号户名
	                        entity.setInbrch(line[14]);//转入机构号
	                        entity.setTrbrch(trcrch);//受理机构号
	                        entity.setPrdate(line[16]);//前置交易日期
	                        entity.setPrbrmk(line[17]);//代理机构标识码
	                        entity.setTrbrmk(line[18]);//发送机构标识码
	                        entity.setTrcode(line[19]);//银联交易码
	                        entity.setStand1(line[20]);//32域
	                        entity.setStand2(line[21]);//33域
	                        entity.setRetrdt(line[22]);//设备交易日期时间
	                        entity.setUniseq(line[23]);//银联流水号
	                        entity.setResssq(line[24]);//原系统跟踪号
	                        entity.setReprsq(line[25]);//原前置流水号
	                        entity.setServsq(line[26]);//全渠道流水号
	                        entity.setChckno(line[27]);//对账分类编号
	                        entity.setChrgam(new BigDecimal(line[28]));//手续费金额
	                        entity.setBusino(line[29]);//商户代码
	                        entity.setBusitp(line[30]);//商户类型
	                        entity.setAuthno(line[31]);//预授权标识码
	                        entity.setMesstp(line[32]);//报文类型
	                        entity.setProccd(line[33]);//处理码
	                        entity.setSpared(line[34]);//备用
	                        entity.setFrondt(line[35]);//支付前置日期
	                        entity.setFronsq(line[36]);//支付前置流水号
	                        entity.setMntrsq(line[37]);//核心流水
	                        entity.setTrandt(line[38]);//交易日期
	                        entity.setDjtype(CommUtil.toEnum(BaseEnumType.E_DJTYPE.class, ditype));//登记方式
	                        entity.setPrepty(CommUtil.toEnum(BaseEnumType.E_PREPTY.class, line[40]));//银联交易类型
	                        entity.setErroty(CommUtil.toEnum(E_CPRSST.class, line[41]));//差错类型
	                        entity.setErromk(line[42]);//差错描述
	                        
	                        entity.setTranst(E_CUPSST.DCL);//6-待处理
	                        KnlIoblCupsErrorDao.insert(entity);
	                        counts.value++;
	                        sumamt = sumamt.add(entity.getTranam());
	                    }
	                } catch (Exception e) {
	                    throw DpModuleError.DpstComm.E9999("读取银联无卡差错文件第" + arg0 + "条记录处理异常！",e); 
	                }
	                
	            }
	        },encoding);
	        biz.debug("<<<<<<<<<<文件读取结束>>>>>>>>>>>>>>>>>>");
	        // 判断实际处理与文件总是否相符
	        if (CommUtil.compare(tolnnm.value, counts.value) != 0) {
	            throw FdError.FundAcct.E0001("读取银联差错文件总记录数[" + tolnnm.value+ "]与实际处理记录数[" + counts.value + "]不符！");
	        }
	        if (CommUtil.compare(tolnam.value, sumamt) != 0) {
	            throw FdError.FundAcct.E0001("读取银联差错文件总金额[" + tolnam.value+ "]与实际处理总金额[" + sumamt + "]不符！");
	        }        
	        //更改读取日期标志 
	        para.setParm_value4(trandt);
	        KnpParameterDao.updateOne_odb1(para);
	        DBTools.commit();
		}
	 
	 /**
	 * end
	 */
	 
	 /**
      * 读取超级网银对账差错文件
      * @param filedt
      */
	 private void readSuprChkFile(String filedt) {
	        String fileName = null;
	        KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "readfl", "rdcrfi", "SP", true);
	        
	        //读取开关控制校验
	        if(CommUtil.compare("0", para.getParm_value5())==0){
	            biz.debug("<<<<<<<<<<读取超级网银对账差错文件,跳过处理>>>>>>>>>>>>>>>>>>");
	            return;
	        }
	        
	        //如果当前系统日期与参数表中日期相等，表示已读取过文件
	        if(CommUtil.equals(trandt, para.getParm_value4())&&CommUtil.isNotNull(para.getParm_value4())){
	            return;
	        }
	       
	        fileName=para.getParm_value1()+para.getParm_value2()+filedt+para.getParm_value3();
	        File file = new File(fileName);
	        if(!file.exists()){
	        	 biz.debug("<<<<<<<<<<超级网银对账文件不存在,跳过处理>>>>>>>>>>>>>>>>>>");
	            return;
	        }
	        biz.debug("<<<<<<<<<<获取文件路径>>>>>>>>>>>>>>>>>>"+fileName);
	        //读取传统核心差错文件
	        final ByRef<Long> tolnnm = new ByRef<Long>(0L); // 应处理总笔数
	        final ByRef<Long> counts = new ByRef<Long>(0L); // 实际处理总笔数
	        final ByRef<BigDecimal> tolnam = new ByRef<BigDecimal>(); // 应处理总金额
	        sumamt = BigDecimal.ZERO;  // 实际处理总金额
	        biz.debug("<<<<<<<<<<开始读取文件>>>>>>>>>>>>>>>>>>");
	        //读取传统核心差错文件
	        FileUtil.readFile(fileName, new FileDataExecutor() {
	            
	            @Override
	            public void process(int arg0, String arg1) {
	                 MsSystemSeq.getTrxnSeq();//重置流水
	                try {
	                    //行信息
	                    String[] line = arg1.split(fileSepa1);
	                    
	                    if(arg0 == 1){
	                        //读取文件头
	                        if (line.length != 2) {
	                            throw DpModuleError.DpstComm.E9999("超级网银对账文件第1行字段数量不符，文件异常。");
	                        }
	                        tolnnm.value = Long.parseLong(line[0]);//应处理总笔数
	                        tolnam.value = new BigDecimal(line[1]);//应处理总金额
	                        return;
	                    }
	                    if(arg0 > 1){
	                        String sCapitp = line[0];//转账交易类型-字符
	                        String toacct = line[1];//转出方卡号/账号
	                        String crcycd = line[6];//币种
	                        BigDecimal tranam = new BigDecimal(line[8]);//交易金额
	                        String cardno = line[10];//转入方卡号/账号
	                        
	                        String tobrch = null;//line[5];//转出方所属机构
	                        String brchno = null;//line[14];//转入方所属机构
	                        
	                        E_IOFLAG ioflag = null;//出入金标志
	                        
	                        if(CommUtil.isNull(sCapitp)){
	                            throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第1列转账交易类型不能为空！");
	                        }
	                        if(CommUtil.isNull(toacct)){
	                            throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第2列转出方卡号不能为空！");
	                        }
//	                      if(CommUtil.isNull(tobrch)){
//	                          throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第6列转出方所属机构不能为空！");
//	                      }
//	                      if(CommUtil.isNull(crcycd)){
//	                          throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第7列币种不能为空！");
//	                      }
	                        if(CommUtil.isNull(tranam)){
	                            throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第9列交易金额不能为空！");
	                        }
	                        if(CommUtil.isNull(cardno)){
	                            throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第10列转入方卡号不能为空！");
	                        }
	                        
	                        
	                        E_CAPITP capitp = CommUtil.toEnum(E_CAPITP.class, sCapitp);//转账交易类型
	                        if(E_CAPITP.IN109 == capitp){
	                            //109 - 超网来账
	                            ioflag = E_IOFLAG.IN;
	                        }else if(E_CAPITP.OT208 == capitp){
	                            //208 - 超网往账
	                            ioflag = E_IOFLAG.OUT;
	                        }else{
	                            throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第1列转账交易类型不支持！");
	                        }
	                        
	                        //交易机构 和 币种处理
	                        crcycd = CommUtil.isNull(crcycd)?"CNY":crcycd;
	                        brchno = SysUtil.getRemoteInstance(IoSrvPbBranch.class)
	                                .getRootBranch(CommUtil.isNull(crcycd)?"CNY":crcycd, E_BRMPTP.B);
	                        tobrch = SysUtil.getRemoteInstance(IoSrvPbBranch.class)
	                                .getRootBranch(CommUtil.isNull(crcycd)?"CNY":crcycd, E_BRMPTP.B);
	                        
	                        //读取差错明细，并登记
	                        KnlIoblError entity = SysUtil.getInstance(KnlIoblError.class);
	                        entity.setRdtrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//读取流水
	                        entity.setRdtrdt(CommTools.getBaseRunEnvs().getTrxn_date());//读取日期
	                        entity.setCapitp(capitp);//转账交易类型
	                        entity.setToacct(toacct);//转出方帐号/卡号
//	                      entity.setTo(line[2]);//转出方子账号类型
	                        entity.setToacno(line[3]);//转出方子账号
//	                      entity.setTo(line[4]);//转出方户名
	                        entity.setTobrch(tobrch);//转出方账户所属机构
	                        entity.setCrcycd(crcycd);//币种
//	                      entity.setCs(line[7]);//钞汇标志
	                        entity.setTranam(tranam);//交易金额
//	                      entity.set(line[9]);//摘要码
	                        entity.setCardno(cardno);//转入方帐号/卡号
//	                      entity.set(line[11]);//转入方子账号类型
	                        entity.setAcctno(line[12]);//转入方子账号
//	                      entity.set(line[13]);//转入方户名
	                        entity.setBrchno(brchno);//转入方账户所属机构
	                        entity.setTlcgam(new BigDecimal(line[15]));//收费总额
	                        entity.setChckdt(line[16]);//对账日期
	                        entity.setKeepdt(line[17]);//清算日期
//	                      entity.set(line[18]);//备用字段1
//	                      entity.set(line[19]);//备用字段2
//	                      entity.set(line[20]);//支付前置日期
//	                      entity.set(line[21]);//支付前置流水号
	                        entity.setTransq(line[22]);//核心流水
	                        entity.setTrandt(line[23]);//交易日期
//	                      entity.setTrandt(line[24]);//支付交易渠道
	                        entity.setErroty(CommUtil.toEnum(E_CPRSST.class, line[25]));//差错类型
	                        entity.setErromk(line[26]);//差错描述
	                        
	                        entity.setIoflag(ioflag);//来往账标志
	                        entity.setStatus(E_TRANST.WAIT);//状态-6待处理
	                        entity.setServtp("SI");;//交易渠道
	                        
	                        KnlIoblErrorDao.insert(entity);
	                        counts.value++;
	                        sumamt = sumamt.add(entity.getTranam());
	                    }
	                } catch (Exception e) {
	                    throw DpModuleError.DpstComm.E9999("读取超级网银差错文件第" + arg0 + "条记录处理异常！",e); 
	                }
	                
	            }
	        },"GBK");
	        biz.debug("<<<<<<<<<<开始读取结束>>>>>>>>>>>>>>>>>>");
	        // 判断实际处理与文件总是否相符
	        if (CommUtil.compare(tolnnm.value, counts.value) != 0) {
	            throw FdError.FundAcct.E0001("读取超级网银差错文件总记录数[" + tolnnm.value+ "]与实际处理记录数[" + counts.value + "]不符！");
	        }
	        if (CommUtil.compare(tolnam.value, sumamt) != 0) {
	            throw FdError.FundAcct.E0001("读取超级网银差错文件总金额[" + tolnam.value+ "]与实际处理总金额[" + sumamt + "]不符！");
	        }
	        //更改读取日期标志 
	        para.setParm_value4(trandt);
	        KnpParameterDao.updateOne_odb1(para);
	        DBTools.commit();
        
    }
    /**
	     * 读取传统核心对账差错文件
	     * @param filedt
	     */
	private void readSfcdChkFile(String filedt) {
	    String fileName = null;
	    KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "readfl", "rdcrfi", "CTHX", true);
	    
	    //读取开关控制校验
  		if(CommUtil.compare("0", para.getParm_value5())==0){
  			biz.debug("<<<<<<<<<<读取传统核心对账差错文件,跳过处理>>>>>>>>>>>>>>>>>>");
  			return;
  		}
  		
	    
	    //如果当前系统日期与参数表中日期相等，表示已读取过文件
	    if(CommUtil.equals(trandt, para.getParm_value4())&&CommUtil.isNotNull(para.getParm_value4())){
	        return;
	    }
	    
        fileName=para.getParm_value1()+para.getParm_value2()+filedt+para.getParm_value3();
        File file = new File(fileName);
        if(!file.exists()){
            return;
        }
        biz.debug("<<<<<<<<<<获取文件路径>>>>>>>>>>>>>>>>>>"+fileName);
        //读取传统核心差错文件
        final ByRef<Long> tolnnm = new ByRef<Long>(0L); // 应处理总笔数
        final ByRef<Long> counts = new ByRef<Long>(0L); // 实际处理总笔数
        final ByRef<BigDecimal> tolnam = new ByRef<BigDecimal>(); // 应处理总金额
        sumamt = BigDecimal.ZERO;  // 实际处理总金额
        biz.debug("<<<<<<<<<<开始读取文件>>>>>>>>>>>>>>>>>>");
        //读取传统核心差错文件
        FileUtil.readFile(fileName, new FileDataExecutor() {
            
            @Override
            public void process(int arg0, String arg1) {
                 MsSystemSeq.getTrxnSeq();//重置流水
                try {
                    //行信息
                    String[] line = arg1.split(fileSepa1);
                    
                    if(arg0 == 1){
                        //读取文件头
                        if (line.length != 2) {
                            throw DpModuleError.DpstComm.E9999("传统核心对账文件第1行字段数量不符，文件异常。");
                        }
                        tolnnm.value = Long.parseLong(line[0]);//应处理总笔数
                        tolnam.value = new BigDecimal(line[1]);//应处理总金额
                        return;
                    }
                    if(arg0 > 1){
                    	String sCapitp = line[0];//转账交易类型-字符
                    	String toacct = line[1];//转出方卡号/账号
                    	String crcycd = line[6];//币种
                    	BigDecimal tranam = new BigDecimal(line[8]);//交易金额
                    	String cardno = line[10];//转入方卡号/账号
                    	
                    	String tobrch = null;//line[5];//转出方所属机构
                    	String brchno = null;//line[14];//转入方所属机构
                    	
                    	E_IOFLAG ioflag = null;//出入金标志
                    	
                    	if(CommUtil.isNull(sCapitp)){
                    		throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第1列转账交易类型不能为空！");
                    	}
                    	if(CommUtil.isNull(toacct)){
                    		throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第2列转出方卡号不能为空！");
                    	}
//                    	if(CommUtil.isNull(tobrch)){
//                    		throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第6列转出方所属机构不能为空！");
//                    	}
//                    	if(CommUtil.isNull(crcycd)){
//                    		throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第7列币种不能为空！");
//                    	}
                    	if(CommUtil.isNull(tranam)){
                    		throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第9列交易金额不能为空！");
                    	}
                    	if(CommUtil.isNull(cardno)){
                    		throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第10列转入方卡号不能为空！");
                    	}
                    	
                    	
                    	E_CAPITP capitp = CommUtil.toEnum(E_CAPITP.class, sCapitp);//转账交易类型
                    	if(E_CAPITP.IN101 == capitp){
                    		//101 - 本行借记卡转电子账户
                    		ioflag = E_IOFLAG.IN;
                    	}else if(E_CAPITP.OT201 == capitp){
                    		//201 - 电子账户转本行借记卡
                    		ioflag = E_IOFLAG.OUT;
                    	}else{
                    		throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第1列转账交易类型不支持！");
                    	}
                    	
					    //交易机构 和 币种处理
                    	crcycd = CommUtil.isNull(crcycd)?"CNY":crcycd;
                    	brchno = SysUtil.getRemoteInstance(IoSrvPbBranch.class)
					    		.getRootBranch(CommUtil.isNull(crcycd)?"CNY":crcycd, E_BRMPTP.B);
                    	tobrch = SysUtil.getRemoteInstance(IoSrvPbBranch.class)
					    		.getRootBranch(CommUtil.isNull(crcycd)?"CNY":crcycd, E_BRMPTP.B);
                    	
                        //读取差错明细，并登记
                        KnlIoblError entity = SysUtil.getInstance(KnlIoblError.class);
                        entity.setRdtrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//读取流水
                        entity.setRdtrdt(CommTools.getBaseRunEnvs().getTrxn_date());//读取日期
                        entity.setCapitp(capitp);//转账交易类型
                        entity.setToacct(toacct);//转出方帐号/卡号
//                      entity.setTo(line[2]);//转出方子账号类型
                        entity.setToacno(line[3]);//转出方子账号
//                      entity.setTo(line[4]);//转出方户名
                        entity.setTobrch(tobrch);//转出方账户所属机构
                        entity.setCrcycd(crcycd);//币种
//                      entity.setCs(line[7]);//钞汇标志
                        entity.setTranam(tranam);//交易金额
//                      entity.set(line[9]);//摘要码
                        entity.setCardno(cardno);//转入方帐号/卡号
//                      entity.set(line[11]);//转入方子账号类型
                        entity.setAcctno(line[12]);//转入方子账号
//                      entity.set(line[13]);//转入方户名
                        entity.setBrchno(brchno);//转入方账户所属机构
                        entity.setTlcgam(new BigDecimal(line[15]));//收费总额
                        entity.setChckdt(line[16]);//对账日期
                        entity.setKeepdt(line[17]);//清算日期
//                      entity.set(line[18]);//备用字段1
//                      entity.set(line[19]);//备用字段2
//                      entity.set(line[20]);//支付前置日期
//                      entity.set(line[21]);//支付前置流水号
                        entity.setTransq(line[22]);//核心流水
                        entity.setTrandt(line[23]);//交易日期
//                      entity.setTrandt(line[24]);//支付交易渠道
                        entity.setErroty(CommUtil.toEnum(E_CPRSST.class, line[25]));//差错类型
                        entity.setErromk(line[26]);//差错描述
                        
                        entity.setIoflag(ioflag);//来往账标志
                        entity.setStatus(E_TRANST.WAIT);//状态-6待处理
                        entity.setServtp("IM");;//交易渠道
                        KnlIoblErrorDao.insert(entity);
                        counts.value++;
                        sumamt = sumamt.add(entity.getTranam());
                    }
                } catch (Exception e) {
                    throw DpModuleError.DpstComm.E9999("读取传统核心差错文件第" + arg0 + "条记录处理异常！",e); 
                }
                
            }
        },"UTF-8");
        biz.debug("<<<<<<<<<<开始读取结束>>>>>>>>>>>>>>>>>>");
        // 判断实际处理与文件总是否相符
        if (CommUtil.compare(tolnnm.value, counts.value) != 0) {
            throw FdError.FundAcct.E0001("读取传统核心差错文件总记录数[" + tolnnm.value+ "]与实际处理记录数[" + counts.value + "]不符！");
        }
        if (CommUtil.compare(tolnam.value, sumamt) != 0) {
            throw FdError.FundAcct.E0001("读取传统核心差错文件总金额[" + tolnam.value+ "]与实际处理总金额[" + sumamt + "]不符！");
        }
        //更改读取日期标志 
        para.setParm_value4(trandt);
        KnpParameterDao.updateOne_odb1(para);
        DBTools.commit();
    }

    /**
	 * 读取银联对账差错文件 
	 * @param filedt
	 */
	private void readcupsChkFile(String filedt) {
	    String fileName = null;
	    KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "readfl", "rdcrfi", "YLCP", true);
	    
	    //读取开关控制校验
  		if(CommUtil.compare("0", para.getParm_value5())==0){
  			biz.debug("<<<<<<<<<<读取银联对账差错文件,跳过处理>>>>>>>>>>>>>>>>>>");
  			return;
  		}
	    
	    //如果当前系统日期与参数表中日期相等，表示已读取过文件
        if(CommUtil.equals(trandt, para.getParm_value4())&&CommUtil.isNotNull(para.getParm_value4())){
            return;
        }
        fileName=para.getParm_value1()+para.getParm_value2()+filedt+para.getParm_value3();
        File file = new File(fileName);
        if(!file.exists()){
            return;
        }
        biz.debug("<<<<<<<<<<获取文件路径>>>>>>>>>>>>>>>>>>"+fileName);
        //读取银联差错文件
        final ByRef<Long> tolnnm = new ByRef<Long>(0L); // 应处理总笔数
        final ByRef<Long> counts = new ByRef<Long>(0L); // 实际处理总笔数
        final ByRef<BigDecimal> tolnam = new ByRef<BigDecimal>(); // 应处理总金额
        sumamt = BigDecimal.ZERO; // 实际处理总金额
        biz.debug("<<<<<<<<<<开始读取文件>>>>>>>>>>>>>>>>>>");
        //读取银联差错文件
        FileUtil.readFile(fileName, new FileDataExecutor() {
            
            @Override
            public void process(int arg0, String arg1) {
                 MsSystemSeq.getTrxnSeq();//重置流水
                try {
                    //行信息
                    String[] line = arg1.split(fileSepa1);
                    
                    if(arg0 == 1){
                        //读取文件头
                        if (line.length != 2) {
                            throw DpModuleError.DpstComm.E9999("银联CUPS对账文件第1行字段数量不符，文件异常。");
                        }
                        tolnnm.value = Long.parseLong(line[0]);//应处理总笔数
                        tolnam.value = new BigDecimal(line[1]);//应处理总金额
                        return;
                    }
                    if(arg0 > 1){
                    	String cardno = line[1];//交易卡号
                    	String amntcd = line[3];//借贷标志
                    	String tranam = line[4];//交易金额
                    	String ditype = line[39];//登记方式
                    	
                    	String cycycd = line[6];//币种
                    	String trcrch = line[15];//机构
                  
                    	//验空
					    if(CommUtil.isNull(cardno)){
					    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第2列交易卡号不能为空！");
					    }
					    if(CommUtil.isNull(amntcd)){
					    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第4列借贷标志不能为空！");
					    }
					    if(CommUtil.isNull(tranam)){
					    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第5列交易金额不能为空！");
					    }
					    if(CommUtil.isNull(ditype)){
					    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第40列登记方式不能为空！");
					    }
					    
					    //交易机构 和 币种处理
					    cycycd = CommUtil.isNull(cycycd)?"CNY":cycycd;
					    trcrch = SysUtil.getRemoteInstance(IoSrvPbBranch.class)
					    		.getRootBranch(CommUtil.isNull(cycycd)?"CNY":cycycd, E_BRMPTP.B);
                    	
                        //读取差错明细，并登记
                        KnlIoblCupsError entity = SysUtil.getInstance(KnlIoblCupsError.class);
                        entity.setRdtrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//读取流水
                        entity.setRdtrdt(CommTools.getBaseRunEnvs().getTrxn_date());//读取日期
                        entity.setPrepsq(line[0]);//银联前置流水
                        entity.setCardno(cardno);//交易卡号
                        entity.setDevcno(line[2]);//设备号
                        entity.setAmntcd(CommUtil.toEnum(E_AMNTCD.class, amntcd));//借贷标志
                        entity.setTranam(new BigDecimal(tranam));//金额
                        entity.setPrepdt(line[5]);//银联前置日期
                        entity.setCrcycd(cycycd);//币种
                        entity.setCnkpdt(line[7]);//传统核心清算日期
                        entity.setUnkpdt(line[8]);//银联清算日期
                        entity.setOtacct(line[9]);//转出账号
                        entity.setOtacna(line[10]);//转出账号户名
                        entity.setOtbrch(line[11]);//转出机构号
                        entity.setInacct(line[12]);//转入账号
                        entity.setInacna(line[13]);//转入账号户名
                        entity.setInbrch(line[14]);//转入机构号
                        entity.setTrbrch(trcrch);//受理机构号
                        entity.setPrdate(line[16]);//前置交易日期
                        entity.setPrbrmk(line[17]);//代理机构标识码
                        entity.setTrbrmk(line[18]);//发送机构标识码
                        entity.setTrcode(line[19]);//银联交易码
                        entity.setStand1(line[20]);//32域
                        entity.setStand2(line[21]);//33域
                        entity.setRetrdt(line[22]);//设备交易日期时间
                        entity.setUniseq(line[23]);//银联流水号
                        entity.setResssq(line[24]);//原系统跟踪号
                        entity.setReprsq(line[25]);//原前置流水号
                        entity.setServsq(line[26]);//全渠道流水号
                        entity.setChckno(line[27]);//对账分类编号
                        entity.setChrgam(new BigDecimal(line[28]));//手续费金额
                        entity.setBusino(line[29]);//商户代码
                        entity.setBusitp(line[30]);//商户类型
                        entity.setAuthno(line[31]);//预授权标识码
                        entity.setMesstp(line[32]);//报文类型
                        entity.setProccd(line[33]);//处理码
                        entity.setSpared(line[34]);//备用
                        entity.setFrondt(line[35]);//支付前置日期
                        entity.setFronsq(line[36]);//支付前置流水号
                        entity.setMntrsq(line[37]);//核心流水
                        entity.setTrandt(line[38]);//交易日期
                        entity.setDjtype(CommUtil.toEnum(BaseEnumType.E_DJTYPE.class, ditype));//登记方式
                        entity.setPrepty(CommUtil.toEnum(BaseEnumType.E_PREPTY.class, line[40]));//银联交易类型
                        entity.setErroty(CommUtil.toEnum(E_CPRSST.class, line[41]));//差错类型
                        entity.setErromk(line[42]);//差错描述
                        
                        entity.setTranst(E_CUPSST.DCL);//6-待处理
                        KnlIoblCupsErrorDao.insert(entity);
                        counts.value++;
                        sumamt = sumamt.add(entity.getTranam());
                    }
                } catch (Exception e) {
                    throw DpModuleError.DpstComm.E9999("读取银联CUPS差错文件第" + arg0 + "条记录处理异常！",e); 
                }
                
            }
        },encoding);
        biz.debug("<<<<<<<<<<文件读取结束>>>>>>>>>>>>>>>>>>");
        // 判断实际处理与文件总是否相符
        if (CommUtil.compare(tolnnm.value, counts.value) != 0) {
            throw FdError.FundAcct.E0001("读取银联差错文件总记录数[" + tolnnm.value+ "]与实际处理记录数[" + counts.value + "]不符！");
        }
        if (CommUtil.compare(tolnam.value, sumamt) != 0) {
            throw FdError.FundAcct.E0001("读取银联差错文件总金额[" + tolnam.value+ "]与实际处理总金额[" + sumamt + "]不符！");
        }        
        //更改读取日期标志 
        para.setParm_value4(trandt);
        KnpParameterDao.updateOne_odb1(para);
        DBTools.commit();
	}

	/**
	 * 读取小额对账差错文件
	 * @param filedt
	 */
	private void readsmamChkFile(String filedt) {
	    String fileName = null;
	    KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "readfl", "rdcrfi", "SE", true);
	    
	    //读取开关控制校验
  		if(CommUtil.compare("0", para.getParm_value5())==0){
  			biz.debug("<<<<<<<<<<读取小额对账差错文件,跳过处理>>>>>>>>>>>>>>>>>>");
  			return;
  		}
	    
        fileName=para.getParm_value1()+para.getParm_value2()+filedt+para.getParm_value3();
        File file = new File(fileName);
        if(!file.exists()){
            return;
        }
        //如果当前系统日期与参数表中日期相等，表示已读取过文件
        if(CommUtil.equals(trandt, para.getParm_value4())&&CommUtil.isNotNull(para.getParm_value4())){
            return;
        }
        biz.debug("<<<<<<<<<<获取文件路径>>>>>>>>>>>>>>>>>>"+fileName);
        //读取小额差错文件
        final ByRef<Long> tolnnm = new ByRef<Long>(0L); // 应处理总笔数
        final ByRef<Long> counts = new ByRef<Long>(0L); // 实际处理总笔数
        final ByRef<BigDecimal> tolnam = new ByRef<BigDecimal>(); // 应处理总金额
        sumamt = BigDecimal.ZERO; // 实际处理总金额
        biz.debug("<<<<<<<<<<开始读取文件>>>>>>>>>>>>>>>>>>");
        //读取小额差错文件
        FileUtil.readFile(fileName, new FileDataExecutor() {
            
            @Override
            public void process(int arg0, String arg1) {
                 MsSystemSeq.getTrxnSeq();//重置流水
                try {
                    //行信息
                    String[] line = arg1.split(fileSepa1);
                    
                    if(arg0 == 1){
                        //读取文件头
                        if (line.length != 2) {
                            throw DpModuleError.DpstComm.E9999("小额对账文件第1行字段数量不符，文件异常。");
                        }
                        tolnnm.value = Long.parseLong(line[0]);//应处理总笔数
                        tolnam.value = new BigDecimal(line[1]);//应处理总金额
                        return;
                    }
                    if(arg0 > 1){
					    String subsys = line[0];//清算渠道
					    String iotype = line[7];//往来标志
					    String pyerac = line[13];//付款人账号
					    BigDecimal tranam = new BigDecimal(line[19]);//交易金额
					    String pyeeac = line[15];//收款人账号
					    String brchno = line[8];//交易机构
					    
					    //验空
					    if(CommUtil.isNull(subsys)){
					    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第1列清算渠道不能为空！");
					    }
					    if(CommUtil.isNull(iotype)){
					    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第8列往来标志不能为空！");
					    }
					    if(CommUtil.isNull(pyerac)){
					    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第14列付款人账号不能为空！");
					    }
					    if(CommUtil.isNull(pyeeac)){
					    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第16列收款人账号不能为空！");
					    }
					    
					    //交易机构处理
					    brchno = SysUtil.getRemoteInstance(IoSrvPbBranch.class)
					    		.getRootBranch(CommUtil.isNull(brchno)?"CNY":brchno, E_BRMPTP.B);
                    	
                        //读取差错明细，并登记
                        KnlCnapotError entity = SysUtil.getInstance(KnlCnapotError.class);
                        entity.setRdtrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//读取流水
                        entity.setRdtrdt(CommTools.getBaseRunEnvs().getTrxn_date());//读取日期
                        entity.setSubsys(subsys);//清算渠道      
                        entity.setMsetdt(line[1]);//委托日期      
                        entity.setMsetsq(line[2]);//交易序号      
                        entity.setCrdbtg(line[3]);//借贷标志      
//                      entity.setBusitp(line[4]);//业务代码      
//                      entity.setBusikd(line[5]);//业务种类      
                        entity.setMesgtp(line[6]);//报文编号      
                        entity.setIotype(CommUtil.toEnum(E_IOTYPE.class, iotype));//往来标志      
                        entity.setCrcycd(line[8]);//币种          
                        entity.setCstrfg(line[9]);//现转标志      
                        entity.setCsextg(CommUtil.toEnum(E_CSEXTG.class, line[10]));//钞汇属性      
                        entity.setPyercd(line[11]);//发起行行号    
                        entity.setPyeecd(line[12]);//接收行行号    
                        entity.setPyerac(pyerac);//付款人账号    
                        entity.setPyerna(line[14]);//付款人名称    
                        entity.setPyeeac(pyeeac);//收款人账号    
                        entity.setPyeena(line[16]);//收款人名称    
                        entity.setPriotp(line[17]);//加急标志      
                        entity.setAfeetg(line[18]);//收费标志      
                        entity.setTranam(tranam);//发生额        
                        entity.setAfeeam(new BigDecimal(line[20]));//手续费        
                        entity.setFeeam1(new BigDecimal(line[21]));//汇划费        
                        entity.setChfcnb(line[22]);//对账分类编号  
                        entity.setServdt(line[23]);//支付前置日期  
                        entity.setServsq(line[24]);//支付前置流水号
                        entity.setBrchno(brchno);//交易机构号    
                        entity.setUserid(line[26]);//录入柜员      
                        entity.setCkbkus(line[27]);//复核柜员      
                        entity.setAuthus(line[28]);//授权柜员      
                        entity.setKeepdt(line[29]);//清算日期      
                        entity.setNpcpdt(line[30]);//轧差日期      
                        entity.setNpcpbt(line[31]);//轧差场次      
                        entity.setRemark1(line[32]);//备用字段1     
                        entity.setRemark2(line[33]);//备用字段2     
//                      entity.setSmrycd(line[34]);//摘要码        
                        entity.setTransq(line[35]);//核心流水      
                        entity.setTrandt(line[36]);//交易日期      
                        entity.setErroty(CommUtil.toEnum(E_CPRSST.class, line[37]));//差错类型      
                        entity.setErromk(line[38]);//差错描述    
                        
                        entity.setStatus(E_TRANST.WAIT);//状态-6待处理
                        KnlCnapotErrorDao.insert(entity);
                        counts.value++;
                        sumamt = sumamt.add(entity.getTranam());
                    }
                } catch (Exception e) {
                    throw DpModuleError.DpstComm.E9999("读取小额差错文件第" + arg0 + "条记录处理异常！",e); 
                }
                
            }
        },encoding);
        biz.debug("<<<<<<<<<<读取文件结束>>>>>>>>>>>>>>>>>>");
        // 判断实际处理与文件总是否相符
        if (CommUtil.compare(tolnnm.value, counts.value) != 0) {
            throw FdError.FundAcct.E0001("读取小额差错文件总记录数[" + tolnnm.value+ "]与实际处理记录数[" + counts.value + "]不符！");
        }
        if (CommUtil.compare(tolnam.value, sumamt) != 0) {
            throw FdError.FundAcct.E0001("读取小额差错文件总金额[" + tolnam.value+ "]与实际处理总金额[" + sumamt + "]不符！");
        }
        //更改读取日期标志 
        para.setParm_value4(trandt);
        KnpParameterDao.updateOne_odb1(para);
        DBTools.commit();
	}


	/**
	 * 读取大额对账差错文件 
	 * @param filedt
	 */
	private void readLgamChkFile(String filedt) {
		String fileName = null;
		KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "readfl", "rdcrfi", "DE", true);
		
	    //读取开关控制校验
  		if(CommUtil.compare("0", para.getParm_value5())==0){
  			biz.debug("<<<<<<<<<<读取大额对账差错文件,跳过处理>>>>>>>>>>>>>>>>>>");
  			return;
  		}
		 
		fileName=para.getParm_value1()+para.getParm_value2()+filedt+para.getParm_value3();
		File file = new File(fileName);
        if(!file.exists()){
            return;
        }
		//如果当前系统日期与参数表中日期相等，表示已读取过文件
	    if(CommUtil.equals(trandt, para.getParm_value4())&&CommUtil.isNotNull(para.getParm_value4())){
	    	biz.debug("<<<<<<<<<<["+trandt+"]日大额对账差错文件已经读取，跳过处理>>>>>>>>>>>>>>>>>>"+fileName);    
	    	return;
	     }
		 
		 biz.debug("<<<<<<<<<<获取文件路径>>>>>>>>>>>>>>>>>>"+fileName);
		//读取大额差错文件
		final ByRef<Long> tolnnm = new ByRef<Long>(0L); // 应处理总笔数
		final ByRef<Long> counts = new ByRef<Long>(0L); // 实际处理总笔数
		final ByRef<BigDecimal> tolnam = new ByRef<BigDecimal>(); // 应处理总金额
		sumamt = BigDecimal.ZERO; // 实际处理总金额
		biz.debug("<<<<<<<<<<开始读取文件>>>>>>>>>>>>>>>>>>");
		//读取大额差错文件
		FileUtil.readFile(fileName, new FileDataExecutor() {
			
			@Override
			public void process(int arg0, String arg1) {
				 MsSystemSeq.getTrxnSeq();//重置流水
				try {
					//行信息
					String[] line = arg1.split(fileSepa1);
					
					if(arg0 == 1){
						//读取文件头
						if (line.length != 2) {
							throw DpModuleError.DpstComm.E9999("大额对账文件第1行字段数量不符，文件异常。");
						}
						tolnnm.value = Long.parseLong(line[0]);//应处理总笔数
						tolnam.value = new BigDecimal(line[1]);//应处理总金额
						return;
					}
					if(arg0 > 1){
					    String subsys = line[0];//清算渠道
					    String iotype = line[7];//往来标志
					    String pyerac = line[13];//付款人账号
					    BigDecimal tranam = new BigDecimal(line[19]);//交易金额
					    String pyeeac = line[15];//收款人账号
					    String brchno = line[8];//交易机构
					    
					    //验空
					    if(CommUtil.isNull(subsys)){
					    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第1列清算渠道不能为空！");
					    }
					    if(CommUtil.isNull(iotype)){
					    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第8列往来标志不能为空！");
					    }
					    if(CommUtil.isNull(pyerac)){
					    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第14列付款人账号不能为空！");
					    }
					    if(CommUtil.isNull(pyeeac)){
					    	throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第16列收款人账号不能为空！");
					    }
					    
					    //交易机构处理
					    brchno = SysUtil.getRemoteInstance(IoSrvPbBranch.class)
					    		.getRootBranch(CommUtil.isNull(brchno)?"CNY":brchno, E_BRMPTP.B);
					    
						//读取差错明细，并登记
						KnlCnapotError entity = SysUtil.getInstance(KnlCnapotError.class);
						entity.setRdtrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//读取流水
						entity.setRdtrdt(CommTools.getBaseRunEnvs().getTrxn_date());//读取日期
						entity.setSubsys(subsys);//清算渠道      
						entity.setMsetdt(line[1]);//委托日期      
						entity.setMsetsq(line[2]);//交易序号      
						entity.setCrdbtg(line[3]);//借贷标志      
//						entity.setBusitp(line[4]);//业务代码      
//						entity.setBusikd(line[5]);//业务种类      
						entity.setMesgtp(line[6]);//报文编号      
						entity.setIotype(CommUtil.toEnum(E_IOTYPE.class, iotype));//往来标志      
						entity.setCrcycd(line[8]);//币种          
						entity.setCstrfg(line[9]);//现转标志      
						entity.setCsextg(CommUtil.isInEnum(E_CSEXTG.class, line[10])?CommUtil.toEnum(E_CSEXTG.class, line[10]):null);//钞汇属性      
						entity.setPyercd(line[11]);//发起行行号    
						entity.setPyeecd(line[12]);//接收行行号    
						entity.setPyerac(pyerac);//付款人账号    
						entity.setPyerna(line[14]);//付款人名称    
						entity.setPyeeac(pyeeac);//收款人账号    
						entity.setPyeena(line[16]);//收款人名称    
						entity.setPriotp(line[17]);//加急标志      
						entity.setAfeetg(line[18]);//收费标志      
						entity.setTranam(tranam);//发生额        
						entity.setAfeeam(new BigDecimal(line[20]));//手续费        
						entity.setFeeam1(new BigDecimal(line[21]));//汇划费        
						entity.setChfcnb(line[22]);//对账分类编号  
						entity.setServdt(line[23]);//支付前置日期  
						entity.setServsq(line[24]);//支付前置流水号
						entity.setBrchno(brchno);//交易机构号    
						entity.setUserid(line[26]);//录入柜员      
						entity.setCkbkus(line[27]);//复核柜员      
						entity.setAuthus(line[28]);//授权柜员      
						entity.setKeepdt(line[29]);//清算日期      
						entity.setNpcpdt(line[30]);//轧差日期      
						entity.setNpcpbt(line[31]);//轧差场次      
						entity.setRemark1(line[32]);//备用字段1     
						entity.setRemark2(line[33]);//备用字段2     
//						entity.setSmrycd(line[34]);//摘要码        
						entity.setTransq(line[35]);//核心流水      
						entity.setTrandt(line[36]);//交易日期      
						entity.setErroty(CommUtil.toEnum(E_CPRSST.class, line[37]));//差错类型      
						entity.setErromk(line[38]);//差错描述
						
						entity.setStatus(E_TRANST.WAIT);//状态-6待处理
						KnlCnapotErrorDao.insert(entity);
						counts.value++;
						sumamt = sumamt.add(entity.getTranam());
					}
				} catch (Exception e) {
					throw DpModuleError.DpstComm.E9999("读取大额差错文件第" + arg0 + "条记录处理异常！",e); 
				}
				
			}
		},encoding);
		biz.debug("<<<<<<<<<<文件读取结束>>>>>>>>>>>>>>>>>>");
		// 判断实际处理与文件总是否相符
		if (CommUtil.compare(tolnnm.value, counts.value) != 0) {
			throw FdError.FundAcct.E0001("读取大额差错文件总记录数[" + tolnnm.value+ "]与实际处理记录数[" + counts.value + "]不符！");
		}
		if (CommUtil.compare(tolnam.value, sumamt) != 0) {
			throw FdError.FundAcct.E0001("读取大额差错文件总金额[" + tolnam.value+ "]与实际处理总金额[" + sumamt + "]不符！");
		}
		//更改读取日期标志 
	    para.setParm_value4(trandt);
	    KnpParameterDao.updateOne_odb1(para);
	    DBTools.commit();
	}
	
}


