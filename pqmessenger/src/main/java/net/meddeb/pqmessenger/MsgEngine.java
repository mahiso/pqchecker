package net.meddeb.pqmessenger;
/*--------------------------------------------------------------------
pqMessenger, passwords quality settings messenger for pqChecker
Messaging middleware between pqChecker OpenLDAP plugin and a JMS 
compliant application.
Copyright (C) 2015, Abdelhamid MEDDEB (abdelhamid@meddeb.net)  

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
---------------------------------------------------------------------*/


import java.lang.reflect.Field;
import java.util.Hashtable;

import net.meddeb.japptools.JApptoolsPin;
import net.meddeb.japptools.common.JMSConfigDto;

import org.apache.log4j.Logger;

/**
 * Valid arguments:
 * 1/ --config-path						:	path to configuration files (log and conf), default /etc/ldap/pqchecker 
 * 2/ --config-file						:	configuration file name, default config.xml 
 * 3/ --config-log						:	logging configuration file name, default log4j.xml
 * 4/ --msg-server-id					: jms messaging server id used in configuration file, default PQMsgServer
 * 5/ --connection-retry-time	: time, in seconds, to retry when connection to messaging server fail or lost.
 */
public class MsgEngine {
	private final static String VERSION = "2.0.0-SNAPSHOT";

	private final static String CONFPATH_ARG_KEY = "--config-path";
	private final static String CONFFILE_ARG_KEY = "--config-file";
	private final static String CONFLOG_ARG_KEY = "--config-log";
	private final static String MSGSERVER_ARG_KEY = "--msg-server-id";
	private final static String CONNECTION_RETRY_TIME_ARG_KEY = "--connection-retry-time";
	
	private final static String CONFPARAM_NATIVELIBPATH = "nativelibpath";
	private final static String CONFPARAM_CNXRETRYTIME = "cnxretrytime";
	
	private final static String DEFAULT_CONFPATH = "/etc/ldap/pqchecker";
	private final static String DEFAULT_CONFFILE = "config.xml";
	private final static String DEFAULT_LOGFILE = "log4j.xml";
	private final static String DEFAULT_MSGSERVER_ID = "PQMsgServer";
	private final static int DEFAULT_RETRY_TIME = 1800; //30mn
	
	private JMSConfigDto msgServerConf = null;

	private Thread listener = null;
	private Logger logger = null;
	// Time to retry connection in seconds
	private int retryTime = -1; 
	
	private void printWelcomeMessage(){
		System.out.println("");
		System.out.println(LoggingMsg.getOut("sepLine"));
		System.out.println(LoggingMsg.getOut("pqmsgTitle") + VERSION);
		System.out.println("Copyright (C) 2015, Abdelhamid MEDDEB (abdelhamid@meddeb.net)");
		System.out.println(LoggingMsg.getOut("freeNoWarranty"));
		System.out.println(LoggingMsg.getOut("sepLine"));
		System.out.println("");
	}
	
	/**
	 * @param String[] args: of initialization arguments
	 * @param String argKey: argument key
	 * @return String argument value, empty is no argument found
	 */
	private String getArgValue(String[] args, String argKey){
		String rslt = "";
		if (args.length > 1){
			for (int i=0; i<args.length; i++){
				if ((args[i].equalsIgnoreCase(argKey)) && (i<(args.length-1))){
					rslt = args[i+1];
					break;
				}
			}
		}
		return rslt;
	}

	private void setRetryTime(String[] args, Hashtable<String, String> params){
		String strInt = "";
		if (params != null){
			strInt = params.get(CONFPARAM_CNXRETRYTIME);
			if (strInt == null) strInt = "";
		}
		if ((strInt.isEmpty())&&(args != null)){
			for (int i=0; i<args.length; i++){
				if ((args[i].equalsIgnoreCase(CONNECTION_RETRY_TIME_ARG_KEY)) && (i<(args.length-1))){
					strInt = args[i+1];
					if (strInt == null) strInt = "";
					break;
				}
			}
		}
		if (strInt.isEmpty()){
			retryTime = DEFAULT_RETRY_TIME;
		} else {	
			retryTime = Integer.parseInt(strInt);
		}
		if (retryTime <= 0) retryTime = DEFAULT_RETRY_TIME;
	}
	
