package cn.sunline.ltts.busi.dp.password;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.ca.base.CaTools;
import cn.sunline.ltts.busi.dp.tables.TabDpPassword.DpbPswd;
import cn.sunline.ltts.busi.dp.tables.TabDpPassword.DpbPswdDao;
import cn.sunline.ltts.busi.sys.dict.ApDict;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.CaError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PSDWST;

/**
 * 交易密码公共逻辑处理类
 * 
 * @author cuijia
 * 
 */
public class DpPassword {

	/**
	 * 检查交易密码
	 * 
	 * @param acctno
	 *            账号
	 * @param password
	 *            交易密码
	 * @param servtp
	 *            渠道类型
	 * 
	 * @param servno
	 *            渠道号
	 * 
	 */
	public static void validatePassword(String acctno, String password,
			String servtp, String servno) {
		CommTools.fieldNotNull(acctno, BaseDict.Comm.acctno.getId(),
				BaseDict.Comm.acctno.getLongName());
		CommTools.fieldNotNull(password, ApDict.Aplt.passwd.getId(),
				ApDict.Aplt.passwd.getLongName());
		CommTools.fieldNotNull(servtp, BaseDict.Comm.servtp.getId(),
				BaseDict.Comm.servtp.getLongName());
		CommTools.fieldNotNull(servno, BaseDict.Comm.servno.getId(),
				BaseDict.Comm.servno.getLongName());

		final DpbPswd dpbPasswordDO = DpbPswdDao.selectOne_odb1(acctno, servno,
				servtp, false);
		if (CommUtil.isNull(dpbPasswordDO)) {
			throw CaError.Passwd.E0001(acctno, servtp, servno);
		}
		// 检查当前密码状态,冻结状态只能晚间批量自动解冻或手工解冻
		if (dpbPasswordDO.getPsdwst() == E_PSDWST.LOSS) {
			throw CaError.Passwd.E0002(dpbPasswordDO.getMaxerr());
		}
		// 关闭状态，可能为特殊处理，需要人工干预
		if (dpbPasswordDO.getPsdwst() == E_PSDWST.CLOSE) {
			throw CaError.Passwd.E0003();
		}

		// 加密后的密码变量
		String cryptoPassword = null;
        //加密因子，根据卡号取证件号
        String cryptoAcctno = CaTools.getIdtfno(acctno);
		// 加密模式，调用加密机组件
		if ("1".equals(BusiTools.getPasswdModule())) {
//			BaseComp.Security security = SysUtil.getInstance(BaseComp.Security.class, SAFSecurityComponent.SAFSecurity);
//			cryptoPassword = security.translatePin(CommTools.getBaseRunEnvs().getInitiator_system(), CommTools.getBaseRunEnvs().getSystcd(), cryptoAcctno,
//					cryptoAcctno, password);
		} else {
			// 明文
			cryptoPassword = password;
		}
		// 检查交易密码
		if (CommUtil.equals(cryptoPassword, dpbPasswordDO.getPasswd())) {
			// 将错误次数归零
			if (dpbPasswordDO.getPwerct().intValue() > 0) {
				/**
				 * 启动独立事务，以免业务错误导致错误次数重置回归
				 */
				DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
					@Override
					public Void execute() {
						dpbPasswordDO.setPwerct(0);
						DpbPswdDao.updateOne_odb1(dpbPasswordDO);
						return null;
					}
				});
			}
		} else {
			/**
			 * 交易密码不等处理错误次数，并检查到达最大错误次数更新状态 使用独立事务，进行提交不受主事务失败回滚
			 * 如果最大错误次数设置为零表示不控制
			 * modify lull 2017/10/13
			 */
			
			if (dpbPasswordDO.getMaxerr().intValue() == 0) {
				throw CaError.Passwd.E0005();
			}else{
				final int currentErrorCount = dpbPasswordDO.getPwerct()
						.intValue() + 1;
				DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
					@Override
					public Void execute() {
						if (currentErrorCount >= dpbPasswordDO.getMaxerr()
								.intValue()) {
							// 超过最大错误次数，做冻结状态更新
							dpbPasswordDO.setPsdwst(E_PSDWST.LOSS);
							dpbPasswordDO.setPwerct(currentErrorCount);
						} else {
							// 变更错误次数
							dpbPasswordDO.setPwerct(currentErrorCount);
						}
						DpbPswdDao.updateOne_odb1(dpbPasswordDO); // 更新到数据库
						return null;
					}
				});
				if(currentErrorCount == dpbPasswordDO.getMaxerr()
						.intValue()){
					//超过最大次数，另外报错
					//throw CaError.Passwd.BNAS3000(currentErrorCount, dpbPasswordDO.getMaxerr());
				    throw DpModuleError.DpstComm.E9901("密码错误次数已达到"+dpbPasswordDO.getMaxerr()+"次，请重置密码");
				}else {
					throw CaError.Passwd.E0004(currentErrorCount, dpbPasswordDO.getMaxerr());
				}
			}
		}
	}

	/**
	 * 新增交易密码或重置交易密码
	 * 
	 * @param acctno
	 *            账号
	 * @param acctrt
	 *            账号类型
	 * @param password
	 *            交易密码
	 * @param servtp
	 *            交易渠道类型
	 * @param servno
	 *            交易渠道号
     * @param cryptoAcctno
     *            加密因子
	 */
	public static void savePassword(String acctno,
			BaseEnumType.E_ACCTROUTTYPE acctrt, String password,
			String servtp, String servno, String cryptoAcctno) {

		CommTools.fieldNotNull(acctno, BaseDict.Comm.acctno.getId(),
				BaseDict.Comm.acctno.getLongName());
//	    CommTools.fieldNotNull(password, ApDict.Aplt.passwd.getId(),
//						ApDict.Aplt.passwd.getLongName());
		CommTools.fieldNotNull(servtp, BaseDict.Comm.servtp.getId(),
				BaseDict.Comm.servtp.getLongName());
		CommTools.fieldNotNull(servno, BaseDict.Comm.servno.getId(),
				BaseDict.Comm.servno.getLongName());

		
//		//加密因子，根据卡号取证件号
//		if(CommUtil.isNull(cryptoAcctno)){
//		    cryptoAcctno = CaTools.getIdtfno(acctno);
//		}
//		if(CommUtil.isNull(password)){
//			//静默开户，不用交易密码，默认为证件号后六位
//				cryptoPasswords = cryptoAcctno.substring(12);
//		}else{
//			//普通开户，客户自己设置交易密码
//			if ("1".equals(BusiTools.getPasswdModule())) {
//				BaseComp.Security security = SysUtil.getInstance(
//						BaseComp.Security.class,
//						SAFSecurityComponent.SAFSecurity);
//				cryptoPassword = security.translatePin(CommTools.getBaseRunEnvs()
//						.getInpucd(), CommTools.getBaseRunEnvs().getSystcd(),
//						cryptoAcctno, cryptoAcctno, password);
//			} else {
//				// 明文
//				cryptoPassword = password;
//			}
//		    cryptoPasswords=password;
//		}
		//调加密机
		String cryptoPassword = null;
		//加密因子为空时不加密
		if(CommUtil.isNull(cryptoAcctno)){
		    cryptoPassword=password;
		}else{
//		    EncryTools encryTools=SysUtil.getInstance(EncryTools.class);
//		    cryptoPassword=EncryTools.encryPassword(password, cryptoAcctno,acctno); 
		}
		
		 

		DpbPswd dpbPasswordDO = DpbPswdDao.selectOne_odb1(acctno, servno,
				servtp, false);
		// 没有找到做新增处理
		if (CommUtil.isNull(dpbPasswordDO)) {
			CommTools.fieldNotNull(acctrt, BaseDict.Comm.acctrt.getId(),
					BaseDict.Comm.acctrt.getLongName());

			DpbPswd passwordDO = SysUtil.getInstance(DpbPswd.class);
			passwordDO.setAcctno(acctno);
			passwordDO.setAcctrt(acctrt);
			
			passwordDO.setPasswd(cryptoPassword);
			/*
			 * JF Modify：密码处理逻辑后面完善。
			passwordDO
					.setMaxerr(Integer.valueOf(BusiTools.getPasswdMaxErrCnt()));
			 */
			passwordDO.setPsdwst(E_PSDWST.NORMAL);
			passwordDO.setPwerct(0);
			passwordDO.setServno(servno);
			passwordDO.setServtp(servtp);

			DpbPswdDao.insert(passwordDO);
		} else {
			// 做密码重置处理
			dpbPasswordDO.setPasswd(cryptoPassword);
			//如果当前错误次数大于零或冻结可以重置
			dpbPasswordDO.setPwerct(0);
			dpbPasswordDO.setPsdwst(E_PSDWST.NORMAL);
			DpbPswdDao.updateOne_odb1(dpbPasswordDO);
		}

	}

}
