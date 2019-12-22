package cn.sunline.ltts.busi.ca.base;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.namedsql.EacctMainDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KcdProd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccsDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbWhit;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbWhitDao;
import cn.sunline.ltts.busi.dp.client.CapitalTransDeal;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAssetInfoList;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaClientLedgerOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaPdtlInfoList;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaProdInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.errors.CaError.Eacct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CAPITP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_OPACWY;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_PDTLTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_VAILST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTP;

/**
 * <p>
 * 文件功能说明：卡电子账户模块工具类
 * </p>
 * 
 * @author renjinghua
 * 
 */
public class CaTools {


	/**
	 * <p>
	 * 生成卡号
	 * </p>
	 * 
	 * @author renjinghua
	 *         <p>
	 *         <li>2015年3月26日</li>
	 *         </p>
	 * 
	 * @return
	 */
	public static String genCardno(String cardProd) {
		if (CommUtil.isNull(cardProd))
			throw Eacct.BNAS1424();
		int len = 16;
		StringBuffer cardno = new StringBuffer(len);

		//kcd_prod prod = Kcd_prodDao.selectOne_odb1(cardProd, false);
		
		String corpno = BusiTools.getCenterCorpno();
		KcdProd prod = EacctMainDao.selKcdProd(cardProd, corpno, false);
		
		if (CommUtil.isNull(prod))
			throw Eacct.BNAS1425();

		if (E_PRODST.NORMAL != prod.getProdst()) {
			throw Eacct.BNAS1426();
		}

		if (CommUtil.isNull(prod.getCardbn())) {
			throw Eacct.BNAS1427();
		}
		cardno.append(prod.getCardbn()).append(
				BusiTools.getSequence("card_no", len
						- prod.getCardbn().length()));

		return cardno.toString();
	}
	
