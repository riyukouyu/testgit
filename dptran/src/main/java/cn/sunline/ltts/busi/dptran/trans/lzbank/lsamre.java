package cn.sunline.ltts.busi.dptran.trans.lzbank;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
//import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppCldt;
//import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppCldtDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.ap.iobus.type.IoApReverseType.IoApReverseIn;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
//import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
//import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapotDao;
import cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Lsamre.Input;
//import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
//import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CRDBTG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOTYPE;


public class lsamre {

	private static final BizLog bizlog = BizLogUtil.getBizLog(lsamre.class);
	
	/**
	 * 冲账
	 * @param input
	 * @param output
	 */
	public static void dealLsamre( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Lsamre.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Lsamre.Output output){
		
			String msetdt = input.getMsetdt();//原委托日期 
			String msetsq = input.getMsetsq();//原交易序号 
			String pyercd = input.getPyercd();//原发起行号
			E_IOTYPE iotype = input.getIotype();//原往来标志 
			E_CRDBTG crdbtg = input.getCrdbtg();//原借贷标志

		
			/**
			 * 输入检查
			 */
			preTranCheck(input); 
			
			/**
			 * 获取原纪录
			 */
			KnlCnapot tblKnlCnapot1 = KnlCnapotDao.selectOne_odb2(msetdt, msetsq, crdbtg.getValue(), iotype, pyercd, E_TRANST.NORMAL, false);
			
			/**
			 * 冲账交易先于正交易到达处理
			 */
			if(CommUtil.isNull(tblKnlCnapot1)){
				KnlCnapot tblKnlCnapot2 = KnlCnapotDao.selectOne_odb2(msetdt, msetsq, crdbtg.getValue(), iotype, pyercd, E_TRANST.STRIKED, false);
				if(CommUtil.isNotNull(tblKnlCnapot2)){
					BusiTools.getBusiRunEnvs().setRemark("交易已冲正");
					output.setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch()); 
					return;
				}
				KnlCnapot entity = SysUtil.getInstance(KnlCnapot.class);
				entity.setSubsys(input.getSubsys());//原清算渠道
				entity.setMsetdt(input.getMsetdt());//原委托日期
				entity.setMsetsq(input.getMsetsq());//原交易序号
				entity.setCrdbtg(input.getCrdbtg().getValue());//原借贷标志
				entity.setMesgtp(input.getMesgtp()); //原报文编号
				entity.setIotype(input.getIotype()); //原往来标志
				entity.setCrcycd(input.getCrcycd()); //原币种
				entity.setCstrfg(input.getCstrfg()); //原现转标志
				entity.setCsextg(input.getCsextg()); //原钞汇属性
				entity.setPyercd(input.getPyercd()); //原发起行行号
				entity.setPyeecd(input.getPyeecd()); //原接受行行号
				entity.setPyerac(input.getPyerac()); //原付款人账号
				entity.setPyerna(input.getPyerna()); //原付款人名称
				entity.setPyeeac(input.getPyeeac()); //原收款人账号
				entity.setPyeena(input.getPyeena()); //原收款人名称
				entity.setPriotp(input.getPriotp()); //原加急标志
				entity.setAfeetg(input.getAfeetg()); //原收费标志
				entity.setTranam(input.getTranam()); //原发生额
				entity.setAfeeam(input.getAfeeam()); //原手续费
				entity.setFeeam1(input.getFeeam1()); //原汇划费
				entity.setChfcnb(input.getChfcnb()); //原对账分类编号
				entity.setServdt(input.getFrondt()); //支付前置日期--渠道日期
				entity.setServsq(input.getFronsq()); //支付前置流水--渠道流水
				entity.setBrchno(input.getBrchno()); //交易机构
				entity.setUserid(input.getUserid()); //录入柜员
				entity.setAuthus(input.getAuthus()); //授权柜员
				entity.setStatus(E_TRANST.STRIKED);
				entity.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq()); // 业务跟踪编号
				entity.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 渠道
				entity.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
				entity.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
				entity.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
				KnlCnapotDao.insert(entity);
				
