package cn.sunline.ltts.busi.dptran.batchtran.lzbank;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.ca.base.DecryptConstant;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.dp.servicetype.DpFileNotify;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KnbPidl;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcct;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaAcctDao;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxac;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMain.KnaFxacDao;
import cn.sunline.ltts.busi.dp.type.DpYhtType;
import cn.sunline.ltts.busi.dp.type.DpYhtType.Attachments;
import cn.sunline.ltts.busi.dp.type.DpYhtType.DpAcctno;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDYTTP;
/**
 * 存款子账户明细文件
 *
 */

public class dlfileDataProcessor extends
BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Dlfile.Input, cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Dlfile.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(dlfileDataProcessor.class);
	
	private static final String fileSepa1 = "|@|";//文件分隔符
	private static BizLog biz = BizLogUtil.getBizLog(dlfileDataProcessor.class);
	BigDecimal lastbl = BigDecimal.ZERO; //上日账户余额
	E_DPACST acctst = E_DPACST.NORMAL; //账户状态
	String isexpire = "00"; //是否到期，默认未到期
	E_IDTFTP idtftp = E_IDTFTP.SFZ; //证件类型
	E_IDYTTP idtype = E_IDYTTP.SFZ; //银户通对应证件类型
	String idtfno = ""; //证件号码
	String prodcd = ""; //产品编号
	BigDecimal cabrin = BigDecimal.ZERO; //到期利息
	String upbldt = ""; //余额更新日期
	String chckdt = DateTools2.getDateInfo().getLastdt();//上次交易日期
	String trandt = DateTools2.getDateInfo().getSystdt();//对账日期
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Dlfile.Input input, cn.sunline.ltts.busi.dptran.batchtran.lzbank.intf.Dlfile.Property property) {

		KnpParameter para = KnpParameterDao.selectOne_odb1("YHT", "dlfile","file","959300", false);
		String path = para.getParm_value2()+trandt+File.separator; //绝对路径
		String fileName = para.getParm_value1()+trandt+para.getParm_value4();
		List<DpAcctno> acctnos = CaDao.selKnlBillByDate(chckdt, false);
		String size = String.valueOf(acctnos.size()) ;//总条数
		biz.debug("<<<<<<<<<<<<<<<<<对账文件名>>>>>>>>>>>>>>>>>"+fileName);
		String encoding=para.getParm_value5();//文件编码
		final LttsFileWriter file = new LttsFileWriter(path, fileName,encoding);
		file.open();
		biz.debug("<<<<<<<<<<<<<<<<<开始写入文件>>>>>>>>>>>>>>>>>");
		Params params = new Params();
		params.put("lastdt", chckdt);
		StringBuffer lineInfo2 = SysUtil.getInstance(StringBuffer.class);
		lineInfo2.append(chckdt).append(fileSepa1).append(size);//文件头
		file.write(lineInfo2.toString());

		try {
			DaoUtil.selectList(CaDao.namedsql_selKnlBillByDate, params, new CursorHandler<DpAcctno>() {
				@Override
				public boolean handle(int arg0, DpAcctno dpAcctno) {
					
					KnaAcct tblKnaAcct = KnaAcctDao.selectOne_odb1(dpAcctno.getAcctno(), false);
					if (CommUtil.isNotNull(tblKnaAcct)){
						acctst = tblKnaAcct.getAcctst();
						prodcd = tblKnaAcct.getProdcd();
						upbldt = tblKnaAcct.getUpbldt();
						if (!CommUtil.equals(upbldt, chckdt)){
							lastbl = tblKnaAcct.getLastbl();
						}else{
							lastbl = tblKnaAcct.getOnlnbl();
						}
//						CifCust tblCifCust = CifCustDao.selectOne_odb1(tblKnaAcct.getCustno(), false);
//						if (tblCifCust.getIdtftp() == E_IDTFTP.SFZ) {
//							idtype = E_IDYTTP.SFZ;
//						}
//						idtfno = tblCifCust.getIdtfno();
						if (E_DPACST.CLOSE == acctst) { //存款产品到期时会销户
							KnbPidl tblKnbPidl = DpAcctDao.selKnbPidlMaxNum(dpAcctno.getAcctno(), false);
							cabrin = new BigDecimal(""+tblKnbPidl.getRlintr()).setScale(2, BigDecimal.ROUND_HALF_UP);
							isexpire = "01"; //到期
						}else{
							isexpire = "00"; //未到期
						}
					}else{
						KnaFxac talKnaFxac = KnaFxacDao.selectOne_odb1(dpAcctno.getAcctno(), false);
						acctst = talKnaFxac.getAcctst();
						prodcd = talKnaFxac.getProdcd();
						upbldt = talKnaFxac.getUpbldt();
						if (!CommUtil.equals(upbldt, chckdt)){
							lastbl = talKnaFxac.getOnlnbl();
						}else{
							lastbl = talKnaFxac.getLastbl();
						}
//						CifCust tblCifCust = CifCustDao.selectOne_odb1(talKnaFxac.getCustno(), false);
//						if (tblCifCust.getIdtftp() == E_IDTFTP.SFZ) {
//							idtype = E_IDYTTP.SFZ;
//						}
//						idtfno = tblCifCust.getIdtfno();
						if (E_DPACST.CLOSE == acctst) { //存款产品到期时会销户
							KnbPidl tblKnbPidl = DpAcctDao.selKnbPidlMaxNum(dpAcctno.getAcctno(), false);
							cabrin = new BigDecimal(""+tblKnbPidl.getRlintr()).setScale(2, BigDecimal.ROUND_HALF_UP);
							isexpire = "01"; //到期
						}else{
							isexpire = "00"; //未到期
						}
							
					}
					StringBuffer lineInfo = SysUtil.getInstance(StringBuffer.class);
					lineInfo.append(idtype).append(fileSepa1);//证件类型
					lineInfo.append(idtfno).append(fileSepa1);//证件号码
					lineInfo.append(prodcd).append(fileSepa1);//产品编号
					lineInfo.append(dpAcctno.getAcctno()).append(fileSepa1);//客户存款子账户
					lineInfo.append(lastbl).append(fileSepa1);//上日账户余额                          
					lineInfo.append(cabrin).append(fileSepa1);//到期利息                   
					lineInfo.append(isexpire);//账户状态            
					file.write(lineInfo.toString());
					return true;
				}
			});
		} finally{
			biz.debug("<<<<<<<<<<<<<<<<<文件写入完成>>>>>>>>>>>>>>>>>");
			file.close();
		} 

		File file1 = new File(path+fileName);
		String fileSize = String.valueOf(file1.length());

		//外调文件生成通知服务实例化
		DpFileNotify fileNotify = SysUtil.getInstanceProxyByBind(
				DpFileNotify.class, "grfile");

		//文件附件区复合类型
		DpYhtType.Attachments attachment = SysUtil.getInstance(DpYhtType.Attachments.class);
		attachment.setFileName(fileName);
		attachment.setFileSize(fileSize);

		Options<Attachments> attachments = new DefaultOptions<Attachments>();
		attachments.add(attachment);
		//存款产品支取对账文件生成结果通知输入接口
		DpFileNotify.grfileNotifyInfo.InputSetter fileNotifyInfo = SysUtil.getInstance(DpFileNotify.grfileNotifyInfo.InputSetter.class);
		//存款产品支取对账文件生成结果通知输出接口
		DpFileNotify.grfileNotifyInfo.Output output = SysUtil
				.getInstance(DpFileNotify.grfileNotifyInfo.Output.class);

		fileNotifyInfo.setFileType(para.getParm_value3());
		fileNotifyInfo.setFileUrl(path);
		fileNotifyInfo.setTransTime(BusiTools.getBusiRunEnvs().getTrantm());
		fileNotifyInfo.setPrcscd("YHT09001");
		fileNotifyInfo.setAttachments(attachments);

		try {
			fileNotify.grfileNotifyImpl(fileNotifyInfo, output);
		} catch (Exception e) {
			// TODO: handle exception
			bizlog.debug("[%s]", e);
//			e.printStackTrace();
		}

	}

}


