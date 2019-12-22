package cn.sunline.ltts.busi.intran.trans;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InTranOutDao;
import cn.sunline.ltts.busi.in.serviceimpl.IoInAcctTranOutImpl;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.KnsCmbk;
import cn.sunline.ltts.busi.in.tables.In.KnsCmbkDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
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
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOFLAG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CNTSYS;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_IAVCTP;


public class iavccm {

	public static void AfterTransferCheck( String acstno,  String trandt,  String transq,  String acctdt,  final cn.sunline.ltts.busi.intran.trans.intf.Iavccm.Property property){
		E_CLACTP clactp = E_CLACTP._99;
		if(property.getCntsys() == E_CNTSYS._0){
			//登记出入金登记簿
			List<KnsCmbk> knsCmbkList  = InTranOutDao.selKnsCmbkByAcst(acstno, CommTools.getBaseRunEnvs().getTrxn_date(), false);
			property.setFronsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//前置流水
			property.setBusisq(CommTools.getBaseRunEnvs().getTrxn_seq());//业务跟踪号//getBstrsq() to getBusisq() 19/4/17 rambo
			property.setKeepdt(BusiTools.getBusiRunEnvs().getClerdt());//清算日期
			property.setCapitp(cn.sunline.ltts.busi.sys.type.DpEnumType.E_CAPITP.IT601);
			property.setIoflag(E_IOFLAG.IN);//出入金标志
			if(CommUtil.isNotNull(knsCmbkList)){
//				IoBrchInfo brch= SysUtil.getInstance(IoBrchSvcType.class).getGenClerbr();	
				String brchno = BusiTools.getBusiRunEnvs().getCentbr();
				String otbrch = InQuerySqlsDao.sel_GlKnaAcct_by_acct(knsCmbkList.get(0).getOtacno(),true).getBrchno();
				if(otbrch.equals(brchno)){
					property.setBrchno(brchno);
					property.setAmntcd(E_AMNTCD.DR);
					
				}
				
				property.setToacct(knsCmbkList.get(0).getOtacno());
				property.setCrcycd(knsCmbkList.get(0).getCrcycd());
				//登记出入金登记金额翻倍修改 modify  by chenlk 2017-03-02
				property.setTranam(knsCmbkList.get(0).getTranam());
			}		
			property.setTranst(E_TRANST.NORMAL);
	
			if(property.getCardno().length() == 16){
				clactp = E_CLACTP._09 ;//清算账户类型为信用卡
			}else{
				clactp= E_CLACTP._01;  //跨系统转账时，清算账户类型为网络柜面
			}	
		}else{
			
			//系统内，转电子账户，且跨机构时，清算账户类型为系统内
			if(property.getIavctp() == E_IAVCTP._2){
				//IoCaKnaCust knaCust = SysUtil.getInstance(IoCaSevQryTableInfo.class).kna_cust_selectOne_odb1ByCardno(property.getCardno(), true);				
				// 调入电子账号信息
				IoCaKnaAcdc tblKnaAcdc = CommTools.getRemoteInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb2(property.getCardno(), false);
				if(CommUtil.isNull(tblKnaAcdc)){
					throw InError.comm.E0003("该转入账号["+property.getCardno()+"]不存在！");
				}			
				IoCaKnaCust tblKnaCust = CommTools.getRemoteInstance(
						IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(
						tblKnaAcdc.getCustac(), true);
								
				List<KnsCmbk> knsCmbks  = InTranOutDao.selKnsCmbkByAcst(acstno, CommTools.getBaseRunEnvs().getTrxn_date(), true);
				//if(CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), knaCust.getBrchno())){
				//modify by wuzx 内转客跨法人记账修改
				GlKnaAcct glKnaAcct = InQuerySqlsDao.sel_GlKnaAcct_by_acct(knsCmbks.get(0).getOtacno(), true);
				
				if(!CommUtil.equals(glKnaAcct.getBrchno(), tblKnaCust.getBrchno())){	
					
					clactp = E_CLACTP._10; 
				}
				
				//设置额度扣减报文
				property.setIsckqt(E_YES___.YES);
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
			
				property.setLimttp(E_LIMTTP.TI);
				property.setDcflag1(E_RECPAY.REC);
				
				property.setCustac(tblKnaAcdc.getCustac());
			}
				
		
		}
		
		SysUtil.getInstance(IoCheckBalance.class).checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getMain_trxn_seq(),clactp);
	
	}

