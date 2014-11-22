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

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.log4j.Logger;

public class MsgDaemon implements Daemon {
	private Thread mainThread; 
  private boolean stopped = false;
	private boolean connected = false;
	private MsgEngine msgEngine = null;
	private Logger logger = null;
	public MsgDaemon() {
	}

	@Override
	public void destroy() {
		mainThread = null;
	}

	@Override
	public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
		String[] args = daemonContext.getArguments(); 
		connected = false;
		msgEngine = new MsgEngine(args);
		logger =  Logger.getLogger(this.getClass());
    mainThread = new Thread(){
      @Override
      public synchronized void start() {
        MsgDaemon.this.stopped = false;
        super.start();
      }
      @Override
      public void run() {            
        logger.info("");
        logger.info("-------------------");
        logger.info(LoggingMsg.getLog("pqMsgStart"));
        logger.info("-------------------");
        while(!stopped){
  				logger.info(LoggingMsg.getLog("pqMsgTrycnx"));
  				msgEngine.startConnection();
  				if (msgEngine.isConnectionStarted()){
  					connected = true;
  				} else {
  					String msg = LoggingMsg.getLog("pqMsgCnxFail");
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
  					logger.info(LoggingMsg.getLog("pqMsgdcnx"));
  					connected = false;
  				}
  				try {
						Thread.sleep(msgEngine.getTimeRetry());
					} catch (InterruptedException e) {
						logger.error("e: " + e.getMessage());
					} //wait 
  			}
      }
    };

	}

	@Override
	public void start() throws Exception {
		mainThread.start();
	}

	@Override
	public void stop() throws Exception {
		stopped = true;
    try{
    	mainThread.join(1000);
    	if (connected) {
    		msgEngine.stopConnection();
				connected = false;
    	}
      logger.info("------------------");
      logger.info(LoggingMsg.getLog("pqMsgStop"));
      logger.info("------------------");
    }catch(InterruptedException e){
      System.err.println(e.getMessage());
      throw e;
    }	
  }

}
