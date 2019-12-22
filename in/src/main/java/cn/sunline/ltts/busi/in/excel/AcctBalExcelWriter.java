package cn.sunline.ltts.busi.in.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Collection;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.in.type.InExcelTypes.AcctBalInfo;
import cn.sunline.ltts.busi.in.type.InExcelTypes.BizCalInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;

@SuppressWarnings("restriction")
public class AcctBalExcelWriter {
	protected File file;
	protected OutputStream os;
	protected Workbook book = null;
	public AcctBalExcelWriter() {
		super();
	}
	
	
	public AcctBalExcelWriter(File file,String sheetName) throws Exception, InvalidFormatException {
		super();
		this.file = file;
		if(!file.exists()) {
			if(file.createNewFile()) {
				throw DpModuleError.DpstComm.E9999("创建文件失败");
			}
		}
		os = new FileOutputStream(file);
		book = new XSSFWorkbook();
		Sheet sheet = book.createSheet(sheetName);
		
		String[] title = {"电子账号", "客户名称", "上日活期余额","上日定期余额"};
		Row titleRow = sheet.createRow(0);
		for(int i = 0; i < title.length; i++) {
			Cell cell = titleRow.createCell(i + 1);
			cell.setCellValue(title[i]);
		}
	}
	
	public void Write(AcctBalInfo info,String sheetName) throws Exception {
		Sheet sheet = book.getSheet(sheetName);
		int lastRowNum = sheet.getLastRowNum();
		Row currentRow = sheet.createRow(lastRowNum + 1);
		currentRow.createCell(0).setCellFormula("ROW() - 1");
		currentRow.createCell(1).setCellValue(info.getCustac());
		currentRow.createCell(2).setCellValue(info.getCustna());
		currentRow.createCell(3).setCellValue(info.getAcctbl().doubleValue());
		currentRow.createCell(4).setCellValue(info.getFxacbl().doubleValue());
	}
	
	public void Write(Collection<AcctBalInfo> infos,String sheetName) throws Exception {
		for(AcctBalInfo info : infos) {
			this.Write(info,sheetName);
		}
		BigDecimal acctblSum = BigDecimal.ZERO;
		BigDecimal fxacblSum = BigDecimal.ZERO;
		for(AcctBalInfo entity : infos) {
			acctblSum = acctblSum.add(entity.getAcctbl());
			fxacblSum = fxacblSum.add(entity.getFxacbl());
		}
		AcctBalInfo info = SysUtil.getInstance(AcctBalInfo.class);
		info.setCustac("");
		info.setCustna("合计：");
		info.setAcctbl(acctblSum);
		info.setFxacbl(fxacblSum);
		this.Write(info,sheetName);
	}
	
	public void Write(BizCalInfo... infos) throws Exception {
		for(BizCalInfo info : infos) {
			this.Write(info);
		}
	}
	
	public void Extract() throws Exception {
		book.write(os);
		book.close();
	}
	
}
