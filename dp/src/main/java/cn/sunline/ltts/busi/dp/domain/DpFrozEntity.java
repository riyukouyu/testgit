package cn.sunline.ltts.busi.dp.domain;

public class DpFrozEntity {
	
	//冻结编号
	private String frozno;
	
	//客户号
	private String custno;
	
	public void setFrozno(String frozno){
		this.frozno = frozno;
	}
	
	public String getFrozno(){
		return this.frozno;
	}

	public String getCustno() {
		return custno;
	}

	public void setCustno(String custno) {
		this.custno = custno;
	}

}
