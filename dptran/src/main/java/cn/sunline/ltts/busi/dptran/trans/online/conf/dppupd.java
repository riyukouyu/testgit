package cn.sunline.ltts.busi.dptran.trans.online.conf;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActpTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActpTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAddtTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAddtTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrchTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrchTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfoList;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BRSTUS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BRCHFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_MGINFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRENTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODCT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTG;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;


public class dppupd {

	private final static BizLog bizlog = BizLogUtil.getBizLog(dppupd.class);
	
	public static void updDpp( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dppupd.Input Input){
		
		String    prodcd = Input.getProdcd();//产品编号
		String    prodtx = Input.getProdtx();//产品名称	
		E_PRODCT  prodtp = Input.getProdtp();//业务中类
		E_FCFLAG  pddpfg = Input.getPddpfg();//业务小类
		E_DEBTTP  debttp = Input.getDebttp();//业务细类
		String  pdcrcy = Input.getPdcrcy();//产品币种
		E_ACCATP  accatp = Input.getAccatp();//账户分类
		E_PRODTG  prodtg = Input.getProdtg();//产品性质
		E_PRENTP  prentp = Input.getPrentp();//产品启用方式
		E_BRCHFG  brchfg = Input.getBrchfg();//机构控制标志
		E_CUSACT  cacttp = Input.getCacttp();//客户账号类型
		E_YES___  acolfg = Input.getAcolfg();//账户下唯一标志		
		String    efctdt = Input.getEfctdt();//生效日期	
		String    inefdt = Input.getInefdt();//失效日期
		String    protno = Input.getProtno();//协议文本代码
//		E_YES___  isdrft = Input.getIsdrft();//是否允许透支
		String sTime =CommTools.getBaseRunEnvs().getTrxn_date();//当前交易日期
		String corpno =CommTools.getBaseRunEnvs().getBusi_org_id();// 法人代码
		
		// 校验机构只有省级机构才能操作
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
		
        if (CommUtil.isNull(prodcd)) {
        	throw DpModuleError.DpstProd.BNAS1054();	
          }
		
        if (CommUtil.isNull(prodtx)) {
        	throw DpModuleError.DpstComm.BNAS2082();	
          }
        
        if (CommUtil.isNull(prodtp)) {
        	throw DpModuleError.DpstComm.BNAS2083();	
          }
        
        if (CommUtil.isNull(pddpfg)) {
        	throw DpModuleError.DpstComm.BNAS2084();	
          }
        
        if (CommUtil.isNull(debttp)) {
        	throw DpModuleError.DpstComm.BNAS2085();	
          }
        
        if (CommUtil.isNull(pdcrcy)) {
        	throw DpModuleError.DpstComm.BNAS2086();	
          }
        
        if (CommUtil.isNull(prentp)) {
        	throw DpModuleError.DpstComm.BNAS2088();	
          }
        
        if (CommUtil.isNull(cacttp)) {
        	throw DpModuleError.DpstComm.BNAS1963();	
          }
        
        if (CommUtil.isNull(acolfg)) {
        	throw DpModuleError.DpstComm.BNAS2089();	
          }   
        
		if (E_PRENTP.ASSE == prentp) {
			if (CommUtil.isNull(protno)) {
				throw DpModuleError.DpstComm.BNAS2090();
			}
		} else {
			if (CommUtil.isNotNull(protno)) {
				throw DpModuleError.DpstComm.BNAS2091();
			}
		}
        
//        if (CommUtil.isNull(isdrft)) {
//        	throw DpModuleError.DpstComm.E9999( "是否允许透支必输不能为空");	
//          }
		
		//判断记录是否已生效
		KupDppb   dppb = KupDppbDao.selectOne_odb1(prodcd, false);
		
		if( CommUtil.isNotNull(dppb)){
			throw DpModuleError.DpstComm.BNAS2113();
		}

		//判断原记录是否存在 产品基础属性临时表
	    KupDppbTemp tmp = KupDppbTempDao.selectFirst_odb2(prodcd, false);
        
		if( CommUtil.isNull(tmp)){
			throw DpModuleError.DpstComm.BNAS2114();
		}
		
		// 币种
		if (CommUtil.isNotNull(tmp.getPdcrcy()) && CommUtil.compare(tmp.getPdcrcy(), pdcrcy) != 0) {
			throw DpModuleError.DpstComm.BNAS2115();
		}
		
        //产品启用方式为 直接装配启用时，生效日期、失效日期必须设置
        if (E_PRENTP.ASSE == prentp) {
        	
	        if (CommUtil.isNull(efctdt)) {
	         	throw DpModuleError.DpstComm.BNAS2098();	
	        }
	        if (CommUtil.isNull(inefdt)) {
	         	throw DpModuleError.DpstComm.BNAS2098();	
	        }
        	if (DateUtil.compareDate(inefdt, efctdt) <= 0) {
    			throw DpModuleError.DpstComm.BNAS2100();
    		}
    		if (DateUtil.compareDate(efctdt, sTime) <= 0) {
    			throw DpModuleError.DpstComm.BNAS2101();
    		}
			
		}
        
        //产品启用方式为 销售工厂启用时，生效日期、失效日期不允许设置
        if (E_PRENTP.SALE == prentp) {
        	
	        if (CommUtil.isNotNull(efctdt)) {
	         	throw DpModuleError.DpstComm.BNAS2102();	
	        }
	        if (CommUtil.isNotNull(inefdt)) {
	         	throw DpModuleError.DpstComm.BNAS2102();	
	        }
        	
			
		}
        
        // 产品名称唯一检查
 		String prodcd1 = DpProductDao.selDppbByProdtx(prodtx, corpno, false);
 		if (CommUtil.isNotNull(prodcd1) && !CommUtil.equals(prodcd, prodcd1)) {
 			throw DpModuleError.DpstComm.BNAS2094();
 		}
            
        //判断业务小类和业务中类是否匹配
        String testa = pddpfg.getValue().substring(0, prodtp.getValue().length());
        
        if (!CommUtil.equals(testa, prodtp.getValue())) {
          	throw DpModuleError.DpstComm.BNAS2103();
		}
        
		//判断业务小类和业务细类是否匹配
        String testb = debttp.getValue().substring(0, pddpfg.getValue().length());
        
        if (!CommUtil.equals(testb, pddpfg.getValue())) {
          	throw DpModuleError.DpstComm.BNAS2104();
		}
        
		//业务小类选择0204-个人电子账户活期存款时 账户分类必输
		if (E_FCFLAG.CURRENT == pddpfg ) {	
			
			//  业务细类为智能活期时，账户分类需为空
		    if (E_DEBTTP.DP2404 == debttp) {
		    	if (CommUtil.isNotNull(accatp)) {
			    	throw DpModuleError.DpstComm.BNAS2099();	
			    }
			} else {
				bizlog.debug("账户分类:" + accatp);
				if (CommUtil.isNull(accatp)) {
					throw DpModuleError.DpstComm.BNAS0182();
				}
			}		    	
		    
		}else {			
			
			bizlog.debug("账户分类:" + accatp);

			if (CommUtil.isNotNull(accatp)) {
				//throw DpModuleError.DpstComm.BNAS2116();
				throw DpModuleError.DpstComm.E9999("业务小类不是个人电子账户活期存款时，账户分类不可输入");
			}
		}
		
		//产品启用方式1-直接装配启用时，产品性质只能选1-通用产品
		if (E_PRENTP.ASSE == prentp) {
			if (E_PRODTG.CURREN != prodtg ) {
				throw DpModuleError.DpstComm.BNAS2117();
			}
		}
		
		// 产品启用方式为2-销售工厂启用时，产品性质置灰
		if (E_PRENTP.SALE == prentp) {
			if (CommUtil.isNotNull(prodtg)) {
				throw DpModuleError.DpstComm.BNAS2118();
			}
		}
		
		//产品启用方式自能选择1-直接装配启用时，机构控制标志必选
		if (E_PRENTP.ASSE == prentp) {
			if (CommUtil.isNull(brchfg)) {
				throw DpModuleError.DpstComm.BNAS2108();
			}
		    
		    // 机构控制标志为空
		    if (CommUtil.isNull(tmp.getBrchfg())) {
		    	if (brchfg == E_BRCHFG.ALL) {
		    		// 新增全部机构为适用机构
		    		addBrchTemp(prodcd, pdcrcy,efctdt, inefdt);
		    	}
		    	
		    } else {
		    	// 由适用机构改为全部机构
		    	if (brchfg == E_BRCHFG.ALL && tmp.getBrchfg() == E_BRCHFG.USE) {
		    		// 先删除原所有适用机构
		    		DpProductDao.delKupBrchTempByProdcd(prodcd, tmp.getPdcrcy());
		    		// 新增全部机构为适用机构
		    		addBrchTemp(prodcd, pdcrcy,efctdt, inefdt);
		    
		    	// 由全部机构改为适用机构
				} else if (brchfg == E_BRCHFG.USE && tmp.getBrchfg() == E_BRCHFG.ALL) {
					// 删除原有所有适用机构
					DpProductDao.delKupBrchTempByProdcd(prodcd, tmp.getPdcrcy());
				
				// 没有发生变化修改机构生效/失效日期为产品生效失效日期
				} else {
					
					DpProductDao.updBrchTempByprodcd(prodcd, efctdt, inefdt);
				}
		    }
		}
		
		//产品启用方式自能选择2-销售工厂启用时，机构控制标志不可选
		
		if (E_PRENTP.SALE == prentp) {
		    if (CommUtil.isNotNull(brchfg)) {
        	throw DpModuleError.DpstComm.BNAS2109();	
              }	
		}
	
		//判断产品部件是否启用是否存在
		KupDppbPartTemp    parttemp = KupDppbPartTempDao.selectOne_odb1(E_BUSIBI.DEPO, prodcd, E_PARTCD._CK01, false);
		if( CommUtil.isNull(parttemp) || parttemp.getPartfg() != E_YES___.YES){
			throw DpModuleError.DpstProd.BNAS1355();
		} 
		
		// 检查产品性质是否与产品部件表一致
		if (parttemp.getProdtg() != prodtg) {
			throw DpModuleError.DpstComm.BNAS2092();
		}
		// 检查产品启用方式是否与产品部件表一致
		if (parttemp.getPrentp() != prentp) {
			throw DpModuleError.DpstComm.BNAS2093();
		}
		// 早起息标志为0-否时，早起息天数为 null
		if (tmp.getMginfg() != E_MGINFG.PMIT) {
			tmp.setMgindy(null);
		}
		
		//修改纪录 产品基础属性临时表		
		tmp.setProdcd(prodcd);
		tmp.setProdtx(prodtx);		
		tmp.setProdtp(prodtp);
		tmp.setPddpfg(pddpfg);
		tmp.setDebttp(debttp);
		tmp.setPdcrcy(pdcrcy);
		tmp.setProdtg(prodtg);
		tmp.setPrentp(prentp);
		tmp.setBrchfg(brchfg);		
		tmp.setEfctdt(efctdt);
		tmp.setInefdt(inefdt);
		tmp.setProtno(protno);
//		tmp.setIsdrft(isdrft);
		tmp.setProdst(E_PRODST.INPUT);//产品状态为录入
		KupDppbTempDao.updateOne_odb1(tmp);
		
		
		//判断原记录是否存在 产品账户类型控制临时表
		KupDppbActpTemp termtmp = KupDppbActpTempDao.selectOne_odb1(prodcd, cacttp, false);
		if( CommUtil.isNull(termtmp)){
			//新增纪录 产品账户类型控制临时表
			termtmp = SysUtil.getInstance(KupDppbActpTemp.class);
			termtmp.setProdcd(prodcd);
			termtmp.setCacttp(cacttp);
			termtmp.setAcolfg(acolfg);
			KupDppbActpTempDao.insert(termtmp);
		} else {
			
			//修改纪录 产品账户类型控制临时表
			termtmp.setProdcd(prodcd);
			termtmp.setCacttp(cacttp);
			termtmp.setAcolfg(acolfg);
			KupDppbActpTempDao.updateOne_odb1(termtmp);
		}
		
		//修改纪录 产品附加属性临时表
		if (E_FCFLAG.CURRENT == tmp.getPddpfg()) {
			
			KupDppbAddtTemp addttemp = KupDppbAddtTempDao.selectOne_odb1( prodcd, false);
			if (CommUtil.isNull(addttemp)) {
				
				// 新增
				addttemp = SysUtil.getInstance(KupDppbAddtTemp.class);
				addttemp.setProdcd(prodcd);
				addttemp.setAccatp(accatp);
				KupDppbAddtTempDao.insert(addttemp);
				
			} else {
				// 修改
				addttemp.setProdcd(prodcd);
				addttemp.setAccatp(accatp);
				KupDppbAddtTempDao.updateOne_odb1(addttemp);
			}

		}
		
	}
	
