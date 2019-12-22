package cn.sunline.ltts.busi.dp.domain;

import java.math.BigDecimal;

import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

public class DpAcctOnlnblEntity {
	// 活期账户
	private String acctno;
	// 借贷标志
	private E_AMNTCD amntcd;
	// 交易金额
	private BigDecimal tranam;
	// 币种
	private String crcycd;
	// 凭证号
	private String cardno;
	// 电子账户
	private String custac;
	// 子账号序号
	private String acseno;
	// 转账账号
	private String toacct;
	// 开户名称
	private String opacna;
	// 转账账号所属机构
	private String opbrch;
	// 应入账日期
	private String acctdt;
	//产品号
	private String prodcd;
	//核算代码
	private String dtitcd;
	//存期
	private E_TERMCD termcd;
	//存期天数
	private Long deptdy;
	//账单明细
	private Long detlsq;
	//原交易日期
	private String ortrdt;
	//原主交易流水
	private String ortrsq;
	//原业务流水
	private String origpq;
	//原柜员流水流水
	private String origaq;
	//连笔号
	private String linkno;
	//是否明细标志，传统定期该标志为否，智能储蓄存款该标志为是
	private E_YES___ detlfg;
	//账户状态，传统定期账户支取时会销户，需要冲正
	private E_DPACST acctst;
	//账户所属机构
	private String acctbr;
	//对方金融机构代码
	private String bankcd;
	//对方金融机构名称
	private String bankna;
	//摘要代码
	private String smrycd;
	//摘要描述
	private String smryds;
	//备注
	private String remark;
	
	private BigDecimal interest; //利息
	
	private E_YES___ sigle;
	
	private String openbr; //开户机构
	private String acctna; //账户名称
	private BigDecimal onlnbl; //账户余额
	private E_YES___ fxaufg; //自动转存标志
	
	// MAC地址
    private String macdrs;
    // 手机号码
    private String teleno;
    // 设备信息IMEI
    private String imeino;
    // 设备信息UDID
    private String udidno;
    // 交易发生地点
    private String trands;
    // 交易渠道
    private String servtp;
    // 内部交易码
    private String intrcd;
    // 主交易流水
    private String transq;
    
    public String getMacdrs() {
        return macdrs;
    }

    public void setMacdrs(String macdrs) {
        this.macdrs = macdrs;
    }

    public String getTeleno() {
        return teleno;
    }

    public void setTeleno(String teleno) {
        this.teleno = teleno;
    }

    public String getImeino() {
        return imeino;
    }

    public void setImeino(String imeino) {
        this.imeino = imeino;
    }

    public String getUdidno() {
        return udidno;
    }

    public void setUdidno(String udidno) {
        this.udidno = udidno;
    }

    public String getTrands() {
        return trands;
    }

    public void setTrands(String trands) {
        this.trands = trands;
    }

    public String getServtp() {
        return servtp;
    }

    public void setServtp(String servtp) {
        this.servtp = servtp;
    }

    public String getIntrcd() {
        return intrcd;
    }

    public void setIntrcd(String intrcd) {
        this.intrcd = intrcd;
    }

    public String getTransq() {
        return transq;
    }

    public void setTransq(String transq) {
        this.transq = transq;
    }

	public E_YES___ getFxaufg() {
		return fxaufg;
	}

	public void setFxaufg(E_YES___ fxaufg) {
		this.fxaufg = fxaufg;
	}

	public BigDecimal getOnlnbl() {
		return onlnbl;
	}

	public void setOnlnbl(BigDecimal onlnbl) {
		this.onlnbl = onlnbl;
	}

	public String getOpenbr() {
		return openbr;
	}

	public void setOpenbr(String openbr) {
		this.openbr = openbr;
	}

	public String getAcctna() {
		return acctna;
	}

	public void setAcctna(String acctna) {
		this.acctna = acctna;
	}

	public String getBankcd() {
		return bankcd;
	}

	public void setBankcd(String bankcd) {
		this.bankcd = bankcd;
	}

	public String getBankna() {
		return bankna;
	}

