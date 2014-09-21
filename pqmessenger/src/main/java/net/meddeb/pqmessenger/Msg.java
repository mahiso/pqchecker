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
