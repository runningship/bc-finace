package org.bc.finance;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bc.sdak.CommonDaoService;
import org.bc.sdak.Page;
import org.bc.sdak.SimpDaoTool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class StockTuiJian {

	static CommonDaoService dao = SimpDaoTool.getGlobalCommonDaoService();

	public static void main(String[] args) throws IOException {
		 StartUpListener.initDataSource();
		// //所有非创业板股票
		Page<Stock> page = new Page<Stock>();
		page.setPageSize(100);
		page = dao.findPage(page, "from Stock where code not like '3%' ");
//		List<Stock> list = dao.listByParams(Stock.class,"from Stock where code not like '3%' ");
		//http://table.finance.yahoo.com/table.csv?s=000061.ss
		//http://table.finance.yahoo.com/table.csv?s=000061.sz
		 List<AverageFC> result = new ArrayList<AverageFC>();
		for(Stock stock : page.getResult()){
			try{
				AverageFC afc = null;
				if("sz".equals(stock.type)){
					afc = cacuMoveAverage("SHE:"+stock.code , stock.name);
				}else if("sh".equals(stock.type)){
					afc = cacuMoveAverage("SHA:"+stock.code , stock.name);
				}
				if(afc!=null){
					result.add(afc);
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		Collections.sort(result);
		System.out.println();
		for(AverageFC afc :result){
			if(afc.fangcha==0){
				continue;
			}
			System.out.println(afc.code+"("+afc.name+")的方差="+afc.fangcha);
		}
	}

	private static AverageFC cacuMoveAverage(String code , String name) throws IOException {
//		URL url = new URL("http://table.finance.yahoo.com/table.csv?s="+code+".ss&d=3&e=31&f=2015&g=d&a=1&b=1&c=2015&ignore=.csv");
		URL url = new URL("http://www.google.com.hk/finance/historical?q="+code);
		//http://www.google.com.hk/finance/historical?q=000861
		URLConnection conn = url.openConnection();
		conn.setConnectTimeout(10000);
		conn.setReadTimeout(10000);
		String result = IOUtils.toString(conn.getInputStream() , "utf8");
		Document doc = Jsoup.parse(result);
		Elements rows = doc.select("#prices tr");
		
		float average5 = 0;
		float average10 = 0;
		float average15 = 0;
		float average20 = 0;
		float average30 = 0;
		float total = 0;
		float firstPrice = 0;
		for(int row=1;row<rows.size();row++) {
			if (row > 60) {
				break;
			}
			
			String day = rows.get(row).child(0).ownText();
			//TODO过滤掉停牌的
			try {
				Date date = DataHelper.sdf.parse(day);
				Calendar now = Calendar.getInstance();
				//两天前
				now.add(Calendar.DAY_OF_MONTH, -2);
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				if(cal.before(now)){
					System.out.println(code+","+name+"已停牌");
					continue;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			String close = rows.get(row).child(4).ownText();
			Float price = Float.valueOf(close);
			if(row==1){
				firstPrice=price; 
			}
			total += price;
			if (row == 5) {
				average5 = total / 5;
			}
			if (row == 10) {
				average10 = total / 10;
			}
			if (row == 20) {
				average20 = total / 20;
			}
			if (row == 30) {
				average30 = total / 30;
				if(firstPrice>price){
					//当前价格高于历史价格，处于上升期拐点,过滤
					System.out.println(code+","+name+"处于上升期拐点");
					return null;
				}
				break;
			}
		}
		float fc = cacuVariance2(average5 , average10 ,average20, average30);
		System.out.println(code+"("+name+")五日均价:" + average5 + ";十日均价为:" + average10 + ";二十日均价为:" + average20 + ";三十日均价为:" + average30+"方差="+fc);
		
		AverageFC afc = new AverageFC();
		afc.code = code;
		afc.name = name;
		afc.fangcha = fc;
		return afc;
	}

	
	private static float cacuVariance(Float... nums){
		float total = 0;
		//平方总值
		float pingfang = 0;
		for(float  num : nums){
			total+=num;
			pingfang+= num*num;
		}
		float average = total/nums.length;
		//平方的均值-均值的平方
		return pingfang/nums.length - average*average;
	}
	
	private static float cacuVariance2(Float... nums){
		float total = 0;
		float fangcha = 0;
		for(float  num : nums){
			total+=num;
		}
		float average = total/nums.length;
		for(float  num : nums){
			fangcha += (num-average)*(num-average);
		}
		//平方的均值-均值的平方
		fangcha= fangcha/nums.length;
		
		//方差除以均值的比例更有意义
		return fangcha/average;
	}
	
}
