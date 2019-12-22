package cn.sunline.ltts.busi.catran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacd;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCacdDao;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctDao;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.cf.IoCuTable.IoCifCustAccs;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_BINDTP;

public class cadebc {

	public static void bindmq(
			final cn.sunline.ltts.busi.catran.trans.intf.Cadebc.Input input,
			final cn.sunline.ltts.busi.catran.trans.intf.Cadebc.Property property,
			final cn.sunline.ltts.busi.catran.trans.intf.Cadebc.Output output) {
	    // 根据电子账号查询出用户ID
        IoCaKnaCust knacust = SysUtil.getInstance(IoCaSevQryTableInfo.class)
                .getKnaCustByCardnoOdb1(property.getCardno(), true);
        
        KnaCacd knaCacd = KnaCacdDao.selectOne_odb1(knacust.getCustac(), input.getCdopac(), false);
        if (CommUtil.isNull(knaCacd)) {
            throw CaError.Eacct.BNAS0765();
        }
        IoCifCustAccs   cifCustAccs = DpAcctDao.selCifCustAccsByCustno(knacust.getCustno(),false);
        if(CommUtil.isNull(cifCustAccs)){
            throw DpModuleError.DpstComm.BNAS1932();
        }
        
        /*
        IoCifCustAccs cifcustaccs = SysUtil.getInstance(
                IoCuSevQryTableInfo.class).cif_cust_accsByCustno(
                knacust.getCustno(), true, E_STATUS.NORMAL);
                */
        
        /*KnpParameter para = KnpParameterDao.selectOne_odb1("BDCAMQ", "%", "%", "%", true);
        
        String bdid = para.getParm_value1();// 服务绑定ID
        
        String mssdid = CommTools.getMySysId();// 随机生成消息ID
        
        String mesdna = para.getParm_value2();// 媒介名称
*/      
        
        E_BINDTP bindtp = E_BINDTP.UNBD;
//        MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
//        
//        //modify zhaodj 20181016  begin
//        mri.setMtopic("Q0201001");//原来为Q0101003
//        //modify zhaodj 20181016  end
//        
//        //add by zhaodj 20181016  begin
//        //DCN 号
//        mri.setTdcnno("000");
//        //add by zhaodj 20181016   end 
//        
//        //IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
//        
//        //IoCaOtherService.IoCaBindMqService.InputSetter mqInput = SysUtil.getInstance(IoCaOtherService.IoCaBindMqService.InputSetter.class);
//        DpBindMqService mqInput = SysUtil.getInstance(DpBindMqService.class);
//        //mqInput.setMsgid(mssdid); //发送消息ID
//        //mqInput.setMdname(mesdna); //媒介名称
//        mqInput.setBindst(bindtp); //绑定方式
//        mqInput.setEactno(input.getCardno()); //电子账号
//        mqInput.setBindno(input.getCdopac()); //绑定账户
//        mqInput.setAtbkno(knaCacd.getBrchno()); //账户开户行号
//        mqInput.setAcusna(knaCacd.getAcctna()); //绑定账户名称
//        mqInput.setAtbkna(knaCacd.getBrchna()); //绑定开户行名称
//        mqInput.setAccttp(knaCacd.getCardtp()); //绑定账户类型
//        mqInput.setIsiner(knaCacd.getIsbkca()); //是否本行账户
//        mqInput.setCustid(cifCustAccs.getCustid());// 用户ID
//        
//        mri.setMsgtyp("ApSmsType.DpBindMqService");
//        mri.setMsgobj(mqInput); 
//        AsyncMessageUtil.add(mri); //将待发送消息放入当前交易暂存区，commit后发送
        //caOtherService.bindMqService(mqInput);
		
	  }
}
