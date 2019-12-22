package cn.sunline.ltts.busi.cg.utils;


/**
 * 
 * @ClassName: RegUtils 
 * @Description: 登记簿维护公共类
 * @author songliangwei
 * @date 2016年7月25日 下午2:31:42 
 *
 */
public class RegUtils {
	

	/**
	 * 
	 * @Title: KcbMntn 
	 * @Description: 登记簿维护
	 * @param mntntp 维护类别
	 * @param tblena 表名
	 * @author songliangwei
	 * @date 2016年7月25日 下午2:37:53 
	 * @version V2.3.0
	 */
//	public void KcbMntn(E_MNTNTP mntntp, String tblena){
		
//		bizlog.method(">>>>>>>>>>>>>>>>>KcbMntn begin>>>>>>>>>>>>>>>");
//		bizlog.parm("维护类别：mntntp[%s], 表名：tblena[%s]", mntntp, tblena);
//		
//		kcb_mntn kcbMntn = SysUtil.getInstance(kcb_mntn.class);	//维护登记簿
//		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq(); //流水号
//		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller(); //交易柜员
//		String prcscd = CommTools.getBaseRunEnvs().getTrxn_code(); //交易码
//		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
//		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch(); //交易机构
//		
//		kcbMntn.setTransq(transq); 
//		kcbMntn.setBrchno(tranbr);
//		kcbMntn.setPrcscd(prcscd);
//	    kcbMntn.setTranus(tranus); 
//	    kcbMntn.setMntntp(mntntp);
//		kcbMntn.setTblena(tblena); 
//		kcbMntn.setDatetm(trandt);
//		
//		//新增记录
//		Kcb_mntnDao.insert(kcbMntn);
		
//		bizlog.method(">>>>>>>>>>>>>>>>KcbMntn end>>>>>>>>>>>>>>>>>");
//	} 
	
	/**
	 * 
	 * @Title: KcbMntnRgst 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param num
	 * @param afvalu 后映像
	 * @param bfvalu 前映像
	 * @param fielna 字段名
	 * @param fielcn 字段中文名
	 * @author songliangwei
	 * @date 2016年7月29日 下午3:18:52 
	 * @version V2.3.0
	 */
//	public void KcbMntnRgst(Long num, String afvalu, String bfvalu, String fielna, String fielcn){
//		
////		bizlog.method(">>>>>>>>>>>>>>>>>>KcbMntnRgst begin>>>>>>>>>>>>>>");
////		bizlog.parm("序列:num[%s], 后映像：afvalu[%s], 前映像：bfvalu[%s], 字段名[%s], 字段中文名[%s]", num, afvalu, bfvalu, fielna, fielcn);
////		
////		//实例化明细登记簿
////		kcb_mntn_rgst kcbMntnRest = SysUtil.getInstance(kcb_mntn_rgst.class);
////		String transq = CommTools.getBaseRunEnvs().getMain_trxn_seq(); //流水号
////		
////		kcbMntnRest.setTransq(transq);
////		kcbMntnRest.setSernum(num);
////		kcbMntnRest.setAfvalu(afvalu);
////		kcbMntnRest.setBfvalu(bfvalu);
////		kcbMntnRest.setFielna(fielna);
////		kcbMntnRest.setFielcn(fielcn);
////		Kcb_mntn_rgstDao.insert(kcbMntnRest);
////		
////		bizlog.method(">>>>>>>>>>>>>>>>KcbMntnRgst end>>>>>>>>>>>>>>>>");
//	}
	
}
