package cn.sunline.ltts.busi.in.serviceimpl;

import java.util.List;

import cn.sunline.adp.cedar.base.engine.sequence.SequenceManager;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.in.inner.InFnsCmbk;
import cn.sunline.ltts.busi.in.tables.In.FnsCmbk;
import cn.sunline.ltts.busi.in.tables.In.FnsCmbkDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBusi;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBusiDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnpItem;
import cn.sunline.ltts.busi.in.tables.In.GlKnpItemDao;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.InFnsCmbkOut;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CMBK_TRANST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_IOFLAG;
 /**
  * 财务往来记账相关服务实现
  * 财务往来记账相关服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoInFnsCmbkImpl", longname="财务往来记账相关服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoInFnsCmbkImpl implements cn.sunline.ltts.busi.iobus.servicetype.in.IoInFnsCmbk{
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(IoInFnsCmbkImpl.class);
 /**
  * 财务往来记账登记
  *
  */
	public cn.sunline.ltts.busi.in.type.InacTransComplex.InFnsCmbkOut fnsRegister( final cn.sunline.ltts.busi.in.type.InacTransComplex.InFnsCmbkInfo input){
		
		bizlog.debug("input:[%s]", input);
		InFnsCmbkOut out = SysUtil.getInstance(InFnsCmbkOut.class);
		//子户号
		String subSac = null;
		//业务代码
		if(CommUtil.isNotNull(input.getBusino())){
			GlKnpBusi glKnpBusi = GlKnpBusiDao.selectOne_odb1(input.getBusino(), false);
			if(CommUtil.isNull(glKnpBusi)){
				throw InError.comm.E0003("业务代码"+input.getBusino()+"在业务编码表中未配置");
			}
			subSac = "";//glKnpBusi.getSubsac();
		}
		//查询科目表
		GlKnpItem glKnpItem = GlKnpItemDao.selectOne_odb1(input.getItemcd(), false);
		if(CommUtil.isNull(glKnpItem)){
			throw InError.comm.E0003("科目号"+input.getItemcd()+"在科目表中未配置");
		}
		E_IOFLAG ioFlag = glKnpItem.getIoflag();//表内外标志
		
		String commsq = null;//套账号
		String trandt = null;//记账日期
		if(CommUtil.isNull(input.getCommsq())){
			//commsq = SequenceManager.nextval("fns_cmbk");
			commsq = CoreUtil.nextValue("fns_cmbk");
			trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		}else{
			commsq = input.getCommsq();
			if(CommUtil.isNull(input.getTrandt())) throw InError.comm.E0003("记账日期不允许为空");
			trandt = input.getTrandt();
		}
		
		//插入财务往来记账登记簿
		FnsCmbk fnsCmbk = SysUtil.getInstance(FnsCmbk.class);
		fnsCmbk.setCommdt(input.getCommdt());//录入日期
		fnsCmbk.setCommsq(commsq);//套账号
		fnsCmbk.setCommnm(InFnsCmbk.getMaxCommnm(commsq, trandt));//套票顺序号
		fnsCmbk.setInptsq(input.getInptsq());//录入流水
		fnsCmbk.setTranbr(CommTools.getBaseRunEnvs().getTrxn_branch());//交易机构
		fnsCmbk.setItemcd(input.getItemcd());;//科目号
		fnsCmbk.setSubsac(subSac);//子户号
		fnsCmbk.setBusino(input.getBusino());//核心业务码
		fnsCmbk.setIoflag(ioFlag);//表内外标志
		fnsCmbk.setCrcycd(input.getCrcycd());//币种
		fnsCmbk.setAmntcd(input.getAmntcd());//记账方向
		fnsCmbk.setTranam(input.getTranam());//记账金额
		fnsCmbk.setSmrytx(input.getSmrytx());//摘要
		fnsCmbk.setTranst(E_CMBK_TRANST._0);//传票状态
		fnsCmbk.setTrandt(trandt);//记账日期
		fnsCmbk.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller());//柜员
		bizlog.debug("fnsCmbk：[%s]", fnsCmbk);
		FnsCmbkDao.insert(fnsCmbk);
		
		//微众记账特殊处理
		InFnsCmbk.transfer(fnsCmbk);
		
		out.setCommsq(commsq);
		out.setTrandt(trandt);
		return out;
	}
 /**
  * 财务往来记账登记簿入账
  *
  */
	public cn.sunline.ltts.busi.in.type.InacTransComplex.InFnsCmbkOut fnsAccounting( String commsq,  String trandt){
		
		InFnsCmbkOut out = SysUtil.getInstance(InFnsCmbkOut.class);
		//记账流水
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		List<FnsCmbk> fnsCmbkList = FnsCmbkDao.selectAll_odb2(commsq, E_CMBK_TRANST._0, trandt, false);
		if(CommUtil.isNull(fnsCmbkList)){
			throw InError.comm.E0003("套账号、记账日期对应登记簿找不到记录");
		}
		for(FnsCmbk fnsCmbk:fnsCmbkList){
			IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
			iaAcdrInfo.setBusino(fnsCmbk.getBusino());//业务编码
			iaAcdrInfo.setTranam(fnsCmbk.getTranam());//记账金额
			iaAcdrInfo.setCrcycd(fnsCmbk.getCrcycd());//币种
			iaAcdrInfo.setAcbrch(fnsCmbk.getTranbr());//账户机构
			//记账方向
			E_AMNTCD amntcd = fnsCmbk.getAmntcd();
			//调用内部户记账服务
			IoInAccount ioInAcctount = SysUtil.getInstance(IoInAccount.class);
			switch (amntcd){
				case DR:
					ioInAcctount.ioInAcdr(iaAcdrInfo);//内部户借方服务
					break;
				case CR:
					ioInAcctount.ioInAccr(iaAcdrInfo);//内部户贷方服务
					break;
				case RV:
					ioInAcctount.ioInAcrv(iaAcdrInfo);//内部户收方服务
					break;
				case PY:
					ioInAcctount.ioInAcpv(iaAcdrInfo);//内部户收方服务
					break;
				default:
					throw InError.comm.E0003("记账方向:"+amntcd.getValue()+"["+amntcd.getLongName()+"]不支持");
			}
			//更新财务往来记账登记簿
			fnsCmbk.setTranst(E_CMBK_TRANST._1);//传票状态
			fnsCmbk.setTransq(transq);//记账流水
			FnsCmbkDao.updateOne_odb1(fnsCmbk);
		}
		out.setTrandt(trandt);//记账日期
		out.setTransq(transq);//记账流水
		//冲正登记（不需要）
		
		return out;
	}
}

