
package cn.sunline.edsp.busi.dptran.batchtran.ca;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.ListBatchDataWalker;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.namedsql.ca.DmTmpDao;
import cn.sunline.edsp.busi.dp.type.ca.DmEnumType.E_EXPSTS;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.ca.base.DecryptConstant;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcalDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccsDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaMaadDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpac;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpacDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.dp_icore_open_account;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddt;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctAddtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctMatu;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctMatuDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDraw;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaDrawDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSave;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaSaveDao;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.errors.CaError.Eacct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINRD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DRAWCT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRFLPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRRTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PSDWST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SAVECT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SPECTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_USCHNL;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_USTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_IDCKRT;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_MPCKRT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IBAMMD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEBS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_REPRWY;

/**
 * 电子账户开户
 * 
 * @author
 * @Date
 */

public class btopenDataProcessor extends
		// AbstractBatchDataProcessor<cn.sunline.edsp.busi.icore.dm.batchtran.intf.Opaccd.Input,
		// cn.sunline.edsp.busi.icore.dm.batchtran.intf.Opaccd.Property,
		// cn.sunline.edsp.busi.icore.dm.tables.DmDsTmp.dm_icore_open_account> {
		AbstractBatchDataProcessorWithJobDataItem<cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Btopen.Input, cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Btopen.Property, String, cn.sunline.ltts.busi.ca.tables.ElectronicAccount.dp_icore_open_account> {
	private static final BizLog LOGGER = BizLogUtil.getBizLog(btopenDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param job
	 *            批次作业ID
	 * @param index
	 *            批次作业第几笔数据(从1开始)
	 * @param dataItem
	 *            批次数据项
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(String jobId, int index,
			cn.sunline.ltts.busi.ca.tables.ElectronicAccount.dp_icore_open_account dataItem,
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Btopen.Input input,
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Btopen.Property property) {
		try {
			// 初始化流水
			CommTools.getBaseRunEnvs().setMain_trxn_seq(CoreUtil.nextValue("opaccdBatch"));

			// CommTools.createNewTrxnSeq();

			String trandt = CommTools.getBaseRunEnvs().getTrxn_date();

			String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();

			String nextTranDt = CommTools.getBaseRunEnvs().getNext_date();

			// TODO:
			CommTools.getBaseRunEnvs().setTrxn_branch("70100");
			// 获取唯一索引【自增列】查询出唯一数据
			Integer exportId = dataItem.getExport_id();
			if (CommUtil.isNull(exportId)) {
				throw CaError.Eacct.E0001("[自增列][EXPORT_ID]不能为空！");
			}

			E_CUSACT cacttp = dataItem.getCacttp();// 客户账号类型 eg:E_CUSACT.ACC
			E_ACCATP accatp = dataItem.getAccatp();// 账户分类 eg:E_ACCATP.FINANCE
			String custno = dataItem.getCustno();// 用户ID eg:"XXX"
			String custna = dataItem.getCustna();// 客户姓名 eg:"XXX"
			String tmcustna = DecryptConstant.maskName(custna);// 脱敏客户姓名 eg:"XXX"
			String decustna = DecryptConstant.decrypt(custna);// 解密姓名
			E_IDTFTP idtftp = dataItem.getIdtftp();// 证件类型 eg:E_IDTFTP.SFZ
			String idtfno = dataItem.getIdtfno();// 证件号码 eg:"350101199609096512"
			String tmidtfno = DecryptConstant.maskIdCard(idtfno);// 证件号码 eg:"350101199609096512"
			String tlphno = dataItem.getTlphno();// 手机号码 eg"17801010202"
			String tmtlphno = DecryptConstant.maskMobile(tlphno);// 手机号码 eg"17801010202"
			String brchno = dataItem.getBrchno();// 账户归属机构 eg:"70100"
			E_IDCKRT idckrt = dataItem.getIdckrt();// 身份核查结果 eg:E_IDCKRT.SUCCESS
			E_MPCKRT mpckrt = dataItem.getMpckrt();// 人脸/持证照识别结果 eg:E_MPCKRT.SUCCESS
			String crcycd = dataItem.getCrcycd();// 币种 "CNY"
			E_YES___ ispswd = dataItem.getIspswd();// 是否有交易密码 eg:E_YES___.NO
			/* ==== 无交易密码时 下面三项可放空 ==== */
			String passwd = dataItem.getPasswd();// 账户密码 eg:"XXX"
			E_PSDWST acpwst = dataItem.getAcpwst();// 账户密码状态 eg:E_PSDWST.NORMAL
			String authif = dataItem.getAuthif();// 加密因子 eg:"XXX"

			E_USCHNL uschnl = dataItem.getUschnl();// 即富开户渠道 eg:E_JFCHID.UBACK
			String mactid = dataItem.getMactid();// 电子账户开户标识 eg:"111"
			E_USTYPE ustype = dataItem.getUsertp();// 客户标识 eg:E_JFCUTP.JFMECHANT
			/* ==== 下面五项可空 ==== */

			// 品牌信息列表
			String sbrand = dataItem.getSbrand();// 品牌ID eg:"111"
			String bradna = dataItem.getBradna();// 品牌名称 eg:"XXX"
			if (StringUtils.isBlank(bradna)) {
				bradna = sbrand;
			}
			String acctid = dataItem.getAcctid();// 子账户开户标识 eg"111"

			KnaMaad tbKnaMaad = KnaMaadDao.selectOne_odb2(mactid, false);
			// 开户主体ID存在，不新增电子账户。
			KnaCust knaCust = null;
			if (CommUtil.isNotNull(tbKnaMaad)) {
				knaCust = KnaCustDao.selectOne_odb1(tbKnaMaad.getCustac(), false);
			} else {

				// 插入客户信息表
				knaCust = SysUtil.getInstance(KnaCust.class);

				// 生成电子帐号
				String custac = BusiTools.genCustac();
				String cardno = prcCardInfo(E_ACCATP.FINANCE);

				knaCust.setAcctst(E_ACCTST.NORMAL);
				knaCust.setCacttp(E_CUSACT.ACC);
				knaCust.setBrchno("70100");
				knaCust.setMpckrt(E_MPCKRT.SUCCESS);
				knaCust.setIdckrt(E_IDCKRT.SUCCESS);
				knaCust.setAccttp(E_ACCATP.FINANCE);
				knaCust.setUschnl(E_USCHNL.PMOUT);
				knaCust.setOpendt(trandt);
				knaCust.setOpensq(transq);
				knaCust.setCustno(dataItem.getCustno());
				knaCust.setCustna(custna);
				knaCust.setTmcustna(tmcustna);
				knaCust.setCustac(custac);

				KnaCustDao.insert(knaCust);

				KnaAcal knaAcal = SysUtil.getInstance(KnaAcal.class);

				knaAcal.setAcalst(E_ACALST.NORMAL);
				knaAcal.setCustac(custac);
				knaAcal.setTlphno(tlphno);
				knaAcal.setTmtlphno(tmtlphno);
				knaAcal.setAcaltp(E_ACALTP.CELLPHONE);

				KnaAcalDao.insert(knaAcal);

				KnbOpac tblKnbOpac = SysUtil.getInstance(KnbOpac.class);

				tblKnbOpac.setCustac(custac); // 电子账号
				tblKnbOpac.setCustna(custna); // 账户名称
				tblKnbOpac.setTmcustna(tmcustna); // 账户名称
				tblKnbOpac.setAccttp(knaCust.getAccttp()); // 账户分类
				tblKnbOpac.setTlphno(tlphno); // 手机号
				tblKnbOpac.setTmtlphno(tmtlphno); // 手机号
				tblKnbOpac.setBrchno("70100"); // 所属机构
				tblKnbOpac.setCrcycd("CNY"); // 币种
				tblKnbOpac.setOpentm(BusiTools.getBusiRunEnvs().getTrantm()); // 开户时间
				// 钞汇标志

				tblKnbOpac.setCsextg(E_CSEXTG.CASH);
				// 开户日期
				tblKnbOpac.setOpendt(trandt);
				// 开户流水
				tblKnbOpac.setOpensq(transq);
				// 开户渠道
				tblKnbOpac.setUschnl(E_USCHNL.PMOUT);
				tblKnbOpac.setServsq(transq);
				KnbOpacDao.insert(tblKnbOpac);

				KnaMaad tblKnaMaad = SysUtil.getInstance(KnaMaad.class);
				tblKnaMaad.setCustac(custac);
				tblKnaMaad.setCardno(cardno);
				tblKnaMaad.setCustno(custno);
				tblKnaMaad.setCustna(custna);
				tblKnaMaad.setTmcustna(tmcustna);
				tblKnaMaad.setIdtfno(idtfno);
				tblKnaMaad.setTmidtfno(tmidtfno);
				tblKnaMaad.setIdtftp(idtftp);
				tblKnaMaad.setUschnl(uschnl);
				tblKnaMaad.setUsertp(ustype);
				tblKnaMaad.setMactid(mactid);

				KnaMaadDao.insert(tblKnaMaad);

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

			KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(acctid, false);

			if (CommUtil.isNull(tblKnaSbad)) {
				KnaAcct acct = SysUtil.getInstance(KnaAcct.class);

				String acctno = BusiTools.getAcctno();
				acct.setAcctno(acctno);

				acct.setAcsetp(E_ACSETP.SA);
				acct.setAcctna(decustna);
				acct.setAcctcd("9922021100000001");
				acct.setAcctst(E_DPACST.NORMAL);
				acct.setAccttp(E_YES___.YES); // 是否结算户

				acct.setBgindt(CommTools.getBaseRunEnvs().getTrxn_date()); // 起息日期
				acct.setMatudt(null);
				acct.setDeptdy(0L);
				acct.setBkmony(BigDecimal.ZERO); // 备用金额
				acct.setBrchno("70100"); // 机构
				acct.setCrcycd("CNY"); // 币种
				acct.setCsextg(E_CSEXTG.CASH); // 钞汇标志
				acct.setCustac(knaCust.getCustac()); // 电子账户
				acct.setCustno(custno); // 客户号
				acct.setDebttp(E_DEBTTP.DP2401); // 储蓄种类 业务细类
				acct.setHdmimy(BigDecimal.ZERO); // 账户最小留存金额
				acct.setOnlnbl(BigDecimal.ZERO); // 账户余额 调用存入服务时写入
				acct.setOpendt(trandt); // 开户日期
				acct.setOpensq(transq); // 开户流水
				acct.setOpmony(BigDecimal.ZERO); // 开户金额
				acct.setPddpfg(E_FCFLAG.CURRENT); // 定活标志
				acct.setProdcd("JFSD70120180101408"); // 产品编号
				acct.setSleptg(E_YES___.NO); // 形态转移标志
				acct.setSpectp(E_SPECTP.PERSON_BASE); // 账户性质
				acct.setDepttm(E_TERMCD.T000); // 存期
				acct.setIsdrft(E_YES___.YES); // 是否允许透支
				acct.setLstrdt(trandt);
				acct.setLastbl(BigDecimal.ZERO);//新开户上日账户余额为0


				KnaAcctDao.insert(acct);

				KnaAcctAddt addt = SysUtil.getInstance(KnaAcctAddt.class);
				addt.setAccatp(E_ACCATP.FINANCE);
				addt.setAcctno(acctno);
				addt.setHigham(new BigDecimal(0));

				KnaAcctAddtDao.insert(addt);

				KnaSave save = SysUtil.getInstance(KnaSave.class);

				save.setAcctno(acctno);
				save.setPosttp(E_SAVECT.YES); // 存入控制方式
				save.setSpbkfg(E_YES___.NO);
				save.setResvam(BigDecimal.ZERO);

				KnaSaveDao.insert(save);

				KnaDraw KnaDraw = SysUtil.getInstance(KnaDraw.class);
				KnaDraw.setAcctno(acctno);
				KnaDraw.setDpbkfg(E_YES___.NO);
				KnaDraw.setDrawtp(E_DRAWCT.YES);
				KnaDraw.setRedwnm(0L);
				KnaDraw.setIsmamt(E_YES___.NO);
				KnaDraw.setRedqam(BigDecimal.ZERO);

				KnaDrawDao.insert(KnaDraw);

				KnaAcctMatu matu = SysUtil.getInstance(KnaAcctMatu.class);

				matu.setAcctno(acctno); // 负债账号
				matu.setCrcycd("CNY");
				matu.setUndump(0);

				KnaAcctMatuDao.insert(matu);

				KnaAcctProd tblKnaAcctProd = SysUtil.getInstance(KnaAcctProd.class);
				tblKnaAcctProd.setAcctno(acctno);// 负债账号
				KnaAcctProdDao.insert(tblKnaAcctProd);

				KubInrt inrt = SysUtil.getInstance(KubInrt.class);

				inrt.setAcctno(acctno); // 账号
				inrt.setIntrtp(E_INTRTP.ZHENGGLX); // 利息类型
				inrt.setIndxno(1L); // 顺序号
				inrt.setIntrcd("JFSD70101"); // 利率编码
				inrt.setIncdtp(E_IRCDTP.Reference); // 利率代码类型
				inrt.setLvindt(E_TERMCD.T000); // 层次存期
				inrt.setOpintr(BigDecimal.ZERO); // 开户利率
				inrt.setBsintr(BigDecimal.ZERO); // 基准利率
				inrt.setIrflby(E_IRFLPF.RATE); // 浮动方式
				// 执行利率
				inrt.setCuusin(BigDecimal.ZERO);
				// inrt.setCuusin(layer.getIntrvl()); //当前执行利率
				// inrt.setRealin(BigDecimal.ZERO);
				inrt.setIsfavo(E_YES___.YES); // 是否优惠
				// inrt.setPfirwy(E_IRFLPF.); //优惠浮动方式
				inrt.setFavovl(BigDecimal.ZERO); // 优惠浮动值
				inrt.setFavort(BigDecimal.ZERO); // 利率浮动百分比
				inrt.setLacain(BigDecimal.ZERO);
				inrt.setLastbl(BigDecimal.ZERO); // 上日余额
				inrt.setLastdt(trandt); // 上日余额更新日期
				inrt.setClvsmt(BigDecimal.ZERO); // 当前层次积数
				inrt.setClvamt(BigDecimal.ZERO); // 当前层级计息金额
				inrt.setClvudt(""); // 当前层次计息金额更新日期

				KubInrtDao.insert(inrt);

				KnbAcin acin = SysUtil.getInstance(KnbAcin.class);
				acin.setAcctno(acctno);// 账号
				acin.setIntrtp(E_INTRTP.ZHENGGLX); // 利率类型
				acin.setCrcycd("CNY"); // 币种
				acin.setInbefg(E_INBEFG.NOINBE); // 计息标志
				acin.setTxbefg(E_YES___.NO);// 计税标志
				acin.setTxbebs(E_INBEBS.REALSTAD);// 计息基础
				acin.setHutxfg(E_CAINRD.QE);// 舍弃角分计息标志
				acin.setInammd(E_IBAMMD.ACCT); // 计息金额模式
				acin.setReprwy(E_REPRWY.ALL);// 重订价利息处理方式
				acin.setIntrcd("JFSD70101"); // 利率编号

				acin.setInprwy(E_IRRTTP.NO); // 利率重定价方式
				// 计算出下次优惠更新日期
				acin.setNxindt(trandt); // 下次计息日
				acin.setIntrdt(E_INTRDT.OPEN); // 利率确定日方式

				acin.setOpendt(trandt);// 开户日期
				acin.setBgindt(trandt);// 起息日期
				acin.setPlanin(BigDecimal.ZERO);// 计提利息
				acin.setLastdt(trandt); // 最近更新日期
				acin.setPlblam(BigDecimal.ZERO); // 计息累计余额(积数)
				acin.setNxdtin(BigDecimal.ZERO); // 上计提日利率
				acin.setMustin(BigDecimal.ZERO);// 应缴税金
				acin.setLsinop(E_INDLTP.CAIN); // 上次利息操作
				acin.setIndtds(0); // 计息天数
				acin.setEvrgbl(BigDecimal.ZERO); // 平均余额
				acin.setCutmin(BigDecimal.ZERO); // 本期利息
				acin.setCutmis(BigDecimal.ZERO); // 本期利息税
				acin.setCutmam(BigDecimal.ZERO);// 本期积数
				acin.setAmamfy(BigDecimal.ZERO); // 本年累计积数
				acin.setLyamam(BigDecimal.ZERO); // 上年累计积数
				acin.setDiffin(BigDecimal.ZERO); // 应加/减利息
				acin.setDiffct(BigDecimal.ZERO); // 应加/减积数
				acin.setLsinsq(String.valueOf(0));// 上次利息序号

				acin.setProdcd("JFSD70120180101408"); // 产品号
				acin.setDetlfg(E_YES___.NO); // 明细汇总
				acin.setPddpfg(E_FCFLAG.CURRENT); // 定活标志
				acin.setLaamdt(trandt); // 积数更新日期

				acin.setIncdtp(E_IRCDTP.Reference); // 利率代码类型
				KnbAcinDao.insert(acin);

				KnaSbad subaddInfo = SysUtil.getInstance(KnaSbad.class);
				/*
				 * 登记子账户附加信息。
				 */
				subaddInfo.setAcctid(acctid);
				subaddInfo.setAcctno(acctno);
				subaddInfo.setCustac(knaCust.getCustac());
				subaddInfo.setMactid(mactid);
				subaddInfo.setSbrand(sbrand);
				subaddInfo.setBradna(bradna);
				subaddInfo.setAcctna(decustna);
				// subaddInfo.setTmacctna(DecryptConstant.maskName(acctna));
				KnaSbadDao.insert(subaddInfo);

				KnaAccs tblKna_accs = KnaAccsDao.selectOne_odb2(acctno, false);

				if (CommUtil.isNull(tblKna_accs)) {

					String subsac = BusiTools.genSubEAccountno();

					KnaAccs tblKna_accs_one = SysUtil.getInstance(KnaAccs.class);
					tblKna_accs_one.setCustac(knaCust.getCustac()); // 电子账号
					tblKna_accs_one.setSubsac(subsac); // 子户号
					tblKna_accs_one.setAcctno(acctno); // 负债账号
					tblKna_accs_one.setFcflag(E_FCFLAG.CURRENT); // 定活标志
					tblKna_accs_one.setProdtp(E_PRODTP.DEPO); // 产品类型
					tblKna_accs_one.setCsextg(E_CSEXTG.CASH); // 钞汇标志
					tblKna_accs_one.setProdcd("JFSD70120180101408");// 产品号
					tblKna_accs_one.setCrcycd(crcycd);// 币种
					tblKna_accs_one.setAcctst(E_DPACST.NORMAL); // 状态

					// 插入操作
					KnaAccsDao.insert(tblKna_accs_one);
				}
			}

			// String acctno = "";// 子账号
			// // 绑卡信息列表
			// /*
			// * String cdopac = "222";// 银行卡号 String cdopna = "222";// 持卡人姓名 String brchna
			// =
			// * "222";// 银行卡开户行名称 String opbrch = "222";// 银行卡开户行号 String cardtp = "222";//
			// * 绑定账户类型 E_YES___ bdcart = null;// 绑卡认证结果 String bdcatp = "222";// 绑定账户分类
			// * String custty = "222";// 对公对私标志 String custna = "222";// 证件姓名 String idtfno
			// =
			// * "222";// 证件号码 String idtftp = "222";// 证件类型 String teleno = "222";// 手机号码
			// * String isdflt = "222";// 是否默认卡 String acbdtp = "222";// 绑定账户标识
			// */
			//
			// InputSetter dpInputSetter = SysUtil.getInstance(InputSetter.class);
			// Property dpProperty = SysUtil.getInstance(Property.class);
			// Output dpOutput = SysUtil.getInstance(Output.class);
			// // 赋值
			// dpInputSetter.setCacttp(cacttp);
			// dpInputSetter.setAccatp(accatp);
			// dpInputSetter.setCustno(custno);
			// dpInputSetter.setCustna(custna);
			// dpInputSetter.setIdtftp(idtftp);
			// dpInputSetter.setIdtfno(idtfno);
			// dpInputSetter.setTlphno(tlphno);
			// dpInputSetter.setBrchno(brchno);
			// dpInputSetter.setIdckrt(idckrt);
			// dpInputSetter.setMpckrt(mpckrt);
			// dpInputSetter.setCrcycd(crcycd);
			// dpInputSetter.setIspswd(ispswd);
			// /* ==== 无交易密码时 下面三项可放空 ==== */
			// dpInputSetter.setPasswd(passwd);
			// dpInputSetter.setAcpwst(acpwst);
			// dpInputSetter.setAuthif(authif);
			//
			// dpInputSetter.setUschnl(uschnl);
			// dpInputSetter.setMactid(mactid);
			// dpInputSetter.setUsertp(ustype);
			// /* ==== 下面五项可空 ==== */
			// dpInputSetter.setFacesg(facesg);
			// // dpInputSetter.setCustcd(custcd);
			// dpInputSetter.setCkrtsq(ckrtsq);
			// dpInputSetter.setAgrtno(agrtno);
			// dpInputSetter.setVesion(vesion);
			//
			// Options<BrandsInfo> lsBrands = SysUtil.getInstance(Options.class);
			// BrandsInfo brandsInfo = SysUtil.getInstance(BrandsInfo.class);
			// brandsInfo.setSbrand(sbrand);
			// brandsInfo.setBradna(bradna);
			// brandsInfo.setAcctid(acctid);
			// brandsInfo.setAcctno(acctno);
			// lsBrands.add(brandsInfo);
			// dpInputSetter.setBrandList(lsBrands);
			//
			// dpProperty.setDepttm(E_TERMCD.T000);
			// dpProperty.setProdtp(E_PRODTP.DEPO);
			//
			// // 检查开户信息
			// opaccd.chkOpaccInfo(dpInputSetter, dpProperty, dpOutput);
			// // 绑卡认证结果
			// opaccd.chkBdcart(dpInputSetter, dpProperty, dpOutput);
			//
			// KnaMaad tblKnaMaad = KnaMaadDao.selectOne_odb2(mactid, false);
			// // 主账户开户标识存在，不新增电子账户。
			// KnaCust tbKnaCust = null;
			// String custac = null;
			// String cardno = null;
			// IoCaSrvGenEAccountInfo accountInfoImpl =
			// SysUtil.getInstance(IoCaSrvGenEAccountInfo.class);
			//
			// if (CommUtil.isNotNull(tblKnaMaad)) {
			// tbKnaCust = KnaCustDao.selectOne_odb1(tblKnaMaad.getCustac(), false);
			// custac = tbKnaCust.getCustac();
			// }
			//
			// if (CommUtil.isNotNull(tbKnaCust)) {
			// // 如果存在状态正常的电子账户，则不新开电子账户，直接返回可用电子账户信息。
			// if (E_ACCTST.NORMAL == tbKnaCust.getAcctst()) {
			// // 状态不正常，暂时抛错，后期确认后调整。
			// } else {
			// throw JFDpError.EAcct.E0016(mactid);
			// }
			//
			// KnaAcdc tbKnaAcdc = KnaAcdcDao.selectFirst_odb3(tbKnaCust.getCustac(), true);
			// cardno = tbKnaAcdc.getCardno();
			//
			// } else {
			// // 开立电子账户服务
			// IoCaAddEAccountIn accountIn = SysUtil.getInstance(IoCaAddEAccountIn.class);
			// accountIn.setCustna(custna);
			// accountIn.setCustno(custno);// 客户号
			// accountIn.setIdckrt(idckrt);
			// accountIn.setMpckrt(mpckrt);
			// accountIn.setCacttp(cacttp);
			// accountIn.setBrchno(brchno);
			// accountIn.setBdrtfg(dpProperty.getBdrtfg());
			// accountIn.setUschnl(uschnl);
			// accountIn.setAccttp(accatp);
			// // accountIn.setIsopcd(null);// 开卡号标志
			// accountIn.setOpacwy(dpProperty.getOpacwy());// 开户方式
			// accountIn.setCrcycd(crcycd);
			// accountIn.setCsextg(dpProperty.getCsextg());// 钞汇标志
			// accountIn.setTlphno(tlphno);
			// // accountIn.setTranpw(null);// 交易密码
			// // accountIn.setAuthif(authif);
			// accountIn.setCustno(custno);
			// accountIn.setFacesg(facesg);
			// accountIn.setIdtfno(idtfno);
			// accountIn.setIdtftp(idtftp);
			// accountIn.setUsertp(ustype);
			// accountIn.setMactid(mactid);
			// IoCaAddEAccountOut outputEAcc = accountInfoImpl.prcAddEAccount(accountIn);
			// custac = outputEAcc.getCustac();
			// cardno = outputEAcc.getCardno();
			// // LOGGER.debug("进入开主账户");
			//
			// }
			// // 根据品牌信息列表循环开立品牌子账户
			// /*
			// * dpProperty.setBaprcd(null); dpProperty.setOpacfg(null);
			// * dpProperty.setCusttp(null); dpProperty.setProdtp(null);
			// * dpProperty.setCustac(out1.getCustac());
			// dpProperty.setDepttm(E_TERMCD.T000);
			// * opaccd.openSubaccts(dpInputSetter, dpProperty, dpOutput);
			// */
			// // 开立结算负债子账户
			// cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.AddAcct.InputSetter
			// addAcctInput = SysUtil
			// .getInstance(cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType.AddAcct.InputSetter.class);
			// addAcctInput.setCustac(custac);// 客户账号
			// addAcctInput.setCustno(custno);
			// addAcctInput.setAcctna(custna);// 账户姓名
			// addAcctInput.setCacttp(cacttp);
			// addAcctInput.setProdcd(dpProperty.getBaprcd());// 产品编号
			// addAcctInput.setDepttm(dpProperty.getDepttm());// 存期
			// addAcctInput.setCrcycd(crcycd);
			// addAcctInput.setCusttp(dpProperty.getCusttp());// 客户类型
			// // addAcctInput.setOpenir(null);// 币种
			// // addAcctInput.setTranam(null);// 交易金额
			// // addAcctInput.setAcsetp(null);// 子账户类型
			// DpAcctSvcImpl dpAcctSvcImpl = SysUtil.getInstance(DpAcctSvcImpl.class);
			// AddSubAcctOut outputSubAcc = dpAcctSvcImpl.addAcct(addAcctInput);
			// // 负债子账户与电子账户关联服务
			// IoCaAddEARelaIn eARelaIn = SysUtil.getInstance(IoCaAddEARelaIn.class);
			// eARelaIn.setAcctno(outputSubAcc.getAcctno());// 负债账号
			// eARelaIn.setCustac(custac);// 电子账号
			// eARelaIn.setCrcycd(crcycd);
			// eARelaIn.setProdtp(dpProperty.getProdtp());// 产品类型
			// eARelaIn.setFcflag(outputSubAcc.getPddpfg());// 定活标志
			// eARelaIn.setProdcd(outputSubAcc.getProdcd());
			// eARelaIn.setCsextg(dpProperty.getCsextg());
			// // eARelaIn.setSubsac(null);// 子户号
			// accountInfoImpl.prcAddEARela(eARelaIn);
			//
			// // 新添加品牌子账号信息初始化
			// KnaSbad subaddInfo = SysUtil.getInstance(KnaSbad.class);
			// subaddInfo.setCustac(custac);
			// subaddInfo.setAcctno(outputSubAcc.getAcctno());
			// subaddInfo.setMactid(custno);
			// subaddInfo.setAcctid(acctid);
			// subaddInfo.setSbrand(sbrand);
			// subaddInfo.setBradna(bradna);
			// subaddInfo.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
			// subaddInfo.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp());
			// subaddInfo.setAcctna(DecryptConstant.decrypt(custna));
			// KnaSbadDao.insert(subaddInfo);
			//
			// // 绑定手机号
			// IoCaSevAliasBindChange bindChangeImpl =
			// SysUtil.getInstance(IoCaSevAliasBindChange.class);
			// bindChangeImpl.addBindAlias(cardno, dpInputSetter.getTlphno(),
			// dpProperty.getAcaltp(), dpProperty.getCustac());
			// 绑定账户信息
			// IoCaSevGenBindCard bindCardImpl =
			// SysUtil.getInstance(IoCaSevGenBindCard.class);
			// bindCardImpl.addBindCarList(dpInputSetter.getLstBindcaInfo(),
			// outputEAcc.getCustac(), dpInputSetter.getAccatp(), dpProperty.getCardno());
			// 身份核查流水登记
			/*
			 * IoCaKnbCksq caKnbCKsq = SysUtil.getInstance(IoCaKnbCksq.class);
			 * caKnbCKsq.setCkrtsq(ckrtsq); caKnbCKsq.setCustac(dpProperty.getCustac());
			 * caKnbCKsq.setCustid(custid);
			 * caKnbCKsq.setCktntp(dpProperty.getCktntp());//检查交易类型
			 * caKnbCKsq.setIdckrt(idckrt); IoCaSevAccountManager accountManageImpl =
			 * SysUtil.getInstance(IoCaSevAccountManager.class);
			 * accountManageImpl.caCheckSqReg(caKnbCKsq);
			 */
			// 修改状态
			// dm_icore_open_account dmIcoreOpenAccount =
			// Dm_icore_open_accountDao.selectOne_odb1(exportId, false);
			// if (CommUtil.isNotNull(dmIcoreOpenAccount)) {
			// 导出时间
			SimpleDateFormat sdfExp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();
			String exportTime = sdfExp.format(date);
			// dataItem.setExport_time(exportTime);
			// 导出状态
			// dataItem.setExport_status(E_EXPSTS.SUCESS);
			DmTmpDao.updateDmIcoreOpenAccountById(dataItem.getExport_id(), E_EXPSTS.SUCESS, exportTime);
			// Dm_icore_open_accountDao.updateOne_odb1(dataItem);
			// }
		} catch (Exception e) {
			LOGGER.error("报错信息：" + e.toString());
			DmTmpDao.updateDmIcoreOpenAccountById(dataItem.getExport_id(), E_EXPSTS.FAIL, "");
		}
	}

	/**
	 * 获取数据遍历器。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @return 数据遍历器
	 */
	// @Override
	// public
	// BatchDataWalker<cn.sunline.edsp.busi.icore.dm.tables.DmDsTmp.dm_icore_open_account>
	// getBatchDataWalker(
	// cn.sunline.edsp.busi.icore.dm.batchtran.intf.Opaccd.Input input,
	// cn.sunline.edsp.busi.icore.dm.batchtran.intf.Opaccd.Property property) {
	// /*
	// * 写代码根据exportID做数据拆分
	// */
	// Params param = new Params();
	// return new
	// CursorBatchDataWalker<cn.sunline.edsp.busi.icore.dm.tables.DmDsTmp.dm_icore_open_account>(
	// DmDsTmpDao.namedsql_select_dm_icore_open_account_not_export_01, param);
	// }

	@Override
	public BatchDataWalker<dp_icore_open_account> getJobBatchDataWalker(
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Btopen.Input arg0,
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Btopen.Property arg1, String dataItem) {
		String[] arrays = dataItem.split("-");
		if (arrays.length != 2) {
			return null;
		}
		String start = StringUtils.isBlank(arrays[0]) ? null : arrays[0];
		String end = StringUtils.isBlank(arrays[1]) ? null : arrays[1];
		LOGGER.info("BatchDataWalker. dm_icore_open_account01 :start:%s, end:%s", start, end);
		List<dp_icore_open_account> list = DmTmpDao.select_dm_icore_open_account_not_by_export_id_01(start, end,
				false);
		LOGGER.debug("exportId list %s",
				list.stream().filter(Objects::nonNull).map(s -> s.getCustno()).collect(Collectors.toList()));
		return new ListBatchDataWalker<>(list);
	}

	@Override
	public BatchDataWalker<String> getBatchDataWalker(cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Btopen.Input arg0,
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Btopen.Property arg1) {
		Long count = DmTmpDao.select_count_dm_icore_open_account_01(false);
		Integer num = count > 1000 ? 1000 : count.intValue();
		// 分段的结
		java.util.List<String> rowIdList = DmTmpDao.select_dm_icore_open_account_not_export_id_01(num, false);
		java.util.List<String> listmap = new ArrayList<>();
		if (CommUtil.isNotNull(rowIdList) && rowIdList.size() >= 1) {
			listmap.add(" -" + rowIdList.get(0));
			LOGGER.debug("exportId : start [%s], end:[%s]", rowIdList.get(0), rowIdList.get(rowIdList.size() - 1));
			for (int i = 1; i < rowIdList.size(); i++) {
				listmap.add(rowIdList.get(i - 1) + "-" + rowIdList.get(i));
			}
			listmap.add(rowIdList.get(rowIdList.size() - 1) + "- ");
		}
		return new ListBatchDataWalker<>(listmap);
	}

	/**
	 * 获取卡号
	 * 
	 * @param accttp
	 * @return cardno
	 */
	public static String prcCardInfo(E_ACCATP accttp) {

		/**
		 * add by xj 20180518 柳州银行II类户卡号 621412(卡bin)+0132(产品号)+7位序号+1位校验位
		 */
		KnpParameter cardPara = SysUtil.getInstance(KnpParameter.class);
		String sortcd = null;
		if (BaseEnumType.E_ACCATP.FINANCE == accttp) {
			cardPara = KnpParameterDao.selectOne_odb1("KcdProd.cardbn", "kabin", "lzbank", "2", true);
			sortcd = "Cardno12_17_2";
		} else if (BaseEnumType.E_ACCATP.WALLET == accttp) {
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

		cardBuff = cardBuff.append(kabin).append(cardPara.getParm_value2()).append(BusiTools.getSequence(sortcd, 7));
		String chkNo = CaTools.countParityBit(cardBuff);
		String cardno = cardBuff.append(chkNo).toString();
		return cardno;
	}
}
