package cn.sunline.ltts.busi.dptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;

/**
 * 
 * <p>Title:dprepd</p>
 * <p>Description: 预约购买产品输入为空校验</p>
 * 
 * @author XX
 * @date 2018年3月8日
 */
public class dprepd {
    /**
     * 
     * <p>Title:dprepd </p>
     * <p>Description: 预约购买产品输入为空校验</p>
     * 
     * @author XX
     * @date 2018年3月8日
     * @param input
     * @param property
     * @param output
     */
    public static void dprepd(final cn.sunline.ltts.busi.dptran.trans.intf.Dprepd.Input input, final cn.sunline.ltts.busi.dptran.trans.intf.Dprepd.Property property,
            final cn.sunline.ltts.busi.dptran.trans.intf.Dprepd.Output output) {
        // 操作类型
        if (CommUtil.isNull(input.getOperat())) {
            throw DpModuleError.DpstProd.E0010("预约购买产品输入信息操作类型不能为空");
        }

        // 卡号
        if (CommUtil.isNull(input.getCardno())) {
            throw DpModuleError.DpstProd.E0010("预约购买产品输入信息卡号不能为空");
        }
    }
}
