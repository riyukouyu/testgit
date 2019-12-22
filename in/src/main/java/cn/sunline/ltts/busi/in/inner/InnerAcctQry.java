package cn.sunline.ltts.busi.in.inner;

import java.math.BigDecimal;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.busi.dp.errors.InModuleError;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaLsbl;
import cn.sunline.ltts.busi.in.tables.In.GlKnaLsblDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnbOcac;
import cn.sunline.ltts.busi.in.tables.In.GlKnbOcacDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBusi;
import cn.sunline.ltts.busi.in.tables.In.GlKnpSuac;
import cn.sunline.ltts.busi.in.tables.In.GlKnpSuacDao;
import cn.sunline.ltts.busi.in.type.InQueryTypes.InacProInfo;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.in.IoInOpenCloseComplex.IoInacOpen_IN;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInAcctTmp;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_AUTOOP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BEINTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ISOPEN;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ISPAYA;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_OPACTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_RLBLTG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRCHLV;

/**
 * 内部账户查询相关逻辑
 * */
public class InnerAcctQry {
	private static final BizLog bizlog = BizLogUtil
			.getBizLog(InnerAcctQry.class);

	/**
	 * @author wanggl
	 * 
	 *         <p>
	 *         <li>通过内部户帐号查询内部户信息</li>
	 *         <li>2015-03-28 14:48</li>
	 *         </p>
	 * @param acctno
	 *            内部户帐号
	 * @return 内部户信息复杂类型
	 *         {@link cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo}
	 * */
	public static IoInacInfo qryInacInfoByAcctno(String acctno) {
		if (CommUtil.isNull(acctno)) {
			throw InError.comm.E0001();
		}
		GlKnaAcct tblGlKnaAcct = null;
		try {
			tblGlKnaAcct = InQuerySqlsDao.sel_GlKnaAcct_by_acct(acctno, true);// GlKnaAcctDao.selectOne_odb1(acctno,
																				// true);
		} catch (Exception e) {
			return null;
		}
		IoInacInfo inacInfo = SysUtil.getInstance(IoInacInfo.class);
		CommUtil.copyProperties(inacInfo, tblGlKnaAcct, false);

		// 内部户余额返回 add by chenjk 20161221
		inacInfo.setOnlnbl(InnerAcctQry.queryInnerAcctApi(acctno).getOnlnbl());

		return inacInfo;
	}

	/**
	 * 根据当前交易法人，获取清算法人
	 * @return
	 */
	public static String getClearCorpno() {
		String curCorpno = BusiTools.getBusiRunEnvs().getSpcono();
		KnpParameter para = KnpParameterDao.selectOne_odb1("ClearCorpno", curCorpno, "%",
				"%", false);
		String clearCorpno;
		if(CommUtil.isNull(para)){//参数表未配置，取中心法人为清算法人
			clearCorpno = CommTools.getBaseRunEnvs().getCenter_org_id();
		} else {
			if(CommUtil.isNull(para.getPrimary_key1())) {
				throw InError.comm.E0003("当前交易法人未正确配置清算法人,pmkey1为空");
			}
			clearCorpno = para.getParm_value1();
		}
		return clearCorpno;
	}
	/**
	 * 根据机构号获取法人号
	 * @param brchno
	 * @return
	 */
	public static String getCorpnoByBrchno(String brchno) {
		IoBrchInfo branch = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(brchno);
		return branch.getCorpno();
	}
	/**
	 * 根据机构号获取中心机构号
	 * @param brchno
	 * @return
	 */
	public static String getCenterBrch(String brchno) {
		String centbr = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(brchno).getBrchno();
		return centbr;
	}
	