	/**
	 * 
	 * @author renjinghua
	 * 		<p>
	 *	    <li>2016年6月23日-下午4:18:13<li>
	 *      <li>功能描述：根据规则字符串生成卡号，如手机号</li>
	 *      <li>规则：先查询规则字符串对应最大序号，如果序号用完，则使用正常排序生成账号</li>
	 *      </p>
	 * moified by xieqq 2017-5-25: 卡产品号直接改为卡bin号
	 * @param kabin 卡bin
	 * @param ruleStr 生成账号规则字符串
	 * @return opacwy 开户方式
	 * @return 卡号
	 */
	public static String genCardno(String kabin, String ruleStr, E_OPACWY opacwy, String idtfno, E_IDTFTP idtftp) {
		
		//String timetm = BusiTools.getBusiRunEnvs().getTrantm();// 当前交易时间
		String seqnum = "";// 生成卡号的中间12位序号
		
		if (CommUtil.isNull(kabin))
			throw Eacct.BNAS1427();
		
		int len = 19;
		StringBuffer cardno = new StringBuffer();
		
		// 白名单功能开关
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("WhiteSwitch", "%", "%", "%", false);
		if (CommUtil.equals(tblKnpParameter.getParm_value1(), "Y")) {
			
			KnbWhit tblKnbWhit = KnbWhitDao.selectFirst_odb1(idtfno, idtftp, false); 
			if (CommUtil.isNotNull(tblKnbWhit)
					&& tblKnbWhit.getVailst() != E_VAILST.USED
					&& tblKnbWhit.getVailst() != E_VAILST.APPOINTMENT) {
				cardno.append(kabin).append(tblKnbWhit.getSeqnum());
				// 生成最后一位校验位 
				String checkNo = countParityBit(cardno);
				cardno.append(checkNo);
				// 更新白名单为已使用
				tblKnbWhit.setVailst(E_VAILST.USED);
				KnbWhitDao.update_odb1(tblKnbWhit);
				
			} else {
				
				//如果是单笔开户，则先按规则字符(手机号)生成，用完再按顺序号生成
				if(E_OPACWY.SINGLE == opacwy){
					
					//查询最大序号,根据卡bin+规则字符串查询对应最大卡号，取最大序号，生成下一个卡号
					String cdrlno = kabin + ruleStr;
					String maxCardno = CaDao.selMaxCardnoByCardno(cdrlno, false);
					Integer sequno = 0;
					// 初始化为0，已经存在的序号加1
					if (CommUtil.isNotNull(maxCardno)) {
						sequno = ConvertUtil.toInteger(maxCardno.substring(
								maxCardno.length() - 2,
								maxCardno.length() - 1));
						sequno = sequno + 1;
					}
					
					while (true) {
						// 根据手机和顺序号组成的序号查询是否于存在白名单中
						seqnum = ruleStr + sequno;
						
						// 查询是否存在白名单中，若不存在，则跳出循环
						KnbWhit tblKnbWhit1 = KnbWhitDao.selectOne_odb2(
								seqnum, false);
						if (CommUtil.isNull(tblKnbWhit1)) {
							break;
						} else {
							sequno = sequno + 1;
						}
						
					}
					
					//序号超过9，则需要根据另外规则生成卡号，按照顺序号排序
					if(CommUtil.compare(sequno, 9) <= 0){
						cardno.append(kabin).append(ruleStr).append(sequno.toString());
						
						//生成最后一位校验位 
						String checkNo = countParityBit(cardno);
						cardno.append(checkNo);
						
					}else{
						
/*						// 根据另外规则生成卡号时，先去取电子账户卡号复用表中的数据
						knb_recl tblKnbRecl = CaDao.selKnbReclByTel(ruleStr,
								E_ENABST.NOTENABLED, false);
						if (CommUtil.isNotNull(tblKnbRecl)) {
							cardno.delete(0, cardno.length()).append(
									tblKnbRecl.getCardno());
							// 更新卡号启用状态为已重启
							CaDao.updKnbReclEnabst(E_ENABST.ENABLED, tblKnbRecl.getCardno(), trandt, timetm,mtdate);
						} else {
							knb_recl tblKnbRecl1 = CaDao.selKnbReclInfo(
									E_ENABST.NOTENABLED, false);
							if (CommUtil.isNotNull(tblKnbRecl1)) {
								cardno.delete(0, cardno.length()).append(
										tblKnbRecl1.getCardno());
								// 更新卡号启用状态为已重启
								CaDao.updKnbReclEnabst(E_ENABST.ENABLED, tblKnbRecl1.getCardno(), trandt, timetm,mtdate);
							} else {*/

						while (true) {

							seqnum = BusiTools.getSequence("card_no", len
									- kabin.length() - 1);

							// 查询取出来的卡号序号是否于存在白名单中
							KnbWhit tblKnbWhit2 = KnbWhitDao.selectOne_odb2(
									seqnum, false);

							if (CommUtil.isNull(tblKnbWhit2)) {
								break;
							}
						}

						cardno.append(kabin).append(seqnum);

						// 生成最后一位校验位
						String checkNo = countParityBit(cardno);
						cardno.append(checkNo);

					}

				}
				// 如果是批量开户，则直接使用顺序号排序
				else {
					
/*					// 根据另外规则生成卡号时，先去取电子账户卡号复用表中的数据
					knb_recl tblKnbRecl = CaDao.selKnbReclInfo(E_ENABST.NOTENABLED,
							false);
					if (CommUtil.isNotNull(tblKnbRecl)) {
						cardno.delete(0, cardno.length()).append(
								tblKnbRecl.getCardno());
						// 更新卡号启用状态为已重启
						CaDao.updKnbReclEnabst(E_ENABST.ENABLED, tblKnbRecl.getCardno(), trandt, timetm,mtdate);
					} else {*/
					
					while (true) {

						seqnum = BusiTools.getSequence("card_no", len
								- kabin.length() - 1);

						// 查询取出来的卡号序号是否于存在白名单中
						KnbWhit tblKnbWhit3 = KnbWhitDao.selectOne_odb2(
								seqnum, false);

						if (CommUtil.isNull(tblKnbWhit3)) {
							break;
						}
					}
					
					cardno.append(kabin).append(seqnum);
					// 生成最后一位校验位
					String checkNo = countParityBit(cardno);
					cardno.append(checkNo);

				}
			}
		} else {
			//如果是单笔开户，则先按规则字符(手机号)生成，用完再按顺序号生成
			if(E_OPACWY.SINGLE == opacwy){
				
				//查询最大序号,根据卡bin+规则字符串查询对应最大卡号，取最大序号，生成下一个卡号
				String cdrlno = kabin + ruleStr;
				String maxCardno = CaDao.selMaxCardnoByCardno(cdrlno, false);
				
				//初始化为0，已经存在的序号加1
				Integer sequno = 0;
				if(CommUtil.isNotNull(maxCardno)){			
					sequno = ConvertUtil.toInteger(maxCardno.substring(maxCardno.length()-2, maxCardno.length() - 1));
					sequno = sequno + 1;
				}
				
				//序号超过9，则需要根据另外规则生成卡号，按照顺序号排序
				if(CommUtil.compare(sequno, 9) <= 0){
					cardno.append(kabin).append(ruleStr).append(sequno.toString());
					
					//生成最后一位校验位 
					String checkNo = countParityBit(cardno);
					cardno.append(checkNo);
				}else{
					
					/*// 根据另外规则生成卡号时，先去取电子账户卡号复用表中的数据
					knb_recl tblKnbRecl = CaDao.selKnbReclByTel(ruleStr,
							E_ENABST.NOTENABLED, false);
					if (CommUtil.isNotNull(tblKnbRecl)) {
						cardno.delete(0, cardno.length()).append(
								tblKnbRecl.getCardno());
						// 更新卡号启用状态为已重启
						CaDao.updKnbReclEnabst(E_ENABST.ENABLED, tblKnbRecl.getCardno(), trandt, timetm,mtdate);
					} else {
						knb_recl tblKnbRecl1 = CaDao.selKnbReclInfo(
								E_ENABST.NOTENABLED, false);
						if (CommUtil.isNotNull(tblKnbRecl1)) {
							cardno.delete(0, cardno.length()).append(
									tblKnbRecl1.getCardno());
							// 更新卡号启用状态为已重启
							CaDao.updKnbReclEnabst(E_ENABST.ENABLED, tblKnbRecl1.getCardno(), trandt, timetm,mtdate);
						} else {*/
					
					cardno.append(kabin).append(
							BusiTools.getSequence("card_no", len
									- kabin.length() - 1));

					// 生成最后一位校验位
					String checkNo = countParityBit(cardno);
					cardno.append(checkNo);

				}
				
			}
			// 如果是批量开户，则直接使用顺序号排序
			else {
				
			/*	// 根据另外规则生成卡号时，先去取电子账户卡号复用表中的数据
				knb_recl tblKnbRecl = CaDao.selKnbReclInfo(E_ENABST.NOTENABLED,
						false);
				if (CommUtil.isNotNull(tblKnbRecl)) {
					cardno.delete(0, cardno.length()).append(
							tblKnbRecl.getCardno());
					// 更新卡号启用状态为已重启
					CaDao.updKnbReclEnabst(E_ENABST.ENABLED, tblKnbRecl.getCardno(), trandt, timetm,mtdate);
				} else {*/
				
				cardno.append(kabin).append(
						BusiTools.getSequence("card_no", len
								- kabin.length() - 1));
				// 生成最后一位校验位
				String checkNo = countParityBit(cardno);
				cardno.append(checkNo);

			}
		}
		
		return cardno.toString();
	}

