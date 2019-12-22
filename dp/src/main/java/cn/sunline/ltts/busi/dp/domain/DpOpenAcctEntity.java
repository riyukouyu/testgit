package cn.sunline.ltts.busi.dp.domain;

import java.math.BigDecimal;

import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CUSTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;

public class DpOpenAcctEntity {

	//产品号
	private String prodcd;
	//账户名称
	private String acctna;
	//客户号
	private String custno;
	//电子账号
	private String custac;
	//存期
	private E_TERMCD depttm;
	//客户账号类型
	private E_CUSACT cacttp;
	//币种
	private String crcycd;
	//首开户标志
	private E_YES___ opacfg;
	//负债账号
	private String acctno;
	//产品定活标志
	private E_FCFLAG pddpfg;
	//存入金额
	private BigDecimal tranam;
	
	private E_DEBTTP debttp;
	//子账号类型
	private E_ACSETP acsetp; 
	
	public BigDecimal getTranam() {
		return tranam;
	}
	public void setTranam(BigDecimal tranam) {
		this.tranam = tranam;
	}
	public E_FCFLAG getPddpfg() {
		return pddpfg;
	}
	public void setPddpfg(E_FCFLAG pddpfg) {
		this.pddpfg = pddpfg;
	}
	//客户类型
	private E_CUSTTP custtp;
	//开户协议利率
	private BigDecimal openir;
	
	
	public String getProdcd() {
		return prodcd;
	}
	public void setProdcd(String prodcd) {
		this.prodcd = prodcd;
	}
	public String getAcctna() {
		return acctna;
	}
	public void setAcctna(String acctna) {
		this.acctna = acctna;
	}
	public String getCustno() {
		return custno;
	}
	public void setCustno(String custno) {
		this.custno = custno;
	}
	public E_CUSTTP getCusttp() {
		return custtp;
	}
	public void setCusttp(E_CUSTTP custtp) {
		this.custtp = custtp;
	}
	public BigDecimal getOpenir() {
		return openir;
	}
	public void setOpenir(BigDecimal openir) {
		this.openir = openir;
	}
	public String getCustac() {
		return custac;
	}
	public void setCustac(String custac) {
		this.custac = custac;
	}
	public E_TERMCD getDepttm() {
		return depttm;
	}
	public void setDepttm(E_TERMCD depttm) {
		this.depttm = depttm;
	}
	public E_CUSACT getCacttp() {
		return cacttp;
	}
	public void setCacttp(E_CUSACT string) {
		this.cacttp = string;
	}
	public String getCrcycd() {
		return crcycd;
	}
	public void setCrcycd(String crcycd) {
		this.crcycd = crcycd;
	}
	public E_YES___ getOpacfg() {
		return opacfg;
	}
	public void setOpacfg(E_YES___ opacfg) {
		this.opacfg = opacfg;
	}
	public String getAcctno() {
		return acctno;
	}
	public void setAcctno(String acctno) {
		this.acctno = acctno;
	}
	public E_DEBTTP getDebttp() {
		return debttp;
	}
	public void setDebttp(E_DEBTTP debttp) {
		this.debttp = debttp;
	}
	public E_ACSETP getAcsetp() {
		return acsetp;
	}
	public void setAcsetp(E_ACSETP acsetp) {
		this.acsetp = acsetp;
	}
	
	
}
