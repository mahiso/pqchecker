package net.meddeb.pqmessenger;

import java.util.ResourceBundle;

class Msg {
	private static final String LOG_PROPNAME = "pqmsglogmsg";
	private static final String OUT_PROPNAME = "pqmsgoutmsg";
	private static ResourceBundle logMessages = null;
	private static ResourceBundle outMessages = null;
	private static ResourceBundle getLogMessages(){
		if (logMessages == null) logMessages = ResourceBundle.getBundle(LOG_PROPNAME);
		return logMessages;
	}
	private static ResourceBundle getOutMessages(){
		if (outMessages == null) outMessages = ResourceBundle.getBundle(OUT_PROPNAME);
		return outMessages;
	}
	public static String getLog(String key){
		return getLogMessages().getString(key);
	}
	public static String getOut(String key){
		return getOutMessages().getString(key);
	}
	private Msg(){
	}
}
