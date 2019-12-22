package cn.sunline.ltts.busi.catran.trans;

import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.card.process.CaCardProc;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcalDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbCksq;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbCksqDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpac;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpacDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbProm;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbPromDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEAccountIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEAccountOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAgrtInfos;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PROCST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BINDTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CKTNTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_IDCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MPCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PROMST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_REVITP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;

public class dpckop {

	public static void prcCkopacBefore( final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Output output){
			
		String custac = input.getCustac();//电子账号ID
		E_REVITP revitp = input.getRevitp();//交易类型
		E_MPCKRT revirs = input.getRevirs();// 人脸核查结果

		// 检查输入接口不能为空
		// 身份核查结果
		if (CommUtil.isNull(custac)) {
			throw DpModuleError.DpstProd.BNAS0935();
		}
		// 用户ID
		if (CommUtil.isNull(revitp)) {
			throw CaError.Eacct.BNAS0615();
		}
		// 用户ID
		if (CommUtil.isNull(revirs)) {
			throw CaError.Eacct.BNAS0378();
		}

		E_CKTNTP cktntp =null;
		//类型的转换
		if(revitp == E_REVITP.OPEN){
			cktntp = E_CKTNTP.OPEN;
		}
		
		if(revitp == E_REVITP.UPG){
			cktntp = E_CKTNTP.UPGRADE;
		}
		
		// 根据核查流水号查询出核查记录
		KnbCksq tblKnbCksq = KnbCksqDao.selectFirst_odb2(custac, cktntp, false);
		
		// 检查查询结果是否为空
		if (CommUtil.isNull(tblKnbCksq)) {
			throw CaError.Eacct.BNAS0679();
		}
		
		// 查询电子账户表
		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(custac, false);
		if (CommUtil.isNull(tblKnaCust)) {
			throw DpModuleError.DpstComm.BNAS0754();
		}
		
		// 查询绑定手机号
		KnaAcal tblKnaAcal = CaDao.selKnaAcalByCustac(tblKnaCust.getCustac(), E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);
		if (CommUtil.isNotNull(tblKnaAcal)) {
			property.setAcalno(tblKnaAcal.getTlphno());// 电子账户绑定手机号
		}	
		
		// 根据电子账号查询用户ID
		/*
		IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(
				IoCuSevQryTableInfo.class).cif_cust_accsByCustno(
				tblKnaCust.getCustno(), false, E_STATUS.NORMAL);
		*/
		// 查询证件类型证件号码
		/*
		IoCucifCust cplCifCust = SysUtil.getInstance(
				IoCuSevQryTableInfo.class).cif_cust_selectOne_odb1(
				tblKnaCust.getCustno(), true);
				*/
//		IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//		queryCust.setCustno(tblKnaCust.getCustno());
//		IoSrvCfPerson.IoGetCifCust.Output cplCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//		SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, cplCifCust);
		
		// 根据核查交易类型获取开户产品号
		KnpParameter tblKnpParameter = SysUtil.getInstance(KnpParameter.class);
		if (tblKnbCksq.getCktntp() == E_CKTNTP.OPEN) {

			// 根据账户分类获取开户产品号
			tblKnpParameter = SysUtil.getInstance(DpProdSvcType.class)
					.qryProdcd(tblKnbCksq.getAccatp());

		} else if (tblKnbCksq.getCktntp() == E_CKTNTP.UPGRADE) {

			// 查询升级登记簿
			KnbProm tblKnbProm = KnbPromDao.selectFirst_odb2(custac,
					E_PROMST.ACCEPTED, false);

			// 检查查询记录是否存在
			if (CommUtil.isNull(tblKnbProm)) {
				throw CaError.Eacct.BNAS0362();
			}

			// 根据账户分类获取开户产品号
			tblKnpParameter = SysUtil.getInstance(DpProdSvcType.class)
					.qryProdcd(tblKnbProm.getUpactp());

		}

		// 将核查交易类型映射到属性区
		property.setCustid(tblKnaCust.getCustno());// 用户ID
//		property.setIdtfno(cplCifCust.getIdtfno());// 证件号码
//		property.setIdtftp(cplCifCust.getIdtftp());// 证件类型
		property.setAgrtno(tblKnbCksq.getAgrtno());// 协议模板编号
		property.setVesion(tblKnbCksq.getVesion());// 版本号
		property.setCrcycd(tblKnbCksq.getCrcycd());// 币种
		property.setCsextg(tblKnbCksq.getCsextg());// 钞汇标识
		property.setAccatp(tblKnbCksq.getAccatp());// 账户分类
		property.setCustno(tblKnaCust.getCustno());// 客户号
		property.setCacttp(tblKnaCust.getCacttp());// 客户账户类型
		property.setCustna(tblKnaCust.getCustna());// 客户名称
		property.setBrchno(tblKnaCust.getBrchno());// 归属机构
		property.setBaprcd(tblKnpParameter.getParm_value1());// 开户基础负债产品
		property.setWaprcd(tblKnpParameter.getParm_value2());// 钱包账户开户产品号
		
	}

