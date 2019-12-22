package cn.sunline.ltts.busi.in.serviceimpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.clwj.msap.biz.transaction.MsEvent;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.iobus.type.IoMsReverseType.IoMsRegEvent;
import cn.sunline.clwj.msap.util.sequ.MsSeqUtil;

//import com.mysql.fabric.xmlrpc.base.Array;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
//import cn.sunline.ltts.busi.aplt.tools.AsyncMessageUtil;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InTranOutDao;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.InacctTran.Input;
import cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.SelIavcql.Output;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.KnsCmbk;
import cn.sunline.ltts.busi.in.tables.In.KnsCmbkDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPaya;
import cn.sunline.ltts.busi.in.tables.In.KnsPayaDao;
import cn.sunline.ltts.busi.in.tables.In.KnsPayd;
import cn.sunline.ltts.busi.in.tables.In.KnsPaydDao;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.IavcbkDetail;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaDetail;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaFirst;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaList;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaListOutPut;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PayaSecond;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydDetail;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydFirst;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydList;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydListOutPut;
import cn.sunline.ltts.busi.in.type.InAcctTranOut.PaydSecond;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaPayaDetail;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaPaydDetail;
//import cn.sunline.ltts.busi.iobus.servicetype.cf.IoSrvCfPerson;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
//import cn.sunline.ltts.busi.iobus.type.cf.IoCuTable.IoCifCustAccs;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.SaveDpAcctIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInacInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
//import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchUserQt;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.errors.PbError;
//import cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_APPTTP;
//import cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_CLIKTP;
//import cn.sunline.ltts.busi.sys.type.ApSmsType.ToAppSendMsg;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AUTHFG;
//import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STATUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CKTRTP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TRINTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CMBK_TRANST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CNTSYS;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_IAVCTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INACST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_KPACFG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAQRST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYAST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYATP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PAYDST;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_PDQRST;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

