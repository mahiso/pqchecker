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

import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Entrypoint {
	private static boolean connected = false;
	private static MsgEngine msgEngine = null;
	
	private static Logger logger = null;

	public static void main(String[] args) {
		connected = false;
		msgEngine = new MsgEngine(args);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	    public void run()
	    {
	    	if (connected) {
	    		msgEngine.stopConnection();
					connected = false;
	    	}
        logger.info("------------------");
        logger.info(LoggingMsg.getLog("pqMsgStart"));
        logger.info("------------------");
	    }
		}));
		logger =  LogManager.getLogger(Entrypoint.class);
    logger.info("");
    logger.info("-------------------");
    logger.info(LoggingMsg.getLog("pqMsgStart"));
    logger.info("-------------------");
		try{
			while (true){
				logger.info(LoggingMsg.getLog("pqMsgTrycnx"));
				msgEngine.startConnection();
				if (Messenger.getInstance().isConnected()){
					connected = true;
				} else {
					String msg = LoggingMsg.getLog("pqMsgCnxFail");
					msg = MessageFormat.format(msg, msgEngine.getStrTimeretry());
					logger.info(msg);
				}
				while ((true)&&(Messenger.getInstance().isConnected())){
					try{
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						logger.error("e: " + e.getMessage());
					}
				}
				if (connected){
					logger.info(LoggingMsg.getLog("pqMsgdcnx"));
					connected = false;
				}
				Thread.sleep(msgEngine.getTimeRetry());
			}
		} catch (Exception e){
			logger.error(LoggingMsg.getLog("sysError") + e.getMessage());
		} finally {
			logger.info(LoggingMsg.getLog("stopListen"));
		}
	}

}
