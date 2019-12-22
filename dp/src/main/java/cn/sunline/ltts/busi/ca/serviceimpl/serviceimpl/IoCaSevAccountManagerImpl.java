package cn.sunline.ltts.busi.ca.serviceimpl.serviceimpl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.eacct.process.CaEAccountProc;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbAgnt;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbAgntDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbCgbr;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbCgbrDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbCksq;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbCksqDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbProm;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbPromDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbSlep;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbSlepDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountManager.IoCaAccQrcqifQuery.Input;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountManager.IoCaAccQrcqifQuery.Output;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCustQrckif;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaagntinfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbCgbr;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbCksq;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbSlep;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaOpenAccInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PROCST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CKTNTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_IDCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MPCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PROMST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PROMSV;
 /**
  * 电子账户功能管理服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoCaSevAccountManagerImpl", longname="电子账户功能管理服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoCaSevAccountManagerImpl implements cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountManager{
 	/**
	 * 
	 * @author xiongzhao
	 *         <p>
	 *         <li>2016年6月21日-下午6:03:40</li>
	 *         <li>功能描述：电子账户客户端升级</li>
	 *         </p>
	 * 
	 * @param cplAcUpg
	 */
	
	public void upgAccount(
			final cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAccountUpIn cplAcUpg) {
		
		String sCardno = cplAcUpg.getCardno();// 电子账号
		E_ACCATP eOdactp = cplAcUpg.getOdactp();// 原账户分类
		E_ACCATP eUpactp = cplAcUpg.getUpactp();// 升级账户分类
		E_IDCKRT eIdckrt = cplAcUpg.getIdckrt();// 身份核查结果
		E_MPCKRT eMpckrt = cplAcUpg.getMpckrt();// 人脸识别结果
		E_PROMSV ePromsv = cplAcUpg.getPromsv();// 升级渠道
		E_YES___ eRisklv = cplAcUpg.getRisklv();// 风险承受等级
		E_YES___ eBdcafg = cplAcUpg.getBdcafg();// 是否绑卡认证通过结果
		String sCustac = null;// 电子账号ID
		
		// 根据电子账号查询出电子账号ID
		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(sCardno, false);
		if (CommUtil.isNull(tblKnaAcdc)) {
			throw CaError.Eacct.BNAS1279();
		}
		sCustac = tblKnaAcdc.getCustac();//电子账号ID
		
		KnaCust tblKnaCust = SysUtil.getInstance(KnaCust.class);
		tblKnaCust = KnaCustDao.selectOne_odb1(sCustac, false);
		
		// 检查查询电子账户表记录是否为空
		if (CommUtil.isNull(tblKnaCust)) {
			throw CaError.Eacct.BNAS1279();
		}
		
		// 检查电子账户状态
		E_CUACST eCustac = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(sCustac);
		if (eCustac != E_CUACST.NORMAL && eCustac != E_CUACST.NOACTIVE) {
			throw CaError.Eacct.BNAS0441();
		}
		
		// 调用DP模块服务查询冻结状态，检查电子账户状态字
		IoDpFrozSvcType froz = SysUtil.getInstance(IoDpFrozSvcType.class);
		IoDpAcStatusWord cplGetAcStWord = froz.getAcStatusWord(sCustac);
		if (cplGetAcStWord.getClstop() == E_YES___.YES) {
			throw CaError.Eacct.BNAS0441();
		}
		
		//获取电子账户分类
		E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
				.qryAccatpByCustac(sCustac);

		// 检查升级账户分类
		if (eAccatp != eOdactp) {
			throw CaError.Eacct.BNAS1676();
		}
		if (eOdactp == E_ACCATP.WALLET) {
			if (eUpactp != E_ACCATP.FINANCE && eUpactp != E_ACCATP.GLOBAL) {
				throw CaError.Eacct.BNAS1677();
			}
		}
		if (eOdactp == E_ACCATP.FINANCE) {
			if (eUpactp != E_ACCATP.GLOBAL) {
				throw CaError.Eacct.BNAS1677();
			}
		}
		if (eOdactp == E_ACCATP.GLOBAL) {
			throw CaError.Eacct.BNAS1678();
		}

		// 升级机构检查 
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("UpgControl", "%","%","%", true);
		if (CommUtil.equals("Y", tblKnpParameter.getParm_value1())) {
			if(!CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(), tblKnaCust.getCorpno())) {
				throw CaError.Eacct.BNAS1679();
			}
		}
		

		// 查询线上升级记录
		KnbProm tblKnbProm = KnbPromDao.selectFirst_odb1(sCustac,
				E_PROMSV.TELCLIENT, E_PROMST.ACCEPTED, false);
		
		if (CommUtil.isNotNull(tblKnbProm)) {
			// 检查升级记录是否有效
			if (tblKnbProm.getIdckrt() == E_IDCKRT.CHECKING
					|| tblKnbProm.getMpckrt() == E_MPCKRT.CHECKING) {
				throw CaError.Eacct.BNAS1680();
			}
		}
		
		KnbProm tblKnbProm1 = SysUtil.getInstance(KnbProm.class);
		// 查询线下升级记录
		KnbProm tblKnbProm2 = KnbPromDao.selectFirst_odb1(sCustac,
				E_PROMSV.INCASE, E_PROMST.APPLY, false);

		if (CommUtil.isNotNull(tblKnbProm2)) {
			// 检查升级记录是否有效
			if (ePromsv == E_PROMSV.INCASE) {
				throw CaError.Eacct.BNAS1681();
			}

			// 若再次提交线上升级，将原线上升级纪录置为作废
			if (ePromsv == E_PROMSV.TELCLIENT) {
				tblKnbProm2.setPromst(E_PROMST.CANCEL);
				KnbPromDao.updateOne_odb4(tblKnbProm2);
			}
		}
		
		KnbProm tblKnbProm3 = KnbPromDao.selectFirst_odb1(sCustac,
				E_PROMSV.INCASE, E_PROMST.ACCEPTED, false);
		if (CommUtil.isNotNull(tblKnbProm3)) {
			throw CaError.Eacct.BNAS1682();
		}

		// 若升级渠道为柜面，直接登记升级登记簿
		if (ePromsv == E_PROMSV.INCASE) {
			tblKnbProm1.setCustac(sCustac);// 电子账号
			tblKnbProm1.setCustna(tblKnaCust.getCustna());// 客户名称
			tblKnbProm1.setOdactp(eOdactp);// 原账户分类
			tblKnbProm1.setUpactp(eUpactp);// 升级账户分类
			tblKnbProm1.setBrchno(tblKnaCust.getBrchno());// 账户归属机构
			tblKnbProm1.setAplydt(CommTools.getBaseRunEnvs().getTrxn_date());// 申请日期
			tblKnbProm1.setPromsv(ePromsv);// 升级渠道
			tblKnbProm1.setRisklv(eRisklv);// 风险承受等级
			tblKnbProm1.setPromst(E_PROMST.APPLY);// 升级状态
			tblKnbProm1.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
			tblKnbProm1.setMesssq(CommTools.getBaseRunEnvs().getTrxn_seq());
			
			// 插入升级登记簿
			KnbPromDao.insert(tblKnbProm1);
		}
		
		// 若升级渠道为手机端，检查身份核查结果和人脸识别结果是否通过
		else if (ePromsv == E_PROMSV.TELCLIENT) {
			
			// 身份核查结果和人脸识别结果和绑卡认证结果有一个失败，升级失败
			if (eIdckrt == E_IDCKRT.FAILD || eMpckrt == E_MPCKRT.FAILD || eBdcafg == E_YES___.NO) {
				throw CaError.Eacct.BNAS1683();
			}
			
			// 身份核查结果和人脸识别结果和绑卡认证结果有一个核查中，登记升级登记簿
			if ((eIdckrt == E_IDCKRT.CHECKING || eMpckrt == E_MPCKRT.CHECKING) && eBdcafg == E_YES___.YES) {
				
				// 登记升级登记簿
				tblKnbProm1.setCustac(sCustac);// 电子账号
				tblKnbProm1.setCustna(tblKnaCust.getCustna());// 客户名称
				tblKnbProm1.setIdckrt(eIdckrt);// 身份核查结果
				tblKnbProm1.setMpckrt(eMpckrt);// 人脸识别结果
				tblKnbProm1.setOdactp(eOdactp);// 原账户分类
				tblKnbProm1.setUpactp(eUpactp);// 升级账户分类
				tblKnbProm1.setBrchno(tblKnaCust.getBrchno());// 账户归属机构
				tblKnbProm1.setAplydt(CommTools.getBaseRunEnvs().getTrxn_date());// 申请日期
				tblKnbProm1.setPromsv(ePromsv);// 升级渠道
				tblKnbProm1.setPromdt(CommTools.getBaseRunEnvs().getTrxn_date());// 升级日期
				tblKnbProm1.setRisklv(eRisklv);// 风险承受等级
				tblKnbProm1.setPromst(E_PROMST.ACCEPTED);// 升级状态
				tblKnbProm1.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
				tblKnbProm1.setMesssq(CommTools.getBaseRunEnvs().getTrxn_seq());
				
				// 插入升级登记簿
				KnbPromDao.insert(tblKnbProm1);
				
			}
			
			// 身份核查结果和人脸识别结果和绑卡认证结果都为成功，升级成功
			if (eIdckrt == E_IDCKRT.SUCCESS && eMpckrt == E_MPCKRT.SUCCESS && eBdcafg == E_YES___.YES) {
				
				// 登记升级登记簿
				tblKnbProm1.setCustac(sCustac);// 电子账号
				tblKnbProm1.setCustna(tblKnaCust.getCustna());// 客户名称
				tblKnbProm1.setIdckrt(eIdckrt);// 身份核查结果
				tblKnbProm1.setMpckrt(eMpckrt);// 人脸识别结果
				tblKnbProm1.setOdactp(eOdactp);// 原账户分类
				tblKnbProm1.setUpactp(eUpactp);// 升级账户分类
				tblKnbProm1.setBrchno(tblKnaCust.getBrchno());// 账户归属机构
				tblKnbProm1.setAplydt(CommTools.getBaseRunEnvs().getTrxn_date());// 申请日期
				tblKnbProm1.setPromsv(ePromsv);// 升级渠道
				tblKnbProm1.setPromdt(CommTools.getBaseRunEnvs().getTrxn_date());// 升级日期
				tblKnbProm1.setRisklv(eRisklv);// 风险承受等级
				tblKnbProm1.setPromst(E_PROMST.SUCCESS);// 升级状态
				tblKnbProm1.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
				tblKnbProm1.setMesssq(CommTools.getBaseRunEnvs().getTrxn_seq());
				
				// 插入升级登记簿
				KnbPromDao.insert(tblKnbProm1);
			}
		}
		else {
			throw CaError.Eacct.BNAS1684();
		}
	}

	/**
	 * 
	 * @Title: incaseUpgrade 
	 * @Description:(电子账户柜面/移动平板升级) 
	 * @param custac 电子账号ID
	 * @param idckrt 身份核查结果
	 * @param mpckrt 人脸识别结果
	 * @author xiongzhao 
	 * @date 2016年7月7日 上午10:01:03 
	 * @version V2.3.0
	 */
	@Override
	public void incaseUpgrade(String custac, E_IDCKRT idckrt, E_MPCKRT mpckrt) {
		
		String timetm =DateTools2.getCurrentTimestamp();

		// 查询升级登记簿
		KnbProm tblKnbProm = KnbPromDao.selectFirst_odb1(custac,
				E_PROMSV.INCASE, E_PROMST.APPLY, false);
		if (CommUtil.isNull(tblKnbProm)) {
			throw CaError.Eacct.BNAS0362();
		}
		
		// 查询线上升级记录
		KnbProm tblKnbProm1 = KnbPromDao.selectFirst_odb1(custac,
				E_PROMSV.TELCLIENT, E_PROMST.ACCEPTED, false);
		
		if (CommUtil.isNotNull(tblKnbProm1)) {
			// 检查升级记录是否有效
			if (tblKnbProm.getIdckrt() == E_IDCKRT.CHECKING
					|| tblKnbProm.getMpckrt() == E_MPCKRT.CHECKING) {
				throw CaError.Eacct.BNAS1680();
			}
		}
		
		// 查询线下升级记录
		KnbProm tblKnbProm2 = KnbPromDao.selectFirst_odb1(custac,
				E_PROMSV.INCASE, E_PROMST.ACCEPTED, false);

		if (CommUtil.isNotNull(tblKnbProm2)) {
			// 检查升级记录是否有效
				throw CaError.Eacct.BNAS1680();
			}

		// 查询电子账户表信息
		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(custac, false);
		if (CommUtil.isNull(tblKnaCust)) {
			throw CaError.Eacct.BNAS1685();
		}

		// 检查电子账户状态
		E_CUACST eCustac = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
		if (eCustac != E_CUACST.NORMAL && eCustac != E_CUACST.NOACTIVE) {
			throw CaError.Eacct.BNAS1686();
		}

		// 调用DP模块服务查询冻结状态，检查电子账户状态字
		IoDpFrozSvcType froz = SysUtil.getInstance(IoDpFrozSvcType.class);
		IoDpAcStatusWord cplGetAcStWord = froz.getAcStatusWord(custac);
		if (cplGetAcStWord.getClstop() == E_YES___.YES) {
			throw CaError.Eacct.BNAS1686();
		}
		
		// 升级机构检查 
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("UpgControl", "%","%","%", false);
		if (CommUtil.equals("Y", tblKnpParameter.getParm_value1())) {
			if(!CommUtil.equals(CommTools.getBaseRunEnvs().getBusi_org_id(), tblKnaCust.getCorpno())) {
				throw CaError.Eacct.BNAS1679();
			}
		}
		
		// 获取电子账户分类，检查升级账户分类
		E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(
				custac);
		if (eAccatp != tblKnbProm.getOdactp()) {
			throw CaError.Eacct.BNAS1676();
		}
		
		// 检查身份核查结果和人脸识别结果是否通过
		// 身份核查结果和人脸识别结果有一个失败，升级失败
		if (idckrt == E_IDCKRT.FAILD || mpckrt == E_MPCKRT.FAILD) {
			throw CaError.Eacct.BNAS1683();
		}
		
		// 身份核查结果和人脸识别结果有一个为核查中，更新升级登记簿信息
		if (idckrt == E_IDCKRT.CHECKING || mpckrt == E_MPCKRT.CHECKING) {
			
			tblKnbProm.setIdckrt(idckrt);// 身份核查标志
			tblKnbProm.setMpckrt(mpckrt);// 人脸识别标志
			tblKnbProm.setPromst(E_PROMST.ACCEPTED);// 升级状态
			tblKnbProm.setPromdt(CommTools.getBaseRunEnvs().getTrxn_date());// 升级日期
			
			// 更新升级登记簿
			KnbPromDao.updateOne_odb4(tblKnbProm);
			
		}
		
		// 身份核查结果和人脸识别结果均为成功，更新升级登记簿信息
		if (idckrt == E_IDCKRT.SUCCESS && mpckrt == E_MPCKRT.SUCCESS) {
			
			// 更新升级登记簿
			CaDao.updKnbProm(idckrt, mpckrt, E_PROMST.SUCCESS, CommTools.getBaseRunEnvs().getTrxn_date(), 
					CommTools.getBaseRunEnvs().getMain_trxn_seq(), custac, E_PROMST.APPLY, timetm);

		}

	}
	/**
	 * 
	 * @author leipeng
	 * 		<p>
	 *	    <li>2016年6月27日-下午7:33:21</li>
	 *      <li>功能描述：查询人工审核信息</li>
	 *      </p>
	 * 
	 * @param cumano
	 * @param custac
	 * @param idckrt
	 * @param mpckrt
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void accQrcqifQuery(Input input, Output output) {
		long counts = 0;// 记录总数
		int pageno = input.getPageno();
		int pagesz = input.getPgsize();
		String revist = String.valueOf(input.getRevist());
		long totlCount = 0;
		String custno = "%";
		
		// 检查输入接口必输项是否为空
		if (CommUtil.isNull(input.getBgindt())) {
			throw CaError.Eacct.BNAS1642();
		}
		
		if (CommUtil.isNull(input.getEndddt())) {
			throw CaError.Eacct.BNAS0061();
		}
		
		if (DateUtil.compareDate(input.getBgindt(), CommTools.getBaseRunEnvs().getTrxn_date()) > 0) {
			throw CaError.Eacct.BNAS1687();
		}
		
		if (DateUtil.compareDate(input.getBgindt(), input.getEndddt()) > 0) {
			throw CaError.Eacct.BNAS1688();
		}
		
		if (CommUtil.isNull(pageno)){
			throw CaError.Eacct.BNAS0406();
		}
		
		if (CommUtil.isNull(pagesz)) {
			throw CaError.Eacct.BNAS0460();
		}
		
        if(CommUtil.isNull(input.getRevist())){
        	revist = "%";
		}
        
        //校验证件类型、证件号码
        BusiTools.chkCertnoInfo(input.getIdtftp(), input.getIdtfno());
        
		//查询客户信息
		if(CommUtil.isNotNull(input.getIdtftp())&&CommUtil.isNotNull(input.getIdtfno())){
			//取消掉客户信息相关内容，业务模块拆分
//			IoSrvCfPerson.IoGetCifCust.InputSetter queryCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//			IoSrvCfPerson.IoGetCifCust.Output cust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//			IoSrvCfPerson cifCustServ = SysUtil.getInstance(IoSrvCfPerson.class);
//			queryCifCust.setIdtftp(input.getIdtftp());
//			queryCifCust.setIdtfno(input.getIdtfno());
//			cifCustServ.getCifCust(queryCifCust, cust);
//			
//			if (CommUtil.isNull(cust)){
//				throw CaError.Eacct.BNAS0722();
//		    }
//			
//			
//			
//			custno = cust.getCustno();
		}
		
		
		//分页查询开户升级信息
		Page<IoCaKnaCustQrckif> listinfo= EacctMainDao.selCustPromMpckrt(revist, custno, input.getBgindt(),
				input.getEndddt(), (pageno-1L)*pagesz, pagesz,totlCount,true);
		
		Options<IoCaKnaCustQrckif> info = SysUtil.getInstance(Options.class);
		
		info.addAll(listinfo.getRecords());
		
		counts = counts+listinfo.getRecordCount();
		
		for (IoCaKnaCustQrckif listinf: info) {
			if(CommUtil.equals(String.valueOf(listinf.getAccttp()), "1")){//全功能账户
				
				listinf.setRevids("开立全功能账户");
			}else if(CommUtil.equals(String.valueOf(listinf.getAccttp()), "2")){//理财功能户
				
				listinf.setRevids("开立理财功能账户");
			}else if(CommUtil.equals(String.valueOf(listinf.getAccttp()), "3")){//电子钱包账户
				
				listinf.setRevids("开立电子钱包账户");
			}

		}
	   /* //查询电子账户信息表
		Page<IoCaKnaCustQrckif> listinfo= EacctMainDao.selKnaCustMpckrtt(input.getRevist(), custno, input.getBgindt(),
				input.getEndddt(), (pageno-1)*pagesz, pagesz,totlCount,true);
		info.addAll(listinfo.getRecords());
		
		counts = counts+listinfo.getRecordCount();
		
		for (IoCaKnaCustQrckif listinf: info) {
			listinf.setRevitp(E_REVITP.kh);//开户类型是 1-开户
			if(CommUtil.equals(String.valueOf(listinf.getAccttp()), "1")){//全功能账户
				
				listinf.setRevids("开立全功能账户");
			}else if(CommUtil.equals(String.valueOf(listinf.getAccttp()), "2")){//理财功能户
				
				listinf.setRevids("开立理财功能账户");
			}else if(CommUtil.equals(String.valueOf(listinf.getAccttp()), "3")){//电子钱包账户
				
				listinf.setRevids("开立电子钱包账户");
			}

		}
		
		//查询电子账户升级登记簿
		Page<IoCaKnaCustQrckif> listinfone= EacctMainDao.selKnpPromMpckrtt(input.getRevist(), custno, input.getBgindt(),
				input.getEndddt(), (pageno-1)*pagesz, pagesz,totlCount,true);
		info.addAll(listinfone.getRecords());
		
		counts = counts+listinfone.getRecordCount();
		
		for (IoCaKnaCustQrckif listinf: info) {
			listinf.setRevitp(E_REVITP.sj);//开户类型是 2-升级
			if(CommUtil.equals(String.valueOf(listinf.getAccttp()), "1")){//全功能账户
				
				listinf.setRevids("开立全功能账户");
			}else if(CommUtil.equals(String.valueOf(listinf.getAccttp()), "2")){//理财功能户
				
				listinf.setRevids("开立理财功能账户");
			}else if(CommUtil.equals(String.valueOf(listinf.getAccttp()), "3")){//电子钱包账户
				
				listinf.setRevids("开立电子钱包账户");
			}

		}*/
		output.setQrckifInfoList(info);
		CommTools.getBaseRunEnvs().setTotal_count(counts);
	}

	/**
	 * 
	 * @author leipeng
	 * 		<p>
	 *	    <li>2016年6月28日-下午7:33:21</li>
	 *      <li>功能描述：新增代理单位信息</li>
	 *      </p>
	 * 
	 * 
	 */
	@Override
	public void caAccDlagnoInsert(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountManager.IoCaAccDlagnoInsert.Input input) {
		// 检查输入接口必输项是否为空
		if (CommUtil.isNull(input.getAgntno())) {
			
			throw CaError.Eacct.BNAS1689();
		}
		
		if (CommUtil.isNull(input.getAgntna())) {
			
			throw CaError.Eacct.BNAS0993();
		}
		
		if (CommUtil.isNull(input.getAgactp())) {
			
			throw CaError.Eacct.BNAS0990();
		}
		KnbAgnt entity = SysUtil.getInstance(KnbAgnt.class);
		entity.setAgactp(input.getAgactp());
		entity.setAgntna(input.getAgntna());
		entity.setAgntno(input.getAgntno());
		entity.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
		entity.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());
		
		//查询单位代理信息
		KnbAgnt knbAgntInfo = KnbAgntDao.selectOne_odb1(input.getAgntno(), false);
		if(CommUtil.isNull(knbAgntInfo)){
			
			//插入代理单位信息
			KnbAgntDao.insert(entity);
		}else{
			
			throw CaError.Eacct.BNAS0991();
		}
	}
	/**
	 * 
	 * @author leipeng
	 * 		<p>
	 *	    <li>2016年6月28日-下午7:33:21</li>
	 *      <li>功能描述：修改代理单位信息</li>
	 *      </p>
	 */
	@Override
	public void caAccDlagnoUpdate(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountManager.IoCaAccDlagnoUpdate.Input input) {

		// 检查输入接口必输项是否为空
		if (CommUtil.isNull(input.getAgntno())) {
			throw CaError.Eacct.BNAS1689();
		}
		if (CommUtil.isNull(input.getAgntna())) {
			throw CaError.Eacct.BNAS0993();
		}
		if (CommUtil.isNull(input.getAgactp())) {
			throw CaError.Eacct.BNAS0990();
		}
		
		//查询代理单位信息
		KnbAgnt knbAgntInfo = KnbAgntDao.selectOne_odb1(input.getAgntno(), false);
		
		if(CommUtil.isNotNull(knbAgntInfo)){
			
			knbAgntInfo.setAgactp(input.getAgactp());
			knbAgntInfo.setAgntna(input.getAgntna());
			knbAgntInfo.setAgntno(input.getAgntno());
			//代理单位信息只能新增单位信息机构维护
			/*if(CommTools.getBaseRunEnvs().getTrxn_branch() != knbAgntInfo.getBrchno()){
				throw CaError.Eacct.E0001("该机构不能更新代理单位信息");
			}*/
			
			//更新代理单位信息
			KnbAgntDao.updateOne_odb1(knbAgntInfo);//更新单位信息
		}else{
			
			throw CaError.Eacct.BNAS0992();
		}
	}
	/**
	 * 
	 * @author leipeng
	 * 		<p>
	 *	    <li>2016年6月28日-下午7:33:21</li>
	 *      <li>功能描述：删除代理单位信息</li>
	 *      </p>
	 */
	@Override
	public void caAccDlagnoDelete(String agntno) {

		// 检查输入接口必输项是否为空
		if (CommUtil.isNull(agntno)) {
			throw CaError.Eacct.BNAS1689();
		}
		
		//查询代理单位信息
		KnbAgnt knbAgntInfo = KnbAgntDao.selectOne_odb1(agntno, false);
		if(CommUtil.isNotNull(knbAgntInfo)){
			
			//代理单位信息只能新增单位信息机构维护
			/*if(CommTools.getBaseRunEnvs().getTrxn_branch() != knbAgntInfo.getBrchno()){
				throw CaError.Eacct.E0001("该机构不能删除代理单位信息");
			}*/
			
			KnbAgntDao.deleteOne_odb1(agntno);//删除代理单位信息
		}else{
			
			throw CaError.Eacct.BNAS0992();
		}
	}
	/**
	 * 
	 * @author leipeng
	 * 		<p>
	 *	    <li>2016年6月28日-下午7:33:21</li>
	 *      <li>功能描述：根据机构号查询代理单位信息</li>
	 *      </p>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void caAccDlagnoSelectByBrchno(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountManager.IoCaAccDlagnoSelectByBrchno.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountManager.IoCaAccDlagnoSelectByBrchno.Output output) {

		int pageno = input.getPageno();
		int pagesize = input.getPgsize();
		long totlCount = 0;
		
		// 检查输入接口必输项是否为空
		if (CommUtil.isNull(input.getBrchno())) {
			throw CaError.Eacct.BNAS1675();
		}
		
		if (CommUtil.isNull(pageno)){
			throw CaError.Eacct.BNAS0406();
		}
		
		if (CommUtil.isNull(pagesize)) {
			throw CaError.Eacct.BNAS0460();
		}
		
		//本机构不能查询到其他机构的代理单位信息
	    if(!CommUtil.equals(input.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch())){
	    	throw CaError.Eacct.BNAS1104();
	    }
		
		//查询代理单位信息
		Page<IoCaKnaagntinfo> listinfo = CaDao.selAccknbagntByBrchno(input.getBrchno(), (pageno-1L)*pagesize, pagesize, totlCount, true);
		
		//Options转换list
		Options<IoCaKnaagntinfo> info = SysUtil.getInstance(Options.class);
		info.addAll(listinfo.getRecords());
		
		output.setAgntInfoList(info);
		output.setCounts(listinfo.getRecordCount());
		CommTools.getBaseRunEnvs().setTotal_count(listinfo.getPageCount());
	}
	/**
	 * 
	 * @author leipeng
	 * 		<p>
	 *	    <li>2016年6月28日-下午7:33:21</li>
	 *      <li>功能描述：根据代理单位编号查询代理单位信息</li>
	 *      </p>
	 */
	@Override
	public void caAccDlagnoSelectByAgntno(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountManager.IoCaAccDlagnoSelectByAgntno.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountManager.IoCaAccDlagnoSelectByAgntno.Output output) {
		// 检查输入接口必输项是否为空
		if (CommUtil.isNull(input.getAgntno())) {
			throw CaError.Eacct.BNAS0994();
		}
		
		//查询代理单位信息
		KnbAgnt knbagnt = KnbAgntDao.selectOne_odb1(input.getAgntno(), true);
		
		/*//代理单位信息只能新增单位信息机构维护
		if(CommTools.getBaseRunEnvs().getTrxn_branch() != knbagnt.getBrchno()){
			throw CaError.Eacct.E0001("该机构不能查询代理单位信息");
		}*/
		output.setAgntno(knbagnt.getAgntno());
		output.setAgntna(knbagnt.getAgntna());
		output.setAgactp(knbagnt.getAgactp());
		output.setBrchno(knbagnt.getBrchno());
		output.setUserid(knbagnt.getUserid());
	}

	/**
	 * 
	 * 
	 * @Title: caCheckSqReg
	 * @Description: (身份核查流水登记)
	 * @param tblKnbCksq
	 * @author xiongzhao
	 * @date 2016年7月20日 上午9:26:47
	 * @version V2.3.0
	 */
	public void caCheckSqReg(IoCaKnbCksq tblKnbCksq) {
        
		String ckrtsq = tblKnbCksq.getCkrtsq();// 核查流水
		String custac = tblKnbCksq.getCustac();// 电子账号
		String custid = tblKnbCksq.getCustid();// 用户ID
		E_CKTNTP cktntp = tblKnbCksq.getCktntp();// 核查交易类型
		E_IDCKRT idckrt = tblKnbCksq.getIdckrt();// 身份核查标志
		
		// 检查必输项是否为空
		// 核查流水
		if (CommUtil.isNull(ckrtsq)) {
			throw CaError.Eacct.BNAS0680();
		}
		// 电子账号
		if (E_CKTNTP.KINSHIP != tblKnbCksq.getCktntp()) {
			if (CommUtil.isNull(custac)) {
				throw DpModuleError.DpstProd.BNAS0926();
			}
		}
		// 用户ID
		if (CommUtil.isNull(custid)) {
			throw CaError.Eacct.BNAS0241();
		}
		// 核查交易类型
		if (CommUtil.isNull(cktntp)) {
			throw CaError.Eacct.BNAS0682();
		}
		// 身份核查标志
		if (CommUtil.isNull(idckrt)) {
			throw CaError.Eacct.BNAS0372();
		}
		
		// 检查核查流水是否已经登记处理
		KnbCksq tblKnbCksq1 = KnbCksqDao.selectOne_odb1(ckrtsq, false);
		if (CommUtil.isNotNull(tblKnbCksq1)) {
			throw CaError.Eacct.BNAS0731();
		}
		
		// 登记处理状态为待处理
		if (tblKnbCksq.getIdckrt() == E_IDCKRT.CHECKING) {
			tblKnbCksq.setProcst(E_PROCST.SUSPEND);
		} else if (tblKnbCksq.getIdckrt() == E_IDCKRT.SUCCESS) {
			tblKnbCksq.setProcst(E_PROCST.SUCCESS);
		} else {
			throw CaError.Eacct.BNAS0370();
		}
		
		// 登记身份核查流水登记簿
		KnbCksq tblKnbCksq2 = SysUtil.getInstance(KnbCksq.class);

		// 将输入的复合类型传入创建的单例对象
		CommUtil.copyProperties(tblKnbCksq2, tblKnbCksq);

		// 将数据插入身份核查流水登记簿
		KnbCksqDao.insert(tblKnbCksq2);

	}

	/**
	 * 
	 * @Title: knbSlepInsert
	 * @Description: (休眠登记簿登记)
	 * @param cplKnbSlep
	 * @author xiongzhao
	 * @date 2016年9月26日 上午11:23:52
	 * @version V2.3.0
	 */
	@Override
	public void knbSlepInsert(IoCaKnbSlep cplKnbSlep) {
		
		
		
		KnbSlep tblKnbSlep = SysUtil.getInstance(KnbSlep.class);
		
		tblKnbSlep.setCustac(cplKnbSlep.getCustac());// 电子账号
		tblKnbSlep.setCustna(cplKnbSlep.getCustna());// 客户名称
		tblKnbSlep.setAccttp(cplKnbSlep.getAccttp());// 电子账户分类
		tblKnbSlep.setBrchno(cplKnbSlep.getBrchno());// 账户归属机构
		tblKnbSlep.setTrandt(cplKnbSlep.getTrandt());// 交易日期
		tblKnbSlep.setTrantm(cplKnbSlep.getTrantm());// 交易时间
		tblKnbSlep.setTransq(cplKnbSlep.getTransq());// 交易流水
		tblKnbSlep.setUserid(cplKnbSlep.getUserid());// 操作柜员
		tblKnbSlep.setUssqno(cplKnbSlep.getUssqno());// 操作柜员流水
		tblKnbSlep.setSlepst(cplKnbSlep.getSlepst());// 休眠状态
		tblKnbSlep.setCuacst(cplKnbSlep.getCuacst());// 原客户账户状态
		
		// 登记销户登记簿
		KnbSlepDao.insert(tblKnbSlep);
		

	}

	/**
	 * 
	 * @Title: knbCgbrInsert 
	 * @Description: (变更机构登记簿登记) 
	 * @param cplKnbCgbr
	 * @author xiongzhao
	 * @date 2017年3月13日 下午4:24:28 
	 * @version V2.3.0
	 */
	@Override
	public void knbCgbrInsert(IoCaKnbCgbr cplKnbCgbr) {
		KnbCgbr tblKnbCgbr = SysUtil.getInstance(KnbCgbr.class);
		tblKnbCgbr.setCustac(cplKnbCgbr.getCustac());
		tblKnbCgbr.setOdbrch(cplKnbCgbr.getOdbrch());
		tblKnbCgbr.setNwbrch(cplKnbCgbr.getNwbrch());
		tblKnbCgbr.setOdcpno(cplKnbCgbr.getOdcpno());
		tblKnbCgbr.setNwopno(cplKnbCgbr.getNwopno());
		tblKnbCgbr.setChngdt(cplKnbCgbr.getChngdt());
		tblKnbCgbr.setTranus(cplKnbCgbr.getTranus());
		tblKnbCgbr.setTransq(cplKnbCgbr.getTransq());
		tblKnbCgbr.setServtp(cplKnbCgbr.getServtp());
		tblKnbCgbr.setStndy1(cplKnbCgbr.getStndy1());
		tblKnbCgbr.setTmstmp(cplKnbCgbr.getTmstmp());
		tblKnbCgbr.setTmstmp(cplKnbCgbr.getTmstmp());
		//tblKnbCgbr.setMtdate(cplKnbCgbr.getMtdate());
		KnbCgbrDao.insert(tblKnbCgbr);
	}

	/**
	 * 
	 * @Title: knbOpacInsert 
	 * @Description: (开户登记簿登记) 
	 * @param cplKnbOpac
	 * @author xiongzhao
	 * @date 2017年3月14日 下午4:02:22 
	 * @version V2.3.0
	 */
	@Override
	public void knbOpacInsert(IoCaOpenAccInfo cplKnbOpac) {
		//登记开户登记簿
		CaEAccountProc.addOpenAcctBook(cplKnbOpac);
		
	}

}

