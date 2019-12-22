package cn.sunline.ltts.busi.ca.serviceimpl;

import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbAcif;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbAcifDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbUnat;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbUnatDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaQryOpenAcct.Input;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaQryOpenAcct.Output;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.cf.IoCuCustComplexType.CustInfo;
import cn.sunline.ltts.busi.iobus.type.cf.IoCuTable.IoCucifCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaOpenAccInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryDeRegIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryDeRegOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryDisaInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryInfoList;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryModInfoList;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQrySlepInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryUpRegIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryUpRegOut;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACUTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEALST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MANTWY;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PROMST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SLEPST;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRCHLV;

/**
 * 电子账户登记簿查询服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoCaSevQryAccout", longname = "电子账户登记簿查询服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoCaSevQryAccoutImpl implements
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout {

	private static BizLog bizLog = BizLogUtil.getBizLog(IoCaSevQryAccoutImpl.class);
	
	/**
	 * 
	 * @Title: QryUpRegistry
	 * @Description: (电子账户升级登记簿查询)
	 * @param cplUpgInfo
	 * @return
	 * @author xiongzhao
	 * @date 2016年7月21日 下午9:47:18
	 * @version V2.3.0
	 */
	@Override
	public void QryUpRegistry(
			IoCaQryUpRegIn cplUpgInfo,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaQryUpRegistry.Output output) {

		E_PROMST promst = cplUpgInfo.getPromst();// 升级状态
		String idtfno = cplUpgInfo.getIdtfno();// 证件号码
		E_IDTFTP idtftp = cplUpgInfo.getIdtftp();// 证件类型
		String stardt = cplUpgInfo.getStardt();// 起始日期
		String stopdt = cplUpgInfo.getStopdt();// 终止日期
		String brchno = cplUpgInfo.getBrchno();// 账户归属机构

		int totlCount = 0; // 记录总数
		int startno = (cplUpgInfo.getPageno() - 1) * cplUpgInfo.getPgsize();// 起始记录数

		// 账户归属机构不能为空
		if (CommUtil.isNull(brchno)) {
			throw CaError.Eacct.BNAS1640();
		}
		// 页数
		if (CommUtil.isNull(cplUpgInfo.getPageno())) {
			throw CaError.Eacct.BNAS0249();
		}
		// 页容量
		if (CommUtil.isNull(cplUpgInfo.getPgsize())) {
			throw CaError.Eacct.BNAS0461();
		}

		// 起始日期，终止日期必须都不为空，或都为空
		if (CommUtil.isNull(stardt)) {
			if (CommUtil.isNotNull(stopdt)) {
				throw CaError.Eacct.BNAS0408();
			}
		} else {
			if (CommUtil.isNull(stopdt)) {
				throw CaError.Eacct.BNAS0410();
			}
		}

		// 起始日期不能大于终止日期，终止日期不能大于当前日期,查询打印时间间隔不能超过一年
		if (CommUtil.isNotNull(stardt) && CommUtil.isNotNull(stopdt)) {
			if (CommUtil.compare(stardt, stopdt) > 0) {
				throw CaError.Eacct.BNAS0412();
			}
			if (CommUtil.compare(stopdt, CommTools.getBaseRunEnvs().getTrxn_date()) > 0) {
				throw CaError.Eacct.BNAS0062();
			}
			if (CommUtil.compare(DateTools2.calDateByTerm(stardt, "1Y"), stopdt) < 0) {
				throw CaError.Eacct.BNAS1077();
			}

		}

		// 证件类型证件号码必须都不为空，或都为空
		if (CommUtil.isNull(idtfno)) {
			if (CommUtil.isNotNull(idtftp)) {
				throw CaError.Eacct.BNAS0152();
			}
		} else {
			if (CommUtil.isNull(idtftp)) {
				throw CaError.Eacct.BNAS0156();
			}
		}

		// 电子账号与证件信息不能同时为空
		if (CommUtil.isNull(promst) && CommUtil.isNull(brchno)) {
			if (CommUtil.isNull(idtfno) || CommUtil.isNull(idtftp)) {
				throw CaError.Eacct.BNAS1641();
			}
		}

		// 检查证件类型证件号码是否有效
		if (CommUtil.isNotNull(idtfno) && CommUtil.isNotNull(idtftp)) {
			BusiTools.chkCertnoInfo(idtftp, idtfno);
		}

		// 交易渠道为移动终端时，需要查询的为要升级的记录
		if (CommUtil.equals(CommTools.getBaseRunEnvs().getChannel_id(), "MT")) {
			promst = E_PROMST.APPLY;
		}
		
		// 分页查询电子账户升级登记簿
		if (CommUtil.isNotNull(idtfno) && CommUtil.isNotNull(idtftp)) {
			// 当证件类型证件号码存在是，机构号默认为空
			brchno = null;
			
			Page<IoCaQryUpRegOut> upInfoList = EacctMainDao.selInfoUpregTable(
					promst, brchno, stardt, stopdt, idtfno, idtftp, startno,
					cplUpgInfo.getPgsize(), totlCount, false);
			
			//modify by songkl 20170220  升级新增对已销户升级记录的查询
			//前面取消对电话号码的了链表查询，单独查询取第一条记录，不保证取到的电话号码是升级时绑定的电话号码
			for(IoCaQryUpRegOut upinfo : upInfoList.getRecords()){
				KnaAcal telinfo = EacctMainDao.selknaAcalbycustac(upinfo.getCustac(), false);
				if(CommUtil.isNotNull(telinfo)){
					upinfo.setTeleno(telinfo.getTlphno());
				}
				
			}
			
			// 将查询出的数据和总笔数映射到输出接口
			output.getUpgInfoList().addAll(upInfoList.getRecords());
			output.setCounts(ConvertUtil.toInteger(upInfoList.getRecordCount()));
			
			// 设置报文头总记录条数
			CommTools.getBaseRunEnvs().setTotal_count(
					upInfoList.getRecordCount());
			
		} else {
			
			Page<IoCaQryUpRegOut> upInfoList = EacctMainDao.selInfoUpregTable(
					promst, brchno, stardt, stopdt, idtfno, idtftp, startno,
					cplUpgInfo.getPgsize(), totlCount, false);
			
			//modify by songkl 20170220  升级新增对已销户升级记录的查询
			//前面取消对电话号码的了链表查询，单独查询取第一条记录，不保证取到的电话号码是升级时绑定的电话号码
			for(IoCaQryUpRegOut upinfo : upInfoList.getRecords()){
				KnaAcal telinfo = EacctMainDao.selknaAcalbycustac(upinfo.getCustac(), false);
				if(CommUtil.isNotNull(telinfo)){
					upinfo.setTeleno(telinfo.getTlphno());
				}
				
			}
			
			// 将查询出的数据和总笔数映射到输出接口
			output.getUpgInfoList().addAll(upInfoList.getRecords());
			output.setCounts(ConvertUtil.toInteger(upInfoList.getRecordCount()));
			
			// 设置报文头总记录条数
			CommTools.getBaseRunEnvs().setTotal_count(
					upInfoList.getRecordCount());

		}
	}

	/**
	 * 
	 * @Title: IoCaQryOpenAcct
	 * @Description: 电子账户开户登记簿查询
	 * @param input
	 *            查询条件
	 * @param output
	 *            查询结果
	 * @author zhangjunlei
	 * @date 2016年7月7日 上午10:38:44
	 * @version V2.3.0
	 */
	@Override
	public void IoCaQryOpenAcct(Input input, Output output) {

		int totlCount = 0; // 记录总数
		int startno = (input.getPageno() - 1) * input.getPageSize();// 起始记录数
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();// 交易机构号
		String orgnbr = input.getOrgnbr();// 传入机构号
		String crcycd = input.getCrcycd();// 币种
		E_CSEXTG csextg = input.getCsextg();// 钞汇属性
		String bgindt = input.getBgindt();// 起始日期
		String matudt = input.getMatudt();// 终止日期
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
		// 当前页码
		if (CommUtil.isNull(input.getPageno())) {
			throw CaError.Eacct.BNAS0977();
		}
		// 页容量
		if (CommUtil.isNull(input.getPageSize())) {
			throw CaError.Eacct.BNAS0463();
		}

		// 机构号不为空的时候判断机构信息是否存在
		if (CommUtil.isNotNull(orgnbr)) {
			// IoPbKubBrch cplKubBrch =
			// AccountLimitDao.selKubBrchByBrhcno(orgnbr, false);
			IoBrchInfo cplKubBrch = SysUtil.getInstance(IoSrvPbBranch.class)
					.getBranch(orgnbr);
			if (CommUtil.isNull(cplKubBrch)) {
				throw CaError.Eacct.BNAS1268();
			}
		}

		// 机构号为空则为报文中机构号
		if (CommUtil.isNull(orgnbr) && CommUtil.isNull(input.getCardno())) {
			orgnbr = brchno;
		}

		// 获取机构级别,非省级机构只能查询本机构信息
		IoBrchInfo cplKubBrch = SysUtil.getInstance(IoSrvPbBranch.class)
				.getBranch(orgnbr);
		if (E_BRCHLV.PROV != cplKubBrch.getBrchlv()) {
			if (!orgnbr.equals(brchno)) {
				throw CaError.Eacct.BNAS0786();
			}
		}

		// 起始日期不能为空
		if (CommUtil.isNull(bgindt)) {
			throw CaError.Eacct.BNAS1642();
		}
		// 终止日期不能为空
		if (CommUtil.isNull(matudt)) {
			throw CaError.Eacct.BNAS0061();
		}

		// 终止日期不能小于开始日期
		if (DateUtil.compareDate(bgindt, matudt) > 0) {
			throw CaError.Eacct.BNAS0060();
		}

		// 起始日期必须小于当前日期
		if (DateUtil.compareDate(bgindt, sTime) > 0) {
			throw CaError.Eacct.BNAS1644();
		}

		// 终止日期必须小于当前日期
		if (DateUtil.compareDate(matudt, sTime) > 0) {
			throw CaError.Eacct.BNAS1645();
		}
		
		// 币种为人民币时，钞汇属性不可选
		if (BusiTools.getDefineCurrency().equals(crcycd) && CommUtil.isNotNull(csextg)) {
			throw CaError.Eacct.BNAS0982();
		}


		// 分页查询开户登记簿
		Page<IoCaOpenAccInfo> infos = CaDao.selOpenAcct(input.getCardno(),orgnbr, bgindt, matudt,
				crcycd, csextg, startno, input.getPageSize(), totlCount, false);
		
		for (IoCaOpenAccInfo cplOpenac : infos.getRecords()) {
			KnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCustac(cplOpenac.getCustac(), false);
			if (CommUtil.isNotNull(tblKnaAcdc)) {
				cplOpenac.setCardno(tblKnaAcdc.getCardno());
			}
			KnaCust tblKnaCust = CaDao.selKnaCustByCustac(cplOpenac.getCustac(), false);
			if (CommUtil.isNotNull(tblKnaCust)) {
				//取消客户信息相关 业务拆分内容
//				IoSrvCfPerson.IoGetCifCust.InputSetter queryCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//				IoSrvCfPerson.IoGetCifCust.Output cplCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//				IoSrvCfPerson cifCustServ = SysUtil.getInstance(IoSrvCfPerson.class);
//				queryCifCust.setCustno(tblKnaCust.getCustno());
//				cifCustServ.getCifCust(queryCifCust, cplCifCust);
//				
//				if (CommUtil.isNotNull(cplCifCust)) {
//					cplOpenac.setIdtfno(cplCifCust.getIdtfno());
//					cplOpenac.setIdtftp(cplCifCust.getIdtftp());
//				}
			}
		}
		output.getOpenqrInfoList().addAll(infos.getRecords());// 复合类型信息复制给output
		output.setCounts(ConvertUtil.toInteger(infos.getRecordCount()));// 设置总记录数

		CommTools.getBaseRunEnvs().setTotal_count(
				infos.getRecordCount());// 设置报文头总记录条数
	}

	/**
	 * 
	 * @Title: QryStop
	 * @Description: (停用转久悬登记簿查询)
	 * @param input
	 * @param output
	 * @author xiongzhao
	 * @date 2016年7月19日 下午8:12:19
	 * @version V2.3.0
	 */
	public void QryStop(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaQryStop.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaQryStop.Output output) {

		int totlCount = 0; // 记录总数
		int startno = (input.getPageno() - 1) * input.getPgsize();// 起始记录数

		String brchno = input.getBrchno(); // 传入机构号

		E_ACUTST ecctst = input.getEcctst(); // 查询内容
		String cardno = input.getCardno(); // 电子账号
		E_DEALST dealst = input.getDealst(); // 处理状态
		String stardt = input.getStardt(); // 起始日期
		String stopdt = input.getStopdt(); // 终止日期

		// 页码
		if (CommUtil.isNull(input.getPageno())) {
			throw CaError.Eacct.BNAS0253();
		}
		// 页容量
		if (CommUtil.isNull(input.getPgsize())) {
			throw CaError.Eacct.BNAS1646();
		}
		// 查询内容
		if (CommUtil.isNull(ecctst)) {
			throw CaError.Eacct.BNAS1064();
		}
		// 机构号
		if (CommUtil.isNull(brchno)) {
			throw CaError.Eacct.BNAS0655();
		}

		// 起始日期，终止日期必须都不为空，或都为空
		if (CommUtil.isNull(stardt)) {
			if (CommUtil.isNotNull(stopdt)) {
				throw CaError.Eacct.BNAS0408();
			}
		} else {
			if (CommUtil.isNull(stopdt)) {
				throw CaError.Eacct.BNAS0410();
			}
		}

		// 卡号不输时，起止日期必输
		if (CommUtil.isNull(cardno)) {
			if (CommUtil.isNull(stardt) || CommUtil.isNull(stopdt)) {
				throw CaError.Eacct.BNAS0927();
			}
		}

		// 起始日期不能大于终止日期
		if (CommUtil.isNotNull(stardt) && CommUtil.isNotNull(stopdt)) {
			if (CommUtil.compare(stardt, stopdt) > 0) {
				throw CaError.Eacct.BNAS0412();
			}
			if (CommUtil.compare(stopdt, CommTools.getBaseRunEnvs().getTrxn_date()) > 0) {
				throw CaError.Eacct.BNAS1139(stopdt,CommTools.getBaseRunEnvs().getTrxn_date());
			}
			if (CommUtil.compare(DateTools2.calDateByTerm(stardt, "1Y"), stopdt) < 0) {
				throw CaError.Eacct.BNAS1077();
			}
		}

		// 查询停用登记簿
		if (ecctst == E_ACUTST.STOP) {

			Page<IoCaQryDisaInfo> listinfo = EacctMainDao.selKnbcasp(dealst,
					cardno, stardt, stopdt, brchno, startno, input.getPgsize(),
					totlCount, false);

			output.getStopInfoList().addAll(listinfo.getRecords());
			output.setCounts(listinfo.getRecordCount());

			// 设置报文头总记录条数
			CommTools.getBaseRunEnvs().setTotal_count(
					listinfo.getRecordCount());

		}
		// 查询停用转久悬登记簿
		else if (ecctst == E_ACUTST.PROP) {

			Page<IoCaQryDisaInfo> listinfo = EacctMainDao.selKnbdisa(dealst,
					cardno, stardt, stopdt, brchno, startno, input.getPgsize(),
					totlCount, false);

			output.getStopInfoList().addAll(listinfo.getRecords());
			output.setCounts(listinfo.getRecordCount());

			// 设置报文头总记录条数
			CommTools.getBaseRunEnvs().setTotal_count(
					listinfo.getRecordCount());
		}
	}

	@Override
	public void qryInfoList(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaCloseQryInfoList.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaCloseQryInfoList.Output output) {
		int totlCount = 0; // 记录总数
		int startno = (input.getPageno() - 1) * input.getPagesize();// 起始记录数
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();// 交易机构号
		String orgnbr = input.getOrgnbr();// 传入机构号
		String crcycd = input.getCrcycd();// 币种
		E_CSEXTG csextg = input.getCsextg();// 钞汇属性
		String bgindt = input.getBgindt();// 起始日期
		String matudt = input.getMatudt();// 终止日期
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
		if (CommUtil.isNull(input.getPageno())) {
			throw CaError.Eacct.BNAS0977();
		}
		if (CommUtil.isNull(input.getPagesize())) {
			throw CaError.Eacct.BNAS0463();
		}
		if (CommUtil.isNotNull(orgnbr)) {
			IoBrchInfo cplKubBrch = SysUtil.getInstance(IoSrvPbBranch.class)
					.getBranch(brchno);
			if (CommUtil.isNull(cplKubBrch)) {
				throw CaError.Eacct.BNAS1268();
			}
		}
		if (CommUtil.isNull(orgnbr) && CommUtil.isNull(input.getCardno())) {
			orgnbr = brchno;
		}
		
		// 获取机构级别,非省级机构只能查询本机构信息
		IoBrchInfo cplKubBrch = SysUtil.getInstance(IoSrvPbBranch.class)
				.getBranch(brchno);
		if (E_BRCHLV.PROV != cplKubBrch.getBrchlv()) {
			if (!orgnbr.equals(brchno)) {
				throw CaError.Eacct.BNAS0786();
			}
		}
		// 起始日期不为空
		if (CommUtil.isNull(bgindt)) {
			throw CaError.Eacct.BNAS1642();
		}

		// 终止日期不能为空
		if (CommUtil.isNull(matudt)) {
			throw CaError.Eacct.BNAS0061();
		}

		// 终止日期不能小于开始日期
		if (DateUtil.compareDate(bgindt, matudt) > 0) {
			throw CaError.Eacct.BNAS0060();
		}

		// 起始日期必须小于当前日期
		if (DateUtil.compareDate(bgindt, sTime) > 0) {
			throw CaError.Eacct.BNAS1644();
		}

		// 终止日期必须小于当前日期
		if (DateUtil.compareDate(matudt, sTime) > 0) {
			throw CaError.Eacct.BNAS1645();
		}

		if (BusiTools.getDefineCurrency().equals(crcycd) && CommUtil.isNotNull(csextg)) {
			throw CaError.Eacct.BNAS0982();
		}

		// 查询销户登记簿
		Page<IoCaQryInfoList> infos = CaDao.selList(input.getCardno(),orgnbr, bgindt, matudt,
				crcycd, csextg, startno, input.getPagesize(), totlCount, false);
		
		for (IoCaQryInfoList cplClosac : infos.getRecords()) {
			KnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCustac(cplClosac.getCustac(), false);
			if (CommUtil.isNotNull(tblKnaAcdc)) {
				cplClosac.setCardno(tblKnaAcdc.getCardno());
			}
			KnaCust tblKnaCust = CaDao.selKnaCustByCustac(cplClosac.getCustac(), false);
			if (CommUtil.isNotNull(tblKnaCust)) {
				cplClosac.setBrchno(tblKnaCust.getBrchno());
				cplClosac.setOpendt(tblKnaCust.getOpendt());
				//客户信息相关取消，模块拆分
//				IoSrvCfPerson.IoGetCifCust.InputSetter queryCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//				IoSrvCfPerson.IoGetCifCust.Output cplCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//				IoSrvCfPerson cifCustServ = SysUtil.getInstance(IoSrvCfPerson.class);
//				queryCifCust.setCustno(tblKnaCust.getCustno());
//				cifCustServ.getCifCust(queryCifCust, cplCifCust);
				
//				if (CommUtil.isNotNull(cplCifCust)) {
//					cplClosac.setIdtfno(cplCifCust.getIdtfno());
//					cplClosac.setIdtftp(cplCifCust.getIdtftp());
//				}
			}
		
		}
		// 输出映射
		output.getClosqrInfoList().addAll(infos.getRecords());
		// 设置总记录数
		output.setCounts(ConvertUtil.toInteger(infos.getRecordCount()));
		// 设置报文头总记录条数
		CommTools.getBaseRunEnvs().setTotal_count(
				infos.getRecordCount());

	}

	/**
	 * 
	 * @Title: queryModRegistry
	 * @Description: (绑定账户修改登记簿查询服务实现)
	 * @param input
	 * @param output
	 * @author xiongzhao
	 * @date 2016年7月20日 下午5:08:55
	 * @version V2.3.0
	 */
	@Override
	public void queryModRegistry(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaQryMod.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaQryMod.Output output) {

		String cardno = input.getCardno();// 卡号
		E_IDTFTP idtftp = input.getIdtftp();// 证件类型
		String idtfno = input.getIdtfno();// 证件号码
		String bgindt = input.getBgindt();// 起始日期
		String matudt = input.getMatudt();// 终止日期
		String brchno = input.getOrgnbr();// 操作机构
		E_MANTWY mantwy = input.getMantwy();// 维护方式
		String servtp = input.getServtp();// 渠道

		int totlCount = 0; // 记录总数
		int startno = (input.getPageno() - 1) * input.getPagesize();// 起始记录数

		// 页数
		if (CommUtil.isNull(input.getPageno())) {
			throw CaError.Eacct.BNAS0249();
		}
		// 记录数
		if (CommUtil.isNull(input.getPagesize())) {
			throw CaError.Eacct.BNAS1646();
		}

		// 电子账号/证件号码/操作机构三者必输其一
		if (CommUtil.isNull(cardno) && CommUtil.isNull(idtfno)
				&& CommUtil.isNull(brchno)) {
			throw CaError.Eacct.BNAS0949();
		}

		// 起始日期，终止日期必须都不为空，或都为空
		if (CommUtil.isNull(bgindt)) {
			if (CommUtil.isNotNull(matudt)) {
				throw CaError.Eacct.BNAS0408();
			}
		} else {
			if (CommUtil.isNull(matudt)) {
				throw CaError.Eacct.BNAS0410();
			}
		}

		// 起始日期不能大于终止日期，终止日期不能大于当前日期
		if (CommUtil.isNotNull(bgindt) && CommUtil.isNotNull(matudt)) {

			String trandt = DateTools2.calDateByTerm(bgindt, "3M");

			if (CommUtil.isNull(cardno) && CommUtil.isNull(idtfno)) {

				if (CommUtil.compare(trandt, matudt) < 0) {
					throw CaError.Eacct.BNAS0057();
				}
			}
			if (CommUtil.compare(bgindt, matudt) > 0) {
				throw CaError.Eacct.BNAS0412();
			}

			if (CommUtil.compare(matudt, CommTools.getBaseRunEnvs().getTrxn_date()) > 0) {
				throw CaError.Eacct.BNAS0062();
			}
		}

		// 证件类型证件号码必须都不为空，或都为空
		if (CommUtil.isNull(idtfno)) {
			if (CommUtil.isNotNull(idtftp)) {
				throw CaError.Eacct.BNAS0152();
			}
		} else {
			if (CommUtil.isNull(idtftp)) {
				throw CaError.Eacct.BNAS0156();
			}
		}

		// 电子账号，证件类型不输时，起止日期必输！
		if (CommUtil.isNull(cardno) && CommUtil.isNull(idtfno)
				&& CommUtil.isNull(idtftp)) {
			if (CommUtil.isNull(bgindt) || CommUtil.isNull(matudt)) {
				throw CaError.Eacct.BNAS0951();
			}
		}

		if (CommUtil.isNotNull(idtftp) && CommUtil.isNotNull(idtfno)) {
			// 检查证件类型证件号码是否有效
			BusiTools.chkCertnoInfo(idtftp, idtfno);
		}

			/*	// 根据证件类型证件号码查询电子账户表
			IoCaKnaCust cplKnaCust = EacctMainDao.selByCusInfo(idtftp, idtfno,
					false);

			// 检查查询记录是否为空
			if (CommUtil.isNull(cplKnaCust)) {
				throw CaError.Eacct.E0001("该客户未开立电子账户!");
			}
			custac1 = cplKnaCust.getCustac();// 电子账号
		}

		// 如果卡号不为空，根据卡号查询出电子账号
		if (CommUtil.isNotNull(cardno)) {
			KnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByCardno(cardno,
					E_DPACST.NORMAL, false);
			if (CommUtil.isNull(tblKnaAcdc)) {
				throw CaError.Eacct.E0001("电子账号不存在！");
			}
			custac = tblKnaAcdc.getCustac();// 电子账号
		}

		if (CommUtil.isNotNull(cardno) && CommUtil.isNotNull(idtfno)
				&& CommUtil.isNotNull(idtftp)) {
			// 检查证件类型，证件号码和电子账号是否匹配
			if (!CommUtil.equals(custac, custac1)) {
				throw CaError.Eacct.E0001("电子账号和证件号码不匹配");
			}
		}*/

		Page<IoCaQryModInfoList> listQryMod = EacctMainDao.selKnbCacqInfos(
				cardno, idtftp, idtfno, bgindt, matudt, mantwy, servtp, brchno,
				startno, input.getPagesize(), totlCount, false);
		output.getSelupdInfoList().addAll(listQryMod.getRecords());
		output.setCounts(ConvertUtil.toInteger(listQryMod.getRecordCount()));

		// 设置报文头总记录条数
		CommTools.getBaseRunEnvs().setTotal_count(
				listQryMod.getRecordCount());
	}

	/**
	 * @author songkailei 涉案及可疑登记簿查询
	 * 
	 */
	@Override
	public void qryInac(
			String cardno,
			String custna,
			String idtfno,
			E_IDTFTP idtftp,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaQryInacInfo.Output output) {

		// 检查输入项
		// 户名
		if (CommUtil.isNull(custna)) {
			throw CaError.Eacct.BNAS1107();
		}
		// 证件类型证件号码必须都不为空，或都为空
		if (CommUtil.isNull(idtfno)) {
			if (CommUtil.isNotNull(idtftp)) {
				throw CaError.Eacct.BNAS0152();
			}
		} else {
			if (CommUtil.isNull(idtftp)) {
				throw CaError.Eacct.BNAS0156();
			}
		}
//		// 账号，证件类型不允许同时输入
//		if (CommUtil.isNotNull(cardno) && CommUtil.isNotNull(idtfno)) {
//			throw CaError.Eacct.E0001("账号和证件信息不允许同时输入！");
//		}

		String datatp = null;// 数据类型
		// 初始化涉案可疑标识
		E_INSPFG inspfg = E_INSPFG.NONE; // 是否涉案账户
		E_INSPFG inspfg1 = E_INSPFG.NONE;
		
		// 账号，证件信息必须有一个输入
		if (CommUtil.isNull(cardno)) {
			if (CommUtil.isNull(idtfno)) {
				throw CaError.Eacct.BNAS0147();
			} else {
				cardno = idtftp + "_" + idtfno;
				datatp = "IDType_IDNumber";
			}
		} else {
			datatp = "AccountNumber";
		}
		
		if(CommUtil.isNotNull(cardno)){
			datatp = "AccountNumber";
			
			// 查询是否涉案
			List<KnbAcif> tblKnbAcif = KnbAcifDao.selectAll_odb3(datatp, cardno, custna,E_YES___.YES, false);

			// 判断涉案标志
			if (CommUtil.isNotNull(tblKnbAcif)) {
				for (KnbAcif acif :tblKnbAcif ){
					
					
					if(acif.getInevil() ==  E_INSPFG.INVO){
						inspfg = E_INSPFG.INVO;
						
						//如果是涉案账户则跳出循环
						break;
					}
					if(acif.getInevil() ==  E_INSPFG.SUSP){
						
						//如果是可疑账户则继续检查
						inspfg = E_INSPFG.SUSP;				
					}
				}
			}
		}
	
		if(CommUtil.isNotNull(idtfno)){
			cardno = idtftp + "_" + idtfno;
			datatp = "IDType_IDNumber";
			
			// 查询是否涉案
			List<KnbAcif> tblKnbAcif = KnbAcifDao.selectAll_odb3(datatp, cardno, custna,E_YES___.YES, false);

			// 判断涉案标志
			if (CommUtil.isNotNull(tblKnbAcif)) {
				for (KnbAcif acif :tblKnbAcif ){
					
					
					if(acif.getInevil() ==  E_INSPFG.INVO){
						inspfg1 = E_INSPFG.INVO;
						
						//如果是涉案账户则跳出循环
						break;
					}
					if(acif.getInevil() ==  E_INSPFG.SUSP){
						
						//如果是可疑账户则继续检查
						inspfg1 = E_INSPFG.SUSP;				
					}
				}
			}
		}
		
		//如果任意方式查出账号涉案，则返回涉案
		if(inspfg == E_INSPFG.INVO || inspfg1 == E_INSPFG.INVO){
			inspfg = E_INSPFG.INVO;
		}
		//一方可疑一方空，或者双方都可疑则返回可疑
		if((inspfg == E_INSPFG.SUSP && inspfg1 == E_INSPFG.SUSP) || (inspfg == E_INSPFG.SUSP && inspfg1 == E_INSPFG.NONE)
				|| (inspfg == E_INSPFG.NONE && inspfg1 == E_INSPFG.SUSP)){
			inspfg = E_INSPFG.SUSP;
		}
		
		
		// 输出
		output.setInspfg(inspfg);// 涉案可疑标识

	}

	/**
	 * 查询电子账户休眠登记簿列表信息
	 */
	@Override
	public void QrySlep(
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaQrySlep.Input input,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaQrySlep.Output output) {
		// TODO Auto-generated method stub
		String brchno = input.getBrchno();// 机构号
		long pageno = input.getPageno();// 页码
		Integer pagesize = input.getPagesize();// 页容量
		String startdt = input.getStartdt();// 起始日期
		String cardno = input.getCardno();// 电子账号
		String stopdt = input.getStopdt();// 终止日期
		E_SLEPST slepst = input.getSlepst();// 休眠状态

		if (CommUtil.isNull(brchno)) {
			throw CaError.Eacct.BNAS0655();
		}
		// 页数
		if (CommUtil.isNull(pageno)) {
			throw CaError.Eacct.BNAS0249();
		}

		// 页容量
		if (CommUtil.isNull(pagesize)) {
			throw CaError.Eacct.BNAS0461();
		}

		if (CommUtil.isNotNull(brchno)) {
			// IoPbKubBrch cplKubBrch =
			// AccountLimitDao.selKubBrchByBrhcno(brchno, false);
			IoBrchInfo cplKubBrch = SysUtil.getInstance(IoSrvPbBranch.class)
					.getBranch(brchno);
			if (CommUtil.isNull(cplKubBrch)) {
				throw CaError.Eacct.BNAS1268();
			}
		}

		// 起始日期输入的，终止日期必输，且终止日期不得小于起始日期
		if (CommUtil.isNotNull(startdt)) {
			if (CommUtil.isNotNull(cardno)) {
				throw CaError.Eacct.BNAS0375();
			}
			if (CommUtil.isNull(stopdt)) {
				throw CaError.Eacct.BNAS0061();
			}
			if (CommUtil.compare(DateTools2.calDateByTerm(startdt, "1Y"), stopdt) < 0) {
				throw CaError.Eacct.BNAS1077();
			}
			if (startdt.compareTo(CommTools.getBaseRunEnvs().getTrxn_date()) > 0) {
				throw DpModuleError.DpstComm.BNAS1617();
			}
			if (stopdt.compareTo(startdt) < 0) {
				throw CaError.Eacct.BNAS0060();
			}
		}

		// if (CommUtil.isNull(slepst)) {
		// throw CaError.Eacct.E0001("操作方式不能为空");
		// }

		long starno = (pageno - 1) * pagesize; // 起始数
		int totlCount = 0;

		Page<IoCaQrySlepInfo> slepInfo = EacctMainDao.selSlepInfoBybrchno(
				brchno, cardno, startdt, stopdt, slepst, starno, pagesize,
				totlCount, false);
		
		output.getSleepInfoList().addAll(slepInfo.getRecords());
		output.setCount(slepInfo.getRecordCount());// 设置总记录数

		CommTools.getBaseRunEnvs().setTotal_count(
				slepInfo.getRecordCount());// 设置报文头总记录条数
	}
	
	/**
	 * 
	 * @Title: IoCaQryInwadeInfo 
	 * @Description: 涉恐名单查询，与涉案分离；涉恐只能是证件号加证件类型 查询
	 * @param cardno
	 * @param custna
	 * @param idtftp
	 * @param idtfno
	 * @author songkailei
	 * @date 2016年11月14日 上午8:51:26 
	 * @version V2.3.0
	 */
	
	@Override
	public void IoCaQryInwadeInfo(
			String cardno,
			String custna,
			E_IDTFTP idtftp,
			String idtfno) {
		// 检查输入项
		
		// 证件类型证件号码必须都不为空，或都为空
		if (CommUtil.isNull(idtfno)) {
			if (CommUtil.isNotNull(idtftp)) {
				throw CaError.Eacct.BNAS0152();
			}
		} else {
			if (CommUtil.isNull(idtftp)) {
				throw CaError.Eacct.BNAS0156();
			}
		}

		String datatp = "IDType_IDNumber";// 涉恐数据类型只能为证件类型加证件号码
		E_IDTFTP cifidtftp = null;
		String cifidtfno = null;
		
		// 账号，证件信息必须有一个输入
		if (CommUtil.isNull(cardno)) {
			
			if (CommUtil.isNull(idtfno)) {
				throw CaError.Eacct.BNAS0147();
			} else {
				cardno = idtftp + "_" + idtfno;
				
				//根据证件类型和证件号码查询客户名称
				//客户信息拆分取消，模块拆分
//				IoSrvCfPerson.IoGetCifCust.InputSetter queryCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//				IoSrvCfPerson.IoGetCifCust.Output cplCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//				IoSrvCfPerson cifCustServ = SysUtil.getInstance(IoSrvCfPerson.class);
//				queryCifCust.setIdtftp(idtftp);
//				queryCifCust.setIdtfno(idtfno);
//				cifCustServ.getCifCust(queryCifCust, cplCifCust);
//				
//				if(CommUtil.isNotNull(cplCifCust)){
//					custna = cplCifCust.getCustna();
//				}
			}
		} else {
			//若电子账户不为空，则根据电子账户查询客户信息
			IoCucifCust cifcust = CaDao.selIdtpnoBycardno(cardno, false);
			
			if(CommUtil.isNotNull(cifcust)){
				cifidtftp = cifcust.getIdtftp();
				cifidtfno = cifcust.getIdtfno();
				custna = cifcust.getCustna();
			}
			
			
			//检查输入的证件号码与证件类型是否与原记录一致
			if(CommUtil.isNotNull(idtfno)){
				if(CommUtil.isNotNull(cifcust)){
					if(idtftp != cifidtftp){
						throw CaError.Eacct.BNAS1261();
					}
				}
				
				
				if(!CommUtil.equals(idtfno, cifidtfno)){
					throw CaError.Eacct.BNAS1647();
				}
				
				cardno = cifidtftp + "_" + cifidtfno;
				
			}else{
				cardno = cifidtftp + "_" + cifidtfno;
			}
			
			
			}
		KnbAcif tblknbacif = KnbAcifDao.selectOne_odb1(datatp, cardno, custna, E_INSPFG.INEV, false);
		
		if(CommUtil.isNotNull(tblknbacif)){
			if(tblknbacif.getIseffe() == E_YES___.YES){
				throw DpModuleError.DpstComm.BNAS0574(); 
			}
		}
	}

	public void ioCaqryKnaUnat( String cardno,  String custna,  final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaqryKnaUnat.Output output){
		
		if(CommUtil.isNull(cardno)){
			throw CaError.Eacct.BNAS0190();
		}
		
		if(CommUtil.isNull(custna)){
			throw DpModuleError.DpstComm.BNAS0534();
		}
		
		//查询非活跃登记簿
		KnbUnat tblknaunat = KnbUnatDao.selectFirst_odb1(cardno, custna, E_YES___.YES, false);
		
		if(CommUtil.isNotNull(tblknaunat)){
			output.setIsacti(E_YES___.YES);
		}
		
	}
	
	/**
	 * 涉恐检查
	 */

	public void ioCaChkWade( cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP idtftp,  String idtfno,  String cardno,  final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaChkWade.Output output){
		
		if(CommUtil.isNull(cardno)){
			throw CaError.Eacct.BNAS0190();
		}
		//判断电子账户是否存在
		IoCaKnaAcdc inacdc = ActoacDao.selKnaAcdc(cardno, false);
        if(CommUtil.isNull(inacdc)){
            throw DpModuleError.DpstComm.BNAS0750();
        }
		//根据卡号查电子账号
		KnaAcdc tblknaacdc = KnaAcdcDao.selectOne_odb2(cardno, false);
		if(CommUtil.isNull(tblknaacdc)){
			throw CaError.Eacct.BNAS0200();
		}
		
		if(CommUtil.isNotNull(idtftp) || CommUtil.isNotNull(idtfno)){
			throw CaError.Eacct.BNAS0203();
		}
		
		//根据电子账号查客户号
		IoCaKnaCust knacust = SysUtil.getInstance(IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(tblknaacdc.getCustac(), true);
		
		//根据客户号查证件类型及证件号码
		//客户信息相关取消掉，模块拆分
//		IoSrvCfPerson.IoGetCifCust.InputSetter queryCifCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//		IoSrvCfPerson.IoGetCifCust.Output tblcifcust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//		IoSrvCfPerson cifCustServ = SysUtil.getInstance(IoSrvCfPerson.class);
//		queryCifCust.setCustno(knacust.getCustno());
//		cifCustServ.getCifCust(queryCifCust, tblcifcust);
		//暂不支持直接用证件查
		/*if(CommUtil.isNull(idtftp)){
			throw CaError.Eacct.E0001("证件类型不能为空！");
		}
		
		if(CommUtil.isNull(idtfno)){
			throw CaError.Eacct.E0001("证件号码不能为空！");
		}
		
		//若证件类型为身份证，则进行校验，其它不校验
		if(idtftp == E_IDTFTP.SFZ){
			BusiTools.chkCertnoInfo(idtftp, idtfno);
		}*/
		
		//根据证件类型及证件号码查询黑名单登记簿
		String datatype = "IDType_IDNumber";
//		String card = tblcifcust.getIdtftp() + "_" + tblcifcust.getIdtfno();
		
		List<KnbAcif> tblKnbAcif = KnbAcifDao.selectAll_odb6(datatype, "", E_YES___.YES, false);
		
		if(CommUtil.isNotNull(tblKnbAcif)){
			output.setIswade(E_YES___.YES);
		}else{
			output.setIswade(E_YES___.NO);
		}
	}
	
	/**
	 * 
	 * @Title: IoQryDeRegistry
	 * @Description: 电子账户降级登记簿查询
	 * @param 降级登记簿查询输入集合
	 * @author guizhengjia
	 * @date 2017年09月11日 下午14:10:26 
	 * @version V2.3.0
	 */
	@Override
	public void QryDeRegistry(
			IoCaQryDeRegIn cplDegInfo,
			cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryAccout.IoCaQryDeRegistry.Output output) {

		E_PROMST deomst = E_PROMST.SUCCESS;//降级状态
		String idtfno = cplDegInfo.getIdtfno();// 证件号码
		E_IDTFTP idtftp = cplDegInfo.getIdtftp();// 证件类型
		String stardt = cplDegInfo.getStardt();// 起始日期
		String stopdt = cplDegInfo.getStopdt();// 终止日期
		String brchno = cplDegInfo.getBrchno();// 账户归属机构

		int totlCount = 0; // 记录总数
		int startno = (cplDegInfo.getPageno() - 1) * cplDegInfo.getPgsize();// 起始记录数
		
		// 账户归属机构不能为空
		if (CommUtil.isNull(brchno)) {
			throw CaError.Eacct.BNAS0888();
		}
		// 页数
		if (CommUtil.isNull(cplDegInfo.getPageno())) {
			throw CaError.Eacct.BNAS0977();
		}
		// 页容量
		if (CommUtil.isNull(cplDegInfo.getPgsize())) {
			throw CaError.Eacct.BNAS0461();
		}

		// 起始日期，终止日期必须都不为空，或都为空
		if (CommUtil.isNull(stardt)) {
			if (CommUtil.isNotNull(stopdt)) {
				throw CaError.Eacct.BNAS0410();
			}
		} else {
			if (CommUtil.isNull(stopdt)) {
				throw CaError.Eacct.BNAS0408();
			}
		}

		// 起始日期不能大于终止日期，终止日期不能大于当前日期,查询打印时间间隔不能超过一年
		if (CommUtil.isNotNull(stardt) && CommUtil.isNotNull(stopdt)) {
			if (CommUtil.compare(stardt, stopdt) > 0) {
				throw CaError.Eacct.BNAS0412();
			}
			if (CommUtil.compare(stopdt, CommTools.getBaseRunEnvs().getTrxn_date()) > 0) {
				throw CaError.Eacct.BNAS0062();
			}
			if (CommUtil.compare(DateTools2.calDateByTerm(stardt, "1Y"), stopdt) < 0) {
				throw CaError.Eacct.E0001("终止日期和起始日期间隔不能超过一年！");
			}

		}

		// 证件类型证件号码必须都不为空，或都为空
		if (CommUtil.isNull(idtfno)) {
			if (CommUtil.isNotNull(idtftp)) {
				throw CaError.Eacct.BNAS0152();
			}
		} else {
			if (CommUtil.isNull(idtftp)) {
				throw CaError.Eacct.BNAS0156(); 
			}
		}
		
		// 检查证件类型证件号码是否有效
		if (CommUtil.isNotNull(idtfno) && CommUtil.isNotNull(idtftp)) {
			BusiTools.chkCertnoInfo(idtftp, idtfno);
		}	
		
		// 分页查询电子账户降级登记簿
		if (CommUtil.isNotNull(idtfno) && CommUtil.isNotNull(idtftp)) {
			// 当证件类型证件号码存在时，机构号默认为空
			brchno = null;
			
			//根据证件类型和证件号码查询客户号 
			//取消客户信息相关内容模块拆分
//			CustInfo custInfo = SysUtil.getInstance(IoCuCustSvcType.class).selById(idtftp, idtfno);
			
			//根据客户号查询客户信息 可能多条 
			List<IoCaKnaCust> caKnaCust = CaDao.selKnaCustAByCustno("", true);
			Page<IoCaQryDeRegOut> deInfoList = SysUtil.getInstance(Page.class);
			StringBuffer custacs = new StringBuffer(); 
			
			//若只有一条记录则不存在销户重开的情况
			if(CommUtil.isNotNull(caKnaCust) && caKnaCust.size() == 1){
				deInfoList = EacctMainDao.selInfoDeregTable(
						deomst, brchno, stardt, stopdt, caKnaCust.get(0).getCustac(), 
						startno, cplDegInfo.getPgsize(), totlCount, false);
			}
			
			//若存在多条记录则查询全部降级信息
			if(caKnaCust.size() > 1){
				for(IoCaKnaCust cust : caKnaCust){
					custacs = custacs.append(cust.getCustac()).append(",");
				}
				
				//去除最后的“，”
				String custacss = custacs.substring(0, custacs.length()-1);
				bizLog.debug("++++++" + custacss + "++++++++");
				deInfoList = EacctMainDao.selInfoDeregTables(
						deomst, brchno, stardt, stopdt, custacss, 
						startno, cplDegInfo.getPgsize(), totlCount, false);
				
			}
			
			for(IoCaQryDeRegOut deinfo : deInfoList.getRecords()){
				//查询卡客户账号对照表
				KnaAcdc tblKna_acdc = CaDao.selKnaAcdcByCustac(deinfo.getCustac(), false);
				
				deinfo.setIdtfno(idtfno);
				deinfo.setIdtftp(idtftp);
				deinfo.setCardno(tblKna_acdc.getCardno());
				
				KnaAcal telinfo = EacctMainDao.selknaAcalbycustac(deinfo.getCustac(), false);
				if(CommUtil.isNotNull(telinfo)){
					deinfo.setTeleno(telinfo.getTlphno());
				}
				
			}
			
			// 将查询出的数据和总笔数映射到输出接口
			output.getDegInfoList().addAll(deInfoList.getRecords());
			output.setCounts(ConvertUtil.toInteger(deInfoList.getRecordCount()));
			
			// 设置报文头总记录条数
			CommTools.getBaseRunEnvs().setTotal_count(deInfoList.getRecordCount());
			
		} else {
			
			Page<IoCaQryDeRegOut> deInfoList = EacctMainDao.selInfoDeregTable(
					deomst, brchno, stardt, stopdt, null, 
					startno, cplDegInfo.getPgsize(), totlCount, false);
			
			//循环写入证件类型/号码、电子账号  
			for(IoCaQryDeRegOut deinfo : deInfoList.getRecords()){
				//查询客户信息
				CustInfo custInfo = EacctMainDao.selIdIfInfo(deinfo.getCustac(), false);
				//查询卡客户账号对照表
				KnaAcdc knaAcdc = CaDao.selKnaAcdcByCustac(deinfo.getCustac(), false);
				
				deinfo.setIdtfno(custInfo.getIdtfno());
				deinfo.setIdtftp(custInfo.getIdtftp());
				deinfo.setCardno(knaAcdc.getCardno());
				
				KnaAcal telinfo = EacctMainDao.selknaAcalbycustac(deinfo.getCustac(), false);
				if(CommUtil.isNotNull(telinfo)){
					deinfo.setTeleno(telinfo.getTlphno());
				}
				
			}
			
			// 将查询出的数据和总笔数映射到输出接口
			output.getDegInfoList().addAll(deInfoList.getRecords());
			output.setCounts(ConvertUtil.toInteger(deInfoList.getRecordCount()));
			
			// 设置报文头总记录条数
			CommTools.getBaseRunEnvs().setTotal_count(deInfoList.getRecordCount());

		}
	
	}
}
