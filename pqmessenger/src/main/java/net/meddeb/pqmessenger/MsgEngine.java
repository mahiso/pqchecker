package net.meddeb.pqmessenger;

import java.io.File;

import net.meddeb.japptools.JApptoolsPin;
import net.meddeb.japptools.Serverconf;

import org.apache.log4j.Logger;

public class MsgEngine {
	private final static String VERSION = "1.1.0";
	private final static String DEFAULT_MSGSERVER_ID = "PQMsgServer";
	private final static String CONFFILE_ARG_KEY = "--config-file";
	private final static String MSGSERVER_ARG_KEY = "--msg-server-id";
	private final static String CNX_RETRY_TIME_ARG_KEY = "--connection-retry-time";
	private final static String DEFAULT_CONFFILENAME = "config.xml";
	private final static int DEFAULT_TIME_RETRY = 900; //15mn
	private Messenger messenger = null;
	private Logger logger = null;
	
	private int timeRetry = -1; 
	
	private void printWelcomeMessage(){
		System.out.println("");
		System.out.println("---------------------------------------------------------------------");
		System.out.println("pqMessenger, Password quality messenger for pqchecker - Version " + VERSION);
		System.out.println("Copyright (C) 2014, Abdelhamid MEDDEB (abdelhamid@meddeb.net)");
    System.out.println("This program is free software and comes with ABSOLUTELY NO WARRANTY.");
		System.out.println("---------------------------------------------------------------------");
		System.out.println("");
	}
	
	/**
	 * Arguments supported:
	 * 1/ --config-file						:	Can be path to configuration files (log and conf) or full qualified 
	 * 															configuration file name. Default configuration file name is 
	 * 															"config.xml". Log configuration file name must be log4j.xml
	 * 															both configuration files must be in the same directory.
	 * 2/ --msg-server-id					: jms messaging server id used in configuration file. 
	 * 															default value: "PQMsgServer"
	 * 3/ --connection-retry-time	: time, in secend, to retry when connection to msg server fail.
	 * 
	 * @param args list of arguments and its values
	 * @return full qualified configuration file name
	 */
	private String getConfigFilename(String[] args){
		String rslt = "";
		String configPath = "";
		if (args.length > 1){
			for (int i=0; i<args.length; i++){
				if ((args[i].equalsIgnoreCase(CONFFILE_ARG_KEY)) && (i<(args.length-1))){
					configPath = args[i+1];
					break;
				}
			}
		}
		if (!configPath.isEmpty()){
			File f = new File(configPath);
			String sep = System.getProperty("file.separator");
			if ((f.exists()) && (f.isFile())){
				rslt = configPath.substring(configPath.lastIndexOf(sep)+1,configPath.length());
				configPath = configPath.substring(0,configPath.lastIndexOf(sep)+1);
			} else
			if ((f.exists()) && (f.isDirectory())){
				if (configPath.lastIndexOf(sep) != (configPath.length()-1)){
					configPath = configPath + sep;
				}
				rslt = configPath;// + DEFAULT_CONFFILENAME;
			}
		}
		if (rslt.isEmpty()) rslt = DEFAULT_CONFFILENAME;
		return rslt;
	}
	
	private String getConfigServerID(String[] args){
		String rslt = "";
		if (args.length > 1){
			for (int i=0; i<args.length; i++){
				if ((args[i].equalsIgnoreCase(MSGSERVER_ARG_KEY)) && (i<(args.length-1))){
					rslt = args[i+1];
					break;
				}
			}
		}
		if (rslt.isEmpty()) rslt = DEFAULT_MSGSERVER_ID;
		return rslt;
	}
	
	private void initTimeRetry(String[] args){
		timeRetry = -1;
		String strInt = "";
		if (args.length > 1){
			for (int i=0; i<args.length; i++){
				if ((args[i].equalsIgnoreCase(CNX_RETRY_TIME_ARG_KEY)) && (i<(args.length-1))){
					strInt = args[i+1];
					if (strInt.isEmpty()){
						timeRetry = DEFAULT_TIME_RETRY;
					} else {	
						timeRetry = Integer.parseInt(strInt);
					}
					break;
				}
			}
		}
		if (timeRetry <= 0) timeRetry = DEFAULT_TIME_RETRY;
	}

	public MsgEngine(String[] args) {
		printWelcomeMessage();
		boolean confInitialized = false;		
		JApptoolsPin toolsPin = JApptoolsPin.getInstance();
		String configFilename = getConfigFilename(args);
		initTimeRetry(args);
		if (!configFilename.isEmpty()) confInitialized = toolsPin.initConfig(configFilename);
		if (!confInitialized) {
			confInitialized = toolsPin.initConfig();
		}
		if (toolsPin.initLog()) {
			logger =  Logger.getLogger(this.getClass());
		} else System.out.println("Cannot configure logging system.");
		if (confInitialized) {
			String serverID = getConfigServerID(args);
			Serverconf msgServerConf = toolsPin.getServerconf(serverID);
			if (msgServerConf == null) {
				if (logger != null) logger.warn("Configuration for messaging server not found, uses default settings.");
				messenger = new Messenger();
			} else	messenger = new Messenger(msgServerConf);
			
		} else {
			messenger = new Messenger();
			System.out.println("No configuration file found, uses default settings.");
		}
	}
	
	public void startConnection(){
		if (!messenger.isConnectionInitialized()) messenger.initConnection();
		if ((messenger.isConnectionInitialized()) && (!messenger.isConnected())) {
			messenger.startConnection();
			if ((logger != null)&&(messenger.isConnected())) logger.info("-- c o n n e c t e d ---<o>---");
		}
	}
	
	public void stopConnection(){
		if (messenger.isConnected()) {
			messenger.stopConnection();
			if ((logger != null)&&(!messenger.isConnected()))	logger.info("-- d i s c o n n e c t e d ---[ o ]---");
		}
	}
	
	public boolean isConnectionStarted(){
		return messenger.isConnected();
	}

	public int getTimeRetry() {
		if (timeRetry < 1){
			return 1000;
		} else return timeRetry * 1000;
	}
	/**
	 * Return human readable retry time
	 * @return String, readable time
	 */
	public String getStrTimeretry() {
		int t = getTimeRetry() / 1000;
		String rslt = "";
		if (t < 60) {
			rslt = Integer.toString(t) + " s";
		} else if (t < 3600) {
			int s = t % 60;
			t = t / 60;
			rslt = Integer.toString(t) + " mn";
			if (s > 0) rslt = rslt + ", " + Integer.toString(s) + " s";
		} else {
			int s = t % 3600;
			t = t / 3600;
			s = s % 60;
			int m = s / 60;
			rslt = Integer.toString(t) + " h";
			if (m > 0) rslt = rslt + ", " + Integer.toString(m) + " mn";
			if (s > 0) rslt = rslt + ", " + Integer.toString(s) + " s";
		}
		return rslt;
	}

}
