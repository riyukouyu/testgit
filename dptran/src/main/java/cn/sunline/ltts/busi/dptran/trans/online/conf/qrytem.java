package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.type.DpProdType.kupModeqryInfo;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;


public class qrytem {

	public static void qrytem( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Qrytem.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Qrytem.Property property,  final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Qrytem.Output output){
		//分页查询页码、页容量设置
		int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());//页码
		int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());//页容量
		int totlCount = 0; // 记录总数
		int startno = (pageno - 1) * pgsize;// 起始记录数
		
		//操作网点权限验证
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
				
		E_BUSIBI busibi = input.getBusibi();//业务大类
		String modena = input.getModena();  //模板名称
		
//		if(CommUtil.isNull(busibi)){
//			DpModuleError.DpstComm.E9027("业务大类");
//		}
		
		// 取得模板信息列表
		Page<kupModeqryInfo> listinfo = DpProductDao.selProdtem(busibi, modena, startno, pgsize, totlCount, false);
		Options<kupModeqryInfo> cplkupModeqry = SysUtil.getInstance(Options.class);
		cplkupModeqry.addAll(listinfo.getRecords());
		
		// 设置总记录数
		CommTools.getBaseRunEnvs().setTotal_count(listinfo.getRecordCount());
		
		// 设置输出参数
		output.setModeOutput(cplkupModeqry);
	}
}
