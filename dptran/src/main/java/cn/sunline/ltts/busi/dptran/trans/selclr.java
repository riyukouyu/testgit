package cn.sunline.ltts.busi.dptran.trans;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.namedsql.ProdClearBatchDao;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsqCler;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIoblCups;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.SelClrBmdpList;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpCapital.SelClrClerList;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLERST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BMDPTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUPSST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SUMMTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class selclr {


	private static BizLog log = BizLogUtil.getBizLog(selclr.class);
	
	/**
	 * @Title: selClr 
	 * @Description: 银联清算核准统计查询  
	 * @param input
	 * @param property
	 * @param output
	 * @author zhangan
	 * @date 2017年3月29日 下午2:41:23 
	 * @version V2.3.0
	 */
	public static void selClr( final cn.sunline.ltts.busi.dptran.trans.intf.Selclr.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Selclr.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Selclr.Output output){
		
		//查询银联贷记业务 内部户业务代码
		KnpParameter para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", E_CLACTP._04.getValue(), "%", true);
		String busino = para.getParm_value1();
		String stardt = input.getStardt();
		String endtdt = input.getEndtdt();
		
		//清算账户类型
		E_CLACTP clactp = E_CLACTP._04;
		
		if(input.getBmdptp() == E_BMDPTP.T1){
			String tmp_date = "";
			SelClrClerList clrlst = SysUtil.getInstance(SelClrClerList.class);
			
			List<KnsAcsqCler> lstKnsAcsqCler = ProdClearBatchDao.selKnsAcsqClerByClerdt(busino, clactp, stardt, endtdt, false);
			for(KnsAcsqCler cler : lstKnsAcsqCler){
				if(!CommUtil.equals(cler.getClerdt(), tmp_date)){ //
					tmp_date = cler.getClerdt();
					//初始化操作
					clrlst = SysUtil.getInstance(SelClrClerList.class);
					clrlst.setCnkpdt(cler.getClerdt());
					clrlst.setDecamt(BigDecimal.ZERO);
					clrlst.setDedamt(BigDecimal.ZERO);
					clrlst.setRecamt(BigDecimal.ZERO);
					clrlst.setRedamt(BigDecimal.ZERO);
					
					output.getClerList().add(clrlst); //设置返回参数
				}
				
				if(cler.getClerst() == E_CLERST.SUCCESS){ //清算成功
					if(cler.getAmntcd() == E_AMNTCD.DR){ //借方
						clrlst.setRedamt(clrlst.getRedamt().add(cler.getTranam()));
					}else{ //贷方
						clrlst.setRecamt(clrlst.getRecamt().add(cler.getTranam()));
					}
				}else{ //未清算
					if(cler.getAmntcd() == E_AMNTCD.DR){ //借方
						clrlst.setDedamt(clrlst.getDedamt().add(cler.getTranam()));
					}else{ //贷方
						clrlst.setDecamt(clrlst.getDecamt().add(cler.getTranam()));
					}
				}
				
			}
			
		}else if(input.getBmdptp() == E_BMDPTP.T2){
			SelClrBmdpList bmdlst_1 = SysUtil.getInstance(SelClrBmdpList.class);
			bmdlst_1.setSummtp(E_SUMMTP.T1);
			bmdlst_1.setSumdmt(BigDecimal.ZERO);
			bmdlst_1.setSumcmt(BigDecimal.ZERO);
			bmdlst_1.setOffamt(BigDecimal.ZERO);
			
			SelClrBmdpList bmdlst_2 = SysUtil.getInstance(SelClrBmdpList.class);
			bmdlst_2.setSummtp(E_SUMMTP.T2);
			bmdlst_2.setSumdmt(BigDecimal.ZERO);
			bmdlst_2.setSumcmt(BigDecimal.ZERO);
			bmdlst_2.setOffamt(BigDecimal.ZERO);
			
			SelClrBmdpList bmdlst_3 = SysUtil.getInstance(SelClrBmdpList.class);
			bmdlst_3.setSummtp(E_SUMMTP.T3);
			bmdlst_3.setSumdmt(BigDecimal.ZERO);
			bmdlst_3.setSumcmt(BigDecimal.ZERO);
			bmdlst_3.setOffamt(BigDecimal.ZERO);
			
			output.getBmdpList().add(bmdlst_1);
			output.getBmdpList().add(bmdlst_2);
			output.getBmdpList().add(bmdlst_3);
			
			List<KnlIoblCups> lstCups = ProdClearBatchDao.selKnlIoblCupsByClerdt(stardt, endtdt, E_CUPSST.SUCC, false);
			for(KnlIoblCups cups : lstCups){
//				if(cups.getAmntcd() == E_AMNTCD.DR){ //借方统计
//					bmdlst_1.setSumdmt(bmdlst_1.getSumdmt().add(cups.getTranam()));
//					if(CommUtil.compare(cups.getCnkpdt(), stardt) >= 0 && CommUtil.compare(cups.getCnkpdt(), endtdt) <= 0){
//						//当期金额
//						bmdlst_3.setSumdmt(bmdlst_3.getSumdmt().add(cups.getTranam()));
//					}else if(CommUtil.compare(cups.getCnkpdt(), endtdt) > 0){
//						//扎入下一期的金额
//						bmdlst_2.setSumdmt(bmdlst_2.getSumdmt().add(cups.getTranam()));
//					}else{
//						//上一期的金额
//					}
//				}else{
//					bmdlst_1.setSumcmt(bmdlst_1.getSumcmt().add(cups.getTranam()));
//					if(CommUtil.compare(cups.getCnkpdt(), stardt) >= 0 && CommUtil.compare(cups.getCnkpdt(), endtdt) <= 0){
//						//当期金额
//						bmdlst_3.setSumcmt(bmdlst_3.getSumcmt().add(cups.getTranam()));
//					}else if(CommUtil.compare(cups.getCnkpdt(), endtdt) > 0){
//						//扎入下一期的金额
//						bmdlst_2.setSumcmt(bmdlst_2.getSumcmt().add(cups.getTranam()));
//					}else{
//						//上一期的金额
//					}
//				}
			}
			bmdlst_1.setOffamt(bmdlst_1.getSumdmt().subtract(bmdlst_1.getSumcmt()));
			bmdlst_2.setOffamt(bmdlst_2.getSumdmt().subtract(bmdlst_2.getSumcmt()));
			bmdlst_3.setOffamt(bmdlst_3.getSumdmt().subtract(bmdlst_3.getSumcmt()));
			
		}else{
			throw DpModuleError.DpstComm.BNAS0246();
		}
		
		
		
		
		
	}
}