	/**
	 * 
	 * @Title: prcOpenBefore
	 * @Description: (开户信息更新前处理)
	 * @param input
	 * @param property
	 * @param output
	 * @author douwb
	 * @date 2016年7月20日 下午10:54:06
	 * @version V2.3.0
	 */
	public static void prcOpenBefore( final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Output output){
		
		String custac = input.getCustac();//电子账号ID
		E_REVITP revitp = input.getRevitp();//交易类型
		E_MPCKRT revirs = input.getRevirs();// 人脸核查结果

		E_CKTNTP cktntp =null;
		//类型的转换
		if(revitp == E_REVITP.OPEN){
			cktntp = E_CKTNTP.OPEN;
		}
		
		if(revitp == E_REVITP.UPG){
			cktntp = E_CKTNTP.UPGRADE;
		}
		
		// 查询电子账户表
		KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(custac,false);
		if (CommUtil.isNull(tblKnaCust)) {
			throw DpModuleError.DpstComm.BNAS0754();
		}

		// 将身份核查标志，人脸识别结果映射到属性区
		property.setIdckru(tblKnaCust.getIdckrt());// 身份核查标志
		property.setMpckrt(tblKnaCust.getMpckrt());// 人脸识别结果
		
		// 查询身份核查流水登记簿
		KnbCksq tblKnbCksq = KnbCksqDao.selectFirst_odb2(custac, cktntp, false);
		// 检查查询记录是否存在
		if (CommUtil.isNull(tblKnbCksq)) {
			throw CaError.Eacct.BNAS0679();
		}

		// 查询出身份核查结果为核查中，人脸识别结果为核查中
		if (tblKnaCust.getIdckrt() == E_IDCKRT.CHECKING
				&& tblKnaCust.getMpckrt() == E_MPCKRT.CHECKING) {

			if (revirs == E_MPCKRT.FAILD) {

				// 更改人脸核查结果标志，修改账户状态为关闭，修改客户化状态为作废
				tblKnaCust.setMpckrt(revirs);// 人脸核查标志
				tblKnaCust.setAcctst(E_ACCTST.CLOSE);// 账户状态

				// 更新电子账户表
				KnaCustDao.updateOne_odb1(tblKnaCust);
				
				//登记电子账户状态
				IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
				cplDimeInfo.setCustac(custac);
				cplDimeInfo.setDime01(revirs.getValue());
				SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
				
				// 更新身份核查流水登记簿
				tblKnbCksq.setProcst(E_PROCST.FAIL);
				KnbCksqDao.updateOne_odb1(tblKnbCksq);


			} else if (revirs == E_MPCKRT.SUCCESS) {

				// 更改人脸核查结果标志
				tblKnaCust.setMpckrt(revirs);// 人脸核查标志

				// 更新电子账户表
				KnaCustDao.updateOne_odb1(tblKnaCust);
			} else {
				throw CaError.Eacct.BNAS1283();
			}

		}

		// 查询出身份识别结果为成功,人脸核查结果为核查中
		if (tblKnaCust.getIdckrt() == E_IDCKRT.SUCCESS
				&& tblKnaCust.getMpckrt() == E_MPCKRT.CHECKING) {

			if (revirs == E_MPCKRT.FAILD) {
				// 更改人脸核查结果标志，修改账户状态为关闭，修改客户化状态为作废
				tblKnaCust.setMpckrt(revirs);// 人脸核查标志
				tblKnaCust.setAcctst(E_ACCTST.CLOSE);// 账户状态

				// 更新电子账户表
				KnaCustDao.updateOne_odb1(tblKnaCust);
				
				//登记电子账户状态
				IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
				cplDimeInfo.setCustac(custac);
				cplDimeInfo.setDime01(revirs.getValue());
				SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
				
				// 更新身份核查流水登记簿
				tblKnbCksq.setProcst(E_PROCST.FAIL);
				KnbCksqDao.updateOne_odb1(tblKnbCksq);

			} else if (revirs == E_MPCKRT.SUCCESS) {

				// 查询账户别名表
				KnaAcal tblKnaAcal = KnaAcalDao.selectFirst_odb4(
						input.getCustac(), E_ACALTP.CELLPHONE,
						E_ACALST.NORMAL, false);

				// 校验查询结果是否为空
				if (CommUtil.isNull(tblKnaAcal)) {
					throw CaError.Eacct.BNAS1111();
				}

				// 产生卡信息
				IoCaAddEAccountIn cplCaAddEAccountIn = SysUtil
						.getInstance(IoCaAddEAccountIn.class);
				IoCaAddEAccountOut cplAddEAccountOut = SysUtil
						.getInstance(IoCaAddEAccountOut.class);

				cplCaAddEAccountIn.setTlphno(tblKnaAcal.getTlphno());// 绑定手机号
				cplCaAddEAccountIn.setOpacwy(property.getOpacwy());// 开户方式，默认单笔开户

				// 返回卡号
				String cardno = CaCardProc.prcCardInfo(cplCaAddEAccountIn,
						cplAddEAccountOut);

				// 建立电子账号与卡号关联关系
				CaCardProc.prcEacctCardLink(input.getCustac(), cardno);

				// 更改身份核查结果标志，修改账户状态为正常，修改客户化状态为正常
				tblKnaCust.setMpckrt(revirs);// 人脸核查标志
				tblKnaCust.setAcctst(E_ACCTST.NORMAL);// 账户状态

				// 更新电子账户表
				KnaCustDao.updateOne_odb1(tblKnaCust);
				
				//登记电子账户状态
				IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
				cplDimeInfo.setCustac(custac);
				cplDimeInfo.setDime01(revirs.getValue());
				SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(cplDimeInfo);
				
				//登记开户登记簿
				KnbOpac tblKnbOpac = SysUtil.getInstance(KnbOpac.class);
				tblKnbOpac.setCustac(tblKnaCust.getCustac());// 电子账号
				tblKnbOpac.setAccttp(tblKnbCksq.getAccatp());// 账户分类
				tblKnbOpac.setCustna(tblKnaCust.getCustna());// 户名
				tblKnbOpac.setCrcycd(tblKnbCksq.getCrcycd());// 币种
				tblKnbOpac.setCsextg(tblKnbCksq.getCsextg());// 钞汇标识
				tblKnbOpac.setTlphno(tblKnaAcal.getTlphno());// 绑定手机号
				tblKnbOpac.setBrchno(tblKnaCust.getBrchno());// 归属机构
				tblKnbOpac.setOpendt(tblKnaCust.getOpendt());// 开户日期
				tblKnbOpac.setOpentm(BusiTools.getBusiRunEnvs().getTrantm()); // 开户时间
				tblKnbOpac.setOpensq(tblKnaCust.getOpensq());// 开户流水
				tblKnbOpac.setOpenus(tblKnbCksq.getUserid());// 开户柜员
				tblKnbOpac.setUschnl(tblKnaCust.getUschnl());// 开户渠道
				KnbOpacDao.insert(tblKnbOpac);

			} else {
				throw CaError.Eacct.BNAS0369();
			}
		}
	}

