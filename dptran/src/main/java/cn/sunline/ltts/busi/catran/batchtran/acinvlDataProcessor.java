package cn.sunline.ltts.busi.catran.batchtran;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.ca.namedsql.CaBatchTransDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcalDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccs;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAccsDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCuadDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCustDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpac;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpacDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;

	 /**
	  * 电子账户作废
	  *
	  */

public class acinvlDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.catran.batchtran.intf.Acinvl.Input, cn.sunline.ltts.busi.catran.batchtran.intf.Acinvl.Property, cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust> {
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust dataItem, cn.sunline.ltts.busi.catran.batchtran.intf.Acinvl.Input input, cn.sunline.ltts.busi.catran.batchtran.intf.Acinvl.Property property) {
			
			if(CommUtil.isNull(dataItem)){
				return ;
			}
			
			//电子账号ID
			String custac = dataItem.getCustac();
			
			//删除电子账户状态
			KnaCuadDao.deleteOne_knaCuadOdx1(custac);
			
			//从电子账户表中删除数据
			KnaCustDao.deleteOne_odb1(custac);
			
			//从外部卡与客户账号对照表删除数据
			List<KnaCacd> tblKnaCacd = KnaCacdDao.selectAll_odb5(custac, false);
			if(CommUtil.isNotNull(tblKnaCacd)){
				KnaCacdDao.delete_odb5(custac);
			}
			
			//从账户别名关系表删除数据
			List<KnaAcal> tblKnaAcal = KnaAcalDao.selectAll_odb5(custac, false);
			if(CommUtil.isNotNull(tblKnaAcal)){
				KnaAcalDao.delete_odb5(custac);
			}
			
			//从卡客户账号对照表删除数据
			List<KnaAcdc> tblKnaAcdc = KnaAcdcDao.selectAll_odb3(custac, false);
			if(CommUtil.isNotNull(tblKnaAcdc)){
				KnaAcdcDao.delete_odb3(custac);
			}
			
			//从负债账号客户账号对照表删除数据
			List<KnaAccs> tblKnaAccs = KnaAccsDao.selectAll_odb5(custac, false);
			if(CommUtil.isNotNull(tblKnaAccs)){
				KnaAccsDao.delete_odb5(custac);
			}
			
			//从删除数据knb_opac
			List<KnbOpac> tblKnbOpac = KnbOpacDao.selectAll_odb1(custac, false);
			if(CommUtil.isNotNull(tblKnbOpac)){
				KnbOpacDao.delete_odb1(custac);
			}
			
			//根据电子账号获取所有负债账号
			IoDpSrvQryTableInfo dpSrvQryTableInfo = SysUtil.getInstance(IoDpSrvQryTableInfo.class);
			Options<IoDpKnaAcct> dpKnaAcct =dpSrvQryTableInfo.listKnaAcctOdb6(custac, false);
			
			if(CommUtil.isNotNull(dpKnaAcct)){
				
				for(IoDpKnaAcct knaAcct : dpKnaAcct){
					
					//负债账号
					String acctno = knaAcct.getAcctno();
					
					//从负债活期账户信息表删除数据
					dpSrvQryTableInfo.removeKnaAcctOdb1(acctno);
					
					//从负债活期账户支取控制表删除数据
					dpSrvQryTableInfo.removeKnaDrawOdb1(acctno);
					
					//从负债活期账户支取计划表删除数据
					dpSrvQryTableInfo.removeKnaDrawPlanOdb2(acctno);
					
					//从负债活期账户到期信息表删除数据
					dpSrvQryTableInfo.removeKnaAcctMatuOdb1(acctno);
					
				}
				
			}
			
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust> getBatchDataWalker(cn.sunline.ltts.busi.catran.batchtran.intf.Acinvl.Input input, cn.sunline.ltts.busi.catran.batchtran.intf.Acinvl.Property property) {
			Params params = new Params();
			
			params.add("acctst", E_ACCTST.CLOSE);
			params.add("cuacst", E_CUACST.DELETE);
			params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
			
			return new CursorBatchDataWalker<cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust>(CaBatchTransDao.namedsql_selAcInvalidInfos, params);
		}

}


