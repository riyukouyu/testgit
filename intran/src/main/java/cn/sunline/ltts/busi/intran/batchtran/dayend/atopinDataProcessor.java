package cn.sunline.ltts.busi.intran.batchtran.dayend;

import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.in.inner.InnerAcctQry;
import cn.sunline.ltts.busi.in.namedsql.InDayEndSqlsDao;
import cn.sunline.ltts.busi.in.tables.In.GlKnpSuac;
import cn.sunline.ltts.busi.in.tables.In.GlKnpSuacDao;
import cn.sunline.ltts.busi.in.type.InQueryTypes.InacProInfo;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnpBusi;
import cn.sunline.ltts.busi.iobus.type.in.IoInOpenCloseComplex.IoInacOpen_IN;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoOpenBrchInfoListOut;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BRSTUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_AUTOOP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRCHLV;
import cn.sunline.edsp.base.lang.Params;

/**
 * 自动开立内部户
 * 
 */

public class atopinDataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Atopin.Input, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Atopin.Property, cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnpBusi> {
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
	public void process(String jobId, int index, cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnpBusi dataItem, cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Atopin.Input input,
			cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Atopin.Property property) {
		// 获取所有开启状态的机构号
		IoOpenBrchInfoListOut cplBrch = SysUtil.getRemoteInstance(IoSrvPbBranch.class).getValidBranchList(E_BRSTUS.valid);
		List<IoBrchInfo> brchList = new ArrayList<>();
		brchList = cplBrch.getBrinfo();
		for (IoBrchInfo brch : brchList) {
			String acctbr = brch.getBrchno();
			String crcycd = BusiTools.getDefineCurrency();// 只开立人民币账户
			E_BRCHLV brchlv = brch.getBrchlv();// 机构级别
			E_AUTOOP provop = dataItem.getProvop();// 省中心开立标志
			E_AUTOOP counop = dataItem.getCounop();// 县中心开立标志
			String busino = dataItem.getBusino();// 业务代码
			E_YES___ fnsign = dataItem.getFnsign();
			// 省级机构开户控制
			if (E_AUTOOP._0 == provop && brchlv == E_BRCHLV.PROV) {
				continue;
			}
			// 县级机构开户控制
			if (E_AUTOOP._0 == counop && brchlv == E_BRCHLV.COUNT) {
				continue;
			}
			// 外币专用内部户产品调过
			if (fnsign == E_YES___.YES) {
				continue;
			}
			/*
			 IoBrchCrcycdListOut cplbrcy = SysUtil.getInstance(IoBrchSvcType.class).
			 selBrchCrcycdList(acctbr, crcycd, new Long(1), new Long(100), null); 
			 List<IoBrchCrcycdList> brcyList = cplbrcy.getBrcyList(); 
			 if(brcyList==null||brcyList.size()==0){//未找到机构开通币种记录调过 
			 	continue; 
			 }
			 if(brcyList.get(0).getNetype()==E_NETYPE.CLOS){ //人民币未开通调过
			 	continue; 
			 }
			 */// rambo delete //TODO selBrchCrcycdList
			List<GlKnpSuac> suacList = GlKnpSuacDao.selectAll_odb2(busino, false);
			if (suacList != null && suacList.size() > 0) {
				// 开立专用账户
				for (GlKnpSuac suac : suacList) {
					String subsac = suac.getSubsac();
					// 检查账户并新开内部户
					genAcctno(acctbr, busino, subsac, crcycd);
				}
			} else {
				// 开立基准账户
				genAcctno(acctbr, busino, null, crcycd);
			}
		}
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
	public BatchDataWalker<cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnpBusi> getBatchDataWalker(cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Atopin.Input input,
			cn.sunline.ltts.busi.intran.batchtran.dayend.intf.Atopin.Property property) {
		Params params = SysUtil.getInstance(Params.class);

		return new CursorBatchDataWalker<IoGlKnpBusi>(InDayEndSqlsDao.namedsql_selGlKnpBusiList, params);
	}

	private void genAcctno(String acctbr, String busino, String subsac, String crcycd) {
		// 1、内部户查询
		InacProInfo info = SysUtil.getInstance(InacProInfo.class);
		info = InnerAcctQry.qryAcctPro(crcycd, acctbr, busino, null, null, subsac);
		// 账户不存，则新开账户在
		if (info.getIsexis() == E_YES___.NO) {
			IoInacOpen_IN inacopIn = SysUtil.getInstance(IoInacOpen_IN.class);

			inacopIn.setAcbrch(acctbr);
			inacopIn.setBusino(busino);
			inacopIn.setCrcycd(crcycd);
			inacopIn.setSubsac(subsac);

			// 新开内部户
			InnerAcctQry.addInAcct(inacopIn);
		}

	}
}