	/**
	 * 
	 * @Title: prcUpgDealBefore
	 * @Description: (升级信息更新前处理)
	 * @param input
	 * @param property
	 * @param output
	 * @author douwb
	 * @date 2016年7月21日 上午09:31:06
	 * @version V2.3.0
	 */
	public static void prcUpgDealBefore( final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Output output){
		
		String mtdate =CommTools.getBaseRunEnvs().getTrxn_date();
		String timetm = DateTools2.getCurrentTimestamp();
		// 查询卡客户账户对照表
		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectFirst_odb1(input.getCustac(),
				E_DPACST.NORMAL, false);
		if (CommUtil.isNull(tblKnaAcdc)
				|| tblKnaAcdc.getStatus() != E_DPACST.NORMAL) {
			throw DpModuleError.DpstComm.BNAS0754();
		}

		// 将卡号映射到属性区
		property.setCardno(tblKnaAcdc.getCardno());// 卡号

		// 查询升级登记簿
		KnbProm tblKnbProm = KnbPromDao.selectFirst_odb2(
				input.getCustac(), E_PROMST.ACCEPTED, false);

		// 将身份核查标志，人脸识别结果映射到属性区
		property.setUpactp(tblKnbProm.getUpactp());// 升级账户分类
		property.setIdckru(tblKnbProm.getIdckrt());// 身份核查标志
		property.setMpckrt(tblKnbProm.getMpckrt());// 人脸识别结果
		
		//设置摘要码为‘升级’
		property.setSmrycd1(BusinessConstants.SUMMARY_SJ);

		// 检查查询记录是否存在
		if (CommUtil.isNull(tblKnbProm)) {
			throw CaError.Eacct.BNAS0362();
		}
		
		
		String custac = input.getCustac();//电子账号ID
		E_MPCKRT revirs = input.getRevirs();// 人脸核查结果
		
		E_CKTNTP cktntp = E_CKTNTP.UPGRADE;
		
		// 查询身份核查流水登记簿
		KnbCksq tblKnbCksq = KnbCksqDao.selectFirst_odb2(custac, cktntp, false);
		

		// 检查查询记录是否存在
		if (CommUtil.isNull(tblKnbCksq)) {
			throw CaError.Eacct.BNAS0679();
		}

		// 检查升级登记簿中人脸识别和身份核查标志的结果
		if (tblKnbProm.getIdckrt() == E_IDCKRT.CHECKING
				&& tblKnbProm.getMpckrt() == E_MPCKRT.CHECKING) {

			if (revirs == E_MPCKRT.FAILD) {

				// 更改身份核查结果标志,更新升级状态，登记升级日期
				CaDao.updKnbPromInfosByMpckrt(revirs, E_PROMST.FAILURE,
						CommTools.getBaseRunEnvs().getTrxn_date(),
						input.getCustac(), E_PROMST.ACCEPTED,timetm);
				
				// 更新身份核查流水登记簿
				tblKnbCksq.setProcst(E_PROCST.FAIL);
				KnbCksqDao.updateOne_odb1(tblKnbCksq);

			} else if (revirs == E_MPCKRT.SUCCESS) {

				// 更新人脸核查结果标志
				tblKnbProm.setMpckrt(revirs);// 人脸核查标志

				// 更新升级登记簿
				KnbPromDao.update_odb1(tblKnbProm);
			} else {
				throw CaError.Eacct.BNAS0369();
			}

		}

		// 查询出身份核查结果为成功，人脸识别结果为核查中
		if (tblKnbProm.getIdckrt() == E_IDCKRT.SUCCESS
				&& tblKnbProm.getMpckrt() == E_MPCKRT.CHECKING) {
			
			if (revirs == E_MPCKRT.FAILD) {

				// 更改人脸核查结果标志,更新升级状态，登记升级日期
				CaDao.updKnbPromInfosByMpckrt(revirs, E_PROMST.FAILURE,
						CommTools.getBaseRunEnvs().getTrxn_date(),
						input.getCustac(), E_PROMST.ACCEPTED,timetm);
				
				// 更新身份核查流水登记簿
				tblKnbCksq.setProcst(E_PROCST.FAIL);
				KnbCksqDao.updateOne_odb1(tblKnbCksq);

			} else if (revirs == E_MPCKRT.SUCCESS) {
				
				// 升级成功，若原账户状态为三类户，更新电子账户状态
				if(tblKnbProm.getOdactp() == E_ACCATP.WALLET) {
					// 更新电子账户状态
					IoCaUpdAcctstIn cplDimeInfo = SysUtil
							.getInstance(IoCaUpdAcctstIn.class);
					cplDimeInfo.setCustac(input.getCustac());
					cplDimeInfo.setDime01(revirs.getValue());
					SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(
							cplDimeInfo);

					KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(input.getCustac(),
							true);
					tblKnaCust.setAcctst(E_ACCTST.NORMAL);
					KnaCustDao.updateOne_odb1(tblKnaCust);
				}

				// 更新人脸核查结果标志
				CaDao.updKnbPromInfosByMpckrt(revirs, E_PROMST.SUCCESS,
						CommTools.getBaseRunEnvs().getTrxn_date(),
						input.getCustac(), E_PROMST.ACCEPTED,timetm);

				// 更新升级登记簿
				KnbPromDao.update_odb1(tblKnbProm);

			} else {
				throw CaError.Eacct.BNAS1283();
			}
		}
		
		// 查询电子账户个人结算户信息
		IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(IoDpKnaAcct.class);
		if (tblKnbProm.getOdactp() == E_ACCATP.FINANCE) {
			cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctSub(tblKnaAcdc.getCustac(),E_ACSETP.SA);
			// 将结算户负债子账号映射到属性区，提供新开负债账户后的签约处理
			property.setAcctnm(cplKnaAcct.getAcctno());// 需解约的负债账号
		}
	}

