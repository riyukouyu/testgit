package cn.sunline.ltts.busi.dptran.trans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.dp.acct.DpAcctProc;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.dayend.DpInterest;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcin;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbAcinDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbdl;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbCbdlDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctProdDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDetl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacMatu;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacMatuDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProd;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacProdDao;
import cn.sunline.ltts.busi.dp.type.DpInterestType.DpInstPrcIn;
//import cn.sunline.ltts.busi.dptran.trans.intf.Qrbank.Property;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
//import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.selAcctnoInfo.Output;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbInterestRate;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpDpAssetsQry;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSign;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaSignDetl;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaSelAcctno;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaAssetInfoList;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaClientLedgerOut;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaPdtlInfoList;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaProdInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbIntrPublicEntity;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.PbError.Intr;
//import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AVBLDT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CAINPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_INDLTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRRTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CYCLTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IBAMMD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrbank {

public static void qryTranInfo( final cn.sunline.ltts.busi.dptran.trans.intf.Qrbank.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrbank.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrbank.Output output){
	String cardno = input.getCardno();
	if(CommUtil.isNull(cardno)){
		throw DpModuleError.DpstComm.BNAS0541();
	}
	IoCaKnaAcdc tblacdc = SysUtil.getInstance(IoCaSevQryTableInfo.class)
			.getKnaAcdcOdb2(cardno, true);
	
	if(tblacdc.getStatus() ==  E_DPACST.CLOSE){
		throw DpModuleError.DpstComm.BNAS0428();
	}
	//查询电子账户分类
	E_ACCATP eAccatp = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
			.qryAccatpByCustac(tblacdc.getCustac());
	if(eAccatp == E_ACCATP.WALLET){
		throw DpModuleError.DpstAcct.BNAS0813();
	}
	
	//电子账户状态字检查
	String custac = tblacdc.getCustac();
	IoDpAcStatusWord cplAcStatus = SysUtil.getInstance(IoDpFrozSvcType.class).getAcStatusWord(custac);
	if(E_YES___.YES == cplAcStatus.getClstop()){
		throw DpModuleError.DpstComm.BNAS0440();
	}
	//电子账户状态检查
	E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).selCaStInfo(custac);
	if (CommUtil.isNotNull(cuacst)) {
		
		if (cuacst == E_CUACST.CLOSED) { // 销户
			throw DpModuleError.DpstComm.BNAS0894();

		} else if (cuacst == E_CUACST.OUTAGE) { // 停用
			throw DpModuleError.DpstComm.BNAS0895();

		} else if (cuacst == E_CUACST.PREOPEN) { // 预开户
			throw DpModuleError.DpstComm.BNAS0893();
		}
	} else {
		throw DpModuleError.DpstComm.BNAS1206();
	}
	
	
	//分页查询页码、页容量设置
	int pageno = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_start());//页码
	int pgsize = ConvertUtil.toInteger(CommTools.getBaseRunEnvs().getPage_size());//页容量
	
	property.setPageno(pageno);//页码
	property.setPagesize(pgsize);//页容量
}

