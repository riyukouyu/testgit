package cn.sunline.ltts.busi.intran.trans;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.in.tables.In.GlKnpTprc;
import cn.sunline.ltts.busi.in.tables.In.GlKnpTprcDao;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_INTYPE;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_BLNCDN;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_KPACFG;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_YES___;


public class iniaqr {

	/**
	 * 
	 * @param input
	 * @param property
	 * @param output
	 * 2016年12月8日-下午6:42:56
	 * @auther chenjk
	 */
	public static void beforeCheck( final cn.sunline.ltts.busi.intran.trans.intf.Iniaqr.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Iniaqr.Property property,  final cn.sunline.ltts.busi.intran.trans.intf.Iniaqr.Output output){
		
		if(CommUtil.isNull(input.getIntype())){
			throw InError.comm.E0003("跨机构转入类型不能为空");
		}
		if(input.getIntype() == E_INTYPE.JSZS){
			if(CommUtil.isNull(input.getCrcycd())){
				throw InError.comm.E0003("转入类型为结算代收时，输入币种不能为空");
			}
			if(CommUtil.isNull(input.getBrchno())){
				throw InError.comm.E0003("转入类型为结算代收时，输入机构号不能为空");
			}
			if(CommUtil.equals(input.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch())){
				throw InError.comm.E0003("跨机构转入不能转入本机构的账号！");
			}
			//参数表待配置
			property.setBusino( KnpParameterDao.selectOne_odb1("INIAQR", "busino", "%", "%",true).getParm_value1());
		}else{
			
			if(CommUtil.isNull(input.getAcctno())){
				throw InError.comm.E0003("转入类型为指定内部户时，输入账号不能为空");
			}
		}
		
	}

	//账户余额方向及参数表检查 
	public static void acctCheck( final cn.sunline.ltts.busi.intran.trans.intf.Iniaqr.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Iniaqr.Property property,  final cn.sunline.ltts.busi.intran.trans.intf.Iniaqr.Output output){
		
		IoInacInfo inacInfo = output.getIoInacInfo(); //内部户明细
		
		if(CommUtil.isNull(inacInfo)){
			throw InError.comm.E0003("该转入账户不存在，请核查");
		}
		
		if(inacInfo.getBlncdn() != E_BLNCDN.C){
			throw InError.comm.E0003("现只支持贷方余额账户，该转入账户余额方向不支持");
		}
		
		if(CommUtil.equals(inacInfo.getBrchno(), CommTools.getBaseRunEnvs().getTrxn_branch())){
			throw InError.comm.E0003("该转入账户不能为本机构内部户，请核查");
		}
		
		if(inacInfo.getAcctst() != E_INACST.NORMAL){
			throw InError.comm.E0003("该指定账号已销户");
		}
		
		if(inacInfo.getKpacfg() == E_KPACFG._1){
			throw InError.comm.E0003("该指定账号不允许手工记账");
		}
		
		//参数表核查
		GlKnpTprc tblTprc = GlKnpTprcDao.selectOne_odx1(inacInfo.getBusino(), inacInfo.getCrcycd(), inacInfo.getBrchno(), false);
		if(CommUtil.isNull(tblTprc)){
			throw InError.comm.E0003("该转入账户未在参数表中进行配置，请核查");
		}
		
		if(tblTprc.getIsactv() == E_YES___.NO){
			throw InError.comm.E0003("该转入账户参数未生效");
		}
		
	}
}