	/**
	 * 
	 * @Title: sedOpenInfoMessage
	 * @Description: (开户结果通知)
	 * @param input
	 * @param property
	 * @author xiongzhao
	 * @date 2016年7月27日 下午6:54:02
	 * @version V2.3.0
	 */
	public static void sedOpenInfoMessage( final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Output output){
		

		/*KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("OPUPGD", "CUSTSM",
				"%", "%", true);
		
		String bdid = tblKnaPara.getParm_value1();// 服务绑定ID
		
		IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind(
				IoCaOtherService.class, bdid);

		// 1.开户成功发送开户结果到客户信息
		String mssdid = CommTools.getMySysId();// 消息ID
		String mesdna = tblKnaPara.getParm_value2();// 媒介名称

		IoCaOtherService.IoCaOpenUpgSendMsg.InputSetter openSendMsgInput = CommTools
				.getInstance(IoCaOtherService.IoCaOpenUpgSendMsg.InputSetter.class);
		 */
//		MessageRealInfo mri1 = SysUtil.getInstance(MessageRealInfo.class);
//		mri1.setMtopic("Q0101002");
//		//mri.setTdcnno("R00");  //测试指定DCN
//		DpOpenUpgSendMsg openSendMsgInput = CommTools
//				.getInstance(DpOpenUpgSendMsg.class);
//		//openSendMsgInput.setMsgid(mssdid); // 发送消息ID
//		//openSendMsgInput.setMdname(mesdna); // 媒介名称
//		openSendMsgInput.setOpacrt(E_YES___.YES);// 是否开户成功
//		openSendMsgInput.setCustid(property.getCustid());// 用户ID
//		openSendMsgInput.setBrchno(property.getBrchno());// 机构号
//		openSendMsgInput.setAccatp(property.getAccatp());// 账户分类
//		openSendMsgInput.setCktntp(E_CKTNTP.OPEN);// 交易类型
//		openSendMsgInput.setMsgstr("开户通知信息，当前时间："+DateUtil.getNow());
//		
//		mri1.setMsgtyp("ApSmsType.DpOpenUpgSendMsg");
//		mri1.setMsgobj(openSendMsgInput); 
//		AsyncMessageUtil.add(mri1); //将待发送消息放入当前交易暂存区，commit后发送
		//caOtherService.openUpgSendMsg(openSendMsgInput);

		/*// 2.开户成功发送协议到合约库
		KnpParameter tblKnaPara1 = KnpParameterDao.selectOne_odb1("OPUPGD", "AGRTSM",
				"%", "%", true);

		String mssdid1 = CommTools.getMySysId();// 消息ID
		String mesdna1 = tblKnaPara1.getParm_value2();// 媒介名称

		IoCaOtherService.IoCaSendContractMsg.InputSetter openSendAgrtInput = SysUtil.getInstance(IoCaOtherService.IoCaSendContractMsg.InputSetter.class);

		String sAgdata = property.getIdtftp() + "|" + property.getIdtfno() + "|"
				+ property.getCustna() + "|" + property.getCardno() + "|"
				+ property.getAccatp() + "|" + property.getBrchno() + "|"
				+ CommTools.getBaseRunEnvs().getTrxn_date();// 协议回填字段
		
		openSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
		openSendAgrtInput.setMdname(mesdna1); // 媒介名称
		openSendAgrtInput.setUserId(property.getCustid());// 用户ID
		openSendAgrtInput.setOpenOrg(property.getBrchno());// 机构号
		openSendAgrtInput.setAcctNo(property.getCardno());// 电子账号
		openSendAgrtInput.setAcctName(property.getCustna());// 户名
		openSendAgrtInput.setRecordCount(ConvertUtil.toInteger(1));// 记录数
		
		IoCaAgrtInfos cplAgrtInfos = SysUtil.getInstance(IoCaAgrtInfos.class);
		cplAgrtInfos.setAgrTemplateNo(property.getAgrtno());// 协议模板编号
		cplAgrtInfos.setVersion(property.getVesion());// 版本号
		cplAgrtInfos.setAgrData(sAgdata);// 协议回填字段
		openSendAgrtInput.getAgreementList().add(cplAgrtInfos);// 协议列表

		caOtherService.sendContractMsg(openSendAgrtInput);*/
		
		// 3.开立二类户成功将理财签约相关信息发送到合约库
		if (property.getAccatp() == E_ACCATP.FINANCE) {
			/*KnpParameter tblKnaPara3 = KnpParameterDao.selectOne_odb1("OPENMN", "AGRTSM",
					"%", "%", true);

			String mssdid2 = CommTools.getMySysId();// 消息ID
			String mesdna2 = tblKnaPara3.getParm_value2();// 媒介名称

			IoCaOtherService.IoCaSendMonMsg.InputSetter sendMonMsgInput = SysUtil.getInstance(IoCaOtherService.IoCaSendMonMsg.InputSetter.class);

			sendMonMsgInput.setMsgid(mssdid2); // 发送消息ID
			sendMonMsgInput.setMdname(mesdna2); // 媒介名称
			sendMonMsgInput.setUserId(property.getCustid());// 用户ID
			sendMonMsgInput.setAcctNo(property.getCardno());// 电子账号
			sendMonMsgInput.setAcctName(property.getCustna());// 客户姓名
			sendMonMsgInput.setCertNo(property.getIdtfno());// 证件号码
			sendMonMsgInput.setCertType(property.getIdtftp());// 证件类型
			sendMonMsgInput.setTransBranch(property.getBrchno());// 机构编号
			sendMonMsgInput.setMobileNo(property.getAcalno());// 绑定手机号

			caOtherService.sendMonMsg(sendMonMsgInput);*/
			
			// 4.开户成功将绑定账户的信息发送至客户信息
			List<KnaCacd> tblKnaCacd = KnaCacdDao.selectAll_odb3(input.getCustac(), E_DPACST.NORMAL, false);
			for(KnaCacd cpltblKnaCacd : tblKnaCacd){
				
				/*KnpParameter para = KnpParameterDao.selectOne_odb1("BDCAMQ", "%", "%", "%", true);
				
				String mssdid3 = CommTools.getMySysId();// 随机生成消息ID
				
				String mesdna3 = para.getParm_value2();// 媒介名称
				
				E_BINDTP bindtp = E_BINDTP.BIND;// 绑定方式
				
				IoCaOtherService.IoCaBindMqService.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaBindMqService.InputSetter.class);
				*/
				E_BINDTP bindtp = E_BINDTP.BIND;// 绑定方式
				
//				MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//				mri.setMtopic("Q0101003");
//				DpBindMqService mqInput = SysUtil.getInstance(DpBindMqService.class);
//				
//				//mqInput.setMsgid(mssdid3); //发送消息ID
//				//mqInput.setMdname(mesdna3); //媒介名称
//				mqInput.setBindst(bindtp); //绑定方式
//				mqInput.setEactno(property.getCardno()); //电子账号
//				mqInput.setBindno(cpltblKnaCacd.getCardno()); //绑定账户
//				mqInput.setAtbkno(cpltblKnaCacd.getBrchno()); //账户开户行号
//				mqInput.setAcusna(cpltblKnaCacd.getAcctna()); //绑定账户名称
//				mqInput.setAtbkna(cpltblKnaCacd.getBrchna()); //开户行名称
//				mqInput.setAccttp(cpltblKnaCacd.getCardtp()); //绑定账户类型
//				mqInput.setIsiner(cpltblKnaCacd.getIsbkca()); //是否本行账户
//				mqInput.setCustid(property.getCustid());// 用户ID
//				
//				mri.setMsgtyp("ApSmsType.DpBindMqService");
//				mri.setMsgobj(mqInput); 
//				AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
//				//caOtherService.bindMqService(mqInput);
			}
			
		}
	}

