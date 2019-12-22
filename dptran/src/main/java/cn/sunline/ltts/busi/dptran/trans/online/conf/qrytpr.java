package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupModePart;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupModePartDao;
import cn.sunline.ltts.busi.dp.type.DpProdType.kupModePartqryInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;


public class qrytpr {

	public static void qrytpr( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Qrytpr.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Qrytpr.Output output){
				
				//操作网点权限验证
				DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
				
				E_BUSIBI busibi = input.getBusibi();//业务大类
				String modeno = input.getModeno();  //模板编号
				
				//检查输入值
				if(CommUtil.isNull(busibi)){
					DpModuleError.DpstComm.BNAS1946();
				}
				
				if(CommUtil.isNull(modeno)){
					DpModuleError.DpstComm.BNAS1982();
				}
				
				// 取得模板部件信息列表
				List<KupModePart> KupModePart = KupModePartDao.selectAll_odb2(busibi, modeno, false);
				
				//循环获取每条记录
				for(KupModePart info:KupModePart){
					
					kupModePartqryInfo infos = SysUtil.getInstance(kupModePartqryInfo.class);
					
					infos.setBusibi(info.getBusibi());//业务大类
					infos.setModeno(info.getModeno());//模板编号
					infos.setPartcd(info.getPartcd());//部件编号
					infos.setPartfg(info.getPartfg());//部件启用标志
					infos.setPartna(info.getPartna());//部件名称
					
					//设置输出参数
					output.getModePaOutput().add(infos);
					
				}
	}
}
