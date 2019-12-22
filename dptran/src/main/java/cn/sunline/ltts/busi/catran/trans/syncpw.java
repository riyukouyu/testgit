package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcpw;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcpwDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;

public class syncpw {
	/**
	 * 
	 * @author xiongzhao
	 *         <p>
	 *         <li>2016年6月25日-上午11:35:18</li>
	 *         <li>功能描述：账户密码同步</li>
	 *         </p>
	 * 
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void prcAcctPassWord(
			final cn.sunline.ltts.busi.catran.trans.intf.Syncpw.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Syncpw.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Syncpw.Output output) {
		String sCustac = null;
		String sAccppw = input.getAccppw();
		String sAccpst = input.getAccpst();
		String sCardno = input.getCardno();
		String sCustid = input.getCustid();
		String sServdt = input.getServdt();
		// 检查输入接口必输项是否为空
		if (CommUtil.isNull(sAccppw)) {
			throw CaError.Eacct.BNAS1287();
		}
		if (CommUtil.isNull(sAccpst)) {
			throw CaError.Eacct.BNAS1288();
		}
		if (CommUtil.isNull(sCardno)) {
			throw DpModuleError.DpstProd.BNAS0926();
		}
		if (CommUtil.isNull(sCustid)) {
			throw CaError.Eacct.BNAS0241();
		}
		if (CommUtil.isNull(sServdt)) {
			throw CaError.Eacct.BNAS1289();
		}

		// 根据电子账号获取电子账号ID
		KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(sCardno, false);
		if (CommUtil.isNull(tblKnaAcdc)) {
			throw DpModuleError.DpstComm.BNAS1264();
		}
		sCustac = tblKnaAcdc.getCustac();
		
		// 判断用户ID和电子账号是否匹配
		KnaCust tblKnaCust = CaDao.selCustacByCustid(sCustid, false);
		if (CommUtil.isNull(tblKnaCust)) {
			throw CaError.Eacct.BNAS0242();
		}
		if (!CommUtil.equals(sCustac, tblKnaCust.getCustac())) {
			throw CaError.Eacct.BNAS1290();
		}


		// 根据电子账号ID查询出账户密码信息
		KnaAcpw tblKnaAcpw = KnaAcpwDao.selectOne_odb1(sCustac, false);

		if (CommUtil.isNull(tblKnaAcpw)) {
			//如果不存在记录，插入数据
			KnaAcpw tblKnaAcpw1 = SysUtil.getInstance(KnaAcpw.class);
			tblKnaAcpw1.setAccppw(sAccppw);
			tblKnaAcpw1.setAccpst(sAccpst);
			tblKnaAcpw1.setCustac(sCustac);
			tblKnaAcpw1.setCustid(sCustid);
			tblKnaAcpw1.setServdt(sServdt);
			KnaAcpwDao.insert(tblKnaAcpw1);
		} else {
		//如果存在记录，更新数据 
		tblKnaAcpw.setAccppw(sAccppw);
		tblKnaAcpw.setAccpst(sAccpst);
		tblKnaAcpw.setServdt(sServdt);
		KnaAcpwDao.updateOne_odb1(tblKnaAcpw);
		}
	}
}
