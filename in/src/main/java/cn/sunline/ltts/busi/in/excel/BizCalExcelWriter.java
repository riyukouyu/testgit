package cn.sunline.ltts.busi.in.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cn.sunline.ltts.busi.in.type.InExcelTypes.BizCalInfo;
import cn.sunline.edsp.busi.dp.errors.DpModuleError;
import cn.sunline.edsp.busi.dp.errors.DpModuleError.DpstComm;

@SuppressWarnings("restriction")
public class BizCalExcelWriter {
	protected File file;
	protected OutputStream os;
	protected Workbook book = null;
	public BizCalExcelWriter() {
		super();
	}
	
	
	public BizCalExcelWriter(File file) throws Exception, InvalidFormatException {
		super();
		this.file = file;
		if(!file.exists()) {
			if(file.createNewFile()) {
				throw DpModuleError.DpstComm.E9999("创建文件失败");
			}
		}
		os = new FileOutputStream(file);
		book = new XSSFWorkbook();
		Sheet sheet = book.createSheet("statistics");
		
		String[] title = {"交易码", "交易名称", "访问量"};
		Row titleRow = sheet.createRow(0);
		for(int i = 0; i < title.length; i++) {
			Cell cell = titleRow.createCell(i + 1);
			cell.setCellValue(title[i]);
		}
	}
	
	public void Write(BizCalInfo info) throws Exception {
		Sheet sheet = book.getSheet("statistics");
		int lastRowNum = sheet.getLastRowNum();
		Row currentRow = sheet.createRow(lastRowNum + 1);
		currentRow.createCell(0).setCellFormula("ROW() - 1");
		currentRow.createCell(1).setCellValue(info.getPrcscd());
		currentRow.createCell(2).setCellValue(info.getPrcsna());
		currentRow.createCell(3).setCellValue(info.getCounts());
	}
	
	public void Write(Collection<BizCalInfo> infos) throws Exception {
		for(BizCalInfo info : infos) {
			this.Write(info);
		}
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
	
//	public static void main(String[] args) throws Exception{
//		File file = new File("/Users/jiangyaming/Documents/test2.xlsx");  
//        if(file.exists()) {  
//            file.delete();  
//        }  
//        BizCalExcelWriter writer = new BizCalExcelWriter(file);  
//          
//        BizCalInfo user1 = new BizCalInfo("admin", "admin", "Administrator");  
//        BizCalInfo user2 = new BizCalInfo("user1", "user1", "Sally");  
//        BizCalInfo user3 = new BizCalInfo("user2", "zhangsan", "张三");  
//          
//        List<User> users = new ArrayList<User>();  
//          
//        users.add(user1);  
//        users.add(user2);  
//        users.add(user3);  
//          
//        writer.Write(users);  
//        writer.Extract();  
//
//	}
}
