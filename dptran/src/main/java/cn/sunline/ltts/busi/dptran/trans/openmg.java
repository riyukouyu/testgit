package cn.sunline.ltts.busi.dptran.trans;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcalDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpac;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpacDao;
import cn.sunline.ltts.busi.dptran.trans.intf.Openmg.Output.OpenListInfo;
import cn.sunline.ltts.busi.dptran.trans.intf.Openmg.Output.OpenListInfo.LstBindcaInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CHNLID;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;

/**
 * 
 * @author liuz
 * 2018/10/13
 * 已开户信息查询
 */
public class openmg {

	public static void selOpenMesg( final cn.sunline.ltts.busi.dptran.trans.intf.Openmg.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Openmg.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Openmg.Output output){
		BaseEnumType.E_IDTFTP idtftp = input.getIdtftp(); //证件类型
		String idtfno = input.getIdtfno(); //证件号码
		String custna = input.getCustna(); //客户名称
		//	String cardnu = null; //卡数量
		String teleno = null; //手机号码
		//客户名称长度不能超过80
		if (CommUtil.isNotNull(custna) && custna.getBytes().length > 80) {
			throw DpModuleError.DpstComm.BNAS0523();
		}

		//证件类型、证件号码不能为空
		if (CommUtil.isNull(idtfno)) {
			CommTools.fieldNotNull(idtfno, BaseDict.Comm.idtfno.getId(), BaseDict.Comm.idtfno.getLongName());
		}
		if (CommUtil.isNull(idtftp)) {
			CommTools.fieldNotNull(idtftp, BaseDict.Comm.idtftp.getId(), BaseDict.Comm.idtftp.getLongName());
		}

//		CifCust tblCifCust = CifCustDao.selectOne_odb2(idtftp, idtfno, false);
		
		long count = 0;

		//    IoCaTypGenEAccountInfo.QryOpaccdEdInfos qryEacctInfo = SysUtil.getInstance(IoCaTypGenEAccountInfo.QryOpaccdEdInfos.class);
		//    IoCaTypeGenBindCard.IoCaBindCardInfo ioCaBindCardInfo = SysUtil.getInstance(IoCaTypeGenBindCard.IoCaBindCardInfo.class);
//		if (CommUtil.isNotNull(tblCifCust)){
//			//    	throw CfError.Cust.E0004(idtftp,idtfno);
//			//    	cardnu = "0";
//
//			List<KnaCust> tblKnbCusts = KnaCustDao.selectAll_odb3(tblCifCust.getCustno(), false);
//			KnaAcdc tblKnaAcdc = null;
//			KnaAcal tblKnaAcal = null;
//			String custac = null;
//			for (KnaCust knaCust : tblKnbCusts) {
//				custna = knaCust.getCustna();
//				custac = knaCust.getCustac();
//				List<KnbOpac> tblKnbOpac = KnbOpacDao.selectAll_odb1(custac, false);
//				for (KnbOpac knbOpac : tblKnbOpac) {
//					//通户处理，银户通只能查出手机银行和银户通开的电子账户
//					if (knbOpac.getChnlid()==E_CHNLID.SH || knbOpac.getChnlid()==E_CHNLID.YT){
//						//获取账户状态
//						E_CUACST checkcuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);	 
//						//账户类型
//						E_ACCATP accatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(custac); 
//
//						tblKnaAcdc = KnaAcdcDao.selectFirst_odb3(custac, false);
//						String cardno = tblKnaAcdc.getCardno(); //电子账号
//
//						count = count + CaDao.selCountKnaCacd(custac,false); 
//						tblKnaAcal = KnaAcalDao.selectFirst_odb4(custac, E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);
//						if (CommUtil.isNotNull(tblKnaAcal)){
//							teleno = tblKnaAcal.getAcalno(); //手机号码
//						}
//						//电子账户信息查询列表复合类型
//						OpenListInfo openListInfo = SysUtil.getInstance(OpenListInfo.class);
//						openListInfo.setAccatp(accatp);;//账户类型
//						openListInfo.setCardno(cardno);//电子账号
//						openListInfo.setCustna(custna);//客户名称
//						openListInfo.setIdtftp(idtftp);//证件类型
//						openListInfo.setIdtfno(idtfno);//证件号码
//						openListInfo.setAcctst(checkcuacst);//账户状态
//						openListInfo.setTeleno(teleno);//手机号码
//						List<KnaCacd> tblKnaCacds = KnaCacdDao.selectAll_odb5(custac, false);
//						if(CommUtil.isNotNull(tblKnaCacds)){
//							for (KnaCacd knaCacd : tblKnaCacds) {
//								//电子账户绑卡信息复合类型
//								LstBindcaInfo ioCaBindCardInfo = SysUtil.getInstance(LstBindcaInfo.class);
//								ioCaBindCardInfo.setCdopac(knaCacd.getCardno()); //绑定账户
//								ioCaBindCardInfo.setCdopna(knaCacd.getAcctna()); //绑定账户名称
//								ioCaBindCardInfo.setOpbrch(knaCacd.getBrchno()); //绑定一类卡所属行号
//								ioCaBindCardInfo.setBrchna(knaCacd.getBrchna()); //绑定一类卡所属行名
//								openListInfo.getLstBindcaInfo().add(ioCaBindCardInfo);
//							}
//						}
//						output.getOpenListInfo().add(openListInfo);
//					}
//						
//				}
//				
//			}
//			output.setCardnm(count);
//		}else{
//			output.setCardnm(count);
//		}
	}

}
