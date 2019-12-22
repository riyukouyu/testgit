package cn.sunline.ltts.busi.catran.batchtran;

//import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
//import java.util.HashMap;
import java.util.List;
//import java.util.Map;

import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
//import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit;
//import cn.sunline.ltts.busi.aplt.para.ApBatchFileParams;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
//import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbUnat;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbUnatDao;
import cn.sunline.ltts.busi.dp.froz.DpAcctStatus;
import cn.sunline.ltts.busi.dp.namedsql.DpDayEndDao;
//import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatch;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnlBill;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaUpdAcctstIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpAcStatusWord;
//import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
//import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
//import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILEST;
//import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
//import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_CUACST;
//import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.cedar.base.logging.BizLog;
//import cn.sunline.edsp.base.lang.options.DefaultOptions;
//import cn.sunline.edsp.base.lang.Params;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

//import com.alibaba.fastjson.JSON;
	 /**
	  * 非活跃账户同步
	  *
	  */

public class unactiDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.catran.batchtran.intf.Unacti.Input, 
  cn.sunline.ltts.busi.catran.batchtran.intf.Unacti.Property> {
	private static BizLog bizlog = BizLogUtil.getBizLog(unactiDataProcessor.class);
//	private static String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
/*	// 存放目录更改
	private static String filePath = "D://CIF"; // 文件路径

	private static E_FILETP filetp = E_FILETP.DP021400;
	private static String fengefu = "|@|";
	private static String filesq = "NASPSS" + trandt;// 统一生成一个流水号

	private static String md5 = ""; // MD5值
	private static String path2 = File.separator + filetp + File.separator
			+ trandt + File.separator;// 相对路径，与数据子系统协调后确定
*/	
//	DefaultOptions<BatchFileSubmit> ls = new DefaultOptions<BatchFileSubmit>();
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.catran.batchtran.intf.Unacti.Input input, cn.sunline.ltts.busi.catran.batchtran.intf.Unacti.Property property) {
		
//		BatchFileSubmit batch = SysUtil.getInstance(BatchFileSubmit.class);
//		Map<String, String> map = new HashMap<String, String>();
		 
		//根据当前日期算出6月前开户的账户，再根据账单表查寻6个月内是否有交易记录，有则返回，无记录登记为非活跃
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//当前交易时间
		String timetm = DateTools2.getCurrentTimestamp();// 当前交易日期时间戳
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		GregorianCalendar gc = new GregorianCalendar();
		
		KnpParameter tblPara = KnpParameterDao.selectOne_odb1("unacti", "%", "%", "%", false);
		if(CommUtil.isNull(tblPara)){
			throw CaError.Eacct.E0001("未配置非活跃账户参数！");
		}
		int actidt =  Integer.parseInt(tblPara.getParm_value1());//间隔日期，超出则登记非活跃登记簿
		try {
			Date unacti = sdf.parse(trandt);
			
			//当前日期减6个月
			gc.setTime(unacti);
			gc.add(2, actidt);
			
		} catch (ParseException e) {
			throw ExceptionUtil.wrapThrow(e);
		}
		
		String opendt = sdf.format(gc.getTime());
		
		bizlog.debug("*******开户日期******" + opendt + "*************");
		
/*		Params param = new Params();
		param.add("opendt", opendt);
		param.add("corpno", CommTools.getBaseRunEnvs().getBusi_org_id());
		String fileph = filePath;
		//TODO 文件名待定
		String filename = "unacti_test.txt";*/
//		String namedSqlId = "";// 查询数据集的命名sql
//
//		namedSqlId = CaDao.namedsql_selknacustbyopendt;
		
		List<KnaCust> knaCust = CaDao.selknacustbyopendt(opendt, CommTools.getBaseRunEnvs().getBusi_org_id(), false);
		if(CommUtil.isNull(knaCust)){
			return ;
		}
		for(KnaCust custs:knaCust){
			String custac = custs.getCustac();//电子账号
			String custno = custs.getCustno();//客户号
			String custna = custs.getCustna();//户名
		
			
			IoDpKnlBill tblknlbill = CaDao.selknlbillbycustac(custac, false);
			
			//若账单表有记录 则返回
			if(CommUtil.isNotNull(tblknlbill)){
				continue;
			}
			
			//根据客户号查询客户信息表
//			IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
//			queryCust.setCustno(custno);
//			IoSrvCfPerson.IoGetCifCust.Output tblcifcust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
//			SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, tblcifcust);
			
			
			KnaAcdc tblknaacdc = KnaAcdcDao.selectFirst_odb1(custac, E_DPACST.NORMAL, false);
			if(CommUtil.isNull(tblknaacdc)){
				continue ;
			}
			
			/**
			 * modify yusheng
			 */
			// 查询电子账户状态
			E_CUACST cuacst = SysUtil.getInstance(IoCaSrvGenEAccountInfo.class)
					.selCaStInfo(custac);
			// 只有正常户才可以转非活跃户
			if (cuacst != E_CUACST.NORMAL) {
				bizlog.error("电子账户状态异常不允许转非活跃!",trandt);
				continue;
			}
			
			// 调用获取电子账户状态字方法
						IoDpAcStatusWord cplGetAcStWord = DpAcctStatus.GetAcStatus(custac);
						// 部冻、借冻、双冻、部止、全止（银行止付）、全止（外部止付）、质押、存款证明、开单和预授权不允许转为非活跃
						if (cplGetAcStWord.getPtfroz() == E_YES___.YES
								|| cplGetAcStWord.getBrfroz() == E_YES___.YES
								|| cplGetAcStWord.getDbfroz() == E_YES___.YES
								|| cplGetAcStWord.getPtstop() == E_YES___.YES
								|| cplGetAcStWord.getBkalsp() == E_YES___.YES
								|| cplGetAcStWord.getOtalsp() == E_YES___.YES
								|| cplGetAcStWord.getPledge() == E_YES___.YES
								|| cplGetAcStWord.getCertdp() == E_YES___.YES
								|| cplGetAcStWord.getBillin() == E_YES___.YES
								|| cplGetAcStWord.getPreaut() == E_YES___.YES) {
							// throw DpModuleError.DpstComm.E9999("电子账户状态字异常不允许休眠! ");
							bizlog.error("电子账户状态字异常不允许转非活跃!", trandt);
							continue;
						}
			
			
			// 写文件
//			StringBuffer file_Info = SysUtil.getInstance(StringBuffer.class);// 拼接字符串
			
			String cardno = tblknaacdc.getCardno();
//			String idtftp = tblcifcust.getIdtftp().getValue();
//			String idtfno = tblcifcust.getIdtfno();
			String opendt1 = custs.getOpendt();
//			E_IDTFTP idtftp1 = tblcifcust.getIdtftp();
			
			// 根据电子账户获取电子账户表数据
			IoCaKnaCust cplCaKnaCust = SysUtil.getInstance(
					IoCaSevQryTableInfo.class).getKnaCustByCustacOdb1(custac,
					false);
			if (CommUtil.isNull(cplCaKnaCust)) {
				// throw DpModuleError.DpstComm.E9999("电子账号不存在! ");
				bizlog.error("电子账号不存在! ", trandt);
				continue;
			}
			// 更新电子账户账户状态为非活跃
			DpDayEndDao.updKnaCustSleep(E_ACCTST.INTIVE,
								cplCaKnaCust.getCustac(),timetm);
			// 更新电子账户负债活期子账户状态为非活跃
			DpDayEndDao.updKnaAcctSleep(E_ACCTST.INTIVE,
								cplCaKnaCust.getCustac(), E_ACCTST.NORMAL,timetm);
			//登记非活跃账户登记簿
			KnbUnat knbUnat = SysUtil.getInstance(KnbUnat.class);
			knbUnat.setCardno(cardno);
			knbUnat.setCustac(custac);
			knbUnat.setCustna(custna);
//			knbUnat.setIdtfno(idtfno);
//			knbUnat.setIdtftp(idtftp1);
			knbUnat.setIsacti(E_YES___.YES);
			knbUnat.setOpendt(opendt1);
			knbUnat.setTrandt(trandt); //交易日期
			knbUnat.setUserid(CommTools.getBaseRunEnvs().getTrxn_teller()); //操作柜员
			knbUnat.setTrantm(BusiTools.getBusiRunEnvs().getTrantm()); //交易时间
			KnbUnatDao.insert(knbUnat);
			
			// 登记客户化状态
			// 更新非活跃状态
			IoCaUpdAcctstIn cplDimeInfo = SysUtil
					.getInstance(IoCaUpdAcctstIn.class);
			cplDimeInfo.setCustac(custac);
			SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).updCustAcctst(
					cplDimeInfo);
		}
		
		
		
		
