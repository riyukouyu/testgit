package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.namedsql.redpck.ActoacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlSpnd;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.ServEacctSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaKnbTrinInput;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_ACLMFG;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_CUSTLV;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_PYTLTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_REBKTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RISKLV;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INSPFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SPETTP;
import cn.sunline.ltts.busi.wa.type.WaAcctType.IoWaKnaRelt;



public class acprst {

public static void chkAcpaytInfo( final cn.sunline.ltts.busi.dptran.trans.client.intf.Acprst.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Acprst.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Acprst.Output output){
		
		// 获取输入项
		E_YES___ ispass = input.getChkpwd().getIspass();// 是否验密
		String passtp = input.getChkpwd().getPasstp();// 密码类型
		String authif = input.getChkpwd().getAuthif();// 加密因子
		String passwd = input.getChkpwd().getPasswd();// 密码

		// 电子账户额度扣减控制输入项
		E_YES___ isckqt = input.getChkqtn().getIsckqt();// 额度验证扣减标志
		E_ACCTROUTTYPE custtp = input.getChkqtn().getAcctrt();// 客户类型
		E_LIMTTP limttp = input.getChkqtn().getLimttp();// 额度类型
		E_RISKLV risklv = input.getChkqtn().getRisklv();// 风险承受等级
		String authtp = input.getChkqtn().getAuthtp();// 认证方式
		E_SBACTP sbactp = input.getChkqtn().getSbactp();// 子账户类型
		E_PYTLTP pytltp = input.getChkqtn().getPytltp();// 支付工具
		E_REBKTP rebktp = input.getChkqtn().getRebktp();// 收款行范围
		E_CUSTLV custlv = input.getChkqtn().getCustlv();// 客户等级
		E_ACLMFG aclmfg = input.getChkqtn().getAclmfg();// 累计限额标志
		String custid = input.getChkqtn().getCustid();// 客户id
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();
		IoBrchInfo acbrch = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(brchno);
		property.setAcbrch(acbrch.getBrchno());// 省级机构号
		
		// 输入项非空检查
		if (CommUtil.isNull(input.getCrcycd())) {
			throw DpModuleError.DpstAcct.BNAS0634();
		}
		if (CommUtil.isNull(input.getCsextg())) {
			throw DpModuleError.DpstComm.BNAS1047();
		}
		// 人民币只能是现钞
		if (CommUtil.equals(BusiTools.getDefineCurrency(), input.getCrcycd()) && E_CSEXTG.EXCHANGE == input.getCsextg()) {
			throw DpModuleError.DpstAcct.BNAS1880();
		}
		if (CommUtil.isNull(input.getTranam())) {
			throw DpModuleError.DpstAcct.BNAS0623();
		}
		if (CommUtil.compare(input.getTranam(),BigDecimal.ZERO) <= 0) {
			throw DpModuleError.DpstComm.BNAS0621();
		}
		if (CommUtil.isNull(input.getSmrycd())) {
			throw DpModuleError.DpstComm.BNAS0195();
		}
		if (CommUtil.isNull(input.getCardno())) {
			throw DpModuleError.DpstAcct.BNAS1881();
			
		}
		
		if (CommUtil.isNull(input.getAcctnm())) {
			throw DpModuleError.DpstComm.BNAS0534();
		}
		if (CommUtil.isNull(input.getIdtype())) {
			throw DpModuleError.DpstAcct.BNAS1899();
		}
		if (CommUtil.isNull(input.getIdcode())) {
			throw DpModuleError.DpstAcct.BNAS1900();
		}
		if (CommUtil.isNull(input.getCellno())) {
			throw DpModuleError.DpstAcct.BNAS1901();
		}
//		if (CommUtil.isNull(input.getTobrch())) {
//			throw DpModuleError.DpstAcct.E9999("收款方机构不能为空");
//		}
//		if (CommUtil.isNull(input.getToacct())) {
//			throw DpModuleError.DpstAcct.E9999("收款卡号/帐号不能为空");
//		}
//		if (CommUtil.isNull(input.getToacnm())) {
//			throw DpModuleError.DpstAcct.E9999("收款方名称不能为空");
//		}
		if (CommUtil.isNull(input.getChckdt())) {
			throw DpModuleError.DpstComm.BNAS0808();
		}
		if (CommUtil.isNull(input.getKeepdt())) {
			throw DpModuleError.DpstComm.BNAS0399();
		}
		
		// 判断记录是存在
		KnlSpnd tblKnlSpnd = DpAcctDao.selKnlSpndInfo(CommTools.getBaseRunEnvs().getMain_trxn_seq(), CommTools.getBaseRunEnvs().getTrxn_date(), false);
		if (CommUtil.isNotNull(tblKnlSpnd)) {
			throw DpModuleError.DpstAcct.BNAS1882();
		}
		
		
		IoCaKnaAcdc kna_acdc = DpAcctDao.selKnaAcdcByCardno(input.getCardno(), false);
		if(CommUtil.isNull(kna_acdc)){
			throw DpModuleError.DpstComm.BNAS0750();
		}
		
//		CommTools.getBaseRunEnvs().setBusi_org_id(kna_acdc.getCorpno());// 初始化法人代码
		
		E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(kna_acdc.getCustac());
		KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
		
		if(CommUtil.isNotNull(input.getAcesno())){ //子账号不为空
			tblKnaAcct = ActoacDao.selKnaAcct(input.getAcesno(), false);
		}else{
			tblKnaAcct = CapitalTransDeal.getSettKnaAcctAc(kna_acdc.getCustac());
		}
		
		if(CommUtil.isNull(tblKnaAcct)){
			throw DpModuleError.DpstAcct.BNAS1883();
		}
		//状态
		if(tblKnaAcct.getAcctst() != E_DPACST.NORMAL){
			throw DpModuleError.DpstAcct.BNAS1884();
		}
		// 账号匹配检查
		if (!CommUtil.equals(kna_acdc.getCustac(), tblKnaAcct.getCustac())) {
			throw DpModuleError.DpstAcct.BNAS1885();
		}
		
		// 币种
		if (!CommUtil.equals(input.getCrcycd(),tblKnaAcct.getCrcycd())) {
			throw DpModuleError.DpstAcct.BNAS1886();
		}
		// 客户名称
		if (!CommUtil.equals(tblKnaAcct.getAcctna(), input.getAcctnm())) {
			throw DpModuleError.DpstAcct.BNAS1887();
		}
		
		if(CommUtil.isNotNull(input.getAcsetp()) && tblKnaAcct.getAcsetp() != input.getAcsetp()){
			throw DpModuleError.DpstAcct.BNAS1888();
		}
		
		if (tblKnaAcct.getAcsetp() == E_ACSETP.FW && E_YES___.YES == isckqt) {
			throw DpModuleError.DpstAcct.BNAS1889();
		}
		
		// 亲情钱包消费，本人电子账户状态为停用、状态字为账户保护不允许交易
		if (tblKnaAcct.getAcsetp() == E_ACSETP.FW) {
			IoWaKnaRelt cplKnaRelt = DpAcctDao.selknaReltId(input.getAcesno(), true);
			
			// 查询关联人电子账号信息
//			IoCaKnaCust cplKnaCust = SysUtil.getInstance(IoWaSrvWalletAccountType.class).kna_cust_selectOne(cplKnaRelt.getElacct());
//			if (CommUtil.isNotNull(cplKnaCust)) {
//				// 检查关联人电子账户状态是否允许被开通亲情钱包
//				E_CUACST euacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(cplKnaCust.getCustac());
//				if (E_CUACST.OUTAGE == euacst) {
//					throw DpModuleError.DpstAcct.BNAS1890();
//				}
//				
//				// 判断关联人电子账户状态字是否能创建亲情钱包
//				IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(cplKnaCust.getCustac());
//				if (CommUtil.isNotNull(cplAcStatus)) {
//					if (E_YES___.YES == cplAcStatus.getClstop()) {
//						
//						throw DpModuleError.DpstAcct.BNAS1891();
//					}
//				}
//				
//			} else {
//				// 没有开立电子账户不处理
//			}
		
		}
				
		// 检查验密信息
		chkPasswd(ispass, passtp, authif, passwd);

		// 检查电子账户额度扣减控制
		//chkqtn(isckqt, custtp, limttp, risklv, authtp, sbactp, pytltp, rebktp, custlv, aclmfg, custid);
	
		
		property.setCustac(kna_acdc.getCustac());
		property.setAcctno(tblKnaAcct.getAcctno());
		property.setAccatp(accatp);
		property.setAcsetp2(tblKnaAcct.getAcsetp());
		// 取得收款方法人代码
		String incorp = null;
		try{
			incorp = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(input.getTobrch()).getCorpno();
		}
		catch (Exception e) {
			// 不处理
		}
		
		// 面签标志
		E_YES___ facesg = DpAcctQryDao.selFacesgByCustac(kna_acdc.getCustac(), false);
		
		// 设置属性值信息
		property.setFacesg(CommUtil.nvl(facesg, E_YES___.NO));// 面签标志
		property.setIncorp(incorp);// 收款方法人代码
		property.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());// 机构号
		property.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
//		property.setServtp(String.DP);// TODO 报文头交易渠道有问题，取出为交易码
		property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());// 渠道交易日期
		property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 渠道流水
		property.setCustac(kna_acdc.getCustac());// 电子账号

		CapitalTransCheck.ChkAcctstOT(kna_acdc.getCustac()); //1.转出方电子账户状态校验
		CapitalTransCheck.ChkAcctstWord(kna_acdc.getCustac()); //1.转出方电子账户状态字校验
		
		// 获取业务编号
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		para = KnpParameterDao.selectOne_odb1("InParm.clearbusi",
				"in", E_CLACTP._02.getValue(), "%", true);
		
		property.setBusino(para.getParm_value1()); //业务编码
		property.setSubsac(para.getParm_value2());//子户号
		property.setSpettp(E_SPETTP.PAY);// 电子账户缴费
		
	}

	public static void chkTransAfter( final cn.sunline.ltts.busi.dptran.trans.client.intf.Acprst.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Acprst.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Acprst.Output output){
	
		// 平衡性检查
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();

		IoCheckBalance ioCheckBalance = SysUtil.getInstance(IoCheckBalance.class);
		ioCheckBalance.checkBalance(trandt, transq,E_CLACTP._02);
	}

	/**
	 * 验密信息检查
	 * 
	 * @param ispass
	 *            是否验密
	 * @param passtp
	 *            密码类型
	 * @param authif
	 *            加密因子
	 * @param passwd
	 *            密码
	 */
	public static void chkPasswd(E_YES___ ispass, String passtp, String authif,
			String passwd) {

		if (CommUtil.isNull(ispass)) {
			throw DpModuleError.DpstAcct.BNAS1892();
		}

		// 检查验密
		if (E_YES___.YES == ispass) {
			throw DpModuleError.DpstAcct.BNAS1893();

		} else {
			// 密码类型
			if (CommUtil.isNotNull(passtp)) {
				throw DpModuleError.DpstAcct.BNAS1894();
			}
			// 加密因子
			if (CommUtil.isNotNull(authif)) {
				throw DpModuleError.DpstAcct.BNAS1895();
			}
			// 密码
			if (CommUtil.isNotNull(passwd)) {
				throw DpModuleError.DpstAcct.BNAS1896();
			}
		}
	}

	/**
	 * 不扣减电子账户额度的校验
	 * 
	 * @param isckqt
	 *            额度验证扣减标志
	 * @param custtp
	 *            客户类型
	 * @param limttp
	 *            额度类型
	 * @param risklv
	 *            风险承受等级
	 * @param authtp
	 *            认证方式
	 * @param sbactp
	 *            子账户类型
	 * @param pytltp
	 *            支付工具
	 * @param rebktp
	 *            收款行范围
	 * @param custlv
	 *            客户等级
	 * @param aclmfg
	 *            累计限额标志
	 * @param custid
	 *            客户id
	 */
	public static void chkqtn(E_YES___ isckqt, E_ACCTROUTTYPE custtp,
			E_LIMTTP limttp, E_RISKLV risklv, String authtp, E_SBACTP sbactp,
			E_PYTLTP pytltp, E_REBKTP rebktp, E_CUSTLV custlv, E_ACLMFG aclmfg,
			String custid) {

		// 非空项检查
		if (CommUtil.isNull(isckqt)) {
			throw DpModuleError.DpstAcct.BNAS1897();
		}

		// 不扣减额度时，其他项应该为空
		if (E_YES___.NO == isckqt) {

			if (CommUtil.isNotNull(custtp) || CommUtil.isNotNull(limttp)
					|| CommUtil.isNotNull(risklv) || CommUtil.isNotNull(authtp)
					|| CommUtil.isNotNull(sbactp) || CommUtil.isNotNull(pytltp)
					|| CommUtil.isNotNull(rebktp) || CommUtil.isNotNull(custlv)
					|| CommUtil.isNotNull(aclmfg) || CommUtil.isNotNull(custid)) {

				throw DpModuleError.DpstAcct.BNAS1898();
			}

		}

}

	/**
	 * 涉案账号交易信息登记
	 * @Title: prcyInacRegister 
	 * @Description: 涉案账号交易信息登记 
	 * @param input
	 * @param property
	 * @param output
	 * @author liaojincai
	 * @date 2016年8月2日 上午9:19:26 
	 * @version V2.3.0
	 */
	public static void prcyInacRegister( final cn.sunline.ltts.busi.dptran.trans.client.intf.Acprst.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Acprst.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Acprst.Output output){

		E_INSPFG invofg = property.getInvofg();// 转出账号是否涉案
		E_INSPFG invofg1 = property.getInvofg1();// 转入账号是否涉案

		// 涉案账户交易信息登记
		if (E_INSPFG.INVO == invofg || E_INSPFG.INVO == invofg1) {

			// 独立事务
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				@Override
				public Void execute() {

					// 获取涉案账户交易信息登记输入信息
					IoCaKnbTrinInput cplKnbTrin = SysUtil.getInstance(IoCaKnbTrinInput.class);
					cplKnbTrin.setTrantp(cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRANTP.TRANSFER);// 交易类型
					cplKnbTrin.setOtcard(input.getCardno());// 转出账号
					cplKnbTrin.setOtacna(input.getAcctnm());// 转出账号名称
					cplKnbTrin.setOtbrch(CommTools.getBaseRunEnvs().getTrxn_branch());// 转出机构
					cplKnbTrin.setIncard(input.getToacct());// 转入账号
					cplKnbTrin.setInacna(input.getToacnm());// 转入账户名称
					cplKnbTrin.setInbrch(input.getTobrch());// 转入账户机构
					cplKnbTrin.setTranam(input.getTranam());// 交易金额
					cplKnbTrin.setCrcycd(input.getCrcycd());// 币种
					cplKnbTrin.setIssucc(E_YES___.NO);// 交易是否成功

					// 涉案账户交易信息登记
					SysUtil.getInstance(ServEacctSvcType.class).ioCaKnbTrinRegister(cplKnbTrin);

					return null;
				}
			});
			
			// 转出账号涉案
			if (E_INSPFG.INVO == invofg) {
				throw DpModuleError.DpstAcct.BNAS0770();
			}
			
			// 转入账号涉案
			if (E_INSPFG.INVO == invofg1) {
				throw DpModuleError.DpstAcct.BNAS0770();
			}
			

		}

	}
}