/**
 * 内部户转出实现 内部户转出实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoInAcctTranOutImpl", longname = "内部户转出实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoInAcctTranOutImpl implements
		cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut {

	private static final BizLog bizlog = BizLogUtil.getBizLog(IoInAcctTranOutImpl.class);

	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-06 21：21</li>
	 *         <li>内部账转客户账录入</li>
	 *         </p>
	 * @param input
	 *           内部账转客户账录入信息
	 * */
	@Override
	public void insIacutr(
			cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.InsIacutr.Input input,
			cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.InsIacutr.Output output) {

		bizlog.debug("==========内部账转客户账录入处理开始==========");
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq(); //交易流水
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller(); //交易柜员
		
		if(CommUtil.isNull(input.getAcstno())){
            throw InError.comm.E0003("输入套号不能为空");
        }
        
        if(input.getAcstno().length() != 11){
            throw InError.comm.E0003("输入套号不合法，请核查！");
        }
        
        if(CommTools.rpxMatch("W[0-9]+", input.getAcstno()) == 1){
            throw InError.comm.E0003("输入套号不合法，请核查！");
        }
		// 传票录入登记簿录入
		bizlog.debug("传票录入登记簿录入开始==========");
		KnsCmbk knsCmbk = SysUtil.getInstance(KnsCmbk.class);
		
		knsCmbk.setAcctbr(input.getAcctbr());
		knsCmbk.setOtacna(input.getOtacna());
		knsCmbk.setOtacno(input.getOtacno());
		knsCmbk.setAcstno(input.getAcstno());
		knsCmbk.setOtamcd(input.getOtamcd());
		knsCmbk.setCsextg(input.getCsextg());
		knsCmbk.setCrcycd(input.getCrcycd());
		knsCmbk.setIavctp(E_IAVCTP._2);
		knsCmbk.setInacna(input.getInacna());
		knsCmbk.setInacno(input.getInacno());
		knsCmbk.setInamcd(input.getInamcd());
		knsCmbk.setPayatp(input.getPayatp());
		knsCmbk.setSmrytx(input.getSmrytx());
		knsCmbk.setPayseq(1);  //内部户转客户账一套传票中只有一条
		knsCmbk.setTranam(input.getTranam());
		knsCmbk.setCntsys(input.getCntsys()); //跨系统转账标志
		knsCmbk.setIavcst(E_CMBK_TRANST._0);  //传票状态:0 - 录入
		knsCmbk.setTrandt(trandt);
		knsCmbk.setTransq(transq);
		knsCmbk.setTranus(tranus);
		
		KnsCmbkDao.insert(knsCmbk);
		bizlog.debug("==========传票录入登记簿录入结束");
		
		//挂账登记簿录入
		if(knsCmbk.getPayatp() == E_PAYATP._1){
			
			bizlog.debug("挂账登记簿录入开始==========");
			for (PayaDetail payaDetail : input.getPayaListInfo()) {
				
				KnsPaya knsPaya = SysUtil.getInstance(KnsPaya.class);
				String payasq= MsSeqUtil.genSeq("PAYASQ", CommTools.getBaseRunEnvs().getTrxn_date());
				
				knsPaya.setPayasq(payasq);
				knsPaya.setAcstno(input.getAcstno());
				knsPaya.setPayseq(1); //内部户转客户账一套传票中只有一条
				knsPaya.setToacct(payaDetail.getToacno());
				knsPaya.setToacna(payaDetail.getToacna());
				knsPaya.setPayabr(payaDetail.getPayabr());
				knsPaya.setPayamn(payaDetail.getPayamn());
				knsPaya.setRsdlmn(payaDetail.getPayamn());
				knsPaya.setPayaac(input.getOtacno());
				knsPaya.setPayast(E_PAYAST.WFH);
				knsPaya.setCrcycd(input.getCrcycd());
				knsPaya.setTrandt(trandt);
				knsPaya.setTransq(transq);
				knsPaya.setTranus(tranus);
				knsPaya.setTemp01(input.getSmrytx());//摘要
				KnsPayaDao.insert(knsPaya);
				
				output.setPayasq(payasq);
				bizlog.debug("生成挂账记录挂账序号为：" + payasq);
			}
			bizlog.debug("==========挂账登记簿录入结束");
		}

		//销账登记簿录入
		if(knsCmbk.getPayatp() == E_PAYATP._2){
			
			bizlog.debug("销账登记簿录入开始==========");
			for (PaydDetail paydDetail : input.getPaydListInfo()) {
				
				KnsPayd knsPayd = SysUtil.getInstance(KnsPayd.class);
				//KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydDetail.getPrpysq(), true);
				KnsPaya knsPaya = InTranOutDao.selKnsPayaByPayasq(paydDetail.getPrpysq(), true);//改为不带法人查询
				String paydsq= MsSeqUtil.genSeq("PAYDSQ", CommTools.getBaseRunEnvs().getTrxn_date());
				
				knsPayd.setPaydsq(paydsq);//生成方法待定,测试暂设
				knsPayd.setAcstno(input.getAcstno());
				knsPayd.setPayseq(1); //内部户转客户账一套传票中只有一条
				knsPayd.setPayamn(paydDetail.getPaydmn());
				knsPayd.setPayasq(paydDetail.getPrpysq());
				knsPayd.setPaydac(knsPaya.getPayaac());
				knsPayd.setPayabr(knsPaya.getPayabr()); //挂账机构
				knsPayd.setPaydbr(CommTools.getBaseRunEnvs().getTrxn_branch()); //销账机构
				
				//change by chenjk 销账明细对方账号应为转入账号
//				knsPayd.setToacct(knsPaya.getToacct());
//				knsPayd.setToacna(knsPaya.getToacna());
				knsPayd.setToacct(knsCmbk.getInacno());	//转入账号		    
				knsPayd.setToacna(knsCmbk.getInacna());//转入户名
				knsPayd.setRsdlmn(knsPaya.getRsdlmn().subtract(paydDetail.getPaydmn()));
				knsPayd.setTotlmn(knsPaya.getRsdlmn());
				knsPayd.setPaydst(E_PAYDST.WFH);
				knsPayd.setTrandt(trandt);
				knsPayd.setTransq(transq);
				knsPayd.setUntius(tranus);
				knsPayd.setTemp01(input.getSmrytx());//摘要
				KnsPaydDao.insert(knsPayd);
				
				output.setPaydsq(paydsq);
				bizlog.debug("生成挂账记录销账序号为：" + paydsq);
				
			}
			bizlog.debug("==========销账登记簿录入结束");
		}
		
		bizlog.debug("==========内部账转客户账录入成功==========");
		
		dealCmbkData(trandt);  //联机交易时，处理之前垃圾数据
	}
	
	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-06 21：21</li>
	 *         <li>内部账转客户账查询</li>
	 *         </p>
	 * @param acstno
	 *          套号
	 * @param output
	 * 	     	内部账转客户账信息
	 * */
	@Override
	public void selIacuql(
			String acstno,
			cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.SelIacuql.Output output) {
	
		bizlog.debug("==========内部账转客户账查询处理开始==========");
		
		if(CommUtil.isNull(acstno)){
			throw InError.comm.E0003("输入套号不能为空");
		}
		
		if(acstno.length() != 11){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		if(CommTools.rpxMatch("W[0-9]+", acstno) == 1){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		
		bizlog.debug("传票信息处理开始==========");
		List<KnsCmbk> knsCmbkList = InTranOutDao.selKnsCmbkByAcst(acstno, trandt, false);
		//add by wuzx 20160910 beg
		if(null==knsCmbkList||knsCmbkList.size()==0){
			throw InError.comm.E0003("该套号对应的记录不存在！");
		}
		//add by wuzx 20160910 end 
		//内部户转客户账传票只能为一条
		/*if(knsCmbkList.size() > 1){
			throw InError.comm.E0003("检索出传票记录多余一条，请核查！");
		}*/
		KnsCmbk knsCmbk = knsCmbkList.get(0);  
		
		/*if(!CommUtil.equals(knsCmbk.getAcctbr(), tranbr)){
			//非本机构传票，直接返回
			return;
		}*/
		
		/*//传票类型验证
		if(CommUtil.isNotNull(knsCmbk.getIavctp())){
			
			if(knsCmbk.getIavctp() != E_IAVCTP._2){
				throw InError.comm.E0003("所查传票类型不为内部户转客户账，请核查");
			}
		}*/
		
		output.setAcstno(acstno);
		output.setSmrytx(knsCmbk.getSmrytx());
		output.setCntsys(knsCmbk.getCntsys());
		output.setIavcst(knsCmbk.getIavcst());//返回传票状态
		output.setOtacna(knsCmbk.getOtacna());
		output.setOtacno(knsCmbk.getOtacno());
		output.setOtamcd(knsCmbk.getOtamcd());
		output.setCrcycd(knsCmbk.getCrcycd());
		output.setPayatp(knsCmbk.getPayatp());
		output.setTranam(knsCmbk.getTranam());
		output.setInacna(knsCmbk.getInacna());
		output.setInacno(knsCmbk.getInacno());
		output.setAcctbr(knsCmbk.getAcctbr());
		output.setCsextg(knsCmbk.getCsextg());
		output.setInamcd(knsCmbk.getInamcd());
		output.setIntype(knsCmbk.getIntype());//跨法人标志
		
		bizlog.debug("==========传票信息填充结束");
		
		//如果传票类型是挂账
		if(knsCmbk.getPayatp() == E_PAYATP._1){
			
			bizlog.debug("销账信息填充开始==========");
			List<KnsPaya> knsPayaList = InTranOutDao.selKnsPayaByAcst(acstno, trandt,knsCmbk.getOtacno(), true);  //sql中自动过滤已作废记录
				
			Options<PayaDetail> payaDetailoptions = new DefaultOptions<PayaDetail>();
			for (KnsPaya knsPaya : knsPayaList) {
				
				PayaDetail payaDetail = SysUtil.getInstance(PayaDetail.class);
				payaDetail.setPayasq(knsPaya.getPayasq());
				payaDetail.setPayamn(knsPaya.getPayamn());
				payaDetail.setPayaac(knsPaya.getPayaac());
				payaDetail.setPayabr(knsPaya.getPayabr());
				payaDetail.setToacna(knsPaya.getToacna());
				payaDetail.setToacno(knsPaya.getToacct());
				
				payaDetailoptions.add(payaDetail);
				
			}
			output.setPayaListInfo(payaDetailoptions);
			bizlog.debug("==========销账信息填充结束");
				
		}

		//如果传票类型是销账
		if(knsCmbk.getPayatp() == E_PAYATP._2){
			
			bizlog.debug("销账信息填充开始==========");
			List<KnsPayd> knsPaydList = InTranOutDao.selKnsPaydByAcst(acstno, trandt, knsCmbk.getOtacno(),true); //sql中自动过滤已作废记录
				
			Options<PaydDetail> paydDetailoptions = new DefaultOptions<PaydDetail>();
			for (KnsPayd knsPayd : knsPaydList) {
				
				PaydDetail paydDetail = SysUtil.getInstance(PaydDetail.class);
				paydDetail.setPaydsq(knsPayd.getPaydsq());
				paydDetail.setPrpysq(knsPayd.getPayasq());
				paydDetail.setTotlmn(knsPayd.getTotlmn());
				paydDetail.setPaydmn(knsPayd.getPayamn());
				paydDetail.setPaydac(knsPayd.getPaydac());
				paydDetail.setRsdlmn(knsPayd.getRsdlmn());
				
				paydDetailoptions.add(paydDetail);
				
			}
			output.setPaydListInfo(paydDetailoptions);
			bizlog.debug("==========销账信息填充结束");
				
		}
		
		bizlog.debug("==========内部账转客户账查询处理成功==========");
	}
	
	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-06 21：21</li>
	 *         <li>内部账转客户账维护</li>
	 *         </p>
	 * @param input
	 *           内部账转客户账维护信息
	 * */
	@Override
	public void updMncutr(
			cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.UpdMncutr.Input input) {
		bizlog.debug("==========内部账转客户账维护处理开始==========");
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller();
		
		// 传票录入登记簿录入
		bizlog.debug("传票登记簿录入开始==========");
		KnsCmbk knsCmbk = KnsCmbkDao.selectFirst_kns_cmbk_odx2(input.getAcstno(), trandt, true);
		if(!CommUtil.equals(knsCmbk.getTranus(), tranus)){
			throw InError.comm.E0003("维护柜员必须为原操作柜员！");
		}
        if(knsCmbk.getIavcst() != E_CMBK_TRANST._0){
        	throw InError.comm.E0003("该记录不允许修改！");
        }
        
		knsCmbk.setAcctbr(input.getAcctbr());
		knsCmbk.setOtacna(input.getOtacna());
		knsCmbk.setOtacno(input.getOtacno());
		knsCmbk.setOtamcd(input.getOtamcd());
		knsCmbk.setCsextg(input.getCsextg());
		knsCmbk.setCrcycd(input.getCrcycd());
		knsCmbk.setInacna(input.getInacna());
		knsCmbk.setInacno(input.getInacno());
		knsCmbk.setInamcd(input.getInamcd());
		knsCmbk.setPayatp(input.getPayatp());
		knsCmbk.setSmrytx(input.getSmrytx());
		knsCmbk.setTranam(input.getTranam());
		knsCmbk.setPayseq(1);
		knsCmbk.setTrandt(trandt);
		knsCmbk.setTransq(transq);
		knsCmbk.setTranus(tranus);
		
		KnsCmbkDao.updateOne_kns_cmbk_odx1(knsCmbk);
		bizlog.debug("==========传票登记簿录入结束");
		
		
		if (input.getPayatp() == E_PAYATP._1) {
			
			bizlog.debug("挂账登记簿录入开始==========");
			if(CommUtil.isNull(input.getPayaListInfo())){
				throw InError.comm.E0003("挂账记录不能为空");
			}
			
			//将数据库中，该套号里数据放入map中
			List<KnsPaya> knsPayaList = KnsPayaDao.selectAll_kns_paya_odx5(input.getAcstno(), knsCmbk.getPayseq(), trandt, false);
			//add by wuzx-20161201 - 销账改挂账情况 -beg
			if(CommUtil.isNull(knsPayaList)||knsPayaList.size()==0){
				
				for (PayaDetail payaDetail : input.getPayaListInfo()) {					
					KnsPaya knsPaya = SysUtil.getInstance(KnsPaya.class);
					knsPaya.setPayabr(payaDetail.getPayabr());
					knsPaya.setPayamn(payaDetail.getPayamn());
					knsPaya.setRsdlmn(payaDetail.getPayamn());
					knsPaya.setToacct(payaDetail.getToacno());
					knsPaya.setToacna(payaDetail.getToacna());
					knsPaya.setTrandt(trandt);
					knsPaya.setTransq(transq);
					knsPaya.setTranus(tranus);
					knsPaya.setPayseq(1);
					knsPaya.setAcstno(input.getAcstno());
					knsPaya.setPayaac(input.getOtacno());
					knsPaya.setCrcycd(input.getCrcycd());
					knsPaya.setPayast(E_PAYAST.WFH);
					knsPaya.setTemp01(input.getSmrytx());//摘要				
					
					String payasq= MsSeqUtil.genSeq("PAYASQ", CommTools.getBaseRunEnvs().getTrxn_date());
					knsPaya.setPayasq(payasq);
					KnsPayaDao.insert(knsPaya);									
				}
				//add by wuzx-20161201 - 销账改挂账情况 -end
			}else{
				Map<String,KnsPaya> knsPayaMap = new HashMap<String,KnsPaya>();
				
				for(KnsPaya knsPaya : knsPayaList){
					knsPayaMap.put(knsPaya.getPayasq(), knsPaya);
				}
				
				//如果有序号
				for (PayaDetail payaDetail : input.getPayaListInfo()) {
					
					KnsPaya knsPaya = SysUtil.getInstance(KnsPaya.class);
					
					if(CommUtil.isNotNull(payaDetail.getPayasq())){
						
						knsPaya = knsPayaMap.get(payaDetail.getPayasq());
					}
					knsPaya.setAcstno(input.getAcstno());//套号
					knsPaya.setPayaac(input.getOtacno());//挂账序号
					knsPaya.setPayast(E_PAYAST.WFH);//挂账状态
					knsPaya.setCrcycd(input.getCrcycd());//币种
					knsPaya.setTemp01(input.getSmrytx());//摘要
					knsPaya.setPayabr(payaDetail.getPayabr());
					knsPaya.setPayamn(payaDetail.getPayamn());
					knsPaya.setRsdlmn(payaDetail.getPayamn());
					knsPaya.setToacct(payaDetail.getToacno());
					knsPaya.setToacna(payaDetail.getToacna());
					knsPaya.setTrandt(trandt);
					knsPaya.setTransq(transq);
					knsPaya.setTranus(tranus);
					knsPaya.setPayseq(1);
					
					if(CommUtil.isNotNull(payaDetail.getPayasq())){
						
						KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
						knsPayaMap.remove(payaDetail.getPayasq());
					}else{
						String payasq= MsSeqUtil.genSeq("PAYASQ", CommTools.getBaseRunEnvs().getTrxn_date());
						knsPaya.setPayasq(payasq);
						KnsPayaDao.insert(knsPaya);
					}
					
				}
				
				//更新记录中未出现销账明细,状态置为删除
				for(String payasq : knsPayaMap.keySet()){
					
					KnsPaya knsPaya = knsPayaMap.get(payasq);
					knsPaya.setPayast(E_PAYAST.ZF);
					KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
				}
				bizlog.debug("==========挂账登记簿录入结束");
			}		
		}
			
		//销账记录处理思路：输入中有销账序号的更新，无销账序号的新增；只在表中存在，但输入中为出现的状态改为作废。
		if (input.getPayatp() == E_PAYATP._2) {
			
			bizlog.debug("销账登记簿录入开始==========");
			if(CommUtil.isNull(input.getPaydListInfo())){
				throw InError.comm.E0003("销账记录不能为空");
			}
			
			//将数据库中，该套号里数据放入map中
			List<KnsPayd> knsPaydList = KnsPaydDao.selectAll_kns_payd_odx5(input.getAcstno(), knsCmbk.getPayseq(), trandt, false);
			//add by wuzx-20161201 - 内转客挂账改成销账情况 -beg
			if(CommUtil.isNull(knsPaydList)||knsPaydList.size()==0){
				
				for (PaydDetail paydDetail : input.getPaydListInfo()) {					
					KnsPayd knsPayd = SysUtil.getInstance(KnsPayd.class);
					KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydDetail.getPrpysq(), true);					
					knsPayd.setAcstno(input.getAcstno());
					knsPayd.setPayamn(paydDetail.getPaydmn());
					knsPayd.setToacct(input.getInacno());
					knsPayd.setToacna(input.getInacna());
					knsPayd.setPaydac(input.getOtacno()); //guazhang,fuhe
					 //记账之前，这两个字段无意义
					knsPayd.setRsdlmn(paydDetail.getRsdlmn());  
					knsPayd.setTotlmn(paydDetail.getTotlmn());
					knsPayd.setPayasq(paydDetail.getPrpysq());
					knsPayd.setPayabr(knsPaya.getPayabr());
					knsPayd.setPaydbr(CommTools.getBaseRunEnvs().getTrxn_branch());
					knsPayd.setPaydst(E_PAYDST.WFH);
					knsPayd.setTrandt(trandt);
					knsPayd.setTransq(transq);
					knsPayd.setUntius(tranus);
					knsPayd.setTemp01(input.getSmrytx());//摘要
					knsPayd.setPayseq(1);
					String paydsq= MsSeqUtil.genSeq("PAYDSQ", CommTools.getBaseRunEnvs().getTrxn_date());
					knsPayd.setPaydsq(paydsq);
					KnsPaydDao.insert(knsPayd);					
				}
				//add by wuzx-20161201 - 内转客挂账改成销账情况 -end
			}else{
				Map<String,KnsPayd> knsPaydMap = new HashMap<String,KnsPayd>();
				
				for(KnsPayd knsPayd : knsPaydList){
					knsPaydMap.put(knsPayd.getPaydsq(), knsPayd);
				}
				
				//如果有序号
				for (PaydDetail paydDetail : input.getPaydListInfo()) {
					
					KnsPayd knsPayd = SysUtil.getInstance(KnsPayd.class);
					
					if(CommUtil.isNotNull(paydDetail.getPaydsq())){
						
						knsPayd = knsPaydMap.get(paydDetail.getPaydsq());
					}
					KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydDetail.getPrpysq(), true);
					
					knsPayd.setAcstno(input.getAcstno());
					knsPayd.setPayamn(paydDetail.getPaydmn());
					knsPayd.setToacct(input.getInacno());
					knsPayd.setToacna(input.getInacna());
					knsPayd.setPaydac(input.getOtacno()); //guazhang,fuhe
					 //记账之前，这两个字段无意义
					knsPayd.setRsdlmn(paydDetail.getRsdlmn());  
					knsPayd.setTotlmn(paydDetail.getTotlmn());
					knsPayd.setPayasq(paydDetail.getPrpysq());
					knsPayd.setPayabr(knsPaya.getPayabr());
					knsPayd.setPaydbr(CommTools.getBaseRunEnvs().getTrxn_branch());
					knsPayd.setPaydst(E_PAYDST.WFH);
					knsPayd.setTrandt(trandt);
					knsPayd.setTransq(transq);
					knsPayd.setUntius(tranus);
					knsPayd.setPayseq(1);
					if(CommUtil.isNotNull(paydDetail.getPaydsq())){
						KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
						knsPaydMap.remove(paydDetail.getPaydsq());
					}else{
						String paydsq= MsSeqUtil.genSeq("PAYDSQ", CommTools.getBaseRunEnvs().getTrxn_date());
						
						knsPayd.setPaydsq(paydsq);
						KnsPaydDao.insert(knsPayd);
					}
					bizlog.debug("==========销账信息维护结束");
				}
				
				//更新记录中未出现销账明细,状态置为删除
				for(String paydsq : knsPaydMap.keySet()){
					KnsPayd knsPayd = knsPaydMap.get(paydsq);
					knsPayd.setPaydst(E_PAYDST.ZF);
					KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
				}		
			}
			bizlog.debug("==========内部账转客户账维护成功==========");		
			}	
	}

	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-06 21：21</li>
	 *         <li>内部账转内部户录入</li>
	 *         </p>
	 * @param input
	 *           内部账转内部户录入信息
	 * */
	public void insIavcbk(
			cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.InsIavcbk.Input input,
			cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.InsIavcbk.Output output) {
		
		bizlog.debug("==========内部账转内部户录入处理开始==========");
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller();
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		/*List<KnsCmbk> knsCmbks = InTranOutDao.selDelKnsCmbkByAcstno(input.getAcstno(), trandt, false);
		if(CommUtil.isNotNull(knsCmbks)||knsCmbks.size()>0){
			throw InError.comm.E0003("当日套号["+input.getAcstno()+"]已经删除，不能再次录入！");
		}*/
		if(CommUtil.isNull(input.getIavcbkListInfo()) || input.getIavcbkListInfo().size() == 0){
		    throw InError.comm.E0003("套号["+input.getAcstno()+"]下传票明细不能为空！");
		}
		
		//获取转入转出账号，方便销账明细获取对方账号和户名 add by chenjk 20161226
		String fistno = "";
		String fistna = "";
		String secdna = "";
		String secdno = "";
		if(input.getIavcbkListInfo().size() == 2){
			fistno = input.getIavcbkListInfo().get(0).getAcctno();
			fistna = input.getIavcbkListInfo().get(0).getAcctna();
			secdno = input.getIavcbkListInfo().get(1).getAcctno();
			secdna = input.getIavcbkListInfo().get(1).getAcctna();
		}
		
		// 传票录入登记簿录入 序号从1开始逐渐开始
		for(IavcbkDetail iavcbkDetail : input.getIavcbkListInfo()){

			bizlog.debug("传票登记簿录入开始==========");
			KnsCmbk knsCmbk = SysUtil.getInstance(KnsCmbk.class);
			int payseq = InTranOutDao.queryMaxCmbkSeq(input.getAcstno(), trandt, true);
			knsCmbk.setOtacna(iavcbkDetail.getAcctna());
			knsCmbk.setOtacno(iavcbkDetail.getAcctno());
			knsCmbk.setAcstno(input.getAcstno());
			knsCmbk.setOtamcd(iavcbkDetail.getAmntcd());
			knsCmbk.setCrcycd(iavcbkDetail.getCrcycd());
			knsCmbk.setIavctp(input.getIavctp());
			knsCmbk.setPayatp(iavcbkDetail.getPayatp());
			//knsCmbk.setSmrytx(input.getSmrytx());
			knsCmbk.setSmrytx(iavcbkDetail.getSmrytx());//摘要需要放入循环里面
			knsCmbk.setPayseq(payseq);
			knsCmbk.setTranam(iavcbkDetail.getTranam());
			knsCmbk.setCntsys(input.getCntsys());
			knsCmbk.setIavcst(E_CMBK_TRANST._0);
			knsCmbk.setTrandt(trandt);
			knsCmbk.setTransq(transq);
			knsCmbk.setTranus(tranus);
			knsCmbk.setAcctbr(tranbr);
			if(input.getIavcbkListInfo().size() == 2 && !CommUtil.equals(iavcbkDetail.getAcctno(), fistno)){
				knsCmbk.setInacno(fistno);
				knsCmbk.setInacna(fistna);
			}else if (input.getIavcbkListInfo().size() == 2 && !CommUtil.equals(iavcbkDetail.getAcctno(), secdno)) {
				knsCmbk.setInacno(secdno);
				knsCmbk.setInacna(secdna);
			}
			
			KnsCmbkDao.insert(knsCmbk);
			
			bizlog.debug("==========传票登记簿录入结束");
			
			
			if (iavcbkDetail.getPayatp() == E_PAYATP._1) {
				bizlog.debug("挂账登记簿录入开始==========");
				
				for (PayaDetail payaDetail : input.getPayaListInfo()) {
					
					/*if(input.getCntsys() == E_CNTSYS._0){
						//跨系统转账时，通过挂账账号匹配出传票所对应的挂账信息
						if(!CommUtil.equals(iavcbkDetail.getAcctno(), payaDetail.getPayaac())){
							continue;
						}
					}*/
					if(!CommUtil.equals(iavcbkDetail.getAcctno(), payaDetail.getPayaac()) 
							|| payseq != payaDetail.getCmbksq()){
						continue;
					}
					
					KnsPaya knsPaya = SysUtil.getInstance(KnsPaya.class);
					knsPaya.setAcstno(input.getAcstno());
					knsPaya.setPayseq(payseq);
					knsPaya.setPayabr(payaDetail.getPayabr());
					knsPaya.setPayamn(payaDetail.getPayamn());
					String payasq= MsSeqUtil.genSeq("PAYASQ", CommTools.getBaseRunEnvs().getTrxn_date());
					
					knsPaya.setPayasq(payasq);
					knsPaya.setPayaac(iavcbkDetail.getAcctno());
					knsPaya.setRsdlmn(payaDetail.getPayamn());
					knsPaya.setToacct(payaDetail.getToacno());
					knsPaya.setToacna(payaDetail.getToacna());
					knsPaya.setCrcycd(iavcbkDetail.getCrcycd());
					knsPaya.setPayast(E_PAYAST.WFH);
					knsPaya.setTrandt(trandt);
					knsPaya.setTransq(transq);
					knsPaya.setTranus(tranus);
					knsPaya.setTemp01(iavcbkDetail.getSmrytx());
					/*if(E_CNTSYS._0 == input.getCntsys()&&input.getIavcbkListInfo().size()==2){
						String Inacno2 = input.getIavcbkListInfo().get(0).getAcctno();//第二笔为系统外内部户账号
						String Inacna2 = input.getIavcbkListInfo().get(0).getAcctna();//第二笔系统内的内部户名称
						knsPaya.setToacct(Inacno2);//对方账号
						knsPaya.setToacna(Inacna2);//对方户名
					}*/
					KnsPayaDao.insert(knsPaya);
					
					output.setPayasq(payasq);
					bizlog.debug("生成挂账记录销账序号为：" + payasq);
					
				}
				bizlog.debug("==========挂账登记簿录入结束");
				
			}
	
			if (iavcbkDetail.getPayatp() == E_PAYATP._2) {
				
				bizlog.debug("销账登记簿录入开始==========");
				
				for (PaydDetail paydDetail : input.getPaydListInfo()) {
					
					/*if(input.getCntsys() == E_CNTSYS._0){
						//跨系统转账时，通过销账账号匹配出传票所对应的销账信息
						if(!CommUtil.equals(iavcbkDetail.getAcctno(), paydDetail.getPaydac())){
							continue;
						}
					}*/
					if(!CommUtil.equals(iavcbkDetail.getAcctno(), paydDetail.getPaydac())
							|| payseq != paydDetail.getCmbksq()){
						continue;
					}
					
					KnsPayd knsPayd = SysUtil.getInstance(KnsPayd.class);
					
					//通过挂账流水匹配到对应挂账记录
					KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydDetail.getPrpysq(), true);
					knsPayd.setAcstno(input.getAcstno());    //套号
					knsPayd.setPayseq(payseq);  //套内序号
					String paydsq= MsSeqUtil.genSeq("PAYDSQ", CommTools.getBaseRunEnvs().getTrxn_date());
					knsPayd.setPaydsq(paydsq);//生成方法待定,测试暂设
					knsPayd.setPaydac(iavcbkDetail.getAcctno());
					knsPayd.setPayamn(paydDetail.getPaydmn()); //销账金额
					knsPayd.setPayasq(paydDetail.getPrpysq()); //挂账序号
					knsPayd.setPayabr(knsPaya.getPayabr()); //挂账机构
					knsPayd.setPaydbr(CommTools.getBaseRunEnvs().getTrxn_branch()); //销账机构
					
					// modify by chenjk 20161226
					if(input.getIavcbkListInfo().size() == 2 && CommUtil.equals(knsPayd.getPaydac(), fistno)){
						//一挂一销，且销账账号与账号一相同时， 对方账号为账号二
						knsPayd.setToacct(secdno);//对方账号
						knsPayd.setToacna(secdna);//对方户名
					}else{
						
						//1.一挂一销，且销账账号与账号一不同时， 对方账号为账号一
						//2.非一挂一销时，对方账号为空（此时账号一为空）
						knsPayd.setToacct(fistno);//对方账号
						knsPayd.setToacna(fistna);//对方户名
					}
					/*if(E_CNTSYS._0 == input.getCntsys()&&input.getIavcbkListInfo().size()==2){
						String Inacno1 = input.getIavcbkListInfo().get(1).getAcctno();//第一笔系统内的内部户账号
						String Inacna1 = input.getIavcbkListInfo().get(1).getAcctna();//第一笔系统内的内部户名称
						knsPayd.setToacct(Inacno1);//对方账号
						knsPayd.setToacna(Inacna1);//对方户名
					}*/
					knsPayd.setRsdlmn(knsPaya.getRsdlmn().subtract(paydDetail.getPaydmn()));
					knsPayd.setTotlmn(knsPaya.getRsdlmn());
					
					knsPayd.setPaydst(E_PAYDST.WFH); //销账状态
					knsPayd.setTrandt(trandt); //日期
					knsPayd.setTransq(transq); //流水
					knsPayd.setUntius(""); //销账柜员应为记账柜员，故此处为空
					knsPayd.setTemp01(iavcbkDetail.getSmrytx());
					KnsPaydDao.insert(knsPayd); //
					
					output.setPaydsq(paydsq);
					bizlog.debug("生成挂账记录销账序号为：" + paydsq);
					
				}
				bizlog.debug("==========销账登记簿录入结束");
			}
		}
		bizlog.debug("==========内部账转内部户录入成功==========");
		dealCmbkData(trandt);  //联机交易时，处理之前垃圾数据
		
	}

	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-07 08：32</li>
	 *         <li>内部账转内部账查询</li>
	 *         </p>
	 * @param acstno
	 *          套号
	 * @param output  
	 * 			内部账转内部账输出信息      
	 * */
	@Override
	public void selIavcql(String acstno, Output output) {
		
		bizlog.debug("==========内部账转内部账查询处理开始==========");
		
		if (CommUtil.isNull(acstno)) {
			throw InError.comm.E0003("套号不能为空");
		}
		
		if(acstno.length() != 11){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		if(CommTools.rpxMatch("W[0-9]+", acstno) == 1){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		 List<KnsCmbk> knsCmbkList  = InTranOutDao.selKnsCmbkByAcst(acstno, trandt, false);
		if(CommUtil.isNull(knsCmbkList)){
			throw InError.comm.E0003("根据套号没有查询到记录！");
		}

		// iavcbkList内容填充
		Options<IavcbkDetail> iavcbkoptions = new DefaultOptions<IavcbkDetail>();
		Options<PaydDetail> paydDetailoptions = new DefaultOptions<PaydDetail>();
		Options<PayaDetail> payaDetailoptions = new DefaultOptions<PayaDetail>();
		
		bizlog.debug("传票登记簿信息处理开始==========");
		
		for(KnsCmbk knsCmbk : knsCmbkList){
			//过滤已作废传票
			if(knsCmbk.getIavcst()==E_CMBK_TRANST._2){
				continue;
			}
			if(CommUtil.isNotNull(knsCmbk.getIntype())){
				throw InError.comm.E0003("跨机构套号不允许交易！");
			}
			IavcbkDetail iavcbkDetail1 = SysUtil.getInstance(IavcbkDetail.class);
			iavcbkDetail1.setAcctna(knsCmbk.getOtacna());
			iavcbkDetail1.setAcctno(knsCmbk.getOtacno());
			iavcbkDetail1.setAmntcd(knsCmbk.getOtamcd());
			iavcbkDetail1.setCrcycd(knsCmbk.getCrcycd());
			iavcbkDetail1.setPayatp(knsCmbk.getPayatp());
			iavcbkDetail1.setPayseq(knsCmbk.getPayseq());
			iavcbkDetail1.setTranam(knsCmbk.getTranam());
			iavcbkDetail1.setIavcst(knsCmbk.getIavcst());
			iavcbkDetail1.setSmrytx(knsCmbk.getSmrytx());
			output.setAcstno(acstno);
			output.setCntsys(knsCmbk.getCntsys());//返回是否跨系统标识
			output.setIavctp(knsCmbk.getIavctp());
			output.setIntype(knsCmbk.getIntype());//返回是否跨法人标志
			output.setSmrytx(knsCmbk.getSmrytx());		
			iavcbkoptions.add(iavcbkDetail1);
			bizlog.debug("==========传票登记簿信息处理结束");
			
			// 挂账处理
			if(knsCmbk.getPayatp() ==  E_PAYATP._1){
				
				bizlog.debug("挂账信息处理开始==========");
				
				//List<KnsPaya> knsPayaList = InTranOutDao.selKnsPayaByAcst(acstno, trandt,knsCmbk.getOtacno(), true);
				//List<KnsPaya> knsPayaList = KnsPayaDao.selectAll_kns_paya_odx2(acstno, trandt, true);
				//只查询当前传票明细下的挂账序号   套号+传票序号+交易日期
				List<KnsPaya> knsPayaList = KnsPayaDao.selectAll_kns_paya_odx5(acstno, knsCmbk.getPayseq(), trandt, false);
				if (CommUtil.isNull(knsPayaList)) {
					throw InError.comm.E0003("传票为挂账，挂账明细不能为空");
				}
				for(KnsPaya knsPaya : knsPayaList){
					//过滤已作废挂账
					if(knsPaya.getPayast() == E_PAYAST.ZF){
						continue;
					}
					
					PayaDetail payaDetail = SysUtil.getInstance(PayaDetail.class);
					payaDetail.setPayasq(knsPaya.getPayasq());
					payaDetail.setCmbksq(knsPaya.getPayseq());
					payaDetail.setPayaac(knsPaya.getPayaac());
					payaDetail.setPayabr(knsPaya.getPayabr());
					payaDetail.setPayamn(knsPaya.getPayamn());
					payaDetail.setToacna(knsPaya.getToacna());
					payaDetail.setToacno(knsPaya.getToacct());
					payaDetailoptions.add(payaDetail);
				}
				bizlog.debug("==========挂账信息处理结束");
			}

			if(knsCmbk.getPayatp() ==  E_PAYATP._2){
				
				bizlog.debug("销账信息处理开始==========");
				//List<KnsPayd> knsPaydList =InTranOutDao.selKnsPaydByAcst(acstno, trandt,knsCmbk.getOtacno(),true);
				//只查询当前传票明细下的挂账序号   套号+传票序号+交易日期
				List<KnsPayd> knsPaydList = KnsPaydDao.selectAll_kns_payd_odx5(acstno, knsCmbk.getPayseq(), trandt, false);
				if (CommUtil.isNull(knsPaydList)) {
					throw InError.comm.E0003("传票为销账，销账明细不能为空");
				}
				for (KnsPayd knsPayd : knsPaydList) {
					
					//过滤已作废记录
					if(knsPayd.getPaydst() == E_PAYDST.ZF){
						continue;
					}
					
					PaydDetail paydDetail = SysUtil.getInstance(PaydDetail.class);
					paydDetail.setPaydmn(knsPayd.getPayamn());
					paydDetail.setCmbksq(knsPayd.getPayseq());
					paydDetail.setPrpysq(knsPayd.getPayasq());
					paydDetail.setPaydac(knsPayd.getPaydac());
					paydDetail.setRsdlmn(knsPayd.getRsdlmn());
					paydDetail.setTotlmn(knsPayd.getTotlmn());
					paydDetail.setPaydsq(knsPayd.getPaydsq());
					paydDetailoptions.add(paydDetail);
				}
				bizlog.debug("==========销账信息处理结束");
			}
		}
		output.setPayaListInfo(payaDetailoptions);
		output.setPaydListInfo(paydDetailoptions);
		output.setIavcbkListInfo(iavcbkoptions);
		bizlog.debug("==========内部账转内部账查询处理成功==========");
	}

	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-07 08：32</li>
	 *         <li>内部账转内部户维护</li>
	 *         </p>
	 * @param input
	 *           内部账转内部户录入信息
	 * */
	@Override
	public void updMnvcbk(
			cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.UpdMnvcbk.Input input) {
		
		bizlog.debug("==========内部账转内部户维护处理开始==========");
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller();
 
		// modify by chenjk 20161226
		//更新销账记录对方账号标志，若传票为更新变动且账号发生变动，uppayd状态置为true。
		//当knsCmbkList记录数为2,且非维护状态的另一笔传票为销账时，更新该销账明细对方账号。
		boolean uppayd = false;
		String acctno = ""; //对方账号
		String acctna = ""; //对方户名

		bizlog.debug("传票信息维护开始==========");
		
		List<KnsCmbk> knsCmbkList = InTranOutDao.selKnsCmbkByAcst2(input.getAcstno(), trandt,true);//过滤掉不能进行维护的传票
	    if(CommUtil.isNull(knsCmbkList)){
	    	throw InError.comm.E0003("没有可以进行维护的传票！");
	    }
		Map<Integer, KnsCmbk> knsCmbkMap = new HashMap<Integer, KnsCmbk>();		
		for(KnsCmbk knsCmbk : knsCmbkList){
			if(!CommUtil.equals(knsCmbk.getTranus(), tranus)){
				throw InError.comm.E0003("维护柜员必须为原录入柜员！");
			}
			knsCmbkMap.put(knsCmbk.getPayseq(), knsCmbk);		
		}
		
		for(IavcbkDetail iavcbkDetail : input.getIavcbkListInfo()){
			
			if(iavcbkDetail.getTranam() == BigDecimal.ZERO){
				throw InError.comm.E0003("交易金额不能为零");
			}
			//传票为新增时，标志为false，否则为true
			boolean upflag = true;  //默认为true
			KnsCmbk knsCmbk = knsCmbkMap.get(iavcbkDetail.getPayseq());
			Integer payseq = iavcbkDetail.getPayseq();
			
			//若表里不存在该条传票，则新建对象且upflag改为false
			if(CommUtil.isNull(knsCmbk)){
				upflag = false; 
				knsCmbk = SysUtil.getInstance(KnsCmbk.class);
				knsCmbk.setAcstno(input.getAcstno());
				payseq = InTranOutDao.queryMaxCmbkSeq(input.getAcstno(), trandt, true);
				knsCmbk.setPayseq(payseq);
				knsCmbk.setIavcst(E_CMBK_TRANST._0);
				knsCmbk.setIavctp(E_IAVCTP._1);
			}else{
				
				if(input.getIavcbkListInfo().size() ==2 && knsCmbk.getPayatp() == E_PAYATP._2){
					
					//当一次维护两条传票（跨系统转账）且其中一条有销账信息,将其保存map中，需进一步判断是否需更新销账明细对方账号
				}else{
					
					knsCmbkMap.remove(payseq);
				}
				
				// add by chenjk 20161226
				//账号发生变动，uppayd状态置为true
				if(!CommUtil.equals(knsCmbk.getOtacno(), iavcbkDetail.getAcctno())){
					acctno = iavcbkDetail.getAcctno(); //对方账号
					acctna = iavcbkDetail.getAcctna(); //对方户名
					uppayd = true;
				}
				
			}
			
			knsCmbk.setOtacna(iavcbkDetail.getAcctna());
			knsCmbk.setOtacno(iavcbkDetail.getAcctno());
			knsCmbk.setOtamcd(iavcbkDetail.getAmntcd());
			knsCmbk.setCrcycd(iavcbkDetail.getCrcycd());
			knsCmbk.setPayatp(iavcbkDetail.getPayatp());
			knsCmbk.setSmrytx(iavcbkDetail.getSmrytx());//摘要改为里面
			knsCmbk.setTranam(iavcbkDetail.getTranam());
			knsCmbk.setTrandt(trandt);
			knsCmbk.setTransq(transq);
			knsCmbk.setTranus(tranus);
				
			if(upflag){
				KnsCmbkDao.updateOne_kns_cmbk_odx1(knsCmbk);
			}else{
				KnsCmbkDao.insert(knsCmbk);
			}
			
			bizlog.debug("==========传票信息维护结束");
			if(iavcbkDetail.getPayatp() == E_PAYATP._0){//非挂销账
				InTranOutDao.upDelKnsPaydByAcstno(input.getAcstno(), knsCmbk.getPayseq(), trandt, DateTools2.TIMESTAMP21);
				InTranOutDao.upDelKnsPayaByAcstno(input.getAcstno(), knsCmbk.getPayseq(), trandt, DateTools2.TIMESTAMP21);
			}
			if(iavcbkDetail.getPayatp() == E_PAYATP._1){
				
				
				bizlog.debug("挂账信息维护开始==========");
				
				if (CommUtil.isNull(input.getPayaListInfo())) {
					throw InError.comm.E0003("本传票挂账记录不能为空");
				}
				List<KnsPaya> knsPayaList = KnsPayaDao.selectAll_kns_paya_odx5(input.getAcstno(), payseq, trandt, false);
                //需要根据套号转出账号查询挂账记录
				//销账改为挂账情况 modify by wuzx 2016118 beg
				if (CommUtil.isNull(knsPayaList)) {
					for (PayaDetail payaDetail : input.getPayaListInfo()) {
						
						if(payaDetail.getPayamn() == BigDecimal.ZERO){
							throw InError.comm.E0003("挂账金额不能为零");
						}
						
						//过滤掉非本传票的挂账明细  add by chenjk 20161226
						if(!CommUtil.equals(payaDetail.getPayaac(), knsCmbk.getOtacno())){
							continue;
						}
						
						KnsPaya knsPaya = SysUtil.getInstance(KnsPaya.class);
						knsPaya.setAcstno(input.getAcstno());
						knsPaya.setPayseq(payseq);
						knsPaya.setPayabr(payaDetail.getPayabr());
						knsPaya.setPayamn(payaDetail.getPayamn());
						knsPaya.setRsdlmn(payaDetail.getPayamn());
						knsPaya.setToacct(payaDetail.getToacno());
						knsPaya.setToacna(payaDetail.getToacna());
						knsPaya.setCrcycd(knsCmbk.getCrcycd());
						knsPaya.setPayast(E_PAYAST.WFH);
						knsPaya.setTrandt(trandt);
						knsPaya.setTransq(transq);
						knsPaya.setTranus(tranus);
						knsPaya.setTemp01(iavcbkDetail.getSmrytx());
						knsPaya.setPayaac(iavcbkDetail.getAcctno());//挂账账号
						String payasq= MsSeqUtil.genSeq("PAYASQ", CommTools.getBaseRunEnvs().getTrxn_date());
						
						knsPaya.setPayasq(payasq);
						KnsPayaDao.insert(knsPaya);
						//KnsPaydDao.delete_kns_payd_odx5(input.getAcstno(), payseq, trandt);//删除原有销账记录
					    InTranOutDao.updKnsPaydDel2(input.getAcstno(), payseq, trandt, DateTools2.TIMESTAMP21);
					}
					//销账改为挂账情况 modify by wuzx 2016118 end
					}else{
						  if(E_PAYAST.YFH== knsPayaList.get(0).getPayast()){
								throw InError.comm.E0003("本条挂账记录已复核！");
							}
							
							Map<String,KnsPaya> knsPayaMap = new HashMap<String,KnsPaya>();
							
							for(KnsPaya knsPaya : knsPayaList){
								knsPayaMap.put(knsPaya.getPayasq(), knsPaya);
							}
							
							for (PayaDetail payaDetail : input.getPayaListInfo()){
								
								if(payaDetail.getPayamn() == BigDecimal.ZERO){
									throw InError.comm.E0003("挂账金额不能为零");
								}
								
								//过滤掉非本传票的挂账明细  add by chenjk 20161226
								if(!CommUtil.equals(payaDetail.getPayaac(), knsCmbk.getOtacno())){
									continue;
								}
								
								KnsPaya knsPaya = SysUtil.getInstance(KnsPaya.class);
								if(CommUtil.isNotNull(payaDetail.getPayasq())){
									knsPaya = knsPayaMap.get(payaDetail.getPayasq());
									if(CommUtil.isNull(knsPaya)){
										 knsPaya = SysUtil.getInstance(KnsPaya.class);
									}
								}
								
								knsPaya.setAcstno(input.getAcstno());
								knsPaya.setPayseq(payseq);
								knsPaya.setPayaac(iavcbkDetail.getAcctno());//挂账账号
								knsPaya.setPayabr(payaDetail.getPayabr());
								knsPaya.setPayamn(payaDetail.getPayamn());
								knsPaya.setRsdlmn(payaDetail.getPayamn());
								knsPaya.setToacct(payaDetail.getToacno());
								knsPaya.setToacna(payaDetail.getToacna());
								knsPaya.setCrcycd(knsCmbk.getCrcycd());
								knsPaya.setPayast(E_PAYAST.WFH);
								knsPaya.setTrandt(trandt);
								knsPaya.setTransq(transq);
								knsPaya.setTranus(tranus);
								knsPaya.setTemp01(iavcbkDetail.getSmrytx());
								
								if(CommUtil.isNotNull(payaDetail.getPayasq())){
									KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
									knsPayaMap.remove(payaDetail.getPayasq());
								}else{
									String payasq= MsSeqUtil.genSeq("PAYASQ", CommTools.getBaseRunEnvs().getTrxn_date());
									
									knsPaya.setPayasq(payasq);
									KnsPayaDao.insert(knsPaya);
								}
								
							}
							
							//更新记录中未出现挂账明细,状态置为删除
							for(String payasq : knsPayaMap.keySet()){
								KnsPaya knsPaya = knsPayaMap.get(payasq);
								knsPaya.setPayast(E_PAYAST.ZF);
								KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
							}
							bizlog.debug("==========挂账信息维护结束");
						}
					}
    			 
			if(iavcbkDetail.getPayatp() == E_PAYATP._2){
				bizlog.debug("销账信息维护开始==========");
				
				if (CommUtil.isNull(input.getPaydListInfo())) {
					throw InError.comm.E0003("本传票销账记录不能为空");
				}
				
				List<KnsPayd> knsPaydList = KnsPaydDao.selectAll_kns_payd_odx5(input.getAcstno(), payseq, trandt, false);
				//modify by wuzx 20161118 挂账改为销账时 beg
				if(CommUtil.isNull(knsPaydList)){
					for(PaydDetail paydDetail : input.getPaydListInfo()){
						
						if(paydDetail.getPaydmn() == BigDecimal.ZERO){
							throw InError.comm.E0003("销账金额不能为零");
						}
						
						//过滤掉非本传票的销账明细  add by chenjk 20161226
						if(!CommUtil.equals(paydDetail.getPaydac(), knsCmbk.getOtacno())){
							continue;
						}
						
						KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydDetail.getPrpysq(), true);
						KnsPayd knsPayd = SysUtil.getInstance(KnsPayd.class);
						knsPayd.setAcstno(input.getAcstno());
						knsPayd.setPayseq(payseq);
						knsPayd.setPayamn(paydDetail.getPaydmn());
						knsPayd.setPaydac(iavcbkDetail.getAcctno()); //guazhang,fuhe
						knsPayd.setPayasq(paydDetail.getPrpysq());
						knsPayd.setPayabr(knsPaya.getPayabr());
						knsPayd.setPaydbr(CommTools.getBaseRunEnvs().getTrxn_branch());
						knsPayd.setPaydst(E_PAYDST.WFH);
						knsPayd.setTrandt(trandt);
						knsPayd.setTransq(transq);
						knsPayd.setUntius(""); //销账柜员应为记账柜员，故此处为空
						
						//销账记录中原未销金额为对应挂账当前剩余挂账金额，销账记录中剩余挂账金额为原未销金额减去本次销账金额
//						knsPayd.setRsdlmn(knsPaya.getRsdlmn().subtract(paydDetail.getPaydmn()));//更新剩余挂账金额
//						knsPayd.setTotlmn(knsPaya.getPayamn());
						knsPayd.setRsdlmn(paydDetail.getRsdlmn());
						knsPayd.setTotlmn(paydDetail.getTotlmn());
						String paydsq= MsSeqUtil.genSeq("PAYDSQ", CommTools.getBaseRunEnvs().getTrxn_date());
						knsPayd.setPaydsq(paydsq);
						KnsPaydDao.insert(knsPayd);
						InTranOutDao.updKnsPayaDel2(input.getAcstno(), payseq, trandt, DateTools2.TIMESTAMP21);
						//modify by wuzx 20161118 挂账改为销账时  end
					}
				}else{
					//add by wuzx 20160911
					if(E_PAYDST.YFH == knsPaydList.get(0).getPaydst()){
						throw InError.comm.E0003("本条销账记录已复核！");
					}
					//add by wuzx 20160911
					Map<String,KnsPayd> knsPaydMap = new HashMap<String,KnsPayd>();
					
					for(KnsPayd knsPayd : knsPaydList){
						knsPaydMap.put(knsPayd.getPaydsq(), knsPayd);
					}
					
					//如果有序号
					for (PaydDetail paydDetail : input.getPaydListInfo()) {
						
						if(paydDetail.getPaydmn() == BigDecimal.ZERO){
							throw InError.comm.E0003("销账金额不能为零");
						}
						
						//过滤掉非本传票的销账明细  add by chenjk 20161226
						if(!CommUtil.equals(paydDetail.getPaydac(), knsCmbk.getOtacno())){
							continue;
						}
						
						KnsPayd knsPayd = SysUtil.getInstance(KnsPayd.class);
						if(CommUtil.isNotNull(paydDetail.getPaydsq())){
							knsPayd = knsPaydMap.get(paydDetail.getPaydsq());
						}
						
						KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydDetail.getPrpysq(), true);
						
						knsPayd.setAcstno(input.getAcstno());
						knsPayd.setPayseq(payseq);
						knsPayd.setPayamn(paydDetail.getPaydmn());
						knsPayd.setPaydac(iavcbkDetail.getAcctno()); //guazhang,fuhe
						knsPayd.setPayasq(paydDetail.getPrpysq());
						knsPayd.setPayabr(knsPaya.getPayabr());
						knsPayd.setPaydbr(CommTools.getBaseRunEnvs().getTrxn_branch());
						knsPayd.setPaydst(E_PAYDST.WFH);
						knsPayd.setTrandt(trandt);
						knsPayd.setTransq(transq);
						knsPayd.setUntius(""); //销账柜员应为记账柜员，故此处为空
						//销账记录中原未销金额为对应挂账当前剩余挂账金额，销账记录中剩余挂账金额为原未销金额减去本次销账金额
//						knsPayd.setRsdlmn(knsPaya.getRsdlmn().subtract(paydDetail.getPaydmn()));//更新剩余挂账金额
//						knsPayd.setTotlmn(knsPaya.getPayamn());
						knsPayd.setRsdlmn(paydDetail.getRsdlmn());
						knsPayd.setTotlmn(paydDetail.getTotlmn());
						
						if(CommUtil.isNotNull(paydDetail.getPaydsq())){
							KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
							knsPaydMap.remove(paydDetail.getPaydsq());
						}else{
							String paydsq= MsSeqUtil.genSeq("PAYDSQ", CommTools.getBaseRunEnvs().getTrxn_date());
							knsPayd.setPaydsq(paydsq);
							KnsPaydDao.insert(knsPayd);
						}
						
					}
					//更新记录中未出现销账明细,状态置为删除
					for(String paydsq : knsPaydMap.keySet()){
						KnsPayd knsPayd = knsPaydMap.get(paydsq);
						knsPayd.setPaydst(E_PAYDST.ZF);
						KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
					}
					
					bizlog.debug("==========销账信息维护结束");
				}
				
				
			}
	}		
		for(Integer payseq : knsCmbkMap.keySet()){
			KnsCmbk knsCmbk = knsCmbkMap.get(payseq);
			//knsCmbk.setIavcst(E_CMBK_TRANST._2);
			
			if(uppayd ==true && knsCmbkList.size() == 2 && knsCmbk.getPayatp() == E_PAYATP._2){
				// modify by chenjk 20161226
				//更新销账记录对方账号标志，若传票为更新变动且账号发生变动，uppayd状态置为true。
				//当knsCmbkList记录数为2,且非维护状态的另一笔传票为销账时，更新该销账明细对方账号。
				List<KnsPayd> paydList = KnsPaydDao.selectAll_kns_payd_odx5(knsCmbk.getAcstno(), payseq, trandt, true);
				for(KnsPayd knsPayd : paydList){
					if(!CommUtil.equals(knsPayd.getPaydac(), acctno)){  // 维护两条传票时，通过本条件排除掉销账明细自身传票账号发生变动的情况
						
						knsPayd.setToacct(acctno);
						knsPayd.setToacna(acctna);
						KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
					}
				}
			}
			
//			KnsCmbkDao.updateOne_kns_cmbk_odx1(knsCmbk);
		}
		bizlog.debug("==========内部账转内部户维护成功==========");
	}
