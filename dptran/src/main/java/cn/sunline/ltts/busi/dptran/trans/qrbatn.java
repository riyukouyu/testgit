
package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.clwj.msap.core.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.type.DpTransfer.DpKnbAcctBach;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.lang.Options;

/**
 * 查询批量转账结果
 * 20180120
 * @author 嵇志荣
 *
 */
public class qrbatn {

	public static void selBatranResult( final cn.sunline.ltts.busi.dptran.trans.intf.Qrbatn.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrbatn.Output output){
		if(CommUtil.isNull(input.getFilesq())){
			throw DpModuleError.DpstComm.E9901("文件批次号不能为空");
		}
		RunEnvsComm runEnvsComm = CommTools.getBaseRunEnvs();//获取公共运行变量
		//从公共运行变量获取页码页数
		long prePageno = CommTools.getBaseRunEnvs().getPage_start();
		long record = CommTools.getBaseRunEnvs().getPage_size();
		long pageno = (prePageno - 1) * record;
		long totlCount = 0L;
		//分页查询
		Page<DpKnbAcctBach> dpKnbAcctBachs = DpAcctDao.selBatranResult(input.getFilesq(), input.getTranst(), input.getAcctno(), input.getAcctna(), 
				input.getPyacct(), input.getPyacna(), pageno, record, totlCount, false);
		Options<DpKnbAcctBach> options = new DefaultOptions<DpKnbAcctBach>();// 定义输出集合
		options.setValues(dpKnbAcctBachs.getRecords());
		//输出
		output.setBatran(options);
		runEnvsComm.setTotal_count(dpKnbAcctBachs.getRecordCount());
	}
}
