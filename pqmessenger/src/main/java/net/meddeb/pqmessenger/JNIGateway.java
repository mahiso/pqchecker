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
import java.nio.ByteBuffer;

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
    logger.info("Data received from native");
    boolean dataCorrupted = false;
    if (data != null && data.length > 4) {
    	/*
    	byte[] length = new byte[4];
    	for (int i=0; i<4; i++) length[i] = data[i];
    	ByteBuffer buffer = ByteBuffer.wrap(length);
    	*/
      int dataLength = data[0];
      logger.info("Login length: " + dataLength);
      byte[] login = new byte[dataLength];
      for (int i=0; i <= dataLength; i++) {
      	if ((i+4)<data.length) login[i] = data[i+4];
      	else dataCorrupted = true;
      }
      if (!dataCorrupted) {
        String strLogin = "";
        String strPwd = "";
        /*
      	int offset = 4 + dataLength;
      	dataLength = 0;
      	while ((data.length>offset+dataLength) && (data[offset+dataLength] != '0')) {
      		dataLength++;
      	}
        byte[] pwd = new byte[dataLength];
        for (int i=offset; i <= (offset+dataLength); i++) {
        	pwd[i] = data[i+offset];
        }
        */
        try {
    			strLogin = new String(login, "UTF-8");
    			//strPwd = new String(pwd, "UTF-8");
    		} catch (UnsupportedEncodingException e) {
    			logger.error("Conversion error: " + e.getMessage());
    		}
        //logger.info("Login: " + strLogin + " - Pwd: " + strPwd);
        logger.info("Login: " + strLogin);
      }
    } else dataCorrupted = true;
    if (dataCorrupted) logger.info("Data corrupted");
    //System.out.println("Sent from native: " + user + " - " + pwd);
  }
	
}
