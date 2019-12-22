package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.iobus.type.dp.IoProRateSel.ProInfoAll;
import cn.sunline.ltts.busi.pb.namedsql.intr.ProintrSelDao;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class allprd {

public static void selAllprd( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Allprd.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Allprd.Output output){
	String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
	List<ProInfoAll> infos = ProintrSelDao.selProdInfoAll(corpno ,false);
	Options<ProInfoAll> prodInfos = new DefaultOptions<ProInfoAll>();
	prodInfos.addAll(infos);
	output.setProdInfo(prodInfos);
}
}