public static void qrdeal( final cn.sunline.ltts.busi.dptran.trans.intf.Qrbank.Input input,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrbank.Property property,  final cn.sunline.ltts.busi.dptran.trans.intf.Qrbank.Output output){

    long totlCount = 0; // 记录总数
    //int startno = (pageno - 1) * pgsize;// 起始记录数
    long startno = (property.getPageno() - 1L) * property.getPagesize();// 起始记录数
    // 获取输入数据
    String cardno = input.getCardno();// 电子账号
    String acctno = input.getAcctno();// 负债账号
    String prodcd = input.getProdcd();// 产品编号

    // 电子账号
    if (CommUtil.isNull(cardno)) {
        throw DpModuleError.DpstAcct.BNAS0311();
    }

    // 当前页码
    if (CommUtil.isNull(property.getPageno())) {//
        throw CaError.Eacct.BNAS0977();
    }

    // 页容量
    if (CommUtil.isNull(property.getPagesize())) {//
        throw CaError.Eacct.BNAS0463();
    }
	/**
	 * 电子账户客户端分户账户查询服务实现
	 * 
	 */
	IoCaClientLedgerOut infoOut = SysUtil.getInstance(IoCaClientLedgerOut.class);
	
	// 查询信息
 			infoOut = CaTools.qryClientLedgerByCustac(cardno);
 			
 		//获取活期金额	
 			BigDecimal onlnbl = BigDecimal.ZERO;// 活期产品当前余额
 			onlnbl = infoOut.getOnlnbl();
 			List<IoCaAssetInfoList> acctList = infoOut.getAcctInfoList();
 		//获取电子账户资产信息列表
 			for(IoCaAssetInfoList acct : acctList){
 				//获取资产产品类型列表
 				List<IoCaPdtlInfoList> acct1 =	acct.getPdtlInfoList();
 				for(IoCaPdtlInfoList acct2 :acct1){
 					//获取产品信息列表
 					List<IoCaProdInfo>  acct3 = acct2.getProdInfoLIst();
 					for(IoCaProdInfo acct4 : acct3){
 						if(acct4.getProdtp().equals("111")){
 						onlnbl=	acct4.getOnlnbl();//活期产品当前余额
 						}
 					}
 					
 				}
 				
 			}
 			
    long pagesize = property.getPagesize();
    // 根据cardno，acctno，prodcd查询负债账号和电子账号对照信息,结果集为output
    cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo.selAcctnoInfo.Output selact = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
            .selAcctnoInfo(cardno, acctno, prodcd, input.getPddpfg(), input.getSprdid(), input.getSprdna(),
                    startno,pagesize, totlCount);

    // 将获取的结果集由output转换为option
    Options<IoCaSelAcctno> results = new DefaultOptions<IoCaSelAcctno>();
    results = selact.getSelact();
System.err.println("电子账户关联负债账户信息   =  "+infoOut.toString());
    // 将结果集由option转换为list
    List<IoCaSelAcctno> kna_accs = new ArrayList<IoCaSelAcctno>();
    kna_accs = results.getValues();
    // 初始化变量
    BigDecimal curbal = BigDecimal.ZERO;// 活期总额
    BigDecimal fxabal = BigDecimal.ZERO;// 定期总额
    BigDecimal cerbal = BigDecimal.ZERO;// 大额存单
    BigDecimal doubal = BigDecimal.ZERO;// 双整存单
    BigDecimal acctbl = BigDecimal.ZERO;// 余额
    String nowDate = CommTools.getBaseRunEnvs().getTrxn_date();//当前日期
    String lsdate = CommTools.getBaseRunEnvs().getLast_date();//上次交易日期
    E_TERMCD depttm = E_TERMCD.T000;// 存期
    long depday = 0;// 存期天数
    long stoday = 0;// 已存天数
    String iscurr = "N";// 是否有签约活期
    String lsisnl = "Y";// 列表是否为空
    String begndt = "";// 开始日期
    String fishdt = "";// 结束日期
    String faflag = "";// 定活标志
    //		E_YES___ trdpfg = E_YES___.NO;//是否转存标志

    //根据电子账号获取电子账号ID
    IoCaKnaAcdc cplKnaAcdc = SysUtil.getInstance(IoCaSevQryTableInfo.class)
            .getKnaAcdcOdb2(cardno, false);
    //查询该电子账号是否签约
    IoCaKnaSign cplKnaSign = SysUtil.getInstance(IoCaSevQryTableInfo.class)
            .getKnaSignOdb1(cplKnaAcdc.getCustac(),
                    cn.sunline.ltts.busi.sys.type.CaEnumType.E_SIGNTP.ZNCXL, E_SIGNST.QY, false);
    if (CommUtil.isNotNull(cplKnaSign)) {
        iscurr = "Y";
    }
    // 如果列表信息为空，返回数据都为0
    if (kna_accs.size() == 0) {

        output.setTotbal(onlnbl);// 存款总额   onlnbl
        output.setCurbal(BigDecimal.ZERO);// 活期总额
        output.setFxabal(BigDecimal.ZERO);// 定期总额
        output.setIscurr(iscurr);// 是否有签约活期
        output.setLsisnl(lsisnl);// 列表是否为空
        output.setPddetl(null);// 列表为空
        output.setCerbal(BigDecimal.ZERO);//大额存单
        output.setDoubal(BigDecimal.ZERO);//双整存款
        output.setOnlnbl(onlnbl);//普通活期
    } else {

        // 循环获取列表信息
        for (IoCaSelAcctno acinfo : kna_accs) {

            IoDpDpAssetsQry acdetl = SysUtil.getInstance(IoDpDpAssetsQry.class);//
            
            // 获取电子账号，定活标志，币种，产品编号、产品细类
            acdetl.setAcctno(acinfo.getAcctno());
            acdetl.setCrcycd(acinfo.getCrcycd());
            acdetl.setProdcd(acinfo.getProdcd());
            E_FCFLAG faflag1 = acinfo.getFaflag();
            E_DEBTTP debttp = null;//产品细类
            E_YES___ trdpfg = null;//是否转存标志
            
            //查询负债账户计息信息表中判断利率确定日期
            KnbAcin tblKnbAcin = KnbAcinDao.selectOne_odb1(acinfo.getAcctno(), false);
            if (CommUtil.isNotNull(tblKnbAcin)) {
                if (faflag1 == E_FCFLAG.CURRENT) {
                    BigDecimal cuusin = prcAcctCuusin(tblKnbAcin, acinfo);
                    acdetl.setCuusin(cuusin);
                } else if (faflag1 == E_FCFLAG.FIX) {
                    BigDecimal cuusin = prcFxacCuusin(tblKnbAcin, acinfo);
                    acdetl.setCuusin(cuusin);
                }
            }

            // 活期
            if (faflag1 == E_FCFLAG.CURRENT) {

                // 查询活期账信息
                KnaAcct KnaAcct = KnaAcctDao.selectOne_odb1(acinfo.getAcctno(), false);

                if (CommUtil.isNull(KnaAcct)) {

                } else if (CommUtil.isNotNull(KnaAcct)) {

                    //查询负债账户计提明细的计提利息
                    KnbCbdl tblKnbCbdl = KnbCbdlDao.selectOne_odb2(lsdate, acinfo.getAcctno(), false);
                    if (CommUtil.isNotNull(tblKnbCbdl)) {
                        acdetl.setIntrvl(tblKnbCbdl.getCabrin());
                    }

                    //新增可售产品ID
                    KnaAcctProd tblKnaAcctProd = KnaAcctProdDao.selectOne_odb1(
                            acinfo.getAcctno(), false);

                    if (CommUtil.isNotNull(tblKnaAcctProd)) {
                        acdetl.setSprdid(tblKnaAcctProd.getSprdid());// 可售产品ID
                        acdetl.setSprdvr(tblKnaAcctProd.getSprdvr());// 当前版本号
                        acdetl.setSprdna(tblKnaAcctProd.getObgaon());//可售产品名称
                    }

                    // 活期账户信息不为空
                    acctbl = DpAcctProc.getAcctBalance(KnaAcct);// 余额
                    begndt = KnaAcct.getOpendt();// 开始日期

                    // 结束日期，有销户日期选销户日期，否则选到期日期
                    if (CommUtil.isNull(KnaAcct.getClosdt())) {
                        fishdt = KnaAcct.getMatudt();
                    } else {
                        fishdt = KnaAcct.getClosdt();
                    }

                    stoday = DateTools2.calDays(begndt, nowDate, 0, 0);// 已存天数
                    curbal = curbal.add(acctbl);// 活期总额
                    faflag = "01";// 定活标志
                }
            } else if (faflag1 == E_FCFLAG.FIX) {

                KnaFxac KnaFxac = KnaFxacDao.selectOne_odb1(acinfo.getAcctno(), false);// 查询定期信息

                //获取账户利率表信息
                KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acinfo.getAcctno(), true);

                if (CommUtil.isNull(KnaFxac)) {
                    	output.setCerbal(BigDecimal.ZERO);//大额存单
                        output.setDoubal(BigDecimal.ZERO);//双整存款
                    
                } else if (CommUtil.isNotNull(KnaFxac)) {

                    //计算定期预期收益
                    if (CommUtil.compare(KnaFxac.getUpbldt(), nowDate) >= 0 && tblKnbAcin.getDetlfg() == E_YES___.NO) {
                        String sEdindt = DateTimeUtil.dateAdd("day", nowDate, -1);
                        //计算计提程序输入
                        DpInstPrcIn cplDpInstPrcIn = SysUtil.getInstance(DpInstPrcIn.class);
                        cplDpInstPrcIn.setInoptp(E_INDLTP.CAIN);
                        cplDpInstPrcIn.setTrandt(nowDate);
                        cplDpInstPrcIn.setTransq(CommTools.getBaseRunEnvs().getMain_trxn_seq());
                        cplDpInstPrcIn.setLstrdt(lsdate);
                        cplDpInstPrcIn.setEdindt(sEdindt);
                        cplDpInstPrcIn.setOnlnbl(KnaFxac.getOnlnbl()); //当前账户余额
                        cplDpInstPrcIn.setCrcycd(KnaFxac.getCrcycd()); //账户货币代号
                        cplDpInstPrcIn.setBrchno(KnaFxac.getBrchno()); //所属机构
                        cplDpInstPrcIn.setProdcd(KnaFxac.getProdcd()); //产品编号
                        cplDpInstPrcIn.setAcctcd(KnaFxac.getAcctcd()); //核算代码
                        //计算计提利息
                        Map<String, BigDecimal> mapCabrin = DpInterest.prcInstMain(tblKnbAcin, tblKubInrt, cplDpInstPrcIn);
                        acdetl.setIntrvl(mapCabrin.get("bigCabrin"));
                    } else {
                        //查询负债账户计提明细的计提利息
                        KnbCbdl tblKnbCbdl = KnbCbdlDao.selectOne_odb2(lsdate, acinfo.getAcctno(), false);
                        if (CommUtil.isNotNull(tblKnbCbdl)) {
                            acdetl.setIntrvl(tblKnbCbdl.getCabrin());
                        }
                    }

                    // chaiwenchang 新增可售产品ID 当前版本号输出接口
                    KnaFxacProd tblKnaFxacProd = KnaFxacProdDao
                            .selectOne_odb1(acinfo.getAcctno(), false);

                    if (CommUtil.isNotNull(tblKnaFxacProd)) {
                        acdetl.setSprdid(tblKnaFxacProd.getSprdid());// 可售产品ID
                        acdetl.setSprdvr(tblKnaFxacProd.getSprdvr());// 当前版本号
                        acdetl.setSprdna(tblKnaFxacProd.getObgaon());// 可售产品名称
                    }

                    KnaFxacMatu tblKnaFxacMatu = KnaFxacMatuDao.selectOne_odb1(KnaFxac.getAcctno(), false);
                    if (CommUtil.isNotNull(tblKnaFxacMatu)) {
                        trdpfg = tblKnaFxacMatu.getTrdpfg();
                    }
                    // 定期账户信息不为空
                    acctbl = KnaFxac.getOnlnbl();// 余额
                    begndt = KnaFxac.getOpendt();// 开始日期

                    // 结束日期，有销户日期选销户日期，否则选到期日期
                    if (CommUtil.isNull(KnaFxac.getClosdt())) {
                        fishdt = KnaFxac.getMatudt();
                    } else {
                        fishdt = KnaFxac.getClosdt();
                    }

                    depttm = KnaFxac.getDepttm();// 存期

                    if (CommUtil.isNotNull(KnaFxac.getMatudt())) {
                        depday = DateTools2.calDays(KnaFxac.getOpendt(), KnaFxac.getMatudt(), 0, 0);// 存期天数
                    }

                    stoday = DateTools2.calDays(begndt, nowDate, 0, 0);// 已存天数
                   
                    faflag = "02";
                    debttp = KnaFxac.getDebttp();
                    /*
                     * 拆分定期总额 ：
                     * 判断产品细类  是大额存单还是双整
                     */
                  //  List<KnaFxac> Kna_Fxac = new ArrayList<KnaFxac>();
    				if(debttp == E_DEBTTP.DP2508 ){//大额存单
    					cerbal = cerbal.add(acctbl);  
    				}else if(debttp == E_DEBTTP.DP2501 ){//双整存单
    					doubal = doubal.add(acctbl);
    				}else {
    					 fxabal = fxabal.add(acctbl);// 定期总额
    				}
                }
            }
          
            acdetl.setFaflag(faflag);// 定活标志
            acdetl.setAcctbl(acctbl);// 余额
            acdetl.setBegndt(begndt);// 开始日期
            acdetl.setFishdt(fishdt);// 结束日期
            acdetl.setDepttm(depttm);// 存期
            acdetl.setDepday(depday);// 存期天数
            acdetl.setStoday(stoday);// 已存天数
            acdetl.setTrdpfg(trdpfg);//是否自动转存标识

            output.getPddetl().add(acdetl);// 把结果集返回给output
        }
        BigDecimal middle = onlnbl;// 中间变量=普通活期+双整存款+大额存单+定期总额+活期总额
        middle = middle.add(doubal).add(cerbal).add(fxabal).add(curbal);
       // System.err.println("======="+middle+"=========普通活期 = "+onlnbl+"活期总额="+curbal+"定期总额="+fxabal+"大额存单="+cerbal+"双整存款="+doubal);
        lsisnl = "N";// 列表是否为空
        
        output.setTotbal(middle);// 存款总额
        output.setCurbal(curbal);// 活期总额
        output.setFxabal(fxabal);// 定期总额
        output.setIscurr(iscurr);// 是否有签约活期
        output.setLsisnl(lsisnl);// 列表是否为空
        output.setCerbal(cerbal);//大额存单
        output.setDoubal(doubal);//双整存款
        output.setOnlnbl(onlnbl);//普通活期


    }
    // 设置总记录数
    output.setCounts(selact.getCounts());
    // 设置报文头总记录条数
    CommTools.getBaseRunEnvs().setTotal_count(selact.getCounts());


}

