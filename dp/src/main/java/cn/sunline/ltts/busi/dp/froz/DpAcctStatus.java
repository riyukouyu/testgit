package cn.sunline.ltts.busi.dp.froz;

import java.util.List;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpFrozDao;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZTP;

public class DpAcctStatus {

	public static IoDpAcStatusWord GetAcStatus(String custac) {

		E_YES___ ptfroz = E_YES___.NO;// 部冻
		E_YES___ brfroz = E_YES___.NO;// 借冻
		E_YES___ dbfroz = E_YES___.NO;// 双冻
		E_YES___ alstop = E_YES___.NO;// 全止
		E_YES___ ptstop = E_YES___.NO;// 部止
		E_YES___ bkalsp = E_YES___.NO;// 银行止付全止
		E_YES___ otalsp = E_YES___.NO;// 外部止付全止
		E_YES___ clstop = E_YES___.NO;// 客户止付
		E_YES___ preaut = E_YES___.NO;// 预授权
		E_YES___ billin = E_YES___.NO;// 开单
		E_YES___ certdp = E_YES___.NO;// 存款证明
		E_YES___ pledge = E_YES___.NO;// 质押

		List<IoDpKnbFroz> listKnbFroz = DpFrozDao.selFrozByCustac(custac,
				E_FROZST.VALID, false);
		for (IoDpKnbFroz ioDpKnbFroz : listKnbFroz) {
			if (ioDpKnbFroz.getFroztp() == E_FROZTP.JUDICIAL) {
				if (ioDpKnbFroz.getFrlmtp() == E_FRLMTP.ALL) {
					dbfroz = E_YES___.YES;// 双冻
				} else if (ioDpKnbFroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
					ptfroz = E_YES___.YES;// 部冻
				} else if (ioDpKnbFroz.getFrlmtp() == E_FRLMTP.OUT) {
					brfroz = E_YES___.YES;// 借冻
				}
			} else if (ioDpKnbFroz.getFroztp() == E_FROZTP.ADD) {
				if (ioDpKnbFroz.getFrlmtp() == E_FRLMTP.ALL) {
					dbfroz = E_YES___.YES;// 双冻
				} else if (ioDpKnbFroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
					ptfroz = E_YES___.YES;// 部冻
				} else if (ioDpKnbFroz.getFrlmtp() == E_FRLMTP.OUT) {
					brfroz = E_YES___.YES;// 借冻
				}
				/*
				 * 4JF：银行止付=即富止付。
				 */
			} else if (ioDpKnbFroz.getFroztp() == E_FROZTP.BANKSTOPAY) {
				//指定金额
				if (ioDpKnbFroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
					ptstop = E_YES___.YES;// 部止
					//只进不出
				} else if (ioDpKnbFroz.getFrlmtp() == E_FRLMTP.OUT) {
					bkalsp = E_YES___.YES;// 银行止付全止
					alstop = E_YES___.YES;// 全止
				}
			} else if (ioDpKnbFroz.getFroztp() == E_FROZTP.EXTSTOPAY) {
				if (ioDpKnbFroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
					ptstop = E_YES___.YES;// 部止
				} else if (ioDpKnbFroz.getFrlmtp() == E_FRLMTP.OUT) {
					otalsp = E_YES___.YES;// 外部止付全止
					alstop = E_YES___.YES;// 全止
				}
			} else if (ioDpKnbFroz.getFroztp() == E_FROZTP.CUSTSTOPAY) {
				clstop = E_YES___.YES;// 客户止付
				alstop = E_YES___.YES;// 全止
			} else if (ioDpKnbFroz.getFroztp() == E_FROZTP.DEPRSTOPAY) {
				certdp = E_YES___.YES;// 存款证明
			} 
/*			else if (ioDpKnbFroz.getFroztp() == E_FROZTP.FNFROZ) {
				if (ioDpKnbFroz.getFrlmtp() == E_FRLMTP.AMOUNT) {
					ptfroz = E_YES___.YES;// 部冻
			}
			}*/
		}
		
		// 电子账户状态字数字化
		StringBuffer acstsz = new StringBuffer("0000000000000000");
		if (ptfroz == E_YES___.YES) {
			acstsz.setCharAt(0, '1');
		}
		if (brfroz == E_YES___.YES) {
			acstsz.setCharAt(1, '1');
		}
		if (dbfroz == E_YES___.YES) {
			acstsz.setCharAt(2, '1');
		}
		if (ptstop == E_YES___.YES) {
			acstsz.setCharAt(3, '1');
		}
		if (otalsp == E_YES___.YES) {
			acstsz.setCharAt(4, '1');
		}
		if (bkalsp == E_YES___.YES) {
			acstsz.setCharAt(5, '1');
		}
		if (clstop == E_YES___.YES) {
			acstsz.setCharAt(6, '1');
		}
		if (certdp == E_YES___.YES) {
			acstsz.setCharAt(7, '1');
		}
		if (preaut == E_YES___.YES) {
			acstsz.setCharAt(8, '1');
		}
		if (billin == E_YES___.YES) {
			acstsz.setCharAt(9, '1');
		}
		if (pledge == E_YES___.YES) {
			acstsz.setCharAt(10, '1');
		}
		
		IoDpAcStatusWord cplAcStatusWord = SysUtil.getInstance(IoDpAcStatusWord.class);
		cplAcStatusWord.setPtfroz(ptfroz);// 部冻
		cplAcStatusWord.setBrfroz(brfroz);// 借冻
		cplAcStatusWord.setDbfroz(dbfroz);// 双冻
		cplAcStatusWord.setAlstop(alstop);// 全止
		cplAcStatusWord.setPtstop(ptstop);// 部止
		cplAcStatusWord.setClstop(clstop);// 客户止付
		cplAcStatusWord.setPreaut(preaut);// 预授权
		cplAcStatusWord.setCertdp(certdp);// 存款证明
		cplAcStatusWord.setBillin(billin);// 开单
		cplAcStatusWord.setPledge(pledge);// 质押
		cplAcStatusWord.setBkalsp(bkalsp);//银行止付全止
		cplAcStatusWord.setOtalsp(otalsp);//外部止付全止
		cplAcStatusWord.setAcstsz(acstsz.toString());//电子账户状态字
		
		return cplAcStatusWord;
	}

}