	public void setBankna(String bankna) {
		this.bankna = bankna;
	}

	public String getSmrycd() {
		return smrycd;
	}

	public void setSmrycd(String smrycd) {
		this.smrycd = smrycd;
	}

	public String getSmryds() {
		return smryds;
	}

	public void setSmryds(String smryds) {
		this.smryds = smryds;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getAcctbr() {
		return acctbr;
	}

	public void setAcctbr(String acctbr) {
		this.acctbr = acctbr;
	}

	public String getOpbrch() {
		return opbrch;
	}

	public void setOpbrch(String opbrch) {
		this.opbrch = opbrch;
	}

	public E_DPACST getAcctst() {
		return acctst;
	}

	public void setAcctst(E_DPACST acctst) {
		this.acctst = acctst;
	}

	public E_YES___ getDetlfg() {
		return detlfg;
	}

	public void setDetlfg(E_YES___ detlfg) {
		this.detlfg = detlfg;
	}

	public String getOrtrdt() {
		return ortrdt;
	}

	public void setOrtrdt(String ortrdt) {
		this.ortrdt = ortrdt;
	}

	public String getOrtrsq() {
		return ortrsq;
	}

	public void setOrtrsq(String ortrsq) {
		this.ortrsq = ortrsq;
	}

	public String getOrigpq() {
		return origpq;
	}

	public void setOrigpq(String origpq) {
		this.origpq = origpq;
	}

	public String getOrigaq() {
		return origaq;
	}

	public void setOrigaq(String origaq) {
		this.origaq = origaq;
	}

	public Long getDetlsq() {
		return detlsq;
	}

	public void setDetlsq(Long detlsq) {
		this.detlsq = detlsq;
	}

	public E_TERMCD getTermcd() {
		return termcd;
	}

	public void setTermcd(E_TERMCD termcd) {
		this.termcd = termcd;
	}

	public String getProdcd() {
		return prodcd;
	}

	public void setProdcd(String prodcd) {
		this.prodcd = prodcd;
	}

	public String getDtitcd() {
		return dtitcd;
	}

	public void setDtitcd(String dtitcd) {
		this.dtitcd = dtitcd;
	}

	public String getAcctno() {
		return acctno;
	}

	public void setAcctno(String acctno) {
		this.acctno = acctno;
	}

	public E_AMNTCD getAmntcd() {
		return amntcd;
	}

	public void setAmntcd(E_AMNTCD amntcd) {
		this.amntcd = amntcd;
	}

	public BigDecimal getTranam() {
		return tranam;
	}

	public void setTranam(BigDecimal tranam) {
		this.tranam = tranam;
	}



	public String getCrcycd() {
		return crcycd;
	}

	public void setCrcycd(String crcycd) {
		this.crcycd = crcycd;
	}

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}

	public String getCustac() {
		return custac;
	}

	public void setCustac(String custac) {
		this.custac = custac;
	}

	public String getAcseno() {
		return acseno;
	}

	public void setAcseno(String acseno) {
		this.acseno = acseno;
	}

	public String getToacct() {
		return toacct;
	}

	public void setToacct(String toacct) {
		this.toacct = toacct;
	}

	public String getOpacna() {
		return opacna;
	}

	public void setOpacna(String opacna) {
		this.opacna = opacna;
	}

	public String getAcctdt() {
		return acctdt;
	}

	public void setAcctdt(String acctdt) {
		this.acctdt = acctdt;
	}

	public String getLinkno() {
		return linkno;
	}

	public void setLinkno(String linkno) {
		this.linkno = linkno;
	}

	public Long getDeptdy() {
		return deptdy;
	}

	public void setDeptdy(Long deptdy) {
		this.deptdy = deptdy;
	}

	public BigDecimal getInterest() {
		return interest;
	}

	public void setInterest(BigDecimal interest) {
		this.interest = interest;
	}

	public E_YES___ getSigle() {
		return sigle;
	}

	public void setSigle(E_YES___ sigle) {
		this.sigle = sigle;
	}
	
}
