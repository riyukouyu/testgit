package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccountClearInfo;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLERST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INPTSR;

	 /**
	  * 系统内跨法人清算
	  *
	  */

public class acclinDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acclin.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acclin.Property, cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccountClearInfo> {
	
	private final static BizLog bizlog = BizLogUtil.getBizLog(acclinDataProcessor.class);
	
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccountClearInfo dataItem, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acclin.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acclin.Property property) {
			bizlog.debug("----------会计流水："+dataItem.getMntrsq()+" 进入清算记账----------");
			//重新设置交易流水
			 MsSystemSeq.getTrxnSeq();
			
			
			
			String trandt = dataItem.getTrandt(); //交易日期
			String mntrsq = dataItem.getMntrsq(); //交易流水
			String clersq = CommTools.getBaseRunEnvs().getMain_trxn_seq(); //清算记账流水
			
			//根据交易流水查询清算数据
			List<IoAccountClearInfo> lstKnsClearInfo = InDayEndSqlsDao.selKnsAcsqClinByTransq(trandt, mntrsq, E_CLERST.WAIT, false);
			
			//处理清算数据，进行记账
			if(CommUtil.isNotNull(lstKnsClearInfo) && lstKnsClearInfo.size() > 0){
				
				bizlog.debug("----------会计流水："+dataItem.getMntrsq()+" 开始记账----------");
				
				for(IoAccountClearInfo cplKnsClearInfo : lstKnsClearInfo){
					
					if(E_AMNTCD.DR == cplKnsClearInfo.getAmntcd()){
						// 调用内部户借方记账服务
						IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
						
						iaAcdrInfo.setTrantp(E_TRANTP.TR);
						iaAcdrInfo.setInptsr(E_INPTSR.GL00);
						iaAcdrInfo.setAcctno(cplKnsClearInfo.getAcctno()); // 记账账号
						iaAcdrInfo.setTranam(cplKnsClearInfo.getTranam()); // 记账金额
						iaAcdrInfo.setCrcycd(cplKnsClearInfo.getCrcycd()); // 币种
						iaAcdrInfo.setAcbrch(cplKnsClearInfo.getAcctbr()); //清算机构	
						iaAcdrInfo.setDscrtx("系统内法人清算");
						iaAcdrInfo.setToacct(null); //对方账号
						iaAcdrInfo.setToacna(null); //对方户名
						
						// 内部户借方服务
						SysUtil.getInstance(IoInAccount.class).ioInAcdr(
								iaAcdrInfo);
						
						
					}else if(E_AMNTCD.CR == cplKnsClearInfo.getAmntcd()){
						//系统间往来对应贷方记账
						IaAcdrInfo iaAccrInfo = SysUtil.getInstance(IaAcdrInfo.class);
						iaAccrInfo.setTrantp(E_TRANTP.TR);
						iaAccrInfo.setInptsr(E_INPTSR.GL00);
						iaAccrInfo.setAcctno(cplKnsClearInfo.getAcctno()); // 记账账号
						iaAccrInfo.setTranam(cplKnsClearInfo.getTranam()); // 记账金额
						iaAccrInfo.setAcbrch(cplKnsClearInfo.getAcctbr()); // 币种
						iaAccrInfo.setCrcycd(cplKnsClearInfo.getCrcycd()); //清算机构	
						iaAccrInfo.setToacct(null); //对方账号
						iaAccrInfo.setToacna(null); //对方户名
						iaAccrInfo.setDscrtx("系统内法人清算");

						// 内部户贷方服务
						SysUtil.getInstance(IoInAccount.class).ioInAccr(
								iaAccrInfo);
					}
				}
				
			}
			
			InDayEndSqlsDao.updKnsAcsqClinByTransq(trandt, mntrsq, E_CLERST.SUCCESS, clersq);
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccountClearInfo> getBatchDataWalker(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acclin.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Acclin.Property property) {
			
			//如果不是定时任务则记上日账
			if(E_YES___.YES != input.getBachfg()){
				ApSysDateStru cplDatInfo = DateTools2.getDateInfo();
				CommTools.getBaseRunEnvs().setTrxn_date(cplDatInfo.getLastdt());
//				CommTools.getBaseRunEnvs().setJiaoyirq(cplDatInfo.getLastdt());//jiaoyiriqi() to trandt()
				CommTools.getBaseRunEnvs().setLast_date(cplDatInfo.getBflsdt());
			}
			
			String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
			
			Params params = new Params();
			params.add("clerst", E_CLERST.WAIT);
			params.add("trandt", trandt);
			
			return new CursorBatchDataWalker<IoAccountClearInfo>(
					InDayEndSqlsDao.namedsql_selKnsAcsqClinInfo, params);
		}

}


