package cn.sunline.ltts.busi.dp.domain;

import java.math.BigDecimal;

import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;

public class DpOpprEntity {
	private String custna;
	
	private BigDecimal amount;
	
	private String crcycd;
	
	private E_CSEXTG csextg;
	
	private String deprnm;
	
	public String getDeprnm() {
		return deprnm;
	}

	public void setDeprnm(String deprnm) {
		this.deprnm = deprnm;
	}

	public void setCustna(String custna) {
		this.custna = custna;
	}
	
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public void setCrcycd(String crcycd) {
		this.crcycd = crcycd;
	}

	public void setCsextg(E_CSEXTG csextg) {
		this.csextg = csextg;
	}
	
	public String getCustna() {
		return custna;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	public String getCrcycd() {
		return crcycd;
	}
	
	public E_CSEXTG getCsextg() {
		return csextg;
	}
}
