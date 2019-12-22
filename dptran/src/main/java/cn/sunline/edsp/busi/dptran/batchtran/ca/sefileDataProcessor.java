
package cn.sunline.edsp.busi.dptran.batchtran.ca;

import java.io.File;
import java.math.BigDecimal;

import com.jfpal.legends.security.EncryptTool;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.clwj.msap.core.tools.DateTools;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.busi.dp.namedsql.ca.EdmAfterDayBatchDao;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccount;
import cn.sunline.edsp.busi.dp.tables.online.EdnAfter.EdmSettleAccountDao;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUPSST;
import cn.sunline.edsp.busi.dp.type.EnumType.JfDpEnumType.E_CUSTTP;
import cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.FileDataGroupNo;
import cn.sunline.ltts.apollo.ApolloConfigBean;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_OFCHAR;

/**
 * T1批量代发生成扣款文件
 * 
 * @author
 * @Date
 */

public class sefileDataProcessor extends
		AbstractBatchDataProcessor<cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Sefile.Input, cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Sefile.Property, cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.FileDataGroupNo> {
	private static BizLog biz = BizLogUtil.getBizLog(sefileDataProcessor.class);
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
	public void process(String jobId, int index, cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.FileDataGroupNo dataItem,
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Sefile.Input input, cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Sefile.Property property) {
		biz.debug("<<<<<<<<<<<<<<<<<文件开始>>>>>>>>>>>>>>>>>");
		// 使 用com.ctrip.framework.apollo.Config // api取apollo配置,获取解密需要的密钥
		//modify by hhh 20191201
		//Config apolloConfig = ConfigService.getAppConfig();
        //String jfSecret = apolloConfig.getProperty("legend.secret.key", "UISwD9fW6rFh9SNS");
        String jfSecret=ApolloConfigBean.getInstance().getSecretKey();
        //modify
		// 获取利率文件相关数据
		/**
		 * INSERT INTO KNP_PARAMETER (PARM_CODE, PRIMARY_KEY1, PRIMARY_KEY2,
		 * PRIMARY_KEY3, PARM_VALUE1, PARM_VALUE2, PARM_VALUE3, PARM_VALUE4,
		 * PARM_VALUE5, REMARK, MODULE, ORG_ID, DATA_CREATE_TIME,
		 * DATA_UPDATE_TIME, DATA_CREATE_USER, DATA_UPDATE_USER, DATA_VERSION)
		 * VALUES ('EdmSettleAccount.file', 'settdw', '%', '%',
		 * '/sharedir/dataplat/in/icore', 'REQ', null, '.txt',, 'T1批量代发文件生成',
		 * null, null, null, null, null, null, null, null);
		 */
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("EdmSettleAccount.file", "settdw", "%", "%", true);
		// 产生文件的日期目录
		final String trandt = DateTools2.getDateInfo().getSystdt();// 获取上次交易日期
		// 产生文件的日期目录
		String filePath = tblKnpParameter.getParm_value1();
		// String filePath = "D:\\123\\";
		String sequen = BusiTools.getSequence("setrsq", 4);
		String fileName = tblKnpParameter.getParm_value2() + E_OFCHAR.UND + dataItem.getOrgaid() + E_OFCHAR.UND + trandt + E_OFCHAR.UND + sequen + tblKnpParameter.getParm_value4();
		String time = DateTools.getCurrentTime();
		LttsFileWriter file = new LttsFileWriter(filePath, fileName, "GBK");
		Params params = new Params();
		params.add("taskid", input.getTaskid());
		params.add("orgaid", dataItem.getOrgaid());
		file.open();

		String hzanam = (dataItem.getTranam().multiply(BigDecimal.valueOf(100))).setScale(0, BigDecimal.ROUND_DOWN).toString();// 结算金额
		file.write("#" + E_OFCHAR.VER + "NGP-SYSTEM-T1" + E_OFCHAR.VER + dataItem.getSetnum() + E_OFCHAR.VER + hzanam + E_OFCHAR.VER + sequen);

		try {
			DaoUtil.selectList(EdmAfterDayBatchDao.namedsql_selDrawedSettleDataInfo, params, new CursorHandler<EdmSettleAccount>() {
				@Override
				public boolean handle(int arg0, EdmSettleAccount arg1) {
					StringBuffer lnreq = new StringBuffer();

					String settsq = arg1.getSettsq();// 代发流水
					String acctid = arg1.getAcctid();// 内部商户号
					// 需要转加密
					String acctna = KnaCacdDao.selectFirst_odb3(arg1.getAcctno(), E_DPACST.DEFAULT, false).getAcctna();
					// String inmena = arg1.getInmena();//内部商户名称
					String sacdno = arg1.getSacdno();// 收款账户
					try {
						acctna = EncryptTool.sm4ConvertJfpal(acctna, jfSecret, "GBK");
						sacdno = EncryptTool.sm4ConvertJfpal(sacdno, jfSecret, "GBK");
					}
					catch (Exception e) {
						EdmSettleAccount edmSettleAccount = EdmSettleAccountDao.selectOne_odb01(arg1.getMntrsq(), trandt, false);
						edmSettleAccount.setSettdt(CommTools.getBaseRunEnvs().getTrxn_date());
						edmSettleAccount.setTranst(E_CUPSST.FAIL);
						edmSettleAccount.setRetmsg("密文转换失败");
						EdmSettleAccountDao.updateOne_odb01(edmSettleAccount);
						return true;
					}

					String bankno = arg1.getBankno();// 行号
					String sabkna = arg1.getSabkna();// 行名
					String tranam = (arg1.getToanam().multiply(BigDecimal.valueOf(100))).setScale(0, BigDecimal.ROUND_DOWN).toString();// 结算金额
					String settdt = arg1.getSettdt();// 代发日期
					E_CUSTTP puacfg = arg1.getPuacfg();// 是否对公对私

					lnreq.append(settsq).append(E_OFCHAR.VER);
					lnreq.append(acctid).append(E_OFCHAR.VER);
					lnreq.append(acctna).append(E_OFCHAR.VER);
					lnreq.append(sacdno).append(E_OFCHAR.VER);
					lnreq.append(bankno).append(E_OFCHAR.VER);
					lnreq.append(sabkna).append(E_OFCHAR.VER);
					lnreq.append(tranam).append(E_OFCHAR.VER);
					lnreq.append(settdt).append(E_OFCHAR.VER);
					lnreq.append(time).append(E_OFCHAR.VER);
					lnreq.append("01").append(E_OFCHAR.VER);// 无实际意义，但不能为空
					lnreq.append(puacfg);
					file.write(lnreq.toString());
					return true;
				}
			});
			String okFileName = tblKnpParameter.getParm_value2() + E_OFCHAR.UND + dataItem.getOrgaid() + E_OFCHAR.UND + trandt + E_OFCHAR.UND + sequen + ".OK";
			LttsFileWriter fileOk = new LttsFileWriter(filePath, okFileName, "GBK");
			fileOk.open();
			try {
				fileOk.write(trandt);
			}
			finally {
				fileOk.close();
			}
		}
		finally {
			file.close();
		}
		biz.debug("<<<<<<<<<<<<<<<<<文件结束>>>>>>>>>>>>>>>>>");
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
	public BatchDataWalker<cn.sunline.edsp.busi.dp.type.ca.EdmAfterDay.FileDataGroupNo> getBatchDataWalker(cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Sefile.Input input,
			cn.sunline.edsp.busi.dptran.batchtran.ca.intf.Sefile.Property property) {
		Params parm = new Params();
		parm.add("taskid", input.getTaskid());
		return new CursorBatchDataWalker<FileDataGroupNo>(EdmAfterDayBatchDao.namedsql_selBatchNoSettleDataInfo, parm);
	}
}
