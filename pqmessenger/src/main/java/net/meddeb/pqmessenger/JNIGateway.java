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

import org.apache.log4j.Logger;

public class JNIGateway {
	static {
		System.loadLibrary("pqchecker");
	}
  private static JNIGateway instance = null;
	
	private JNIGateway() {
	}

  private Logger logger =  Logger.getLogger(this.getClass());
  
  public static JNIGateway getInstance() {
    if (instance == null) instance = new JNIGateway();
    return instance;
  }

	public native String getParams(String fmt);
	
	public native boolean setParams(String params, String fmt);

	public native void setCacheData(boolean cacheData);

	public native void stopListen();

  public void sendData(byte[] data) {
    if (logger == null) logger =  Logger.getLogger(this.getClass());
    logger.info("Sent from native");
    //System.out.println("Sent from native: " + user + " - " + pwd);
  }
	
}
