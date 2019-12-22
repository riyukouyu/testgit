package cn.sunline.ltts.busi.dp.layer;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbdl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbdlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInstCalc;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInstPrcIn;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEBS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_LYINWY;

/**
 *分层账户日终计提 
 */
public class LayerAccrued {
	
	private final static BizLog bizlog = BizLogUtil.getBizLog(BizLog.class);
	
	/**
	 * 功能: 负债分层账户日终计息处理
	 * 输入: KnbAcin 负债账户计息信息
	 * 输入: lstrdt 上次交易日期
	 * 输入: trandt 交易日期
	 * 输入: isAdd 是否增量计提
	 */
	public static void prcCrcLay(KnbAcin KnbAcin, String lstrdt, String trandt,E_YES___ isAdd){
		String acctno = KnbAcin.getAcctno();
		DpInstCalc rtnCalc = SysUtil.getInstance(DpInstCalc.class);
		bizlog.debug("分层账户开始计息，账号 = [%s]", acctno);
		//不计息、只滚积数处理
		if(CommUtil.equals(E_INBEFG.NOINBE.getValue(), KnbAcin.getInbefg().getValue())){
//			KnbAcin.setLaindt(lstrdt); // 上次计息日： 必须填上日交易日期
//			KnbAcin.setNxindt(trandt); // 下次计息日：必须填交易日期
//			KnbAcin.setLsinop(E_INDLTP.CAIN); // 上次利息操作
			KnbAcin.setIndtds(DateTools2.calDays(KnbAcin.getBgindt(), trandt, 1, 0)); //计提天数
//			KnbAcin.setLastdt(lstrdt); //最近更新日期
			KnbAcin.setPlanin(BigDecimal.ZERO); //计提利息
			
			KnbAcinDao.updateOne_odb1(KnbAcin);
			
			bizlog.parm("只滚积数不计息[%s]", KnbAcin.getAcctno());
			return;
		}
		
		/* 测试环境可能跳跑日终, 上次交易日期lstrdt和交易日期的上一日sEdindt是很可能不相等的 */
		String sEdindt = DateTimeUtil.dateAdd("day", trandt, -1);
		
		List<KubInrt> lstInrt = LayerAcctSrv.getKubInrt(acctno); //获取账户利率信息
		KnaAcct KnaAcct = KnaAcctDao.selectOne_odb1(acctno, true); //获取负债账户信息
		KnbCbdl KnbCbdl = SysUtil.getInstance(KnbCbdl.class);
		BigDecimal onlnbl = DpAcctProc.getAcctBalance(KnaAcct);
		//计算计提程序输入
		DpInstPrcIn calcIn = SysUtil.getInstance(DpInstPrcIn.class);
		calcIn.setInoptp(E_INDLTP.CAIN);
		calcIn.setTrandt(trandt);
		calcIn.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
		calcIn.setLstrdt(lstrdt);
		calcIn.setEdindt(sEdindt);
		calcIn.setOnlnbl(onlnbl);  //当期账户余额
		calcIn.setCrcycd(KnaAcct.getCrcycd());  //账户货币代号
		calcIn.setBrchno(KnaAcct.getBrchno());  //所属机构
		calcIn.setProdcd(KnaAcct.getProdcd());  //产品编号
		calcIn.setAcctcd(KnaAcct.getAcctcd());  //核算代码
		
		//标记当前余额所在层次的编号
		int mark = LayerAcctSrv.getLayerMark(lstInrt,0L, KnaAcct.getOnlnbl());

		rtnCalc = calcLayerIntr(KnbAcin,lstInrt,calcIn);
		
		
		if(KnbAcin.getLyinwy() == E_LYINWY.ALL){ //全额累进
			//登记计提明细
			KnbCbdl.setCabrdt(lstrdt); //计提日期
			KnbCbdl.setAcctno(acctno); //负债账号
			KnbCbdl.setBrchno(KnaAcct.getBrchno()); //所属机构
			KnbCbdl.setAcctcd(KnaAcct.getAcctcd()); //核算代码
			KnbCbdl.setCrcycd(KnaAcct.getCrcycd()); //货币代号
			KnbCbdl.setProdcd(KnaAcct.getProdcd()); //产品编号
			KnbCbdl.setCabrin(rtnCalc.getInstam()); //计提利息
			KnbCbdl.setIntxam(rtnCalc.getIntxam());
			KnbCbdl.setAcmltn(rtnCalc.getTotsmt()); //积数
			KnbCbdl.setIntrvl(lstInrt.get(mark).getCuusin()); //执行利率
			KnbCbdl.setIsadd(isAdd); //是否增量计提
			KnbCbdlDao.insert(KnbCbdl);
		}else if(KnbAcin.getLyinwy() == E_LYINWY.OVER){ //超额累进
			KnbCbdl.setCabrdt(lstrdt); //计提日期
			KnbCbdl.setAcctno(acctno); //负债账号
			KnbCbdl.setBrchno(KnaAcct.getBrchno()); //所属机构
			KnbCbdl.setAcctcd(KnaAcct.getAcctcd()); //核算代码
			KnbCbdl.setCrcycd(KnaAcct.getCrcycd()); //货币代号
			KnbCbdl.setProdcd(KnaAcct.getProdcd()); //产品编号
			KnbCbdl.setCabrin(rtnCalc.getInstam()); //计提利息
			KnbCbdl.setIntxam(rtnCalc.getIntxam());
			KnbCbdl.setAcmltn(rtnCalc.getTotsmt()); //积数
			KnbCbdl.setIntrvl(BigDecimal.ZERO); //执行利率 超额累进分层产品执行利率输入0
			KnbCbdl.setIsadd(isAdd); //是否增量计提
			KnbCbdlDao.insert(KnbCbdl);
		}
		
		
	}
	
