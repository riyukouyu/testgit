
package cn.sunline.ltts.busi.dptran.trans.online;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaadDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypeGenBindCard.IoCaSrvGenBindCard;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BRSTUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_USCHNL;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_USTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BDCART;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_IDCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MPCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_OPACRT;

public class opactf {

	/**
	 * 检查开户信息。
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void chkOpaccInfo( final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Property property,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Output output){

		BizLog bizLog = BizLogUtil.getBizLog(opaccd.class);
		String custna = input.getCustna(); //客户名称
		String idtfno = input.getIdtfno(); //证件号码
		E_IDTFTP idtftp = input.getIdtftp(); //证件类型
		String tlphno = input.getTlphno(); //联系方式
		String crcycd = input.getCrcycd(); //货币代号
		E_IDCKRT idckrt = input.getIdckrt(); //身份核查结果
		E_MPCKRT mpckrt = input.getMpckrt(); //人脸识别结果
		String custno = input.getCustno(); //用户ID
		E_ACCATP accatp = input.getAccatp(); //账户分类
		//Options<IoCaBindCardInfo>  lstBindcaInfos = input.getLstBindcaInfo(); //绑定账户信息列表
		//  E_CHNLID chnlid = input.getChnlid();
		/*
		 * JF Add：主账户开户标识、子账户开户标识、品牌ID、品牌名称、即富开户渠道、客户类型。
		 */
		String mactid = input.getMactid();
		String acctid = input.getAcctid();
		String sbrand = input.getSbrand();
		String bradna = input.getBradna();
		E_USTYPE usertp = input.getUsertp();
		E_USCHNL uschnl = input.getUschnl();


		if(CommUtil.isNull(mactid)) {
			throw DpModuleError.DpAcct.AT010013();
		}
		if(CommUtil.isNull(acctid)) {
			throw DpModuleError.DpAcct.AT010026();
		}
		if(CommUtil.isNull(sbrand)) {
			throw DpModuleError.DpAcct.AT010006();
		}
		if(CommUtil.isNull(bradna)) {
			throw DpModuleError.DpAcct.AT010015();
		}
		if(CommUtil.isNull(usertp)) {
			throw DpModuleError.DpAcct.AT010014();
		}
		if(CommUtil.isNull(uschnl)) {
			throw DpModuleError.DpAcct.AT010002();
		}

		//mod by xj 2018613 开户渠道不能为空
		/*
		 * JF Delete
    if (CommUtil.isNull(chnlid)) {
        throw CaError.Eacct.BNAS0384();
    }
		 */

		if (CommUtil.isNull(input.getCacttp())) {
			throw CaError.Eacct.BNAS0516("[" + input.getCacttp() + "]");
		}
		//客户名称不能为空，并且长度不能超过100
		if (CommUtil.isNull(custna)) {
			CommTools.fieldNotNull(custna, BaseDict.Comm.custna.getId(), BaseDict.Comm.custna.getLongName());
		}
		/*
		 * JF Delete：客户名称长度不检查。
    if (custna.getBytes().length > 100) {
        throw DpModuleError.DpstComm.BNAS0523();
    }
		 */
		//证件类型、证件号码不能为空
		if (CommUtil.isNull(idtfno)) {
			CommTools.fieldNotNull(idtfno, BaseDict.Comm.idtfno.getId(), BaseDict.Comm.idtfno.getLongName());
		}
		if (CommUtil.isNull(idtftp)) {
			CommTools.fieldNotNull(idtftp, BaseDict.Comm.idtftp.getId(), BaseDict.Comm.idtftp.getLongName());
		}

		//暂时只支持身份证开户
		/*
		 * JF Delete：支持组织机构代码开户。
    if (E_IDTFTP.SFZ != idtftp) {
        throw CaError.Eacct.BNAS0199();
    }
		 */

		//出生日期加16与交易日期对比
		/*
		 * JF Delete：证件号已加密。
    String trandt = DateTools2.covDateToString(DateTools2.addDays(DateTools2.covStringToDate(BusiTools.getBirthBySID(idtfno)), 5844));
    bizLog.debug("满16岁" + trandt + ",当前日期：" + CommTools.getBaseRunEnvs().getTrxn_date());
    //未满16周不能开立电子账户
    if (DateUtil.compareDate(trandt, CommTools.getBaseRunEnvs().getTrxn_date()) > 0) {
        throw CaError.Eacct.BNAS1211();
    }
		 */