	/**
	 * 
	 * @Title: prcTransInform
	 * @Description: (升级结果通知)
	 * @param input
	 * @param property
	 * @author xiongzhao
	 * @date 2016年7月27日 下午6:54:16
	 * @version V2.3.0
	 */
	public static void prcTransInform( final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Output output){
		

		KnpParameter tblKnaPara = KnpParameterDao.selectOne_odb1("OPUPGD", "CUSTSM",
				"%", "%", true);
		
		String bdid = tblKnaPara.getParm_value1();// 服务绑定ID
		
		IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind(
				IoCaOtherService.class, bdid);

		// 1.升级成功发送升级结果到客户信息
		String mssdid = CommTools.getMySysId();// 消息ID
		String mesdna = tblKnaPara.getParm_value2();// 媒介名称

		IoCaOtherService.IoCaOpenUpgSendMsg.InputSetter upgdSendMsgInput = SysUtil
				.getInstance(IoCaOtherService.IoCaOpenUpgSendMsg.InputSetter.class);
		
		upgdSendMsgInput.setMsgid(mssdid); // 发送消息ID
		upgdSendMsgInput.setMdname(mesdna); // 媒介名称
		upgdSendMsgInput.setOpacrt(E_YES___.YES);// 是否开户成功
		upgdSendMsgInput.setCustid(property.getCustid());// 用户ID
		upgdSendMsgInput.setBrchno(property.getBrchno());// 机构号
		upgdSendMsgInput.setAccatp(property.getUpactp());// 账户分类

		caOtherService.openUpgSendMsg(upgdSendMsgInput);

		// 2.升级成功发送协议到合约库
		KnpParameter tblKnaPara1 = KnpParameterDao.selectOne_odb1("OPUPGD", "AGRTSM",
				"%", "%", true);

		String mssdid1 = CommTools.getMySysId();// 消息ID
		String mesdna1 = tblKnaPara1.getParm_value2();// 媒介名称

		IoCaOtherService.IoCaSendContractMsg.InputSetter upgdSendAgrtInput = SysUtil.getInstance(IoCaOtherService.IoCaSendContractMsg.InputSetter.class);
		
		String sAgdata = property.getIdtftp() + "|" + property.getIdtfno() + "|"
				+ property.getCustna() + "|" + property.getCardno() + "|"
				+ property.getUpactp() + "|" + property.getBrchno() + "|"
				+ CommTools.getBaseRunEnvs().getTrxn_date();// 协议回填字段
		
		upgdSendAgrtInput.setMsgid(mssdid1); // 发送消息ID
		upgdSendAgrtInput.setMdname(mesdna1); // 媒介名称
		upgdSendAgrtInput.setUserId(property.getCustid());// 用户ID
		upgdSendAgrtInput.setOpenOrg(property.getBrchno());// 机构号
		upgdSendAgrtInput.setAcctNo(property.getCardno());// 电子账号
		upgdSendAgrtInput.setAcctName(property.getCustna());// 户名
		upgdSendAgrtInput.setRecordCount(ConvertUtil.toInteger(1));// 记录数
		
		IoCaAgrtInfos cplAgrtInfos = SysUtil.getInstance(IoCaAgrtInfos.class);
		cplAgrtInfos.setAgrTemplateNo(property.getAgrtno());// 协议模板编号
		cplAgrtInfos.setVersion(property.getVesion());// 版本号
		cplAgrtInfos.setAgrData(sAgdata);// 协议回填字段
		upgdSendAgrtInput.getAgreementList().add(cplAgrtInfos);// 协议列表

		caOtherService.sendContractMsg(upgdSendAgrtInput);
		
		// 3.升级二类户成功将理财签约相关信息发送到合约库
		if (property.getUpactp() == E_ACCATP.FINANCE) {
			KnpParameter tblKnaPara3 = KnpParameterDao.selectOne_odb1("OPENMN", "AGRTSM",
					"%", "%", true);

			String mssdid2 = CommTools.getMySysId();// 消息ID
			String mesdna2 = tblKnaPara3.getParm_value2();// 媒介名称

			IoCaOtherService.IoCaSendMonMsg.InputSetter sendMonMsgInput = SysUtil.getInstance(IoCaOtherService.IoCaSendMonMsg.InputSetter.class);

			sendMonMsgInput.setMsgid(mssdid2); // 发送消息ID
			sendMonMsgInput.setMdname(mesdna2); // 媒介名称
			sendMonMsgInput.setUserId(property.getCustid());// 用户ID
			sendMonMsgInput.setAcctNo(property.getCardno());// 电子账号
			sendMonMsgInput.setAcctName(property.getCustna());// 客户姓名
			sendMonMsgInput.setCertNo(property.getIdtfno());// 证件号码
			sendMonMsgInput.setCertType(property.getIdtftp());// 证件类型
			sendMonMsgInput.setTransBranch(property.getBrchno());// 机构编号
			sendMonMsgInput.setMobileNo(property.getAcalno());// 绑定手机号

			caOtherService.sendMonMsg(sendMonMsgInput);
		}
	}

	/**
	 * 
	 * @Title: prcCancelContract
	 * @Description: (二类户升级为一类户更新原签约信息)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年9月12日 上午10:37:49
	 * @version V2.3.0
	 */
	public static void prcCancelContract( final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Output output){		
		
		// 将签约信息更新为一类户的负债账户签约
		String mtdate = CommTools.getBaseRunEnvs().getTrxn_date();
		String timetm = DateTools2.getCurrentTimestamp();
		
		CaDao.updKnaSignDetlinfo(property.getAcctno(), property.getBaprcd(),
				property.getAcctnm(), E_SIGNST.QY,timetm);
		
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._01);
	}
	
	/**
	 * 
	 * @Title: updTrdStatus
	 * @Description: (三类户升级更新电子账户状态)
	 * @param input
	 * @param property
	 * @param output
	 * @author xiongzhao
	 * @date 2016年9月24日 上午11:19:48
	 * @version V2.3.0
	 */
	public static void updTrdStatus( final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Input input,  final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Property property,  final cn.sunline.ltts.busi.catran.trans.intf.Dpckop.Output output){




	
	}


}
