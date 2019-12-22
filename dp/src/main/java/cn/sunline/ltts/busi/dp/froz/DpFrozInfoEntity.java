package cn.sunline.ltts.busi.dp.froz;

import java.math.BigDecimal;

import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;

public class DpFrozInfoEntity {

	private E_FROZOW frozow;// 冻结主体类型
	private String frowid;// 冻结主体id
	private E_YES___ frinfg;// 入冻结(只付不收)
	private E_YES___ frotfg;// 出冻结（只收不付）
	private E_YES___ fralfg;// 全额冻结（不收不付）
	private BigDecimal frozbl;// 冻结余额

	public E_FROZOW getFrozow() {
		return frozow;
	}

	public void setFrozow(E_FROZOW frozow) {
		this.frozow = frozow;
	}

	public String getFrowid() {
		return frowid;
	}

	public void setFrowid(String frowid) {
		this.frowid = frowid;
	}

	public E_YES___ getFrinfg() {
		return frinfg;
	}

	public void setFrinfg(E_YES___ frinfg) {
		this.frinfg = frinfg;
	}

	public E_YES___ getFrotfg() {
		return frotfg;
	}

	public void setFrotfg(E_YES___ frotfg) {
		this.frotfg = frotfg;
	}

	public E_YES___ getFralfg() {
		return fralfg;
	}

	public void setFralfg(E_YES___ fralfg) {
		this.fralfg = fralfg;
	}
	
	public BigDecimal getFrozbl() {
		return frozbl;
	}

	public void setFrozbl(BigDecimal frozbl) {
		this.frozbl = frozbl;
	}
	
}
