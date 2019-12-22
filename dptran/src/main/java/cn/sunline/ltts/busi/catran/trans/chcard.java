package cn.sunline.ltts.busi.catran.trans;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdc;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaAcdcDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpac;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnbOpacDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CHNLID;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STATUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BACATP;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BINDTP;

public class chcard {

	public static void chkchcardInfo(
			final cn.sunline.ltts.busi.catran.trans.intf.Chcard.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Chcard.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Chcard.Output output) {

		String cardno = input.getCardno(); // 虚拟卡号
		String cdopac = input.getCdopac(); // 绑定账户
		String odopac = input.getOdopac();// 原绑定账户
		String cdopna = input.getCdopna(); // 绑定账户名称
		E_BACATP cardtp = input.getCardtp(); // 绑定账户类型
		E_YES___ isbkca = input.getIsbkca(); // 是否本行账户
	    String sOpbrch = input.getOpbrch();// 账户开户行号
	    String sBrchna = input.getBrchna();// 账户开户行名称
	    String sCustac = "";// 电子账号
	    
		// 判断传进来的参数：账户开户行号不能为空
		if (CommUtil.isNull(sOpbrch)) {
			throw CaError.Eacct.BNAS0179();
		}
		// 判断传进来的参数：账户开户行名称不能为空
		if (CommUtil.isNull(sBrchna)) {
			throw CaError.Eacct.BNAS0178();
		}
		// 判断传进来的参数：绑定账户不能为空
		if (CommUtil.isNull(cdopac)) {
			throw CaError.Eacct.BNAS1112();
		}
		// 判断传进来的参数：绑定账户名称不能为空
		if (CommUtil.isNull(cdopna)) {
			throw CaError.Eacct.BNAS1107();
		}
		// 判断传进来的参数：绑定账户类型不能为空
		if (CommUtil.isNull(cardtp)) {
			throw CaError.Eacct.BNAS1108();
		}
		// 判断传进来的参数：是否本行账户标识不能为空
		if (CommUtil.isNull(isbkca)) {
			throw CaError.Eacct.BNAS0354();
		}

		// 断传进来的参数：电子账号ID不能为空
		if (CommUtil.isNull(sCustac)) {
			if (CommUtil.isNull(cardno)) {
				throw DpModuleError.DpstComm.BNAS0955();
			}

			// 根据卡号获取电子账号
			KnaAcdc tblKnaAcdc = KnaAcdcDao.selectOne_odb2(input.getCardno(),
					false);
			if (CommUtil.isNull(tblKnaAcdc)) {
				throw DpModuleError.DpstComm.BNAS1264();
			}
			
			if(tblKnaAcdc.getStatus() != E_DPACST.NORMAL){
				throw CaError.Eacct.BNAS0906();
			}

			sCustac = tblKnaAcdc.getCustac();// 电子账号

//			// 获取电子账户分类
//			E_ACCATP eAccatp = SysUtil.getInstance(
//					IoCaSrvGenEAccountInfo.class).qryAccatpByCustac(sCustac);
//			if (eAccatp != E_ACCATP.FINANCE) {
//				throw CaError.Eacct.BNAS0108();
//			}

			// 计算电子账户下状态正常的绑定借记账户
			int size = 0;

			List<KnaCacd> lstKnaCacd = KnaCacdDao.selectAll_odb4(sCustac,
					E_DPACST.NORMAL, E_BACATP.DEPOSIT, false);
			if (CommUtil.isNotNull(lstKnaCacd)) {
				size = size + lstKnaCacd.size();
			}

			List<KnaCacd> lstKnaCacd1 = KnaCacdDao.selectAll_odb4(sCustac,
					E_DPACST.NORMAL, E_BACATP.DPACCT, false);
			if (CommUtil.isNotNull(lstKnaCacd1)) {
				size = size + lstKnaCacd1.size();
			}

			if (size != 1) {
				throw CaError.Eacct.BNAS1086();
			}
			/**
			 * modify by liuz
			 * 20181025
			 */
			List<KnbOpac> tblKnbOpac = KnbOpacDao.selectAll_odb1(sCustac, false);
			if (CommUtil.isNotNull(tblKnbOpac)){
//				for (KnbOpac knbOpac : tblKnbOpac) {
//					//开户渠道为银户通只绑定一张绑定卡
//					if (knbOpac.getChnlid() == E_CHNLID.YT){
//						KnaCacd tblKnaCacd = KnaCacdDao.selectFirst_odb3(sCustac, E_DPACST.NORMAL, false);
//						// 变更的原绑定账户账户类型不能为贷记卡
//						if (CommUtil.isNull(tblKnaCacd)) {
//							throw CaError.Eacct.BNAS0221();
//						}
//						if (tblKnaCacd.getCardtp() == E_BACATP.DRCARD) {
//							throw CaError.Eacct.BNAS1086();
//						}
//						property.setOdbind(tblKnaCacd.getCardno());
//					}else{
//						KnaCacd tblKnaCacd = KnaCacdDao.selectOne_odb2(sCustac, odopac, E_DPACST.NORMAL , false);
//						// 变更的原绑定账户账户类型不能为贷记卡
//						if (CommUtil.isNull(tblKnaCacd)) {
//							throw CaError.Eacct.BNAS0221();
//						}
//						if (tblKnaCacd.getCardtp() == E_BACATP.DRCARD) {
//							throw CaError.Eacct.BNAS1086();
//						}
//						property.setOdbind(tblKnaCacd.getCardno());
//					}
//				}
			}else{
				throw DpModuleError.DpstComm.E9999("开户登记簿无此记录");
			}
			/**
			 * end
			 */
			// 变更绑定账户只能绑定借记账户
			if (input.getCardtp() != E_BACATP.DEPOSIT
					&& input.getCardtp() != E_BACATP.DPACCT) {
				throw CaError.Eacct.BNAS1095();
			}

		}
	}

