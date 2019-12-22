package cn.sunline.ltts.busi.in.inner;

import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.FnsCmbk;
import cn.sunline.ltts.busi.in.tables.In.FnsCmbkDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBusi;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBusiDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnpItem;
import cn.sunline.ltts.busi.in.tables.In.GlKnpItemDao;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_IOFLAG;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 财务往来记账相关处理
 * */
public class InFnsCmbk {

	private static final BizLog bizlog = BizLogUtil.getBizLog(InFnsCmbk.class);
	
	/*
	 * 微众记账特殊处理
	 * 1.存放境内同业款项科目WB1011010100从系统内往来科目WB3002990100过一下
	 */
	public static void transfer(FnsCmbk fnsCmbk){
		
		bizlog.debug("transfer fnsCmbk:[%s]", fnsCmbk);
		List<KnpParameter> paras = KnpParameterDao.selectAll_odb2("FnsCmbk", true);
		String old_busiNo = null;//原业务编码
		String busiNo = null;//转化业务编码
		String itemCd = null;//科目号
		String subSac = null;//子户号
		for(KnpParameter para:paras){
			bizlog.debug("参数表KnpParameter:[%s]", para);
			old_busiNo = para.getParm_value1();
			busiNo = para.getParm_value2();
			//knp_para参数表中存在业务编码的配置，则做转化
			if(CommUtil.isNotNull(old_busiNo) && CommUtil.isNotNull(busiNo) && 
					CommUtil.equals(fnsCmbk.getBusino(), old_busiNo) ){
				//业务代码
				GlKnpBusi glKnpBusi = GlKnpBusiDao.selectOne_odb1(busiNo, false);
				if(CommUtil.isNull(glKnpBusi)){
					throw InError.comm.E0003("业务代码"+busiNo+"在业务编码表中未配置");
				}
				subSac ="";// glKnpBusi.getSubsac();
				itemCd = "";//glKnpBusi.getItemcd();
				//查询科目表
				GlKnpItem glKnpItem = GlKnpItemDao.selectOne_odb1(itemCd, false);
				if(CommUtil.isNull(glKnpItem)){
					throw InError.comm.E0003("科目号"+itemCd+"在科目表中未配置");
				}
				E_IOFLAG ioFlag = glKnpItem.getIoflag();//表内外标志
				
				fnsCmbk.setItemcd(itemCd);//科目号
				fnsCmbk.setSubsac(subSac);//子户号
				fnsCmbk.setBusino(busiNo);//核心业务码
				fnsCmbk.setIoflag(ioFlag);//表内外标志
				
				//借方记一笔
				fnsCmbk.setCommnm(getMaxCommnm(fnsCmbk.getCommsq(), fnsCmbk.getTrandt()));//传票顺序
				fnsCmbk.setAmntcd(E_AMNTCD.DR);//记账方向
				FnsCmbkDao.insert(fnsCmbk);
				bizlog.debug("借方记账fnsCmbk:[%s]", fnsCmbk);
				
				//贷方记一笔
				fnsCmbk.setCommnm(getMaxCommnm(fnsCmbk.getCommsq(), fnsCmbk.getTrandt()));//传票顺序
				fnsCmbk.setAmntcd(E_AMNTCD.CR);//记账方向
				FnsCmbkDao.insert(fnsCmbk);
				bizlog.debug("贷方记账fnsCmbk:[%s]", fnsCmbk);
			}
		}
	}
	
	/*
	 * 根据套账号、记账日期获取最大套票顺序号
	 * 
	 * 默认从1000000001开始
	 * @param String commsq 套账号
	 *        String trandt 记账日期 
	 *        
	 * @return 最大套票顺序号
	 */
	public static String getMaxCommnm(String commsq, String trandt){
		String commnm = InacSqlsDao.queryMaxCommnm(commsq, trandt, false);
		if(CommUtil.isNull(commnm)){
			commnm = "1000000001";
		}else{
			commnm = String.valueOf(Long.valueOf(commnm) + 1);
		}
		return commnm;
	}
	
}
