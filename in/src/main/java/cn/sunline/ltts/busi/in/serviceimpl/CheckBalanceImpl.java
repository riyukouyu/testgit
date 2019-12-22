package cn.sunline.ltts.busi.in.serviceimpl;

import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.in.inner.InnerAcctQry;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.type.InQueryTypes.BalanceOfCmda;
import cn.sunline.ltts.busi.in.type.InQueryTypes.InacProInfo;
import cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoInWriteOffComplex.IoCheckBlanaceStrike;
import cn.sunline.ltts.busi.iobus.type.in.IoInOpenCloseComplex.IoInacOpen_IN;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * 交易平衡性检查服务实现 交易平衡性检查服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
public class CheckBalanceImpl implements
		cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance {
	/**
	 * 检验平衡性
	 * 
	 */
    static final BizLog bizlog = BizLogUtil.getBizLog(CheckBalanceImpl.class);
	public void checkBalance(String trandt, String transq, E_CLACTP clactp) {
		// 平衡性检查
		// KnpParameter para =
		// SysUtil.getInstance(KnpParameter.class);
		// para = KnpParameterDao.selectOne_odb1("GlParm.switch",
		// "verctl", "%", "%", true);

		// 平衡检查
		List<BalanceOfCmda> cmdas = null;
		try {
			cmdas = InacSqlsDao
					.CheckBalanceOfGlvcByTransq(trandt, transq, false);
		} catch (Exception e) {
			throw InError.comm.E0003("交易平衡性数据查询失败，其他错误");
		}
		bizlog.debug("不平流水=[%s]", cmdas);
		if (CommUtil.isNotNull(cmdas)) {
			
			throw InError.comm.E0003("交易平衡性检查失败！");
			
		}

	}

	/**
	 * 根据业务代码、机构号、币种查询返回内部户
	 * 
	 * @param busino
	 * @param acctbr
	 * @param crcycd
	 * @return acctno
	 */
	public static GlKnaAcct getAcctno(String busino, String acctbr,
			String crcycd, String subsac) {
		InacProInfo info = SysUtil.getInstance(InacProInfo.class);
		info = InnerAcctQry.qryAcctPro(crcycd, acctbr, busino, null, null,
				subsac);
		// 账户不存，则新开账户在
		if (info.getIsexis() == E_YES___.NO) {
			IoInacOpen_IN inacopIn = SysUtil.getInstance(IoInacOpen_IN.class);

			inacopIn.setAcbrch(acctbr);
			inacopIn.setCrcycd(crcycd);
			inacopIn.setBusino(busino);
			inacopIn.setSubsac(subsac);
			return InnerAcctQry.addInAcct(inacopIn);
		} else {
			GlKnaAcct acct = SysUtil.getInstance(GlKnaAcct.class);
			acct.setAcctno(info.getAcctno());
			acct.setAcctna(info.getAcctna());
			return acct;
		}
	}

	/**
	 * 清算补记账冲正登记簿
	 */
	@Override
	public void strikeCheckBalanceClear(IoCheckBlanaceStrike checkstrike) {
		/*
		 * List<IoAccountClearInfo> clerList =
		 * SysUtil.getInstance(IoAccountSvcType
		 * .class).queryKnsAcsqCler(checkstrike.getTrandt(),
		 * checkstrike.getTransq()); if(null
		 * ==clerList||CommUtil.isNull(clerList.get(0).getMntrsq())){ throw
		 * InError.comm.E0003("清算记录不存在，无法冲正！"); } for(IoAccountClearInfo cler
		 * :clerList ){ //登记清算补记账信息 IoAccountClearInfo clearInfo =
		 * SysUtil.getInstance(IoAccountClearInfo.class);
		 * clearInfo.setCorpno(cler.getCorpno());//账务机构法人行
		 * clearInfo.setAcctno(cler.getAcctno());//账号
		 * clearInfo.setAcctna(cler.getAcctna());//账户名称
		 * clearInfo.setProdcd(cler.getProdcd());//产品编码
		 * clearInfo.setAcctbr(cler.getAcctbr());//账务机构
		 * clearInfo.setToacbr(cler.getToacbr());//对方机构
		 * clearInfo.setCrcycd(cler.getCrcycd());//币种
		 * clearInfo.setAmntcd(cler.getAmntcd());//借贷标志
		 * clearInfo.setTranam(cler.getTranam().negate());//交易金额
		 * clearInfo.setClerst(E_CLERST.WAIT);//清算状态
		 * clearInfo.setClactp(cler.getClactp());//系统内往来账户类型
		 * clearInfo.setToacct(cler.getToacct());//
		 * 
		 * //登记会计流水清算信息
		 * SysUtil.getInstance(IoAccountSvcType.class).registKnsAcsqCler
		 * (clearInfo); }
		 * 
		 * //冲正系统内跨法人清算，查询原记录冲正登记，不存在记录则不处理 //查询原交易系统内跨法人清算信息
		 * Options<IoAccountClearInfo> lstClearInfo =
		 * SysUtil.getInstance(IoAccountSvcType
		 * .class).qryKnsAcsqClin(checkstrike.getTrandt(),
		 * checkstrike.getTransq());
		 * 
		 * if(CommUtil.isNotNull(lstClearInfo) && lstClearInfo.size() > 0){
		 * for(IoAccountClearInfo cplClearInfo :lstClearInfo ){ //登记清算补记账信息
		 * IoAccountClearInfo clearInfo =
		 * SysUtil.getInstance(IoAccountClearInfo.class);
		 * clearInfo.setCorpno(cplClearInfo.getCorpno());//账务机构法人行
		 * clearInfo.setAcctno(cplClearInfo.getAcctno());//账号
		 * clearInfo.setAcctna(cplClearInfo.getAcctna());//账户名称
		 * clearInfo.setProdcd(cplClearInfo.getProdcd());//产品编码
		 * clearInfo.setAcctbr(cplClearInfo.getAcctbr());//账务机构
		 * clearInfo.setToacbr(cplClearInfo.getToacbr());//对方机构
		 * clearInfo.setCrcycd(cplClearInfo.getCrcycd());//币种
		 * clearInfo.setAmntcd(cplClearInfo.getAmntcd());//借贷标志
		 * clearInfo.setTranam(cplClearInfo.getTranam().negate());//交易金额
		 * clearInfo.setClerst(E_CLERST.WAIT);//清算状态
		 * clearInfo.setClactp(cplClearInfo.getClactp());//系统内往来账户类型
		 * clearInfo.setToacct(cplClearInfo.getToacct());//
		 * 
		 * //登记系统内跨法人会计流水清算信息
		 * SysUtil.getInstance(IoAccountSvcType.class).registKnsAcsqClin
		 * (clearInfo); } }
		 */

	}

	@Override
	public IoGlKnaAcct getGlKnaAcct(String busino, String brchno,
			String crcycd, String subsac) {
		GlKnaAcct acct = getAcctno(busino, brchno, crcycd, subsac);
		IoGlKnaAcct ioGlKnaAcct = SysUtil.getInstance(IoGlKnaAcct.class);
		CommUtil.copyProperties(ioGlKnaAcct, acct);

		return ioGlKnaAcct;
	}

	/**
	 * 大小额来往帐、大小额冲正平衡性检查
	 */
	@Override
	public void checkBalanceLsam(String trandt, String transq, E_CLACTP clactp) {

		// 平衡检查
		List<BalanceOfCmda> cmdas = null;
		try {
			cmdas = InacSqlsDao
					.CheckBalanceOfGlvcByTransq(trandt, transq, true);
		} catch (Exception e) {
			throw InError.comm.E0003("交易平衡性数据查询失败，其他错误");
		}
		if (CommUtil.isNotNull(cmdas)) {
			throw InError.comm.E0003("交易平衡性检查失败！");
		}

	}

}
