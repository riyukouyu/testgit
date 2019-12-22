package cn.sunline.ltts.busi.dp.base;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.util.sequ.MsSeqUtil;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcctTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcctTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrchTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCustTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCustTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrplTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrplTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntrTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntrTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatuTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatuTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPoplTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPoplTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTermTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTermTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbUser;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbUserDao;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.pb.namedsql.intr.ProintrSelDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstAcct;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DRAWCT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRRTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MADTBY;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVBKAD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVBKLI;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVPLGN;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_AMNTWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BSINDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IBAMMD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INADTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INBEFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INEDSC;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRENTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_REPRWY;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TEARTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TIMEWY;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRCHLV;

public class DpPublic {

	private static final BizLog bizlog = BizLogUtil.getBizLog(DpPublic.class);

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2014年12月17日-下午1:04:59</li>
	 *         <li>功能说明：依据指定的日期、周期算得下个日期</li>
	 *         </p>
	 * @param trandt
	 *            当前交易日期
	 * @param nextdt
	 *            下次交易日期\
	 * @param freqcy
	 *            频率
	 * @return
	 */
	public static String getNextPeriod(String trandt, String nextdt, String freqcy) {

		String sNextDate = trandt;

		// 计算下次结息日期
		do {
		    /**
             * start
             * change by huangwh 20181009  注释计算下次日期为包含当期，添加计算下次日期不包含当期。
             */
//			sNextDate = DateTools2.calDateByFreq(sNextDate, freqcy);
	        sNextDate = DateTools2.calDateByNextFreq(sNextDate, freqcy);


			// 防止死循环
			if (CommUtil.compare(trandt, sNextDate) == 0) {
				throw DpModuleError.DpstAcct.BNAS1743( freqcy , sNextDate );
			}

			if (CommUtil.compare(sNextDate, nextdt) >= 0)
				break;
		}
		while (CommUtil.compare(sNextDate, nextdt) < 0);

		return sNextDate;
	}

	/**
	 * @Author v_mingyzhou
	 *         <p>
	 *         <li>2014年12月30日-下午1:04:59</li>
	 *         <li>功能说明：依据频率节点日期和频率算得上个日期</li>
	 *         </p>
	 * @param sPinlDate
	 *            频率点日期
	 * @param sZhouqplv
	 *            频率
	 * @return
	 */
	public static String getLastPeriod(String sPinlDate, String sZhouqplv) {

		/*
		 * 频率点日期(sPinlDate)是指处在频率节点上的日期, 比如活期的频率点日期就是每年3月21,, 6月21, 9月21日,
		 * 12月21日, 频率(sZhouqplv) 为1QA21E
		 */

		/* 最简洁的算法是先求上一年的第一天，再通过循环往后推算出目标值, 这样程序简单但若频率比较小则循环次数比较多, 故不采用 */

		// 先由频率节点日期求得下一个频率节点日期
		String sPinlDate_New = DateTools2.calDateByFreq(sPinlDate, sZhouqplv);

		// 计算两个频率节点之间的间隔天数
		int idays = DateTools2.calDays(sPinlDate, sPinlDate_New, 0, 0);

		// 每两个频率节点日期间的间隔天数不一定相等, 乘以双倍再往前推算
		String sTerm = -2 * idays + "D";

		String sTempDate = DateTools2.calDateByTerm(sPinlDate, sTerm);

		bizlog.parm("推前双倍天数后的日期", sTempDate);

		// 往后推一个频率
		sTempDate = DateTools2.calDateByFreq(sTempDate, sZhouqplv);

		String sLastDate = null;
		do {
			sLastDate = sTempDate;

			// 往后推一个频率判断是否可以退出循环
			sTempDate = DateTools2.calDateByFreq(sTempDate, sZhouqplv);

		}
		while (CommUtil.compare(sTempDate, sPinlDate) < 0);

		bizlog.parm("上个频率节点日期", sLastDate);

		return sLastDate;
	}
	
	/**
	 * 计算实际积数
	 * @param curram 当前积数
	 * @param onlnbl 账户余额
	 * @param currdt 当前日期
	 * @param lsamdt 积数变更日期
	 * @return 积数
	 * 
	 * 
	 * 计提/结息时由于积数是在账户余额变更时才变动，所有要计算出实际积数值
	 * 传入当前积数、账户余额、积数变更日期和当前日期
	 * 
	 * 
	 * 在账户余额变更时计算积数
	 * 传入当前积数、上日账户余额、积数变更日期和应入账日期
	 * 
	 */
	public static BigDecimal calRealTotalAmt(BigDecimal curram,BigDecimal onlnbl, String currdt,String lsamdt){

		//modify by chenlk  2016-12-1 删除积数不能为负数的校验
//		if(CommUtil.compare(curram, BigDecimal.ZERO) < 0)
//			throw DpModuleError.DpstProd.E0010("当前积数不能为负数");
		if(CommUtil.compare(onlnbl, BigDecimal.ZERO) < 0)
			onlnbl=BigDecimal.ZERO;
		
		BigDecimal days = new BigDecimal(DateTools2.calDays(lsamdt, currdt, 0, 0));
		if(CommUtil.compare(days, BigDecimal.ZERO) < 0)
			throw DpModuleError.DpstProd.BNAS1434(lsamdt,currdt);
		return curram.add(onlnbl.multiply(days));
	}
	
	public static BigDecimal getOnlnblByAcctno(String acctno){
		if(CommUtil.isNull(acctno)){
			throw DpModuleError.DpstAcct.BNAS0766();
		}
		BigDecimal onlnbl = BigDecimal.ZERO;
		try {
			onlnbl = DpAcctDao.selOnlnByAcctno(acctno, true).getOnlnbl();
		} catch (Exception e) {
			throw DpModuleError.DpstProd.BNAS1435(acctno);
		}
		return onlnbl;
	}

	/**
	 * 根据账户分类获取开户产品编号
	 * 
	 * @param accatp
	 *            账户分类
	 * @return prodcd 产品号
	 * 
	 */
	public static KnpParameter getProdcdByAccatp(E_ACCATP accatp) {
		KnpParameter para = SysUtil.getInstance(KnpParameter.class);
		//默认使用法人自己的开户产品。若自己无，则使用省级的开户产品 update by xieqq -20170714
		if (E_ACCATP.GLOBAL == accatp) {
			para = KnpParameterDao.selectOne_odb1("ACCATP", "GLOBAL", CommTools.getBaseRunEnvs().getBusi_org_id(), "%", false);
			if(CommUtil.isNull(para)){
				para = KnpParameterDao.selectOne_odb1("ACCATP", "GLOBAL", "%", "%", true);
			}
		} else if (E_ACCATP.FINANCE == accatp) {
			para = KnpParameterDao.selectOne_odb1("ACCATP", "FINANCE", CommTools.getBaseRunEnvs().getBusi_org_id(), "%",false);
			if(CommUtil.isNull(para)){
				para = KnpParameterDao.selectOne_odb1("ACCATP", "FINANCE", "%", "%",true);
			}
			
		} else if (E_ACCATP.WALLET == accatp) {
			para = KnpParameterDao.selectOne_odb1("ACCATP", "WALLET", CommTools.getBaseRunEnvs().getBusi_org_id(), "%", false);
			if(CommUtil.isNull(para)){
				para = KnpParameterDao.selectOne_odb1("ACCATP", "WALLET", "%", "%", true);
			}
			
		}
		return para;
	}
	
	
	/**
	 * 
	 * @param prodbi
	 * @param brchno
	 * @return
	 */
	public static String getModeno(String prodbi, String brchno) {

		String modeno = "";// 基础产品模板编号
		String sequence = "";// 序号

		sequence = BusiTools.getSequence("modeno_no", 6);
		modeno = new StringBuilder().append("JM").append(prodbi).append(brchno)
				.append(sequence).toString();

		return modeno;
	}
	
	/**
	 * 获取产品编号
	 * 
	 * @return 产品编号
	 */
	
