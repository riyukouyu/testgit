package cn.sunline.ltts.busi.dptran.batchtran.dayend;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbIndlDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.IntrSubSection;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.KtpAdinType;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitr.Input;
import cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitr.Property;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbInRaSelSvc;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRRTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_REPRWY;
	 /**
	  * 利率调整(分段处理)
	  * 单层利率
	  *
	  */

public class subitrDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitr.Input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitr.Property, cn.sunline.ltts.busi.dp.type.DpDayEndType.KtpAdinType> {
	  
	private static BizLog bizlog = BizLogUtil.getBizLog(subitrDataProcessor.class);
	//private static String cnfrdm;; //省中心法人代码
	
	//存放分档利率的代码
	//private static List<String> lstRlir = null;
	
	//存放需要调整的浮动利率和基础利率的特征值(利率代码+存期+币种的字符串组合)
	//private static Map<String, IntrSubSection> lstIntr = null;
	
	
	//private static boolean isrun = false;
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param job 批次作业ID
	 * @param index  批次作业第几笔数据(从1开始)
	 * @param dataItem 批次数据项
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	@Override
	public void process(String jobId, int index, cn.sunline.ltts.busi.dp.type.DpDayEndType.KtpAdinType dataItem, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitr.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitr.Property property) {
		
		bizlog.debug("开始调整账户["+dataItem.getAcctno()+"]的利率");
		bizlog.debug("dataItem===================="+dataItem);
		
//		String oldCorpno = CommTools.getBaseRunEnvs().getBusi_org_id();
//		CommTools.getBaseRunEnvs().setBusi_org_id(dataItem.getCorpno());
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String lstrdt = CommTools.getBaseRunEnvs().getLast_date();
		//String mtdate = DateTools2.getDateInfo().getSystdt();
		String timetm = DateTools2.getCurrentTimestamp();
		
		
		E_DPACST acctst = null;
		E_TERMCD depttm = E_TERMCD.T000;
		BigDecimal onlnbl = BigDecimal.ZERO;
		String brchno = "";
		
		
		if(dataItem.getIncdtp() == E_IRCDTP.LAYER){
			if(property.getLstRlir().contains(dataItem.getIntrcd())){
				bizlog.debug("<<======账号:[%s],利率代码:[%s],利率代码类型:[%s]删除当日预提数据======>>",dataItem.getAcctno(), dataItem.getIntrcd(), dataItem.getIncdtp().getValue());
				DpDayEndDao.delknbcbdlData(lstrdt, dataItem.getAcctno()); // 删除切日后的上日预计提数据
			}else{
				bizlog.debug("<<======账号:[%s],利率代码:[%s],利率代码类型:[%s]不需要调整利率======>>",dataItem.getAcctno(), dataItem.getIntrcd(), dataItem.getIncdtp().getValue());
			}
			return;
		}
		String tmp = "";
		if (CommUtil.isNotNull(dataItem.getLvindt())) {
			tmp = dataItem.getLvindt().getValue();
		}
		
		String onlyCode = dataItem.getIntrcd() + tmp + dataItem.getCrcycd() + dataItem.getCorpno();
		if(!property.getLstIntr().contains(onlyCode)){
			onlyCode = dataItem.getIntrcd() + tmp + dataItem.getCrcycd() + property.getCnfrdm();
			if(!property.getLstIntr().contains(onlyCode)){
				bizlog.debug("<<======账号:[%s],利率代码:[%s],利率代码类型:[%s]不需要调整利率======>>",dataItem.getAcctno(), dataItem.getIntrcd(), dataItem.getIncdtp().getValue());
				return;
			}
		}
		
		if(dataItem.getPddpfg() == E_FCFLAG.CURRENT){
			KnaAcct acct = KnaAcctDao.selectOne_odb1(dataItem.getAcctno(), true);
			acctst = acct.getAcctst();
//			onlnbl = acct.getOnlnbl();
			onlnbl = DpAcctProc.getAcctBalance(acct);
			brchno = acct.getBrchno();
			depttm = acct.getDepttm();
		}else if(dataItem.getPddpfg() == E_FCFLAG.FIX){
			KnaFxac fxac = KnaFxacDao.selectOne_odb1(dataItem.getAcctno(), true);
			acctst = fxac.getAcctst();
			onlnbl = fxac.getOnlnbl();
			brchno = fxac.getBrchno();
			depttm = fxac.getDepttm();
		}else{
			
		}
		
		if(CommUtil.isNull(depttm)){
			depttm = E_TERMCD.T000;
		}
		
		
		if(acctst == E_DPACST.CLOSE){ //非正常账户排除
			return;
		}
		
		bizlog.debug("<<======账号:[%s],利率代码:[%s],利率代码类型:[%s]开始调整利率======>>",dataItem.getAcctno(), dataItem.getIntrcd(), dataItem.getIncdtp().getValue());
		
		E_YES___ isrect = E_YES___.NO; //是否需要调整
		E_YES___ isblock = E_YES___.NO; //是否需要分段
		
		if(dataItem.getInprwy() == E_IRRTTP.AZ){ //指定周期重定价
			String inadlv = dataItem.getInadlv(); //利率调整频率
			if(CommUtil.isNull(dataItem.getLuindt())){
				dataItem.setLuindt(dataItem.getOpendt());
			}
			
//			String inaddt = DateTools2.calDateByFreq(dataItem.getLuindt(), inadlv, "", "", 3, 0);
			String inaddt = DateTools2.calDateByFreq(dataItem.getLuindt(), inadlv, null, 0);
			if(CommUtil.equals(inaddt, lstrdt)){
				isrect = E_YES___.YES;
				dataItem.setLuindt(inaddt);
			}
		}else if(dataItem.getInprwy() == E_IRRTTP.CK){
			isrect = E_YES___.YES;
		}else if(dataItem.getInprwy() == E_IRRTTP.MT){ //升息比重重定价
			throw DpModuleError.DpstComm.E9999("系统暂不支持:[" + E_IRRTTP.MT.getLongName() + "]");
		}else if(dataItem.getInprwy() == E_IRRTTP.QD || dataItem.getInprwy() == E_IRRTTP.NO){ //到期转存重订价
			return;
		}
		
		if(dataItem.getReprwy() == E_REPRWY.ALL){ //全部调整,表示不需要分段
			isblock = E_YES___.NO;
		}else if(dataItem.getReprwy() == E_REPRWY.BACK){ //后段调整，表示需要分段
			isblock = E_YES___.YES;
		}else if(dataItem.getReprwy() == E_REPRWY.PART){ //升息比重重定价时使用，前后段分别调整
			throw DpModuleError.DpstComm.E9999("系统暂不支持:[" + E_REPRWY.PART.getLongName() + "]");
		}
		
		if(isrect == E_YES___.YES){
			IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
			if(isblock == E_YES___.YES){
				
				
				//1、计算账户积数
				BigDecimal curram = dataItem.getCutmam();
				//BigDecimal onlnbl = DpPublic.getOnlnblByAcctno(dataItem.getAcctno());
				
				bizlog.debug("当前日期：["+trandt+"]", "");
				String lsamdt = dataItem.getLaamdt();
				bizlog.debug("当前日期：["+lsamdt+"]", "");
				BigDecimal cut = DpPublic.calRealTotalAmt(curram, onlnbl, trandt, lsamdt);
				
				// 利息税计算
				BigDecimal taxrat = SysUtil.getInstance(IoPbInRaSelSvc.class).inttxRate(dataItem.getIntxcd()).getTaxrat();
				BigDecimal taxCut =  BigDecimal.ZERO;
				BigDecimal rlintr =  BigDecimal.ZERO;// 利息
				if(!CommUtil.equals(BigDecimal.ZERO, cut)){
					rlintr = pbpub.countInteresRateByBase(dataItem.getCuusin(), cut);
					taxCut = rlintr.multiply(taxrat);
				}
				
				//2、记录到负债账户利息明细表
				KnbIndl indl = SysUtil.getInstance(KnbIndl.class);
				
				indl.setGradin(BigDecimal.ZERO);//档次计息余额
				indl.setTotlin(BigDecimal.ZERO);//总计息余额
				indl.setRlintr(rlintr);//实际利息发生额
				indl.setCatxrt(taxrat);//计提税率
				indl.setRlintx(taxCut);//实际利息税发生额
				
				indl.setAcbsin(dataItem.getAcbsin());//基准利率
				indl.setAcctno(dataItem.getAcctno());//负债账号
				indl.setAcmltn(cut);//积数
				indl.setCuusin(dataItem.getCuusin());//当前执行利率
				indl.setDetlsq(getDetlsq(dataItem.getAcctno(),dataItem.getIntrtp()));//明细序号
				indl.setIncdtp(dataItem.getIncdtp());//利率代码类型
				indl.setIndlst(E_INDLST.YOUX);//负债利息明细状态
				indl.setIndxno(getIndexNo(dataItem.getAcctno(),dataItem.getIntrtp()));
				indl.setIneddt(trandt);//计息终止日期
				indl.setInstdt(dataItem.getInstdt());//计息开始日期
				indl.setIntrcd(dataItem.getIntrcd());//利率编号
				indl.setIntrdt(trandt);//计息日期
				indl.setIntrsq(CommTools.getBaseRunEnvs().getMain_trxn_seq());//计息流水
				indl.setIntrwy(dataItem.getIntrwy());//利率靠档方式
				indl.setLsinoc(E_INDLTP.UPIR);//上次利息操作代码
				indl.setLvamot(dataItem.getLvamot());
				indl.setLvindt(dataItem.getLvindt());
				indl.setLyinwy(dataItem.getLyinwy());
				indl.setTxbebs(dataItem.getTxbebs());
				indl.setIntrtp(dataItem.getIntrtp());
				try {
					KnbIndlDao.insert(indl);
				} catch (Exception e) {
					DpModuleError.DpstAcct.BNAS1735();
				}
				//修改负债账户计息明细
				DpDayEndDao.upAcinForSubByAcctno(dataItem.getAcctno(), lstrdt, lstrdt, BigDecimal.ZERO , E_INDLTP.UPIR, lstrdt,timetm);
			}
			
			//查询当前执行利率和基准利率
			//IntrPublicEntity entity = SysUtil.getInstance(IntrPublicEntity.class);
			IoPbIntrPublicEntity entity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
			entity.setCrcycd(dataItem.getCrcycd());
			entity.setIntrcd(dataItem.getIntrcd());
			entity.setIncdtp(dataItem.getIncdtp());
			entity.setTrandt(trandt);
			//entity.setDepttm(E_TERMCD.T000);
			entity.setDepttm(depttm);
			
			entity.setCorpno(dataItem.getCorpno());
			entity.setBrchno(brchno);
			
			entity.setLevety(dataItem.getLevety());
		/*	if(dataItem.getIntrdt() == E_INTRDT.OPEN){
				entity.setTrandt(dataItem.getOpendt());
				entity.setTrantm("999999");
			}*/
			
			pbpub.countInteresRate(entity);
			
			BigDecimal favovl = dataItem.getFavovl();
			BigDecimal favort = dataItem.getFavort();
			
			BigDecimal baseir = entity.getBaseir();
			BigDecimal intrvl = entity.getIntrvl();
			BigDecimal cuusin = intrvl.add(intrvl.multiply(favort.divide(BigDecimal.valueOf(100))).add(favovl));
			
			//mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
			//利率的最大范围值
			BigDecimal intrvlmax = entity.getBaseir().multiply(BigDecimal.ONE.add(entity.getFlmxsc().divide(BigDecimal.valueOf(100))));
			//利率的最小范围值
			BigDecimal intrvlmin = entity.getBaseir().multiply(BigDecimal.ONE.add(entity.getFlmnsc().divide(BigDecimal.valueOf(100))));
			
			if(CommUtil.compare(cuusin, intrvlmin)<0){
				cuusin = intrvlmin;
			}else if(CommUtil.compare(cuusin, intrvlmax)>0){
				cuusin = intrvlmax;
			}
			//mod by leipeng   优惠后判断时候超出基础浮动范围20170220  end--
			
			//修改账户利率信息表
			DpDayEndDao.upKubinrtForSubByAcctno(dataItem.getAcctno(), baseir, cuusin, entity.getFlirwy(), entity.getFlirvl(), entity.getFlirrt(),timetm);
			
			// 修改负债账户计息信息利率调整日期
			if (isblock == E_YES___.NO) {
				DpDayEndDao.upAcinLuindtByAcctno(dataItem.getAcctno(), lstrdt, timetm);
			}
			
			DpDayEndDao.delknbcbdlData(lstrdt, dataItem.getAcctno()); // 删除切日后的上日预计提数据
		}
		
//		CommTools.getBaseRunEnvs().setBusi_org_id(oldCorpno);
	}
		