//}

	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-07 08：32</li>
	 *         <li>套账删除</li>
	 *         </p>
	 * @param acstno
	 *           套号
	 * */
	@Override
	public void delIavcde(String acstno) {
		
		bizlog.debug("==========套账删除处理开始==========");
		
		if (CommUtil.isNull(acstno)) {
			throw InError.comm.E0003("套号不能为空");
		}
		
		if(acstno.length() != 11){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		if(CommTools.rpxMatch("W[0-9]+", acstno) == 1){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller();
		List<KnsCmbk> knsCmbkList = KnsCmbkDao.selectAll_kns_cmbk_odx2(acstno, trandt, true);
		
		for(KnsCmbk knsCmbk : knsCmbkList){
			
			/*if(!CommUtil.equals(knsCmbk.getAcctbr(), tranbr)){
				throw InError.comm.E0003("非本机构传票！");
			}*/
			
			if(knsCmbk.getIavcst() == E_CMBK_TRANST._1){
				throw InError.comm.E0003("该套号已入账");
			}
			
			if(knsCmbk.getIavcst() == E_CMBK_TRANST._3){
				throw InError.comm.E0003("该套号已复核");
			}
			
			/*if(knsCmbk.getIavcst() == E_CMBK_TRANST._2){
				throw InError.comm.E0003("该套号已删除");
			}*/
			
			if(!CommUtil.equals(knsCmbk.getTranus(), tranus)){
				throw InError.comm.E0003("此操作只允许原录入柜员进行");
			}
			
			knsCmbk.setIavcst(E_CMBK_TRANST._2);
			KnsCmbkDao.updateOne_kns_cmbk_odx1(knsCmbk);
			
			bizlog.debug("==========传票状态更改结束");
			
			if(knsCmbk.getPayatp() == E_PAYATP._1){
				
				List<KnsPaya> knsPayaList = KnsPayaDao.selectAll_kns_paya_odx2(acstno, trandt, true);
				for(KnsPaya knsPaya : knsPayaList){
					
					//过滤已作废记录
					if(knsPaya.getPayast()==E_PAYAST.ZF){
						continue;
					}
					
					knsPaya.setPayast(E_PAYAST.ZF);
					KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
				}
				
				bizlog.debug("==========挂账信息处理结束");
			}
			
			//处理销账记录
			if(knsCmbk.getPayatp() == E_PAYATP._2){
				
				List<KnsPayd> knsPaydList = KnsPaydDao.selectAll_kns_payd_odx2(acstno, trandt, true);
				for(KnsPayd knsPayd : knsPaydList){
					
					//过滤已作废记录
					if(knsPayd.getPaydst()==E_PAYDST.ZF){
						continue;
					}
					
					knsPayd.setPaydst(E_PAYDST.ZF);
					KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
					
				}
				
				bizlog.debug("==========销账信息处理结束==========");
			}
			
		}
		bizlog.debug("==========套账删除处理结束==========");
	}

	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-07 08：32</li>
	 *         <li>内部账转内部账单条删除</li>
	 *         </p>
	 * @param acstno
	 *           套号
	 * @param payseq
	 *           套内序号
	 * */
	@Override
	public void delDevcbk(String acstno, Integer payseq) {
		bizlog.debug("==========内部账转内部账单条删除处理开始==========");
		String mtdate =CommTools.getBaseRunEnvs().getTrxn_date();
		String timetm =DateTools2.getCurrentTimestamp();
		
		if(CommUtil.isNull(acstno)){
			throw InError.comm.E0003("套号不能为空");
		}
		
		if(CommUtil.isNull(payseq)){
			throw InError.comm.E0003("套内序号不能为空");
		}
		//add bu wuzx 20161021 已经入账的套号不允许录入
		/*Integer exit  = InTranOutDao.selAcctKnsCmbk(acstno,CommTools.getBaseRunEnvs().getTrxn_date(),false);
			if(exit != 0){
			throw InError.comm.E0003("该套号已入账，不允许进行此业务！");
				}*/
		if(acstno.length() != 11){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		if(CommTools.rpxMatch("W[0-9]+", acstno) == 1){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		if(payseq < 0){
			throw InError.comm.E0003("套内序号不合法，请核查！");
		}
		
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		KnsCmbk knsCmbk = KnsCmbkDao.selectOne_kns_cmbk_odx1(acstno, payseq, trandt, true);
		
		if(knsCmbk.getIavcst() == E_CMBK_TRANST._1){
			throw InError.comm.E0003("该套号已入账");
		}
		
		if(knsCmbk.getIavcst() == E_CMBK_TRANST._2){
			throw InError.comm.E0003("该套号已删除");
		}
		
		if(knsCmbk.getIavcst() == E_CMBK_TRANST._3){
			throw InError.comm.E0003("该套号已复核");
		}
		
		if(!CommUtil.equals(knsCmbk.getTranus(), tranus)){
			throw InError.comm.E0003("此操作只允许原录入柜员进行");
		}
		
		knsCmbk.setIavcst(E_CMBK_TRANST._2);
		KnsCmbkDao.updateOne_kns_cmbk_odx1(knsCmbk);
		
		bizlog.debug("==========传票信息删除结束");
		
		if(knsCmbk.getPayatp() == E_PAYATP._1){
			InTranOutDao.updKnsPayaDel2(acstno, payseq, trandt,timetm);
			bizlog.debug("==========挂账信息删除结束");
		}
		
		if(knsCmbk.getPayatp() == E_PAYATP._2){
			InTranOutDao.updKnsPaydDel2(acstno, payseq, trandt,timetm);
			bizlog.debug("==========销账信息删除结束");
		}
		
		bizlog.debug("==========内部账转内部账单条删除处理成功==========");
	}

	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-07 08：32</li>
	 *         <li>内部账转客户账复核</li>
	 *         </p>
	 * @param input
	 *           内部账转客户账复核信息
	 * */
	@Override
	public void chkIacuck(
			cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.ChkIacuck.Input input) {
		
		bizlog.debug("==========内部账转客户账复核处理开始==========");
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller();//交易柜员		
		KnsCmbk knsCmbk = InTranOutDao.selFirstKnsCmbkByAcstno(input.getAcstno(), trandt, false);
		//KnsCmbk knsCmbk = KnsCmbkDao.selectFirst_kns_cmbk_odx2(input.getAcstno(), trandt, false);
		//modify by wuzx-20160910 -录入后，使用原录入柜员复核未报错 beg
		if(CommUtil.equals(knsCmbk.getTranus(),tranus)){
			throw InError.comm.E0003("复核柜员不能为原操作柜员+["+knsCmbk.getTranus()+"]！");
		}
		if(E_CMBK_TRANST._3==knsCmbk.getIavcst()){
			throw InError.comm.E0003("不能重复复核！");
		}
		if(!CommUtil.equals(input.getAcstno(), knsCmbk.getAcstno())){
			throw InError.comm.E0003("该套号对应的记录不存在！");
		}
		//modify by wuzx-20160910 -录入后，使用原录入柜员复核未报错 end 
		//复核传票记录
		knsCmbk.setIavcst(E_CMBK_TRANST._3);
		knsCmbk.setCkbsus(tranus);//复核柜员
		KnsCmbkDao.update_kns_cmbk_odx2(knsCmbk);
		
//		//复核挂账记录
//		if(input.getPayatp() == E_PAYATP._1){
//			
//			List<KnsPaya> knsPayaList = KnsPayaDao.selectAll_kns_paya_odx2(input.getAcstno(), trandt, true);
//			for(KnsPaya knsPaya : knsPayaList){
//				
//				//过滤已作废记录
//				if(knsPaya.getPayast()==E_PAYAST.ZF){
//					continue;
//				}
//				
//				knsPaya.setPayast(E_PAYAST.YFH);
//				KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
//			}
//			
//			bizlog.debug("==========挂账信息复核结束");
//		}
		//复核销账记录
		if(input.getPayatp() == E_PAYATP._2){
			
			List<KnsPayd> knsPaydList = KnsPaydDao.selectAll_kns_payd_odx2(input.getAcstno(), trandt, true);
			for(KnsPayd knsPayd : knsPaydList){
				
				//过滤已作废记录
				if(knsPayd.getPaydst()==E_PAYDST.ZF){
					continue;
				}
				
				knsPayd.setPaydst(E_PAYDST.YFH);
				KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
				
			}
			
			bizlog.debug("==========销账信息复核结束");
		}
	
		bizlog.debug("==========内部账转客户账复核处理成功==========");
	}

	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-07 08：32</li>
	 *         <li>取消复核</li>
	 *         </p>
	 * @param acstno
	 *           套号
	 * */
	@Override
	public void dckIackbk(String acstno) {
		
		bizlog.debug("=========取消复核处理开始==========");
		String mtdate =CommTools.getBaseRunEnvs().getTrxn_date();
		String timetm =DateTools2.getCurrentTimestamp();
		
		if(CommUtil.isNull(acstno)){
			throw InError.comm.E0003("输入套号不能为空");
		}
		
		if(acstno.length() != 11){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		if(CommTools.rpxMatch("W[0-9]+", acstno) == 1){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller();
		//modify by wuzx 取消复核调整 beg
		
		List<KnsCmbk> knsCmbks =  InTranOutDao.selKnsCmbkChkByAcstno(acstno, trandt, false);
		if(CommUtil.isNull(knsCmbks)){
			throw InError.comm.E0003("无可以取消复核的传票或处于此状态下的传票不能做此操作!");
		}
        for(KnsCmbk iavcbkDetail : knsCmbks){
        	if(iavcbkDetail.getIavcst() == E_CMBK_TRANST._1){
    			throw InError.comm.E0003("套内传票已入账，请检查!");
    		}
    		
    		if(iavcbkDetail.getIavcst() == E_CMBK_TRANST._9){
    			throw InError.comm.E0003("套内传票已冲账，请检查!");
    		}
    	
    		if(iavcbkDetail.getIavcst() == E_CMBK_TRANST._0){
    			throw InError.comm.E0003("套内传票未复核，请检查!");
    		}
    		
    		if(iavcbkDetail.getIavcst() == E_CMBK_TRANST._2){
    			throw InError.comm.E0003("套内传票已删除，请检查!");
    		}

    		if(!CommUtil.equals(iavcbkDetail.getCkbsus(), tranus)){
    			throw InError.comm.E0003("操作柜员必须为原复核柜员！");
    		}
    		//传票记录取消复核
    		InTranOutDao.updKnsCmbkDck(acstno, trandt,timetm);
    		bizlog.debug("==========传票处理结束");
    		//挂账记录取消复核
    		InTranOutDao.updKnsPayaDck(acstno, trandt,timetm);
    		bizlog.debug("==========挂账处理结束");
    		//销账记录取消复核
    		InTranOutDao.updKnsPaydDck(acstno, trandt,timetm);
    		//modify by wuzx 取消复核调整 end
    		bizlog.debug("==========销账处理结束");
    		bizlog.debug("=========取消复核处理成功==========");
        }
				
	}

	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-07 08：32</li>
	 *         <li>套平入账</li>
	 *         </p>
	 * @param acstno
	 *           套号
	 * String trandt
	 *           支付平台日期
	 * String transq
	 *           支付平台流水	
	 * */
	@Override
	public void accIavccm(String acstno, String trandt, String transq, cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.AccIavccm.Output output) {
		
		bizlog.debug("==========套平入账处理开始==========");
//		iavccmChk(acstno, transq); //数据检查 //放到交易前方法里面
		
		//因一般习惯用trandt，transq变量表示交易日期和交易流水，故处理中将支付平台日期和流水改用其它变量处理
		String platdt = trandt; //支付平台日期
		String platsq = transq; //支付平台流水
		trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期

		bizlog.debug("==========套平入账检查结束--入账操作处理开始==========");
		
		// 判断账号1是否内部账号:
		// 1，若账号1是内部账号则记一笔内部账，再判断是否转客户账，不是则结束，若是则继续判断是系统内还是系统外，是则调记内部客户账，不是则调一笔跨系统补记。
		// 2.若账号1不是内部账号，则调一笔跨系统补记
		Boolean cntsysFlag = true; //跨系统标志
		List<KnsCmbk> cmbkList = KnsCmbkDao.selectAll_kns_cmbk_odx2(acstno, trandt, true);
		
		//注册冲正交易总金额计算
		BigDecimal totlam = new BigDecimal(0);
	
		for(KnsCmbk knsCmbk: cmbkList){
			
			totlam = totlam.add(knsCmbk.getTranam());
			if (knsCmbk.getIavcst() !=E_CMBK_TRANST._3) {
				 throw InError.comm.E0003("该套号存在未复核的传票明细");
			}
			//复核柜员赋值
			BusiTools.getBusiRunEnvs().setCkbsus(knsCmbk.getCkbsus());
			
			if(knsCmbk.getIavcst() == E_CMBK_TRANST._2){
				continue;
			}
			
			output.setCntsys(knsCmbk.getCntsys()); //跨系统转账标志
			output.setIavctp(knsCmbk.getIavctp()); //传票类型
			//判断账号1是否是本系统内部账号  内部户生产的账号规则改变  无法根据业务编码校验
			//内部账号是否本系统账号  直接默认为本系统账号
			/*if(knsCmbk.getOtacno().length() == 23){
				IoBusinoInfo ioBusinoInfo = SysUtil.getInstance(IoInQuery.class).selBusiIndoByBusino(knsCmbk.getOtacno().substring(8, 18));
				if(CommUtil.isNotNull(ioBusinoInfo)){
					// 长度23位，且第九位至第十九位为内部户产品表中某产品代码，判断为本系统内部账号
					cntsysFlag =true;
				}
			}*/
			//20180522 yanghao 修改内部户校验规则，注释跨系统转账处理
			GlKnaAcct glKnaAcct = InQuerySqlsDao.sel_GlKnaAcct_by_acct(knsCmbk.getOtacno(),false);
			if(cntsysFlag&&CommUtil.isNotNull(glKnaAcct)){
			   
				bizlog.debug("系统内部户入账开始==========");
				insysInaccAcc(knsCmbk, acstno, trandt);
				bizlog.debug("==========系统内内部户入账处理结束");
				
				//判断是转客户账
				/*if(knsCmbk.getIavctp() == E_IAVCTP._2){ //rambo delete 拆分成客入帐服务
					//继续判断是系统内客户账，还是跨系统客户账
					if(knsCmbk.getCntsys() == E_CNTSYS._1){
						
						// 系统内转客户账处理
						bizlog.debug("系统内客户账入账处理开始==========");
						insysCustaccAcc(knsCmbk);
						bizlog.debug("==========系统内客户账入账处理结束");
						output.setCardno(knsCmbk.getInacno()); //为系统内内部户时，传递卡号，用于后处理平衡检查 //to iavccm line129
						
					}else{
						
						//跨系统客户账记账
						bizlog.debug("跨系统客户账入账处理开始==========");
						crosysCustaccAcc(knsCmbk, acstno, trandt); 
						bizlog.debug("==========跨系统客户账入账处理结束");
						output.setCardno(knsCmbk.getInacno());//为系统内内部户时，传递卡号，用于后处理平衡检查
					}
				//若不是转客户账，空处理
				}*/
//			}else{
//			    
//				bizlog.debug("跨系统内部户入账处理开始==========");
//				crosysInaccAcc(knsCmbk, acstno, trandt);
//				bizlog.debug("==========跨系统内部户入账处理结束");
//				output.setCardno(knsCmbk.getInacno());
//				
			}
			
			knsCmbk.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			knsCmbk.setPlatsq(platsq);
			knsCmbk.setPlatdt(platdt);
			knsCmbk.setIavcst(E_CMBK_TRANST._1);
			KnsCmbkDao.updateOne_kns_cmbk_odx1(knsCmbk);
		
		}
		output.setTranam(totlam);
		
		//冲正注册
		//此处是冲正时执行ApStrike中TRANS_EVENT_INACOT代码段注册
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
        cplInput.setTranam(totlam);
        cplInput.setTranac(cmbkList.get(0).getOtacno()); 
        cplInput.setEvent1(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //交易流水
        cplInput.setCrcycd(cmbkList.get(0).getCrcycd());
        cplInput.setBgindt(CommTools.getBaseRunEnvs().getTrxn_date());//交易日期
        cplInput.setTranev(ApUtil.TRANS_EVENT_INACOT);
        //ApStrike.regBook(cplInput);	
        IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);      
        apinput.setReversal_event_id(cplInput.getTranev());
        apinput.setInformation_value(SysUtil.serialize(cplInput));
        MsEvent.register(apinput, true);
    	bizlog.debug("==========套平入账成功==========");
	}

	/**
	 * @author chenjk
	 *         <p>
	 *         <li>2016-07-07 08：32</li>
	 *         <li>内部账转内部户复核</li>
	 *         </p>
	 * @param input
	 *           内部账转内部户复核信息
	 * */
	@Override
	public void chkIavcck(
			cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.ChkIavcck.Input input) {
		
		bizlog.debug("==========内部账转内部户复核开始==========");
	    
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller();//交易柜员
		
		//输入list,拿到list里面每个payseq,查出对应的记录
		for(IavcbkDetail iavcbkDetail : input.getIavcbkListInfo()){
			
			//modify by wuzx-20160910 -录入后，使用原录入柜员复核未报错 beg
			if(CommUtil.isNull(iavcbkDetail.getPayseq())||iavcbkDetail.getPayseq()==0){
					throw InError.comm.E0003("序号不能为空！");
				}
			KnsCmbk knsCmbk = KnsCmbkDao.selectOne_kns_cmbk_odx1(input.getAcstno(), iavcbkDetail.getPayseq(), trandt, false);	
			//add by wuzx 同一笔套号只能有一个柜员复核
			/*if(input.getIavcbkListInfo().get(0).getIavcst()== E_CMBK_TRANST._3){
				if(!CommUtil.equals(knsCmbk.getCkbsus(),tranus)){
					throw InError.comm.E0003("复核柜员与初始柜员不符合！");
				}
			}*/
			if(CommUtil.isNull(knsCmbk)){
				throw InError.comm.E0003("该套号对应的记录不存在！");
			}
			List<KnsCmbk> knscmbklist = InTranOutDao.selKnsCmbkChkByAcstno(input.getAcstno(), trandt, false);
			if(knscmbklist!=null&&knscmbklist.size()>0){ 
				if(knscmbklist.get(0).getIavcst() == E_CMBK_TRANST._3){
					if(!CommUtil.equals(knscmbklist.get(0).getCkbsus(),tranus)){
						throw InError.comm.E0003("复核柜员与初始柜员不符合！");
					}
				}
			}
			if(!CommUtil.equals(iavcbkDetail.getTranam(), knsCmbk.getTranam())){
					throw InError.comm.E0003("交易金额与录入时不一致！");				
			}
			E_AMNTCD amtcd1 = iavcbkDetail.getAmntcd();
			E_AMNTCD amtcd2 = knsCmbk.getOtamcd();
			if(!amtcd1.equals(amtcd2)){
					throw InError.comm.E0003("借贷方向与录入时不一致！");
			}
			if(CommUtil.compare(iavcbkDetail.getPayseq(), knsCmbk.getPayseq()) != 0){
					throw InError.comm.E0003("序号不一致！！");
			}
			if(!CommUtil.equals(iavcbkDetail.getSmrytx(),knsCmbk.getSmrytx())){
				throw InError.comm.E0003("摘要不一致！");	
			}
			if(CommUtil.equals(knsCmbk.getTranus(),tranus)){
				throw InError.comm.E0003("复核柜员不能为原操作柜员！");
			}	
			if(E_CMBK_TRANST._3==knsCmbk.getIavcst()){
				throw InError.comm.E0003("不能重复复核！");
			}
			//modify by wuzx-20160910 -录入后，使用原录入柜员复核未报错 end
			  //复核传票记录
				knsCmbk.setIavcst(E_CMBK_TRANST._3);
				knsCmbk.setCkbsus(tranus);
				KnsCmbkDao.updateOne_kns_cmbk_odx1(knsCmbk);
				
				bizlog.debug("==========传票登记簿复核结束");
				
				//复核挂账记录
				if(iavcbkDetail.getPayatp() == E_PAYATP._1){
					//数据库记录
					List<KnsPaya> knsPayaList = KnsPayaDao.selectAll_kns_paya_odx5(input.getAcstno(), iavcbkDetail.getPayseq(), trandt, true);
					//内管需要复核的数据
					List<PayaDetail> payaListInfo = new ArrayList<PayaDetail>();
					for (PayaDetail payaDetail : input.getPayaListInfo()) {
						if(CommUtil.equals(iavcbkDetail.getPayseq().toString(), payaDetail.getCmbksq().toString())){
							payaListInfo.add(payaDetail);
						}
					}
					if(knsPayaList.size() == payaListInfo.size()){
						for (int j = 0; j < knsPayaList.size(); j++) {
							/*if (!CommUtil.equals(knsPayaList.get(j).getPayaac(),payaListInfo.get(j).getPayaac())) {
								throw InError.comm.E0003("传票序号" +iavcbkDetail.getPayseq()+
										"第"+j+1+"条，挂账明细中挂账账号与录入不一致");
							}*/
							if (!CommUtil.equals(knsPayaList.get(j).getPayabr(),payaListInfo.get(j).getPayabr())) {
								throw InError.comm.E0003("传票序号" +iavcbkDetail.getPayseq()+
										"第"+j+1+"条，挂账明细中挂账机构与录入不一致");
							}
							if (!CommUtil.equals(knsPayaList.get(j).getPayasq(),payaListInfo.get(j).getPayasq())) {
								throw InError.comm.E0003("传票序号" +iavcbkDetail.getPayseq()+
										"第"+j+1+"条，挂账明细中挂账序号与录入不一致");
							}
							if (!CommUtil.equals(knsPayaList.get(j).getToacna(),payaListInfo.get(j).getToacna())) {
								throw InError.comm.E0003("传票序号" +iavcbkDetail.getPayseq()+
										"第"+j+1+"条，挂账明细中对方户名与录入不一致");
							}
							if (!CommUtil.equals(knsPayaList.get(j).getToacct(),payaListInfo.get(j).getToacno())) {
								throw InError.comm.E0003("传票序号" +iavcbkDetail.getPayseq()+
										"第"+j+1+"条，挂账明细中对方账号与录入不一致");
							}
						}
					}else {
						throw InError.comm.E0003("传票序号" +iavcbkDetail.getPayseq()+ "挂账信息与原录入不一致");
					}
					
					for(KnsPaya knsPaya : knsPayaList){
						//modify by wuzx-20160910 -复核校验 beg
						if(CommUtil.isNull(knsPaya)){
							throw InError.comm.E0003("该套号对应的记录不存在！");
						}
						//modify by wuzx
						if(CommUtil.equals(knsPaya.getTranus(),tranus)){
							throw InError.comm.E0003("复核柜员不能为原操作柜员！");
						}	
						if(E_PAYAST.YFH == knsPaya.getPayast()){
							throw InError.comm.E0003("不能重复复核！");
						}				
						if(CommUtil.isNull(knsPaya.getPayasq())){
							throw InError.comm.E0003("挂账序号不能为空！");
						}
						//modify by wuzx-20160910 -复核校验 end
						//过滤已作废记录
						if(knsPaya.getPayast()==E_PAYAST.ZF){
							continue;
						}
						
						knsPaya.setPayast(E_PAYAST.YFH);
						KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
					}
					
					bizlog.debug("==========挂账信息复核结束");
				}
				
				//复核销账记录
				if(iavcbkDetail.getPayatp() == E_PAYATP._2){
					
					//过滤已作废记录
					List<KnsPayd> knsPaydList = KnsPaydDao.selectAll_kns_payd_odx5(input.getAcstno(),iavcbkDetail.getPayseq(), trandt, true);
					
					//内管需要复核的数据
					List<PaydDetail> paydListInfo = new ArrayList<PaydDetail>();
					for (PaydDetail paydDetail : input.getPaydListInfo()) {
						if(CommUtil.equals(iavcbkDetail.getPayseq().toString(),paydDetail.getCmbksq().toString())){
							paydListInfo.add(paydDetail);
						}
					}
					if(knsPaydList.size() == paydListInfo.size()){
						for (int j = 0; j < knsPaydList.size(); j++) {
							if (!CommUtil.equals(knsPaydList.get(j).getPaydac(),paydListInfo.get(j).getPaydac())) {
								throw InError.comm.E0003("传票序号" +iavcbkDetail.getPayseq()+
										"第"+j+1+"条，销账明细中销账账号与录入不一致");
							}
							if (!CommUtil.equals(knsPaydList.get(j).getPayamn(),paydListInfo.get(j).getPaydmn())) {
								throw InError.comm.E0003("传票序号" +iavcbkDetail.getPayseq()+
										"第"+j+1+"条，销账明细中本次销账金额与录入不一致");
							}
							if (!CommUtil.equals(knsPaydList.get(j).getPaydsq(),paydListInfo.get(j).getPaydsq())) {
								throw InError.comm.E0003("传票序号" +iavcbkDetail.getPayseq()+
										"第"+j+1+"条，销账明细中销账序号与录入不一致");
							}
							if (!CommUtil.equals(knsPaydList.get(j).getPayasq(),paydListInfo.get(j).getPrpysq())) {
								throw InError.comm.E0003("传票序号" +iavcbkDetail.getPayseq()+
										"第"+j+1+"条，销账明细中挂账序号与录入不一致");
							}
							if (!CommUtil.equals(knsPaydList.get(j).getRsdlmn(),paydListInfo.get(j).getRsdlmn())) {
								throw InError.comm.E0003("传票序号" +iavcbkDetail.getPayseq()+
										"第"+j+1+"条，销账明细中剩余挂账金额与录入不一致");
							}
							if (!CommUtil.equals(knsPaydList.get(j).getTotlmn(),paydListInfo.get(j).getTotlmn())) {
								throw InError.comm.E0003("传票序号" +iavcbkDetail.getPayseq()+
										"第"+j+1+"条，销账明细中原未销金额与录入不一致");
							}
						}
					}else {
						throw InError.comm.E0003("传票序号" +iavcbkDetail.getPayseq()+ "销账信息与原录入不一致");
					}
					
					for(KnsPayd knsPayd : knsPaydList){
						//modify by wuzx-20160910 -复核校验 beg
						if(CommUtil.isNull(knsPayd)){
							throw InError.comm.E0003("该套号对应的记录不存在！");
						}
						//modify by wuzx 20161026 复核控制到套号即可
						/*if(CommUtil.equals(knsPayd.getUntius(),tranus)){
							throw InError.comm.E0003("复核柜员不能为原操作柜员！");
						}*/
						if(E_PAYDST.YFH == knsPayd.getPaydst()){
							throw InError.comm.E0003("不能重复复核！");
						}					
						/*if(CommUtil.isNull(knsPayd.getPayasq())){
							throw InError.comm.E0003("挂账序号不能为空！");
						}*/
						//modify by wuzx-20160910 -复核校验 end
						if(knsPayd.getPaydst()==E_PAYDST.ZF){
							continue;
						}		
						knsPayd.setUntius(tranus); //销账柜员为记账柜员，复核时赋值
						knsPayd.setPaydst(E_PAYDST.YFH);
						KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
						
					}
					
					bizlog.debug("==========销账信息复核结束");
				}
			
		}
		bizlog.debug("==========内部账转内部户复核成功==========");
	}
	
	/**
	 * 挂账明细查询
	 */
	@Override
	public Options<PayaListOutPut> selPayaList(String payasq, String acctno, E_PAQRST status,
			String bgdate, String endate) {
		String sendbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		try {
			
			 SysUtil.getInstance(IoSrvPbBranch.class).getBranch(sendbr);
		} catch (Exception e) {
			throw InError.comm.E0003("查询不到数据!");
		}

		String tranbr =CommTools.getBaseRunEnvs().getTrxn_branch(); //交易机构
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		
		if(CommUtil.isNotNull(bgdate)){
			
			if(CommUtil.compare(bgdate, trandt) > 0){
				throw InError.comm.E0003("起始日期不能大于当前交易日期，请核查!");
			}
			
			if(CommUtil.isNotNull(endate) && CommUtil.compare(bgdate, endate) > 0){
				throw InError.comm.E0003("起始日期不能大于截止日期，请核查!");
			}
			
		}
		
		if(CommUtil.isNotNull(endate) && CommUtil.compare(endate, trandt) > 0){
			throw InError.comm.E0003("截止日期不能大于当前交易日期，请核查!");
		}
		
		long pageno =CommTools.getBaseRunEnvs().getPage_start(); //页码
		long pagesize =CommTools.getBaseRunEnvs().getPage_size();//页容量
		
		
		E_PAYAST payast =null;//默认查询正常状态
		if(status == E_PAQRST.YX){
			
			payast= E_PAYAST.JQ;
		}else if (status == E_PAQRST.WX){
			
			payast=E_PAYAST.ZC;
		}
		//查询下级机构插入临时表
		//SysUtil.getInstance(IoBrchSvcType.class).insLowerLevelBrchno(tranbr, E_CRCYCD.RMB);	
		//查询挂账信息
		Page<PayaList> list = InTranOutDao.selPayaList(payasq, acctno, payast, bgdate, endate, tranbr, (pageno - 1)*pagesize, pagesize,0, false);
		
		Options<PayaListOutPut> payaInfo = new DefaultOptions<PayaListOutPut>();
		for(PayaList info: list.getRecords()){
			PayaListOutPut out = SysUtil.getInstance(PayaListOutPut.class);
			CommUtil.copyProperties(out, info);
			out.setPayasq(info.getPayasq());
			out.setAcctno(info.getPayaac());//挂账账号
			out.setPayadt(info.getTrandt());//挂账日期
			out.setAcctbr(info.getPayabr());
			out.setTranus(info.getTranus());
			out.setToacct(info.getToacct());
			out.setToacna(info.getToacna());
			out.setCrcycd(info.getCrcycd());
			out.setTranam(info.getPayamn());
			out.setCavfct(info.getPaydnm());//已经核销笔数
			out.setCavfam(info.getPayamn().subtract(info.getRsdlmn()));//已销金额
			out.setRemnam(info.getRsdlmn());//未销金额
		    out.setRemktx(info.getTemp01());//摘要
			GlKnaAcct acct = InQuerySqlsDao.sel_GlKnaAcct_by_acct(info.getPayaac(), true);
			out.setAmntcd(acct.getBlncdn());
			payaInfo.add(out);
		}
		CommTools.getBaseRunEnvs().setTotal_count(list.getRecordCount());
		return payaInfo;
	}
	
		/**
		 * 销账明细查询
		 */
	@Override
	public Options<PaydListOutPut> selPaydList(String payasq, String acctno, E_PDQRST status,
			String bgdate, String endate, String toacct, String paydsq) {
		
		
		String tranbr =CommTools.getBaseRunEnvs().getTrxn_branch(); //机构号
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		
		try {
			
			 SysUtil.getInstance(IoSrvPbBranch.class).getBranch(tranbr);
		} catch (Exception e) {
			throw InError.comm.E0003("查询不到数据!");
		}
		
		if(CommUtil.isNotNull(bgdate)){
			
			if(CommUtil.compare(bgdate, trandt) > 0){
				throw InError.comm.E0003("起始日期不能大于当前交易日期，请核查!");
			}
			
			if(CommUtil.isNotNull(endate) && CommUtil.compare(bgdate, endate) > 0){
				throw InError.comm.E0003("起始日期不能大于截止日期，请核查!");
			}
			
		}
		
		if(CommUtil.isNotNull(endate) && CommUtil.compare(endate, trandt) > 0){
			throw InError.comm.E0003("截止日期不能大于当前交易日期，请核查!");
		}
		
		long pageno =CommTools.getBaseRunEnvs().getPage_start();	//页码
		long pagesize =CommTools.getBaseRunEnvs().getPage_size();	//页容量
		
		E_PAYDST paydst =E_PAYDST.ZC;//默认查询正常状态
		if(status == E_PDQRST.ZC){
			
			paydst= E_PAYDST.ZC;
		}else if (status == E_PDQRST.YCZ){
			
			paydst=E_PAYDST.CX;
		}
		//查询下级机构插入临时表
		//SysUtil.getInstance(IoSrvPbBranch.class).insLowerLevelBrchno(tranbr, E_CRCYCD.RMB);	
		//查询销账信息
		Page<PaydList> list = InTranOutDao.selPaydList(payasq, acctno, paydst, bgdate, endate, tranbr, paydsq, toacct, (pageno - 1)*pagesize, pagesize, 0, false);
		
		Options<PaydListOutPut> paydInfo = new DefaultOptions<PaydListOutPut>();
		for(PaydList info :list.getRecords()){
			
			PaydListOutPut out = SysUtil.getInstance(PaydListOutPut.class);
			KnsPaya tblPaya = InTranOutDao.selKnsPayaByPayasq(info.getPayasq(), true);
			CommUtil.copyProperties(out, info);
			out.setPayddt(info.getTrandt());//挂账日期
			out.setPaydus(info.getUntius());//销账柜员
			out.setAcctno(info.getPaydac());//挂账账号
			out.setPayadt(tblPaya.getTrandt());
			out.setAcctbr(info.getPayabr());//挂账机构
			out.setTranus(tblPaya.getTranus());//挂账柜员
			out.setCavfam(info.getPayamn());
			out.setRemktx(info.getTemp01());//摘要
			out.setToacct(info.getToacct());//对方账号
			out.setToacna(info.getToacna());//对方户名
			paydInfo.add(out);
			
		}
	
		CommTools.getBaseRunEnvs().setTotal_count(list.getRecordCount());
		return paydInfo;
	}
	
	//
	private void dealCmbkData(String trandt){
		 String mtdate = CommTools.getBaseRunEnvs().getTrxn_date();
		 String timetm = DateTools2.getCurrentTimestamp();
		 bizlog.debug("套平入账垃圾数据清除-日终处理开始==========");
		 InTranOutDao.upDelKnsCmbk(trandt, timetm);//清除垃圾传票
		 InTranOutDao.upDelknsPaya(trandt, timetm);//清除垃圾挂账记录
		 InTranOutDao.upDelknsPayd(trandt, timetm);//清除垃圾销账记录		
		 bizlog.debug("==========套平入账垃圾数据清除-日终处理结束");
	 }

	@Override
	/**
	 * @author hetao
	 *         <p>
	 *         <li>2016-07-23 10：21</li>
	 *         <li>内部户转入转出(系统外)</li>
	 *         </p>
	 * @param input
	 * @param output
	 * */
	public void InacctTran(Input input, cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.InacctTran.Output output) {

		if(CommUtil.isNull(input.getCapitp())){
			throw InError.comm.E0003("转账交易类型为必输!");
		}
		if(CommUtil.isNull(input.getOtacno())){
			throw InError.comm.E0003("转出方账号必输!");
		}
		if(CommUtil.isNull(input.getOtacna())){
			throw InError.comm.E0003("转出方户名必输!");
		}
		if(CommUtil.isNull(input.getOtbrch())){
			throw InError.comm.E0003("转出方账户所属机构必输!");
		}
		if(CommUtil.isNull(input.getCrcycd())){
			throw InError.comm.E0003("币种必输!");
		}
		if(CommUtil.compare(input.getTranam(),BigDecimal.ZERO)<=0){
			throw InError.comm.E0003("交易金额必须大于零!");
		}
		if(CommUtil.isNull(input.getInacno())){
			throw InError.comm.E0003("转入方账号必输!");
		}		
		if(CommUtil.isNull(input.getInacna())){
			throw InError.comm.E0003("转入方户名必输!");
		}		
		
		if(CommUtil.isNull(input.getInbrch())){
			throw InError.comm.E0003("转入方机构必输!");
		}		
		if(CommUtil.isNull(input.getSmrycd())){
			throw InError.comm.E0003("摘要码必输!");
		}		
		String Clerbr = BusiTools.getBusiRunEnvs().getCentbr();//获取省联社清算中心
		
		    //TRINTP = '2' D 内部户； C 系统往来
		 if(input.getCapitp()==E_TRINTP.IN102){
			 
			 IoInacInfo inacInfo =SysUtil.getInstance(IoInQuery.class).InacInfoQuery(input.getOtacno());
			 if(null==inacInfo){
				 throw InError.comm.E0003("转出账号不存在!");
			 }
			 if(!CommUtil.equals(inacInfo.getBrchno(), input.getOtbrch())){
				 throw InError.comm.E0003("转出机构和账户所属机构不符!");
			 }
			 
			 if(!CommUtil.equals(inacInfo.getCrcycd(),input.getCrcycd())){
				 throw InError.comm.E0003("币种和账户所属币种不符!");
			 }
			 
				//调用内部户记账服务
				IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
				
				iaAcdrInfo.setTrantp(E_TRANTP.TR);
				iaAcdrInfo.setAcctno(input.getOtacno());
				iaAcdrInfo.setTranam(input.getTranam());//记账金额
				iaAcdrInfo.setCrcycd(input.getCrcycd());//币种
				iaAcdrInfo.setAcbrch(input.getInbrch());//账户机构
				iaAcdrInfo.setToacct(input.getInacno());
				iaAcdrInfo.setToacna(input.getInacna());
				iaAcdrInfo.setSmrycd(input.getSmrycd());
				iaAcdrInfo.setPayadetail(input.getIapayadetail());
				iaAcdrInfo.setPayddetail(input.getIapayddetail());
				//SysUtil.getInstance(IoInAccount.class).ioInAcdr(iaAcdrInfo);//内部户借方服务
				CommTools.getRemoteInstance(IoInAccount.class).ioInAcdr(iaAcdrInfo);//内部户借方服务
				//跨系统转内部账处理
				
				IaAcdrInfo iaAcdrInfo2 = SysUtil.getInstance(IaAcdrInfo.class);
				iaAcdrInfo2.setTrantp(E_TRANTP.TR);
				iaAcdrInfo2.setTranam(input.getTranam());
				iaAcdrInfo2.setAcbrch(Clerbr);
				iaAcdrInfo2.setCrcycd(input.getCrcycd());
				iaAcdrInfo2.setToacct(input.getOtacno());
				iaAcdrInfo2.setToacna(input.getOtacna());
				iaAcdrInfo2.setSmrycd(input.getSmrycd());
				
				//业务编码配在参数表中，通过查表来赋值
				KnpParameter para1 = SysUtil.getInstance(KnpParameter.class);
	        	para1 = KnpParameterDao.selectOne_odb1("IAVCCM", "busino", "%", "%",true);
				iaAcdrInfo2.setBusino(para1.getParm_value1()); //9930310402
				iaAcdrInfo2.setSubsac(para1.getParm_value2()); 
				//SysUtil.getInstance(IoInAccount.class).ioInAccr(iaAcdrInfo2);//内部户贷方服务
				CommTools.getRemoteInstance(IoInAccount.class).ioInAccr(iaAcdrInfo2);//内部户贷方服务
		 }else{
			 
			   //跨系统转内部账处理
				IaAcdrInfo iaAcdrInfo2 = SysUtil.getInstance(IaAcdrInfo.class);
				iaAcdrInfo2.setTrantp(E_TRANTP.TR);
				iaAcdrInfo2.setTranam(input.getTranam());
				iaAcdrInfo2.setAcbrch(Clerbr);
				iaAcdrInfo2.setCrcycd(input.getCrcycd());
				iaAcdrInfo2.setToacct(input.getInacno());
				iaAcdrInfo2.setToacna(input.getInacna());
				iaAcdrInfo2.setSmrycd(input.getSmrycd());
				
				//业务编码配在参数表中，通过查表来赋值
				KnpParameter para1 = SysUtil.getInstance(KnpParameter.class);
	        	para1 = KnpParameterDao.selectOne_odb1("IAVCCM", "busino", "%", "%",true);
				iaAcdrInfo2.setBusino(para1.getParm_value1()); //9930310402
				iaAcdrInfo2.setSubsac(para1.getParm_value2()); 
//				SysUtil.getInstance(IoInAccount.class).ioInAcdr(iaAcdrInfo2);//内部户借方服务
				CommTools.getRemoteInstance(IoInAccount.class).ioInAcdr(iaAcdrInfo2);//内部户借方服务
				 IoInacInfo inacInfo =SysUtil.getInstance(IoInQuery.class).InacInfoQuery(input.getInacno());
				 if(null==inacInfo){
					 throw InError.comm.E0003("转入账号不存在!");
				 }
				 if(!CommUtil.equals(inacInfo.getBrchno(), input.getInbrch())){
					 throw InError.comm.E0003("转出机构和账户所属机构不符!");
				 }
				 
				 if(!CommUtil.equals(inacInfo.getCrcycd(),input.getCrcycd())){
					 throw InError.comm.E0003("币种和账户所属币种不符!");
				 }				
				
				//调用内部户记账服务
				IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
				
				iaAcdrInfo.setTrantp(E_TRANTP.TR);
				iaAcdrInfo.setAcctno(input.getInacno());
				iaAcdrInfo.setTranam(input.getTranam());//记账金额
				iaAcdrInfo.setCrcycd(input.getCrcycd());//币种
				iaAcdrInfo.setAcbrch(input.getInbrch());//账户机构
				iaAcdrInfo.setToacct(input.getOtacno());
				iaAcdrInfo.setToacna(input.getOtacna());				
				iaAcdrInfo.setPayadetail(input.getIapayadetail());
				iaAcdrInfo.setPayddetail(input.getIapayddetail());	
				iaAcdrInfo.setSmrycd(input.getSmrycd());
				
//				SysUtil.getInstance(IoInAccount.class).ioInAccr(iaAcdrInfo);//内部户贷方服务
				CommTools.getRemoteInstance(IoInAccount.class).ioInAccr(iaAcdrInfo);//内部户贷方服务
					
		 }
		
	}
	
	//套平入账数据检查
//	private void iavccmChk(String acstno, String transq){
	public static void iavccmChk(String acstno, String transq){
		//因一般习惯用trandt，transq变量表示交易日期和交易流水，故处理中将支付平台日期和流水改用其它变量处理
		String platsq = transq; //支付平台流水
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller(); //交易柜员
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch(); //交易机构
		E_AUTHFG authfg = BusiTools.getBusiRunEnvs().getAuthvo().getAuthfg();

		if(authfg == E_AUTHFG.YES){
			throw InError.comm.E0003("该交易必须要授权才能通过！");
		}
		if(authfg == E_AUTHFG.NOTICE){
			throw InError.comm.E0003("该交易是否需要授权才能通过！");
		}
		if(CommUtil.isNull(acstno)){
			throw InError.comm.E0003("输入套号不能为空");
		}
		
		if(acstno.length() != 11){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		if(CommTools.rpxMatch("W[0-9]+", acstno) == 1){
			throw InError.comm.E0003("输入套号["+acstno+"]不合法，请核查！");
		}
		
		//支付平台输入判断
		if(CommUtil.isNotNull(platsq)){
			
			if(CommUtil.isNull(trandt)){
				throw InError.comm.E0003("支付平台流水和日期必须一起输入！");
			}
			
			if(CommUtil.isNotNull(KnsCmbkDao.selectAll_kns_cmbk_odx4(platsq, false))){
				throw InError.comm.E0003("支付流水["+platsq+"]重复，请核查！");
			}
		}
		bizlog.debug("套平入账检查开始==========");
		
		List<KnsCmbk> knsCmbkList = InacSqlsDao.selKnsCmbkUserByBusina(acstno, trandt, false);
       
		//List<KnsCmbk> knsCmbkList = KnsCmbkDao.selectAll_kns_cmbk_odx2(acstno, trandt, false);  
		
		if(CommUtil.isNull(knsCmbkList)){
			throw InError.comm.E0003("套号["+acstno+"]记录不存在，请核对！");
		}
		 
		BigDecimal tranC = new BigDecimal(0);	//贷方发生额
		BigDecimal tranD = new BigDecimal(0);	//借方发生额
		
		for(KnsCmbk knsCmbk : knsCmbkList){          
			
	    if(knsCmbk.getIavcst() == E_CMBK_TRANST._2){
					continue;
			}
		    //modify by wuzx 20161114 去掉机构校验 beg
/*			if(!CommUtil.equals(knsCmbk.getAcctbr(), tranbr)){
				throw InError.comm.E0003("非本机构的套号，不允许操作！");
			}
			*/
			if(knsCmbk.getIavcst() != E_CMBK_TRANST._3){
				throw InError.comm.E0003("套内传票状态不为已复核，无法入账，请检查！");
			}
			
			if(!CommUtil.equals(knsCmbk.getCkbsus(), tranus)){
				throw InError.comm.E0003("操作柜员必须为原复核柜员！");
			}
			
			if(knsCmbk.getIavcst() == E_CMBK_TRANST._1){
				throw InError.comm.E0003("套内传票已入账，请检查!");
			}
			
			if(knsCmbk.getIavcst() == E_CMBK_TRANST._9){
				throw InError.comm.E0003("套内传票已冲账，请检查!");
			}
			IoInacInfo ioInacInfo = SysUtil.getInstance(IoInSrvQryTableInfo.class).selKnaGlAcctnoByAcctno(knsCmbk.getOtacno(), false);
			
			//不为空时，表明此账号为系统内内部户
			if(CommUtil.isNotNull(ioInacInfo)){
				
				if(E_INACST.CLOSED.equals(ioInacInfo.getAcctst())){
					throw InError.comm.E0003("当前转出账户已销户！");
				}
				
				if(E_KPACFG._1.equals(ioInacInfo.getKpacfg())){
					throw InError.comm.E0003("当前转出账户不允许手工记账！");
				}
				
				if(!CommUtil.equals(tranbr, ioInacInfo.getBrchno())){
					throw InError.comm.E0003("跨法人机构无权进行该项业务！");
				}
			}
			//系统内内部户，检查状态
			if(knsCmbk.getCntsys() == E_CNTSYS._1){
				
				if(CommUtil.isNull(ioInacInfo)){
					throw InError.comm.E0003("未找到该内部户信息，请核查！");
				}
				
		        //客户账信息查询
		    	if(knsCmbk.getIavctp() == E_IAVCTP._2){
					
					cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStaPublic.IoDpCheckAcCarry.InputSetter inputSetter = SysUtil.getInstance(cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStaPublic.IoDpCheckAcCarry.InputSetter.class);
					
					inputSetter.setCardno(knsCmbk.getInacno());
					inputSetter.setToacct(knsCmbk.getOtacno());
					inputSetter.setTranam(knsCmbk.getTranam());
					inputSetter.setTrantp(E_CKTRTP.CUPCR);
					
					// add by wuzx -增加转出账户状态判断 beg
					IoCaKnaAcdc cplKnaAcdc = CommTools.getRemoteInstance(
							IoCaSevQryTableInfo.class)
							.getKnaAcdcByCardno(knsCmbk.getInacno(),
									false);//不带法人查询
					if (CommUtil.isNull(cplKnaAcdc)) {
						throw InError.comm.E0003("该转入账户["+knsCmbk.getInacno()+"]不存在！");
					}
					E_CUACST status = CommTools.getRemoteInstance(
							IoCaSrvGenEAccountInfo.class).selCaStInfo(
							cplKnaAcdc.getCustac());// 查询电子账户状态信息
					if (status == E_CUACST.PREOPEN || status == E_CUACST.CLOSED
							|| status == E_CUACST.DELETE
							|| status == E_CUACST.PRECLOS) {
						throw PbError.PbComm.E2015("该转入账户["+knsCmbk.getInacno()+"]为非正常状态["+status+"],不允许交易！");
					}
			        
		    	}
			}
			 
			//销账记录检查  
			if(knsCmbk.getPayatp() == E_PAYATP._2){
				
				List<KnsPayd> knsPaydDbList = InTranOutDao.selKnsPaydByAcst(acstno, trandt,knsCmbk.getOtacno(), true);
				for(KnsPayd knsPayd : knsPaydDbList){
					
				//	KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(knsPayd.getPayasq(), true);
					KnsPaya knsPaya = InTranOutDao.selKnsPayaByPayasq(knsPayd.getPayasq(), true);//改为不带法人查询
					if(!CommUtil.equals(knsPaya.getPayaac(), knsCmbk.getOtacno())){
						throw InError.comm.E0003("销账对应挂账明细的挂账账号，应为当前转出账号");
					}
					
					if(CommUtil.compare(knsPayd.getPayamn(), knsPaya.getRsdlmn()) > 0){
						throw InError.comm.E0003("销账金额大于挂账金额");
					}
					
				}
			}
			
			//系统内
			//借贷平衡验证
			if(knsCmbk.getIavctp() == E_IAVCTP._1){
				
				//内部户转内部户验证
				if(knsCmbk.getOtamcd() ==E_AMNTCD.CR){
					tranC = tranC.add(knsCmbk.getTranam());
				}
				
				if(knsCmbk.getOtamcd() ==E_AMNTCD.DR){
					tranD = tranD.add(knsCmbk.getTranam());
				}
				
			}else{
				 // 内部户转客户账借贷，表外支付无须验证
				tranC = knsCmbk.getTranam();
				tranD = knsCmbk.getTranam();
			}
			
		}
		 
		bizlog.debug("贷方发生额为:" + tranC + ", 借方发生额为：" + tranD);
		
		if(!CommUtil.equals(tranC, tranD)){
			throw InError.comm.E0003("该套票借贷不平衡，请核查！");
		}
		
		//复核柜员额度验证
		//TODO 复核柜员额度验证
		/*IoBrchUserQt ioBrchUserQt = SysUtil.getInstance(IoBrchUserQt.class);
		ioBrchUserQt.setBrchno(tranbr);
		ioBrchUserQt.setBusitp(E_BUSITP.TR);
		ioBrchUserQt.setCrcycd(knsCmbkList.get(0).getCrcycd());
		ioBrchUserQt.setTranam(tranC);
		ioBrchUserQt.setUserid(tranus);
		SysUtil.getInstance(IoBrchSvcType.class).selBrchUserQt(ioBrchUserQt);*/
	}
	
	//系统内内部户记账
	private void insysInaccAcc(KnsCmbk knsCmbk, String acstno, String trandt){
		//转出账号是内部账号，先记一笔内部账
		IaAcdrInfo iaAcdrInfo = SysUtil.getInstance(IaAcdrInfo.class);
		iaAcdrInfo.setTrantp(E_TRANTP.TR);
		iaAcdrInfo.setAcctno(knsCmbk.getOtacno());
		iaAcdrInfo.setAcctna(knsCmbk.getOtacna());
		iaAcdrInfo.setTranam(knsCmbk.getTranam());//记账金额
		/*if(knsCmbk.getIavctp() == E_IAVCTP._2){//内转客
			iaAcdrInfo.setToacct(knsCmbk.getInacno());//转入账号为对方账号
			iaAcdrInfo.setToacna(knsCmbk.getInacna());//转入户名为对方户名
		}*/
		//转内部账也登记对方账号
		iaAcdrInfo.setToacct(knsCmbk.getInacno());//转入账号为对方账号
		iaAcdrInfo.setToacna(knsCmbk.getInacna());//转入户名为对方户名
		if(CommUtil.isNotNull(knsCmbk.getIntype())){//跨法人记账
			iaAcdrInfo.setToacct(knsCmbk.getInacno());//转入账号为对方账号
			iaAcdrInfo.setToacna(knsCmbk.getInacna());//转入户名为对方户名
		}
		iaAcdrInfo.setCrcycd(knsCmbk.getCrcycd());//币种
		iaAcdrInfo.setAcbrch(knsCmbk.getAcctbr());//账户机构
		iaAcdrInfo.setDscrtx(knsCmbk.getSmrytx());//摘要
		if (knsCmbk.getPayatp() == E_PAYATP._1) {

			Options<IaPayaDetail> iaPayaDetailOption = new DefaultOptions<IaPayaDetail>();
			
			/*List<KnsPaya> knsPayaList = KnsPayaDao.selectAll_kns_paya_odx5(
					acstno, knsCmbk.getPayseq(), trandt, true);*/
			List<KnsPaya> knsPayaList = InTranOutDao.selKnsPayaByAcstnoAndPayaseq(acstno, knsCmbk.getPayseq(), trandt, true);
			for (KnsPaya knsPaya : knsPayaList) {

				if (knsPaya.getPayast() == E_PAYAST.ZF) {
					continue;
				}

				IaPayaDetail iaPayaDetail = SysUtil.getInstance(IaPayaDetail.class);
				iaPayaDetail.setPayaac(knsPaya.getPayaac());
				iaPayaDetail.setPayabr(knsPaya.getPayabr());
				iaPayaDetail.setPayamn(knsPaya.getPayamn());
				iaPayaDetail.setPayasq(knsPaya.getPayasq());
				iaPayaDetail.setToacna(knsPaya.getToacna());
				iaPayaDetail.setToacno(knsPaya.getToacct());
				iaPayaDetailOption.add(iaPayaDetail);

			}

			iaAcdrInfo.setPayadetail(iaPayaDetailOption);

		}
		
		if(knsCmbk.getPayatp() == E_PAYATP._2){
			
			Options<IaPaydDetail> iaPaydDetailOption = new DefaultOptions<IaPaydDetail>();
			//
			List<KnsPayd> knsPaydList  = InTranOutDao.selKnsPaydByAcstnoAndPayseq(acstno, trandt, knsCmbk.getPayseq(), true);
			//List<KnsPayd> knsPaydList1 = KnsPaydDao.selectAll_kns_payd_odx5(acstno, knsCmbk.getPayseq(), trandt, true);
			for(KnsPayd knsPayd : knsPaydList){
				
				if(knsPayd.getPaydst() == E_PAYDST.ZF){
					continue;
				}
				
				IaPaydDetail iaPaydDetail = SysUtil.getInstance(IaPaydDetail.class);
				iaPaydDetail.setPaydsq(knsPayd.getPaydsq());
				iaPaydDetail.setPaydmn(knsPayd.getPayamn());
				iaPaydDetail.setPrpysq(knsPayd.getPayasq());
				iaPaydDetail.setPaydac(knsPayd.getPaydac());
				iaPaydDetail.setRsdlmn(knsPayd.getRsdlmn());
				iaPaydDetail.setTotlmn(knsPayd.getTotlmn());
				iaPaydDetailOption.add(iaPaydDetail);
				
			}
			
			iaAcdrInfo.setPayddetail(iaPaydDetailOption);
		}
		
		//记账方向
		E_AMNTCD amntcd = knsCmbk.getOtamcd();
		//add by wuzx 表外收付记账时，需改为借贷
		if(knsCmbk.getIavctp()==E_IAVCTP._3){//表外收付
			if(amntcd == E_AMNTCD.DR){
				amntcd = E_AMNTCD.RV;
			}else if(amntcd == E_AMNTCD.CR){
				amntcd = E_AMNTCD.PY;
			}		
		}
		//add by wuzx 表外收付记账时，需改为借贷
		//调用内部户记账服务
		IoInAccount ioInAcctount = SysUtil.getInstance(IoInAccount.class);
		switch (amntcd){
			case DR:
				ioInAcctount.ioInAcdr(iaAcdrInfo);//内部户借方服务				
				break;
			case CR:
				ioInAcctount.ioInAccr(iaAcdrInfo);//内部户贷方服务
				break;
			case RV:
				ioInAcctount.ioInAcrv(iaAcdrInfo);//内部户收方服务
				break;
			case PY:
				ioInAcctount.ioInAcpv(iaAcdrInfo);//内部户收方服务
				break;
			default:
				throw InError.comm.E0003("记账方向:"+amntcd.getValue()+"["+amntcd.getLongName()+"]不支持");
				
		}
	}
	
	//系统内客户账记账
	private void insysCustaccAcc(KnsCmbk knsCmbk){
		
		//通过客户卡号获取电子账号
		IoCaKnaAcdc ioCaKnaAcdc =  CommTools.getRemoteInstance(IoCaSevQryTableInfo.class).getKnaAcdcOdb2(knsCmbk.getInacno(), true);
		if(ioCaKnaAcdc==null){
			throw InError.comm.E0003("账号不存在！");
		}
		String custac = ioCaKnaAcdc.getCustac();
		IoDpKnaAcct ioDpKnaAcct= CommTools.getRemoteInstance(DpAcctSvcType.class).getSettKnaAcctAc(custac);
		E_CUACST status = CommTools.getRemoteInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);//查询电子账户状态信息
		//客户账记账输入类字段赋值
		if(CommUtil.isNull(knsCmbk.getInamcd())){
			
			throw InError.comm.E0003("该笔客户账对应借贷标志为空，请核查");
		}
		
		SaveDpAcctIn saveDpAcctIn = SysUtil.getInstance(SaveDpAcctIn.class);
		saveDpAcctIn.setAcctno(ioDpKnaAcct.getAcctno());
		saveDpAcctIn.setCustac(custac);
		saveDpAcctIn.setCardno(knsCmbk.getInacno());
		saveDpAcctIn.setOpacna(knsCmbk.getOtacna());
		saveDpAcctIn.setToacct(knsCmbk.getOtacno());
		saveDpAcctIn.setTranam(knsCmbk.getTranam());
		saveDpAcctIn.setCrcycd(knsCmbk.getCrcycd());
		saveDpAcctIn.setSmrycd(BusinessConstants.SUMMARY_ZR);
		saveDpAcctIn.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_ZR));
		saveDpAcctIn.setRemark(knsCmbk.getSmrytx());
		//saveDpAcctIn.setSmryds(knsCmbk.getSmrytx());//摘要码
		//SysUtil.getInstance(DpAcctSvcType.class).addPostAcctDp(saveDpAcctIn);
		//modify by sh 20170802 支持外调服务处理
		CommTools.getRemoteInstance(DpAcctSvcType.class).addPostAcctDp(saveDpAcctIn);
		
/*		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String trantm = BusiTools.getBusiRunEnvs().getTrantm();
		IoCaSevQryTableInfo caqry = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaCust cust = caqry.getKnaCustByCustacOdb1(custac, true);
		IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(IoSrvCfPerson.class).getCifCustAccsByCustno(cust.getCustno(), E_STATUS.NORMAL);
		//消息推送至APP客户端
		MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
		mri.setMtopic("Q0101005");
		//mri.setTdcnno("R00");  //测试指定DCN
		ToAppSendMsg toAppSendMsg = CommTools
				.getInstance(ToAppSendMsg.class);
		
		// 消息内容
		toAppSendMsg.setUserId(cplCifCustAccs.getCustid()); //用户ID
		toAppSendMsg.setOutNoticeId("Q0101005"); //外部消息ID
		toAppSendMsg.setNoticeTitle("资金变动"); //公告标题
		toAppSendMsg.setContent("交易时间："+trandt.substring(0, 4)+"年"+trandt.substring(4, 6)
				+"月"+trandt.substring(4,6)+" "+trantm.substring(0, 2)+":"+trantm.substring(3, 5)
				+"\r交易类型：转入 \r收入金额："+knsCmbk.getTranam() +"\r请点击查看详情。"); //公告内容
		toAppSendMsg.setSendtime(CommTools.getBaseRunEnvs().getTrxn_date()+BusiTools.getBusiRunEnvs().getTrantm()); //消息生成时间
		toAppSendMsg.setTransType(E_APPTTP.RECHARGE); //交易类型
		toAppSendMsg.setTirggerSys(SysUtil.getSystemId()); //触发系统
		toAppSendMsg.setClickType(E_CLIKTP.NO);   //点击动作类型
		//toAppSendMsg.setClickValue(clickValue); //点击动作值
		
		mri.setMsgtyp("ApSmsType.ToAppSendMsg");
		mri.setMsgobj(toAppSendMsg); 
		AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
*/		//客户化休眠处理
		//SysUtil.getInstance(DpAcctSvcType.class).dealAcctStatAndSett(ioDpKnaAcct, status);
		CommTools.getRemoteInstance(DpAcctSvcType.class).dealAcctStatAndSett(ioDpKnaAcct, status);
		
	}
	
	//跨系统客户账记账
	private void crosysCustaccAcc(KnsCmbk knsCmbk, String acstno, String trandt){
		
		// 跨系统转客户账处理
		IaAcdrInfo iaAcdrInfo2 = SysUtil.getInstance(IaAcdrInfo.class);
		iaAcdrInfo2.setTrantp(E_TRANTP.TR);
		iaAcdrInfo2.setTranam(knsCmbk.getTranam());
		//iaAcdrInfo2.setAcbrch(knsCmbk.getAcctbr());
		IoBrchInfo ioBrchInfo  = SysUtil.getInstance(IoSrvPbBranch.class)
				.getCenterBranch(BusiTools.getBusiRunEnvs().getCentbr());//获取清算中心
		iaAcdrInfo2.setAcbrch(ioBrchInfo.getBrchno());//送总行机构
		iaAcdrInfo2.setCrcycd(knsCmbk.getCrcycd());
		iaAcdrInfo2.setToacct(knsCmbk.getOtacno());
		iaAcdrInfo2.setToacna(knsCmbk.getOtacna());
		iaAcdrInfo2.setDscrtx(knsCmbk.getSmrytx());//摘要
		//业务编码配在参数表中，通过查表来赋值
		
		KnpParameter para1 = SysUtil.getInstance(KnpParameter.class);
    	para1 = KnpParameterDao.selectOne_odb1("IAVCCM", "busino", "%", "%",true);
    	//add by wuzx 增加转入账号判断是信用卡储蓄卡 20161212
    	if(knsCmbk.getInacno().length()==16){
    		iaAcdrInfo2.setBusino(para1.getParm_value3()); //9930310401
    		iaAcdrInfo2.setSubsac(para1.getParm_value4());
		}else{
			iaAcdrInfo2.setBusino(para1.getParm_value1()); //9930310401
			iaAcdrInfo2.setSubsac(para1.getParm_value2());
		}		
		E_AMNTCD amntcd2 = knsCmbk.getInamcd();
		
		//调用内部户记账服务
		//IoInAccount ioInAcctount2 = SysUtil.getInstance(IoInAccount.class);
		IoInAccount ioInAcctount2 = CommTools.getRemoteInstance(IoInAccount.class);
		switch (amntcd2){
			case DR:
				ioInAcctount2.ioInAcdr(iaAcdrInfo2);//内部户借方服务
				break;
			case CR:
				ioInAcctount2.ioInAccr(iaAcdrInfo2);//内部户贷方服务
				break;
			default:
				throw InError.comm.E0003("记账方向:"+amntcd2.getValue()+"["+amntcd2.getLongName()+"]不支持");
		}
		
	}
	
	//跨系统内部账记账
	private void crosysInaccAcc(KnsCmbk knsCmbk, String acstno, String trandt){
	
		//跨系统转内部账处理
		IaAcdrInfo iaAcdrInfo2 = SysUtil.getInstance(IaAcdrInfo.class);
		iaAcdrInfo2.setTrantp(E_TRANTP.TR);
		iaAcdrInfo2.setTranam(knsCmbk.getTranam());
		//iaAcdrInfo2.setAcbrch(knsCmbk.getAcctbr());
		IoBrchInfo ioBrchInfo  = SysUtil.getInstance(IoSrvPbBranch.class).
				getCenterBranch(BusiTools.getBusiRunEnvs().getCentbr());//获取清算中心
		iaAcdrInfo2.setAcbrch(ioBrchInfo.getBrchno());//送总行机构
		iaAcdrInfo2.setCrcycd(knsCmbk.getCrcycd());
		iaAcdrInfo2.setToacct(knsCmbk.getOtacno());
		iaAcdrInfo2.setToacna(knsCmbk.getOtacna());
		iaAcdrInfo2.setDscrtx(knsCmbk.getSmrytx());
		
		//业务编码配在参数表中，通过查表来赋值
		KnpParameter para1 = SysUtil.getInstance(KnpParameter.class);
    	para1 = KnpParameterDao.selectOne_odb1("IAVCCM", "busino", "%", "%",true);
		iaAcdrInfo2.setBusino(para1.getParm_value1()); //9930310401
		iaAcdrInfo2.setSubsac(para1.getParm_value2());
		E_AMNTCD amntcd2 = knsCmbk.getOtamcd();//modify by wuzx
		
		//调用内部户记账服务
//		IoInAccount ioInAcctount2 = SysUtil.getInstance(IoInAccount.class);
		IoInAccount ioInAcctount2 = CommTools.getRemoteInstance(IoInAccount.class);
		switch (amntcd2){
			case DR:
				ioInAcctount2.ioInAcdr(iaAcdrInfo2);//内部户借方服务
				break;
			case CR:
				ioInAcctount2.ioInAccr(iaAcdrInfo2);//内部户贷方服务
				break;
			default:
				throw InError.comm.E0003("记账方向:"+amntcd2.getValue()+"["+amntcd2.getLongName()+"]不支持");
		}
		
		//跨系统转内部账，需在本处更改挂销账标志，且冲账时也需更改
		if(knsCmbk.getPayatp() == E_PAYATP._1){
			List<KnsPaya> knsPayaList = KnsPayaDao.selectAll_kns_paya_odx5(acstno, knsCmbk.getPayseq(), trandt, true);
			
			for(KnsPaya knsPaya : knsPayaList){
				//作废记录过滤
				if(knsPaya.getPayast() == E_PAYAST.ZF){
					continue;
				}
				
				knsPaya.setPayast(E_PAYAST.ZC);
				KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
			}
		}
		
		if(knsCmbk.getPayatp() == E_PAYATP._2){
			List<KnsPayd> knsPaydList = KnsPaydDao.selectAll_kns_payd_odx5(acstno, knsCmbk.getPayseq(), trandt, true);
			
			for(KnsPayd knsPayd : knsPaydList){
				//作废记录过滤
				if(knsPayd.getPaydst() == E_PAYDST.ZF){
					continue;
				}
				
				KnsPaya paya = KnsPayaDao.selectOne_kns_paya_odx1(knsPayd.getPayasq(), true);
				if(CommUtil.compare(knsPayd.getPayamn(), paya.getRsdlmn())>0){
					throw InError.comm.E0003("剩余挂账金额["+knsPayd.getPayamn()+"]小于本次销账金额["+paya.getRsdlmn()+"]！");
				}
				
				if(CommUtil.compare(knsPayd.getPayamn(), paya.getRsdlmn())==0){
					paya.setPayast(E_PAYAST.JQ);
					
				}else if (CommUtil.compare(paya.getRsdlmn(), knsPayd.getPayamn())<0){
					throw InError.comm.E0003("挂账序号["+paya.getPayasq()+"]下剩余挂账金额["+paya.getRsdlmn()+"]小于销账序号["+knsPayd.getPayasq()+"]下销账金额"
							+knsPayd.getPayamn()+"],请检查！");
					
				}
				paya.setRsdlmn(paya.getRsdlmn().subtract(knsPayd.getPayamn()));

				if(!CommUtil.equals(paya.getPayaac(), knsCmbk.getOtacno())){
					throw InError.comm.E0003("挂账账号与交易账号不符，请检查！");
					
				}
				
				paya.setPaydnm(paya.getPaydnm()+1);
				KnsPayaDao.updateOne_kns_paya_odx1(paya);//更新挂账登记簿记录
				
				knsPayd.setPaydst(E_PAYDST.ZC);
				KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);//更新销账登记簿记录
			}
		}
	}

	/**
	 * 
	 * @param acstno
	 * @param output
	 * 2016年12月8日-下午7:24:04
	 * @auther chenjk
	 */
	@Override
	public void selIavcqr(
			String acstno,
			cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.SelIavcqr.Output output) {
		bizlog.debug("==========跨机构转账查询处理开始==========");
		
		if(CommUtil.isNull(acstno)){
			throw InError.comm.E0003("输入套号不能为空");
		}
		
		if(acstno.length() != 11){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		if(CommTools.rpxMatch("W[0-9]+", acstno) == 1){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		
		bizlog.debug("传票信息处理开始==========");
		List<KnsCmbk> knsCmbkList = InTranOutDao.selKnsCmbkByAcst(acstno, trandt, false);
		//跨法人只能查询到跨法人的套号
		//List<KnsCmbk> knsCmbkList = InTranOutDao.selKnsCmbk2ByAcstno(acstno, trandt, false);
		//add by wuzx 20160910 beg
		if(null==knsCmbkList||knsCmbkList.size()==0){
			throw InError.comm.E0003("该套号对应的记录不存在！");
		}
		//add by wuzx 20160910 end 
		/*if(knsCmbkList.size() != 2){
			throw InError.comm.E0003("检索出传票记录数量有误，请核查！");
		}
		*/
		//输出数据处理
		for(KnsCmbk knsCmbk : knsCmbkList){  
		
			/*if(!CommUtil.equals(knsCmbk.getAcctbr(), tranbr)){
				throw InError.comm.E0003("该套号非本机构的套号，请核查！");
			}*/
			
			if(knsCmbk.getIavcst() ==  E_CMBK_TRANST._2){
				throw InError.comm.E0003("该套号["+knsCmbk.getAcstno()+"]已删除，请核查！");
			}
			
			//传票类型验证
			/*if(CommUtil.isNotNull(knsCmbk.getIavctp())){
				
				if(knsCmbk.getIavctp() == E_IAVCTP._2){
					throw InError.comm.E0003("所查传票类型为内部户转客户账，请核查");
				}
			}
			*/
			output.setAcstno(acstno);
			output.setSmrytx(knsCmbk.getSmrytx());
			output.setIavcst(knsCmbk.getIavcst());//返回传票状态
			output.setTranam(knsCmbk.getTranam());
			output.setAcctbr(knsCmbk.getAcctbr());
			output.setCsextg(knsCmbk.getCsextg());
			output.setIavctp(knsCmbk.getIavctp());
			output.setCntsys(knsCmbk.getCntsys());
			output.setIntype(knsCmbk.getIntype());//跨法人标志
			if(knsCmbk.getPayseq() == 1){
				bizlog.debug("转出账号传票信息填充开始==========");
				output.setOtacna(knsCmbk.getOtacna());
				output.setOtacno(knsCmbk.getOtacno());
				output.setOtamcd(knsCmbk.getOtamcd());
				output.setOtcrcd(knsCmbk.getCrcycd());
				output.setOtpatp(knsCmbk.getPayatp());
				/*output.setInacna(knsCmbk.getInacna());
				output.setInacno(knsCmbk.getInacno());
				output.setInamcd(knsCmbk.getInamcd());*/
				output.setIntype(knsCmbk.getIntype());
				bizlog.debug("==========转出账号传票信息填充结束");
				
				//如果传票类型是挂账
				if(knsCmbk.getPayatp() == E_PAYATP._1){
					
					bizlog.debug("转出账号挂账信息填充开始==========");
					List<KnsPaya> knsPayaList = InTranOutDao.selKnsPayaByAcst(acstno, trandt,knsCmbk.getOtacno(), true);  //sql中自动过滤已作废记录
						
					Options<PayaFirst> payaFirstOptions = new DefaultOptions<PayaFirst>();
					for (KnsPaya knsPaya : knsPayaList) {
						
						PayaFirst payafirst = SysUtil.getInstance(PayaFirst.class);
						payafirst.setPayasq(knsPaya.getPayasq());
						payafirst.setPayamn(knsPaya.getPayamn());
						payafirst.setPayaac(knsPaya.getPayaac());
						payafirst.setPayabr(knsPaya.getPayabr());
						payafirst.setToacna(knsPaya.getToacna());
						payafirst.setToacno(knsPaya.getToacct());
						
						payaFirstOptions.add(payafirst);
						
					}
					output.setPayaListInfoFirst(payaFirstOptions);
					bizlog.debug("==========转出账号挂账信息填充结束");
				}
			
				//如果传票类型是销账
				if(knsCmbk.getPayatp() == E_PAYATP._2){
					
					bizlog.debug("销账信息填充开始==========");
					List<KnsPayd> knsPaydList = InTranOutDao.selKnsPaydByAcst(acstno, trandt, knsCmbk.getOtacno(),true); //sql中自动过滤已作废记录
						
					Options<PaydFirst> paydfirstOptions = new DefaultOptions<PaydFirst>();
					for (KnsPayd knsPayd : knsPaydList) {
						
						PaydFirst paydFirst = SysUtil.getInstance(PaydFirst.class);
						paydFirst.setPaydsq(knsPayd.getPaydsq());
						paydFirst.setCharsq(knsPayd.getPayasq());
						paydFirst.setTotlmn(knsPayd.getTotlmn());
						paydFirst.setCharam(knsPayd.getPayamn());
						paydFirst.setPaydac(knsPayd.getPaydac());
						paydFirst.setBalanc(knsPayd.getRsdlmn());
						
						paydfirstOptions.add(paydFirst);
						
					}
					
					output.setPaydListInfoFirst(paydfirstOptions);
					bizlog.debug("==========转出账号销账信息填充结束");
						
				}
				bizlog.debug("==========转出账号信息处理结束");
		
			}else{
				
				bizlog.debug("转入账号传票信息填充开始==========");
				output.setInacna(knsCmbk.getOtacna());
				output.setInacno(knsCmbk.getOtacno());
				//output.setInamcd(knsCmbk.getInamcd());
				output.setInamcd(knsCmbk.getOtamcd());
				output.setInpatp(knsCmbk.getPayatp());
				output.setIncrcd(knsCmbk.getCrcycd());
				output.setIntype(knsCmbk.getIntype());
				bizlog.debug("==========转入账号传票信息填充结束");
				
				//如果传票类型是挂账
				if(knsCmbk.getPayatp() == E_PAYATP._1){
					
					bizlog.debug("转入账号挂账信息填充开始==========");
					List<KnsPaya> knsPayaList = InTranOutDao.selKnsPayaByAcst(acstno, trandt,knsCmbk.getOtacno(), true);  //sql中自动过滤已作废记录
						
					Options<PayaSecond> payaSecondOptions = new DefaultOptions<PayaSecond>();
					for (KnsPaya knsPaya : knsPayaList) {
						
						PayaSecond payaSecond = SysUtil.getInstance(PayaSecond.class);
						payaSecond.setPayase(knsPaya.getPayasq());
						payaSecond.setPayamt(knsPaya.getPayamn());
						payaSecond.setPayaae(knsPaya.getPayaac());
						payaSecond.setPayabh(knsPaya.getPayabr());
						payaSecond.setToacnm(knsPaya.getToacna());
						payaSecond.setToacnb(knsPaya.getToacct());
						
						payaSecondOptions.add(payaSecond);
						
					}
					output.setPayaListInfoSecond(payaSecondOptions);
					bizlog.debug("==========转入账号挂账信息填充结束");
			}
				//如果传票类型是销账
				if(knsCmbk.getPayatp() == E_PAYATP._2){
					
					bizlog.debug("转入账号销账信息填充开始==========");
					List<KnsPayd> knsPaydList = InTranOutDao.selKnsPaydByAcst(acstno, trandt, knsCmbk.getOtacno(),true); //sql中自动过滤已作废记录
						
					Options<PaydSecond> paydDetailoptions = new DefaultOptions<PaydSecond>();
					for (KnsPayd knsPayd : knsPaydList) {
						
						PaydSecond paydSecond = SysUtil.getInstance(PaydSecond.class);
						paydSecond.setPaydse(knsPayd.getPaydsq());
						paydSecond.setCharse(knsPayd.getPayasq());
						paydSecond.setTotlmt(knsPayd.getTotlmn());
						paydSecond.setCharan(knsPayd.getPayamn());
						paydSecond.setPaydae(knsPayd.getPaydac());
						paydSecond.setBalane(knsPayd.getRsdlmn());
						
						paydDetailoptions.add(paydSecond);
						
					}
					output.setPaydListInfoSecond(paydDetailoptions);
					bizlog.debug("==========转入账号销账信息填充结束");
						
				}
				
			}

		
		}
		bizlog.debug("==========内部账跨机构转账查询处理成功==========");
		
	}
    /**
     * 内转内跨法人传票录入
     * @author wuzhixiang
     * 12,Dec
     */
	@Override
	public void insIavcbl(
			cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.InsIavcbl.Input input,
			cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.InsIavcbl.Output output) {								
		bizlog.debug("==========内部账转內部账跨法人录入处理开始==========");
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); // 交易日期
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq(); // 交易流水
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller(); // 交易柜员
		String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();//原始法人号
		GlKnaAcct glKnaInAcct = InQuerySqlsDao.sel_GlKnaAcct_by_acct(input.getInacno(), true);
		// 转出账号传票录入登记簿录入
		bizlog.debug("传票录入登记簿录入开始==========");
		List<KnsCmbk> knsCmbkList = InTranOutDao.selKnsCmbkByAcst(input.getAcstno(), CommTools.getBaseRunEnvs().getTrxn_date(), false);
		if(knsCmbkList.size()>0){	
			throw InError.comm.E0003("当日已存在一套转入套号和转出套号，该套号不允许再次使用！");
		}
		//转出账号
		KnsCmbk knsCmbk = SysUtil.getInstance(KnsCmbk.class);
		int payseq = InTranOutDao.queryMaxCmbkSeq(input.getAcstno(), trandt,true);
		knsCmbk.setAcctbr(input.getAcctbr());// 记账机构
		knsCmbk.setOtacna(input.getOtacna());// 转出户名
		knsCmbk.setOtacno(input.getOtacno());// 转出账号
		knsCmbk.setInacno(input.getInacno());//对方账号
		knsCmbk.setInacna(input.getInacna());//对方户名
		knsCmbk.setAcstno(input.getAcstno());// 套号
		knsCmbk.setOtamcd(input.getOtamcd());// 转出借贷标志
		knsCmbk.setIavctp(E_IAVCTP._1);// 内转内
		knsCmbk.setIntype(input.getIntype());// 转入类型
        knsCmbk.setCrcycd(input.getOtcrcd());//币种
		knsCmbk.setSmrytx(input.getSmrytx());// 摘要
		knsCmbk.setPayatp(input.getOtpatp());
		knsCmbk.setPayseq(payseq); // 内部户转内部账跨法人一套传票中只有一条
		knsCmbk.setTranam(input.getTranam());
		knsCmbk.setCntsys(E_CNTSYS._1); // 系统内
		knsCmbk.setIavcst(E_CMBK_TRANST._0); // 传票状态:0 - 录入
		knsCmbk.setTrandt(trandt);
		knsCmbk.setTransq(transq);
		knsCmbk.setTranus(tranus);

		KnsCmbkDao.insert(knsCmbk);
		bizlog.debug("==========传票录入登记簿录入结束");
		//转入账号传票登记簿录入
		KnsCmbk knsCmbk2 = SysUtil.getInstance(KnsCmbk.class);
		int payseq2 = InTranOutDao.queryMaxCmbkSeq(input.getAcstno(), trandt,true);
		knsCmbk2.setAcctbr(input.getAcctbr());// 记账机构
		knsCmbk2.setOtacna(input.getInacna());// 转出户名
		knsCmbk2.setOtacno(input.getInacno());// 转出账号
		knsCmbk2.setInacno(input.getOtacno());//对方账号
		knsCmbk2.setInacna(input.getOtacna());//对方户名
		knsCmbk2.setAcstno(input.getAcstno());// 套号
		knsCmbk2.setOtamcd(input.getInamcd());// 转入借贷标志
		//knsCmbk2.setInamcd(input.getInamcd());// 转入借贷标志
		knsCmbk2.setIavctp(E_IAVCTP._1);// 内转内
		knsCmbk2.setIntype(input.getIntype());// 转入类型
		knsCmbk2.setCrcycd(input.getIncrcd());//币种
		knsCmbk2.setSmrytx(input.getSmrytx());// 摘要
		knsCmbk2.setPayatp(input.getInpatp());//挂销账标志
		knsCmbk2.setPayseq(payseq2); // 内部户转内部账跨法人一套传票中只有一条
		knsCmbk2.setTranam(input.getTranam());
		knsCmbk2.setCntsys(E_CNTSYS._1); // 系统内
		knsCmbk2.setIavcst(E_CMBK_TRANST._0); // 传票状态:0 - 录入
		knsCmbk2.setTrandt(trandt);
		knsCmbk2.setTransq(transq);
		knsCmbk2.setTranus(tranus);
//		CommTools.getBaseRunEnvs().setBusi_org_id(glKnaInAcct.getCorpno());//转入法人号
		KnsCmbkDao.insert(knsCmbk2);
//		CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);//原始法人号
		// 转出挂账登记簿1录入
		bizlog.debug("==========转出账号挂账录入登记簿录入开始");
		if (input.getOtpatp() == E_PAYATP._1) {
			for (PayaFirst payaFrist : input.getPayaListInfoFirst()) {
				KnsPaya knsPaya = SysUtil.getInstance(KnsPaya.class);
				String payasq= MsSeqUtil.genSeq("PAYASQ", CommTools.getBaseRunEnvs().getTrxn_date());
				knsPaya.setPayasq(payasq);
				knsPaya.setAcstno(input.getAcstno());
				knsPaya.setPayseq(payseq); // 内部内跨机构一套传票中只有一条
				knsPaya.setToacct(payaFrist.getToacno());
				knsPaya.setToacna(payaFrist.getToacna());
				knsPaya.setPayabr(payaFrist.getPayabr());
				knsPaya.setPayamn(payaFrist.getPayamn());
				knsPaya.setRsdlmn(payaFrist.getPayamn());
				knsPaya.setPayaac(input.getOtacno());
				knsPaya.setPayast(E_PAYAST.WFH);
				knsPaya.setCrcycd(input.getOtcrcd());
				knsPaya.setTrandt(trandt);
				knsPaya.setTransq(transq);
				knsPaya.setTranus(tranus);
				knsPaya.setTemp01(input.getSmrytx());// 摘要

				KnsPayaDao.insert(knsPaya);
				
			}
			bizlog.debug("==========转出账号挂账录入登记簿录入结束");
		}
		
		// 转入账号挂账登记簿录入
		if (input.getInpatp() == E_PAYATP._1) {
			bizlog.debug("==========转入账号挂账录入登记簿录入开始");
			for (PayaSecond payaSecond : input.getPayaListInfoSecond()) {
				KnsPaya knsPaya = SysUtil.getInstance(KnsPaya.class);
				String payasq = MsSeqUtil.genSeq("PAYASQ", CommTools.getBaseRunEnvs().getTrxn_date());
				knsPaya.setPayasq(payasq);
				knsPaya.setAcstno(input.getAcstno());
				knsPaya.setPayseq(payseq2); // 内部内跨机构一套传票中只有一条
				knsPaya.setToacct(payaSecond.getToacnb());
				knsPaya.setToacna(payaSecond.getToacnm());
				knsPaya.setPayabr(payaSecond.getPayabh());
				knsPaya.setPayamn(payaSecond.getPayamt());
				knsPaya.setRsdlmn(payaSecond.getPayamt());
				knsPaya.setPayaac(input.getInacno());// 转入挂账账号
				knsPaya.setPayast(E_PAYAST.WFH);
				knsPaya.setCrcycd(input.getOtcrcd());
				knsPaya.setTrandt(trandt);
				knsPaya.setTransq(transq);
				knsPaya.setTranus(tranus);
				knsPaya.setTemp01(input.getSmrytx());// 摘要
//				CommTools.getBaseRunEnvs().setBusi_org_id(glKnaInAcct.getCorpno());//转入法人号
				KnsPayaDao.insert(knsPaya);
//				CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);//原始法人号
			}
			bizlog.debug("==========转入账号挂账录入登记簿录入结束");
		}
        //转出账号销账录入登记簿录入
		if (input.getOtpatp() == E_PAYATP._2) {
			bizlog.debug("==========转出账号销账录入登记簿录入开始");
			for (PaydFirst paydFirst : input.getPaydListInfoFirst()) {

				KnsPayd knsPayd = SysUtil.getInstance(KnsPayd.class);

		
				
				// 通过挂账流水匹配到对应挂账记录
				KnsPaya knsPaya = InTranOutDao.selKnsPayaByPayasq(paydFirst.getCharsq(), true);
				//KnsPaya knsPaya = InTranOutDao.selKnsPayaByAcstnoAndPayasq(input.getAcstno(), paydFirst.getCharsq(), trandt, true);
				knsPayd.setAcstno(input.getAcstno()); // 套号
				knsPayd.setPayseq(payseq); // 套内序号
				String paydsq = MsSeqUtil.genSeq("PAYDSQ", CommTools.getBaseRunEnvs().getTrxn_date());
				knsPayd.setPaydsq(paydsq);// 生成方法待定,测试暂设
				knsPayd.setPaydac(input.getOtacno());
				knsPayd.setPayamn(paydFirst.getCharam()); // 销账金额
				knsPayd.setPayasq(paydFirst.getCharsq()); // 挂账序号
				knsPayd.setPayabr(knsPaya.getPayabr()); // 挂账机构
				knsPayd.setPaydbr(CommTools.getBaseRunEnvs().getTrxn_branch()); // 销账机构
				knsPayd.setToacct(knsPaya.getToacct());// 对方账号
				knsPayd.setToacna(knsPaya.getToacna());// 对方户名
				knsPayd.setRsdlmn(knsPaya.getRsdlmn().subtract(paydFirst.getCharam()));
				knsPayd.setTotlmn(knsPaya.getRsdlmn());

				knsPayd.setPaydst(E_PAYDST.WFH); // 销账状态
				knsPayd.setTrandt(trandt); // 日期
				knsPayd.setTransq(transq); // 流水
				knsPayd.setUntius(tranus); // 柜员
				KnsPaydDao.insert(knsPayd); //

				output.setPaydsq(paydsq);
				bizlog.debug("生成挂账记录销账序号为：" + paydsq);

			}
			bizlog.debug("==========转出账号挂账录入登记簿录入结束");
		}
        //转入账号销账录入登记簿录入
		if (input.getInpatp() == E_PAYATP._2) {
			
			bizlog.debug("==========转入账号销账录入登记簿录入开始");
			for (PaydSecond paydSecond : input.getPaydListInfoSecond()) {

				KnsPayd knsPayd = SysUtil.getInstance(KnsPayd.class);

				// 通过挂账流水匹配到对应挂账记录
				//KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(paydSecond.getCharse(), true);
				KnsPaya knsPaya = InTranOutDao.selKnsPayaByAcstnoAndPayasq(input.getAcstno(), paydSecond.getCharse(), trandt, true);
				knsPayd.setAcstno(input.getAcstno()); // 套号
				knsPayd.setPayseq(payseq2); // 套内序号
				String paydsq = MsSeqUtil.genSeq("PAYDSQ", CommTools.getBaseRunEnvs().getTrxn_date());
				knsPayd.setPaydsq(paydsq);// 生成方法待定,测试暂设
				knsPayd.setPaydac(input.getInacno());
				knsPayd.setPayamn(paydSecond.getCharan()); // 销账金额
				knsPayd.setPayasq(paydSecond.getPaydse()); // 挂账序号
				knsPayd.setPayabr(knsPaya.getPayabr()); // 挂账机构
				knsPayd.setPaydbr(CommTools.getBaseRunEnvs().getTrxn_branch()); // 销账机构
				knsPayd.setToacct(knsPaya.getToacct());// 对方账号
				knsPayd.setToacna(knsPaya.getToacna());// 对方户名
				knsPayd.setRsdlmn(knsPaya.getRsdlmn().subtract(paydSecond.getCharan()));
				knsPayd.setTotlmn(knsPaya.getRsdlmn());

				knsPayd.setPaydst(E_PAYDST.WFH); // 销账状态
				knsPayd.setTrandt(trandt); // 日期
				knsPayd.setTransq(transq); // 流水
				knsPayd.setUntius(tranus); // 柜员
//				CommTools.getBaseRunEnvs().setBusi_org_id(glKnaInAcct.getCorpno());//转入法人号
				KnsPaydDao.insert(knsPayd); //
//				CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);//原始法人号
				output.setPaydsq(paydsq);
				bizlog.debug("生成挂账记录销账序号为：" + paydsq);

			}
			bizlog.debug("==========转入账号销账录入登记簿录入结束");
		}
		bizlog.debug("==========内部账转內部账跨法人录入处理結束==========");
		dealCmbkData(trandt); // 联机交易时，处理之前垃圾数据
      
	}

    /**
     * @author wuzhixiang
     * @data 13,Dec
     * 内部户转内部户跨法人复核
     */
	@Override
	public void chkIavccl(
			cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.ChkIavccl.Input input) {
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller();//交易柜员
		
		String mtdate =CommTools.getBaseRunEnvs().getTrxn_date();
		String timetm =DateTools2.getCurrentTimestamp();
		
		//复核转出账号
		KnsCmbk knsCmbk = InTranOutDao.selKnsCmbkByOtacnoAndAcstno(input.getAcstno(), trandt, input.getOtacno(), true);
		if(CommUtil.isNull(knsCmbk)){
			throw InError.comm.E0003("该套号对应的记录不存在！");
		}
		if(CommUtil.equals(knsCmbk.getTranus(),tranus)){
			throw InError.comm.E0003("复核柜员不能为原操作柜员["+knsCmbk.getTranus()+"],请换人复核！");
		}
		if(E_CMBK_TRANST._3 == knsCmbk.getIavcst()){
			throw InError.comm.E0003("该套号已复核，请核对！");
		}
		if(E_CMBK_TRANST._1 == knsCmbk.getIavcst()){
			throw InError.comm.E0003("该套号已入账，请核对！");
		}
		if(E_CMBK_TRANST._2 == knsCmbk.getIavcst()){
			throw InError.comm.E0003("该套号已删除，请核对！");
		}
		if(!CommUtil.equals(input.getAcstno(), knsCmbk.getAcstno())){
			throw InError.comm.E0003("该套号对应的记录不存在！");
		}		
		knsCmbk.setIavcst(E_CMBK_TRANST._3);
		knsCmbk.setCkbsus(tranus);//复核柜员
		
		InTranOutDao.updKnsCmbkChk(input.getAcstno(), trandt, tranus,timetm);
		// 复核转入转出账号挂账记录
		if (input.getOtpatp() == E_PAYATP._1
				|| input.getInpatp() == E_PAYATP._1) {
			/*List<KnsPaya> knsPayaList = KnsPayaDao.selectAll_kns_paya_odx2(
					input.getAcstno(), trandt, true);*/
			List<KnsPaya> knsPayaList = InTranOutDao.selKnsPayaByAcstnoAndTrandt(input.getAcstno(), trandt, true);
			for (KnsPaya knsPaya : knsPayaList) {

				// 过滤已作废记录
				if (knsPaya.getPayast() == E_PAYAST.ZF) {
					continue;
				}

				knsPaya.setPayast(E_PAYAST.YFH);
				KnsPayaDao.updateOne_kns_paya_odx1(knsPaya);
			}

			bizlog.debug("==========挂账信息复核结束");
		}
		// 复核转出账号销账账记录
		if (input.getOtpatp() == E_PAYATP._2
				|| input.getInpatp() == E_PAYATP._2) {
			List<KnsPayd> knsPaydList = KnsPaydDao.selectAll_kns_payd_odx2(
					input.getAcstno(), trandt, true);
			for (KnsPayd knsPayd : knsPaydList) {
				// 过滤已作废记录
				if (knsPayd.getPaydst() == E_PAYDST.ZF) {
					continue;
				}
				knsPayd.setPaydst(E_PAYDST.YFH);
				KnsPaydDao.updateOne_kns_payd_odx1(knsPayd);
			}
			bizlog.debug("==========销账信息复核结束");
		}
	}

	/**
	 * 
	 * @param acstno
	 * @param output
	 * 2016年12月12日-下午4:52:32
	 * @auther chenjk
	 */
	@Override
	public void accIavcdm(
			String acstno,
			cn.sunline.ltts.busi.in.servicetype.IoInAcctTranOut.AccIavcdm.Output output) {
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
	
		bizlog.debug("==========跨机构套平入账处理开始==========");
		iavcdmChk(acstno); //数据检查
		bizlog.debug("==========跨机构套平入账检查结束--入账操作处理开始==========");
		// 
		//List<KnsCmbk> cmbkList = KnsCmbkDao.selectAll_kns_cmbk_odx2(acstno, trandt, true);
		List<KnsCmbk> cmbkList = InTranOutDao.selKnsCmbk2ByAcstno(acstno, trandt, true);
		//注册冲正交易总金额计算
		BigDecimal totlam = new BigDecimal(0);
	
		for(KnsCmbk knsCmbk: cmbkList){
			
			totlam = totlam.add(knsCmbk.getTranam());
			
			if(knsCmbk.getIavcst() == E_CMBK_TRANST._2){
				continue;
			}
			
			output.setIavctp(knsCmbk.getIavctp()); //传票类型
			
			bizlog.debug("系统内部户入账开始==========");
			insysInaccAcc(knsCmbk, acstno, trandt);
			bizlog.debug("==========系统内内部户入账处理结束");
			
			knsCmbk.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
			knsCmbk.setIavcst(E_CMBK_TRANST._1);
			KnsCmbkDao.updateOne_kns_cmbk_odx1(knsCmbk);
		
		}
		
		//冲正注册
		//此处是冲正时执行ApStrike中TRANS_EVENT_INACOT代码段注册
		IoApRegBook cplInput = SysUtil.getInstance(IoApRegBook.class);
        cplInput.setTranam(totlam);
        cplInput.setTranac(cmbkList.get(0).getOtacno()); 
        cplInput.setEvent1(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //交易流水
        cplInput.setCrcycd(cmbkList.get(0).getCrcycd());
        cplInput.setBgindt(trandt);//交易日期
        cplInput.setTranev(ApUtil.TRANS_EVENT_INACOT);
       // ApStrike.regBook(cplInput);	
        IoMsRegEvent apinput = SysUtil.getInstance(IoMsRegEvent.class);      
        apinput.setReversal_event_id(cplInput.getTranev());
        apinput.setInformation_value(SysUtil.serialize(cplInput));
        MsEvent.register(apinput, true);
        
    	bizlog.debug("==========套平入账成功==========");
		
	}
	
	/**
	 * 
	 * @param acstno
	 * 2016年12月13日-上午8:52:43
	 * @auther chenjk
	 */
	//跨机构套平入账数据检查
	private void iavcdmChk(String acstno){
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller(); //交易柜员
		E_AUTHFG authfg = BusiTools.getBusiRunEnvs().getAuthvo().getAuthfg();
	
		if(authfg == E_AUTHFG.YES){
			throw InError.comm.E0003("该交易必须要授权才能通过！");
		}
		if(authfg == E_AUTHFG.NOTICE){
			throw InError.comm.E0003("该交易是否需要授权才能通过！");
		}
		if(CommUtil.isNull(acstno)){
			throw InError.comm.E0003("输入套号不能为空");
		}
		
		if(acstno.length() != 11){
			throw InError.comm.E0003("输入套号不合法，请核查！");
		}
		
		if(CommTools.rpxMatch("W[0-9]+", acstno) == 1){
			throw InError.comm.E0003("输入套号["+acstno+"]不合法，请核查！");
		}
		
		bizlog.debug("跨机构套平入账检查开始==========");
		
		List<KnsCmbk> knsCmbkList = InacSqlsDao.selKnsCmbkUserByBusina(acstno, trandt, false);
	   
		//List<KnsCmbk> knsCmbkList = KnsCmbkDao.selectAll_kns_cmbk_odx2(acstno, trandt, false);  
		
		if(CommUtil.isNull(knsCmbkList)){
			throw InError.comm.E0003("套号["+acstno+"]记录不存在，请核对！");
		}
		 
		BigDecimal tranC = new BigDecimal(0);	//贷方发生额
		BigDecimal tranD = new BigDecimal(0);	//借方发生额
		
		for(KnsCmbk knsCmbk : knsCmbkList){          
			
		    if(knsCmbk.getIavcst() == E_CMBK_TRANST._2){
						continue;
			}
		    //modify by wuzx 20161114 去掉机构校验 beg
	/*			if(!CommUtil.equals(knsCmbk.getAcctbr(), tranbr)){
					throw InError.comm.E0003("非本机构的套号，不允许操作！");
				}
				*/
			if(knsCmbk.getIavcst() != E_CMBK_TRANST._3){
				throw InError.comm.E0003("套内传票状态不为已复核，无法入账，请检查！");
			}
			
			if(!CommUtil.equals(knsCmbk.getCkbsus(), tranus)){
				throw InError.comm.E0003("操作柜员必须为原复核柜员！");
			}
			
			if(knsCmbk.getIavcst() == E_CMBK_TRANST._1){
				throw InError.comm.E0003("套内传票已入账，请检查!");
			}
			
			if(knsCmbk.getIavcst() == E_CMBK_TRANST._9){
				throw InError.comm.E0003("套内传票已冲账，请检查!");
			}
			//IoInacInfo ioInacInfo = SysUtil.getInstance(IoInSrvQryTableInfo.class).selKnaGlAcctnoByAcctno(knsCmbk.getOtacno(), false);
			GlKnaAcct ioInacInfo = InQuerySqlsDao.sel_GlKnaAcct_by_acct(knsCmbk.getOtacno(), true);
			
			if(CommUtil.isNull(ioInacInfo)){
				throw InError.comm.E0003("未找到该内部户信息，请核查！");
			}
			
			if(E_INACST.CLOSED.equals(ioInacInfo.getAcctst())){
				throw InError.comm.E0003("当前转出账户已销户！");
			}
			
			if(E_KPACFG._1.equals(ioInacInfo.getKpacfg())){
				throw InError.comm.E0003("当前转出账户不允许手工记账！");
			}
			
			/*if(CommUtil.equals(tranbr, ioInacInfo.getBrchno())){
				throw InError.comm.E0003("跨法人机构无权进行该项业务！");
			}
			*/
			 
			//销账记录检查  
			if(knsCmbk.getPayatp() == E_PAYATP._2){
				
				List<KnsPayd> knsPaydDbList = InTranOutDao.selKnsPaydByAcst(acstno, trandt,knsCmbk.getOtacno(), true);
				for(KnsPayd knsPayd : knsPaydDbList){
					
				//	KnsPaya knsPaya = KnsPayaDao.selectOne_kns_paya_odx1(knsPayd.getPayasq(), true);
					KnsPaya knsPaya = InTranOutDao.selKnsPayaByPayasq(knsPayd.getPayasq(), true);//改为不带法人查询
					if(!CommUtil.equals(knsPaya.getPayaac(), knsCmbk.getOtacno())){
						throw InError.comm.E0003("销账对应挂账明细的挂账账号，应为当前转出账号");
					}
					
					if(CommUtil.compare(knsPayd.getPayamn(), knsPaya.getRsdlmn()) > 0){
						throw InError.comm.E0003("销账金额大于挂账金额");
					}
					
				}
			}
				
			//借贷平衡验证
			if(knsCmbk.getOtamcd() ==E_AMNTCD.CR){
				tranC = tranC.add(knsCmbk.getTranam());
			}
			
			if(knsCmbk.getOtamcd() ==E_AMNTCD.DR){
				tranD = tranD.add(knsCmbk.getTranam());
			}
					
		}
		bizlog.debug("贷方发生额为:" + tranC + ", 借方发生额为：" + tranD);
		if(!CommUtil.equals(tranC, tranD)){
			throw InError.comm.E0003("该套票借贷不平衡，请核查！");
		}
		
		//复核柜员额度验证
		//TODO 复核柜员额度验证
		/*IoBrchUserQt ioBrchUserQt = SysUtil.getInstance(IoBrchUserQt.class);
		ioBrchUserQt.setBrchno(tranbr);
		ioBrchUserQt.setBusitp(E_BUSITP.TR);
		ioBrchUserQt.setCrcycd(knsCmbkList.get(0).getCrcycd());
		ioBrchUserQt.setTranam(tranC);
		ioBrchUserQt.setUserid(tranus);
		SysUtil.getInstance(IoBrchSvcType.class).selBrchUserQt(ioBrchUserQt);*/
	}



}
