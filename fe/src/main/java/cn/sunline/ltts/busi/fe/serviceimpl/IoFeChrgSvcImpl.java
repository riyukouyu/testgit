package cn.sunline.ltts.busi.fe.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.edsp.busi.jfpal.ngp.aplt.parm.TrxEnvs.CjSfsv;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.fe.chrg.CalCharg;
import cn.sunline.ltts.busi.fe.chrg.ChargPublic;
import cn.sunline.ltts.busi.fe.namedsql.FeCodeDao;
import cn.sunline.ltts.busi.fe.namedsql.FeSceneDao;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpChrg;
import cn.sunline.ltts.busi.fe.tables.FeTable.KcpScevDetl;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgCalFee_IN;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgCalFee_OUT;
import cn.sunline.ltts.busi.fe.type.FeComplexType.CgFFEEHD;
import cn.sunline.ltts.busi.fe.type.FeComplexType.listnm;
import cn.sunline.ltts.busi.iobus.servicetype.IoFeChrgSvcType.IoSelKcpChrg.Output;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgCalFee_OUT;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgFEEINFO;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgFFEEHD;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgFrmFeeHD;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgScenDimInfo;
import cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoKcpChrg;
import cn.sunline.ltts.busi.sys.errors.FeError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_EVETUS;
import cn.sunline.ltts.busi.sys.type.FeEnumType.E_MODULE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