private static BigDecimal prcAcctCuusin(KnbAcin tblKnbAcin, IoCaSelAcctno acinfo) {


    String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//当前日期
    //由于行内利率会出现变化，固活期需要分段计息

    BigDecimal cutmam = tblKnbAcin.getCutmam(); //本期积数
    BigDecimal avgtranam = BigDecimal.ZERO; //平均余额

    //活期总积数
    BigDecimal totalAcmltn = BigDecimal.ZERO;
    //执行利率
    BigDecimal intrvl = BigDecimal.ZERO;
    //实际积数
    BigDecimal realCutmam = DpPublic.calRealTotalAmt(cutmam, acinfo.getOnlnbl(), trandt, tblKnbAcin.getLaamdt());

    totalAcmltn = totalAcmltn.add(realCutmam); //加实际积数			

    IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);

    //查询账户利率表，获取利率优惠值
    KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(tblKnbAcin.getAcctno(), false);

    if (E_IRCDTP.LAYER == tblKnbAcin.getIncdtp()) {

        int days = 1; //计提天数					

        if (CommUtil.compare(tblKnbAcin.getInammd(), E_IBAMMD.ACCT) == 0) { //账户余额				

            throw Intr.E9999("活期产品暂不支持账户余额靠档利率");

        } else if (CommUtil.compare(tblKnbAcin.getInammd(), E_IBAMMD.AVG) == 0) {//平均余额

            days = calAvgDays(tblKnbAcin.getIrwptp(), tblKnbAcin.getBldyca(), tblKnbAcin.getTxbefr(),
                    tblKnbAcin.getLcindt(), tblKnbAcin.getNcindt(), trandt);

            if (CommUtil.equals(totalAcmltn, BigDecimal.ZERO)) {
                avgtranam = acinfo.getOnlnbl();//平均余额
            } else {
                avgtranam = totalAcmltn.divide(BigDecimal.valueOf(days), 2, BigDecimal.ROUND_HALF_UP);//平均余额
            }

            IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
            intrEntity.setCorpno(tblKnbAcin.getCorpno()); //法人代码
            intrEntity.setBrchno(acinfo.getBrchno());//机构号
            intrEntity.setTranam(avgtranam);//交易金额
            intrEntity.setTrandt(trandt);//交易日期
            intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码 
            intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
            intrEntity.setCrcycd(acinfo.getCrcycd());//币种
            intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
            intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
            intrEntity.setCainpf(E_CAINPF.T1); //计息规则
            intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起息日期
            //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
            //				if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
            //					String termcd = acinfo.getDepttm().getValue();
            //					if(CommUtil.equals(termcd.substring(0, 1),"9")){
            //						intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
            //					}else{
            //						intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
            //					}
            //				}

            KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(acinfo.getAcctno(), true);
            if (CommUtil.isNotNull(tblKnaAcct)) {
                if (CommUtil.isNotNull(tblKnaAcct.getMatudt())) {
                    intrEntity.setEdindt(tblKnaAcct.getMatudt());//止息日
                } else {
                    intrEntity.setEdindt(trandt); //止息日
                }
            }

            if (CommUtil.isNull(intrEntity.getEdindt())) {
                intrEntity.setEdindt(trandt); //止息日
            }
            //				intrEntity.setEdindt(cplDpInstPrcIn.getTrandt());  //止息日

            intrEntity.setLevety(tblKnbAcin.getLevety());
            if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                intrEntity.setTrandt(tblKnbAcin.getOpendt());
                intrEntity.setTrantm("999999");
            }
            pbpub.countInteresRate(intrEntity);

            BigDecimal cuusin = intrEntity.getIntrvl();//获取利率
            //利率可取最大值
            BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
            //利率可取最小值
            BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

            // 利率优惠后执行利率
            cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                    divide(BigDecimal.valueOf(100))));
            //若优惠后的利率小于最大可取利率则赋值为最大可取利率
            if (CommUtil.compare(cuusin, maxval) > 0) {
                cuusin = maxval;
            }
            //若优惠后的利率小于最小可取利率则赋值为最小可取利率
            if (CommUtil.compare(cuusin, minval) < 0) {
                cuusin = minval;
            }
            intrvl = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);

        } else if(CommUtil.compare(tblKnbAcin.getInammd(), E_IBAMMD.SUM) == 0){//积数
            /** add by huangwh 20181122 start  积数靠档 */
            
            IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
            intrEntity.setCorpno(tblKnbAcin.getCorpno()); //法人代码
            intrEntity.setBrchno(acinfo.getBrchno());//机构号
            intrEntity.setTranam(totalAcmltn);/** 交易金额   = 活期总积数 */
            intrEntity.setTrandt(trandt);//交易日期
            intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码 
            intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
            intrEntity.setCrcycd(acinfo.getCrcycd());//币种
            intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
            intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
            intrEntity.setCainpf(E_CAINPF.T1); //计息规则
            intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起息日期
            //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
            //              if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
            //                  String termcd = acinfo.getDepttm().getValue();
            //                  if(CommUtil.equals(termcd.substring(0, 1),"9")){
            //                      intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
            //                  }else{
            //                      intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));           
            //                  }
            //              }

            KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(acinfo.getAcctno(), true);
            if (CommUtil.isNotNull(tblKnaAcct)) {
                if (CommUtil.isNotNull(tblKnaAcct.getMatudt())) {
                    intrEntity.setEdindt(tblKnaAcct.getMatudt());//止息日
                } else {
                    intrEntity.setEdindt(trandt); //止息日
                }
            }

            if (CommUtil.isNull(intrEntity.getEdindt())) {
                intrEntity.setEdindt(trandt); //止息日
            }
            //              intrEntity.setEdindt(cplDpInstPrcIn.getTrandt());  //止息日

            intrEntity.setLevety(tblKnbAcin.getLevety());
            if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                intrEntity.setTrandt(tblKnbAcin.getOpendt());
                intrEntity.setTrantm("999999");
            }
            pbpub.countInteresRate(intrEntity);

            BigDecimal cuusin = intrEntity.getIntrvl();//获取利率
            //利率可取最大值
            BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
            //利率可取最小值
            BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

            // 利率优惠后执行利率
            cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                    divide(BigDecimal.valueOf(100))));
            //若优惠后的利率小于最大可取利率则赋值为最大可取利率
            if (CommUtil.compare(cuusin, maxval) > 0) {
                cuusin = maxval;
            }
            //若优惠后的利率小于最小可取利率则赋值为最小可取利率
            if (CommUtil.compare(cuusin, minval) < 0) {
                cuusin = minval;
            }
            intrvl = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
            
            /** add by huangwh 20181122 end */
        }else {
            throw DpModuleError.DpstComm.BNAS1593();
        }

    } else if (E_IRCDTP.Reference == tblKnbAcin.getIncdtp() || E_IRCDTP.BASE == tblKnbAcin.getIncdtp()) { //参考利率

        // modify 20161221 liaojc 当利率确定日期为支取日，重新获取当前执行利率
        BigDecimal cuusin = tblKubInrt.getCuusin(); // 账户利率表执行利率

        if (tblKnbAcin.getIntrdt() == E_INTRDT.DRAW) {

            IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
            intrEntity.setCrcycd(acinfo.getCrcycd()); //币种
            intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码

            //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
            intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
            intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
            intrEntity.setTrandt(trandt);
            intrEntity.setDepttm(E_TERMCD.T000);// 存期
            intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起始日期
            //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
            //				if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
            //					String termcd = acinfo.getDepttm().getValue();
            //					if(CommUtil.equals(termcd.substring(0, 1),"9")){
            //						intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
            //					}else{
            //						intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
            //					}
            //				}

            KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(acinfo.getAcctno(), false);
            if (CommUtil.isNotNull(tblKnaAcct)) {
                if (CommUtil.isNotNull(tblKnaAcct.getMatudt())) {
                    intrEntity.setEdindt(tblKnaAcct.getMatudt());//止息日
                } else {
                    intrEntity.setEdindt(trandt); //止息日
                }
            }

            if (CommUtil.isNull(intrEntity.getEdindt())) {
                intrEntity.setEdindt(trandt); //止息日
            }
            //				intrEntity.setEdindt(trandt); //结束日期
            intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
            intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
            intrEntity.setCorpno(tblKnbAcin.getCorpno());//法人代码
            intrEntity.setBrchno(acinfo.getBrchno());//机构

            intrEntity.setLevety(tblKnbAcin.getLevety());
            pbpub.countInteresRate(intrEntity);

            cuusin = intrEntity.getIntrvl(); //当前执行利率

            //利率可取最大值
            BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
            //利率可取最小值
            BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
            // 利率优惠后执行利率
            cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                    divide(BigDecimal.valueOf(100))));

            //若优惠后的利率大于最大可取利率则赋值为最大可取利率
            if (CommUtil.compare(cuusin, maxval) > 0) {
                cuusin = maxval;
            }
            //若优惠后的利率小于最小可取利率则赋值为最小可取利率
            if (CommUtil.compare(cuusin, minval) < 0) {
                cuusin = minval;
            }
            cuusin = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
        }

        intrvl = cuusin;

    } else {
        throw DpModuleError.DpstComm.BNAS1081();
    }
    return intrvl;

}