	/**
	 * 功能:分层账户计算利息
	 * */
	public static DpInstCalc calcLayerIntr(KnbAcin KnbAcin,List<KubInrt> lstInrt,DpInstPrcIn calcIn){
		//超额累进分层账户，需要将当前层次及以上的层次分别统计实际积数进行计提，当前层以下的记录的实际积数即是当前记录中的积数
		
		String acctno = KnbAcin.getAcctno(); //负债账号
		String trandt = calcIn.getTrandt(); //当前交易日期
		String lstrdt = calcIn.getLstrdt(); //上次交易日期
		
		E_INBEBS txbebs = KnbAcin.getTxbebs(); //计息基础
		
		BigDecimal onlnbl = calcIn.getOnlnbl(); //账户余额
		String crcycd = calcIn.getCrcycd(); //币种
		
		BigDecimal currIntr = BigDecimal.ZERO; //账户当前计提利息
		BigDecimal currTax = BigDecimal.ZERO;  //账户当前利息税
		BigDecimal totalSmt = BigDecimal.ZERO; //总积数
		
		BigDecimal currRateIntr = BigDecimal.ZERO; //当前利率下的计提利息
		BigDecimal lastRateIntr = BigDecimal.ZERO; //变更前利率下的计提利息 (已分段的部分)
		BigDecimal currRateTax = BigDecimal.ZERO; //当前利率下的利息税
		
		BigDecimal lastRateTax = BigDecimal.ZERO; //变更前利率下的利息税
		BigDecimal currSmt = BigDecimal.ZERO; //当前积数
		BigDecimal bal = BigDecimal.ZERO;

		DpInstCalc rtnCalc = SysUtil.getInstance(DpInstCalc.class);
		
		int total = lstInrt.size();
		int days = 0;
		if(txbebs == E_INBEBS.STADSTAD){
			days = DateTools2.calDays(lstrdt, trandt, 1, 0);
		}else{
			days = DateTools2.calDays(lstrdt, trandt, 0, 0);
		}
		
		int mark = LayerAcctSrv.getLayerMark(lstInrt, days, onlnbl); //获取当前账户余额所在层次编号
		
		//正常部分的计提
		for(int i =0; i < total; i++){
			if(KnbAcin.getLyinwy() == E_LYINWY.ALL){ //全额累进
				if(i == mark){ 
					bal = onlnbl; //当前账户余额
					currSmt = DpPublic.calRealTotalAmt(lstInrt.get(i).getClvsmt(), bal, trandt, lstInrt.get(i).getLastdt()); //实际积数
				}else{ 
					currSmt = lstInrt.get(i).getClvsmt();
				}
			}else if(KnbAcin.getLyinwy() == E_LYINWY.OVER){ //超额累进
				if(i < mark){ //对档次层次及以上的层次记录进行逐层计提
					bal = lstInrt.get(i).getLvamot();
					currSmt = DpPublic.calRealTotalAmt(lstInrt.get(i).getClvsmt(), bal, trandt, lstInrt.get(i).getLastdt()); //实际积数
				}else if(i == mark){
					bal = onlnbl.subtract(lstInrt.get(i).getLvamot());
					currSmt = DpPublic.calRealTotalAmt(lstInrt.get(i).getClvsmt(), bal, trandt, lstInrt.get(i).getLastdt()); //实际积数
				}else{ //对档次层次以下的层次记录进行逐层计提
					currSmt = lstInrt.get(i).getClvsmt();
				}
			}
			
			totalSmt = totalSmt.add(currSmt);
			
			rtnCalc = LayerCalcu.calc(currSmt,lstInrt.get(i).getCuusin(), KnbAcin);
			currRateIntr = currRateIntr.add(BusiTools.roundByCurrency(crcycd, ConvertUtil.toBigDecimal(rtnCalc.getInstam()),null)); //按币种取有效值
			currRateTax = currRateTax.add(BusiTools.roundByCurrency(crcycd, ConvertUtil.toBigDecimal(rtnCalc.getIntxam()),null));
		}
		//分段部分的计提
		
		List<KnbIndl> tblKnbIndls = KnbIndlDao.selectAll_odb4(acctno,E_INDLST.YOUX, false);
		for(KnbIndl tblKnbIndl : tblKnbIndls){
			totalSmt = totalSmt.add(tblKnbIndl.getAcmltn());
			//计算活期分段计提利息
			rtnCalc = LayerCalcu.calc(tblKnbIndl.getAcmltn(),tblKnbIndl, KnbAcin);
			
			BigDecimal bigOneCabrin = ConvertUtil.toBigDecimal(rtnCalc.getInstam());
			bigOneCabrin = BusiTools.roundByCurrency(crcycd, bigOneCabrin,null); //按币种取有效值
			BigDecimal tax = ConvertUtil.toBigDecimal(rtnCalc.getIntxam());
			tax = BusiTools.roundByCurrency(crcycd, tax,null);
			if(calcIn.getIstest() != E_YES___.YES){ //试算标志
				//更新分段利息表的相关计提信息
				tblKnbIndl.setRlintr(bigOneCabrin); //分段计提利息，无此字段，赋值给实际利息发生额
				tblKnbIndl.setIntrdt(lstrdt); //计息日期
				tblKnbIndl.setIntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //计息流水
				tblKnbIndl.setLsinoc(E_INDLTP.CAIN); //上次利息操作代码
				KnbIndlDao.updateOne_odb1(tblKnbIndl);
			}
			//当前段计提利息加上分段计提利息
			lastRateIntr = lastRateIntr.add(bigOneCabrin);
			lastRateTax = lastRateTax.add(tax);
		}
		
		currIntr = currIntr.add(currRateIntr).add(lastRateIntr);
		currTax = currTax.add(currRateTax).add(lastRateTax);
		//计提天数
		//calcDays = DateTools2.calDays(KnbAcin.getBgindt(), trandt, 1, 0);
		if(calcIn.getIstest() != E_YES___.YES){ //试算标志
			//更新负债账户计息信息
//			KnbAcin.setLaindt(calcIn.getLstrdt()); //上次计息日期
//			KnbAcin.setLsinop(E_INDLTP.CAIN); //上次利息操作代码
//			KnbAcin.setNxindt(trandt); //下次计息日期
//			KnbAcin.setPlanin(currIntr); //计提利息
//			KnbAcin.setLastdt(calcIn.getLstrdt()); //最近更新日期
//			KnbAcin.setIndtds(calcDays); //计提天数
//			KnbAcin.setMustin(currTax); //应缴税金
//			KnbAcinDao.updateOne_odb1(KnbAcin);
		}
		
		
		rtnCalc.setTotsmt(totalSmt); //总积数
		rtnCalc.setInstam(currIntr); //计提利息
		rtnCalc.setIntxam(currTax); //计提税金
		
		return rtnCalc;
	}
	
	
	

}
