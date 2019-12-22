package cn.sunline.edsp.busi.dptran.tools;

/**
 * <p>
 * 文件功能说明：
 * </p>
 * 
 * @Author songlw
 *         <p>
 *         <li>2019年12月10日-上午11:39:57</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2019年12月10日：存款常量定义类</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class DpConstantDefine {
	// 交易信息
	public static final String TRAN_FRACT_PROVISION_INFO = "月结分润计提";
	// 交易备注
	public static final String TRAN_FRACT_PROVISION_REMARK = "月结分润计提存入";
	// 摘要代码
	public static final String TRAN_FRACT_PROVISION_SMRYCD = "YJFRJT";
	// 摘要信息
	public static final String TRAN_FRACT_PROVISION_SMRYDS = "存现";

	public static final String TRAN_FRACT_SIGN_PROVISION = "1";

	public static final String TRAN_FRACT_SIGN_REVERSE = "0";
	// 交易备注
	public static final String TRAN_FRACT_REVERSE_REMARK = "日结分润计提冲正";
	// 摘要代码
	public static final String TRAN_FRACT_REVERSE_SMRYCD = "RJFRJTCZ";
	// 摘要信息
	public static final String TRAN_FRACT_REVERSE_SMRYDS = "存现冲正";
	// 批量分润记账批量组
	public static final String TRAN_TIMING_PROFIT_CODE = "ST1007";
	// 批量代发记账批量组
	public static final String TRAN_TIMING_PAID_CODE = "ST1008";
	// 批量代发摘要码
	public static final String TRAN_PAID_SMRYCD = "STDR";
	
	public static final String TRAN_PAID_REMARK = "T1批量代发";
}
