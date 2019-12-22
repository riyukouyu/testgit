package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.in.namedsql.InTranOutDao;
import cn.sunline.ltts.busi.in.tables.In.KnsCmbk;
import cn.sunline.ltts.busi.in.tables.In.KnsCmbkDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPaya;
import cn.sunline.ltts.busi.in.tables.In.KnsPayaDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPayd;
import cn.sunline.ltts.busi.in.tables.In.KnsPaydDao;
import cn.sunline.ltts.busi.in.tables.In.KnsStrk;
import cn.sunline.ltts.busi.in.tables.In.KnsStrkDao;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CMBK_TRANST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYAST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYATP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYDST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_STRKST;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * @author chenjk
 *         <p>
 *         <li>2016-07-12 09：06</li>
 *         <li>套平入账垃圾数据清除-日终</li>
 *         </p>
 * */
public class upclrbDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.intf.Upclrb.Input, cn.sunline.ltts.busi.intran.batchtran.intf.Upclrb.Property> {
  
	private static final BizLog bizlog = BizLogUtil.getBizLog(upclrbDataProcessor.class);
	
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.intran.batchtran.intf.Upclrb.Input input, cn.sunline.ltts.busi.intran.batchtran.intf.Upclrb.Property property) {
		 bizlog.debug("==========套平入账垃圾数据清除-日终处理开始==========");
		 
		 String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		 
		 dealCmbkData(trandt);  //传票登记簿垃圾数据清除
		 dealStrkData(trandt);  //隔日错账登记簿垃圾数据清除
		 
		 bizlog.debug("==========套平入账垃圾数据清除-日终处理成功==========");
	}
	 
	 //传票登记簿垃圾数据清除
	 private void dealCmbkData(String trandt){
		 
		 bizlog.debug("套平入账垃圾数据清除-日终处理开始==========");
		 
		 //检索出当天所有状态为0：录入；3：复核的记录
		 //TODO 因本任务由日终改为在联机交易时处理，sql语句中日期条件由等于trandt改为了小于trandt，若重新使用本方法，需调整回来
		 List<KnsCmbk> knsCmbkList = InTranOutDao.selCmbkRubbish(trandt, false);
		
		 if(CommUtil.isNull(knsCmbkList)){
			 
			 bizlog.debug(">>>>>>>>>>传票登记簿无未复核记录<<<<<<<");
		 }else{
			 
			 bizlog.debug(">>>>>>>>>>传票登记簿有" + knsCmbkList.size() + "条数据需要处理<<<<<<<");
			 
			 int count = 1; //记录处理第几条记录，日志打印用
			 for(KnsCmbk knsCmbk : knsCmbkList){
				 
				knsCmbk.setIavcst(E_CMBK_TRANST._2); // 
				KnsCmbkDao.updateOne_kns_cmbk_odx1(knsCmbk);
				
				//挂账登记簿处理
				if(knsCmbk.getPayatp() == E_PAYATP._1){
					List<KnsPaya> knsPayaList = KnsPayaDao.selectAll_kns_paya_odx5(knsCmbk.getAcstno(), knsCmbk.getPayseq(), trandt, true);
					
					for(KnsPaya knsPaya : knsPayaList){
						
						//已删除数据过滤
						if(knsPaya.getPayast() == E_PAYAST.ZF){
							continue;
						}
						
						//传票未复核，若对应挂账状态为结清或销账或入账则报错，即不为3：未复核,4：已复核,5：作废报错
						if(knsPaya.getPayast() != E_PAYAST.WFH && knsPaya.getPayast() != E_PAYAST.YFH){
							throw InError.comm.E0011(knsPaya.getPayasq());
						}
						
						knsPaya.setPayast(E_PAYAST.ZF);
						KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
					}
				}
				
				//销账登记簿处理
				if(knsCmbk.getPayatp() == E_PAYATP._2){
					List<KnsPayd> knsPaydList = KnsPaydDao.selectAll_kns_payd_odx5(knsCmbk.getAcstno(), knsCmbk.getPayseq(), trandt, true);
					
					for(KnsPayd knsPayd : knsPaydList){
						
						//已删除数据过滤
						if(knsPayd.getPaydst() == E_PAYDST.ZF){
							continue;
						}
						
						//传票未复核，若对应挂账状态为结清或销账或入账则报错，即不为3：未复核,4：已复核,5：作废报错
						if(knsPayd.getPaydst() != E_PAYDST.WFH && knsPayd.getPaydst() != E_PAYDST.YFH){
							throw InError.comm.E0012(knsPayd.getPaydsq());
						}
						
						knsPayd.setPaydst(E_PAYDST.ZF);
						KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
					}
				}
				
				//打印当前处理记录信息，方便排查
				bizlog.debug(">>>>>>>>>>第" + count + "条传票，套号" + knsCmbk.getAcstno() + "处理成功<<<<<<<");
				count++;
			 }
			 
		 }
		 bizlog.debug("==========套平入账垃圾数据清除-日终处理结束");
	 }
	 
	 //隔日错账登记簿垃圾数据清除
	 private void dealStrkData(String trandt){
		 bizlog.debug("隔日错账冲正垃圾数据清除-日终处理开始==========");
		 
		 //检索出当日所有状态为 3：未复核， 4：已复核的记录
		 //TODO 因本任务由日终改为在联机交易时处理，sql语句中日期条件由等于trandt改为了小于trandt，若重新使用本方法，需调整回来
		 List<KnsStrk> knsStrkList = InTranOutDao.selStrkRubbish(trandt, false);
		 if(CommUtil.isNull(knsStrkList)){
				 
			 bizlog.debug(">>>>>>>>>>隔日错账冲正登记簿无未复核记录<<<<<<<");
		 }else{
			 
			 bizlog.debug(">>>>>>>>>>隔日错账冲正登记簿有" + knsStrkList.size() + "条数据需要处理<<<<<<<");
			 
			 int count = 1; //记录处理第几条记录，日志打印用
			 for(KnsStrk knsStrk :knsStrkList){
				 
				knsStrk.setStrkst(E_STRKST.ZF);
				KnsStrkDao.updateOne_kns_strk_odx1(knsStrk);
				
				//打印当前处理记录信息，方便排查
				bizlog.debug(">>>>>>>>>>第" + count + "条隔日错账冲正记录，套号" + knsStrk.getNumbsq() + "处理成功<<<<<<<");
				count++;
			 }
		 
		 }
		 
		 bizlog.debug("==========隔日错账冲正垃圾数据清除-日终处理结束");
	 }

}


