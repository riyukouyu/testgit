package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.text.DecimalFormat;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdf;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdfDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdt;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmdtDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmex;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrgFmexDao;
import cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adfmex.Input.Fmexif;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_FELYTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class adfmex {
	private static final BizLog bizlog = BizLogUtil.getBizLog(adfmex.class); 
	/**
	 * 
	 * @Title: adfmex 
	 * @Description: 标准费率定义新增 
	 * @param input
	 * @param property
	 * @author songliangwei
	 * @date 2016年7月7日 下午7:20:02 
	 * @version V2.3.0
	 */
	public static void adfmex( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adfmex.Input input,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Adfmex.Property property){
		bizlog.method("adfmex begin >>>>>>");
		//判断当前机构是否为省中心机构
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		
		if (input.getFmexif().size() <= 0){
			throw FeError.Chrg.BNASF004();
		}
		
		for (Fmexif fmexif : input.getFmexif()) {
			String chrgcd = fmexif.getChrgcd(); //费种代码
			String crcycd = BusiTools.getDefineCurrency(); //默认人民币
			String chrgfm = fmexif.getChrgfm(); //计费公式代码
			String scencd = "%"; //默认"%"
			String efctdt = fmexif.getEfctdt(); //生效日期
			String inefdt = fmexif.getInefdt(); //失效日期
			String sTime = CommTools.getBaseRunEnvs().getTrxn_date(); //交易日期
			String seqnum = BusiTools.getSequence("fmex_seq", 6);
			
		    if (CommUtil.isNull(chrgcd)) {
		        throw FeError.Chrg.BNASF076();
		    }
		    if (CommUtil.isNull(chrgfm)) {
		        throw FeError.Chrg.BNASF142();
		    }
		    if (CommUtil.isNull(crcycd)) {
		        throw FeError.Chrg.BNASF156();
		    }
		    if (CommUtil.isNull(scencd)) {
		        throw FeError.Chrg.BNASF016();
		    }
		    if(CommUtil.isNull(efctdt)){
		    	throw FeError.Chrg.BNASF207();
		    }
		    if(CommUtil.isNull(inefdt)){
		    	throw FeError.Chrg.BNASF212();
		    }
			if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
				throw FeError.Chrg.BNASF210();
			}
			if (DateUtil.compareDate(efctdt, sTime) <= 0) {
				throw FeError.Chrg.BNASF204();
			}
			
			if (CommUtil.isNull(KcpChrgDao.selectAll_odb2(chrgcd, crcycd, false))) {
				throw FeError.Chrg.BNASF074();
			}
			
			KcpChrgFmdf tblKcp_chrg_fmdf = KcpChrgFmdfDao.selectOne_odb1(chrgfm, false);
			
			if (CommUtil.isNull(tblKcp_chrg_fmdf)) {
				throw FeError.Chrg.BNASF141();
			}
			
			//add 20170320 增加时间的判断
			if(CommUtil.compare(efctdt, tblKcp_chrg_fmdf.getEfctdt()) < 0 || CommUtil.compare(inefdt, tblKcp_chrg_fmdf.getInefdt()) > 0){
				throw FeError.Chrg.BNASF114();
			}
			
			KcpChrgFmex tblChrgfmex = SysUtil.getInstance(KcpChrgFmex.class);
			KcpChrgFmex tblChrg = SysUtil.getInstance(KcpChrgFmex.class);
			
			tblChrg = KcpChrgFmexDao.selectOne_odb1(chrgcd, chrgfm, false);
			if(CommUtil.isNotNull(tblChrg)){
				throw FeError.Chrg.BNASF080();
			}
			
			//add 20170204 songlw 费种代码取值类型与计费公式档次下限是否匹配
			if(E_FELYTP.NUM == KcpChrgDao.selectOne_odb1(chrgcd, crcycd, false).getFelytp()){ //若为按笔数
				List<KcpChrgFmdt> lstKcp_chrg_fmdt = KcpChrgFmdtDao.selectAll_odb3(chrgfm, false);
				DecimalFormat df = new DecimalFormat("###.####");
				for(KcpChrgFmdt tblKcp_chrg_fmdt : lstKcp_chrg_fmdt){
					String limiam = df.format(tblKcp_chrg_fmdt.getLimiam()); //金额区间下限
					bizlog.debug("limiam的值为[%s]", limiam);
					for(int i = limiam.length(); --i >= 0;){
						if(!Character.isDigit(limiam.charAt(i))){
							throw FeError.Chrg.BNASF083();
						}
					}
				}
			}
			
			tblChrgfmex.setChrgcd(chrgcd); //费种代码
			tblChrgfmex.setChrgfm(chrgfm); //计费公式代码
			tblChrgfmex.setEfctdt(efctdt); //生效日期
			tblChrgfmex.setInefdt(inefdt); //失效日期
			tblChrgfmex.setCrcycd(crcycd); //币种
			tblChrgfmex.setScencd(scencd); //场景
			tblChrgfmex.setSeqnum(seqnum);
			//新增标准费率管理
			KcpChrgFmexDao.insert(tblChrgfmex);		
			
			//增加审计
			ApDataAudit.regLogOnInsertParameter(tblChrgfmex);
			
		}
		
	}
}