	/**
	 * 根据内部卡号获得电子账号 找不到数据时返回空
	 * 
	 * @param cardno
	 * @return
	 */
	public static String getCustacByCardno(String cardno) {

		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(cardno, false);

		if (CommUtil.isNotNull(tblKnaAcdc)) {
			return tblKnaAcdc.getCustac();
		} else {
			throw CaError.Eacct.BNAS1428(cardno);
		}
	}
	
	/**
	 * 根据内部卡号获得账户状态正常电子账号 找不到数据时返回空
	 * 
	 * @param cardno
	 * @return
	 */
	public static String getCustacByCardnoCheckStatu(String cardno) {

		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(cardno, false);
		if (CommUtil.isNotNull(tblKnaAcdc)) {
		    KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(tblKnaAcdc.getCustac(), false);
			  if(tblKnaCust.getAcctst()!=E_ACCTST.NORMAL){
			      throw CaError.Eacct.E0001("该电子账户[" + cardno + "]异常");
			  }else{
				  return tblKnaCust.getCustac();
			  }
             
		} else {
			throw CaError.Eacct.E0001("查询卡号[" + cardno + "]对应的电子账号不存在。");
		}
	}

	/**
	 * 获取电子账号与负债账户关联信息
	 * 
	 * @param custac
	 *            电子账户
	 * @param prodcd
	 *            产品号
	 * @param prodtp
	 *            产品种类
	 * @param crcycd
	 *            币种
	 * @param csextf
	 *            钞汇标志
	 * @return
	 */
	public static KnaAccs getAcctno(String custac, String prodcd,
			E_PRODTP prodtp, String crcycd, E_CSEXTG csextg) {
		if (CommUtil.isNull(custac)) {
			throw DpModuleError.DpstAcct.BNAS0311();
		}
		if (CommUtil.isNull(prodcd)) {
			throw CaError.Eacct.BNAS1429();
		}
		if (CommUtil.isNull(prodtp)) {
			throw CaError.Eacct.BNAS1430();
		}
		if (CommUtil.isNull(crcycd)) {
			throw CaError.Eacct.BNAS1431();
		}
		if (CommUtil.isNull(csextg)) {
			throw CaError.Eacct.BNAS1432();
		}
		KnaAccs accs = KnaAccsDao.selectFirst_odb6(custac, crcycd, csextg,
				prodtp, prodcd, E_DPACST.NORMAL, false);
		if (CommUtil.isNull(accs)) {
			throw CaError.Eacct.BNAS1433();
		}
		return accs;
	}

