package cn.sunline.ltts.busi.dptran.trans.online.conf;

import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActpTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActpTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAddtTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAddtTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCATP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BRCHFG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CUSACT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PARTCD;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRENTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODCT;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRODTG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRTRTP;
/**
 * 
 * @ClassName: dppins 
 * @Description: 增加产品基础部件交易
 * @author xvdawei
 * @date 2016年7月20日 上午11:33:39 
 *
 */

public class dppins {

	public static void insDpp( final cn.sunline.ltts.busi.dptran.trans.online.conf.intf.Dppins.Input Input){
		
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
		E_YES___  isdrft = Input.getIsdrft();//是否允许透支
		String sTime = CommTools.getBaseRunEnvs().getTrxn_date();//当前交易日期
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();// 法人代码
		
		// 校验机构只有省级机构才能操作
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
		
		// 输入项非空检查
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
        
        if (CommUtil.isNull(prodtg)) {
        	throw DpModuleError.DpstComm.BNAS2087();	
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
        
		// 判断产品部件是否启用是否存在
		KupDppbPartTemp parttemp = KupDppbPartTempDao.selectOne_odb1(E_BUSIBI.DEPO, prodcd, E_PARTCD._CK01, false);
		if (CommUtil.isNull(parttemp) || parttemp.getPartfg() != E_YES___.YES) {
			
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
		
		// 产品名称唯一检查
		String prodcd1 = DpProductDao.selDppbByProdtx(prodtx, corpno, false);
		if (CommUtil.isNotNull(prodcd1)) {
			throw DpModuleError.DpstComm.BNAS2094();
		}
		
		// 检查业务大中小细类是否与产品部件一致
		if (parttemp.getProdtp() != prodtp) {
			throw DpModuleError.DpstComm.BNAS2095();
		}
		// 业务小类
		if (parttemp.getPddpfg() != pddpfg) {
			throw DpModuleError.DpstComm.BNAS2096();
		}
		// 业务细类
		if (parttemp.getDebttp() != debttp) {
			throw DpModuleError.DpstComm.BNAS2097();
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
			
		    if (CommUtil.isNull(accatp)) {
		    	throw DpModuleError.DpstComm.BNAS0182();			    	
		    }
		    
		}else {
			
			 if (CommUtil.isNotNull(accatp)) {
			    	throw DpModuleError.DpstComm.BNAS2105();	
			    }
 		 }
		
		//当产品新增为1-通用产品时 ，产品启用方式	
		if (E_PRODTG.CURREN == prodtg) {
			if (CommUtil.isNull(prodtg)) {
				throw DpModuleError.DpstComm.BNAS2106();
			}
		}
		
		//当产品新增为2-定制产品时 ，产品启用方式自能选择2-销售工厂启用		
		if (E_PRODTG.CUSTTOM == prodtg) {
			if (E_PRENTP.SALE != prentp) {
				throw DpModuleError.DpstComm.BNAS2107();
			}
		}
		
		//产品启用方式自能选择1-直接装配启用时，机构控制标志必选
		if (E_PRENTP.ASSE == prentp) {
		    if (CommUtil.isNull(brchfg)) {
        	throw DpModuleError.DpstComm.BNAS2108();	
              }	
		}
		
		
		//产品启用方式自能选择2-销售工厂启用时，机构控制标志不可选		
		if (E_PRENTP.SALE == prentp) {
		    if (CommUtil.isNotNull(brchfg)) {
        	throw DpModuleError.DpstComm.BNAS2109();	
              }	
		}
		
		
		//判断原记录是否存在 产品基础属性临时表
        KupDppbTemp tmp = KupDppbTempDao.selectFirst_odb2(prodcd, false);
		if( CommUtil.isNotNull(tmp)){
			throw DpModuleError.DpstComm.BNAS2110();
		}
		
		//判断原记录是否存在 产品基础属性临时表
		List<KupDppb> tmpa = KupDppbDao.selectAll_odb4(prodcd, false);
		if( CommUtil.isNotNull(tmpa) && tmpa.size() != 0){
			throw DpModuleError.DpstComm.BNAS2110();
		}
		
		//插入新纪录 产品基础属性临时表
		KupDppbTemp entity = SysUtil.getInstance(KupDppbTemp.class);
		entity.setProdcd(prodcd);
		entity.setProdtx(prodtx);		
		entity.setProdtp(prodtp);
		entity.setPddpfg(pddpfg);
		entity.setDebttp(debttp);
		entity.setPdcrcy(pdcrcy);
		entity.setProdtg(prodtg);
		entity.setPrentp(prentp);
		entity.setBrchfg(brchfg);		
		entity.setEfctdt(efctdt);
		entity.setInefdt(inefdt);
		entity.setProtno(protno);
//		entity.setIsdrft(isdrft);
		entity.setProdst(E_PRODST.INPUT);//产品状态为录入
		KupDppbTempDao.insert(entity);
		
		
		//判断原记录是否存在 产品账户类型控制临时表
		KupDppbActpTemp termtmp = KupDppbActpTempDao.selectOne_odb1(prodcd, cacttp, false);
		
		if( CommUtil.isNotNull(termtmp)){
			throw DpModuleError.DpstComm.BNAS2111();
		}
		
		//插入新纪录 产品账户类型控制临时表
		KupDppbActpTemp enttemp = SysUtil.getInstance(KupDppbActpTemp.class);
		
		enttemp.setProdcd(prodcd);
		enttemp.setCacttp(cacttp);
		enttemp.setAcolfg(acolfg);
		KupDppbActpTempDao.insert(enttemp);
		
		// 活期产品新增附加属性表信息
		if (E_FCFLAG.CURRENT == pddpfg) {
			//插入新纪录 产品附加属性临时表
			KupDppbAddtTemp   addttemp = KupDppbAddtTempDao.selectOne_odb1(prodcd, false);
			if( CommUtil.isNotNull(addttemp)){
				throw DpModuleError.DpstComm.BNAS2112();
			}
			// 新增
			KupDppbAddtTemp addtemp = SysUtil.getInstance(KupDppbAddtTemp.class);
			addtemp.setProdcd(prodcd);
			addtemp.setAccatp(accatp);
			KupDppbAddtTempDao.insert(addtemp);
		}
		
		// 产品操作柜员登记
		SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.DEPO, prodcd, E_PRTRTP.ADD);
	}

}
