/*
package cn.sunline.edsp.busi.dptran.trans.ca.online.ecct;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.type.CaCustInfo;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.errors.PbError;
import cn.sunline.edsp.base.lang.Page;

public class qrsbat {

	public static void qrySubAcct( 
			final cn.sunline.edsp.busi.dptran.trans.ca.online.ecct.intf.Qrsbat.Input input,  
			final cn.sunline.edsp.busi.dptran.trans.ca.online.ecct.intf.Qrsbat.Property property,  
			final cn.sunline.edsp.busi.dptran.trans.ca.online.ecct.intf.Qrsbat.Output output){
		
		 // 页码
        Long iPageno = CommTools.getBaseRunEnvs().getPage_start();
        // 每页记录数
        Long iPgsize = CommTools.getBaseRunEnvs().getPage_size();
      // 输入项
        String acctno =  input.getAcctno();
        String acctid = input.getAcctid();
		String cardno = input.getCardno(); 
		boolean isAllNull = CommUtil.isNull(cardno)&&CommUtil.isNull(acctno)&&CommUtil.isNull(acctid);
		if(isAllNull) {
			throw CaError.Eacct.E0001("输入条件不能全为空！");
		}
		
		String custac = null;
		if(CommUtil.isNotNull(cardno)) {
			KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(cardno, false);
			if(CommUtil.isNotNull(tblKnaAcdc)) {
				custac = tblKnaAcdc.getCustac();
			}else {
				return;
			}
		}
		
		// 分页查询电子子账户列表信息。
		Page<DpDepoBusiMain.KnaAcct>  KnaAcctInfo = DpAcctDao.selSubAccts(custac, acctno, acctid, (iPageno - 1) * iPgsize, iPgsize, 0, false);
		// 子户列表。
		List<DpDepoBusiMain.KnaAcct> lstKnaAcct = KnaAcctInfo.getRecords();
		// 子户数量。
		long totalno = KnaAcctInfo.getRecordCount();
		output.getSubAcctInfoList().setValues(lstKnaAcct);
		CommTools.getBaseRunEnvs().setTotal_count(totalno);
	}

}
*/