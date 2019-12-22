
package cn.sunline.ltts.busi.dptran.batchtran.redpck;

import java.io.File;
import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.FileUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameter;
import cn.sunline.clwj.msap.core.tables.MsCoreTable.KnpParameterDao;
import cn.sunline.clwj.msap.core.tools.DBTools;
import cn.sunline.clwj.msap.core.tools.MsSystemSeq;
import cn.sunline.edsp.base.file.FileDataExecutor;
import cn.sunline.edsp.base.lang.ByRef;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBach;
import cn.sunline.ltts.busi.dp.tables.RpMgr.KnbRptrBachDao;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.ltts.busi.sys.errors.FdError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACSETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CSEXTG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_RPTRTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRMPTP;
	 /**
	  * 红包批量退款/冲正文件读取
	  *
	  */


public class rprdfiDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rprdfi.Input, cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rprdfi.Property> {
    
    private static final String fileSepa1 = "|@|";//文件分隔符
    private static final String encoding = "UTF-8";//文件编码
    String trandt = DateTools2.getDateInfo().getSystdt();//系统日期
    private BigDecimal sumamt = BigDecimal.ZERO; // 实际处理总金额
    private static BizLog biz = BizLogUtil.getBizLog(rprdfiDataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rprdfi.Input input, cn.sunline.ltts.busi.dptran.batchtran.redpck.intf.Rprdfi.Property property) {
	     
	     
	     kapb_wjplxxb kapbWjplxxb = Kapb_wjplxxbDao.selectOne_odb1(input.getFilesq(), true);
         
         String fileName = null;
         final String filesq = input.getFilesq();
         
         KnpParameter para = KnpParameterDao.selectOne_odb1("DpParm", "readfl", "rprdfi", "%", true);
         
         //读取开关控制校验
         if(CommUtil.compare("0", para.getParm_value5())==0){
             biz.debug("<<<<<<<<<<读取红包批量退款文件,跳过处理>>>>>>>>>>>>>>>>>>");
             return;
         }
         
         fileName=kapbWjplxxb.getDownph()+kapbWjplxxb.getDownna();
         File file = new File(fileName);
         if(!file.exists()){
             return;
         }
         biz.debug("<<<<<<<<<<获取文件路径>>>>>>>>>>>>>>>>>>"+fileName);
         //读取红包批量还款文件
         final ByRef<Long> tolnnm = new ByRef<Long>(0L); // 应处理总笔数
         final ByRef<Long> counts = new ByRef<Long>(0L); // 实际处理总笔数
         final ByRef<BigDecimal> tolnam = new ByRef<BigDecimal>(); // 应处理总金额
         sumamt = BigDecimal.ZERO;  // 实际处理总金额
         biz.debug("<<<<<<<<<<开始读取文件>>>>>>>>>>>>>>>>>>");
         //读取红包批量还款文件
         FileUtil.readFile(fileName, new FileDataExecutor() {
             
             @Override
             public void process(int arg0, String arg1) {
                  MsSystemSeq.getTrxnSeq();//重置流水
                 try {
                     //行信息
                     String[] line = arg1.split("\\|@\\|",-1);
                     
                     if(arg0 == 1){
                         //读取文件头
                         if (line.length != 2) {
                             throw DpModuleError.DpstComm.E9999("读取红包批量退款文件第1行字段数量不符，文件异常。");
                         }
                         tolnnm.value = Long.parseLong(line[0]);//应处理总笔数
                         tolnam.value = new BigDecimal(line[1]);//应处理总金额
                         return;
                     }
                     if(arg0 > 1){
                         String soursq = line[0];//来源方交易流水
                         String sourdt = line[1];//来源方交易日期
                         String sRptrtp = line[2];//红包交易类型
                         String sRptype = line[3];//红包类型
                         String smrycd = line[4];//摘要码
                         String deborg = line[5];//借方机构
                         String decard = line[6];//借方卡号
                         String debact = line[7];//借方账号
                         String sDecstp = line[8];//借方子账户类型
                         BigDecimal tranam = new BigDecimal(line[9]);//交易金额
                         String crcycd = line[10];//货币代号
                         String sCsextg = line[11];//钞汇标志
                         String crdorg = line[12];//贷方机构
                         String crcard = line[13];//贷方卡号
                         String crdact = line[14];//贷方账号
                         String sCrcstp = line[15];//贷方子账户类型
                         String userid = line[16];//用户ID
                         String rpcode = line[17];//红包编号
                         String oridat = line[18];//原交易日期
                         String stady1 = line[19];//备用字段1
                         String stady2 = line[20];//备用字段2
                         String stady3 = line[21];//备用字段3
                         
                         //转换为枚举类型
                         E_RPTRTP rptrtp = CommUtil.toEnum(E_RPTRTP.class, sRptrtp);
                         E_RPTRTP rptype = CommUtil.toEnum(E_RPTRTP.class, sRptype);
                         E_ACSETP decstp = CommUtil.toEnum(E_ACSETP.class, sDecstp);
                         E_CSEXTG csextg = CommUtil.toEnum(E_CSEXTG.class, sCsextg);
                         E_ACSETP crcstp = CommUtil.toEnum(E_ACSETP.class, sCrcstp);
                         
                         if(rptrtp != E_RPTRTP.BT303){
                             throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第3列红包交易类型不支持！");
                         }
                         if(CommUtil.isNull(debact)){
                             throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第8列转出方卡号不能为空！");
                         }
//                     if(CommUtil.isNull(tobrch)){
//                         throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第6列转出方所属机构不能为空！");
//                     }
//                     if(CommUtil.isNull(crcycd)){
//                         throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第7列币种不能为空！");
//                     }
                         if(CommUtil.isNull(tranam)){
                             throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第10列交易金额不能为空！");
                         }
                         if(CommUtil.isNull(crdact)){
                             throw DpModuleError.DpstComm.E9999("文件第" + arg0 + "行记录第15列转入方卡号不能为空！");
                         }
                         
                         //交易机构 和 币种处理
                         crcycd = CommUtil.isNull(crcycd)?"CNY":crcycd;
                         crdorg = SysUtil.getRemoteInstance(IoSrvPbBranch.class)
                                 .getRootBranch(CommUtil.isNull(crcycd)?"CNY":crcycd, E_BRMPTP.B);
                         deborg = SysUtil.getRemoteInstance(IoSrvPbBranch.class)
                                 .getRootBranch(CommUtil.isNull(crcycd)?"CNY":crcycd, E_BRMPTP.B);
                         
                         //读取红包批量退款文件，登记到红包系统批量交易明细表
                         KnbRptrBach rptr = SysUtil.getInstance(KnbRptrBach.class);
                         rptr.setSoursq(soursq);//来源方交易流水
                         rptr.setSourdt(sourdt);//来源方交易日期
                         rptr.setRptrtp(rptrtp);//红包交易类型
                         rptr.setRptype(rptype);//红包类型
                         rptr.setSmrycd(smrycd);//摘要代码
                         rptr.setDeborg(deborg);//借方机构
                         rptr.setDecard(decard);//借方卡号
                         rptr.setDebact(debact);//借方账号
                         rptr.setDecstp(decstp);//借方子账户类型
                         rptr.setTranam(tranam);//交易金额
                         rptr.setCrcycd(crcycd);//货币代号
                         rptr.setCsextg(csextg);//钞汇标志
                         rptr.setCrdorg(crdorg);//贷方机构
                         rptr.setCrcard(crcard);//贷方卡号
                         rptr.setCrdact(crdact);//贷方账号
                         rptr.setCrcstp(crcstp);//贷方子账户类型
                         rptr.setUserid(userid);//用户编号
                         rptr.setRpcode(rpcode);//红包编号
                         rptr.setOridat(oridat);//原交易日期
                         rptr.setStady1(stady1);//备用字段1
                         rptr.setStady2(stady2);//备用字段2
                         rptr.setStady3(stady3);//备用字段3
                         rptr.setFilesq(filesq);//文件批次号
                         rptr.setDataid(String.valueOf(counts));//任务编号
                         rptr.setTranst(E_TRANST.WAIT);//交易状态
                         KnbRptrBachDao.insert(rptr);
                         
                         counts.value++;
                         sumamt = sumamt.add(rptr.getTranam());
                     }
                 } catch (Exception e) {
                     throw DpModuleError.DpstComm.E9999("读取红包批量退款文件第" + arg0 + "条记录处理异常！",e); 
                 }
             }
         },encoding);
         biz.debug("<<<<<<<<<<开始读取结束>>>>>>>>>>>>>>>>>>");
         // 判断实际处理与文件总是否相符
         if (CommUtil.compare(tolnnm.value, counts.value) != 0) {
             throw FdError.FundAcct.E0001("读取红包批量退款文件总记录数[" + tolnnm.value+ "]与实际处理记录数[" + counts.value + "]不符！");
         }
         if (CommUtil.compare(tolnam.value, sumamt) != 0) {
             throw FdError.FundAcct.E0001("读取红包批量退款文件总金额[" + tolnam.value+ "]与实际处理总金额[" + sumamt + "]不符！");
         }
         DBTools.commit(); 
         
	}

}