/**
 * @author yanghang
 *         <p>
 *         <li>2016年8月28日 </li>
 *         <li>计算平均余额对应周期天数</li>
 *         </p>
 * 
 * @param cycltp 周期类型
 * @param txbefr 结息频率
 * @param lcdt 上次结息日期
 * @param ncdt 下次结息日期
 * @param curdt 当前日期
 * @param avbldt 当前日期
 * @return 平均余额对应周期天数
 */
private static int calAvgDays(E_CYCLTP cycltp, E_AVBLDT avbldt, String txbefr, String lcdt, String ncdt, String curdt) {

    int days;

    if (CommUtil.isNull(ncdt)) {
        throw DpModuleError.DpstComm.BNAS1594();
    }
    if (CommUtil.isNull(lcdt)) {
        if (CommUtil.isNull(txbefr)) {
            throw DpModuleError.DpstComm.BNAS1595();
        }
        //	    	lcdt = DateTools2.calDateByFreq(ncdt, txbefr, "", "", 3, 2);
        lcdt = DateTools2.calDateByFreq(ncdt, txbefr, null, 2);
    }

    if (CommUtil.compare(avbldt, E_AVBLDT.T1) == 0) {//实际天数
        days = DateTools2.calDays(lcdt, ncdt, 0, 0); // 实际天数
    } else {
        days = DateTools2.calDays(lcdt, ncdt, 1, 0); // 储蓄天数
    }

    return days;

}

