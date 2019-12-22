package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfir;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DRINTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;

public class ddfdel {

    public static void delDdf(final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Ddfdel.Input Input) {
        String prodcd = Input.getProdcd();
        String crcycd = Input.getCrcycd();
        E_DRINTP drintp = Input.getDrintp();
        String ingpcd = Input.getIngpcd();
        E_INTRTP intrtp = Input.getIntrtp();
        //传入值检查
        if (CommUtil.isNull(prodcd)) {
        	DpModuleError.DpstProd.BNAS1328();
        }
        if (CommUtil.isNull(crcycd)) {
        	DpModuleError.DpstComm.BNAS1101();
        }
        if (CommUtil.isNull(drintp)) {
            DpModuleError.DpstComm.BNAS1218();
        }
        if (CommUtil.isNull(ingpcd)) {
            DpModuleError.DpstComm.BNAS1976();
        }
        if (CommUtil.isNull(intrtp)) {
            DpModuleError.DpstComm.BNAS0473();
        }
        //判断纪录是否存在
        KupDppbDfir entity = SysUtil.getInstance(KupDppbDfir.class);
        entity = KupDppbDfirDao.selectOne_odb1(prodcd, crcycd, drintp, ingpcd, intrtp, false);
        if (CommUtil.isNull(entity)) {
            throw DpModuleError.DpstComm.BNAS1977();
        }
        //删除原纪录
        KupDppbDfirDao.deleteOne_odb1(prodcd, crcycd, drintp, ingpcd, intrtp);

    }
}
