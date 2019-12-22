package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrchTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrchTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPart;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupModePart;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupModePartDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupPart;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupPartDao;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpInrpat;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_ADDMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRENTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODCT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRTRTP;

public class inrpat {

	/**
	 * 
	 * @author songkailei
	 * @param input
	 *            业务大类 业务中类 业务小类 业务细类
	 * @param output
	 * 
	 *            功能： 校验传入部件信息，新增产品部件
	 */

	public static void addinrpat(
			final cn.sunline.ltts.busi.dptran.trans.intf.Inrpat.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Inrpat.Output output) {

		E_ADDMTP addmtp = input.getAddmtp();// 新增模式
		E_BUSIBI busibi = input.getBusibi();// 业务大类
		E_PRODCT prodtp = input.getProdtp();// 业务中类
		E_FCFLAG pddpfg = input.getPddpfg();// 业务小类
		E_DEBTTP debttp = input.getDebttp();// 业务细类
		String prodtx = input.getProdtx();// 产品名称
		E_PRODTG prodtg = input.getProdtg();// 产品性质
		E_PRENTP prentp = input.getPrentp();// 产品启用方式
		String oprocd = input.getOprocd();// 原产品编号 如果新增模式为复制新增则为必输项
		String modeno = input.getModeno();// 模板编号，若果新增模式为模板新增则为必输项
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();// 法人代码
		String timetm = DateTools2.getCurrentTimestamp();// 时间戳
		String prodcd = "";// 产品编号
	//	String mtdate = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
		
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());// 操作网点权限验证

		if (CommUtil.isNull(addmtp)) {
			throw DpModuleError.DpstComm.BNAS2168();
		}
		
		if (CommUtil.isNull(prodtx)) {
			throw DpModuleError.DpstComm.BNAS2169();
		}
		
		// 产品名称唯一检查
		String prodcd1 = DpProductDao.selDppbByProdtx(prodtx, corpno, false);
		if (CommUtil.isNotNull(prodcd1)) {
			throw DpModuleError.DpstComm.BNAS2094();
		}

