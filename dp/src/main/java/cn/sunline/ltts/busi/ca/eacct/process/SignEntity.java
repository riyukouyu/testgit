package cn.sunline.ltts.busi.ca.eacct.process;

import java.math.BigDecimal;

import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP;

public class SignEntity {

	//签约类型
	private E_SIGNTP signtp;
	//客户号
	private String custno;
	//电子账号
	private String custac;
	//活期账户
	private String acctno;
	//定期账户
	private String fxacct;
	//自动签约标志
	private E_YES___ autofg;
	//签约金额
	private BigDecimal signam;
	
	public BigDecimal getSignam() {
		return signam;
	}
	public void setSignam(BigDecimal signam) {
		this.signam = signam;
	}
	public E_YES___ getAutofg() {
		return autofg;
	}
	public void setAutofg(E_YES___ autofg) {
		this.autofg = autofg;
	}
	public E_SIGNTP getSigntp() {
		return signtp;
	}
	public void setSigntp(E_SIGNTP signtp) {
		this.signtp = signtp;
	}
	public String getCustno() {
		return custno;
	}
	public void setCustno(String custno) {
		this.custno = custno;
	}
	public String getCustac() {
		return custac;
	}
	public void setCustac(String custac) {
		this.custac = custac;
	}
	public String getAcctno() {
		return acctno;
	}
	public void setAcctno(String acctno) {
		this.acctno = acctno;
	}
	public String getFxacct() {
		return fxacct;
	}
	public void setFxacct(String fxacct) {
		this.fxacct = fxacct;
	}
}
