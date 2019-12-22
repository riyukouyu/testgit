package cn.sunline.ltts.busi.fetran.trans.chrg;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.namedsql.FeDiscountDao;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgCpal;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrcpal {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(qrcpal.class);
	/**
	 * 
	 * @Author levi
	 *         <p>
	 *         功能说明：查询超额优惠解析
	 *         </p>
	 * @param @param input
	 * @param @param output
	 * @return void
	 * @throws
	 */
	public static void qryTranInfo( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpal.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpal.Property property,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpal.Output output){
		property.setOrgnbr(CommTools.getBaseRunEnvs().getTrxn_branch());// 交易机构号
		property.setStart(CommTools.getBaseRunEnvs().getPage_start());// 页码
		property.setCount(CommTools.getBaseRunEnvs().getPage_size());// 页容量
	}

	public static void qrcpal( final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpal.Input input,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpal.Property property,  final cn.sunline.ltts.busi.fetran.transdef.chrg.Qrcpal.Output output){
		String chrgcd = input.getChrgcd(); //费种代码
//		String fadmtp = input.getFadmtp(); //维度类型
		String crcycd = BusiTools.getDefineCurrency();
		String smfacd = input.getSmfacd(); //超额优惠代码
		long pageno = CommTools.getBaseRunEnvs().getPage_start();//页码
		long pgsize = CommTools.getBaseRunEnvs().getPage_size();//页容量
		String brchno = input.getBrchno();
		
		long totlCount = 0;
			
		if(CommUtil.isNull(brchno)){
			//汇总有权限的查询机构
			IoBrchInfo brchinfo = SysUtil.getInstance(IoBrchInfo.class);
			brchinfo.setBrchno(CommTools.getBaseRunEnvs().getTrxn_branch());
			property.getBrchinfo().add(brchinfo);
		    property.getBrchinfo().addAll(property.getUpBrchinfo());
		    property.getBrchinfo().addAll(property.getDoBrchinfo());
			
			StringBuilder sb = new StringBuilder();
			Options<IoBrchInfo> brInfo = property.getBrchinfo();
			int count=0;
			for (int i = 0; i < brInfo.size(); i++) {
				//modify by wenbo 20170706 增加非空判断
				if(CommUtil.isNotNull(brInfo.get(i).getBrchno())){
					if(CommUtil.compare(count,0)>0){
						sb.append(",");
					}
					if(i == brInfo.size() - 1){					
						sb.append(String.valueOf(brInfo.get(i).getBrchno()));				
					}else{					
						sb.append(String.valueOf(brInfo.get(i).getBrchno()));
						count++;
					}					
				}
			}
			brchno = sb.toString();
		}
		bizlog.debug("<<<<<<机构号Brchno=" + brchno + ">>>>>>");
		Page<IoCgCpal> lstkcp_favo_smex = FeDiscountDao.selall_kcp_favo_smex(chrgcd, null, crcycd, smfacd, brchno, (pageno - 1) * pgsize, pgsize, totlCount, false);
		Options<IoCgCpal> optkcp_favo_smex = new DefaultOptions<IoCgCpal>();// 初始化输出对象
		optkcp_favo_smex.addAll(lstkcp_favo_smex.getRecords());
		output.setPinfos(optkcp_favo_smex);
		
		CommTools.getBaseRunEnvs().setTotal_count(lstkcp_favo_smex.getRecordCount());// 记录总数

	}
}
