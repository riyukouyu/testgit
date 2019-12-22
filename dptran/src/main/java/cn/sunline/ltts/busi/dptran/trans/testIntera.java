package cn.sunline.ltts.busi.dptran.trans;

public class testIntera {

	public static void test(
			String source,
			String target,
			final cn.sunline.ltts.busi.dptran.trans.intf.TestIntera.Output output) {
		System.out.println("原系统ID:" + source + "<<<<<<<<<<<<<>>>>>>>>>>>>>>>>"
				+ "目标系统ID" + target);
		output.setSource(source);
		output.setTarget(target);
	}
}
