package cn.sunline.ltts.busi.intran.batchtran.dayend;



import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.KnsCler;
import cn.sunline.ltts.busi.in.type.InQueryTypes.BalanceOfCmda;
import cn.sunline.ltts.busi.in.type.InQueryTypes.KnsAcsqGroupInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INPTSR;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

	 /**
	  * 系统内机构清算
	  *
	  */

public class clerbrDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Clerbr.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Clerbr.Property> {
	private static final BizLog bizlog = BizLogUtil
			.getBizLog(clerbrDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Clerbr.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Clerbr.Property property) {


			String trandt = DateTools2.getDateInfo().getLastdt();//获取上日日期,任务在日切之后
			CommTools.getBaseRunEnvs().setTrxn_date(trandt);

			//获取系统内机构清算业务代码
			 KnpParameter para = KnpParameterDao.selectOne_odb1("InParm.clearbrch","clerbr", "%", "%", true);
			

			//删除单日未清算数据，支持重跑
			InacSqlsDao.delKnsClerDetl(trandt);
			InacSqlsDao.delKnsCler(trandt, E_YES___.NO);

			List<KnsAcsqGroupInfo> info = null;
			
			//获取非省中心机构
			info = InacSqlsDao.selBrchnoForClear( false);

				
/*			List<BalanceOfCmda> cmdbf = null;
			cmdbf = InacSqlsDao.CheckBalanceOfGlvcByAcctbr(trandt, false);
			if(CommUtil.isNull(cmdbf)){
				//如果当日未发生跨法人、跨系统账务且机构账务已经平衡 则直接跳出
				return;
			}*/
		
			
			/*===============准备清算数据==========*/
 			//清除上日数据
			InacSqlsDao.deleteKnsBrclTrsq(trandt);
			//插入当日数据
			
			InacSqlsDao.insertKnsBrclTrsq(trandt);
			//插入需要清算机构的账务
			for(KnsAcsqGroupInfo cmd :info){
			
				//插入明细流水
				InacSqlsDao.insKnsClerFromKnsAcsqDetl(trandt,cmd.getAcctbr());
				//汇总借方数据
				InacSqlsDao.insKnsClerFromKnsAcsqDr(trandt,cmd.getAcctbr());
				//汇总贷方数据
				InacSqlsDao.insKnsClerFromKnsAcsqCr(trandt,cmd.getAcctbr());
				
			}
				
			List<KnsCler> lstClerBrchInfo = InacSqlsDao.queryClerBrchList(trandt,  false);
			int i=0;
			if (CommUtil.isNotNull(lstClerBrchInfo)) {
				for (KnsCler clerBrchInfo : lstClerBrchInfo) {
					 i++;
					//重新设置交易流水
					 MsSystemSeq.getTrxnSeq();
					
					bizlog.debug("第["+i+"]记录");
					bizlog.debug("日期["+trandt+"]");
					bizlog.debug("流水["+CommTools.getBaseRunEnvs().getTrxn_seq()+"]");
					bizlog.debug("下级清算机构["+clerBrchInfo.getLwbrch()+"]");
					bizlog.debug("上级清算机构["+clerBrchInfo.getClerbr()+"]");
					bizlog.debug("清算级别["+clerBrchInfo.getClerlv()+"]，["+clerBrchInfo.getClerlv().getLongName()+"]");
					bizlog.debug("币种["+clerBrchInfo.getCrcycd()+"]");
					bizlog.debug("记账金额["+clerBrchInfo.getTranam()+"]");
					
					bizlog.debug("交易机构["
							+ CommTools.getBaseRunEnvs().getTrxn_branch() + "]");
					bizlog.debug("开始清算。");
					
					String drbrch = "";//借方机构
					String crbrch = "";//贷方机构
					if(CommUtil.compare(clerBrchInfo.getTranam(), BigDecimal.ZERO)>0){
						//下级机构借方多  则 D:上级机构 C 下级机.构
						drbrch = clerBrchInfo.getClerbr();
						crbrch = clerBrchInfo.getLwbrch() ;
						
					}else{
						//下级机构贷方多  则 D:下级机构 C 上级机构
						drbrch = clerBrchInfo.getLwbrch();
						crbrch = clerBrchInfo.getClerbr();					
					}
						
						
						
						// 
						IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);

						iaAcdrInfo.setTrantp(E_TRANTP.TR);
						iaAcdrInfo.setBusino(para.getParm_value1());
						iaAcdrInfo.setAcbrch(drbrch);
						iaAcdrInfo.setInptsr(E_INPTSR.GL00);
 						iaAcdrInfo.setTranam(clerBrchInfo.getTranam().abs());// 记账金额
						iaAcdrInfo.setCrcycd(clerBrchInfo.getCrcycd());// 币种
 
						SysUtil.getInstance(IoInAccount.class).ioInAcdr(
								iaAcdrInfo);
						bizlog.debug("----------调用内部户借方记账服务成功----------");
					
						IaAcdrInfo iaAcdrInfo2 = SysUtil.getInstance(IaAcdrInfo.class);
						iaAcdrInfo2.setTrantp(E_TRANTP.TR);
						iaAcdrInfo2.setBusino(para.getParm_value1());	
						iaAcdrInfo.setInptsr(E_INPTSR.GL00);
 						iaAcdrInfo2.setTranam(clerBrchInfo.getTranam().abs()); // 记账金额
						iaAcdrInfo2.setAcbrch(crbrch);
						iaAcdrInfo2.setCrcycd(clerBrchInfo.getCrcycd());


						SysUtil.getInstance(IoInAccount.class).ioInAccr(
								iaAcdrInfo2);// 内部户贷方服
						bizlog.debug("----------调用内部户贷方记账服务成功----------");
					
					//更新清算状态
						InacSqlsDao.updateClerData(
							trandt,
							clerBrchInfo.getClerbr(),
							clerBrchInfo.getCrcycd(),
							clerBrchInfo.getClerlv(),
							CommTools.getBaseRunEnvs().getMain_trxn_seq(), E_YES___.YES);
					
				}
			}
			
			List<BalanceOfCmda> cmdas = null;
			cmdas = InacSqlsDao.CheckBalanceOfGlvcByAcctbr(trandt, false);
 
 
		if (CommUtil.isNotNull(cmdas)) {
			throw InError.comm.E0003("系统内清算失败,账务不平["+cmdas+"]");
		}			
		//删除30天前数据
		 String backupdt = DateTimeUtil.dateAdd("day", trandt, -30);
		 InacSqlsDao.delHistKnsCler(backupdt);
	}

}


