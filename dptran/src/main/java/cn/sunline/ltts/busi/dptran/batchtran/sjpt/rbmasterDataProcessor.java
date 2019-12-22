package cn.sunline.ltts.busi.dptran.batchtran.sjpt;

import java.io.File;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.dp.namedsql.sjpt.sjptdtDao;
import cn.sunline.ltts.busi.dp.type.sjpt.sjptdt.rbaccountmasterInfo;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_OFCHAR;
/**
 * 数据平台供述-存款主账户
 * @author Administrator
 *
 */
public class rbmasterDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbmaster.Input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbmaster.Property> {
	private static final BizLog bizlog = BizLogUtil
			.getBizLog(rbmasterDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbmaster.Input input, cn.sunline.ltts.busi.dptran.batchtran.sjpt.intf.Rbmaster.Property property) {
		//从公共参数表中获取需要写出的文件路径信息
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.rbmaster", "rbmasterFile", "rbmasterData", "%", true);

		/*KnpParameter sqNoKnpParameter = KnpParameterDao.selectOne_odb1("Sjpt.sqno", "sqno", "number", "%", true);
		String pmval1 = sqNoKnpParameter.getParm_value1();//当前值
*/		// 产生文件的日期目录
		final String trandt = DateTools2.getDateInfo().getLastdt();//获取上次交易日期
		// 产生文件的日期目录
		String filePath = tblKnpParameter.getParm_value1() + File.separator + trandt;
		//String filePath = "E:\\123\\";
		String fileName = tblKnpParameter.getParm_value2() + E_OFCHAR.UND + trandt + E_OFCHAR.UND + tblKnpParameter.getParm_value3() + E_OFCHAR.UND + "0001" + tblKnpParameter.getParm_value4();
		 
		bizlog.debug("贷款还款计划明细文件:[" + filePath + fileName + "]");
		 
		// 获取是否产生文件标志
		final LttsFileWriter file = new LttsFileWriter(filePath, fileName);
		
		Params params = new Params();
		params.add("trandt", trandt); 
		
		if (true) {
			file.open();
			try {
				final StringBuilder end = new StringBuilder("0");
				DaoUtil.selectList(sjptdtDao.namedsql_selRbAccountMaster, params, new CursorHandler<rbaccountmasterInfo>() {

					@Override
					public boolean handle(int arg0, rbaccountmasterInfo arg1) {
						StringBuffer lnreq = new StringBuffer();
						lnreq.append(trandt).append(E_OFCHAR.VER);			//数据日期
						String cardno		 = arg1.getCardno();		 	//绑定卡号	- 主账号	
						lnreq.append(cardno).append(E_OFCHAR.VER);							
						String brchno		 = arg1.getBrchno();		 	//归属机构	
						lnreq.append(brchno).append(E_OFCHAR.VER);			
						String custno		 = arg1.getCustno();		 	//客户号	
						lnreq.append(custno).append(E_OFCHAR.VER);			
						String opendt		 = arg1.getOpendt();		 	//开户日期	
						lnreq.append(opendt).append(E_OFCHAR.VER);			
						String closdt		 = arg1.getClosdt();		 	//销户日期	
						lnreq.append(closdt).append(E_OFCHAR.VER);			
						String sunacs		 = arg1.getSunacs();		 	//账号状态	
						lnreq.append(sunacs).append(E_OFCHAR.VER);			
						String acctst		 = arg1.getAcctst();		 	//主账户状态
						lnreq.append(acctst).append(E_OFCHAR.VER);			
						String withdt		 = arg1.getWithdt();		 	//支取类型		
						lnreq.append(withdt).append(E_OFCHAR.VER);		
						String mainpt		 = arg1.getMainpt();		 	//主产品类型	
						lnreq.append(mainpt).append(E_OFCHAR.VER);		
						String sperna		 = arg1.getSperna();		 	//代理人名称	
						lnreq.append(sperna).append(E_OFCHAR.VER);		
						String sperit		 = arg1.getSperit();		 	//代理人证件类型
						lnreq.append(sperit).append(E_OFCHAR.VER);		
						String sperid		 = arg1.getSperid();		 	//代理人号码		
						lnreq.append(sperid).append(E_OFCHAR.VER);	
						String idckrt		 = arg1.getIdckrt();		 	//身份核查结果		
						lnreq.append(idckrt).append(E_OFCHAR.VER);	
						String dealty		 = arg1.getDealty();		 	//无法核实的处理方式
						lnreq.append(dealty).append(E_OFCHAR.VER);	
						String verire		 = arg1.getVerire();		 	//无法核实原因			
						lnreq.append(verire).append(E_OFCHAR.VER);
						String verida		 = arg1.getVerida();		 	//核实日期		
						lnreq.append(verida).append(E_OFCHAR.VER);		
						String idtftp		 = arg1.getIdtftp();		 	//证件类型		
						lnreq.append(idtftp).append(E_OFCHAR.VER);		
						String idtfno		 = arg1.getIdtfno();		 	//证件号码		
						lnreq.append(idtfno).append(E_OFCHAR.VER);		
						/*String verify_opper	 = arg1.getverify_opper	 		
						String verify_branch = arg1.getverify_branch  				
						*/					
						String openus		 = arg1.getOpenus();		 	
						lnreq.append(openus).append(E_OFCHAR.VER);			//核实柜员		
						lnreq.append(brchno).append(E_OFCHAR.VER);			//核实网点	
						lnreq.append(openus).append(E_OFCHAR.VER);			//开户柜员
						String closus		 =arg1.getClosus();		 		//销户柜员	
						lnreq.append(closus).append(E_OFCHAR.VER);				
						String fcflag		 = arg1.getFcflag();		 	//定活标志	
						lnreq.append(fcflag).append(E_OFCHAR.VER);				
						String actind		 = arg1.getActind();		 	//是否对外开通标志		
						lnreq.append(actind).append(E_OFCHAR.VER);	
						String onefol		 = arg1.getOnefol();		 	//是否一本通			
						lnreq.append(onefol).append(E_OFCHAR.VER);	
						String fintyp		 = arg1.getFintyp();		 	//财政账户类型	
						lnreq.append(fintyp).append(E_OFCHAR.VER);			
						String finbrt		 = arg1.getFinbrt();		 	//金融机构类型		
						lnreq.append(finbrt).append(E_OFCHAR.VER);		
						String acctke		 = arg1.getAcctke();		 	//保证金结算帐号		
						lnreq.append(acctke).append(E_OFCHAR.VER);	
						lnreq.append(opendt).append(E_OFCHAR.VER);			//启用日期
						String accttp		 = arg1.getAccttp();		 	//账户类型				
						lnreq.append(accttp).append(E_OFCHAR.VER);	
						String trandt		 = arg1.getTrandt();		 	//转睡眠户日期		
						lnreq.append(trandt).append(E_OFCHAR.VER);		
						String bkupdt		 = arg1.getBkupdt();		 	//转久悬日期		
						lnreq.append(bkupdt).append(E_OFCHAR.VER);		
						String actiop		 = arg1.getActiop();		 	//激活柜员	
						lnreq.append(actiop).append(E_OFCHAR.VER);				
						String actibr		 = arg1.getActibr();		 	//激活机构		
						lnreq.append(actibr).append(E_OFCHAR.VER);			
						String actida		 = arg1.getActida();		 	//激活日期		
						lnreq.append(actida).append(E_OFCHAR.VER);			
						String longsf		 = arg1.getLongsf();		 	//不动户/久悬户标识	
						lnreq.append(longsf).append(E_OFCHAR.VER);			
						
						file.write(lnreq.toString());
						
						if (end.toString().length() > 0) {
							end.delete(0, end.toString().length());//数据重置
						}
						end.append(arg0);
						return true;
					}
				});
				file.write("END" + E_OFCHAR.VER + end.toString() + E_OFCHAR.VER);
				/*String okFileName = tblKnpParameter.getParm_value2() + ".ok";
				LttsFileWriter fileOk = new LttsFileWriter(path1, okFileName);
				fileOk.open();
				try{
					fileOk.write(trandt);
				}finally{
					fileOk.close();
				}*/
			} finally {
				file.close();
			}
			bizlog.debug("数据平台供述-存款主账户 ：" + fileName + "文件产生完成");
		}
		 
	}

}