	/**
	 * 获取默认活期产品账户信息
	 * 
	 * @param custac
	 * @param crcycd
	 * @return
	 */
	public static KnaAccs getAcctAccs(String custac, String crcycd) {

	    /*
		KnpAcctType type = EacctMainDao.selKnpAcctType(BusiTools.getCenterCorpno(), E_CUSACT.ACC, true);//暂时默认为电子账户
		return getAcctno(custac, type.getBaprcd(), E_PRODTP.DEPO, crcycd,
				E_CSEXTG.EXCHANGE);
		*/
		
		KnaAcct knaAcctDO = CapitalTransDeal.getSettKnaAcctAc(custac);
		return KnaAccsDao.selectOne_odb2(knaAcctDO.getAcctno(), true);
	}

	/**
	 * 获取活期账户
	 * 
	 * @param custac
	 *            电子账号
	 * @param crcycd
	 *            币种
	 * @return
	 */
	public static String getAcctno(String custac, String crcycd) {

		return getAcctAccs(custac, crcycd).getAcctno();
	}

	/**
	 * 获取活期账户子账户
	 * 
	 * @param custac
	 *            电子账号
	 * @param crcycd
	 *            币种
	 * @return
	 */
	public static String getSubsac(String custac, String crcycd) {

		return getAcctAccs(custac, crcycd).getSubsac();
	}

	/**
	 * 获取产品信息列表（亲情钱包明细）
	 * 
	 */
	public static List<IoCaProdInfo> CaTools_getProdInfoList(String custac,
			String crcycd) {

		// 亲情钱包明细
		List<IoCaProdInfo> listInfo = CaDao.selProdInfoListByCustac(custac,
				false);

		return listInfo;
	}

