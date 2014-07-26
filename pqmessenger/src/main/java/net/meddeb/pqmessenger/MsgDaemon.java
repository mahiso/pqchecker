package net.meddeb.pqmessenger;

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
        logger.info("pqMessenger starts.");
        logger.info("-------------------");
        while(!stopped){
  				logger.info("Try connection to messaging server...");
  				msgEngine.startConnection();
  				if (msgEngine.isConnectionStarted()){
  					connected = true;
  				} else logger.info("Connection failed, wait before retry..");
  				while ((true)&&(msgEngine.isConnectionStarted())){
  					try{
  						Thread.sleep(500);
  					} catch (InterruptedException e) {
  						logger.error("e: " + e.getMessage());
  					}
  				}
  				if (connected){
  					logger.info("-- d i s c o n n e c t e d ---[ o ]---");
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
      logger.info("------------------------");
      logger.info("pqMessenger shutdown.");
      logger.info("------------------------");
    }catch(InterruptedException e){
      System.err.println(e.getMessage());
      throw e;
    }	
  }

}
