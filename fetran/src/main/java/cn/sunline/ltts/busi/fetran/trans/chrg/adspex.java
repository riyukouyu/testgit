package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.math.BigDecimal;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeCodeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrg;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSpex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpFavoSpexDao;
import cn.sunline.ltts.busi.iobus.servicetype.IoPbTableSvr;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTable.IoPbKubBrch;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class adspex {
	private static final BizLog bizlog = BizLogUtil.getBizLog(adspex.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：单一优惠管理新增
	 *         </p>
	 * @param @param input
	 * @return void
	 * @throws
	 */
	public static void adspex( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adspex.Input input,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adspex.Property property){
		bizlog.method("adspex begin >>>>>>");
		
		String chrgcd = input.getChrgcd(); //费种代码
		String brchno = input.getBrchno(); //机构号
		String crcycd = BusiTools.getDefineCurrency(); //默认人民币
		String efctdt = input.getEfctdt(); //生效日期
		String inefdt = input.getInefdt(); //失效日期
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
		
		if(CommUtil.isNull(chrgcd)){
			throw FeError.Chrg.BNASF076();
		}
		
		//判断费种代码是否存在与费种代码定义表中
//		kcp_chrg tblkcpchrg = Kcp_chrgDao.selectOne_odb1(chrgcd, crcycd, true);
		KcpChrg tblkcpchrg = FeCodeDao.selall_kcp_chrg_code(chrgcd, false);
		if(CommUtil.isNull(tblkcpchrg)){
			throw FeError.Chrg.BNASF074();
		}
		
		if(CommUtil.isNull(brchno)){
			throw FeError.Chrg.BNASF131();
		}
		
		if(CommUtil.isNull(input.getFasttp())){
			throw FeError.Chrg.BNASF042();
		}
		
		//判断输入机构是否存在
		IoPbTableSvr tbl = CommTools.getRemoteInstance(IoPbTableSvr.class);
		IoPbKubBrch tblkub_brch = tbl.kub_brch_selectOne_odb1(brchno, false);
		if(CommUtil.isNull(tblkub_brch)){
			throw FeError.Chrg.BNASF130();
		}
		
		if(CommUtil.isNull(input.getFavoir())){
			throw FeError.Chrg.BNASF297();
		}
		
		if (CommUtil.compare(input.getFavoir(), BigDecimal.ZERO) <= 0 
				||	CommUtil.compare(input.getFavoir(), BigDecimal.TEN.multiply(BigDecimal.TEN)) > 0) {
			throw FeError.Chrg.BNASF296();
		}
		
		//优惠比例判断

		//del 不判断与费种最低/最高优惠比例　2016／8／1
//		if (CommUtil.compare(input.getFavoir(), tblkcpchrg.getMxfvrt()) > 0) {
//			throw FeError.Chrg.E9999("优惠比例不得大于该费种代码最高优惠比例！");
//		}
//		
//		if (CommUtil.compare(input.getFavoir(), tblkcpchrg.getMnfvrt()) < 0) {
//			throw FeError.Chrg.E9999("优惠比例不得小于该费种代码最低优惠比例！");
//		}
		
		
		//单一优惠起点
		if(CommUtil.isNull(input.getFastam()) || CommUtil.compare(input.getFastam(), BigDecimal.ZERO) < 0){
			throw FeError.Chrg.BNASF041();
		}
		
		//维度类别
		if(CommUtil.isNull(input.getFatype())){
			throw FeError.Chrg.BNASF251();
		}
		
		//维度类别是否存在
		java.util.List<cn.sunline.ltts.busi.fe.tables.FeTable.KcpDime> tblkcpdime = FeDimeDao.selone_kcp_dime_cg(input.getFatype(), false);
		if(CommUtil.isNull(tblkcpdime)){
			throw FeError.Chrg.BNASF250();
		}
		
		//维度值
		if(CommUtil.isNull(input.getFavalu())){
			throw FeError.Chrg.BNASF259();
		}
		
		if (CommUtil.isNull(FeDimeDao.selone_evl_dime(input.getFavalu(), input.getFatype(), false))) {
			throw FeError.Chrg.BNASF258();
		}
		
		if (CommUtil.isNull(inefdt)) {
			throw FeError.Chrg.BNASF212();
		}
		
		if (CommUtil.isNull(efctdt)) {
			throw FeError.Chrg.BNASF207();
		}
		
		if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
			throw FeError.Chrg.BNASF210();
		}
		
		if (DateUtil.compareDate(efctdt, trandt) <= 0) {
			throw FeError.Chrg.BNASF204();
		}
		
	   //判断传入机构是否存在
	   IoSrvPbBranch brchSvrType = SysUtil.getInstance(IoSrvPbBranch.class);
	   try{
		   IoBrchInfo brchinfo = brchSvrType.getBranch(brchno);
	   }catch(Exception e){
		   throw FeError.Chrg.E9999(e.getMessage());
	   }
		
		//省县两级参数管理员均有操作权限，县级行社参数管理员只允许新增本行社
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), BusiTools.getBusiRunEnvs().getCentbr()) &&
				!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), input.getBrchno())) {
			throw FeError.Chrg.BNASF158();
		}
		KcpFavoSpex  kcpFavoSpex = KcpFavoSpexDao.selectOne_odb1(chrgcd, brchno, input.getFasttp(), input.getFastam(), input.getFatype(), input.getFavalu(), false);
		if(CommUtil.isNotNull(kcpFavoSpex)){
			throw FeError.Chrg.BNASF396();
		}
		KcpFavoSpex  tblFavospex = SysUtil.getInstance(KcpFavoSpex.class);
		tblFavospex.setChrgcd(chrgcd); //费种代码
		tblFavospex.setBrchno(brchno); //机构号
		tblFavospex.setCrcycd(crcycd);
		tblFavospex.setFasttp(input.getFasttp()); //单一优惠起点类型
		tblFavospex.setFastam(input.getFastam()); //单一优惠起点
		tblFavospex.setFatype(input.getFatype()); //维度类别
		tblFavospex.setFavalu(input.getFavalu()); //维度值
		tblFavospex.setFavoir(input.getFavoir()); //优惠比例
		tblFavospex.setEfctdt(efctdt); //生效日期
		tblFavospex.setInefdt(inefdt); //失效日期
		
		KcpFavoSpexDao.insert(tblFavospex);
		
		//增加审计
		ApDataAudit.regLogOnInsertParameter(tblFavospex);
		
	}
}
