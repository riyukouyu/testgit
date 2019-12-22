package cn.sunline.ltts.busi.dp.serviceimpl;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.tables.DpHisDepo.HKnaAcct;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlBill;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnlIobl;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.KnaAcctType;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.KnaFxacType;
import cn.sunline.ltts.busi.dp.type.DpDayEndType.KnlBillCustType;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DfaHoldType;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.DincinfoType;
import cn.sunline.ltts.busi.iobus.type.dp.DpProdSvc.FdaFundType;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.ln.IoLnQryComplex.IoXmCpIoBill;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IRFLPF;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YENDST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PMCRAC;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_GLCLASSCD;

/**
 * 负债相关公共服务实现 负债相关公共服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
public class IoDpSrvImpl implements
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpCommSvr {

	private static final BizLog bizlog = BizLogUtil
			.getBizLog(IoDpSrvImpl.class);

	private static final String fileSepa1 = "^";
	
	private static final String fileSepa2 = "|";

	private static StringBuffer buf = new StringBuffer();

	/**
	 * 生成存款信息文件接口
	 * 
	 */
	public void genMDSFiles(String lstrdt) {
		
//		 判断是否为年结
       KnpParameter yearendPara = KnpParameterDao.selectOne_odb1("GlParm.SysDate", "YearEnd", "%", "%", true);
   		boolean isYearend = false;
   	
//   	判断是否为年底最后一天
   		boolean isLastDay = CommUtil.equals(lstrdt,DateTimeUtil.lastDay(lstrdt, E_GLCLASSCD.Y.getId()));
//   	判断是否为年结，需不需要特殊处理
	   	if (CommUtil.equals(yearendPara.getParm_value1(), E_YENDST.NORMAL.getValue()) && isLastDay) {
	   		isYearend = true;
		}
		
		// 产生文件的日期目录
		String lstrdtPath = lstrdt + "/";

		/******* 存款交易流水开始 ***********/
		// 获取文件生产路径
		
        KnpParameter para1 = SysUtil.getInstance(KnpParameter.class);
        if (isYearend) {
        	para1 = KnpParameterDao.selectOne_odb1("ZXYH_end", "dpfile", "01", "%",true);
		}else {
			para1 = KnpParameterDao.selectOne_odb1("ZXYH", "dpfile", "01", "%",true);
		}
		String path1 = para1.getParm_value1();
		path1 = para1.getParm_value1() + lstrdtPath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");
		// 获取文件名
		String filename1 = para1.getParm_value2();
		bizlog.debug("文件名称 filename:[" + filename1 + "]");
		// 获取是否产生文件标志
		String isCreateFlg1 = para1.getParm_value3();
		bizlog.debug("文件产生标志 :[" + isCreateFlg1 + "]");
		// 获取加载模式（增量/全量）
		String createMode1 = CommUtil.nvl(para1.getParm_value5(), "ZL");
		bizlog.debug("文件加载模式 :[" + createMode1 + "]");
		if (CommUtil.equals(isCreateFlg1, "Y")) {
			final LttsFileWriter file = new LttsFileWriter(path1, filename1);
			Params params = new Params();
            String namedSqlId = "";
			if (CommUtil.equals(createMode1, "QL")) {
				namedSqlId =DpAcctDao.namedsql_selKnlBillAndKnaCust;
			} else {
				namedSqlId =DpAcctDao.namedsql_selKnlBillAndKnaCustByTrandt;
				params.add("datetm", lstrdt);
			}
			if (true) {
				file.open();
				try {
					if (!isYearend) {
					   DaoUtil.selectList(namedSqlId, params, new CursorHandler<KnlBillCustType>() {
						    int i = 0;
		                      @Override
		                      public boolean handle(int index, KnlBillCustType entity) {
							String acctno = entity.getAcctno();// 负债账号
							String intrcd = entity.getTransq();// 内部流水序号
							String posino = Integer.toString(i);// 流水序号(非空 10)
							String trandt = entity.getTrandt();// 交易日期
							String transq = entity.getTransq();// 交易流水
							String bgindt = CommUtil.nvl(entity.getBgindt(),"");// 起息日期
//									entity.getBgindt();
							String tranbr = CommUtil.nvl(entity.getTranbr(),"");// 交易营业机构
//									entity.getTranbr();
							String trante = entity.getIntrcd(); // 交易类型（交易码）
							String servtp = CommUtil.isNotNull(entity.getServtp())?entity.getServtp().toString():""; // 交易渠道
							String userid = entity.getUserid();// 操作柜员
							String strktp = CommUtil.isNotNull(entity.getStrktp())?entity.getStrktp().toString():"";// 冲正标志
							String currcd = "RMB"; // 标准货币
//							String source = entity.getTrancy();// 源货币
//							if ("01".equals(source.toString())) {
//								source = CommUtil.toEnum(String.class, "RMB");
//							}
							String source = "RMB";
							E_AMNTCD amntcd = entity.getAmntcd();// 借贷标志
							String amntcda = CommUtil.isNotNull(entity.getAmntcd())?entity.getAmntcd().toString():"";
							String  baseta = CommUtil.isNotNull(entity.getTranam())?entity.getTranam().toString():"";// 标准货币交易数额
							String sourta = CommUtil.isNotNull(entity.getTranam())?entity.getTranam().toString():"";// 源交易数额
							String custac = entity.getCustac();// 客户账号
							String servicecd = "0000";// 服务编码
							String sysindicator = "0000";// 系统标识
							String capitalpurpose = "ZXYH";// 资金用途_备注 (转账)
							String cdtrfg = CommUtil.isNotNull(entity.getCstrfg())?entity.getCstrfg().toString():"";// 现转标志
							String  cdfrflag = "";
							if (CommUtil.isNotNull(cdtrfg)) {
								cdfrflag = cdtrfg;
							}
							String cashopercode = "2";// 现金代码 0-本行现金 1-本行转账
														// 2-他行转账
//							kna_cust tblKna_cust = Kna_custDao.selectOne_odb1(
//									custac, true);
							String custno = entity.getCustno();// 客户编号23
							String trantm =CommUtil.isNotNull(entity.getTrantm())?entity.getTrantm().toString():""; // 交易时间24
							String trantime = CommUtil.lpad(trantm,9,"0");
//							kna_accs tblkna_acct = Kna_accsDao.selectOne_odb2(
//									acctno, true);
							String prodcd = entity.getProdcd();// 产品编号
							String vouchertp = "099";// 凭证种类
							String cardno = CommUtil.nvl(entity.getCardno(),"");// 凭证号码27----------
//									entity.getCardno();
							String brchna = "";//绑定卡对应行名 30  如果是银联和chinapay的出入金交易，才对行名赋值
							if (CommUtil.equals(trante, "sealou") || CommUtil.equals(trante, "seanou") ||CommUtil.equals(trante, "secpin")
									|| CommUtil.equals(trante, "secpou") || CommUtil.equals(trante, "sencou") || CommUtil.equals(trante, "sealin")
									) {
								brchna = entity.getBrchna();
							}
							String intsedenum = "1";// 国际结算申报号_核销专用号36
							String revtranid = CommUtil.nvl(entity.getOrigtq(),"");// 冲正流水号42
//									entity.getOrigtq();
							String trnotrannum = "000000000000000";// DM账号_转出
							String trnitrannum = "0000000000000000";// DM账号_转入
							String trnitype = "099";// 转入方凭证类型
							String trintypenum =  CommUtil.nvl(entity.getCardno(),"");// 转入方凭证号码-----------
							String itemtype = "1";// 交易种类 1-转账
							String poid = "0000";// PO_ID
							String refbanks = "0000";// 本行对方银行国家代码
							String refbankl = "0000";// 本行对方银行代码
							String refacext =CommUtil.nvl(entity.getOpcuac(),"") ;// 本行对方账号
							String reverseps = "";// 冲销原流水序列号52
							String cust04 = "ZXYH";// 备注字段_银行附言56
							E_PMCRAC tabpmcrac = entity.getPmcrac();
							String itemstatus;// 流水状态57
							if (CommUtil.isNull(tabpmcrac)) {
								itemstatus = "";
							}else {
								itemstatus = tabpmcrac.toString();
							}
							String payactname = "";// 转出方户名
							if (E_AMNTCD.DR == amntcd) {
								payactname = entity.getAcctna();
							} else if (E_AMNTCD.CR == amntcd) {
								payactname = entity.getOpacna();
							}
							String paybankname = "0000";// 转出方行名
							String paycast01 = "0000";// 转出方备用字段1 61
							String rcvactname = "";// 转入行户名63
							if (E_AMNTCD.DR == amntcd) {
								rcvactname = entity.getOpacna();
							} else if (E_AMNTCD.CR == amntcd) {
								rcvactname = entity.getAcctna();
							}
							String rcvbankname = "0000";// 转入方行名64
							String revcuat01 = "0000";// 转入方备用字段1 65
							String buprcode = "0000";// 流程服务编码68
							String paympayh = "0000";// 通道70

//							String dcmttp = CommUtil.isNotNull(entity
//									.getDcmttp()) ? entity.getDcmttp()
//									.getValue() : "";
//							String dcbtno = CommUtil
//									.nvl(entity.getDcbtno(), "");// 凭证批号
//							String opcuac = CommUtil
//									.nvl(entity.getOpcuac(), "");// 对方客户账户
//							String opacna = CommUtil
//									.nvl(entity.getOpacna(), "");// 对方户名
//							String bankna = CommUtil
//									.nvl(entity.getBankna(), "");// 对方金融机构名称
							// 写文件
							buf.delete(0, buf.length());
//							  .append(fileSepa1)
							buf.append(acctno).append(fileSepa1).append(intrcd).append(fileSepa1).append(posino)
							 .append(fileSepa1) .append(trandt) .append(fileSepa1) .append(transq) .append(fileSepa1)
							 .append(bgindt) .append(fileSepa1) .append(tranbr) .append(fileSepa1) .append(trante)
							 .append(fileSepa1).append(servtp).append(fileSepa1).append(userid).append(fileSepa1)
							 .append(strktp).append(fileSepa1).append(currcd).append(fileSepa1)
							 .append( source).append(fileSepa1)
							 .append(amntcda).append(fileSepa1)
							 .append(baseta).append(fileSepa1)
							 .append(sourta).append(fileSepa1).append(custac).append(fileSepa1)
							 .append(servicecd).append(fileSepa1).append(sysindicator).append(fileSepa1)
							 .append(capitalpurpose).append(fileSepa1).append(cdfrflag)
							 .append(fileSepa1).append(cashopercode).append(fileSepa1).append(custno)
							 .append(fileSepa1).append(trantime).append(fileSepa1).append(prodcd).append(fileSepa1)
							 .append(vouchertp).append(fileSepa1).append(cardno).append(fileSepa1).append(fileSepa1).append(fileSepa1).append(brchna).append(fileSepa1).append(fileSepa1).append(fileSepa1).append(fileSepa1).append(fileSepa1).append(fileSepa1)
							.append(intsedenum).append(fileSepa1).append(fileSepa1).append(fileSepa1).append(fileSepa1).append(fileSepa1).append(fileSepa1).append(revtranid).append(fileSepa1)
							 .append(trnotrannum).append(fileSepa1).append(trnitrannum).append(fileSepa1)
							 .append(trnitype).append(fileSepa1).append(trintypenum).append(fileSepa1)
							 .append(itemtype).append(fileSepa1).append(poid).append(fileSepa1).append(refbanks)
							 .append(fileSepa1).append(refbankl).append(fileSepa1).append(refacext).append(fileSepa1)
							 .append(reverseps).append(fileSepa1).append(fileSepa1).append(fileSepa1).append(fileSepa1).append(cust04).append(fileSepa1)
							 .append(itemstatus).append(fileSepa1).append(fileSepa1).append(payactname).append(fileSepa1)
							 .append(paybankname).append(fileSepa1).append(paycast01).append(fileSepa1).append(fileSepa1)
							 .append(rcvactname).append(fileSepa1).append(rcvbankname).append(fileSepa1)
							 .append(revcuat01).append(fileSepa1).append(fileSepa1).append(fileSepa1).append(buprcode).append(fileSepa1).append(fileSepa1)
							 .append(paympayh).append(fileSepa1);
							 file.write(buf.toString());
//									 acctno + "^" + intrcd + "^" + posino
//									+ "^" + trandt + "^" + transq + "^"
//									+ bgindt + "^" + tranbr + "^" + trante
//									+ "^" + servtp + "^" + userid + "^"
//									+ strktp.toString() + "^" + currcd + "^"
//									+ source.toString() + "^"
//									+ amntcd.toString() + "^"
//									+ baseta.toString() + "^"
//									+ sourta.toString() + "^" + custac + "^"
//									+ servicecd + "^" + sysindicator + "^"
//									+ capitalpurpose + "^" + cdtrfg.toString()
//									+ "^" + cashopercode + "^" + custno
//									+ "^" + trantime + "^" + prodcd + "^"
//									+ vouchertp + "^" + cardno + "^"+ "^"+ "^"+ "^"+ "^"+ "^"+ "^"+ "^"+ "^"
//									+ intsedenum + "^" + "^"+ "^"+ "^"+ "^"+ "^"+ revtranid + "^"
//									+ trnotrannum + "^" + trnitrannum + "^"
//									+ trnitype + "^" + trintypenum + "^"
//									+ itemtype + "^" + poid + "^" + refbanks        
//									+ "^" + refbankl + "^" + refacext + "^"
//									+ reverseps + "^"+ "^"+ "^"+ "^" + cust04 + "^"
//									+ itemstatus + "^"+ "^" + payactname + "^"
//									+ paybankname + "^" + paycast01 + "^"+ "^"
//									+ rcvactname + "^" + rcvbankname + "^"11111
//									+ revcuat01 + "^"+ "^"+ "^" + buprcode + "^"+ "^"
//									+ paympayh + "^"
////									+ "^" + dcmttp + "^" + dcbtno
////									+ "^" + opcuac + "^" + opacna + "^"
////									+ bankna + "^" + "^" + "^" + "^" + "^"
////									+ "^" + "^" + "^" + "^" + "^" + "^"
////									+ "^" + "^" + "^" + "^" + "^" + "^"
//									);
							 i++;
							 return true;
		                      }
	                    });
	                }}
					finally {
					file.close();
				}
			}

			bizlog.debug("存款交易流水" + filename1 + "文件产生完成");
		}
		/******* 存款交易流水结束 ***********/

		/******* 产品类型开始 ***********/
		 KnpParameter para2 = SysUtil.getInstance(KnpParameter.class);
	        if (isYearend) {
	        	para2 = KnpParameterDao.selectOne_odb1("ZXYH_end", "dpfile", "02", "%",true);
			}else {
				para2 = KnpParameterDao.selectOne_odb1("ZXYH", "dpfile", "02", "%",true);
			}
		// 获取文件生产路径
		String path2 = para2.getParm_value1();
		path2 = para2.getParm_value1() + lstrdtPath;
		bizlog.debug("文件产生路径 path:[" + path2 + "]");
		// 获取文件名
		String filename2 = para2.getParm_value2();
		bizlog.debug("文件名称 filename:[" + filename2 + "]");
		// 获取是否产生文件标志
		String isCreateFlg2 = para2.getParm_value3();
		bizlog.debug("文件产生标志 :[" + isCreateFlg2 + "]");
		if (CommUtil.equals(isCreateFlg2, "Y")) {
			LttsFileWriter file = new LttsFileWriter(path2, filename2);
			// 默认获取全量数据
			List<KupDppb> entities = DpAcctDao.selDppbInfo(false);
			if (true) {
				file.open();
				try {
					if (!isYearend) {
					if (CommUtil.isNotNull(entities)) {
						KupDppb entity = SysUtil.getInstance(KupDppb.class);
						for (int i = 0; i < entities.size(); i++) {
							entity = entities.get(i);
							String prodcd = entity.getProdcd();// 产品编号1
							String productattribute = "";// 产品属性2
							String prodtx = CommUtil.nvl(entity.getProdtx(),"");//产品说明
//									entity.getProdtx();
							String finprodtypcd = "ACCT";// 金融产品类型代码4
							String prodcatcd = "";// 产品类别代码*************************5
							if (E_FCFLAG.FIX  == entity.getPddpfg()) {
								prodcatcd = "60";
								productattribute = "02";
							} else {
								prodcatcd = "90";
								productattribute = "01";
							}
							buf.delete(0, buf.length());
							buf.append(prodcd).append(fileSepa1).append(productattribute).append(fileSepa1).append(prodtx).append(fileSepa1).append(finprodtypcd).append(fileSepa1)
							.append(prodcatcd).append(fileSepa1).append(fileSepa1).append(fileSepa1).append(lstrdt).append(fileSepa1).append(lstrdt).append(fileSepa1);
							// 写文件
							file.write(buf.toString());
//									prodcd + "^" + productattribute + "^" +prodtx+ "^" + finprodtypcd+ "^" + prodcatcd + "^" + "^" + "^" + lstrdt + "^"+ lstrdt + "^");
						}

					}
				} }
					finally {
					file.close();
				}
			}

			bizlog.debug("产品类型" + filename2 + "文件产生完成");
		}
		/******* 产品类型结束 ***********/

		/******* 存款日末余额开始 ***********/
		 KnpParameter para3 = SysUtil.getInstance(KnpParameter.class);
	        if (isYearend) {
	        	para3 = KnpParameterDao.selectOne_odb1("ZXYH_end", "dpfile", "03", "%",true);
			}else {
				para3 = KnpParameterDao.selectOne_odb1("ZXYH", "dpfile", "03", "%",true);
			}
		// 获取文件生产路径
		String path3 = para3.getParm_value1();
		path3 = para3.getParm_value1() + lstrdtPath;
		bizlog.debug("文件产生路径 path:[" + path3 + "]");
		// 获取文件名
		String filename3 = para3.getParm_value2();
		bizlog.debug("文件名称 filename:[" + filename3 + "]");
		// 获取是否产生文件标志
		String isCreateFlg3 = para3.getParm_value3();
		bizlog.debug("文件产生标志 :[" + isCreateFlg3 + "]");
		// 获取加载模式（增量/全量）
		String createMode3 = CommUtil.nvl(para3.getParm_value5(), "ZL");
		bizlog.debug("文件加载模式 :[" + createMode3 + "]");
		if (CommUtil.equals(isCreateFlg3, "Y")) {
			final LttsFileWriter file = new LttsFileWriter(path3, filename3);
			Params params = new Params();
			params.add("acctdt", lstrdt);
            String namedSqlId = "";
			if (CommUtil.equals(createMode3, "QL")) {
				namedSqlId =DpAcctDao.namedsql_selHKnaAcct;
			} else {
				namedSqlId =DpAcctDao.namedsql_selHKnaAcctByUpldt;
			}
			if (true) {
				file.open();
				try {
					if (!isYearend) {
					 DaoUtil.selectList(namedSqlId, params, new CursorHandler<HKnaAcct>() {
                         @Override
                         public boolean handle(int index, HKnaAcct entity) {
							String acctno = entity.getAcctno();// 负债账户
//							String crcycd = entity.getCrcycd();// 货币代号
							String crcycd = "RMB";// 货币代号
							BigDecimal onlnbl = entity.getOnlnbl();// 当前账户余额
							String acctdt = CommUtil
									.nvl(entity.getAcctdt(), "");// 账务日期
							entity.getUpbldt();//余额更新日期 (最后交易日期)
							String opendt = CommUtil.nvl(entity.getOpendt(),
									acctdt);
							buf.delete(0, buf.length());
							buf.append(acctno).append(fileSepa1).append(opendt).append(fileSepa1).append(crcycd)
							.append(fileSepa1).append(onlnbl).append(fileSepa1).append(acctdt).append(fileSepa1);
							
							
							// 写文件
							file.write(buf.toString());
									
//									acctno + "^" + opendt + "^" + crcycd
//									+ "^" + onlnbl + "^" + acctdt + "^");
							
							return true;
                         }
                     });
                 } }
				finally {
					file.close();
				}
			}
			bizlog.debug("存款日末余额" + filename3 + "文件产生完成");
		}
		/******* 存款日末余额结束 ***********/

		/******* 对私活期存款主信息开始 ***********/
		KnpParameter para4 = SysUtil.getInstance(KnpParameter.class);
        if (isYearend) {
        	para4 = KnpParameterDao.selectOne_odb1("ZXYH_end", "dpfile", "04", "%",true);
		}else {
			para4 = KnpParameterDao.selectOne_odb1("ZXYH", "dpfile", "04", "%",true);
		}
		// 获取文件生产路径
		String path4 = para4.getParm_value1();
		path4 = para4.getParm_value1() + lstrdtPath;
		bizlog.debug("文件产生路径 path:[" + path4 + "]");
		// 获取文件名
		String filename4 = para4.getParm_value2();
		bizlog.debug("文件名称 filename:[" + filename4 + "]");
		// 获取是否产生文件标志
		String isCreateFlg4 = para4.getParm_value3();
		bizlog.debug("文件产生标志 :[" + isCreateFlg4 + "]");
		// 获取加载模式（增量/全量）
		String createMode4 = CommUtil.nvl(para4.getParm_value5(), "ZL");
		bizlog.debug("文件加载模式 :[" + createMode4 + "]");
		if (CommUtil.equals(isCreateFlg4, "Y")) {
			final LttsFileWriter file = new LttsFileWriter(path4, filename4);
			 Params params = new Params();
            String namedSqlId = "";
			if (CommUtil.equals(createMode4, "QL")) {
				namedSqlId = DpAcctDao.namedsql_selKnaAcctForMDS;
			} else {
				namedSqlId = DpAcctDao.namedsql_selKnaAcctForMDSByDate;
				params.add("datetm", lstrdt);
			}
			if (true) {
				file.open();
				try {
					if (!isYearend) {
					 DaoUtil.selectList(namedSqlId, params, new CursorHandler<KnaAcctType>() {
                         @Override
                         public boolean handle(int index, KnaAcctType entity) {

							String acctno = entity.getAcctno();// 负债账号1
							String brchno = entity.getBrchno();// 所属机构2
							brchno = brchno.substring(0, 4);
							String opendt = entity.getOpendt();// 开户日期3
							String closdt = CommUtil
									.nvl(entity.getClosdt(), "");// 销户日期4
							String acctst =  CommUtil.isNotNull(entity.getAcctst())?entity.getAcctst().toString():""; // 账户状态5
							String acctstatus = "";
							if (CommUtil.equals(acctst, "1")) {
								acctstatus = "30";
							}else if (CommUtil.equals(acctst, "2")) {
								acctstatus = "50";
							}else if (CommUtil.equals(acctst, "3")) {
							acctstatus = "30";
							}
							String acctbrnum = entity.getBrchno();// 账户分行6
							String custno = entity.getCustno();// 客户号8
							String productid = entity.getProdcd();// 银行产品代码9
							String acctna = entity.getAcctna();// 账户名称12
							String invocationdt = entity.getOpendt();// 启用日期13
							String csextg = CommUtil.isNotNull(entity.getCsextg())?entity.getCsextg().toString():""; // 账户钞汇标志20         *************************** 1 汇；2 钞
							String sextflag = "";
							 if (CommUtil.equals(csextg, "0")) {  
								 sextflag = "2";
							}else if(CommUtil.equals(csextg, "1")){
								sextflag = "1";
							}
							
//							String currcd = entity.getCrcycd();// 币种21
							String currcd = "RMB";
							String lstrdt1 = entity.getLstrdt();// 上次交易日期22
							String bgindt = entity.getBgindt();// 起息日期23
							String matudt = CommUtil
									.nvl(entity.getMatudt(), "");// 到期日期24
							entity.getBgindt();
//							String trndflag = "";// 转存标志25
							String periodnum = "";// 期限数量28  活期送空
							String periodunit = "";// 期限单位29  活期送空
//							knb_acin talknb_acin = Knb_acinDao.selectOne_odb1(
//									acctno, true);
							String inctsty =  CommUtil.isNotNull(entity.getInbefg())?entity.getInbefg().toString():"";// 计息方式31
							
//							kub_inrt  tabKub_inrt = Kub_inrtDao.selectOne_odb1(acctno, false);
							String baseintrate =  CommUtil.isNotNull(entity.getBsintr())?entity.getBsintr().toString():"";// 基准利率33
							String cuusin =CommUtil.isNotNull(entity.getCuusin())?entity.getCuusin().toString():"";//当前执行利率
							String irflpfa = CommUtil.isNotNull(entity.getIrflby())?entity.getIrflby().toString():"";
							E_IRFLPF irflpf =entity.getIrflby();// 利率浮动方式
							String birfp = "";// 基准利率浮动值_比率35
							if (irflpf  == E_IRFLPF.POINT) {
								birfp = CommUtil.isNotNull(entity.getInflpo())?entity.getInflpo().toString():"";
							}else if(irflpf  == E_IRFLPF.RATE){
								birfp = CommUtil.isNotNull(entity.getInflrt())?entity.getInflrt().toString():"";
							}else{
								birfp = "0000";
							}
							String interestcal = "0033";// 计息周期代码36
							String interestpaytype = "5";// 利息支付方式40
							String savsettflag = "0000";// 储蓄_结算标志47
							String intbranchflag = "0000";// 通存通兑标志48
							String conadd = "0000";// 联系人58
							String conmannum = "0000";// 联系地址59
							String custmannum = "0000";// 客户经理员工号60
							String promannum = "0000";// 产品经理员工号61
							String avlodcrlmt = "0000";// 可透支额度64
							String opnteller = "1001";// 开户柜员70

							buf.delete(0, buf.length()) ;
							buf.append(acctno).append(fileSepa1);// 负债账号1
							buf.append(brchno).append(fileSepa1);// 所属机构2
							buf.append(opendt).append(fileSepa1);// 开户日期3
							buf.append(closdt).append(fileSepa1);// 销户日期4
							buf.append(acctstatus).append(fileSepa1);// 账户状态5
							buf.append(acctbrnum).append(fileSepa1);// 账户分行6
							buf.append(fileSepa1);
							buf.append(custno).append(fileSepa1);// 客户号8
							buf.append(productid).append(fileSepa1);// 银行产品代码9
							buf.append(fileSepa1).append(fileSepa1);
							buf.append(acctna).append(fileSepa1);// 账户名称12
							buf.append(invocationdt).append(fileSepa1);// 启用日期13
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);
							buf.append(sextflag).append(fileSepa1);// 账户钞汇标志20
							buf.append(currcd).append(fileSepa1);// 币种21
							buf.append(lstrdt1).append(fileSepa1);// 上次交易日期22
							buf.append(bgindt).append(fileSepa1);// 起息日期23
							buf.append(matudt).append(fileSepa1);// 到期日期24
							buf.append("").append(fileSepa1);// 转存标志25
							buf.append(fileSepa1).append(fileSepa1);
							buf.append(periodnum).append(fileSepa1);// 期限数量28
							buf.append(periodunit).append(fileSepa1);// 期限单位29
							buf.append(fileSepa1);
							buf.append(inctsty).append(fileSepa1);// 计息方式31
							buf.append(cuusin).append(fileSepa1);//当前执行利率32
							buf.append(baseintrate).append(fileSepa1);// 基准利率33
							buf.append(irflpfa).append(fileSepa1);// 利率浮动方式34
							buf.append(birfp).append(fileSepa1);// 基准利率浮动值_比率35
							buf.append(interestcal).append(fileSepa1);// 计息周期代码36
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);
							buf.append(interestpaytype).append(fileSepa1);// 利息支付方式40
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);
							buf.append(savsettflag).append(fileSepa1);// 储蓄_结算标志47
							buf.append(intbranchflag).append(fileSepa1);// 通存通兑标志48
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);
							buf.append(conadd).append(fileSepa1);// 联系人58
							buf.append(conmannum).append(fileSepa1);// 联系地址59
							buf.append(custmannum).append(fileSepa1);// 客户经理员工号60
							buf.append(promannum).append(fileSepa1);// 产品经理员工号61
							buf.append(fileSepa1).append(fileSepa1);
							buf.append(avlodcrlmt).append(fileSepa1);// 可透支额度64
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1).append(fileSepa1).append(fileSepa1);
							buf.append(opnteller).append(fileSepa1).append(fileSepa1);// 开户柜员70
							//写文件
							file.write(buf.toString());
							
							return true;
                         }
                     });
                 } }
					finally {
					file.close();
				}
			}
			bizlog.debug("对私活期存款主信息" + filename4 + "文件产生完成");
		}
		/******* 对私活期存款主信息 ***********/

		/******* 对私定期存款主信息开始 ***********/
		
		KnpParameter para5 = SysUtil.getInstance(KnpParameter.class);
        if (isYearend) {
        	para5 = KnpParameterDao.selectOne_odb1("ZXYH_end", "dpfile", "05", "%",true);
		}else {
			para5 = KnpParameterDao.selectOne_odb1("ZXYH", "dpfile", "05", "%",true);
		}
		// 获取文件生产路径
		String path5 = para5.getParm_value1();
		path5 = para5.getParm_value1() + lstrdtPath;
		bizlog.debug("文件产生路径 path:[" + path5 + "]");
		// 获取文件名
		String filename5 = para5.getParm_value2();
		bizlog.debug("文件名称 filename:[" + filename5 + "]");
		// 获取是否产生文件标志
		String isCreateFlg5 = para5.getParm_value3();
		bizlog.debug("文件产生标志 :[" + isCreateFlg5 + "]");
		// 获取加载模式（增量/全量）
		String createMode5 = CommUtil.nvl(para5.getParm_value5(), "ZL");
		bizlog.debug("文件加载模式 :[" + createMode5 + "]");
		if (CommUtil.equals(isCreateFlg5, "Y")) {
			final LttsFileWriter file = new LttsFileWriter(path5, filename5);
//			List<KnaFxacType> entities = null; 
			Params params = new Params();
            String namedSqlId = "";
			if (CommUtil.equals(createMode5, "QL")) {
//				entities = DpAcctDao.selKnaFxacForMDS(false);
				namedSqlId = DpAcctDao.namedsql_selKnaFxacForMDS;
			} else {
				namedSqlId = DpAcctDao.namedsql_selKnaFxacForMDSByDate;
				params.add("datetm", lstrdt);
//				entities = DpAcctDao.selKnaFxacForMDSByDate(lstrdt, false);
			}
			if (true) {
				file.open();
				try {
					if (!isYearend) {
					 DaoUtil.selectList(namedSqlId, params, new CursorHandler<KnaFxacType>() {
                         @Override
                         public boolean handle(int index, KnaFxacType entity) {
//							knb_acin talknb_acin = Knb_acinDao.selectOne_odb1(entity.getAcctno(), true);
							String inctsty =CommUtil.isNotNull(entity.getInbefg())?entity.getInbefg().toString():"";// 计息方式31
//							kub_inrt  tabKub_inrt = Kub_inrtDao.selectOne_odb1(entity.getAcctno(), false);
							String baseintrate =  CommUtil.isNotNull(entity.getBsintr())?entity.getBsintr().toString():"";// 基准利率33
							String cuusin =CommUtil.isNotNull(entity.getCuusin())?entity.getCuusin().toString():"";//当前执行利率
							E_IRFLPF irflpf =entity.getIrflby();// 利率浮动方式
							String irflpfa =CommUtil.isNotNull(entity.getIrflby())?entity.getIrflby().toString():"";// 利率浮动方式
							String birfp = "";// 基准利率浮动值_比率35
							if (irflpf  == E_IRFLPF.POINT) {
								birfp = CommUtil.isNotNull(entity.getInflpo())?entity.getInflpo().toString():"";
							}else if(irflpf  == E_IRFLPF.RATE){
								birfp = CommUtil.isNotNull(entity.getInflrt())?entity.getInflrt().toString():"";
							}else{
								birfp = "0000";
							}
							String periodnum = "";// 期限数量28  
							String periodunit = "";// 期限单位29      1-day天   2-week星期  3-month月   4-year年   
							entity.getDepttm().getValue();//根据存期去判断期限数量和单位    
							if (E_TERMCD.T101 == entity.getDepttm()) {
								periodnum = "1";
								periodunit = "1";		
							} else if (E_TERMCD.T103 == entity.getDepttm()) {
								periodnum = "3";
								periodunit = "1";		
							} else if (E_TERMCD.T107 == entity.getDepttm()) {
								periodnum = "7";
								periodunit = "1";		
							} else if (E_TERMCD.T201== entity.getDepttm()) {
								periodnum = "1";
								periodunit = "3";		
							} else if (E_TERMCD.T202== entity.getDepttm()) {
								periodnum = "2";
								periodunit = "3";		
							} else if (E_TERMCD.T203== entity.getDepttm()) {
								periodnum = "3";
								periodunit = "3";		
							} else if (E_TERMCD.T206== entity.getDepttm()) {
								periodnum = "6";
								periodunit = "3";		
							} else if (E_TERMCD.T301== entity.getDepttm()) {
								periodnum = "1";
								periodunit = "4";		
							} else if (E_TERMCD.T302== entity.getDepttm()) {
								periodnum = "2";
								periodunit = "4";		
							} else if (E_TERMCD.T303== entity.getDepttm()) {
								periodnum = "3";
								periodunit = "4";		
							} else if (E_TERMCD.T305== entity.getDepttm()) {
								periodnum = "5";
								periodunit = "4";		
							} else if (E_TERMCD.T306== entity.getDepttm()) {
								periodnum = "6";
								periodunit = "4";		
							} else if (E_TERMCD.T308== entity.getDepttm()) {
								periodnum = "8";
								periodunit = "4";		
							} else if (E_TERMCD.T330== entity.getDepttm()) {
								periodnum = "30";
								periodunit = "4";		
							}else if(E_TERMCD.T000== entity.getDepttm()){
								periodnum = "";
								periodunit = "";
							}else {
								periodnum = CommUtil.isNotNull(entity.getDeptdy())?entity.getAcctst().toString():"";
								periodunit = "1";
							}
							
							String acctst = CommUtil.isNotNull(entity.getAcctst())?entity.getAcctst().toString():"";
							buf.delete(0, buf.length());
							buf.append(CommUtil.nvl(entity.getAcctno(),"")).append(fileSepa1);// 负债账号1
							buf.append(entity.getBrchno().substring(0, 4)).append(fileSepa1);// 所属机构2
							buf.append(CommUtil.nvl(entity.getOpendt(),"")).append(fileSepa1);// 开户日期3
							buf.append(CommUtil.nvl(entity.getClosdt(),"")).append(fileSepa1);// 销户日期4
							buf.append(CommUtil.equals(acctst, "2")?"50":"30").append(fileSepa1);// 账户状态5
							buf.append(entity.getBrchno()).append(fileSepa1);// // 账户分行6
							buf.append(fileSepa1);// // 7
							buf.append(entity.getCustno()).append(fileSepa1);// 客户号8
							buf.append(entity.getProdcd()).append(fileSepa1);// 银行产品代码9
							buf.append(fileSepa1).append(fileSepa1);// 10、11
							buf.append(entity.getAcctna()).append(fileSepa1);// 账户名称12za
							buf.append(entity.getOpendt()).append(fileSepa1);// 启用日期13
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);// 
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);// 
							buf.append(CommUtil.equals(entity.getCsextg().toString(), "1")?"1":"2").append(fileSepa1);// 账户钞汇标志20
							buf.append("RMB").append(fileSepa1);// 币种21
							buf.append(entity.getLstrdt()).append(fileSepa1);// 上次交易日期22
							buf.append(entity.getBgindt()).append(fileSepa1);// 起息日期23
							buf.append(CommUtil.nvl(entity.getMatudt(), "")).append(fileSepa1);// 到期日期24
							buf.append("").append(fileSepa1);//转存标志25
							buf.append("").append(fileSepa1);// 转存起息日26
							buf.append("").append(fileSepa1);// 转存到期日matudt27
							buf.append(periodnum).append(fileSepa1);// 期限数量28
							buf.append(periodunit).append(fileSepa1);// 期限单位29
							buf.append(fileSepa1);// 30
							buf.append(inctsty).append(fileSepa1);// 计息方式31
							buf.append(cuusin).append(fileSepa1);// 32当前执行利率
							buf.append(baseintrate).append(fileSepa1);// 基准利率33
							buf.append(irflpfa).append(fileSepa1);// 34利率浮动方式
							buf.append(birfp).append(fileSepa1);// 基准利率浮动值_比率35
							buf.append("0033").append(fileSepa1);// 计息周期代码36
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);// 
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);// 
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1).append(fileSepa1);// 
							buf.append("0000").append(fileSepa1);// 储蓄_结算标志47
							buf.append("0000").append(fileSepa1);// 通存通兑标志48
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);// 
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);// 
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);// 
							buf.append("0000").append(fileSepa1);// 联系人58
							buf.append("0000").append(fileSepa1);//联系地址59
							buf.append("0000").append(fileSepa1);//客户经理员工号60
							buf.append("0000").append(fileSepa1);// 产品经理员工号61
							buf.append(fileSepa1).append(fileSepa1);// 62-63
							buf.append("0000").append(fileSepa1);// 可透支额度64
							buf.append(entity.getUpbldt()).append(fileSepa1);// 变更时间65
							buf.append(entity.getOnlnbl().toString()).append(fileSepa1);// 定期类存款当期金额66
							buf.append(fileSepa1).append(fileSepa1).append(fileSepa1);// 67-69
							buf.append("1001").append(fileSepa1);// 开户柜员70
							buf.append(fileSepa1);// 71
							// 写文件
							file.write(buf.toString());
							return true;
                         }
                     });
                 } }finally {
					file.close();
				}
			}
			bizlog.debug("对私定期存款主信息" + filename5 + "文件产生完成");
		}
		/******* 对私定期存款主信息结束 ***********/

		
