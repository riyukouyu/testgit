package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.dayend.DpDayEndInt;
import cn.sunline.ltts.busi.dp.layer.LayerAccrued;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.CrcabrProcData;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.IntrSubSection;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.KtpAdinType;
import cn.sunline.ltts.busi.dptran.batchtran.intf.Befabr.Input;
import cn.sunline.ltts.busi.dptran.batchtran.intf.Befabr.Property;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbInRaSelSvc;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaBatchWarnInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
//import cn.sunline.ltts.busi.ltts.zjrc.infrastructure.dap.plugin.type.ECustPlugin.E_MEDIUM;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRRTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_REPRWY;

//import com.alibaba.fastjson.JSON;

	 /**
	  * 负债账户日终预计提
	  *
	  */

public class befabrDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.intf.Befabr.Input, cn.sunline.ltts.busi.dptran.batchtran.intf.Befabr.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.CrcabrProcData> {
	private final static BizLog bizlog = BizLogUtil.getBizLog(BizLog.class);
	/**
		 * 批次数据项处理逻辑。
		 * 
		 * @param job 批次作业ID
		 * @param index  批次作业第几笔数据(从1开始)
		 * @param dataItem 批次数据项
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 */
		@Override
		public void process(String jobId, int index, cn.sunline.ltts.busi.dp.type.DpDayEndType.CrcabrProcData dataItem, cn.sunline.ltts.busi.dptran.batchtran.intf.Befabr.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Befabr.Property property) {
			 
			String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
			
//			CommTools.getBaseRunEnvs().setBusi_org_id(dataItem.getCorpno()); //交易前设置法人
			
/*			=================利率调整beg===================
		    boolean result=	adjustInterestRate(dataItem.getAcctno());
		
			=================利率调整end===================
			
			=================税率调整beg===================
		    if(!result){
		    	//已经做过利率调整，不进行税率调整
		    	
		    	adjustInterestTax(dataItem.getAcctno());
		    }
		
			=================税率调整end===================*/
			
			
			//预计提为日期后，日期即为当前日期预计提
            String lstDay =  dataItem.getTrandt();
            int count =0;
//            String tranDay =   DateTimeUtil.dateAdd("day", dataItem.getTrandt(), 1);
            String tranDay =   DateTools2.getDateInfo().getNextdt();
            //通过计提明细knb_cbdl有无当日计提数据，判断是否已预计提
            count = DpDayEndDao.selcountcbdlacct(lstDay, dataItem.getAcctno(), false);
            if(CommUtil.compare(count, 0)>0){
            	bizlog.error("负债账户"+dataItem.getAcctno()+"计提日期[%s]已计提，将跳过不重复计提", lstDay);
            	return;
            }
            
            KnbAcin tblKnbAcin = KnbAcinDao.selectOneWithLock_odb1(dataItem.getAcctno(), true);
            
            //add by 20171214 结息日等于当天的不做预计提 （活期帐户日终余额变化会做增量计提，预计提没有必要；定期账户如果预计提批量先跑，会造成预计提不对）                                                      
            if(CommUtil.compare(tblKnbAcin.getNcindt(),DateTools2.getDateInfo().getSystdt()) == 0 ){
            	bizlog.error("负债账户"+dataItem.getAcctno()+"下次结息日期[%s]为当天，将跳过不做预计提", tblKnbAcin.getNcindt());
            	return;
            }          
            
			//计息处理: 计提是在日切后执行, 计提后下次计息日期就更新为交易日期, 因此下次计息日小于交易日期是计息执行的条件
//				if(CommUtil.compare(tblKnbAcin.getNxindt(), tranDay)<0){
//					if(tblKnbAcin.getIncdtp() == E_IRCDTP.LAYER){ //分层计提
//						DpDayEndLayerClcIntr.prcCrcLay(tblKnbAcin,lstDay,tranDay);
//						return;
//					}
//					DpDayEndInt.prcCrcabr(tblKnbAcin,lstDay,tranDay); 
//				}else{
//					bizlog.error("负债账户"+tblKnbAcin.getAcctno()+"计提日期[%s]不满足计提条件，不计提", tblKnbAcin.getNxindt());
//				}
        
//				if(tblKnbAcin.getIncdtp() == E_IRCDTP.LAYER){ //分层计提
//					DpDayEndLayerClcIntr.prcCrcLay(tblKnbAcin,lstDay,tranDay,E_YES___.NO);
//					return; (dataItem.getAcctno() == "801000011456077")
//				}
            if (tblKnbAcin.getIncdtp() == E_IRCDTP.LAYER && tblKnbAcin.getInclfg() == E_YES___.NO) { // 分层计提
            	LayerAccrued.prcCrcLay(tblKnbAcin,lstDay,tranDay,E_YES___.NO);
            }else{
            	DpDayEndInt.prcCrcabr(tblKnbAcin,lstDay,tranDay,E_YES___.NO); 
            }
//			CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno); //交易后设置法人为原法人
					
		}
		
		
		@Override
		public void jobExceptionProcess(String taskId, Input input,
				Property property, String jobId, CrcabrProcData dataItem,
				Throwable t) {
			//监控预警平台
			KnpParameter para = KnpParameterDao.selectOne_odb1("DAYENDNOTICE", "%", "%", "%", true);
			
			String bdid = para.getParm_value1();// 服务绑定ID
			
			String mssdid = CommTools.getMySysId();// 随机生成消息ID
			
			String mesdna = para.getParm_value2();// 媒介名称
			
//			E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE;// 消息媒介
			
			IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
			
			IoCaOtherService.IoCaDayEndFailNotice.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaDayEndFailNotice.InputSetter.class);
			
