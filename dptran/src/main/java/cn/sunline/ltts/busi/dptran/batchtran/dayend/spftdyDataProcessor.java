
package cn.sunline.ltts.busi.dptran.batchtran.dayend;
import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.ds.iobus.servicetype.ds.IoDsManage;
import cn.sunline.edsp.busi.ds.iobus.type.DsComplex.KubDayProf;
import cn.sunline.edsp.busi.ds.iobus.type.DsEnumType.E_DEALST;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbad;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaSbadDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Spftdy.Input;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Spftdy.Property;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoSaveIoTransBill;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DrawDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;	
	 /**
	  * 日结分润
	  * 日终切日后：
	
	汇总需分润总金额
	
	记账
	  * @author 
	  * @Date 
	  */

public class spftdyDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Spftdy.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Spftdy.Property, cn.sunline.edsp.busi.ds.iobus.type.DsComplex.KubDayFrTotalList> {
	
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
		public void process(String jobId, int index, cn.sunline.edsp.busi.ds.iobus.type.DsComplex.KubDayFrTotalList dataItem, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Spftdy.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Spftdy.Property property) {
			
			String trandt = CommTools.getBaseRunEnvs().getLast_date();
			
			// 生成新的流水
			String newTrxnSeq = CommTools.createNewTrxnSeq();
			
			CommTools.getBaseRunEnvs().setTrxn_date(trandt);
			
			CommTools.getBaseRunEnvs().setTrxn_seq(newTrxnSeq);
			CommTools.getBaseRunEnvs().setMain_trxn_seq(newTrxnSeq);
			
			// 分润金额
			BigDecimal prftam = dataItem.getPrftam();
			
			if(CommUtil.compare(BigDecimal.ZERO, prftam) != 0) {
				
				// 获取基本信息
				KnaSbad knaSbad = KnaSbadDao.selectOne_odb2(dataItem.getAgntid(), false);
				if(CommUtil.isNull(knaSbad)) {
					throw DpModuleError.DpAcct.AT020054(dataItem.getAgntid());
				}
				
				// 配置不同渠道 ：点刷和新即付宝 具体值需要在数据库配置
				KnpParameter sbrandPara = KnpParameterDao.selectOne_odb1("DpParam.EndDay", "share", "profit", knaSbad.getSbrand(), false);
				if(CommUtil.isNull(sbrandPara)) {
					throw DpModuleError.DpAcct.AT020056(knaSbad.getSbrand());
				}
				
				KnaAcct knaAcct = KnaAcctDao.selectOne_odb1(knaSbad.getAcctno(), false);
				if(CommUtil.isNull(knaAcct)) {
					throw DpModuleError.DpAcct.AT020055(knaSbad.getAcctno());
				}
				
				/*
				 *  点刷和新即付宝：
				 *  	Cr	600103手续费收入（红字）		-1.5
				 *  	Cr	220211应付账款-代理商存款		1.5
				 *  其它通道：
				 *  	Dr	64010301分润成本		1.5
						Cr	220211应付账款-代理商存款	0.5	
						...
				 */
				
				IoAccounttingIntf intf = SysUtil.getInstance(IoAccounttingIntf.class);
				intf.setTranms("日结分润计提");					// 交易信息
				intf.setTrsqtp(E_ATSQTP.ACCOUNT);				// 会计流水类型
				intf.setAtowtp(E_ATOWTP.IN);					// 会计主体类型
				intf.setTrandt(trandt);							// 交易日期
				intf.setCrcycd(knaAcct.getCrcycd());			// 币种
				intf.setCuacno(sbrandPara.getParm_value2());	// 记账账号
				intf.setDtitcd(sbrandPara.getParm_value2());	// 核算口径
				intf.setAcctno(sbrandPara.getParm_value2());	// 负债账号
				intf.setProdcd(sbrandPara.getParm_value2());	// 产品编号
				intf.setAcctdt(trandt);							// 应入账日期
				intf.setAcctbr(knaAcct.getBrchno());			// 账务机构
				intf.setBltype(E_BLTYPE.BALANCE);				// 余额属性
				intf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());	// 主交易流水
				intf.setToacct(knaAcct.getAcctno());			// 对方账号
				intf.setToacna(knaAcct.getAcctna());			// 对方户名
				
				if(CommUtil.equals("1", sbrandPara.getParm_value1())) {
					intf.setAmntcd(E_AMNTCD.CR);					// 借贷标志
					intf.setTranam(prftam.negate());				// 交易金额
				}else if(CommUtil.equals("0", sbrandPara.getParm_value1())) {
					intf.setAmntcd(E_AMNTCD.DR);					// 借贷标志
					intf.setTranam(prftam);							// 交易金额
				}
				
				SysUtil.getInstance(IoSaveIoTransBill.class).SaveKnlAcsq(intf, E_BLNCDN.C);
				
				// 存入
				if(CommUtil.compare(BigDecimal.ZERO, prftam) < 0) {
				
					SaveDpAcctIn cplSaveAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
					cplSaveAcctIn.setAcctno(knaAcct.getAcctno());
					cplSaveAcctIn.setCrcycd(knaAcct.getCrcycd());
					cplSaveAcctIn.setCustac(knaAcct.getCustac());
					cplSaveAcctIn.setIntrcd(CommTools.getBaseRunEnvs().getTrxn_code());
					cplSaveAcctIn.setNegafg(E_YES___.NO);
					cplSaveAcctIn.setOpacna(sbrandPara.getParm_value3());
					cplSaveAcctIn.setRemark("日结分润计提存入");
					cplSaveAcctIn.setSmrycd("RJFRJTCR");
					cplSaveAcctIn.setSmryds("存现");
					cplSaveAcctIn.setStrktg(E_YES___.YES);
					cplSaveAcctIn.setToacct(sbrandPara.getParm_value2());
					cplSaveAcctIn.setTranam(prftam);
					cplSaveAcctIn.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));
					SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(cplSaveAcctIn);
					
