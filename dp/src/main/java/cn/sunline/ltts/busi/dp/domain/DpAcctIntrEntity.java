package cn.sunline.ltts.busi.dp.domain;

import java.math.BigDecimal;

import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;

public class DpAcctIntrEntity {
	
	private String prodcd;
	
	private String acctno;
	
	private String crcycd;
	
	private BigDecimal openir;
	
	private E_TERMCD depttm;
	
	private long deptdy;
	
	private E_FCFLAG pddpfg;
	
	private E_YES___ detlfg;
	
	private BigDecimal tranam;
	
	
	public BigDecimal getTranam() {
		return tranam;
	}
	public void setTranam(BigDecimal tranam) {
		this.tranam = tranam;
	}
	public String getProdcd() {
		return prodcd;
	}
	public void setProdcd(String prodcd) {
		this.prodcd = prodcd;
	}
	public String getAcctno() {
		return acctno;
	}
	public void setAcctno(String acctno) {
		this.acctno = acctno;
	}

	public String getCrcycd() {
		return crcycd;
	}
	public void setCrcycd(String crcycd) {
		this.crcycd = crcycd;
	}
	public BigDecimal getOpenir() {
		return openir;
	}
	public void setOpenir(BigDecimal openir) {
		this.openir = openir;
	}
	public E_TERMCD getDepttm() {
		return depttm;
	}
	public void setDepttm(E_TERMCD depttm) {
		this.depttm = depttm;
	}
	public long getDeptdy() {
		return deptdy;
	}
	public void setDeptdy(long deptdy) {
		this.deptdy = deptdy;
	}
	public E_FCFLAG getPddpfg() {
		return pddpfg;
	}
	public void setPddpfg(E_FCFLAG pddpfg) {
		this.pddpfg = pddpfg;
	}
	public E_YES___ getDetlfg() {
		return detlfg;
	}
	public void setDetlfg(E_YES___ detlfg) {
		this.detlfg = detlfg;
	}
	
	
}