	/**
	 * @author wanggl
	 * 
	 *         <p>
	 *         <li>内部户开户查询逻辑</li>
	 *         <li>2015-03-28 16:06</li>
	 *         </p>
	 * @param String
	 *            itemcd 科目 String crcycd 币种 String acbrch 机构 String busino
	 *            业务编码IA String acctno 帐号,String subsac 子户号
	 * @return InacProInfo
	 * */
	public static InacProInfo qryAcctPro(String crcycd, String acbrch,
			String busino, String acctno, String itemcd, String subsac) {
		// 返回对象
		InacProInfo info = SysUtil.getInstance(InacProInfo.class);
		// 取消初始化提高性能 modify by chenlk 20161230
		GlKnpBusi tblbusi = null;// SysUtil.getInstance(GlKnpBusi.class);
		GlKnaAcct knaAcctTmp = null;// SysUtil.getInstance(GlKnaAcct.class);

		info.setIsexis(E_YES___.YES);
		if (CommUtil.isNull(acctno)) {
			
			String inputCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();//调用方交易法人
			/*String corpno = getClearCorpno();//获取清算法人
			if(CommUtil.isNull(acbrch)) {
				acbrch = CommTools.getBaseRunEnvs().getTrxn_branch();
			}
			//如果清算法人不为当前交易法人，获取中心机构
			if(!CommUtil.equals(corpno,inputCorpno)) {
				acbrch = getCenterBrch(acbrch);
			} else {
				acbrch = CommTools.getBaseRunEnvs().getTrxn_branch();
			}*/
			
			// 判断以那种形式获取开户账户属性
			if (CommUtil.isNull(busino)) {
				throw InError.comm.E0001();
			} else {
				// 依据业务编码IA获取
				tblbusi = InQuerySqlsDao.selBusinoDetail(busino,inputCorpno,true);
			}
			if (CommUtil.isNotNull(subsac)) {
				// 子户号不为空，则用子户号查询内部户
				knaAcctTmp = InacSqlsDao.queryInAcctBySubsac(busino, crcycd, acbrch, subsac, false);
			} else {
				// 默认查询基准账户
				knaAcctTmp = InacSqlsDao.queryInBaseAcct(busino, crcycd, acbrch, false);
			}
			// knaAcctTmp = GlKnaAcctDao.selectFirst_odb2(acbrch, crcycd,
			// E_INACTP.BASE, busino, false);
			if (CommUtil.isNull(knaAcctTmp)) {
				info.setIsexis(E_YES___.NO);
				CommUtil.copyProperties(info, tblbusi);
			} else {
				info.setIsexis(E_YES___.YES);
				CommUtil.copyProperties(info, knaAcctTmp);
			}
		} else {
			knaAcctTmp = InQuerySqlsDao.sel_GlKnaAcct_by_acct(acctno, false);

			if (CommUtil.isNotNull(knaAcctTmp)) {
				if (CommUtil.compare(knaAcctTmp.getCrcycd(), crcycd) != 0) {
					throw InError.comm.E0003("账号[" + acctno + "]币种与交易币种不符");
				}

				info.setIsexis(E_YES___.YES);
				CommUtil.copyProperties(info, knaAcctTmp, false);

			} else {
				info.setIsexis(E_YES___.NO);
			}
		}
		return info;
	}

	private static String getGenAcctno(String busino, String crcycd,
			String acbrch, E_INACTP inactp, String subsac) {
		// 生成账号
		String acctno = InnerAcctQry.genAcctno(busino, crcycd, acbrch, inactp,
				subsac);
		bizlog.debug("生成的帐号：" + acctno, acctno);
		return acctno;
	}

	private static String genAcctno(String busino, String crcycd,
			String brchno, E_INACTP inactp, String subsac) {
		String acctno = null;
		if (CommUtil.isNull(busino)) {
			throw InError.comm.E0003("产品编码不能为空!");
		}

		if (CommUtil.isNull(crcycd)) {
			throw InError.comm.E0003("币种不能为空!");
		}

		if (CommUtil.isNull(brchno)) {
			throw InError.comm.E0003("部门不能为空!");
		}
		String serial = "";
		String scrcycd = BusiTools.getApCurrency(crcycd).getCcynum();

		//截取内部户产品后十位
		String sub_busino = busino.substring(6);
		// 取最大账户序号
		serial = InacSqlsDao.queryMaxSerial(brchno + scrcycd + sub_busino, true);

		Long maxvalue = ConvertUtil.toLong(CommUtil.nvl(serial, "0000"));

		maxvalue = maxvalue + 1;
		serial = "0000" + maxvalue.toString();
		serial = serial.substring(maxvalue.toString().length());

		// 生成账号
		acctno = BusiTools.genCheckNumberReturnAcctno(brchno + scrcycd + sub_busino+ serial);

		return acctno;
	}

