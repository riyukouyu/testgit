package cn.sunline.ltts.busi.dp.froz;


import java.util.Collections;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.namedsql.DpFrozDao;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFroz;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpUnStopPayIn;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZWY;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

public class FrozPublic {

	/**
	 * 根据电子账号获取电子账号的冻结状态
	 * 
	 * @author douwenbo
	 * @date 2016-05-24 20:55
	 * @param custac 电子账号
	 * @return E_FROZST 冻结状态
	 */
	public static E_FROZST KnbFroz_getFrozstByCustac(String custac,boolean isable){
		List<KnbFroz> tblKnbFroz = KnbFrozDao.selectAll_odb11(custac, E_FROZST.VALID, isable);
		if(tblKnbFroz.size() > 0){
			return E_FROZST.VALID;
		}
		return E_FROZST.INVALID;
	}
	
	/**
	 * 根据电子账号获取电子账号的冻结信息
	 * 
	 * @author douwenbo
	 * @date 2016-05-25 08:59
	 * @param custac 电子账号
	 * @param isable 是否
	 * @return cplDpKnbFrozList 冻结信息
	 */
	public static Options<IoDpKnbFroz> KnbFroz_getInfoByCustac(String custac, Boolean isable) {
		//获取冻结集合输出接口
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch();
		E_FROZST frozst = E_FROZST.VALID;
		
		Options<IoDpKnbFroz> ops = new DefaultOptions<>();
		List<IoDpKnbFroz> knbFrozList = DpFrozDao.selFrozInfoByCustac(custac, tranbr, trandt, frozst, isable);
		if(knbFrozList.size() >0){
			Collections.copy(ops, knbFrozList);
		}
		return ops;
	}
	
	/**
	 * 根据冻结流水和日期冻结
	 * 
	 * @author douwenbo
	 * @date 2016-06-01 14:46
	 * @param mntrsq 冻结主流水
	 * @param trandt 冻结日期
	 */
	public static void DoFrozByMntrsqdt(String mntrsq , String trandt, Boolean isable){
		KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb13(mntrsq, trandt, isable);
		if(CommUtil.isNull(tblKnbFroz)){
			throw DpModuleError.DpstComm.BNAS1630();
		}
		//TODO 冻结输入接口
//		IoDpStopPayIn dpStopPayIn = SysUtil.getInstance(IoDpStopPayIn.class);
//		dpStopPayIn.setFrlmtp(E_FRLMTP.AMOUNT); //限制类型
//		dpStopPayIn.setFrozow(); //冻结主体类型
//		dpStopPayIn.setFrozcd(); //冻结分类码
//		dpStopPayIn.setFrcttp(); //冻结证明文书类别 
//		dpStopPayIn.setFrozwy(); //冻解方式
//		dpStopPayIn.setFrozno(); //冻结编号
//		dpStopPayIn.setCardno(); //电子账号
//		dpStopPayIn.setCustna(); //客户名称
//		dpStopPayIn.setCrcycd(); //币种
//		dpStopPayIn.setCsextg(); //账户钞汇标志
//		dpStopPayIn.setFrctno(); //冻结通知书编号
//		dpStopPayIn.setFrozam(); //冻结金额
//		dpStopPayIn.setFrozdt(); //冻结日期
//		dpStopPayIn.setFreddt(); //冻结终止日期
//		dpStopPayIn.setFrexog(); //执法部门
//		dpStopPayIn.setFrogna(); //执法部门名称
//		dpStopPayIn.setFrna01(); //执法人员1姓名
//		dpStopPayIn.setIdtp01(); //执法人员1证件种类
//		dpStopPayIn.setIdno01(); //执法人员1证件号码
//		dpStopPayIn.setFrna02(); //执法人员2姓名
//		dpStopPayIn.setIdtp02(); //执法人员2证件种类
//		dpStopPayIn.setIdno02(); //执法人员2证件号码
//		dpStopPayIn.setFrreas(); //冻结原因
//		dpStopPayIn.setRemark(); //备注
//		SysUtil.getInstance(IoDpFrozSvcType.class).IoDpFrozByLaw(dpStopPayIn);
		
		
		//解冻输入接口
//		IoDpUnStopPayIn dpUnfrIn = SysUtil.getInstance(IoDpUnStopPayIn.class);
//		dpUnfrIn.setFrozwy(E_FROZWY.TSOLVE);//解冻
//		dpUnfrIn.setCardno(tblKnbFroz.getCustac());
//		dpUnfrIn.setCrcycd(tblKnbFroz.getCrcycd());
//		dpUnfrIn.setFrogna(tblKnbFroz.getFrogna());
//		dpUnfrIn.setOdfrno(tblKnbFroz.getFrozno());
//		dpUnfrIn.setUfexog(tblKnbFroz.getFrexog());
//		dpUnfrIn.setUfna01(tblKnbFroz.getFrna01());
//		dpUnfrIn.setIdtp01(tblKnbFroz.getIdtp01());
//		dpUnfrIn.setIdno01(tblKnbFroz.getIdno01());
//		dpUnfrIn.setUfna02(tblKnbFroz.getFrna02());
//		dpUnfrIn.setIdtp02(tblKnbFroz.getIdtp02());
//		dpUnfrIn.setIdno02(tblKnbFroz.getIdno02());
//		dpUnfrIn.setUfreas("TODO");
//		SysUtil.getInstance(IoDpFrozSvcType.class).IoDpUnfrByLaw(dpUnfrIn);
	}
	
