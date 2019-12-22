
package cn.sunline.edsp.busi.dptran.trans;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.busi.dp.namedsql.ca.AccountFlowDao;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_PASTAT;
import cn.sunline.edsp.busi.dptran.trans.intf.Dmrdlt.Input.Dtlist;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.ca.type.CaCustInfo;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdm;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmContro;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblEdmDao;

public class dmrdlt {

public static void qyrEdmDetail( final cn.sunline.edsp.busi.dptran.trans.intf.Dmrdlt.Input input,  final cn.sunline.edsp.busi.dptran.trans.intf.Dmrdlt.Property property,  final cn.sunline.edsp.busi.dptran.trans.intf.Dmrdlt.Output output){
	// 检查输入项
			String agntna = input.getAgntna();// 服务商名称
			String bstype = input.getBstype();// 业务类型(预留字段)
			String dmmnth = input.getDmmnth();// 分润月份
			E_PASTAT edmflg = input.getEdmflg();// 代发状态
			String dfbgdt=null;
			String dfeddt=null;
			Options<CaCustInfo.recordLists> results = new DefaultOptions<CaCustInfo.recordLists>();
			Options<Dtlist> dllist= input.getDtlist();
			for(Dtlist dfdate : dllist) 
			 {
				  dfbgdt=dfdate.getDfbgdt();
				  dfeddt=dfdate.getDfeddt();
		     }
			List<KnlIoblEdmContro> tblKnlIoblEdmContro=AccountFlowDao.selectKnlIoblEdmContro(dfbgdt, dfeddt, edmflg, false);
			for(KnlIoblEdmContro knliobledmcontro:tblKnlIoblEdmContro)
			{
			   CaCustInfo.recordLists acctAllInfos = SysUtil.getInstance(CaCustInfo.recordLists.class);
			   if(knliobledmcontro.getEdmflg()==E_PASTAT.UNTREAT)
			   { 
				   KnlIoblCups knlioblcups=AccountFlowDao.selectKnlIoblCups(agntna, knliobledmcontro.getRetrsq(), false);  
				   if (CommUtil.isNull(knlioblcups)) {
					   continue;
				   }             
				   KnaAcdc knaacdc=KnaAcdcDao.selectOne_odb2(knlioblcups.getCardno(),false);   
				   if (CommUtil.isNull(knaacdc)) {
					   continue;
				   }
				   KnaSbad knasbad=KnaSbadDao.selectOne_odb3(knaacdc.getCustac(), false);
				   if (CommUtil.isNull(knasbad)) {
					   continue;
				   }
				   acctAllInfos.setBradna(knasbad.getBradna()); 
				   acctAllInfos.setTlphno(knlioblcups.getTeleno()); 
				   acctAllInfos.setCustna(knlioblcups.getInmena());
				   acctAllInfos.setCardno(knlioblcups.getCardno());
			   }
			   else
			   {
				   //其他类型数据
				   KnlIoblCups knlioblcups=AccountFlowDao.selectKnlIoblCups(agntna, knliobledmcontro.getRetrsq(), false); 
				   if (CommUtil.isNull(knlioblcups)) {
					   continue;
				   }
				   KnaAcdc knaacdc=KnaAcdcDao.selectOne_odb2(knlioblcups.getCardno(),false);   
				   if (CommUtil.isNull(knaacdc)) {
					   continue;
				   }
				   KnaSbad knasbad=KnaSbadDao.selectOne_odb3(knaacdc.getCustac(), false);
				   if (CommUtil.isNull(knasbad)) {
					   continue;
				   }
				   KnlIoblEdm knlIoblEdm=KnlIoblEdmDao.selectFirst_odb02(knliobledmcontro.getRetrsq(), false);
				   acctAllInfos.setAgntna(agntna);
				   acctAllInfos.setBradna(knasbad.getBradna()); 
				   acctAllInfos.setTlphno(knlioblcups.getTeleno());
				   acctAllInfos.setCustna(knlioblcups.getInmena());
				   acctAllInfos.setCardno(knlioblcups.getCardno());
				   acctAllInfos.setDmmnth(dmmnth);
				   acctAllInfos.setTempno(knlIoblEdm.getOrgaid());
				   acctAllInfos.setDfamnt(knlIoblEdm.getTranam());
				   acctAllInfos.setDfddat(knlIoblEdm.getTrandt());
				   acctAllInfos.setDftdat(knlioblcups.getMntrtm());
				   acctAllInfos.setMntrsq(knliobledmcontro.getMntrsq());
				   acctAllInfos.setDfdetl(knlIoblEdm.getRemark());   
			   }
				results.add(acctAllInfos);
			}
			output.setRecordLists(results);
	
}
}
