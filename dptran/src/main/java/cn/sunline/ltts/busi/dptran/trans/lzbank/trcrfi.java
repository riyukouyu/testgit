package cn.sunline.ltts.busi.dptran.trans.lzbank;

import cn.sunline.adp.cedar.base.engine.BatchConfigConstant;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;


public class trcrfi {

	public static void dealtrcrfi( final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Trcrfi.Input input,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Trcrfi.Property property,  final cn.sunline.ltts.busi.dptran.trans.lzbank.intf.Trcrfi.Output output){
		
	    String trandt = input.getTrandt();//文件日期
	    if(CommUtil.isNull(trandt)){
	        throw DpModuleError.DpstComm.E9999("文件日期不能为空!");
        }
		DataArea dateArea = DataArea.buildWithEmpty();
		dateArea.getInput().setString("trandt", trandt);
		dateArea.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE, CommTools.getBaseRunEnvs().getTrxn_date());
		BatchUtil.submitAndRunBatchTran(BatchUtil.getTaskId(),"LZB1001", "rdcrfi", dateArea);		
	}
}
