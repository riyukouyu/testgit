
package cn.sunline.ltts.busi.dp.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.namedsql.CustInfoCountSqlDao;
import cn.sunline.ltts.busi.dp.type.CustInfoCountType.CountAccttpThisYear;
import cn.sunline.ltts.busi.dp.type.CustInfoCountType.CountAgeGroup;
import cn.sunline.ltts.busi.dp.type.CustInfoCountType.CountCustInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

/**
 * 统计客户信息
 * 20180115
 * @author 嵇志荣
 *
 */
public class custct {

	public static void selCountCustInfo( final cn.sunline.ltts.busi.dp.trans.intf.Custct.Input input,  final cn.sunline.ltts.busi.dp.trans.intf.Custct.Output output){
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();//获取法人
		CountCustInfo countCustInfo = SysUtil.getInstance(CountCustInfo.class);//实例化客户信息
		
		switch (input.getCountp()) {
		case accyer:
			//统计客户账户分类
			Options<CountAccttpThisYear> optionsAccttp = new DefaultOptions<CountAccttpThisYear>();// 定义输出集合
			optionsAccttp.setValues(CustInfoCountSqlDao.selCountAccttpThisYear(corpno, false));
			countCustInfo.setAccyer(optionsAccttp);//统计账户分类
			break;
		case agegrp:
			//年龄分段统计
			Options<CountAgeGroup> optionsAgegrp = new DefaultOptions<CountAgeGroup>();
			optionsAgegrp.setValues(CustInfoCountSqlDao.selCountAgeGroup(corpno, false));
			countCustInfo.setAgegrp(optionsAgegrp);//统计
			break;
		default:
			throw DpModuleError.DpstComm.E9990("请选择统计类目！");
		}
		output.setResult(countCustInfo);
	}
}
