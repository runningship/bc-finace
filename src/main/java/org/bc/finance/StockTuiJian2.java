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

public class StockTuiJian2 {

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
		 int index=0;
		for(Stock stock : page.getResult()){
			try{
				List<HistoryPrice> list = new ArrayList<HistoryPrice>();
				list.addAll(getHistoryBySession(stock.code , stock.name ,2));
				list.addAll(getHistoryBySession(stock.code , stock.name ,1));
				AverageFC afc = cacuVariance2(stock,list);
				if(afc!=null){
					result.add(afc);
				}
				index++;
				System.out.println(index);
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

	private static List<HistoryPrice> getHistoryBySession(String code , String name , int session) throws IOException {
		URL url = new URL("http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/"+code+".phtml?year=2015&jidu="+session);
		URLConnection conn = url.openConnection();
		conn.setConnectTimeout(10000);
		conn.setReadTimeout(10000);
		String result = IOUtils.toString(conn.getInputStream() , "gbk");
		Document doc = Jsoup.parse(result);
		Elements rows = doc.select("#FundHoldSharesTable tr");
		List<HistoryPrice> list = new ArrayList<HistoryPrice>();
		for(int row=2;row<rows.size();row++) {
			String day = rows.get(row).child(0).text();
			String open = rows.get(row).child(1).text();
			Float openPrice = Float.valueOf(open);
			String close = rows.get(row).child(3).text();
			Float closePrice = Float.valueOf(close);
			HistoryPrice hp = new HistoryPrice();
			hp.code = code;
			hp.name = name;
			hp.zhangfu = (closePrice-openPrice)/openPrice;
			list.add(hp);
		}
		return list;
	}

	
	private static AverageFC cacuVariance2(Stock stock,List<HistoryPrice> list){
		if(list.isEmpty()){
			System.out.println(stock.name+"停牌中...");
			return null;
		}
		float total = 0;
		float pfTotal = 0;
		float fangcha = 0;
		for(HistoryPrice hp : list){
			total+=hp.zhangfu;
			pfTotal = hp.zhangfu*hp.zhangfu;
		}
		float average = total/list.size();
		//平方的均值-均值的平方
		fangcha= pfTotal/list.size() - average*average;
		AverageFC afc = new AverageFC();
		afc.code = list.get(0).code;
		afc.name = list.get(0).name;
		afc.fangcha = fangcha;
		return afc;
		//方差除以均值的比例更有意义
	}
	
}