	public static void bindmq( 
			final cn.sunline.ltts.busi.catran.trans.intf.Chcard.Input input,  
			final cn.sunline.ltts.busi.catran.trans.intf.Chcard.Property property,  
			final cn.sunline.ltts.busi.catran.trans.intf.Chcard.Output output){
	    
		 // 根据电子账号查询出用户ID
	        IoCaKnaCust knacust = SysUtil.getInstance(IoCaSevQryTableInfo.class)
	                .getKnaCustByCardnoOdb1(input.getCardno(), true);
	        KnaCacd knacacd1 = KnaCacdDao.selectOne_odb1(knacust.getCustac(), property.getOdbind(), true);
            
//            String custid = Cif_cust_accsDao.selectOne_odb1(knacust.getCustno(), E_STATUS.NORMAL, true).getCustid();
	        /*
	        IoCifCustAccs cifcustaccs = SysUtil.getInstance(
	                IoCuSevQryTableInfo.class).cif_cust_accsByCustno(
	                knacust.getCustno(), true, E_STATUS.NORMAL);
	         */
	        /*KnpParameter para = KnpParameterDao.selectOne_odb1("BDCAMQ", "%", "%", "%", true);
	        
	        String bdid = para.getParm_value1();// 服务绑定ID
	        
	        String mssdid1 = UUID.randomUUID().toString();// 随机生成消息ID
	        
	        String mesdna1 = para.getParm_value2();// 媒介名称
	        
	        
	        String mssdid2 = UUID.randomUUID().toString();// 随机生成消息ID
	        
	        String mesdna2 = para.getParm_value2();// 媒介名称
	*/      
	        
	        E_BINDTP bindtp1 = E_BINDTP.UNBD;// 绑定方式
//	        MessageRealInfo mri1 = SysUtil.getInstance(MessageRealInfo.class);
//	        mri1.setMtopic("Q0201001");//原来为Q0101003
//	        //DCN 号
//	        mri1.setTdcnno("000");
//
//
//	        E_BINDTP bindtp2 = E_BINDTP.BIND;// 绑定方式
//	        MessageRealInfo mri2 = SysUtil.getInstance(MessageRealInfo.class);
//	        //DCN 号
//	        mri2.setTdcnno("000");
//	        mri2.setMtopic("Q0201001");//原来为Q0101003
//	        
//	        //IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
//	        
//	        //IoCaOtherService.IoCaBindMqService.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaBindMqService.InputSetter.class);
//	        DpBindMqService mqInput = SysUtil.getInstance(DpBindMqService.class);
//	        //mqInput.setMsgid(mssdid1); //发送消息ID
//	        //mqInput.setMdname(mesdna1); //媒介名称
//	        
//	        mqInput.setBindst(bindtp1); //绑定方式
//	        mqInput.setEactno(input.getCardno()); //电子账号
//	        mqInput.setBindno(property.getOdbind()); //绑定账户
//	        mqInput.setAtbkno(knacacd1.getBrchno()); //账户开户行号
//	        mqInput.setAcusna(knacacd1.getAcctna()); //绑定账户名称
//	        mqInput.setAtbkna(knacacd1.getBrchna()); //开户行名称
//	        mqInput.setAccttp(knacacd1.getCardtp()); //绑定账户类型
//	        mqInput.setIsiner(knacacd1.getIsbkca()); //是否本行账户
//	        mqInput.setCustid(custid);// 用户ID
//	        mri1.setMsgtyp("ApSmsType.DpBindMqService");
//	        mri1.setMsgobj(mqInput); 
//	        //AsyncMessageUtil.add(mri1); //将待发送消息放入当前交易暂存区，commit后发送
//	        //caOtherService.bindMqService(mqInput);
//	        
//	        //IoCaOtherService.IoCaBindMqService.InputSetter mqInput2 = SysUtil.getInstance(IoCaOtherService.IoCaBindMqService.InputSetter.class);
//	        DpBindMqService mqInput2 = SysUtil.getInstance(DpBindMqService.class);
//	        //mqInput2.setMsgid(mssdid2); //发送消息ID
//	        //mqInput2.setMdname(mesdna2); //媒介名称
//	        
//	        mqInput2.setBindst(bindtp2); //绑定方式
//	        mqInput2.setEactno(input.getCardno()); //电子账号
//	        mqInput2.setBindno(input.getCdopac()); //绑定账户
//	        mqInput2.setAtbkno(input.getOpbrch()); //账户开户行号
//	        mqInput2.setAcusna(input.getCdopna()); //绑定账户名称
//	        mqInput2.setAtbkna(input.getBrchna()); //开户行名称
//	        mqInput2.setAccttp(input.getCardtp()); //绑定账户类型
//	        mqInput2.setIsiner(input.getIsbkca()); //是否本行账户
//	        mqInput2.setCustid(custid);// 用户ID
//	        
//	        mri2.setMsgtyp("ApSmsType.DpBindMqService");
//	        mri2.setMsgobj(mqInput2); 
//	        AsyncMessageUtil.add(mri2); //将待发送消息放入当前交易暂存区，commit后发送    绑定
//	        AsyncMessageUtil.add(mri1); //将待发送消息放入当前交易暂存区，commit后发送    解绑
	        //caOtherService.bindMqService(mqInput2);
	  }
}