		//联系方式不能为空，长度为11位
		if (CommUtil.isNull(tlphno)) {
			CommTools.fieldNotNull(tlphno, BaseDict.Comm.teleno.getId(), BaseDict.Comm.teleno.getLongName());

		}
		/*
		 * JF Delete：手机号已加密。
    if (teleno.length() != 11) {
        throw DpModuleError.DpstComm.BNAS0469();
    }
    // 校验手机号是否全为数字
    if (!BusiTools.isNum(teleno)) {
        throw CaError.Eacct.BNAS0319();
    }
		 */

		//货币代号
		if (CommUtil.isNull(crcycd)) {
			throw DpModuleError.DpstComm.BNAS0665();
		}

		/**
		 * mod by xj 20180526
		 */
		if (CommUtil.isNull(input.getIspswd()) ){
			throw DpModuleError.DpAcct.AT010003();
		}

		if (E_YES___.YES == input.getIspswd() && CommUtil.isNull(input.getPasswd())) {
			throw DpModuleError.DpAcct.AT010004();
		}
		/**end*/

		//身份核查结果
		if (CommUtil.isNull(idckrt)) {
			throw CaError.Eacct.BNAS0368();
		}

		//人脸识别结果
		if (CommUtil.isNull(mpckrt)) {
			throw CaError.Eacct.BNAS0378();
		}

		//面签标识
		//		if(CommUtil.isNull(facesg)){
		//			throw CaError.Eacct.E0001("面签标识不能为空");
		//		}

		//用户ID
		if (CommUtil.isNull(custno)) {
			throw CaError.Eacct.BNAS0241();
		}

		//开户渠道为手机银行时密码为必输项 20180613 yanghao
		/*
		 * JF Delete：no need.
    if (input.getChnlid()==E_CHNLID.SH) {
        if(CommUtil.isNull(input.getPasswd())){
            throw CaError.Eacct.E0001("开户渠道为手机银行时交易密码不能为空");
        }
        if(CommUtil.isNull(input.getAuthif())){
            throw CaError.Eacct.E0001("开户渠道为手机银行时加密因子不能为空");
        }
    }
		 */
		/*
		 * JF Delete：客户内码暂不需要。
    if (CommUtil.isNull(input.getCustcd())) {
        throw CaError.Eacct.E0001("客户内码不能为空");
    }
     if (idckrt == E_IDCKRT.SUCCESS) {
        if (CommUtil.isNull(input.getCustcd())) {
            throw CaError.Eacct.BNAS0371();
        }
    }
		 */

		//电子账户分类
		if (CommUtil.isNull(accatp)) {
			throw CaError.Eacct.BNAS0896();
		}

		//账户所属机构
		if (CommUtil.isNull(input.getBrchno())) {
			throw CaError.Eacct.BNAS0888();
		}

		//app推送ID，移动前端(丰收互联)开户时，必输
		//		if(CommTools.getBaseRunEnvs().getChannel_id() == "NM" 
		//				&& CommUtil.isNull(CommTools.getBaseRunEnvs().getAppsid())){
		//			throw CaError.Eacct.E0001("app推送ID不能为空");
		//		}

		/*
		 * JF Delete：即富开立电子账户时可以不绑定提款账户。
    //绑定账户信息，如果开立的为二类户或者三类户，绑定账户信息不能为空
    if ((E_ACCATP.FINANCE == accatp || E_ACCATP.WALLET == accatp) && (CommUtil.isNull(lstBindcaInfos) || lstBindcaInfos.size() == 0)) {
        throw CaError.Eacct.BNAS0561(accatp.getLongName());
    }
		 */
		//如果开立的为Ⅲ类户，人脸识别结果默认为成功，不为成功则报错
		if (E_ACCATP.WALLET == accatp && E_MPCKRT.SUCCESS != mpckrt) {
			throw CaError.Eacct.BNAS1944();
		}