/**
 * 公共计费外部服务实现 公共计费外部服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoFeChrgSvcImpl", longname = "公共计费外部服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoFeChrgSvcImpl implements
		cn.sunline.ltts.busi.iobus.servicetype.IoFeChrgSvcType {
	private static final BizLog bizlog = BizLogUtil.getBizLog(IoFeChrgSvcImpl.class);

	/**
	 * 统一收费
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgCalFee_OUT CalCharge(
			final cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgCalFee_IN cplIn) {
		IoCgCalFee_OUT cplOut = SysUtil.getInstance(IoCgCalFee_OUT.class);
        CalCharg CalChrg = SysUtil.getInstance(CalCharg.class);
        CgCalFee_IN cplFeeIn = SysUtil.getInstance(CgCalFee_IN.class);
        cplFeeIn.setAmount(cplIn.getAmount());    
        cplFeeIn.setCstrfg(cplIn.getCstrfg());
        cplFeeIn.setChevno(cplIn.getChevno());
        cplFeeIn.setChgflg(cplIn.getChgflg());
        cplFeeIn.setChrgcd(cplIn.getChrgcd());
        cplFeeIn.setChrgcy(cplIn.getChrgcy());
        cplFeeIn.setChrgpd(cplIn.getChrgpd());
        cplFeeIn.setChrgsr(cplIn.getChrgsr());
        cplFeeIn.getCplKfzhu().setCustac(cplIn.getCplKfzhu().getCustac());
        cplFeeIn.getCplKfzhu().setCgsbsq(cplIn.getCplKfzhu().getCgsbsq());
        cplFeeIn.getCplKfzhu().setCrcycd(cplIn.getCplKfzhu().getCrcycd());
        cplFeeIn.getCplKfzhu().setCsexfg(cplIn.getCplKfzhu().getCsexfg());
        cplFeeIn.setCsexfg(cplIn.getCsexfg());
        cplFeeIn.setCsprcd(cplIn.getCsprcd());
        cplFeeIn.setCustac(cplIn.getCustac());
        cplFeeIn.setCustno(cplIn.getCustno());
        cplFeeIn.setDcmttp(cplIn.getDcmttp());
        cplFeeIn.setDvidam(cplIn.getDvidam());
        cplFeeIn.setInbrno(cplIn.getInbrno());
        cplFeeIn.setLastdt(cplIn.getLastdt());
        cplFeeIn.setModtyp(cplIn.getModtyp());
        cplFeeIn.setOubrno(cplIn.getOubrno());
        cplFeeIn.setProdcd(cplIn.getProdcd());
        cplFeeIn.setRemark(cplIn.getRemark());
        cplFeeIn.setScencd(cplIn.getScencd());
        //判断是否是场景分析过来的场景代码
        if(BusiTools.getBusiRunEnvs().getChgevo().getCjsfls().size() > 0){
        	 List<CjSfsv> lstCjsfls = BusiTools.getBusiRunEnvs().getChgevo().getCjsfls().getValues();
        	 for(CjSfsv tblCjsfsv : lstCjsfls){
        		 //取场景分析代码
        		 cplFeeIn.setScencd(tblCjsfsv.getScencd());
        	 }
        }
        cplFeeIn.setSeqnum(cplIn.getSeqnum());
        cplFeeIn.setSmrycd(cplIn.getSmrycd());
        cplFeeIn.setSmryds(cplIn.getSmryds());
        cplFeeIn.setSpcham(cplIn.getSpcham());
        cplFeeIn.setTotflg(cplIn.getTotflg());
        cplFeeIn.setTranam(cplIn.getTranam());
        cplFeeIn.setTranbr(cplIn.getTranbr());
        cplFeeIn.setDifage(cplIn.getDifage());
        cplFeeIn.setTrancy(BusiTools.getDefineCurrency()); //默认 人民币
        cplFeeIn.setPredim(cplIn.getPredim()); //优惠维度列表
        
        CgCalFee_OUT cplFee_OUT = CalChrg.calCharge(cplFeeIn); //  统一计费/收费处理 
        
        if (CommUtil.isNotNull(cplFee_OUT)) {
            cplOut.setCgpyrv(cplFee_OUT.getCgpyrv());
            cplOut.setTotamt(cplFee_OUT.getTotamt());
            cplOut.setCustno(cplFee_OUT.getCustno());
            cplOut.setCustac(cplFee_OUT.getCustac());
            cplOut.setAccgac(cplFee_OUT.getAccgac());
            Options<IoCgFEEINFO> lstCgFEEINFO = new DefaultOptions<IoCgFEEINFO>();
            if (CommUtil.isNotNull(cplFee_OUT.getListnm())) {
                for (int i = 0; i < cplFee_OUT.getListnm().size(); i++) {
                    IoCgFEEINFO cplCgFEEINFO = SysUtil.getInstance(IoCgFEEINFO.class);
                    listnm cplTmp = cplFee_OUT.getListnm().get(i);
                    cplCgFEEINFO.setChrgcd(cplTmp.getChrgcd());
                    cplCgFEEINFO.setAmount(cplTmp.getAmount());
                    cplCgFEEINFO.setTranam(cplTmp.getTranam());
                    cplCgFEEINFO.setClcham(cplTmp.getClcham());
                    cplCgFEEINFO.setFavoir(cplTmp.getDisrat());
                    cplCgFEEINFO.setDircam(cplTmp.getDircam());
                    cplCgFEEINFO.setDioage(cplTmp.getDioage()); //分润方一机构号
                    cplCgFEEINFO.setDiwage(cplTmp.getDiwage()); //分润方二机构号
                    cplCgFEEINFO.setDitage(cplTmp.getDitage()); //分润方三机构号
                    cplCgFEEINFO.setDifage(cplTmp.getDifage()); //分润方四机构号
                    cplCgFEEINFO.setDioamo(cplTmp.getDioamo()); //分润方一金额
                    cplCgFEEINFO.setDiwamo(cplTmp.getDiwamo()); //分润方二金额
                    cplCgFEEINFO.setDitamo(cplTmp.getDitamo()); //分润方三金额
                    cplCgFEEINFO.setDifamo(cplTmp.getDifamo()); //分润方四金额
                    cplCgFEEINFO.setSubnum(cplTmp.getSubnum()); //科目号
                    cplCgFEEINFO.setIntacc(cplTmp.getIntacc()); //内部账对应顺序号
                    cplCgFEEINFO.setAcclev( cplTmp.getAcclev());
                    cplCgFEEINFO.setPronum(cplTmp.getPronum()); //产品编号
                    cplCgFEEINFO.setTrinfo(cplTmp.getTrinfo()); //交易信息
                    cplCgFEEINFO.setPrmark(cplTmp.getPrmark()); //对应产品标志
                    lstCgFEEINFO.add(cplCgFEEINFO);
                }
            }
            CommTools.getBaseRunEnvs().setTotal_count(Long.valueOf(cplFee_OUT.getListnm().size())); //条数
            cplOut.setListnm(lstCgFEEINFO);
            cplOut.setArrgam(cplFee_OUT.getArrgam());  //欠费金额
            cplOut.setApcgam(cplFee_OUT.getApcgam());  //实付金额
            cplOut.setChrgcy(cplFee_OUT.getChrgcy());  //收费币种
            cplOut.setAcclam(cplFee_OUT.getAcclam());  //实收金额
            cplOut.setAmount(cplFee_OUT.getAmount());  //数量
            cplOut.setSpcgam(cplFee_OUT.getSpcgam());  //应付金额
            cplOut.setRecvam(cplFee_OUT.getRecvam());  //应收金额
            cplOut.setAcchnm(cplFee_OUT.getAcchnm());  //账户中文名
        }
        
        return cplOut;
	}

	/**
	 * 场景分析
	 * 
	 */
	public void AnalyseScene(
			final cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgScenAnalyse_IN cplIn) {
		bizlog.method("AnalyseScene begin >>>>>>>>>>>>>>>>>>>>>>>>>");
		bizlog.parm("cplIn[%s]", cplIn);

		if (CommUtil.isNull(cplIn.getEvetcd())) {
			throw FeError.Chrg.BNASF019();
		}

		cplIn.setModule(E_MODULE.CG); //默认费用模块
		
		if (CommUtil.isNull(cplIn.getModule())) {
			throw FeError.Chrg.BNASF188();
		}

		// 增加事件用途判断
		// if (E_QDAOLEIX.GM == CommTools.getBaseRunEnvs().getChannel_id() ||
		// E_QDAOLEIX.JYPL == CommTools.getBaseRunEnvs().getQudaohao())
		// {
		if (CommUtil.isNull(cplIn.getEvetus())) {
			cplIn.setEvetus(E_EVETUS.SF); // SF-收费
		}
		// }

		if (CommUtil.isNull(cplIn.getScencd())) {
			// 如果没有传入 场景代码, 则通过场景事件维度查询获取场景代码
			if (CommUtil.isNotNull(cplIn.getDimlst())) {
				bizlog.debug("输入维度信息[%s]", cplIn.getDimlst());
				List<KcpScevDetl> lstCjsjmx = FeSceneDao.selall_kcp_scev_detl_evet(cplIn.getEvetcd(), false);
//				List<kcp_scev_detl> lstCjsjmx = Kcp_scev_detlDao.selectAll_odb2(cplIn.getModule(), cplIn.getEvetcd(), false);
				bizlog.debug("数据库中模块[%s]事件编号[%s]对应场景要素[%s]", cplIn.getModule(), cplIn.getEvetcd(), lstCjsjmx);

				E_YES___ eFlag = E_YES___.YES; // 是否存在维度值不符合的记录
				String sMessage = "";
				List<String> lstBuf = new ArrayList<String>();
				if (CommUtil.isNotNull(lstCjsjmx)) {
					for (KcpScevDetl tblTmp : lstCjsjmx) {
						sMessage = tblTmp.getScencd() + tblTmp.getModule() + tblTmp.getEvetcd() + tblTmp.getEvetus();
						bizlog.debug("sMessage[%s]", sMessage);

						// 防止相同场景代码相同模块相同事件编号，类别不同的场景添加多次
						if (!(lstBuf.contains(sMessage))) {
							lstBuf.add(sMessage);
							bizlog.debug("取单条场景进行分析,场景代码[%s] >>>>>",tblTmp.getScencd());
							
							List<KcpScevDetl> lstTmp = FeSceneDao.selall_kcp_scev_detl_scev(tblTmp.getEvetcd(), tblTmp.getScencd(), false);
//							List<kcp_scev_detl> lstTmp = Kcp_scev_detlDao.selectAll_odb3(tblTmp.getScencd(), tblTmp.getModule(), tblTmp.getEvetcd(), false);
							bizlog.debug("单条场景代码[%s]模块[%s]事件编号[%s]对应单条场景列表[%s]",
									tblTmp.getScencd(), tblTmp.getModule(), tblTmp.getEvetcd(), lstTmp);

							List<String> lstWdTmp = new ArrayList<String>();
							if (CommUtil.isNotNull(lstTmp)) {
								bizlog.debug("单条维度值大小[%s]", lstTmp.size());
								for (KcpScevDetl tblBuf : lstTmp) {
									eFlag = E_YES___.YES; // 初始化
									for (IoCgScenDimInfo cplWdzhi : cplIn.getDimlst()) {
										if (CommUtil.compare( tblBuf.getDimecg(), cplWdzhi.getDimecg()) == 0) {
											bizlog.debug( "tblBuf.getDimecg()[%s]", tblBuf.getDimecg());
											bizlog.debug( "tblBuf.getDimevl()[%s]cplWdzhi.getDimevl()[%s]", tblBuf.getDimevl(), cplWdzhi.getDimevl());
											if (CommUtil.compare( tblBuf.getDimevl(), cplWdzhi.getDimevl()) == 0) {
												eFlag = E_YES___.NO;
												lstWdTmp.add(tblBuf.getDimecg());
											}
										}
									}
									bizlog.debug("tblBuf[%s]eFlag[%s]", tblBuf, eFlag);

									if (eFlag == E_YES___.YES)// 只要有一个不符合就不可以用此场景
									{
										break;
									}
								}

							}
							bizlog.debug("符合条件的维度列表[%s]", lstWdTmp);
							bizlog.debug("符合条件的维度列表大小[%s]", lstWdTmp.size());
							// 符合的维度值相同时，才可以添加此场景代码
							if (lstWdTmp.size() == lstTmp.size()) {
								CjSfsv cplCjSfsv = SysUtil .getInstance(CjSfsv.class);
								cplCjSfsv.setScencd(tblTmp.getScencd());
								BusiTools.getBusiRunEnvs().getChgevo().getCjsfls().add(cplCjSfsv);
							}else{
								bizlog.debug("没有找到符合条件的场景代码");
							}
						}
						bizlog.debug("单条场景分析结束eFlag[%s]", eFlag);

					}
				}

			} else {
				bizlog.debug("模块[%s]事件编号[%s]没有对应的场景代码", cplIn.getModule(), cplIn.getEvetcd());
				throw FeError.Chrg.BNASF215();
			}
		} else {
			bizlog.debug("传入的场景代码：>>[%s]", cplIn.getScencd());

			CjSfsv cplCjSfsv = SysUtil.getInstance(CjSfsv.class);
			cplCjSfsv.setScencd(cplIn.getScencd());

			BusiTools.getBusiRunEnvs().getChgevo().getCjsfls().add(cplCjSfsv);

		}

		bizlog.method("AnalyseScene end <<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * 按场景收费
	 * 
	 */
	public cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgFFEEHD SceneCharge(
			final cn.sunline.ltts.busi.iobus.type.fe.IoFeChrgComplexType.IoCgCalFee_IN cplIn) {
		IoCgFFEEHD cplOut = SysUtil.getInstance(IoCgFFEEHD.class);
		CgCalFee_IN cplFeeIn = SysUtil.getInstance(CgCalFee_IN.class);

		// 场景计费输入
		cplFeeIn.setScencd(cplIn.getScencd());
		cplFeeIn.setCustac(cplIn.getCustac());
		cplFeeIn.setCustno(cplIn.getCustno());
		cplFeeIn.setPredim(cplIn.getPredim());
		cplFeeIn.setTranbr(cplIn.getTranbr());
		cplFeeIn.setTranam(cplIn.getTranam());
		cplFeeIn.setAmount(cplIn.getAmount());
		cplFeeIn.setOubrno(cplIn.getOubrno());
		cplFeeIn.setInbrno(cplIn.getInbrno());
		cplFeeIn.setTranbr(cplIn.getTranbr());
		cplFeeIn.setTrancy(BusiTools.getDefineCurrency()); // 默认人民币
		cplFeeIn.setChrgcy(BusiTools.getDefineCurrency());
		cplFeeIn.setCstrfg(cplIn.getCstrfg());

		CgFFEEHD cplOutTmp = ChargPublic.calCjsf(cplFeeIn);

		if (CommUtil.isNotNull(cplOutTmp)) {
			cplOut.setRemark(cplOutTmp.getRemark());
			cplOut.setAmount(cplOutTmp.getAmount());
			cplOut.setCgpyrv(cplOutTmp.getCgpyrv());
			cplOut.setTotamt(cplOutTmp.getTotamt());
			cplOut.setTrnnam(cplOutTmp.getTrnnam());
			cplOut.setCuscnm(cplOutTmp.getCuscnm());

			if (CommUtil.isNotNull(cplOutTmp.getLstFEEHD())) {
				for (int i = 0; i < cplOutTmp.getLstFEEHD().size(); i++) {
					IoCgFrmFeeHD cplFeeHD = SysUtil
							.getInstance(IoCgFrmFeeHD.class);
					cplFeeHD.setCgpyrv(cplOutTmp.getLstFEEHD().get(i)
							.getCgpyrv());
					cplFeeHD.setArrgam(cplOutTmp.getLstFEEHD().get(i)
							.getArrgam());
					cplFeeHD.setChrgna(cplOutTmp.getLstFEEHD().get(i)
							.getChrgna());
					cplFeeHD.setChrgcd(cplOutTmp.getLstFEEHD().get(i)
							.getChrgcd());
					cplFeeHD.setRecvfg(cplOutTmp.getLstFEEHD().get(i)
							.getRecvfg());
					cplFeeHD.setFeeamt(cplOutTmp.getLstFEEHD().get(i)
							.getFeeamt());
					cplOut.getLstFEEHD().add(cplFeeHD);

				}
			}

			cplOut.setChacno(cplOutTmp.getChacno());
			cplOut.setChrgcy(cplOutTmp.getChrgcy());
			cplOut.setCstrfg(cplOutTmp.getCstrfg());
			cplOut.setSmryds(cplOutTmp.getSmryds());
			cplOut.setAcctno(cplOutTmp.getAcctno());
		}

		return cplOut;
	}

	@Override
	public void SelKcpChrg(String chrgcd, Output output) {
		if(CommUtil.isNull(chrgcd)){
			throw FeError.Chrg.BNASF076();
		}
		KcpChrg tblKcpChrg = FeCodeDao.selone_kcp_chrg(chrgcd, false);
		if(CommUtil.isNull(tblKcpChrg)){
			throw FeError.Chrg.BNASF074();
		}
		IoKcpChrg ioKcpChrg = SysUtil.getInstance(IoKcpChrg.class);
		CommUtil.copyProperties(ioKcpChrg, tblKcpChrg);
		output.setOut(ioKcpChrg);
	}

}
