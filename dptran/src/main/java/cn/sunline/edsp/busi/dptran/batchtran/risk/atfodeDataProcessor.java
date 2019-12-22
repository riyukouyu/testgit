
package cn.sunline.edsp.busi.dptran.batchtran.risk;
import java.util.List;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
//import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
//import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.edsp.busi.dp.iobus.servicetype.risk.DpRiskService;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskEnumType.E_DEAPST;
import cn.sunline.edsp.busi.dp.iobus.type.risk.DpRiskType.KnbAplyDto;
import cn.sunline.edsp.busi.dp.namedsql.DpRiskDao;
	 /**
	  * 退单自动强扣
	  * @author 
	  * @Date 
	  */

public class atfodeDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.edsp.busi.dptran.batchtran.risk.intf.Atfode.Input, cn.sunline.edsp.busi.dptran.batchtran.risk.intf.Atfode.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.edsp.busi.dptran.batchtran.risk.intf.Atfode.Input input, cn.sunline.edsp.busi.dptran.batchtran.risk.intf.Atfode.Property property) {
        List<KnbAplyDto> knbAplyDtos =
            DpRiskDao.selectFrozenAplyInfos(null, null, null, E_DEAPST.PARTIAL_DEDUCTED.getValue(), null, false);
        DpRiskService dpRiskService = SysUtil.getInstance(DpRiskService.class);
        for (KnbAplyDto knbAplyDto : knbAplyDtos) {
            dpRiskService.forceDeduct(knbAplyDto.getAplyno());
        }
	}

}


