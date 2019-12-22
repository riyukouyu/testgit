package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.FnEnumType.E_QRYCON;
import cn.sunline.ltts.busi.sys.type.FnEnumType.E_WARNTP;

public class qrclun {

	public static void qrclunCheck( 
			final cn.sunline.ltts.busi.catran.trans.intf.Qrclun.Input input,  
			final cn.sunline.ltts.busi.catran.trans.intf.Qrclun.Property property,  
			final cn.sunline.ltts.busi.catran.trans.intf.Qrclun.Output output){
		
		//分页查询页码、页容量设置
		int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());
		int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());
		
		property.setPageno(pageno);
		property.setPgsize(pgsize);
		
		//默认当前交易机构 modify by chenlk 
/*		if(CommUtil.isNotNull(input.getBrchno())){
			try {
				
				SysUtil.getInstance(IoBrchSvcType.class).getBrchInfo(input.getBrchno());
			} catch (Exception e) {
				
				throw FnError.FinaComm.E9999("机构"+input.getBrchno()+"下无记录！");
			}
			if(!CommUtil.equals(sendbr, tranbr)){
				
				throw FnError.FinaComm.E9999("只允许查询自身及其下属机构！");
			}
			
			IoBrchInfo centbrInfo = SysUtil.getInstance(IoBrchSvcType.class).getGenClerbr();
			
			if(!CommUtil.equals(tranbr, centbrInfo.getBrchno())&&!CommUtil.equals(input.getBrchno(),tranbr)){
				
				throw FnError.FinaComm.E9999("只允许查询自身及其下属机构！");
			}
		}*/
		
		if(CommUtil.isNull(input.getQurytp())){
			throw DpModuleError.DpstComm.BNAS0226();
		}
		
		if(input.getQurytp() != E_WARNTP.CLOSING&&E_WARNTP.ACOUNT!=input.getQurytp()){
			throw DpModuleError.DpstComm.BNAS0225();
		}
		
		if(CommUtil.isNull(input.getQrcond())){
			throw DpModuleError.DpstComm.BNAS1062();
		}
		
		if(input.getQrcond() == E_QRYCON.DEALST || input.getQrcond() == E_QRYCON.PRODCD){
			throw DpModuleError.DpstComm.BNAS1061();
		}
		
		if(CommUtil.isNull(input.getQrvalu())){
			throw DpModuleError.DpstComm.BNAS1056();
		}
		//户名只做前台反显 查看用，后台不做处理、校验
/*		if(input.getQrcond() == E_QRYCON.ACCOUNT){
			if(CommUtil.isNull(input.getCustna())){
				throw FnError.FinaComm.E9999("户名不能为空");
			}
		}*/
		
		if(input.getQrcond()== E_QRYCON.PAPERS){
			E_IDTFTP idtftp = CommUtil.toEnum(E_IDTFTP.class, input.getQrvalu().substring(0, 3));
			//户名只做前台反显 查看用，后台不做处理、校验
/*			if(CommUtil.isNotNull(input.getCustna())){
				throw FnError.FinaComm.E9999("户名不可录入");
			}*/
			String qrvalu=input.getQrvalu().substring(3, input.getQrvalu().length());
			if(qrvalu.length() != 18 && idtftp==E_IDTFTP.SFZ){
				throw DpModuleError.DpstComm.BNAS0154();
			}
		}
		
		if(CommUtil.isNotNull(input.getBegidt())){
			
			if(CommUtil.compare(input.getBegidt(), CommTools.getBaseRunEnvs().getTrxn_date()) > 0){
				throw DpModuleError.DpstComm.BNAS0557();
			}
			if(CommUtil.isNotNull(input.getEndddt())){
				
				String sMaxDate = DateTimeUtil.dateAdd("mm", input.getEndddt(), -12);
				sMaxDate = DateTimeUtil.dateAdd("dd", sMaxDate, -1);
				
				if(CommUtil.compare(input.getBegidt(), sMaxDate) < 0){
					throw DpModuleError.DpstComm.BNAS0553();
				}
			}
			
		}
		
		if(CommUtil.isNotNull(input.getEndddt())){
			
			if(CommUtil.compare(input.getEndddt(), input.getBegidt()) < 0){
				throw CaError.Eacct.BNAS0060();
			}
			
			if(CommUtil.compare(input.getEndddt(), CommTools.getBaseRunEnvs().getTrxn_date()) > 0){
				throw CaError.Eacct.BNAS0062();
			}
			
		}
		
	}

	
	
}
