package cn.sunline.ltts.busi.dp.froz;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.namedsql.DpFrozDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDedu;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDepr;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDeprOwne;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbDeprOwneDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbUnfr;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbUnfrDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoQueryDpSpecForDedu;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoQueryDpSpecForDepr;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType.IoQueryDpSpecForFroz;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpSpecInfoListForDeduIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpSpecInfoListForDeduOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpSpecInfoListForDeprIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpSpecInfoListForDeprOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpSpecInfoListForFrozIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpSpecInfoListForFrozOt;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.DpSpecInfoListIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoCustacDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoCustacInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpFrozIf;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpFrozQrIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpSpecInfo;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEPRBP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEPRTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPRECY;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_LAWCOP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUREAS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_OTSPTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SPECST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SPECTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SPTYPE;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_STOPMS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_STTMCT;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

public class DpFrozQuery {
	
	/**
	 * @author douwenbo
	 * @date 2016-05-03 19:49
	 * 查询冻解冻/解止付信息前的基本检查
	 * @param specInfoListForFrozIn
	 */
	
	public static void queryDpSpecForFrozCheck(DpSpecInfoListForFrozIn specInfoListForFrozIn){
		if(CommUtil.isNull(specInfoListForFrozIn.getFrozno())){
			throw DpModuleError.DpstComm.BNAS0834();
		}
		
		if(specInfoListForFrozIn.getFrozsq() > 0 && specInfoListForFrozIn.getUnfrsq() > 0){
			throw DpModuleError.DpstComm.BNAS0822();
		}
		
		if(specInfoListForFrozIn.getFrozsq() == 0 && specInfoListForFrozIn.getUnfrsq()== 0){
			throw DpModuleError.DpstComm.BNAS0823();
		}
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-05-03 21:16
	 * 查询冻结登记簿、冻结明细登记簿、解冻登记簿信息
	 * @param specInfoListForFrozIn
	 * @param output
	 */
	public static void queryDpSpecForFrozInfo(DpSpecInfoListForFrozIn specInfoListForFrozIn,IoQueryDpSpecForFroz.Output output){
		
		String frozno = specInfoListForFrozIn.getFrozno();//冻结编号
		long frozsq = specInfoListForFrozIn.getFrozsq();//冻结序号
		long unfrsq = specInfoListForFrozIn.getUnfrsq();//解冻序号
		
		DpSpecInfoListForFrozOt cplFrozInfo = SysUtil.getInstance(DpSpecInfoListForFrozOt.class);
		
		//判断是否为冻结或止付查询
		if(CommUtil.isNotNull(frozno) && frozsq > 0){
			//判断为冻结查询
			if(CommUtil.equals("21", frozno.substring(0, 2))){
				
				cplFrozInfo = DpFrozDao.selFrozDetail(frozno, frozsq, true);
				output.setSpecInfoListForFrozOt(cplFrozInfo);
			
		    //判断为止付查询
			}else if(CommUtil.equals("22", frozno.substring(0, 2))){
				
				cplFrozInfo = DpFrozDao.selStopDetail(frozno, frozsq, true);
				//银行止付
				if(cplFrozInfo.getFroztp() == E_FROZTP.BANKSTOPAY){
					cplFrozInfo.setSptype(E_SPTYPE.BANKSTOPAY);
				}
				
				//外部止付
				if(cplFrozInfo.getFroztp() == E_FROZTP.EXTSTOPAY){
					cplFrozInfo.setSptype(E_SPTYPE.EXTSTOPAY);
					
					if(E_STTMCT.HOUR == cplFrozInfo.getSttmct()){
						cplFrozInfo.setFreddt(null);
					}
				}
				
				//存款证明止付
				if(cplFrozInfo.getFroztp() == E_FROZTP.DEPRSTOPAY){
					cplFrozInfo.setSptype(E_SPTYPE.BANKSTOPAY);
					
					//存款证明止付机构
//					if(CommUtil.isNotNull(cplFrozInfo.getTranbr())){
//						IoBrchInfoList brchInfo = DpFrozDao.selBrchInfoByBrchno(cplFrozInfo.getTranbr(), true);
//						cplFrozInfo.setFrogna(brchInfo.getBrchna());
//					}
//					
//					//存款证明解止机构
//					if(CommUtil.isNotNull(cplFrozInfo.getBrchno())){
//						IoBrchInfoList brchInfo = DpFrozDao.selBrchInfoByBrchno(cplFrozInfo.getBrchno(), true);
//						cplFrozInfo.setUfogna(brchInfo.getBrchna());
//					}
					//IoPbKubBrch brchInfo = SysUtil.getInstance(IoPbTableSvr.class).kub_brch_selectOne_odb1(cplFrozInfo.getBrchno(), true);
					
				}
				
				output.setSpecInfoListForFrozOt(cplFrozInfo);
				
			}else if(CommUtil.equals("24", frozno.substring(0, 2))){
				cplFrozInfo = DpFrozDao.selCustDetailInfo(frozno, frozsq, true);
				E_CUREAS frreas = CommUtil.toEnum(E_CUREAS.class, cplFrozInfo.getFrreas());
				cplFrozInfo.setCureas(frreas);
				output.setSpecInfoListForFrozOt(cplFrozInfo);
			}else{
				throw DpModuleError.DpstComm.BNAS1610();
			}
		}
		
		//判断为解冻或解止查询
		if(CommUtil.isNotNull(frozno) && unfrsq > 0){
			//判断为解冻查询
			if(CommUtil.equals("21", frozno.substring(0, 2))){
				cplFrozInfo = DpFrozDao.selUnfrozDetail(frozno, unfrsq, true);
				output.setSpecInfoListForFrozOt(cplFrozInfo);
			
			//判断为解止
			}else if(CommUtil.equals("22", frozno.substring(0, 2))){
				
				cplFrozInfo = DpFrozDao.selUnstopDetail(frozno, unfrsq, true);
				output.setSpecInfoListForFrozOt(cplFrozInfo);
				
			}else if(CommUtil.equals("24", frozno.substring(0, 2))){
				//对于账户解除保护查询交易前台不需要展示，暂不做开发处理
			}
			
		}	
//			
//		}else if(unfrsq>0){
//			//查询解冻登记簿
//			 tblKnbUnfr2 = KnbUnfrDao.selectOne_odb2(frozno, unfrsq, frozsq, false);
//			if(CommUtil.isNull(tblKnbUnfr2)){
//				throw DpModuleError.DpstComm.E9999("根据冻结编号、冻结序号查询解冻登记簿，无对应记录");
//			}
//		}
//		
//		//查询冻结明细登记簿
//		KnbFroz_detl tblKnbFrozDetl2 = KnbFroz_detlDao.selectOne_odb2(frozno, frozsq, false);
//		if(CommUtil.isNull(tblKnbFrozDetl2)){
//			throw DpModuleError.DpstComm.E9999("根据冻结编号、冻结序号查询冻结明细登记簿无对应记录");
//		}
//		
//		E_OTSPTP otsptp = null;//输出特殊业务类型
//		if((tblKnbFroz8.getFroztp() == E_FROZTP.JUDICIAL || tblKnbFroz8.getFroztp() == E_FROZTP.ADD)
//				&& tblKnbFroz8.getFrlmtp() == E_FRLMTP.AMOUNT){
//			if(tblKnbFroz8.getFrozst() == E_FROZST.VALID){
//				otsptp = E_OTSPTP.PCFROZ;//部冻  
//				cplFrozInfo.setFrozam(tblKnbUnfr2.getUnfram());//解冻金额
//			}
//			if(tblKnbFroz8.getFrozst() == E_FROZST.INVALID){
//				otsptp = E_OTSPTP.UNFROZ;//解冻
//				cplFrozInfo.setFrozam(tblKnbFrozDetl2.getFrozam());//冻结金额
//			}
//			cplFrozInfo.setFrozbl(tblKnbFrozDetl2.getFrozbl());//冻结余额
//		}
//		if((tblKnbFroz8.getFroztp() == E_FROZTP.JUDICIAL || tblKnbFroz8.getFroztp() == E_FROZTP.JUDICIAL)
//				&& tblKnbFroz8.getFrlmtp() == E_FRLMTP.ALL){
//			otsptp = E_OTSPTP.PCFROZ;//双冻
//			cplFrozInfo.setFrozam(tblKnbFrozDetl2.getFrozam());//冻结金额
//		}
//		if((tblKnbFroz8.getFroztp() == E_FROZTP.JUDICIAL || tblKnbFroz8.getFroztp() == E_FROZTP.JUDICIAL)
//				&& tblKnbFroz8.getFrlmtp() == E_FRLMTP.OUT){
//			otsptp = E_OTSPTP.PCFROZ;//借冻
//		}
//		if((tblKnbFroz8.getFroztp() == E_FROZTP.EXTSTOPAY || tblKnbFroz8.getFroztp() == E_FROZTP.BANKSTOPAY || tblKnbFroz8.getFroztp() == E_FROZTP.CUSTSTOPAY)){
//			if(tblKnbFroz8.getFrlmtp() == E_FRLMTP.ALL){
//				otsptp = E_OTSPTP.ALLSTOP;//全止
//			}
//			if(tblKnbFroz8.getFrlmtp() == E_FRLMTP.AMOUNT){
//				otsptp = E_OTSPTP.PORSTO;//部止
//			}
//		}
//		
//		//根据电子账号查找户名
//		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
//		IoCaKnaCust tblKnaCust = caqry.kna_cust_selectOneWithLock_odb1(tblKnbFroz8.getCustac(), true);
//		
//		cplFrozInfo.setOtsptp(otsptp);//输出特殊业务类型
//		cplFrozInfo.setCustna(tblKnaCust.getCustna());//客户名称
//		
//		output.setSpecInfoListForFrozOt(cplFrozInfo);
		
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-05-04 16:44
	 * 查询扣划信息前的基本检查
	 * @param specInfoListForDeduIn
	 */
	public static void queryDpSpecForDeduCheck(DpSpecInfoListForDeduIn specInfoListForDeduIn){
		
		if(CommUtil.isNull(specInfoListForDeduIn.getDeduno())){
			throw DpModuleError.DpstComm.BNAS0510();
		}
	}
	
	
	/**
	 * @author douwenbo
	 * @date 2016-05-04 19:27
	 * 查询扣划登记簿信息
	 * @param specInfoListForDeduIn
	 * @param output
	 */
	public static void queryDpSpecForDeduInfo(DpSpecInfoListForDeduIn specInfoListForDeduIn,IoQueryDpSpecForDedu.Output output){
		
		String deduno = specInfoListForDeduIn.getDeduno();//冻结编号
		
		KnbDedu knbDedu = DpFrozDao.selDeduInfoByDeduno(deduno, false);
		
		if(CommUtil.isNull(knbDedu)){
			throw DpModuleError.DpstComm.BNAS0509();
		}
		
		//根据电子账号查找户名
		IoCustacDetl custacDl = DpFrozDao.selCustacDetl(knbDedu.getCustac(), true);
		
		//根据电子账号Id查询电子账号
		String cardno = DpFrozDao.selKnaAcdcInfo(custacDl.getCustac(), true);
		
		DpSpecInfoListForDeduOt cplDeduInfo = SysUtil.getInstance(DpSpecInfoListForDeduOt.class);
		CommUtil.copyProperties(cplDeduInfo,knbDedu);
		cplDeduInfo.setSpectp(E_SPECTP.DEDUCT);
		cplDeduInfo.setCustna(custacDl.getCustna());
		cplDeduInfo.setCardno(cardno);
		cplDeduInfo.setFrozdt(knbDedu.getTrandt());
		output.setSpecInfoListForDeduOt(cplDeduInfo);
		
	}
	
	
	/**
	 * @author douwenbo
	 * @date 2016-05-05 16:18
	 * 查询存款证明信息前的基本检查
	 * @param specInfoListForDpurIn
	 */
	public static void queryDpSpecForDeprCheck(DpSpecInfoListForDeprIn specInfoListForDeprIn){
		
		if(CommUtil.isNull(specInfoListForDeprIn.getDeprbp())){
			throw DpModuleError.DpstComm.BNAS1031();
		}
		
		if(CommUtil.isNull(specInfoListForDeprIn.getDeprnm())){
			throw DpModuleError.DpstComm.BNAS1030();
		}
		
		if(CommUtil.isNull(specInfoListForDeprIn.getDeprtp())){
			throw DpModuleError.DpstComm.BNAS1028();
		}
		
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-05-05 17:04
	 * 查询存款证明主体登记簿信息
	 * @param specInfoListForDeprIn
	 * @param output
	 */
	public static void queryDpSpecForDeprInfo(DpSpecInfoListForDeprIn specInfoListForDeprIn,IoQueryDpSpecForDepr.Output output){
		
		//KnbDepr tblKnbDepr = KnbDeprDao.selectFirst_odb2(specInfoListForDeprIn.getDeprnm(), specInfoListForDeprIn.getDeprtp(), E_DEPRBP.OP, false);
		KnbDepr tblKnbDepr = DpFrozDao.selDeprInfo(specInfoListForDeprIn.getDeprnm(), specInfoListForDeprIn.getDeprtp(), E_DEPRBP.OP, false);
		if(CommUtil.isNull(tblKnbDepr)){
			throw DpModuleError.DpstComm.BNAS1196();
		}
		
//		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		
//		CommTools.getBaseRunEnvs().setBusi_org_id(tblKnbDepr.getCorpno());
		
		String cardno = DpFrozDao.selKnaAcdcInfo(tblKnbDepr.getCustac(),true);
		
		KnbDeprOwne tblKnbDeprOwne = KnbDeprOwneDao.selectOne_odb2(specInfoListForDeprIn.getDeprnm(), false);
		if(CommUtil.isNull(tblKnbDeprOwne)){
			throw DpModuleError.DpstComm.BNAS0757();
		}
		
		DpSpecInfoListForDeprOt cplDeprInfo = SysUtil.getInstance(DpSpecInfoListForDeprOt.class);
		CommUtil.copyProperties(cplDeprInfo,tblKnbDeprOwne);
		
		cplDeprInfo.setCardno(cardno);//电子账号
		cplDeprInfo.setCustna(tblKnbDepr.getCustna());//客户名称
		cplDeprInfo.setBegndt(tblKnbDepr.getBegndt());//开立日期
		cplDeprInfo.setEnddat(tblKnbDepr.getEnddat());//到期日期
		cplDeprInfo.setCrcycd(tblKnbDepr.getCrcycd());//币种
		cplDeprInfo.setDepram(tblKnbDepr.getDepram());//证明金额
		cplDeprInfo.setTranbr(tblKnbDepr.getTranbr());//经办机构
		cplDeprInfo.setTranus(tblKnbDepr.getTranus());//经办柜员
		cplDeprInfo.setOpna01(tblKnbDepr.getOpna01());//经办人姓名
		cplDeprInfo.setOptp01(tblKnbDepr.getOptp01());//经办人证件种类
		cplDeprInfo.setOpno01(tblKnbDepr.getOpno01());//经办人证件号码
		
		if(tblKnbDeprOwne.getIsrecy() == E_DPRECY.Y){//已解除，已回收
			KnbFroz tblknbfroz = KnbFrozDao.selectOne_odb17(tblKnbDepr.getCustac(), tblKnbDepr.getMntrsq(), false);
			if(CommUtil.isNull(tblknbfroz)){
				//小于当天的时点证明没有止付记录，不需要报错    lull
				if ( tblKnbDepr.getDeprtp() == E_DEPRTP.TP
					&& CommUtil.compare(tblKnbDepr.getBegndt(),tblKnbDepr.getTrandt()) < 0) {
				} else {
					throw DpModuleError.DpstComm.BNAS0755();
				}
				
			}else {
				//存款证明止付记录解止时只登记一条
				KnbUnfr tblknbunfr = KnbUnfrDao.selectFirst_odb3(tblknbfroz.getFrozno(), tblknbfroz.getFrozsq(), false);
				if(CommUtil.isNull(tblknbunfr)){
					if ( tblKnbDepr.getDeprtp() == E_DEPRTP.TP
							&& CommUtil.compare(tblKnbDepr.getBegndt(),tblKnbDepr.getTrandt()) < 0) {
						} else {
							throw DpModuleError.DpstComm.BNAS0758();
						}
				}else {
					cplDeprInfo.setUndobr(tblknbunfr.getTranus());//撤销机构
					cplDeprInfo.setUndois(tblknbunfr.getTranbr());//撤销柜员
				}
			}
			
			cplDeprInfo.setUndodt(tblKnbDeprOwne.getUnfrdt());//撤销日期
			cplDeprInfo.setUndonb(tblKnbDeprOwne.getRetnum());//回收份数
			cplDeprInfo.setOpna01(tblKnbDepr.getOpna01());//经办人姓名
			cplDeprInfo.setOptp01(tblKnbDepr.getOptp01());//经办人证件种类
			cplDeprInfo.setOpno01(tblKnbDepr.getOpno01());//经办人证件号码
			
			
		}else{
			cplDeprInfo.setUndonb(tblKnbDeprOwne.getRetnum());//回收份数
			cplDeprInfo.setUndobr(tblKnbDeprOwne.getUndobr());//撤销机构
			cplDeprInfo.setUndois(tblKnbDeprOwne.getUndois());//撤销柜员
			
		}
		
//		CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
		
		output.setSpecInfoListForDeprOt(cplDeprInfo);
		
	}
	
	
	/**
	 * @author douwenbo
	 * @date 2016-05-04 09:01
	 * @param cpliodpunfrozin
	 * 获取解冻登记簿中的最大解冻序号
	 * @return unfrsq解冻序号
	 */
	public static long getMaxUnfrsq(DpSpecInfoListForFrozIn specInfoListForFrozIn){
		long unfrsq = 1;
		if(CommUtil.isNull(DpFrozDao.selUnFrMaxseq(specInfoListForFrozIn.getFrozno(), false))){
			return unfrsq;
		}else{
			return DpFrozDao.selUnFrMaxseq(specInfoListForFrozIn.getFrozno(), false);
		}
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-04-28 15:08
	 * 特殊业务查询输入参数的基本检查
	 * @param specInfoListIn 特殊业务查询输入参数
	 */
	public static void specInfoListInCheck(DpSpecInfoListIn specInfoListIn){
		
		//特殊业务类型检查
		if(CommUtil.isNull(specInfoListIn.getSpectp())){
			throw DpModuleError.DpstComm.BNAS1611();
		}
		
		if(E_SPECTP.CUSTOP == specInfoListIn.getSpectp()
				|| E_SPECTP.DPCTCT == specInfoListIn.getSpectp()
				|| E_SPECTP.FREEZE == specInfoListIn.getSpectp()
				|| E_SPECTP.STOPPY == specInfoListIn.getSpectp()){
			
			//特殊业务状态检查
			if(CommUtil.isNull(specInfoListIn.getSpecst())){
				throw DpModuleError.DpstComm.BNAS1612();
			}
		}
		
		//特殊业务编号、电子账号、交易起始日期、止付措施、部门类型最少输入一项
		if (CommUtil.isNull(specInfoListIn.getSpecno())&&CommUtil.isNull(specInfoListIn.getCardno())
				&& CommUtil.isNull(specInfoListIn.getBgindt())&&CommUtil.isNull(specInfoListIn.getStopms())
				&&CommUtil.isNull(specInfoListIn.getFrexog())) {
			
			throw DpModuleError.DpstComm.BNAS1613();
		}
		
		//查询省中心机构
//		IoBrchInfo brchInfo = SysUtil.getInstance(IoBrchInfo.class);
//		brchInfo = SysUtil.getInstance(IoBrchSvcType.class).getGenClerbr();
		
		//电子账号规则检查
		if(CommUtil.isNotNull(specInfoListIn.getCardno())){
//			//判断是否为省中心机构
//			if(CommUtil.equals(brchInfo.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch())){
//				if(!CommUtil.equals(brchInfo.getCorpno(), CommTools.getBaseRunEnvs().getBusi_org_id())){
//					throw DpModuleError.DpstComm.E9999("");
//				}
//			}
			if(specInfoListIn.getSpectp() != E_SPECTP.CUSTOP){
				IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
				
				IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(specInfoListIn.getCardno(), false);
				
				if(CommUtil.isNull(caKnaAcdc)){
					throw DpModuleError.DpstComm.BNAS0753();
				}
			
			}else{
				//根据电子账号查询电子账号ID
				IoCustacInfo custacInfo = DpFrozDao.selCustacInfoByCardno(specInfoListIn.getCardno(), false);
				if(CommUtil.isNull(custacInfo)){
					throw CaError.Eacct.BNAS1279();
				}
			}
		}
		
		// 日期格式检查
		if (CommUtil.isNotNull(specInfoListIn.getBgindt()) && !DateTools2.chkIsDate(specInfoListIn.getBgindt())) {
			throw DpModuleError.DpstComm.BNAS1614();
		}
		
		if (CommUtil.isNotNull(specInfoListIn.getEndddt()) && !DateTools2.chkIsDate(specInfoListIn.getEndddt())) {
			throw DpModuleError.DpstComm.BNAS1615();
		}
		
		if(CommUtil.isNotNull(specInfoListIn.getBgindt())){
			
			if(CommUtil.isNull(specInfoListIn.getEndddt())){
				throw DpModuleError.DpstComm.BNAS1616();
			}
			
		}
		if(CommUtil.compare(specInfoListIn.getBgindt(), CommTools.getBaseRunEnvs().getTrxn_date()) > 0){
			throw DpModuleError.DpstComm.BNAS1617();
		}
		
		if(CommUtil.compare(specInfoListIn.getEndddt(), CommTools.getBaseRunEnvs().getTrxn_date()) > 0){
			throw DpModuleError.DpstComm.BNAS1618();
		}
		
		if(CommUtil.compare(specInfoListIn.getBgindt(), specInfoListIn.getEndddt()) > 0){
			throw DpModuleError.DpstComm.BNAS1619();
		}
		
		//如果特殊业务编号不为空,则检查有无记录  账户保护需要跨法人查询，不做检查
//		if(specInfoListIn.getSpectp() != E_SPECTP.CUSTOP){
//			if(CommUtil.isNotNull(specInfoListIn.getSpecno())){
//				
//				List<KnbFroz> tblKnbFroz = KnbFrozDao.selectAll_odb4(specInfoListIn.getSpecno(), false);
//				
//				if(CommUtil.isNull(tblKnbFroz)){
//					
//					KnbDedu tblKnbDedu = KnbDeduDao.selectOne_odb1(specInfoListIn.getSpecno(), false);
//					
//					if(CommUtil.isNull(tblKnbDedu)){
//						
//						KnbDepr tblKnbDepr = KnbDeprDao.selectFirst_odb3(specInfoListIn.getSpecno(), false);
//						
//						if(CommUtil.isNull(tblKnbDepr)){
//							throw DpModuleError.DpstComm.E9999("没有符合条件的记录");
//						}
//					}
//				}
//			}
//		}
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-04-28 15:56
	 * 特殊业务类型为 1冻结/解冻时的查询
	 * @param specInfoListIn
	 * @param cplDpSpecInfoList
	 * @return cplDpSpecInfoList
	 */
	public static Page<IoDpSpecInfo> querySpecInfoListForFreeze(DpSpecInfoListIn specInfoListIn, long totlCount, long startno,long pgsize){
		
		String custac = null;//电子账号
		String tranbr = specInfoListIn.getTranbr();//交易机构
		String specno = specInfoListIn.getSpecno();//特殊业务编号
		E_SPECST specst = specInfoListIn.getSpecst();//特殊业务状态
		String bgindt = specInfoListIn.getBgindt();//交易起始日期
		String endddt = specInfoListIn.getEndddt();//交易终止日期
		E_LAWCOP frexog = specInfoListIn.getFrexog();//部门类型
		Page<IoDpSpecInfo> specInfoList = null;
		String corpno = null; //法人代码
		
		
		if(CommUtil.isNotNull(specInfoListIn.getCardno())){
			
			IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			
			IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(specInfoListIn.getCardno(), false);
			
			if(CommUtil.isNull(caKnaAcdc)){
				throw DpModuleError.DpstComm.BNAS0692();
			}
			
//			if(caKnaAcdc.getStatus() != E_DPACST.NORMAL){
//				throw DpModuleError.DpstComm.E9999("根据电子账号获取电子账号ID状态不正常");
//			}
			
			custac = caKnaAcdc.getCustac();
		}
		
		//机构号为空则根据行社查询满足条件的记录
		if(CommUtil.isNull(tranbr)){
			corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		
		//若不为空则需判断输入机构是否为当前登陆行社下所属机构
		}else{
			/*if(tranbr.length() != 6){
				throw DpModuleError.DpstComm.BNAS1620();
			}*/
			String cur_corpno = tranbr.substring(0,3); 
			
			//判断当前登录机构是否为省中心
			if(!CommUtil.equals("999", CommTools.getBaseRunEnvs().getBusi_org_id())){
				if(!CommUtil.equals(cur_corpno, CommTools.getBaseRunEnvs().getBusi_org_id())){
					throw DpModuleError.DpstComm.BNAS1621();
				}
			}
		}
		
		//当前登录机构非省中心机构
		if(!CommUtil.equals("999", CommTools.getBaseRunEnvs().getBusi_org_id())){
			if(specst == E_SPECST.NOREMO){ //未解除

				specInfoList = DpFrozDao.selFrozSpecInfoPage(tranbr, specno, custac, bgindt, endddt, frexog, corpno, 
						startno, pgsize, totlCount, false);
				if(CommUtil.isNull(specInfoList)){
					throw DpModuleError.DpstComm.BNAS0729();	
				}
				for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
					//设置输出业务类型
					if(specInfo.getFroztp() == E_FROZTP.JUDICIAL || specInfo.getFroztp() == E_FROZTP.ADD){
						if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
							specInfo.setOtsptp(E_OTSPTP.PCFROZ);//部冻
						}
						if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
							specInfo.setOtsptp(E_OTSPTP.CMFROZ);//借冻
						}
						if(specInfo.getFrlmtp() == E_FRLMTP.ALL){
							specInfo.setOtsptp(E_OTSPTP.ALLFROZ);//双冻
						}
						specInfo.setSpecst(specst);
					}else{
						specInfo.setOtsptp(E_OTSPTP.UNFROZ); //解冻
					}
				}
			}
			
			if(specst == E_SPECST.OKREMO){ //已解除
				
				specInfoList = DpFrozDao.selUnfrSpecInfoPage(tranbr, specno ,custac, bgindt, endddt, frexog, corpno, 
						startno, pgsize, totlCount, false); 
				if(CommUtil.isNull(specInfoList)){
					throw DpModuleError.DpstComm.BNAS0729();	
				}	
				for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
					//设置输出业务类型
					if(specInfo.getFroztp() == E_FROZTP.JUDICIAL || specInfo.getFroztp() == E_FROZTP.ADD){
						if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
							specInfo.setOtsptp(E_OTSPTP.PCFROZ);//部冻
						}
						if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
							specInfo.setOtsptp(E_OTSPTP.CMFROZ);//借冻
						}
						if(specInfo.getFrlmtp() == E_FRLMTP.ALL){
							specInfo.setOtsptp(E_OTSPTP.ALLFROZ);//双冻
						}
							
					}else{
						specInfo.setOtsptp(E_OTSPTP.UNFROZ); //解冻
					}
					specInfo.setSpecst(specst);
				}
			}
			
			if(specst == E_SPECST.ALL){//全部
				//当前已输入机构
				if(CommUtil.isNotNull(tranbr)){
					specInfoList = DpFrozDao.selAllFrozSpecInfo(tranbr, specno, custac, bgindt, endddt, frexog, 
							startno, pgsize, totlCount, false);
					if(CommUtil.isNull(specInfoList)){
						throw DpModuleError.DpstComm.BNAS0729();	
					}
					for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
						//设置输出业务类型
						if(specInfo.getFroztp() == E_FROZTP.JUDICIAL || specInfo.getFroztp() == E_FROZTP.ADD){
							if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
								specInfo.setOtsptp(E_OTSPTP.PCFROZ);//部冻
							}
							if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
								specInfo.setOtsptp(E_OTSPTP.CMFROZ);//借冻
							}
							if(specInfo.getFrlmtp() == E_FRLMTP.ALL){
								specInfo.setOtsptp(E_OTSPTP.ALLFROZ);//双冻
							}
						}else{
							//查询当前解冻记录对应的冻结信息
							DpSpecInfoListForFrozOt info = DpFrozDao.selFrozDetail(specInfo.getSpecno(), specInfo.getOdfrsq(), false);
							if(CommUtil.isNull(info)){
								throw DpModuleError.DpstComm.BNAS0729();	
							}
							//设置当前冻结记录法人
//							String cur_corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
							
//							CommTools.getBaseRunEnvs().setBusi_org_id(info.getCorpno());
							
							List<KnbFroz> listInfo = KnbFrozDao.selectAll_odb4(info.getFrozno(), false);							
							if(CommUtil.isNull(listInfo)){
								throw DpModuleError.DpstComm.BNAS0197();	
							}
							long frozsq = DpFrozDao.selFrozMaxseq(info.getFrozno(), true);
							
							//判断当前冻结记录是否存在续冻
							if(listInfo.size() > 1 
									&& CommUtil.compare(frozsq, specInfo.getOdfrsq()) == 0){
								specInfo.setSpecst(E_SPECST.UNFROZ);//续冻联动解冻
							}
							
//							CommTools.getBaseRunEnvs().setBusi_org_id(cur_corpno);
							
							specInfo.setOtsptp(E_OTSPTP.UNFROZ); //解冻
						}
						
						if(specInfo.getFrozst() == E_FROZST.INVALID){
							specInfo.setSpecst(E_SPECST.OKREMO);
						
						}else if(specInfo.getFrozst() == E_FROZST.VALID){
							specInfo.setSpecst(E_SPECST.NOREMO);
						}
					}
				
