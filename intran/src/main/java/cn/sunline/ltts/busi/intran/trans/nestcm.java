package cn.sunline.ltts.busi.intran.trans;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.in.namedsql.WrAccRbDao;
import cn.sunline.ltts.busi.in.tables.In.KnsStrk;
import cn.sunline.ltts.busi.in.tables.In.KnsStrkDao;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_ACLMFG;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_LIMTTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_REBKTP;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RECPAY;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_RISKLV;
import cn.sunline.ltts.busi.qt.type.QtEnumType.E_SBACTP;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_STRKST;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class nestcm {
	private static final BizLog bizlog = BizLogUtil.getBizLog(nestcm.class);

	public static void befTran(final cn.sunline.ltts.busi.intran.trans.intf.Nestcm.Input input, final cn.sunline.ltts.busi.intran.trans.intf.Nestcm.Property property) {

		property.setAclmfg(E_ACLMFG._3);
		property.setAuthtp("02");
		property.setAcctrt(E_ACCTROUTTYPE.PERSON);
		property.setRebktp(E_REBKTP._99);
		property.setRisklv(E_RISKLV._01);
		property.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
		property.setSbactp(E_SBACTP._11);
		property.setAccttp(E_ACCATP.GLOBAL);
		property.setServtp(CommTools.getBaseRunEnvs().getChannel_id()); // 交易渠道
		property.setServsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); // 业务跟踪编号
		property.setServdt(CommTools.getBaseRunEnvs().getTrxn_date()); // 交易日期

		if (CommUtil.compare(property.getTranam(), BigDecimal.ZERO) > 0) {
			property.setTranam(property.getTranam().negate());
		}

		if (CommUtil.isNotNull(property.getOtacno())) { // 客户账号1
			property.setIsckqt(E_YES___.YES);

			if (property.getAmntcd1() == E_AMNTCD.CR) {
				property.setLimttp(E_LIMTTP.TI);
				property.setDcflag1(E_RECPAY.REC);
			} else {
				property.setLimttp(E_LIMTTP.TR);
				property.setDcflag1(E_RECPAY.PAY);
			}

		}
		if (CommUtil.isNotNull(property.getInacno())) { // 客户账号2
			property.setIsckqt1(E_YES___.YES);

			if (property.getAmntcd2() == E_AMNTCD.CR) {
				property.setLimttp(E_LIMTTP.TI);
				property.setDcflag1(E_RECPAY.REC);
			} else {
				property.setLimttp(E_LIMTTP.TR);
				property.setDcflag1(E_RECPAY.PAY);
			}

		}
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),E_CLACTP._10);
	}

	public static void befAccNestcm(final cn.sunline.ltts.busi.intran.trans.intf.Nestcm.Input input, final cn.sunline.ltts.busi.intran.trans.intf.Nestcm.Property property) {
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		
		int ibdays = DateTools2.calDays(input.getPrtrdt(), trandt, 0, 0);// 天数差

		bizlog.debug("===============隔日冲正调整天数：" + ibdays);

		List<KnsStrk> knsStrkList = WrAccRbDao.selKnsStrkByNumbsq(input.getNumbsq(), trandt, false);// 改为套号加日期查询
		
		//复核柜员赋值
		BusiTools.getBusiRunEnvs().setCkbsus(knsStrkList.get(0).getChckus());
		/*
		 *1. 如果记录数为两条则转账2.一条则为表外记账3.报错
		 * */
		if (knsStrkList.size() == 2) {
		    KnsStrk knsStrk1 = knsStrkList.get(0);
	        KnsStrk knsStrk2 = knsStrkList.get(1);

	        if (knsStrk1.getStrkst() == E_STRKST.ZF) {
	            throw InError.comm.E0003("套号已作废，请核对！");
	        }

	        if (knsStrk1.getStrkst() != E_STRKST.YFH) {
	            throw InError.comm.E0003("套号不处于已复核状态，不能进行入账操作！");
	        }

	        if (!CommUtil.equals(tranbr, knsStrk1.getWronbr())) {
	            throw InError.comm.E0003("非本机构错账记录，不能复核！");
	        }

	        if (!CommUtil.equals(tranus, knsStrk1.getChckus())) {
	            throw InError.comm.E0003("入账柜员必须与复核柜员相同！");
	        }
	        property.setWronbr1(knsStrk1.getWronbr());
	        property.setWronbr2(knsStrk2.getWronbr());
	        property.setNumbsq1(knsStrk1.getNumbsq());
	        property.setNumbsq2(knsStrk2.getNumbsq());
	        property.setReason1(knsStrk1.getReason());
	        property.setReason2(knsStrk2.getReason());
	        property.setCustac1(knsStrk1.getCustac());
	        property.setCustac2(knsStrk2.getCustac());
	        bizlog.debug("Custac1[%s] -- Custac2[%s]",property.getCustac1(),property.getCustac2());
	        property.setCrcycd1(knsStrk1.getCrcycd());
	        property.setCrcycd2(knsStrk2.getCrcycd());
	        property.setRebuwo1(knsStrk1.getRebuwo());
	        property.setRebuwo2(knsStrk2.getRebuwo());
	        property.setAmntcd1(knsStrk1.getAmntcd());
	        property.setAmntcd2(knsStrk2.getAmntcd());
	        property.setHappbl1(knsStrk1.getHappbl());
	        property.setHappbl2(knsStrk2.getHappbl());
	        property.setCharlg1(knsStrk1.getCharlg());
	        property.setCharlg2(knsStrk2.getCharlg());
	        
	        if(CommUtil.compare(ApAcctRoutTools.getRouteType(knsStrk1.getCustac()) , E_ACCTROUTTYPE.INSIDE) == 0) {
	            property.setIsInac1(E_YES___.YES);//账户1为内部户
	            property.setIstaou(E_YES___.NO);//账户1是否表外账户
	        } else {
	            property.setIsInac1(E_YES___.NO);//账户1为电子账户
	        }
	        if(CommUtil.compare(ApAcctRoutTools.getRouteType(knsStrk2.getCustac()) , E_ACCTROUTTYPE.INSIDE) == 0) {
	            property.setIsInac2(E_YES___.YES);//账户2为内部户
	        } else {
	            property.setIsInac2(E_YES___.NO);//账户2为电子账户
	        }
	        //账号一更新状态,更新交易流水为入账流水
	        knsStrk1.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
	        knsStrk1.setStrkst(E_STRKST.JQ);
	        KnsStrkDao.updateOne_kns_strk_odx1(knsStrk1);
	        knsStrk2.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
	        knsStrk2.setStrkst(E_STRKST.JQ);
	        KnsStrkDao.updateOne_kns_strk_odx1(knsStrk2);
			
		}
		else if(knsStrkList.size() == 1){
		    KnsStrk knsStrk1 = knsStrkList.get(0);
		    if (knsStrk1.getStrkst() == E_STRKST.ZF) {
                throw InError.comm.E0003("套号已作废，请核对！");
            }

            if (knsStrk1.getStrkst() != E_STRKST.YFH) {
                throw InError.comm.E0003("套号不处于已复核状态，不能进行入账操作！");
            }

            if (!CommUtil.equals(tranbr, knsStrk1.getWronbr())) {
                throw InError.comm.E0003("非本机构错账记录，不能复核！");
            }

            if (!CommUtil.equals(tranus, knsStrk1.getChckus())) {
                throw InError.comm.E0003("入账柜员必须与复核柜员相同！");
            }
            property.setWronbr1(knsStrk1.getWronbr());
            property.setNumbsq1(knsStrk1.getNumbsq());
            property.setReason1(knsStrk1.getReason());
            property.setCustac1(knsStrk1.getCustac());
            property.setCrcycd1(knsStrk1.getCrcycd());
            property.setRebuwo1(knsStrk1.getRebuwo());
            property.setAmntcd1(knsStrk1.getAmntcd());
            property.setHappbl1(knsStrk1.getHappbl());
            property.setCharlg1(knsStrk1.getCharlg());
            property.setIsInac1(E_YES___.YES);//账户1为内部户
            property.setIstaou(E_YES___.YES);//账户1为表外账户
            //更新隔日冲正登记薄交易流水为入账流水
            knsStrk1.setTransq(CommTools.getBaseRunEnvs().getTrxn_seq());
	        knsStrk1.setStrkst(E_STRKST.JQ);
	        KnsStrkDao.updateOne_kns_strk_odx1(knsStrk1);
		}
		else {
		    throw InError.comm.E0003("该记录数据有误，请核查！");
		}
	}
}
