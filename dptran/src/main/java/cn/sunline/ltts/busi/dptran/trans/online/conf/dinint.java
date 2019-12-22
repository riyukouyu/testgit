package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntrTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntrTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.ltts.busi.pb.namedsql.intr.ProintrSelDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AVBLDT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINRD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRRTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CYCLTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IBAMMD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEBS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRDPWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_LEVETY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_LYDTTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_LYINWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_REPRWY;

public class dinint {

	public static void intDin(
			final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dinint.Input input) {

		// 校验机构只有省级机构才能操作
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());// 权限检查
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();// 交易机构
		String prodcd = input.getProdcd();// 产品号
		// 传入值检查
		if (CommUtil.isNull(prodcd)) {
			throw DpModuleError.DpstProd.BNAS1054();
		}
		
		KupDppbTemp tblkup_dppbt = KupDppbTempDao.selectOne_odb1(prodcd, false);
		if (CommUtil.isNull(tblkup_dppbt)) {
			throw DpModuleError.DpstProd.BNAS1337();
		}
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
		String trantm = BusiTools.getBusiRunEnvs().getTrantm();// 交易时间
		String trantn = trandt.concat(trantm);// 交易时间14位
		// 查看部件信息
		KupDppbPartTemp selPartTemp = KupDppbPartTempDao.selectOne_odb1( E_BUSIBI.DEPO, input.getProdcd(), E_PARTCD._CK09, false);

