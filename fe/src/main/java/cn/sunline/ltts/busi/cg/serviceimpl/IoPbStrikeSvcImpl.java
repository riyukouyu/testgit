package cn.sunline.ltts.busi.cg.serviceimpl;

import java.math.BigDecimal;

import cn.sunline.ltts.busi.cg.charg.ChargStrike;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTypeStrikeInfo.ChargStrikeOutput;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTypeStrikeInfo.ProcPbChargStrikeInput;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.edsp.base.lang.Options;
 /**
  * 公共冲正服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoPbStrikeSvcImpl", longname="公共冲正服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoPbStrikeSvcImpl implements cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbStrikeSvcType{
 /**
  * 收费登记冲正
  *
  */
/*	public Options<ChargStrikeOutput> procPbChargStrike( final cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbStrikeSvcType.ProcPbChargStrike Input){
	    
	    ProcPbChargStrikeInput param = Input.getStrikeInput();
	    String sOrtrdt = param.getOrtrdt(); //原交易日期
        String sOrtrsq = param.getOrtrsq(); //原交易流水
		Options<ChargStrikeOutput> lstOutput = ChargStrike.strikeChargRegst(sOrtrdt, sOrtrsq);  //收费登记冲正
		
		Input.getStrikeInput().setOutput(lstOutput);
	}*/
	@Override
	public Options<ChargStrikeOutput> procPbChargStrike(ProcPbChargStrikeInput strikeInput) {
		// ProcPbChargStrikeInput param = Input.getStrikeInput();
	    String sOrtrdt = strikeInput.getOrtrdt(); //原交易日期
        String sOrtrsq = strikeInput.getOrtrsq(); //原交易流水
        E_YES___ isdieb = strikeInput.getIsdieb(); //是否隔日冲正
		Options<ChargStrikeOutput> lstOutput = ChargStrike.strikeChargRegst(sOrtrdt, sOrtrsq, isdieb);  //收费登记冲正
		return lstOutput;
	}

@Override
public void procPbChargOffStrike(ProcPbChargStrikeInput strikeInput) {

	String sOrtrdt = strikeInput.getOrtrdt(); //原交易日期
    String sOrtrsq = strikeInput.getOrtrsq(); //原交易流水
    Long evrgsq = strikeInput.getEvrgsq();//事件登记序号
    BigDecimal tranam = strikeInput.getTranam();//交易金额
	ChargStrike.strikeChargOffRegst(sOrtrdt, sOrtrsq, evrgsq,tranam);  //销记收费登记冲正	
}

@Override
public void procPbChargAdjtStrike(ProcPbChargStrikeInput strikeInput) {
	String sOrtrdt = strikeInput.getOrtrdt(); //原交易日期
    String sOrtrsq = strikeInput.getOrtrsq(); //原交易流水
    BigDecimal tranam = strikeInput.getTranam();//交易金额
	ChargStrike.strikeChargAdjtRegst(sOrtrdt, sOrtrsq,tranam);  //销记收费登记冲正	
	
}


}

