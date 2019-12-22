package trans;

import org.junit.Test;

/**
 * { "input":{
 * 
 * }, "sys":{ "prcscd":"opdppr", "erortx":null, "erorcd":null, "corpno":"985",
 * "langcd":null, "routrl":null, "tdcnno":"AA0", "status":null }, "comm_req":{
 * "smsvrf":null, "authpw":null, "smryds":null, "routtp":null, "authfg":null,
 * "rviast":null, "counts":null, "authtp":null, "scenid":null, "sessid":null,
 * "aptrtp":null, "servtp":"TM", "routky":null, "inpudt":"20170516",
 * "servno":null, "authbr":null, "tranbr":"985000", "bizzsq":null,
 * "authlv":null, "spcapi":null, "inpucd":"01", "retrtm":null, "busisq":null,
 * "inpusq":"20170516100009", "authif":null, "corpno":null, "authsq":null,
 * "authus":null, "aubrlv":null, "fatype":null, "tranus":"9854015",
 * "smrycd":null, "acctno":null, "favalu":null, "passwd":null, "surefg":null,
 * "pageno":null, "pgsize":null, "spared":null, "device":null } }
 * 
 * @author Administrator
 * 
 */
public class opdpprTest {
	//@ClassRule
	// public static ApTestRule rule = new ApTestRule();

	@Test
    public void testSucess() throws Exception {
        //交易输入，采用javabean的方式操作
 //   	cn.sunline.ltts.busi.dptran.trans.trans.intf.Opdppr.InputSetter input = rule.createModel(cn.sunline.ltts.busi.dptran.trans.trans.intf.Opdppr.InputSetter.class);
		 
//        input.setQrusid("777");
        //执行交易
 //       TransReponse responseData = rule.runTrans("opdppr", input);
        //结构短语
  //      Assert.assertEquals(responseData.isSucess(), true);
  //      Assert.assertEquals(responseData.getCommRes().getTranus(), "777");
        //返回output，采用javabean的方式操作
  //      Output output = responseData.getOutput(Opdppr.Output.class);
         
    }
}