		if (CommUtil.isNotNull(selPartTemp)) {
			if (selPartTemp.getPartfg() == E_YES___.YES) {

				E_INTRTP intrtp = input.getIntrtp();
				E_INBEFG inbefg = input.getInbefg();
				E_YES___ txbefg = input.getTxbefg();
				E_INBEBS txbebs = input.getTxbebs();
				E_CAINRD hutxfg = input.getHutxfg();
				String txbefr = input.getTxbefr();
				String intrcd = input.getIntrcd();
				E_IRDPWY intrwy = input.getIntrwy();
				E_IRCDTP incdtp = input.getIncdtp();
				E_LYINWY lyinwy = input.getLyinwy();
				E_IBAMMD inammd = input.getInammd();
				E_AVBLDT bldyca = input.getBldyca();
				E_IRRTTP inprwy = input.getInprwy();
				String inadlv = input.getInadlv();
				E_REPRWY reprwy = input.getReprwy();
				String taxecd = input.getTaxecd();
				E_CYCLTP cycltp = input.getCycltp();
				E_YES___ isrgdt = input.getIsrgdt();
				E_LYDTTP lydttp = input.getLydttp();
				E_YES___ inwytp = input.getInwytp();  //利率靠档标志
				String tebehz = input.getTebehz();
				E_INTRDT intrdt = input.getIntrdt();// 利率确定日期
				E_LEVETY levety = input.getLevety();// 靠档规则

				if (CommUtil.isNull(intrtp)) {
					throw DpModuleError.DpstComm.BNAS0473();
				}

				if (CommUtil.isNull(inbefg)) {
					throw DpModuleError.DpstComm.BNAS0647();
				}

				if (E_INBEFG.INBE == inbefg) {
					if (CommUtil.isNull(txbefg)) {
						throw DpModuleError.DpstComm.BNAS0649();
					}
	
					// 利息类型只能为正利息
					if (E_INTRTP.ZHENGGLX != intrtp) {
						throw DpModuleError.DpstComm.BNAS2042();
					}
					// 当该属性选择1-是时，税率编号为必输项。
					if (txbefg == E_YES___.YES) {
						if (CommUtil.isNull(taxecd)) {
							throw DpModuleError.DpstComm.BNAS1245();
						}
						// 查询利率代码定义
						long count = ProintrSelDao.intxcodSelPb(taxecd, CommTools.getBaseRunEnvs().getBusi_org_id(), false);
	
						if (count <= 0) {
							throw DpModuleError.DpstComm.BNAS2043();
						}
	
					} else {
						if (CommUtil.isNotNull(taxecd)) {
							throw DpModuleError.DpstComm.BNAS2044();
						}
					}

					if (CommUtil.isNull(txbebs)) {
						throw DpModuleError.DpstComm.BNAS2045();
					}
	
					if (CommUtil.isNull(hutxfg)) {
						throw DpModuleError.DpstComm.BNAS0374();
					}

					// 当该属性选择1-计息时，计息频率必选设置。
					if (inbefg == E_INBEFG.INBE) {
						if (CommUtil.isNull(tebehz)) {
							throw DpModuleError.DpstComm.BNAS0640();
						}
	
						// 检查计息频率是否合法
						if (!DateTools2.chkFrequence(tebehz)) {
							throw DpModuleError.DpstComm.BNAS2046( tebehz);
						}
	
					}
					// 检查结息频率是否合法
					if (CommUtil.isNotNull(txbefr)) {
						if (!DateTools2.chkFrequence(txbefr)) {
							throw DpModuleError.DpstComm.BNAS2047(txbefr);
						}
	
					}
					// 利率调整频率是否合法
					if (CommUtil.isNotNull(inadlv)) {
						if (!DateTools2.chkFrequence(inadlv)) {
							throw DpModuleError.DpstComm.BNAS2048();
						}
					}
	
					if (CommUtil.isNull(intrcd)) {
						throw DpModuleError.DpstComm.BNAS0490();
					}
	
					if (CommUtil.isNull(incdtp)) {
						throw DpModuleError.DpstComm.BNAS0487();
					}

					// 检查基础利率是否是否存在
					if (E_IRCDTP.Reference == input.getIncdtp()) {
	//					SysUtil.getInstance(IoIntrSvrType.class).selRfirByrfircdOne( brchno, input.getIntrcd(), trandt, trantm);
						int count = DpProductDao.selRfirByRfircd(input.getIntrcd(), trantn,corpno, false);
						if (count <= 0) {
							throw DpModuleError.DpstProd.BNAS1321();
						}
						
						// 检查浮动利率是否是否存在
					} else if (E_IRCDTP.BASE == input.getIncdtp()) {
	//					SysUtil.getInstance(IoIntrSvrType.class).selBkirByintrcd( brchno, input.getIntrcd(), trandt, trantm);
						int count = DpProductDao.selBkirByIntrcd(input.getIntrcd(), trantn, corpno, false);
						if (count <= 0) {
							throw DpModuleError.DpstProd.BNAS1321();
						}
						
						// 检查分档利率是否是否存在
					} else if (E_IRCDTP.LAYER == input.getIncdtp()) {
	//					SysUtil.getInstance(IoIntrSvrType.class).SelRlirByintrcd( brchno, input.getIntrcd());
						int count = DpProductDao.selRlirByIntrcd(input.getIntrcd(), trantn, corpno, false);
						if (count <= 0) {
							throw DpModuleError.DpstProd.BNAS1321();
						}
						
					} else {
						throw DpModuleError.DpstProd.BNAS1323();
					}

					// 利率确定日期不能为空
					if (CommUtil.isNull(intrdt)) {
						throw DpModuleError.DpstComm.BNAS0478();
					}
	
					// 分档利率，当选择3-分档利率代码时，需要选择设置利率靠档标志
					if (E_IRCDTP.LAYER == incdtp) {
						if (CommUtil.isNull(inwytp)) {
							throw DpModuleError.DpstComm.BNAS2050();
						}
					} else {
						if (CommUtil.isNotNull(inwytp)) {
							throw DpModuleError.DpstComm.BNAS2049();
						}
					}

					// 该属性选择1-是时，利率靠档方式必选，分层计息方式、是否登记分层明细和分层明细积数调整方式不可设置。
					if (inwytp == E_YES___.YES) {
						if (CommUtil.isNull(intrwy)) {
							throw DpModuleError.DpstComm.BNAS0481();
						}
						if (CommUtil.isNull(levety)) {
							throw DpModuleError.DpstComm.BNAS0551();
						}
						if (CommUtil.isNotNull(lyinwy)) {
							throw DpModuleError.DpstComm.BNAS2051();
						}
						if (CommUtil.isNotNull(isrgdt)) {
							throw DpModuleError.DpstComm.BNAS2052();
						}
						if (CommUtil.isNotNull(lydttp)) {
							throw DpModuleError.DpstComm.BNAS2053();
						}
					}

					// 选择0-否时，利率靠档方式不设置，分层计息方式、是否登记分层明细需要设置。
					if (inwytp == E_YES___.NO) {
						if (CommUtil.isNotNull(intrwy)) {
							throw DpModuleError.DpstComm.BNAS2054();
						}
						if (CommUtil.isNotNull(levety)) {
							throw DpModuleError.DpstProd.BNAS1326();
						}
						if (CommUtil.isNull(lyinwy)) {
							throw DpModuleError.DpstComm.BNAS0483();
						}
						if (CommUtil.isNull(isrgdt)) {
							throw DpModuleError.DpstComm.BNAS0353();
						}
					}

					if (E_YES___.YES == isrgdt && CommUtil.isNull(lydttp)) {
						throw DpModuleError.DpstComm.BNAS0774();
					}
	
					if (E_YES___.NO == isrgdt && CommUtil.isNotNull(lydttp)) {
						throw DpModuleError.DpstComm.BNAS2055();
					}
	
					if (CommUtil.isNull(inammd)) {
						throw DpModuleError.DpstComm.BNAS0642();
					}

					/*
					 * if (CommUtil.isNull(bldyca)) { throw
					 * DpModuleError.DpstComm.E9027("平均余额天数计算方式"); }
					 * 
					 * if (CommUtil.isNull(cycltp)) { throw
					 * DpModuleError.DpstComm.E9027("周期类型"); }
					 */
	
					if (CommUtil.isNull(inprwy)) {
						throw DpModuleError.DpstComm.BNAS2056();
					}
					
					/**
					 * 1.业务小类为活期时，利率重订价方式只能选不重订价、参考利率变化日重订价、按指定周期重订价
					 * 2.业务小类为定期时，不控制
					 */
					if (E_FCFLAG.CURRENT == tblkup_dppbt.getPddpfg()) {
						if (E_IRRTTP.NO != inprwy && E_IRRTTP.CK != inprwy && E_IRRTTP.AZ != inprwy){
							
							throw DpModuleError.DpstComm.BNAS2057();
						}
					}
				
//				if (E_FCFLAG.FIX == tblkup_dppbt.getPddpfg()) {
//					if (E_IRRTTP.NO != inprwy && E_IRRTTP.MT != inprwy && E_IRRTTP.QD != inprwy){
//						
//						throw DpModuleError.DpstComm.E9999("利率重定价方式选择错误，请重新选项");
//					}
//				}

					// 该属性选择1-账户余额时，平均余额天数计算方式和周期类型置灰。该属性选择2-平均余额时，平均余额天数计算方式和周期类型为必选项。
					if (inammd == E_IBAMMD.AVG) {
						if (CommUtil.isNull(bldyca)) {
							throw DpModuleError.DpstComm.BNAS0424();
						}
	
	//					if (CommUtil.isNull(cycltp)) {
	//						throw DpModuleError.DpstComm.E9027("周期类型");
	//					}
					} else {
						if (CommUtil.isNotNull(bldyca)) {
							throw DpModuleError.DpstComm.BNAS2058();
						}
	
	//					if (CommUtil.isNotNull(cycltp)) {
	//						throw DpModuleError.DpstComm.E9999("周期类型不可输");
	//					}
					}
					/*
					 * 该属性选择1-不重定价时，重定价利率处理方式不可选。
					 * 当该属性选择2-参考利率变化日重定价或3-按指定周期重定价时，重定价利率处理方式可选择1-后段调整处理或2-全部调整处理。
					 * 该属性选择4-到期转存重定价时，重定价利率处理方式只能选择1-后段调整处理,2-全部调整处理。
					 * 该属性选择5-升息比较重定价时，重定价利率处理方式只能选择3-前后段分别调整处理
					 */
					if ((inprwy == E_IRRTTP.CK) || (inprwy == E_IRRTTP.AZ)) {
						if (CommUtil.isNull(reprwy)) {
							throw DpModuleError.DpstComm.BNAS2059();
						}
						if (reprwy == E_REPRWY.PART) {
							throw DpModuleError.DpstComm.BNAS2060();
						}
					} else if (inprwy == E_IRRTTP.QD) {
						if (reprwy != E_REPRWY.BACK && reprwy != E_REPRWY.ALL) {
							throw DpModuleError.DpstComm.BNAS2060();
						}
					} else if (inprwy == E_IRRTTP.MT) {
						if (reprwy != E_REPRWY.PART) {
							throw DpModuleError.DpstComm.BNAS2060();
						}
					}
					
				} else {
					
					// 业务要求暂时不控制
					/*// 查看违约部件信息
					KupDppbPartTemp selPartDfir = KupDppbPartTempDao.selectOne_odb1( E_BUSIBI.DEPO, input.getProdcd(), E_PARTCD._CK10, false);
					if (CommUtil.isNotNull(selPartDfir)) {
						if (E_YES___.YES == selPartDfir.getPartfg()) {
							throw DpModuleError.DpstComm.E9999("存款产品违约支取利息利率部件启用时，计息标志只能为1-是");
						}
					}
					
					// 不计息产品其他属性需为空
					if (CommUtil.isNotNull(input.getTxbefg())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，计税标志须为空");
					}
					if (CommUtil.isNotNull(input.getTaxecd())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，税率编号须为空");
					}
					if (CommUtil.isNotNull(input.getTxbebs())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，计息基准天数须为空");
					}
					if (CommUtil.isNotNull(input.getHutxfg())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，舍弃角分计息标志须为空");
					}
					if (CommUtil.isNotNull(input.getTebehz())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，计息频率须为空");
					}
					if (CommUtil.isNotNull(input.getTxbefr())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，结息频率须为空");
					}
					if (CommUtil.isNotNull(input.getIntrcd())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，利率代码须为空");
					}
					if (CommUtil.isNotNull(input.getIncdtp())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，利率代码类型须为空");
					}
					if (CommUtil.isNotNull(input.getIntrdt())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，利率确定日期须为空");
					}
					if (CommUtil.isNotNull(input.getInwytp())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，利率靠档标志须为空");
					}
					if (CommUtil.isNotNull(input.getLevety())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，违约靠档规则须为空");
					}
					if (CommUtil.isNotNull(input.getIntrwy())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，利率靠档方式须为空");
					}
					if (CommUtil.isNotNull(input.getLyinwy())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，分层计息方式须为空");
					}
					if (CommUtil.isNotNull(input.getIsrgdt())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，是否登记分层明细须为空");
					}
					if (CommUtil.isNotNull(input.getLydttp())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，分层明细积数调整方式须为空");
					}
					if (CommUtil.isNotNull(input.getInammd())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，计息金额模式须为空");
					}
					if (CommUtil.isNotNull(input.getBldyca())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，平均余额天数计算方式须为空");
					}
					if (CommUtil.isNotNull(input.getCycltp())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，周期类型须为空");
					}
					if (CommUtil.isNotNull(input.getInprwy())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，利率重定价方式须为空");
					}
					if (CommUtil.isNotNull(input.getInadlv())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，利率调整频率须为空");
					}
					if (CommUtil.isNotNull(input.getReprwy())){
						throw DpModuleError.DpstComm.E9999("计息标志为0-否时，重定价利率处理方式须为空");
					}*/
				}

				// 产品基础属性临时表查询币种
				KupDppbTemp temp = KupDppbTempDao.selectOne_odb1(prodcd, false);

				if (CommUtil.isNull(temp)) {
					throw DpModuleError.DpstComm.BNAS0761();
				}

				String crcycd = temp.getPdcrcy();

				// 判断原记录是否存在
				KupDppbIntrTemp tmp = KupDppbIntrTempDao.selectOne_odb2( prodcd, crcycd, intrtp, false);
				if (CommUtil.isNotNull(tmp)) {
					throw DpModuleError.DpstProd.BNAS1356();
				}
				// 新增纪录
				KupDppbIntrTemp entity = SysUtil.getInstance(KupDppbIntrTemp.class);
				entity.setProdcd(prodcd);
				entity.setCrcycd(crcycd);
				entity.setIntrtp(intrtp);
				entity.setInbefg(inbefg);
				entity.setTxbefg(txbefg);
				entity.setTxbebs(txbebs);
				entity.setHutxfg(hutxfg);
				entity.setTxbefr(txbefr);
				entity.setIntrcd(intrcd);
				entity.setIntrdt(intrdt);
				entity.setLevety(levety);
				entity.setIntrwy(intrwy);
				entity.setIncdtp(incdtp);
				entity.setLyinwy(lyinwy);
				entity.setInammd(inammd);
				entity.setBldyca(bldyca);
				entity.setInprwy(inprwy);
				entity.setInadlv(inadlv);
				entity.setReprwy(reprwy);
				entity.setTaxecd(taxecd);
				entity.setCycltp(cycltp);
				entity.setIsrgdt(isrgdt);
				entity.setLydttp(lydttp);
				entity.setInwytp(inwytp);
				entity.setTebehz(tebehz);
				KupDppbIntrTempDao.insert(entity);
				
			} else {
				throw DpModuleError.DpstComm.BNAS2061();
			}
		} else {
			throw DpModuleError.DpstComm.BNAS2062();
		}
	}
}
