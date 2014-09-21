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
        logger.info("pqMessenger stops.");
        logger.info("------------------");
	    }
		}));
		logger =  Logger.getLogger(Entrypoint.class);
    logger.info("");
    logger.info("-------------------");
    logger.info("pqMessenger starts.");
    logger.info("-------------------");
		try{
			while (true){
				logger.info("Try connection to messaging server...");
				msgEngine.startConnection();
				if (msgEngine.isConnectionStarted()){
					connected = true;
				} else logger.info("Connection failed, wait for " + msgEngine.getStrTimeretry() + 
						                " to retry..");
				while ((true)&&(msgEngine.isConnectionStarted())){
					try{
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						logger.error("e: " + e.getMessage());
					}
				}
				if (connected){
					logger.info("-- d i s c o n n e c t e d ---[ o ]---");
					connected = false;
				}
				Thread.sleep(msgEngine.getTimeRetry());
			}
		} catch (Exception e){
			logger.error("JMS error: " + e.getMessage());
		} finally {
			logger.info("Stops the listening.");
		}
	}

}
