
package cn.sunline.edsp.busi.dptran.trans.online;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
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
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;

public class upopcd {

	public static void checkOpcd(final cn.sunline.edsp.busi.dptran.trans.online.intf.Upopcd.Input input,
			final cn.sunline.edsp.busi.dptran.trans.online.intf.Upopcd.Property property,
			final cn.sunline.edsp.busi.dptran.trans.online.intf.Upopcd.Output output) {

		String custna = input.getCustna(); // 客户名称
		String idtfno = input.getIdtfno(); // 证件号码
		E_IDTFTP idtftp = input.getIdtftp(); // 证件类型
		String mactid = input.getMactid();
		String acctid = input.getAcctid();
		KnaMaad tblKnaMaad = KnaMaadDao.selectOne_odb2(mactid, false);
		KnaCust tbKnaCust = null;
		property.setMAccountFlag(E_YES___.YES);
		if (CommUtil.isNotNull(tblKnaMaad)) {
			// 开户主体ID存在，不新增电子账户。
			String custac = tblKnaMaad.getCustac();
			tbKnaCust = KnaCustDao.selectOne_odb1(custac, false);
			// 如果存在状态正常的电子账户，则不新开电子账户，直接返回可用电子账户信息。
			if (E_ACCTST.NORMAL == tbKnaCust.getAcctst()) {
				property.setMAccountFlag(E_YES___.NO);
				property.setCustac(custac);
			} else {
				throw DpModuleError.DpAcct.AT020016(mactid);
			}
		}else {
			if (CommUtil.isNull(input.getCacttp())) {
				throw CaError.Eacct.BNAS0516("[" + input.getCacttp() + "]");
			}
			// 客户名称不能为空，并且长度不能超过100
			if (CommUtil.isNull(custna)) {
				CommTools.fieldNotNull(custna, BaseDict.Comm.custna.getId(), BaseDict.Comm.custna.getLongName());
			}
			// 证件类型、证件号码不能为空
			if (CommUtil.isNull(idtfno)) {
				CommTools.fieldNotNull(idtfno, BaseDict.Comm.idtfno.getId(), BaseDict.Comm.idtfno.getLongName());
			}
			if (CommUtil.isNull(idtftp)) {
				CommTools.fieldNotNull(idtftp, BaseDict.Comm.idtftp.getId(), BaseDict.Comm.idtftp.getLongName());
			}
		}
		KnaSbad tblKnaSbad = KnaSbadDao.selectOne_odb2(acctid, false);
		if(CommUtil.isNull(tblKnaSbad)) {
			throw DpModuleError.DpAcct.AT020010();
		}else {
			property.setAcctno(tblKnaSbad.getAcctno());
		}
	}

	/**
	 * 服务商信息变更
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void updateOpaccd( final cn.sunline.edsp.busi.dptran.trans.online.intf.Upopcd.Input input,  final cn.sunline.edsp.busi.dptran.trans.online.intf.Upopcd.Property property,  final cn.sunline.edsp.busi.dptran.trans.online.intf.Upopcd.Output output){
		String acctno =property.getAcctno();
		String acctid = input.getAcctid();
		String custac = property.getCustac();
		String mactid = input.getMactid();
		String custno = input.getCustno();
		String tlphno = input.getTlphno();
		updateSbad(acctid,custac,mactid);
		updateAccs(custac,acctno);
		updateAcct(custac,acctno,custno);
		updateAcal(custac,tlphno);
		updateMaad(input, property, custac);
	}
	
	/**
	 * 修改主体信息
	 * @param custac
	 * @param tlphno
	 */
	public static void updateMaad(final cn.sunline.edsp.busi.dptran.trans.online.intf.Upopcd.Input input,final cn.sunline.edsp.busi.dptran.trans.online.intf.Upopcd.Property property, String custac) {
		if(property.getMAccountFlag().getValue().equals(E_YES___.NO.getValue())){
			KnaMaad acal = KnaMaadDao.selectOne_odb1(custac, false);
			String custNa = input.getCustna();
			E_IDTFTP idtftp = input.getIdtftp();
			String idtfno = input.getIdtfno();
			if(CommUtil.isNotNull(idtftp)) {
				acal.setIdtftp(idtftp);
			}
			if(CommUtil.isNotNull(custNa)) {
				acal.setCustna(custNa);
				acal.setTmcustna(DecryptConstant.maskMobile(custNa));
			}
			if(CommUtil.isNotNull(idtfno)) {
				acal.setIdtfno(idtfno);
				acal.setTmidtfno(DecryptConstant.maskMobile(idtfno));
			}
			KnaMaadDao.updateOne_odb1(acal);
		}
	}
	
	/**
	 * 修改手机号信息
	 * @param custac
	 * @param tlphno
	 */
	public static void updateAcal(String custac,String tlphno) {
		KnaAcal acal = KnaAcalDao.selectFirst_odb6(custac, E_ACALTP.CELLPHONE, false);
		if(CommUtil.isNotNull(tlphno)&&!tlphno.equals(acal.getTlphno())) {
			acal.setTlphno(tlphno);
			acal.setTmtlphno(DecryptConstant.maskMobile(tlphno));
			KnaAcalDao.updateOne_odb8(acal);
		}
	} 
	
	/**
	 * 修改电子子账户附加信息表
	 * @param acctid
	 * @param custac
	 * @param mactid
	 */
	public static void updateSbad(String acctid,String custac,String mactid) {
		KnaSbad sbad = KnaSbadDao.selectOne_odb2(acctid, false);
		sbad.setMactid(mactid);
		sbad.setCustac(custac);
		KnaSbadDao.updateOne_odb2(sbad);
	} 
	
	/**
	 * 电子账户子户映射表
	 * @param custac
	 * @param acctno
	 */
	public static void updateAccs(String custac,String acctno) {
		KnaAccs accs = KnaAccsDao.selectOne_odb2(acctno, false);
		accs.setCustac(custac);
		KnaAccsDao.updateOne_odb2(accs);
	} 
	
	/**
	 * 电子账户子户映射表
	 * @param custac
	 * @param acctno
	 */
	public static void updateAcct(String custac,String acctno,String custno) {
		KnaAcct acct = KnaAcctDao.selectOne_odb1(acctno, false);
		acct.setCustno(custno);
		acct.setCustac(custac);
		KnaAcctDao.updateOne_odb1(acct);
	}

	public static void outResult( final cn.sunline.edsp.busi.dptran.trans.online.intf.Upopcd.Input input,  final cn.sunline.edsp.busi.dptran.trans.online.intf.Upopcd.Property property,  final cn.sunline.edsp.busi.dptran.trans.online.intf.Upopcd.Output output){
		output.setCardno(property.getCardno());
		output.setAcctid(input.getAcctid());
		output.setAcctno(property.getAcctno());
		output.setBrchno(input.getBrchno());
		output.setMactid(input.getMactid());
	}

	public static void setCardNo( final cn.sunline.edsp.busi.dptran.trans.online.intf.Upopcd.Input input,  final cn.sunline.edsp.busi.dptran.trans.online.intf.Upopcd.Property property,  final cn.sunline.edsp.busi.dptran.trans.online.intf.Upopcd.Output output){
		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectFirst_odb3(property.getCustac(), false);
		if(CommUtil.isNull(tblKnaAcdc)) {
			throw CaError.Eacct.BNAS0393();
		}
		property.setCardno(tblKnaAcdc.getCardno());
	} 
}
