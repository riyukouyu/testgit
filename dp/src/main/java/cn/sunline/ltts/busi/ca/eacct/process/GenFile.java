package cn.sunline.ltts.busi.ca.eacct.process;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.ca.namedsql.CaDao;
import cn.sunline.ltts.busi.ca.tables.ElectronicAccount.KnaCust;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable;

public class GenFile {
	private static final BizLog bizlog = BizLogUtil.getBizLog(GenFile.class);
	/**
	 * 
	 * <p>
	 * <li>作者 ：zhangll</li>
	 * <li>日期 ：2016年1月9日下午4:41:16</li>
	 * <li>方法描述：
	 * </li>
	 * 参数描述：
	 * 		生成kna_cust表数据
	 *   @param trandt 日期
	 */
	public void GenKnaCustInfo(String trandt,String scjioyrq){
		
		final String fileSepa1 = "^"; //分隔符
		// 产生文件的日期目录
		String trdtPath = trandt + "/";
		
		KnpParameter para1 = KnpParameterDao.selectOne_odb1("CaParm.custfile", "file", "%", "%",true);
		String path1 = para1.getParm_value1();
		path1 = para1.getParm_value1() + trdtPath;
		bizlog.debug("文件产生路径 path:[" + path1 + "]");
		// 获取文件名
		String filename1 = para1.getParm_value2();
		bizlog.debug("文件名称 filename:[" + filename1 + "]");
		// 获取是否产生文件标志
		String isCreateFlg1 = para1.getParm_value3();
		bizlog.debug("文件产生标志 :[" + isCreateFlg1 + "]");
		// 获取加载模式（增量/全量）
		String createMode1 =  para1.getParm_value4();
		bizlog.debug("文件加载模式 :[" + createMode1 + "]");
		if (CommUtil.equals(isCreateFlg1, "Y")) {
			final LttsFileWriter file = new LttsFileWriter(path1, filename1);
//			List<IoLnCustType> entities = null;
			Params params = new Params();
            String namedSqlId = null;//查询数据集的命名sql
			if (CommUtil.equals(createMode1, "QL")) {
				namedSqlId = CaDao.namedsql_selKnaCustInfoByDatetm;
			} else {
				namedSqlId = CaDao.namedsql_selKnaCustInfoByDatetm;
				params.add("datetm", scjioyrq);
			}
			
				file.open();
				try {
					DaoUtil.selectList(namedSqlId, params, new CursorHandler<KnaCust>() {
	    	    		@Override
	    	    		public boolean handle(int index, KnaCust entity) {
							// 写文件
	    	    			StringBuffer buf = new StringBuffer();
							buf.append(entity.getCustac()).append(fileSepa1);
							buf.append(entity.getCacttp()).append(fileSepa1);
							buf.append(entity.getCustno()).append(fileSepa1);
							buf.append(entity.getCustna()).append(fileSepa1);
							buf.append(entity.getOpendt()).append(fileSepa1);
							buf.append(entity.getOpensq()).append(fileSepa1);
							buf.append(entity.getClosdt()).append(fileSepa1);
							buf.append(entity.getClossq()).append(fileSepa1);
							buf.append(entity.getCardno()).append(fileSepa1);
							buf.append(entity.getAccttp()).append(fileSepa1);
							buf.append(entity.getBrchno()).append(fileSepa1);
							buf.append(entity.getAcctst()).append(fileSepa1);
							buf.append(entity.getCorpno()).append(fileSepa1);
							//buf.append(entity.getDatetm()).append(fileSepa1);
							buf.append(entity.getTmstmp()).append(fileSepa1);
							file.write(buf.toString());
	    	    			return true;
	    	    		}
	    	    	});
					
				} finally {
					file.close();
				}
			}
		bizlog.debug("KNA_CUST" + filename1 + "文件产生完成");
	}
	
	public void genKnaCustFile(String lstrdt) {
		final String delimt = "^"; //分隔符
		String lstrdtPath = lstrdt + "/"; // 日期目录
		KnpParameter tblKnpParameter = KnpParameterDao.selectOne_odb1("ACCT", "cafile", "01", "%", true);
		String path = tblKnpParameter.getParm_value1(); // 文件路径
		path += lstrdtPath;
		bizlog.debug("文件产生路径:[" + path + "]");
		
		String fileName = tblKnpParameter.getParm_value2(); // 文件名
		bizlog.debug("文件名:[" + fileName + "]");
		
		String creatFlag = CommUtil.nvl(tblKnpParameter.getParm_value3(), "Y"); // 是否创建文件标志
		bizlog.debug("是否创建文件标志:[" + creatFlag + "]");
		
		String creatMode = CommUtil.nvl(tblKnpParameter.getParm_value5(), "QL"); // 加载模式(增量/全量)
		bizlog.debug("文件加载模式:[" + creatMode + "]");
		
		if (creatFlag.equals("Y")) {
			final LttsFileWriter file = new LttsFileWriter(path, fileName);
			Params params = new Params();
			String namedSqlId = "";
			bizlog.debug("creatMode:[" + creatMode + "]");
			if (CommUtil.equals("QL", creatMode)) {
				namedSqlId = CaDao.namedsql_selKnCustAll;
			}
			
			file.open();
			try {
				DaoUtil.selectList(namedSqlId, params, new CursorHandler<IoCaTable.IoCaKnaCust>() {
					@Override
					public boolean handle(int index, IoCaTable.IoCaKnaCust entity) {
						String custac = CommUtil.isNotNull(entity.getCustac()) ? entity.getCustac() : "";
						String cacttp = CommUtil.isNotNull(entity.getCacttp().getValue()) ? entity.getCacttp().getValue() : "";
                        String custno = CommUtil.isNotNull(entity.getCustno()) ? entity.getCustno() : "";
                        String custna = CommUtil.isNotNull(entity.getCustna()) ? entity.getCustna() : "";
                        String opendt = CommUtil.isNotNull(entity.getOpendt()) ? entity.getOpendt() : "";
                        String opensq = CommUtil.isNotNull(entity.getOpensq()) ? entity.getOpensq() : "";
                        String closdt = CommUtil.isNotNull(entity.getClosdt()) ? entity.getClosdt() : "";
                        String clossq = CommUtil.isNotNull(entity.getClossq()) ? entity.getClossq() : "";
                        String cardno = CommUtil.isNotNull(entity.getCardno()) ? entity.getCardno() : "";
                        String accttp = CommUtil.isNotNull(entity.getAccttp()) ? entity.getAccttp().getValue() : "";
                        String brchno = CommUtil.isNotNull(entity.getBrchno()) ? entity.getBrchno() : "";
                        String acctst = CommUtil.isNotNull(entity.getAcctst()) ? entity.getAcctst().getValue() : "";
                        String corpno = CommUtil.isNotNull(entity.getCorpno()) ? entity.getCorpno() : "";
                        //String datetm = CommUtil.isNotNull(entity.getDatetm()) ? entity.getDatetm() : "";
                        String timetm = CommUtil.isNotNull(entity.getTmstmp()) ? entity.getTmstmp().toString() : "";

                        file.write(custac + delimt + cacttp + delimt + custno
                                + delimt + custna + delimt + opendt + delimt + opensq
                                + delimt + closdt + delimt + clossq + delimt + cardno
                                + delimt + accttp + delimt + brchno + delimt + acctst
                                + delimt + corpno + delimt +  delimt + timetm
                                );
						return true;
					}
				});
			}
			finally {
				file.close();
			}
			bizlog.debug("电子账户（导出数据）" + fileName + "文件产生完成");
		}
	}
}