/*		final LttsFileWriter file = new LttsFileWriter(fileph, filename,
				"UTF-8");
		
		file.open();
		try{
			DaoUtil.selectList(namedSqlId, param, new CursorHandler<KnaCust>(){

				@Override
				public boolean handle(int arg0, KnaCust arg1) {
					String custac = arg1.getCustac();//电子账号
					String custno = arg1.getCustno();//客户号
					String custna = arg1.getCustna();//户名
					
					IoDpKnlBill tblknlbill = CaDao.selknlbillbycustac(custac, false);
					
					//若账单表无记录 则返回
					if(CommUtil.isNotNull(tblknlbill)){
						return true;
					}
					
					//根据客户号查询客户信息表
					IoSrvCfPerson.IoGetCifCust.InputSetter queryCust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.InputSetter.class);
					queryCust.setCustno(custno);
					IoSrvCfPerson.IoGetCifCust.Output tblcifcust = SysUtil.getInstance(IoSrvCfPerson.IoGetCifCust.Output.class);
					SysUtil.getInstance(IoSrvCfPerson.class).getCifCust(queryCust, tblcifcust);
					
					
					KnaAcdc tblknaacdc = KnaAcdcDao.selectFirst_odb1(custac, E_DPACST.NORMAL, false);
					if(CommUtil.isNull(tblknaacdc)){
						return true;
					}
					// 写文件
					StringBuffer file_Info = SysUtil.getInstance(StringBuffer.class);// 拼接字符串
					
					String cardno = tblknaacdc.getCardno();
					String idtftp = tblcifcust.getIdtftp().getValue();
					String idtfno = tblcifcust.getIdtfno();
					String opendt = arg1.getOpendt();
					E_IDTFTP idtftp1 = tblcifcust.getIdtftp();
					//登记非活跃账户登记簿
					KnbUnat knbUnat = SysUtil.getInstance(KnbUnat.class);
					knbUnat.setCardno(cardno);
					knbUnat.setCustac(custac);
					knbUnat.setCustna(custna);
					knbUnat.setIdtfno(idtfno);
					knbUnat.setIdtftp(idtftp1);
					knbUnat.setIsacti(E_YES___.YES);
					knbUnat.setOpendt(opendt);
					KnbUnatDao.insert(knbUnat);
					
					file_Info.append(cardno).append(fengefu)//卡号
						.append(custac).append(fengefu)//电子账号
						.append(custna).append(fengefu)//户名
						.append(idtftp).append(fengefu)//证件类型
						.append(idtfno).append(fengefu)//证件号码
						.append(opendt);//开户日期
					
					// 打印文件
					file.write(file_Info.toString());
					return true;
				}
				
			});
			
		}finally{
			file.close();
		}*/
/*		
		try{
			md5 = MD5EncryptUtil.getFileMD5String(new File(fileph
					.concat(File.separator).concat(filename)));
		}catch(Exception e){
			throw ApError.BusiAplt.E0042(filename);
		}
		
		map.put(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);
		map.put(ApBatchFileParams.BATCH_PMS_TRANDT, trandt);

		batch.setFilenm(filename);
		batch.setFlpath(path2);
		batch.setFilemd(md5);
//		batch.setParams(JSON.toJSONString(map));

		ls.add(batch);
		
		E_SYSCCD target = E_SYSCCD.CIF;
		String status = E_FILEST.SUCC.getValue();
		String descri = "非活跃账户同步";
		SysUtil.getInstance(IoApFileBatch.class).doBatchSubmitBackNotice(
				status, descri, target, filetp, ls);*/
	}

}


