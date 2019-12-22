package cn.sunline.ltts.busi.ca.serviceimpl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.ca.base.DecryptConstant;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcal;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcalDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALST;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_ACALTP;

/**
 * 电子账户账户别名绑定服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoCaSevAliasBindChange", longname = "电子账户账户别名绑定服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoCaSevAliasBindChangeImpl implements
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAliasBindChange {
	/**
	 * 
	 * @author xiongzhao
	 *         <p>
	 *         <li>2016年6月24日-下午3:51:05</li>
	 *         <li>功能描述：电子账户绑定账户别名服务</li>
	 *         </p>
	 * 
	 * @param cardno
	 * @param acalno
	 * @param acaltp
	 * @param custac
	 */
	public void addBindAlias(String cardno, String tlphno, E_ACALTP acaltp,
			String custac) {

		// 输入接口检验
		// 电子账号ID
		String timetm=DateTools2.getCurrentTimestamp();
		
		if (CommUtil.isNull(custac)) {
			//电子账号
			if (CommUtil.isNull(cardno)) {
				throw DpModuleError.DpstProd.BNAS0926();
			}
			
			// 根据电子账号获取电子账号ID
			KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(cardno, false);
			// 检查查询记录是否为空
			if (CommUtil.isNull(tblKnaAcdc)) {
				throw CaError.Eacct.BNAS1279();
			}

			custac = tblKnaAcdc.getCustac();// 电子账号ID 
		}

		// 绑定手机号
		if (CommUtil.isNull(tlphno)) {
			throw CaError.Eacct.BNAS1110();
		}

		// 校验手机号位数
//		if (tlphno.length() != 11) {
//			throw CaError.Eacct.BNAS0397();
//		}

		// 校验手机号是否全为数字
//		if (!BusiTools.isNum(tlphno)) {
//			throw CaError.Eacct.BNAS0319();
//		}

		// 校验新绑定别名信息是否正确
		KnaAcal tblKnaAcal = KnaAcalDao.selectOne_odb1(custac,
				E_ACALTP.CELLPHONE, tlphno, false);
		
		// 检查查询结果是否为空
		if (CommUtil.isNotNull(tblKnaAcal)) {

			// 若绑定手机号状态正常
//			if (tblKnaAcal.getAcalst() == E_ACALST.NORMAL) {
//				throw CaError.Eacct.BNAS1638();
//			}

			// 若绑定手机号状态为失效
			if (tblKnaAcal.getAcalst() == E_ACALST.INVALID) {

				// 更新新账户别名状态为正常
				tblKnaAcal.setAcalst(E_ACALST.NORMAL);
				KnaAcalDao.updateOne_odb1(tblKnaAcal);

			}
		} else {
			
			// 查询绑定手机号是否已绑定启其他电子账号
			KnaAcal tblKnaAca2 = KnaAcalDao.selectFirst_odb3(
					E_ACALTP.CELLPHONE, tlphno, E_ACALST.NORMAL, false);
			
			// 检查查询结果是否为空
			if (CommUtil.isNotNull(tblKnaAca2)) {
				
				// 若交易渠道为移动前端，抛出异常
				if (CommUtil.equals(CommTools.getBaseRunEnvs().getChannel_id(), "NM")) {
					if (!CommUtil.equals(tblKnaAca2.getCustac(), custac)) {
						throw CaError.Eacct.BNAS0714();
					}
				} 
				// 若交易渠道为统一后管，将绑定关系置为异常
				else if (CommUtil.equals(CommTools.getBaseRunEnvs().getChannel_id(), "EB")) {
					
					// 将原绑定手机号绑定记录置为异常
					CaDao.updKnaAcalAcalst(tlphno,timetm);
					
				}
			}
			
			// 将绑定账户别名信息插入到账户别名表
			KnaAcal tblKnaAcal1 = SysUtil.getInstance(KnaAcal.class);
			tblKnaAcal1.setAcalst(E_ACALST.NORMAL);// 关联状态
			tblKnaAcal1.setTlphno(tlphno);// 绑定手机号
			tblKnaAcal1.setTmtlphno(DecryptConstant.maskMobile(tlphno));// 绑定手机号
			tblKnaAcal1.setAcaltp(E_ACALTP.CELLPHONE);// 账户别名类型
			tblKnaAcal1.setCustac(custac);// 电子账号ID
			
			// 插入账户别名表
			KnaAcalDao.insert(tblKnaAcal1);
			
		}
	}

	/**
	 * 
	 * @author xiongzhao
	 *         <p>
	 *         <li>2016年6月24日-下午3:51:21</li>
	 *         <li>功能描述：电子账户账户别名变更服务</li>
	 *         </p>
	 * 
	 * @param cardno 电子账号
	 * @param custid 用户ID
	 * @param acalno 新绑定手机号
	 * @param acaltp 绑定别名类型
	 * @param odalno 原绑定手机号
	 */
	public void ChangeBindAlias(String cardno, String custid, String acalno,
			E_ACALTP acaltp, String odalno, String custac) {

		// 输入字段校验
		// 新绑定手机号
		if (CommUtil.isNull(acalno)) {
			throw DpModuleError.DpstComm.BNAS0275();
		}
		// 原绑定手机号
		if (CommUtil.isNull(odalno)) {
			throw CaError.Eacct.BNAS0222();
		}
		// 用户ID
		if (CommUtil.isNull(custid)) {
			throw CaError.Eacct.BNAS0241();
		}

		// 校验手机号位数
		if (acalno.length() != 11) {
			throw CaError.Eacct.BNAS0397();
		}
		if (odalno.length() != 11) {
			throw CaError.Eacct.BNAS0397();
		}

		// 校验手机号是否全为数字
		if (!BusiTools.isNum(acalno)) {
			throw CaError.Eacct.BNAS0319();
		}
		if (!BusiTools.isNum(odalno)) {
			throw CaError.Eacct.BNAS0319();
		}

		// 判断用户ID和电子账号是否匹配
		KnaCust tblKnaCust = CaDao.selCustacByCustid(custid, false);
		if (CommUtil.isNull(tblKnaCust)) {
			KnaAcal tblKnaAca2 = CaDao.selKnaAcalByAcalno(acalno,
					E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);
			if (CommUtil.isNotNull(tblKnaAca2)) {
				// 将新手机号和原账户的关系解绑
				tblKnaAca2.setAcalst(E_ACALST.INVALID);
				KnaAcalDao.updateOne_odb1(tblKnaAca2);
			}
		} else {

			// 将电子账户的法人和归属机构传入公共运行变量中
//			CommTools.getBaseRunEnvs().setBusi_org_id(tblKnaCust.getCorpno());
			CommTools.getBaseRunEnvs().setTrxn_branch(tblKnaCust.getBrchno());

			// 校验原绑定别名信息是否正确
			KnaAcal tblKnaAcal = KnaAcalDao.selectOne_odb1(tblKnaCust.getCustac(),
					E_ACALTP.CELLPHONE, odalno, false);
			if (CommUtil.isNotNull(tblKnaAcal)
					&& tblKnaAcal.getAcalst() == E_ACALST.NORMAL) {
				// 更新原账户别名状态为失效
				tblKnaAcal.setAcalst(E_ACALST.INVALID);
				KnaAcalDao.updateOne_odb1(tblKnaAcal);
			}

			// 校验新绑定别名信息是否正确
			KnaAcal tblKnaAcal1 = KnaAcalDao.selectOne_odb1(tblKnaCust.getCustac(),
					E_ACALTP.CELLPHONE, acalno, false);
			if (CommUtil.isNotNull(tblKnaAcal1)) {
				
				if (tblKnaAcal1.getAcalst() == E_ACALST.NORMAL) {
					throw DpModuleError.DpstComm.BNAS0274();
				}
				
				KnaAcal tblKnaAca2 = CaDao.selKnaAcalByAcalno(acalno,
						E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);
				if (CommUtil.isNotNull(tblKnaAca2)) {
					// 将新手机号和原账户的关系解绑
					tblKnaAca2.setAcalst(E_ACALST.INVALID);
					KnaAcalDao.updateOne_odb1(tblKnaAca2);
				}

				if (tblKnaAcal1.getAcalst() == E_ACALST.INVALID) {
					// 更新新账户别名状态为正常
					tblKnaAcal1.setAcalst(E_ACALST.NORMAL);
					KnaAcalDao.updateOne_odb1(tblKnaAcal1);
				}
			} else {
				KnaAcal tblKnaAca2 = CaDao.selKnaAcalByAcalno(acalno,
						E_ACALTP.CELLPHONE, E_ACALST.NORMAL, false);
				if (CommUtil.isNotNull(tblKnaAca2)) {
					// 将新手机号和原账户的关系解绑
					tblKnaAca2.setAcalst(E_ACALST.INVALID);
					KnaAcalDao.updateOne_odb1(tblKnaAca2);
				}

				// 将绑定账户别名信息插入到账户别名表
				KnaAcal tblKnaAcal3 = SysUtil.getInstance(KnaAcal.class);
				tblKnaAcal3.setAcalst(E_ACALST.NORMAL);
				tblKnaAcal3.setTlphno(acalno);
				tblKnaAcal3.setAcaltp(E_ACALTP.CELLPHONE);
				tblKnaAcal3.setCustac(custac);
				KnaAcalDao.insert(tblKnaAcal3);

			}
		}
	}
}