	//循序号 和 明细序号
	private Long getDetlsq(String acctno, E_INTRTP intrtp) {
		//查询该账户信息是否存在该表中
		long count = 0;
		try {
			count = DpDayEndDao.getAcctnoIsInIndl(acctno, intrtp, true);
		} catch (Exception e) {
			return new Long(1);
		} 
		return count+1;
		
	}
	//明细序号 暂时都为1
	private Long getIndexNo(String acctno, E_INTRTP intrtp) {
		
		return new Long(1);
	}
	/**
	 * 获取数据遍历器。
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<KtpAdinType> getBatchDataWalker(cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitr.Input input, cn.sunline.ltts.busi.dptran.batchtran.dayend.intf.Subitr.Property property) {
//		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String lstrdt = CommTools.getBaseRunEnvs().getLast_date(); //上次交易日期
		String sql = "";
		Params params = new Params();
		//params.add("intrcd", dataItem.getIntrcd());
		//params.add("crcycd", dataItem.getCrcycd());
		params.add("currdt", lstrdt);
		params.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		/*if(CommUtil.equals(dataItem.getCorpno(), cnfrdm)){
			sql = DpDayEndDao.namedsql_selAdinTranDataNoCorp;
		}else{
			params.add("corpno", dataItem.getCorpno());
			sql = DpDayEndDao.namedsql_selAdinTranDataByCorp;
		}*/
		