//		/******* 生成屹通存款交易明细文件开始 ***********/
//		// 获取文件生产路径
//		KnpParameter para6 = KnpParameterDao.selectOne_odb1("ZXYH", "transdetail", "01", "%",
//				true);
//		String path6 = para6.getParm_value1() + lstrdtPath;
//		bizlog.debug("文件产生路径 path:[" + path6 + "]");
//
//		// 获取文件名
//		bizlog.debug("文件前缀 fileprefix:[" + para6.getParm_value3() + "]");
//		bizlog.debug("文件后缀 filesuffix:[" + para6.getParm_value4() + "]");
//		String filename6 = para6.getParm_value2();
//		bizlog.debug("文件完整路径 fullpath[" + path6 + filename6 + "]");
//		// 获取是否产生文件标志
//		String isCreateFlg6 = para6.getParm_value3();
//			if (CommUtil.equals(isCreateFlg6, "Y")) {
//				// 产生文件
//				final LttsFileWriter file = new LttsFileWriter(path6, filename6);
//				List<KnlBill> entities = null;
//				String startDate = DateTools2.dateAdd (-10, lstrdt);//lstrdt往后10天
//				Params params = new Params();
//				params.add("datetm", lstrdt);
//	            String namedSqlId = "";
//				params.add("startDate", startDate);
//				
//				bizlog.debug("上日交易日期退后十天的日期是：["+startDate+"]");
//				namedSqlId = DpAcctDao.namedsql_selKnlBillByDateForYitong;
////				entities = DpAcctDao.selKnlBillByDateForYitong(startDate, lstrdt,false);
//		
//				if (true) {
//					file.open();
//					try {
//						 DaoUtil.selectList(namedSqlId, params, new CursorHandler<KnlBill>() {
//	                         @Override
//	                         public boolean handle(int index, KnlBill entity) {
//								String ussqno = CommUtil.nvl(entity.getUssqno(), "");// 交易记录号
//								String corrtg =CommUtil.isNotNull(entity.getCorrtg())?entity.getCorrtg().toString():"" ;// 
//								String chongzhengflag = "";		//交易结果  01--成功，02--失败
//								if (CommUtil.equals(corrtg, "0")) {
//									chongzhengflag = "01";
//								}else {
//									chongzhengflag = "02";
//								}
//								String tranam =CommUtil.isNotNull(entity.getTranam())?entity.getTranam().toString():"" ;// 交易金额
//		
//								buf.delete(0, buf.length());
//								buf.append(ussqno).append(fileSepa1).append(chongzhengflag).append(fileSepa1).append(tranam);
//								// 写文件
//								file.write(buf.toString());
//								return true;
//	                         }
//	                     });
//	                 } finally {
//							file.close();
//						}
//						}
//					bizlog.debug("屹通" + filename6 + "文件产生完成");
//				}
//		/******* 生成屹通存款交易明细文件结束 ***********/
//		
//		
//		/******* 生成屹通账户信息数据文件开始 ***********/
//		// 获取文件生产路径
//				KnpParameter para7 = KnpParameterDao.selectOne_odb1("ZXYH", "accountinfo", "01", "%",
//						true);
//				String path7 = para7.getParm_value1() + lstrdtPath;
//				bizlog.debug("文件产生路径 path:[" + path7 + "]");
//
//				// 获取文件名
//				bizlog.debug("文件前缀 fileprefix:[" + para7.getParm_value3() + "]");
//				bizlog.debug("文件后缀 filesuffix:[" + para7.getParm_value4() + "]");
//				String filename7 = para7.getParm_value2();
//				bizlog.debug("文件完整路径 fullpath[" + path7 + filename7 + "]");
//				// 获取是否产生文件标志
//				String isCreateFlg7 = para7.getParm_value3();
//				if (CommUtil.equals(isCreateFlg7, "Y")) {
//				// 产生文件
//				final LttsFileWriter file2 = new LttsFileWriter(path7, filename7);
//				List<kna_cust> entities2 = null;
//				Params params = new Params();
//		        String namedSqlId = "";
//		        namedSqlId = DpAcctDao.namedsql_selCustacAcctstByAll;
////				entities2 = DpAcctDao.selCustacAcctstByAll(false);
//				if (true) {
//					file2.open();
//					try {
//						 DaoUtil.selectList(namedSqlId, params, new CursorHandler<kna_cust>() {
//	                         @Override
//	                         public boolean handle(int index, kna_cust entity) {
//								String custac = entity.getCustac();// 电子账号
//								BigDecimal  balance  = getBal(custac, BusiTools.getDefineCurrency());//历史余额
//								String webacstate = CommUtil.isNotNull(entity.getAcctst())?entity.getAcctst().toString():"" ; //电子账号状态 1--正常，2--关闭，3--睡眠，4--未生效
//								
//								buf.delete(0, buf.length());
//								buf.append(custac).append(fileSepa1).append(balance.toString()).append(fileSepa1).append(webacstate);
//								// 写文件
//								file2.write(buf.toString());
//								
//								return true;
//	                         }
//	                     });
//	                 } finally {
//						file2.close();
//					}
//				}
//				bizlog.debug("屹通" + filename7 + "文件产生完成");
//				}
//				
//				/******* 生成屹通基金明细文件开始 ***********/
//				// 获取文件生产路径
//				KnpParameter para8 = KnpParameterDao.selectOne_odb1("ZXYH", "OXYGENINFO", "01", "%",
//						true);
//				String path8 = para8.getParm_value1() + lstrdtPath;
//				bizlog.debug("文件产生路径 path:[" + path8 + "]");
//
//				// 获取文件名
//				bizlog.debug("文件前缀 fileprefix:[" + para8.getParm_value3() + "]");
//				bizlog.debug("文件后缀 filesuffix:[" + para8.getParm_value4() + "]");
//				String filename8 = para8.getParm_value2();
//				bizlog.debug("文件完整路径 fullpath[" + path8 + filename8 + "]");
//				// 获取是否产生文件标志
//				String isCreateFlg8 = para8.getParm_value3();
//				if (CommUtil.equals(isCreateFlg8, "Y")) {
//				// 产生文件
//				final LttsFileWriter file3 = new LttsFileWriter(path8, filename8);
////				List<FdaFundType> entities3 = null;
//				Params params = new Params();
//				String namedSqlId = DpAcctDao.namedsql_selOxygenInfoForYitong;
////				entities3 = DpAcctDao.selOxygenInfoForYitong(false);
//				
//					if (true) {
//						file3.open();
//						try {
//								DaoUtil.selectList(namedSqlId, params, new CursorHandler<FdaFundType>() {
//		                            @Override
//		                            public boolean handle(int index, FdaFundType entity) {
//									
//									String custac = entity.getCustac();// 电子账号
//									BigDecimal  balance  = entity.getOnlnbl().add(entity.getFrozbl());//账户总价值
//									String webacstate = CommUtil.isNotNull(entity.getAcctst())?entity.getAcctst().toString():"" ; //电子账号状态 1--正常，2--关闭，3--睡眠，4--未生效
//									
//									buf.delete(0, buf.length());
//									buf.append(custac).append(fileSepa1).append(balance.toString()).append(fileSepa1).append(webacstate);
//									// 写文件
//									file3.write(buf.toString());
//
//		                            return true;
//		                         }
//		                     });
//		                 } finally {
//							file3.close();
//						}
//					}
//				bizlog.debug("屹通" + filename8 + "文件产生完成");
//				}
//				/******* 生成屹通基金明细文件结束 ***********/
//			
//				
//				/******* 生成屹通保险明细文件开始 ***********/
//				// 获取文件生产路径
//				KnpParameter para9 = KnpParameterDao.selectOne_odb1("ZXYH", "INSURANCEINFO", "01", "%",
//						true);
//				String path9 = para9.getParm_value1() + lstrdtPath;
//				bizlog.debug("文件产生路径 path:[" + path9 + "]");
//
//				// 获取文件名
//				bizlog.debug("文件前缀 fileprefix:[" + para9.getParm_value3() + "]");
//				bizlog.debug("文件后缀 filesuffix:[" + para9.getParm_value4() + "]");
//				String filename9 = para9.getParm_value2();
//				bizlog.debug("文件完整路径 fullpath[" + path9 + filename9 + "]");
//				// 获取是否产生文件标志
//				String isCreateFlg9 = para9.getParm_value3();
//				if (CommUtil.equals(isCreateFlg9, "Y")) {
//				// 产生文件
//				final LttsFileWriter file4 = new LttsFileWriter(path9, filename9);
//				List<DfaHoldType> entities4 = null;
//				Params params = new Params();
//				String namedSqlId =DpAcctDao.namedsql_selInsurancefoForYitong;
////				entities4 = DpAcctDao.selInsurancefoForYitong(false);
//					if (true) {
//						file4.open();
//						try {
//							 DaoUtil.selectList(namedSqlId, params, new CursorHandler<DfaHoldType>() {
//		                         @Override
//		                         public boolean handle(int index, DfaHoldType entity) {
//								String custac = entity.getCustac();// 电子账号
//								BigDecimal  balance  = entity.getOnlnbl();//账户总价值
//								String webacstate = CommUtil.isNotNull(entity.getAcctst())?entity.getAcctst().toString():"" ; //电子账号状态 1--正常，2--关闭，3--睡眠，4--未生效
//								
//								buf.delete(0, buf.length());
//								buf.append(custac).append(fileSepa1).append(balance.toString()).append(fileSepa1).append(webacstate);
//								// 写文件
//								file4.write(buf.toString());
//								return true;
//		                         }
//		                     });
//		                     } finally {
//							file4.close();
//						}
//					}
//					bizlog.debug("屹通" + filename9 + "文件产生完成");
//				}
//
//				/******* 生成屹通保险明细文件结束 ***********/
//				
//		
//		/******* 生成屹通传统定期到期支取明细文件开始 ***********/
//
//		// 获取文件生产路径
//		KnpParameter para10 = KnpParameterDao.selectOne_odb1("ZXYH", "DINCINFO", "01", "%",
//				true);
//		String path10 = para10.getParm_value1() + lstrdtPath;
//		bizlog.debug("文件产生路径 path:[" + path10 + "]");
//
//		// 获取文件名
//		bizlog.debug("文件前缀 fileprefix:[" + para10.getParm_value3() + "]");
//		bizlog.debug("文件后缀 filesuffix:[" + para10.getParm_value4() + "]");
//		String filename10 = para10.getParm_value2();
//		bizlog.debug("文件完整路径 fullpath[" + path10 + filename10 + "]");
//		// 获取是否产生文件标志
//		String isCreateFlg10 = para10.getParm_value3();
//		if (CommUtil.equals(isCreateFlg10, "Y")) {
//		// 产生文件
//		LttsFileWriter file5 = new LttsFileWriter(path10, filename10);
//		List<DincinfoType> entities5 = null;
//		String sysdate = DateTools2.getDateInfo().getJiaoyirq();
//		entities5 = DpAcctDao.selDincinfoForYitong(sysdate, false);//取当前交易日期(日切过后的日期)
//			if (true) {
//				file5.open();
//				try {
//					if (CommUtil.isNotNull(entities5)) {
//						DincinfoType entity = SysUtil.getInstance(DincinfoType.class);
//						for (int i = 0; i < entities5.size(); i++) {
//							entity = entities5.get(i);
//							
//							String custac = entity.getCustac();// 电子账号
//							String transtype = "";//交易类型编码
//							if (CommUtil.equals(entity.getProdcd(), "010010003")) {
//								transtype = "DZ688";
//							}else if (CommUtil.equals(entity.getProdcd(), "010010004")) {
//								transtype = "DZ788";
//							}
//							String reccardno = entity.getCustac();// 收款方账号
//							String transstate = "";//交易结果
//							if (entity.getPmcrac() == E_PMCRAC.NORMAL) {
//								transstate = "01";
//							}else {
//								transstate = "02";
//							}
//							String transq = entity.getTransq();//交易流水
//							String tranam = entity.getTranam().toString();//交易金额
//							String trandt = entity.getTrandt();//交易日期
//							String trantm = entity.getTrantm().toString();//交易时间
//							String prodcd = entity.getProdcd();//产品编号
//							
//							buf.delete(0, buf.length());
//							buf.append(custac).append(fileSepa1).append(transtype).append(fileSepa1)
//							.append(reccardno).append(fileSepa1).append(transstate).append(fileSepa1)
//							.append(transq).append(fileSepa1).append(tranam).append(fileSepa1).append(trandt)
//							.append(fileSepa1).append(trantm.substring(0, 6)).append(fileSepa1).append(prodcd);
//							
//							// 写文件
//							file5.write(buf.toString());
//						}
//					}
//	
//				} finally {
//					file5.close();
//				}
//			}
//			bizlog.debug("屹通" + filename10 + "文件产生完成");
//		}
//
//		/******* 生成屹通传统定期到期支取明细文件开始 ***********/
		
	}
	
	
	
	/**
	 * 获取上日余额
	 */
	public static BigDecimal getBal(String custac,String crcycd){
		String today = CommTools.getBaseRunEnvs().getTrxn_date();
		BigDecimal bal = BigDecimal.ZERO;
		//查询活期产品资金产品池
		
		bal = DpAcctDao.selTotalValueForAccount(crcycd, today, custac, false);
		bal = bal.divide(new BigDecimal(1) ,2 ,BigDecimal.ROUND_HALF_UP) ;
		
//		List<KupDptd> dptds = KupDptdDao.selectAll_odb1(CommTools.getBaseRunEnvs().getBusi_org_id(), false);
//		List<KupDptd> dptds = DpBaseProdDao.selKupDptdAll(CommTools.getCenterFrdm(), false);
//		for(KupDptd dptd : dptds){
//			String prodcd = dptd.getProdcd();
//			if(E_PRODTP.DEPO == dptd.getProdtp()){
//				//存款
//				//查询活期账户
//				List<kna_acct> accts = Kna_acctDao.selectAll_odb3(crcycd ,prodcd, custac, false);
//				for(kna_acct acct : accts){
//					if(!CommUtil.equals(acct.getUpbldt(),today)){
//						bal = bal.add(acct.getOnlnbl());
//					}else{
//						bal = bal.add(acct.getLastbl());
//					}
//				}
//				//查询定期账户
//				List<kna_fxac> fxacs = Kna_fxacDao.selectAll_odb2(crcycd ,prodcd, custac, false);
//				for(kna_fxac fxac : fxacs){
//					if(!CommUtil.equals(fxac.getUpbldt(),today)){
//						bal = bal.add(fxac.getOnlnbl());
//					}else{
//						bal = bal.add(fxac.getLastbl());
//					}
//				}
//			}
//		}
		
		
		
		
		return bal;
	}
	

	/**
	 * 生成ChinaPay对账文件
	 */
	@Override
	public void genChinaPayFiles(String bftrandt) {

		// 获取文件生产路径 和时间参数
		KnpParameter para = KnpParameterDao.selectOne_odb1("FRONT", "chinapay", "%", "%",
				true);
		String path = para.getParm_value1();
		int trantm = Integer.valueOf(para.getParm_value2());
		bizlog.debug("文件产生路径 path:[" + path + "]");
		bizlog.debug("文件产生时间trantm:[" + trantm + "]");
		// 获取文件名
		bizlog.debug("文件前缀 fileprefix:[" + para.getParm_value3() + "]");
		bizlog.debug("文件后缀 filesuffix:[" + para.getParm_value4() + "]");
		String filename = para.getParm_value3() + bftrandt + para.getParm_value4();
		bizlog.debug("文件名 filename[" + filename + "]");
		bizlog.debug("文件完整路径 fullpath[" + path + filename + "]");

		// 产生文件
		final LttsFileWriter file = new LttsFileWriter(path, filename);
		file.open();
		try {
			final int cnt1 = DpAcctDao.selChinaPayCnt(bftrandt,false); //电子账户chinapay出入金
			final int cnt2 = DpAcctDao.selXmChinaPayCnt(bftrandt,false); //小马内部户chinapay出入金
			final int cnt = cnt1+cnt2;
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt));
			} else {
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt));
				DaoUtil.selectList(DpAcctDao.namedsql_selChinaPay, bftrandt, new CursorHandler<KnlIobl>(){
					@Override
					public boolean handle(int index, KnlIobl entity) {
						buf.delete(0, buf.length());
						buf.append(entity.getTrandt()).append(fileSepa2);// 交易日期1
						buf.append(entity.getTransq()).append(fileSepa2);// 主交易流水2
						buf.append(entity.getTrantm()).append(fileSepa2);// 交易时间3
						buf.append(entity.getPrcscd()).append(fileSepa2);// 处理码4
						buf.append(entity.getCuacno()).append(fileSepa2);// 电子账户5
						buf.append(entity.getTranam().toString()).append(fileSepa2);// 交易金额6
						buf.append(entity.getStatus().toString()).append(fileSepa2);// 交易状态7
						buf.append(entity.getServdt()).append(fileSepa2);// 渠道日期8
						buf.append(entity.getServsq());// 渠道流水9
						if(index==cnt){
							file.writeLastLine(buf.toString());
						}else{
							file.write(buf.toString());
						}
						return true;
					}
					
				});
				
				DaoUtil.selectList(DpAcctDao.namedsql_selXmChinaPay, bftrandt, new CursorHandler<IoXmCpIoBill>(){
					@Override
					public boolean handle(int index, IoXmCpIoBill entity) {
						buf.delete(0, buf.length());
						buf.append(entity.getTrandt()).append(fileSepa2);// 交易日期1
						buf.append(entity.getTransq()).append(fileSepa2);// 主交易流水2
						buf.append(entity.getTrantm()).append(fileSepa2);// 交易时间3
						buf.append(entity.getPrcscd()).append(fileSepa2);// 处理码4
						buf.append(entity.getAcctno()).append(fileSepa2);// 电子账户5
						buf.append(entity.getTranam().toString()).append(fileSepa2);// 交易金额6
						buf.append(entity.getStatus().toString()).append(fileSepa2);// 交易状态7
						buf.append(entity.getServdt()).append(fileSepa2);// 渠道日期8
						buf.append(entity.getServsq());// 渠道流水9
						if(index+cnt1==cnt){
							file.writeLastLine(buf.toString());
						}else{
							file.write(buf.toString());
						}
						return true;
					}
					
				});
				
			}
		} finally {
			// 关闭文件
			file.close();
		}
	}

	/**
	 * 生成银联对账文件  cups_日期.txt
	 */
	@Override
	public void genUnionPayFiles(String lstrdt) {
		// 获取文件生产路径 和时间参数
		KnpParameter para = KnpParameterDao.selectOne_odb1("FRONT", "unionpay", "%", "%",
				true);
		String path = para.getParm_value1();
		bizlog.debug("文件产生路径 path:[" + path + "]");

		// 获取文件名
		bizlog.debug("文件前缀 fileprefix:[" + para.getParm_value3() + "]");
		bizlog.debug("文件后缀 filesuffix:[" + para.getParm_value4() + "]");
		String filename = para.getParm_value3() + lstrdt + para.getParm_value4();
		bizlog.debug("文件完整路径 fullpath[" + path + filename + "]");
//		 String jiaoyirq = CommTools.getBaseRunEnvs().getJiaoyirq();
		// 产生文件
		final LttsFileWriter file = new LttsFileWriter(path, filename);
		file.open();
		try {
			final int cnt = DpAcctDao.selUnionPayCnt(lstrdt, false);
			if (0 == cnt) {
				// 插入文件第一行，总记录数
				file.writeLastLine(Integer.toString(cnt));
			} else { 
				// 插入文件第一行，总记录数
				file.write(Integer.toString(cnt));
				// 插入明细记录
				DaoUtil.selectList(DpAcctDao.namedsql_selUnionPay, lstrdt, new CursorHandler<KnlIobl>(){
					@Override
					public boolean handle(int index, KnlIobl entity) {
						buf.delete(0, buf.length());
						buf.append(entity.getTrandt()).append(fileSepa2);// 交易日期1
						buf.append(entity.getTransq()).append(fileSepa2);// 主交易流水2 
						buf.append(entity.getTrantm()).append(fileSepa2);// 交易时间3
						buf.append(entity.getPrcscd()).append(fileSepa2);// 处理码4
						buf.append(entity.getCuacno()).append(fileSepa2);// 电子账户5
						buf.append(entity.getTranam().toString()).append(fileSepa2);// 交易金额6
						buf.append(entity.getStatus().toString()).append(fileSepa2);// 交易状态7
						buf.append(entity.getServdt()).append(fileSepa2);// 渠道日期8
						buf.append(entity.getServsq());// 渠道流水9
						if (CommUtil.equals(entity.getFromtp().getValue(), "0")) {
							if (index == cnt) {
								// 写文件
								file.writeLastLine(buf.toString());
							} else {
								// 写文件
								file.write(buf.toString());
							}
						}
						return true;
					}
					
				});
				/*List<KnlIobl> entities = DpAcctDao.selUnionPay(lstrdt, false);
				KnlIobl entity = SysUtil.getInstance(KnlIobl.class);
				if (CommUtil.isNotNull(entities)) {
					for (int i = 0; i < entities.size(); i++) {
						entity = entities.get(i);
						buf.delete(0, buf.length());
						buf.append(entity.getTrandt()).append(fileSepa2);// 交易日期1
						buf.append(entity.getTransq()).append(fileSepa2);// 主交易流水2 
						buf.append(entity.getTrantm()).append(fileSepa2);// 交易时间3
						buf.append(entity.getPrcscd()).append(fileSepa2);// 处理码4
						buf.append(entity.getCuacno()).append(fileSepa2);// 电子账户5
						buf.append(entity.getTranam().toString()).append(fileSepa2);// 交易金额6
						buf.append(entity.getStatus().toString()).append(fileSepa2);// 交易状态7
						buf.append(entity.getServdt()).append(fileSepa2);// 渠道日期8
						buf.append(entity.getServsq());// 渠道流水9
						if (CommUtil.equals(entity.getFromtp().getValue(), "0")) {
							if (i == entities.size() - 1) {
								// 写文件
								file.writeLastLine(buf.toString());
							} else {
								// 写文件
								file.write(buf.toString());
							}
						}
					}
				}*/
			}
		} finally {
			// 关闭文件
			file.close();
		}
	}

	/**
	 * 生成屹通存款交易明细文件
	 */

	@Override
	public void genYiTongFiles(String jiaoyirq, String lstrdt) {		
		
		/******* 生成屹通存款交易明细文件开始 ***********/
		
		// 产生文件的日期目录
				String lstrdtPath = lstrdt + "/";
		
		// 获取文件生产路径
		KnpParameter para6 = KnpParameterDao.selectOne_odb1("ZXYH", "transdetail", "01", "%",
				true);
		String path6 = para6.getParm_value1() + lstrdtPath;
		bizlog.debug("文件产生路径 path:[" + path6 + "]");

		// 获取文件名
		bizlog.debug("文件前缀 fileprefix:[" + para6.getParm_value3() + "]");
		bizlog.debug("文件后缀 filesuffix:[" + para6.getParm_value4() + "]");
		String filename6 = para6.getParm_value2();
		bizlog.debug("文件完整路径 fullpath[" + path6 + filename6 + "]");
		// 获取是否产生文件标志
		String isCreateFlg6 = para6.getParm_value3();
			if (CommUtil.equals(isCreateFlg6, "Y")) {
				// 产生文件
				final LttsFileWriter file = new LttsFileWriter(path6, filename6);
//				List<KnlBill> entities = null;
				String startDate = DateTools2.dateAdd (-10, lstrdt);//lstrdt往后10天
				Params params = new Params();
				params.add("datetm", lstrdt);
	            String namedSqlId = "";
				params.add("startDate", startDate);
				
				bizlog.debug("上日交易日期退后十天的日期是：["+startDate+"]");
				namedSqlId = DpAcctDao.namedsql_selKnlBillByDateForYitong;
//				entities = DpAcctDao.selKnlBillByDateForYitong(startDate, lstrdt,false);
		
				if (true) {
					file.open();
					try {
						 DaoUtil.selectList(namedSqlId, params, new CursorHandler<KnlBill>() {
	                         @Override
	                         public boolean handle(int index, KnlBill entity) {
								String ussqno = CommUtil.nvl(entity.getUssqno(), "");// 交易记录号
								String corrtg =CommUtil.isNotNull(entity.getCorrtg())?entity.getCorrtg().toString():"" ;// 
								String chongzhengflag = "";		//交易结果  01--成功，02--失败
								if (CommUtil.equals(corrtg, "0")) {
									chongzhengflag = "01";
								}else {
									chongzhengflag = "02";
								}
								String tranam =CommUtil.isNotNull(entity.getTranam())?entity.getTranam().toString():"" ;// 交易金额
		
								buf.delete(0, buf.length());
								buf.append(ussqno).append(fileSepa1).append(chongzhengflag).append(fileSepa1).append(tranam);
								// 写文件
								file.write(buf.toString());
								return true;
	                         }
	                     });
	                 } finally {
							file.close();
						}
						}
					bizlog.debug("屹通" + filename6 + "文件产生完成");
				}
		/******* 生成屹通存款交易明细文件结束 ***********/
		
		
		/******* 生成屹通账户信息数据文件开始 ***********/
		// 获取文件生产路径
				KnpParameter para7 = KnpParameterDao.selectOne_odb1("ZXYH", "accountinfo", "01", "%",
						true);
				String path7 = para7.getParm_value1() + lstrdtPath;
				bizlog.debug("文件产生路径 path:[" + path7 + "]");

				// 获取文件名
				bizlog.debug("文件前缀 fileprefix:[" + para7.getParm_value3() + "]");
				bizlog.debug("文件后缀 filesuffix:[" + para7.getParm_value4() + "]");
				String filename7 = para7.getParm_value2();
				bizlog.debug("文件完整路径 fullpath[" + path7 + filename7 + "]");
				// 获取是否产生文件标志
				String isCreateFlg7 = para7.getParm_value3();
				if (CommUtil.equals(isCreateFlg7, "Y")) {
				// 产生文件
				final LttsFileWriter file2 = new LttsFileWriter(path7, filename7);
//				List<kna_cust> entities2 = null;
				Params params = new Params();
		        String namedSqlId = "";
		        namedSqlId = DpAcctDao.namedsql_selCustacAcctstByAll;
//				entities2 = DpAcctDao.selCustacAcctstByAll(false);
				if (true) {
					file2.open();
					try {
						 DaoUtil.selectList(namedSqlId, params, new CursorHandler<IoCaKnaCust>() {
	                         @Override
	                         public boolean handle(int index, IoCaKnaCust entity) {
								String custac = entity.getCustac();// 电子账号
								BigDecimal  balance  = getBal(custac, BusiTools.getDefineCurrency());//历史余额
								String webacstate = CommUtil.isNotNull(entity.getAcctst())?entity.getAcctst().toString():"" ; //电子账号状态 1--正常，2--关闭，3--睡眠，4--未生效
								
								buf.delete(0, buf.length());
								buf.append(custac).append(fileSepa1).append(balance.toString()).append(fileSepa1).append(webacstate);
								// 写文件
								file2.write(buf.toString());
								
								return true;
	                         }
	                     });
	                 } finally {
						file2.close();
					}
				}
				bizlog.debug("屹通" + filename7 + "文件产生完成");
				}
				
				/******* 生成屹通基金明细文件开始 ***********/
				// 获取文件生产路径
				KnpParameter para8 = KnpParameterDao.selectOne_odb1("ZXYH", "OXYGENINFO", "01", "%",
						true);
				String path8 = para8.getParm_value1() + lstrdtPath;
				bizlog.debug("文件产生路径 path:[" + path8 + "]");

				// 获取文件名
				bizlog.debug("文件前缀 fileprefix:[" + para8.getParm_value3() + "]");
				bizlog.debug("文件后缀 filesuffix:[" + para8.getParm_value4() + "]");
				String filename8 = para8.getParm_value2();
				bizlog.debug("文件完整路径 fullpath[" + path8 + filename8 + "]");
				// 获取是否产生文件标志
				String isCreateFlg8 = para8.getParm_value3();
				if (CommUtil.equals(isCreateFlg8, "Y")) {
				// 产生文件
				final LttsFileWriter file3 = new LttsFileWriter(path8, filename8);
//				List<FdaFundType> entities3 = null;
				Params params = new Params();
				String namedSqlId = DpAcctDao.namedsql_selOxygenInfoForYitong;
//				entities3 = DpAcctDao.selOxygenInfoForYitong(false);
				
					if (true) {
						file3.open();
						try {
								DaoUtil.selectList(namedSqlId, params, new CursorHandler<FdaFundType>() {
		                            @Override
		                            public boolean handle(int index, FdaFundType entity) {
									
									String custac = entity.getCustac();// 电子账号
									BigDecimal  balance  = entity.getOnlnbl().add(entity.getFrozbl());//账户总价值
									String webacstate = CommUtil.isNotNull(entity.getAcctst())?entity.getAcctst().toString():"" ; //电子账号状态 1--正常，2--关闭，3--睡眠，4--未生效
									
									buf.delete(0, buf.length());
									buf.append(custac).append(fileSepa1).append(balance.toString()).append(fileSepa1).append(webacstate);
									// 写文件
									file3.write(buf.toString());

		                            return true;
		                         }
		                     });
		                 } finally {
							file3.close();
						}
					}
				bizlog.debug("屹通" + filename8 + "文件产生完成");
				}
				/******* 生成屹通基金明细文件结束 ***********/
			
				
				/******* 生成屹通保险明细文件开始 ***********/
				// 获取文件生产路径
				KnpParameter para9 = KnpParameterDao.selectOne_odb1("ZXYH", "INSURANCEINFO", "01", "%",
						true);
				String path9 = para9.getParm_value1() + lstrdtPath;
				bizlog.debug("文件产生路径 path:[" + path9 + "]");

				// 获取文件名
				bizlog.debug("文件前缀 fileprefix:[" + para9.getParm_value3() + "]");
				bizlog.debug("文件后缀 filesuffix:[" + para9.getParm_value4() + "]");
				String filename9 = para9.getParm_value2();
				bizlog.debug("文件完整路径 fullpath[" + path9 + filename9 + "]");
				// 获取是否产生文件标志
				String isCreateFlg9 = para9.getParm_value3();
				if (CommUtil.equals(isCreateFlg9, "Y")) {
				// 产生文件
				final LttsFileWriter file4 = new LttsFileWriter(path9, filename9);
//				List<DfaHoldType> entities4 = null;
				Params params = new Params();
				String namedSqlId =DpAcctDao.namedsql_selInsurancefoForYitong;
//				entities4 = DpAcctDao.selInsurancefoForYitong(false);
					if (true) {
						file4.open();
						try {
							 DaoUtil.selectList(namedSqlId, params, new CursorHandler<DfaHoldType>() {
		                         @Override
		                         public boolean handle(int index, DfaHoldType entity) {
								String custac = entity.getCustac();// 电子账号
								BigDecimal  balance  = entity.getOnlnbl();//账户总价值
								String webacstate = CommUtil.isNotNull(entity.getAcctst())?entity.getAcctst().toString():"" ; //电子账号状态 1--正常，2--关闭，3--睡眠，4--未生效
								
								buf.delete(0, buf.length());
								buf.append(custac).append(fileSepa1).append(balance.toString()).append(fileSepa1).append(webacstate);
								// 写文件
								file4.write(buf.toString());
								return true;
		                         }
		                     });
		                     } finally {
							file4.close();
						}
					}
					bizlog.debug("屹通" + filename9 + "文件产生完成");
				}

				/******* 生成屹通保险明细文件结束 ***********/
				
		
		/******* 生成屹通传统定期到期支取明细文件开始 ***********/

		// 获取文件生产路径
		KnpParameter para10 = KnpParameterDao.selectOne_odb1("ZXYH", "DINCINFO", "01", "%",
				true);
		String path10 = para10.getParm_value1() + lstrdtPath;
		bizlog.debug("文件产生路径 path:[" + path10 + "]");

		// 获取文件名
		bizlog.debug("文件前缀 fileprefix:[" + para10.getParm_value3() + "]");
		bizlog.debug("文件后缀 filesuffix:[" + para10.getParm_value4() + "]");
		String filename10 = para10.getParm_value2();
		bizlog.debug("文件完整路径 fullpath[" + path10 + filename10 + "]");
		// 获取是否产生文件标志
		String isCreateFlg10 = para10.getParm_value3();
		if (CommUtil.equals(isCreateFlg10, "Y")) {
		// 产生文件
		final LttsFileWriter file5 = new LttsFileWriter(path10, filename10);
//		List<DincinfoType> entities5 = null;
		String sysdate = DateTools2.getDateInfo().getSystdt();
		Params params = new Params();
		params.add("trandt", sysdate);
        String namedSqlId = "";
        namedSqlId = DpAcctDao.namedsql_selDincinfoForYitong;
//		entities5 = DpAcctDao.selDincinfoForYitong(sysdate, false);//取当前交易日期(日切过后的日期)
			if (true) {
				file5.open();
				try {
//						DincinfoType entity = SysUtil.getInstance(DincinfoType.class);
						 DaoUtil.selectList(namedSqlId, params, new CursorHandler<DincinfoType>() {
	                         @Override
	                         public boolean handle(int index, DincinfoType entity) {
							
							String custac = entity.getCustac();// 电子账号
							String transtype = "";//交易类型编码
							if (CommUtil.equals(entity.getProdcd(), "010010003")) {
								transtype = "DZ688";
							}else if (CommUtil.equals(entity.getProdcd(), "010010004")) {
								transtype = "DZ788";
							}
							String reccardno = entity.getCustac();// 收款方账号
							String transstate = "";//交易结果
							if (entity.getPmcrac() == E_PMCRAC.NORMAL) {
								transstate = "01";
							}else {
								transstate = "02";
							}
							String transq = entity.getTransq();//交易流水
							String tranam = entity.getTranam().toString();//交易金额
							String trandt = entity.getTrandt();//交易日期
							String trantm = entity.getTrantm().toString();//交易时间
							String prodcd = entity.getProdcd();//产品编号
							
							buf.delete(0, buf.length());
							buf.append(custac).append(fileSepa1).append(transtype).append(fileSepa1)
							.append(reccardno).append(fileSepa1).append(transstate).append(fileSepa1)
							.append(transq).append(fileSepa1).append(tranam).append(fileSepa1).append(trandt)
							.append(fileSepa1).append(trantm.substring(0, 6)).append(fileSepa1).append(prodcd);
							
							// 写文件
							file5.write(buf.toString());
							return true;
	                         }
						 
	                     });
	                     } finally {
					file5.close();
	                     }
			}
			
			bizlog.debug("屹通" + filename10 + "文件产生完成");
		}
	
		/******* 生成屹通传统定期到期支取明细文件结束 ***********/
		
		/******* 生成屹通END文件开始 ***********/		
		/**
		 * 生成日期end文件
		 * 
		 * @param lstrdt
		 *            统计日期
		 */
	}
  
}
