package cn.sunline.ltts.busi.in.serviceimpl;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.in.inner.InacTransDeal;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INPTSR;
 /**
  * 内部户记账相关服务实现
  * 内部户记账相关服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
public class IoInAccountImpl implements cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount{
	/**
	  * 内部户借方服务实现
	 * @return 
	  *
	  */
	@Override
	public IaTransOutPro ioInAcdr(IaAcdrInfo input) {
		input.setAmntcd(E_AMNTCD.DR);
		input.setInptsr(CommUtil.nvl(input.getInptsr(),E_INPTSR.GL01));
		return InacTransDeal.dealInnerAccountTran(input);
	}
	/**
	  * 内部户贷方服务实现
	 * @return 
	  *
	  */
	@Override
	public IaTransOutPro ioInAccr(IaAcdrInfo input) {
		input.setAmntcd(E_AMNTCD.CR);
		input.setInptsr(CommUtil.nvl(input.getInptsr(),E_INPTSR.GL01));
		if (CommUtil.isNull(CommTools.getBaseRunEnvs().getMain_trxn_seq())) {
			CommTools.getBaseRunEnvs().setMain_trxn_seq(CommTools.getBaseRunEnvs().getTrxn_seq());//sdw add
		}
		return InacTransDeal.dealInnerAccountTran(input);
	}
	/**
	 * 内部户收方服务
	 */
	@Override
	public IaTransOutPro ioInAcrv(IaAcdrInfo input) {
		input.setAmntcd(E_AMNTCD.RV);
		input.setInptsr(CommUtil.nvl(input.getInptsr(),E_INPTSR.GL01));
		return InacTransDeal.dealInnerAccountTran(input);
	}
	/**
	 * 内部户付方服务
	 */	
	@Override
	public IaTransOutPro ioInAcpv(IaAcdrInfo input) {
		input.setAmntcd(E_AMNTCD.PY);
		input.setInptsr(CommUtil.nvl(input.getInptsr(),E_INPTSR.GL01));
		return InacTransDeal.dealInnerAccountTran(input);
	}
	/**
	 * 内部户贷方服务定义（外调）
	 */
	@Override
	public IaTransOutPro IoInAccrAdm(IaAcdrInfo input) {
		input.setAmntcd(E_AMNTCD.CR);
		input.setInptsr(CommUtil.nvl(input.getInptsr(),E_INPTSR.GL01));
		IaTransOutPro iaTransOutPro = InacTransDeal.dealInnerAccountTran(input);
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
		return iaTransOutPro;
	}
	/**
	 * 内部户借方服务定义（外调）
	 */
	@Override
	public IaTransOutPro IoInAcdrAdm(IaAcdrInfo input) {
		input.setAmntcd(E_AMNTCD.DR);
		input.setInptsr(CommUtil.nvl(input.getInptsr(),E_INPTSR.GL01));
		IaTransOutPro iaTransOutPro = InacTransDeal.dealInnerAccountTran(input);
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
		return iaTransOutPro;
	}
	@Override
	public IaTransOutPro ioInAcrvAdm(IaAcdrInfo input) {
		input.setAmntcd(E_AMNTCD.RV);
		input.setInptsr(CommUtil.nvl(input.getInptsr(),E_INPTSR.GL01));
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
		return InacTransDeal.dealInnerAccountTran(input);
	}
	@Override
	public IaTransOutPro ioInAcpvAdm(IaAcdrInfo input) {
		input.setAmntcd(E_AMNTCD.PY);
		input.setInptsr(CommUtil.nvl(input.getInptsr(),E_INPTSR.GL01));
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),null);
		return InacTransDeal.dealInnerAccountTran(input);
	}

}

