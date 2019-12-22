package cn.sunline.ltts.busi.dp.serviceimpl;


import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.icore.parent.errors.ItError;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.dp.IoProRateSel.ProRateSelOut;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.pb.namedsql.intr.ProintrSelDao;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRCHLV;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

/**
 * 产品利率代码查询 产品利率代码查询
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoProRateSelSvrImpl", longname = "产品利率代码查询", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoProRateSelSvrImpl implements
		cn.sunline.ltts.busi.iobus.servicetype.dp.IoProRateSelSvr {
	/**
	 * 产品利率代码查询
	 * 
	 */
	public void proratecosel(
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoProRateSelSvr.ProRateSelSvr.Input input,
			final cn.sunline.ltts.busi.iobus.servicetype.dp.IoProRateSelSvr.ProRateSelSvr.Output output) {
		
		String tranbr = CommTools.getBaseRunEnvs().getTrxn_branch(); //交易机构
		String brchno = input.getBrchno();//输入机构号
		String corpno = CommTools.getBaseRunEnvs().getBusi_org_id();//交易法人
		String intrcd = input.getIntrcd();//利率编号
		String prodcd = input.getProdcd();//产品编号
		long start = input.getStart();//页码
		long count = input.getCount();//页容量
		long totlCount = 0;
		/*if(CommUtil.isNull(brchno)){
			throw ItError.intr.BNASL069();
		}*/
		if(CommUtil.isNull(brchno)&&CommUtil.isNull(intrcd)&&CommUtil.isNull(prodcd)){
			throw ItError.intr.BNASL067();
		}
		
		//当前机构级别
		E_BRCHLV brchlv = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(tranbr).getBrchlv();
		
		
		Page<ProRateSelOut> pageInfo = null;
		//操作的机构为县级
		if(brchlv == E_BRCHLV.COUNT){
		/*	//查询操作机构的上级机构
			String upbrchone = SysUtil.getInstance(IoSrvPbBranch.class).getUpprBranch(tranbr, E_BRMPTP.M, BusiTools.getDefineCurrency()).getBrchno();
		     if(CommUtil.equals(brchno, upbrchone)){
		    	 throw ItError.intr.BNASL147();
		     }*/
			//机构号不为空
			if(CommUtil.isNotNull(brchno)){	
				//查询输入机构的上级机构的级别
				IoBrchInfo brchInfo = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(brchno);
				if(CommUtil.isNull(brchInfo)){
					throw ItError.intr.BNASL008();
				}
				
				//交易机构为县级机构只能查本机构和下级机构
				if(!(CommUtil.equals(brchno, tranbr))){
					throw ItError.intr.BNASL147();
				}
			}
		}	
	 if(CommUtil.isNotNull(brchno)){
		// 1.只输入机构号
		if(CommUtil.isNull(intrcd) && CommUtil.isNull(prodcd)){
			IoBrchInfo brchInfo = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(brchno);
			if(CommUtil.isNull(brchInfo)){
				throw ItError.intr.BNASL008();
			}
			if(CommUtil.equals(corpno, CommTools.getBaseRunEnvs().getCenter_org_id())){
				pageInfo = ProintrSelDao.selProdintrByBrchProv(brchno, brchno.substring(0,3), (start-1)*count, count, totlCount, false);
			}else{
				pageInfo = ProintrSelDao.selProdintrByBrchProv(brchno, corpno, (start-1)*count, count, totlCount, false);
			}
			
		}
		
		//2.输人机构号和产品号
		if(CommUtil.isNotNull(prodcd) && CommUtil.isNull(intrcd)){
			if(CommUtil.equals(corpno, CommTools.getBaseRunEnvs().getCenter_org_id())){
				pageInfo = ProintrSelDao.selProdintrByBrAnProProv(brchno, brchno.substring(0,3),prodcd,(start-1)*count, count, totlCount, false);
			}else{
				pageInfo = ProintrSelDao.selProdintrByBrAnProProv(brchno,corpno, prodcd,(start-1)*count, count, totlCount, false);
			}
			
		}
		//3.输入机构号和产品代码
		if(CommUtil.isNull(prodcd) && CommUtil.isNotNull(intrcd)){
			if(CommUtil.equals(corpno, CommTools.getBaseRunEnvs().getCenter_org_id())){
				pageInfo = ProintrSelDao.selProdintrByBrchAndIntr(brchno, intrcd, brchno.substring(0,3), (start-1)*count, count, totlCount, false);
			}else{
				pageInfo = ProintrSelDao.selProdintrByBrchAndIntr(brchno, intrcd, corpno,(start-1)*count, count, totlCount, false);
			}
			
		}
		//4.输入机构号和产品号，利率代码
		if(CommUtil.isNotNull(prodcd) && CommUtil.isNotNull(intrcd)){
			if(CommUtil.equals(corpno, CommTools.getBaseRunEnvs().getCenter_org_id())){
				pageInfo = ProintrSelDao.selProdintrByBpiProv(brchno, prodcd, intrcd, brchno.substring(0,3), (start-1)*count, count, totlCount, false);
			}else{
				pageInfo = ProintrSelDao.selProdintrByBpiProv(brchno, prodcd, intrcd, corpno, (start-1)*count, count, totlCount, false);
			}
			
		}
	 }		
			
		//产品号不为空
		if(CommUtil.isNotNull(prodcd)){
			//2输入产品号
			if(CommUtil.isNull(brchno) && CommUtil.isNull(intrcd)){
				pageInfo = ProintrSelDao.selProdIntrByProno(prodcd, corpno, tranbr, (start-1)*count, count, totlCount, false);
			}
			
			//3输入产品号和利率代码
			if(CommUtil.isNull(brchno) && CommUtil.isNotNull(intrcd)){
				pageInfo = ProintrSelDao.selProdIntrByPronoAndIntr(prodcd, intrcd, corpno, tranbr, (start-1)*count, count, totlCount, false);
			}
		}
			
			
		//利率代码不为空
		if(CommUtil.isNotNull(intrcd)){
			//3输入利率代码
			if(CommUtil.isNull(prodcd) && CommUtil.isNull(brchno)){
				pageInfo = ProintrSelDao.selProdIntrByIntr(intrcd, corpno, tranbr, start, count, totlCount, false);
			}
		}
			

		
		Options<ProRateSelOut> optionInfo = new DefaultOptions<>();
		optionInfo.addAll(pageInfo.getRecords());
		output.setResult(optionInfo);
		CommTools.getBaseRunEnvs().setTotal_count(pageInfo.getRecordCount());
	}
}