private static BigDecimal prcFxacCuusin(KnbAcin tblKnbAcin, IoCaSelAcctno acinfo) {


    //定期账户
    String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//当前日期

    //执行利率
    BigDecimal intrvl = BigDecimal.ZERO;

    IoSrvPbInterestRate pbpub = SysUtil.getInstance(IoSrvPbInterestRate.class);
    //传统定期
    if (CommUtil.equals(E_YES___.NO.getValue(), tblKnbAcin.getDetlfg().getValue())) {

        // 获取定期负债账户信息
        KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(acinfo.getAcctno(), true);

        //计算计提利息

        if (E_IRCDTP.Reference == tblKnbAcin.getIncdtp() || E_IRCDTP.BASE == tblKnbAcin.getIncdtp()) { //参考利率
            //获取账户利率信息
            KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acinfo.getAcctno(), true);

            // modify 20161221 liaojc 当利率确定日期为支取日，重新获取当前执行利率
            BigDecimal cuusin = tblKubInrt.getCuusin(); // 账户利率表执行利率

            if (tblKnbAcin.getIntrdt() == E_INTRDT.DRAW) {

                IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                intrEntity.setCrcycd(acinfo.getCrcycd()); //币种
                intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码

                //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                intrEntity.setTrandt(trandt);
                intrEntity.setDepttm(tblKnaFxac.getDepttm());// 存期
                intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起始日期
                //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
                //					if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
                //						String termcd = acinfo.getDepttm().getValue();
                //						if(CommUtil.equals(termcd.substring(0, 1),"9")){
                //							intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
                //						}else{
                //							intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
                //						}
                //					}
                //获取负债账号签约明细信息
                IoCaKnaSignDetl cplKnaSignDetl = DpAcctDao.selKnaSignDetlInfo(acinfo.getAcctno(), false);

                if (CommUtil.isNotNull(tblKnaFxac)) {
                    if (CommUtil.isNotNull(tblKnaFxac.getMatudt())) {
                        intrEntity.setEdindt(tblKnaFxac.getMatudt());//止息日
                    } else if (CommUtil.isNotNull(cplKnaSignDetl)) {
                        if (CommUtil.isNotNull(cplKnaSignDetl.getEffedt())) {
                            intrEntity.setEdindt(cplKnaSignDetl.getEffedt()); //止息日
                        }
                    } else {
                        intrEntity.setEdindt("20991231");
                    }
                }

                if (CommUtil.isNull(intrEntity.getEdindt())) {
                    intrEntity.setEdindt("20991231"); //止息日
                }
                //					intrEntity.setEdindt(trandt); //结束日期
                intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                intrEntity.setCorpno(tblKnbAcin.getCorpno());//法人代码
                intrEntity.setBrchno(tblKnaFxac.getBrchno());//机构

                intrEntity.setLevety(tblKnbAcin.getLevety());
                pbpub.countInteresRate(intrEntity);

                cuusin = intrEntity.getIntrvl(); //当前执行利率

                //利率可取最大值
                BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                //利率可取最小值
                BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
                // 利率优惠后执行利率
                cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                        divide(BigDecimal.valueOf(100))));

                //若优惠后的利率小于最大可取利率则赋值为最大可取利率
                if (CommUtil.compare(cuusin, maxval) > 0) {
                    cuusin = maxval;
                }
                //若优惠后的利率小于最小可取利率则赋值为最小可取利率
                if (CommUtil.compare(cuusin, minval) < 0) {
                    cuusin = minval;
                }
                cuusin = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
            }

            //自动转存重订价产品处理
            if (tblKnbAcin.getInprwy() == E_IRRTTP.QD) {

                //计算利息，使用行内基准的活期利率
                //IntrPublicEntity intrMatuEntity = new IntrPublicEntity();
                IoPbIntrPublicEntity intrMatuEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                intrMatuEntity.setCrcycd(tblKnaFxac.getCrcycd()); //币种
                intrMatuEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码
                intrMatuEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                intrMatuEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                intrMatuEntity.setDepttm(tblKnaFxac.getDepttm()); //存期
                intrMatuEntity.setTrandt(trandt);
                intrMatuEntity.setBgindt(tblKnaFxac.getBgindt()); //起始日期
                intrMatuEntity.setEdindt(tblKnaFxac.getMatudt()); //结束日期
                intrMatuEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                intrMatuEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                intrMatuEntity.setCorpno(tblKnaFxac.getCorpno());//法人代码
                intrMatuEntity.setBrchno(tblKnaFxac.getBrchno());//机构

                intrMatuEntity.setLevety(tblKnbAcin.getLevety());
                if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                    intrMatuEntity.setTrandt(tblKnaFxac.getOpendt());
                    intrMatuEntity.setTrantm("999999");
                }
                pbpub.countInteresRate(intrMatuEntity);

                //bigInstam = intrMatuEntity.getInamnt();

                cuusin = intrMatuEntity.getIntrvl(); //当前执行利率

                // 利率优惠后执行利率
                cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                        divide(BigDecimal.valueOf(100))));

                //mod by leipeng   优惠后判断利率是否超出基础浮动范围20170220  start--
                //利率的最大范围值
                BigDecimal intrvlmax = intrMatuEntity.getBaseir().multiply(BigDecimal.ONE.add(intrMatuEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                //利率的最小范围值
                BigDecimal intrvlmin = intrMatuEntity.getBaseir().multiply(BigDecimal.ONE.add(intrMatuEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

                if (CommUtil.compare(cuusin, intrvlmin) < 0) {
                    cuusin = intrvlmin;
                } else if (CommUtil.compare(cuusin, intrvlmax) > 0) {
                    cuusin = intrvlmax;
                }
            }

            intrvl = cuusin;

        } else if (E_IRCDTP.LAYER == tblKnbAcin.getIncdtp()) {

            IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
            intrEntity.setCorpno(tblKnbAcin.getCorpno()); //法人代码
            intrEntity.setBrchno(acinfo.getBrchno());//机构号
            intrEntity.setTranam(acinfo.getOnlnbl());//交易金额
            intrEntity.setTrandt(trandt);//交易日期
            intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码 
            intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
            intrEntity.setCrcycd(acinfo.getCrcycd());//币种
            intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
            intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
            intrEntity.setCainpf(E_CAINPF.T1); //计息规则
            intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起息日期

            //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
            //					if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
            //						String termcd = acinfo.getDepttm().getValue();
            //						if(CommUtil.equals(termcd.substring(0, 1),"9")){
            //							intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
            //						}else{
            //							intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
            //						}
            //					}

            //获取负债账号签约明细信息
            IoCaKnaSignDetl cplKnaSignDetl = DpAcctDao.selKnaSignDetlInfo(acinfo.getAcctno(), false);

            if (CommUtil.isNotNull(tblKnaFxac)) {
                if (CommUtil.isNotNull(tblKnaFxac.getMatudt())) {
                    intrEntity.setEdindt(tblKnaFxac.getMatudt());//止息日
                } else if (CommUtil.isNotNull(cplKnaSignDetl)) {
                    if (CommUtil.isNotNull(cplKnaSignDetl.getEffedt())) {
                        intrEntity.setEdindt(cplKnaSignDetl.getEffedt()); //止息日
                    }
                } else {
                    intrEntity.setEdindt("20991231");
                }
            }

            if (CommUtil.isNull(intrEntity.getEdindt())) {
                intrEntity.setEdindt("20991231"); //止息日
            }

            intrEntity.setLevety(tblKnbAcin.getLevety());
            if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                intrEntity.setTrandt(tblKnbAcin.getOpendt());
                intrEntity.setTrantm("999999");
            }
            pbpub.countInteresRate(intrEntity);

            //获取账户利率信息
            KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acinfo.getAcctno(), true);

            //利率可取最大值
            BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
            //利率可取最小值
            BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

            // 利率优惠后执行利率
            BigDecimal cuusin = intrEntity.getIntrvl();
            cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                    divide(BigDecimal.valueOf(100))));

            //若优惠后的利率小于最大可取利率则赋值为最大可取利率
            if (CommUtil.compare(cuusin, maxval) > 0) {
                cuusin = maxval;
            }
            //若优惠后的利率小于最小可取利率则赋值为最小可取利率
            if (CommUtil.compare(cuusin, minval) < 0) {
                cuusin = minval;
            }

            intrvl = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);

        } else { // 分层利率在利率优惠代码块中进行实现
            throw DpModuleError.DpstComm.BNAS1081();
        }

    } else {//智能储蓄

        // 获取定期负债账户信息
        KnaFxac tblKnaFxac = KnaFxacDao.selectOne_odb1(acinfo.getAcctno(), true);

        if (E_IRCDTP.Reference == tblKnbAcin.getIncdtp() || E_IRCDTP.BASE == tblKnbAcin.getIncdtp()) { //参考利率
            //获取账户利率信息
            KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acinfo.getAcctno(), true);

            // modify 20161221 liaojc 当利率确定日期为支取日，重新获取当前执行利率
            BigDecimal cuusin = tblKubInrt.getCuusin(); // 账户利率表执行利率

            if (tblKnbAcin.getIntrdt() == E_INTRDT.DRAW) {

                IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
                intrEntity.setCrcycd(acinfo.getCrcycd()); //币种
                intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码

                //如果是靠档则取靠当利率，靠档方式默认为当前档，否则则取行内基准利率，默认为活期
                intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
                intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
                intrEntity.setTrandt(trandt);
                intrEntity.setDepttm(tblKnaFxac.getDepttm());// 存期
                intrEntity.setBgindt(tblKnbAcin.getBgindt()); //起始日期
                //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
                //					if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
                //						String termcd = acinfo.getDepttm().getValue();
                //						if(CommUtil.equals(termcd.substring(0, 1),"9")){
                //							intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
                //						}else{
                //							intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
                //						}
                //					}
                //获取负债账号签约明细信息
                IoCaKnaSignDetl cplKnaSignDetl = DpAcctDao.selKnaSignDetlInfo(acinfo.getAcctno(), false);

                if (CommUtil.isNotNull(tblKnaFxac)) {
                    if (CommUtil.isNotNull(tblKnaFxac.getMatudt())) {
                        intrEntity.setEdindt(tblKnaFxac.getMatudt());//止息日
                    } else if (CommUtil.isNotNull(cplKnaSignDetl)) {
                        if (CommUtil.isNotNull(cplKnaSignDetl.getEffedt())) {
                            intrEntity.setEdindt(cplKnaSignDetl.getEffedt()); //止息日
                        }
                    } else {
                        intrEntity.setEdindt("20991231");
                    }
                }

                if (CommUtil.isNull(intrEntity.getEdindt())) {
                    intrEntity.setEdindt("20991231"); //止息日
                }
                //					intrEntity.setEdindt(trandt); //结束日期
                intrEntity.setCainpf(E_CAINPF.T1); // addby luxy 靠档利率需要
                intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
                intrEntity.setCorpno(tblKnbAcin.getCorpno());//法人代码
                intrEntity.setBrchno(tblKnaFxac.getBrchno());//机构

                intrEntity.setLevety(tblKnbAcin.getLevety());
                pbpub.countInteresRate(intrEntity);

                cuusin = intrEntity.getIntrvl(); //当前执行利率

                //利率可取最大值
                BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
                //利率可取最小值
                BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));
                // 利率优惠后执行利率
                cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                        divide(BigDecimal.valueOf(100))));
                //若优惠后的利率大于最大可取利率则赋值为最大可取利率
                if (CommUtil.compare(cuusin, maxval) > 0) {
                    cuusin = maxval;
                }
                //若优惠后的利率小于最小可取利率则赋值为最小可取利率
                if (CommUtil.compare(cuusin, minval) < 0) {
                    cuusin = minval;
                }
                cuusin = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);
            }

            intrvl = cuusin;

        } else if (E_IRCDTP.LAYER == tblKnbAcin.getIncdtp()) {

            IoPbIntrPublicEntity intrEntity = SysUtil.getInstance(IoPbIntrPublicEntity.class);
            intrEntity.setCorpno(tblKnbAcin.getCorpno()); //法人代码
            intrEntity.setBrchno(acinfo.getBrchno());//机构号

            //查询定期明细表的记录
            KnaFxacDetl tblKnaFxacDetl = DpAcctDao.selKnaFxacDetlByAcctno(acinfo.getAcctno(), false);
            if (CommUtil.isNotNull(tblKnaFxacDetl)) {
                intrEntity.setTranam(tblKnaFxacDetl.getOnlnbl());//交易金额
                intrEntity.setBgindt(tblKnaFxacDetl.getBgindt()); //起息日期
            } else {
                intrEntity.setTranam(acinfo.getOnlnbl());//交易金额
                intrEntity.setBgindt(acinfo.getBgindt()); //起息日期
            }

            intrEntity.setTrandt(trandt);//交易日期
            intrEntity.setIntrcd(tblKnbAcin.getIntrcd()); //利率代码 
            intrEntity.setIncdtp(tblKnbAcin.getIncdtp()); //利率代码类型
            intrEntity.setCrcycd(acinfo.getCrcycd());//币种
            intrEntity.setInbebs(tblKnbAcin.getTxbebs()); //计息基础
            intrEntity.setIntrwy(tblKnbAcin.getIntrwy()); //靠档方式
            intrEntity.setCainpf(E_CAINPF.T1); //计息规则

            //到期日期计算，存期是自定义的，则根据存期天数计算到期日期，否则根据传统存期计算
            //					if (CommUtil.isNotNull(acinfo.getDepttm()) && E_TERMCD.T000 != acinfo.getDepttm()) {
            //						String termcd = acinfo.getDepttm().getValue();
            //						if(CommUtil.equals(termcd.substring(0, 1),"9")){
            //							intrEntity.setEdindt(DateTools2.dateAdd (acinfo.getDeptdy().intValue(), acinfo.getBgindt()));
            //						}else{
            //							intrEntity.setEdindt(DateTools2.calDateByTerm(acinfo.getBgindt(),acinfo.getDepttm()));			
            //						}
            //					}
            //					if(CommUtil.isNull(intrEntity.getEdindt())){
            //						intrEntity.setEdindt(trandt); //止息日
            //					}

            //获取负债账号签约明细信息
            IoCaKnaSignDetl cplKnaSignDetl = DpAcctDao.selKnaSignDetlInfo(acinfo.getAcctno(), false);

            if (CommUtil.isNotNull(tblKnaFxac)) {
                if (CommUtil.isNotNull(tblKnaFxac.getMatudt())) {
                    intrEntity.setEdindt(tblKnaFxac.getMatudt());//止息日
                } else if (CommUtil.isNotNull(cplKnaSignDetl)) {
                    if (CommUtil.isNotNull(cplKnaSignDetl.getEffedt())) {
                        intrEntity.setEdindt(cplKnaSignDetl.getEffedt()); //止息日
                    }
                } else {
                    intrEntity.setEdindt("20991231");
                }
            }

            if (CommUtil.isNull(intrEntity.getEdindt())) {
                intrEntity.setEdindt("20991231"); //止息日
            }

            intrEntity.setLevety(tblKnbAcin.getLevety());
            if (tblKnbAcin.getIntrdt() == E_INTRDT.OPEN) {
                intrEntity.setTrandt(tblKnbAcin.getOpendt());
                intrEntity.setTrantm("999999");
            }
            pbpub.countInteresRate(intrEntity);

            //获取账户利率信息
            KubInrt tblKubInrt = KubInrtDao.selectOne_odb1(acinfo.getAcctno(), true);
            //利率可取最大值
            BigDecimal maxval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmxsc().divide(BigDecimal.valueOf(100))));
            //利率可取最小值
            BigDecimal minval = intrEntity.getBaseir().multiply(BigDecimal.ONE.add(intrEntity.getFlmnsc().divide(BigDecimal.valueOf(100))));

            // 利率优惠后执行利率
            BigDecimal cuusin = intrEntity.getIntrvl();
            cuusin = cuusin.add(cuusin.multiply(CommUtil.nvl(tblKubInrt.getFavort(), BigDecimal.ZERO).
                    divide(BigDecimal.valueOf(100))));

            //若优惠后的利率小于最大可取利率则赋值为最大可取利率
            if (CommUtil.compare(cuusin, maxval) > 0) {
                cuusin = maxval;
            }
            //若优惠后的利率小于最小可取利率则赋值为最小可取利率
            if (CommUtil.compare(cuusin, minval) < 0) {
                cuusin = minval;
            }

            intrvl = cuusin.setScale(6, BigDecimal.ROUND_HALF_UP);

        } else { // 分层利率在利率优惠代码块中进行实现
            throw DpModuleError.DpstComm.BNAS1081();
        }
    }

    return intrvl;

}
}
