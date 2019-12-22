package cn.sunline.ltts.busi.ca.card.process;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.sys.type.MsType;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.ca.serviceimpl.IoCaSrvGenEAccountInfoImpl;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.dp.password.DpPassword;
import cn.sunline.ltts.busi.dp.tables.TabDpPassword.DpbPswdDao;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEAccountIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAddEAccountOut;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.errors.CaError.Eacct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;

/**
 * <p>
 * 文件功能说明：卡的相关处理
 * </p>
 * 
 * @author renjinghua
 *         <p>
 *         <li>2015年3月26日</li>
 *         </p>
 *
 */
public class CaCardProc {

	/**
	 * <p>
	 * 获得卡信息包括卡号，并做插入操作
	 * </p>
	 * 
	 * @author renjinghua
	 *         <p>
	 *         <li>2015年3月26日</li>
	 *         </p>
	 * 
	 * @param cplCaAddCardIn
	 *            开户电子账户输入接口
	 */
	private static final BizLog bizlog = BizLogUtil.getBizLog(IoCaSrvGenEAccountInfoImpl.class);

	public static String prcCardInfo(IoCaAddEAccountIn cplAddEAccountIn, IoCaAddEAccountOut cplAddEAccount) {

		// //获取卡bin
		// KnpParameter kabin = KnpParameterDao.selectOne_odb1("KcdProd.cardbn",
		// "kabin", "%", "%", true);
		// //update by xieqq 20170724
		// 修改生成卡号规则：电子账户卡BIN为“621737”，可以设置总长度为19为的标准银联卡卡号，其中第7至8位取值为“98”；9至11位地区代码保持金谷农商银行“001”不变；12至18位的顺序编码金谷农商银行可自行分配。
		// StringBuffer midstr = new StringBuffer();
		// String cardPart =
		// midstr.append(kabin.getParm_value2()).append(kabin.getParm_value3()).append(BusiTools.getSequence("Cardno12_18",
		// 6)).toString();
		//
		// String cardno = CaTools.genCardno(kabin.getParm_value1(),
		// cardPart, cplAddEAccountIn.getOpacwy(),
		// cplAddEAccountIn.getIdtfno(), cplAddEAccountIn.getIdtftp());

		/**
		 * add by xj 20180518 柳州银行II类户卡号 621412(卡bin)+0132(产品号)+7位序号+1位校验位
		 */
		KnpParameter cardPara = SysUtil.getInstance(KnpParameter.class);
		String sortcd = null;
		if (BaseEnumType.E_ACCATP.FINANCE == cplAddEAccountIn.getAccttp()) {
			cardPara = KnpParameterDao.selectOne_odb1("KcdProd.cardbn", "kabin", "lzbank", "2", true);
			sortcd = "Cardno12_17_2";
		} else if (BaseEnumType.E_ACCATP.WALLET == cplAddEAccountIn.getAccttp()) {
			cardPara = KnpParameterDao.selectOne_odb1("KcdProd.cardbn", "kabin", "lzbank", "3", true);
			sortcd = "Cardno12_17_3";
		} else {
			throw CaError.Eacct.E0901("账户类型非II/III类户");
		}
		if (CommUtil.isNull(cardPara)) {
			throw CaError.Eacct.E0901("开户卡bin未配置");
		}
		String kabin = cardPara.getParm_value1();
		if (CommUtil.isNull(kabin))
			throw Eacct.BNAS1427();
		StringBuffer cardBuff = new StringBuffer();

		String keyno = CoreUtil.nextValue(sortcd);
		// bizlog.debug("===== CoreUtil.nextValue(key)====" + keyno + "=============");
		cardBuff = cardBuff.append(kabin).append(cardPara.getParm_value2()).append(BusiTools.getSequence(sortcd, 7));
		String chkNo = CaTools.countParityBit(cardBuff);
		String cardno = cardBuff.append(chkNo).toString();
		/** end */

		// add by sh 20170714 废弃原来kcdcard表，用新表dpb_pswd
		// 取消登记密码表逻辑 modified by sunzy 20191114
		// DpPassword.savePassword(cardno, E_ACCTROUTTYPE.CARD,
		// cplAddEAccountIn.getTranpw(), "ALL", "%", cplAddEAccountIn.getAuthif());

		return cardno;
	}

	/**
	 * 建立电子账号与卡关联关系
	 * 
	 * @param custac
	 *            电子账号
	 * @param cardno
	 *            卡号
	 */
	public static void prcEacctCardLink(String custac, String cardno) {
		// 检查输入电子账号与卡号信息
		if (CommUtil.isNull(custac)) {
			throw CaError.Eacct.BNAS0636();
		}

		if (CommUtil.isNull(cardno)) {
			throw CaError.Eacct.BNAS0635();
		}

		if (CommUtil.isNull(KnaCustDao.selectOne_odb1(custac, false))) {
			throw CaError.Eacct.BNAS0952(custac);
		}
		// 注销开户登记密码表
		// if (CommUtil.isNull(DpbPswdDao.selectOne_odb1(cardno, "%", "ALL", false))) {
		// throw CaError.Eacct.BNAS0571(cardno);
		// }

		// 检查电子账号与卡号关联关系是否建立
		KnaAcdc tblKna_acdc = KnaAcdcDao.selectOne_odb2(cardno, false);
		if (CommUtil.isNull(tblKna_acdc)) {
			tblKna_acdc = SysUtil.getInstance(KnaAcdc.class);
			tblKna_acdc.setCardno(cardno); // 客户账号
			tblKna_acdc.setCustac(custac); // 电子账号
			tblKna_acdc.setStatus(E_DPACST.NORMAL); // 状态
			KnaAcdcDao.insert(tblKna_acdc);
		}
	}

}
