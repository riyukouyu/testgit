package cn.sunline.ltts.busi.dp.domain;

import java.math.BigDecimal;

import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_DEDUTP;

public class DpSaveEntity {
	// 负债账号
	private String acctno;
	// 定期明细序号
    private Long detlsq;
	// 币种
	private String crcycd;
	// 交易金额
	private BigDecimal tranam;
	// 卡号
	private String cardno;
	// 电子账户
	private String custac;
	// 电子账户子号
	private String acseno;
	// 转账账号
	private String toacct;
	// 转账户名
	private String opacna;
	// 转账账户所属机构
	private String opbrch;
	//交易密码
	private String passwd;
	//检查密码标志
	private boolean chckfg;
	//定期到期自动转存交易标志
	private E_YES___ fxaufg;
	//交易金额可负标志
	private E_YES___ negafg;
	//原交易日期
	private String ortrdt;
	//原借贷标志
	private E_AMNTCD oramnt;
	//红蓝字标志
	private E_COLOUR colrfg;
	//冲正冲账标志
	private E_STACPS stacps;
	//智能储蓄存取款标志(空=YES，为NO是表示直接对子帐号进行存取而不是对智能储蓄进行存取)
	private E_YES___ auacfg;
	//是否允许透支标志
	private E_YES___ ngblfg;
	//连笔号
	private String linkno;
	//结息利息
	private BigDecimal instam;
	//利息税
	private BigDecimal intxam;
	//是否明细标志，传统定期该标志为否，智能储蓄存款该标志为是
	private E_YES___ detlfg;
	//账户状态
	private E_DPACST acctst;
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
	//是否允许冲正
	private E_YES___ strktg;
	//是否校验标志
	private E_YES___ ischck;
	
	private E_YES___ isdedu;
	
	private E_DEDUTP dedutp;
	//追缴金额
	private BigDecimal pyafamount;
	//追缴产生的bill流水号
	private long pydetlsq;
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

	public long getPydetlsq() {
        return pydetlsq;
    }

    public void setPydetlsq(long pydetlsq) {
        this.pydetlsq = pydetlsq;
    }

    public BigDecimal getPyafamount() {
        return pyafamount;
    }

    public void setPyafamount(BigDecimal pyafamount) {
        this.pyafamount = pyafamount;
    }

    public E_YES___ getIschck() {
		return ischck;
	}

	public void setIschck(E_YES___ ischck) {
		this.ischck = ischck;
	}

	public E_YES___ getStrktg() {
		return strktg;
	}

	public void setStrktg(E_YES___ strktg) {
		this.strktg = strktg;
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

	public BigDecimal getInstam() {
		return instam;
	}

	public void setInstam(BigDecimal instam) {
		this.instam = instam;
	}
	public BigDecimal getIntxam() {
		return intxam;
	}

	public void setIntxam(BigDecimal intxam) {
		this.intxam = intxam;
	}
	public E_STACPS getStacps() {
		return stacps;
	}

	public void setStacps(E_STACPS stacps) {
		this.stacps = stacps;
	}

	public E_AMNTCD getOramnt() {
		return oramnt;
	}

	public void setOramnt(E_AMNTCD oramnt) {
		this.oramnt = oramnt;
	}

	public E_COLOUR getColrfg() {
		return colrfg;
	}

	public void setColrfg(E_COLOUR colrfg) {
		this.colrfg = colrfg;
	}

	public String getOrtrdt() {
		return ortrdt;
	}

	public void setOrtrdt(String ortrdt) {
		this.ortrdt = ortrdt;
	}

	public E_YES___ getNegafg() {
		return negafg;
	}

	public void setNegafg(E_YES___ negafg) {
		this.negafg = negafg;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public boolean isChckfg() {
		return chckfg;
	}

	public void setChckfg(boolean chckfg) {
		this.chckfg = chckfg;
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

	public BigDecimal getTranam() {
		return tranam;
	}

	public void setTranam(BigDecimal tranam) {
		this.tranam = tranam;
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
	
    public Long getDetlsq() {
		return detlsq;
	}

    public void setDetlsq(Long detlsq) {
		this.detlsq = detlsq;
	}

	public E_YES___ getFxaufg() {
		return fxaufg;
	}

	public void setFxaufg(E_YES___ fxaufg) {
		this.fxaufg = fxaufg;
	}
	
	public E_YES___ getAuacfg() {
		return auacfg;
	}

	public void setAuacfg(E_YES___ auacfg) {
		this.auacfg = auacfg;
	}
	
	public E_YES___ getNgblfg() {
		return ngblfg;
	}

	public void setNgblfg(E_YES___ ngblfg) {
		this.ngblfg = ngblfg;
	}

	public String getLinkno() {
		return linkno;
	}

	public void setLinkno(String linkno) {
		this.linkno = linkno;
	}

	public E_YES___ getIsdedu() {
		return isdedu;
	}

	public void setIsdedu(E_YES___ isdedu) {
		this.isdedu = isdedu;
	}

	public E_DEDUTP getDedutp() {
		return dedutp;
	}

	public void setDedutp(E_DEDUTP dedutp) {
		this.dedutp = dedutp;
	}
	
}
