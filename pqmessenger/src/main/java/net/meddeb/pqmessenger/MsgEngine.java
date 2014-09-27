package net.meddeb.pqmessenger;
/*--------------------------------------------------------------------
pqMessenger, passwords quality settings messenger for pqChecker
Messaging middleware between pqChecker OpenLDAP plugin and messaging 
service on TomEE+ applicaion server
Copyright (C) 2014, Abdelhamid MEDDEB (abdelhamid@meddeb.net)  

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
import net.meddeb.japptools.Serverconf;

import org.apache.log4j.Logger;

public class MsgEngine {
	private final static String VERSION = "1.2.0";
	private final static String DEFAULT_MSGSERVER_ID = "PQMsgServer";
	private final static String CONFFILE_ARG_KEY = "--config-file";
	private final static String MSGSERVER_ARG_KEY = "--msg-server-id";
	private final static String CNX_RETRY_TIME_ARG_KEY = "--connection-retry-time";
	private final static int DEFAULT_TIME_RETRY = 1800; //30mn
	private final static String PARAM_NATIVELIBPATH = "nativelibpath";
	private final static String PARAM_CNXRETRYTIME = "cnxretrytime";
	private Messenger messenger = null;
	private Logger logger = null;
	// Time to retry connection in seconds
	private int timeRetry = -1; 
	
	private void printWelcomeMessage(){
		System.out.println("");
		System.out.println(Msg.getOut("sepLine"));
		System.out.println(Msg.getOut("pqmsgTitle") + VERSION);
		System.out.println("Copyright (C) 2014, Abdelhamid MEDDEB (abdelhamid@meddeb.net)");
		System.out.println(Msg.getOut("freeNoWarranty"));
		System.out.println(Msg.getOut("sepLine"));
		System.out.println("");
	}
	
	/**
	 * Arguments supported:
	 * 1/ --config-file						:	May be path to configuration files (log and conf) or full qualified 
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
		String configPath = "";
		if (args.length > 1){
			for (int i=0; i<args.length; i++){
				if ((args[i].equalsIgnoreCase(CONFFILE_ARG_KEY)) && (i<(args.length-1))){
					configPath = args[i+1];
					break;
				}
			}
		}
		return configPath;
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
	
	private void setNativelibraryPath(String strPath){
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

	public MsgEngine(String[] args) {
		printWelcomeMessage();
		boolean confInitialized = false;		
		Hashtable<String, String> paramList = null;
		JApptoolsPin toolsPin = JApptoolsPin.getInstance();
		String configFilename = getConfigFilename(args);
		initTimeRetry(args);
		if (!configFilename.isEmpty()) confInitialized = toolsPin.initConfig(configFilename);
		if (!confInitialized) {
			confInitialized = toolsPin.initConfig();
		}
		if (toolsPin.initLog()) {
			logger =  Logger.getLogger(this.getClass());
		} else System.out.println(Msg.getOut("cantConfLog"));
		if (confInitialized) {
			String serverID = getConfigServerID(args);
			Serverconf msgServerConf = toolsPin.getServerconf(serverID);
			if (msgServerConf == null) {
				if (logger != null) logger.warn(Msg.getLog("confnotFound"));
				messenger = new Messenger();
			} else	messenger = new Messenger(msgServerConf);
			paramList = toolsPin.getParams();
			if (paramList == null) {
				if (logger != null) logger.warn(Msg.getLog("paramnotFound"));
			} else {	
				String param = paramList.get(PARAM_NATIVELIBPATH);
				if (!param.isEmpty()) setNativelibraryPath(param);
				param = paramList.get(PARAM_CNXRETRYTIME);
				if (!param.isEmpty()) timeRetry = Integer.parseInt(param);
			}
		} else {
			messenger = new Messenger();
			System.out.println(Msg.getOut("confFilenotfound"));
		}
	}
	
	public void startConnection(){
		if (!messenger.isConnectionInitialized()) messenger.initConnection();
		if ((messenger.isConnectionInitialized()) && (!messenger.isConnected())) {
			messenger.startConnection();
			if ((logger != null)&&(messenger.isConnected())) logger.info(Msg.getLog("pqMsgcnx"));
		}
	}
	
	public void stopConnection(){
		if (messenger.isConnected()) {
			messenger.stopConnection();
			if ((logger != null)&&(!messenger.isConnected()))	logger.info(Msg.getLog("pqMsgdcnx"));
		}
	}
	
	public boolean isConnectionStarted(){
		return messenger.isConnected();
	}
	/**
	 * Connection retry time in milliseconds
	 * @return time to retry cnx 
	 */
	public int getTimeRetry() {
		if (timeRetry < 1){
			return DEFAULT_TIME_RETRY * 1000;
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
