
package cn.sunline.ltts.busi.intran.trans;



import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.tables.DpAccount.KnsAcsq;
import cn.sunline.ltts.busi.in.inner.InacTransDeal;
import cn.sunline.ltts.busi.in.namedsql.InQuerySqlsDao;
import cn.sunline.ltts.busi.in.namedsql.InacSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcct;
import cn.sunline.ltts.busi.in.tables.In.GlKnaAcctDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBusi;
import cn.sunline.ltts.busi.in.tables.In.GlKnpBusiDao;
import cn.sunline.ltts.busi.in.tables.cler.UionCler;
import cn.sunline.ltts.busi.in.tables.cler.UionClerDao;
import cn.sunline.ltts.busi.in.type.InQueryTypes.fnclerinfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
import cn.sunline.ltts.busi.intran.trans.intf.Incler.Output.Acvoch;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.pb.IoChrgComplexType.IoCgCalCenterReturn;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INPTSR;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ISPAYA;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

/**
 * 
 * @author cqm
 * @since 20171001 
 * <p>仅供清算使用<p>
 *
 */
public class incler {
	 private static BizLog log = BizLogUtil.getBizLog(incler.class);

	public static void predeal( final cn.sunline.ltts.busi.intran.trans.intf.Incler.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Incler.Property property,  final cn.sunline.ltts.busi.intran.trans.intf.Incler.Output output){
		
		if(CommUtil.isNull(input.getKeepdt())){
			throw InError.comm.E0003("清算日期不能为空");
		}
		//add by xionlz 通联金融手续支出，兴业银行手续费支出单独清算，可不输入交易金额
		if(CommUtil.compare(input.getTranam(),BigDecimal.ZERO)<=0
				&&!CommUtil.equals(E_CLACTP._28.getValue(), input.getClactp().getValue())
				&&!CommUtil.equals(E_CLACTP._29.getValue(), input.getClactp().getValue())){
			throw InError.comm.E0003("交易金额必须大于0且不能为空");
		}
		if(CommUtil.compare(input.getIntamt(),BigDecimal.ZERO)<0){
			throw InError.comm.E0003("利息金额不能小于0");
		}
		if(CommUtil.isNull(input.getOrdrid())){
			throw InError.comm.E0003("清算订单号不能为空");
		}
		if(CommUtil.isNull(input.getCrcycd())){
			throw InError.comm.E0003("币种不能为空");
		}
		UionCler tbUionCler = UionClerDao.selectOne_odb1(input.getOrdrid(), false);
		if(CommUtil.isNotNull(tbUionCler)){
			throw InError.comm.E0003("该清算订单号已经做过清算");
		}
		if(input.getClactp()!=E_CLACTP._21&&CommUtil.compare(input.getIntamt(),BigDecimal.ZERO)!=0){
				throw InError.comm.E0003("非到期清算,利息金额必须为0或空");
		}
		if((input.getClactp()==E_CLACTP._12||input.getClactp()==E_CLACTP._13)&&
				CommUtil.compare(input.getFeeamt(),BigDecimal.ZERO)!=0){
			throw InError.comm.E0003("黄金清算,费用金额必须为0或空");
		}
		log.info("清算类型为:[%s]",input.getClactp());
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		//IoCgCalCenterReturn tbIoCgCalCenterReturn = SysUtil.getInstance(IoCgCalCenterReturn.class);
		if(CommUtil.equals(E_CLACTP._19.getValue(), input.getClactp().getValue())
				||CommUtil.equals(E_CLACTP._17.getValue(),input.getClactp().getValue())
				||CommUtil.equals(E_CLACTP._12.getValue(), input.getClactp().getValue())
				){//D:银联代付 通联 买金   D:手续费支出 C:与线下核心往来   提现清算
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", input.getClactp().getValue(), "%", true);
			para.setParm_value3(input.getTranam().toString());
			para.setParm_value4(E_AMNTCD.DR.toString());
			property.setDcctno(unionCharg(para,input).getAcctno());
			log.info("银联(通联)代付黄金买金++###+++++++++++Dcctno:[%s]",property.getDcctno());
			if(CommUtil.compare(input.getFeeamt(),BigDecimal.ZERO)>0){
				para = KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", input.getClactp().getValue(), "feeamt", true);
				//para.setParm_value1("N985201700000010");
				para.setParm_value3(input.getFeeamt().toString());
				para.setParm_value4(E_AMNTCD.DR.toString());
				property.setCcctwo(unionChargFee(para,input));
			}
			log.info("银联(通联)代付黄金买金++###+++++++++++setCcctwo:[%s]",property.getCcctwo());
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", "xxwl", "%",true);
			para.setParm_value3(input.getTranam().add(input.getFeeamt()).toString());
			para.setParm_value4(E_AMNTCD.CR.toString());
			
			property.setCcctno(unionCharg(para,input).getAcctno());
			log.info("银联(通联)代付++++++++++++++Ccctno:[%s]",property.getCcctno());
		}else if(CommUtil.equals(E_CLACTP._18.getValue(),input.getClactp().getValue())
				||CommUtil.equals(E_CLACTP._13 .getValue(),input.getClactp().getValue())
				||CommUtil.equals(E_CLACTP._16.getValue(),input.getClactp().getValue())
				){// D与线下核心往来   D:手续费支出  C:银联代收 
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", "xxwl", "%",true);
			para.setParm_value3(input.getTranam().toString());
			para.setParm_value4(E_AMNTCD.DR.toString());
			property.setDcctno(unionCharg(para,input).getAcctno());
			log.info("银联(通联,通联金融，兴业银行)代收黄金卖金++++++++++++Dcctno:[%s]",property.getDcctno());
			if(CommUtil.compare(input.getFeeamt(),BigDecimal.ZERO)>0){
				para = KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", input.getClactp().getValue(), "feeamt", true);
				//para.setParm_value1("N985201700000010");
				para.setParm_value3(input.getFeeamt().toString());
				para.setParm_value4(E_AMNTCD.DR.toString());
				property.setCcctwo(unionChargFee(para,input));
			}
			log.info("银联(通联，通联金融，兴业银行)代收黄金卖金++###+++++++++++setCcctwo:[%s]",property.getCcctwo());
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", input.getClactp().getValue(), "%", true);
			para.setParm_value3(input.getTranam().add(input.getFeeamt()).toString());
			para.setParm_value4(E_AMNTCD.CR.toString());
			property.setCcctno(unionCharg(para,input).getAcctno());
			log.info("银联(通联，通联金融，兴业银行)代收++++++&*&&&e+++++++Ccctno:[%s]",property.getCcctno());
		}else if(CommUtil.equals(E_CLACTP._25.getValue(),input.getClactp().getValue())
				||CommUtil.equals(E_CLACTP._27.getValue(),input.getClactp().getValue())){
			//D:与线下核心往来 C:通联金融代收（兴业银行代收）
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", "xxwl", "%",true);
			para.setParm_value3(input.getTranam().toString());
			para.setParm_value4(E_AMNTCD.DR.toString());
			property.setDcctno(unionCharg(para,input).getAcctno());
			log.info("通联金融，兴业银行代收++++++++++++Dcctno:[%s]",property.getDcctno());
			log.info("通联金融，兴业银行代收++###+++++++++++setCcctwo:[%s]",property.getCcctwo());
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi","in", input.getClactp().getValue(), "%", true);
			para.setParm_value3(input.getTranam().toString());
			para.setParm_value4(E_AMNTCD.CR.toString());
			property.setCcctno(unionCharg(para,input).getAcctno());
			log.info("通联金融，兴业银行代收++++++&*&&&e+++++++Ccctno:[%s]",property.getCcctno());
		}else if(CommUtil.equals(E_CLACTP._29.getValue(),input.getClactp().getValue())||
		         CommUtil.equals(E_CLACTP._28.getValue(),input.getClactp().getValue())){
			//手续费清算，费用金额与进项税不能为空
			if(CommUtil.isNull(input.getFeeamt())){
				throw InError.comm.E0003("费用不能为空！");
			}
			if(CommUtil.isNull(input.getVatnam())){
				throw InError.comm.E0003("进项税不能为空！");
			}
			//D:（通联金融，兴业银行）手续费支出 D:应交税费-应交增值税（进项税22210101）C:与核心清算往来
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", input.getClactp().getValue(), "feeamt", true);
			para.setParm_value3(input.getFeeamt().toString());
			para.setParm_value4(E_AMNTCD.DR.toString());
			property.setCcctwo(unionChargFee(para,input));
			log.info("兴业银行手续费支出++++++++++++Ccctno:[%s]",property.getCcctwo());
			
			para =KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", "ZZS", "%", true);
			para.setParm_value3(input.getVatnam().toString());
			para.setParm_value4(E_AMNTCD.DR.toString());
			property.setCcctee(unionChargFee(para,input));
			log.info("进项税++++++++++++Ccctno:[%s]",property.getCcctee());
			
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", "xxwl", "%",true);
			para.setParm_value3(input.getFeeamt().add(input.getVatnam()).toString());
			para.setParm_value4(E_AMNTCD.CR.toString());
			property.setCcctno(unionCharg(para,input).getAcctno());
			log.info("与线下核心清算往来++++++&*&&&e+++++++Ccctno:[%s]",property.getCcctno());
		}else if(CommUtil.equals(E_CLACTP._24.getValue(),input.getClactp().getValue())
				||CommUtil.equals(E_CLACTP._26.getValue(),input.getClactp().getValue())
				||CommUtil.equals(E_CLACTP._31.getValue(), input.getClactp().getValue())){
			//D:通联金融头寸户（兴业银行头寸户，兴业银行保证金划转） C:与核心清算往来
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", input.getClactp().getValue(), "%", true);
			para.setParm_value3(input.getTranam().toString());
			para.setParm_value4(E_AMNTCD.DR.toString());
			property.setDcctno(unionCharg(para,input).getAcctno());
			log.info("通联金融头寸户（兴业银行头寸户，兴业银行保证金）++++++++++++Ccctno:[%s]",property.getDcctno());
			
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", "xxwl", "%",true);
			para.setParm_value3(input.getTranam().toString());
			para.setParm_value4(E_AMNTCD.CR.toString());
			property.setCcctno(unionCharg(para,input).getAcctno());
			log.info("与线下核心清算往来++++++&*&&&e+++++++Ccctno:[%s]",property.getCcctno());
		}else if(CommUtil.equals(E_CLACTP._32.getValue(), input.getClactp().getValue())){
		    //D:与核心往来C:兴业银行头寸户利息收入
		    para = KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", "xxwl", "%",true);
		    para.setParm_value3(input.getTranam().toString());
		    para.setParm_value4(E_AMNTCD.DR.toString());
		    property.setDcctno(unionCharg(para,input).getAcctno());
		    log.info("(与核心系统往来)++++++++++++Dcctno:[%s]",property.getDcctno());
		    
		    para = KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", input.getClactp().getValue(), "feeamt", true);
            para.setParm_value3(input.getTranam().toString());
            para.setParm_value4(E_AMNTCD.CR.toString());
            property.setCcctno(unionChargFee(para, input));
            log.info("(兴业银行头寸户利息收入)++++++++++++cctno:[%s]",property.getDcctno());
            
		}else if (CommUtil.equals(E_CLACTP._20.getValue(), input.getClactp().getValue())){
			//理财成立清算
			if(CommUtil.isNull(input.getProdcd())){
				throw InError.comm.E0003("产品号不能为空");
			}
			if(CommUtil.compare(input.getFeeamt(),BigDecimal.ZERO)!=0){
				throw InError.comm.E0003("理财成立清算费用金额必须为0或空");
			}
			//借方：负债端本金
			fnclerinfo tbfnclerinfo = InQuerySqlsDao.selfnclerbusi(input.getProdcd(), false);
			if(CommUtil.isNull(tbfnclerinfo)){
				throw InError.comm.E0003("产品号输入有误,请核对该产品是否存在");
			}
			//查询子户号
			GlKnpBusi tblglknpbusi=GlKnpBusiDao.selectOne_odb1(tbfnclerinfo.getPrbusi(), false);
			para.setParm_value2(tblglknpbusi.getSubsac());//设置子户号
			
			para.setParm_value1(tbfnclerinfo.getPrbusi());
			para.setParm_value3(input.getTranam().toString());
			para.setParm_value4(E_AMNTCD.DR.toString());
			property.setDcctno(unionCharg(para,input).getAcctno());
			log.info("理财成立+++++++++++++Dcctno:[%s]",property.getDcctno());
//			//贷方:线下往来户
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", "xxwl", "%",true);
        	para.setParm_value3(input.getTranam().toString());
			para.setParm_value4(E_AMNTCD.CR.toString());
			property.setCcctno(unionCharg(para,input).getAcctno());
			log.info("理财成立+++++++++++++Ccctno:[%s]",property.getCcctno());
		}else if(CommUtil.equals(E_CLACTP._21.getValue(),input.getClactp().getValue())){
			//借方：往来户
			para = KnpParameterDao.selectOne_odb1("InParm.clearbusi", "in", "xxwl", "%",true);
			if(CommUtil.isNull(input.getProdcd())){
				throw InError.comm.E0003("产品号不能为空");
			}
			para.setParm_value3(input.getTranam().toString());
			para.setParm_value4(E_AMNTCD.DR.toString());
			property.setDcctno(unionCharg(para,input).getAcctno());
			log.info("理财到期清算+++++++++++++Dcctno:[%s]",property.getDcctno());
//			//贷方1:负债端本金
			fnclerinfo tbfnclerinfo = InQuerySqlsDao.selfnclerbusi(input.getProdcd(), false);
			if(CommUtil.isNull(tbfnclerinfo)){
				throw InError.comm.E0003("产品号输入有误,请核对该产品是否存在");
			}
			//查询子户号
			GlKnpBusi tblglknpbusi=GlKnpBusiDao.selectOne_odb1(tbfnclerinfo.getPrbusi(), false);
			para.setParm_value2(tblglknpbusi.getSubsac());//设置子户号
			
			para.setParm_value1(tbfnclerinfo.getPrbusi());
			para.setParm_value3(input.getTranam().toString());
			para.setParm_value4(E_AMNTCD.CR.toString());
			property.setCcctno(unionCharg(para,input).getAcctno());
			log.info("理财到期清算+++++++++++++Ccctno:[%s]",property.getCcctno());
		}else{
			throw InError.comm.E0003("清算类型和交易账户不能同时为空");
		}

		
	    output.setMntrdt(CommTools.getBaseRunEnvs().getTrxn_date());
	    output.setMntrsq(CommTools.getBaseRunEnvs().getTrxn_seq());
	    output.setMntrtm(BusiTools.getBusiRunEnvs().getTrantm());

	}
	public static Options<IoCgCalCenterReturn> register(IoCgCalCenterReturn IoCgCalCenterReturn,final cn.sunline.ltts.busi.intran.trans.intf.Incler.Input input){
		Options<IoCgCalCenterReturn> tbchrgpm  = new DefaultOptions<IoCgCalCenterReturn>();
		IoCgCalCenterReturn  tbIoCgCalCenterReturn =  SysUtil.getInstance(IoCgCalCenterReturn.class);
		tbIoCgCalCenterReturn.setChrgcd(IoCgCalCenterReturn.getChrgcd());
		tbIoCgCalCenterReturn.setCustac(IoCgCalCenterReturn.getCustac());
		tbIoCgCalCenterReturn.setTranam(input.getTranam().add(input.getFeeamt()).add(input.getIntamt()));
		tbIoCgCalCenterReturn.setAmount(new Long("1"));
		tbIoCgCalCenterReturn.setClcham(input.getFeeamt());
		tbIoCgCalCenterReturn.setPaidam(input.getFeeamt());//
		tbIoCgCalCenterReturn.setDircam(input.getFeeamt());//优惠后应收总额
		tbIoCgCalCenterReturn.setChnotp(IoCgCalCenterReturn.getChnotp());
		tbIoCgCalCenterReturn.setChnona(IoCgCalCenterReturn.getChnona());
		tbIoCgCalCenterReturn.setPronum(IoCgCalCenterReturn.getPronum());
		tbIoCgCalCenterReturn.setTrinfo("1");
		tbIoCgCalCenterReturn.setServtp("TM");
		tbIoCgCalCenterReturn.setScencd("%");
		tbIoCgCalCenterReturn.setScends("通用场景");
		tbchrgpm.add(tbIoCgCalCenterReturn);
		return tbchrgpm;
	}
	public static IaTransOutPro unionCharg(KnpParameter tbKnpParameter,final cn.sunline.ltts.busi.intran.trans.intf.Incler.Input input){
			String busino = tbKnpParameter.getParm_value1();
			String  subsac = tbKnpParameter.getParm_value2();
			BigDecimal tranam = new BigDecimal(tbKnpParameter.getParm_value3());//金额
			E_AMNTCD amntcd =E_AMNTCD.get(tbKnpParameter.getParm_value4());
			String  crcycd = input.getCrcycd();//
			String commnt = tbKnpParameter.getParm_value4();

			
			IaAcdrInfo inAcctSvcInput = SysUtil.getInstance(IaAcdrInfo.class);
			inAcctSvcInput.setTranam(tranam);
			inAcctSvcInput.setAcbrch(CommTools.getBaseRunEnvs().getTrxn_branch());
			
			inAcctSvcInput.setDscrtx(commnt);
//			if(CommUtil.equals(busino.substring(0, 4),"9001") && (busino.length() == 18)){
//				inAcctSvcInput.setAcctno(busino); //如果是9开头，说明配的是内部账号
//			}else{
			inAcctSvcInput.setBusino(busino);
//			}
			inAcctSvcInput.setCrcycd(crcycd);
//			inAcctSvc.ioInAcdr(inAcctSvcInput);
			inAcctSvcInput.setAmntcd(amntcd);
			inAcctSvcInput.setInptsr(E_INPTSR.GL01);
			inAcctSvcInput.setSubsac(subsac);
			return InacTransDeal.dealInnerAccountTran(inAcctSvcInput);
	}
	