	public static String getProdcd(E_BUSIBI busibi,E_DEBTTP debttp,String corpno) {
		int year = DateTools2.getYear(ConvertUtil.toDate(CommTools.getBaseRunEnvs().getTrxn_date()));
		
		// 获取录入编号
		StringBuilder bf = new StringBuilder();
		//TODO 产品编号J打头，未知哪里配置，待修改
		bf.append("J").append(corpno).append(year).append(busibi.getValue()).append(debttp.getValue());

		return MsSeqUtil.genSeq("PRODCD", bf.toString());
		
	}
	
	
	/**
	 * 
	 * @Description: (省级机构检查) 
	 * @author chenjk
	 * @return 
	 */	
	public static void provinceBrchCheck(String branch){
		
		if (CommUtil.isNull(branch)) {
			
			throw DpModuleError.DpstProd.BNAS1438();
		}
		
		// 获取机构信息
		IoBrchInfo cplKubBrch = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(branch);

		if(cplKubBrch.getBrchlv() != E_BRCHLV.PROV){
			throw DpModuleError.DpstProd.BNAS1439();
		}

		
	/*	// 检查是否省级机构
		if(cplKubBrch.getBrchlv() != E_BRCHLV.PROV){
			
			throw DpModuleError.DpstProd.E0010("该交易必须要省级产品配置操作岗维护！"); //产品编号为空判断
		}*/
		
	}
	/**
	 * 
	 * @Title: provinceUserCheck 
	 * @Description: 产品修改柜员只能为新增柜员
	 * @param prodcd
	 * @return
	 * @author liaojincai
	 * @date 2016年10月11日 下午7:56:30 
	 * @version V2.3.0
	 */
	public static void provinceUserCheck(E_BUSIBI busibi, String prodcd){
		
		// 查询产品新增柜员
		KupDppbUser tblkup_user = KupDppbUserDao.selectOne_odb(busibi, prodcd, E_PRTRTP.ADD, true);
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_teller(), tblkup_user.getTranus())) {
			throw DpModuleError.DpstProd.BNAS1440();
		}
		
	}
	/**
	 * 
	 * @Title: chkProdInfos 
	 * @Description: (产品录入提交校验) 
	 * @param prodcd 产品号
	 * @param proflag 场景标志，录入提交传“0”，复核调用传其他。
	 * @return
	 * @author leipeng
	 * @date 2016年7月20日 上午10:08:52 
	 * @version V2.3.0
	 */
	 
    @SuppressWarnings("unused")
	public static boolean chkProdInfos(String prodcd,String proflag) {
    	
    	E_BUSIBI busibi = E_BUSIBI.DEPO;//业务大类
    	
    	String sTime = CommTools.getBaseRunEnvs().getTrxn_date();// 交易日期
    	String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();// 交易日期
    	
    	String crcycd = null;//币种
    	
    	if(CommUtil.isNull(prodcd)){
    		if(CommUtil.equals(proflag, "0")){
    			throw DpModuleError.DpstProd.BNAS1054();
    		}else{
    			throw DpModuleError.DpstProd.BNAS1441();
    		}
    	}
    	
        //查询基础产品信息    	
    	KupDppbTemp seltemp = KupDppbTempDao.selectOne_odb1(prodcd, false);
    	
    	if(CommUtil.isNull(seltemp)){
    		throw DpModuleError.DpstProd.BNAS1442();
    		/*if(CommUtil.equals(proflag, "0")){
    			throw DpModuleError.DpstProd.BNAS1442();
    		}else{
    			throw DpModuleError.DpstProd.BNAS1442();
    		}*/
		}
    	
    	bizlog.debug("<----------------" + "基础属性校验开始"+"---------------->" );
    	//查看部件信息
    	KupDppbPartTemp selPartTemp = KupDppbPartTempDao.selectOne_odb1(busibi, prodcd, E_PARTCD._CK01, false);
    	
    	//启用标志为启用，未启用，不录入数据
    	if(selPartTemp.getPartfg() == E_YES___.YES){
			
			crcycd = seltemp.getPdcrcy();//币种
			
		    if (CommUtil.isNull(prodcd)) {
		    	if(CommUtil.equals(proflag, "0")){
		  	        throw DpModuleError.DpstProd.BNAS1054();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
		    }
			
		    if (CommUtil.isNull(seltemp.getProdcd())) {
		    	if(CommUtil.equals(proflag, "0")){
		  	        throw DpModuleError.DpstComm.BNAS1442();	
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
		    }
		  
		    if (CommUtil.isNull(seltemp.getProdtp())) {
		    	if(CommUtil.equals(proflag, "0")){
		  	        throw DpModuleError.DpstComm.BNAS0255();
    		    }else{
    		    	throw DpModuleError.DpstProd.BNAS1441();
	    		}
		    }
		  
		    if (CommUtil.isNull(seltemp.getPddpfg())) {
		    	if(CommUtil.equals(proflag, "0")){
		  	        throw DpModuleError.DpstComm.BNAS0258();	
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
		    }
		  
		    if (CommUtil.isNull(seltemp.getDebttp())) {
		    	if(CommUtil.equals(proflag, "0")){
		  	        throw DpModuleError.DpstComm.BNAS0262();	
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
		    }
		  
		    if (CommUtil.isNull(seltemp.getPdcrcy())) {
		    	if(CommUtil.equals(proflag, "0")){
		  	        throw DpModuleError.DpstComm.BNAS1443();	
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
		    }
		  
		    if (CommUtil.isNull(seltemp.getPrentp())) {
		    	if(CommUtil.equals(proflag, "0")){
		  	        throw DpModuleError.DpstComm.BNAS1444();	
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
		    }
	        
		    // 直接装配启用的产品协议文本代码不能为空，销售工厂启用的产品协议文本需为空
			if (E_PRENTP.ASSE == seltemp.getPrentp()) {
				if (CommUtil.isNull(seltemp.getProtno())) {
					if (CommUtil.equals(proflag, "0")) {
						throw DpModuleError.DpstComm.BNAS1445();
					} else {
						throw DpModuleError.DpstProd.BNAS1441();
					}
				}
			} else {
				// 获取机构信息
				IoBrchInfo cplKubBrch = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(CommTools.getBaseRunEnvs().getTrxn_branch());
                if(!cplKubBrch.getBrchlv().getValue().equals("1")){
                	throw DpModuleError.DpstComm.E9999("产品为销售工厂启用时，必须是省级机构！");
                }

				if (CommUtil.isNotNull(seltemp.getProtno())) {
					if (CommUtil.equals(proflag, "0")) {
						throw DpModuleError.DpstComm.BNAS1446();
					} else {
						throw DpModuleError.DpstProd.BNAS1441();
					}
				}
			}
	        
	        
//	        if (CommUtil.isNull(seltemp.getIsdrft())) {
//	        	if(CommUtil.equals(proflag, "0")){
//		  	        throw DpModuleError.DpstComm.E9999( "是否允许透支必输不得为空");	
//		    	}else{
//		    		throw DpModuleError.DpstProd.BNAS1441();
//	    		}
//	        }
	        
	        //产品启用方式为 直接装配启用时，生效日期、失效日期必须设置,机构控制标志必输
		    if(seltemp.getPrentp() == E_PRENTP.ASSE){
		    	if (CommUtil.isNull(seltemp.getBrchfg())) {
			    	if(CommUtil.equals(proflag, "0")){
			  	        throw DpModuleError.DpstComm.BNAS1447();	
			    	}else{
    		    		throw DpModuleError.DpstProd.BNAS1441();
		    		}
			    }
			    if (CommUtil.isNull(seltemp.getEfctdt())) {
			    	if(CommUtil.equals(proflag, "0")){
			  	        throw DpModuleError.DpstComm.BNAS1448();	
			    	}else{
    		    		throw DpModuleError.DpstProd.BNAS1441();
		    		}
			    }
			    if (CommUtil.isNull(seltemp.getInefdt())) {
			    	if(CommUtil.equals(proflag, "0")){
			  	        throw DpModuleError.DpstComm.BNAS1449();
			    	}else{
    		    		throw DpModuleError.DpstProd.BNAS1441();
		    		}
			    }
			    if (DateUtil.compareDate(seltemp.getInefdt(), seltemp.getEfctdt()) <= 0) {
			    	if(CommUtil.equals(proflag, "0")){
				        throw DpModuleError.DpstComm.BNAS1450();
			    	}else{
    		    		throw DpModuleError.DpstProd.BNAS1441();
		    		}
			    }
		    
			    if (DateUtil.compareDate(seltemp.getEfctdt(), sTime) <= 0) {
			    	if(CommUtil.equals(proflag, "0")){
				        throw DpModuleError.DpstComm.BNAS1451();
			    	}else{
    		    		throw DpModuleError.DpstProd.BNAS1441();
		    		}
			    }
		    }
		    
		    //产品启用方式为 销售工厂启用时，生效日期、失效日期不允许设置
	        if (seltemp.getPrentp() == E_PRENTP.SALE) {
	        	
		        if (CommUtil.isNotNull(seltemp.getEfctdt())) {
		        	if(CommUtil.equals(proflag, "0")){
				        throw DpModuleError.DpstComm.BNAS1452();	
			    	}else{
    		    		throw DpModuleError.DpstProd.BNAS1441();
		    		}
		        }
		        if (CommUtil.isNotNull(seltemp.getInefdt())) {
		        	if(CommUtil.equals(proflag, "0")){
				        throw DpModuleError.DpstComm.BNAS1453();	
			    	}else{
    		    		throw DpModuleError.DpstProd.BNAS1441();
		    		}
		        }
	        	
				
			}
		    //判断业务小类和业务中类是否匹配
		    String testa = seltemp.getPddpfg().getValue().substring(0, 2);
		  
		    if (!CommUtil.equals(testa, seltemp.getProdtp().getValue())) {
		    	if(CommUtil.equals(proflag, "0")){
		    	    throw DpModuleError.DpstComm.BNAS1454();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
			};
		  
			//判断业务小类和业务细类是否匹配
		    String testb = seltemp.getDebttp().getValue().substring(0, 4);
		  
		    if (!CommUtil.equals(testb, seltemp.getPddpfg().getValue())) {
		    	if(CommUtil.equals(proflag, "0")){
		    	    throw DpModuleError.DpstComm.BNAS1455();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
			};
			
			//当产品新增为1-通用产品时 ，产品启用方式	
			if (seltemp.getProdtg() == E_PRODTG.CURREN) {
				 if (CommUtil.isNull(seltemp.getProdtg())) {
					 if(CommUtil.equals(proflag, "0")){
			    	     throw DpModuleError.DpstComm.BNAS1456();
			    	 }else{
			    		 throw DpModuleError.DpstProd.BNAS1441();
		    		 }
				}
			}
			
			//产品启用方式1-直接装配启用时，产品性质只能选1-通用产品
			if (E_PRENTP.ASSE == seltemp.getPrentp()) {
				if (E_PRODTG.CURREN != seltemp.getProdtg() ) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1457();
					}else{
			    		 throw DpModuleError.DpstProd.BNAS1441();
		    		 }
				}
			}
			
			// 产品启用方式为2-销售工厂启用时，产品性质置灰
			if (E_PRENTP.SALE == seltemp.getPrentp()) {
				if (CommUtil.isNotNull(seltemp.getProdtg())) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1458();
					}else{
			    		 throw DpModuleError.DpstProd.BNAS1441();
		    		 }
				}
			}
			
			//产品启用方式自能选择1-直接装配启用时，机构控制标志必选
			if (seltemp.getPrentp() == E_PRENTP.ASSE) {
			    if (CommUtil.isNull(seltemp.getBrchfg())) {
			    	if(CommUtil.equals(proflag, "0")){
		  	            throw DpModuleError.DpstComm.BNAS1459();	
			    	}else{
    		    		throw DpModuleError.DpstProd.BNAS1441();
		    		}
	            }	
			}
			
			//产品启用方式自能选择2-销售工厂启用时，机构控制标志不可选
			if (seltemp.getPrentp() == E_PRENTP.SALE) {
			    if (CommUtil.isNotNull(seltemp.getBrchfg())) {
			    	if(CommUtil.equals(proflag, "0")){
		  	            throw DpModuleError.DpstComm.BNAS1460();
			    	}else{
    		    		throw DpModuleError.DpstProd.BNAS1441();
		    		}
		        }	
			}
		}else{
			if(CommUtil.isNotNull(seltemp)){
				if(CommUtil.equals(proflag, "0")){
	  	            throw DpModuleError.DpstComm.BNAS1461();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
			}
		}	
    	
    	bizlog.debug("<----------------" + "基础属性校验结束"+"---------------->" );
    	
    	bizlog.debug("<----------------" + "存款机构控制部件校验开始"+"---------------->" );
    	
    	
    	//查看部件信息
    	KupDppbPartTemp selPartBrch = KupDppbPartTempDao.selectOne_odb1(busibi, prodcd, E_PARTCD._CK02, false);
    		
    	//查询产品机构控制信息
    	List<KupDppbBrchTemp> selBrchTemp = DpProductDao.selKupDppbBrchTempByProdcd(prodcd, false);
    	
    	//启用标志为未启用，不录入数据
		if(selPartBrch.getPartfg() == E_YES___.YES){
			
			if (CommUtil.isNull(selBrchTemp)) {
				if (CommUtil.isNotNull(seltemp.getBrchfg())){
					if(CommUtil.equals(proflag, "0")){
			  	        throw DpModuleError.DpstComm.BNAS1462();	
					}else{
			    		throw DpModuleError.DpstProd.BNAS1441();
		    		}
				}
				
		    }
			
			for(KupDppbBrchTemp brchtemp : selBrchTemp){
				//启用方式选择“2-销售工厂启用”时，机构号应为空
				if(CommUtil.isNotNull(seltemp.getPrentp())){
					if(seltemp.getPrentp() == E_PRENTP.SALE){
						if (CommUtil.isNotNull(brchtemp.getBrchno())) {
							if(CommUtil.equals(proflag, "0")){
					  	        throw DpModuleError.DpstComm.BNAS1463();	
							}else{
		    		    		throw DpModuleError.DpstProd.BNAS1441();
		    	    		}
					    }
					}
				}
				
				if (CommUtil.isNull(brchtemp.getProdcd())) {
					if(CommUtil.equals(proflag, "0")){
			  	        throw DpModuleError.DpstComm.BNAS1464();
					}else{
    		    		throw DpModuleError.DpstProd.BNAS1441();
    	    		}
			    }
				
				if (CommUtil.isNull(brchtemp.getBrchno())) {
					if(CommUtil.equals(proflag, "0")){
			  	        throw DpModuleError.DpstComm.BNAS1465();	
					}else{
    		    		throw DpModuleError.DpstProd.BNAS1441();
    	    		}
			    }
				
				if (CommUtil.isNull(brchtemp.getCrcycd())) {
					if(CommUtil.equals(proflag, "0")){
			  	        throw DpModuleError.DpstComm.BNAS1466();	
					}else{
    		    		throw DpModuleError.DpstProd.BNAS1441();
    	    		}
			    }
			}
		}else{
			if(CommUtil.isNotNull(selBrchTemp)){
				if(CommUtil.equals(proflag, "0")){
	  	            throw DpModuleError.DpstComm.BNAS1467();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
			}
		}
    	
    	bizlog.debug("<----------------" + "存款机构控制部件校验结束"+"---------------->" );
    	
    	bizlog.debug("<----------------" + "存款开户控制部件校验开始"+"---------------->" );
    	
    	
    	//查看部件信息
    	KupDppbPartTemp selPartCust = KupDppbPartTempDao.selectOne_odb1(busibi, prodcd, E_PARTCD._CK03, false);
    			
    	//查询开户控制表信息
    	KupDppbCustTemp selCustTemp = KupDppbCustTempDao.selectOne_odb1(prodcd, crcycd, false);
    	
    	
		if(selPartCust.getPartfg() == E_YES___.YES){
			//查询存期控制信息
			List<KupDppbTermTemp> selTermTemp = KupDppbTermTempDao.selectAll_odb3(prodcd, crcycd, false);
			
			if(CommUtil.isNull(selCustTemp)){
				if(CommUtil.equals(proflag, "0")){
				    throw DpModuleError.DpstComm.BNAS1468();	
				}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
			}
			
//			if(CommUtil.isNull(selTermTemp)){
//				if(CommUtil.equals(proflag, "0")){
//				    throw DpModuleError.DpstComm.E9999( "存期控制信息不存在");	
//				}else{
//		    		throw DpModuleError.DpstProd.BNAS1441();
//	    		}
//			}
			
			if (CommUtil.isNull(selCustTemp.getProdcd())) {
				if(CommUtil.equals(proflag, "0")){
		       	    throw DpModuleError.DpstComm.BNAS1469();
				}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
		    }
			
		    if (CommUtil.isNull(selCustTemp.getMadtby())) {
		    	if(CommUtil.equals(proflag, "0")){
		       	    throw DpModuleError.DpstComm.BNAS1470();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
		    }
			// 业务小类为活期时，到期日确定方式只能为无到期日
			if (E_FCFLAG.CURRENT == seltemp.getPddpfg()) {
				if (E_MADTBY.NO != selCustTemp.getMadtby()) {
					if (CommUtil.equals(proflag, "0")) {
						throw DpModuleError.DpstComm.BNAS1471();
					} else {
						throw DpModuleError.DpstProd.BNAS1441();
					}
				}
			}
			
		    if (CommUtil.isNull(selCustTemp.getOnlyfg())) {
		    	if(CommUtil.equals(proflag, "0")){
		       	    throw DpModuleError.DpstComm.BNAS1472();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
		    }
		    
		    if (CommUtil.compare(selCustTemp.getSrdpam(), BigDecimal.ZERO) < 0) {
		    	if(CommUtil.equals(proflag, "0")){
		       	    throw DpModuleError.DpstComm.BNAS1473();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
		    }
		    
		    if (CommUtil.isNull(selCustTemp.getStepvl())) {
		    	if(CommUtil.equals(proflag, "0")){
		       	    throw DpModuleError.DpstComm.BNAS1474();	
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
		    }
		    
			for(KupDppbTermTemp termInfo : selTermTemp){
			    //当属性为 无到期日时，存期只能为 活期或空
			   /* if (selCustTemp.getMadtby() == E_MADTBY.NO) {
			   	  if (termInfo.getDepttm() != E_TERMCD.T000 && CommUtil.isNull(termInfo.getDepttm())) {
    			   		if(CommUtil.equals(proflag, "0")){
    				        throw DpModuleError.DpstComm.E9999( "存期当属性为无到期日时，存期只能为活期或空");
    			   		}else{
        		    		throw DpModuleError.DpstProd.BNAS1441();
    	    		    }
				    }
			     }
			    
			    //当属性为 指定到期日时，存期只能为 空
			    if (selCustTemp.getMadtby() == E_MADTBY.SET) {
			   	    if (CommUtil.isNotNull(termInfo.getDepttm())) {
			   	    	if(CommUtil.equals(proflag, "0")){
				            throw DpModuleError.DpstComm.E9999( "存期必当属性为 指定到期日时，存期只能为 空");
			   	    	}else{
        		    		throw DpModuleError.DpstProd.BNAS1441();
    	    		    }
				    }
			    }*/
			    
			    //当属性为根据开户日计算到期日或者根据首次存入日计算到期日时，存期可选 活期除外的其他存期
			    if (selCustTemp.getMadtby() == E_MADTBY.T_OR_S
			   		 || selCustTemp.getMadtby() == E_MADTBY.TERMCD) {
			   	 
			   	    if (termInfo.getDepttm() == E_TERMCD.T000
			   			 || CommUtil.isNull(termInfo.getDepttm())) {
			   		    if(CommUtil.equals(proflag, "0")){
				        	throw DpModuleError.DpstComm.BNAS1475();	
			   		    }else{
        		    		throw DpModuleError.DpstProd.BNAS1441();
    	    		    }
			   		}
			    }
			    
//			    if (CommUtil.isNull(termInfo.getDepttm())) {
//			    	if(CommUtil.equals(proflag, "0")){
//			       	    throw DpModuleError.DpstComm.E9999( "存期必输不得为空");	
//			    	}else{
//    		    		throw DpModuleError.DpstProd.BNAS1441();
//	    		    }
//			    }
			    
			  //判断当存期为自定义时 需要录入具体的天数
			    String testdatea = termInfo.getDepttm().getValue().substring(0, 2);
			    String testdateb = "90";
			    if (CommUtil.equals(testdatea,testdateb)){
			   	     if (CommUtil.isNull(termInfo.getDeptdy())) {
			   	    	if(CommUtil.equals(proflag, "0")){
				            throw DpModuleError.DpstComm.BNAS1476();
			   	    	}else{
        		    		throw DpModuleError.DpstProd.BNAS1441();
    	    		    }
				     }	    	 
			    } else {
			   	     if (CommUtil.isNotNull(termInfo.getDeptdy())&& CommUtil.compare(termInfo.getDeptdy(), 0l) != 0) {
			   	    	 if(CommUtil.equals(proflag, "0")){
			   	    	      throw DpModuleError.DpstComm.BNAS1477();	
			   	    	 }else{
        		    		  throw DpModuleError.DpstProd.BNAS1441();
    	    		     }
				     }	
				}
			}
		
		}else{
			if(CommUtil.isNotNull(selCustTemp)){
				if(CommUtil.equals(proflag, "0")){
	  	            throw DpModuleError.DpstComm.BNAS1478();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
			}
		}
	
        bizlog.debug("<----------------" + "存款开户控制部件校验结束"+"---------------->" );
        
        
        bizlog.debug("<----------------" + "存款存入控制部件校验开始"+"---------------->" );
        
        
        //查看部件信息
      	KupDppbPartTemp selPartPost = KupDppbPartTempDao.selectOne_odb1(busibi, prodcd, E_PARTCD._CK04, false);
      			
      	//查询产品存入控制临时表
      	KupDppbPostTemp selPostTemp = KupDppbPostTempDao.selectOne_odb1(prodcd, crcycd, false);
      	
  		if(selPartPost.getPartfg() == E_YES___.YES){
			    
			    if (CommUtil.isNull(selPostTemp)) {
			    	if(CommUtil.equals(proflag, "0")){
			    		 throw DpModuleError.DpstComm.BNAS1479();
			    	}else{
			    		 throw DpModuleError.DpstProd.BNAS1441();
				    }
				}
				// 产品编号
				if (CommUtil.isNull(selPostTemp.getProdcd())) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1480();
					}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
			
				// 存入控制方式
				if (CommUtil.isNull(selPostTemp.getPosttp())) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1481();
					}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
				
				// 是否明细汇总
				if (CommUtil.isNull(selPostTemp.getDetlfg())) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1482();
					}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
				
			if (seltemp.getPddpfg() == E_FCFLAG.CURRENT && selPostTemp.getDetlfg() != E_YES___.NO) {
				if (CommUtil.equals(proflag, "0")) {
					throw DpModuleError.DpstComm.BNAS1483();
				} else {
					throw DpModuleError.DpstProd.BNAS1441();
				}
			}
				
				// 账户留存最大余额
				if (CommUtil.isNotNull(selPostTemp.getMaxibl())) {
					if (CommUtil.compare(selPostTemp.getMaxibl(), BigDecimal.ZERO) < 0) {
						throw DpModuleError.DpstComm.BNAS1484();
					}
				}
				
				/*// 存入控制方式为有条件允许存入，存入控制方法不能为空
				if (selPostTemp.getPosttp() == E_SAVECT.COND) {
			
					if (CommUtil.isNull(selPostTemp.getPostwy())) {// 存入控制方法不能为空
						if(CommUtil.equals(proflag, "0")){
							throw DpModuleError.DpstComm.E9999("存入控制方法不能为空");
						}else{
   			    		    throw DpModuleError.DpstProd.BNAS1441();
   				        }
					} else if (CommUtil.isNotNull(selPostTemp.getPostwy())) {// 存入控制方法不为空
						// 存入控制方法为金额控制
						if (selPostTemp.getPostwy() == E_POSTWY.AMCL) {
							ckamntwy(selPostTemp.getAmntwy(), (BigDecimal)selPostTemp.getMiniam(), (BigDecimal)selPostTemp.getMaxiam(),proflag);// 存入控制方法为金额控制时检查
							// 存入次数控制应为空
							if (CommUtil.isNotNull(selPostTemp.getTimewy())
									|| (CommUtil.compare(selPostTemp.getMinitm(),0l) != 0 && CommUtil.isNotNull(selPostTemp.getMinitm()))
									|| (CommUtil.compare(selPostTemp.getMaxitm(),0l) != 0 && CommUtil.isNotNull(selPostTemp.getMaxitm()))) {
								if(CommUtil.equals(proflag, "0")){
									throw DpModuleError.DpstComm.E9999("存入控制方法为金额控制，存入次数控制应为空");
								}else{
	       			    		    throw DpModuleError.DpstProd.BNAS1441();
	       				        }
							}
			
							// 存入控制方法为次数控制
						} else if (selPostTemp.getPostwy() == E_POSTWY.TMCL) {
							cktimewy(selPostTemp.getTimewy(), selPostTemp.getMinitm(), selPostTemp.getMaxitm(),proflag);// 存入控制方法为次数控制时检查
			
							//支取金额控制应为空
							if (CommUtil.isNotNull(selPostTemp.getAmntwy())
									|| (!CommUtil.equals(selPostTemp.getMiniam(),BigDecimal.ZERO)&&CommUtil.isNotNull(selPostTemp.getMiniam()))
									|| (!CommUtil.equals(selPostTemp.getMaxiam(),BigDecimal.ZERO)&&CommUtil.isNotNull(selPostTemp.getMaxiam()))) {
								if(CommUtil.equals(proflag, "0")){
									throw DpModuleError.DpstComm.E9999("存入控制方法为次数控制，存入金额控制应为空");
								}else{
	       			    		    throw DpModuleError.DpstProd.BNAS1441();
	       				        }
							}
			
							// 存入控制方法为金额和次数控制
						} else if (selPostTemp.getPostwy() == E_POSTWY.TMCL) {
							ckamntwy(selPostTemp.getAmntwy(), (BigDecimal)selPostTemp.getMiniam(), (BigDecimal)selPostTemp.getMaxiam(),proflag);// 对金额控制检查
							cktimewy(selPostTemp.getTimewy(), selPostTemp.getMinitm(), selPostTemp.getMaxitm(),proflag);// 对次数控制检查
						}
					}
			
					// 存入控制方式为无条件允许存入时条件为空
				} else if (selPostTemp.getPosttp() == E_SAVECT.YES) {
					if (CommUtil.isNotNull(selPostTemp.getPostwy()) || CommUtil.isNotNull(selPostTemp.getAmntwy())
							|| (!CommUtil.equals(selPostTemp.getMiniam() , BigDecimal.ZERO ) && CommUtil.isNotNull(selPostTemp.getMiniam())) 
							|| (!CommUtil.equals(selPostTemp.getMaxiam() , BigDecimal.ZERO )&& CommUtil.isNotNull(selPostTemp.getMaxiam()))
							|| CommUtil.isNotNull(selPostTemp.getTimewy()) 
							|| (CommUtil.compare(selPostTemp.getMinitm(), 0l) != 0 && CommUtil.isNotNull(selPostTemp.getMinitm()))
							|| (CommUtil.compare(selPostTemp.getMaxitm(), 0l) != 0 &&CommUtil.isNotNull(selPostTemp.getMaxitm()))) {
						if(CommUtil.equals(proflag, "0")){
							throw DpModuleError.DpstComm.E9999("存入控制方式为无条件允许存入，其他条件应为空");
						}else{
   			    		    throw DpModuleError.DpstProd.BNAS1441();
   				        }
					}
				}*/
	  	}else{
	  		if(CommUtil.isNotNull(selPostTemp)){
				if(CommUtil.equals(proflag, "0")){
	  	            throw DpModuleError.DpstComm.BNAS1485();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
			}
		}
	
      	
    	bizlog.debug("<----------------" + "存款存入控制部件校验结束"+"---------------->" );
    	
    	
    	bizlog.debug("<----------------" + "存款存入计划控制部件校验开始"+"---------------->" );
    	
    	KupDppbPartTemp selPartPopl = KupDppbPartTempDao.selectOne_odb1(busibi, prodcd, E_PARTCD._CK05, false);
    		
    	//查询存入计划表
    	KupDppbPoplTemp selPoplTemp = KupDppbPoplTempDao.selectOne_odb1(prodcd, crcycd, false);
      	
		if(selPartPopl.getPartfg() == E_YES___.YES){
			
			if (CommUtil.isNull(selPoplTemp)) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1486();
				}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			// 产品编号
			if (CommUtil.isNull(selPoplTemp.getProdcd())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1480();
				}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
		
			// 存入计划生成方式
			if (CommUtil.isNull(selPoplTemp.getGentwy())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1487();
				}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
		
			// 存入计划生成方式按周期分
			if (selPoplTemp.getGentwy() == E_SVPLGN.T1) {
		        
				// 当存入计划生成方式按周期分时，存入计划生成周期不能为空
				if (CommUtil.isNull(selPoplTemp.getPlanpd())) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1488();
					}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
		
				// 存入计划生成方式按计划分
			} else if (selPoplTemp.getGentwy() == E_SVPLGN.T2) {
		
				// 当存入计划生成方式按周期分时，存入计划生成周期不能为空
				if (CommUtil.isNotNull(selPoplTemp.getPlanpd())) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1489();
					}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
			}
		
			// 存入漏补方式
			if (CommUtil.isNull(selPoplTemp.getSvlewy())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1490();
				}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			} else {
		
				// 存入漏补方式为控制漏补次数时，最大补足次数必输
				if (selPoplTemp.getSvlewy() == E_SVBKAD.COUNT) {
					if (CommUtil.isNull(selPoplTemp.getMaxisp())) {
						if(CommUtil.equals(proflag, "0")){
							throw DpModuleError.DpstComm.BNAS1491();
						}else{
			    		    throw DpModuleError.DpstProd.BNAS1441();
				        }
					}
				}else {
					
					// 存入漏补方式为控制漏补次数时，最大补足次数必空
					if (!CommUtil.equals(selPoplTemp.getMaxisp().toString(), "0") && CommUtil.isNotNull(selPoplTemp.getMaxisp())) {
						if(CommUtil.equals(proflag, "0")){
							throw DpModuleError.DpstComm.BNAS1492();
						}else{
			    		    throw DpModuleError.DpstProd.BNAS1441();
				        }
					}
				}
			}
		
			// 存入违约标准
			if (CommUtil.isNull(selPoplTemp.getDfltsd())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1493();
				}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			} else {
		
				// 当存入违约标准为大于漏存次数时，漏存次数必输
				if (selPoplTemp.getDfltsd() == E_SVBKLI.COUNT) {
					if (CommUtil.isNull(selPoplTemp.getSvletm())) {
						if(CommUtil.equals(proflag, "0")){
							throw DpModuleError.DpstComm.BNAS1494();
						}else{
			    		    throw DpModuleError.DpstProd.BNAS1441();
				        }
					}
				} else {
		
					// 当存入违约标准为大于漏存次数时，漏存次数必空
					if (!CommUtil.equals(selPoplTemp.getSvletm().toString(), "0") && CommUtil.isNotNull(selPoplTemp.getSvletm())) {
						if(CommUtil.equals(proflag, "0")){
							throw DpModuleError.DpstComm.BNAS1495();
						}else{
			    		    throw DpModuleError.DpstProd.BNAS1441();
				        }
					}
				}
			}
		
			// 存入违约处理方式
			if (CommUtil.isNull(selPoplTemp.getDfltwy())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1002();
				}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
		
			// 存入计划控制方式
			if (CommUtil.isNull(selPoplTemp.getPscrwy())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1017();
				}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
	  	}else{
	  		if(CommUtil.isNotNull(selPoplTemp)){
				if(CommUtil.equals(proflag, "0")){
	  	            throw DpModuleError.DpstComm.BNAS1496();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
			}
		}
      	
    	bizlog.debug("<----------------" + "存款存入计划控制部件校验结束"+"---------------->" );
    	
    	
    	bizlog.debug("<----------------" + "存款支取控制校验开始"+"---------------->" );
    	
    	KupDppbPartTemp selPartDraw = KupDppbPartTempDao.selectOne_odb1(busibi, prodcd, E_PARTCD._CK06, false);
    	
    	//查询支取控制信息
    	KupDppbDrawTemp selDrawTemp = KupDppbDrawTempDao.selectOne_odb1(prodcd, crcycd, false);
      
  		if(selPartDraw.getPartfg() == E_YES___.YES){
			
			if (CommUtil.isNull(selDrawTemp)) {
				if(CommUtil.equals(proflag, "0")){
	    		    throw DpModuleError.DpstComm.BNAS1497();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			// 支取控制方式
			if (CommUtil.isNull(selDrawTemp.getDrawtp())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1498();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
		
//			// 支取规则
//			if (CommUtil.isNull(selDrawTemp.getDrrule())) {
//				if(CommUtil.equals(proflag, "0")){
//					throw DpModuleError.DpstComm.E9999("支取规则不能为空");
//	    		}else{
//	    		    throw DpModuleError.DpstProd.BNAS1441();
//		        }
//			}
			
			// 产品存入控制临时表
			KupDppbPostTemp tblKup = KupDppbPostTempDao.selectOne_odb1(prodcd,crcycd, false);
			if (CommUtil.isNull(tblKup)) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1499();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			if(tblKup.getDetlfg()==E_YES___.YES && CommUtil.isNull(selDrawTemp.getDrrule())){
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1500();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			if(tblKup.getDetlfg()==E_YES___.NO && CommUtil.isNotNull(selDrawTemp.getDrrule())){
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1501();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
		
			// 账户留存最小余额
			if (CommUtil.isNotNull(selDrawTemp.getMinibl())){
				
			}
		
			// 是否允许小于账户留存最小余额
			if (CommUtil.isNull(selDrawTemp.getIsmibl())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1502();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			if (CommUtil.compare(selDrawTemp.getMinibl(), BigDecimal.ZERO) == 0 && E_YES___.NO != selDrawTemp.getIsmibl()) {
				if (CommUtil.equals(proflag, "0")) {
					throw DpModuleError.DpstComm
							.BNAS1503();
				} else {
					throw DpModuleError.DpstProd.BNAS1441();
				}
			}
		
			// 支取控制方式为有条件允许支取，支取控制方法不能为空
			if (selDrawTemp.getDrawtp() == E_DRAWCT.COND) {
		
				if (CommUtil.isNull(selDrawTemp.getCtrlwy())) {// 支取控制方法不能为空
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1504();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				} else if (CommUtil.isNotNull(selDrawTemp.getCtrlwy())) {// 支取控制方法不为空
		
					/*// 支取控制方法为金额控制
					if (selDrawTemp.getCtrlwy() == E_CTRLWY.AMCL) {
						ckdramwy(selDrawTemp.getDramwy(), (BigDecimal)selDrawTemp.getDrmiam(), (BigDecimal)selDrawTemp.getDrmxam(),proflag);// 支取控制方法为金额控制时检查
		
						//支取次数控制应为空
						if (CommUtil.isNotNull(selDrawTemp.getDrtmwy())
								|| (CommUtil.compare(selDrawTemp.getDrmitm(), 0l) != 0 && CommUtil.isNotNull(selDrawTemp.getDrmitm()))
								|| (CommUtil.compare(selDrawTemp.getDrmitm(), 0l) != 0 && CommUtil.isNotNull(selDrawTemp.getDrmxtm()))) {
							if(CommUtil.equals(proflag, "0")){
								throw DpModuleError.DpstComm.E9999("支取控制方法为金额控制，支取次数控制应为空");
	        	    		}else{
	        	    		    throw DpModuleError.DpstProd.BNAS1441();
	        		        }
						}
		
						// 支取控制方法为次数控制
					} else if (selDrawTemp.getCtrlwy() == E_CTRLWY.TMCL) {
						ckdrtmwy(selDrawTemp.getDrtmwy(), selDrawTemp.getDrmitm(), selDrawTemp.getDrmxtm(),proflag);// 支取控制方法为次数控制时检查
		
						//支取金额控制应为空
						if (CommUtil.isNotNull(selDrawTemp.getDramwy())
								|| (!CommUtil.equals(selDrawTemp.getDrmiam(),BigDecimal.ZERO) && CommUtil.isNotNull(selDrawTemp.getDrmiam()))
								|| (!CommUtil.equals(selDrawTemp.getDrmxam(),BigDecimal.ZERO) && CommUtil.isNotNull(selDrawTemp.getDrmxam()))) {
							if(CommUtil.equals(proflag, "0")){
								throw DpModuleError.DpstComm.E9999("支取控制方法为次数控制，支取金额控制应为空");
	        	    		}else{
	        	    		    throw DpModuleError.DpstProd.BNAS1441();
	        		        }
						}
		
						// 支取控制方法为金额和次数控制
					} else if (selDrawTemp.getCtrlwy() == E_CTRLWY.TMCL) {
						ckdramwy(selDrawTemp.getDramwy(), (BigDecimal)selDrawTemp.getDrmiam(), (BigDecimal)selDrawTemp.getDrmxam(),proflag);// 对金额控制检查
						ckdrtmwy(selDrawTemp.getDrtmwy(), selDrawTemp.getDrmitm(), selDrawTemp.getDrmxtm(),proflag);// 对次数控制检查
					}*/
				}
		
				// 支取控制方式为无条件允许支取时条件为空
			} /*else if (selDrawTemp.getDrawtp() == E_DRAWCT.YES) {
				if (CommUtil.isNotNull(selDrawTemp.getCtrlwy()) || CommUtil.isNotNull(selDrawTemp.getDramwy())
						|| (!CommUtil.equals(selDrawTemp.getDrmiam(), BigDecimal.ZERO) && CommUtil.isNotNull(selDrawTemp.getDrmiam())) 
						|| (!CommUtil.equals(selDrawTemp.getDrmiam(), BigDecimal.ZERO) && CommUtil.isNotNull(selDrawTemp.getDrmxam()))
						|| CommUtil.isNotNull(selDrawTemp.getDrtmwy()) 
						|| (CommUtil.compare(selDrawTemp.getDrmitm(), 0l) != 0 && CommUtil.isNotNull(selDrawTemp.getDrmitm()))
						|| (CommUtil.compare(selDrawTemp.getDrmxtm(), 0l) != 0 && CommUtil.isNotNull(selDrawTemp.getDrmxtm()))) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.E9999("支取控制方式为无条件允许支取，其他条件应为空");
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
			}*/
	  	}else{
	  		if(CommUtil.isNotNull(selDrawTemp)){
				if(CommUtil.equals(proflag, "0")){
	  	            throw DpModuleError.DpstComm.BNAS1505();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
			}
		}
      	
    	bizlog.debug("<----------------" + "存款支取控制校验结束"+"---------------->" );
    	
    	bizlog.debug("<----------------" + "存款支取计划控制校验开始"+"---------------->" );
    	
    	//查看部件信息
        KupDppbPartTemp selPartDrpl = KupDppbPartTempDao.selectOne_odb1(busibi, prodcd, E_PARTCD._CK07, false);
    	
        KupDppbDrplTemp selDrplTemp = KupDppbDrplTempDao.selectOne_odb1(prodcd, crcycd, false);
  		
        if(selPartDrpl.getPartfg() == E_YES___.YES){
			
			if (CommUtil.isNull(selDrplTemp)) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1506();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			// 产品编号
			if (CommUtil.isNull(selDrplTemp.getProdcd())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1480();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
		
			// 支取计划生成方式
			if (CommUtil.isNull(selDrplTemp.getDradwy())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS0128();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			} else {
		
				// 当支取计划生成方式定义为按周期均分时，支取计划生成周期必输
				if (selDrplTemp.getDradwy() == E_SVPLGN.T1) {
					if (CommUtil.isNull(selDrplTemp.getGendpd())) {
						if(CommUtil.equals(proflag, "0")){
	    					throw DpModuleError.DpstComm.BNAS1507();
	    	    		}else{
	    	    		    throw DpModuleError.DpstProd.BNAS1441();
	    		        }
					}
					
				// 当支取计划生成方式定义为按计划分时，支取计划生成周期必空
				}else if (selDrplTemp.getDradwy() == E_SVPLGN.T2) {
					if (CommUtil.isNotNull(selDrplTemp.getGendpd())) {
						if(CommUtil.equals(proflag, "0")){
	    					throw DpModuleError.DpstComm.BNAS1508();
	    	    		}else{
	    	    		    throw DpModuleError.DpstProd.BNAS1441();
	    		        }
					}
				}
			}
		
			// 产品计划控制方式
			if (CommUtil.isNull(selDrplTemp.getDrcrwy())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1509();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
		
			// 支取违约标准
			if (CommUtil.isNull(selDrplTemp.getDrdfsd())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS0116();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
		
			// 支取违约处理方式
			if (CommUtil.isNull(selDrplTemp.getDrdfwy())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS0114();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
		
			// 支取时结息处理方式
			if (CommUtil.isNull(selDrplTemp.getBeinfg())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1510();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
	  	}else{
	  		if(CommUtil.isNotNull(selDrplTemp)){
				if(CommUtil.equals(proflag, "0")){
	  	            throw DpModuleError.DpstComm.BNAS1505();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
			}
		}
      	
    	bizlog.debug("<----------------" + "存款支取计划控制校验结束"+"---------------->" );
    	
    	bizlog.debug("<----------------" + "到期控制部件校验开始"+"---------------->" );
    	
    	//查看部件信息
        KupDppbPartTemp selPartMatu = KupDppbPartTempDao.selectOne_odb1(busibi, prodcd, E_PARTCD._CK08, false);
    	
        //查询产品到期控制信息
        KupDppbMatuTemp selMatuTemp = KupDppbMatuTempDao.selectOne_odb1(prodcd, crcycd, false);
      	
  		if(selPartMatu.getPartfg() == E_YES___.YES){
			
			if (CommUtil.isNull(selMatuTemp)) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1511();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			// 传入值检查
			if (CommUtil.isNull(selMatuTemp.getProdcd())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstProd.BNAS1054();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			if (CommUtil.isNull(selMatuTemp.getFestdl())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1512();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			if (CommUtil.isNull(selMatuTemp.getDelyfg())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1513();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			//当该属性选择1-是时，到期宽限期为必输项。
			if (selMatuTemp.getDelyfg() == E_YES___.YES) {
				if (CommUtil.isNull(selMatuTemp.getMatupd())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1514();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
			}
		
			if (CommUtil.isNull(selMatuTemp.getTrdpfg())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1515();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			//当该属性选择1-允许时，是否可以更换转存产品号、转存次数、转存方式和转存利率调整方式为必输项。
			if (selMatuTemp.getTrdpfg() == E_YES___.YES) {
				if (CommUtil.isNull(selMatuTemp.getTrpdfg())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1516();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				
				if (CommUtil.isNull(selMatuTemp.getTrsvtp())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1517();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				
				if (CommUtil.isNull(selMatuTemp.getTrinwy())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1518();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				
				if (CommUtil.isNull(selMatuTemp.getTrintm())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1519();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
			}
			
			//该属性选择1-可以时，转存金融基础产品编号为必输项
			if (selMatuTemp.getTrpdfg() == E_YES___.YES) {
				if (CommUtil.isNull(selMatuTemp.getTrprod())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1520();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
			}
	  	}else{
	  		if(CommUtil.isNotNull(selMatuTemp)){
				if(CommUtil.equals(proflag, "0")){
	  	            throw DpModuleError.DpstComm.BNAS1521();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
			}
		}
      	
    	bizlog.debug("<----------------" + "到期控制部件校验结束"+"---------------->" );
    	
    	
    	bizlog.debug("<----------------" + "存款利息利率部件校验开始"+"---------------->" );
    	
    	//查看部件信息
        KupDppbPartTemp selPartIntr = KupDppbPartTempDao.selectOne_odb1(busibi, prodcd, E_PARTCD._CK09, false);
    	
        //查询存款利息利率信息
      	KupDppbIntrTemp selIntrTemp = KupDppbIntrTempDao.selectOne_odb1(prodcd, crcycd, false);
      		
  	      if(CommUtil.equals(selPartIntr.getPartfg().getValue(), E_YES___.YES.getValue())){
			
			if (CommUtil.isNull(selIntrTemp)) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1522();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			// 传入值检查
			if (CommUtil.isNull(selIntrTemp.getProdcd())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstProd.BNAS1054();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			if (CommUtil.isNull(selIntrTemp.getIntrtp())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS0473();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
		    
			if (CommUtil.isNull(selIntrTemp.getInbefg())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS0647();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			if (CommUtil.isNull(selIntrTemp.getTxbefg())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS0649();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			if (selIntrTemp.getTxbefg() == E_YES___.YES) {
				if (CommUtil.isNull(selIntrTemp.getTaxecd())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1245();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				
				//查询利率代码定义
				long count = ProintrSelDao.intxcodSelPb(selIntrTemp.getTaxecd(), CommTools.getBaseRunEnvs().getBusi_org_id(), false);
				
				if(count <= 0){
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1523();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
			}
			
			if (CommUtil.isNull(selIntrTemp.getTxbebs())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1524();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			if (CommUtil.isNull(selIntrTemp.getHutxfg())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS0374();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			//当该属性选择1-计息时，计息频率必选设置。
			if (selIntrTemp.getInbefg() == E_INBEFG.INBE) {
				if (CommUtil.isNull(selIntrTemp.getTebehz())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS0640();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
//				if (CommUtil.isNull(selIntrTemp.getTxbefr())) {
//					if(CommUtil.equals(proflag, "0")){
//    					throw DpModuleError.DpstComm.E9027("结息频率");
//    	    		}else{
//    	    		    throw DpModuleError.DpstProd.BNAS1441();
//    		        }
//				}
			}
			
			if (CommUtil.isNull(selIntrTemp.getIncdtp())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS0487();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			if (CommUtil.isNull(selIntrTemp.getIntrcd())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS0490();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
//			// 检查基础利率是否是否存在
//			if (E_IRCDTP.Reference == selIntrTemp.getIncdtp()) {
//				SysUtil.getInstance(IoIntrSvrType.class).selRfirByrfircd(brchno, selIntrTemp.getIntrcd());
//				
//			// 检查浮动利率是否是否存在
//			} else if (E_IRCDTP.BASE == selIntrTemp.getIncdtp()) {
//				SysUtil.getInstance(IoIntrSvrType.class).selBkirByintrcd(brchno, selIntrTemp.getIntrcd());
//				
//			// 检查分档利率是否是否存在
//			} else if (E_IRCDTP.LAYER == selIntrTemp.getIncdtp()) {
//				SysUtil.getInstance(IoIntrSvrType.class).SelRlirByintrcd(brchno, selIntrTemp.getIntrcd());
//				
//			} else {
//				throw DpModuleError.DpstComm.E9999("违约利率代码类型有误");
//			}
			
			//分档利率，当选择3-分档利率代码时，需要选择设置利率靠档标志
			if (E_IRCDTP.LAYER== selIntrTemp.getIncdtp()) {
				if (CommUtil.isNull(selIntrTemp.getInwytp())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1525();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
			}
			
			
			//该属性选择1-是时，利率靠档方式必选，分层计息方式、是否登记分层明细和分层明细积数调整方式不可设置。
			if (selIntrTemp.getInwytp() == E_YES___.YES) {
				if (CommUtil.isNull(selIntrTemp.getIntrwy())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1525();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				if (CommUtil.isNotNull(selIntrTemp.getLyinwy())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1526();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				if (CommUtil.isNotNull(selIntrTemp.getIsrgdt())) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1527();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				if (CommUtil.isNotNull(selIntrTemp.getLydttp())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1528();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
			}
			
			//选择0-否时，利率靠档方式不设置，分层计息方式、是否登记分层明细需要设置。
			if (selIntrTemp.getInwytp() == E_YES___.NO) {
				if (CommUtil.isNotNull(selIntrTemp.getIntrwy())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1529();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				if (CommUtil.isNull(selIntrTemp.getLyinwy())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1530();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				if (CommUtil.isNull(selIntrTemp.getIsrgdt())) {
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS0353();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
			}
			//该属性选择1-是时，利率靠档方式必选，分层计息方式、是否登记分层明细和分层明细积数调整方式不可设置。选择0-否时，利率靠档方式不设置，分层计息方式、是否登记分层明细需要设置。
			if(CommUtil.isNotNull(selIntrTemp.getInwytp())){
				if (selIntrTemp.getInwytp() == E_YES___.NO) {
					if (CommUtil.isNull(selIntrTemp.getLyinwy())) {
						if(CommUtil.equals(proflag, "0")){
        					throw DpModuleError.DpstComm.BNAS1530();
        	    		}else{
        	    		    throw DpModuleError.DpstProd.BNAS1441();
        		        }
					}
					if (CommUtil.isNull(selIntrTemp.getIsrgdt())) {
						if(CommUtil.equals(proflag, "0")){
        					throw DpModuleError.DpstComm.BNAS0353();
        	    		}else{
        	    		    throw DpModuleError.DpstProd.BNAS1441();
        		        }
					}
				}
			}
			if (E_YES___.YES == selIntrTemp.getIsrgdt()) {
				if (CommUtil.isNull(selIntrTemp.getLydttp())) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS0774();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
			}
			
			
			if (CommUtil.isNull(selIntrTemp.getInammd())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS0642();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			/*if (CommUtil.isNull(selIntrTemp.getBldyca())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.E9027("平均余额天数计算方式");
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			if (CommUtil.isNull(selIntrTemp.getCycltp())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.E9027("周期类型");
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}*/
			
			if (CommUtil.isNull(selIntrTemp.getInprwy())) {
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1531();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			/**
			 * 1.业务小类为活期时，利率重订价方式只能选不重订价、参考利率变化日重订价、按指定周期重订价
			 * 2.业务小类为定期时，不控制
			 */
			if (E_FCFLAG.CURRENT == seltemp.getPddpfg()) {
				if (E_IRRTTP.NO != selIntrTemp.getInprwy()
						&& E_IRRTTP.CK != selIntrTemp.getInprwy()
						&& E_IRRTTP.AZ != selIntrTemp.getInprwy()) {
					if (CommUtil.equals(proflag, "0")) {
						throw DpModuleError.DpstComm.E9999("利率重定价方式选择错误，请重新选项");
					} else {
						throw DpModuleError.DpstProd.BNAS1441();
					}
				}
			}
			
//			if (E_FCFLAG.FIX == seltemp.getPddpfg()) {
//				if (E_IRRTTP.NO != selIntrTemp.getInprwy()
//						&& E_IRRTTP.MT != selIntrTemp.getInprwy()
//						&& E_IRRTTP.QD != selIntrTemp.getInprwy()) {
//					if (CommUtil.equals(proflag, "0")) {
//						throw DpModuleError.DpstComm.E9999("利率重定价方式选择错误，请重新选项");
//					} else {
//						throw DpModuleError.DpstProd.BNAS1441();
//					}
//				}
//			}
			
			//该属性选择1-账户余额时，平均余额天数计算方式和周期类型置灰。该属性选择2-平均余额时，平均余额天数计算方式和周期类型为必选项。
			if(selIntrTemp.getInammd() == E_IBAMMD.AVG){
				if (CommUtil.isNull(selIntrTemp.getBldyca())) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.E9027("平均余额天数计算方式");
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
				
//				if (CommUtil.isNull(selIntrTemp.getCycltp())) {
//					if(CommUtil.equals(proflag, "0")){
//						throw DpModuleError.DpstComm.E9027("周期类型");
//		    		}else{
//		    		    throw DpModuleError.DpstProd.BNAS1441();
//			        }
//				}
			}else{
				if (CommUtil.isNotNull(selIntrTemp.getBldyca())) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.E9999("平均余额天数计算方式不可输");
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
				
//				if (CommUtil.isNotNull(selIntrTemp.getCycltp())) {
//					if(CommUtil.equals(proflag, "0")){
//						throw DpModuleError.DpstComm.E9999("周期类型不可输");
//		    		}else{
//		    		    throw DpModuleError.DpstProd.BNAS1441();
//			        }
//				}
			}
			
		   /*该属性选择1-不重定价时，重定价利率处理方式不可选。
			*当该属性选择2-参考利率变化日重定价或3-按指定周期重定价时，重定价利率处理方式可选择1-后段调整处理或2-全部调整处理。
			*该属性选择4-到期转存重定价时，重定价利率处理方式只能选择1-后段调整处理,2-全部调整处理。
			*该属性选择5-升息比较重定价时，重定价利率处理方式只能选择3-前后段分别调整处理
			*/
			if ((selIntrTemp.getInprwy() == E_IRRTTP.CK) || (selIntrTemp.getInprwy() == E_IRRTTP.AZ)) {
				if(selIntrTemp.getReprwy() == E_REPRWY.PART){
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.E9999("重定价利率处理方式选择错误");
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
			}else if(selIntrTemp.getInprwy() == E_IRRTTP.QD){
				if(selIntrTemp.getReprwy() != E_REPRWY.BACK  && selIntrTemp.getReprwy() != E_REPRWY.ALL){
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.E9999("重定价利率处理方式选择错误");
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
			}else if(selIntrTemp.getInprwy() == E_IRRTTP.MT){
				if(selIntrTemp.getReprwy() != E_REPRWY.PART){
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.E9999("重定价利率处理方式选择错误");
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
			}
	  	}else{
	  		if(CommUtil.isNotNull(selIntrTemp)){
				if(CommUtil.equals(proflag, "0")){
	  	            throw DpModuleError.DpstComm.E9999( "存款利息利率部件未启用,无需录入数据");
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
			}
		}
      	
    	bizlog.debug("<----------------" + "存款利息利率部件校验结束"+"---------------->" );
    	
    	
    	bizlog.debug("<----------------" + "存款违约支取利息利率部件校验开始"+"---------------->" );
    	//查看部件信息
        KupDppbPartTemp selPartDfir = KupDppbPartTempDao.selectOne_odb1(busibi, prodcd, E_PARTCD._CK10, false);
    	
        //查询存款违约支取利息利率部件信息
        List<KupDppbDfirTemp> selDfirTemp = KupDppbDfirTempDao.selectAll_odb5(prodcd, crcycd, false);
      	
  		if(selPartDfir.getPartfg() == E_YES___.YES){
			
			if(CommUtil.isNull(selDfirTemp)){
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1532();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			for(KupDppbDfirTemp dfirInfo : selDfirTemp){
				
				if(CommUtil.isNull(dfirInfo.getProdcd())){
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1480();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				
				if(CommUtil.isNull(dfirInfo.getTeartp())){
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1218();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				
				if(CommUtil.isNull(dfirInfo.getBsincd())){
					if(CommUtil.equals(proflag, "0")){
    					throw DpModuleError.DpstComm.BNAS1533();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				
				if(CommUtil.isNull(dfirInfo.getBsinrl())){
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1534();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				
				if(CommUtil.isNull(dfirInfo.getInadtp())){
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1221();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				
				if(CommUtil.isNull(dfirInfo.getInsrwy())){
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1535();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				
				if(CommUtil.isNull(dfirInfo.getBsinam())){
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1536();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				
				if(CommUtil.isNull(dfirInfo.getBsindt())){
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1226();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				
				if(CommUtil.isNull(dfirInfo.getInedsc())){
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1225();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				
				if(CommUtil.isNull(dfirInfo.getDrdein())){
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1537();
    	    		}else{
    	    		    throw DpModuleError.DpstProd.BNAS1441();
    		        }
				}
				
				if(CommUtil.isNotNull(dfirInfo.getDrintp())){
					//当违约支取利息类型为提前销户，提前支取，超期支取时，违约利息调整类型只能为全额调整
					if((dfirInfo.getTeartp() == E_TEARTP.OVTM) 
						|| (dfirInfo.getTeartp() == E_TEARTP.TQXH)
							|| (dfirInfo.getTeartp() == E_TEARTP.ZDLC)){
								
						if(dfirInfo.getInadtp()!= E_INADTP.QETZ){
							throw DpModuleError.DpstComm.BNAS1538();
						}
					}
					//当违约支取利息类型为超期支取，起始日来源只能选择到期日，终止日来源只能选择当前交易日
					if(dfirInfo.getTeartp() != E_TEARTP.OVTM){
						if(dfirInfo.getBsindt() == E_BSINDT.TMDT){
							throw DpModuleError.DpstComm.BNAS1539();
						}
						if(dfirInfo.getInedsc() != E_INEDSC.DQJY){
							throw DpModuleError.DpstComm.BNAS1540();
						}
					}
				}
			}
	  	}else{
	  		if(CommUtil.isNotNull(selDfirTemp)){
				if(CommUtil.equals(proflag, "0")){
	  	            throw DpModuleError.DpstComm.BNAS1541();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
			}
		}
      	
    	bizlog.debug("<----------------" + "存款违约支取利息利率部件校验结束"+"---------------->" );
    	
    	
    	bizlog.debug("<----------------" + "存款核算部件校开始"+"---------------->" );
    	
    	//查看部件信息
        KupDppbPartTemp selPartAcct = KupDppbPartTempDao.selectOne_odb1(busibi, prodcd, E_PARTCD._CK11, false);
       
        //查询存款核算部件信息
        List<KupDppbAcctTemp> selAcctTemp = KupDppbAcctTempDao.selectAll_odb2(prodcd, false);
        
       
    	
  		if(selPartAcct.getPartfg() == E_YES___.YES){
			
			if(CommUtil.isNull(selAcctTemp)){
				if(CommUtil.equals(proflag, "0")){
					throw DpModuleError.DpstComm.BNAS1542();
	    		}else{
	    		    throw DpModuleError.DpstProd.BNAS1441();
		        }
			}
			
			 // 核算代码
	        String acctcd = selAcctTemp.get(0).getAcctcd();
			//删除所有存取
			KupDppbAcctTempDao.delete_odb2(prodcd);
			
			//查询所有存期信息
			List<KupDppbTermTemp> selTermTemp = KupDppbTermTempDao.selectAll_odb3(prodcd, crcycd, false);
			
			KupDppbAcctTemp termInfos = null;
			KupDppbTermTemp terpInfos = SysUtil.getInstance(KupDppbTermTemp.class);
			
			if (CommUtil.isNotNull(selTermTemp)) {
				// 循环存期表
				for (KupDppbTermTemp termInfo : selTermTemp) {
					termInfos = KupDppbAcctTempDao.selectOne_odb1(prodcd, termInfo.getDepttm(), false);
					if (CommUtil.isNull(termInfos)) {// 如果核算部件表中无数据，就要添加
						termInfos = SysUtil.getInstance(KupDppbAcctTemp.class);
						termInfos.setProdcd(prodcd);
						termInfos.setDepttm(termInfo.getDepttm());
						termInfos.setAcctcd(acctcd);
						KupDppbAcctTempDao.insert(termInfos);
					}
				}
				
			} else {
				// 没有存期默认为全部
				termInfos = SysUtil.getInstance(KupDppbAcctTemp.class);
				termInfos.setProdcd(prodcd);
				termInfos.setDepttm(E_TERMCD.ALL);
				termInfos.setAcctcd(acctcd);
				KupDppbAcctTempDao.insert(termInfos);
			}
	  	}else{
	  		if(CommUtil.isNotNull(selAcctTemp)){
				if(CommUtil.equals(proflag, "0")){
	  	            throw DpModuleError.DpstComm.BNAS1543();
		    	}else{
		    		throw DpModuleError.DpstProd.BNAS1441();
	    		}
			}
		}
      	
    	bizlog.debug("<----------------" + "存款核算部件校结束"+"---------------->" );
    	
    	return true;
	}
    /**
	 * 
	 * @Title: cktimewy
	 * @Description: 存入控制方法为次数控制时检查
	 * @param timewy
	 *            存入次数控制方式
	 * @param minitm
	 *            最小存入次数
	 * @param maxitm
	 *            最大存入次数
	 * @author zhangjunlei
	 * @date 2016年7月13日 下午4:11:48
	 * @version V2.3.0
	 */
	public static void cktimewy(E_TIMEWY timewy, long minitm, long maxitm,String proflag) {

		// 存入次数控制方式不能为空
		if (CommUtil.isNull(timewy)) {
			if(CommUtil.equals(proflag, "0")){
				throw DpModuleError.DpstComm.BNAS1544();
    		}else{
    		    throw DpModuleError.DpstProd.BNAS1441();
	        }
		} else if (CommUtil.isNotNull(timewy)) {// 存入金额控制方式不为空

			if (timewy == E_TIMEWY.MNTM) {// 存入次数控制方式为控制最小次数

				// 最小存入次数不能为空
				if (CommUtil.isNull(minitm)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1545();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

				// 最大存入次数应为空
				if (CommUtil.isNotNull(maxitm)&&CommUtil.compare(maxitm, 0l)!=0) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1546();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

			} else if (timewy == E_TIMEWY.MXTM) {// 存入次数控制方式为控制最大次数

				// 最大存入次数不能为空
				if (CommUtil.isNull(maxitm)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1547();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

				// 最小存入次数应为空
				if (CommUtil.isNotNull(minitm)&&CommUtil.compare(minitm, 0l)!=0) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1548();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

			} else if (timewy == E_TIMEWY.SCTM) {// 存入金额控制方式为控制次数范围

				// 最小存入次数不能为空
				if (CommUtil.isNull(minitm)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1549();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

				// 最大存入次数不能为空
				if (CommUtil.isNull(maxitm)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1550();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

				// 最小存入次数不能大于最大存入次数
				if (CommUtil.compare(minitm, maxitm) > 0) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1551();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
			}
		}
	}
	/**
	 * 
	 * @Title: ckamntwy
	 * @Description: 存入控制方法为金额控制时检查
	 * @param amntwy
	 *            存入金额控制方式
	 * @param miniam
	 *            单次存入最小金额
	 * @param maxiam
	 *            单次存入最大金额
	 * @author zhangjunlei
	 * @date 2016年7月13日 下午4:04:12
	 * @version V2.3.0
	 */
	public static void ckamntwy(E_AMNTWY amntwy, BigDecimal miniam,
			BigDecimal maxiam,String proflag) {

		if (CommUtil.isNull(amntwy)) {// 存入金额控制方式不能为空
			if(CommUtil.equals(proflag, "0")){
				throw DpModuleError.DpstComm.BNAS1552();
    		}else{
    		    throw DpModuleError.DpstProd.BNAS1441();
	        }
		} else if (CommUtil.isNotNull(amntwy)) {// 存入金额控制方式不为空

			if (amntwy == E_AMNTWY.MNAC) {// 存入金额控制方式为控制最小金额

				// 单次存入最小金额不能为空
				if (CommUtil.isNull(miniam)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1553();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

				// 单次存入最大金额应为空
				if (!CommUtil.equals(maxiam, BigDecimal.ZERO) && CommUtil.isNotNull(maxiam)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1554();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

			} else if (amntwy == E_AMNTWY.MXAC) {// 存入金额控制方式为控制最大金额

				// 单次存入最大金额不能为空
				if (CommUtil.isNull(maxiam)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1555();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

				// 单次存入最小金额应为空
				if (!CommUtil.equals(maxiam, BigDecimal.ZERO) && CommUtil.isNotNull(miniam)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1556();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

			} else if (amntwy == E_AMNTWY.SCAC) {// 存入金额控制方式为控制金额范围

				// 单次存入最小金额不能为空
				if (CommUtil.isNull(miniam)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1557();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

				// 单次存入最大金额不能为空
				if (CommUtil.isNull(maxiam)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1558();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

				// 单次存入最小金额不能大于单次存入最大金额
				if (CommUtil.compare(miniam, maxiam) > 0) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1559();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
			}
		}
	}
	/**
	 * 
	 * @Title: ckdramwy
	 * @Description: 支取控制方法为金额控制时检查
	 * @param dramwy
	 *            支取金额控制方式
	 * @param drmiam
	 *            单次支取最小金额
	 * @param drmxam
	 *            单次支取最大金额
	 * @author zhangjunlei
	 * @date 2016年7月14日 上午9:19:33
	 * @version V2.3.0
	 */
	public static void ckdramwy(E_AMNTWY dramwy, BigDecimal drmiam,
			BigDecimal drmxam,String proflag) {

		if (CommUtil.isNull(dramwy)) {// 支取金额控制方式不能为空
			if(CommUtil.equals(proflag, "0")){
				throw DpModuleError.DpstComm.BNAS1560();
    		}else{
    		    throw DpModuleError.DpstProd.BNAS1441();
	        }
		} else if (CommUtil.isNotNull(dramwy)) {// 支取金额控制方式不为空

			if (dramwy == E_AMNTWY.MNAC) {// 支取金额控制方式为控制最小金额

				// 单次支取最小金额不能为空
				if (CommUtil.isNull(drmiam)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1561();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
				
				// 单次支取最大金额应为空
				if (!CommUtil.equals(drmxam, BigDecimal.ZERO) && CommUtil.isNotNull(drmxam)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1562();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

			} else if (dramwy == E_AMNTWY.MXAC) {// 支取金额控制方式为控制最大金额

				// 单次支取最大金额不能为空
				if (CommUtil.isNull(drmxam)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1563();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
				
				// 单次支取最小金额应为空
				if (!CommUtil.equals(drmiam, BigDecimal.ZERO) && CommUtil.isNotNull(drmiam)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1564();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
				
			} else if (dramwy == E_AMNTWY.SCAC) {// 支取金额控制方式为控制金额范围

				// 单次支取最小金额不能为空
				if (!CommUtil.equals(drmiam, BigDecimal.ZERO) && CommUtil.isNull(drmiam)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1565();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

				// 单次支取最大金额不能为空
				if (!CommUtil.equals(drmxam, BigDecimal.ZERO) && CommUtil.isNull(drmxam)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1566();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

				// 单次支取最小金额不能大于单次支取最大金额
				if (CommUtil.compare(drmiam, drmxam) > 0) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1567();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
			}
		}
	}

	/**
	 * 
	 * @Title: ckdrtmwy
	 * @Description: 支取控制方法为次数控制时检查
	 * @param drtmwy
	 *            支取次数控制方式
	 * @param drmitm
	 *            最小支取次数
	 * @param drmxtm
	 *            最大支取次数
	 * @author zhangjunlei
	 * @date 2016年7月14日 上午9:21:16
	 * @version V2.3.0
	 */
	public static void ckdrtmwy(E_TIMEWY drtmwy, Long drmitm, Long drmxtm,String proflag) {

		// 支取次数控制方式不能为空
		if (CommUtil.isNull(drtmwy)) {
			if(CommUtil.equals(proflag, "0")){
				throw DpModuleError.DpstComm.BNAS1568();
    		}else{
    		    throw DpModuleError.DpstProd.BNAS1441();
	        }
		} else if (CommUtil.isNotNull(drtmwy)) {// 支取金额控制方式不为空

			if (drtmwy == E_TIMEWY.MNTM) {// 支取次数控制方式为控制最小次数

				// 最小支取次数不能为空
				if (CommUtil.isNull(drmitm)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1569();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
				
				// 最大支取次数应为空
				if (CommUtil.isNotNull(drmxtm)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1570();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

			} else if (drtmwy == E_TIMEWY.MXTM) {// 支取次数控制方式为控制最大次数

				// 最大支取次数不能为空
				if (CommUtil.isNull(drmxtm)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1571();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
				
				// 最小支取次数应为空
				if (CommUtil.isNotNull(drmitm) && drmitm != 0) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1572();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

			} else if (drtmwy == E_TIMEWY.SCTM) {// 支取金额控制方式为控制次数范围

				// 最小支取次数不能为空
				if (CommUtil.isNull(drmitm)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1573();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

				// 最大支取次数不能为空
				if (CommUtil.isNull(drmxtm)) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1574();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}

				// 最小支取次数不能大于最大支取次数
				if (CommUtil.compare(drmitm, drmxtm) > 0) {
					if(CommUtil.equals(proflag, "0")){
						throw DpModuleError.DpstComm.BNAS1575();
		    		}else{
		    		    throw DpModuleError.DpstProd.BNAS1441();
			        }
				}
			}
		}
	}

	/**
	 * 
	 * @Title: getProdTyinno 
	 * @Description: (生成15位（机构号3位+年份日期8位+序号4位）) 
	 * 每日自动初始化序号
	 * @return tyinno 录入编号
	 * @author liaojincai
	 * @date 2016年11月29日 下午3:44:27 
	 * @version V2.3.0
	 */
	public static String getProdTyinno(){
		
		return MsSeqUtil.genSeq("PRODTY", CommTools.getBaseRunEnvs().getBusi_org_id() + CommTools.getBaseRunEnvs().getTrxn_date());
		
	}
	
	/**
	 * @Description：查询redis上账户可用余额
	 * @author Xiaoyu Luo
	 * @param acctno 活期负债结算账号
	 * @return 返回活期结算账户可用余额
	 */
	public static BigDecimal getAvaiam(String acctno){
		//TODO redis可用余额查询
		return BigDecimal.ZERO;
	}
	
    public static String parseTimestamp(String timestamp) {
        Date date = DateUtil.parseDate(timestamp.substring(0, 18), "yyyyMMddHHmmss.SSS");
        return DateUtil.formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String parseTime(String time) {
        Date date = DateUtil.parseDate(time, "HH:mm:ss SSS");
        return DateUtil.formatDate(date, "HH:mm:ss");
    }
}
