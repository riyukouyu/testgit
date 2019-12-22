package cn.sunline.ltts.busi.dp.serviceimpl;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.busi.dp.iobus.servicetype.DpBatchDebitAcctno.addDrawAcctsDp.Input;
import cn.sunline.edsp.busi.dp.iobus.type.dp.DpdebitAcctnos;
import cn.sunline.edsp.busi.dp.iobus.type.dp.DpdebitAcctnos.BatchNoParmentAccts;
import cn.sunline.edsp.busi.dp.iobus.type.dp.DpdebitAcctnos.BatchNoParmentIn;
import cn.sunline.edsp.busi.dp.iobus.type.dp.DpdebitAcctnos.DpdebitAcctnosIn;
import cn.sunline.edsp.busi.dp.namedsql.soc.SocQyAcctsDao;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccsDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcDrawStrikeInput;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;

/**
  * 负债子账户批量扣款
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="DpBatchDebitAcctnoImpl", longname="负债子账户批量扣款", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class DpBatchDebitAcctnoImpl implements cn.sunline.edsp.busi.dp.iobus.servicetype.DpBatchDebitAcctno{

	@Override
	public void addDrawAcctsDp(Input input) {
		//input.getCplDrawAcctsIn().g;
		Options<DpdebitAcctnosIn> list = input.getCplDrawAcctsIn();
		if (CommUtil.isNull(list) || list.size() == 0) {
			throw DpModuleError.DpstComm.E9990("扣款账户列表不能为空");
		}
		String crcycd = BusiTools.getDefineCurrency();
		//预收账款 - 2203 - SocPassiveAcct
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("SocPassiveAcct", "22030101", "%", "%",true);
		BigDecimal totleAmt = BigDecimal.ZERO;
		DpAcctSvcImpl dpAcctSvcImpl = SysUtil.getInstance(DpAcctSvcImpl.class);
		if (CommUtil.isNull(CommTools.getBaseRunEnvs().getMain_trxn_seq())) {
			CommTools.getBaseRunEnvs().setMain_trxn_seq(CommTools.getBaseRunEnvs().getTrxn_seq());//sdw add
		}
		for (DpdebitAcctnosIn dpdebitAcctnosIn : list) {
			String acctno = dpdebitAcctnosIn.getAcctno();
			BigDecimal tranam = dpdebitAcctnosIn.getTranam();
			totleAmt = totleAmt.add(tranam);
			String remark = dpdebitAcctnosIn.getRemark();
			if (CommUtil.isNull(tranam) || tranam.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			if (CommUtil.isNull(acctno)) {
				throw DpModuleError.DpstComm.E9990("扣款账号不能为空");
			}
			KnaAcct knaAcct = KnaAcctDao.selectOne_odb1(acctno, false);
			if (CommUtil.isNull(knaAcct)) {
				throw DpModuleError.DpstComm.E9990("扣款账号错误：" + knaAcct);
			}
			KnaAcdc knaAcdc = KnaAcdcDao.selectFirst_odb3(knaAcct.getCustac(), false);
			KnaAccs knaAccs = KnaAccsDao.selectOne_odb2(acctno, false);
			
			DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
			cplDrawAcctIn.setAcctno(acctno);
			cplDrawAcctIn.setCrcycd(crcycd);
			cplDrawAcctIn.setTranam(tranam);
			cplDrawAcctIn.setCardno(knaAcdc.getCardno());
			cplDrawAcctIn.setCustac(knaAcct.getCustac());
			cplDrawAcctIn.setAcseno(knaAccs.getSubsac());
			cplDrawAcctIn.setToacct(knpParameter.getParm_value1());//对方账号 - 预收账款科目
			cplDrawAcctIn.setOpacna("预收账款");//对方户名
			cplDrawAcctIn.setSmrycd("SCQK");//摘要代码 - 赊销强扣
			cplDrawAcctIn.setRemark(remark);
			cplDrawAcctIn.setIssucc(E_YES___.YES);
			cplDrawAcctIn.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));
			dpAcctSvcImpl.addDrawAcctDp(cplDrawAcctIn);
			
			IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
			iaAcdrInfo.setCrcycd(crcycd);
			iaAcdrInfo.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
			iaAcdrInfo.setBusino(knpParameter.getPrimary_key1());
			iaAcdrInfo.setItemcd(knpParameter.getParm_value1());
			iaAcdrInfo.setSubsac(knpParameter.getParm_value2());
			iaAcdrInfo.setTranam(totleAmt);
			iaAcdrInfo.setToacct(acctno);
			iaAcdrInfo.setToacna(knaAcct.getAcctna());
			IoInAccount ioInAccount = SysUtil.getInstance(IoInAccount.class);
			ioInAccount.ioInAccr(iaAcdrInfo);
		}
	}

	@Override
	public void batchNoParmentIn(
			cn.sunline.edsp.busi.dp.iobus.servicetype.DpBatchDebitAcctno.batchNoParmentIn.Input input) {
		Options<BatchNoParmentIn> list = input.getBatchNoParmentIn();
		if (CommUtil.isNull(CommTools.getBaseRunEnvs().getMain_trxn_seq())) {
			CommTools.getBaseRunEnvs().setMain_trxn_seq(CommTools.getBaseRunEnvs().getTrxn_seq());//sdw add
		}
		for (BatchNoParmentIn batchNoParmentIn : list) {
			//查询子账户信息
			String mactid = batchNoParmentIn.getMactid();//主体ID
			String psedid = batchNoParmentIn.getPsedid();//品牌ID
			List<DpdebitAcctnos.BatchNoParmentAccts> list2 = SocQyAcctsDao.selAcctnoInfos(mactid, psedid, false);
			if (CommUtil.isNotNull(list2) && list2.size() > 0) {
				for (BatchNoParmentAccts batchNoParmentAccts : list2) {
					IoDpFrozSvcType ioDpFrozSvcType = SysUtil.getInstance(IoDpFrozSvcType.class);
					//加入判断改子账户是否已经止付的逻辑 - 如果已经止付，则跳过
					//KnbFrozDao.selectAll_odb11(batchNoParmentAccts.getAcctno(), E_FROZST.VALID, false);
					List<KnbFroz> knbFrozs = KnbFrozDao.selectAll_odb14(batchNoParmentAccts.getAcctno(), E_FROZTP.BANKSTOPAY, E_FROZST.VALID, false);
					if (CommUtil.isNotNull(knbFrozs)) {
						continue;
					}
					//此止付服务有问题
					ioDpFrozSvcType.IoDpSyStopay(batchNoParmentAccts.getAcctno(), batchNoParmentAccts.getOnlnbl(), batchNoParmentAccts.getCrcycd(), E_FROZTP.BANKSTOPAY);
				}
			}
		}
		
	}

	public void passivePayBackCorrect( final cn.sunline.edsp.busi.dp.iobus.servicetype.DpBatchDebitAcctno.passivePayBackCorrect.Input input){
		Options<DpdebitAcctnosIn> list = input.getDrawAcctsIn();
		if (CommUtil.isNull(list) || list.size() == 0) {
			throw DpModuleError.DpstComm.E9990("冲正交易，账户列表不能为空");
		}
		String crcycd = BusiTools.getDefineCurrency();
//		BigDecimal totleAmt = BigDecimal.ZERO;
		KnpParameter knpParameter = KnpParameterDao.selectOne_odb1("SocPassiveAcct", "22030101", "CORRECT", "%",true);
		for(DpdebitAcctnosIn dpdebitAcctnosIn:list) {
			String acctNo = dpdebitAcctnosIn.getAcctno();
			BigDecimal tranAm = dpdebitAcctnosIn.getTranam();
//			totleAmt = totleAmt.add(tranAm);
//			String remark = dpdebitAcctnosIn.getRemark();
			if (CommUtil.isNull(tranAm) || tranAm.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			if (CommUtil.isNull(acctNo)) {
				throw DpModuleError.DpstComm.E9990("扣款账号不能为空");
			}
			KnaAcct knaAcct = KnaAcctDao.selectOne_odb1(acctNo, false);
			if (CommUtil.isNull(knaAcct)) {
				throw DpModuleError.DpstComm.E9990("扣款账号错误：" + knaAcct);
			}
//			KnaAcdc knaAcdc = KnaAcdcDao.selectFirst_odb3(knaAcct.getCustac(), false);
			KnaAccs knaAccs = KnaAccsDao.selectOne_odb2(acctNo, false);
			
			//支取冲正
			IoDpStrikeSvcType service=SysUtil.getInstance(IoDpStrikeSvcType.class);

			ProcDrawStrikeInput in=SysUtil.getInstance(ProcDrawStrikeInput.class);
//			ProcDrawStrikeInput param=SysUtil.getInstance(ProcDrawStrikeInput.class); 
			in.setAcctno(acctNo);
			in.setAcctst(knaAccs.getAcctst());
			in.setAmntcd(E_AMNTCD.PY);//借贷标记-付
			in.setColrfg(BaseEnumType.E_COLOUR.RED);//红蓝字标识  -账务出方 记负数
			in.setCrcycd(crcycd);
			in.setCustac(knaAcct.getCustac());
			in.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));//余额变更流水?
			in.setInstam(BigDecimal.ZERO);//支取利息
			in.setIntxam(BigDecimal.ZERO);//利息税
			in.setOrtrdt(input.getOrderDate());//原订单日期 
			in.setPyafam(BigDecimal.ZERO);//追缴金额
			in.setTranam(tranAm);
			in.setStacps(BaseEnumType.E_STACPS.POSITIVE);
			in.setPydlsq(null);//追缴金额变更流水?
			service.procDrawStrike(in);
			
			//内部户
			IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
			iaAcdrInfo.setCrcycd(crcycd);
			iaAcdrInfo.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
			iaAcdrInfo.setBusino(knpParameter.getPrimary_key1());
			iaAcdrInfo.setItemcd(knpParameter.getParm_value1());
			iaAcdrInfo.setSubsac(knpParameter.getParm_value2());
			iaAcdrInfo.setTranam(tranAm);
			iaAcdrInfo.setToacct(acctNo);
			iaAcdrInfo.setToacna(knaAcct.getAcctna());
			IoInAccount ioInAccount = SysUtil.getInstance(IoInAccount.class);
			ioInAccount.ioInAccr(iaAcdrInfo);
		}
		
		
		
	}
}

