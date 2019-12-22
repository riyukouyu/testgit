package dp;


import org.junit.Test;

import cn.sunline.clwj.msap.core.junit.OnlineTest;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DEBTTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_BUSIBI;

public class TestFroz extends OnlineTest {

	
	@Test
	public void test1(){
	     
		E_BUSIBI s = E_BUSIBI.DEPO;
		E_DEBTTP a = E_DEBTTP.DP2404;
		String prodcd = DpPublic.getProdcd(s, a, "999");// 产品编号
		System.out.println(prodcd);
	}
}
