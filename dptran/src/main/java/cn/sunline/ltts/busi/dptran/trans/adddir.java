package cn.sunline.ltts.busi.dptran.trans;

//import org.apache.tools.ant.taskdefs.Truncate;

//import com.alibaba.druid.sql.visitor.functions.Substring;


/**
 * 
 * @ClassName: adddir 
 * @Description: (目录增加) 
 * @author lei wei
 * @date 2016年7月20日 下午4:45:29 
 *
 */

public class adddir {

public static void addirel( final cn.sunline.ltts.busi.dptran.trans.intf.Adddir.Input input){
	/*
	    String catana = input.getCatana(); //产品目录名称
	    E_BUSIBI busibi = input.getBusibi();//业务大类
		E_PRODCT prodtp = input.getProdtp();//业务中类
		E_FCFLAG pddpfg = input.getPddpfg();//业务小类
		E_DEBTTP debttp	= input.getDebttp();//业务细类
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();//交易机构
		String creadt = CommTools.getBaseRunEnvs().getTrxn_date();//交易日期
		String  trantm = BusiTools.getBusiRunEnvs().getTrantm();//交易时间
		String creatm = DateTools2.get14DateTime(ConvertUtil.toLong(trantm));// 取交易时间位数
         String cataid =""; // 定义目录编号
		
		
		if(CommUtil.isNull(catana)){
			throw PbError.Intr.E9999("产品目录不能为空！");
		}
		
		if(CommUtil.isNull(busibi)){
			throw PbError.Intr.E9999("业务大类不能为空！");
		}
		
		IoPbKubBrch cplKubBrch = SysUtil.getInstance(IoPbTableSvr.class).kub_brch_selectOne_odb1(brchno, true);
		if(E_BRCHLV.PROV != cplKubBrch.getBrchlv()){
			throw DpModuleError.DpstAcct.E9999("非省级机构没有操作权限！");
		}
	
		if(CommUtil.isNotNull(pddpfg)){
			if(CommUtil.isNull(prodtp)){
				PbError.Intr.E9999("业务小类输出有误！");
			}
		}
		if(CommUtil.isNotNull(debttp)){
			if(CommUtil.isNull(pddpfg)){
				PbError.Intr.E9999("业务细类输出有误！");
			}
		}
		// 对目录编号进行拼接
		//业务中类
		if (CommUtil.isNull(prodtp)) {
			        cataid = busibi.toString();	
		}else
			//业务小类
			if(CommUtil.isNull(pddpfg))	{
                	cataid = busibi.toString()+prodtp.toString();
              }else 
            	  //业务细类
            	  if (CommUtil.isNull(debttp)){
				    cataid = busibi.toString()+pddpfg.toString();
            	  }else { 
		
            		  cataid = busibi.toString()+debttp.toString();	
            	  }
		
         // //判断表里是否已存在目录编号
		 kup_cata tabCata = Kup_cataDao.selectOne_kup_cata_idx1(cataid, false);
		 if (CommUtil.isNotNull(tabCata)) {
				PbError.Intr.E9999("目录编码已存在！");
			}
		 //判断表里是否已存在目录名称
		 kup_cata tebCata = Kup_cataDao.selectOne_kup_cata_idx2(catana, false);
		 if (CommUtil.isNotNull(tebCata)) {
				PbError.Intr.E9999("目录名称已存在！");
			}
			
	        kup_cata   entity = SysUtil.getInstance(kup_cata.class);
	           entity.setCataid(cataid);
	           entity.setBusibi(busibi);
	           entity.setCatana(catana);
	           entity.setCreadt(creadt);
	           entity.setCreatm(creatm.substring(8));
	           entity.setDebttp(debttp);
	           entity.setProdtp(prodtp);
	           entity.setPddpfg(pddpfg);
           
		       Kup_cataDao.insert(entity);*/
}
}
