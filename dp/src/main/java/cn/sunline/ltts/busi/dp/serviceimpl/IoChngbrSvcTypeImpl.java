package cn.sunline.ltts.busi.dp.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.namedsql.AccChngbrDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccChngbrSvc.Chngbr.Input;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccChngbrSvc.Chngbr.Output;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountManager;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCacd;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbCacq;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbCgbr;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbClac;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaOpenAccInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.ApSmsType.DpBindMqService;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STATUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BINDTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MANTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DCMTST;

/**
 * 
 * @ClassName: IoChngbrSvcTypeImpl
 * @Description: 电子账户变更服务实现
 * @author chengen
 * @date 2016年7月7日 上午9:47:28
 * 
 */
public class IoChngbrSvcTypeImpl implements
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccChngbrSvc {

	@Override
	public void changebr(Input input, Output output) {

		String sCustac = input.getCustac();// 电子账号
		String sCardno = input.getCardno();// 虚拟交易卡号
		String sNwbrno = input.getNwbrno();// 变更的机构号
		E_ACCATP eAccatp = input.getAccatp();// 账户分类
		String sTrandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易时间
		String stmstmp = DateTools2.getCurrentTimestamp();// 时间戳
		String eServtp = CommTools.getBaseRunEnvs().getChannel_id();// 交易渠道
		E_ACCTST eAcctst = E_ACCTST.NORMAL;

		// 查询账户变更机构是否存在
		IoBrchInfo brinfo = SysUtil.getInstance(IoSrvPbBranch.class)
				.getBranch(input.getNwbrno());

		if (CommUtil.isNull(brinfo)) {
			throw DpModuleError.DpstComm.BNAS0167();
		}
		
		// 查询电子账户表
		IoCaKnaCust cplKnaCust = SysUtil.getInstance(
				IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(sCustac,
				false);
		if (CommUtil.isNull(cplKnaCust)) {
			throw DpModuleError.DpstComm.BNAS0928();
		}

/*		// 查询卡号是否已经登记了复用信息
		IoCaKnbRecl cplKnbRecl = SysUtil.getInstance(
				IoCaSevQryTableInfo.class).knb_recl_selectOne_odb1(sCardno,
				false);

		if (CommUtil.isNotNull(cplKnbRecl)) {
			// 更新信息
			SysUtil.getInstance(IoCaSevQryTableInfo.class)
					.knb_recl_updateOne_odb1(sCardno, sTrandt, E_ENABST.ENABLED);
		} else {
			throw DpModuleError.DpstComm.E9027("需要重启账号未查询到记录");
		}*/
		
		// 查询销户登记簿中的绑定手机号，用户ID
		IoCaKnbClac cplKnbClac = SysUtil.getInstance(IoCaKnbClac.class);
		List<IoCaKnbClac> lstKnbClac = AccChngbrDao.selKnbclacByCus(sCustac,
				false);
		if (CommUtil.isNotNull(lstKnbClac) && lstKnbClac.size() != 0) {
			cplKnbClac = lstKnbClac.get(0);
		}

		// 查询绑定信息修改登记簿
		List<IoCaKnbCacq> lstKnbCacq = AccChngbrDao.selKnbcacqByClossq(
				cplKnbClac.getClossq(), false);
		for (IoCaKnbCacq cplKnbCacq : lstKnbCacq) {
			// 更新外部卡与客户账户对照表
			AccChngbrDao.updKnacacdByCardno(brinfo.getCorpno(), stmstmp,
					E_DPACST.NORMAL, cplKnbCacq.getOdopac(), sCustac);
			
			// 登记账户信息修改登记簿
			AccChngbrDao.insKnbcacqInfo(brinfo.getCorpno(), sCustac, null,
					cplKnbCacq.getOdopac(), sTrandt, eServtp, E_MANTWY.ADD,
					null, stmstmp);
			
			// 查询外部卡与客户账户对照表
			IoCaKnaCacd cplKnaCacd = AccChngbrDao.selKnacacdByCardno(cplKnbCacq.getOdopac(), sCustac, brinfo.getCorpno(), false);
			if (CommUtil.isNull(cplKnaCacd)){
				throw DpModuleError.DpstComm.BNAS1096();
			}

			// 发送绑定账户MQ信息
			/*KnpParameter para = KnpParameterDao.selectOne_odb1("BDCAMQ", "%", "%", "%", true);
			
			String bdid = para.getParm_value1();// 服务绑定ID
			
			String mssdid = CommTools.getMySysId();// 随机生成消息ID
			
			String mesdna = para.getParm_value2();// 媒介名称
			 		
			IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
			
			IoCaOtherService.IoCaBindMqService.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaBindMqService.InputSetter.class);
			*/	
			
			E_BINDTP bindtp = E_BINDTP.BIND;// 绑定方式
			//MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
			//mri.setMtopic("Q0101003");
			DpBindMqService mqInput = SysUtil.getInstance(DpBindMqService.class);
			
			//mqInput.setMsgid(mssdid); //发送消息ID
			//mqInput.setMdname(mesdna); //媒介名称
			mqInput.setBindst(bindtp); //绑定方式
			mqInput.setEactno(sCardno); //电子账号
			mqInput.setBindno(cplKnbCacq.getOdopac()); //绑定账户
			mqInput.setAtbkno(cplKnaCacd.getBrchno()); //账户开户行号
			mqInput.setAcusna(cplKnaCacd.getAcctna()); //绑定账户名称
			mqInput.setAtbkna(cplKnaCacd.getBrchna()); //账户开户行名称
			mqInput.setAccttp(cplKnaCacd.getCardtp()); //绑定账户类型
			mqInput.setIsiner(cplKnaCacd.getIsbkca()); //是否本行账户
			mqInput.setCustid(cplKnbClac.getCustid());// 用户ID
			
//			mri.setMsgtyp("ApSmsType.DpBindMqService");
//			mri.setMsgobj(mqInput); 
//			AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
			CommTools.addMessagessToContext("Q0101003", "ApSmsType.PbinusSendMsg");
//			//caOtherService.bindMqService(mqInput);
		
		}
		// 变更电子账户信息表
		AccChngbrDao.updKnaCustByCus(sCustac, brinfo.getCorpno(), sNwbrno,
				eAcctst, stmstmp);
		// 变更卡信息表
		AccChngbrDao.updKcdcardByCus(sCustac, brinfo.getCorpno(),
				E_DCMTST.NORMAL, stmstmp);
		// 变更账户别名信息表
		AccChngbrDao.updKnaacalByCus(brinfo.getCorpno(), E_ACALST.NORMAL,
				sCustac, cplKnbClac.getAcalno(), E_ACALTP.CELLPHONE, stmstmp);
		// 变更负债账户客户账户对照表
		AccChngbrDao.updKnaaccsByCus(brinfo.getCorpno(), E_DPACST.NORMAL,
				sCustac, stmstmp);
		// 变更负债账户信息表
		AccChngbrDao.updKnaacctByCus(brinfo.getCorpno(), sNwbrno,
				E_DPACST.NORMAL, sCustac, stmstmp);
		// 变更卡客户账户对照表
		AccChngbrDao.updKnaacdcByCus(brinfo.getCorpno(), E_DPACST.NORMAL,
				sCustac, stmstmp);
		// 变更客户信息关联表
		AccChngbrDao.updCifcustaccsByCustid(E_STATUS.NORMAL,
				cplKnbClac.getCustid());
		// 变更负债活期账户支取控制表
		AccChngbrDao.updKnadrawByCus(sCustac, brinfo.getCorpno(), stmstmp);
		// 变更负债活期账户存入控制表
		AccChngbrDao.updKnasaveByCus(sCustac, brinfo.getCorpno(), stmstmp);
		// 变更负债账户计息信息表
		AccChngbrDao.updKnbacinByCus(sCustac, brinfo.getCorpno(), stmstmp);
		// 变更账户利率信息表
		AccChngbrDao.updKubinrtByCus(sCustac, brinfo.getCorpno(), stmstmp);
		// 变更负债活期账户存入计划表
		AccChngbrDao.updKnasaveplanByCus(sCustac, brinfo.getCorpno(), stmstmp);
		// 变更负债活期账户支取计划表
		AccChngbrDao.updKnadrawplanByCus(sCustac, brinfo.getCorpno(), stmstmp);
		// 变更负债活期账户到期信息表
		AccChngbrDao.updKnaacctmatuByCus(sCustac, brinfo.getCorpno(), stmstmp);
		// 变更负债账户附加信息表
		AccChngbrDao.updKnaacctaddtByCus(sCustac, brinfo.getCorpno(), stmstmp);
		// 变更活期附加产品信息表
		AccChngbrDao.updKnaacctprodByCus(brinfo.getCorpno(), stmstmp, sCustac);
		
		// 登记变更机构登记簿
		IoCaKnbCgbr cplKnbCgbr = SysUtil.getInstance(IoCaKnbCgbr.class);
		cplKnbCgbr.setCustac(sCustac);// 电子账号
		cplKnbCgbr.setOdbrch(cplKnaCust.getBrchno());// 原归属机构
		cplKnbCgbr.setNwbrch(sNwbrno);// 变更机构
		cplKnbCgbr.setOdcpno(cplKnaCust.getCorpno());// 原法人代码
		cplKnbCgbr.setNwopno(brinfo.getCorpno());// 新法人代码
		cplKnbCgbr.setChngdt(sTrandt);// 变更日期
		cplKnbCgbr.setTranus(CommTools.getBaseRunEnvs().getTrxn_teller());// 交易柜员
		cplKnbCgbr.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
		cplKnbCgbr.setServtp(eServtp);// 交易渠道
		cplKnbCgbr.setTmstmp(stmstmp);// 时间戳
		cplKnbCgbr.setMtdate(sTrandt);// 维护日期
		SysUtil.getInstance(IoCaSevAccountManager.class).knbCgbrInsert(cplKnbCgbr);
			
		// 登记开户登记簿
		IoDpKnaAcct cplKnaAcct = SysUtil.getInstance(DpAcctSvcType.class).getSettKnaAcctAc(sCustac);
		IoCaOpenAccInfo cplKnbOpac = SysUtil.getInstance(IoCaOpenAccInfo.class);
		cplKnbOpac.setCustac(sCustac);// 电子账号
		cplKnbOpac.setAccttp(eAccatp);// 账户分类
		cplKnbOpac.setCustna(cplKnaCust.getCustna());// 客户名称
		cplKnbOpac.setTlphno(cplKnbClac.getAcalno());// 绑定手机号
		if (CommUtil.isNotNull(cplKnaAcct)) {
			cplKnbOpac.setCrcycd(cplKnaAcct.getCrcycd());// 币种
			cplKnbOpac.setCsextg(cplKnaAcct.getCsextg());// 钞汇标识
		}
		cplKnbOpac.setBrchno(sNwbrno);// 开户机构
		cplKnbOpac.setOpendt(sTrandt);// 开户日期
		cplKnbOpac.setOpenus(CommTools.getBaseRunEnvs().getTrxn_teller());// 开户柜员
		cplKnbOpac.setOpensq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 开户流水
		cplKnbOpac.setUschnl(CommUtil.toEnum(BaseEnumType.E_USCHNL.class, eServtp));// 开户渠道
		SysUtil.getInstance(IoCaSevAccountManager.class).knbOpacInsert(cplKnbOpac);
		
		// 登记客户化状态
		IoCaUpdAcctstIn cplDimeInfo = SysUtil.getInstance(IoCaUpdAcctstIn.class);
		cplDimeInfo.setCustac(sCustac);
		cplDimeInfo.setDime01(eAccatp.getValue()); // 维度1 电子账户类型
		SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(
				cplDimeInfo);

		// 输出映射
		output.setCustid(cplKnbClac.getCustid());
		output.setAcalno(cplKnbClac.getAcalno());

	}

	/**
	 * 
	 * @Title: judgeOpenJust
	 * @Description: (判断是否只发生过开户交易)
	 * @author xiongzhao
	 * @date 2016年11月3日 下午2:20:58
	 * @version V2.3.0
	 */
	@Override
	public E_YES___ judgeOpenJust(String custac) {

		E_YES___ obopfg = E_YES___.YES;// 是否只开过户标志

		// 检查是否有升级交易
		int counts = AccChngbrDao.selKnbpromByCus(custac, false);
		if (counts > 0) {
			obopfg = E_YES___.NO;
		}

		// 检查是否有资金交易
		List<KnaAcct> tblKnaAcct = new ArrayList<KnaAcct>();
		tblKnaAcct = AccChngbrDao.selKnaacctByUpbldt(custac, false);
		for (KnaAcct acct : tblKnaAcct) {
		    if (CommUtil.isNull(acct)) {
	            obopfg = E_YES___.YES;
		    }
		    else{
		        String upbldt = acct.getUpbldt();
		        if (!CommUtil.isNull(upbldt)) {
		            obopfg = E_YES___.NO;
		        }
		    }
		}

		// 返回是否只开过户标志
		return obopfg;

	}

}
