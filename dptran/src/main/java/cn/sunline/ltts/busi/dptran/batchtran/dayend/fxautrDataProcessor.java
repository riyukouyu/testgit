package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublicServ;
import cn.sunline.ltts.busi.dp.domain.DpSaveEntity;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.FxautrTranData;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Fxautr.Input;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Fxautr.Property;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoCheckBalance;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbSmsSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.dp.IoAccountComplexType.IoAccounttingIntf;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubSqrd;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRDT;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Params;

/**
 * 定期到期自动转存
 *  1.只处理智能储蓄产品的定期到期数据
 *  2.将利息转存到本金中，即：新本金=本金+利息
 * 
 */

public class fxautrDataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Fxautr.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Fxautr.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.FxautrTranData> {

	private static final BizLog bizlog = BizLogUtil
			.getBizLog(fxautrDataProcessor.class);

	private static final String Parm_code = "DpParm.auacct";// 智能储蓄产品参数名
  

    /**
     * 获取智能储蓄产品号
     * 
     * @return
     */
    private static String getAuacpd() {
        return KnpParameterDao.selectOne_odb1(Parm_code, "prodcd", "%", "%", false)
                .getParm_value1();
    }

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param job
	 *            批次作业ID
	 * @param index
	 *            批次作业第几笔数据(从1开始)
	 * @param dataItem
	 *            批次数据项
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(
			String jobId,
			int index,
			cn.sunline.ltts.busi.dp.type.DpDayEndType.FxautrTranData dataItem,
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Fxautr.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Fxautr.Property property) {
		bizlog.debug("账号[" + dataItem.getAcctno() + "]自动转存单笔执行");

		//只处理智能储蓄产品
//		private String trandt = DateTools2.getDateInfo().getSystdt();// 交易日期
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String auacpd = getAuacpd();// 智能储蓄产品号
		if(!CommUtil.equals(auacpd, dataItem.getProdcd())){
		    return;
		}
//		CommTools.getBaseRunEnvs().sett
		// 每一笔交易重新生成一笔流水，用来进行平衡性检查
		 MsSystemSeq.getTrxnSeq();

		try {
			bizlog.debug("获取利率");
			// 获取利率
			KubInrt inrt = KubInrtDao.selectOne_odb1(dataItem.getAcctno(),
					false);
			if (CommUtil.isNull(inrt))
				throw ApError.BusiAplt.E0000("账户利率信息为空");
			//IntrPublicEntity entity = new IntrPublicEntity();
			IoPbIntrPublicEntity entity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
			entity.setCrcycd(dataItem.getCrcycd());
			entity.setIntrcd(inrt.getIntrcd());
			entity.setIncdtp(inrt.getIncdtp());
			entity.setTrandt(trandt);
			entity.setTranam(dataItem.getOnlnbl());
			entity.setCainpf(E_CAINPF.T1); //addby luxy 靠档利率需要
			entity.setIntrwy(inrt.getIntrwy());
			entity.setBgindt(dataItem.getBgindt());
			entity.setEdindt(trandt);
			entity.setDepttm(dataItem.getDepttm());
			entity.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
			//-----add by xieqq --20170624，登记会计流水表的时候需要账务所属机构。根据账号是活期或者定期获取。start----
		    KnaFxac fx = KnaFxacDao.selectOne_odb1(dataItem.getAcctno(), false);
		    if(CommUtil.isNotNull(fx)){
				entity.setBrchno(fx.getBrchno());
				CommTools.getBaseRunEnvs().setTrxn_branch(fx.getBrchno());
		    }
		  //-----add by xieqq --20170624，登记会计流水表的时候需要账务所属机构。根据账号是活期或者定期获取。end----
			//IntrPublic.getIntr(entity);
			
			KnbAcin acin = KnbAcinDao.selectOne_odb1(dataItem.getAcctno(),false);
			
			entity.setLevety(acin.getLevety());
			if(acin.getIntrdt() == E_INTRDT.OPEN){
				entity.setTrandt(acin.getOpendt());
				entity.setTrantm("999999");
			}
			
			SysUtil.getInstance(IoSrvPbInterestRate.class).countInteresRate(entity);
			BigDecimal inamnt = entity.getInamnt();
			bizlog.debug("利率=[" + entity.getIntrvl() + "]");
			bizlog.debug("利息=[" + entity.getInamnt() + "]");

			// 记账（借：应付利息（内部户记账），贷：智能储蓄定期存款）不能调用内部户记账服务
			// 借
			// 将利息、利息税登记到会计流水
			regACSerail(dataItem, entity.getInamnt(), BigDecimal.ZERO);

			// 贷
			DpSaveEntity input_save = SysUtil.getInstance(DpSaveEntity.class);
			input_save.setAcctno(dataItem.getAcctno());
			input_save.setDetlsq(dataItem.getDetlsq());
			input_save.setAcseno(dataItem.getAcseno());
			input_save.setCardno(dataItem.getCardno());
			input_save.setCrcycd(dataItem.getCrcycd());
			input_save.setCustac(dataItem.getCustac());
			input_save.setOpacna(dataItem.getAcctna());
			input_save.setToacct(dataItem.getAcctno());
			input_save.setFxaufg(E_YES___.YES);// 定期自动转存标志
			input_save.setTranam(inamnt);
			input_save.setSmrycd(BusinessConstants.SUMMARY_TZ);
			input_save.setSmryds(ApSmryTools.getText(BusinessConstants.SUMMARY_TZ));
			// input_save.setAuacfg(E_YES___.NO);//不是普通的智能储蓄存取
			DpPublicServ.postAcctDp(input_save);
		} catch (Exception e) {
			String errmsg = "账号[" + dataItem.getAcctno() + "]自动转存失败。";
			bizlog.error(errmsg);
			// TODO 如果需要登记错误信息到数据库中，需要用下面的方法
			// DaoUtil.executeInNewTransation(new RunnableWithReturn<Integer>(){
			//
			// @Override
			// public Integer execute() {
			//
			// return 0;
			// }
			//
			// });
			throw DpModuleError.DpstComm.E9999(errmsg, e);
		}

		// 检查平衡
		
		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
		IoCheckBalance ioCheckBalanceSrv = SysUtil
				.getInstance(IoCheckBalance.class);
		ioCheckBalanceSrv.checkBalance(trandt, transq,null);

		bizlog.debug("账号[" + dataItem.getAcctno() + "]自动转存单笔执行完成");
		
		
		/*
		 * 智能存款转存短信发送
		 */
		// 根据电子账户获取电子账户表数据
		IoCaKnaCust cplCaKnaCust = SysUtil.getInstance(
				IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(dataItem.getCustac(),
				false);
		if (CommUtil.isNull(cplCaKnaCust)) {
			// throw DpModuleError.DpstComm.E9999("电子账号不存在! ");
			bizlog.error("电子账号不存在! ", trandt);
			return;
		}
		// 查询客户关联关系表
		/*
		IoCifCustAccs cplCifCustAccs = SysUtil.getInstance(
				IoCuSevQryTableInfo.class).cif_cust_accsByCustno(
				cplCaKnaCust.getCustno(), false, E_STATUS.NORMAL);
		*/
		// 短信流水登记
		IoPbKubSqrd cplKubSqrd = SysUtil.getInstance(IoPbKubSqrd.class);
		/*
		if (CommUtil.isNotNull(cplCifCustAccs)) {
			cplKubSqrd.setAppsid(cplCifCustAccs.getAppsid());// app推送ID
		}
		*/
		cplKubSqrd.setPrcscd(BusiTools.getBusiRunEnvs().getLttscd());// 内部交易码
		cplKubSqrd.setTrandt(CommTools.getBaseRunEnvs().getTrxn_date());// 交易日期
		cplKubSqrd.setTrantm(BusiTools.getBusiRunEnvs().getTrantm());// 交易时间
		cplKubSqrd.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());// 交易流水
		cplKubSqrd.setTmstmp(DateTools2.getCurrentTimestamp());// 时间戳
		cplKubSqrd.setPmvl01(dataItem.getCustac());
		// 调用短信流水登记服务
		SysUtil.getInstance(IoPbSmsSvcType.class).pbTransqReg(cplKubSqrd);
	}