	private void setNativelibraryPath(Hashtable<String, String> params){
		String strPath = "";
		if (params != null){
			strPath = params.get(CONFPARAM_NATIVELIBPATH);
			if (strPath == null) strPath = "";
		}
		if (!strPath.isEmpty()){
			System.setProperty("java.library.path", strPath);
			Field fieldSysPath;
			try {
				fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
				fieldSysPath.setAccessible(true);
				fieldSysPath.set(null, null);
			} catch (Exception e) {
				if (logger != null) {
					logger.error(e.getMessage());
				} else e.printStackTrace();
			}
		}
	}

	public MsgEngine(String[] args) {
		printWelcomeMessage();
		boolean confInitialized = false;		
		Hashtable<String, String> paramList = null;
		
		JApptoolsPin toolsPin = JApptoolsPin.getInstance();
		String configPath = getArgValue(args, CONFPATH_ARG_KEY);
		if (configPath.isEmpty()) configPath = DEFAULT_CONFPATH;
		String configFilename = getArgValue(args, CONFFILE_ARG_KEY);
		if (configFilename.isEmpty()) configFilename = DEFAULT_CONFFILE;
		String configLogfile = getArgValue(args, CONFLOG_ARG_KEY);
		if (configLogfile.isEmpty()) configLogfile = DEFAULT_LOGFILE;
		confInitialized = toolsPin.initConfig(configPath, configFilename, configLogfile);
		if (confInitialized) {
			logger =  Logger.getLogger(this.getClass());
			paramList = toolsPin.getParams();
			if (paramList == null) {
				if (logger != null) logger.warn(LoggingMsg.getLog("paramnotFound"));
				else System.out.println(LoggingMsg.getLog("paramnotFound")); 
			}
			setRetryTime(args, paramList);
			setNativelibraryPath(paramList);
			String serverID = getArgValue(args, MSGSERVER_ARG_KEY);
			if (serverID.isEmpty()) serverID = DEFAULT_MSGSERVER_ID;
			msgServerConf = toolsPin.getJMSConfig(serverID);
			if ((msgServerConf == null)&&(logger != null)) logger.warn(LoggingMsg.getLog("confnotFound"));
		} else {
			System.out.println(LoggingMsg.getOut("cantConfLog")); 
			System.out.println(LoggingMsg.getOut("confFilenotfound"));
		}
	}
	
	public void startConnection(){
		Messenger.getInstance().initConnection(msgServerConf);
		Messenger.getInstance().startConnection();
   	JNIGateway.getInstance().setCacheData(!Messenger.getInstance().isConnected());
		if ((logger != null)&&(Messenger.getInstance().isConnected())) {
			logger.info(LoggingMsg.getLog("pqMsgcnx"));
		}
	}
	
	public void stopConnection(){
		Messenger.getInstance().stopConnection();
   	JNIGateway.getInstance().setCacheData(!Messenger.getInstance().isConnected());
		if ((logger != null)&&(!Messenger.getInstance().isConnected()))	{
			logger.info(LoggingMsg.getLog("pqMsgdcnx"));
		}
	}
	
	/**
	 * Connection retry time in milliseconds
	 * @return time to retry cnx 
	 */
	public int getTimeRetry() {
		if (retryTime < 1){
			return DEFAULT_RETRY_TIME * 1000;
		} else return retryTime * 1000;
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

  public boolean doListen() {
    JNIGateway.getInstance().setCacheData(true);
	  listener = new Thread(new Listener());
	  listener.start();
	  return true;
  }

  public void stopListen() {
    JNIGateway.getInstance().stopListen();
  }

}