				//当前未输入机构
				}else{
					//若未输入机构则模糊按法人查询
					tranbr = CommTools.getBaseRunEnvs().getBusi_org_id();
					specInfoList = DpFrozDao.selAllFrozInfoByPage(tranbr, specno, custac, bgindt, endddt, frexog, 
							startno, pgsize, totlCount, false);
				if(CommUtil.isNull(specInfoList)){
					throw DpModuleError.DpstComm.BNAS0729();	
				}					
					for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
						//设置输出业务类型
						if(specInfo.getFroztp() == E_FROZTP.JUDICIAL || specInfo.getFroztp() == E_FROZTP.ADD){
								if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
									specInfo.setOtsptp(E_OTSPTP.PCFROZ);//部冻
								}
								if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
									specInfo.setOtsptp(E_OTSPTP.CMFROZ);//借冻
								}
								if(specInfo.getFrlmtp() == E_FRLMTP.ALL){
									specInfo.setOtsptp(E_OTSPTP.ALLFROZ);//双冻
								}
						}else{
							
							//查询当前解冻记录对应的冻结信息
							DpSpecInfoListForFrozOt info = DpFrozDao.selFrozDetail(specInfo.getSpecno(), specInfo.getOdfrsq(), false);
							if(CommUtil.isNull(info)){
								throw DpModuleError.DpstComm.BNAS0729();	
							}
							//设置当前冻结记录法人
//							String cur_corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
							
//							CommTools.getBaseRunEnvs().setBusi_org_id(info.getCorpno());
							
							List<KnbFroz> listInfo = KnbFrozDao.selectAll_odb4(info.getFrozno(), false);
							if(CommUtil.isNull(listInfo)){
								throw DpModuleError.DpstComm.BNAS0197();	
							}
							long frozsq = DpFrozDao.selFrozMaxseq(info.getFrozno(), true);
							
							//判断当前冻结记录是否存在续冻
							if(listInfo.size() > 1 
									&& CommUtil.compare(frozsq, specInfo.getOdfrsq()) == 0){
								specInfo.setSpecst(E_SPECST.UNFROZ);//续冻联动解冻
							}
							
//							CommTools.getBaseRunEnvs().setBusi_org_id(cur_corpno);
						}
						
						if(specInfo.getFrozst() == E_FROZST.INVALID){
							specInfo.setSpecst(E_SPECST.OKREMO);
						
						}else if(specInfo.getFrozst() == E_FROZST.VALID){
							specInfo.setSpecst(E_SPECST.NOREMO);
						}
					}				
				}
			}
		
		//当前登录机构为省中心机构
		}else{
			corpno = null;
			
			if(specst == E_SPECST.NOREMO){ //未解除

				specInfoList = DpFrozDao.selFrozSpecInfoPage(tranbr, specno, custac, bgindt, endddt, frexog, corpno, 
						startno, pgsize, totlCount, false);
				if(CommUtil.isNull(specInfoList)){
					throw DpModuleError.DpstComm.BNAS0729();	
			}
				for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
					//设置输出业务类型
					if(specInfo.getFroztp() == E_FROZTP.JUDICIAL || specInfo.getFroztp() == E_FROZTP.ADD){
						if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
							specInfo.setOtsptp(E_OTSPTP.PCFROZ);//部冻
						}
						if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
							specInfo.setOtsptp(E_OTSPTP.CMFROZ);//借冻
						}
						if(specInfo.getFrlmtp() == E_FRLMTP.ALL){
							specInfo.setOtsptp(E_OTSPTP.ALLFROZ);//双冻
						}
						specInfo.setSpecst(specst);
					}else{
						specInfo.setOtsptp(E_OTSPTP.UNFROZ); //解冻
					}
				}
			}
			
			if(specst == E_SPECST.OKREMO){ //已解除
				
				specInfoList = DpFrozDao.selUnfrSpecInfoPage(tranbr, specno ,custac, bgindt, endddt, frexog, corpno, 
						startno, pgsize, totlCount, false); 
		     if(CommUtil.isNull(specInfoList)){
			   throw DpModuleError.DpstComm.BNAS0729();	
	            }
				for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
					//设置输出业务类型
					if(specInfo.getFroztp() == E_FROZTP.JUDICIAL || specInfo.getFroztp() == E_FROZTP.ADD){
						if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
							specInfo.setOtsptp(E_OTSPTP.PCFROZ);//部冻
						}
						if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
							specInfo.setOtsptp(E_OTSPTP.CMFROZ);//借冻
						}
						if(specInfo.getFrlmtp() == E_FRLMTP.ALL){
							specInfo.setOtsptp(E_OTSPTP.ALLFROZ);//双冻
						}
							
					}else{
						specInfo.setOtsptp(E_OTSPTP.UNFROZ); //解冻
					}
					specInfo.setSpecst(specst);
				}
			}
			
			if(specst == E_SPECST.ALL){//全部
				
				specInfoList = DpFrozDao.selAllFrozSpecInfo(tranbr, specno, custac, bgindt, endddt, frexog, 
						startno, pgsize, totlCount, false);
				if(CommUtil.isNull(specInfoList)){
			     throw DpModuleError.DpstComm.BNAS0729();	
	            }
				for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
					//设置输出业务类型
					if(specInfo.getFroztp() == E_FROZTP.JUDICIAL || specInfo.getFroztp() == E_FROZTP.ADD){
						if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
							specInfo.setOtsptp(E_OTSPTP.PCFROZ);//部冻
						}
						if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
							specInfo.setOtsptp(E_OTSPTP.CMFROZ);//借冻
						}
						if(specInfo.getFrlmtp() == E_FRLMTP.ALL){
							specInfo.setOtsptp(E_OTSPTP.ALLFROZ);//双冻
						}
					}else{
						
						//查询当前解冻记录对应的冻结信息
						DpSpecInfoListForFrozOt info = DpFrozDao.selFrozDetail(specInfo.getSpecno(), specInfo.getOdfrsq(), false);
						if(CommUtil.isNull(info)){
							throw DpModuleError.DpstComm.BNAS0729();	
						}
						//设置当前冻结记录法人
//						String cur_corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
						
//						CommTools.getBaseRunEnvs().setBusi_org_id(info.getCorpno());
						
						List<KnbFroz> listInfo = KnbFrozDao.selectAll_odb4(info.getFrozno(), false);
						if(CommUtil.isNull(listInfo)){
							throw DpModuleError.DpstComm.BNAS0197();	
						}
						long frozsq = DpFrozDao.selFrozMaxseq(info.getFrozno(), true);
						
						//判断当前冻结记录是否存在续冻
						if(listInfo.size() > 1 
								&& CommUtil.compare(frozsq, specInfo.getOdfrsq()) == 0){
							specInfo.setSpecst(E_SPECST.UNFROZ);//续冻联动解冻
						}
						
//						CommTools.getBaseRunEnvs().setBusi_org_id(cur_corpno);
						
						specInfo.setOtsptp(E_OTSPTP.UNFROZ); //解冻
					}
					
					if(specInfo.getFrozst() == E_FROZST.INVALID){
						specInfo.setSpecst(E_SPECST.OKREMO);
					
					}else if(specInfo.getFrozst() == E_FROZST.VALID){
						specInfo.setSpecst(E_SPECST.NOREMO);
					}
				}
			}
		}		
		return specInfoList;
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-04-28 19:45
	 * 特殊业务类型为 2扣划时的查询
	 * @param specInfoListIn
	 * @param cplDpSpecInfoList
	 * @return cplDpSpecInfoList
	 */
	public static Page<IoDpSpecInfo> querySpecInfoListForDeduct(DpSpecInfoListIn specInfoListIn, long totlCount, long startno,long pgsize){
		
		String tranbr = specInfoListIn.getTranbr();//交易机构
		String specno = specInfoListIn.getSpecno();//特殊业务编号
		String bgindt = specInfoListIn.getBgindt();//交易起始日期
		String endddt = specInfoListIn.getEndddt();//交易终止日期
		String custac = null;//电子账号
		E_LAWCOP frexog = specInfoListIn.getFrexog();//部门类型
		String corpno = null; //法人代码
		
		if(CommUtil.isNotNull(specInfoListIn.getCardno())){
			
			IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			
			IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(specInfoListIn.getCardno(), false);
			
			if(CommUtil.isNull(caKnaAcdc)){
				throw DpModuleError.DpstComm.BNAS1622();
			}
			
//			if(caKnaAcdc.getStatus() != E_DPACST.NORMAL){
//				throw DpModuleError.DpstComm.E9999("根据电子账号获取电子账号ID状态不正常");
//			}
			
			custac = caKnaAcdc.getCustac();
		}
		
		//机构号为空则根据行社查询满足条件的记录
		if(CommUtil.isNull(tranbr)){
			corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
				
		//若不为空则需判断输入机构是否为当前登陆行社下所属机构
		}else {
			/*if(tranbr.length() != 6){
				throw DpModuleError.DpstComm.BNAS1620();
			}*/
			
			//获取当前机构对应的法人
			String cur_corpno = tranbr.substring(0,3); 
			
			//判断当前登录机构是否为省中心
			if(!CommUtil.equals("999", CommTools.getBaseRunEnvs().getBusi_org_id())){
				if(!CommUtil.equals(cur_corpno, CommTools.getBaseRunEnvs().getBusi_org_id())){
					throw DpModuleError.DpstComm.BNAS1621();
				}
			}
		}
		
		Page<IoDpSpecInfo> specInfoList = null;
		
		//当前登录机构不为省中心机构
		if(!CommUtil.equals("999", CommTools.getBaseRunEnvs().getBusi_org_id())){
			
			specInfoList = DpFrozDao.selDpSpecDeduInfo(tranbr, specno, custac, bgindt, endddt, frexog, 
					corpno, startno, pgsize, totlCount, false);
		
		//若为省中心机构,则全查整个行社扣划记录
		}else{
			corpno = null;
			
			specInfoList = DpFrozDao.selDpSpecDeduInfo(tranbr, specno, custac, bgindt, endddt, frexog, 
					corpno, startno, pgsize, totlCount, false);
		}
		if(CommUtil.isNull(specInfoList)){
			throw DpModuleError.DpstComm.BNAS1196();
		}
		return specInfoList;
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-04-28 19:46
	 * 特殊业务类型为 3止付/解止时的查询
	 * @param specInfoListIn
	 * @param cplDpSpecInfoList
	 * @return cplDpSpecInfoList
	 */
	public static Page<IoDpSpecInfo> querySpecInfoListForStoppy(DpSpecInfoListIn specInfoListIn, long totlCount, long startno,long pgsize){
		
		String tranbr = specInfoListIn.getTranbr();//交易机构
		String specno = specInfoListIn.getSpecno();//特殊业务编号
		E_SPECST specst = specInfoListIn.getSpecst();//特殊业务状态
		String bgindt = specInfoListIn.getBgindt();//交易起始日期
		String endddt = specInfoListIn.getEndddt();//交易终止日期
		E_STOPMS stopms = specInfoListIn.getStopms();//止付措施
		E_LAWCOP frexog = specInfoListIn.getFrexog();//部门类型
		String custac = null;//电子账号
		String corpno = null;//法人代码
		
		if(CommUtil.isNotNull(specInfoListIn.getCardno())){
			
			IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			
			IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(specInfoListIn.getCardno(), false);
			
			if(CommUtil.isNull(caKnaAcdc)){
				throw DpModuleError.DpstComm.BNAS1622();
			}
			
//			if(caKnaAcdc.getStatus() != E_DPACST.NORMAL){
//				throw DpModuleError.DpstComm.E9999("根据电子账号获取电子账号ID状态不正常");
//			}
			
			custac = caKnaAcdc.getCustac();
		}
		
		//机构号为空则根据行社查询满足条件的记录
		if(CommUtil.isNull(tranbr)){
			corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
				
		//若不为空则需判断输入机构是否为当前登陆行社下所属机构
		}else{
			/*if(tranbr.length() != 6){
				throw DpModuleError.DpstComm.BNAS1620();
			}*/
			String cur_corpno = tranbr.substring(0,3); 
					
			//判断当前登录机构是否为省中心
			if(!CommUtil.equals("999", CommTools.getBaseRunEnvs().getBusi_org_id())){
				if(!CommUtil.equals(cur_corpno, CommTools.getBaseRunEnvs().getBusi_org_id())){
					throw DpModuleError.DpstComm.BNAS1621();
				}
			}
		}
		
		Page<IoDpSpecInfo> specInfoList = null;
		
		//当前登录机构非省中心机构
		if(!CommUtil.equals("999", CommTools.getBaseRunEnvs().getBusi_org_id())){
			if(specst == E_SPECST.NOREMO){ //未解除
				
				specInfoList = DpFrozDao.selStopSpecInfoPage(tranbr, specno, custac, bgindt , endddt ,stopms, frexog, 
							corpno, startno, pgsize, totlCount, false);
				if(CommUtil.isNull(specInfoList)){
					throw DpModuleError.DpstComm.BNAS0729();	
				}
				for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
					//设置输出业务类型
					if(specInfo.getFroztp() == E_FROZTP.BANKSTOPAY || specInfo.getFroztp() == E_FROZTP.EXTSTOPAY ||specInfo.getFroztp() == E_FROZTP.CUSTSTOPAY){
						if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
							specInfo.setOtsptp(E_OTSPTP.PORSTO);//部止
						}
						if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
							specInfo.setOtsptp(E_OTSPTP.ALLSTOP);//全止
						}
					}else if(E_FROZTP.DEPRSTOPAY == specInfo.getFroztp()){
						specInfo.setOtsptp(E_OTSPTP.PORSTO);//部止
					}
					
					specInfo.setSpecst(E_SPECST.NOREMO);
				}
			}
			
			if(specst == E_SPECST.OKREMO){ //已解除
				specInfoList = DpFrozDao.selNonStopSpecInfo(tranbr, specno, custac, bgindt , endddt ,stopms, frexog,
								corpno, startno, pgsize, totlCount, false); 
				if(CommUtil.isNull(specInfoList)){
					throw DpModuleError.DpstComm.BNAS0729();	
				}
				for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
					//设置输出业务类型
					if(specInfo.getFroztp() == E_FROZTP.BANKSTOPAY || specInfo.getFroztp() == E_FROZTP.EXTSTOPAY || specInfo.getFroztp() == E_FROZTP.CUSTSTOPAY){
						if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
							specInfo.setOtsptp(E_OTSPTP.PORSTO);//部止
						}
						if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
							specInfo.setOtsptp(E_OTSPTP.ALLSTOP);//全止
						}
					}else if(E_FROZTP.DEPRSTOPAY == specInfo.getFroztp()){
						specInfo.setOtsptp(E_OTSPTP.PORSTO);//部止
					}
					
					specInfo.setSpecst(E_SPECST.OKREMO);
				}
			}
			
			if(specst == E_SPECST.ALL){//全部
				
				specInfoList = DpFrozDao.selAllStopSpecInfoPage(tranbr, specno, custac, bgindt , endddt, stopms, frexog, 
							corpno, startno, pgsize, totlCount, false); 
				if(CommUtil.isNull(specInfoList)){
					throw DpModuleError.DpstComm.BNAS0729();	
				}	
				for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
					//设置输出业务类型
					if(specInfo.getFroztp() == E_FROZTP.BANKSTOPAY || specInfo.getFroztp() == E_FROZTP.EXTSTOPAY ||specInfo.getFroztp() == E_FROZTP.CUSTSTOPAY){
						
						if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
							specInfo.setOtsptp(E_OTSPTP.PORSTO);//部止
							
						}
						
						if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
							specInfo.setOtsptp(E_OTSPTP.ALLSTOP);//全止
							
						}
					
					}else if(E_FROZTP.DEPRSTOPAY == specInfo.getFroztp()){
						specInfo.setOtsptp(E_OTSPTP.PORSTO);//部止
					
					}else{
						specInfo.setOtsptp(E_OTSPTP.UNSTOP);//解止
						
					}
					
					if(specInfo.getFrozst() == E_FROZST.INVALID){
						specInfo.setSpecst(E_SPECST.OKREMO);
					}else{
						specInfo.setSpecst(E_SPECST.NOREMO);
					}
				}
			}
		
		//当前登录机构为省中心机构,则全查行社所有符合记录
		}else{
			corpno = null;
			
			if(specst == E_SPECST.NOREMO){ //未解除
				
				specInfoList = DpFrozDao.selStopSpecInfoPage(tranbr, specno, custac, bgindt , endddt ,stopms, frexog, 
							corpno, startno, pgsize, totlCount, false);
				if(CommUtil.isNull(specInfoList)){
					throw DpModuleError.DpstComm.BNAS0729();	
				}
				for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
					//设置输出业务类型
					if(specInfo.getFroztp() == E_FROZTP.BANKSTOPAY || specInfo.getFroztp() == E_FROZTP.EXTSTOPAY ||specInfo.getFroztp() == E_FROZTP.CUSTSTOPAY){
						if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
							specInfo.setOtsptp(E_OTSPTP.PORSTO);//部止
						}
						if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
							specInfo.setOtsptp(E_OTSPTP.ALLSTOP);//全止
						}
					}else if(E_FROZTP.DEPRSTOPAY == specInfo.getFroztp()){
						specInfo.setOtsptp(E_OTSPTP.PORSTO);//部止
					}
					
					specInfo.setSpecst(E_SPECST.NOREMO);
				}
			}
			
			if(specst == E_SPECST.OKREMO){ //已解除
				specInfoList = DpFrozDao.selNonStopSpecInfo(tranbr, specno, custac, bgindt , endddt ,stopms, frexog,
								corpno, startno, pgsize, totlCount, false); 
				if(CommUtil.isNull(specInfoList)){
					throw DpModuleError.DpstComm.BNAS0729();	
				}
				for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
					//设置输出业务类型
					if(specInfo.getFroztp() == E_FROZTP.BANKSTOPAY || specInfo.getFroztp() == E_FROZTP.EXTSTOPAY || specInfo.getFroztp() == E_FROZTP.CUSTSTOPAY){
						if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
							specInfo.setOtsptp(E_OTSPTP.PORSTO);//部止
						}
						if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
							specInfo.setOtsptp(E_OTSPTP.ALLSTOP);//全止
						}
					}else if(E_FROZTP.DEPRSTOPAY == specInfo.getFroztp()){
						specInfo.setOtsptp(E_OTSPTP.PORSTO);//部止
					}
					
					specInfo.setSpecst(E_SPECST.OKREMO);
				}
			}
			
			if(specst == E_SPECST.ALL){//全部
				
				specInfoList = DpFrozDao.selAllStopSpecInfoPage(tranbr, specno, custac, bgindt , endddt, stopms, frexog, 
							corpno, startno, pgsize, totlCount, false); 
				if(CommUtil.isNull(specInfoList)){
					throw DpModuleError.DpstComm.BNAS0729();	
				}
				for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
					//设置输出业务类型
					if(specInfo.getFroztp() == E_FROZTP.BANKSTOPAY || specInfo.getFroztp() == E_FROZTP.EXTSTOPAY ||specInfo.getFroztp() == E_FROZTP.CUSTSTOPAY){
						
						if(specInfo.getFrlmtp() == E_FRLMTP.AMOUNT){
							specInfo.setOtsptp(E_OTSPTP.PORSTO);//部止
							
						}
						
						if(specInfo.getFrlmtp() == E_FRLMTP.OUT){
							specInfo.setOtsptp(E_OTSPTP.ALLSTOP);//全止
							
						}
					
					}else if(E_FROZTP.DEPRSTOPAY == specInfo.getFroztp()){
						specInfo.setOtsptp(E_OTSPTP.PORSTO);//部止
					
					}else{
						specInfo.setOtsptp(E_OTSPTP.UNSTOP);//解止
						
					}
					
					if(specInfo.getFrozst() == E_FROZST.INVALID){
						specInfo.setSpecst(E_SPECST.OKREMO);
						
					}else{
						specInfo.setSpecst(E_SPECST.NOREMO);
					}
				}
			}
		}
		
		return specInfoList;
		
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-04-28 19:47
	 * 特殊业务类型为 4存款证明时的查询
	 * @param specInfoListIn
	 * @param cplDpSpecInfoList
	 * @return cplDpSpecInfoList
	 */
	public static Page<IoDpSpecInfo> querySpecInfoListForDpctct(DpSpecInfoListIn specInfoListIn, long totlCount, long startno,long pgsize){
		
		String tranbr = specInfoListIn.getTranbr();//交易机构
		String specno = specInfoListIn.getSpecno();//特殊业务编号
		String bgindt = specInfoListIn.getBgindt();//交易起始日期
		String endddt = specInfoListIn.getEndddt();//交易终止日期
		E_SPECST specst = specInfoListIn.getSpecst();//特殊业务状态
		String custac = null;
		String corpno = null;
		
		if(CommUtil.isNotNull(specInfoListIn.getCardno())){
			
			IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			
			IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(specInfoListIn.getCardno(), false);
			
			if(CommUtil.isNull(caKnaAcdc)){
				throw DpModuleError.DpstComm.BNAS0692();
			}
			
			custac = caKnaAcdc.getCustac();
		}
		
		//机构号为空则根据行社查询满足条件的记录
		if(CommUtil.isNull(tranbr)){
			corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		
		//若不为空则需判断输入机构是否为当前登陆行社下所属机构
		}else{
			/*if(tranbr.length() != 6){
				throw DpModuleError.DpstComm.BNAS1620();
			}*/
			String cur_corpno = tranbr.substring(0,3); 
			
			//判断当前登录机构是否为省中心
			if(!CommUtil.equals("999", CommTools.getBaseRunEnvs().getBusi_org_id())){
				if(!CommUtil.equals(cur_corpno, CommTools.getBaseRunEnvs().getBusi_org_id())){
					throw DpModuleError.DpstComm.BNAS1621();
				}
			}
		}
		
		Page<IoDpSpecInfo> specInfoList = null;
		if(!CommUtil.equals("999", CommTools.getBaseRunEnvs().getBusi_org_id())){
			
			if(E_SPECST.NOREMO == specst){ //未解除
				
				 specInfoList = DpFrozDao.selDpSpecOpdpprInfo(tranbr, specno, custac, bgindt, endddt,E_FROZST.VALID, 
						corpno,startno, pgsize, totlCount, false);
			
			}else if(E_SPECST.OKREMO == specst){//已解除
				
				specInfoList = DpFrozDao.selDpSpecDeptInfo(tranbr, specno, custac, bgindt, endddt,E_FROZST.INVALID,
						corpno, startno, pgsize, totlCount, false);
						
			
			}else if(E_SPECST.ALL == specst){//全部
				
				specInfoList = DpFrozDao.selAllDpprSpecInfo(tranbr, specno, custac, bgindt, endddt,
						corpno, startno, pgsize, totlCount, false);
						
			}

		}else{
			corpno = null;
			
			if(E_SPECST.NOREMO == specst){ //未解除
				
				 specInfoList = DpFrozDao.selDpSpecOpdpprInfo(tranbr, specno, custac, bgindt, endddt,E_FROZST.VALID, 
						corpno,startno, pgsize, totlCount, false);
			
			}else if(E_SPECST.OKREMO == specst){//已解除
				
				specInfoList = DpFrozDao.selDpSpecDeptInfo(tranbr, specno, custac, bgindt, endddt,E_FROZST.INVALID,
						corpno, startno, pgsize, totlCount, false);
						
			
			}else if(E_SPECST.ALL == specst){//全部
				
				specInfoList = DpFrozDao.selAllDpprSpecInfo(tranbr, specno, custac, bgindt, endddt,
						corpno, startno, pgsize, totlCount, false);
						
			}
		}
		
		return specInfoList;
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-04-28 19:47
	 * 特殊业务类型为 5-账户保护/解除保护
	 * @param specInfoListIn
	 * @param cplDpSpecInfoList
	 * @return cplDpSpecInfoList
	 */
	public static Page<IoDpSpecInfo> querySpecInfoListForCustop(DpSpecInfoListIn specInfoListIn, long totlCount, long startno,long pgsize){
		
		String tranbr = specInfoListIn.getTranbr();//交易机构
		String specno = specInfoListIn.getSpecno();//特殊业务编号
		String bgindt = specInfoListIn.getBgindt();//交易起始日期
		String endddt = specInfoListIn.getEndddt();//交易终止日期
		E_SPECST specst = specInfoListIn.getSpecst();//特殊业务状态
		String custac = null;
		
		
		if(CommUtil.isNotNull(specInfoListIn.getCardno())){
			
			IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			
			IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(specInfoListIn.getCardno(), false);
			
			if(CommUtil.isNull(caKnaAcdc)){
				throw DpModuleError.DpstComm.BNAS0692();
			}
			
//			if(caKnaAcdc.getStatus() != E_DPACST.NORMAL){
//				throw DpModuleError.DpstComm.E9999("根据电子账号获取电子账号ID状态不正常");
//			}
			
			custac = caKnaAcdc.getCustac();
		}
		
		Page<IoDpSpecInfo> specInfoList = null;
		
		//当前登录机构是否为省中心机构
		if(!CommUtil.equals("999", CommTools.getBaseRunEnvs().getBusi_org_id())){
			
			//电子账号、机构、特殊业务编号都为空则根据县级行社查询记录
			if(CommUtil.isNull(tranbr) 
					&& CommUtil.isNull(specno) 
					&& CommUtil.isNull(specInfoListIn.getCardno())){
				
				String corpno  = CommTools.getBaseRunEnvs().getBusi_org_id();
				
				if(E_SPECST.NOREMO == specst){ //未解除
					
					specInfoList = DpFrozDao.selDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, E_FROZST.VALID,
							corpno, startno, pgsize, totlCount, false);
					if(CommUtil.isNull(specInfoList)){
						throw DpModuleError.DpstComm.BNAS0729();	
					}
					for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
							//设置输出业务类型
						    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
							specInfo.setSpecst(E_SPECST.NOREMO);
					}
				
				}else if(E_SPECST.OKREMO == specst){//已解除
					
					specInfoList = DpFrozDao.selDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, E_FROZST.INVALID,
							corpno, startno, pgsize, totlCount, false);  
					if(CommUtil.isNull(specInfoList)){
						throw DpModuleError.DpstComm.BNAS0729();	
					}
					for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
						//设置输出业务类型
					    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
						specInfo.setSpecst(E_SPECST.OKREMO);
					}
				
				}else if(E_SPECST.ALL == specst){//全部
					
					specInfoList = DpFrozDao.selAllDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, 
							corpno,startno, pgsize, totlCount, false);
					if(CommUtil.isNull(specInfoList)){
						throw DpModuleError.DpstComm.BNAS0729();	
					}
					for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
						//设置输出业务类型
					    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
					    
					    if(E_FROZST.VALID == specInfo.getFrozst()){
					    	specInfo.setSpecst(E_SPECST.NOREMO);
					    
					    }else if(E_FROZST.INVALID == specInfo.getFrozst()){
					    	specInfo.setSpecst(E_SPECST.OKREMO);
					    }
					}
				}
			
			//机构号为空，电子账号或特殊业务序号不为空则按全省查符合要求的记录
			}else if(CommUtil.isNull(tranbr) 
						&& (CommUtil.isNotNull(specInfoListIn.getCardno())
						|| CommUtil.isNotNull(specno))){
				String corpno = null;
				
				if(E_SPECST.NOREMO == specst){ //未解除
					
					specInfoList = DpFrozDao.selDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, E_FROZST.VALID,
							corpno, startno, pgsize, totlCount, false);
					if(CommUtil.isNull(specInfoList)){
						throw DpModuleError.DpstComm.BNAS0729();	
					}
					for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
							//设置输出业务类型
						    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
							specInfo.setSpecst(E_SPECST.NOREMO);
					}
				
				}else if(E_SPECST.OKREMO == specst){//已解除
					
					specInfoList = DpFrozDao.selDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, E_FROZST.INVALID,
							corpno, startno, pgsize, totlCount, false);  
					if(CommUtil.isNull(specInfoList)){
						throw DpModuleError.DpstComm.BNAS0729();	
					}
					for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
						//设置输出业务类型
					    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
						specInfo.setSpecst(E_SPECST.OKREMO);
					}
				
				}else if(E_SPECST.ALL == specst){//全部
					
					specInfoList = DpFrozDao.selAllDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, 
							corpno,startno, pgsize, totlCount, false);
					if(CommUtil.isNull(specInfoList)){
						throw DpModuleError.DpstComm.BNAS0729();	
					}
					for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
						//设置输出业务类型
					    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
					    
					    if(E_FROZST.VALID == specInfo.getFrozst()){
					    	specInfo.setSpecst(E_SPECST.NOREMO);
					    
					    }else if(E_FROZST.INVALID == specInfo.getFrozst()){
					    	specInfo.setSpecst(E_SPECST.OKREMO);
					    }
					}
				}			
			//机构号不为空
			}else if(CommUtil.isNotNull(tranbr)){
				
				/*if(tranbr.length() != 6){
					throw DpModuleError.DpstComm.BNAS1620();
				}*/
				
				String cur_corpno = tranbr.substring(0,3);
				//当前输入机构和登录机构是否同一行社
				if(!CommUtil.equals(cur_corpno, CommTools.getBaseRunEnvs().getBusi_org_id())){
					
					if(CommUtil.isNull(specno) && CommUtil.isNull(specInfoListIn.getCardno())){
						throw DpModuleError.DpstComm.BNAS1623();
					}
					
					String corpno = null;
					
					if(E_SPECST.NOREMO == specst){ //未解除
						
						specInfoList = DpFrozDao.selDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, E_FROZST.VALID,
								corpno, startno, pgsize, totlCount, false);
						if(CommUtil.isNull(specInfoList)){
							throw DpModuleError.DpstComm.BNAS0729();	
						}
						for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
								//设置输出业务类型
							    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
								specInfo.setSpecst(E_SPECST.NOREMO);
						}
					
					}else if(E_SPECST.OKREMO == specst){//已解除
						
						specInfoList = DpFrozDao.selDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, E_FROZST.INVALID,
								corpno, startno, pgsize, totlCount, false);  
						if(CommUtil.isNull(specInfoList)){
							throw DpModuleError.DpstComm.BNAS0729();	
						}
						for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
							//设置输出业务类型
						    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
							specInfo.setSpecst(E_SPECST.OKREMO);
						}
					
					}else if(E_SPECST.ALL == specst){//全部
						
						specInfoList = DpFrozDao.selAllDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, 
								corpno,startno, pgsize, totlCount, false);
						if(CommUtil.isNull(specInfoList)){
							throw DpModuleError.DpstComm.BNAS0729();	
						}
						for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
							//设置输出业务类型
						    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
						    
						    if(E_FROZST.VALID == specInfo.getFrozst()){
						    	specInfo.setSpecst(E_SPECST.NOREMO);
						    
						    }else if(E_FROZST.INVALID == specInfo.getFrozst()){
						    	specInfo.setSpecst(E_SPECST.OKREMO);
						    }
						}
					}
				
				//当前输入机构为该行社所属机构
				}else{
					String corpno = null;
					if(E_SPECST.NOREMO == specst){ //未解除
						
						specInfoList = DpFrozDao.selDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, E_FROZST.VALID,
								corpno, startno, pgsize, totlCount, false);
						if(CommUtil.isNull(specInfoList)){
							throw DpModuleError.DpstComm.BNAS0729();	
						}
						for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
								//设置输出业务类型
							    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
								specInfo.setSpecst(E_SPECST.NOREMO);
						}
					
					}else if(E_SPECST.OKREMO == specst){//已解除
						
						specInfoList = DpFrozDao.selDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, E_FROZST.INVALID,
								corpno, startno, pgsize, totlCount, false);  
						if(CommUtil.isNull(specInfoList)){
							throw DpModuleError.DpstComm.BNAS0729();	
						}
						for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
							//设置输出业务类型
						    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
							specInfo.setSpecst(E_SPECST.OKREMO);
						}
					
					}else if(E_SPECST.ALL == specst){//全部
						
						specInfoList = DpFrozDao.selAllDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, 
								corpno,startno, pgsize, totlCount, false);
						if(CommUtil.isNull(specInfoList)){
							throw DpModuleError.DpstComm.BNAS0729();	
						}
						for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
							//设置输出业务类型
						    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
						    
						    if(E_FROZST.VALID == specInfo.getFrozst()){
						    	specInfo.setSpecst(E_SPECST.NOREMO);
						    
						    }else if(E_FROZST.INVALID == specInfo.getFrozst()){
						    	specInfo.setSpecst(E_SPECST.OKREMO);
						    }
						}
					}
				}
			}
		
		//当前登录机构为省中心机构
		}else{
			String corpno = null;
			
			if(E_SPECST.NOREMO == specst){//未解除
				specInfoList = DpFrozDao.selDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, E_FROZST.VALID,
						corpno, startno, pgsize, totlCount, false);
				if(CommUtil.isNull(specInfoList)){
					throw DpModuleError.DpstComm.BNAS0729();	
				}
				for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
						//设置输出业务类型
					    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
						specInfo.setSpecst(E_SPECST.NOREMO);
				}
				
			}else if(E_SPECST.OKREMO == specst){
				
				specInfoList = DpFrozDao.selDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, E_FROZST.INVALID,
						corpno, startno, pgsize, totlCount, false);  
				if(CommUtil.isNull(specInfoList)){
					throw DpModuleError.DpstComm.BNAS0729();	
				}
				for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
					//设置输出业务类型
				    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
					specInfo.setSpecst(E_SPECST.OKREMO);
				}
			}else {
				specInfoList = DpFrozDao.selAllDpSpecCustInfo(tranbr, specno, custac, bgindt, endddt, 
						corpno,startno, pgsize, totlCount, false);
				if(CommUtil.isNull(specInfoList)){
					throw DpModuleError.DpstComm.BNAS0729();	
				}
				for(IoDpSpecInfo specInfo : specInfoList.getRecords()){
					//设置输出业务类型
				    specInfo.setOtsptp(E_OTSPTP.ACCTSTOP);
				    
				    if(E_FROZST.VALID == specInfo.getFrozst()){
				    	specInfo.setSpecst(E_SPECST.NOREMO);
				    
				    }else if(E_FROZST.INVALID == specInfo.getFrozst()){
				    	specInfo.setSpecst(E_SPECST.OKREMO);
				    }
				}
			}
		}

		return specInfoList;
	}
	
	/**
	 * @author douwenbo
	 * @date 2016-04-28 19:48
	 * 特殊业务类型为 5全部时的查询
	 * @param specInfoListIn
	 * @param cplDpSpecInfoList
	 * @return cplDpSpecInfoList
	 */
	public static Page<IoDpSpecInfo> querySpecInfoListForAll(DpSpecInfoListIn specInfoListIn, int totlCount, int startno,int pgsize){
		
		String tranbr = specInfoListIn.getTranbr();//交易机构
		String specno = specInfoListIn.getSpecno();//特殊业务编号
		E_SPECST specst = specInfoListIn.getSpecst();//特殊业务状态
		String bgindt = specInfoListIn.getBgindt();//交易起始日期
		String endddt = specInfoListIn.getEndddt();//交易终止日期
		E_STOPMS stopms = specInfoListIn.getStopms();//止付措施
		E_LAWCOP frexog = specInfoListIn.getFrexog();//部门类型
		String custac = null;//电子账号
		
		if(CommUtil.isNotNull(specInfoListIn.getCardno())){
			
			IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			
			IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb2(specInfoListIn.getCardno(), true);
			
			if(CommUtil.isNull(caKnaAcdc)){
				throw DpModuleError.DpstComm.BNAS0692();
			}
			
			if(caKnaAcdc.getStatus() != E_DPACST.NORMAL){
				throw DpModuleError.DpstComm.BNAS1624();
			}
			
			custac = caKnaAcdc.getCustac();
		}
	   
		Page<IoDpSpecInfo> specInfoList = null;
		
		if(E_SPECST.NOREMO == specst){//未解除
			
			specInfoList = DpFrozDao.selAllSpecInfoByPage(tranbr, specno, custac, bgindt, endddt, stopms, E_FROZST.VALID, frexog,startno, pgsize, totlCount, false);
		
		}else if(E_SPECST.OKREMO == specst){//已解除
			
			specInfoList = DpFrozDao.selAllSpecInfoByPage(tranbr, specno, custac, bgindt, endddt, stopms, E_FROZST.INVALID, frexog, startno, pgsize, totlCount, false);
		
		}else if(E_SPECST.ALL == specst){
			
			specInfoList =  DpFrozDao.selAllSpecByPage(tranbr, specno, custac, bgindt, endddt, stopms, frexog,startno, pgsize, totlCount, false);
		}
		
		return specInfoList;
	}
	
	
	//查询前检查
	public static void queryCheck(IoDpFrozQrIn cpliodpfrozqrin){
		// 冻结编号，电子账号，卡号最少输入一项
		if (CommUtil.isNull(cpliodpfrozqrin.getCardno())&&CommUtil.isNull(cpliodpfrozqrin.getCustac())&& CommUtil.isNull(cpliodpfrozqrin.getFrozno())) {

			throw DpModuleError.DpstComm.BNAS0836();
		}
		
		//查询条数必须大于0
		if(cpliodpfrozqrin.getCounts() <= 0){
			throw DpModuleError.DpstComm.BNAS1060();
		}
		
		// 日期格式检查
		if (CommUtil.isNotNull(cpliodpfrozqrin.getBgdate()) && !DateTools2.chkIsDate(cpliodpfrozqrin.getBgdate())) {
			throw DpModuleError.DpstComm.BNAS0409();
		}
		if (CommUtil.isNotNull(cpliodpfrozqrin.getEddate()) && !DateTools2.chkIsDate(cpliodpfrozqrin.getEddate())) {
			throw DpModuleError.DpstComm.BNAS0597();
		}

		//查询起始日期不能大于终止日期
		if(CommUtil.compare(cpliodpfrozqrin.getBgdate(), cpliodpfrozqrin.getEddate()) > 0){
			throw DpModuleError.DpstComm.BNAS1063();
		}
		
		/*//输入的电子账号与卡都不为空则进行匹配性检查
		if(CommUtil.isNotNull(cpliodpfrozqrin.getCustac()) && CommUtil.isNotNull(cpliodpfrozqrin.getCardno())){
			//if(!CommUtil.equals(cpliodpfrozqrin.getCustac(), Kna_acdcDao.selectOne_odb2(cpliodpfrozqrin.getCardno(), true).getCustac())){
			IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			if(!CommUtil.equals(cpliodpfrozqrin.getCustac(), caqry.getKnaAcdcOdb2(cpliodpfrozqrin.getCardno(), true).getCustac())){
				
				throw DpModuleError.DpstComm.BNAS1253(cpliodpfrozqrin.getCardno(),cpliodpfrozqrin.getCustac());
			}
		}*/
	}

	public static Options<IoDpFrozIf> queryFrozInfo(IoDpFrozQrIn cpliodpfrozqrin) {

		List<IoDpFrozIf> lstFrozInfo = null;

		// 冻结编号不为空则按冻结编号查询
		// 子账号不为空，则按电子账号+子账号查询
		// 其他按电子账号查询
		if (CommUtil.isNotNull(cpliodpfrozqrin.getFrozno())) {
			
			lstFrozInfo = DpFrozDao.selFrozInfoByFrozno(
					cpliodpfrozqrin.getFrozno(), 
					cpliodpfrozqrin.getBgrecd(), cpliodpfrozqrin.getCounts(), false);
			
		} else if (CommUtil.isNotNull(cpliodpfrozqrin.getCardno())) {
			//kna_accs tblAccs = Kna_accsDao.selectOne_odb1(cpliodpfrozqrin.getCustac(), cpliodpfrozqrin.getSubsac(),false);
			
			//IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			//IoCaKnaAccs tblAccs = caqry.getKnaAccsOdb1(cpliodpfrozqrin.getCustac(), cpliodpfrozqrin.getSubsac(), false);
			
			lstFrozInfo = DpFrozDao.selFrozInfoByAcct(cpliodpfrozqrin.getCardno(),
					cpliodpfrozqrin.getFroztp(), cpliodpfrozqrin.getFrlmtp(),
					cpliodpfrozqrin.getFrozam(), cpliodpfrozqrin.getBgdate(),
					cpliodpfrozqrin.getEddate(), cpliodpfrozqrin.getFrozst(),
					cpliodpfrozqrin.getBgrecd(), cpliodpfrozqrin.getCounts(), 
					false);
		} else {
			lstFrozInfo = DpFrozDao.selFrozInfoByCusac(
					cpliodpfrozqrin.getCustac(), cpliodpfrozqrin.getFroztp(),
					cpliodpfrozqrin.getFrlmtp(), cpliodpfrozqrin.getFrozam(),
					cpliodpfrozqrin.getBgdate(), cpliodpfrozqrin.getEddate(),
					cpliodpfrozqrin.getFrozst(), cpliodpfrozqrin.getBgrecd(),
					cpliodpfrozqrin.getCounts(), false);
		}

		return new DefaultOptions<IoDpFrozIf>(lstFrozInfo);
	}

}