	public static void befTranCheck( String acstno,  String trandt,  String transq,  String acctdt,  final cn.sunline.ltts.busi.intran.trans.intf.Iavccm.Property property){
		List<KnsCmbk> cmbkList = KnsCmbkDao.selectAll_kns_cmbk_odx2(acstno, CommTools.getBaseRunEnvs().getTrxn_date(), true);
		//内部账转客户账
		if (cmbkList.size() == 1 && cmbkList.get(0).getIavctp() == E_IAVCTP._2) {
			property.setIscust(E_YES___.YES);
		}else {
			property.setIscust(E_YES___.NO);
		}
		KnsCmbk knsCmbk = cmbkList.get(0);
		property.setCardno(knsCmbk.getInacno());//电子账号
		property.setAmntcd(knsCmbk.getInamcd());//借贷标识
		property.setCuacno(knsCmbk.getInacno());//转入账号
		property.setOtacna(knsCmbk.getOtacna());//转出户名
		property.setOtacno(knsCmbk.getOtacno());//转出账号
		property.setCrcycd(knsCmbk.getCrcycd());//币种
		property.setSmrytx(knsCmbk.getSmrytx());//摘要
		property.setTranam(knsCmbk.getTranam());//发生额
		IoInAcctTranOutImpl.iavccmChk(acstno, transq);
	}

	public static void sendCmqToApp( String acstno,  String trandt,  String transq,  String acctdt,  final cn.sunline.ltts.busi.intran.trans.intf.Iavccm.Property property){
		String strandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String trantm = BusiTools.getBusiRunEnvs().getTrantm();
//		IoCifCustAccs cplCifCustAccs = CommTools.getRemoteInstance(IoSrvCfPerson.class).getRemoteCifCustAccsByCardno(property.getCardno(),E_STATUS.NORMAL, true);
		//消息推送至APP客户端
//		MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//		mri.setMtopic("Q0101005");
//		//mri.setTdcnno("R00");  //测试指定DCN
//		ToAppSendMsg toAppSendMsg = CommTools
//				.getInstance(ToAppSendMsg.class);
//		
//		// 消息内容
//		toAppSendMsg.setUserId(cplCifCustAccs.getCustid()); //用户ID
//		toAppSendMsg.setOutNoticeId("Q0101005"); //外部消息ID
//		toAppSendMsg.setNoticeTitle("资金变动"); //公告标题
//		/*toAppSendMsg.setContent("交易时间："+DateTools2.getYear(DateTools2.covStringToDate(strandt))+
//				"年"+DateTools2.getMonth(DateTools2.covStringToDate(strandt))+1 +
//				"月"+DateTools2.getDay(DateTools2.covStringToDate(strandt))+"日 "
//				+trantm.substring(0, 2)+ ":"+trantm.substring(2, 4)
//				+"交易类型：转账，收入金额："+property.getTranam()+"元，请点击查看详情。"); //公告内容
//*/	
//		//交易时间
//		String date  = CommTools.getBaseRunEnvs().getTrxn_date().substring(0,4)+"年"
//			+CommTools.getBaseRunEnvs().getTrxn_date().substring(4,6)+"月"+ 
//			CommTools.getBaseRunEnvs().getTrxn_date().substring(6,8)+"日"+
//			BusiTools.getBusiRunEnvs().getTrantm().substring(0,2)+":"+
//			BusiTools.getBusiRunEnvs().getTrantm().substring(2,4)+":"+
//			BusiTools.getBusiRunEnvs().getTrantm().substring(4,6);
//		StringBuffer sb=new StringBuffer();		   
//        sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易时间：").append(date).
//            append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;交易类型：").append("转账").
//            append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;收入金额：").append(property.getTranam()+"元").
//            append("请点击查看详情。");
//        toAppSendMsg.setContent(sb.toString());
//		toAppSendMsg.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date()+BusiTools.getBusiRunEnvs().getTrantm()); //消息生成时间
//		toAppSendMsg.setTransType(E_APPTTP.CAPICH); //交易类型
//		toAppSendMsg.setTirggerSys(SysUtil.getSystemId()); //触发系统
//		toAppSendMsg.setClickType(E_CLIKTP.YES);   //点击动作类型
//		toAppSendMsg.setClickValue("LOGINURL||/page/electronicAcct/bill/electAcctBill.html"); //点击动作值
//		
//		mri.setMsgtyp("ApSmsType.ToAppSendMsg");
//		mri.setMsgobj(toAppSendMsg); 
//		AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
	}
	
}
