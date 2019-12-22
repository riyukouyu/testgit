package cn.sunline.ltts.busi.fetran.trans.chrg;

import java.util.ArrayList;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeDimeDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpDime;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpDimeDao;
import cn.sunline.ltts.busi.fetran.trans.chrg.intf.Addime.Input.Waylst;
import cn.sunline.ltts.busi.fetran.trans.chrg.intf.Addime.Output.Dimlst;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_WAYTYP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class addime {
	private static final BizLog bizlog = BizLogUtil.getBizLog(addime.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：新增维度类别管理
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */
	public static void addime( final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Addime.Input input,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Addime.Property property,  final cn.sunline.ltts.busi.fetran.trans.chrg.intf.Addime.Output output){
		bizlog.method("addime begin >>>>>>");
		//判断当前机构是否为省中心机构
		if (!CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_branch(), property.getBrchno())) {
			throw FeError.Chrg.BNASF345();
		}
		
		String dimena = input.getDimena();
//		String tranus = CommTools.getBaseRunEnvs().getTrxn_teller(); //交易柜员
//		String transq = CommTools.getBaseRunEnvs().getTrxn_seq(); //流水号
		
		//modify by wenbo 20170705 逻辑判断问题
		/*if (CommUtil.isNull(input.getWaylst().size() <= 0)) {
			throw FeError.Chrg.E1014("维度类型列表");
		}*/
		if (CommUtil.isNull(input.getWaylst())) {
			throw FeError.Chrg.BNASF256();
		}
		if (input.getWaylst().size() <= 0) {
			throw FeError.Chrg.BNASF256();
		}
		
		if (CommUtil.isNull(dimena)) {
			throw FeError.Chrg.BNASF254();
		}
		
		Options<Dimlst> dimlst = new DefaultOptions<Dimlst>();
		
		for (Waylst waylst : input.getWaylst()) {
			
			//维度类型不能为空
			if(CommUtil.isNull(waylst.getWaytyp())){
				throw FeError.Chrg.BNASF255();
			}
			
			E_WAYTYP waytyp = waylst.getWaytyp();
			String dimecg = BusiTools.getSequence("dimecg_seq", 4); //维度类别
			Dimlst dime = SysUtil.getInstance(Dimlst.class);
			dime.setDimecg(dimecg);
			dimlst.add(dime);
			
			KcpDime tblKcpdime = SysUtil.getInstance(KcpDime.class);
		
			//判断维度信息是否存在
			java.util.List<KcpDime> tblKcp = new ArrayList<KcpDime>(); 
			tblKcp = FeDimeDao.selone_kcp_dime(dimena, waytyp,false);   //KcpDimeDao.selectOne_odb1(waytyp, dimecg, false);

			if(CommUtil.isNotNull(tblKcp)){
				throw FeError.Chrg.BNASF104();
			}
		
			tblKcpdime.setWaytyp(waytyp); //维度类型
			tblKcpdime.setDimecg(dimecg); //维度类别
			tblKcpdime.setDimena(input.getDimena()); //维度类别名称
			KcpDimeDao.insert(tblKcpdime);
			
			//增加审计
			ApDataAudit.regLogOnInsertParameter(tblKcpdime);
			
			
		}
		output.setDimlst(dimlst);
		
	}
}