		/*
		 * JF Modify：去掉NM-丰收互联渠道逻辑。
    String servtp = CommTools.getBaseRunEnvs().getChannel_id();
    if ("NM" != servtp) {
        //开户机构与柜员所在交易机构(清算中心)是否一致，不一致报错
        if (CommUtil.compare(input.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch()) != 0) {
            throw CaError.Eacct.BNAS1122(input.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch());
        }
    }

      if ("NM" == servtp) {
        //			CommTools.getBaseRunEnvs().setBusi_org_id(corpno); //设置开户机构所属法人代码设置环境变量中
        CommTools.getBaseRunEnvs().setTrxn_branch(input.getBrchno()); //设置开户机构到环境变量中

        //开户机构是否是县级 by update cqm 只控制到县级机构
        //if (!(cplBrchInfo.getBrchlv() == E_BRCHLV.COUNT)) {
           // throw CaError.Eacct.BNAS0564();
        //}
    } else {

        //判断开户机构与交易机构是否统一法人
        if (CommUtil.compare(corpno, CommTools.getBaseRunEnvs().getBusi_org_id()) != 0) {
            throw CaError.Eacct.BNAS1122(input.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch());
        }
    }
		 */

		//开户机构与柜员所在交易机构(清算中心)是否一致，不一致报错
		if (CommUtil.compare(input.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch()) != 0) {
			throw CaError.Eacct.BNAS1122(input.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch());
		}

		//获取开户机构信息，并获取开户机构对应法人代码
		/*
		 * JF Modify：外调。
    IoBrchInfo cplBrchInfo = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(input.getBrchno());
    String corpno = cplBrchInfo.getCorpno();

    //判断开户机构与交易机构是否统一法人
    if (CommUtil.compare(corpno, CommTools.getBaseRunEnvs().getBusi_org_id()) != 0) {
        throw CaError.Eacct.BNAS1122(input.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch());
    }
		 */
		IoBrchInfo cplBrchInfo = SysUtil.getRemoteInstance(IoSrvPbBranch.class).getBranch(input.getBrchno());
		String corpno = cplBrchInfo.getCorpno();

		//判断开户机构与交易机构是否统一法人
		if (CommUtil.compare(corpno, CommTools.getBaseRunEnvs().getBusi_org_id()) != 0) {
			throw CaError.Eacct.BNAS1122(input.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch());
		}
		/**
		 * mod by xj 20180503 
		 */
		/*//农信的省级机构暂不允许开立电子账户
    if (E_BRCHLV.PROV == cplBrchInfo.getBrchlv()) {
        throw CaError.Eacct.BNAS1127(cplBrchInfo.getBrchno());
    }*/

		// 未启用的机构不允许开户
		if (cplBrchInfo.getBrstus() != E_BRSTUS.valid) {
			throw CaError.Eacct.BNAS1208();
		}

		//校验证件类型、证件号码
		/*
		 * JF Delete：证件号已加密。
    BusiTools.chkCertnoInfo(idtftp, idtfno);
		 */

		// 根据账户分类获取开户产品号
		/*
		 * JF Modify：如果chanid渠道是JFSD-即富收单，
		 * baprcd基础负债产品取parm_value1的值（不计息活期负债产品）。
		 */
		KnpParameter para = DpPublic.getProdcdByAccatp(accatp);
		property.setBaprcd(para.getParm_value1());
//		property.setWaprcd(para.getParm_value2());

		/*
		 * JF Modify：客户信息在用户中心统一管理，核心不登记客户信息，
		 * 如需查询客户信息，需通过查询服务外调获取。

    CifCust tbkCifCust = CifCustDao.selectOne_odb2(idtftp, idtfno, false);

    property.setIfflag(E_YES___.YES);//by update cqm
    //修复销户后重新开户后报错的问题

    //		// 检查电子账户是否存在，并设置客户状态；
    if (CommUtil.isNotNull(tbkCifCust)) {
        KnaCust tbKnaCust = CaDao.selKnaCustByCustno(tbkCifCust.getCustno(), input.getCacttp(), false);//cacttp
        if (CommUtil.isNotNull(tbKnaCust)) {
            KnaAcdc tbKnaAcdc = KnaAcdcDao.selectFirst_odb3(tbKnaCust.getCustac(), true);
            // 修改账户状态 begin
            tbKnaCust.setAcctst(E_ACCTST.NORMAL);
            KnaCustDao.updateOne_odb1(tbKnaCust);
            // 修改账户状态 end
            property.setCustac(tbKnaCust.getCustac());
            property.setCustno(tbkCifCust.getCustno());
            property.setCardno(tbKnaAcdc.getCardno());

            mod by xj 20180523 注释标志，使得同个客户支持开多个电子账号

//            List<KnbOpac> tbknbOpacs = KnbOpacDao.selectAll_odb3(input.getChnlid(), false);
//            if (tbknbOpacs.size() == 1) {
//            	for (KnbOpac knbOpac : tbknbOpacs) {
//            		if (knbOpac.getChnlid()==E_CHNLID.PH || knbOpac.getChnlid()==E_CHNLID.QB ||knbOpac.getChnlid()==E_CHNLID.XA) {
//            			property.setIfflag(E_YES___.NO);
//            		}
//            	}
//            }
//            if (CommUtil.isNotNull(tbknbOpac)) {
//				if (tbknbOpac.getChnlid()==E_CHNLID.PH || tbknbOpac.getChnlid()==E_CHNLID.QB || tbknbOpac.getChnlid()==E_CHNLID.XA) {
//					property.setIfflag(E_YES___.NO);
//				}
//			}
            if (input.getChnlid()==E_CHNLID.QB || input.getChnlid()==E_CHNLID.XA) {
				property.setIfflag(E_YES___.NO);
			}
            //teleno
        }
        // 检查已存在客户是否已绑卡
    }
		 */

