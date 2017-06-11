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
import java.nio.ByteOrder;
import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.meddeb.md.common.PQChannelMsg;

public class Listener implements Runnable {
	
	Logger logger =  LogManager.getLogger(this.getClass());
	
	public native boolean doListen();

	@Override
	public void run() {
		doListen();
	}
	

  public void sendData(byte[] data) {
    if (logger == null) logger =  LogManager.getLogger(this.getClass());
    boolean dataCorrupted = false;
    if (data != null && data.length > 4) {
      logger.debug(LoggingMsg.getLog("pwdModNotif"));
    	byte[] lengthByte = new byte[4];
    	for (int i=0; i<4; i++) {
    		lengthByte[i] = data[i];
    	}
    	ByteBuffer buffer = ByteBuffer.wrap(lengthByte);
    	buffer.order(ByteOrder.LITTLE_ENDIAN);
      int dataLength = buffer.getShort();
      byte[] login = new byte[dataLength];
      for (int i=0; i < dataLength; i++) {
      	if ((i+4)<data.length) login[i] = data[i+4];
      	else dataCorrupted = true;
      }
      if (!dataCorrupted) {
        String strUsr = "";
        String strPwd = "";
      	int offset = 4 + dataLength;
      	dataLength = 0;
      	while ((data.length>offset+dataLength) && ((data[offset+dataLength] & 0xFF) != 0x00)) {
      		dataLength++;
      	}
        byte[] pwd = new byte[dataLength];
        for (int i=0; i < dataLength; i++) {
        	pwd[i] = data[i+offset];
        }
        try {
    			strUsr = new String(login, "UTF-8");
          logger.info(MessageFormat.format(LoggingMsg.getLog("pwdNotifRec"), strUsr));
    			strPwd = new String(pwd, "UTF-8");
          String msg = "{ \"user\": \"" + strUsr + "\", \"pwd\": \"" + strPwd + "\" }";
          Messenger.getInstance().doSend(msg, PQChannelMsg.BROADCAST_PWD.toString());
    		} catch (UnsupportedEncodingException e) {
    			logger.error(LoggingMsg.getLog("dataConvErr") + e.getMessage());
    		}
      }
    } else logger.error(LoggingMsg.getLog("corrDataRec"));
  }
	
}