	// 新增内部账户
	public static GlKnaAcct addInAcct(IoInacOpen_IN inacopIn) {

		String inputCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		/*String corpno = getClearCorpno();//获取清算法人
		if(CommUtil.isNull(inacopIn.getAcbrch())) {
			inacopIn.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
		}
		//如果清算法人不为当前交易法人，获取中心机构
		if(!CommUtil.equals(corpno,inputCorpno)) {
			String centbr = getCenterBrch(inacopIn.getAcbrch());
			inacopIn.setAcbrch(centbr);
		} else {
			inacopIn.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
		}*/
		
		String busino = inacopIn.getBusino();// 业务代码
		String subsac = inacopIn.getSubsac();// 子户号
		E_INACTP inactp = CommUtil.nvl(inacopIn.getInactp(), E_INACTP.BASE);// 内部户账户类型
		String crcycd = inacopIn.getCrcycd();// 币种
		String acbrch = inacopIn.getAcbrch();// 账户所属机构
		String acctna = inacopIn.getAcctna();// 内部户名称
		String itemcd = inacopIn.getItemcd();// 科目
		E_BEINTP beintp = inacopIn.getBeintp();// 结息方式
		BigDecimal ovmony = inacopIn.getOvmony();// 透支限额
		BigDecimal inrate = inacopIn.getInrate();// 利率
		BigDecimal ovrate = inacopIn.getOvrate();// 透支利率
		String reveac = inacopIn.getReveac();// 收息账号
		String paymac = inacopIn.getPaymac();// 付息账号
		E_YES___ isbein = inacopIn.getIsbein();// 是否结息

		// 机构合法性检查
		SysUtil.getRemoteInstance(IoSrvPbBranch.class).inspectBranchLegality(
				acbrch, inacopIn.getCrcycd());
		// 查询业务代码信息
		GlKnpBusi tblbusi = InQuerySqlsDao.selBusinoDetail(busino,inputCorpno, false);
		if (CommUtil.isNull(tblbusi)) {

			throw InModuleError.InAccount.IN030001(busino);
		}
		
		// add by sh 20171212 核心系统不允许开立损益类的内部户，核心不做损益结转，这一类记账直接走会计流水登记记账，不开账户
		if(tblbusi.getBusitp() == InEnumType.E_BUSITP._5){
			throw InModuleError.InAccount.IN030002(busino);
		}

		BigDecimal zero1 = BigDecimal.ZERO;
		if(tblbusi.getPmodtg() == E_YES___.NO && !(CommUtil.compare(inacopIn.getOvmony(),zero1) == 0)) {
			throw InModuleError.InAccount.IN030003(busino);
		}
		
		if (tblbusi.getBusist() != E_PRODST.NORMAL) {

			throw InModuleError.InAccount.IN030004(busino);
		}

		E_ISPAYA ispaya = CommUtil.nvl(inacopIn.getIspaya(),
				tblbusi.getIspaya());// 是否采用销账管理

		if (CommUtil.isNotNull(subsac)) {

			inactp = E_INACTP.SPECIAL;
			if (subsac.length() != 7) {

				throw InModuleError.InAccount.IN030005();
			}
		}

		// 开户前检查获取属性参数
		if (inactp == E_INACTP.SPECIAL) {

			GlKnaAcct knaAcctTmp = InacSqlsDao.queryInAcctBySubsac(busino,
					crcycd, acbrch, subsac, false);

			// 如果基准账户或者专用已经开立，则直接返回账号
			if (CommUtil.isNotNull(knaAcctTmp)
					&& CommUtil.isNotNull(knaAcctTmp.getAcctno())) {

				return knaAcctTmp;
			}
		} else if (inactp == E_INACTP.BASE) {

			GlKnaAcct knaAcctTmp = InacSqlsDao.queryInBaseAcct(busino, crcycd,
					acbrch, false);

			// 如果基准账户或者专用已经开立，则直接返回账号
			if (CommUtil.isNotNull(knaAcctTmp)
					&& CommUtil.isNotNull(knaAcctTmp.getAcctno())) {

				return knaAcctTmp;
			}
		}

		if (tblbusi.getRmsign() == E_YES___.YES
				&& !CommUtil.equals(crcycd,BusiTools.getDefineCurrency())) {

			throw InModuleError.InAccount.IN030006(busino);
		}

		if (tblbusi.getFnsign() == E_YES___.YES
				&& CommUtil.equals(crcycd,BusiTools.getDefineCurrency())) {

			throw InModuleError.InAccount.IN030007(busino);
		}

		if (tblbusi.getIsopen() == E_ISOPEN._0) {

			throw InModuleError.InAccount.IN030008(busino);
		}

		if (tblbusi.getIsopen() == E_ISOPEN._1 && inactp == E_INACTP.MANUAL) {

			throw InModuleError.InAccount.IN030009(busino);
		}
		
		//add by xionglz 增加3-手工单账户类型，允许手工开立单个内部户
		if(tblbusi.getIsopen() == E_ISOPEN._3 &&  inactp == E_INACTP.MANUAL){
			//如果类型为3-手工单账户则校验是否已经开户
			GlKnaAcct tblglknaacct = GlKnaAcctDao.selectFirst_odb3(inacopIn.getAcbrch(), inacopIn.getCrcycd(), 
																	 inacopIn.getBusino(), E_INACST.NORMAL,false);
			//如果已经开户则报错
			if(CommUtil.isNotNull(tblglknaacct)){
				throw InModuleError.InAccount.IN030010(busino);
			}			
			
			//设置子户号
			subsac=tblbusi.getSubsac();
		}

		if (tblbusi.getIspaya() != E_ISPAYA._9 && tblbusi.getIspaya() != ispaya) {

			throw InModuleError.InAccount.IN030011(busino);
		}
		//远程调用代码修正20190831-huwenqing
		IoBrchInfo brchInfo = SysUtil.getRemoteInstance(IoSrvPbBranch.class).getBranch(acbrch);

		BusiTools.getBusiRunEnvs().setSpcono(brchInfo.getCorpno());

		// 省级机构开户控制
		if (E_AUTOOP._0 == tblbusi.getProvop()
				&& brchInfo.getBrchlv() == E_BRCHLV.PROV) {

			throw InModuleError.InAccount.IN030012(busino);
		}
		if (E_AUTOOP._1 == tblbusi.getProvop()
				&& brchInfo.getBrchlv() == E_BRCHLV.PROV
				&& !CommUtil.equals(crcycd,BusiTools.getDefineCurrency())) {

			throw InModuleError.InAccount.IN030013(busino);
		}
		// 县级机构开户控制
		if (E_AUTOOP._0 == tblbusi.getCounop()
				&& brchInfo.getBrchlv() == E_BRCHLV.COUNT) {

			throw InModuleError.InAccount.IN030014(busino);
		}
		if (E_AUTOOP._1 == tblbusi.getCounop()
				&& brchInfo.getBrchlv() == E_BRCHLV.COUNT
				&& !CommUtil.equals(crcycd,BusiTools.getDefineCurrency())) {

			throw InModuleError.InAccount.IN030015(busino);
		}
		/*if (!CommTools.getBaseRunEnvs().getTrxn_code().equals("inopen")
				&& tblbusi.getIspaya() == E_ISPAYA._9) {*/
			ispaya = E_ISPAYA._0;
		/*} else {
			ispaya = CommUtil.nvl(ispaya, tblbusi.getIspaya());
		}*/
		// 检查子户表
		if (CommUtil.isNotNull(subsac)) {

			GlKnpSuac tblGlKnpSuac = GlKnpSuacDao.selectOne_odb1(busino,
					subsac, false);

			if (null == tblGlKnpSuac
					|| CommUtil.isNull(tblGlKnpSuac.getBusino())) {

				throw InModuleError.InAccount.IN030016(busino,subsac);
			} else {
				tblbusi.setBusina(tblGlKnpSuac.getBusina());
			}
		}

		// 基准账户不存在或者开手工账户，则进行开户处理，生成账号
		String acctno = InnerAcctQry.getGenAcctno(busino, crcycd, acbrch,
				inactp, subsac);

		BigDecimal zero = BigDecimal.ZERO;
		// 账户表
		GlKnaAcct acct = SysUtil.getInstance(GlKnaAcct.class);
		acct.setCorpno(brchInfo.getCorpno());// 法人号
		acct.setAcctno(acctno);
		acct.setBusino(busino);
		acct.setAcctna(CommUtil.nvl(acctna, tblbusi.getBusina()));// 户名
		acct.setBrchno(acbrch);// 账户所属机构
		acct.setCrcycd(crcycd);// 币种
		acct.setInactp(inactp);// 账户类型
		acct.setOpenbr(acbrch);// 开户机构
		acct.setOpendt(CommTools.getBaseRunEnvs().getTrxn_date());// 开户日期
		acct.setOptrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 开户流水
		acct.setIoflag(tblbusi.getIoflag());// 表内外标志
		acct.setBlncdn(tblbusi.getBlncdn());// 余额方向
		acct.setItemcd(itemcd);
		acct.setSubsac(subsac);
		acct.setCrctbl(zero);// 联机贷方余额
		acct.setCredbl(zero);// 当日贷方余额
		acct.setDrctbl(zero);// 当日借方余额
		acct.setDredbl(zero);// 当日借方余额
		acct.setAcctst(E_INACST.NORMAL);// 账户状态
		acct.setBusidn(tblbusi.getBlncdn());// 业务代码方向
		acct.setPmodtg(tblbusi.getPmodtg());// 透支许可
		acct.setKpacfg(tblbusi.getKpacfg());// 记账控制
		acct.setIspaya(ispaya);// 是否挂销账
		acct.setRlbltg(tblbusi.getRlbltg());// 余额更新方式
		acct.setKpacbr(acbrch);// 核算账号
		acct.setBusitp(tblbusi.getBusitp());
		acct.setIsbein(CommUtil.nvl(isbein, tblbusi.getIsbein()));// 计息标志
		acct.setBeintp(CommUtil.nvl(beintp, tblbusi.getBeintp()));// 结清方式
		acct.setCractp(tblbusi.getCractp());// 冲销标志
        acct.setOvmony(ovmony == null ? BigDecimal.ZERO : ovmony);// 透支限额
		acct.setInrate(inrate);// 利率
		acct.setOvrate(ovrate);// 透支利率
		if (E_YES___.YES == isbein) {
			// 结息入账时 收付息账号未输入默认自己
			acct.setReveac(CommUtil.nvl(reveac, acctno));// 收息账号
			acct.setPaymac(CommUtil.nvl(paymac, acctno));// 付息账号
		} else {
			acct.setReveac(reveac);// 收息账号
			acct.setPaymac(paymac);// 付息账号
		}

		GlKnaAcctDao.insert(acct);
		
		// 登记路由 rambo add
		ApAcctRoutTools.register(acct.getAcctno(), E_ACCTROUTTYPE.INSIDE);
		
		// 记录上日余额
		GlKnaLsbl lsblTmp = SysUtil.getInstance(GlKnaLsbl.class);

		lsblTmp.setAcctno(acctno);
		lsblTmp.setDrltbl(zero);
		lsblTmp.setCrltbl(zero);
		lsblTmp.setLastdn(tblbusi.getBlncdn());
		lsblTmp.setLastdt(DateTools2.dateAdd (-1, CommTools.getBaseRunEnvs().getTrxn_date()));
		GlKnaLsblDao.insert(lsblTmp);

		// 登记内部账户开销户记录
		GlKnbOcac ocac = SysUtil.getInstance(GlKnbOcac.class);
		ocac.setAcctna(CommUtil.nvl(acctna, tblbusi.getBusina()));
		ocac.setAcctno(acctno);
		ocac.setCrcycd(crcycd);
		if (inactp == E_INACTP.MANUAL) {
			ocac.setOpactp(E_OPACTP._1);
		} else {
			ocac.setOpactp(E_OPACTP._0);
		}
		ocac.setOpbrno(acbrch);
		ocac.setOpendt(CommTools.getBaseRunEnvs().getTrxn_date());
		ocac.setOpensq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		//ocac.setOpiasq(Long.parseLong(SequenceManager.nextval("gl_knb_ocac"))+ "");
		ocac.setOpiasq(CoreUtil.nextValue("gl_knb_ocac"));
        if (CommTools.getBaseRunEnvs().getTrxn_code().equals("inopen")) {
			ocac.setOpener(CommTools.getBaseRunEnvs().getTrxn_teller());
        } else {
			ocac.setOpener("999S201");
        }
		ocac.setTmstmp(CommTools.getBaseRunEnvs().getTimestamp()); // 维护日期
		GlKnbOcacDao.insert(ocac);
		return acct;

	}