				BusiTools.getBusiRunEnvs().setRemark("交易已冲正");
			}else{
//				try{			
					IoApReverseIn cplRvIn = SysUtil.getInstance(IoApReverseIn.class);
					cplRvIn.setOtradt(tblKnlCnapot1.getTrandt());
					cplRvIn.setOtrasq(tblKnlCnapot1.getTransq());
					cplRvIn.setCorrfg(ApBaseEnumType.E_CORRFG.ZHENGCH);//不抹账
					cplRvIn.setStacps(BaseEnumType.E_STACPS.ACCOUT);//冲账
					cplRvIn.setVobkfg(ApBaseEnumType.E_VOBKFG.BUHC);//不回冲-
					
//					cplRvIn.setOtsqtp(E_YES___.YES);//是否发起方流水 

					
					bizlog.debug("大小额冲正交易开始==========");
					String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
					
//					if(CommUtil.equals(tblKnlCnapot1.getTrandt(), trandt)){
						bizlog.debug("大小额当日冲正交易开始==========");
//						ApStrike.prcRollback8(cplRvIn);
						bizlog.debug("大小额当日冲正交易结束==========");
//					}else{
//						InknlcnapotDetl inknlCnapot = SysUtil.getInstance(InknlcnapotDetl.class);
//						CommUtil.copyProperties(inknlCnapot, tblKnlCnapot1);
//						
//						bizlog.debug("大小额隔日冲正交易开始==========");
//						SysUtil.getInstance(IoDpStrikeSvcType.class).procCnapreStrikeDieb(inknlCnapot);
//						bizlog.debug("大小额隔日冲正交易结束==========");
//					}
					
					
//				}catch(Exception e){
//					DaoUtil.rollbackTransaction();
//					String errmes = e.getLocalizedMessage();
//					bizlog.error("===输出错误堆栈===");
//					e.printStackTrace();
//					
//					if(CommUtil.isNotNull(errmes)){
//						
//						int index = errmes.indexOf("]");		
//						if(index >= 0){					
//							errmes = errmes.substring(index + 1).replace("]", "").replace("[", "");
//						}
//					}
//					bizlog.debug("<<======大小额冲正失败挂账:[%s]", errmes);
//					SysUtil.getInstance(IoDpStrikeSvcType.class).procCnapreStrikeHold(knlCnapot, errmes);
//				}
				
				//更新大小额往来账明细登记簿
				tblKnlCnapot1.setStatus(E_TRANST.STRIKED);
				KnlCnapotDao.updateOne_odb1(tblKnlCnapot1);
				
				//更新清算明细登记簿
				SysUtil.getInstance(IoDpStrikeSvcType.class)
					.ProcStrAcqsCler(tblKnlCnapot1.getTransq(), tblKnlCnapot1.getTrandt());
				
				
				//输出赋值
				output.setHostdt(CommTools.getBaseRunEnvs().getTrxn_date());
				output.setHostsq(CommTools.getBaseRunEnvs().getTrxn_seq());				
				output.setRetrsq(tblKnlCnapot1.getTransq());
				output.setRetrdt(tblKnlCnapot1.getTrandt());
				output.setRecldt(tblKnlCnapot1.getClerdt());
				output.setReclod(tblKnlCnapot1.getClenum());
			}
			
			//输出
			output.setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch()); 
			
	}

	
	//交易前检查
	private static void preTranCheck(final Input input){
		
		if(CommUtil.isNull(input.getSubsys())){
			throw DpModuleError.DpstComm.E9027("原交易渠道");
		}
		
		if(CommUtil.isNull(input.getMsetdt())){
			throw DpModuleError.DpstComm.E9027("原委托日期");
		}
		
		if(CommUtil.isNull(input.getMsetsq())){
			throw DpModuleError.DpstComm.E9027("原交易序号");
		}
		
		if(CommUtil.isNull(input.getCrdbtg())){
			throw DpModuleError.DpstComm.E9027("原借贷标志");
		}
		
		if(CommUtil.isNull(input.getIotype())){
			throw DpModuleError.DpstComm.E9027("原往来标志");
		}
		
		if(CommUtil.isNull(input.getCstrfg())){
			throw DpModuleError.DpstComm.E9027("原现转标志");
		}
		
		if(CommUtil.isNull(input.getCsextg())){
			throw DpModuleError.DpstComm.E9027("原钞汇属性");
		}
		
		if(CommUtil.isNull(input.getPyercd())){
			throw DpModuleError.DpstComm.E9027("原发起行行号");
		}
		
		if(CommUtil.isNull(input.getPyeecd())){
			throw DpModuleError.DpstComm.E9027("原接收行行号");
		}
		
		if(CommUtil.isNull(input.getPyerac())){
			throw DpModuleError.DpstComm.E9027("原付款人账号");
		}
		
		if(CommUtil.isNull(input.getPyerna())){
			throw DpModuleError.DpstComm.E9027("原付款人名称");
		}
		
		if(CommUtil.isNull(input.getPyeeac())){
			throw DpModuleError.DpstComm.E9027("原收款人账号");
		}
		
		if(CommUtil.isNull(input.getPyeena())){
			throw DpModuleError.DpstComm.E9027("原收款人名称");
		}
		
		if(CommUtil.isNull(input.getPriotp())){
			throw DpModuleError.DpstComm.E9027("原加急标志");
		}
		
//		if(CommUtil.isNull(input.getAfeetg())){
//			throw DpModuleError.DpstComm.E9027("原收费标志");
//		}
		
		if(CommUtil.isNull(input.getTranam())){
			throw DpModuleError.DpstComm.E9027("原发生额 ");
		}
		
		if(CommUtil.isNull(input.getAfeeam())){
			throw DpModuleError.DpstComm.E9027("原手续费 ");
		}
		
		if(CommUtil.isNull(input.getFeeam1())){
			throw DpModuleError.DpstComm.E9027("原汇划费 ");
		}
		
		if(CommUtil.isNull(input.getChfcnb())){
			//throw DpModuleError.DpstComm.E9027("原对账分类编号");
		}
		
		if(CommUtil.isNull(input.getFrondt())){
			throw DpModuleError.DpstComm.E9027("支付前置日期");
		}
		
		if(CommUtil.isNull(input.getFronsq())){
			throw DpModuleError.DpstComm.E9027("支付前置流水号");
		}
		
		if(CommUtil.isNull(input.getBrchno())){
			throw DpModuleError.DpstComm.E9027("交易机构号");
		}
		
		if(CommUtil.isNull(input.getUserid())){
			throw DpModuleError.DpstComm.E9027("录入柜员");
		}
		
//		if(CommUtil.isNull(input.getAuthus())){
//			throw DpModuleError.DpstComm.E9027("授权柜员");
//		}
		
	}
	
	
}