	/**
	 * 根据冻结流水和日期解冻
	 * 
	 * @author douwenbo
	 * @date 2016-05-25 22:56
	 * @param mntrsq 冻结主流水
	 * * @param trandt 冻结日期
	 */
	public static void DoUnFrByMntrsqdt(String mntrsq , String trandt, Boolean isable){
		KnbFroz tblKnbFroz = KnbFrozDao.selectOne_odb13(mntrsq, trandt, isable);
		if(CommUtil.isNull(tblKnbFroz)){
			throw DpModuleError.DpstComm.BNAS1630();
		}
		IoCaSevQryTableInfo caSevQryTableInfo = SysUtil.getInstance(IoCaSevQryTableInfo.class);
		IoCaKnaAcdc caKnaAcdc = caSevQryTableInfo.getKnaAcdcOdb1(tblKnbFroz.getCustac(), E_DPACST.NORMAL, false);
		if(CommUtil.isNull(caKnaAcdc)){
			throw DpModuleError.DpstComm.BNAS1631();
		}
		String cardno = caKnaAcdc.getCardno();
		
		
		//解冻输入接口
		IoDpUnStopPayIn dpUnfrIn = SysUtil.getInstance(IoDpUnStopPayIn.class);
		dpUnfrIn.setFrozwy(E_FROZWY.TSOLVE);//解冻
		dpUnfrIn.setCardno(cardno);
		dpUnfrIn.setCrcycd(tblKnbFroz.getCrcycd());
		dpUnfrIn.setFrogna(tblKnbFroz.getFrogna());
		dpUnfrIn.setOdfrno(tblKnbFroz.getFrozno());
		dpUnfrIn.setUfexog(tblKnbFroz.getFrexog());
		dpUnfrIn.setUfna01(tblKnbFroz.getFrna01());
		dpUnfrIn.setIdtp01(tblKnbFroz.getIdtp01());
		dpUnfrIn.setIdno01(tblKnbFroz.getIdno01());
		dpUnfrIn.setUfna02(tblKnbFroz.getFrna02());
		dpUnfrIn.setIdtp02(tblKnbFroz.getIdtp02());
		dpUnfrIn.setIdno02(tblKnbFroz.getIdno02());
		dpUnfrIn.setUfreas("TODO");
		SysUtil.getInstance(IoDpFrozSvcType.class).IoDpUnfrByLaw(dpUnfrIn);
	}
}
