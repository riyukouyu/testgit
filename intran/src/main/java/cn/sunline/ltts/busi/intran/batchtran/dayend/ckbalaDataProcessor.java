package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.type.InQueryTypes.BalanceOfCmda;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INPTSR;

	 /**
	  * 单边账日终处理
	  *
	  */

public class ckbalaDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Ckbala.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Ckbala.Property> {
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Ckbala.Input input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Ckbala.Property property) {



			String trandt = DateTools2.getDateInfo().getLastdt();//获取上日日期
			CommTools.getBaseRunEnvs().setTrxn_date(trandt);

			//获取补平的业务代码
			KnpParameter para = KnpParameterDao.selectOne_odb1("InParm.ckbala","%", "%", "%", true);
			


			List<BalanceOfCmda> cmdbf = null;
			cmdbf = InacSqlsDao.CheckBalanceOfGlvcByTrandtAndTransq(trandt,CommTools.getBaseRunEnvs().getBusi_org_id(), false);
			if(CommUtil.isNull(cmdbf)){
				//如果已经平衡则直接跳出
				return;
			}
			
			/*===============准备清算数据==========*/
			String centbr = null;
			//获取除省中心外数据
			for(BalanceOfCmda cmd :cmdbf){
								
				//重新设置交易流水
				 MsSystemSeq.getTrxnSeq();
				// 获取中心法人清算机构
		        IoBrchInfo ctbrInfo = SysUtil.getRemoteInstance(IoSrvPbBranch.class).getCenterBranch(cmd.getAcctbr());
		        centbr = ctbrInfo.getBrchno();
				
				IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
				
				if(CommUtil.compare(cmd.getTranam(),BigDecimal.ZERO)>0){
					//贷方多记，补借方
			
					iaAcdrInfo.setTrantp(E_TRANTP.TR);
					iaAcdrInfo.setBusino(para.getParm_value1());// 9912210109
					iaAcdrInfo.setAcbrch(centbr);//默认补到省清算中心
					iaAcdrInfo.setInptsr(E_INPTSR.GL00);
					iaAcdrInfo.setTranam(cmd.getTranam());// 记账金额
					iaAcdrInfo.setCrcycd(cmd.getCrcycd());// 币种
					iaAcdrInfo.setDscrtx("流水"+cmd.getMntrsq()+"单边账，系统补平！");
					SysUtil.getInstance(IoInAccount.class).ioInAcdr(iaAcdrInfo);
					
				}else{
					
					//借方多级，补贷方
					IaAcdrInfo iaAcdrInfo2 = SysUtil.getInstance(IaAcdrInfo.class);
					iaAcdrInfo2.setTrantp(E_TRANTP.TR);
					iaAcdrInfo2.setBusino(para.getParm_value2());	
					iaAcdrInfo.setInptsr(E_INPTSR.GL00);
					iaAcdrInfo2.setTranam(cmd.getTranam().abs()); // 记账金额
					iaAcdrInfo2.setAcbrch(centbr);
					iaAcdrInfo2.setCrcycd(cmd.getCrcycd());
					iaAcdrInfo.setDscrtx("流水"+cmd.getMntrsq()+"单边账，系统补平！");

					SysUtil.getInstance(IoInAccount.class).ioInAccr(iaAcdrInfo2);// 内部户贷方服
 					

				}

			}
	 }
}