	/**
	 * 
	 * @param custac
	 * 			电子账号ID
	 * @param crcycd
	 * 			币种
	 * @return
	 * 		资产产品类型列表
	 */
	public static List<IoCaPdtlInfoList> CaTools_getPdtlInfoList(String custac, String crcycd) {

		// 资产产品类型列表
		List<IoCaPdtlInfoList> listInfos = CaDao.selPdtlInfoListByCustac(custac, false);
		
		// 亲情钱包产品列表
		List<IoCaProdInfo> wacctinfo = CaTools_getProdInfoList(custac, crcycd);
		
		// 添加列表
		for (IoCaPdtlInfoList listInfo : listInfos) {
			if (listInfo.getPdtltp() == E_PDTLTP.FAMILYAT) {
				for (IoCaProdInfo ProdInfo : wacctinfo) {
					listInfo.getProdInfoLIst().add(ProdInfo);
				}
			}
		}
		
		// 活期存款
		List<IoCaProdInfo> CurrentInfo = CaDao.selCurrentPdtlListByCustac(custac, crcycd, false);
		if(CommUtil.isNotNull(CurrentInfo) && CurrentInfo.size() > 0){
			
			// 添加列表
			for (IoCaPdtlInfoList listInfo : listInfos) {
				if (listInfo.getPdtltp() == E_PDTLTP.DEMANDDP) {
					for (IoCaProdInfo ProdInfo : CurrentInfo) {
						listInfo.getProdInfoLIst().add(ProdInfo);
					}
				}
			}
		}
			
		// 定期存款
		List<IoCaProdInfo> FxacInfo = CaDao.selFxacPdtlListByCustac(custac, crcycd, false);
		if(CommUtil.isNotNull(FxacInfo) && FxacInfo.size() > 0){
			// 添加列表
			for (IoCaPdtlInfoList listInfo : listInfos) {
				if (listInfo.getPdtltp() == E_PDTLTP.TIMEDP) {
					for (IoCaProdInfo ProdInfo : FxacInfo) {
						listInfo.getProdInfoLIst().add(ProdInfo);
					}
				}
			}
		}
		
		// 理财
		List<IoCaProdInfo> FinaInfo = CaDao.selFinaPdtlListByCustac(custac, crcycd, false);
		if(CommUtil.isNotNull(FinaInfo) && FinaInfo.size() > 0){
			// 添加列表
			for (IoCaPdtlInfoList listInfo : listInfos) {
				if (listInfo.getPdtltp() == E_PDTLTP.MANAGE) {
					for (IoCaProdInfo ProdInfo : FinaInfo) {
						listInfo.getProdInfoLIst().add(ProdInfo);
					}
				}
			}
		}
		return listInfos;

	}

