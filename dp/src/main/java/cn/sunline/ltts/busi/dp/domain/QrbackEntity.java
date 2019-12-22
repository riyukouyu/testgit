package cn.sunline.ltts.busi.dp.domain;

import java.math.BigDecimal;

import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_LAWCOP;

public class QrbackEntity {
	
	//冻结终止日期
	private String freddt;
	
	//冻结通知书编号
	private String frctno;
	
	//冻结金额
	private BigDecimal frozam;
	
	//冻结执法部门名称
	private String frogna;
	
	//冻结执法部门
	private E_LAWCOP frexog;

	public String getFreddt() {
		return freddt;
	}

	public void setFreddt(String freddt) {
		this.freddt = freddt;
	}

	public String getFrctno() {
		return frctno;
	}

	public void setFrctno(String frctno) {
		this.frctno = frctno;
	}

	public BigDecimal getFrozam() {
		return frozam;
	}

	public void setFrozam(BigDecimal frozam) {
		this.frozam = frozam;
	}

	public String getFrogna() {
		return frogna;
	}

	public void setFrogna(String frogna) {
		this.frogna = frogna;
	}

	public E_LAWCOP getFrexog() {
		return frexog;
	}

	public void setFrexog(E_LAWCOP frexog) {
		this.frexog = frexog;
	}
	
	
	
	
}
