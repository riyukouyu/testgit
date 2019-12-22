package cn.sunline.ltts.busi.dptran.trans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCorp;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpCopyProdDao;
import cn.sunline.ltts.busi.dp.namedsql.DpProductDao;
import cn.sunline.ltts.busi.dp.servicetype.DpProdSvcType;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppb;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcct;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcctTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAcctTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbActpTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAddt;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAddtTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbAddtTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrch;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrchTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbBrchTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCust;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbCustTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfir;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDfirTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDraw;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawPlan;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrawTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrplTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbDrplTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntr;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntrTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbIntrTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatu;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbMatuTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPart;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPartTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPoplTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPost;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostPlan;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbPostTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbSync;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbSyncDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTemp;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTerm;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbTermTempDao;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbUser;
import cn.sunline.ltts.busi.dp.tables.DpProduct.KupDppbUserDao;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoBasePrdIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpBranchIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCheckPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpCustPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDfirIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDrawIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDrawPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpDrawPlanIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpIntrIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpMatuIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpMatuPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpOpenIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpPlanIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpPostIn;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpPostPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpPostplPartDetail;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpProdParts.IoDpTermInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SYNCST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_INTRTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRENTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_PRTRTP;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class prchck {
	private static BizLog log = BizLogUtil.getBizLog(prchck.class);
	/**
	 * 
	 * @Title: chkPrdInfo 
	 * @Description: 产品配置复合 
	 * @param input
	 * @param output
	 * @author huangzhikai
	 * @date 2016年7月14日 上午11:00:58 
	 * @version V2.3.0
	 */
	public static void chkPrdInfo( final cn.sunline.ltts.busi.dptran.trans.online.conf.Prchck.Input input){
		//操作网点权限验证
		DpPublic.provinceBrchCheck(CommTools.getBaseRunEnvs().getTrxn_branch());
		String timetm = DateTools2.getCurrentTimestamp();// 时间戳
		String prodcd = input.getProdcd();// 产品编号
	//	String mtdate = CommTools.getBaseRunEnvs().getTrxn_date();// 时间
		
		if(CommUtil.isNull(input.getProdcd())){
			throw DpModuleError.DpstProd.BNAS1054();
		}
		
		if (CommUtil.isNull(input.getTyinno())) {
			throw DpModuleError.DpstComm.BNAS1397();
		}
		// 获取产品信息
		KupDppbTemp tblkup_dppb1 = KupDppbTempDao.selectOne_odb1(input.getProdcd(), false);
		if (CommUtil.isNull(tblkup_dppb1)) {
			throw DpModuleError.DpstComm.BNAS1398();
		}
		
		// 查询产品新增柜员
		KupDppbUser tblkup_user = KupDppbUserDao.selectOne_odb(E_BUSIBI.DEPO, input.getProdcd(), E_PRTRTP.ADD, false);
		if (CommUtil.isNull(tblkup_user)){
			throw DpModuleError.DpstComm.BNAS1399();
		}
		if (CommUtil.equals(CommTools.getBaseRunEnvs().getTrxn_teller(), tblkup_user.getTranus())) {
			throw DpModuleError.DpstComm.BNAS1800();
		}
		
		// 检查产品状态是否为待复核的状态
		if (E_PRODST.ASSE != tblkup_dppb1.getProdst()) {
			throw DpModuleError.DpstComm.BNAS1801();
		}
		
		// 判断录入编号是否正确
		if (!CommUtil.equals(input.getTyinno(), tblkup_dppb1.getTyinno())) {
			throw DpModuleError.DpstComm.BNAS1802();
		}
		
		//检查产品部件信息
		DpPublic.chkProdInfos(input.getProdcd(),"1");
		
		log.debug("++++++++产品核算部件复合开始+++++++++");
//	
		KupDppbAcctTemp tblKupDppbAcctTemp = KupDppbAcctTempDao.selectFirst_odb2(input.getProdcd(), false);
		
		//判断核算部件信息是否存在
		IoDpCheckPartDetail accInfo = input.getAcctInfo();
		if(CommUtil.isNotNull(tblKupDppbAcctTemp.getAcctcd())){
			if(CommUtil.isNotNull(accInfo.getAcctcd())){
				if(!CommUtil.equals(tblKupDppbAcctTemp.getAcctcd(), accInfo.getAcctcd())){
					throw DpModuleError.DpstComm.BNAS1803();
				}
			}else{
				throw DpModuleError.DpstComm.BNAS1803();
			}
		}else{
			if(CommUtil.isNotNull(accInfo.getAcctcd())){
				throw DpModuleError.DpstComm.BNAS1803();
			}
		}
		
		log.debug("++++++++产品核算部件复合结束+++++++++");
		
		
		log.debug("++++++++产品违约支取利息利率部件复合开始+++++++++");
		
		//获取产品违约支取利息利率部件信息
		List<IoDpDfirIn> dfirInfo = input.getDfirInfo();
        List<KupDppbDfirTemp> difrInfo1 = KupDppbDfirTempDao.selectAll_odb4(input.getProdcd(), false);
        
        //比较录入的违约支取利息集合和查询的违约支取利息集合
        if(CommUtil.isNotNull(difrInfo1)){
        	if(CommUtil.isNotNull(dfirInfo)){
        		 if(CommUtil.compare(dfirInfo.size(), difrInfo1.size()) != 0){
        			 throw DpModuleError.DpstComm.BNAS1804();
        	     }
        	}else{
        		throw DpModuleError.DpstComm.BNAS1804();
        	}
        }else{
        	if(CommUtil.isNotNull(dfirInfo)){
        		throw DpModuleError.DpstComm.BNAS1804();
        	}
        }
       
		//查询基础属性部件信息获取币种
		KupDppbTemp tbl_KupDppbTemp = KupDppbTempDao.selectOne_odb1(input.getProdcd(), false);
		//判断产品基础部件的币种是否存在
		if(CommUtil.isNull(tbl_KupDppbTemp)){
			throw DpModuleError.DpstComm.BNAS1805();
		}
		
		if(CommUtil.isNotNull(dfirInfo)){
			for(IoDpDfirIn dfirIn : dfirInfo){
				//查询产品违约支取利息信息
				KupDppbDfirTemp tblKupDppbDfirTemp = KupDppbDfirTempDao.selectOne_odb1(input.getProdcd(), tbl_KupDppbTemp.getPdcrcy(), dfirIn.getTeartp(), "8888", E_INTRTP.ZHENGGLX, false);
				
				if(CommUtil.isNull(tblKupDppbDfirTemp)){
					throw DpModuleError.DpstComm.BNAS1532();
				}
				//判断录入的违约支取利息类型和查询的违约支取利息类型是否一致
				if(CommUtil.isNotNull(tblKupDppbDfirTemp.getTeartp())){
					if(CommUtil.isNotNull(dfirIn.getTeartp())){
						if(tblKupDppbDfirTemp.getTeartp() != dfirIn.getTeartp()){
							throw DpModuleError.DpstComm.BNAS1806();
						}
					}else{
						throw DpModuleError.DpstComm.BNAS1806();
					}
				}else{
					if(CommUtil.isNotNull(dfirIn.getTeartp())){
						throw DpModuleError.DpstComm.BNAS1806();
					}
				}
				
				//判断录入的违约利率代码和查询的违约利率代码是否一致
				if(CommUtil.isNotNull(tblKupDppbDfirTemp.getBsincd())){
					if(CommUtil.isNotNull(dfirIn.getBsincd())){
						if(!CommUtil.equals(tblKupDppbDfirTemp.getBsincd(), dfirIn.getBsincd())){
							throw DpModuleError.DpstComm.BNAS1807();
						}
					}else{
						throw DpModuleError.DpstComm.BNAS1807();
					}
				}else{
					if(CommUtil.isNotNull(dfirIn.getBsincd())){
						throw DpModuleError.DpstComm.BNAS1807();
					}
				}
				
				//判断录入违约利息调整类型和查询的违约利息调整类型是否一致
				if(CommUtil.isNotNull(tblKupDppbDfirTemp.getInadtp())){
					if(CommUtil.isNotNull(dfirIn.getInadtp())){
						if(tblKupDppbDfirTemp.getInadtp() != dfirIn.getInadtp()){
							throw DpModuleError.DpstComm.BNAS1808();
						}
					}else{
						throw DpModuleError.DpstComm.BNAS1808();
					}
				}else{
					if(CommUtil.isNotNull(dfirIn.getInadtp())){
						throw DpModuleError.DpstComm.BNAS1808();
					}
				}
				
				//判断录入违约利率靠档标志和查询的违约利率靠档标志是否一致
				if(CommUtil.isNotNull(tblKupDppbDfirTemp.getInclfg())){
					if(CommUtil.isNotNull(dfirIn.getInclfg())){
						if(tblKupDppbDfirTemp.getInclfg() != dfirIn.getInclfg()){
							throw DpModuleError.DpstComm.BNAS1809();
						}
					}else{
						throw DpModuleError.DpstComm.BNAS1809();
					}
				}else{
					if(CommUtil.isNotNull(dfirIn.getInclfg())){
						throw DpModuleError.DpstComm.BNAS1809();
					}
				}
				
				//判断录入违约利率靠档方式和查询的违约利率靠档方式是否一致
				if(CommUtil.isNotNull(tblKupDppbDfirTemp.getIntrwy())){
					if(CommUtil.isNotNull(dfirIn.getIntrwy())){
						if(tblKupDppbDfirTemp.getIntrwy() != dfirIn.getIntrwy()){
							throw DpModuleError.DpstComm.BNAS1810();
						}
					}else{
						throw DpModuleError.DpstComm.BNAS1810();
					}
				}else{
					if(CommUtil.isNotNull(dfirIn.getIntrwy())){
						throw DpModuleError.DpstComm.BNAS1810();
					}
				}
				
				//判断录入违约利率确定方式和查询的违约利率确定方式是否一致
				if(CommUtil.isNotNull(tblKupDppbDfirTemp.getInsrwy())){
					if(CommUtil.isNotNull(dfirIn.getInsrwy())){
						if(tblKupDppbDfirTemp.getInsrwy() != dfirIn.getInsrwy()){
							throw DpModuleError.DpstComm.BNAS1811();
						}
					}else{
						throw DpModuleError.DpstComm.BNAS1811();
					}
				}else{
					if(CommUtil.isNotNull(dfirIn.getInsrwy())){
						throw DpModuleError.DpstComm.BNAS1811();
					}
				}
				
				//判断录入违约结息金额来源和查询的违约结息金额来源是否一致
				if(CommUtil.isNotNull(tblKupDppbDfirTemp.getBsinam())){
					if(CommUtil.isNotNull(dfirIn.getBsinam())){
						if(tblKupDppbDfirTemp.getBsinam() != dfirIn.getBsinam()){
							throw DpModuleError.DpstComm.BNAS1812();
						}
					}else{
						throw DpModuleError.DpstComm.BNAS1812();
					}
				}else{
					if(CommUtil.isNotNull(dfirIn.getBsinam())){
						throw DpModuleError.DpstComm.BNAS1812();
					}
				}
				
				//判断录入违约结息起始日来源和查询的违约结息起始日来源是否一致
				if(CommUtil.isNotNull(tblKupDppbDfirTemp.getBsindt())){
					if(CommUtil.isNotNull(dfirIn.getBsindt())){
						if(tblKupDppbDfirTemp.getBsindt() != dfirIn.getBsindt()){
							throw DpModuleError.DpstComm.BNAS1813();
						}
					}else{
						throw DpModuleError.DpstComm.BNAS1813();
					}
				}else{
					if(CommUtil.isNotNull(dfirIn.getBsindt())){
						throw DpModuleError.DpstComm.BNAS1813();
					}
				}
				
				//判断录入违约结息终止日来源和查询的违约结息终止日来源是否一致
				if(CommUtil.isNotNull(tblKupDppbDfirTemp.getInedsc())){
					if(CommUtil.isNotNull(dfirIn.getInedsc())){
						if(tblKupDppbDfirTemp.getInedsc() != dfirIn.getInedsc()){
							throw DpModuleError.DpstComm.BNAS1814();
						}
					}else{
						throw DpModuleError.DpstComm.BNAS1814();
					}
				}else{
					if(CommUtil.isNotNull(dfirIn.getInedsc())){
						throw DpModuleError.DpstComm.BNAS1814();
					}
				}
				
				//判断录入违约支取是否扣除已付利息和查询的违约支取是否扣除已付利息是否一致
				if(CommUtil.isNotNull(tblKupDppbDfirTemp.getDrdein())){
					if(CommUtil.isNotNull(dfirIn.getDrdein())){
						if(tblKupDppbDfirTemp.getDrdein() != dfirIn.getDrdein()){
							throw DpModuleError.DpstComm.BNAS1815();
						}
					}else{
						throw DpModuleError.DpstComm.BNAS1815();
					}
				}else{
					if(CommUtil.isNotNull(dfirIn.getDrdein())){
						throw DpModuleError.DpstComm.BNAS1815();
					}
				}
			}
		}
		
		log.debug("++++++++产品违约支取利息利率部件复合结束+++++++++");
		
		
		log.debug("++++++++产品利息利率部件复合开始+++++++++");
		
		IoDpIntrIn intrInfo = input.getIntrInfo();
		
		KupDppbIntrTemp tblKupDppbIntrTemp = KupDppbIntrTempDao.selectOne_odb5(input.getProdcd(), false);
		
		//比较录入的产品利息利率部件和查询的产品利息利率部件
		if(CommUtil.isNotNull(tblKupDppbIntrTemp)){
			if(CommUtil.isNull(intrInfo)){
				throw DpModuleError.DpstComm.BNAS1816();
			}
			
		}else{
			if(CommUtil.isNotNull(intrInfo)){
				throw DpModuleError.DpstComm.BNAS1816();
			}
		}
		
		if(CommUtil.isNotNull(intrInfo) && CommUtil.isNotNull(tblKupDppbIntrTemp)){
			
			//判断录入利息类型和查询的利息类型是否一致
			if(CommUtil.isNotNull(tblKupDppbIntrTemp.getIntrtp())){
				if(CommUtil.isNotNull(intrInfo.getIntrtp())){
					if(tblKupDppbIntrTemp.getIntrtp() != intrInfo.getIntrtp()){
						throw DpModuleError.DpstComm.BNAS1817();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1817();
				}
			}else{
				if(CommUtil.isNotNull(intrInfo.getIntrtp())){
					throw DpModuleError.DpstComm.BNAS1817();
				}
			}
			
			//判断录入计息标志和查询的计息标志是否一致
			if(CommUtil.isNotNull(tblKupDppbIntrTemp.getInbefg())){
				if(CommUtil.isNotNull(intrInfo.getInbefg())){
					if(tblKupDppbIntrTemp.getInbefg() != intrInfo.getInbefg()){
						throw DpModuleError.DpstComm.BNAS1818();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1818();
				}
			}else{
				if(CommUtil.isNotNull(intrInfo.getInbefg())){
					throw DpModuleError.DpstComm.BNAS1818();
				}
			}
			
			//判断录入税率编号和查询的税率编号是否一致
			if(CommUtil.isNotNull(tblKupDppbIntrTemp.getTaxecd())){
				if(CommUtil.isNotNull(intrInfo.getTaxecd())){
					if(!CommUtil.equals(tblKupDppbIntrTemp.getTaxecd(), intrInfo.getTaxecd())){
						throw DpModuleError.DpstComm.BNAS1819();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1819();
				}
			}else{
				if(CommUtil.isNotNull(intrInfo.getTaxecd())){
					throw DpModuleError.DpstComm.BNAS1819();
				}
			}
			
			//判断录入计息频率和查询的计息频率是否一致
			if(CommUtil.isNotNull(tblKupDppbIntrTemp.getTebehz())){
				if(CommUtil.isNotNull(intrInfo.getTebehz())){
					if(!CommUtil.equals(tblKupDppbIntrTemp.getTebehz(), intrInfo.getTebehz())){
						throw DpModuleError.DpstComm.BNAS1820();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1820();
				}
			}else{
				if(CommUtil.isNotNull(intrInfo.getTebehz())){
					throw DpModuleError.DpstComm.BNAS1820();
				}
			}
			
			//判断录入结息频率和查询的结息频率是否一致
			if(CommUtil.isNotNull(tblKupDppbIntrTemp.getTxbefr())){
				if(CommUtil.isNotNull(intrInfo.getTxbefr())){
					if(!CommUtil.equals(tblKupDppbIntrTemp.getTxbefr(), intrInfo.getTxbefr())){
						throw DpModuleError.DpstComm.BNAS1821();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1821();
				}
			}else{
				if(CommUtil.isNotNull(intrInfo.getTxbefr())){
					throw DpModuleError.DpstComm.BNAS1821();
				}
			}
			
			//判断录入利率代码和查询的利率代码是否一致
			if(CommUtil.isNotNull(tblKupDppbIntrTemp.getIntrcd())){
				if(CommUtil.isNotNull(intrInfo.getIntrcd())){
					if(!CommUtil.equals(tblKupDppbIntrTemp.getIntrcd(), intrInfo.getIntrcd())){
						throw DpModuleError.DpstComm.BNAS1822();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1822();
				}
			}else{
				if(CommUtil.isNotNull(intrInfo.getIntrcd())){
					throw DpModuleError.DpstComm.BNAS1822();
				}
			}
			
			//判断录入利率代码类型和查询的利率代码类型是否一致
			if(CommUtil.isNotNull(tblKupDppbIntrTemp.getIncdtp())){
				if(CommUtil.isNotNull(intrInfo.getIncdtp())){
					if(tblKupDppbIntrTemp.getIncdtp() != intrInfo.getIncdtp()){
						throw DpModuleError.DpstComm.BNAS1823();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1823();
				}
			}else{
				if(CommUtil.isNotNull(intrInfo.getIncdtp())){
					throw DpModuleError.DpstComm.BNAS1823();
				}
			}
			
			//判断录入利率靠档标志和查询的利率靠档标志是否一致
			if(CommUtil.isNotNull(tblKupDppbIntrTemp.getInwytp())){
				if(CommUtil.isNotNull(intrInfo.getInwytp())){
					if(tblKupDppbIntrTemp.getInwytp() != intrInfo.getInwytp()){
						throw DpModuleError.DpstComm.BNAS1824();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1824();
				}
			}else if(CommUtil.isNull(tblKupDppbIntrTemp.getInwytp())){
				if(CommUtil.isNotNull(intrInfo.getInwytp())){
					throw DpModuleError.DpstComm.BNAS1824();
				}
			}
			
			//判断录入利率靠档方式和查询的利率靠档方式是否一致
			if(CommUtil.isNotNull(tblKupDppbIntrTemp.getIntrwy())){
				if(CommUtil.isNotNull(intrInfo.getIntrwy())){
					if(tblKupDppbIntrTemp.getIntrwy() != intrInfo.getIntrwy()){
						throw DpModuleError.DpstComm.BNAS1825();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1825();
				}
			}else{
				if(CommUtil.isNotNull(intrInfo.getIntrwy())){
					throw DpModuleError.DpstComm.BNAS1825();
				}
			}
			
			//判断录入分层计息方式和查询的分层计息方式是否一致
			if(CommUtil.isNotNull(tblKupDppbIntrTemp.getLyinwy())){
				if(CommUtil.isNotNull(intrInfo.getLyinwy())){
					if(tblKupDppbIntrTemp.getLyinwy() != intrInfo.getLyinwy()){
						throw DpModuleError.DpstComm.BNAS1826();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1826();
				}
			}else{
				if(CommUtil.isNotNull(intrInfo.getLyinwy())){
					throw DpModuleError.DpstComm.BNAS1826();
				}
			}
			
			//判断录入计息金额模式和查询的计息金额模式是否一致
			if(CommUtil.isNotNull(tblKupDppbIntrTemp.getInammd())){
				if(CommUtil.isNotNull(intrInfo.getInammd())){
					if(tblKupDppbIntrTemp.getInammd() != intrInfo.getInammd()){
						throw DpModuleError.DpstComm.BNAS1827();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1827();
				}
			}else{
				if(CommUtil.isNotNull(intrInfo.getInammd())){
					throw DpModuleError.DpstComm.BNAS1827();
				}
			}
			
			//判断录入利率调整频率和查询的利率调整频率是否一致
			if(CommUtil.isNotNull(tblKupDppbIntrTemp.getInadlv())){
				if(CommUtil.isNotNull(intrInfo.getInadlv())){
					if(!CommUtil.equals(tblKupDppbIntrTemp.getInadlv(), intrInfo.getInadlv())){
						throw DpModuleError.DpstComm.BNAS1828();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1828();
				}
			}else{
				if(CommUtil.isNotNull(intrInfo.getInadlv())){
					throw DpModuleError.DpstComm.BNAS1828();
				}
			}
			
			//判断录入重定价利率处理方式和查询的重定价利率处理方式是否一致
			if(CommUtil.isNotNull(tblKupDppbIntrTemp.getReprwy())){
				if(CommUtil.isNotNull(intrInfo.getReprwy())){
					if(tblKupDppbIntrTemp.getReprwy() != intrInfo.getReprwy()){
						throw DpModuleError.DpstComm.BNAS1829();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1829();
				}
			}else{
				if(CommUtil.isNotNull(intrInfo.getReprwy())){
					throw DpModuleError.DpstComm.BNAS1829();
				}
				
			}
		}
		
		log.debug("++++++++产品利息利率部件复合结束+++++++++");
		
		
		log.debug("++++++++产品到期控制部件复合开始+++++++++");
		
		IoDpMatuIn matuInfo = input.getMatuInfo();
//		// 因平台才初始化，检查传入数据是否为空
//		String matufg = "1";
//		if (CommUtil.isNull(matuInfo.getMatupd()) && CommUtil.isNull(matuInfo.getTrinwy()) 
//				&& CommUtil.isNull(matuInfo.getTrprod()) && CommUtil.isNull(matuInfo.getTrprtm())){
//			
//			matufg = null;
//		}
		   
//		kup_dppb_matu_temp tblkup_dppb_matu_temp = KupDppbMatuTempDao.selectOne_odb2(input.getProdcd(), false);
		IoDpMatuPartDetail tblkup_dppb_matu_temp = DpProductDao.selMatuInfoByProdcd(prodcd, false);
//		kup_dppb_matu_temp tblkup_dppb_matu_temp1 = tblkup_dppb_matu_temp;
		if (CommUtil.isNull(tblkup_dppb_matu_temp)) {
			tblkup_dppb_matu_temp = SysUtil.getInstance(IoDpMatuPartDetail.class);
		}
		 
		//判断产品到期控制部件信息是否存在
		if(CommUtil.isNotNull(tblkup_dppb_matu_temp)){
			if(CommUtil.isNull(matuInfo)){
				throw DpModuleError.DpstComm.BNAS1830();
			}
		}else{
			if(CommUtil.isNotNull(matuInfo)){
				throw DpModuleError.DpstComm.BNAS1830();
			}
		}
		
		if(CommUtil.isNotNull(tblkup_dppb_matu_temp) && CommUtil.isNotNull(matuInfo)){
			
			//判断录入到期宽限期和查询的到期宽限期是否一致
			if(CommUtil.isNotNull(tblkup_dppb_matu_temp.getMatupd())){
				if(CommUtil.isNotNull(matuInfo.getMatupd())){
					if(!CommUtil.equals(tblkup_dppb_matu_temp.getMatupd(), matuInfo.getMatupd())){
						throw DpModuleError.DpstComm.BNAS1831();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1831();
				}
			}else{
				if(CommUtil.isNotNull(matuInfo.getMatupd())){
					throw DpModuleError.DpstComm.BNAS1831();
				}
			}
			
			//判断录入转存产品号和查询的转存产品号是否一致
			if(CommUtil.isNotNull(tblkup_dppb_matu_temp.getTrprod())){
				if(CommUtil.isNotNull(matuInfo.getTrprod())){
					if(!CommUtil.equals(tblkup_dppb_matu_temp.getTrprod(), matuInfo.getTrprod())){
						throw DpModuleError.DpstComm.BNAS1832();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1832();
				}
			}else{
				if(CommUtil.isNotNull(matuInfo.getTrprod())){
					throw DpModuleError.DpstComm.BNAS1832();
				}
			}
			
			//判断录入转存次数和查询的转存次数是否一致
			if(CommUtil.isNotNull(tblkup_dppb_matu_temp.getTrprtm())){
				if(CommUtil.isNotNull(matuInfo.getTrprtm())){
					if(CommUtil.compare(tblkup_dppb_matu_temp.getTrprtm(), matuInfo.getTrprtm()) != 0){
						throw DpModuleError.DpstComm.BNAS1833();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1833();
				}
			}else{
				if(CommUtil.isNotNull(matuInfo.getTrprtm())){
					throw DpModuleError.DpstComm.BNAS1833();
				}
			}
			
			//判断录入转存利率调整方式和查询的转存利率调整方式是否一致
			if(CommUtil.isNotNull(tblkup_dppb_matu_temp.getTrinwy())){
				if(CommUtil.isNotNull(matuInfo.getTrinwy())){
					if(tblkup_dppb_matu_temp.getTrinwy() != matuInfo.getTrinwy()){
						throw DpModuleError.DpstComm.BNAS1834();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1834();
				}
			}else{
				if(CommUtil.isNotNull(matuInfo.getTrinwy())){
					throw DpModuleError.DpstComm.BNAS1834();
				}
			}
		}
		
		log.debug("++++++++产品到期控制部件复合结束+++++++++");
		
		
		log.debug("++++++++产品支取计划控制部件复合开始+++++++++");
		
		IoDpDrawPlanIn drplInfo = input.getDrplInfo();
		
		KupDppbDrplTemp tblKupDppbDrplTemp = KupDppbDrplTempDao.selectOne_odb2(input.getProdcd(), false);
		if (CommUtil.isNull(tblKupDppbDrplTemp)) {
			tblKupDppbDrplTemp = SysUtil.getInstance(KupDppbDrplTemp.class);
		}
		
		//判断产品支取计划控制部件信息是否存在
		if(CommUtil.isNotNull(tblKupDppbDrplTemp)){
			if(CommUtil.isNull(drplInfo)){
				throw DpModuleError.DpstComm.BNAS1835();
			}
		}else{
			if(CommUtil.isNotNull(drplInfo)){
				throw DpModuleError.DpstComm.BNAS1835();
			}
		}
		
		if(CommUtil.isNotNull(drplInfo) && CommUtil.isNotNull(tblKupDppbDrplTemp)){
			
			//判断录入支取计划生成周期和查询的支取计划生成周期是否一致
			if(CommUtil.isNotNull(tblKupDppbDrplTemp.getGendpd())){
				if(CommUtil.isNotNull(drplInfo.getGendpd())){
					if(!CommUtil.equals(tblKupDppbDrplTemp.getGendpd(),drplInfo.getGendpd())){
						throw DpModuleError.DpstComm.BNAS1836();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1836();
				}
			}else{
				if(CommUtil.isNotNull(drplInfo.getGendpd())){
					throw DpModuleError.DpstComm.BNAS1836();
				}
			}
			
			//判断录入支取违约处理方式和查询的支取违约处理方式是否一致
			if(CommUtil.isNotNull(tblKupDppbDrplTemp.getDrdfwy())){
				if(CommUtil.isNotNull(drplInfo.getDrdfwy())){
					if(tblKupDppbDrplTemp.getDrdfwy() != drplInfo.getDrdfwy()){
						throw DpModuleError.DpstComm.BNAS1837();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1837();
				}
			}else{
				if(CommUtil.isNotNull(drplInfo.getDrdfwy())){
					throw DpModuleError.DpstComm.BNAS1837();
				}
			}
			
			//判断录入支取时结息处理标志和查询的支取时结息处理标志是否一致
			if(CommUtil.isNotNull(tblKupDppbDrplTemp.getBeinfg())){
				if(CommUtil.isNotNull(drplInfo.getBeinfg())){
					if(tblKupDppbDrplTemp.getBeinfg() != drplInfo.getBeinfg()){
						throw DpModuleError.DpstComm.BNAS1838();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1838();
				}
			}else{
				if(CommUtil.isNotNull(drplInfo.getBeinfg())){
					throw DpModuleError.DpstComm.BNAS1838();
				}
			}
		}
		
		
		log.debug("++++++++产品支取计划控制部件复合结束+++++++++");
		
		
		log.debug("++++++++产品支取控制部件复合开始+++++++++");
		
		IoDpDrawIn drawInfo = input.getDrawInfo();
		
		IoDpDrawPartDetail tblkup_dppb_draw_temp = DpProductDao.selDrawInfoByProdcd(prodcd, false);
//		kup_dppb_draw_temp tblkup_dppb_draw_temp = KupDppbDrawTempDao.selectOne_odb2(input.getProdcd(), false);
		
		//判断产品支取控制部件信息是否存在
		if(CommUtil.isNotNull(tblkup_dppb_draw_temp)){
			if(CommUtil.isNull(drawInfo)){
				throw DpModuleError.DpstComm.BNAS1839();
			}
		}else{
			if(CommUtil.isNotNull(drawInfo)){
				throw DpModuleError.DpstComm.BNAS1839();
			}
		}
		
		if(CommUtil.isNotNull(tblkup_dppb_draw_temp) && CommUtil.isNotNull(drawInfo)){
			
			//判断录入单次支取最小金额和查询的单次支取最小金额是否一致
			if(CommUtil.isNotNull(tblkup_dppb_draw_temp.getDrmiam())){
				if(CommUtil.isNotNull(drawInfo.getDrmiam())){
					if(CommUtil.compare(ConvertUtil.toBigDecimal(tblkup_dppb_draw_temp.getDrmiam()), ConvertUtil.toBigDecimal(drawInfo.getDrmiam())) !=0){
						throw DpModuleError.DpstComm.BNAS1840();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1840();
				}
			}else{
				if(CommUtil.isNotNull(drawInfo.getDrmiam())){
					throw DpModuleError.DpstComm.BNAS1840();
				}
			}
			
			//判断录入单次支取最大金额和查询的单次支取最大金额是否一致
			if(CommUtil.isNotNull(tblkup_dppb_draw_temp.getDrmxam())){
				if(CommUtil.isNotNull(drawInfo.getDrmxam())){
					if(CommUtil.compare(ConvertUtil.toBigDecimal(tblkup_dppb_draw_temp.getDrmxam()), ConvertUtil.toBigDecimal(drawInfo.getDrmxam())) !=0){
						throw DpModuleError.DpstComm.BNAS1841();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1841();
				}
			}else{
				if(CommUtil.isNotNull(drawInfo.getDrmxam())){
					throw DpModuleError.DpstComm.BNAS1841();
				}
			}
			
			//判断录入最小支取次数和查询的最小支取次数是否一致
			if(CommUtil.isNotNull(tblkup_dppb_draw_temp.getDrmitm())){
				if(CommUtil.isNotNull(drawInfo.getDrmitm())){
					if(CommUtil.compare(tblkup_dppb_draw_temp.getDrmitm(), drawInfo.getDrmitm()) != 0){
						throw DpModuleError.DpstComm.BNAS1842();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1842();
				}
			}else{
				if(CommUtil.isNotNull(drawInfo.getDrmitm())){
					throw DpModuleError.DpstComm.BNAS1842();
				}
			}
			
			//判断录入最大支取次数和查询的最大支取次数是否一致
			if(CommUtil.isNotNull(tblkup_dppb_draw_temp.getDrmxtm())){
				if(CommUtil.isNotNull(drawInfo.getDrmxtm())){
					if(CommUtil.compare(tblkup_dppb_draw_temp.getDrmxtm(), drawInfo.getDrmxtm()) !=0){
						throw DpModuleError.DpstComm.BNAS1843();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1843();
				}
			}else{
				if(CommUtil.isNotNull(drawInfo.getDrmxtm())){
					throw DpModuleError.DpstComm.BNAS1843();
				}
			}
			
			//判断录入账户留存最小余额和查询的账户留存最小余额是否一致
			if(CommUtil.isNotNull(tblkup_dppb_draw_temp.getMinibl())){
				if(CommUtil.isNotNull(drawInfo.getMinibl())){
					if(CommUtil.compare(ConvertUtil.toBigDecimal(tblkup_dppb_draw_temp.getMinibl()), ConvertUtil.toBigDecimal(drawInfo.getMinibl())) !=0){
						throw DpModuleError.DpstComm.BNAS1844();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1844();
				}
			}else{
				if(CommUtil.isNotNull(drawInfo.getMinibl())){
					throw DpModuleError.DpstComm.BNAS1844();
				}
			}
			
			//判断录入是否允许小于账户留存最小余额和查询的是否允许小于账户留存最小余额是否一致
			if(CommUtil.isNotNull(tblkup_dppb_draw_temp.getMiblfg())){
				if(CommUtil.isNotNull(drawInfo.getMiblfg())){
					if(tblkup_dppb_draw_temp.getMiblfg() != drawInfo.getMiblfg()){
						throw DpModuleError.DpstComm.BNAS1845();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1845();
				}
			}else{
				if(CommUtil.isNotNull(drawInfo.getMiblfg())){
					throw DpModuleError.DpstComm.BNAS1845();
				}
			}
			
			//判断录入支取规则是否一致
			if(CommUtil.isNotNull(tblkup_dppb_draw_temp.getDrrule())){
				if(CommUtil.isNotNull(drawInfo.getMiblfg())){
					if(tblkup_dppb_draw_temp.getDrrule() != drawInfo.getDrrule()){
						throw DpModuleError.DpstComm.BNAS1846();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1846();
				}
			}else{
				if(CommUtil.isNotNull(drawInfo.getDrrule())){
					throw DpModuleError.DpstComm.BNAS1846();
				}
			}
		}
		
		
		
		log.debug("++++++++产品支取控制部件复合结束+++++++++");
		
		
		log.debug("++++++++存入计划控制部件复合开始+++++++++");
		
		IoDpPlanIn poplInfo = input.getPoplInfo();
		
//		// 因平台才初始化，检查传入数据是否为空
//		String poplfg = "1";
//		if (CommUtil.isNull(poplInfo.getGentpd()) && CommUtil.isNull(poplInfo.getMaxisp()) 
//				&& CommUtil.isNull(poplInfo.getSvlepd()) && CommUtil.isNull(poplInfo.getSvletm())){
//			
//			poplfg = null;
//		}
		
		//存入计划控制部件信息查询
		IoDpPostplPartDetail tblkup_dppb_popl_temp = DpProductDao.selPoplInfoByProdcd(prodcd, false);
//		kup_dppb_popl_temp tblkup_dppb_popl_temp = KupDppbPoplTempDao.selectOne_odb2(input.getProdcd(), false);
		if (CommUtil.isNull(tblkup_dppb_popl_temp)) {
			tblkup_dppb_popl_temp = SysUtil.getInstance(IoDpPostplPartDetail.class);
		}
		
		//判断存入计划控制部件信息是否存在
		if(CommUtil.isNotNull(tblkup_dppb_popl_temp)){
			if(CommUtil.isNull(poplInfo)){
				throw DpModuleError.DpstComm.BNAS1847();
			}
		}else{
			if(CommUtil.isNotNull(poplInfo)){
				
				throw DpModuleError.DpstComm.BNAS1847();
			}
		}
		
		if(CommUtil.isNotNull(tblkup_dppb_popl_temp) && CommUtil.isNotNull(poplInfo)){
			
			//判断录入存入计划生成周期和查询的存入计划生成周期是否一致
			if(CommUtil.isNotNull(tblkup_dppb_popl_temp.getGentpd())){
				if(CommUtil.isNotNull(poplInfo.getGentpd())){
					if(!CommUtil.equals(tblkup_dppb_popl_temp.getGentpd(), poplInfo.getGentpd())){
						throw DpModuleError.DpstComm.BNAS1848();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1848();
				}
			}else{
				if(CommUtil.isNotNull(poplInfo.getGentpd())){
					throw DpModuleError.DpstComm.BNAS1848();
				}
			}
			
			//判断录入漏存补足宽限期和查询的漏存补足宽限期是否一致
			if(CommUtil.isNotNull(tblkup_dppb_popl_temp.getSvlepd())){
				if(CommUtil.isNotNull(poplInfo.getSvlepd())){
					if(!CommUtil.equals(tblkup_dppb_popl_temp.getSvlepd() , poplInfo.getSvlepd())){
						throw DpModuleError.DpstComm.BNAS1849();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1849();
				}
			}else{
				if(CommUtil.isNotNull(poplInfo.getSvlepd())){
					throw DpModuleError.DpstComm.BNAS1849();
				}
			}
			
			
			//判断录入最大补足次数和查询的最大补足次数是否一致
			if(CommUtil.isNotNull(tblkup_dppb_popl_temp.getMaxisp())){
				if(CommUtil.isNotNull(poplInfo.getMaxisp())){
					if(CommUtil.compare(tblkup_dppb_popl_temp.getMaxisp(), poplInfo.getMaxisp()) != 0){
						throw DpModuleError.DpstComm.BNAS1850();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1850();
				}
			}else{
				if(CommUtil.isNotNull(poplInfo.getMaxisp())){
					throw DpModuleError.DpstComm.BNAS1850();
				}
			}
			
			//判断录入漏存次数和查询的漏存次数是否一致
			if(CommUtil.isNotNull(tblkup_dppb_popl_temp.getSvletm())){
				if(CommUtil.isNotNull(poplInfo.getSvletm())){
					if(CommUtil.compare(tblkup_dppb_popl_temp.getSvletm() , poplInfo.getSvletm()) !=  0){
						throw DpModuleError.DpstComm.BNAS1851();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1851();
				}
			}else{
				if(CommUtil.isNotNull(poplInfo.getSvletm())){
					throw DpModuleError.DpstComm.BNAS1851();
				}
			}
		}
		
		log.debug("++++++++存入计划控制部件复合结束+++++++++");
		
		
		
		log.debug("++++++++产品存入控制部件复合开始+++++++++");
		
		IoDpPostIn postInfo = input.getPostInfo();
		
//		kup_dppb_post_temp tblkup_dppb_post_temp = KupDppbPostTempDao.selectOne_odb2(input.getProdcd(), false);
		IoDpPostPartDetail ioDpPostPartDetail = DpProductDao.selPostInfoByProdcd(prodcd, false);
		
		//判断产品存入控制部件信息是否存在
		if(CommUtil.isNotNull(ioDpPostPartDetail)){
			if(CommUtil.isNull(postInfo)){
				throw DpModuleError.DpstComm.BNAS1852();
			}
		}else{
			if(CommUtil.isNotNull(postInfo)){
				throw DpModuleError.DpstComm.BNAS1852();
			}
		}
		
		if(CommUtil.isNotNull(ioDpPostPartDetail)){
			//判断录入单次存入最小金额和查询的单次存入最小金额是否一致
			if(CommUtil.isNotNull(ioDpPostPartDetail.getMiniam())){
				if(CommUtil.isNotNull(postInfo.getMiniam())){
					if(CommUtil.compare(ConvertUtil.toBigDecimal(ioDpPostPartDetail.getMiniam()), ConvertUtil.toBigDecimal(postInfo.getMiniam())) != 0){
						throw DpModuleError.DpstComm.BNAS1853();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1853();
				}
			}else{
				if(CommUtil.isNotNull(postInfo.getMiniam())){
					throw DpModuleError.DpstComm.BNAS1853();
				}
			}
			
			//判断录入单次存入最大金额和查询的单次存入最大金额是否一致
			if(CommUtil.isNotNull(ioDpPostPartDetail.getMaxiam())){
				if(CommUtil.isNotNull(postInfo.getMaxiam())){
					if(CommUtil.compare(ConvertUtil.toBigDecimal(ioDpPostPartDetail.getMaxiam()), ConvertUtil.toBigDecimal(postInfo.getMaxiam())) != 0){
						throw DpModuleError.DpstComm.BNAS1854();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1854();
				}
			}else{
				if(CommUtil.isNotNull(postInfo.getMaxiam())){
					throw DpModuleError.DpstComm.BNAS1854();
				}
			}
			
			//判断录入最小存入次数和查询的最小存入次数是否一致
			if(CommUtil.isNotNull(ioDpPostPartDetail.getMinitm())){
				if(CommUtil.isNotNull(postInfo.getMinitm())){
					if(CommUtil.compare(ioDpPostPartDetail.getMinitm(), postInfo.getMinitm()) != 0){
						throw DpModuleError.DpstComm.BNAS1855();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1855();
				}
			}else{
				if(CommUtil.isNotNull(postInfo.getMinitm())){
					throw DpModuleError.DpstComm.BNAS1855();
				}
			}
			
			//判断录入最大存入次数和查询的最大存入次数是否一致
			if(CommUtil.isNotNull(ioDpPostPartDetail.getMaxitm())){
				if(CommUtil.isNotNull(postInfo.getMaxitm())){
					if(CommUtil.compare(ioDpPostPartDetail.getMaxitm(), postInfo.getMaxitm()) != 0){
						throw DpModuleError.DpstComm.BNAS1856();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1856();
				}
			}else{
				if(CommUtil.isNotNull(postInfo.getMaxitm())){
					throw DpModuleError.DpstComm.BNAS1856();
				}
			}
			
			//判断录入账户留存最大余额和查询的账户留存最大余额是否一致
			if(CommUtil.isNotNull(ioDpPostPartDetail.getMaxibl())){
				if(CommUtil.isNotNull(postInfo.getMaxibl())){
					if(CommUtil.compare(ConvertUtil.toBigDecimal(ioDpPostPartDetail.getMaxibl()), ConvertUtil.toBigDecimal(postInfo.getMaxibl())) != 0){
						throw DpModuleError.DpstComm.BNAS1857();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1857();
				}
			}else{
				if(CommUtil.isNotNull(postInfo.getMaxibl())){
					throw DpModuleError.DpstComm.BNAS1857();
				}
			}
		}
		
		log.debug("++++++++产品存入控制部件复合结束+++++++++");
		
		
		log.debug("++++++++产品开户控制部件复合开始+++++++++");
		
		//获取开户控制部件输入信息
		IoDpOpenIn openIn = input.getCustInfo();
//	    kup_dppb_cust_temp custInfo = KupDppbCustTempDao.selectOne_odb2(input.getProdcd(), false);
	    IoDpCustPartDetail custInfo = DpProductDao.selCustInfoByProdcd(prodcd, false);
	    
		//获取基础属性部件输入信息
		IoBasePrdIn baseInfo = input.getBaseInfo();
		KupDppbTemp tblKupDppbTemp = KupDppbTempDao.selectOne_odb1(input.getProdcd(), false);
		//判断产品基础部件信息是否存在
		if(CommUtil.isNull(tblKupDppbTemp)){
			throw DpModuleError.DpstComm.BNAS1858();
		}
		
		//比较录入的产品开户部件和查询的产品开户部件
        if(CommUtil.isNotNull(custInfo)){
        	if(CommUtil.isNull(openIn)){
        		throw DpModuleError.DpstComm.BNAS1859();
        	}
        }else{
        	if(CommUtil.isNotNull(openIn)){
        		throw DpModuleError.DpstComm.BNAS1859();
        	}
        }
		
    	//查询开户控制部件信息
        IoDpCustPartDetail ioDpCustPartDetail = DpProductDao.selCustInfoByProdcd(prodcd, false);
//    	kup_dppb_cust_temp tblkup_dppb_cust_temp = KupDppbCustTempDao.selectOne_odb1(input.getProdcd(),tbl_KupDppbTemp.getPdcrcy(), false);
    	
    	if(CommUtil.isNotNull(ioDpCustPartDetail)){
    		//判断录入最低起存金额和查询的最低起存金额是否一致
        	if(CommUtil.isNotNull(ioDpCustPartDetail.getSrdpam())){
        		if(CommUtil.isNotNull(openIn.getSrdpam())){
        			if(CommUtil.compare(ConvertUtil.toBigDecimal(ioDpCustPartDetail.getSrdpam()), ConvertUtil.toBigDecimal(openIn.getSrdpam())) != 0){
        				throw DpModuleError.DpstComm.BNAS1860();
        			}
        		}else{
        			throw DpModuleError.DpstComm.BNAS1860();
        		}
        	}else{
        		if(CommUtil.isNotNull(openIn.getSrdpam())){
        			throw DpModuleError.DpstComm.BNAS1860();
        		}
        	}
        			
        	//判断录入步长值和查询的步长值是否一致
        	if(CommUtil.isNotNull(ioDpCustPartDetail.getStepvl())){
        		if(CommUtil.isNotNull(openIn.getStepvl())){
        			if(CommUtil.compare(ioDpCustPartDetail.getStepvl(), openIn.getStepvl()) != 0){
        				throw DpModuleError.DpstComm.BNAS1861();
        			}
        		}else{
        			throw DpModuleError.DpstComm.BNAS1861();
        		}
        	}else{
        		if(CommUtil.isNotNull(openIn.getStepvl())){
        			throw DpModuleError.DpstComm.BNAS1861();
        		}
        	}
    	}
    	
    	List<IoDpTermInfo> termInfoList = null;
    	HashSet<String> set = new HashSet<String>();// 去重的存期
		HashSet<String> set2 = new HashSet<String>();// 去重的存期天数
		List<IoDpTermInfo> list = new ArrayList<IoDpTermInfo>();// 自定义存期
    	//获取存期集合信息
    	if(CommUtil.isNotNull(openIn)){
    		//获取存期信息
    		termInfoList = openIn.getTermInfo();
    			
    		if(CommUtil.isNotNull(termInfoList)){
    			
    			// 获取该产品号所有存期
    			List<IoDpTermInfo> tblkup_dppb_term_temps = DpProductDao.selTermInfoByProdcd(prodcd, false);
//    			List<kup_dppb_term_temp> tblkup_dppb_term_temps = KupDppbTermTempDao.selectAll_odb3(input.getProdcd(), tbl_KupDppbTemp.getPdcrcy(), false);
				if (termInfoList.size() != tblkup_dppb_term_temps.size()) {
					
					throw DpModuleError.DpstComm.BNAS1862();
				}
    			
    			for(IoDpTermInfo termInfo : termInfoList){
//    				kup_dppb_term_temp tblkup_dppb_term_temp = KupDppbTermTempDao.selectOne_odb1(input.getProdcd(), tbl_KupDppbTemp.getPdcrcy(), termInfo.getDepttm(), false);
    				IoDpTermInfo tblkup_dppb_term_temp = DpProductDao.selDepttmByProdcdDepttm(prodcd, termInfo.getDepttm(), false);
    				
    				//判断存款产品开户存期信息是否存在
    	    		if(CommUtil.isNull(tblkup_dppb_term_temp)){
    	    			throw DpModuleError.DpstComm.BNAS1863();
    	    		}
    	    			
    	    		//判断录入存期和查询的存期是否一致
    	    		if(CommUtil.isNotNull(tblkup_dppb_term_temp.getDepttm())){
    	    			if(CommUtil.isNotNull(termInfo.getDepttm())){
    	    				if(tblkup_dppb_term_temp.getDepttm() != termInfo.getDepttm()){
    	    					throw DpModuleError.DpstComm.BNAS1864();
    	    				}
    	    			}else{
    	    				throw DpModuleError.DpstComm.BNAS1864();
    	    			}
    	    		}else{
    	    			if(CommUtil.isNotNull(termInfo.getDepttm())){
    	    				throw DpModuleError.DpstComm.BNAS1864();
    	    			}
    	    		}
    	    				
    	    		//判断录入存期天数和查询的存期天数是否一致
    	    		if(CommUtil.isNotNull(tblkup_dppb_term_temp.getDeptdy())){
    	    			if(CommUtil.isNotNull(termInfo.getDeptdy())){
    	    				if(CommUtil.compare(tblkup_dppb_term_temp.getDeptdy(), termInfo.getDeptdy()) != 0){
    	    					throw DpModuleError.DpstComm.BNAS1865();
    	    				}
    	    			}else{
    	    				throw DpModuleError.DpstComm.BNAS1865();
    	    			}
    	    		}else{
    	    			if(CommUtil.isNotNull(termInfo.getDeptdy())){
    	    				throw DpModuleError.DpstComm.BNAS1865();
    	    			}
    	    		}
    	    		
    	    		set.add(termInfo.getDepttm().getValue());// 去重后的存期
    				
    				if (CommUtil.equals(termInfo.getDepttm().getValue().substring(0, 1), "9")) {
    					list.add(termInfo);
    					set2.add(termInfo.getDeptdy());// 去重后的存期天数
    				}
    	    	}
    		}
    		
    		if (set.size() != openIn.getTermInfo().size()) {
    			throw DpModuleError.DpstComm.BNAS1866();
    		}
    		
    		if (set2.size() != list.size()) {
    			throw DpModuleError.DpstComm.BNAS1867();
    		}
    	}
    	
    	
    			
    	if(CommUtil.isNotNull(ioDpCustPartDetail)){
    		//判断录入早起息天数和查询的早起息天数是否一致
        	if(CommUtil.isNotNull(ioDpCustPartDetail.getMgindy())){
        		if(CommUtil.isNotNull(openIn.getMginnm())){
        			if(CommUtil.compare(ioDpCustPartDetail.getMgindy() , openIn.getMginnm()) != 0){
        				throw DpModuleError.DpstComm.BNAS1868();
        			}
        		}else{
        			throw DpModuleError.DpstComm.BNAS1868();
        		}
        	}else{
        		if(CommUtil.isNotNull(openIn.getMginnm())){
        			throw DpModuleError.DpstComm.BNAS1868();
        		}
        	}
    	
    	
			log.debug("++++++++产品开户控制部件复合结束+++++++++");
			
	
			log.debug("++++++++产品基础部件复合开始+++++++++");
			
			//判断录入账户分类和查询的账户分类是否一致
			if (E_FCFLAG.CURRENT == tblKupDppbTemp.getPddpfg()) {
				KupDppbAddtTemp tblkup_dppb_addt = KupDppbAddtTempDao.selectOne_odb1(input.getProdcd(), true);
				
				if(CommUtil.isNotNull(tblkup_dppb_addt.getAccatp())){
					if(CommUtil.isNotNull(baseInfo.getAccatp())){
						if(tblkup_dppb_addt.getAccatp() != baseInfo.getAccatp()){
							throw DpModuleError.DpstComm.BNAS1869();
						}
					}else{
						throw DpModuleError.DpstComm.BNAS1869();
					}
				}else{
					if(CommUtil.isNotNull(baseInfo.getAccatp())){
						throw DpModuleError.DpstComm.BNAS1869();
					}
				}
			} else {
				if(CommUtil.isNotNull(baseInfo.getAccatp())){
					throw DpModuleError.DpstComm.BNAS1869();
				}
			}
			
			
			//判断录入产品启用方式和查询的产品启用方式是否一致
			if(CommUtil.isNotNull(tblKupDppbTemp.getPrentp())){
				if(CommUtil.isNotNull(baseInfo.getEnabtp())){
					if(tblKupDppbTemp.getPrentp() != baseInfo.getEnabtp()){
						throw DpModuleError.DpstComm.BNAS1870();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1870();
				}
			}else{
				if(CommUtil.isNotNull(baseInfo.getEnabtp())){
					throw DpModuleError.DpstComm.BNAS1870();
				}
			}
			
			//判断录入生效日期和查询的生效日期是否一致
			if(CommUtil.isNotNull(tblKupDppbTemp.getEfctdt())){
				if(CommUtil.isNotNull( baseInfo.getDfctdt())){
					if(!CommUtil.equals(tblKupDppbTemp.getEfctdt(), baseInfo.getDfctdt())){
						throw DpModuleError.DpstComm.BNAS1871();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1871();
				}
			}else{
				if(CommUtil.isNotNull( baseInfo.getDfctdt())){
					throw DpModuleError.DpstComm.BNAS1871();
				}
			}
			
			//判断录入失效日期和查询的生效日期是否一致
			if(CommUtil.isNotNull(tblKupDppbTemp.getInefdt())){
				if(CommUtil.isNotNull(baseInfo.getInefdt())){
					if(!CommUtil.equals(tblKupDppbTemp.getInefdt(), baseInfo.getInefdt())){
						throw DpModuleError.DpstComm.BNAS1872();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1872();
				}
			}else{
				if(CommUtil.isNotNull(baseInfo.getInefdt())){
					throw DpModuleError.DpstComm.BNAS1872();
				}
			}
			
			//判断录入协议文本代码和查询的协议文本代码是否一致
			if(CommUtil.isNotNull(tblKupDppbTemp.getProtno())){
				if(CommUtil.isNotNull(baseInfo.getProtno())){
					if(!CommUtil.equals(tblKupDppbTemp.getProtno(), baseInfo.getProtno())){
						throw DpModuleError.DpstComm.BNAS1873();
					}
				}else{
					throw DpModuleError.DpstComm.BNAS1873();
				}
			}else{
				if(CommUtil.isNotNull(baseInfo.getProtno())){
					throw DpModuleError.DpstComm.BNAS1873();
				}
			}
    	}
		
		log.debug("++++++++产品基础部件复合结束+++++++++");
		
		
		log.debug("++++++++机构控制部件复合开始+++++++++");
		
		List<IoDpBranchIn> brchinfo2 = input.getBrchinfo();
		List<IoDpBranchIn> brchinfo = new ArrayList<IoDpBranchIn>();
		
		// 检查机构号是否传入重复
		for (IoDpBranchIn cplBranchIn : brchinfo2) {
			
			//TODO 判断有问题
			if (!brchinfo.contains(cplBranchIn)) {
				brchinfo.add(cplBranchIn);
			}
			
		}
		
		// 表中机构部件信息
		List<KupDppbBrchTemp> brchinfo1 = DpProductDao.selKupDppbBrchTempByProdcd(input.getProdcd(),false);
		
		//比较录入的违约支取利息集合和查询的违约支取利息集合
        if(CommUtil.isNotNull(brchinfo1)){
        	if(CommUtil.isNotNull(input.getBrchinfo())){
        		 if(CommUtil.compare(brchinfo.size(), brchinfo1.size()) != 0){
        			 throw DpModuleError.DpstComm.BNAS1874();
        	     }
        	}else{
        		throw DpModuleError.DpstComm.BNAS1874();
        	}
        }else{
        	if(CommUtil.isNotNull(input.getBrchinfo())){
        		throw DpModuleError.DpstComm.BNAS1874();
        	}
        }
        
        if(CommUtil.isNotNull(brchinfo)){
        	for(IoDpBranchIn branchIn : brchinfo){
    			KupDppbBrchTemp tblKupDppbBrchTemp = DpProductDao.selKupDppbBrchTemp(input.getProdcd(), tbl_KupDppbTemp.getPdcrcy(), branchIn.getBrchno(), false);

    			//判断录入机构号和查询的机构号是否一致
    			if(CommUtil.isNotNull(tblKupDppbBrchTemp.getBrchno())){
    				if(CommUtil.isNotNull(branchIn.getBrchno())){
    					if(!CommUtil.equals(tblKupDppbBrchTemp.getBrchno(), branchIn.getBrchno())){
    						throw DpModuleError.DpstComm.BNAS1875();
    					}
    				}else{
    					throw DpModuleError.DpstComm.BNAS1875();
    				}
    			}else{
    				if(CommUtil.isNotNull(branchIn.getBrchno())){
    					throw DpModuleError.DpstComm.BNAS1875();
    				}
    			}
    		}
        }
		
		log.debug("++++++++机构控制部件复合结束+++++++++");
		
		//更新产品基础属性部件临时表状态（若启用方式为装配工厂，状态为装备生效；若为销售工厂，状态为组装录入）
		
		if(CommUtil.equals(baseInfo.getEnabtp().getValue(), E_PRENTP.ASSE.getValue())){
			
			tblKupDppbTemp.setProdst(BaseEnumType.E_PRODST.EFFE);
			DpProductDao.updKupDppbTempProdst(input.getProdcd(), input.getTyinno(), E_PRODST.EFFE, null,timetm);
			
		}else{
			
			tblKupDppbTemp.setProdst(BaseEnumType.E_PRODST.ASEN);
			DpProductDao.updKupDppbTempProdst(input.getProdcd(), input.getTyinno(), E_PRODST.ASEN, null,timetm);
		}
		
		log.debug("+++++++++产品部件信息插入正式表开始++++++++");
		
		//插入产品存期正式表
		DpProductDao.insKupDppbTerm("kup_dppb_term", "kup_dppb_term_temp", prodcd, prodcd,timetm);
		
		//插入产品核算部件正式表
		DpProductDao.insKupDppbAcct("kup_dppb_acct", "kup_dppb_acct_temp", prodcd, prodcd, timetm);
		
		//插入产品违约支取利息利率部件正式表
		DpProductDao.insKupDppbDfir("kup_dppb_dfir", "kup_dppb_dfir_temp", prodcd, prodcd, timetm);
		
		//插入产品利息利率部件正式表
		DpProductDao.insKupDppbIntr("kup_dppb_intr", "kup_dppb_intr_temp", prodcd, prodcd, timetm);
		
		//插入产品到期控制部件正式表
		DpProductDao.insKupDppbMatu("kup_dppb_matu", "kup_dppb_matu_temp", prodcd, prodcd, timetm);
		
		//插入产品支取控制计划部件正式表
		DpProductDao.insKupDppbDrawPlan("kup_dppb_draw_plan", "kup_dppb_drpl_temp", prodcd, prodcd, timetm);
		
		//插入产品支取控制部件正式表
		DpProductDao.insKupDppbDraw("kup_dppb_draw", "kup_dppb_draw_temp", prodcd, prodcd, timetm);
		
		//插入产品存入计划控制部件正式表
		DpProductDao.insKupDppbPostPlan("kup_dppb_post_plan", "kup_dppb_popl_temp", prodcd, prodcd, timetm);
		
		//插入产品存入控制部件正式表
		DpProductDao.insKupDppbPost("kup_dppb_post", "kup_dppb_post_temp", prodcd, prodcd, timetm);
		
		//插入产品开户控制部件正式表
		DpProductDao.insKupDppbCust("kup_dppb_cust", "kup_dppb_cust_temp", prodcd, prodcd, timetm);
		
		
		//插入机构部件正式表
		DpProductDao.insKupDppbBrch("kup_dppb_brch", "kup_dppb_brch_temp", prodcd, prodcd, timetm);
		
		//插入产品基础属性部件正式表
		DpProductDao.insKupDppb("kup_dppb", "kup_dppb_temp", prodcd, prodcd, timetm);
		
		// 插入账户类型控制正式表
		DpProductDao.insKupDppbActp("kup_dppb_actp", "kup_dppb_actp_temp", prodcd, prodcd, timetm);
		
		// 产品附加属性表
		DpProductDao.insKupDppbAddt("kup_dppb_addt", "kup_dppb_addt_temp", prodcd, prodcd, timetm);

		// 获取产品部件临时表信息
		DpProductDao.insKupDppbPart("kup_dppb_part", "kup_dppb_part_temp", prodcd, prodcd, timetm);
		
		copyProdToSecondCorpno(prodcd);
		
		log.debug("+++++++++产品部件信息插入正式表结束++++++++");
		
		log.debug("+++++++++产品部件信息临时表开始++++++++");
		
		//删除产品存期临时表信息
		if(CommUtil.isNotNull(termInfoList)){
			KupDppbTermTempDao.delete_odb2(input.getProdcd());
		}
		//删除产品核算部件临时表信息
		KupDppbAcctTempDao.delete_odb2(input.getProdcd());
		
		//删除产品利息利率部件临时表信息
		KupDppbIntrTempDao.deleteOne_odb5(input.getProdcd());
		
		//删除产品违约支取利息利率临时表信息
		if(CommUtil.isNotNull(dfirInfo)){
			KupDppbDfirTempDao.delete_odb4(input.getProdcd());
		}
		
		//删除产品到期控制部件临时表信息
		KupDppbMatuTempDao.deleteOne_odb2(input.getProdcd());
		
		//删除产品支取控制计划部件临时表信息
		KupDppbDrplTempDao.deleteOne_odb2(input.getProdcd());
		
		//删除产品支取控制部件临时表信息
		KupDppbDrawTempDao.deleteOne_odb2(input.getProdcd());
		
		//删除产品存入计划控制部件临时表信息
		KupDppbPoplTempDao.deleteOne_odb2(input.getProdcd());
		
		//删除产品存入控制部件临时表信息
		KupDppbPostTempDao.deleteOne_odb2(input.getProdcd());
		
		//删除产品开户控制部件临时表信息
		KupDppbCustTempDao.deleteOne_odb1(input.getProdcd(),tbl_KupDppbTemp.getPdcrcy());
		
		//删除机构部件临时表信息
		if(CommUtil.isNotNull(brchinfo)){
			KupDppbBrchTempDao.delete_odb4(input.getProdcd());
		}
			
		//删除产品基础属性部件临时表信息
		KupDppbTempDao.deleteOne_odb1(input.getProdcd());
		
		// 删除产品账户类型控制临时表信息
		KupDppbActpTempDao.delete_odb2(input.getProdcd());
		
		// 删除产品附加属性临时表信息
		KupDppbAddtTempDao.deleteOne_odb1(input.getProdcd());
		
		// 删除产品部件临时表
		KupDppbPartTempDao.delete_odb2(E_BUSIBI.DEPO, input.getProdcd());
		
		// 新增产品同步销售工厂登记表
		KupDppbSync tbldppb_sync = SysUtil.getInstance(KupDppbSync.class);
		
		//组装录入 ，登记产品同步销售工厂登记表
        if(CommUtil.equals(baseInfo.getEnabtp().getValue(), E_PRENTP.SALE.getValue())){
			
            tbldppb_sync.setProdcd(input.getProdcd());// 产品号
    		tbldppb_sync.setCaredt(CommTools.getBaseRunEnvs().getTrxn_date());// 创建日期
    		tbldppb_sync.setSyncst(E_SYNCST.WAIT);// 同步状态
    		KupDppbSyncDao.insert(tbldppb_sync);
			
		}

		// 产品操作柜员登记
		SysUtil.getInstance(DpProdSvcType.class).inskupDppbUser(E_BUSIBI.DEPO, input.getProdcd(), E_PRTRTP.CHCK);
	}

	private static void copyProdToSecondCorpno(String prodcd) {
		// TODO Auto-generated method stub
		String centercorpno = BusiTools.getCenterCorpno();
		String crcycd = BusiTools.getDefineCurrency();
		List<AppCorp> appCorplList = DpCopyProdDao.selSecondCorpno(false);
		for (AppCorp appCorp : appCorplList) {
			String corpno = appCorp.getCorpno();
			KupDppb kupDppb = DpCopyProdDao.selKupDppb(corpno, prodcd, false);
			if (CommUtil.isNull(kupDppb)) {
				DpCopyProdDao.insKupDppbToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, false);
			}
			KupDppbCust kupDppbCust = DpCopyProdDao.selKupDppbCust(corpno, prodcd, crcycd, false);
			if (CommUtil.isNull(kupDppbCust)) {
				DpCopyProdDao.insKupDppbCustToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, crcycd, false);
			}
			List<KupDppbBrch> kupDppbBrchlList = DpCopyProdDao.selKupDppbBrch(corpno, prodcd, crcycd, false);
			if (CommUtil.isNull(kupDppbBrchlList)) {
				DpCopyProdDao.insKupDppbBrchToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, crcycd, false);
			}
			List<KupDppbTerm> kupDppbTermlisList = DpCopyProdDao.selKupDppbTerm(corpno, prodcd, crcycd, false);
			if (CommUtil.isNull(kupDppbTermlisList)) {
				DpCopyProdDao.insKupDppbTermToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, crcycd, false);
			}
			KupDppbPost kupDppbPost = DpCopyProdDao.selKupDppbPost(corpno, prodcd, crcycd, false);
			if (CommUtil.isNull(kupDppbPost)) {
				DpCopyProdDao.insKupDppbPostToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, crcycd, false);
			}
			KupDppbDraw kupDppbDraw = DpCopyProdDao.selKupDppbDraw(corpno, prodcd, crcycd, false);
			if (CommUtil.isNull(kupDppbDraw)) {
				DpCopyProdDao.insKupDppbDrawToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, crcycd, false);
			}
			KupDppbMatu kupDppbMatu = DpCopyProdDao.selKupDppbMatu(corpno, prodcd, crcycd, false);
			if (CommUtil.isNull(kupDppbMatu)) {
				DpCopyProdDao.insKupDppbMatuToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, crcycd, false);
			}
			KupDppbDfir kupDppbDfir = DpCopyProdDao.selKupDppbDfir(corpno, prodcd, crcycd, false);
			if (CommUtil.isNull(kupDppbDfir)) {
				DpCopyProdDao.insKupDppbDfirToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, crcycd, false);
			}
			KupDppbPostPlan kupDppbPostPlan = DpCopyProdDao.selKupDppbPostPlan(corpno, prodcd, crcycd, false);
			if (CommUtil.isNull(kupDppbPostPlan)) {
				DpCopyProdDao.insKupDppbPostPlanToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, crcycd, false);
			}
			KupDppbDrawPlan kupDppbDrawPlan = DpCopyProdDao.selKupDppbDrawPlan(corpno, prodcd, crcycd, false);
			if (CommUtil.isNull(kupDppbDrawPlan)) {
				DpCopyProdDao.insKupDppbDrawPlanToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, crcycd, false);
			}
			List<KupDppbIntr> kupDppbIntrList = DpCopyProdDao.selKupDppbIntr(corpno, prodcd, crcycd, false);
			if (CommUtil.isNull(kupDppbIntrList)) {
				DpCopyProdDao.insKupDppbIntrToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, crcycd, false);
			}
			List<KupDppbAcct> kupDppbAcctList = DpCopyProdDao.selKupDppbAcct(corpno, prodcd, false);
			if (CommUtil.isNull(kupDppbAcctList)) {
				DpCopyProdDao.insKupDppbAcctToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, false);
			}
			List<KupDppbActp> kupDppbActpList = DpCopyProdDao.selKupDppbActp(corpno, prodcd, false);
			if (CommUtil.isNull(kupDppbActpList)) {
				DpCopyProdDao.insKupDppbActpToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, false);
			}
			KupDppbAddt kupDppbAddt = DpCopyProdDao.selKupDppbAddt(corpno, prodcd, false);
			if (CommUtil.isNull(kupDppbAddt)) {
				DpCopyProdDao.insKupDppbAddtToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, false);
			}
			List<KupDppbPart> kupDppbPartList = DpCopyProdDao.selKupDppbPart(corpno, prodcd, false);
			if (CommUtil.isNull(kupDppbPartList)) {
				DpCopyProdDao.insKupDppbPartToSecondCorpno(appCorp.getCorpno(), centercorpno, prodcd, false);
			}

		}
	}
}
