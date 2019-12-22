package cn.sunline.ltts.busi.dptran.trans;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.dp.namedsql.DpFrozDao;
import cn.sunline.ltts.busi.dp.tables.DpMessTable.KnpCustIden;
import cn.sunline.ltts.busi.dp.tables.DpMessTable.KnpCustIdenDao;
import cn.sunline.ltts.busi.dp.tables.DpMessTable.KnpPromMess;
import cn.sunline.ltts.busi.dp.tables.DpMessTable.KnpPromMessBatc;
import cn.sunline.ltts.busi.dp.tables.DpMessTable.KnpPromMessBatcDao;
import cn.sunline.ltts.busi.dp.tables.DpMessTable.KnpPromMessDao;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_MESSTP;

public class chklog {

	public static void checkLogin(
			final cn.sunline.ltts.busi.dptran.trans.intf.Chklog.Input input,
			final cn.sunline.ltts.busi.dptran.trans.intf.Chklog.Property property,
			final cn.sunline.ltts.busi.dptran.trans.intf.Chklog.Output output) {
		
		StringBuilder message = new StringBuilder();
		String cardno = input.getCardno();
		String servtp = CommTools.getBaseRunEnvs().getChannel_id()
;
		if (CommUtil.isNull(cardno)) {
			throw DpModuleError.DpstComm.E9027("交易卡号");
		}

		KnpPromMess knpPromMess = getKnpPromMess("EffectiveDateCheck");
		if (getFlag(knpPromMess.getIsprom())) {
			String date = DateTools2.calDateByTerm(CommTools.getBaseRunEnvs().getTrxn_date(), "3M");
			String idtfno = CaTools.getIdtfno(cardno);
			KnpCustIden knpCustIden = KnpCustIdenDao.selectOne_odb1(idtfno, false);
			if (CommUtil.isNotNull(knpCustIden)) {
				if (CommUtil.compare(knpCustIden.getValidt(), date) <= 0) {
					message.append(knpPromMess.getContxt());
				}
			}
		}
		KnpPromMess knpPromMess1 = getKnpPromMess("FrozenTip");
		if (getFlag(knpPromMess1.getIsprom())) {
			String custac = CaTools.getCustacByCardno(cardno);
			List<IoDpKnbFroz> knpFrozList = DpFrozDao.selFrozListByCustac(custac, E_FROZST.INVALID, false);
			if (CommUtil.isNotNull(knpFrozList)) {
				message.append(knpPromMess1.getContxt());
			}
		}
		List<KnpPromMessBatc> knpPromMessBatcList = KnpPromMessBatcDao
				.selectAll_odb2(cardno, E_YES___.YES, servtp, false);
		for (KnpPromMessBatc knpPromMessBatc : knpPromMessBatcList) {
			if (E_MESSTP.TABLEDATE == knpPromMessBatc.getMesstp()) {
				KnpPromMess mess = KnpPromMessDao.selectOne_odb1(
						knpPromMessBatc.getMessno(), true);
				message.append(mess.getContxt());
			} else {
				message.append(knpPromMessBatc.getContxt());
			}
		}
		output.setContxt(message.toString());
	}
	
	public static KnpPromMess getKnpPromMess(String messna){
		KnpPromMess knpPromMess = KnpPromMessDao.selectOne_odb4(messna, false);
		if (CommUtil.isNull(knpPromMess)) {
			throw DpModuleError.DpstComm.E9901("提示信息不存在!");
		}
		return knpPromMess;
	}

	public static Boolean getFlag (E_YES___ yesOrNo){
		if (yesOrNo == E_YES___.YES) {
			return true;
		} 
		return false;
	}
	
}
