package cn.sunline.ltts.busi.dptran.trans.online;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaadDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.ca.type.CaCustInfo.BrandsInfo;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.AddSubAcctOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEARelaIn;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubSqrd;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BRSTUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CUSTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_USCHNL;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_USERTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_USTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_IDCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MPCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_OPACRT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;

public class opaccd {

	public static void chkOpaccInfo(final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Input input,
            final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Property property,
            final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Output output) {
        
    	BizLog bizLog = BizLogUtil.getBizLog(opaccd.class);
        String custna = input.getCustna(); //客户名称
        String idtfno = input.getIdtfno(); //证件号码
        E_IDTFTP idtftp = input.getIdtftp(); //证件类型
        String tlphno = input.getTlphno(); //联系方式
        String crcycd = input.getCrcycd(); //货币代号
        //del by lishuyao 20191210
        //E_IDCKRT idckrt = input.getIdckrt(); //身份核查结果
        //E_MPCKRT mpckrt = input.getMpckrt(); //人脸识别结果
        String custno = input.getCustno(); //用户ID
        E_ACCATP accatp = input.getAccatp(); //账户分类
		// Options<IoCaBindCardInfo> lstBindcaInfos = input.getLstBindcaInfo();
		// //绑定账户信息列表
       //  E_CHNLID chnlid = input.getChnlid();
        /*
         * JF Add非空检查：品牌ID、品牌名称、品牌子账户ID、即富开户渠道、客户类型、开户主体ID。
         */
        String mactid = input.getMactid();
        E_USTYPE usertp = input.getUsertp();
        E_USCHNL uschnl = input.getUschnl();
        Options<BrandsInfo> lsBrand= input.getBrandList();
        // 检查品牌信息是否为空。
        if(CommUtil.isNull(lsBrand) || lsBrand.size() == 0) {
        	throw DpModuleError.DpAcct.AT020017();
        }
        for(BrandsInfo brand : lsBrand) {
			boolean ispsbdNull = CommUtil.isNull(brand.getAcctid()) || CommUtil.isNull(brand.getSbrand())
					|| CommUtil.isNull(brand.getBradna());
        	if(ispsbdNull) {
        		throw DpModuleError.DpAcct.AT020017();
        	}
        }
       
        if(CommUtil.isNull(mactid)) {
        	throw DpModuleError.DpAcct.AT010013();
        }
        if(CommUtil.isNull(usertp)) {
        	throw DpModuleError.DpAcct.AT010014();
        }
        if(CommUtil.isNull(uschnl)) {
        	throw DpModuleError.DpAcct.AT010002();
        }
        
        //mod by xj 2018613 开户渠道不能为空
        /*
		 * JF Delete if (CommUtil.isNull(chnlid)) { throw CaError.Eacct.BNAS0384(); }
         */
        
        if (CommUtil.isNull(input.getCacttp())) {
            throw CaError.Eacct.BNAS0516("[" + input.getCacttp() + "]");
        }
        //客户名称不能为空，并且长度不能超过100
        if (CommUtil.isNull(custna)) {
            CommTools.fieldNotNull(custna, BaseDict.Comm.custna.getId(), BaseDict.Comm.custna.getLongName());
        }
        /*
		 * JF Delete：客户名称长度不检查。 if (custna.getBytes().length > 100) { throw
		 * DpModuleError.DpstComm.BNAS0523(); }
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
		 * JF Delete：支持组织机构代码开户。 if (E_IDTFTP.SFZ != idtftp) { throw
		 * CaError.Eacct.BNAS0199(); }
         */

        //出生日期加16与交易日期对比
        /*
		 * JF Delete：证件号已加密。 String trandt =
		 * DateTools2.covDateToString(DateTools2.addDays(DateTools2.covStringToDate(
		 * BusiTools.getBirthBySID(idtfno)), 5844)); bizLog.debug("满16岁" + trandt +
		 * ",当前日期：" + CommTools.getBaseRunEnvs().getTrxn_date()); //未满16周不能开立电子账户 if
		 * (DateUtil.compareDate(trandt, CommTools.getBaseRunEnvs().getTrxn_date()) > 0)
		 * { throw CaError.Eacct.BNAS1211(); }
         */

        //联系方式不能为空，长度为11位
        if (CommUtil.isNull(tlphno)) {
            CommTools.fieldNotNull(tlphno, BaseDict.Comm.teleno.getId(), BaseDict.Comm.teleno.getLongName());

        }
        /*
		 * JF Delete：手机号已加密。 if (teleno.length() != 11) { throw
		 * DpModuleError.DpstComm.BNAS0469(); } // 校验手机号是否全为数字 if (!BusiTools.isNum(teleno)) {
		 * throw CaError.Eacct.BNAS0319(); }
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
        
        // delete by lishuyao 20191210
        /*//身份核查结果
        if (CommUtil.isNull(idckrt)) {
            throw CaError.Eacct.BNAS0368();
        }

        //人脸识别结果
        if (CommUtil.isNull(mpckrt)) {
            throw CaError.Eacct.BNAS0378();
        }*/

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
		 * JF Delete：no need. if (input.getChnlid()==E_CHNLID.SH) {
		 * if(CommUtil.isNull(input.getPasswd())){ throw
		 * CaError.Eacct.E0001("开户渠道为手机银行时交易密码不能为空"); }
		 * if(CommUtil.isNull(input.getAuthif())){ throw
		 * CaError.Eacct.E0001("开户渠道为手机银行时加密因子不能为空"); } }
        */
        /*
		 * JF Delete：客户内码暂不需要。 if (CommUtil.isNull(input.getCustcd())) { throw
		 * CaError.Eacct.E0001("客户内码不能为空"); } if (idckrt == E_IDCKRT.SUCCESS) { if
		 * (CommUtil.isNull(input.getCustcd())) { throw CaError.Eacct.BNAS0371(); } }
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
		 * JF Delete：即富开立电子账户时可以不绑定提款账户。 //绑定账户信息，如果开立的为二类户或者三类户，绑定账户信息不能为空 if
		 * ((E_ACCATP.FINANCE == accatp || E_ACCATP.WALLET == accatp) &&
		 * (CommUtil.isNull(lstBindcaInfos) || lstBindcaInfos.size() == 0)) { throw
		 * CaError.Eacct.BNAS0561(accatp.getLongName()); }
		 */
        
        //del by lishuyao 20191210
        /*//如果开立的为Ⅲ类户，人脸识别结果默认为成功，不为成功则报错
        if (E_ACCATP.WALLET == accatp && E_MPCKRT.SUCCESS != mpckrt) {
            throw CaError.Eacct.BNAS1944();
        }*/
        
        /*
		 * JF Modify：去掉NM-丰收互联渠道逻辑。 String servtp =
		 * CommTools.getBaseRunEnvs().getChannel_id(); if ("NM" != servtp) {
		 * //开户机构与柜员所在交易机构(清算中心)是否一致，不一致报错 if (CommUtil.compare(input.getBrchno(),
		 * CommTools.getBaseRunEnvs().getTrxn_branch()) != 0) { throw
		 * CaError.Eacct.BNAS1122(input.getBrchno(),
		 * CommTools.getBaseRunEnvs().getTrxn_branch()); } }
		 * 
		 * if ("NM" == servtp) { // CommTools.getBaseRunEnvs().setBusi_org_id(corpno);
		 * //设置开户机构所属法人代码设置环境变量中
		 * CommTools.getBaseRunEnvs().setTrxn_branch(input.getBrchno()); //设置开户机构到环境变量中
		 * 
		 * //开户机构是否是县级 by update cqm 只控制到县级机构 //if (!(cplBrchInfo.getBrchlv() ==
		 * E_BRCHLV.COUNT)) { // throw CaError.Eacct.BNAS0564(); //} } else {
		 * 
		 * //判断开户机构与交易机构是否统一法人 if (CommUtil.compare(corpno,
		 * CommTools.getBaseRunEnvs().getBusi_org_id()) != 0) { throw
		 * CaError.Eacct.BNAS1122(input.getBrchno(),
		 * CommTools.getBaseRunEnvs().getTrxn_branch()); } }
         */
        
        //开户机构与柜员所在交易机构(清算中心)是否一致，不一致报错
        if (CommUtil.compare(input.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch()) != 0) {
            throw CaError.Eacct.BNAS1122(input.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch());
        }

        //获取开户机构信息，并获取开户机构对应法人代码
        /*
		 * JF Modify：外调。 IoBrchInfo cplBrchInfo =
		 * SysUtil.getInstance(IoSrvPbBranch.class).getBranch(input.getBrchno()); String
		 * corpno = cplBrchInfo.getCorpno();
		 * 
		 * //判断开户机构与交易机构是否统一法人 if (CommUtil.compare(corpno,
		 * CommTools.getBaseRunEnvs().getBusi_org_id()) != 0) { throw
		 * CaError.Eacct.BNAS1122(input.getBrchno(),
		 * CommTools.getBaseRunEnvs().getTrxn_branch()); }
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
		/*
		 * //农信的省级机构暂不允许开立电子账户 if (E_BRCHLV.PROV == cplBrchInfo.getBrchlv()) { throw
		 * CaError.Eacct.BNAS1127(cplBrchInfo.getBrchno()); }
		 */

        // 未启用的机构不允许开户
        if (cplBrchInfo.getBrstus() != E_BRSTUS.valid) {
            throw CaError.Eacct.BNAS1208();
        }

        //校验证件类型、证件号码
        /*
		 * JF Delete：证件号已加密。 BusiTools.chkCertnoInfo(idtftp, idtfno);
         */

        // 根据账户分类获取开户产品号
        /*
		 * JF Modify：如果chanid渠道是JFSD-即富收单， baprcd基础负债产品取parm_value1的值（不计息活期负债产品）。
         */
        KnpParameter para = DpPublic.getProdcdByAccatp(accatp);
        if (E_USTYPE.AGENT == usertp || E_USTYPE.SALESMAN == usertp) {
        	property.setBaprcd(para.getParm_value2());
        } else {
        	property.setBaprcd(para.getParm_value1());
        }
        
//        property.setWaprcd(para.getParm_value2());

        /*
		 * JF Modify：客户信息在用户中心统一管理，核心不登记客户信息， 如需查询客户信息，需通过查询服务外调获取。
		 * 
		 * CifCust tbkCifCust = CifCustDao.selectOne_odb2(idtftp, idtfno, false);
		 * 
		 * property.setIfflag(E_YES___.YES);//by update cqm //修复销户后重新开户后报错的问题
		 * 
		 * // // 检查电子账户是否存在，并设置客户状态； if (CommUtil.isNotNull(tbkCifCust)) { KnaCust
		 * tbKnaCust = CaDao.selKnaCustByCustno(tbkCifCust.getCustno(),
		 * input.getCacttp(), false);//cacttp if (CommUtil.isNotNull(tbKnaCust)) {
		 * KnaAcdc tbKnaAcdc = KnaAcdcDao.selectFirst_odb3(tbKnaCust.getCustac(), true);
		 * // 修改账户状态 begin tbKnaCust.setAcctst(E_ACCTST.NORMAL);
		 * KnaCustDao.updateOne_odb1(tbKnaCust); // 修改账户状态 end
		 * property.setCustac(tbKnaCust.getCustac());
		 * property.setCustno(tbkCifCust.getCustno());
		 * property.setCardno(tbKnaAcdc.getCardno());
		 * 
		 * mod by xj 20180523 注释标志，使得同个客户支持开多个电子账号
		 * 
		 * // List<KnbOpac> tbknbOpacs = KnbOpacDao.selectAll_odb3(input.getChnlid(),
		 * false); // if (tbknbOpacs.size() == 1) { // for (KnbOpac knbOpac :
		 * tbknbOpacs) { // if (knbOpac.getChnlid()==E_CHNLID.PH ||
		 * knbOpac.getChnlid()==E_CHNLID.QB ||knbOpac.getChnlid()==E_CHNLID.XA) { //
		 * property.setIfflag(E_YES___.NO); // } // } // } // if
		 * (CommUtil.isNotNull(tbknbOpac)) { // if (tbknbOpac.getChnlid()==E_CHNLID.PH
		 * || tbknbOpac.getChnlid()==E_CHNLID.QB || tbknbOpac.getChnlid()==E_CHNLID.XA)
		 * { // property.setIfflag(E_YES___.NO); // } // } if
		 * (input.getChnlid()==E_CHNLID.QB || input.getChnlid()==E_CHNLID.XA) {
		 * property.setIfflag(E_YES___.NO); } //teleno } // 检查已存在客户是否已绑卡。 }
        */
        
        // 电子账户新增标志。
        property.setIfflag(E_YES___.YES);
        /*
         * JF Modify：根据mactid开户主体ID查询，判断是否需新增主账户。
         */
        KnaMaad tblKnaMaad = KnaMaadDao.selectOne_odb2(mactid, false);
        // 开户主体ID存在，不新增电子账户。
        KnaCust tbKnaCust = null;
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
			 * JF Delete KnaAcal tblKnaAcal =
			 * KnaAcalDao.selectOne_odb7(tbKnaCust.getCustac(), E_ACALTP.CELLPHONE, tlphno,
			 * E_ACALST.NORMAL, false); if (CommUtil.isNull(tblKnaAcal)) { throw
			 * DP.AT020005(); }
       	  */
  	  }
       
