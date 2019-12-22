package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnsTranEror;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnsTranErorDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnlIoblCups;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.CupsTranfe;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUPSST;


public class cupscf {

	public static void dealTransBefore( final cn.sunline.ltts.busi.dptran.trans.client.intf.Cupscf.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Cupscf.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Cupscf.Output output){
		
		chkParam(input); 
		// 摘要码
		BusiTools.getBusiRunEnvs().setSmrycd(BusinessConstants.SUMMARY_ZR);// 转入
		
		String reunsq = input.getReunsq(); //银联流水
		KnlIoblCups cups = ActoacDao.selKnlIoblCups(reunsq, false);
		if(CommUtil.isNotNull(cups)){
			output.setMntrdt(cups.getTrandt());
			output.setMntrsq(cups.getMntrsq());
			output.setMntrtm(cups.getTmstmp());
			return;
		}
		
		dealTrans(input, property, output);
		
	}
	
	/**
	 * CUPS转入记账处理
	 * @param input
	 * @param property
	 * @param output
	 */
	private static void dealTrans(final cn.sunline.ltts.busi.dptran.trans.client.intf.Cupscf.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Cupscf.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Cupscf.Output output){

		
		
		CupsTranfe cupsIN = SysUtil.getInstance(CupsTranfe.class);
		cupsIN.setCrcycd(input.getCrcycd());
		cupsIN.setInacct(input.getInacct());
		cupsIN.setInbrch(input.getInbrch());
		cupsIN.setOtacct(input.getOtacct());
		cupsIN.setOtacna(input.getOtacna());
		cupsIN.setOtbrch(input.getOtbrch());
		cupsIN.setTranam(input.getTranam());
		
		IoDpKnlIoblCups cups = SysUtil.getInstance(IoDpKnlIoblCups.class);
		//KnlIoblCups cups = SysUtil.getInstance(KnlIoblCups.class);
		E_YES___ istrcf = E_YES___.NO;
		
		String clerdt = BusiTools.getBusiRunEnvs().getClerdt();
		
		try {
			
			CapitalTransDeal.dealCUPSTranfe(cupsIN, input.getChkqtn());
			cups.setInacna(cupsIN.getInacna());
			cups.setTranst(E_CUPSST.SUCC);
			cups.setDescrb("转入确认成功");
		} catch (Exception e) {
			DaoUtil.rollbackTransaction();
			
			String errmes = e.getLocalizedMessage();
			
			if(CommUtil.isNotNull(errmes)){
				int index = errmes.indexOf("]");		
				if(index >= 0){					
					errmes = errmes.substring(index + 1).replace("]", "").replace("[", "");
				}
			}
			
			String remark = input.getInacct() + input.getRetrdt() + input.getResssq() + errmes;
			cupsIN.setRemark(remark);
			
			
			IaTransOutPro acdrOt = CapitalTransDeal.dealCUPSConfrim(cupsIN);
			cups.setReprsq(input.getReprsq()); //原前置流水号
			cups.setResssq(input.getResssq()); //原系统跟踪号
			cups.setRetrdt(input.getRetrdt()); //原交易日期时间
			cups.setGlacct(acdrOt.getAcctno()); //挂账账号
			cups.setGlacna(acdrOt.getAcctna());
			cups.setInacna(acdrOt.getAcctna());
			if(CommUtil.isNotNull(acdrOt.getPayasqlist())&&acdrOt.getPayasqlist().size()>0){
				
				cups.setGlseeq(acdrOt.getPayasqlist().get(0).getPayasq());
			}
							
			cups.setTranst(E_CUPSST.HANG);
			cups.setDescrb("转入确认挂账");
			cups.setDescrb(e.getLocalizedMessage());
			
			//登记转账异常预警登记簿
			
			istrcf = E_YES___.YES;
			
			KnsTranEror tblKnsTranEror = SysUtil.getInstance(KnsTranEror.class);
			tblKnsTranEror.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
			tblKnsTranEror.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
    		/*if(CommTools.getBaseRunEnvs().getIscose()==E_YES___.YES){
    			
    			tblKnsTranEror.setErortp(E_WARNTP.CLOSING);	
    		}else{
    			
    			tblKnsTranEror.setErortp(E_WARNTP.ACOUNT);			
    		}	*/
			tblKnsTranEror.setBrchno(input.getInbrch());
			tblKnsTranEror.setOtacct(input.getOtacct());
			tblKnsTranEror.setOtacna(input.getOtacna());
			tblKnsTranEror.setOtbrch(input.getOtbrch());
			tblKnsTranEror.setInacct(input.getInacct());					
			tblKnsTranEror.setInacna(cupsIN.getInacna());
			tblKnsTranEror.setInbrch(input.getInbrch());
			tblKnsTranEror.setTranam(input.getTranam());
			tblKnsTranEror.setCrcycd(input.getCrcycd());
			tblKnsTranEror.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
			tblKnsTranEror.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());
			tblKnsTranEror.setGlacct(acdrOt.getAcctno());
			tblKnsTranEror.setGlacna(acdrOt.getAcctna());
			if(CommUtil.isNotNull(acdrOt.getPayasqlist())&&acdrOt.getPayasqlist().size()>0){
				
				tblKnsTranEror.setGlseeq(acdrOt.getPayasqlist().get(0).getPayasq());
			}
			

				
			
			tblKnsTranEror.setDescrb(errmes);
			//插入登记簿
			KnsTranErorDao.insert(tblKnsTranEror);
			
			
		}
		
		//登记银联CUPS转入登记簿
		
