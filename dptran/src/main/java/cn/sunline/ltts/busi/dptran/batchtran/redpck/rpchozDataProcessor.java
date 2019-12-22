package cn.sunline.ltts.busi.dptran.batchtran.redpck;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.redpck.RpBatchTransDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBachDao;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrDetl;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrDetlDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpchoz.Input;
import cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpchoz.Property;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcSubQuota;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.DpEnumType;


/**
 * 
 * @ClassName: rpchozDataProcessor 
 * @Description: 红包批量冲正 
 * @author huangzhikai
 * @date 2016年7月19日 上午12:53:04 
 *
 */
public class rpchozDataProcessor extends
  AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpchoz.Input, cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpchoz.Property, String, cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach> {
	  private static BizLog log = BizLogUtil.getBizLog(rpbackDataProcessor.class);
	  private static kapb_wjplxxb filetab = SysUtil.getInstance(kapb_wjplxxb.class);
	  
	  /**
	     * 
	     * @Title: beforeTranProcess 
	     * @Description: 批次数据项处理逻辑前处理 
	     * @param input 批量交易输入接口
	     * @param property 批量交易属性接口
	     * @author huangzhikai
	     * @date 2016年7月19日 上午10:54:02 
	     * @version V2.3.0
	     */
		@Override
		public void beforeTranProcess(String taskId, Input input, Property property) {
			log.debug("<<===================批量冲正交易前更新处理状态======================>>");
			filetab = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
			
			property.setSourdt(filetab.getAcctdt());
			
			super.beforeTranProcess(taskId, input, property);
		}
		  
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
		public void process(String jobId, 
				int index, 
				cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach dataItem, 
				cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpchoz.Input input, 
				cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpchoz.Property property) {
			
			log.debug("<<===================批量冲正交易处理开始======================>>");
			log.debug("红包类型：" + dataItem.getRptype());
			log.debug("摘要码：" + dataItem.getSmrycd());
			log.debug("红包交易类型：" + dataItem.getRptrtp());
			log.debug("来源方日期：" + dataItem.getSourdt());
			log.debug("原交易日期：" + dataItem.getOridat());
			log.debug("原交易流水：" + dataItem.getOriseq());
			//生成新的流水
			//CommTools.genNewSerail(dataItem.getSourdt());  
			MsSystemSeq.getTrxnSeq();
			
			dataItem.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			dataItem.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
			
			//mod 20161129 新增冲正检验  查看原交易是否存在及信息核对】
			String oridat = dataItem.getOridat();//原交易日期
			String oriseq = dataItem.getOriseq();//原交易流水
			
			KnbRptrDetl tblknbrptrdetl = KnbRptrDetlDao.selectOne_odb1(oriseq, oridat, false);
			
			if(CommUtil.isNull(tblknbrptrdetl)){
				throw DpModuleError.DpstComm.E9999("原交易["+ oriseq + "]不存在，无法冲正！");
			}
			
			if(CommUtil.compare(tblknbrptrdetl.getTranam(), dataItem.getTranam()) != 0){
				throw DpModuleError.DpstComm.E9999("交易["+ oriseq + "]冲正金额与原交易金额不符，无法冲正！");
			}
			
			if(CommUtil.equals(tblknbrptrdetl.getStady1(), "S")){
				throw DpModuleError.DpstComm.E9999("交易["+ oriseq + "]已冲正，无法再次冲正！");
			}
			
			
			//判断是否为红包批量冲正交易
			if(!CommUtil.equals(dataItem.getRptrtp().getValue(), DpEnumType.E_RPTRTP.BT304.getValue())){
				throw DpModuleError.DpstComm.E9999("交易类型不是红包批量冲正，不允许冲正交易");
			}
			
//			if(CommUtil.isNotNull(dataItem.getOridat()) && CommUtil.isNotNull(dataItem.getSourdt())){
				//判断是为当日冲账还是隔日补账
//				if(CommUtil.equals(dataItem.getOridat(), dataItem.getSourdt())){
////					//判断当前是行社红包还是个人红包
////					if(CommUtil.equals(dataItem.getRptype().getValue(), DpEnumType.E_RPTRTP.SN101.getValue())
////							|| CommUtil.equals(dataItem.getRptype().getValue(), DpEnumType.E_RPTRTP.SN102.getValue())){
//                        
//						Map<String,Object> map = new HashMap<String,Object>();
//						map.put("yszjylsh",dataItem.getOriseq());
//						try {
//							BatchTools.callFlowTran("api111", map);
//						} catch (Exception e) {
//							
//						}
////					}else if(CommUtil.equals(dataItem.getRptype().getValue(), DpEnumType.E_RPTRTP.RV201.getValue())
////								|| CommUtil.equals(dataItem.getRptype().getValue(), DpEnumType.E_RPTRTP.RV202.getValue())){
////					}
//				//若为隔日补账
//				}else{
					//行社发普通红包
					if(CommUtil.equals(dataItem.getRptype().getValue(), DpEnumType.E_RPTRTP.SN101.getValue())){
						
						IoInAccount inac = SysUtil.getInstance(IoInAccount.class);//内部户记账接口
						IaTransOutPro inout_C = SysUtil.getInstance(IaTransOutPro.class); // 内部户贷方记账输出
						BigDecimal tranam = BigDecimal.ZERO.subtract(dataItem.getTranam());  
						//KnpParameter para_C = KnpParameterDao.selectOne_odb1("DPTRAN", "SENDRP", "SN101", "%", true);
						
						KnpParameter para_CD = KnpParameterDao.selectOne_odb1("DPTRAN", "RPPATH", "SN101", "%", true);
						
						//1、内部户贷方记账（记负金额）
						IaAcdrInfo acdr_C = SysUtil.getInstance(IaAcdrInfo.class);//内部户贷方方记账参数
						acdr_C.setAcbrch(dataItem.getDeborg()); // 贷方机构
						acdr_C.setBusino(para_CD.getParm_value1()); //业务代码
						acdr_C.setCrcycd(dataItem.getCrcycd()); // 币种
						acdr_C.setSmrycd(dataItem.getSmrycd()); // 摘要码
						acdr_C.setSubsac(para_CD.getParm_value4());//子户号
						acdr_C.setDscrtx(para_CD.getParm_value2()); // 描述
						acdr_C.setToacct(tblknbrptrdetl.getDebact());//对方账号
						acdr_C.setTranam(tranam);//交易金额
						
						inout_C = inac.ioInAccr(acdr_C);
						
						dataItem.setCrdact(inout_C.getAcctno());
						dataItem.setCrdnam(inout_C.getAcctna());
						
						//2、内部户借方记账(记负金额)
						IaAcdrInfo acdr_D = SysUtil.getInstance(IaAcdrInfo.class);//内部户借方记账参数
						IaTransOutPro inout_D = SysUtil.getInstance(IaTransOutPro.class); // 内部户借方记账输出
						//KnpParameter para_D = KnpParameterDao.selectOne_odb1("DPTRAN", "RECVRP", "SN101", "%", true);
						
						acdr_D.setAcbrch(dataItem.getDeborg()); // 借方机构
						acdr_D.setBusino(para_CD.getParm_value3()); // 业务代码
						acdr_D.setCrcycd(dataItem.getCrcycd()); // 币种
						acdr_D.setSmrycd(dataItem.getSmrycd()); // 摘要码
						//acdr_D.setSubsac(para_CD.getParm_value4());//子户号
						acdr_D.setDscrtx(para_CD.getParm_value2()); // 描述
						acdr_D.setToacct(tblknbrptrdetl.getCrdact());//对方账号
						acdr_D.setTranam(tranam);
						
						inout_D = inac.ioInAcdr(acdr_D); // 内部户借方记账处理
						
						dataItem.setDebact(inout_D.getAcctno());
						dataItem.setDebnam(inout_D.getAcctna());
					
					//行社发活动红包   
					}else if(CommUtil.equals(dataItem.getRptype().getValue(), DpEnumType.E_RPTRTP.SN102.getValue())){
						
						IoInAccount inac = SysUtil.getInstance(IoInAccount.class);//内部户记账接口
						IaTransOutPro inout_C = SysUtil.getInstance(IaTransOutPro.class); // 内部户贷方记账输出
						BigDecimal tranam = BigDecimal.ZERO.subtract(dataItem.getTranam());  
						//KnpParameter para_C = KnpParameterDao.selectOne_odb1("DPTRAN", "SENDRP", "SN102", "%", true);
						
						KnpParameter para_CD = KnpParameterDao.selectOne_odb1("DPTRAN", "RPPATH", "SN102", "%", true);
						
						//1、内部户贷方记账（记负金额） 红包过渡户
						IaAcdrInfo acdr_C = SysUtil.getInstance(IaAcdrInfo.class);//内部户贷方方记账参数
						acdr_C.setAcbrch(dataItem.getDeborg()); // 贷方机构
						acdr_C.setBusino(para_CD.getParm_value1()); // 业务代码 过渡户业务编码
						acdr_C.setCrcycd(dataItem.getCrcycd()); // 币种
						acdr_C.setSmrycd(dataItem.getSmrycd()); // 摘要码
						acdr_C.setSubsac(para_CD.getParm_value4());//子户号
						acdr_C.setDscrtx(para_CD.getParm_value2()); // 描述
						acdr_C.setToacct(tblknbrptrdetl.getDebact());//对方账号
						acdr_C.setTranam(tranam);
						inout_C = inac.ioInAccr(acdr_C);
						dataItem.setCrdact(inout_C.getAcctno());
						dataItem.setCrdnam(inout_C.getAcctna());
						 // 登记会计流水开始 
			            /*IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
			            cplIoAccounttingIntf.setCuacno(para_CD.getParm_value1()); //记账账号-登记核算代码
			            cplIoAccounttingIntf.setAcseno(para_CD.getParm_value1()); //子账户序号-登记核算代码
			            cplIoAccounttingIntf.setAcctno(para_CD.getParm_value1()); //负债账号-登记核算代码
			            cplIoAccounttingIntf.setProdcd(para_CD.getParm_value1()); //产品编号-登记核算代码
			            cplIoAccounttingIntf.setDtitcd(para_CD.getParm_value1()); //核算口径-登记核算代码
			            cplIoAccounttingIntf.setCrcycd(dataItem.getCrcycd()); //币种                 
			            cplIoAccounttingIntf.setTranam(tranam); //交易金额 
			            cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
			            cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水
			            cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
			            cplIoAccounttingIntf.setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch()); //账务机构
			            cplIoAccounttingIntf.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
			            cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR); //借贷标志

			            cplIoAccounttingIntf.setAtowtp(E_ATOWTP.IN); //会计主体类型-内部资金户
			            cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型-账务流水
			            cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE); //余额属性-本金科目
			            cplIoAccounttingIntf.setTranms("1020000");//交易信息
			            cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
			            IoInacInfo ioInacInfo = SysUtil.getInstance(IoInOpenClose.class).ioQueryAndOpen(para_CD.getParm_value3(), dataItem.getDeborg(), "", dataItem.getCrcycd());
			            if(CommUtil.isNotNull(ioInacInfo)){
				            cplIoAccounttingIntf.setToacct(ioInacInfo.getAcctno());
				            cplIoAccounttingIntf.setToacna(ioInacInfo.getAcctna());
			            }
			            //登记会计流水
			            SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);*/
						
						
						//2、内部户借方记账(记负金额) 红包垫款户
						IaAcdrInfo acdr_D = SysUtil.getInstance(IaAcdrInfo.class);//内部户借方记账参数
						IaTransOutPro inout_D = SysUtil.getInstance(IaTransOutPro.class); // 内部户借方记账输出
						//KnpParameter para_D = KnpParameterDao.selectOne_odb1("DPTRAN", "RECVRP", "SN102", "%", true);
						
						acdr_D.setAcbrch(dataItem.getDeborg()); // 借方机构
						acdr_D.setBusino(para_CD.getParm_value3()); // 业务代码 垫款户业务编码
						acdr_D.setCrcycd(dataItem.getCrcycd()); // 币种
						acdr_D.setSmrycd(dataItem.getSmrycd()); // 摘要码
						//acdr_D.setSubsac(para_CD.getParm_value4());//子户号
						acdr_D.setDscrtx(para_CD.getParm_value2()); // 描述
						acdr_D.setToacct(tblknbrptrdetl.getCrdact());//对方账号
						acdr_D.setTranam(tranam);
						
						inout_D = inac.ioInAcdr(acdr_D); // 内部户借方记账处理
						
						dataItem.setDebact(inout_D.getAcctno());
						dataItem.setDebnam(inout_D.getAcctna());
						
					//商户发红包	
					}else if(CommUtil.equals(dataItem.getRptype().getValue(), DpEnumType.E_RPTRTP.SN103.getValue())){
						
						IoInAccount inac = SysUtil.getInstance(IoInAccount.class);//内部户记账接口
						IaTransOutPro inout_C = SysUtil.getInstance(IaTransOutPro.class); // 内部户贷方记账输出
						BigDecimal tranam = BigDecimal.ZERO.subtract(dataItem.getTranam());  
						//KnpParameter para_C = KnpParameterDao.selectOne_odb1("DPTRAN", "SENDRP", "SN103", "%", true);
						
						KnpParameter para_CD = KnpParameterDao.selectOne_odb1("DPTRAN", "RPPATH", "SN103", "%", true);
						
						//1、内部户贷方记账（记负金额）
						IaAcdrInfo acdr_C = SysUtil.getInstance(IaAcdrInfo.class);//内部户贷方方记账参数
						
						acdr_C.setAcbrch(dataItem.getDeborg()); // 贷方机构
						acdr_C.setBusino(para_CD.getParm_value1()); // 业务代码
						acdr_C.setCrcycd(dataItem.getCrcycd()); // 币种
						acdr_C.setSmrycd(dataItem.getSmrycd()); // 摘要码
						acdr_C.setSubsac(para_CD.getParm_value4());//子户号
						acdr_C.setDscrtx(para_CD.getParm_value2()); // 描述
						acdr_C.setToacct(tblknbrptrdetl.getDecard());//对方账号
						acdr_C.setTranam(tranam);
						
						inout_C = inac.ioInAccr(acdr_C);
						
						dataItem.setCrdact(inout_C.getAcctno());
						dataItem.setCrdnam(inout_C.getAcctna());
						
						//个人结算户借方支取记账（记负金额）
						DpAcctSvcType dpac = SysUtil.getInstance(DpAcctSvcType.class);//支取记账接口
						DrawDpAcctIn acctIn = SysUtil.getInstance(DrawDpAcctIn.class);//支取记账输入参数
						IoCaKnaAcdc acdc = SysUtil.getInstance(IoCaKnaAcdc.class);//卡客户账号对照表
						KnaAcct acct = SysUtil.getInstance(KnaAcct.class);//负债活期账户信息表
						IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
						
						acdc = caqry.getKnaAcdcOdb2(dataItem.getDebact(), true);
						
						acct = CapitalTransDeal.getSettKnaAcctAc(acdc.getCustac());
//						ProcDrawStrike.Input inputInfo = SysUtil
//								.getInstance(ProcDrawStrike.Input.class);
//						ProcDrawStrikeInput cplIn = inputInfo.getStrikeInput();
//						
//						cplIn.setCustac(acct.getCustac());// 电子账户
//						cplIn.setAcctno(acct.getAcctno()); // 负债账号
//						cplIn.setStacps(E_STACPS.POSITIVE);// 冲正冲账分类
//						cplIn.setOrtrdt(dataItem.getOridat());// 原交易日期
//						cplIn.setTranam(dataItem.getTranam());// 交易金额
//						cplIn.setAmntcd(E_AMNTCD.D);// 借贷标志
//						cplIn.setColrfg(CommUtil.toEnum(E_COLOUR.class, E_HOLZJZBZ.HZ)); // 红蓝字记账标识
//						cplIn.setDetlsq(Long.parseLong(dataItem.getOriseq()));// 原交易序号
//						cplIn.setCrcycd(acct.getCrcycd());
//						cplIn.setInstam(BigDecimal.ZERO);
//						
//						SysUtil.getInstance(IoDpStrikeSvcType.class).procDrawStrike(inputInfo);
						
						acctIn.setAcctno(acct.getAcctno());
						acctIn.setAcseno("");
						acctIn.setCardno(acdc.getCardno());
						acctIn.setCrcycd(dataItem.getCrcycd());
						acctIn.setCustac(acdc.getCustac());
						acctIn.setOpacna(inout_C.getAcctna());
						acctIn.setToacct(inout_C.getAcctno());
						acctIn.setSmrycd(BusinessConstants.SUMMARY_CZ);
						acctIn.setRemark("红包冲正");
						
					//	acctIn.setSmryds(para_CD.getParm_value2());
						acctIn.setTranam(tranam);
						//支取记账处理
						dpac.addDrawAcctDp(acctIn);
						
						//额度恢复
						RevAcctQuota(acdc.getCustac(), dataItem.getOriseq(), dataItem.getOridat(), tranam.negate());
					
					//个人发红包
					}else if(CommUtil.equals(dataItem.getRptype().getValue(), DpEnumType.E_RPTRTP.SN104.getValue())){
						
						IoInAccount inac = SysUtil.getInstance(IoInAccount.class);//内部户记账接口
						IaTransOutPro inout_C = SysUtil.getInstance(IaTransOutPro.class); // 内部户贷方记账输出
						BigDecimal tranam = BigDecimal.ZERO.subtract(dataItem.getTranam());  
						//KnpParameter para_C = KnpParameterDao.selectOne_odb1("DPTRAN", "SENDRP", "SN104", "%", true);
						
						KnpParameter para_CD = KnpParameterDao.selectOne_odb1("DPTRAN", "RPPATH", "SN104", "%", true);
						
						//1、内部户贷方记账（记负金额）
						IaAcdrInfo acdr_C = SysUtil.getInstance(IaAcdrInfo.class);//内部户贷方方记账参数
						acdr_C.setAcbrch(dataItem.getDeborg()); // 贷方机构
						acdr_C.setBusino(para_CD.getParm_value1()); // 业务代码
						acdr_C.setCrcycd(dataItem.getCrcycd()); // 币种
						acdr_C.setSmrycd(dataItem.getSmrycd()); // 摘要码
						acdr_C.setSubsac(para_CD.getParm_value4());//子户号
						acdr_C.setDscrtx(para_CD.getParm_value2()); // 描述
						acdr_C.setToacct(tblknbrptrdetl.getDecard());//对方账号
						acdr_C.setTranam(tranam);
						
						inout_C = inac.ioInAccr(acdr_C);
						
						dataItem.setCrdact(inout_C.getAcctno());
						dataItem.setCrdnam(inout_C.getAcctna());
						
						//个人结算户借方支取记账（记负金额）
						DpAcctSvcType dpac = SysUtil.getInstance(DpAcctSvcType.class);//支取记账接口
						DrawDpAcctIn acctIn = SysUtil.getInstance(DrawDpAcctIn.class);//支取记账输入参数
						IoCaKnaAcdc acdc = SysUtil.getInstance(IoCaKnaAcdc.class);//卡客户账号对照表
						KnaAcct acct = SysUtil.getInstance(KnaAcct.class);//负债活期账户信息表
						IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
						
						acdc = caqry.getKnaAcdcOdb2(dataItem.getDebact(), true);
						
						acct = CapitalTransDeal.getSettKnaAcctAc(acdc.getCustac());
						
//						ProcDrawStrike.Input inputInfo = SysUtil
//								.getInstance(ProcDrawStrike.Input.class);
//						ProcDrawStrikeInput cplIn = inputInfo.getStrikeInput();
//						
//						long detlsq = Long.parseLong(dataItem.getOriseq());
//						
//						cplIn.setCustac(acct.getCustac());// 电子账户
//						cplIn.setAcctno(acct.getAcctno()); // 负债账号
//						cplIn.setStacps(E_STACPS.POSITIVE);// 冲正冲账分类
//						cplIn.setOrtrdt(dataItem.getOridat());// 原交易日期
//						cplIn.setTranam(dataItem.getTranam());// 交易金额
//						cplIn.setAmntcd(E_AMNTCD.D);// 借贷标志
//						cplIn.setColrfg(CommUtil.toEnum(E_COLOUR.class, E_HOLZJZBZ.HZ)); // 红蓝字记账标识
//						cplIn.setDetlsq(detlsq);// 原交易序号
//						cplIn.setCrcycd(acct.getCrcycd());
//						cplIn.setInstam(BigDecimal.ZERO);
//						//支取冲正
//						SysUtil.getInstance(IoDpStrikeSvcType.class).procDrawStrike(inputInfo);
						acctIn.setAcctno(acct.getAcctno());
						acctIn.setAcseno("");
						acctIn.setCardno(acdc.getCardno());
						acctIn.setCrcycd(dataItem.getCrcycd());
						acctIn.setCustac(acdc.getCustac());
						acctIn.setOpacna(inout_C.getAcctna());
						acctIn.setToacct(inout_C.getAcctno());
						acctIn.setSmrycd(BusinessConstants.SUMMARY_CZ);
						acctIn.setRemark("红包冲正");
						acctIn.setTranam(tranam);
						//支取记账处理
						dpac.addDrawAcctDp(acctIn);
						
						//额度恢复
						RevAcctQuota(acdc.getCustac(), dataItem.getTransq(), dataItem.getOridat(),tranam.negate());
					}
				
				//平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._10);
				
					
				//冲正结束修改红包系统交易明细表中备用字段1为“S”，表示已经冲正
				tblknbrptrdetl.setStady1("S");
				KnbRptrDetlDao.updateOne_odb1(tblknbrptrdetl);
		//		}
				//主交易流水
				dataItem.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
				//主交易日期
				dataItem.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
				//成功状态
				dataItem.setTranst(E_TRANST.SUCCESS);
				//成功描述
				dataItem.setDescrb("成功完成");
				
				KnbRptrBachDao.updateOne_odb1(dataItem);
				log.debug("<<===================红包批量冲正交易处理结束======================>>");
			}
			
	//	}
		
		/**
		 * 某一笔失败处理，更新对应状态。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public void jobExceptionProcess(String taskId, Input input,
				Property property, String jobId, KnbRptrBach dataItem, Throwable t) {
			
			dataItem.setTranst(E_TRANST.FAIL);
			dataItem.setDescrb(t.getMessage());
			KnbRptrBachDao.updateOne_odb1(dataItem);
			//super.jobExceptionProcess(taskId, input, property, jobId, dataItem, t);
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<String> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpchoz.Input input, cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpchoz.Property property) {
			log.debug("<<===================获取作业编号开始======================>>");
			 Params param = new Params();
			 param.put("filesq", input.getFilesq());
			 param.put("sourdt", property.getSourdt());
			
			return new CursorBatchDataWalker<String>(
					RpBatchTransDao.namedsql_selDataidByFilesq,param);
		}
		
		
		/**
		 * 获取作业数据遍历器
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @param dataItem 批次数据项
		 * @return
		 */
		public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach> getJobBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpchoz.Input input, cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpchoz.Property property, String dataItem) {
			log.debug("<<===================获取作业数据集合开始======================>>");
			Params param = new Params();
			param.put("filesq", input.getFilesq());
			param.put("sourdt", property.getSourdt());
			param.put("dataid", dataItem);
			log.debug("<<===================获取作业数据集合结束======================>>");
			return new CursorBatchDataWalker<KnbRptrBach>(
					RpBatchTransDao.namedsql_selBatchDataByFilesq, param);
		}
	  
		/**
		 * 
		 * @Title: afterTranProcess 
		 * @Description: 交易后处理 
		 * @param taskId
		 * @param input
		 * @param property
		 * @author huangzhikai
		 * @date 2016年7月7日 上午11:00:01 
		 * @version V2.3.0
		 */
		public void afterTranProcess(
			String taskId,
			cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpchoz.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rpchoz.Property property) {

			super.afterTranProcess(taskId, input, property);
		}
		
		//add 20170222	songlw 账户额度扣减 红字
		public void RevAcctQuota(String custac, String mntrsq, String trandt, BigDecimal tranam){
			//初始化
			IoCaSevAccountLimit caSevAccountLimit = SysUtil.getInstance(IoCaSevAccountLimit.class);	
			IoAcSubQuota.InputSetter inputSetter = SysUtil.getInstance(IoAcSubQuota.InputSetter.class);
			IoAcSubQuota.Output output = SysUtil.getInstance(IoAcSubQuota.Output.class);
			
			inputSetter.setCustac(custac);
			inputSetter.setServsq(mntrsq); //渠道来源流水
			inputSetter.setServdt(trandt); //渠道来源日期 
			inputSetter.setServtp("NR"); //渠道  默认红包
			inputSetter.setTranam(tranam);
			inputSetter.setOldate(trandt);
			//账户额度恢复
			caSevAccountLimit.SubAcctQuota(inputSetter, output);
		}
}


