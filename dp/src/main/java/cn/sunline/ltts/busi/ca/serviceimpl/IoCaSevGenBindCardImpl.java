package cn.sunline.ltts.busi.ca.serviceimpl;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUSTTP;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.base.DecryptConstant;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaadDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbCacq;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbCacqDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevGenBindCard;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCacd;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypeGenBindCard.IoCaBindCardInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypeGenBindCard.IoCaSrvGenBindCard;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BACATP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BDCART;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MANTWY;


/**
 * 电子账户绑卡服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoCaSevGenBindCard", longname = "电子账户绑卡服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoCaSevGenBindCardImpl implements
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevGenBindCard {
	/**
	 * 
	 * @author xiongzhao
	 *         <p>
	 *         <li>2016年6月21日-下午7:05:13</li>
	 *         <li>功能描述：电子账户绑卡服务</li>
	 *         </p>
	 * 
	 * @param caBindCard
	 */
	public void addBindCard(final cn.sunline.ltts.busi.iobus.type.dp.IoCaTypeGenBindCard.IoCaSrvGenBindCard caBindCard, final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevGenBindCard.IoCaSevGenBindCar.Output output) {

		// 结算卡卡号
		String cdopac = caBindCard.getCdopac(); 
		// 结算卡账户名称
		String cdopna = caBindCard.getCdopna(); 
		// 结算卡开户行行号
		String opbrch = caBindCard.getOpbrch();
		// 结算卡开户行名称
	    String brchna = caBindCard.getBrchna();
	    // 结算卡类型
		E_BACATP cardtp = caBindCard.getCardtp(); 
		// 电子账号
	    String custac = caBindCard.getCustac();
	    // 绑卡认证结果
	    E_BDCART bdcart = caBindCard.getBdcart();
	    // 检查状态字标识
	    E_YES___ isckfg = E_YES___.NO;
	    // 证件姓名
	    String idtfna = caBindCard.getIdtfna();
	    // 证件类型
	    E_IDTFTP idtftp = caBindCard.getIdtftp();
	    // 证件号码
	    String idtfno = caBindCard.getIdtfno();
	    // 结算卡银行预留手机号
	    String bankph = caBindCard.getBankph();
	    // 默认卡标识
	    E_YES___ isdflt = caBindCard.getIsdflt();
	    // 主账户开户标识
	    String mactid = caBindCard.getMactid();
	    // 子账户开户标识
	    String acctid = caBindCard.getAcctid();
	    // 绑定账户标识
	    E_ACCTFG acbdtp = caBindCard.getAcbdtp();
	    // 结算卡对公对私标识
	    E_CUSTTP copefg = caBindCard.getCopefg();
	    
	    // 绑定账户标识
	    if(CommUtil.isNull(acbdtp)) {
	    	throw DpModuleError.DpAcct.AT010027();
	    }
	    // 绑定子账户时，子账户开户标识不能为空。
	    if(E_ACCTFG.SUBACCT == acbdtp) {
	    	// 子账户开户标识
	    	if(CommUtil.isNull(acctid)) {
	    		throw DpModuleError.DpAcct.AT010026();
	    	}
	    }
	    
	    // 对公对私结算卡标识
	    if(CommUtil.isNull(copefg)) {
	    	throw DpModuleError.DpAcct.AT010008();
	    }
	    // 对私结算卡四要素信息必输。
	    if(copefg == E_CUSTTP.PSON) {
	    	// 证件姓名
	    	if(CommUtil.isNull(idtfna)) {
	    		throw DpModuleError.DpAcct.AT010018();
	    	}
	    	// 证件类型、证件号码
	    	if(CommUtil.isNull(idtftp) || CommUtil.isNull(idtfno)) {
	    		throw DpModuleError.DpAcct.AT010019();
	    	}
	    	// 结算卡银行预留手机号
	    	if(CommUtil.isNull(bankph)) {
	    		throw DpModuleError.DpAcct.AT010020();
	    	}
	    }
	    
	    // 结算卡账户类型
	    if (CommUtil.isNull(cardtp)) {
	    	throw CaError.Eacct.BNAS1108();
	    }
	    // 仅支持绑定借记卡
	    if(CaEnumType.E_BACATP.DEPOSIT != cardtp) {
	    	throw DpModuleError.DpAcct.AT020022();
	    }
	    
	    // 主账户开户标识
	    if(CommUtil.isNull(mactid)) {
	    	throw DpModuleError.DpAcct.AT010013();
	    }
	    // 默认卡标识
	    if(CommUtil.isNull(isdflt)) {
	    	throw DpModuleError.DpAcct.AT010021();
	    }
	    // 检查状态字标识
	    if (CommUtil.isNotNull(caBindCard.getIsckfg())) {
	    	isckfg = caBindCard.getIsckfg();
	    }
	    // 结算卡开户行号
	    if (CommUtil.isNull(opbrch)) {
	    	throw CaError.Eacct.BNAS0179();
	    }
	    // 结算卡开户行名称
	    if (CommUtil.isNull(brchna)) {
	    	throw CaError.Eacct.BNAS0178();
	    }
	    // 结算卡卡号
	    if (CommUtil.isNull(cdopac)) {
	    	throw CaError.Eacct.BNAS1112();
	    }
	    // 结算卡账户名称
	    if (CommUtil.isNull(cdopna)) {
	    	throw CaError.Eacct.BNAS1107();
	    }
	    // 绑卡认证结果
	    if (CommUtil.isNull(bdcart)) {
	    	throw CaError.Eacct.BNAS1106();
	    }

		// 检查电子主账户信息是否存在。
		KnaMaad tblKnaMaad = KnaMaadDao.selectOne_odb2(mactid, false);
		if(CommUtil.isNull(tblKnaMaad)) {
			throw DpModuleError.DpAcct.AT020028();
		}
		custac = tblKnaMaad.getCustac();
		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectFirst_odb3(custac, false);
		if(CommUtil.isNull(tblKnaAcdc)) {
			throw CaError.Eacct.BNAS0393();
		}
		output.setCardno(tblKnaAcdc.getCardno());
	
		// 查询电子账户表信息。
		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(custac, false);
		if (CommUtil.isNull(tblKnaCust))
			throw CaError.Eacct.BNAS0393();
		// 检查电子账户状态：正常和未生效都允许绑卡。
		if (E_ACCTST.NORMAL != tblKnaCust.getAcctst() && E_ACCTST.INVALID != tblKnaCust.getAcctst()) {
			throw CaError.Eacct.BNAS0741(tblKnaCust.getCuacst().getLongName());
		}
		
		// 绑定账号。
		String acbdno = "";
		if(acbdtp == E_ACCTFG.MASTERACCT) {
			// 绑定电子账号。
			acbdno = custac;
		}else if(acbdtp == E_ACCTFG.SUBACCT) {
			// 检查电子账号是否一致。
			KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(acctid, false);
			if(CommUtil.isNull(tblKnaSbad)) {
				throw DpModuleError.DpAcct.AT020010();
			}
			if(!CommUtil.equals(tblKnaSbad.getCustac(), custac)) {
				throw DpModuleError.DpAcct.AT020023(tblKnaAcdc.getCardno(), acctid);
			}
			// 绑定子账户号。
			acbdno  = tblKnaSbad.getAcctno();
		}
		output.setAcbdno(acbdno);
		
		if (isckfg == E_YES___.YES) {
			// 校验电子账号的状态
			E_CUACST eCuacst = SysUtil.getInstance(
					IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
			if (eCuacst != E_CUACST.NORMAL && eCuacst != E_CUACST.DORMANT
					&& eCuacst != E_CUACST.PRECLOS) {
				throw CaError.Eacct.BNAS0441();
			}

			// 检验电子账户的状态字
			IoDpAcStatusWord cplGetAcStWord = SysUtil.getInstance(
					IoDpFrozSvcType.class).getAcStatusWord(custac);
			if (cplGetAcStWord.getClstop() == E_YES___.YES) {
				throw CaError.Eacct.BNAS0441();
			}
		}
		
		// 绑卡信息新增标识。
		boolean adcdfg = false;
		// 查询结算卡绑定信息。
		KnaCacd tblKnaCacd = KnaCacdDao.selectOne_odb1(acbdno, cdopac, false);
		if(CommUtil.isNull(tblKnaCacd)) {
			// 新增绑卡信息。
			adcdfg = true;
			tblKnaCacd = SysUtil.getInstance(KnaCacd.class);
			tblKnaCacd.setAcbdno(acbdno);// 绑定账号
			tblKnaCacd.setCdopac(cdopac);// 结算卡卡号
			tblKnaCacd.setTmcdopac(DecryptConstant.maskBankCard(cdopac));// 结算卡卡号脱敏
		}else {
			if(tblKnaCacd.getStatus() == E_DPACST.NORMAL || tblKnaCacd.getStatus() == E_DPACST.DEFAULT) {
				//throw DpModuleError.DpAcct.AT020048(cdopac);
				adcdfg = false;
			}
		}
		tblKnaCacd.setOpbrch(opbrch);// 结算卡账户开户行号
		tblKnaCacd.setAcctna(cdopna);// 结算卡账户名称
		tblKnaCacd.setTmacctna(DecryptConstant.maskName(cdopna));// 结算卡账户名称
		tblKnaCacd.setBrchna(brchna);// 结算卡账户开户行名称
		tblKnaCacd.setCardtp(cardtp);// 结算卡账户类型
		tblKnaCacd.setBinddt(CommTools.getBaseRunEnvs().getTrxn_date());// 绑定日期
		tblKnaCacd.setBdcatp(caBindCard.getBdcatp());// 结算卡账户分类
		tblKnaCacd.setCopefg(copefg);// 结算卡对公对私标识
		tblKnaCacd.setIdtfna(idtfna);// 证件姓名
		tblKnaCacd.setTmidtfna(DecryptConstant.maskName(idtfna));// 证件姓名
		tblKnaCacd.setIdtftp(idtftp);// 证件类型
		tblKnaCacd.setIdtfno(idtfno);// 证件号码
		tblKnaCacd.setTmidtfno(DecryptConstant.maskIdCard(idtfno));// 证件号码
		tblKnaCacd.setBankph(bankph);// 结算卡银行预留手机号
		tblKnaCacd.setTmbankph(DecryptConstant.maskMobile(bankph));// 结算卡银行预留手机号
		tblKnaCacd.setAcbdtp(acbdtp);// 绑定账户标识
		tblKnaCacd.setCustac(custac);// 电子账号
		// 绑卡状态设置。
		if (bdcart == E_BDCART.CHECKING) {
			tblKnaCacd.setStatus(BaseEnumType.E_DPACST.SLEEP);
		} else if (bdcart == E_BDCART.SUCCESS) {
			// 根据默认卡标识设置状态。
			if(E_YES___.YES == isdflt) {
				// 设置已绑定结算卡默认卡状态为正常状态：确保设置默认卡时，只存在一张默认卡。
				List<KnaCacd> lsKnaCacd = KnaCacdDao.selectAll_odb3(acbdno, E_DPACST.DEFAULT , false);
				if(lsKnaCacd.size() > 0) {
					for(KnaCacd cardInfo : lsKnaCacd) {
//						cardInfo.setStatus(E_DPACST.NORMAL);
						cardInfo.setStatus(E_DPACST.CLOSE);
						KnaCacdDao.updateOne_odb1(cardInfo);
					}
				}
				tblKnaCacd.setStatus(E_DPACST.DEFAULT); 

			}else if(E_YES___.NO == isdflt){
				tblKnaCacd.setStatus(E_DPACST.NORMAL); 
			}

		} else {
			throw CaError.Eacct.BNAS1105();
		}

		if (adcdfg) {
			KnaCacdDao.insert(tblKnaCacd);
			// 维护方式为新增
			output.setMantwy(E_MANTWY.ADD);
		}else{
			/*
			 * Modify：绑卡信息存在，直接更新绑卡信息。
						// 存在状态正常的绑卡信息，抛错。
						if (E_DPACST.NORMAL == cacd.getStatus() || E_DPACST.DEFAULT == cacd.getStatus()) {
							throw CaError.Eacct.BNAS0708();
							// 存在状态异常的绑卡信息，更新。
						} else if (E_DPACST.SLEEP == cacd.getStatus() || E_DPACST.CLOSE == cacd.getStatus()) {}
			 */
			KnaCacdDao.updateOne_odb1(tblKnaCacd);

		} 
		/*
		 * JF Delete：注释。
		//add by xieqq 20170729  An account can bind cards with an upper limit of 10
		   List<KnaCacd> cacds = KnaCacdDao.selectAll_odb7(sCustac,E_DPACST.NORMAL, false);
		   KnpParameter KnpParameter = KnpParameterDao.selectOne_odb1("BindCardLimit", "%", "%","%", false);
		   if(cacds.size()>Integer.valueOf(KnpParameter.getParm_value1())){
			   throw CaError.Eacct.BNAS1143(cardno,cacds.size(),KnpParameter.getParm_value1());
		   }
		 */
	}

	/**
	 * 
	 * @author xiongzhao
	 *         <p>
	 *         <li>2016年6月21日-下午7:06:05</li>
	 *         <li>功能描述：解除电子账户绑卡服务</li>
	 *         </p>
	 * 
	 * @param cdopac
	 * @param cardno
	 */

	public void removeBindCar(String cardno, String odopac, String cdopac) {

		String custac = null;
		String timetm=DateTools2.getCurrentTimestamp();
		// 判断输入的参数：解绑账户不能为空
		if (CommUtil.isNull(odopac)) {
			throw CaError.Eacct.BNAS0589();
		}
		// 判断输入的参数：电子账户不能为空
		if (CommUtil.isNull(cardno)) {
			throw DpModuleError.DpstProd.BNAS0926();
		}

		/*
		 KnaCust cust = KnaCustDao.selectOne_odb1(custac, true); 
		 if (E_ACCTST.SLEEP == cust.getAcctst()) { 
			 throw CaError.Eacct.E0001("电子账户已冻结,解绑失败!"); 
			 }
		 */
		
		// 根据电子账号获取电子账号ID
		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(cardno, false);
		if (CommUtil.isNull(tblKnaAcdc)) {
			throw CaError.Eacct.BNAS1279();
		}
		custac = tblKnaAcdc.getCustac();
		
		// 获得外部卡与客户账号对照表中的记录
		KnaCacd tblKnaCacd = KnaCacdDao.selectOne_odb1(custac, odopac, false);
		if (CommUtil.isNull(tblKnaCacd)) {
			throw CaError.Eacct.BNAS0765();
		}
		
		// 获取电子账户表中记录
		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(custac, false);
		if (CommUtil.isNull(tblKnaCust)) {
			throw CaError.Eacct.BNAS1279();
		}
		
		// 校验电子账号的状态
		// 删除&& eCuacst != E_CUACST.PRECLOS 根据需求预销户不能解绑  modify by lull
		E_CUACST eCuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);

		if (eCuacst != E_CUACST.NORMAL && eCuacst != E_CUACST.NOACTIVE && eCuacst != E_CUACST.DORMANT) {
			throw CaError.Eacct.BNAS0441();

		}
		
		// 检验电子账户的状态字
		IoDpAcStatusWord cplGetAcStWord = SysUtil.getInstance(
				IoDpFrozSvcType.class).getAcStatusWord(custac);
		if (cplGetAcStWord.getClstop() == E_YES___.YES) {
			throw CaError.Eacct.BNAS0441();
		}

		// 查询账户分类
		E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
				.qryAccatpByCustac(custac);
		
		// 检查二类户，三类户是否有认证通过的绑定卡
		if (eAccatp == E_ACCATP.FINANCE || eAccatp == E_ACCATP.WALLET) {

			List<KnaCacd> lstKnaCacd = KnaCacdDao.selectAll_odb3(custac,
					E_DPACST.NORMAL, false);

			if (lstKnaCacd.size() == 0) {
				throw CaError.Eacct.BNAS1639();
			}
			
			// 即富无此业务限制，一张卡也可以解绑
//			if(lstKnaCacd.size() == 1){
//				throw CaError.Eacct.E0001("该用户只绑定了一张卡，不能解除，理财功能户，电子钱包户必须绑定一张认证通过的账户,");
//			}

		}
		
		// 判断该卡状态为关闭状态，解绑失败
		if (E_DPACST.CLOSE == tblKnaCacd.getStatus()) {
			throw CaError.Eacct.BNAS0765();
		}
		
		// 判断该卡状态为异常，解绑失败
		if (E_DPACST.SLEEP == tblKnaCacd.getStatus()) {
			throw CaError.Eacct.BNAS0723();
		}
		CaDao.updateBindCaStatus(custac, odopac, timetm);

	}

	@Override
	public IoCaKnaCacd selBindByCard(String custac, String cardno,
			E_DPACST status, Boolean isable) {
		KnaCacd cacd = CaDao.selKnaCacdByCcs(custac, cardno, status, false);
		IoCaKnaCacd dest = SysUtil.getInstance(IoCaKnaCacd.class);
		if (CommUtil.isNotNull(cacd)) {
			CommUtil.copyProperties(dest, cacd);
			return dest;
		}
		return null;
	}

	/**
	 * 
	 * @author xiongzhao
	 * 		<p>
	 *	    <li>2016年6月30日-下午4:22:11</li>
	 *      <li>功能描述：查询电子账户绑卡信息</li>
	 *      </p>
	 * 
	 * @param custac
	 * @param cardno
	 * @param cardtp
	 * @return
	 */

	public Options<IoCaBindCardInfo> selBindCarInfo(String custac,
			String cardno, E_BACATP cardtp) {

		// 输入参数校验
		if (CommUtil.isNull(custac)) {
			if (CommUtil.isNull(cardno)) {
				throw DpModuleError.DpstComm.BNAS0901();
			}
			// 根据电子账号查询出电子账号ID
			KnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCardno(cardno, E_DPACST.NORMAL, false);
			if (CommUtil.isNull(tblKnaAcdc)) {
				throw CaError.Eacct.BNAS0393();
			}
//			CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaAcdc.getCorpno());
			custac = tblKnaAcdc.getCustac();
		}
		
		//	根据电子账号查询电子账户表
		KnaCust tblKnaCust = CaDao.selKnaCustByCustac(custac, false);
		if (CommUtil.isNull(tblKnaCust)) {
			throw CaError.Eacct.BNAS0393();
		}
		
		// 更新APP推送ID
		/*
		if (CommUtil.isNotNull(CommTools.getBaseRunEnvs().getAppsid())) {
			
			IoCifCustAccs cust_accs = SysUtil.getInstance(IoCuSevQryTableInfo.class).cif_cust_accsByCustno(tblKnaCust.getCustno(), false, E_STATUS.NORMAL);
			if(CommUtil.isNull(cust_accs)){
				throw CaError.Eacct.E0001("请输入有效电子账户！");
			}else{
				if(CommUtil.isNotNull(cust_accs.getAppsid())){
					if (!CommUtil.equals(cust_accs.getAppsid(), CommTools.getBaseRunEnvs().getAppsid())){
						CaDao.updCifCustAccsByCustno(CommTools.getBaseRunEnvs().getAppsid(), tblKnaCust.getCustno(),E_STATUS.NORMAL,CommTools.getBaseRunEnvs().getTrxn_date());
					}
					
				}else{
					CaDao.updCifCustAccsByCustno(CommTools.getBaseRunEnvs().getAppsid(), tblKnaCust.getCustno(),E_STATUS.NORMAL,CommTools.getBaseRunEnvs().getTrxn_date());
				}
				
			}
		}
		*/
		
		// 查询电子账户绑定信息，查询状态为正常的
		E_DPACST status = E_DPACST.NORMAL;

		if (CommUtil.isNull(cardtp)) {
			List<KnaCacd> lstKnacacd = KnaCacdDao.selectAll_odb3(custac,
					status, false);

			Options<IoCaBindCardInfo> lstBicaInfos = new DefaultOptions<IoCaBindCardInfo>();

			// 遍历集合，设置绑卡信息到输出
			for (KnaCacd tblKnacacd : lstKnacacd) {
				IoCaBindCardInfo cplBindCardInfo = SysUtil
						.getInstance(IoCaBindCardInfo.class);
				cplBindCardInfo.setCdopac(tblKnacacd.getCdopac());// 绑定卡号
				cplBindCardInfo.setOpbrch(tblKnacacd.getOpbrch());// 开户行号
				cplBindCardInfo.setBrchna(tblKnacacd.getBrchna());// 开户行名称
				cplBindCardInfo.setCdopna(tblKnacacd.getAcctna());// 账户名称
				cplBindCardInfo.setBinddt(tblKnacacd.getBinddt());// 绑定日期
				cplBindCardInfo.setCardtp(tblKnacacd.getCardtp());// 绑定账户类型
				cplBindCardInfo.setIsbkca(tblKnacacd.getIsbkca());// 是否本行账户
				cplBindCardInfo.setBdcatp(tblKnacacd.getBdcatp());// 绑定账户分类

				lstBicaInfos.add(cplBindCardInfo);
				CommTools.getBaseRunEnvs().setTotal_count(Long.valueOf(lstBicaInfos.size()));// 总笔数
			}

			return lstBicaInfos;

		} else {
			List<KnaCacd> lstKnacacd = KnaCacdDao.selectAll_odb4(custac,
					status, cardtp, false);

			Options<IoCaBindCardInfo> lstBicaInfos = new DefaultOptions<IoCaBindCardInfo>();

			// 遍历集合，设置绑卡信息到输出
			for (KnaCacd tblKnacacd : lstKnacacd) {
				IoCaBindCardInfo cplBindCardInfo = SysUtil
						.getInstance(IoCaBindCardInfo.class);
				cplBindCardInfo.setCdopac(tblKnacacd.getCdopac());// 绑定卡号
				cplBindCardInfo.setOpbrch(tblKnacacd.getOpbrch());// 开户行号
				cplBindCardInfo.setBrchna(tblKnacacd.getBrchna());// 开户行名称
				cplBindCardInfo.setCdopna(tblKnacacd.getAcctna());// 账户名称
				cplBindCardInfo.setBinddt(tblKnacacd.getBinddt());// 绑定日期
				cplBindCardInfo.setCardtp(tblKnacacd.getCardtp());// 绑定账户类型
				cplBindCardInfo.setIsbkca(tblKnacacd.getIsbkca());// 是否本行账户
				cplBindCardInfo.setBdcatp(tblKnacacd.getBdcatp());// 绑定账户分类

				lstBicaInfos.add(cplBindCardInfo);
				CommTools.getBaseRunEnvs().setTotal_count(Long.valueOf(lstBicaInfos.size()));// 总笔数
			}
			//poc
			ApDataAudit apAudit=SysUtil.getInstance(ApDataAudit.class);
			apAudit.regLogOnInsertBusiPoc(cardno);
			return lstBicaInfos;
		}
	}

    @Override
    public E_YES___ selectBindCardResult(String acctno, String cdopac) {
        if (CommUtil.isNull(acctno)) {
            throw DpModuleError.DpstComm.BNAS0901();
        }
        if (CommUtil.isNull(cdopac)) {
            throw CaError.Eacct.BNAS1112();
        }
        KnaCacd knaCacd = KnaCacdDao.selectOne_odb1(acctno, cdopac, false);
        if (CommUtil.isNull(knaCacd)) {
            return E_YES___.NO;
        }
        E_DPACST status = knaCacd.getStatus();
        if (status == E_DPACST.NORMAL || status == E_DPACST.DEFAULT) {
            return E_YES___.YES;
        }
        return E_YES___.NO;
    }

	/**
	 * JF Modify：开户绑卡服务（绑多张结算卡）。
	 */
    @Override
	public void addBindCarList( final cn.sunline.ltts.busi.iobus.type.dp.IoCaTypeGenBindCard.IoCaSrvGenBindCard bindcaInfo,  String custac,  cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP accatp,  String cardno){

    	KnaMaad tblKnaMaad = KnaMaadDao.selectOne_odb1(custac, false);
		if(CommUtil.isNull(tblKnaMaad)) {
			throw DpModuleError.DpAcct.AT020028();
		}
		String mactid = tblKnaMaad.getMactid();
		// 调用绑卡服务。
		IoCaSevGenBindCard bindCardService = SysUtil.getInstance(IoCaSevGenBindCard.class);
		IoCaSevGenBindCar.Output output = SysUtil.getInstance(IoCaSevGenBindCar.Output.class);
			// 主账户标识。
		bindcaInfo.setMactid(mactid);
			// 绑定银行卡号。
			String cdopac = bindcaInfo.getCdopac();
			bindCardService.addBindCard(bindcaInfo, output);
			String acbdno = output.getAcbdno();
			String cardnu = output.getCardno();
			E_MANTWY mantwy = output.getMantwy();
			//登记绑定账号修改登记簿
			IoCaSevGenBindCard iocasBindCard = SysUtil.getInstance(IoCaSevGenBindCard.class);
			IoCaSrvGenBindCard bdAcctIn = SysUtil.getInstance(IoCaSrvGenBindCard.class);
			bdAcctIn.setCdopac(cdopac);
			bdAcctIn.setAcbdno(acbdno);
			bdAcctIn.setCardno(cardno);
			bdAcctIn.setMantwy(mantwy);
			bdAcctIn.setCardno(cardnu);
			iocasBindCard.regCardAlter(bdAcctIn);
	}

	/**
	 * 
	 * @author Rock
	 *         <p>
	 *         <li>2019年9月4日-下午9:05:04</li>
	 *         <li>功能描述：绑定账户信息修改登记</li>
	 *         </p>
	 * 
	 * @param bdAcctIn
	 */
	public void regCardAlter( final cn.sunline.ltts.busi.iobus.type.dp.IoCaTypeGenBindCard.IoCaSrvGenBindCard bdAcctIn){

		String custac = bdAcctIn.getCustac();
		String cardno = bdAcctIn.getCardno();
		String cdopac = bdAcctIn.getCdopac();
		String odopac = bdAcctIn.getOdopac();
		String acbdno = bdAcctIn.getAcbdno();
		E_MANTWY mantwy = bdAcctIn.getMantwy();
		
		// 输入参数校验
		if (CommUtil.isNull(custac)) {
			if (CommUtil.isNull(cardno)) {
				throw DpModuleError.DpstProd.BNAS0926();
			}
			// 根据电子账号获取电子账号ID
			KnaAcdc acdc = KnaAcdcDao.selectOne_odb2(cardno, false);
			if (CommUtil.isNull(acdc)) {
				throw CaError.Eacct.BNAS1279();
			}
			custac = acdc.getCustac();
		}
		
		if (mantwy == E_MANTWY.ADD) {
			if (CommUtil.isNull(cdopac)) {
				throw CaError.Eacct.BNAS1215();
			}
		}
		if (mantwy == E_MANTWY.CHANGE) {
			if (CommUtil.isNull(cdopac) || CommUtil.isNull(odopac)) {
				throw CaError.Eacct.BNAS1216();
			}
		}
		if (mantwy == E_MANTWY.REMOVE) {
			if (CommUtil.isNull(odopac)) {
				throw CaError.Eacct.BNAS1214();
			}
		}
		
		// 获取电子账户表中记录
		KnaCust cust = KnaCustDao.selectOne_odb1(custac, false);
		if (CommUtil.isNull(cust)) {
			throw CaError.Eacct.BNAS1279();
		}

		// 根据维护方式不同登记账户修改登记簿
		KnbCacq tblKnbCacq = SysUtil.getInstance(KnbCacq.class);
		if (mantwy == E_MANTWY.ADD) {
			tblKnbCacq.setCdopac(cdopac);// 新绑定账户
			tblKnbCacq.setAcbdno(acbdno);// 账号
			tblKnbCacq.setMantwy(E_MANTWY.ADD);// 维护方式
			tblKnbCacq.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 交易渠道
			tblKnbCacq.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易时间
			KnbCacqDao.insert(tblKnbCacq);
		}
		if (mantwy == E_MANTWY.CHANGE) {
			tblKnbCacq.setCdopac(cdopac);// 新绑定账户
			tblKnbCacq.setAcbdno(acbdno);// 账号
			tblKnbCacq.setMantwy(E_MANTWY.CHANGE);// 维护方式
			tblKnbCacq.setOdopac(odopac);// 原绑定账号
			tblKnbCacq.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 交易渠道
			tblKnbCacq.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易时间
			KnbCacqDao.insert(tblKnbCacq);
		}
		if (mantwy == E_MANTWY.REMOVE) {
			tblKnbCacq.setAcbdno(acbdno);// 账号
			tblKnbCacq.setMantwy(E_MANTWY.REMOVE);// 维护方式
			tblKnbCacq.setOdopac(odopac);// 原绑定账号
			tblKnbCacq.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 交易渠道
			tblKnbCacq.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易时间
			KnbCacqDao.insert(tblKnbCacq);
		}

	
	}

	/**
	 * 设置默认卡
	 */
	public void setDefaultCard( final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevGenBindCard.IoCaSetDefaultCard.Input input){
		
		// 结算卡卡号
		String cdopac = input.getCdopac();
		// 绑定账户标识
		E_ACCTFG acbdtp = input.getAcbdtp();
		// 主账户开户标识
		String mactid = input.getMactid();
		// 子账户开户标识
		String acctid = input.getAcctid();
		
		// 结算卡卡号
		if (CommUtil.isNull(cdopac)) {
			throw CaError.Eacct.BNAS1112();
		}
		// 绑定账户标识
		if(CommUtil.isNull(acbdtp)) {
			throw DpModuleError.DpAcct.AT010027();
		}

		// 绑定账号。
		String acbdno = "";
		// 主账户绑定默认卡时，主账户开户标识不能为空。
		if(E_ACCTFG.MASTERACCT == acbdtp) {
			if(CommUtil.isNull(mactid)) {
				throw DpModuleError.DpAcct.AT010013();
			}
			KnaMaad tblKnaMaad = KnaMaadDao.selectOne_odb2(mactid, false);
			if(CommUtil.isNull(tblKnaMaad)) {
				throw DpModuleError.DpAcct.AT020028();
			}
			acbdno = tblKnaMaad.getCustac();
		}
		// 子账户绑定默认卡时，子账户开户标识不能为空。
		if(E_ACCTFG.SUBACCT == acbdtp) {
			if(CommUtil.isNull(acctid)) {
				throw DpModuleError.DpAcct.AT010026();
			}
			KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(acctid, false);
			if(CommUtil.isNull(tblKnaSbad)) {
				throw DpModuleError.DpAcct.AT020010();
			}
			// 绑定子账户号。
			acbdno  = tblKnaSbad.getAcctno();
		}

		// 设置已绑定结算卡默认卡状态为正常状态：确保设置默认卡时，只存在一张默认卡。
		List<KnaCacd> lsKnaCacd = KnaCacdDao.selectAll_odb3(acbdno, E_DPACST.DEFAULT , false);
		if(lsKnaCacd.size() > 0) {
			for(KnaCacd cardInfo : lsKnaCacd) {
				cardInfo.setStatus(E_DPACST.NORMAL);
				KnaCacdDao.updateOne_odb1(cardInfo);
			}
		}
		
		// 查询结算卡绑定信息。
		KnaCacd tblKnaCacd = KnaCacdDao.selectOne_odb1(acbdno, cdopac, false);
		if (CommUtil.isNull(tblKnaCacd)) {
			throw DpModuleError.DpAcct.AT020032(acbdno, cdopac);
		}
		// 绑卡状态。
		E_DPACST cardStatus = tblKnaCacd.getStatus();
		
		// 绑卡状态为正常或默认卡。
		if (E_DPACST.NORMAL == cardStatus || E_DPACST.DEFAULT == cardStatus) {
			// 设置为默认卡。
			tblKnaCacd.setStatus(E_DPACST.DEFAULT);
			KnaCacdDao.updateOne_odb1(tblKnaCacd);
			
			// 绑卡状态不正常，不能设置为默认卡。
		} else if (E_DPACST.SLEEP == cardStatus || E_DPACST.CLOSE == cardStatus) {
			throw DpModuleError.DpAcct.AT020044();
		}
		
	}

	/**
	 * 结算卡解绑
	 */
	public void unbindCard( final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevGenBindCard.IoCaUnbindCard.Input input,  final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevGenBindCard.IoCaUnbindCard.Output output){

		// 结算卡卡号
		String cdopac = input.getCdopac();
		// 绑定账户标识
		E_ACCTFG acbdtp = input.getAcbdtp();
		// 主账户开户标识
		String mactid = input.getMactid();
		// 子账户开户标识
		String acctid = input.getAcctid();

		// 结算卡卡号
		if (CommUtil.isNull(cdopac)) {
			throw CaError.Eacct.BNAS1112();
		}
		// 绑定账户标识
		if(CommUtil.isNull(acbdtp)) {
			throw DpModuleError.DpAcct.AT010027();
		}

		// 绑定账号。
		String acbdno = "";
		String custac = "";
		String cardno = "";
		// 主账户解绑时，主账户开户标识不能为空。
		if(E_ACCTFG.MASTERACCT == acbdtp) {
			if(CommUtil.isNull(mactid)) {
				throw DpModuleError.DpAcct.AT010013();
			}
			KnaMaad tblKnaMaad = KnaMaadDao.selectOne_odb2(mactid, false);
			if(CommUtil.isNull(tblKnaMaad)) {
				throw DpModuleError.DpAcct.AT020028();
			}
			acbdno = tblKnaMaad.getCustac();
			custac = tblKnaMaad.getCustac();
			cardno = tblKnaMaad.getCardno();
		}
		// 子账户解绑时，子账户开户标识不能为空。
		if(E_ACCTFG.SUBACCT == acbdtp) {
			if(CommUtil.isNull(acctid)) {
				throw DpModuleError.DpAcct.AT010026();
			}
			KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(acctid, false);
			if(CommUtil.isNull(tblKnaSbad)) {
				throw DpModuleError.DpAcct.AT020010();
			}
			KnaMaad tblKnaMaad = KnaMaadDao.selectOne_odb2(tblKnaSbad.getMactid(), false);
			if(CommUtil.isNull(tblKnaMaad)) {
				throw DpModuleError.DpAcct.AT020028();
			}
			// 绑定子账户号。
			acbdno  = tblKnaSbad.getAcctno();
			custac = tblKnaSbad.getCustac();
			cardno = tblKnaMaad.getCardno();
		}
		// 返回信息。
		output.setAcbdno(acbdno);
		output.setCardno(cardno);
		output.setMantwy(E_MANTWY.REMOVE);
		
		// 查询电子账户表信息。
		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(custac, false);
		if (CommUtil.isNull(tblKnaCust))
			throw CaError.Eacct.BNAS0393();
		// 检查电子账户状态：正常和未生效都允许解绑。
		if (E_ACCTST.NORMAL != tblKnaCust.getAcctst() && E_ACCTST.INVALID != tblKnaCust.getAcctst()) {
			throw DpModuleError.DpAcct.AT020045();
		}

		// 查询结算卡绑定信息。
		KnaCacd tblKnaCacd = KnaCacdDao.selectOne_odb1(acbdno, cdopac, false);
		if (CommUtil.isNull(tblKnaCacd)) {
			throw DpModuleError.DpAcct.AT020032(acbdno, cdopac);
		}
		// 绑卡状态。
		E_DPACST cardStatus = tblKnaCacd.getStatus();

		// 绑卡状态不正常，不能解绑。
		if (E_DPACST.SLEEP == cardStatus || E_DPACST.CLOSE == cardStatus) {
			throw DpModuleError.DpAcct.AT020046();
		}
		// 查询账户有效绑卡信息。
		 List<KnaCacd> lsCard = CaDao.selAllCards(acbdno, false);
		// 绑卡总数为1，不能解绑。
		 if(CommUtil.compare(lsCard.size(), 1)== 0 ) {
			 throw DpModuleError.DpAcct.AT020047();
			
			 // 绑卡总数为2。
		 }else if(CommUtil.compare(lsCard.size(), 2) == 0 ) {
			 // 状态正常直接解绑；状态默认，解绑后需更新另一张卡为默认卡。
			 if(E_DPACST.DEFAULT == cardStatus) {
				 for(KnaCacd card : lsCard) {
					 if(E_DPACST.NORMAL == card.getStatus()) {
						 card.setStatus(E_DPACST.DEFAULT);
						 KnaCacdDao.updateOne_odb1(card);
						 break;
					 }
				 }
			 }
			 tblKnaCacd.setStatus(E_DPACST.CLOSE);
			 KnaCacdDao.updateOne_odb1(tblKnaCacd);
			 
			 // 绑卡总数大于2。
		 }else if(CommUtil.compare(lsCard.size(), 2) > 0) {
			 // TODO：默认卡设置待处理。
			 tblKnaCacd.setStatus(E_DPACST.CLOSE);
			 KnaCacdDao.updateOne_odb1(tblKnaCacd);
		 }
		 
	
	}
	
}