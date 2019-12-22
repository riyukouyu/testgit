package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeSceneDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDefn;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDefnDao;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_MODULE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class adsedf {

	private static final BizLog bizlog = BizLogUtil.getBizLog(adsedf.class);

	/**
	 * 
	 * @Title: adsedf 
	 * @Description: (新增场景事件) 
	 * @param input
	 * @param property
	 * @param output
	 * @author leipeng
	 * @date 2016年7月7日 下午7:44:41 
	 * @version V2.3.0
	 */
	public static void adsedf( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adsedf.Input input,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adsedf.Property property,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adsedf.Output output){
		bizlog.method("adsedf begin >>>>>>");
		String evetna = input.getEvetna();
		
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		
		if (CommUtil.isNotNull(FeSceneDao.sellna_kcp_scev_defn(evetna, false))) {
			throw FeError.Chrg.BNASF218();
		}
		
		E_MODULE module = E_MODULE.CG;
		String evetcd = "SJ" + BusiTools.getSequence("evetcd_seq", 4); //事件编号
		
		KcpScevDefn tblScevdefn = SysUtil.getInstance(KcpScevDefn.class);
		
		tblScevdefn.setModule(module); //模块
		tblScevdefn.setEvetcd(evetcd); //事件编号
		tblScevdefn.setEvetna(input.getEvetna()); //事件名称
		tblScevdefn.setRemark(input.getRemark()); //备注
		
		output.setEvetcd(evetcd);
		
		KcpScevDefnDao.insert(tblScevdefn);
		
		//增加审计
		ApDataAudit.regLogOnInsertParameter(tblScevdefn);
		
	  }
	}
