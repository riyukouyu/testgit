package cn.sunline.ltts.busi.dptran.trans.lzbank;

import cn.sunline.adp.cedar.base.engine.BatchConfigConstant;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_RETYPE;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_VAFITY;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCkfi;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlCkfiDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_RQFITY;


public class rqiofi {

	/**
	 * 文件生成请求
	 * @param input
	 * @param output
	 */
	public static void dealRqiofi( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Rqiofi.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Rqiofi.Output output){
		String rqfisq = input.getRqfisq();//请求流水
		String rqfidt = input.getRqfidt();//请求日期
		E_VAFITY rqfity = input.getRqfity();//文件类型
		String trandt = input.getTrandt();//对账日期
		
		String taskId = BatchUtil.getTaskId();
		
		/**
		 * 1，输入校验
		 */
		if (CommUtil.isNull(rqfisq)) {
			throw DpModuleError.DpTrans.TS010006();
        }
        if (CommUtil.isNull(rqfidt)) {
        	throw DpModuleError.DpTrans.TS010007();
        }
        if (CommUtil.isNull(rqfity)) {
        	throw DpModuleError.DpTrans.TS010008();
        }
        if (CommUtil.isNull(trandt)) {
        	throw DpModuleError.DpTrans.TS010009();
        }
        
        /**
         * 2，登记文件生成登记簿
         */
        KnlCkfi tblKnlCkfi = KnlCkfiDao.selectOne_odb2(rqfisq, rqfidt, false);
        if(CommUtil.isNull(tblKnlCkfi)){
        	KnlCkfi entity = SysUtil.getInstance(KnlCkfi.class);
            entity.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//交易流水    
            entity.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期    
            entity.setServsq(rqfisq);//渠道流水    
            entity.setServdt(rqfidt);//渠道日期    
            entity.setTaskid(taskId);//文件批量ID  
            entity.setFilety(rqfity);//文件类型    
            entity.setChckdt(trandt);//对账日期    
            entity.setIsmkfi(E_YES___.NO);//文件是否生成
//            entity.setFimkdt();//文件生成日期
//            entity.setFipath();//文件路径    
//            entity.setFiname();//文件名称 
            KnlCkfiDao.insert(entity);
        }else{
        	//如果已经请求将原请求数据返回
            output.setTaskid(taskId);
            return;
        }
        
        
        /**
         * 3，调用批量
         */
		DataArea dataArea = DataArea.buildWithEmpty();
		dataArea.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE, CommTools.getBaseRunEnvs().getTrxn_date());
		dataArea.getInput().setString("filety", rqfity.getValue());//文件类型
		dataArea.getInput().setString("chckdt", trandt);//对账日期
		dataArea.getInput().setString("servsq", rqfisq);//请求流失-渠道流水
		dataArea.getInput().setString("servdt", rqfidt);//请求日期-渠道日期
		
        BatchUtil.submitAndRunBatchTran(taskId, "LZB1001", "gefile", dataArea);
        
        /**
         * 4,输出赋值
         */
        output.setTaskid(taskId);
        
	}
}