	/**
	 * 获取电子账户资产信息列表
	 * 
	 */
	public static List<IoCaAssetInfoList> CaTools_getAcctInfoList(
			String custac,
			String crcycd) {

		BigDecimal capiam = BigDecimal.ZERO;// 活期存款金额
		BigDecimal asstam = BigDecimal.ZERO;// 投资理财金额
		// 资产信息列表
		List<IoCaAssetInfoList> listInfo = new ArrayList<IoCaAssetInfoList>();

		IoCaAssetInfoList assInfo1 = SysUtil
				.getInstance(IoCaAssetInfoList.class);
		IoCaAssetInfoList assInfo2 = SysUtil
				.getInstance(IoCaAssetInfoList.class);

		List<IoCaPdtlInfoList> pdtls = CaTools_getPdtlInfoList(custac, crcycd);

		for (IoCaPdtlInfoList pdtl : pdtls) {
			// 活期
			if (pdtl.getPdtltp() == E_PDTLTP.DEMANDDP) {
				capiam = capiam.add(pdtl.getPdtlam());
				assInfo1.getPdtlInfoList().add(pdtl);
			}
			// 定期
			if (pdtl.getPdtltp() == E_PDTLTP.TIMEDP) {
				capiam = capiam.add(pdtl.getPdtlam());
				assInfo1.getPdtlInfoList().add(pdtl);
			}
			// 钱包账户
			if (pdtl.getPdtltp() == E_PDTLTP.WALLETAT) {
				capiam = capiam.add(pdtl.getPdtlam());
				assInfo1.getPdtlInfoList().add(pdtl);
			}
			// 电子现金
			if (pdtl.getPdtltp() == E_PDTLTP.ECASHAT) {
				capiam = capiam.add(pdtl.getPdtlam());
				assInfo1.getPdtlInfoList().add(pdtl);
			}
			// 亲情钱包
			if (pdtl.getPdtltp() == E_PDTLTP.FAMILYAT) {
				capiam = capiam.add(pdtl.getPdtlam());
				assInfo1.getPdtlInfoList().add(pdtl);
			}
			// 理财
			if (pdtl.getPdtltp() == E_PDTLTP.MANAGE) {
				asstam = asstam.add(pdtl.getPdtlam());
				assInfo2.getPdtlInfoList().add(pdtl);
			}
			// 基金
			if (pdtl.getPdtltp() == E_PDTLTP.FUND) {
				asstam = asstam.add(pdtl.getPdtlam());
				assInfo2.getPdtlInfoList().add(pdtl);
			}
			// 重金属
			if (pdtl.getPdtltp() == E_PDTLTP.PRECIOUS) {
				asstam = asstam.add(pdtl.getPdtlam());
				assInfo2.getPdtlInfoList().add(pdtl);
			}

		}
		assInfo1.setCapitp(E_CAPITP.CURRENT);
		assInfo1.setCapiam(capiam);

		assInfo2.setCapitp(E_CAPITP.MANAGE);
		assInfo2.setCapiam(asstam);

		listInfo.add(assInfo1);
		listInfo.add(assInfo2);

		return listInfo;

	}

	/**
	 * 根据电子账号查询分户账信息
	 * 
	 */
	public static IoCaClientLedgerOut qryClientLedgerByCustac(String cardno) {

		BigDecimal acctbl = BigDecimal.ZERO;// 可支取金额
		BigDecimal capiam = BigDecimal.ZERO;// 总资产
		
		// 获取卡号对应的电子账号
		KnaAcdc tblkna_acdc = KnaAcdcDao.selectOne_odb2(cardno, false);
		if (CommUtil.isNull(tblkna_acdc) || E_DPACST.NORMAL != tblkna_acdc.getStatus()) {
			
			throw CaError.Eacct.BNAS0752();
		}
		
		// 获取电子账号分类
		E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(tblkna_acdc.getCustac());
		
		//查询电子账户状态
		E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(tblkna_acdc.getCustac());
		
		// 电子账户状态字查询
		IoDpAcStatusWord cplGetAcStWord = SysUtil.getInstance(
				IoDpFrozSvcType.class).getAcStatusWord(tblkna_acdc.getCustac());
		
		IoCaClientLedgerOut infoOut = null;
		
		if (E_ACCATP.WALLET == accatp) {

			// 获取三类户客户端分户账查询信息
			infoOut = CaDao.selClientLedgerByCardno(cardno, E_ACSETP.MA, false);
			if (CommUtil.isNull(infoOut)) {
				throw CaError.Eacct.BNAS1091();
			}

		} else {

			// 获取一、二类户客户端分户账查询信息
			infoOut = CaDao.selClientLedgerByCardno(cardno, E_ACSETP.SA, false);
			if (CommUtil.isNull(infoOut)) {
				throw CaError.Eacct.BNAS1091();
			}
		}
/*		// 查询可支取余额
		DpAcctSvcType carry = SysUtil.getInstance(DpAcctSvcType.class);
		acctbl = carry.getProductBal(infoOut.getCustac(), infoOut.getCrcycd());*/
		
		
		// 可用余额
		acctbl = SysUtil.getInstance(DpAcctSvcType.class)
				.getAcctaAvaBal(infoOut.getCustac(), infoOut.getAcctno(),
						infoOut.getCrcycd(), E_YES___.YES, E_YES___.NO);
		
		/*// 获取转存签约明细信息
		IoCaKnaSignDetl cplkna_sign_detl = SysUtil.getInstance(IoCaSevQryTableInfo.class)
				.kna_sign_detl_selectFirst_odb2(infoOut.getAcctno(), E_SIGNST.QY, false);
		
		// 存在转存签约明细信息则取资金池可用余额
		if (CommUtil.isNotNull(cplkna_sign_detl)) {
			acctbl = SysUtil.getInstance(DpAcctSvcType.class).getProductBal(infoOut.getCustac(), infoOut.getCrcycd(), false);
		} else {
			// 其他取账户余额,正常的支取交易排除冻结金额
			acctbl = SysUtil.getInstance(DpAcctSvcType.class).getOnlnblForFrozbl(infoOut.getAcctno(), false);
		}*/
				
		List<IoCaAssetInfoList> assetInfos = CaTools.CaTools_getAcctInfoList(infoOut.getCustac(), infoOut.getCrcycd());

		for (IoCaAssetInfoList assetInfo : assetInfos) {
			capiam = capiam.add(assetInfo.getCapiam());
			infoOut.getAcctInfoList().add(assetInfo);
		}
		
		// 面签标识查询
		E_YES___ facesg = EacctMainDao.selFacesgByCustac(infoOut.getCustac(), true);
		
		infoOut.setAcctam(capiam);
		infoOut.setAcctbl(acctbl);
		infoOut.setAcctst(cuacst);// 更新为客户化状态
		infoOut.setAccttp(accatp);// 账户分类
		infoOut.setAcstsz(cplGetAcStWord.getAcstsz());// 账户状态字
		infoOut.setFacesg(facesg);// 面签标识

		return infoOut;
	}

