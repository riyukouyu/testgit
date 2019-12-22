package cn.sunline.ltts.busi.dp.froz;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwne;
import cn.sunline.ltts.busi.dp.tables.DpFroz.KnbFrozOwneDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZOW;

/**
 * 提供给外部调用的冻结相关工具
 * @author Administrator
 *
 */
public class DpFrozTools {
		
	/**
	 * 检查是否冻结
	 * @param frozow 冻结主体类型
	 * @param frowid 冻结主体id
	 * @param frinfg 入冻结检查标志
	 * @param frotfg 出冻结检查标志
	 * @param fralfg 全额冻结检查标志
	 * @return
	 */
	public static boolean isFroz(E_FROZOW frozow,String frowid,E_YES___ frinfg,E_YES___ frotfg,E_YES___ fralfg){
		boolean frozfg = false;
		KnbFrozOwne tblKnbFrozOwne = KnbFrozOwneDao.selectOne_odb1(frozow, frowid, false);
		if(CommUtil.isNotNull(tblKnbFrozOwne)){
			if(E_YES___.YES == frinfg && E_YES___.YES == tblKnbFrozOwne.getFrinfg()){
				frozfg = true;
			}
			
			if(E_YES___.YES == frotfg && E_YES___.YES == tblKnbFrozOwne.getFrotfg()){
				frozfg = true;
			}
			
			if(E_YES___.YES == fralfg && E_YES___.YES == tblKnbFrozOwne.getFralfg()){
				frozfg = true;
			}
		}
		 
		return frozfg;
	}
	
	/**
	 * 获取冻结余额
	 * @param frozow 冻结主体类型
	 * @param frowid 冻结主体id
	 * @return
	 */
	public static BigDecimal getFrozBala(E_FROZOW frozow,String frowid){
		BigDecimal frozbl = new BigDecimal("0.00");
		KnbFrozOwne tblKnbFrozOwne = KnbFrozOwneDao.selectOne_odb1(frozow, frowid, false);
		if(CommUtil.isNotNull(tblKnbFrozOwne)) {
			if(null != tblKnbFrozOwne.getFrozbl()) {
				frozbl = tblKnbFrozOwne.getFrozbl();
			}
		}
		return frozbl;
	}

	/**
	 * 获取冻结信息
	 * @param frozow 冻结主体类型
	 * @param frowid 冻结主体id
	 * @return
	 */
	public static DpFrozInfoEntity getFrozInfo(E_FROZOW frozow,String frowid){
		DpFrozInfoEntity entity = new DpFrozInfoEntity();
		entity.setFrozow(frozow);
		entity.setFrowid(frowid);
		entity.setFrozbl(BigDecimal.ZERO);
		
		KnbFrozOwne tblKnbFrozOwne = KnbFrozOwneDao.selectOne_odb1(frozow, frowid, false);
		if(CommUtil.isNotNull(tblKnbFrozOwne)){
			entity.setFrinfg(tblKnbFrozOwne.getFrinfg());
			entity.setFrotfg(tblKnbFrozOwne.getFrotfg());
			entity.setFralfg(tblKnbFrozOwne.getFralfg());
			entity.setFrozbl(tblKnbFrozOwne.getFrozbl());
		}
		return entity;
	}
	
	/**
	 * 判断是否允许存入
	 * @param frozow 冻结主体类型
	 * @param frowid 冻结主体id
	 * @return
	 */
	public static boolean getSaveFg(E_FROZOW frozow,String frowid){	
		boolean savefg = true;
		if(isFroz(frozow, frowid, E_YES___.YES, E_YES___.NO, E_YES___.YES)){
			savefg = false;
		}
		return savefg;
	}
	
	/**
	 * 判断是否允许支取
	 * @param frozow 冻结主体类型
	 * @param frowid 冻结主体id
	 * @return
	 */
	public static boolean getDrawFg(E_FROZOW frozow,String frowid){
		boolean drawfg = true;
		if(isFroz(frozow, frowid, E_YES___.NO, E_YES___.YES, E_YES___.YES)){
			drawfg = false;
		}
		return drawfg;
	}
}
