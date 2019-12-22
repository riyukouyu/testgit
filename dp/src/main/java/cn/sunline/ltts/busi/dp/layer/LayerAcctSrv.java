package cn.sunline.ltts.busi.dp.layer;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.clwj.msap.core.tools.CommTools;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.base.DpPublic;
import cn.sunline.ltts.busi.dp.namedsql.DpAcctQryDao;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrt;
import cn.sunline.ltts.busi.dp.tables.DpDepoInst.KubInrtDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_LYINWY;

/***
 * @author zhangan
 * 功能:提供数据准备和数据计算
 */
public class LayerAcctSrv {

	/**
	 * 获取账户层的多层利率，并根据靠档类型 先存期后金额，先金额后存期 进行排序，默认是先存期后金额
	 * @param acctno
	 * @param cainpf
	 * @return
	 */
	public static List<KubInrt> getKubInrt(String acctno){
		
		return DpAcctQryDao.selAllKubInrtOrder1(acctno, true); //先存期后金额排序
	}
	
	/**
	 * 根据金额和存期天数，获取当前层次编号
	 * @param lstInrt 账户利率信息列表
	 * @param days 存期天数
	 * @param amt 金额值
	 * @return
	 */
	public static int getLayerMark(List<KubInrt> lstInrt, long days, BigDecimal amt){
		
		int count = lstInrt.size();
		int mark = 0;
		BigDecimal lvamot = BigDecimal.ZERO;
		long lvaday = 0L;
		
		for(int i = 0; i < count; i++){
			lvamot = lstInrt.get(i).getLvamot(); //当前层
			lvaday = lstInrt.get(i).getLvaday(); //当前层天数
			if(CommUtil.compare(amt,lvamot) >= 0 && CommUtil.compare(days, lvaday) >= 0){
				mark = i;
			}else{
				break;
			}
		}
		
		return mark;
	}
	/***
	 * 计算并更新各层次积数,日间交易动户滚积数，传入上次余额，及上次余额实际存期天数
	 * @param acctno 负债账号
	 * @param days 上次金额存期天数
	 * @param amt 上次日终/日均金额
	 * @param lyinwy 分层方式
	 */
	public static void rollCumulate(String acctno, long days, BigDecimal amt, E_LYINWY lyinwy){
		
		String trandt = CommTools.getBaseRunEnvs().getTrxn_date();//交易日期
		
		List<KubInrt> lstInrt = LayerAcctSrv.getKubInrt(acctno);
		
		BigDecimal cutmam = BigDecimal.ZERO;
		BigDecimal lastbl = BigDecimal.ZERO;
		
		int mark = LayerAcctSrv.getLayerMark(lstInrt, days, amt); //获取当前所在层次编号
		int count = lstInrt.size();
		
		//按账户上日余额计算所在层次的上日余额；
		
		for(int i = 0; i < count; i++){
			if(lyinwy == E_LYINWY.ALL){ //全额累进，只有一层
				if(i == mark){
					lastbl = amt;
					lstInrt.get(i).setLastbl(lastbl); //当前层次上日余额
					//务必修改上次余额后，再重新计算积数
					cutmam = DpPublic.calRealTotalAmt(lstInrt.get(i).getClvsmt(), lastbl, trandt, lstInrt.get(i).getLastdt());
					lstInrt.get(i).setClvsmt(cutmam); //当前层次积数
					lstInrt.get(i).setLastdt(trandt);
					lstInrt.get(i).setClvudt(trandt);
					lstInrt.get(i).setClmark(E_YES___.YES);
				}else{
					//cutmam = lstInrt.get(i).getClvsmt();
					lstInrt.get(i).setLastdt(trandt); //其他层次积数无变化,只修改余额更新日期
					lstInrt.get(i).setClmark(E_YES___.NO);
				}
				
				KubInrtDao.update_odb3(lstInrt.get(i));
			}else if(lyinwy == E_LYINWY.OVER){ //超额累进
				
				if(mark == count){
					lastbl = amt.subtract(lstInrt.get(mark).getLvamot()); //上日余额减去本层次金额即当前层次上日余额
					lstInrt.get(i).setLastbl(lastbl);
					cutmam = DpPublic.calRealTotalAmt(lstInrt.get(i).getClvsmt(), lastbl, trandt, lstInrt.get(i).getLastdt());
					lstInrt.get(i).setClvsmt(cutmam); //当前层次积数
					lstInrt.get(i).setLastdt(trandt);
					lstInrt.get(i).setClvudt(trandt);
					lstInrt.get(i).setClmark(E_YES___.YES);
				}else{
					if(i < mark){
						lastbl = lstInrt.get(i + 1).getLvamot().subtract(lstInrt.get(i).getLvamot());
						lstInrt.get(i).setLastbl(lastbl);
						cutmam = DpPublic.calRealTotalAmt(lstInrt.get(i).getClvsmt(), lastbl, trandt, lstInrt.get(i).getLastdt());
						lstInrt.get(i).setClvsmt(cutmam); //当前层次积数
						lstInrt.get(i).setLastdt(trandt);
						lstInrt.get(i).setClvudt(trandt);
						lstInrt.get(i).setClmark(E_YES___.NO);
					}else if(i == mark){
						lastbl = amt.subtract(lstInrt.get(i).getLvamot());
						lstInrt.get(i).setLastbl(lastbl);
						cutmam = DpPublic.calRealTotalAmt(lstInrt.get(i).getClvsmt(), lastbl, trandt, lstInrt.get(i).getLastdt());
						lstInrt.get(i).setClvsmt(cutmam); //当前层次积数
						lstInrt.get(i).setLastdt(trandt);
						lstInrt.get(i).setClvudt(trandt);
						lstInrt.get(i).setClmark(E_YES___.YES);
					}else{
						cutmam = lstInrt.get(i).getClvsmt();
						lstInrt.get(i).setClmark(E_YES___.NO);
						lstInrt.get(i).setLastdt(trandt); 
					}
				}
				
				KubInrtDao.update_odb3(lstInrt.get(i));
			} //end elseif 
		} //end for
		
	}
	
	
	