	/**
	 * 获取数据遍历器。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<cn.sunline.ltts.busi.dp.type.DpDayEndType.FxautrTranData> getBatchDataWalker(
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Fxautr.Input input,
			cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Fxautr.Property property) {
		Params params = new Params();
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		params.add("trandt", trandt);
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		return new CursorBatchDataWalker<FxautrTranData>(
				DpDayEndDao.namedsql_selFxautrDatas, params);
	}

	/**
	 * @Author <p>
	 *         <li>功能说明：利息、利息税登记会计流水登记</li>
	 *         </p>
	 * @param trandata
	 */
	public static void regACSerail(FxautrTranData trandata, BigDecimal instam,
			BigDecimal intxam) {
		// 利息金额不为零
		if (CommUtil.compare(instam, BigDecimal.ZERO) != 0) {
			String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
			String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq();
			String mntrsq = CommTools.getBaseRunEnvs().getMain_trxn_seq(); // 主交易流水
			String brchno = CommTools.getBaseRunEnvs().getTrxn_branch(); // 交易机构
			/* 应入账日期 */
			String acctdt = trandt;

			IoAccounttingIntf cplIoAccounttingIntf = SysUtil
					.getInstance(IoAccounttingIntf.class);

			cplIoAccounttingIntf.setCuacno(trandata.getCustac());
			cplIoAccounttingIntf.setAcctno(trandata.getAcctno());
			cplIoAccounttingIntf.setAcseno(trandata.getAcseno());
			cplIoAccounttingIntf.setProdcd(trandata.getProdcd());
			cplIoAccounttingIntf.setDtitcd(trandata.getAcctcd());
			cplIoAccounttingIntf.setCrcycd(trandata.getCrcycd());
			cplIoAccounttingIntf.setTranam(instam);
			cplIoAccounttingIntf.setAcctdt(acctdt); // 应入账日期
			cplIoAccounttingIntf.setTransq(transq);
			cplIoAccounttingIntf.setMntrsq(mntrsq);
			cplIoAccounttingIntf.setTrandt(trandt);
			cplIoAccounttingIntf.setAcctbr(brchno);
			cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR); // 借方
			cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP); // 存款
			cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT); // 账务流水
			cplIoAccounttingIntf.setBltype(E_BLTYPE.PYIN); // 余额属性：利息支出
			// 登记交易信息，供总账解析
			if (CommUtil.equals(
					"1",
					KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
							true).getParm_value1())) {
				KnpParameter para = SysUtil.getInstance(KnpParameter.class);
				para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3010300", "%",
						"%", true);
				cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息 结息
			}			
			SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
					cplIoAccounttingIntf);

			// 利息税金额不为零
			if (CommUtil.compare(intxam, BigDecimal.ZERO) != 0) {

				cplIoAccounttingIntf.setCuacno(trandata.getCustac());
				cplIoAccounttingIntf.setAcctno(trandata.getAcctno());
				cplIoAccounttingIntf.setAcseno(trandata.getAcseno());
				cplIoAccounttingIntf.setProdcd(trandata.getProdcd());
				cplIoAccounttingIntf.setDtitcd(trandata.getAcctcd());
				cplIoAccounttingIntf.setCrcycd(trandata.getCrcycd());
				cplIoAccounttingIntf.setTranam(intxam); // 利息税
				cplIoAccounttingIntf.setAcctdt(acctdt); // 应入账日期
				cplIoAccounttingIntf.setTransq(transq);
				cplIoAccounttingIntf.setMntrsq(mntrsq); // 主交易流水
				cplIoAccounttingIntf.setTrandt(trandt); // 账务日期
				cplIoAccounttingIntf.setAcctbr(brchno);
				cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR);
				cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
				cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
				cplIoAccounttingIntf.setBltype(E_BLTYPE.INTAX);
				// 登记交易信息，供总账解析
				if (CommUtil.equals(
						"1",
						KnpParameterDao.selectOne_odb1("GlAnalysis", "switch", "%", "%",
								true).getParm_value1())) {
					KnpParameter para = SysUtil.getInstance(KnpParameter.class);
					para = KnpParameterDao.selectOne_odb1("GlAnalysis", "3020100", "%",
							"%", true);
					cplIoAccounttingIntf.setTranms(para.getParm_value1());// 登记交易信息 结息
				}					
				SysUtil.getInstance(IoAccountSvcType.class).ioAccountting(
						cplIoAccounttingIntf);
			}
		}
	}

	@Override
	public void beforeTranProcess(String taskId, Input input, Property property) {
//		CommTools.getBaseRunEnvs().setServtp(ApUtil.DP_DAYEND_CHANNEL);// 日终渠道   
		CommTools.getBaseRunEnvs().setChannel_id("NK"); //日中渠道  暂时本系统作为日终渠道
	}
}
