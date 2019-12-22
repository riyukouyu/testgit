package cn.sunline.ltts.busi.ca.serviceimpl;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KcdCard;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KcdCardDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccsDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCuad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSign;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSignDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSignDetl;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSignDetlDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbClac;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbClacDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbRecl;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbReclDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnpAcctType;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnpAcctTypeDao;
import cn.sunline.ltts.busi.dp.namedsql.DpStrikeSqlDao;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaHknsAcsq;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKcdCard;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCacd;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCuad;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSign;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSignDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbClac;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnbRecl;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnpAcctType;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnsTran;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaMainAcctInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypeGenBindCard.KnaCacdInfo;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ENABST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;

/**
 * 查询表信息服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoCaSevQryTableInfoImpl", longname = "查询表信息服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoCaSevQryTableInfoImpl implements
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo {
	
	/**
	 * 根据负债账号查询负债账号客户账号表信息
	 * 
	 */
	@Override
	public IoCaKnaAccs getKnaAccsOdb2(String acctno, Boolean isable) {
		KnaAccs tblKnaAccs = KnaAccsDao.selectOne_odb2(acctno, isable);
		IoCaKnaAccs knaAccs = null;

		if (CommUtil.isNotNull(tblKnaAccs)) {
			knaAccs = SysUtil.getInstance(IoCaKnaAccs.class);
			CommUtil.copyProperties(knaAccs, tblKnaAccs);
		}
		return knaAccs;
	}

	/**
	 * 根据电子账号查询电子账户表
	 * 
	 */
	@Override
	public IoCaKnaCust getKnaCustByCustacOdb1(String custac, Boolean isable) {
		KnaCust tblknacust = CaDao.selKnaCustByCustac(custac, isable);
		IoCaKnaCust knaCust = null;

		if (CommUtil.isNotNull(tblknacust)) {
			knaCust = SysUtil.getInstance(IoCaKnaCust.class);
			CommUtil.copyProperties(knaCust, tblknacust);
		}
		return knaCust;
	}

	/**
	 * 根据电子账号账号状态查询可客户账号对照表
	 * 
	 */
	@Override
	public IoCaKnaAcdc getKnaAcdcOdb1(String custac, E_DPACST status,
			Boolean isable) {

		KnaAcdc tblKnaAcdc = CaDao.selKnaAcdcByAcStutas(custac, status, isable);
		IoCaKnaAcdc knaAcdc = SysUtil.getInstance(IoCaKnaAcdc.class);

		if (CommUtil.isNotNull(tblKnaAcdc)) {
			CommUtil.copyProperties(knaAcdc, tblKnaAcdc);
			return knaAcdc;
		}
		return null;
	}

	/**
	 * 根据客户账号类型查询客户账号类型定义表
	 * 
	 */
	@Override
	public IoCaKnpAcctType getKnpAcctTypeOdb1(E_CUSACT cacttp,
			Boolean isable) {

		//String corpno = BusiTools.getCenterCorpno();
		//KnpAcctType tblKnpAcctType = EacctMainDao.selKnpAcctType(corpno, cacttp, isable);
		
		KnpAcctType tblKnpAcctType = KnpAcctTypeDao.selectOne_odb1(cacttp, isable);
		IoCaKnpAcctType knpAcctType = SysUtil
				.getInstance(IoCaKnpAcctType.class);

		if (CommUtil.isNotNull(tblKnpAcctType)) {
			CommUtil.copyProperties(knpAcctType, tblKnpAcctType);
			return knpAcctType;
		}
		return null;
	}

	/**
	 * 根据绑定卡号查询卡客户账号对照表
	 * 
	 */
	@Override
	public IoCaKnaAcdc getKnaAcdcOdb2(String cardno, Boolean isable) {

		//20161125
     	IoCaKnaAcdc tblKnaAcdc = SysUtil.getInstance(IoCaKnaAcdc.class);
		tblKnaAcdc = CaDao.selKnaAcdcByCard(cardno, isable);
		
//		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(cardno, isable);
		IoCaKnaAcdc knaAcdc = SysUtil.getInstance(IoCaKnaAcdc.class);

		if (CommUtil.isNotNull(tblKnaAcdc)) {
			CommUtil.copyProperties(knaAcdc, tblKnaAcdc);
			return knaAcdc;
		}
		return null;
	}

	/**
	 * 根据客户账号类型客户号查询客户信息
	 * 
	 */
	@Override
	public IoCaKnaCust getKnaCustFirstOdb2(E_CUSACT cacttp, String custno,
			Boolean isable) {
		KnaCust tblKnaCust = KnaCustDao.selectFirst_odb2(cacttp, custno,
				isable);
		IoCaKnaCust knaCust = SysUtil.getInstance(IoCaKnaCust.class);

		if (CommUtil.isNotNull(tblKnaCust)) {
			CommUtil.copyProperties(knaCust, tblKnaCust);
			return knaCust;
		}
		return null;
	}

	/**
	 * 根据卡号查询卡信息表
	 * 
	 */
	@Override
	public IoCaKcdCard getKcdCardOdb1(String cardno, Boolean isable) {
		KcdCard tblKcdCard = KcdCardDao.selectOne_odb1(cardno, isable);
		IoCaKcdCard kcdCard = SysUtil.getInstance(IoCaKcdCard.class);
		if(CommUtil.isNotNull(tblKcdCard)){
			CommUtil.copyProperties(kcdCard, tblKcdCard);
			return kcdCard;
		}
		

		return null;
	}

	/**
	 * 更新卡信息表
	 * 
	 */
	@Override
	public void updateKcdCardOdb1(IoCaKcdCard kcdCard) {

		KcdCard tbKcdCard = SysUtil.getInstance(KcdCard.class);
		CommUtil.copyProperties(tbKcdCard, kcdCard, false);
		KcdCardDao.updateOne_odb1(tbKcdCard);

	}

	/**
	 * 根据电子账号查询负债账号与客户账号对照表
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Options<IoCaKnaAccs> listKnaAccsOdb5(String custac,
			Boolean isable) {

		List<KnaAccs> tblKnaAccss = KnaAccsDao.selectAll_odb5(custac, isable);

		Options<IoCaKnaAccs> opts = SysUtil.getInstance(Options.class);
//		IoCaKnaAccs accs = SysUtil.getInstance(IoCaKnaAccs.class);
		if(CommUtil.isNotNull(tblKnaAccss) || tblKnaAccss.size() > 0){
			for (KnaAccs tblKnaAccs : tblKnaAccss) {
				IoCaKnaAccs accs = SysUtil.getInstance(IoCaKnaAccs.class);
				CommUtil.copyProperties(accs, tblKnaAccs);
				opts.add(accs);
			}
			return opts;
		}
		
		return null;
	}

	/**
	 * 根据电子账号币种查询负债账号与客户账号对照表
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Options<IoCaKnaAccs> listKnaAccsOdb8(String custac,
			String crcycd, Boolean isable) {
		List<KnaAccs> tblKnaAccss = KnaAccsDao.selectAll_odb8(custac,crcycd, isable);

		Options<IoCaKnaAccs> opts = SysUtil.getInstance(Options.class);
		if(CommUtil.isNotNull(tblKnaAccss) || tblKnaAccss.size() > 0){
			for (KnaAccs tblKnaAccs : tblKnaAccss) {
				IoCaKnaAccs accs = SysUtil.getInstance(IoCaKnaAccs.class);
				CommUtil.copyProperties(accs, tblKnaAccs);
				opts.add(accs);
			}
			return opts;
		}
		return null;
	}

	/**
	 * 根据电子账号子户号查询负债账号与客户账号对照表
	 * 
	 */
	@Override
	public IoCaKnaAccs getKnaAccsOdb1(String custac, String subsac,
			Boolean isable) {
		KnaAccs tblKnaAccs = KnaAccsDao.selectOne_odb1(custac, subsac,
				isable);
		IoCaKnaAccs knaAccs = SysUtil.getInstance(IoCaKnaAccs.class);
		if(CommUtil.isNotNull(tblKnaAccs)){
			CommUtil.copyProperties(knaAccs, tblKnaAccs);
			return knaAccs;
		}
		return null;
	}

	/**
	  * 新增负债账号客户账号对照表信息
	  *
	  */
	@Override
	public void saveKnaAccs(IoCaKnaAccs accs) {
		KnaAccs KnaAccs = SysUtil.getInstance(KnaAccs.class);
		CommUtil.copyProperties(KnaAccs, accs);
		KnaAccsDao.insert(KnaAccs);
	}
	
	/**
	  * 根据负债账号更新负债账号客户账号对应表信息
	  *
	  */
	@Override
	public void updateCaDaoKnaAccsByAcctno(E_ACCTST acctst, String acctno) {
		String timetm =DateTools2.getCurrentTimestamp();
		
		CaDao.updKnaAccsByAcctno(acctst, acctno,timetm);
	}
	
	/**
	 * 根据电子账号账号状态查询外部卡与客户账号对照表
	 * 
	 */
	@Override
	public IoCaKnaCacd getKnaCacdFirstOdb3(String custac,
			E_DPACST status, Boolean isable) {

		KnaCacd tblKnaCacd = KnaCacdDao.selectFirst_odb3(custac, status,
				isable);
		IoCaKnaCacd knaCacd = SysUtil.getInstance(IoCaKnaCacd.class);
		if(CommUtil.isNotNull(tblKnaCacd)){
			CommUtil.copyProperties(knaCacd, tblKnaCacd);
			return knaCacd;
		}
		return null;
	}

	/**
	 * 更新电子账户表
	 * 
	 */
	@Override
	public void updateKnaCustOdb1(IoCaKnaCust knaCust) {
		KnaCust tbKnaCust = SysUtil.getInstance(KnaCust.class);
		CommUtil.copyProperties(tbKnaCust, knaCust, false);
		KnaCustDao.updateOne_odb1(tbKnaCust);

	}

	/**
	 * 根据电子账号查询电子账户表
	 * 
	 */
	@Override
	public IoCaKnaCust getKnaCustWithLockByCustacOdb1(String custac,
			Boolean isable) {
		KnaCust tblKnaCust = KnaCustDao.selectOneWithLock_odb1(custac,
				isable);
		IoCaKnaCust knaCust = SysUtil.getInstance(IoCaKnaCust.class);
		if(CommUtil.isNotNull(tblKnaCust)){
			CommUtil.copyProperties(knaCust, tblKnaCust, false);
			return knaCust;
		}
		return null;
		
	}

	 /**
	  * 查询签约信息表
	  *
	  */
	@Override
	public IoCaKnaSign getKnaSignOdb1(String custac, E_SIGNTP signtp, E_SIGNST signst, Boolean isable) {
		KnaSign KnaSign = KnaSignDao.selectFirst_odb2(custac, signtp, signst, isable);
		IoCaKnaSign sign = SysUtil.getInstance(IoCaKnaSign.class);
		if(CommUtil.isNull(KnaSign)){
			return null;
		}else{
			CommUtil.copyProperties(sign, KnaSign);
			return sign;
		}
	}

	/**
	  * 更新签约信息表
	  *
	  */
	@Override
	public void updateKnaSignOdb2(IoCaKnaSign sign, Boolean isable) {
		KnaSign KnaSign = SysUtil.getInstance(KnaSign.class);
		CommUtil.copyProperties(KnaSign, sign);
		KnaSignDao.updateOne_odb1(KnaSign);
	}

	/**
	  * 更新签约明细表
	  *
	  */
	@Override
	public void updateKnaSignDetlOdb1(IoCaKnaSignDetl signdetl, Boolean isable) {
		
		KnaSignDetl KnaSignDetl = SysUtil.getInstance(KnaSignDetl.class);
		CommUtil.copyProperties(KnaSignDetl, signdetl);
		KnaSignDetlDao.updateOne_odb1(KnaSignDetl);
	}

	 /**
	  * 查询签约明细表
	  *
	  */
	@Override
	public IoCaKnaSignDetl getKnaSignDetlOdb1(Long signno,Boolean isable) {
		KnaSignDetl KnaSignDetl = KnaSignDetlDao.selectOne_odb1(signno, isable);
		IoCaKnaSignDetl detl = SysUtil.getInstance(IoCaKnaSignDetl.class);
		if(CommUtil.isNotNull(KnaSignDetl)){
			CommUtil.copyProperties(detl, KnaSignDetl);
			return detl;
		}
		return null;
		
	}

	/**
	  * 根据电子账号获取电子账户信息
	  *
	  */
	@Override
	public IoCaKnaCust getKnaCustByCardnoOdb1(String cardno,
			Boolean isable) {
		// 检查账户状态状态
		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(cardno, isable);
		if (CommUtil.isNull(tblKnaAcdc)) {
			throw CaError.Eacct.BNAS1279();
		}
		String sCustac = tblKnaAcdc.getCustac();
		
		KnaCust tblKnaCust = SysUtil.getInstance(KnaCust.class);
		tblKnaCust = KnaCustDao.selectOne_odb1(sCustac, isable);
		if (CommUtil.isNull(tblKnaCust)) {
			throw CaError.Eacct.BNAS1279();
		}
		IoCaKnaCust caKnaCust = SysUtil.getInstance(IoCaKnaCust.class);
		CommUtil.copyProperties(caKnaCust, tblKnaCust);
		
		return caKnaCust;
	}

	/**
	  * 增加销户登记簿记录
	  *
	  */
	@Override
	public void saveKnbClac(IoCaKnbClac tblKnbClac) {
		
		KnbClac clac = SysUtil.getInstance(KnbClac.class);
		
		CommUtil.copyProperties(clac, tblKnbClac);
		KnbClacDao.insert(clac);
	}

	/**
	 * 
	 * @Title: KnbRecl_selectOne_odb1
	 * @Description: (查询电子账户卡号复用表信息)
	 * @param cardno
	 * @return
	 * @author xiongzhao
	 * @date 2016年8月2日 上午9:27:17
	 * @version V2.3.0
	 */
	@Override
	public IoCaKnbRecl getKnbReclOdb1(String cardno, Boolean isable)  {

		// 创建电子账户卡号复用表复合类型的对象
		IoCaKnbRecl cplKnbRecl = null;

		// 根据卡号查询出电子账户卡号复用表信息
		KnbRecl tblKnbRecl = KnbReclDao.selectOne_odb1(cardno, isable);
		if(CommUtil.isNotNull(tblKnbRecl)){
			cplKnbRecl = SysUtil.getInstance(IoCaKnbRecl.class);
			// 赋值给创建的对象
			CommUtil.copyProperties(cplKnbRecl, tblKnbRecl);
		}

		// 返回查询结果
		return cplKnbRecl;
	}

	/**
	 * 
	 * @Title: KnbRecl_insert 
	 * @Description: (登记电子账户卡号复用表) 
	 * @param cardno
	 * @param trandt
	 * @param enabst
	 * @author xiongzhao
	 * @date 2016年8月2日 上午10:10:47 
	 * @version V2.3.0
	 */

	@Override
	public void saveKnbRecl(String cardno, String trandt, E_ENABST enabst) {
		
		// 创建电子账户卡号复用表的对象
		KnbRecl tblKnbRecl = SysUtil.getInstance(KnbRecl.class);
		
		tblKnbRecl.setCardno(cardno);// 卡号
		tblKnbRecl.setTrandt(trandt);// 交易日期
		tblKnbRecl.setEnabst(enabst);// 启用状态
		
		// 将记录插入电子账户卡号复用表
		KnbReclDao.insert(tblKnbRecl);
		
	}

	/**
	 * 
	 * @Title: KnbRecl_updateOne_odb1
	 * @Description: (更新电子账户卡号复用表信息)
	 * @param cardno
	 * @param trandt
	 * @param enabst
	 * @author xiongzhao
	 * @date 2016年8月2日 上午10:32:20
	 * @version V2.3.0
	 */
	@Override
	public void updateKnbReclOdb1(String cardno, String trandt,
			E_ENABST enabst) {
		
		// 根据卡号查询出电子账户卡号复用表信息
		KnbRecl tblKnbRecl = KnbReclDao.selectOne_odb1(cardno, true);
		
		// 更新表信息
		tblKnbRecl.setEnabst(enabst);
		tblKnbRecl.setTrandt(trandt);
		KnbReclDao.updateOne_odb1(tblKnbRecl);

	}

	/**
	 * 查询转存签约明细信息
	 * @Title: knaSignDetlSelectFirstOdb2 
	 * @Description: 查询转存签约明细信息
	 * @param acctno
	 * @param signst
	 * @param isable
	 * @return
	 * @author liaojincai
	 * @date 2016年8月15日 下午9:59:06 
	 * @version V2.3.0
	 */
	public IoCaKnaSignDetl knaSignDetlSelectFirstOdb2(String acctno, E_SIGNST signst, Boolean isable) {
		
		KnaSignDetl tblKnaSignDetl = KnaSignDetlDao.selectFirst_odb2(acctno, signst, isable);
		IoCaKnaSignDetl cplKnaSignDetl = null;
		
		if (CommUtil.isNotNull(tblKnaSignDetl)) {
			cplKnaSignDetl = SysUtil.getInstance(IoCaKnaSignDetl.class);
			CommUtil.copyProperties(cplKnaSignDetl, tblKnaSignDetl);
		}
		
		return cplKnaSignDetl;
	}

	/**
	 * 
	 * @Title: knaCacdSelectOneOdb1
	 * @Description: (根据电子账号，绑定卡号查询绑定账户信息)
	 * @param custac
	 * @param cardno
	 * @param isable
	 * @return
	 * @author xiongzhao
	 * @date 2016年8月23日 下午9:43:38
	 * @version V2.3.0
	 */
	@Override
	public IoCaKnaCacd getKnaCacdOdb1(String custac, String cardno,
			Boolean isable) {
		KnaCacd tblKnaCacd = KnaCacdDao
				.selectOne_odb1(custac, cardno, isable);
		IoCaKnaCacd knaCacd = SysUtil.getInstance(IoCaKnaCacd.class);
		if (CommUtil.isNotNull(tblKnaCacd)) {
			CommUtil.copyProperties(knaCacd, tblKnaCacd);
			return knaCacd;
		}
		return null;
	}

	/**
	  * 根据电子账号查询卡客户账户对照表
	  *
	  */
	@Override
	public IoCaKnaAcdc getKnaAcdcByCardno(String cardno, Boolean isable) {
		
		IoCaKnaAcdc cplKnaAcdc = CaDao.selKnaAcdcByCard(cardno, isable);

		return cplKnaAcdc;
	}

	/**
	 * 查询转存签约明细信息
	 * @Title: knaSignDetlSelectFirstOdb3 
	 * @Description: 根据负债账号查询转存签约明细信息
	 * @param acctno
	 * @param fxacct
	 * @param signst
	 * @param isable
	 * @return
	 * @author chaiwenchang
	 * @date 2016年10月26日 下午16:20:12 
	 * @version V2.3.0
	 */
	@Override
	public IoCaKnaSignDetl getKnaSignDetlFirstOdb3(String acctno,
			String fxacct, E_SIGNST signst, Boolean isable) {
		
		KnaSignDetl tblKnaSignDetl = KnaSignDetlDao.selectFirst_odb3(acctno, fxacct, signst, isable);
		IoCaKnaSignDetl cplKnaSignDetl = null;
		
		if (CommUtil.isNotNull(tblKnaSignDetl)) {
			cplKnaSignDetl = SysUtil.getInstance(IoCaKnaSignDetl.class);
			CommUtil.copyProperties(cplKnaSignDetl, tblKnaSignDetl);
		}
		
		return cplKnaSignDetl;
	}

	/**
	  * 根据负债账号查询负债账号客户账号表(不带法人代码)
	  *
	  */
	@Override
	public IoCaKnaAccs getKnaAccsOdb3(String acctno, Boolean isable) {
		
		IoCaKnaAccs caKnaAccs = null;
		
		if(CommUtil.isNotNull(acctno)){
			 caKnaAccs = CaDao.selKnaAccsByAcctno(acctno, isable);
		}
		
		return caKnaAccs;
	}


	public IoCaKnaCuad KnaCuad_selectOne_odb1( String custac,  Boolean isable){
		
		IoCaKnaCuad caKnaCuad = SysUtil.getInstance(IoCaKnaCuad.class);
		
		KnaCuad tblKnaCuad = CaDao.selKnaCuadByCustac(custac, isable);
		
		CommUtil.copyProperties(caKnaCuad, tblKnaCuad);
		
		return caKnaCuad;
	}

	public IoCaKnbClac KnbClac_selectOne_odb1( String clossq,  Boolean isable){
		
		IoCaKnbClac caKnbClac = null;
		KnbClac tblKnbClac = CaDao.selKnbClacByClossq(clossq, isable);
		if(CommUtil.isNotNull(tblKnbClac)){
			caKnbClac = SysUtil.getInstance(IoCaKnbClac.class);
			CommUtil.copyProperties(caKnbClac, tblKnbClac);
		}
		
		return caKnbClac;
	}

	@Override
	public IoCaKnaCuad getKnaCuadOdb1(String custac, Boolean isable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IoCaKnbClac getKnbClacOdb1(String clossq, Boolean isable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IoCaKnaSignDetl getKnaSignDetlFirstOdb2(String acctno,
			E_SIGNST signst, Boolean isable) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Options<IoCaHknsAcsq> getKnsAcsqOdb1(String prtrsq, String acctno) {
		//查询kns_acsq与h_kns_acsq
		List<IoCaHknsAcsq> tblknsacsq2 = CaDao.selknsacsq(prtrsq, acctno, false);
		Options<IoCaHknsAcsq> opts = SysUtil.getInstance(Options.class);
		if(CommUtil.isNotNull(tblknsacsq2) || tblknsacsq2.size() > 0){
			for (IoCaHknsAcsq tblKnaAccs : tblknsacsq2) {
				IoCaHknsAcsq accs = SysUtil.getInstance(IoCaHknsAcsq.class);
				CommUtil.copyProperties(accs, tblKnaAccs);
				opts.add(accs);
			}
		}
		
		return opts;
	}


	public cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnsTran getKnsTranOdb1( String prtrsq,  String prtrdt,  String cdcnno){
		IoCaKnsTran knstran = null;
		/*KnsTran tblknstran = KnsTranDao.selectOne_odb1(prtrsq, prtrdt, false);
		if(CommUtil.isNotNull(tblknstran) && tblknstran.getRvfxst()== BaseEnumType.E_RVFXST.NONE ){
			CommUtil.copyProperties(knstran, tblknstran);
		}*/
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		knstran = DpStrikeSqlDao.selKnsTranByTransqAndTrandt(prtrsq, prtrdt, corpno, false);
		if (CommUtil.isNotNull(knstran) && knstran.getRvfxst()== BaseEnumType.E_RVFXST.NONE) {
			return knstran;
		} else {
			return null;
		}
		
	}

	public void getMainKnaCustOdb1( final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo.getMainKnaCustOdb1.Input input,  final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo.getMainKnaCustOdb1.Output output){
		// 输入项
		String cardno = input.getCardno();
		E_IDTFTP idtftp = input.getIdtftp();
		String idtfno = input.getIdtfno();
		String tlphno = input.getTlphno();
		String custno = input.getCustno();
		String custna = input.getCustna();
		Long pageno = CommTools.getBaseRunEnvs().getPage_start();
		Long pagesize = CommTools.getBaseRunEnvs().getPage_size();

		// 输入属性关系检查
		/*boolean isAllNull = CommUtil.isNull(cardno) && CommUtil.isNull(idtfno) && CommUtil.isNull(teleno)
				&& CommUtil.isNull(custid) && CommUtil.isNull(custna);
		if (isAllNull) {
			throw CaError.Eacct.E0001("用户ID/用户名称/电子账号/证件号码/绑定手机号必输其一！");
		}*/
		
		if(CommUtil.isNotNull(idtftp)){
//			if (E_IDTFTP.SFZ != idtftp){
//				throw DpModuleError.DpstComm.E9999("暂时只支持身份证！");
//			}
			if (CommUtil.isNull(idtfno)) {
				throw DpModuleError.DpAcct.AT010037();
			}
		}else if(CommUtil.isNotNull(idtfno)) {
			throw DpModuleError.DpAcct.AT010038();
		}
		
		Page<IoCaMainAcctInfo> mainAcctInfoPage = CaDao.selJfCaList(cardno, idtftp, idtfno, tlphno, custno,
				custna, (pageno - 1) * pagesize, pagesize, 0, false);
					
		long totalNo = mainAcctInfoPage.getRecordCount();
		List<IoCaMainAcctInfo> mainAcctList = mainAcctInfoPage.getRecords();
		output.getCardnoInfoList().setValues(mainAcctList);
		CommTools.getBaseRunEnvs().setTotal_count(totalNo);
	}
	
	/**
	 *  客户结算卡列表查询
	 */
	public void qrybindCarInfoByCano( final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo.qrybindCarInfoByCano.Input input,  final cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo.qrybindCarInfoByCano.Output output){
		long pageno = CommTools.getBaseRunEnvs().getPage_start();
		long pagesize = CommTools.getBaseRunEnvs().getPage_size();
		// 客户账号
		String cardno = input.getCardno();
		// 电话号码
		String tlphno=input.getTlphno();
		// 证件号
		String idtfno=input.getIdtfno();
		// 证件类型
		E_IDTFTP idtftp = input.getIdtftp();
		// 结算卡号
		String cdopac = input.getCdopac();
		// 结算卡名称
		String acctna = input.getCdopna();
		
		boolean isIdnoNull = CommUtil.isNotNull(idtftp) && CommUtil.isNull(idtfno);
		boolean isIdtpNull = CommUtil.isNotNull(idtfno) && CommUtil.isNull(idtftp);
		// 证件类型和证件号必须同时输入。
		if(isIdnoNull || isIdtpNull) {
			throw DpModuleError.DpAcct.AT020051();
		}
		
		Page<KnaCacdInfo> pgKnaCacdList = CaDao.selAllBoundCards(idtftp, idtfno, cardno, cdopac, acctna, tlphno, (pageno-1)*pagesize, pagesize, CommTools.getBaseRunEnvs().getTotal_count(), false);
		output.getBindCardInfo().setValues(pgKnaCacdList.getRecords());
		CommTools.getBaseRunEnvs().setTotal_count(pgKnaCacdList.getRecordCount());
	}


	
}