	/**
	 * 计算卡号最后一位校验数
	 */
	public static String countParityBit(StringBuffer cardno) {
		/*
		 * Luhn 计算模 10“隔位 2 倍加”校验数的公式 计算步骤如下： 
		 * 步骤1：从右边第1个数字（低序）开始每隔一位乘以2。
		 * 步骤2：把在步骤1中获得的乘积的各位数字与原号码中未乘2的各位数字相加。
		 * 步骤3：从邻近的较高的一个以0结尾的数中减去步骤2中所得到的总和[这相当于求这个总和的低
		 * 位数字（个位数）的“10的补数”]。如果在步骤2得到的总和是以零结尾的数（如30、40等等），则 校验数字就是零
		 */
		int a = 0;
		String sCardno = cardno.toString().substring(5);
		int ch[] = new int[sCardno.length()];
		for (int i = 0; i < sCardno.length(); i++) {
			ch[i] = Integer.parseInt(sCardno.substring(sCardno.length() - i - 1, sCardno.length() - i));
			if (i % 2 == 0) {
				a = a + ch[i] * 2;
			} else {
				a = a + ch[i];
			}
		}
		int b = 10 - a % 10;
		if (b == 10) {
			b = 0;
		}
		String checkNo = Integer.toString(b);
		return checkNo;
	}
	
	/**
	 * 根据卡号获取身份证号
	 * <p>Title:getIdtfno </p>
	 * <p>Description:	</p>
	 * @author songhao
	 * @date   2017年7月14日 
	 * @param cardno 卡号
	 * @return
	 */
	//注销掉客户信息相关内容
    public static String getIdtfno(String cardno) {
		return cardno;
//        KnaAcdc tblKna_acdc = KnaAcdcDao.selectOne_odb2(cardno, true);
//        KnaCust tblKnaCust = KnaCustDao.selectOne_odb1(tblKna_acdc.getCustac(), true);
//        CustInfo tblCustInfo = SysUtil.getInstance(IoCuCustSvcType.class).selByCustNo(tblKnaCust.getCustno());
//        return tblCustInfo.getIdtfno();
    }
    
}