	public static String unionChargFee(KnpParameter tbKnpParameter,final cn.sunline.ltts.busi.intran.trans.intf.Incler.Input input){
		String busino = tbKnpParameter.getParm_value1();	//核算代码
		
		// 登记会计流水开始
        IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
        cplIoAccounttingIntf.setCuacno(busino); //记账账号-登记核算代码
        cplIoAccounttingIntf.setAcseno(busino); //子账户序号-登记核算代码
        cplIoAccounttingIntf.setAcctno(busino); //负债账号-登记核算代码
        cplIoAccounttingIntf.setProdcd(busino); //产品编号-登记核算代码
        cplIoAccounttingIntf.setDtitcd(busino); //核算口径-登记核算代码
        cplIoAccounttingIntf.setCrcycd(input.getCrcycd()); //币种                 
        cplIoAccounttingIntf.setTranam(new BigDecimal(tbKnpParameter.getParm_value3())); //交易金额 
        cplIoAccounttingIntf.setAcctdt(CommTools.getBaseRunEnvs().getTrxn_date());// 应入账日期
        cplIoAccounttingIntf.setMntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq()); //主交易流水
        cplIoAccounttingIntf.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date()); //交易日期 
        cplIoAccounttingIntf.setAcctbr(CommTools.getBaseRunEnvs().getTrxn_branch()); //账务机构
        cplIoAccounttingIntf.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
        cplIoAccounttingIntf.setAmntcd(E_AMNTCD.get(tbKnpParameter.getParm_value4())); //借贷标志

        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.FE); //会计主体类型-手续费
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); //会计流水类型-账务流水
        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE); //余额属性-本金科目
        cplIoAccounttingIntf.setTranms(tbKnpParameter.getParm_value4());//交易信息
        cplIoAccounttingIntf.setServtp(CommTools.getBaseRunEnvs().getChannel_id());
        //登记会计流水
        SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(cplIoAccounttingIntf);
		
        return busino;
	}
	


	public static void afterdeal( final cn.sunline.ltts.busi.intran.trans.intf.Incler.Input input,  final cn.sunline.ltts.busi.intran.trans.intf.Incler.Property property,  final cn.sunline.ltts.busi.intran.trans.intf.Incler.Output output){
		//Options<Acvoch> acvoch = SysUtil.getInstance(intfClass);
		List<KnsAcsq> listAcsq= InacSqlsDao.seltransq(CommTools.getBaseRunEnvs().getTrxn_date(), CommTools.getBaseRunEnvs().getTrxn_seq(),true);
		for(KnsAcsq tbKnsAcsq:listAcsq){//会计凭证供打印凭证使用
			Acvoch tbAcvoch = SysUtil.getInstance(Acvoch.class);
			tbAcvoch.setAcctno(tbKnsAcsq.getAcctno());
			tbAcvoch.setAmntcd(tbKnsAcsq.getAmntcd());
			tbAcvoch.setDtitcd(tbKnsAcsq.getDtitcd());
			tbAcvoch.setTranam(tbKnsAcsq.getTranam());
			tbAcvoch.setCrcycd(tbKnsAcsq.getCrcycd());

			GlKnaAcct tbGlKnaAcct = GlKnaAcctDao.selectOne_odb1(tbKnsAcsq.getAcctno(), false);
			if(CommUtil.isNotNull(tbGlKnaAcct)){
				tbAcvoch.setAcctna(tbGlKnaAcct.getAcctna());
				tbAcvoch.setIspaya(tbGlKnaAcct.getIspaya());
			}else{
				GlKnpBusi tblGlKnpbusi = GlKnpBusiDao.selectOne_odb1(tbKnsAcsq.getAcctno(), false);
				if(CommUtil.isNotNull(tblGlKnpbusi)){
					tbAcvoch.setAcctna(tblGlKnpbusi.getBusina());
					tbAcvoch.setIspaya(E_ISPAYA._0);
				}
			}
			output.getAcvoch().add(tbAcvoch);
		}	
		output.setSmrycd(input.getSmrycd());
		SysUtil.getInstance(IoCheckBalance.class).
		checkBalance(CommTools.getBaseRunEnvs().getTrxn_date(),CommTools.getBaseRunEnvs().getTrxn_seq(),
				E_CLACTP._99);

		}

		//output.setAcvoch(acvoch);
	


}