	public static IoInAcctTmp queryInnerAcctApi(String acctno) {

		if (CommUtil.isNull(acctno)) {
			throw InError.comm.E0005(acctno);
		}

		IoInAcctTmp AcctTmp = SysUtil.getInstance(IoInAcctTmp.class);

		// 允许账号输入待销帐序号 TODO
		// String fix = SysUtil.getParam(GlParm.PayaPreFix.class).getPrefix();
		// if(CommUtil.compare(acctno.substring(0, fix.length()),fix) == 0
		// && CommUtil.isNotNull(fix)){
		// GlKdbPaya kdbPayaTmp = SysUtil.getInstance(GlKdbPaya.class);
		// kdbPayaTmp = GlAPIFactory.getRegiAPI().getGlKdbPaya(acctno);
		// acctno = kdbPayaTmp.getAcctno();
		// }
		AcctTmp = InacSqlsDao.queryinAcctApi(acctno, true);
		// 处理非实时余额的情况
		if (AcctTmp.getRlbltg() == E_RLBLTG._2) {
			// 按借方统计
			BigDecimal totlam = null;
			Map<String, Object> ResultMap;
			ResultMap = InacSqlsDao.qrySumRlbl(acctno, false);
			if (CommUtil.isNotNull(ResultMap)) {
				totlam = ConvertUtil.toBigDecimal(ResultMap.get("tranam"));
			}
			if (AcctTmp.getLastdn() == E_BLNCDN.C
					|| AcctTmp.getLastdn() == E_BLNCDN.P) {
				AcctTmp.setCrctbl(AcctTmp.getCrctbl().subtract(totlam));
			} else if (AcctTmp.getLastdn() == E_BLNCDN.D
					|| AcctTmp.getLastdn() == E_BLNCDN.R) {
				AcctTmp.setDrctbl(AcctTmp.getDrctbl().add(totlam));
			}
			if (AcctTmp.getBusidn() == E_BLNCDN.Z
					|| AcctTmp.getBusidn() == E_BLNCDN.B) {
				BigDecimal tranbl = AcctTmp.getDrctbl().subtract(
						AcctTmp.getCrctbl());
				if (CommUtil.compare(tranbl, BigDecimal.ZERO) < 0) {
					AcctTmp.setCrctbl(tranbl.negate());
					AcctTmp.setBlncdn(E_BLNCDN.C);

					AcctTmp.setDrctbl(BigDecimal.ZERO);
				} else if (CommUtil.compare(tranbl, BigDecimal.ZERO) > 0) {
					AcctTmp.setDrctbl(tranbl);
					AcctTmp.setBlncdn(E_BLNCDN.D);

					AcctTmp.setCrctbl(BigDecimal.ZERO);
				}
			}
		}
		// 处理联机余额
		//by update cqm 20171013 cqm 可用余额需加上透支限额  可用余额=当前余额+透支限额
		/*if (AcctTmp.getBlncdn() == E_BLNCDN.C
				|| AcctTmp.getBlncdn() == E_BLNCDN.P) {
			AcctTmp.setOnlnbl(AcctTmp.getCrctbl().subtract(AcctTmp.getDrctbl()).add(AcctTmp.getOvmony()));
		} else {
			AcctTmp.setOnlnbl(AcctTmp.getDrctbl().subtract(AcctTmp.getCrctbl()).add(AcctTmp.getOvmony()));
		}*/
		// 计算的是账户当前余额 
		if (AcctTmp.getBlncdn() == E_BLNCDN.C
				|| AcctTmp.getBlncdn() == E_BLNCDN.P) {
			AcctTmp.setOnlnbl(AcctTmp.getCrctbl().subtract(AcctTmp.getDrctbl()));
		} else {
			AcctTmp.setOnlnbl(AcctTmp.getDrctbl().subtract(AcctTmp.getCrctbl()));
		}

		// 处理上日余额
		if (AcctTmp.getLastdn() == E_BLNCDN.C
				|| AcctTmp.getLastdn() == E_BLNCDN.P) {
			AcctTmp.setLastbl(AcctTmp.getCrltbl());
		} else if (AcctTmp.getLastdn() == E_BLNCDN.D
				|| AcctTmp.getLastdn() == E_BLNCDN.R) {
			AcctTmp.setLastbl(AcctTmp.getDrltbl());
		}
		if (AcctTmp.getBusidn() == E_BLNCDN.Z
				|| AcctTmp.getBusidn() == E_BLNCDN.B) {
			AcctTmp.setLastbl(AcctTmp.getDrltbl().subtract(AcctTmp.getCrltbl())
					.abs());
		}

		return AcctTmp;
	}
	
	/**
	 * 生成套票号
	 * 
	 * W+柜员号(7)+序号(3)
	 * <p>Title:genAcstno </p>
	 * <p>Description:	</p>
	 * @author songhao
	 * @date   2017年8月1日 
	 * @return
	 */
	public static String genAcstno(){
	    return "W" + CommTools.getBaseRunEnvs().getTrxn_teller()  
                    + BusiTools.getSequence(CommTools.getBaseRunEnvs().getTrxn_teller() + CommTools.getBaseRunEnvs().getTrxn_date(), 3);
	}
	
	

}