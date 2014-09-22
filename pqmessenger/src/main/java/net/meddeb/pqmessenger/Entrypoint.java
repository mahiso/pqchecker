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

import java.text.MessageFormat;

import org.apache.log4j.Logger;


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
        logger.info(Msg.getLog("pqMsgStart"));
        logger.info("------------------");
	    }
		}));
		logger =  Logger.getLogger(Entrypoint.class);
    logger.info("");
    logger.info("-------------------");
    logger.info(Msg.getLog("pqMsgStart"));
    logger.info("-------------------");
		try{
			while (true){
				logger.info(Msg.getLog("pqMsgTrycnx"));
				msgEngine.startConnection();
				if (msgEngine.isConnectionStarted()){
					connected = true;
				} else {
					String msg = Msg.getLog("pqMsgCnxFail");
					msg = MessageFormat.format(msg, msgEngine.getStrTimeretry());
					logger.info(msg);
				}
				while ((true)&&(msgEngine.isConnectionStarted())){
					try{
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						logger.error("e: " + e.getMessage());
					}
				}
				if (connected){
					logger.info(Msg.getLog("pqMsgdcnx"));
					connected = false;
				}
				Thread.sleep(msgEngine.getTimeRetry());
			}
		} catch (Exception e){
			logger.error(Msg.getLog("sysError") + e.getMessage());
		} finally {
			logger.info(Msg.getLog("stopListen"));
		}
	}

}
