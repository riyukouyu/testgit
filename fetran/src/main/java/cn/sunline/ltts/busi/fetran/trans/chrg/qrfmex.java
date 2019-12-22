package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeFormulaDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgFmex;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrfmex {
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrfmex.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：计费公式解析表查询
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */
	public static void qrfmex( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrfmex.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrfmex.Output output){
		
		bizlog.method("<<<<<< qrfmex begin >>>>>>");
		
		String chrgcd = input.getChrgcd();
		String chrgfm = input.getChrgfm();
		String crcycd = BusiTools.getDefineCurrency();

		long pageno = CommTools.getBaseRunEnvs().getPage_start();// 页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();// 页容量

		long totlCount = 0;

		Page<IoCgFmex> lstFmex = FeFormulaDao.
				selall_kcp_chrg_fmex(chrgcd, null, crcycd, chrgfm, (pageno - 1) * pgsize, pgsize, totlCount, false);
		Options<IoCgFmex> optfmex = new DefaultOptions<IoCgFmex>();
		optfmex.addAll(lstFmex.getRecords());
		output.setPinfos(optfmex);

		CommTools.getBaseRunEnvs().setTotal_count(lstFmex.getRecordCount());// 记录总数

		}
	}
