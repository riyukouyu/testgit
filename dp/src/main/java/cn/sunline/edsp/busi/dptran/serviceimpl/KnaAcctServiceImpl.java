package cn.sunline.edsp.busi.dptran.serviceimpl;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.busi.dp.servicetype.KnaAcctService.IQueryAcctNbl.Input;
import cn.sunline.edsp.busi.dp.servicetype.KnaAcctService.IQueryAcctNbl.Output;
import cn.sunline.ltts.busi.dp.froz.DpFrozTools;
import cn.sunline.ltts.busi.dp.servicetype.DpAcctSvcType;
import cn.sunline.ltts.busi.dp.tables.online.DpDepoBusiMainDao;
import cn.sunline.ltts.busi.dp.type.DpAcctType.AcctSbad;
import cn.sunline.ltts.busi.dp.type.DpAcctType.AcctSub;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;

/**
 * 查询电子子账户信息实现类
 *
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "KnaAcctServiceImpl", longname = "查询电子子账户信息实现类", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class KnaAcctServiceImpl implements cn.sunline.edsp.busi.dp.servicetype.KnaAcctService {
	/**
	 * 查询电子子账户信息列表
	 *
	 */
	public void IQuerySubAcctInfo(
			final cn.sunline.edsp.busi.dp.servicetype.KnaAcctService.IQuerySubAcctInfo.Input input,
			final cn.sunline.edsp.busi.dp.servicetype.KnaAcctService.IQuerySubAcctInfo.Output output) {

		String custno = input.getCustno();
		String acctno = input.getAcctno();
		String acctna = input.getAcctna();
		String cardno = input.getCardno();// 为外部虚拟卡号
		String acctid = input.getAcctid();
		// String custac="";//内部电子账号

		long pageno = CommTools.getBaseRunEnvs().getPage_start();
		long pagesize = CommTools.getBaseRunEnvs().getPage_size();
		// KnaAcdc knaAcdc =SysUtil.getInstance(KnaAcdc.class);
		// if(CommUtil.isNotNull(cardno)) {
		// knaAcdc = KnaAcdcDao.selectOne_odb2(cardno, false);
		// if(CommUtil.isNotNull(knaAcdc)) {
		// custac = knaAcdc.getCustac();
		// }
		// }
		// 分页查询电子子账户列表信息。
		// Page<DpDepoBusiMain.KnaAcct> KnaAcctInfo = DpAcctDao.selSubAccts(custac,
		// acctno, acctid, (iPageno - 1) * iPgsize, iPgsize, 0, false);
		// 子户列表。
		// List<DpDepoBusiMain.KnaAcct> lstKnaAcct = KnaAcctInfo.getRecords();
		// 子户数量。
		// long totalno = KnaAcctInfo.getRecordCount();
		// output.getSubAcctInfoList().setValues(lstKnaAcct);
		// CommTools.getBaseRunEnvs().setTotal_count(totalno);
		Page<AcctSbad> pages = DpDepoBusiMainDao.querySubAcctByAcctIdOrCardNo(acctno, custno, acctna, cardno,acctid,
				(pageno - 1) * pagesize, pagesize, 0, false);
		if (pages != null) {
			List<AcctSbad> list = pages.getRecords();
			System.out.println("list=" + list.size());
			if (null != list && !list.isEmpty()) {
				list.forEach(li -> {
					String acctNo = li.getAcctno();
					BigDecimal acctbl = SysUtil.getInstance(DpAcctSvcType.class).getAcctAvabal(acctNo, E_YES___.YES,
							false);
					li.setAcctbl(acctbl);
					li.setUnavbl(DpFrozTools.getFrozBala(E_FROZOW.AUACCT, acctNo));
				});
			}
			output.getSubAcctInfoList().setValues(list);
			CommTools.getBaseRunEnvs().setTotal_count(pages.getRecordCount());
		}

	}

	/**
	 * 根据cardno查询电子子账户信息及开户标识列表
	 *
	 */
	@Override
	public void IQuerySubAcctId(final cn.sunline.edsp.busi.dp.servicetype.KnaAcctService.IQuerySubAcctId.Input input,
			final cn.sunline.edsp.busi.dp.servicetype.KnaAcctService.IQuerySubAcctId.Output output) {
		// 为主账户外部虚拟卡号
		String cardno = input.getCardno();

		// 分页信息
		long pageno = CommTools.getBaseRunEnvs().getPage_start();
		long pagesize = CommTools.getBaseRunEnvs().getPage_size();
		// 分页查询主账户下所有子户的信息
		Page<AcctSub> acctSublist = DpDepoBusiMainDao.querySubAcctByCardNo(cardno, (pageno - 1) * pagesize, pagesize, 0,
				false);

		//List<AcctSub> sublist = acctSublist.getRecords();
		// Options list=SysUtil.getInstance(Options.class);
		// list.setValues(sublist);
		// output.setSubList(list);

		// 优化代码by sunzy20190915
		output.getSubList().addAll(acctSublist.getRecords());

		// 返回查询结果总数
		CommTools.getBaseRunEnvs().setTotal_count(acctSublist.getRecordCount());

	}

	@Override
	public void queryAcctOnlNbl(Input input, Output output) {
		String inmeid = input.getInmeid();
		String onlnbl = DpDepoBusiMainDao.queryAcctOnlnbl(inmeid, false);
		BigDecimal blance = new BigDecimal(onlnbl).multiply(new BigDecimal(100));
		output.setOnlnbl(blance);
		output.setHighBalance(new BigDecimal(0));
		output.setLowBalance(blance);
	}

}
