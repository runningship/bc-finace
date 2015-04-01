package org.bc.finance;

import java.util.HashMap;
import java.util.Map;

import org.bc.sdak.MutilSessionFactoryBuilder;
import org.bc.sdak.SQL2008Dialect;
import org.bc.sdak.SessionFactoryBuilder;
import org.bc.sdak.SessionFactoryMapper;
import org.hibernate.cfg.AvailableSettings;

public class StartUpListener{

	

	public static synchronized void initDataSource(){
		Map<String,String> settings = new HashMap<String , String>();
//		settings.put(AvailableSettings.URL, "jdbc:mysql://localhost:3306/ihouse?characterEncoding=utf-8");
//		settings.put(AvailableSettings.USER, "root");
//		settings.put(AvailableSettings.PASS, "");
		settings.put(AvailableSettings.SHOW_SQL, "false");
//		settings.put(AvailableSettings.DRIVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
//		settings.put(AvailableSettings.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
		
//		settings.put(AvailableSettings.DIALECT, "org.hibernate.dialect.SQLServer2008Dialect");
		settings.put(AvailableSettings.DIALECT, SQL2008Dialect.class.getName());
		
//		settings.put(AvailableSettings.DRIVER, "com.mysql.jdbc.Driver");
//		settings.put(AvailableSettings.DIALECT, "org.hibernate.dialect.MySQL5Dialect");
		
		settings.put(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");
//		settings.put(AvailableSettings.HBM2DDL_AUTO, "update");
		settings.put(AvailableSettings.POOL_SIZE, "1");
		settings.put(AvailableSettings.CACHE_REGION_FACTORY, "org.hibernate.cache.ehcache.EhCacheRegionFactory");
		settings.put(AvailableSettings.USE_SECOND_LEVEL_CACHE, "true");
		
//		settings.put(AvailableSettings.PROXOOL_XML, "proxool.xml");//相对目录为classes
		settings.put(AvailableSettings.PROXOOL_XML, "proxool.xml");//相对目录为classes
		settings.put(AvailableSettings.PROXOOL_EXISTING_POOL, "false");
		settings.put(AvailableSettings.PROXOOL_POOL_ALIAS, "stock");
		SessionFactoryBuilder.applySettings(settings);
		settings.put("annotated.packages", "org.bc.finance");
	}
}