		// 电子账户新增标志。
		property.setIfflag(E_YES___.YES);
		/*
		 * JF Modify：根据mactid主账户开户标识查询，判断是否需新增主账户。
		 */
		KnaMaad tblKnaMaad = KnaMaadDao.selectOne_odb2(mactid, false);
		// 主账户开户标识存在，不新增电子账户。
		KnaCust tbKnaCust = SysUtil.getInstance(KnaCust.class);
		if(CommUtil.isNotNull(tblKnaMaad)) {
			tbKnaCust = KnaCustDao.selectOne_odb1(tblKnaMaad.getCustac(), false);
		}

		if(CommUtil.isNotNull(tbKnaCust) && CommUtil.isNotNull(tbKnaCust.getCustno())) {
			// 如果存在状态正常的电子账户，则不新开电子账户，直接返回可用电子账户信息。
			if(E_ACCTST.NORMAL == tbKnaCust.getAcctst()) {
				property.setIfflag(E_YES___.NO);
				// 状态不正常，暂时抛错，后期确认后调整。
			}else {
				throw DpModuleError.DpAcct.AT020016(mactid);
			}
			KnaAcdc tbKnaAcdc = KnaAcdcDao.selectFirst_odb3(tbKnaCust.getCustac(), true);
			property.setCustno(custno);
			property.setCustac(tbKnaCust.getCustac());
			property.setCardno(tbKnaAcdc.getCardno());

			// 不需要重新开户,则需要校验手机号是不是一致。
			/*
			 * JF Delete
			KnaAcal tblKnaAcal = KnaAcalDao.selectOne_odb7(tbKnaCust.getCustac(), E_ACALTP.CELLPHONE, tlphno, E_ACALST.NORMAL, false);
			if (CommUtil.isNull(tblKnaAcal)) {
				throw DP.AT020005();
			}
			 */
		}

