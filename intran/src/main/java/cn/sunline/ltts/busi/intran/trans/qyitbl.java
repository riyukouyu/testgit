package cn.sunline.ltts.busi.intran.trans;

public class qyitbl {
	/**
	 * 	<p>
	 *      <li>2015年9月14日-上午11:08:24</li>
	 *      <li>功能说明</li>
	 *      <li>1.根据“WB”+科目号的busino查询子户号</li>
	 *      <li>2.根据科目，子户号，机构，币种查询内部户信息</li>
	 *  </p>
	 * @Author wanggl
	 *         
	 * @param itemcd 科目号
	 * @param Output 返回结果
	 *
	 */
	public static void qryItemBlForWb( String itemcd, String crcycd, final cn.sunline.ltts.busi.intran.trans.intf.Qyitbl.Output Output){/*
		//查询条件不能为空
		if(CommUtil.isNull(itemcd)){
			throw InError.comm.E0003("查询条件科目号不能为空");
		}
		
		//根据busino查询子户号
		String busino = "WB"+itemcd;
		GlKnpBusi busi = GlKnpBusiDao.selectOne_odb1(busino, false);
		
		if(CommUtil.isNull(busi)){
			throw InError.comm.E0003("查询科目信息失败，无对应记录");
		}
		String brchno = CommTools.getBaseRunEnvs().getTrxn_branch();
		crcycd = CommUtil.nvl(crcycd, "01");
		//查询余额信息
		GlKnaAcct acct = GlKnaAcctDao.selectOne_odb2(brchno, itemcd, crcycd, busi.getSubsac(), false);
		BigDecimal onlibl = BigDecimal.ZERO;
		if(CommUtil.isNull(acct)){
			//查询科目信息
			GlKnpItem item = GlKnpItemDao.selectOne_odb1(itemcd, true); 
			Output.setAcctno("");//账户
			Output.setItemcd(item.getItemcd());//科目
			Output.setItmcdn(item.getBlncdn());
			Output.setBrchno(brchno);
			Output.setCrcycd(crcycd);
			Output.setOnlibl(onlibl);//余额
			Output.setBlncdn(item.getBlncdn());//余额方向
		} else {
			switch (acct.getBlncdn()) {
			case C:
				onlibl = acct.getCrctbl();
				break;
			case D:
				onlibl = acct.getDrctbl();
				break;
			case R:
				onlibl = acct.getDrctbl();
				break;
		
			default:
				break;
			}
			Output.setAcctno(acct.getAcctno());//账户名称
			Output.setItemcd(itemcd);//科目
			Output.setItmcdn(acct.getItmcdn());
			Output.setBrchno(brchno);
			Output.setCrcycd(crcycd);
			Output.setOnlibl(onlibl);//余额
			Output.setBlncdn(acct.getBlncdn());//余额方向
		}
	*/}
}
