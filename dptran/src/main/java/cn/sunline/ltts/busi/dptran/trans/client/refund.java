package cn.sunline.ltts.busi.dptran.trans.client;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.client.CapitalTransCheck;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlSpnd;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_ACLMFG;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_CUSTLV;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_PYTLTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_REBKTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RISKLV;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SPETTP;



public class refund {


public static void chkRefundtInfo( final cn.sunline.ltts.busi.dptran.trans.client.intf.Refund.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Refund.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Refund.Output output){
		
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
		if (CommUtil.isNull(input.getRetram())) {
			
			throw DpModuleError.DpstComm.BNAS1928();
		}
		if (CommUtil.compare(input.getRetram(),BigDecimal.ZERO) <= 0) {
			throw DpModuleError.DpstComm.BNAS1929();
		}
		if (CommUtil.isNull(input.getSmrycd())) {
			
			throw DpModuleError.DpstComm.BNAS0195();
		}
		if (CommUtil.isNull(input.getCardno())) {
			
			throw DpModuleError.DpstAcct.BNAS1881();
		}
//		if (CommUtil.isNull(input.getAcsetp())) {
//			
//			throw DpModuleError.DpstAcct.E9999("子账号类型不能为空");
//		}
//		if (CommUtil.isNull(input.getAcesno())) {
//			
//			throw DpModuleError.DpstAcct.E9999("子账号不能为空");
//		}
		if (CommUtil.isNull(input.getAcctnm())) {
			
			throw DpModuleError.DpstComm.BNAS0534();
		}
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
		
		// 获取电子账户信息
		IoCaKnaAcdc cplknaAcdc = DpAcctDao.selKnaAcdcByCardno(input.getCardno(), false);
		if (CommUtil.isNull(cplknaAcdc)) {
			throw DpModuleError.DpstComm.BNAS0327();
		}
		
//		CommTools.getBaseRunEnvs().setBusi_org_id(cplknaAcdc.getCorpno());// 初始化法人代码
		
		property.setCustac(cplknaAcdc.getCustac());// 电子账号
		KnaAcct tblKnaAcct = SysUtil.getInstance(KnaAcct.class);
		E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(cplknaAcdc.getCustac());
		
		if(CommUtil.isNotNull(input.getAcesno())){ //子账号不为空
			tblKnaAcct = KnaAcctDao.selectOne_odb1(input.getAcesno(), false);
		}else{
			tblKnaAcct = CapitalTransDeal.getSettKnaAcctAc(cplknaAcdc.getCustac());
		}
		
		if(CommUtil.isNull(tblKnaAcct)){
			throw DpModuleError.DpstAcct.BNAS1883();
		}
		//状态
		if(tblKnaAcct.getAcctst() != E_DPACST.NORMAL){
			throw DpModuleError.DpstAcct.BNAS1884();
		}
		// 账号匹配检查
		if (!CommUtil.equals(cplknaAcdc.getCustac(), tblKnaAcct.getCustac())) {
			throw DpModuleError.DpstAcct.BNAS1885();
		}
		
		// 币种
		if (!CommUtil.equals(input.getCrcycd(), tblKnaAcct.getCrcycd())) {
			throw DpModuleError.DpstAcct.BNAS1886();
		}
		// 客户名称
		if (!CommUtil.equals(tblKnaAcct.getAcctna(), input.getAcctnm())) {
			throw DpModuleError.DpstAcct.BNAS1887();
		}
		
		if(CommUtil.isNotNull(input.getAcsetp()) && tblKnaAcct.getAcsetp() != input.getAcsetp()){
			throw DpModuleError.DpstComm.BNAS1930();
		}
		
		if (tblKnaAcct.getAcsetp() == E_ACSETP.FW && E_YES___.YES == isckqt) {
			throw DpModuleError.DpstAcct.BNAS1889();
		}
		
		property.setAcctno(input.getAcesno());
		
		 // add liaojc 亲情钱包支付后退款时，从亲情钱包支付的相应款项退回亲情钱包子账户，如果该亲情钱包已关闭的，退款直接转入创建人的个人结算主账户
    	if (E_ACSETP.FW == input.getAcsetp()) {
    		if (tblKnaAcct.getAcctst() == E_DPACST.CLOSE) {// 销户
    			// 负债账号更新为活期结算户
    			property.setAcctno(CapitalTransDeal.getSettKnaAcctAc(cplknaAcdc.getCustac()).getAcctno());
    			
    			// 亲情钱包已经关闭
    			property.setIsclos(E_YES___.YES);
    			
    		}
    	}
        
		// 检查验密信息
		chkPasswd(ispass, passtp, authif, passwd);

		// 检查电子账户额度扣减控制
		chkqtn(isckqt, custtp, limttp, risklv, authtp, sbactp, pytltp, rebktp, custlv, aclmfg, custid);
		
		// 获取子账户序号
		String acseno = DpAcctQryDao.selKnaAccsByAcctno(input.getAcesno(), true).getSubsac();
		property.setAcseno(acseno);
				
		//1.转出方电子账户状态字校验
		CapitalTransCheck.ChkAcctstRe(cplknaAcdc.getCustac()); 
				
		// 获取报文头信息
		property.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());// 机构号
		property.setServtp(CommTools.getBaseRunEnvs().getChannel_id());// 渠道
//		property.setServtp(String.DP);// TODO 报文头交易渠道有问题，取出为交易码
		property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date());// 渠道交易日期
		property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 渠道流水
		property.setAcsetp2(tblKnaAcct.getAcsetp());
		// 获取业务编号
		KnpParameter para = SysUtil
				.getInstance(KnpParameter.class);
		para = KnpParameterDao.selectOne_odb1("InParm.clearbusi",
				"in", E_CLACTP._01.getValue(), "%", true);
		
		property.setBusino(para.getParm_value1()); //业务编码
		property.setSubsac(para.getParm_value2());//子户号
		property.setSpettp(E_SPETTP.REFUND);// 电子账户退货
	}

public static void chkTransAfter( final cn.sunline.ltts.busi.dptran.trans.client.intf.Refund.Input input,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Refund.Property property,  final cn.sunline.ltts.busi.dptran.trans.client.intf.Refund.Output output){

		// 平衡性检查
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();

		IoCheckBalance ioCheckBalance = SysUtil.getInstance(IoCheckBalance.class);
		ioCheckBalance.checkBalance(trandt, transq,E_CLACTP._01);
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
	public static void chkPasswd(E_YES___ ispass, String passtp, String authif, String passwd) {

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
}