					// 支取
				}else if(CommUtil.compare(BigDecimal.ZERO, prftam) > 0) {
					
					DrawDpAcctIn cplDrawAcctIn = SysUtil.getInstance(DrawDpAcctIn.class);
					cplDrawAcctIn.setAcctno(knaAcct.getAcctno());
					cplDrawAcctIn.setCrcycd(knaAcct.getCrcycd());
					cplDrawAcctIn.setCustac(knaAcct.getCustac());
					cplDrawAcctIn.setIntrcd(CommTools.getBaseRunEnvs().getTrxn_code());
					cplDrawAcctIn.setOpacna(sbrandPara.getParm_value3());
					cplDrawAcctIn.setRemark("日结分润计提冲正");
					cplDrawAcctIn.setSmrycd("RJFRJTCZ");
					cplDrawAcctIn.setSmryds("存现冲正");
					cplDrawAcctIn.setStrktg(E_YES___.YES);
					cplDrawAcctIn.setToacct(sbrandPara.getParm_value2());
					cplDrawAcctIn.setTranam(prftam.negate());
					cplDrawAcctIn.setDetlsq(Long.valueOf(BusiTools.getSequence("detlsq", 10, "0")));
					SysUtil.getInstance(DpAcctSvcType.class).addDrawAcctDp(cplDrawAcctIn);
				}
				
				// 平衡性检查
				SysUtil.getInstance(IoCheckBalance.class).checkBalance(trandt,newTrxnSeq, null);
				
				KubDayProf kubDayProf = SysUtil.getInstance(KubDayProf.class);
				kubDayProf.setAgntid(dataItem.getAgntid());
				kubDayProf.setAgntna(dataItem.getAgntna());
				kubDayProf.setSbrand(knaSbad.getSbrand());
				kubDayProf.setBradna(knaSbad.getBradna());
				kubDayProf.setFr_date(trandt);
				kubDayProf.setPayena(knaAcct.getAcctna());
				kubDayProf.setFr_amt(dataItem.getProfitamt());
				kubDayProf.setChild_amt(dataItem.getChild_amt());
				kubDayProf.setPrftam(prftam);
				kubDayProf.setRemark("日结分润计提");
				SysUtil.getInstance(IoDsManage.class).insertKubDayProf(kubDayProf, null);
			
			}
			
			// 记录处理
			DpAcctDao.updKubDayFrTotalDealst(dataItem.getAgntid(), trandt, E_DEALST.SUCCESS, 
					CommTools.getBaseRunEnvs().getTrxn_code(), null);
		}
		
		@Override
		public void jobExceptionProcess(String taskId, Input input, Property property, String jobId,
				cn.sunline.edsp.busi.ds.iobus.type.DsComplex.KubDayFrTotalList dataItem, Throwable t) {
			
			String trandt = CommTools.getBaseRunEnvs().getLast_date();
			
			// 记录处理
			DpAcctDao.updKubDayFrTotalDealst(dataItem.getAgntid(), trandt, E_DEALST.FAIL,
					CommTools.getBaseRunEnvs().getTrxn_code(), t.getMessage());
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.edsp.busi.ds.iobus.type.DsComplex.KubDayFrTotalList> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Spftdy.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Spftdy.Property property) {
			
			String trandt = CommTools.getBaseRunEnvs().getLast_date();
			
			Params params = new Params();
			params.add("trandt", trandt);
			params.add("dealst", E_DEALST.SUCCESS);
			return new CursorBatchDataWalker<cn.sunline.edsp.busi.ds.iobus.type.DsComplex.KubDayFrTotalList>(DpAcctDao.namedsql_selAllAgntDayFrTotal, params);
		}

}