        /*
		 * JF Add：根据acctid品牌子账户ID查询，若存在，不开子户。 // POS品牌子账户新增标志。
		 * property.setAdsbfg(E_YES___ .YES); KnaSbad tblKnaSbad =
		 * KnaSbadDao.selectOne_odb2(acctid, false); if(CommUtil.isNotNull(tblKnaSbad))
		 * { property.setAdsbfg(E_YES___ .NO); }
         */
        
        KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("IsOpacctOne", "OpenWallet", "%", "%", false);
        property.setIonefg(E_YES___.NO);
        if (CommUtil.isNotNull(tblKnpParameter) && "1".equals(tblKnpParameter.getParm_value1())) {
            property.setIonefg(E_YES___.YES);
        }
    }

    /**
     * 
     * @Title: sedOpenInfoMessage
     * @Description: (开户消息通知)
     * @param input
     * @param property
     * @param output
     * @author xiongzhao
     * @date 2016年7月27日 上午9:59:52
     * @version V2.3.0
     */
	public static void sedOpenInfoMessage(Opaccd.Input input, Opaccd.Property property, Opaccd.Output output) {
		// TrxBaseEnvs.MessageRealInfo mri = (TrxBaseEnvs.MessageRealInfo)
		// SysUtil.getInstance(TrxBaseEnvs.MessageRealInfo.class);
//        mri.setMtopic("Q0101002");
//
		// ApSmsType.DpOpenUpgSendMsg openSendMsgInput = (ApSmsType.DpOpenUpgSendMsg)
		// SysUtil.getInstance(ApSmsType.DpOpenUpgSendMsg.class);
//
//        openSendMsgInput.setOpacrt(BaseEnumType.E_YES___.YES);
//        openSendMsgInput.setCustid(input.getCustid());
//        openSendMsgInput.setBrchno(input.getBrchno());
//        openSendMsgInput.setAccatp(input.getAccatp());
//        openSendMsgInput.setCktntp(CaEnumType.E_CKTNTP.OPEN);
//        openSendMsgInput.setMsgstr("开户通知消息，当前时间:" + DateUtil.getNow());
//
//        mri.setMsgtyp("ApSmsType.DpOpenUpgSendMsg");
//        mri.setMsgobj(openSendMsgInput);
//        AsyncMessageUtil.add(mri);
    }

    	/*	
	 * public static void sedOpenInfoMessage( final
	 * cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Input input, final
	 * cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Property property, final
	 * cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Output output) {
	 * 
	 * //消息推送至APP客户端 MessageRealInfo mri =
	 * SysUtil.getInstance(MessageRealInfo.class); mri.setMtopic("Q0101005");
	 * //mri.setTdcnno("R00"); //测试指定DCN ToAppSendMsg toAppSendMsg =
	 * SysUtil.getInstance(ToAppSendMsg.class);
	 * 
	 * // 消息内容 toAppSendMsg.setUserId(input.getCustid()); //用户ID
	 * toAppSendMsg.setOutNoticeId("Q0101005"); //外部消息ID
	 * toAppSendMsg.setNoticeTitle("电子账户开立成功"); //公告标题
	 * toAppSendMsg.setContent("恭喜您的ThreeBank电子账户已成功开立，开户行为金谷农商银行。"); //公告内容
	 * toAppSendMsg.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date()+BusiTools.
	 * getBusiRunEnvs().getTrantm()); //消息生成时间
	 * toAppSendMsg.setTransType(E_APPTTP.CUACCH); //交易类型
	 * toAppSendMsg.setTirggerSys(SysUtil.getSystemId()); //触发系统
	 * toAppSendMsg.setClickType(E_CLIKTP.NO); //点击动作类型
	 * //toAppSendMsg.setClickValue(clickValue); //点击动作值
	 * 
	 * mri.setMsgtyp("ApSmsType.ToAppSendMsg"); mri.setMsgobj(toAppSendMsg);
	 * AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
	 * 
	 * /* 2017.12.12 由于开户流程变更 ，所以需要注释掉 MessageRealInfo mri =
	 * SysUtil.getInstance(MessageRealInfo.class); mri.setMtopic("Q0101002");
	 * //mri.setTdcnno("R00"); //测试指定DCN DpOpenUpgSendMsg openSendMsgInput =
	 * SysUtil.getInstance(DpOpenUpgSendMsg.class);
	 * 
	 * // 消息内容 //openSendMsgInput.setTdcnno("R00");//DCN号
	 * openSendMsgInput.setOpacrt(E_YES___.YES);// 是否开户成功
	 * openSendMsgInput.setCustid(input.getCustid());// 用户ID
	 * openSendMsgInput.setBrchno(input.getBrchno());// 机构号
	 * openSendMsgInput.setAccatp(input.getAccatp());// 账户分类
	 * openSendMsgInput.setCktntp(E_CKTNTP.OPEN);// 交易类型
	 * openSendMsgInput.setMsgstr("开户通知信息，当前时间："+DateUtil.getNow());
	 * 
	 * mri.setMsgtyp("ApSmsType.DpOpenUpgSendMsg"); mri.setMsgobj(openSendMsgInput);
	 * AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
    *//*
		 * // E_MEDIUM mssdtp = E_MEDIUM.E_QUEUE; // 消息媒介
		 * 
		 * 
		 * /*KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("OPUPGD",
		 * "CUSTSM", "%", "%", true);
		 * 
		 * String bdid = tblKnaPara.getParm_value1();// 服务绑定ID
		 * 
		 * IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind(
		 * IoCaOtherService.class, bdid);
		 * 
		 * // 1.开户成功发送开户结果到客户信息 String mssdid = CommTools.getMySysId();// 消息ID String
		 * mesdna = tblKnaPara.getParm_value2();// 媒介名称
		 * 
		 * IoCaOtherService.IoCaOpenUpgSendMsg.InputSetter openSendMsgInput =
		 * SysUtil.getInstance(IoCaOtherService.IoCaOpenUpgSendMsg.InputSetter.class);
		 * 
		 * // openSendMsgInput.setMedium(mssdtp); // 消息媒介
		 * openSendMsgInput.setMsgid(mssdid); // 发送消息ID
		 * openSendMsgInput.setMdname(mesdna); // 媒介名称
		 * openSendMsgInput.setOpacrt(E_YES___.YES);// 是否开户成功
		 * openSendMsgInput.setCustid(input.getCustid());// 用户ID
		 * openSendMsgInput.setBrchno(input.getBrchno());// 机构号
		 * openSendMsgInput.setAccatp(input.getAccatp());// 账户分类
		 * openSendMsgInput.setCktntp(E_CKTNTP.OPEN);// 交易类型
		 * 
		 * caOtherService.openUpgSendMsg(openSendMsgInput);
		 * 
		 * // 2.开户成功发送协议到合约库 KnpParameter tblKnaPara1 =
		 * KnpParameterDao.selectOne_odb1("OPUPGD", "AGRTSM", "%", "%", true);
		 * 
		 * String mssdid1 = CommTools.getMySysId();// 消息ID String mesdna1 =
		 * tblKnaPara1.getParm_value2();// 媒介名称
		 * 
		 * IoCaOtherService.IoCaSendContractMsg.InputSetter openSendAgrtInput =
		 * SysUtil.getInstance(IoCaOtherService.IoCaSendContractMsg.InputSetter.class);
		 * 
		 * String sAgdata = input.getIdtftp() + "|" + input.getIdtfno() + "|" +
		 * input.getCustna() + "|" + property.getCardno() + "|" + input.getAccatp() +
		 * "|" + input.getBrchno() + "|" + CommTools.getBaseRunEnvs().getTrxn_date();//
		 * 协议回填字段
		 * 
		 * // openSendAgrtInput.setMedium(mssdtp); // 消息媒介
		 * openSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
		 * openSendAgrtInput.setMdname(mesdna1); // 媒介名称
		 * openSendAgrtInput.setUserId(input.getCustid());// 用户ID
		 * openSendAgrtInput.setOpenOrg(input.getBrchno());// 机构号
		 * openSendAgrtInput.setAcctNo(property.getCardno());// 电子账号
		 * openSendAgrtInput.setAcctName(input.getCustna());// 户名
		 * openSendAgrtInput.setRecordCount(ConvertUtil.toInteger(1));// 记录数
		 * openSendAgrtInput.setOpenFlag(E_CKTNTP.OPEN);// 开户升级标志
		 * openSendAgrtInput.setTransTime(DateTools2.getCurrentTimestamp());// 操作时间
		 * 
		 * IoCaAgrtInfos cplAgrtInfos = SysUtil.getInstance(IoCaAgrtInfos.class);
		 * cplAgrtInfos.setAgrTemplateNo(input.getAgrtno());// 协议模板编号
		 * cplAgrtInfos.setVersion(input.getVesion());// 版本号
		 * cplAgrtInfos.setAgrData(sAgdata);// 协议回填字段
		 * openSendAgrtInput.getAgreementList().add(cplAgrtInfos);// 协议列表
		 * 
		 * caOtherService.sendContractMsg(openSendAgrtInput);
		 * 
		 * // 3.开立二类户成功将理财签约相关信息发送到合约库 if (input.getAccatp() == E_ACCATP.FINANCE) {
		 * KnpParameter tblKnaPara3 = KnpParameterDao.selectOne_odb1("OPENMN", "AGRTSM",
		 * "%", "%", true);
		 * 
		 * String mssdid2 = CommTools.getMySysId();// 消息ID String mesdna2 =
		 * tblKnaPara3.getParm_value2();// 媒介名称
		 * 
		 * IoCaOtherService.IoCaSendMonMsg.InputSetter sendMonMsgInput =
		 * SysUtil.getInstance(IoCaOtherService.IoCaSendMonMsg.InputSetter.class);
		 * 
		 * // sendMonMsgInput.setMedium(mssdtp); // 消息媒介
		 * sendMonMsgInput.setMsgid(mssdid2); // 发送消息ID
		 * sendMonMsgInput.setMdname(mesdna2); // 媒介名称
		 * sendMonMsgInput.setUserId(input.getCustid());// 用户ID
		 * sendMonMsgInput.setAcctNo(property.getCardno());// 电子账号
		 * sendMonMsgInput.setAcctName(input.getCustna());// 客户姓名
		 * sendMonMsgInput.setCertNo(input.getIdtfno());// 证件号码
		 * sendMonMsgInput.setCertType(input.getIdtftp());// 证件类型
		 * sendMonMsgInput.setTransBranch(input.getBrchno());// 机构编号
		 * sendMonMsgInput.setMobileNo(input.getTeleno());// 注册手机号
		 * sendMonMsgInput.setTransTime(DateTools2.getCurrentTimestamp());//操作时间
		 * 
		 * caOtherService.sendMonMsg(sendMonMsgInput); }
		 */

    //创建连接配置对象，并初始化连接配置信息
    //		LTTSConnectionConfig connectionConfig = new LTTSConnectionConfig();
	// connectionConfig.setURL("failover:(tcp://158.222.68.167:61616,tcp://158.222.68.166:61616,tcp://158.222.68.169:61616)");
	// //URL
    //		connectionConfig.setUserName(""); //用户名
    //		connectionConfig.setPassword(""); //密码
    //		connectionConfig.setMaxConnections(5); //
    //		connectionConfig.setUseAsyncSend(false); //
    //		connectionConfig.setSendTimeout(0); //
    //		connectionConfig.setMaximumActiveSessionPerConnection(100); //
    //		connectionConfig.setReconnectOnException(true); //
    //		connectionConfig.setIdleTimeout(30000); //
    //		connectionConfig.setExpiryTimeout(0); //
    //		connectionConfig.setCloseTimeout(1500); //
    //		
    //		//创建链接工厂
	// LTTSConnectionFactory connectionFactory = new
	// LTTSConnectionFactory(connectionConfig);
    //		
    //		//创建发布消息连接配置对象，并初始化连接配置
    //		LTTSMessageConfig messageConfig = new LTTSMessageConfig();
	// messageConfig.setDestination("zjrcu.dap.nas.tran.acc.opaccd.openrt");
	// //发布消息包路径，需要询问具体地址
    //		messageConfig.setPriority(4); //优先级，先默认为4级
    //		messageConfig.setPersistent(false); //消息永久行，暂时配置为false
    //		messageConfig.setTransacted(false); //
    //		messageConfig.setTimeToLive(ConvertUtil.toLong(0)); //
    //		messageConfig.setEnableMsgExpHandler(false); //
    //		
    //		
    //		//创建消息发送器
	// LTTSMessagePublisher publisher = new LTTSMessagePublisher(connectionFactory,
	// messageConfig);
    //		
    //		try{
    //			//创建消息消息文本对象
    //			LTTSMessage message = publisher.createMessage();
    //			message.setMessgeType(LTTSMessage.TYPE_TEXT); //传递消息类型，设置为传递文本消息
    //			
    //			//文本消息为json字符串，先设置为数据集再转换成json字符串
    //			HashMap<String, String> messageMap = new HashMap<String,String>();
    //			messageMap.put("idtftp", input.getIdtftp().getValue());
    //			messageMap.put("idtfno", input.getIdtfno());
    //			String messageText = JsonUtil.format(messageMap); //数据集转换成json字符串
    //			message.setTextMsg(messageText); //设置传递消息内容
    //			
    //			publisher.send(message); //发送消息
    //		}catch (Exception e){
    //			throw new RuntimeException("消息发送失败", e);
    //		}

    //	}

    public static void prcOpaccdOut(final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Input input,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Property property,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Output output) {
        output.setCardno(property.getCardno());
        output.setCustac(property.getCustac());
        output.setBrchno(input.getBrchno());
        output.setMactid(input.getMactid());
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
    
        //del by lishuyao
        /*if (E_IDCKRT.SUCCESS == input.getIdckrt() && E_MPCKRT.SUCCESS == input.getMpckrt()) {
            output.setOpacrt(E_OPACRT.SUCCESS);
        } else if (E_IDCKRT.CHECKING == input.getIdckrt() || E_MPCKRT.CHECKING == input.getMpckrt()) {
            output.setOpacrt(E_OPACRT.PREOPEN);
        } else {
            output.setOpacrt(E_OPACRT.FAILD);
        }*/

        //poc增加审计日志
        /*
		 * JF Modify：审件登记暂时注释掉。 ApDataAudit apAudit =
		 * SysUtil.getInstance(ApDataAudit.class);
		 * apAudit.regLogOnInsertBusiPoc(property.getCardno());
         */
    }

    /**
     * 
     * @Title: chkBdcart
     * @Description: (检查绑卡认证标志)
     * @param input
     * @param property
     * @param output
     * @author xiongzhao
     * @date 2017年1月4日 下午8:55:58
     * @version V2.3.0
     */
	public static void chkBdcart(final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Input input,
            final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Property property,
            final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Output output) {
    	/*
		 * JF Modify：服务商开户不传绑卡信息。 Options<IoCaSrvGenBindCard> lstBindcaInfoList =
		 * input.getLstBindcaInfo(); E_YES___ eBdrtfg = E_YES___.NO; if
		 * (CommUtil.isNotNull(lstBindcaInfoList) && lstBindcaInfoList.size() > 0) { for
		 * (IoCaSrvGenBindCard info : lstBindcaInfoList) { if (info.getBdcart() ==
		 * E_BDCART.SUCCESS) { eBdrtfg = E_YES___.YES; property.setBdrtfg(eBdrtfg); } }
		 * } else { eBdrtfg = E_YES___.YES; property.setBdrtfg(eBdrtfg); }
		 */
    	E_YES___ eBdrtfg = E_YES___.YES;
		property.setBdrtfg(eBdrtfg);
	
    }

    /**
     * 开户短信通知
	 * 
     * @param input
     * @param property
     * @param output
     */
	public static void sendOpenMessage(final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Input input,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Property property,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Output output) {
		IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
//		cplKubSqrd.setAppsid();//APP推送ID 
		cplKubSqrd.setCardno(property.getCardno());//交易卡号  
//		cplKubSqrd.setPmvl01();//参数01    
//		cplKubSqrd.setPmvl02();//参数02    
//		cplKubSqrd.setPmvl03();//参数03    
//		cplKubSqrd.setPmvl04();//参数04    
//		cplKubSqrd.setPmvl05();//参数05    
		cplKubSqrd.setPrcscd(CommTools.getBaseRunEnvs().getTrxn_code());//内部交易码
		cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期  
		cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());//交易流水  
		cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());//交易时间  
		/*
		 * JF Modify：外调服务暂时注释 IoPbSmsSvcType svcType =
		 * SysUtil.getInstance(IoPbSmsSvcType.class); svcType.pbTransqReg(cplKubSqrd);
		 */
	}

	/**
	 * JF Add：根据品牌信息列表循环开立品牌子账户。
	 * 
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void openSubaccts(final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Input input,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Property property,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Output output) {

		E_CUSACT cacttp = input.getCacttp();
		String custno = input.getCustno();
//		String acctna = input.getCustna();
		String mactid = input.getMactid();
		String crcycd = input.getCrcycd();
		String custac = property.getCustac();
		String prodcd = property.getBaprcd();
		E_TERMCD depttm = property.getDepttm();
		E_YES___ opacfg = property.getOpacfg();
		E_CUSTTP custtp = property.getCusttp();
		E_PRODTP prodtp = property.getProdtp();
		Options<BrandsInfo> lsBrand= input.getBrandList();

		// 账户服务实例。
		DpAcctSvcType dpAcctService  = SysUtil.getInstance(DpAcctSvcType.class);
		// 新增负债账户输入。
		DpAcctSvcType.AddAcct.InputSetter openSubInput =  SysUtil.getInstance(DpAcctSvcType.AddAcct.InputSetter.class);

		// 电子账户服务实现。
		IoCaSrvGenEAccountInfo eacctService = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
		// 负债子账号与电子账户关联服务输入。
		IoCaAddEARelaIn acctRelationInput = SysUtil.getInstance(IoCaAddEARelaIn.class);

		// 登记电子子账户附加信息。
		KnaSbad subaddInfo = SysUtil.getInstance(KnaSbad.class);
		// 返回子户信息。
		Options<BrandsInfo> opAcctnos = new DefaultOptions<BrandsInfo>();

		// 循环遍历品牌信息，判断是否开子户。
		for(BrandsInfo brand : lsBrand) {
			KnaSbad tblKnaSbad  = KnaSbadDao.selectOne_odb2(brand.getAcctid(), false);
			if(CommUtil.isNotNull(tblKnaSbad)) {
				continue;
			}
		
			/*
			 *  开子户。
			 */
			String acctNa = brand.getAcctna();
			openSubInput.setCacttp(cacttp);
			openSubInput.setCustno(custno);
			openSubInput.setAcctna(acctNa);
			openSubInput.setMactid(mactid);
			openSubInput.setCrcycd(crcycd);
			openSubInput.setCustac(custac);
			openSubInput.setProdcd(prodcd);
			openSubInput.setDepttm(depttm);
			openSubInput.setOpacfg(opacfg);
			openSubInput.setCusttp(custtp);
			// 新增负债账户方法。
			AddSubAcctOut  openSubOut  = dpAcctService.addAcct(openSubInput);
			// 新增负债账户输出。
			String acctno = openSubOut.getAcctno();
			E_FCFLAG pddpfg = openSubOut.getPddpfg();

			/*
			 *  建立映射关系。
			 */
			acctRelationInput.setAcctno(acctno);
			acctRelationInput.setCustac(custac);
			acctRelationInput.setFcflag(pddpfg);
			acctRelationInput.setProdtp(prodtp);
			acctRelationInput.setProdcd(prodcd);
			acctRelationInput.setCrcycd(crcycd);
			// 账户关系建立方法。
			eacctService.prcAddEARela(acctRelationInput);
			
			/*
			 *  登记子账户附加信息。
			 */
			subaddInfo.setAcctid(brand.getAcctid());
			subaddInfo.setAcctno(acctno);
			subaddInfo.setCustac(custac);
			subaddInfo.setMactid(mactid);
			subaddInfo.setSbrand(brand.getSbrand());
			subaddInfo.setBradna(brand.getBradna());
			subaddInfo.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
			subaddInfo.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp());
			subaddInfo.setAcctna(acctNa);
//			subaddInfo.setTmacctna(DecryptConstant.maskName(acctna));


			KnaSbadDao.insert(subaddInfo);
			
			// 返回子户列表信息。
			BrandsInfo acctnoInfo = SysUtil.getInstance(BrandsInfo.class);
			acctnoInfo.setAcctid(brand.getAcctid());
			acctnoInfo.setAcctno(acctno);
			opAcctnos.add(acctnoInfo);
			
		}
		output.setAcctnoList(opAcctnos);
		
	}

	public static void bindmq(final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Input input,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Property property,
			final cn.sunline.ltts.busi.dptran.trans.online.intf.Opaccd.Output output) {
		// TODO.
	}
}