package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.froz.DpUnfrProc;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetl;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDetlDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpUnStopayIn;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_STOPTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_STUNTP;
import cn.sunline.edsp.base.lang.Params;

	 /**
	  * 外部止付到期自动解止
	  *
	  */

public class austunDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Austun.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Austun.Property, cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz> {
	  /**
		 * 批次数据项处理逻辑。
		 * 
		 * @param job 批次作业ID
		 * @param index  批次作业第几笔数据(从1开始)
		 * @param dataItem 批次数据项
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 */
		@Override
		public void process(String jobId, int index, cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz dataItem, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Austun.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Austun.Property property) {
			String  oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//			CommTools.getBaseRunEnvs().setBusi_org_id(dataItem.getCorpno());
			CommTools.getBaseRunEnvs().setTrxn_date(DateTools2.getDateInfo().getLastdt());
			//根据电子账号ID获取电子账号
			IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
			
			IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb1(dataItem.getCustac(), E_DPACST.NORMAL, false);
			
			//根据电子账号查找户名
			IoCaKnaCust tblKnaCust = caSevQryTableInfo.getKnaCustWithLockByCustacOdb1(dataItem.getCustac(), true);
			
			//客户名称
			String custna = tblKnaCust.getCustna();
			
			//电子账号
			String cardno = caKnaAcdc.getCardno();
			
			//查找原止付金额
			KnbFrozDetl tblKnbFrozDetl = KnbFrozDetlDao.selectOne_odb2(dataItem.getFrozno(), dataItem.getFrozsq(), false);
			
			BigDecimal stopam = tblKnbFrozDetl.getFrozbl();
			//调解止服务
			//IoDpFrozSvcType dpFrozSvcType = CommTools.getRemoteInstance(IoDpFrozSvcType.class);
			
			IoDpUnStopayIn dpUnStopayIn = SysUtil.getInstance(IoDpUnStopayIn.class);
			
			dpUnStopayIn.setFrlmtp(dataItem.getFrlmtp()); //限制类型
			dpUnStopayIn.setFrozow(dataItem.getFrozow()); //冻结主体类型
			dpUnStopayIn.setFrozcd(dataItem.getFrozcd()); //冻结分类码
			dpUnStopayIn.setFrcttp(dataItem.getFrcttp()); //冻结证明文书类别
			dpUnStopayIn.setStoptp(E_STOPTP.EXTSTOPAY); //止付类型
			dpUnStopayIn.setStuntp(E_STUNTP.UNSTPL); //止解方式
			dpUnStopayIn.setStopno(dataItem.getFrozno()); //止付序号
			dpUnStopayIn.setCardno(cardno); //电子账号
			dpUnStopayIn.setCustna(custna); //客户名称
			dpUnStopayIn.setCrcycd(dataItem.getCrcycd()); //货币代号
			dpUnStopayIn.setCsextg(dataItem.getCsextg()); //账户钞汇标志
			dpUnStopayIn.setStopam(stopam); //解/止付金额
			dpUnStopayIn.setStbkno(null); //解/止付书编号，暂设置为8888888888888888
//			dpUnStopayIn.setStdptp(dataItem.getFrexog()); //解/止付部门类型
//			dpUnStopayIn.setStladp(dataItem.getFrogna()); //解/止付执法部门
//			dpUnStopayIn.setFrna01(dataItem.getFrna01()); //冻结执法人员1姓名
//			dpUnStopayIn.setIdtp01(dataItem.getIdtp01()); //执法人员1证件种类
//			dpUnStopayIn.setIdno01(dataItem.getIdno01()); //执法人员1证件号码
//			dpUnStopayIn.setFrna02(dataItem.getFrna02()); //冻结执法人员2姓名
//			dpUnStopayIn.setIdtp02(dataItem.getIdtp01()); //执法人员2证件种类
//			dpUnStopayIn.setIdno02(dataItem.getIdno01()); //执法人员2证件号码
			dpUnStopayIn.setSfreas("非紧急止付的到期自动解止"); //解/止付原因
			dpUnStopayIn.setStopdt(dataItem.getFrozdt()); //止付日期
			//查询原止付信息
			KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb1(dataItem.getFrozno(), E_FROZST.VALID, true);
			
			CommTools.getBaseRunEnvs().setTrxn_branch(tblKnbFroz.getTranbr());
			//调用解止服务
			DpUnfrProc.unStopPayDo(dpUnStopayIn, tblKnbFroz);
			//dpFrozSvcType.IoStunpy(dpUnStopayIn);
			
//			CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);
			
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Austun.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Austun.Property property) {
			
			Params params = new Params();
			params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
			params.add("trandt", DateTools2.getDateInfo().getLastdt());
			
			return new CursorBatchDataWalker<KnbFroz>(DpDayEndDao.namedsql_selExtenAuunstInfo, params);
		}

}