	/**
	 * 
	 * @Title: addBrchTemp 
	 * @Description: 新增全部机构为产品适用机构
	 * @param prodcd 产品号
	 * @param crcycd 币种
	 * @param efctdt 产品生效日期
	 * @param inefdt 产品失效日期
	 * @author liaojincai
	 * @date 2016年11月9日 下午2:02:48 
	 * @version V2.3.0
	 */
	public static void addBrchTemp(String prodcd, String crcycd, String efctdt, String inefdt ) {
		
		// 取得所有机构
		Options<IoBrchInfoList> cplBrchListOuts = SysUtil.getInstance(IoSrvPbBranch.class).getBranchList().getBrinfo();
		
		for (IoBrchInfoList cplBrchListOut : cplBrchListOuts) {
			
			// 获取启用的机构
			if (E_BRSTUS.valid == cplBrchListOut.getBrstus()) {
				// 根据机构查询对应法人
				IoBrchInfo cplIoBrchInfo = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(cplBrchListOut.getBrchno());
				if (CommUtil.isNull(cplIoBrchInfo)) {
					throw DpModuleError.DpstComm.BNAS1168(cplBrchListOut.getBrchno());
				}
				
				// 添加机构信息
				KupDppbBrchTemp tblBrchTemp = SysUtil.getInstance(KupDppbBrchTemp.class);
				tblBrchTemp.setBrchno(cplBrchListOut.getBrchno());// 机构号
				tblBrchTemp.setCrcycd(crcycd);// 币种
				tblBrchTemp.setProdcd(prodcd);// 产品号
				tblBrchTemp.setEfctdt(efctdt);// 启用日期
				tblBrchTemp.setInefdt(inefdt);// 停用日期
				tblBrchTemp.setCorpno(cplIoBrchInfo.getCorpno());// 法人代码

				KupDppbBrchTempDao.insert(tblBrchTemp);
			}
			
		}
	}
}
