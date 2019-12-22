package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCnapot;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class hvsaps {
	private static BizLog log = BizLogUtil.getBizLog(hvsaps.class);
	
	/***
	 * 大额清算报文上送电子账户核心
	 * */


	public static void updcnapot( final cn.sunline.ltts.busi.dptran.trans.intf.Hvsaps.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Hvsaps.Output output){
        log.debug("--------------进入大额清算报文上送电子账户核心服务-----------");
        
		
		if(CommUtil.isNull(input.getSubsys())){
			throw DpModuleError.DpstComm.BNAS1361();
		}
		if(CommUtil.isNull(input.getMsetdt())){
			throw DpModuleError.DpstComm.BNAS1373();
		}
		if(CommUtil.isNull(input.getMsetsq())){
			throw DpModuleError.DpstComm.BNAS1374();
		}
		if(CommUtil.isNull(input.getCrdbtg())){
			throw DpModuleError.DpstComm.BNAS1370();
		}
		if(CommUtil.isNull(input.getIotype())){
			throw DpModuleError.DpstComm.BNAS1376();
		}
		if(CommUtil.isNull(input.getPyercd())){
			throw DpModuleError.DpstComm.BNAS1379();
		}
		if(CommUtil.isNull(input.getKeepdt())){
			throw DpModuleError.DpstComm.BNAS0399();
		}
		if(CommUtil.isNull(input.getChfcnb())){
			throw DpModuleError.DpstComm.BNAS1362();
		}
		
		//小额支付
		if(CommUtil.equals(input.getSubsys(), "1")){ 
			if(CommUtil.isNull(input.getNpcpdt())){
				throw DpModuleError.DpstComm.BNAS1392();
			}
			if(CommUtil.isNull(input.getNpcpbt())){
				throw DpModuleError.DpstComm.BNAS1393();
			}
			if(CommUtil.isNull(input.getPakgdt())){
				throw DpModuleError.DpstComm.BNAS1394();
			}
			if(CommUtil.isNull(input.getPakgsq())){
				throw DpModuleError.DpstComm.BNAS1395();
			}				
		}
		
		String mtdate = CommTools.getBaseRunEnvs().getTrxn_date();
		String timetm =DateTools2.getCurrentTimestamp();
		
		KnlCnapot tbKnlCnapot = DpAcctQryDao.selknlcnapotOne(input.getSubsys(), input.getMsetdt(), input.getMsetsq(), input.getCrdbtg(), input.getIotype(), input.getPyercd(), false);
		if(CommUtil.isNull(tbKnlCnapot)){
			throw DpModuleError.DpstComm.BNAS1396();
		}
		
		//更新大额清算报文登记薄信息
		tbKnlCnapot.setKeepdt(input.getKeepdt());
		//委托日期更新为清算日期
		if(CommUtil.isNotNull(input.getKeepdt())){
			tbKnlCnapot.setMsetdt(input.getKeepdt());
		}
		
		tbKnlCnapot.setChfcnb(input.getChfcnb());
		
		if(CommUtil.isNotNull(input.getNpcpdt())){
			tbKnlCnapot.setNpcpdt(input.getNpcpdt());
		}
		if(CommUtil.isNotNull(input.getNpcpbt())){
			tbKnlCnapot.setNpcpbt(input.getNpcpbt());
		}
		if(CommUtil.isNotNull(input.getPakgdt())){
			tbKnlCnapot.setPakgdt(input.getPakgdt());
		}
		if(CommUtil.isNotNull(input.getPakgsq())){
			tbKnlCnapot.setPakgsq(input.getPakgsq());
		}	
		
		
//		KnlCnapotDao.updateOne_odb1(tbKnlCnapot);
		DpAcctQryDao.updknlcnapotOne(tbKnlCnapot.getMsetdt(),tbKnlCnapot.getKeepdt(), tbKnlCnapot.getChfcnb(), tbKnlCnapot.getNpcpdt(), tbKnlCnapot.getNpcpbt(), 
				tbKnlCnapot.getPakgdt(), tbKnlCnapot.getPakgsq(), tbKnlCnapot.getServsq(), tbKnlCnapot.getServdt(), tbKnlCnapot.getCorpno(),timetm);
		
	}
}
