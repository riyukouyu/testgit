
package cn.sunline.edsp.busi.dptran.trans;

import java.util.Map;

import cn.sunline.ltts.busi.ca.namedsql.CaDao;

public class qusdcd {

	public static void seldeBank( final cn.sunline.edsp.busi.dptran.trans.intf.Qusdcd.Input input,  final cn.sunline.edsp.busi.dptran.trans.intf.Qusdcd.Property property,  final cn.sunline.edsp.busi.dptran.trans.intf.Qusdcd.Output output){
		String inmeid = input.getInmeid();
		Map<String,Object> map = CaDao.selDeBank(inmeid, false);
		
		if(null != map) {
			output.setAcbdno(map.get("ACBDNO").toString());
			output.setBrchna(map.get("BRCHNA").toString());
			output.setCdopac(map.get("CDOPAC").toString());
			output.setOpbrch(map.get("OPBRCH").toString());
			output.setStatus(map.get("STATUS").toString());
		}
	}
}