		/*
		 * JF Add：根据acctid子账户开户标识查询，若存在，不开子户。
		 */
		// POS品牌子账户新增标志。
		property.setAdsbfg(E_YES___ .YES);
		KnaSbad tblKnaSbad  = KnaSbadDao.selectOne_odb2(acctid, false);
		if(CommUtil.isNotNull(tblKnaSbad)) {
			// 是否存在多次送同一个acctid子账户开户标识，后期确认。
			property.setAdsbfg(E_YES___ .NO);
		}

		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("IsOpacctOne", "OpenWallet", "%", "%", false);
		property.setIonefg(E_YES___.NO);
		if (CommUtil.isNotNull(tblKnpParameter) && "1".equals(tblKnpParameter.getParm_value1())) {
			property.setIonefg(E_YES___.YES);
		}
	}

	/**
	 * 检查绑卡认证结果。
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void chkBdcart( final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Property property,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Output output){

		/*
		 * JF Modify：绑卡信息改为复合类型，不是列表。
		Options<IoCaSrvGenBindCard> lstBindcaInfoList = input.getLstBindcaInfo();
		E_YES___ eBdrtfg = E_YES___.NO;
		if (CommUtil.isNotNull(lstBindcaInfoList) && lstBindcaInfoList.size() > 0) {
			for (IoCaSrvGenBindCard info : lstBindcaInfoList) {
				if (info.getBdcart() == E_BDCART.SUCCESS) {
					eBdrtfg = E_YES___.YES;
					property.setBdrtfg(eBdrtfg);
				}
			}
		} else {
			eBdrtfg = E_YES___.YES;
			property.setBdrtfg(eBdrtfg);
		}
		 */
		E_YES___ eBdrtfg = E_YES___.NO;
		IoCaSrvGenBindCard bindcaInfo = input.getBindcaInfo();
		if(CommUtil.isNotNull(bindcaInfo)) {
			if (bindcaInfo.getBdcart() == E_BDCART.SUCCESS) {
				eBdrtfg = E_YES___.YES;
				property.setBdrtfg(eBdrtfg);
				property.setBindfg(E_YES___.YES);
			} 
		}else {
			eBdrtfg = E_YES___.YES;
			property.setBdrtfg(eBdrtfg);
			property.setBindfg(E_YES___.NO);
		}

}

	/**
	 * 开立品牌子账户。
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void openSubaccts( final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Property property,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Output output){
//
//		E_CUSACT cacttp = input.getCacttp();
//		String custno = input.getCustid();
//		String acctna = input.getCustna();
//		String mactid = input.getMactid();
//		String crcycd = input.getCrcycd();
//		String custac = property.getCustac();
//		String prodcd = property.getBaprcd();
//		E_TERMCD depttm = property.getDepttm();
//		E_YES___ opacfg = property.getOpacfg();
//		E_CUSTTP custtp = property.getCusttp();
//		E_PRODTP prodtp = property.getProdtp();
//		Options<Agents> lsAgent = input.getAgents();
//		
//
//		// 账户服务实例。
//		DpAcctSvcType dpAcctService  = SysUtil.getInstance(DpAcctSvcType.class);
//		// 新增负债账户输入。
//		DpAcctSvcType.AddAcct.InputSetter openSubInput =  SysUtil.getInstance(DpAcctSvcType.AddAcct.InputSetter.class);
//
//		// 电子账户服务实例。
//		IoCaSrvGenEAccountInfo eacctService = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
//		// 负债子账号与电子账户关联服务输入。
//		IoCaAddEARelaIn acctRelationInput = SysUtil.getInstance(IoCaAddEARelaIn.class);
//
//		// 登记电子子账户附加信息。
//		KnaSbad subaddInfo = SysUtil.getInstance(KnaSbad.class);
//
//		// 服务商分润服务实例。
//		IoDsManage  agentProfitSer = SysUtil.getInstance(IoDsManage.class);
//		// 服务商分润设置输入。
//		IoDsManage.setDsRate.InputSetter agentProfitInput = SysUtil.getInstance(IoDsManage.setDsRate.InputSetter.class);
//		IoDsManage.setDsRate.Output agentProfitOutput = SysUtil.getInstance(IoDsManage.setDsRate.Output.class);
//		// 循环遍历品牌信息，判断是否开子户。
//		for(Agents agent : lsAgent) {
//			KnaSbad tblKnaSbad  = KnaSbadDao.selectOne_odb2(agent.getAcctid(), false);
//			if(CommUtil.isNotNull(tblKnaSbad)) {
//				continue;
//			}
//		
//			/*
//			 *  开子户。
//			 */
//			openSubInput.setCacttp(cacttp);
//			openSubInput.setCustno(custno);
//			openSubInput.setAcctna(acctna);
//			openSubInput.setMactid(mactid);
//			openSubInput.setCrcycd(crcycd);
//			openSubInput.setCustac(custac);
//			openSubInput.setProdcd(prodcd);
//			openSubInput.setDepttm(depttm);
//			openSubInput.setOpacfg(opacfg);
//			openSubInput.setCusttp(custtp);
//			// 新增负债账户方法。
//			AddSubAcctOut  openSubOut  = dpAcctService.addAcct(openSubInput);
//			// 新增负债账户输出。
//			String acctno = openSubOut.getAcctno();
//			E_FCFLAG pddpfg = openSubOut.getPddpfg();
//
//			/*
//			 *  建立映射关系。
//			 */
//			acctRelationInput.setAcctno(acctno);
//			acctRelationInput.setCustac(custac);
//			acctRelationInput.setFcflag(pddpfg);
//			acctRelationInput.setProdtp(prodtp);
//			acctRelationInput.setProdcd(prodcd);
//			acctRelationInput.setCrcycd(crcycd);
//			// 账户关系建立方法。
//			eacctService.prcAddEARela(acctRelationInput);
//			
//			/*
//			 *  登记子账户附加信息。
//			 */
//			subaddInfo.setAcctid(agent.getAcctid());
//			subaddInfo.setAcctno(acctno);
//			subaddInfo.setCustac(custac);
//			subaddInfo.setMactid(mactid);
//			subaddInfo.setPsedid(agent.getPsedid());
//			subaddInfo.setPsedna(agent.getPsedna());
//			subaddInfo.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
//			subaddInfo.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp());
//			KnaSbadDao.insert(subaddInfo);
//			
//			// 登记费率信息。
//			agentProfitInput.setAgntid(agent.getAgntid());
//			agentProfitInput.setAgname(agent.getAgname());
//			agentProfitInput.setCentid(agent.getCentid());
//			agentProfitInput.setCentna(agent.getCentna());
//			agentProfitInput.setTempno(agent.getTempno());
//			agentProfitInput.setRatelist(agent.getRatelist());
//			agentProfitSer.setDsRate(agentProfitInput, agentProfitOutput);
//			
//		}
//	
	}

	/**
	 * 开户输出信息。
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void prcOpaccdOut( final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Property property,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Output output){
		output.setCardno(property.getCardno());
		output.setCustac(property.getCustac());
		output.setBrchno(input.getBrchno());
		output.setMactid(input.getMactid());
		output.setAcctid(input.getAcctid());
		output.setAcctno(property.getAcctno());
		cn.sunline.ltts.busi.sys.type.yht.E_ACCATP accatp = null;
		//if(input.getChnlid()==E_CHNLID.YT){}
		String accatpStr = KnaCustDao.selectOne_odb1(property.getCustac(), false).getAccttp().toString();
		if(CommUtil.equals(accatpStr, "1")){
			accatp = cn.sunline.ltts.busi.sys.type.yht.E_ACCATP.GLOBAL;
		}else if(CommUtil.equals(accatpStr, "2")){
			accatp = cn.sunline.ltts.busi.sys.type.yht.E_ACCATP.FINANCE;
		}else if(CommUtil.equals(accatpStr, "3")){
			accatp = cn.sunline.ltts.busi.sys.type.yht.E_ACCATP.WALLET;
		}
		output.setAccatp(accatp);
	
		if (E_IDCKRT.SUCCESS == input.getIdckrt() && E_MPCKRT.SUCCESS == input.getMpckrt()) {
			output.setOpacrt(E_OPACRT.SUCCESS);
		} else if (E_IDCKRT.CHECKING == input.getIdckrt() || E_MPCKRT.CHECKING == input.getMpckrt()) {
			output.setOpacrt(E_OPACRT.PREOPEN);
		} else {
			output.setOpacrt(E_OPACRT.FAILD);
		}

		//poc增加审计日志
		/*
		 * JF Modify：审件登记暂时注释掉。
        ApDataAudit apAudit = SysUtil.getInstance(ApDataAudit.class);
        apAudit.regLogOnInsertBusiPoc(property.getCardno());
		 */
	}

	public static void bindmq( final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Property property,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Output output){
		// TODO.
	}

	public static void sendOpenMessage( final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Property property,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Output output){
		//TODO.
	}

	public static void sedOpenInfoMessage( final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Property property,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Output output){
		//TODO.
	}

	/**
	 * 登记电子子账户附加信息
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void registKnaSbad( final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Property property,  final cn.sunline.ltts.busi.dptran.trans.online.intf.Opactf.Output output){
		
		// 电子子账户附加信息表实例。
		KnaSbad subaddInfo = SysUtil.getInstance(KnaSbad.class);
		subaddInfo.setCustac(property.getCustac());
		subaddInfo.setAcctno(property.getAcctno());
		subaddInfo.setMactid(input.getMactid());
		subaddInfo.setAcctid(input.getAcctid());
		subaddInfo.setSbrand(input.getSbrand());
		subaddInfo.setBradna(input.getBradna());
		subaddInfo.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		subaddInfo.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp());
		String acctna = input.getAcctna();
		subaddInfo.setAcctna(acctna);
//		subaddInfo.setTmacctna(DecryptConstant.maskName(acctna));
		KnaSbadDao.insert(subaddInfo);
	}


}