		sql = DpDayEndDao.namedsql_selAdinTranDataNoCorp;
		
		//params.add("cnfrdm", cnfrdm);
		//System.out.println("<<===================:" + cnfrdm);
		//params.add("depttm", dataItem.getRfirtm());
		
		if(!property.getIsrun()){
			bizlog.debug("<<======当日无利率调整======>>");
			return null;
		}
		bizlog.debug("上次交易日期"+lstrdt);
		return new CursorBatchDataWalker<KtpAdinType>(sql, params);
	}
	

	
	
	@Override
	public void beforeTranProcess(String taskId, Input input,Property property) {
		String lstrdt = CommTools.getBaseRunEnvs().getLast_date();
		String bflsdt = DateTools2.getDateInfo().getBflsdt();
		
		property.setIsrun(false);
		
		//存放分档利率的代码
		
		//存放需要调整的浮动利率和基础利率的特征值(利率代码+存期+币种的字符串组合)
		Map<String, IntrSubSection> mapLstIntr = new HashMap<String, IntrSubSection>();
		
		//Options<IntrSubSection> lstIntrSubSection = new DefaultOptions<IntrSubSection>();
		
		String depttm = "";
		String intrcd = ""; //定义利率代码变量
		String corpno = "";
		property.setCnfrdm(BusiTools.getCenterCorpno());  //省中心法人代码
		String corpnos =  CommTools.getBaseRunEnvs().getBusi_org_id();
		List<IntrSubSection> lstSub = DpDayEndDao.selIntrByTrandt(lstrdt,bflsdt,corpnos,false);
		
		for(IntrSubSection sub : lstSub){
			
			if(sub.getIncdtp() == E_IRCDTP.LAYER){ //分档利率
				if(CommUtil.equals(intrcd, sub.getIntrcd())){
					continue;
				}else{
					property.getLstRlir().add(sub.getIntrcd());
					intrcd = sub.getIntrcd();
					
					property.setIsrun(true);
				}
				
			}else{
				if(CommUtil.equals(intrcd, sub.getIntrcd()) && CommUtil.equals(depttm, sub.getDepttm().getValue()) && CommUtil.equals(corpno, sub.getCorpno())){
					continue;
				} else {
					//lstIntrSubSection.add(sub);
					mapLstIntr.put(sub.getIntrcd() + sub.getDepttm().getValue() + sub.getCrcycd() + sub.getCorpno(), sub);
					property.getLstIntr().add(sub.getIntrcd() + sub.getDepttm().getValue() + sub.getCrcycd() + sub.getCorpno());
					intrcd = sub.getIntrcd();
					depttm = sub.getDepttm().getValue();
					corpno = sub.getCorpno();
					
					property.setIsrun(true);
				}
			}
		}
		
		bizlog.debug("<<======当日需要调整的利率代码======>>");
		for(String str : property.getLstRlir()){
			bizlog.debug("<<===当日需要调整的分档利率代码：[%s]===>>", str);
		}
		
		for(Map.Entry<String, IntrSubSection> entry : mapLstIntr.entrySet()){
			bizlog.debug("<<===当日需要调整的浮动和基础利率,利率代码:[%s],存期:[%s],法人:[%s]===>>", entry.getValue().getIntrcd(), 
					entry.getValue().getDepttm().getValue(), entry.getValue().getCorpno());
		}
		
	}
}


