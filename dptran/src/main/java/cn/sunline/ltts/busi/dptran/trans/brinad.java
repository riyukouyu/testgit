package cn.sunline.ltts.busi.dptran.trans;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BSINAM;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BSINDT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INADTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INEDSC;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IRCDTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_TEARTP;

public class brinad {
	/**
	 * 
	 * @Title: InsPrdInterst 
	 * @Description: 新增违约支取利息部件信息 
	 * @param input
	 * @param output
	 * @author huangzhikai
	 * @date 2016年7月18日 上午10:05:58 
	 * @version V2.3.0
	 */
	public static void InsPrdInterst( final cn.sunline.ltts.busi.dptran.trans.online.conf.Brinad.Input input,  final cn.sunline.ltts.busi.dptran.trans.online.conf.Brinad.Output output){
		//操作网点权限验证
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
			
	//		List<IoDpDfirIn> dfirInList = input.getDfirInfo();
	//		for(IoDpDfirIn dfirIn : dfirInList){
		if(CommUtil.isNull(input.getProdcd())){
			throw DpModuleError.DpstProd.BNAS1054();
		}
				
		if(CommUtil.isNull(input.getTeartp())){
			throw DpModuleError.DpstComm.BNAS1218();
		}
		
		if (CommUtil.isNull(input.getIncdtp())) {
			throw DpModuleError.DpstProd.BNAS1307();
		}
				
		if(CommUtil.isNull(input.getBsincd())){
			throw DpModuleError.DpstProd.BNAS1308();
		}
				
		if(CommUtil.isNull(input.getBsinrl())){
			throw DpModuleError.DpstProd.BNAS1309();
		}
				
		if(CommUtil.isNull(input.getInadtp())){
			throw DpModuleError.DpstComm.BNAS1221();
		}
				
		if (CommUtil.isNull(input.getIntrdt())) {
			throw DpModuleError.DpstComm.BNAS0478();
		}
		
		if(CommUtil.isNull(input.getInsrwy())){
			throw DpModuleError.DpstProd.BNAS1310();
		}
				
		if(CommUtil.isNull(input.getBsinam())){
			throw DpModuleError.DpstProd.BNAS1311();
		}
				
		if(CommUtil.isNull(input.getBsindt())){
			throw DpModuleError.DpstComm.BNAS1226();
		}
				
		if(CommUtil.isNull(input.getInedsc())){
			throw DpModuleError.DpstComm.BNAS1225();
		}
				
		if(CommUtil.isNull(input.getDrdein())){
			throw DpModuleError.DpstProd.BNAS1312();
		}
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();
		String trantm = BusiTools.getBusiRunEnvs().getTrantm();
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();
		String trantn = trandt.concat(trantm);
		
		if(CommUtil.isNotNull(input.getTeartp())){
			//当违约支取利息类型为提前销户，违约利息调整类型只能选择1-全额调整
			if (input.getTeartp() == E_TEARTP.TQXH) {
				if (input.getInadtp() != E_INADTP.QETZ) {
					throw DpModuleError.DpstProd.BNAS1313();
				}
			}
			
			/**
			 *  当违约支取利息类型为2-提前支取、3-扣划金额小于最小支取金额 4- 提前扣划剩余补足最低留存金额 5- 超期支取
			 *  违约利息调整类型只能选择2-部分支取调整
			 */
			if (input.getTeartp() == E_TEARTP.TQZQ
					|| input.getTeartp() == E_TEARTP.BTMN
					|| input.getTeartp() == E_TEARTP.ZDLC
					|| input.getTeartp() == E_TEARTP.OVTM) {

				if (input.getInadtp() != E_INADTP.BFZQ) {
					throw DpModuleError.DpstProd.BNAS1314();
				}

			}
			/**
			 * 当违约支取利息类型为1-提前销户 2-提前支取 3-扣划金额小于最小支取金额 4-提前扣划剩余补足最低留存金额
			 * 结息起始日来源只能选择 1-起息日 2-上次付息日 3- 首次存入日
			 * 结息终止日期只能选择1-当前交易日
			 */
			if (input.getTeartp() == E_TEARTP.TQXH
					|| input.getTeartp() == E_TEARTP.TQZQ
					|| input.getTeartp() == E_TEARTP.BTMN
					|| input.getTeartp() == E_TEARTP.ZDLC) {
				
				if (input.getBsindt() != E_BSINDT.QXR
						&& input.getBsindt() != E_BSINDT.SCFX
						&& input.getBsindt() != E_BSINDT.SCCR) {
					
					throw DpModuleError.DpstProd.BNAS1315();
				}
				
				if (input.getInedsc() != E_INEDSC.DQJY) {
					throw DpModuleError.DpstProd.BNAS1316();
				}

			}
			
			// 当违约支取利息类型为超期支取，起始日来源只能选择到期日，终止日来源只能选择当前交易日
			if (input.getTeartp() == E_TEARTP.OVTM) {
				if (input.getBsindt() != E_BSINDT.TMDT) {
					throw DpModuleError.DpstProd.BNAS1315();
				}
				if (input.getInedsc() != E_INEDSC.DQJY) {
					throw DpModuleError.DpstProd.BNAS1316();
				}
			}
			
			/**
			 * 若违约支取利息类型为2-提前支取 3-扣划金额小于最小支取金额 4-提前扣划剩余补足最低留存金额
			 * 支取结息金额来源只能选择1-交易金额
			 */
			if (input.getTeartp() == E_TEARTP.TQZQ
					|| input.getTeartp() == E_TEARTP.BTMN
					|| input.getTeartp() == E_TEARTP.ZDLC) {

				if (input.getBsinam() != E_BSINAM.JYJE) {
					throw DpModuleError.DpstProd.BNAS1317();
				}
			}
		}
				
		KupDppbPartTemp tblKupDppbPartTemp = KupDppbPartTempDao.selectOne_odb1(DpEnumType.E_BUSIBI.DEPO, input.getProdcd(), E_PARTCD._CK10, false);
		//判断违约利息产品是否配置部件
		if(CommUtil.isNull(tblKupDppbPartTemp)){
			throw DpModuleError.DpstProd.BNAS1318();
		}else{
			if(CommUtil.isNotNull(tblKupDppbPartTemp.getPartfg())){
				if(tblKupDppbPartTemp.getPartfg() == BaseEnumType.E_YES___.NO){
					throw DpModuleError.DpstProd.BNAS1319();
				}
			}
		}
				
		KupDppbTemp tblKupDppbTemp = KupDppbTempDao.selectOne_odb1(input.getProdcd(), false);
		//判断产品基础部件的币种是否存在
		if(CommUtil.isNull(tblKupDppbTemp)){
			throw DpModuleError.DpstProd.BNAS1320();
		}
		
		// 检查基础利率是否是否存在
		if (E_IRCDTP.Reference == input.getIncdtp()) {
//			SysUtil.getInstance(IoIntrSvrType.class).selRfirByrfircdOne(brchno, input.getBsincd(), trandt, trantm);
			int count = DpProductDao.selRfirByRfircd(input.getBsincd(), trantn,corpno, false);
			if (count <= 0) {
				throw DpModuleError.DpstProd.BNAS1321();
			}
			
		// 检查浮动利率是否是否存在
		} else if (E_IRCDTP.BASE == input.getIncdtp()) {
//			SysUtil.getInstance(IoIntrSvrType.class).selBkirByintrcd(brchno, input.getBsincd(), trandt, trantm);
			int count = DpProductDao.selBkirByIntrcd(input.getBsincd(), trantn, corpno, false);
			if (count <= 0) {
				throw DpModuleError.DpstProd.BNAS1321();
			}
			
		// 检查分档利率是否是否存在
		} else if (E_IRCDTP.LAYER == input.getIncdtp()) {
//			SysUtil.getInstance(IoIntrSvrType.class).SelRlirByintrcd(brchno, input.getBsincd());
			int count = DpProductDao.selRlirByIntrcd(input.getBsincd(), trantn, corpno, false);
			if (count <= 0) {
				throw DpModuleError.DpstProd.BNAS1321();
			}
			
			if(CommUtil.isNull(input.getInclfg())){
				throw DpModuleError.DpstProd.BNAS1322();
			}
			
		} else {
			throw DpModuleError.DpstProd.BNAS1323();
		}
		
		if(input.getInclfg() == BaseEnumType.E_YES___.YES){
			if(CommUtil.isNull(input.getIntrwy())){
				throw DpModuleError.DpstProd.BNAS1324();
			}
			if (CommUtil.isNull(input.getLevety())) {
				throw DpModuleError.DpstComm.BNAS0551();
			}
		}
		
		if(input.getInclfg() == BaseEnumType.E_YES___.NO){
			if(CommUtil.isNotNull(input.getIntrwy())){
				throw DpModuleError.DpstProd.BNAS1325();
			}
			if (CommUtil.isNotNull(input.getLevety())) {
				throw DpModuleError.DpstProd.BNAS1326();
			}
		}
		
		//币种		
		String crcycd = tblKupDppbTemp.getPdcrcy();
		//产品编号
		String prodcd = input.getProdcd();
		//违约支取利息类型
		E_TEARTP teartp = input.getTeartp();
		//利息组代码
		String ingpcd = "8888";
				
		//查询产品违约支取利息信息
		KupDppbDfirTemp tblKupDppbDfirTemp = KupDppbDfirTempDao.selectOne_odb1(prodcd, crcycd, teartp, ingpcd, E_INTRTP.ZHENGGLX, false);
				
		if(CommUtil.isNotNull(tblKupDppbDfirTemp)){
			throw DpModuleError.DpstProd.BNAS1327();
		}
				
		KupDppbDfirTemp entity = SysUtil.getInstance(KupDppbDfirTemp.class);
				
		//法人代码
		entity.setCorpno(CommTools.getBaseRunEnvs().getBusi_org_id());
		//产品编号
		entity.setProdcd(prodcd);
		// 币种
		entity.setCrcycd(crcycd);
		//违约支取利息类型
		entity.setTeartp(teartp);
		// 利率代码类型
		entity.setIncdtp(input.getIncdtp());
		// 违约利率代码
		entity.setBsincd(input.getBsincd());
		//利息类型
		entity.setIntrtp(E_INTRTP.ZHENGGLX);
		//违约利率代码
		entity.setIngpcd(ingpcd);
		//违约计息基础
		entity.setBsinrl(input.getBsinrl());
		//违约利息支付方法说明
		entity.setDrintx(input.getDrintx());
		//违约利息调整类型
		entity.setInadtp(input.getInadtp());
		//违约利率靠档标志
		entity.setInclfg(input.getInclfg());
		//违约利率靠档方式
		entity.setIntrwy(input.getIntrwy());
		//违约利率确定方式
		entity.setInsrwy(input.getInsrwy());
		//基准结息金额来源
		entity.setBsinam(input.getBsinam());
		//违约结息起始日来源
		entity.setBsindt(input.getBsindt());
		//违约结息终止日来源
		entity.setInedsc(input.getInedsc());
		//违约支取是否扣除已付利息
		entity.setDrdein(input.getDrdein());
		// 利率确定日期
		entity.setIntrdt(input.getIntrdt());
		// 违约靠档规则
		entity.setLevety(input.getLevety());
				
		KupDppbDfirTempDao.insert(entity);
				
		//output.setProdcd(prodcd);
	}
		//  }

}
