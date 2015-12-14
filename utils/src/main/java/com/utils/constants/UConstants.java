package com.utils.constants;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

public class UConstants {
	private static UConstants ms_instance;
	final public static String ERR001 = "e1. Bad query";
	final public static String ERR002 = "e2. Bad charachters in the query";
	final public static String ERR003 = "e3. General exception in ES first retrieval";
	final public static String ERR004 = "e4. Was not able to re-rank";
	final public static String CLUSTER_NAME = "prod";
	final public static String INDEX_NAME = "full";
	final public static String HOST_NAME = "localhost";// change for localhost to run it on local
	final public static String HOST_NAME_RABBIT = "localhost";
	final public static String USER_RABBIT = "guest";
	final public static String PASS_RABBIT = "guest";
	final public static String PATH = System.getenv("HOME") + "/Downloads/data_sql/";
	final public static String JEDIS_HOST="localhost";
	public static final Logger log = Logger.getLogger("");
	public static final long MAX_WAIT_KILL_THREAD=10000;
	public UConstants() {
		//Define log pattern layout
		 try {
			BasicConfigurator.configure();
			PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");
			RollingFileAppender fileAppender = new RollingFileAppender(layout, "log4j.log");
			//Add console appender to root logger
			log.addAppender(new ConsoleAppender(layout));
			log.addAppender(fileAppender);
			Logger.getRootLogger().setLevel(Level.ERROR);
		 } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}

	public static final UConstants getInstance() {
		if (ms_instance == null) {
			synchronized (UConstants.class) {
				if (ms_instance == null) {
					ms_instance = new UConstants();
				}
			}
		}
		return ms_instance;
	}

	/**
	 * This method returns the Search engine hostname.
	 * 
	 * @return
	 */
	public static String getHostName() {
		return HOST_NAME;
	}

	/**
	 * This method returns the cluster name.
	 * 
	 * @return
	 */
	public static String getClusterName() {
		return CLUSTER_NAME;
	}

	public static String getIndexName() {
		return INDEX_NAME;
	}

}
