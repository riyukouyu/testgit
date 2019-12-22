package cn.sunline.ltts.busi.dp.serviceimpl;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.froz.FrozPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpFrozDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddtDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStaPublic.IoDpCheckAcCarry.Input;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStaPublic.IoDpCheckAcCarry.Output;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.AcTranfeChkIN;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CKTRIF;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CKTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
/**
  * 冻结信息对外公共服务实现类
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoDpStaPublicImpl", longname="冻结信息对外公共服务实现类", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoDpStaPublicImpl implements cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStaPublic{

	/**
	 * 根据电子账号获取电子账号的冻结状态
	 * 
	 * @author douwenbo
	 * @date 2016-05-24 20:55
	 * @param custac 电子账号
	 */
	@Override
	public E_FROZST KnbFroz_getFrozstByCustac(String custac,Boolean isable) {
		return FrozPublic.KnbFroz_getFrozstByCustac(custac,isable);
	}

	/**
	 * 根据电子账号获取电子账号的冻结信息
	 * 
	 * @author douwenbo
	 * @date 2016-05-25 10:02
	 * @param custac 电子账号
	 * @param isable 是否
	 */
	@Override
	public Options<IoDpKnbFroz> KnbFroz_getInfoByCustac(String custac,Boolean isable) {
		
		//获取冻结集合输出接口
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		E_FROZST frozst = E_FROZST.VALID;
		
		Options<IoDpKnbFroz> ops = new DefaultOptions<IoDpKnbFroz>();
		List<IoDpKnbFroz> knbFrozList = DpFrozDao.selFrozInfoByCustac(custac, tranbr, trandt, frozst, isable);
		if(CommUtil.isNotNull(knbFrozList) && knbFrozList.size() > 0){
			//Collections.copy(ops, knbFrozList);
//			for(IoDpKnbFroz lst : knbFrozList){
//				ops.add(lst);
//			}
			ops.addAll(knbFrozList);
		}
		return ops;
	}

	/**
	 * 根据冻结流水和日期冻结
	 * 
	 * @author douwenbo
	 * @date 2016-06-01 14:45
	 * @param mntrsq 冻结主流水
	 * @param trandt 冻结日期
	 */
	@Override
	public void DoFrozByMntrsqdt(String mntrsq, String trandt, Boolean isable) {
		
		FrozPublic.DoFrozByMntrsqdt(mntrsq , trandt, isable);
	}

	/**
	 * 根据冻结流水和日期解冻
	 * 
	 * @author douwenbo
	 * @date 2016-05-25 22:54
	 * @param mntrsq 冻结主流水
	 * * @param trandt 冻结日期
	 */
	@Override
	public void DoUnFrByMntrsqdt(String mntrsq, String trandt, Boolean isable) {
		
		FrozPublic.DoUnFrByMntrsqdt(mntrsq , trandt, isable);
	}

	@Override
	public void checkAcCarry(Input input, Output output) {
		if(CommUtil.isNull(input.getCardno())){
			throw DpModuleError.DpstAcct.BNAS0311();
		}
		if(CommUtil.isNull(input.getTrantp())){
			throw CaError.Eacct.BNAS0615();
		}
		
		//校验电子账户
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaSrvGenEAccountInfo cagen = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		
		IoCaKnaAcdc kna_acdc = caqry.getKnaAcdcByCardno(input.getCardno(), false); //获得对内的电子账号ID
		
		if(CommUtil.isNull(kna_acdc)){
			throw DpModuleError.DpstComm.BNAS0754();
		}
		//会影响后续交易正常进行，不能修改法人 modify by chenlk 20191129
		//CommTools.getBaseRunEnvs().setBusi_org_id(kna_acdc.getCorpno());
		
		IoCaKnaCust kna_cust = caqry.getKnaCustByCustacOdb1(kna_acdc.getCustac(), true); //根据电子账号ID查询电子账户信息
		
		E_CKTRTP trantp = input.getTrantp();
		E_ACCATP accatp = cagen.qryAccatpByCustac(kna_acdc.getCustac()); //账户类型
		
		//add 20170301 songlw 增加账户状态异常判断 start
		IoCaKnaAcdc inacdc = ActoacDao.selKnaAcdc(input.getCardno(), false);
		if(CommUtil.isNull(inacdc)){
			throw CaError.Eacct.BNAS0750();
		}
		
		if(inacdc.getStatus() == E_DPACST.CLOSE){
			throw CaError.Eacct.BNAS0441();
		}
		AcTranfeChkIN chkIN = SysUtil.getInstance(AcTranfeChkIN.class);
		chkIN.setAccatp(accatp);
		chkIN.setCardno(input.getCardno()); //电子账号卡号
		chkIN.setCustac(inacdc.getCustac()); //电子账号ID
		chkIN.setCustna(kna_cust.getCustna());
		if(E_CKTRTP.ATMDP == input.getTrantp()){ //ATM存现
			chkIN.setCapitp(E_CAPITP.IN106);
		}else if(E_CKTRTP.ATMCR == input.getTrantp()){ //ATM转入
			chkIN.setCapitp(E_CAPITP.IN105);
		}else if(E_CKTRTP.ATMDR == input.getTrantp()){ //ATM取现
			chkIN.setCapitp(E_CAPITP.OT204);
		}else if(E_CKTRTP.CUPCR == input.getTrantp()){ //银联cups转入
			chkIN.setCapitp(E_CAPITP.IN104);
		}
		chkIN.setOpcard(input.getToacct());
		chkIN.setOppona(null);
		chkIN.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
		CapitalTransCheck.chkTranfe(chkIN);
		//end
		
//		boolean out = false ; //转出
		boolean in = false; //转入
		
//		if(trantp == E_CKTRTP.ATMCR){ //1.2
//			if(accatp == E_ACCATP.WALLET){
//				throw DpModuleError.DpstComm.E9999("钱包账户不允许ATM转入");
//			}
//			in = true;
//		}else if(trantp == E_CKTRTP.ATMDP){ //1
//			if(accatp != E_ACCATP.GLOBAL){
//				throw DpModuleError.DpstComm.E9999("非全功能账户不允许ATM无卡存款");
//			}
//			in = true;
//		}else if(trantp == E_CKTRTP.ATMDR){
//			if(accatp != E_ACCATP.GLOBAL){
//				throw DpModuleError.DpstComm.E9999("非全功能账户不允许ATM无卡取款");
//			}
//			out = true;
//		}else 
		if(trantp == E_CKTRTP.CRCCR || trantp == E_CKTRTP.CUPCR || trantp == E_CKTRTP.DBCCR || trantp == E_CKTRTP.UNNCR){
			if(accatp == E_ACCATP.WALLET && CommUtil.compare(input.getTranam(), BigDecimal.ZERO) > 0){
				KnaAcct acct = CapitalTransDeal.getSettKnaAcctSub(kna_acdc.getCustac(), E_ACSETP.MA);
				BigDecimal hdmxmy = KnaAcctAddtDao.selectOne_odb1(acct.getAcctno(), true).getHigham();
				if(!CommUtil.equals(hdmxmy,BigDecimal.ZERO)){
					if(CommUtil.compare(input.getTranam(), hdmxmy.subtract(acct.getOnlnbl())) > 0){
						throw CaError.Eacct.E0001("III类账户余额不得超过2000元");
					}
				}
				KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm.maxbln", "3", "%", "%", true);
				BigDecimal maxval = ConvertUtil.toBigDecimal(para.getParm_value1());
				if(!CommUtil.equals(maxval, BigDecimal.ZERO)){
					if(CommUtil.compare(input.getTranam(), maxval.subtract(acct.getOnlnbl())) > 0){
					    throw CaError.Eacct.E0001("III类账户余额不得超过2000元");
					}
				}
				
			}
			in = true;
		}
		
		if(in){
			CapitalTransCheck.ChkAcctstIN(kna_cust.getCustac());
			CapitalTransCheck.ChkAcctFrozIN(kna_acdc.getCustac());
		}
		/*if(out){
			CapitalTransCheck.ChkAcctstOT(kna_acdc.getCustac());
			CapitalTransCheck.ChkAcctFrozOT(kna_acdc.getCustac());
		}*/
		
		output.setIsable(E_CKTRIF.ENOK);
		output.setCustnm(kna_cust.getCustna());
		
		//poc
		ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
		apAudit.regLogOnInsertBusiPoc(input.getCardno());
	}
	
}

