package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPart;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.type.DpProdType.KupDppbPatOut;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.edsp.base.lang.Options;

public class qrypat {

	public static void selQrypat( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Qrypat.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Qrypat.Output output){
		
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());//操作网点权限验证

		//业务大类
		E_BUSIBI busibi = input.getBusibi();
		
		//产品编号
		String prodcd =  input.getProdcd();
		
		if(CommUtil.isNull(busibi)){
			throw DpModuleError.DpstComm.BNAS1946();
		}
		
		if(CommUtil.isNull(prodcd)){
			throw DpModuleError.DpstProd.BNAS1054();
		}
		
		Options<KupDppbPatOut> list = SysUtil.getInstance(Options.class);
		
		
		List<KupDppbPartTemp> tblKupDppbPartTemps = KupDppbPartTempDao.selectAll_odb2(busibi, prodcd, false);
		
		//查临时表
		if(CommUtil.isNotNull(tblKupDppbPartTemps)){
			
			for (KupDppbPartTemp tblKupDppbPartTemp : tblKupDppbPartTemps){
				KupDppbPatOut kupDppbPatOut = SysUtil.getInstance(KupDppbPatOut.class);
				
				kupDppbPatOut.setBusibi(tblKupDppbPartTemp.getBusibi());
				kupDppbPatOut.setProdcd(tblKupDppbPartTemp.getProdcd());
				kupDppbPatOut.setAddmtp(tblKupDppbPartTemp.getAddmtp());
				kupDppbPatOut.setPartcd(tblKupDppbPartTemp.getPartcd());
				kupDppbPatOut.setPartfg(tblKupDppbPartTemp.getPartfg());
				kupDppbPatOut.setPartna(tblKupDppbPartTemp.getPartna());
				
				list.add(kupDppbPatOut);
			}
		}else{
			//临时表为空则查正式表
			List<KupDppbPart> tblKupDppbParts = KupDppbPartDao.selectAll_odb2(busibi, prodcd, false);
			
			for (KupDppbPart tblKupDppbPart : tblKupDppbParts){
				KupDppbPatOut kupDppbPatOut = SysUtil.getInstance(KupDppbPatOut.class);
				
				kupDppbPatOut.setBusibi(tblKupDppbPart.getBusibi());
				kupDppbPatOut.setProdcd(tblKupDppbPart.getProdcd());
				kupDppbPatOut.setAddmtp(tblKupDppbPart.getAddmtp());
				kupDppbPatOut.setPartcd(tblKupDppbPart.getPartcd());
				kupDppbPatOut.setPartfg(tblKupDppbPart.getPartfg());
				kupDppbPatOut.setPartna(tblKupDppbPart.getPartna());
				
				list.add(kupDppbPatOut);
			}
		}
		
		//输出
		output.setQryPatInfos(list);
		
	}

}