			String timetm = DateTools2.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS000");
			IoCaBatchWarnInfo content = SysUtil.getInstance(IoCaBatchWarnInfo.class);
			content.setPljioyma("befabr");
			content.setPljyzbsh("90060");
			content.setPljyzwmc("预计提异常预警");
			content.setErrmsg("预计提失败");
			content.setTrantm(timetm);
			
			// 发送消息
			mqInput.setMsgid(mssdid); // 消息ID
//			mqInput.setMedium(mssdtp); // 消息媒介
			mqInput.setMdname(mesdna); // 媒介名称
			mqInput.setTypeCode("NAS");
			mqInput.setTypeName("网络金融核心平台-电子账户核心系统");
			mqInput.setItemId("NAS_BATCH_WARN");
			mqInput.setItemName("电子账户核心批量执行错误预警");
			
//			String str =JSON.toJSONString(content);
//			mqInput.setContent(str);
			
			mqInput.setWarnTime(timetm);
			
			caOtherService.dayEndFailNotice(mqInput);
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.CrcabrProcData> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.intf.Befabr.Input input, cn.sunline.ltts.busi.dptran.batchtran.intf.Befabr.Property property) {
			String trandt = DateTools2.getDateInfo().getSystdt();
			String lstrdt = DateTools2.getDateInfo().getLastdt();
			
			Params params = new Params();
			params.add("trandt", trandt);
			params.add("lstrdt", lstrdt);
			params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
//			params.add("trandt", "20160321");
//			params.add("lstrdt", "20160320");

			return new CursorBatchDataWalker<CrcabrProcData>(
					DpDayEndDao.namedsql_selCrcabrAcctData, params);
		}
		
		/***
		 * @Title: adjustInterestRate 
		 * @Description: 利率调整处理
		 * @param acctno
		 * @author chenlk
		 */
		public  boolean  adjustInterestRate( String acctno){
			
			KtpAdinType   cplAdinType  = DpDayEndDao.selAdinTranDataByAcctno(acctno,CommTools.getBaseRunEnvs().getTrxn_date(), false);
			if(CommUtil.isNull(cplAdinType)){
				//不需要调整
				return false;
			}
//			String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
//			String lstrdt = CommTools.getBaseRunEnvs().getLast_date();
			
			String trandt = DateTools2.getDateInfo().getSystdt();
			String lstrdt = DateTools2.getDateInfo().getLastdt();
			
			E_TERMCD depttm = cplAdinType.getLvindt();//存期
			String intrcd = cplAdinType.getIntrcd(); //定义利率代码变量
			E_IRCDTP incdtp = cplAdinType.getIncdtp();//利率代码类型

			List<IntrSubSection> lst = null;
			if(E_IRCDTP.Reference==incdtp){
				//基础利率代码
				
				 lst = DpDayEndDao.selKupRfirIntrByIntrcd(trandt, lstrdt, intrcd, depttm, false);
				 
			}else if(E_IRCDTP.BASE==incdtp){
				//浮动利率
				lst = DpDayEndDao.selKupBkirIntrByIntrcd(trandt, lstrdt, intrcd, depttm, false);
				
			}else if(E_IRCDTP.LAYER==incdtp){
				//靠当利率类型不处理
				return false;
			} else{
				
            	bizlog.error("负债账户"+acctno+"错误的利率代码类型");
            	return false;
            	
			}
			if(CommUtil.isNotNull(lst)){
				//存在利率调整
				bizlog.debug("开始调整账户["+cplAdinType.getAcctno()+"]的利率");
				bizlog.debug("账户信息===================="+cplAdinType);

				String timetm = DateTools2.getCurrentTimestamp();
				
				E_DPACST acctst = null;
				BigDecimal onlnbl = BigDecimal.ZERO;
				String brchno = "";
				String crcycd = null;
				
				if(cplAdinType.getPddpfg() == E_FCFLAG.CURRENT){
					KnaAcct acct = KnaAcctDao.selectOne_odb1(cplAdinType.getAcctno(), true);
					acctst = acct.getAcctst();
					onlnbl = DpAcctProc.getAcctBalance(acct);
					brchno = acct.getBrchno();
					crcycd = acct.getCrcycd();
				}else if(cplAdinType.getPddpfg() == E_FCFLAG.FIX){
					KnaFxac fxac = KnaFxacDao.selectOne_odb1(cplAdinType.getAcctno(), true);
					acctst = fxac.getAcctst();
					onlnbl = fxac.getOnlnbl();
					brchno = fxac.getBrchno();
					crcycd = fxac.getCrcycd();
				}
				
				if(acctst != E_DPACST.NORMAL){ //非正常账户排除
					return false;
				}
				
				E_YES___ isrect = E_YES___.NO; //是否需要调整
				E_YES___ isblock = E_YES___.NO; //是否需要分段
				
				if(cplAdinType.getInprwy() == E_IRRTTP.AZ){ //指定周期重定价
					String inadlv = cplAdinType.getInadlv(); //利率调整频率
					if(CommUtil.isNull(cplAdinType.getLuindt())){
						cplAdinType.setLuindt(cplAdinType.getOpendt());
					}
					
//					String inaddt = DateTools2.calDateByFreq(cplAdinType.getLuindt(), inadlv, "", "", 3, 0);
					String inaddt = DateTools2.calDateByFreq(cplAdinType.getLuindt(), inadlv, null, 0);
					if(CommUtil.equals(inaddt, trandt)){
						isrect = E_YES___.YES;
						cplAdinType.setLuindt(inaddt);
					}
				}else if(cplAdinType.getInprwy() == E_IRRTTP.CK){
					isrect = E_YES___.YES;
				}else if(cplAdinType.getInprwy() == E_IRRTTP.MT){ //升息比重重定价
					throw DpModuleError.DpstComm.E9999("系统暂不支持:[" + E_IRRTTP.MT.getLongName() + "]");
				}else if(cplAdinType.getInprwy() == E_IRRTTP.QD || cplAdinType.getInprwy() == E_IRRTTP.NO){ //到期转存重订价
					return false;
				}
				
				if(cplAdinType.getReprwy() == E_REPRWY.ALL){ //全部调整,表示不需要分段
					isblock = E_YES___.NO;
				}else if(cplAdinType.getReprwy() == E_REPRWY.BACK){ //后段调整，表示需要分段
					isblock = E_YES___.YES;
				}else if(cplAdinType.getReprwy() == E_REPRWY.PART){ //升息比重重定价时使用，前后段分别调整
					throw DpModuleError.DpstComm.E9999("系统暂不支持:[" + E_REPRWY.PART.getLongName() + "]");
				}
				
				if(isrect == E_YES___.YES){
					IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);

					// 利率调整先查询当前执行利率和基准利率
					//IntrPublicEntity entity = SysUtil.getInstance(IntrPublicEntity.class);
					IoPbIntrPublicEntity entity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
					entity.setCrcycd(cplAdinType.getCrcycd());
					entity.setIntrcd(cplAdinType.getIntrcd());
					entity.setIncdtp(cplAdinType.getIncdtp());
					entity.setTrandt(trandt);
					entity.setDepttm(E_TERMCD.T000);
					
					entity.setCorpno(cplAdinType.getCorpno());;
					entity.setBrchno(brchno);
					
					entity.setLevety(cplAdinType.getLevety());
					entity.setDepttm(depttm);
				/*	if(dataItem.getIntrdt() == E_INTRDT.OPEN){
						entity.setTrandt(dataItem.getOpendt());
						entity.setTrantm("999999");
					}*/
					
					pbpub.countInteresRate(entity);
					
					BigDecimal favovl = cplAdinType.getFavovl();
					BigDecimal favort = cplAdinType.getFavort();
					
					BigDecimal baseir = entity.getBaseir();
					BigDecimal intrvl = entity.getIntrvl();
					BigDecimal cuusin = intrvl.add(intrvl.multiply(favort.divide(BigDecimal.valueOf(100))).add(favovl));
					
					//mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
					//利率的最大范围值
					BigDecimal intrvlmax = entity.getBaseir().multiply(BigDecimal.ONE.add(entity.getFlmxsc().divide(BigDecimal.valueOf(100))));
					//利率的最小范围值
					BigDecimal intrvlmin = entity.getBaseir().multiply(BigDecimal.ONE.add(entity.getFlmnsc().divide(BigDecimal.valueOf(100))));
					
					if(CommUtil.compare(cuusin, intrvlmin)<0){
						cuusin = intrvlmin;
					}else if(CommUtil.compare(cuusin, intrvlmax)>0){
						cuusin = intrvlmax;
					}
					//mod by leipeng   优惠后判断时候超出基础浮动范围20170220  end--
					
					if(isblock == E_YES___.YES){

						//1、计算账户积数
						BigDecimal curram = cplAdinType.getCutmam();
						//BigDecimal onlnbl = DpPublic.getOnlnblByAcctno(dataItem.getAcctno());
						
						bizlog.debug("当前日期：["+trandt+"]", "");
						String lsamdt = cplAdinType.getLaamdt();
						bizlog.debug("当前日期：["+lsamdt+"]", "");
						BigDecimal cut = DpPublic.calRealTotalAmt(curram, onlnbl, trandt, lsamdt);
						
						// 利息税计算
						BigDecimal taxrat = BigDecimal.ZERO;//利息税率
						if(CommUtil.isNotNull(cplAdinType.getIntxcd())){
							
							taxrat = SysUtil.getInstance(IoPbInRaSelSvc.class).inttxRate(cplAdinType.getIntxcd()).getTaxrat();
						}
						BigDecimal taxCut =  BigDecimal.ZERO;//利息税
						BigDecimal rlintr =  BigDecimal.ZERO;// 利息
						if(!CommUtil.equals(BigDecimal.ZERO, cut)){
							rlintr = pbpub.countInteresRateByBase(cplAdinType.getCuusin(), cut);
							taxCut = rlintr.multiply(taxrat).divide(BigDecimal.valueOf(100));
						}
						
						rlintr = BusiTools.roundByCurrency(crcycd, rlintr,null);
						taxCut = BusiTools.roundByCurrency(crcycd, taxCut,null);
						//2、记录到负债账户利息明细表
						KnbIndl indl = SysUtil.getInstance(KnbIndl.class);
						
						indl.setGradin(BigDecimal.ZERO);//档次计息余额
						indl.setTotlin(BigDecimal.ZERO);//总计息余额
						indl.setRlintr(rlintr);//实际利息发生额
						indl.setCatxrt(taxrat);//计提税率
						indl.setRlintx(taxCut);//实际利息税发生额
						
						indl.setAcbsin(cplAdinType.getAcbsin());//基准利率
						indl.setAcctno(cplAdinType.getAcctno());//负债账号
						indl.setAcmltn(cut);//积数
						indl.setCuusin(cplAdinType.getCuusin());//当前执行利率
						indl.setDetlsq(getDetlsq(cplAdinType.getAcctno(),cplAdinType.getIntrtp()));//明细序号
						indl.setIncdtp(cplAdinType.getIncdtp());//利率代码类型
						indl.setIndlst(E_INDLST.YOUX);//负债利息明细状态
						indl.setIndxno(getIndexNo(cplAdinType.getAcctno(),cplAdinType.getIntrtp()));
						indl.setIneddt(trandt);//计息终止日期
						indl.setInstdt(cplAdinType.getInstdt());//计息开始日期
						indl.setIntrcd(cplAdinType.getIntrcd());//利率编号
						indl.setIntrdt(trandt);//计息日期
						indl.setIntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//计息流水
						indl.setIntrwy(cplAdinType.getIntrwy());//利率靠档方式
						indl.setLsinoc(E_INDLTP.UPIR);//上次利息操作代码
						indl.setLvamot(cplAdinType.getLvamot());
						indl.setLvindt(cplAdinType.getLvindt());
						indl.setLyinwy(cplAdinType.getLyinwy());
						indl.setTxbebs(cplAdinType.getTxbebs());
						indl.setIntrtp(cplAdinType.getIntrtp());
						try {
							KnbIndlDao.insert(indl);
						} catch (Exception e) {
							DpModuleError.DpstAcct.BNAS1735();
						}
						//修改负债账户计息明细
						DpDayEndDao.upAcinForSubByAcctno(cplAdinType.getAcctno(), trandt, trandt, BigDecimal.ZERO , E_INDLTP.UPIR, trandt,timetm);
					
					}else{
						List<KnbIndl> indllist = KnbIndlDao.selectAll_odb4(acctno, E_INDLST.YOUX, false);
						
						for(KnbIndl indl :indllist){
							BigDecimal newinstam = pbpub.countInteresRateByBase(cuusin, indl.getAcmltn());
							BigDecimal newintxam =newinstam.multiply(indl.getCatxrt()); 
							
							newinstam = BusiTools.roundByCurrency(crcycd, newinstam,null);//新的利息
							newintxam = BusiTools.roundByCurrency(crcycd, newintxam,null);//新的利息税
							
							indl.setRlintr(newinstam);
							indl.setRlintx(newintxam);//实际利息税发生额
							indl.setCuusin(cuusin);//当前执行利率
							try {
								KnbIndlDao.updateOne_odb1(indl);
							} catch (Exception e) {
								DpModuleError.DpstAcct.BNAS1736();
							}
							
						}
						
						
					}
					
					
					
					//修改账户利率信息表
					DpDayEndDao.upKubinrtForSubByAcctno(cplAdinType.getAcctno(), baseir, cuusin, entity.getFlirwy(), entity.getFlirvl(), entity.getFlirrt(),timetm);
					
					
					//DpDayEndDao.delknbcbdlData(lstrdt, cplAdinType.getAcctno()); // 删除切日后的上日预计提数据
				}
				
		
				
				return true;
			}
			return false;
		}
		

		/***
		 * @Title: adjustInterestTax 
		 * @Description: 利息税率调整处理
		 * @param acctno
		 * @author chenlk
		 */
		public  void  adjustInterestTax( String acctno){
			
			KtpAdinType   cplAdinTax  = DpDayEndDao.selAdinTranDataByAcctnoTax(acctno,CommTools.getBaseRunEnvs().getTrxn_date(), false);
			if(CommUtil.isNull(cplAdinTax)){
				//不需要调整
				return ;
			}
//			String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
//			String lstrdt = CommTools.getBaseRunEnvs().getLast_date();
			
			String trandt = DateTools2.getDateInfo().getSystdt();
			String lstrdt = DateTools2.getDateInfo().getLastdt();

			
			bizlog.debug("开始调整账户["+cplAdinTax.getAcctno()+"]的税率");
			bizlog.debug("cplAdinTax===================="+cplAdinTax);
			
 			String timetm = DateTools2.getCurrentTimestamp();
			
			E_DPACST acctst = null;
			BigDecimal onlnbl = BigDecimal.ZERO;
			String crcycd = null;
			
			if(cplAdinTax.getPddpfg() == E_FCFLAG.CURRENT){
				KnaAcct acct = KnaAcctDao.selectOne_odb1(cplAdinTax.getAcctno(), true);
				acctst = acct.getAcctst();
				onlnbl = DpAcctProc.getAcctBalance(acct);
				crcycd = acct.getCrcycd();
			}else if(cplAdinTax.getPddpfg() == E_FCFLAG.FIX){
				KnaFxac fxac = KnaFxacDao.selectOne_odb1(cplAdinTax.getAcctno(), true);
				acctst = fxac.getAcctst();
				onlnbl = fxac.getOnlnbl();
				crcycd = fxac.getCrcycd();
			}else{
				
			}
			
			if(acctst != E_DPACST.NORMAL){ //非正常账户排除
				return;
			}
			
			BigDecimal taxrat = SysUtil.getInstance(IoPbInRaSelSvc.class).getNearTxTate(cplAdinTax.getIntxcd(), lstrdt).getTaxrat();//税率
			
			//1、计算账户积数
			BigDecimal curram = cplAdinTax.getCutmam();
			//BigDecimal onlnbl = DpPublic.getOnlnblByAcctno(dataItem.getAcctno());
			
			bizlog.debug("当前日期：["+trandt+"]", "");
			String lsamdt = cplAdinTax.getLaamdt();
			bizlog.debug("当前日期：["+lsamdt+"]", "");
			BigDecimal cut = DpPublic.calRealTotalAmt(curram, onlnbl, trandt, lsamdt);
			BigDecimal rlintr = BigDecimal.ZERO;// 利息
			
			KubInrt inrt = KubInrtDao.selectOne_odb1(cplAdinTax.getAcctno(), true);
			
			// 利息税发生额
			BigDecimal taxCut =  BigDecimal.ZERO;
			if(!CommUtil.equals(BigDecimal.ZERO, cut)){
				IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
				rlintr = pbpub.countInteresRateByBase(inrt.getCuusin(), cut);
				taxCut = rlintr.multiply(taxrat);
			}
			
			rlintr = BusiTools.roundByCurrency(crcycd, rlintr,null);
			taxCut = BusiTools.roundByCurrency(crcycd, taxCut,null);
			
			//2、记录到负债账户利息明细表
			KnbIndl indl = SysUtil.getInstance(KnbIndl.class);
			
			indl.setGradin(BigDecimal.ZERO);//档次计息余额
			indl.setTotlin(BigDecimal.ZERO);//总计息余额
			indl.setRlintr(rlintr);//实际利息发生额
			indl.setCatxrt(taxrat);//计提税率
			indl.setRlintx(taxCut);//实际利息税发生额
			
			indl.setAcbsin(inrt.getBsintr());//基准利率
			indl.setAcctno(cplAdinTax.getAcctno());//负债账号
			indl.setAcmltn(cut);//积数
			indl.setCuusin(inrt.getCuusin());//当前执行利率
			indl.setDetlsq(getDetlsq(cplAdinTax.getAcctno(),cplAdinTax.getIntrtp()));//明细序号
			indl.setIncdtp(inrt.getIncdtp());//利率代码类型
			indl.setIndlst(E_INDLST.YOUX);//负债利息明细状态
			indl.setIndxno(getIndexNo(cplAdinTax.getAcctno(),cplAdinTax.getIntrtp()));
			indl.setIneddt(trandt);//计息终止日期
			indl.setInstdt(cplAdinTax.getInstdt());//计息开始日期
			indl.setIntrcd(cplAdinTax.getIntrcd());//利率编号
			indl.setIntrdt(trandt);//计息日期
			indl.setIntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//计息流水
			indl.setIntrwy(inrt.getIntrwy());//利率靠档方式
			indl.setLsinoc(E_INDLTP.UPIR);//上次利息操作代码
			indl.setLvamot(inrt.getLvamot());
			indl.setLvindt(inrt.getLvindt());
			indl.setLyinwy(inrt.getLyinwy());
			indl.setTxbebs(cplAdinTax.getTxbebs());
			indl.setIntrtp(inrt.getIntrtp());
			try {
				KnbIndlDao.insert(indl);
			} catch (Exception e) {
				DpModuleError.DpstAcct.BNAS1735();
			}
			//修改负债账户计息明细
			DpDayEndDao.upAcinForSubByAcctno(cplAdinTax.getAcctno(), trandt, trandt, BigDecimal.ZERO , E_INDLTP.UPIR, trandt,timetm);
					
			
		}
		
		//循序号 和 明细序号
		private Long getDetlsq(String acctno, E_INTRTP intrtp) {
			//查询该账户信息是否存在该表中
			long count = 0;
			try {
				count = DpDayEndDao.getAcctnoIsInIndl(acctno, intrtp, true);
			} catch (Exception e) {
				return new Long(1);
			} 
			return count+1;
			
		}
		//明细序号 暂时都为1
		private Long getIndexNo(String acctno, E_INTRTP intrtp) {
			
			return new Long(1);
		}		
}


