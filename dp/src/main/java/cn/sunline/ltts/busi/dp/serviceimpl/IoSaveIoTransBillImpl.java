package cn.sunline.ltts.busi.dp.serviceimpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_EVENTLEVEL;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_EVNTST;
import cn.sunline.clwj.msap.sys.type.MsEnumType.E_YESORNO;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqCler;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqClerDao;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrDetl;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrDetlDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbRegi;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnbRegiDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCary;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCaryDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblChrg;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCupsDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlSpnd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlSpndDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill.AccConsume.Output;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill.SaveChrg.Input;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.InknbRptrDetl;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnlIoblCups;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoKnlCary;
import cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnaAcct;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccountClearInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoKnsAcsqInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.ChrgIN;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdQuery.IoDpToAcctInfoQry;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLERST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CORDST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRAYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_RPTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SPETTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRNESS;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;
 /**
  * 登记出入金服务实现
  * 登记出入金服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
public class IoSaveIoTransBillImpl implements cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill{
	
 /**
  * 记录出入金记录
  *
  */
	public void saveIoBill( final cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill.SaveIoBill.Input Input){
		//KnlIobl iobl = KnlIoblDao.selectOneWithLock_odb1(Input.getFronsq(), Input.getFrondt(), false);
		KnlIobl iobl = SysUtil.getInstance(KnlIobl.class);
		iobl.setCardno(Input.getCardno());
		iobl.setCrcycd(Input.getCrcycd());
		iobl.setFromtp(Input.getFromtp());
		iobl.setPrcscd(Input.getPrcscd());
		iobl.setServdt(Input.getFrondt());//支付前置流水-渠道流水
		iobl.setServsq(Input.getFronsq());//支付前置日期-渠道日期
		iobl.setStatus(CommUtil.nvl(Input.getTranst(), E_TRANST.NORMAL)); //默认正常，为ChinaPay接口考虑
		iobl.setTranam(Input.getTranam());
		iobl.setRemark(Input.getRemark());
		iobl.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		iobl.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
		iobl.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		iobl.setCuacno(Input.getCuacno());
		iobl.setIoflag(Input.getIoflag());
		iobl.setKeepdt(Input.getKeepdt());//清算时间
		iobl.setCapitp(Input.getCapitp());
		
		iobl.setToacno(Input.getToacno());
		iobl.setToscac(Input.getToscac());
		iobl.setBusino(Input.getBusino());
		iobl.setAcctno(Input.getAcctno()); //子账号
		iobl.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq()); //业务跟踪编号
		iobl.setChckdt(Input.getChckdt()); //对账日期
		iobl.setBrchno(Input.getBrchno()); //电子账户所属机构
		iobl.setTobrch(Input.getTobrch()); //对方账号所属机构
		iobl.setTlcgam(Input.getTlcgam()); //收费总金额
		iobl.setToacct(Input.getToacct()); //对方账号
		iobl.setIncorp(Input.getIncorp()); //转入方法人
		iobl.setTocorp(Input.getOtcorp()); //转出方法人
		iobl.setIscler(E_YES___.NO); //是否已清算
		iobl.setIsspan(Input.getIsspan()); //是否跨法人
		iobl.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); //渠道
		KnlIoblDao.insert(iobl);
		
		//注册冲账
		//冲正注册
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		cplInput.setEvent1(Input.getFronsq());
		cplInput.setEvent2(Input.getFrondt());
		cplInput.setTranev(ApUtil.TRANS_EVENT_IOBILL);
		
		//ApStrike.regBook(cplInput);
		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
		apinput.setReversal_event_id(cplInput.getTranev());
		apinput.setInformation_value(SysUtil.serialize(cplInput));
		MsEvent.register(apinput, true);
	}

	@Override
	public void saveChrg(Input input) {
	
		List<KnlIoblChrg> entity = new ArrayList<>();
		Options<ChrgIN> lstChrg = input.getChrg();
		KnlIoblChrg chrg = SysUtil.getInstance(KnlIoblChrg.class);
		chrg.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq());
		chrg.setServdt(input.getTrandt());
		chrg.setServsq(input.getTransq());
		for(ChrgIN ls : lstChrg){
			chrg.setAmount(ls.getAmount());
			chrg.setChrgcd(ls.getChrgcd());
			chrg.setClcham(ls.getClcham());
			chrg.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
			//chrg.setDatetm(CommTools.getBaseRunEnvs().getTrxn_date());
			chrg.setDioage(ls.getDioage());
			chrg.setDioamo(ls.getDioamo());
			chrg.setDircam(ls.getDircam());
			chrg.setDisrat(ls.getDisrat());
			chrg.setDitage(ls.getDitage());
			chrg.setDitamo(ls.getDitamo());
			chrg.setDiwage(ls.getDiwage());
			chrg.setDiwamo(ls.getDiwamo());
			chrg.setPaidam(ls.getPaidam());
			chrg.setPronum(ls.getPronum());
			//chrg.settmstmp(BusiTools.getBusiRunEnvs().getTrantm());
			chrg.setTranam(ls.getTranam());
			chrg.setTrinfo(ls.getTrinfo());
			entity.add(chrg);
		}
		
		DaoUtil.insertBatch(KnlIoblChrg.class, entity);
		
	}

	@Override
	public void SaveActoac(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill.SaveActoac.Input input) {
		
		KnlCary cary = SysUtil.getInstance(KnlCary.class);
		
		cary.setCardno(input.getCardno());
		cary.setCrcycd(input.getCrcycd());
		cary.setFromtp(input.getFromtp());
		cary.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());
		cary.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
		cary.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		// ************mdy by zhanga 20161227***********
		cary.setTrantp(input.getTrantp());
		if(input.getTrantp() == E_TRNESS.T02 || input.getTrantp() == E_TRNESS.T03){ //非实时转账记录为待处理
			cary.setStatus(E_TRANST.WAIT); 
			cary.setAcdate(input.getAcdate());
			cary.setActime(input.getActime());
		}else{
			cary.setStatus(CommUtil.nvl(input.getStatus(), E_TRANST.NORMAL)); //默认正常，为ChinaPay接口考虑
		}
		cary.setTranam(input.getTranam());
		cary.setRemark(input.getRemark());
		cary.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
		cary.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
		cary.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		cary.setCuacno(input.getCuacno());
		cary.setIoflag(input.getIoflag());
		cary.setKeepdt(input.getKeepdt());//清算时间
		cary.setFromtp(E_FROMTP.A);
		cary.setCapitp(E_CAPITP.NT301);
		cary.setToacno(input.getToacno());
		cary.setToscac(input.getToscac());
		cary.setBusino(input.getBusino());
		cary.setAcctno(input.getAcctno()); //子账号
		cary.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq()); //业务跟踪编号
		cary.setChckdt(input.getChckdt()); //对账日期
		cary.setBrchno(input.getBrchno()); //电子账户所属机构
		cary.setTobrch(input.getTobrch()); //对方账号所属机构
		cary.setTlcgam(input.getTlcgam()); //收费总金额
		cary.setToacct(input.getToacct()); //对方账号
		cary.setIncorp(input.getIncorp()); //转入方法人
		cary.setTocorp(input.getTocorp()); //转出方法人
		cary.setIscler(E_YES___.NO); //是否已清算
		cary.setIsspan(input.getIsspan()); //是否跨法人
		cary.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); //渠道
		KnlCaryDao.insert(cary);
		
		//注册冲账
		//冲正注册
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		cplInput.setEvent1(input.getTransq());
		cplInput.setEvent2(input.getTrandt());
		cplInput.setTranev(ApUtil.TRANS_EVENT_ACTOAC);
		
		//ApStrike.regBook(cplInput);
		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
		apinput.setReversal_event_id(cplInput.getTranev());
		apinput.setInformation_value(SysUtil.serialize(cplInput));
		MsEvent.register(apinput, true);
	
	}



	/**
	 * 电子账户消费登记薄
	 */
	public void accConsumef(cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill.AccConsume.Input input, Output output) {
		
		KnlSpnd tblKnlSpnd = SysUtil.getInstance(KnlSpnd.class);
		
		if (CommUtil.isNull(input.getSpettp())){
			throw InError.comm.E0003("电子账户消费类型不能为空");
		}
		// 电子账户消费、缴费
		if (E_SPETTP.SPEND == input.getSpettp()|| E_SPETTP.PAY == input.getSpettp()) {
			// 取得付方账户信息
			IoDpToAcctInfoQry cplToacct = DpAcctDao.seltoAcctInfo(input.getCardno(), true);
			// 取得收款法人

			tblKnlSpnd = SysUtil.getInstance(KnlSpnd.class);
			tblKnlSpnd.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 渠道流水
			tblKnlSpnd.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());// 渠道日期
			tblKnlSpnd.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq());// 业务跟踪号
			tblKnlSpnd.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
			
			if (E_SPETTP.SPEND == input.getSpettp()) {// 资金交易类型
				tblKnlSpnd.setCapitp(E_CAPITP.OU401);// 消费
				
			} else {
				tblKnlSpnd.setCapitp(E_CAPITP.OU402);// 缴费
			}
			
			tblKnlSpnd.setOtidtp(cplToacct.getOtidtp());// 付款方证件类型
			tblKnlSpnd.setOtidno(tblKnlSpnd.getOtidno());// 付款方证件号
			tblKnlSpnd.setOtphno(cplToacct.getOtphno());// 付款方手机号
			tblKnlSpnd.setCorpno(cplToacct.getTocorp());// 付款方账户所属法人
			tblKnlSpnd.setTobrch(cplToacct.getTobrch());// 付款方账号所属机构
			tblKnlSpnd.setToacct(cplToacct.getToacct());// 付款方账号/卡号
			tblKnlSpnd.setToscac(cplToacct.getToscac());// 付款方电子账户
			tblKnlSpnd.setTocstp(input.getAcsetp());// 付款方子账号类型
			tblKnlSpnd.setToacno(input.getAcesno());// 付款方子账号
			tblKnlSpnd.setToname(input.getAcctnm());// 付款方客户名称
			tblKnlSpnd.setTranam(input.getTranam());// 交易金额
			tblKnlSpnd.setCrcycd(input.getCrcycd());// 币种
			tblKnlSpnd.setBusino(input.getBusino());// 业务编号
			tblKnlSpnd.setIncard(input.getIncard());// 收款方卡号/账号
			tblKnlSpnd.setInname(input.getInname());// 收款方客户名称
			tblKnlSpnd.setInbrch(input.getMebrch());// 收款方账户所属机构
			tblKnlSpnd.setIncorp(input.getIncorp());// 收款方账户所属法人
			tblKnlSpnd.setInbsno(input.getMercnm());// 收款方商户编号
			tblKnlSpnd.setIntrsq(input.getMercsq());// 商户流水
			tblKnlSpnd.setIntrdt(input.getMercdt());// 商户交易日期
			tblKnlSpnd.setIntrtm(input.getMerctm());// 商户交易时间
			tblKnlSpnd.setIoflag(E_IOFLAG.OUT);// 出入金标志
			tblKnlSpnd.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());// 处理码
			tblKnlSpnd.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 主交易流水
			tblKnlSpnd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
			tblKnlSpnd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());// 交易时间
			tblKnlSpnd.setStatus(E_TRANST.NORMAL);// 交易状态
			tblKnlSpnd.setRemark(null);// 备注信息
			tblKnlSpnd.setKeepdt(input.getKeepdt());// 清算日期
			tblKnlSpnd.setChckdt(input.getChckdt());// 对账日期
			tblKnlSpnd.setRevrsq(null);// 冲正流水
			tblKnlSpnd.setRevrdt(null);// 冲正日期
			if (cplToacct.getTobrch().equals(input.getIncorp())) {
				tblKnlSpnd.setIsspan(E_YES___.NO);// 是否跨法人
			} else {
				tblKnlSpnd.setIsspan(E_YES___.YES);// 是否跨法人
			}
			tblKnlSpnd.setIscler(E_YES___.NO);// 是否已清算

			KnlSpndDao.insert(tblKnlSpnd);

			// 电子账户退货
		} else if (E_SPETTP.REFUND == input.getSpettp()) {

			tblKnlSpnd = SysUtil.getInstance(KnlSpnd.class);
			tblKnlSpnd.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 主交易流水
			tblKnlSpnd.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());// 渠道日期
			tblKnlSpnd.setBusisq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 业务跟踪号
			tblKnlSpnd.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
			tblKnlSpnd.setCapitp(E_CAPITP.OU403);// 退货
			tblKnlSpnd.setTocstp(input.getAcsetp());// 付款方子账号类型
			tblKnlSpnd.setToacno(input.getAcesno());// 付款方子账号
			tblKnlSpnd.setToname(input.getAcctnm());// 付款方客户名称
			tblKnlSpnd.setTranam(input.getTranam());// 交易金额
			tblKnlSpnd.setCrcycd(input.getCrcycd());// 币种
			tblKnlSpnd.setBusino(input.getBusino());// 业务编号
			tblKnlSpnd.setIncard(input.getIncard());// 收款方卡号/账号
			tblKnlSpnd.setInname(input.getInname());// 收款方客户名称
			tblKnlSpnd.setInbrch(input.getMebrch());// 收款方账户所属机构
			tblKnlSpnd.setIncorp(input.getIncorp());// 收款方账户所属法人
			tblKnlSpnd.setInbsno(input.getMercnm());// 收款方商户编号
			tblKnlSpnd.setIntrsq(input.getMercsq());// 商户流水
			tblKnlSpnd.setIntrdt(input.getMercdt());// 商户交易日期
			tblKnlSpnd.setIntrtm(input.getMerctm());// 商户交易时间
			tblKnlSpnd.setIoflag(E_IOFLAG.IN);// 出入金标志
			tblKnlSpnd.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());// 处理码
			tblKnlSpnd.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 主交易流水
			tblKnlSpnd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
			tblKnlSpnd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());// 交易时间
			tblKnlSpnd.setStatus(E_TRANST.NORMAL);// 交易状态
			tblKnlSpnd.setRemark(null);// 备注信息
			tblKnlSpnd.setKeepdt(input.getKeepdt());// 清算日期
			tblKnlSpnd.setChckdt(input.getChckdt());// 对账日期
			tblKnlSpnd.setRevrsq(null);// 冲正流水
			tblKnlSpnd.setRevrdt(null);// 冲正日期
			tblKnlSpnd.setIsspan(E_YES___.NO);// 是否跨法人
			tblKnlSpnd.setIscler(E_YES___.NO);// 是否已清算
			tblKnlSpnd.setRetrsq(input.getRetrsq());// 原支付平台流水
			tblKnlSpnd.setRetrdt(input.getRetrdt());// 原支付平台日期

			KnlSpndDao.insert(tblKnlSpnd);
						
		}
		// 输出信息
		
		output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
		output.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 主交易流水
		output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());// 交易时间
		
		
		// 冲正注册
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		cplInput.setEvent1(tblKnlSpnd.getServsq());
		cplInput.setEvent2(tblKnlSpnd.getServdt());
		cplInput.setEvent3(input.getAcsetp().getValue()); //付款子账户类型
		cplInput.setEvent4(tblKnlSpnd.getToacno()); //付款方子账号
		if (input.getSpettp() == E_SPETTP.SPEND) { // 消费
			cplInput.setTranev(ApUtil.TRANS_EVENT_SPEND);
		} else if (input.getSpettp() == E_SPETTP.REFUND) { // 退货
			cplInput.setTranev(ApUtil.TRANS_EVENT_REFUND);
		} else { // 缴费
			cplInput.setTranev(ApUtil.TRANS_EVENT_PAY);
		}

		//ApStrike.regBook(cplInput);
		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
		apinput.setReversal_event_id(cplInput.getTranev());
		apinput.setInformation_value(SysUtil.serialize(cplInput));
		MsEvent.register(apinput, true);
	}

	@Override
	public void rptrDetlBill(InknbRptrDetl indptb) {
		// 
		KnbRptrDetl rptr = SysUtil.getInstance(KnbRptrDetl.class);
		rptr.setTransq(indptb.getTransq());
		rptr.setBusisq(indptb.getBusisq());
		rptr.setTrandt(indptb.getTrandt());
		rptr.setSoursq(indptb.getSoursq());
		rptr.setSourdt(indptb.getSourdt());
		rptr.setSournm(indptb.getSournm());
		rptr.setRptrtp(indptb.getRptrtp());
		rptr.setDeborg(indptb.getDeborg());
		rptr.setDecard(indptb.getDecard());
		rptr.setDebact(indptb.getDebact());
		rptr.setDecstp(indptb.getDecstp());
		rptr.setDeacct(indptb.getDeacct());
		rptr.setDebnam(indptb.getDebnam());
		rptr.setCrdorg(indptb.getCrdorg());
		rptr.setCrcard(indptb.getCrcard());
		rptr.setCrdact(indptb.getCrdact());
		rptr.setCrcstp(indptb.getCrcstp());
		rptr.setCracct(indptb.getCracct());
		rptr.setCrbnam(indptb.getCrbnam());
		rptr.setCrdnam(indptb.getCrdnam());
		rptr.setTranam(indptb.getTranam());
		rptr.setCrcycd(indptb.getCrcycd());
		rptr.setRpcode(indptb.getRpcode());
		rptr.setUserid(indptb.getUserid());
		rptr.setTranst(indptb.getTranst());
		rptr.setDescrb(indptb.getDescrb());
		rptr.setChckdt(indptb.getChckdt());
		rptr.setKeepdt(indptb.getKeepdt());
		rptr.setStady1(indptb.getStady1());
		rptr.setStady2(indptb.getStady2());
		rptr.setRemark(indptb.getRemark());
		KnbRptrDetlDao.insert(rptr);
		
		//注册冲账
		//冲正注册
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		cplInput.setEvent1(indptb.getTransq());
		cplInput.setEvent2(indptb.getTrandt());
		if(indptb.getRptrtp() == E_RPTRTP.RV201 || indptb.getRptrtp() == E_RPTRTP.RV202 
				|| indptb.getRptrtp() == E_RPTRTP.RV203){
			cplInput.setTranev(ApUtil.TRANS_EVENT_RECVRP);
		}else{
			cplInput.setTranev(ApUtil.TRANS_EVENT_SENDRP);
		}
		
		
		//ApStrike.regBook(cplInput);
		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
		apinput.setReversal_event_id(cplInput.getTranev());
		apinput.setInformation_value(SysUtil.serialize(cplInput));
		MsEvent.register(apinput, true);
		
	}

	/**
	 * 电子账户购买存款时登记订单信息
	 */
	@Override
	public void AccRegisterr(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill.AccRegister.Input input) {
		// 
		
		String cordno = input.getCordno();  //子订单号
		String chckdt = input.getChckdt();//对账日期
		String chcksq = input.getChcksq() ;//对账流水
		E_CORDST cordst = E_CORDST.SUCCESS;//订单状态赋值
		//BigDecimal sbtram = input.getSbtram();//订购金额
		BigDecimal sbtram = input.getTranam();//交易金额
	
		if(CommUtil.isNull(cordno)){
			throw CaError.Eacct.BNAS0020();
		}
	
		if(CommUtil.isNull(chckdt)){
			throw CaError.Eacct.BNAS0808();
		}
		if(CommUtil.isNull(chcksq)){
			throw CaError.Eacct.BNAS0810();
		}
		
		E_TRAYPE traype = null;
		if(CommUtil.isNotNull(input.getTraype())){
			traype = input.getTraype();//初始化
		}
		
		KnbRegi regi2 = KnbRegiDao.selectOne_db1(cordno,  false);
		if(CommUtil.isNotNull(regi2)){
			throw CaError.Eacct.BNAS1247();
		}
		KnbRegi regi1 = KnbRegiDao.selectOne_db2(chcksq, false);
		if(CommUtil.isNotNull(regi1)){
			throw CaError.Eacct.BNAS0809();
		}
		
		KnbRegi regi = SysUtil.getInstance(KnbRegi.class);
		regi.setCocrdt(CommTools.getBaseRunEnvs().getTrxn_date());
		regi.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		regi.setCordno(cordno);
		regi.setCordst(E_CORDST.get(cordst));
		regi.setSbtram(sbtram);
		regi.setChckdt(chckdt);
		regi.setChcksq(chcksq);
		regi.setTraype(traype);
		regi.setCardno(input.getCardno());
		regi.setProdcd(input.getProdcd());
		KnbRegiDao.insert(regi);
		
		}

		@Override
		public void saveCupstr(IoDpKnlIoblCups knlIoblCups, E_YES___ istrcf) {
			//登记银联CUPS转入登记簿
			KnlIoblCups cups = SysUtil.getInstance(KnlIoblCups.class);
			cups.setAuthno(knlIoblCups.getAuthno());
//			cups.setBusino(knlIoblCups.getBusino()); //商户代码
//			cups.setBusitp(knlIoblCups.getBusitp());
			cups.setCardno(knlIoblCups.getCardno());
//			cups.setCnkpdt(knlIoblCups.getCnkpdt()); //核心对账日期
			cups.setCrcycd(knlIoblCups.getCrcycd()); //币种
//			cups.setInacct(knlIoblCups.getInacct()); //转入账号，电子账号卡号
//			cups.setInacna(knlIoblCups.getInacna());  //户名
//			cups.setMesstp(knlIoblCups.getMesstp()); //报文类型
			cups.setMntrsq(knlIoblCups.getMntrsq());
//			cups.setOtacct(knlIoblCups.getOtacct());	//转出账号
//			cups.setOtacna(knlIoblCups.getOtacna()); //转出户名
			cups.setPrepdt(knlIoblCups.getPrepdt()); //银联前置日期
			cups.setPrepsq(knlIoblCups.getPrepsq());	//银联前置流水
			cups.setProccd(knlIoblCups.getProccd()); //银联处里面
//			cups.setReprsq(knlIoblCups.getReprsq()); //原前置流水号
//			cups.setResssq(knlIoblCups.getResssq()); //原系统跟踪号
//			cups.setRetrdt(knlIoblCups.getRetrdt()); //原交易日期时间
//			cups.setServsq(knlIoblCups.getServsq()); //渠道流水
//			cups.setServtp(knlIoblCups.getServtp()); //渠道
//			cups.setSpared(knlIoblCups.getSpared()); //备用
//			cups.setStand1(knlIoblCups.getStand1());
//			cups.setStand2(knlIoblCups.getStand2());
			cups.setTranam(knlIoblCups.getTranam());
			cups.setTrandt(knlIoblCups.getTrandt());
//			cups.setTranst(knlIoblCups.getTranst());
			
//			cups.setPrdate(knlIoblCups.getPrdate());
//			cups.setPrbrmk(knlIoblCups.getPrbrmk());
//			cups.setTrbrmk(knlIoblCups.getTrbrmk());
			
//			cups.setTrbrch(knlIoblCups.getTrbrch()); //交易受理机构 tranbr
//			cups.setTrcode(knlIoblCups.getTrcode()); //银联交易码
			cups.setUniseq(knlIoblCups.getUniseq()); //银联流水
			cups.setUnkpdt(knlIoblCups.getUnkpdt()); //银联清算日期
//			cups.setDescrb(knlIoblCups.getDescrb());
			
			KnlIoblCupsDao.insert(cups);
			
			
			//注册银联来账登记簿冲正事件
			IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
			cplInput.setCustac(knlIoblCups.getCardno());
			cplInput.setEvent1(knlIoblCups.getMntrsq());
			cplInput.setEvent2(knlIoblCups.getTrandt());
			cplInput.setEvent3(istrcf.getValue()); //是否是银联转入确认交易
			cplInput.setTranev(ApUtil.TRANS_EVENT_CUPSTR);
			//ApStrike.regBook(cplInput);
    		IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);    		
    		apinput.setReversal_event_id(cplInput.getTranev());
    		apinput.setInformation_value(SysUtil.serialize(cplInput));
    		MsEvent.register(apinput, true);
			
		}

		public void SaveActoacPeer( final cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill.SaveActoacPeer.Input input){
			
			KnlCary cary2 = ActoacDao.selKnlCary(CommTools.getBaseRunEnvs().getMain_trxn_seq(), CommTools.getBaseRunEnvs().getTrxn_date(), false);
			if(CommUtil.isNotNull(cary2)){
				return;
			}
			
			KnlCary cary = SysUtil.getInstance(KnlCary.class);
			
			cary.setCardno(input.getCardno());
			cary.setCrcycd(input.getCrcycd());
			cary.setFromtp(input.getFromtp());
			cary.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());
			cary.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());
			cary.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			// ************mdy by zhanga 20161227***********
			cary.setTrantp(input.getTrantp());
			if(input.getTrantp() == E_TRNESS.T02 || input.getTrantp() == E_TRNESS.T03){ //非实时转账记录为待处理
				cary.setStatus(E_TRANST.WAIT); 
				cary.setAcdate(input.getAcdate());
				cary.setActime(input.getActime());
			}else{
				cary.setStatus(CommUtil.nvl(input.getStatus(), E_TRANST.NORMAL)); //默认正常，为ChinaPay接口考虑
			}
			cary.setTranam(input.getTranam());
			cary.setRemark(input.getRemark());
			cary.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());
			cary.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());
			cary.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			cary.setCuacno(input.getCuacno());
			cary.setIoflag(input.getIoflag());
			cary.setKeepdt(input.getKeepdt());//清算时间
			cary.setFromtp(E_FROMTP.A);
			cary.setCapitp(E_CAPITP.NT301);
			cary.setToacno(input.getToacno());
			cary.setToscac(input.getToscac());
			cary.setBusino(input.getBusino());
			cary.setAcctno(input.getAcctno()); //子账号
			cary.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq()); //业务跟踪编号
			cary.setChckdt(input.getChckdt()); //对账日期
			cary.setBrchno(input.getBrchno()); //电子账户所属机构
			cary.setTobrch(input.getTobrch()); //对方账号所属机构
			cary.setTlcgam(input.getTlcgam()); //收费总金额
			cary.setToacct(input.getToacct()); //对方账号
			cary.setIncorp(input.getIncorp()); //转入方法人
			cary.setTocorp(input.getTocorp()); //转出方法人
			cary.setIscler(E_YES___.NO); //是否已清算
			cary.setIsspan(input.getIsspan()); //是否跨法人
			cary.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); //渠道
			KnlCaryDao.insert(cary);
			
			//注册冲账
			IoMsRegEvent ioMsReg = SysUtil.getInstance(IoMsRegEvent.class);
			ioMsReg.setCall_out_seq(CommTools.getBaseRunEnvs().getCall_out_seq());//外调流水
			ioMsReg.setConfirm_event_id("");//二提交事件id
			ioMsReg.setEvent_status(E_EVNTST.SUCCESS);//事件状态SUCCESS（成功）STRIKED（已冲正）NEED2C（需要二次提交）
			ioMsReg.setInformation_value(SysUtil.serialize(cary));//冲正信息值
	    	ioMsReg.setReversal_event_id("saveActoacPeer");//冲正事件ID
	    	ioMsReg.setService_id("saveActoacPeer");//服务ID
			ioMsReg.setSub_system_id("01002");//子系统ID
			ioMsReg.setTarget_dcn(CoreUtil.getCurrentShardingId());//目标DCB编号
			ioMsReg.setTarget_org_id("025");//目标法人
			ioMsReg.setTxn_event_level(E_EVENTLEVEL.INQUIRE);//教义事件级别NORMAL（）INQUIRE（）LOCAL（）CRDIT（）
			ioMsReg.setIs_across_dcn(E_YESORNO.NO);
			MsEvent.register(ioMsReg, true);
		}
		
		

		/**
		 * 会计流水清算登记
		 */
		@Override
		public void SaveKnlAcsqCler(IoAccountClearInfo clerinfo) {
			String trandt = clerinfo.getTrandt();//交易日期      
			String mntrsq = clerinfo.getMntrsq();//主交易流水    
//			Long recdno = clerinfo.getRecdno();//记录次序号    
			String acctno = clerinfo.getAcctno();//账号          
			String acctna = clerinfo.getAcctna();//账户名称      
			String prodcd = clerinfo.getProdcd();//产品编号      
			E_CLACTP clactp = clerinfo.getClactp();//系统内账号类型
//			String toacct = clerinfo.getToacct();//对方账号      
//			String toacbr = clerinfo.getToacbr();//对方机构号    
			String acctbr = clerinfo.getAcctbr();//账务机构      
			String crcycd = clerinfo.getCrcycd();//币种          
			E_AMNTCD amntcd = clerinfo.getAmntcd();//借贷标志      
			BigDecimal tranam = clerinfo.getTranam();//交易金额      
			String servtp = clerinfo.getServtp();//交易渠道      
			String clerdt = clerinfo.getClerdt();//清算日期      
			Integer clenum = clerinfo.getClenum();//清算场次      
//			E_CLERST clerst = clerinfo.getClerst();//数据同步标志
			
			if(CommUtil.isNull(acctbr)){
				throw DpModuleError.DpstComm.E9999("登记清算时，机构不能为空");
			}
			if(CommUtil.isNull(crcycd)){
				throw DpModuleError.DpstComm.E9999("登记清算时，币种不能为空");
			}
			
			if (CommUtil.isNull(trandt)) {
	        	trandt = CommTools.getBaseRunEnvs().getTrxn_date();
	        }
			if (CommUtil.isNull(mntrsq)) {
	        	mntrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
	        }
	        if (CommUtil.isNull(servtp)) {
	            servtp = CommTools.getBaseRunEnvs().getChannel_id();
	        }
	        if (CommUtil.isNull(clerdt)) {
	        	clerdt = BusiTools.getBusiRunEnvs().getClerdt();//清算日期
	        }
	        if (CommUtil.isNull(clenum)) {
	        	clenum = BusiTools.getBusiRunEnvs().getClenum();//清算场次
	        }
	        
	        //对方账户 固定为 系统往来户
	        KnpParameter para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","lzbank", "08", "%", true);
			IoGlKnaAcct glacct = SysUtil.getInstance(IoCheckBalance.class)
					.getGlKnaAcct(para.getParm_value1(), acctbr, crcycd, para.getParm_value2());
	        
			
			KnsAcsqCler entity = SysUtil.getInstance(KnsAcsqCler.class);
			entity.setTrandt(trandt);//交易日期      
			entity.setMntrsq(mntrsq);//主交易流水    
			//entity.setRecdno(Long.parseLong(SequenceManager.nextval("KnsAcsqCler")));//记录次序号    
			entity.setRecdno(Long.parseLong(CoreUtil.nextValue("KnsAcsqCler")));//记录次序号    
			entity.setAcctno(acctno);//账号          
			entity.setAcctna(acctna);//账户名称      
			entity.setProdcd(prodcd);//产品编号      
			entity.setClactp(clactp);//系统内账号类型
			entity.setToacct(glacct.getAcctno());//对方账号      
			entity.setToacbr(glacct.getBrchno());//对方机构号    
			entity.setAcctbr(acctbr);//账务机构      
			entity.setCrcycd(crcycd);//币种          
			entity.setAmntcd(amntcd);//借贷标志      
			entity.setTranam(tranam);//交易金额      
			entity.setServtp(servtp);//交易渠道      
			entity.setClerdt(clerdt);//清算日期      
			entity.setClenum(clenum);//清算场次      
			entity.setClerst(E_CLERST.WAIT);//数据同步标志  
			entity.setTranst(E_TRANST.NORMAL);//交易状态
			KnsAcsqClerDao.insert(entity);	        
		}

		@Override
		public void SaveActoacPeerReversal(IoKnlCary ioknlcary) {
			
			KnlCary cary = SysUtil.getInstance(KnlCary.class);
			
			CommUtil.copyProperties(cary, ioknlcary);
			
			KnlCaryDao.insert(cary);
			
			//注册冲账
			IoMsRegEvent ioMsReg = SysUtil.getInstance(IoMsRegEvent.class);
			ioMsReg.setCall_out_seq(CommTools.getBranchSeq());//外调流水
			ioMsReg.setConfirm_event_id("");//二提交事件id
			ioMsReg.setEvent_status(E_EVNTST.SUCCESS);//事件状态SUCCESS（成功）STRIKED（已冲正）NEED2C（需要二次提交）
			ioMsReg.setInformation_value(SysUtil.serialize(cary));//冲正信息值
	    	ioMsReg.setReversal_event_id("saveActoacPeer");//冲正事件ID
	    	ioMsReg.setService_id("saveActoacPeer");//服务ID
			ioMsReg.setSub_system_id("01002");//子系统ID
			ioMsReg.setTarget_dcn(CoreUtil.getCurrentShardingId());//目标DCB编号
			ioMsReg.setTarget_org_id("025");//目标法人
			ioMsReg.setTxn_event_level(E_EVENTLEVEL.INQUIRE);//教义事件级别NORMAL（）INQUIRE（）LOCAL（）CRDIT（）
			ioMsReg.setIs_across_dcn(E_YESORNO.NO);
			MsEvent.register(ioMsReg, true);
		}

	@Override
	public void SaveKnlAcsq(IoAccounttingIntf cplIoAccounttingIntf, E_BLNCDN busidn) {
		E_AMNTCD amntcd = cplIoAccounttingIntf.getAmntcd();
		cplIoAccounttingIntf.getBltype();

		//登记交易信息，供总账解析
		if (CommUtil.equals("1", KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%", true).getParm_value1())) {
			KnpParameter para1 = SysUtil.getInstance(KnpParameter.class);
			para1 = KnpParameterDao.selectOne_odb1("GlAnalysis", "1010000", "%", "%", true);//产品增加
			KnpParameter para2 = SysUtil.getInstance(KnpParameter.class);
			para2 = KnpParameterDao.selectOne_odb1("GlAnalysis", "1020000", "%", "%", true);//产品减少
			if (amntcd == E_AMNTCD.DR && busidn == E_BLNCDN.D) {
				cplIoAccounttingIntf.setTranms(para1.getParm_value1());//产品增加
			}
			else if (amntcd == E_AMNTCD.DR && busidn == E_BLNCDN.C) {
				cplIoAccounttingIntf.setTranms(para2.getParm_value1());//产品减少
			}
			else if (amntcd == E_AMNTCD.CR && busidn == E_BLNCDN.C) {
				cplIoAccounttingIntf.setTranms(para1.getParm_value1());//产品增加
			}
			else if (amntcd == E_AMNTCD.CR && busidn == E_BLNCDN.D) {
				cplIoAccounttingIntf.setTranms(para2.getParm_value1());//产品减少
			}
			else if (amntcd == E_AMNTCD.RV && busidn == E_BLNCDN.R) {
				cplIoAccounttingIntf.setTranms(para1.getParm_value1());//产品增加
			}
			else if (amntcd == E_AMNTCD.PY && busidn == E_BLNCDN.R) {
				cplIoAccounttingIntf.setTranms(para2.getParm_value1());//产品减少
			}
			else if (amntcd == E_AMNTCD.DR && busidn == E_BLNCDN.Z) {
				cplIoAccounttingIntf.setTranms(para1.getParm_value1());//产品增加
			}
			else if (amntcd == E_AMNTCD.CR && busidn == E_BLNCDN.Z) {
				cplIoAccounttingIntf.setTranms(para2.getParm_value1());//产品减少
			}
			else if (amntcd == E_AMNTCD.DR && busidn == E_BLNCDN.B) {
				cplIoAccounttingIntf.setTranms(para1.getParm_value1());//产品增加
			}
			else if (amntcd == E_AMNTCD.CR && busidn == E_BLNCDN.B) {
				cplIoAccounttingIntf.setTranms(para2.getParm_value1());//产品减少
			}
			else {
				throw InError.comm.E0003("账户余额方向错误");
			}
		}
		cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());//渠道       

		SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);

		/**
		 * 冲正登记
		 */
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
		cplInput.setCustac(cplIoAccounttingIntf.getDtitcd()); // 电子账户号
		cplInput.setTranac(cplIoAccounttingIntf.getAcctno()); // 负债账号
		cplInput.setTranam(cplIoAccounttingIntf.getTranam());// 交易金额
		cplInput.setAmntcd(cplIoAccounttingIntf.getAmntcd());// 借贷标志
		cplInput.setCrcycd(cplIoAccounttingIntf.getCrcycd());// 货币代号

		cplInput.setEvent1(cplIoAccounttingIntf.getTransq());
		cplInput.setEvent2(cplIoAccounttingIntf.getTrandt());

		IoMsRegEvent ioMsReg = SysUtil.getInstance(IoMsRegEvent.class);
		ioMsReg.setEvent_status(E_EVNTST.SUCCESS);//事件状态SUCCESS（成功）STRIKED（已冲正）NEED2C（需要二次提交）
		ioMsReg.setInformation_value(SysUtil.serialize(cplInput));
		ioMsReg.setReversal_event_id("strkeSaveKnsAcsq");//冲正事件ID
		ioMsReg.setService_id("strkeSaveKnsAcsq");//服务ID
		ioMsReg.setSub_system_id(CoreUtil.getSubSystemId());//子系统ID
		ioMsReg.setTxn_event_level(E_EVENTLEVEL.LOCAL);//教义事件级别NORMAL（）INQUIRE（）LOCAL（）CRDIT（）
		ioMsReg.setIs_across_dcn(E_YESORNO.NO);

		MsEvent.register(ioMsReg, true);
	}
}


