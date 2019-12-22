package cn.sunline.ltts.busi.dptran.trans;

import java.io.File;

import cn.sunline.adp.cedar.base.engine.BatchConfigConstant;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;


public class cupsck {

	private static BizLog bizlog = BizLogUtil.getBizLog(cupsck.class);
	
	public static void dealCupsck( final cn.sunline.ltts.busi.dptran.trans.intf.Cupsck.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Cupsck.Output output){
		bizlog.method(">>>>>>>>>>>SelUnPayRecRst begin>>>>>>>>>>>");
		
		DataArea area = null;
		
		String filesq = "DZ" + CommTools.getBaseRunEnvs().getMain_trxn_seq() +  BusiTools.getSequence("unpayrecrst_seq", 9);
		KnpParameter para = KnpParameterDao.selectOne_odb1("DPTRAN", "RPPATH", "%", "%", true);
		
		String rootpath = para.getParm_value1(); 
		String filetp = E_FILETP.DP021300.getValue();
		String trandt = input.getTrandt();
		
		
		String path = rootpath.concat(filetp).concat(File.separator).concat(trandt).concat(File.separator);
		String filename = "CUPSCK_".concat(trandt).concat(".txt");
		
		area = DataArea.buildWithEmpty();
		area.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE, CommTools.getBaseRunEnvs().getTrxn_date());
		area.getInput().setString("filesq", filesq);
		area.getInput().setString("busseq", filesq);
		area.getInput().setString("trandt", trandt);
		area.getInput().setString("dwname", filename);
		area.getInput().setString("dwpath", path);
		
		KnpParameter para1 = KnpParameterDao.selectOne_odb1("Batch.File", "%", "%", "%", true);
		String user_home = para1.getParm_value2(); //工作目录
		String sep = File.separator; //路径分隔符
		String relaph = E_FILETP.DP021300.getValue() + sep + trandt; //相对路径

		String locaph = user_home + relaph + sep;
		kapb_wjplxxb wjb = SysUtil.getInstance(kapb_wjplxxb.class);
		wjb.setBusseq(CommTools.getBaseRunEnvs().getMain_trxn_seq());	//业务流水
		wjb.setBtchno(filesq);	//文件批次号
		wjb.setAcctdt(input.getTrandt());	//业务日期
		wjb.setTrandt(trandt);	//交易日期
		wjb.setDownna(filename);		//下载文件名
		wjb.setDownph(path);		//下载文件路径
		wjb.setUpfena(filename);	//返回文件名
		wjb.setUpfeph(path);	//返回文件路径
		wjb.setFiletp(E_FILETP.DP021300);	//文件类型
		wjb.setLocaph(locaph);	//文件本地存放路径
		wjb.setBtfest(E_BTFEST.DING); //文件处理状态
		Kapb_wjplxxbDao.insert(wjb);
		
		//调用文件批量组
		BatchUtil.submitAndRunBatchTranGroup(filesq, "0213", area);
		
		//输出
		output.setFilena(filename); //文件名称
		output.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); 			//交易流水
		output.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());			//交易日期
		
		bizlog.method(">>>>>>>>>>>SelUnPayRecRst end>>>>>>>>>>>");
	}
}
