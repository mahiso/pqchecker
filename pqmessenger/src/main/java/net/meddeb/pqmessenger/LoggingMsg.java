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

import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

class LoggingMsg {
	private static Logger logger =  Logger.getLogger(LoggingMsg.class);
	private static final String LOG_PROPNAME = "net.meddeb.pqmessenger.pqmsglogmsg";
	private static final String OUT_PROPNAME = "net.meddeb.pqmessenger.pqmsgoutmsg";
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
	
	@SuppressWarnings("finally")
	private static String formatEncoding(String msg){
		String rslt = "";
		try {
			rslt = new String(msg.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		} finally {
			return rslt;
		}
	}
	
	public static String getLog(String key){
		String msg = getLogMessages().getString(key);
		return formatEncoding(msg);
	}
	public static String getOut(String key){
		String msg = getOutMessages().getString(key);
		return formatEncoding(msg);
	}
	private LoggingMsg(){
	}
}