	/**
	 * 功能：日切后记上日账时，修改分层账户积数
	 * acctno 负债子账号
	 * days 上次余额周期天数
	 * amt: 当日余额
	 * lastbl 上次余额
	 * lyinwy:分层方式
	 * amntcd:借贷标志
	 */
	public static void rollCumuByCutDay(String acctno, long days, BigDecimal amt, BigDecimal lastbl, E_LYINWY lyinwy,E_AMNTCD amntcd){
		
		List<KubInrt> lstInrt = LayerAcctSrv.getKubInrt(acctno);
		
		int total = lstInrt.size();
		
		KubInrt last_inrt = null; //上一个层次
		KubInrt curr_inrt = null; //余额当前层次
		KubInrt next_inrt = null; //下一个层次
		//获取当前层次
		int mark = LayerAcctSrv.getLayerMark(lstInrt, days, amt); //标记上日余额所在层次的编号
		curr_inrt = lstInrt.get(mark);
		
		
		if(lyinwy == E_LYINWY.ALL){ //全额累进
			
			if(amntcd == E_AMNTCD.CR){ //贷方,余额增加
				if(mark == total){ //当前层次是最后一层
					next_inrt = curr_inrt;
					next_inrt.setLastbl(next_inrt.getLastbl().add(amt));
					next_inrt.setClvsmt(next_inrt.getClvsmt().add(amt));
					KubInrtDao.update_odb3(next_inrt);
				}else{ //不是最后一层,需要校验上日余额加校验金额后,是否达到下一个层次
					if(CommUtil.compare(curr_inrt.getLastbl().add(amt),lstInrt.get(mark + 1).getLvamot()) > 0){ 
						//达到下一层
						next_inrt = lstInrt.get(mark + 1);
						//当前层次的积数应该减去当前层次上日余额的数量，下一层次的积数应增加上日余额+交易金额的数量
						curr_inrt.setClvsmt(curr_inrt.getClvsmt().subtract(amt)); 
						curr_inrt.setLastbl(curr_inrt.getLastbl().subtract(amt));
						
						next_inrt.setClvsmt(next_inrt.getClvsmt().add(curr_inrt.getLastbl()).add(amt));
						next_inrt.setLastbl(lastbl.add(amt)); //全额累进时，账户上日余额等于上日余额所在层次的上日余额，此处向下进一层，应修改层次上日余额等于账户上日余额
						
						KubInrtDao.update_odb3(next_inrt);
						KubInrtDao.update_odb3(curr_inrt);
						//curr_inrt.setLastbl();
					}else{
						next_inrt = curr_inrt;
						next_inrt.setLastbl(next_inrt.getLastbl().add(amt));
						next_inrt.setClvsmt(next_inrt.getClvsmt().add(amt));
						KubInrtDao.update_odb3(next_inrt);
					}
				}
			}else if(amntcd == E_AMNTCD.DR){ //借方,余额减少
				if(mark == 0){ //第一层
					last_inrt = curr_inrt;
					last_inrt.setLastbl(last_inrt.getLastbl().subtract(amt));
					last_inrt.setClvsmt(last_inrt.getClvsmt().subtract(amt));
					KubInrtDao.update_odb3(last_inrt);
				}else{
					//减去交易金额后，掉入上个层次
					if(CommUtil.compare(curr_inrt.getLastbl().subtract(amt),lstInrt.get(mark).getLvamot()) < 0){
						//当前层次的积数应该减去当前层次余额的数量，上个层次的积数应累加上日余额减去交易金额的数量
						last_inrt = lstInrt.get(mark - 1);
						curr_inrt.setClvsmt(curr_inrt.getClvsmt().subtract(amt));
						curr_inrt.setLastbl(curr_inrt.getLastbl().subtract(amt)); 
						
						last_inrt.setClvsmt(last_inrt.getClvsmt().add((curr_inrt.getLastbl().subtract(amt))));
						last_inrt.setLastbl(lastbl.add(amt));
						
						KubInrtDao.update_odb3(last_inrt);
						KubInrtDao.update_odb3(curr_inrt);
					}else{
						last_inrt = curr_inrt;
						last_inrt.setLastbl(last_inrt.getLastbl().subtract(amt));
						last_inrt.setClvsmt(last_inrt.getClvsmt().subtract(amt));
						KubInrtDao.update_odb3(last_inrt);
					}
				}
			}
		}else if(lyinwy == E_LYINWY.OVER){
			if(amntcd == E_AMNTCD.CR){
				if(mark == total){ //当前层次等于最后一层
					next_inrt = curr_inrt;
					next_inrt.setLastbl(next_inrt.getLastbl().add(amt));
					next_inrt.setClvsmt(next_inrt.getClvsmt().add(amt));
					KubInrtDao.update_odb3(next_inrt);
				}else{
					//超额累进判断是否进入下一层需要使用总余额来校验
					if(CommUtil.compare(lastbl.add(amt),lstInrt.get(mark + 1).getLvamot()) > 0){
						//当前层次上日余额+交易金额后进入下一层次,因此应修改当前层次上日金额为层次金额,
						//下一层次金额 =（当前层次上日余额+交易金额-层次金额）,积数调整
						next_inrt = lstInrt.get(mark+1);
						curr_inrt.setClvsmt(curr_inrt.getClvsmt().add(curr_inrt.getLvamot().subtract(curr_inrt.getLastbl())));
						curr_inrt.setLastbl(curr_inrt.getLvamot());
						
						next_inrt.setClvsmt(next_inrt.getClvsmt().add(amt.add(curr_inrt.getLastbl()).subtract(curr_inrt.getLvamot())));
						next_inrt.setLastbl(lastbl.add(amt).subtract(lstInrt.get(mark + 1).getLvamot()));
						KubInrtDao.update_odb3(next_inrt);
						KubInrtDao.update_odb3(curr_inrt);
					}else{
						next_inrt = curr_inrt;
						next_inrt.setLastbl(next_inrt.getLastbl().add(amt));
						next_inrt.setClvsmt(next_inrt.getClvsmt().add(amt));
						KubInrtDao.update_odb3(next_inrt);
					}
				}
			}else if(amntcd == E_AMNTCD.DR){
				if(mark == 0){ //第一层
					last_inrt = curr_inrt;
					last_inrt.setLastbl(last_inrt.getLastbl().subtract(amt));
					last_inrt.setClvsmt(last_inrt.getClvsmt().subtract(amt));
					KubInrtDao.update_odb3(last_inrt);
				}else{
					//如果当前层次金额比交易金额小，则进入上一层
					if(CommUtil.compare(lastbl.subtract(amt),lstInrt.get(mark).getLvamot()) < 0){
						//当前层次上日余额-交易金额后掉入上一层,则当前层次上日余额更新为0,积数减少
						//上一层余额=上日余额-（交易金额-当前层上日余额）
						last_inrt = lstInrt.get(mark - 1);
						curr_inrt.setClvsmt(curr_inrt.getClvsmt().subtract(amt));
						curr_inrt.setLastbl(BigDecimal.ZERO);
						
						last_inrt.setClvsmt(last_inrt.getClvsmt().subtract(amt.subtract(curr_inrt.getLastbl())));
						last_inrt.setLastbl(last_inrt.getLastbl().subtract(amt.subtract(curr_inrt.getLastbl())));
						
						KubInrtDao.update_odb3(last_inrt);
						KubInrtDao.update_odb3(curr_inrt);
					}else{
						last_inrt = curr_inrt;
						last_inrt.setLastbl(last_inrt.getLastbl().subtract(amt));
						last_inrt.setClvsmt(last_inrt.getClvsmt().subtract(amt));
						KubInrtDao.update_odb3(last_inrt);
					}
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