		cups.setAmntcd(input.getAmntcd()); //借贷标志
		cups.setAuthno(input.getAuthno());
		cups.setBusino(input.getBusino()); //商户代码
		cups.setBusitp(input.getBusitp());
		cups.setCardno(input.getCardno());
		cups.setChckno(input.getChckno()); //对账分组编号
		cups.setChrgam(input.getChrgam()); //手续费
		cups.setCrcycd(input.getCrcycd()); //币种
		cups.setDevcno(input.getDevcno()); //设备编号
		cups.setInacct(input.getInacct()); //转入账号，电子账号卡号
		//cups.setInbrch(input.getInbrch()); //电子账号所属机构
		cups.setInbrch(cupsIN.getInbrch());
		cups.setInacna(cupsIN.getInacna());
		cups.setMesstp(input.getMesstp()); //报文类型
		cups.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		cups.setOtacct(input.getOtacct());	//转出账号
		cups.setOtacna(input.getOtacna()); //转出户名
		cups.setOtbrch(input.getOtbrch()); //转出机构
		cups.setPrepdt(input.getPrepdt()); //银联前置日期
		cups.setPrepsq(input.getPrepsq());	//银联前置流水
		cups.setProccd(input.getProccd()); //银联处里面
		cups.setReprsq(input.getReprsq()); //原前置流水号
		cups.setResssq(input.getResssq()); //原系统跟踪号
		cups.setRetrdt(input.getRetrdt()); //原交易日期时间
		cups.setServsq(input.getServsq()); //渠道流水
		cups.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); //渠道
		cups.setSpared(input.getSpared()); //备用
		cups.setStand1(input.getStand1());
		cups.setStand2(input.getStand2());
		cups.setTranam(input.getTranam());
		cups.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		
		cups.setPrdate(input.getPrdate());
		cups.setPrbrmk(input.getPrbrmk());
		cups.setTrbrmk(input.getTrbrmk());
		
		cups.setTrbrch(input.getTrbrch()); //交易受理机构 tranbr
		cups.setTrcode(input.getTrcode()); //银联交易码
		cups.setUniseq(input.getUniseq()); //银联流水
		cups.setUnkpdt(input.getUnkpdt()); //银联清算日期
		cups.setCnkpdt(clerdt); //核心对账日期
		//KnlIoblCupsDao.insert(cups);
		CommTools.getRemoteInstance(IoSaveIoTransBill.class).saveCupstr(cups, istrcf);
		
		//平衡性检查+涉案检查
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_CLACTP._04);
		
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());
		
	}
	
	
	/**
	 * 输入参数非空检查
	 * @param input
	 */
	private static void chkParam(final cn.sunline.ltts.busi.dptran.trans.client.intf.Cupscf.Input input){
		if(CommUtil.isNull(input.getPrepsq())){
			throw DpModuleError.DpstComm.BNAS1914();
		}
		
		if(CommUtil.isNull(input.getCardno())){
			throw DpModuleError.DpstComm.BNAS0570();
		}
		
		if(CommUtil.isNull(input.getDevcno())){
			throw DpModuleError.DpstComm.BNAS1915();
		}
		
		if(CommUtil.isNull(input.getAmntcd())){
			throw DpModuleError.DpstComm.BNAS1370();
		}
		
		if(CommUtil.isNull(input.getPrepdt())){
			throw DpModuleError.DpstComm.BNAS1916();
		}
		
		if(CommUtil.isNull(input.getCrcycd())){
			throw DpModuleError.DpstComm.BNAS0195();
		}
		
		if(CommUtil.isNull(input.getCnkpdt())){
			//throw DpModuleError.DpstComm.E9999("核心清算日期不能为空");
		}
		
		if(CommUtil.isNull(input.getUnkpdt())){
			throw DpModuleError.DpstComm.BNAS1917();
		}
		
		if(CommUtil.isNull(input.getInacct())){
			throw DpModuleError.DpstAcct.BNAS0028();
		}
		
		if(CommUtil.isNull(input.getOtacct())){
			throw DpModuleError.DpstComm.BNAS1918();
		}
		
		if(CommUtil.isNull(input.getInbrch())){
			//throw DpModuleError.DpstComm.E9999("转入机构不能为空");
		}
		
		if(CommUtil.isNull(input.getOtbrch())){
			//throw DpModuleError.DpstComm.E9999("转出机构不能为空");
		}
		
		if(CommUtil.isNull(input.getTrbrch())){
			//throw DpModuleError.DpstComm.E9999("受理机构不能为空");
		}
		
		if(CommUtil.isNull(input.getTrcode())){
			throw DpModuleError.DpstComm.BNAS1919();
		}
		
		if(CommUtil.isNull(input.getServsq())){
			throw DpModuleError.DpstComm.BNAS1920();
		}
		
		if(CommUtil.isNull(input.getChckno())){
			//throw DpModuleError.DpstComm.E9999("对账分类编号不能为空");
		}
		
		if(CommUtil.isNull(input.getOtacna())){
			//throw DpModuleError.DpstComm.E9999("转出户名不能为空");
		}
		
		if(CommUtil.isNull(input.getUniseq())){
			throw DpModuleError.DpstComm.BNAS1921();
		}
		
		if(CommUtil.compare(input.getTranam(), BigDecimal.ZERO) <= 0){  //校验交易金额
			throw DpModuleError.DpstComm.BNAS0627();
		}
		
		if(CommUtil.isNull(input.getChkqtn().getIsckqt())){
			throw DpModuleError.DpstAcct.BNAS1897();
		}
	}
}