		// 新增模式为直接新增
		if (input.getAddmtp() == E_ADDMTP.DIRE) {

			if (CommUtil.isNull(busibi)) {
				throw DpModuleError.DpstComm.BNAS1946();
			}

			if (busibi != E_BUSIBI.DEPO) {
				throw DpModuleError.DpstComm.BNAS1962();
			}

			if (CommUtil.isNull(prodtp)) {
				throw DpModuleError.DpstComm.BNAS0255();
			}

			if (CommUtil.isNull(pddpfg)) {
				throw DpModuleError.DpstComm.BNAS0258();
			}

			if (!CommUtil.equals(prodtp.getValue(), pddpfg.getValue()
					.substring(0, prodtp.getValue().length()))) {
				throw DpModuleError.DpstComm.BNAS2170();
			}

			if (CommUtil.isNull(debttp)) {
				throw DpModuleError.DpstComm.BNAS0262();
			}

			if (!CommUtil.equals(pddpfg.getValue(), debttp.getValue()
					.substring(0, pddpfg.getValue().length()))) {
				throw DpModuleError.DpstComm.BNAS2171();
			}
			
			if (CommUtil.isNull(prentp)) {
				throw DpModuleError.DpstComm.BNAS2172();
			}
			
			// 核算代码
			String acctcd = busibi.getValue().concat(debttp.getValue()).concat("000");
			/*Options<IoKnsProdClerInfo> list = SysUtil.getInstance(IoAccountSvcType.class).selKnsProdClerInfo(corpno,acctcd, null);
			if (CommUtil.isNull(list)) {
				throw DpModuleError.DpstComm.BNAS2173();
			} else {
				if (CommUtil.isNull(list.get(0).getProdcd())) {
					throw DpModuleError.DpstComm.BNAS2173();
				}
			}*/
			
			if(CommUtil.isNotNull(prodcd)){
				throw DpModuleError.DpstComm.BNAS2174();
			}
			
			if(CommUtil.isNotNull(modeno)){
				throw DpModuleError.DpstComm.BNAS2174();
			}

			Map<E_PARTCD, E_PARTCD> map = new HashMap<E_PARTCD, E_PARTCD>();// 存放部件编号，用于自身重复性检验
			Map<E_PARTCD, String> map1 = new HashMap<E_PARTCD, String>();// 存放部件编号，部件名称
			List<IoDpInrpat> ioDpInrpats = input.getIoDpInrpat();// 输入模板部件列表
			prodcd = DpPublic.getProdcd(busibi, debttp, CommTools.getBaseRunEnvs().getBusi_org_id());// 产品编号

			KupDppb prodInfo = KupDppbDao.selectOne_odb1(prodcd, false);

			if (CommUtil.isNotNull(prodInfo)) {

				throw DpModuleError.DpstComm.BNAS2175();
			}

			// 获取部件定义信息列表
			List<KupPart> tblKupParts = KupPartDao.selectAll_odb2(busibi,true);

			for (KupPart tblKupPart : tblKupParts) {

				map1.put(tblKupPart.getPartcd(), tblKupPart.getPartna());
			}

			// 判断接收的数据自身是否重复
			for (IoDpInrpat info : ioDpInrpats) {

				E_PARTCD partcd = info.getPartcd();

				// 部件编号校验
				if (CommUtil.isNull(info.getPartcd())) {
					throw DpModuleError.DpstComm.BNAS1949();
				}

				// 启用标志校验
				if (CommUtil.isNull(info.getPartfg())) {
					throw DpModuleError.DpstComm.BNAS1950();
				}

				if (E_PARTCD._CK02 == info.getPartcd()) {
					if (E_YES___.NO == info.getPartfg()) {
						if (E_PRENTP.ASSE == prentp) {
							throw DpModuleError.DpstComm.BNAS2176();
						}
					} else {
						if (E_PRENTP.SALE == prentp) {
							throw DpModuleError.DpstComm.BNAS2177();
						}
					}
				}
				
				if (E_PARTCD._CK01 == info.getPartcd()) {
					if (E_YES___.NO == info.getPartfg()) {
						throw DpModuleError.DpstComm.BNAS2178();
					}
				}
				if (E_PARTCD._CK03 == info.getPartcd()) {
					if (E_YES___.NO == info.getPartfg()) {
						throw DpModuleError.DpstComm.BNAS1952();
					}
				}
				if (E_PARTCD._CK04 == info.getPartcd()) {
					if (E_YES___.NO == info.getPartfg()) {
						throw DpModuleError.DpstComm.BNAS1953();
					}
				}
				if (E_PARTCD._CK06 == info.getPartcd()) {
					if (E_YES___.NO == info.getPartfg()) {
						throw DpModuleError.DpstComm.BNAS1954();
					}
				}
				if (E_PARTCD._CK09 == info.getPartcd()) {
					if (E_YES___.NO == info.getPartfg()) {
						throw DpModuleError.DpstComm.BNAS1955();
					}
				}
				if (E_PARTCD._CK11 == info.getPartcd()) {
					if (E_YES___.NO == info.getPartfg()) {
						throw DpModuleError.DpstComm.BNAS1956();
					}
				}
				map.put(partcd, partcd);

			}

			if (CommUtil.compare(map.size(), input.getIoDpInrpat().size()) != 0) {
				throw DpModuleError.DpstComm.BNAS2179();
			}

			// 传入部件数量与部件定义表部件数据是否相同
			if (CommUtil.compare(map.size(), tblKupParts.size()) != 0) {
			//tblKupParts.size() == 11	
				throw DpModuleError.DpstComm.BNAS2180();
			}

//			// 根据产品编号判断接收部件信息是否与定义表中信息一致
//			for (KupPart tblKupPart : tblKupParts) {
//
//				E_PARTCD spartcd = map.get(tblKupPart.getPartcd());
//
//				if (CommUtil.isNull(spartcd)) {
//					throw DpModuleError.DpstComm.E9999("缺少部件信息");
//				}
//			}
			
			// 检查该产品是否已经新增产品部件
			List<KupDppbPartTemp> tblKupDppbPartTemps = KupDppbPartTempDao.selectAll_odb2(E_BUSIBI.DEPO, prodcd, false);
			if (CommUtil.isNotNull(tblKupDppbPartTemps)) {
				throw DpModuleError.DpstComm.BNAS2181();
			}
			
			// 新增产品部件
			for (IoDpInrpat info : ioDpInrpats) {
				
				KupDppbPartTemp tblKupPart_temp = SysUtil.getInstance(KupDppbPartTemp.class);

				tblKupPart_temp.setBusibi(busibi);// 业务大类
				tblKupPart_temp.setProdtp(prodtp);// 业务中类
				tblKupPart_temp.setPddpfg(pddpfg);// 业务小类
				tblKupPart_temp.setDebttp(debttp);// 业务细类
				tblKupPart_temp.setProdtg(prodtg);// 产品性质
				tblKupPart_temp.setPrentp(prentp);// 产品启用方式
				tblKupPart_temp.setAddmtp(addmtp);// 新增模式
				tblKupPart_temp.setProdcd(prodcd);// 产品编号
				tblKupPart_temp.setPartcd(info.getPartcd());// 部件编号
				tblKupPart_temp.setPartna(map1.get(info.getPartcd()));// 部件名称
				tblKupPart_temp.setPartfg(info.getPartfg());// 部件启用标志

				KupDppbPartTempDao.insert(tblKupPart_temp);
			}

			// 设置输出产品编号
			output.setProdcd(prodcd);
		} else {
			// 新增模式为复制新增
			if (input.getAddmtp() == E_ADDMTP.COPY) {

				if (CommUtil.isNull(oprocd)) {
					throw DpModuleError.DpstComm.BNAS2182();
				}

				if (CommUtil.isNotNull(modeno)) {
					throw DpModuleError.DpstComm.BNAS2183();
				}

				if (CommUtil.isNotNull(busibi)) {
					throw DpModuleError.DpstComm.BNAS2184();
				}

				if (CommUtil.isNotNull(prodtp)) {
					throw DpModuleError.DpstComm.BNAS2185();
				}

				if (CommUtil.isNotNull(pddpfg)) {
					throw DpModuleError.DpstComm.BNAS2186();
				}

				if (CommUtil.isNotNull(debttp)) {
					throw DpModuleError.DpstComm.BNAS2187();
				}

				if (input.getIoDpInrpat().size() != 0) {
					throw DpModuleError.DpstComm.BNAS2188();
				}
				
				if (CommUtil.isNull(prentp)) {
					throw DpModuleError.DpstComm.BNAS2172();
				}

				// 获取原产品编号产品部件的数据
				List<KupDppbPart> tblKupParts = KupDppbPartDao.selectAll_odb3(CommTools.getBaseRunEnvs().getBusi_org_id(),E_BUSIBI.DEPO, oprocd, false);
				if (CommUtil.isNull(tblKupParts)) {
					throw DpModuleError.DpstComm.BNAS2189();
				}

				KupDppbPart tblKupPart = KupDppbPartDao.selectFirst_odb3(CommTools.getBaseRunEnvs().getBusi_org_id(),E_BUSIBI.DEPO, oprocd, false);

				if (CommUtil.isNull(tblKupParts)) {

					throw DpModuleError.DpstComm.BNAS2190();
				}

				String Corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
				busibi = tblKupPart.getBusibi();// 业务大类
				prodtp = tblKupPart.getProdtp();// 业务中类
				pddpfg = tblKupPart.getPddpfg();// 业务小类
				debttp = tblKupPart.getDebttp();// 业务细类
				prodcd = DpPublic.getProdcd(busibi, debttp, Corpno);// 产品编号
				
				// 检查输入产品启用方式与原产品启用方式是否一致
				if (tblKupPart.getPrentp() != prentp) {
					throw DpModuleError.DpstComm.BNAS2191();
				}
				
				// 判断产品编号是否已在基础属性正式表中存在
				KupDppb prodInfo = KupDppbDao.selectOne_odb1(prodcd, false);

				if (CommUtil.isNotNull(prodInfo)) {

					throw DpModuleError.DpstComm.BNAS2175();
				}

				for (KupDppbPart KupPart : tblKupParts) {

					KupDppbPartTemp tblKupPart1 = SysUtil.getInstance(KupDppbPartTemp.class);

					tblKupPart1.setBusibi(busibi);// 业务大类
					tblKupPart1.setProdtp(prodtp);// 业务中类
					tblKupPart1.setPddpfg(pddpfg);// 业务小类
					tblKupPart1.setDebttp(debttp);// 业务细类
					tblKupPart1.setProdtg(prodtg);// 产品性质
					tblKupPart1.setPrentp(prentp);// 产品启用方式
					tblKupPart1.setAddmtp(addmtp);// 新增模式
					tblKupPart1.setProdcd(prodcd);// 产品编号
					tblKupPart1.setPartcd(KupPart.getPartcd());// 部件编号
					tblKupPart1.setPartna(KupPart.getPartna());// 部件名称
					tblKupPart1.setPartfg(KupPart.getPartfg());// 部件启用标志

					//list.add(tblKupPart1);// 将信息添加入list
					KupDppbPartTempDao.insert(tblKupPart1);
				}
				
				/** 复制产品信息*/
				// 基础部件
				DpProductDao.insKupDppbinfo("kup_dppb_temp", "kup_dppb", prodcd, oprocd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				
				// 更新产品名称、状态、录入编号
				DpProductDao.updKupDppbTempProdst(prodcd, null, E_PRODST.INPUT, prodtx,timetm);
				
				KupDppbTemp tblKupDppbT = KupDppbTempDao.selectOne_odb1(prodcd, true);
				
				// 开户控制部件
				DpProductDao.insKupDppbCustbByCorpno("kup_dppb_cust_temp", "kup_dppb_cust", prodcd, oprocd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				
				// 机构控制部件
				DpProductDao.insKupDppbBrchByCorpno("kup_dppb_brch_temp", "kup_dppb_brch", prodcd, oprocd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				List<KupDppbBrchTemp> tblKupBrchTs = DpProductDao.selKupDppbBrchTempByProdcd(prodcd, false);
				for (KupDppbBrchTemp tblKupBrchT : tblKupBrchTs) {
					tblKupBrchT.setProdcd(prodcd);// 产品编号
					tblKupBrchT.setEfctdt(tblKupDppbT.getEfctdt());// 生效日期
					tblKupBrchT.setInefdt(tblKupDppbT.getInefdt());// 失效日期

					KupDppbBrchTempDao.updateOne_odb2(tblKupBrchT);
				}
				
				// 存期控制部件
				DpProductDao.insKupDppbTermByCorpno("kup_dppb_term_temp", "kup_dppb_term", prodcd, oprocd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				
				// 存入控制部件
				DpProductDao.insKupDppbPostByCorpno("kup_dppb_post_temp", "kup_dppb_post", prodcd, oprocd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				
				// 支取控制部件
				DpProductDao.insKupDppbDrawByCorpno("kup_dppb_draw_temp", "kup_dppb_draw", prodcd, oprocd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				
				// 到期控制部件
				DpProductDao.insKupDppbMatuBycorpno("kup_dppb_matu_temp", "kup_dppb_matu", prodcd, oprocd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				
				
				// 违约利息利率部件
				DpProductDao.insKupDppbDfirByCorpno("kup_dppb_dfir_temp", "kup_dppb_dfir", prodcd, oprocd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				
				// 存入计划部件
				DpProductDao.insKupDppbPostPlanByCorpno("kup_dppb_popl_temp", "kup_dppb_post_plan", prodcd, oprocd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				
				// 支取计划部件
				DpProductDao.insKupDppbDrawPlanByCorpno("kup_dppb_drpl_temp", "kup_dppb_draw_plan", prodcd, oprocd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				
				// 利息利率部件
				DpProductDao.insKupDppbIntrByCorpno("kup_dppb_intr_temp", "kup_dppb_intr", prodcd, oprocd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				
				// 核算部件
				DpProductDao.insKupDppbAcctByCorpno("kup_dppb_acct_temp", "kup_dppb_acct", prodcd, oprocd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				
				// 产品账户类型控制表
				DpProductDao.insKupDppbActpByCorpno("kup_dppb_actp_temp", "kup_dppb_actp", prodcd, oprocd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				
				// 产品附加属性表
				DpProductDao.insKupDppbAddtByCorpno("kup_dppb_addt_temp", "kup_dppb_addt", prodcd, oprocd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				
				// 产品部件表
				DpProductDao.insKupDppbPartByCorpno("kup_dppb_part_temp", "kup_dppb_part", prodcd, prodcd, timetm,CommTools.getBaseRunEnvs().getBusi_org_id());
				
				// 设置输出产品编号
				output.setProdcd(prodcd);
			} else {

				// 模板新增
				if (CommUtil.isNull(modeno)) {
					throw DpModuleError.DpstComm.BNAS1982();
				}

				if (CommUtil.isNotNull(prodcd)) {
					throw DpModuleError.DpstComm.BNAS2192();
				}

				if (CommUtil.isNull(busibi)) {
					throw DpModuleError.DpstComm.BNAS1946();
				}

				if (busibi != E_BUSIBI.DEPO) {
					throw DpModuleError.DpstComm.BNAS1962();
				}

				if (CommUtil.isNull(prodtp)) {
					throw DpModuleError.DpstComm.BNAS0255();
				}

				if (CommUtil.isNull(pddpfg)) {
					throw DpModuleError.DpstComm.BNAS0258();
				}
				
				if (CommUtil.isNull(prentp)) {
					throw DpModuleError.DpstComm.BNAS2172();
				}

				if (!CommUtil.equals(prodtp.getValue(), pddpfg.getValue()
						.substring(0, prodtp.getValue().length()))) {
					throw DpModuleError.DpstComm.BNAS2170();
				}

				if (CommUtil.isNull(debttp)) {
					throw DpModuleError.DpstComm.BNAS0262();
				}

				if (!CommUtil.equals(pddpfg.getValue(), debttp.getValue()
						.substring(0, pddpfg.getValue().length()))) {
					throw DpModuleError.DpstComm.BNAS2171();
				}

				if (input.getIoDpInrpat().size() != 0) {
					throw DpModuleError.DpstComm.BNAS2188();
				}

				// 获取模板部件列表
				List<KupModePart> tblKupModeParts = KupModePartDao.selectAll_odb2(busibi, modeno, true);
				prodcd = DpPublic.getProdcd(busibi, debttp, CommTools.getBaseRunEnvs().getBusi_org_id());// 产品编号

				// 判断产品编号是否一在基础属性表中存在
				KupDppb prodInfo = KupDppbDao.selectOne_odb1(prodcd, false);

				if (CommUtil.isNotNull(prodInfo)) {

					throw DpModuleError.DpstComm.BNAS2175();
				}

				if (CommUtil.isNull(tblKupModeParts)) {
					throw DpModuleError.DpstComm.BNAS2193();
				}
				for (KupModePart KupModePart : tblKupModeParts) {

					KupDppbPartTemp tblKupPart_temp1 = SysUtil.getInstance(KupDppbPartTemp.class);

					tblKupPart_temp1.setBusibi(busibi);// 业务大类
					tblKupPart_temp1.setProdtp(prodtp);// 业务中类
					tblKupPart_temp1.setPddpfg(pddpfg);// 业务小类
					tblKupPart_temp1.setDebttp(debttp);// 业务细类
					tblKupPart_temp1.setProdtg(prodtg);// 产品性质
					tblKupPart_temp1.setPrentp(prentp);// 产品启用方式
					tblKupPart_temp1.setAddmtp(addmtp);// 模板名称
					tblKupPart_temp1.setProdcd(prodcd);// 产品编号
					tblKupPart_temp1.setPartcd(KupModePart.getPartcd());// 部件编号
					tblKupPart_temp1.setPartna(KupModePart.getPartna());// 部件名称
					tblKupPart_temp1.setPartfg(KupModePart.getPartfg());// 部件启用标志

					//list.add(tblKupPart_temp1);// 将信息添加入list
					KupDppbPartTempDao.insert(tblKupPart_temp1);
				}

				// 批量新增记录
				//DaoUtil.insertBatch(KupDppbPartTemp.class, list);
				// 设置输出产品编号
				output.setProdcd(prodcd);
			}
		}
		
		/**新增基础部件信息*/
		if (input.getAddmtp() != E_ADDMTP.COPY) {
			//产品启用方式1-直接装配启用时，产品性质只能选1-通用产品
			if (E_PRENTP.ASSE == prentp) {
				if (E_PRODTG.CURREN != prodtg ) {
					throw DpModuleError.DpstComm.BNAS2117();
				}
			}
			
			// 产品启用方式为2-销售工厂启用时，产品性质置灰
			if (E_PRENTP.SALE == prentp) {
				if (CommUtil.isNotNull(prodtg)) {
					throw DpModuleError.DpstComm.BNAS2118();
				}
			}
			
			//判断原记录是否存在 产品基础属性临时表
	        KupDppbTemp tmp = KupDppbTempDao.selectFirst_odb2(prodcd, false);
			if( CommUtil.isNotNull(tmp)){
				throw DpModuleError.DpstComm.BNAS2194();
			}
			
			//判断原记录是否存在 产品基础属性临时表
			List<KupDppb> tmpa = KupDppbDao.selectAll_odb4(prodcd, false);
			if( CommUtil.isNotNull(tmpa) && tmpa.size() != 0){
				throw DpModuleError.DpstComm.BNAS2194();
			}
			
			//插入新纪录 产品基础属性临时表
			KupDppbTemp entity = SysUtil.getInstance(KupDppbTemp.class);
			entity.setProdcd(prodcd);
			entity.setProdtx(prodtx);		
			entity.setProdtp(prodtp);
			entity.setPddpfg(pddpfg);
			entity.setDebttp(debttp);
			entity.setProdtg(prodtg);
			entity.setPrentp(prentp);
			entity.setMgindy(null);
			entity.setProdst(E_PRODST.INPUT);//产品状态为录入
			KupDppbTempDao.insert(entity);
		}
		
		// 产品操作柜员登记
		SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.DEPO, prodcd, E_PRTRTP.ADD);
	}

}
