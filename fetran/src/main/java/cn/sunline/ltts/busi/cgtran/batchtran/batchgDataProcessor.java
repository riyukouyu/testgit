package cn.sunline.ltts.busi.cgtran.batchtran;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.cg.charg.ChargeProc;
import cn.sunline.ltts.busi.cg.namedsql.charg.PBChargeRegisterDao;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgRgst;
import cn.sunline.ltts.busi.cg.tables.PBCharge.KcbChrgRgstDao;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgChargeFee_IN;
import cn.sunline.ltts.busi.cg.type.CgComplexType.CgPlKouf;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSTRFG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_OPRFLG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_YES___;

	 /**
	  * 批量收费
	  *

测试步骤
1、准备数据
update kcb_chrg_rgst a set  a.recvfg='0' ,a.arrgam=2 where  a.custno in ('10000000036');

2、提前查询余额及明细
SELECT  * from  kna_acct  a where a.custno='10000000036';
select  * from knl_bill a where a.acctno='60000000000000000001017';

3、执行批量交易

4、再次查看余额及明细
说明：同3



	  */

	public class batchgDataProcessor extends
	  AbstractBatchDataProcessor<cn.sunline.ltts.busi.cgtran.batchtran.intf.Batchg.Input, cn.sunline.ltts.busi.cgtran.batchtran.intf.Batchg.Property, cn.sunline.ltts.busi.cg.type.CgComplexType.CgPlKouf> {
	  
		private static BizLog bizlog = BizLogUtil.getBizLog(batchgDataProcessor.class);
	
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.cg.type.CgComplexType.CgPlKouf dataItem, cn.sunline.ltts.busi.cgtran.batchtran.intf.Batchg.Input input, cn.sunline.ltts.busi.cgtran.batchtran.intf.Batchg.Property property) {
			
			bizlog.debug(">>>>>>>>>>>>>>>>>批量收费开始>>>>>>>>>>>>");
			
			String sTrandt = DateTools2.getDateInfo().getLastdt();//获取上日日期,任务在日切之后
			CommTools.getBaseRunEnvs().setTrxn_date(sTrandt);
			/**
			 *  cn.sunline.ltts.core.api.exception.Exception: [DeptAcct.E9999][逻辑错误：应入账日期[20180627]不能小于交易日期[20180628]]
				at cn.sunline.ltts.busi.sys.errors.DpError$DeptAcct.E9999(DpError.java:1474) ~[classes/:?]
				at cn.sunline.ltts.busi.dp.serviceimpl.IoAccountSvcTypeImpl.ioAccountting(IoAccountSvcTypeImpl.java:130) ~[dp-2.0.0-SNAPSHOT.jar:2.0.0-SNAPSHOT]
				
				该服务会做日期判断，故这做日期的处理
			 */
			CommTools.getBaseRunEnvs().setInitiator_date(sTrandt);
			//
			
			//若之前批量扣费失败，且客户账户余额未动，无需扣款
			if(CommUtil.compare(dataItem.getUpbldt(), dataItem.getLschda()) < 0 &&
					CommUtil.compare(dataItem.getChgdat(), sTrandt) != 0 &&
					CommUtil.compare(dataItem.getLschda(), dataItem.getChgdat()) >= 0){
				return;
			}
			
			//收费输入
			CgChargeFee_IN cplChFeeIn = SysUtil.getInstance(CgChargeFee_IN.class);
			
			cplChFeeIn.setOprflg(E_OPRFLG.SEQNO); /* 按计费流水+序号收 */
			cplChFeeIn.setTrnseq(dataItem.getTrnseq()); //柜员流水
			cplChFeeIn.setEvrgsq(dataItem.getEvrgsq()); //事件登记序号
			cplChFeeIn.setIfflag(E_YES___.YES); //是否标志 默认是
			cplChFeeIn.setTrancy(dataItem.getCrcycd()); //交易币种
			cplChFeeIn.setTrandt(dataItem.getTrandt()); //交易日期
			cplChFeeIn.setCstrfg(E_CSTRFG.TRNSFER); //现转标志
			
			cplChFeeIn.setDecuac(dataItem.getAcctno()); //客户账号
			cplChFeeIn.setCustno(dataItem.getCustno());//客户号
			cplChFeeIn.setAcclam(dataItem.getArrgam()); //实收金额
			
			bizlog.debug(">>>>>>>>>>>>>>>cplChFeeIn[%s]", cplChFeeIn);
			
			try{
				ChargeProc.chargeFee(cplChFeeIn); //收费处理
			}catch(Exception e){
				bizlog.error("客户号[%s]批量收费失败，失败原因[%s]",e, dataItem.getAcctno(), e.getMessage());
				
				//mod songlw 更新错误信息			
				KcbChrgRgst tblKcb_chrg_rgst = KcbChrgRgstDao.selectOne_odb1(dataItem.getTrandt(), dataItem.getTrnseq(), dataItem.getEvrgsq(),true);

				tblKcb_chrg_rgst.setTrandt(dataItem.getTrandt());
				tblKcb_chrg_rgst.setTrnseq(dataItem.getTrnseq());
				tblKcb_chrg_rgst.setEvrgsq(dataItem.getEvrgsq());
				tblKcb_chrg_rgst.setArgrsn(StringUtil.maxstr(e.getMessage(), 200));
				
				KcbChrgRgstDao.updateOne_odb1(tblKcb_chrg_rgst);
				
				return;
			}
			
			bizlog.debug(">>>>>>>>>>>>>>>>批量收费结束>>>>>>>>>>>>>>");
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.cg.type.CgComplexType.CgPlKouf> getBatchDataWalker(cn.sunline.ltts.busi.cgtran.batchtran.intf.Batchg.Input input, cn.sunline.ltts.busi.cgtran.batchtran.intf.Batchg.Property property) {

			bizlog.debug(">>>>>>>>>>>>BatchDataWalker begin>>>>>>>>>>>>>>>");
			
			//设置参数
			Params parm = new Params();
			parm.add("corpno", BusiTools.getFrdm(KcbChrgRgst.class));
			parm.add("chgdat", CommTools.getBaseRunEnvs().getLast_date());
			
			bizlog.debug(">>>>>>>>>>>>BatchDataWalker end>>>>>>>>>>>>>>>>>");
			
			return new CursorBatchDataWalker<CgPlKouf>(PBChargeRegisterDao.namedsql_selChargeRegistrForBatchCharge, parm);
		
		}

}


