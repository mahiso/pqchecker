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

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import net.meddeb.bee.common.MsgProperties;
import net.meddeb.japptools.common.JMSConfigDto;
import net.meddeb.md.common.ChannelID;
import net.meddeb.md.common.PQChannelMsg;
import net.meddeb.md.common.SendStatus;
import net.meddeb.md.common.TestChannelsMsg;
import net.meddeb.md.common.data.PQParamsDto;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;


public class Messenger {
	private final static String DEFAULT_HOST = "localhost";
	private final static String DEFAULT_PORT = "61616";
	private String senderID = "";
	private Logger logger = Logger.getLogger(this.getClass());
	private String topicName = "";
	private String listenUrl = "";
	private String user = "";
	private String password = "";
	private ConnectionFactory connectionFactory = null;	
	private Connection connection = null;
	private Topic topic = null;
	private boolean connected = false;
	private boolean connectionInitialized = false;
	//listen fields
	private Session session = null;
	private MessageConsumer consumer = null;
	
	@SuppressWarnings("finally")
	private String getHostname(){
		String rslt = "";
		try {
			rslt = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			rslt="";
		} finally{
			if (rslt == null) rslt = "";
			return rslt;
		}
	}
	
	private class ParamsListener implements MessageListener{
		public void onMessage(Message message) {
			logger.debug(LoggingMsg.getLog("receivMsg"));
			TextMessage msg = null;
			try {
				if (message instanceof TextMessage) {
					msg = (TextMessage) message;
					String msgType = msg.getStringProperty(MsgProperties.TYPE.toString());
					if ((msgType == null) || msgType.isEmpty()){
						logger.warn(LoggingMsg.getLog("msgNonUsable"));
						return;
					}
					logger.info(LoggingMsg.getLog("receivMsg") + msgType + "] " + msg.getText());
					String params = "";
					switch (PQChannelMsg.fromName(msgType)){
						case WRITE_REQUEST:
							params = msg.getText();
							if (JNIGateway.getInstance().setParams(params.trim(), PQParamsDto.FORMAT)){
								doSend(SendStatus.SUCCESS.toString(), PQChannelMsg.WRITE_RESPONSE.toString());
							} else doSend(SendStatus.FAIL.toString(), PQChannelMsg.WRITE_RESPONSE.toString());
							break;
						case READ_REQUEST:
							params = JNIGateway.getInstance().getParams(PQParamsDto.FORMAT);
							if (params == null){
								doSend(SendStatus.FAIL.toString(), PQChannelMsg.READ_RESPONSE.toString());
							} else doSend(params.trim(), PQChannelMsg.READ_RESPONSE.toString());
							break;
						default: //when response message, do nothing but test
							if (TestChannelsMsg.fromName(msgType) == TestChannelsMsg.TEST_REQUEST) {
								doSend(SendStatus.SUCCESS.toString(), TestChannelsMsg.TEST_RESPONSE.toString());
							}
							break;	
					}
				} else {
					logger.warn(LoggingMsg.getLog("typeNotsupp") + message.getClass().getName());
				}
			} catch (JMSException e) {
				logger.error(LoggingMsg.getLog("msgError") + e.toString());
			} catch (Exception e) {
				logger.error(LoggingMsg.getLog("sysError") + e.toString());
			}
		}
	}
	
	public Messenger(JMSConfigDto serverConf) {
		senderID = getHostname();
		connected = false;
		topicName = ChannelID.PQPARAMS.toString();
		listenUrl = "tcp://";
		if (serverConf.getHost().isEmpty()) listenUrl = listenUrl + DEFAULT_HOST;
		else listenUrl = listenUrl + serverConf.getHost();
		listenUrl = listenUrl + ":";
		if (serverConf.getPort().isEmpty()) listenUrl = listenUrl + DEFAULT_PORT;
		else listenUrl = listenUrl + serverConf.getPort();
		user = serverConf.getUser();
		password = serverConf.getPassword();
	}
	
	public Messenger() {
		senderID = getHostname();
		connected = false;
		topicName = ChannelID.PQPARAMS.toString();
		listenUrl = "tcp://" + DEFAULT_HOST + ":" + DEFAULT_PORT;
	}
	
	@SuppressWarnings("finally")
	public boolean initConnection(){
		connectionInitialized = false;
		logger.debug(LoggingMsg.getLog("initCnx"));
		try {
			connectionFactory = new ActiveMQConnectionFactory(listenUrl);
			logger.debug(LoggingMsg.getLog("factoryInit"));
			if ((!user.isEmpty()) && (!password.isEmpty())){
				connection = connectionFactory.createConnection(user, password);
			} else connection = connectionFactory.createConnection();
			logger.debug(LoggingMsg.getLog("cnxCreate") + connection);
			connection.setExceptionListener(new ExceptionListener() {
				@Override
				public void onException(JMSException e) {
					connected = false;
					connectionInitialized = false;
					logger.error(LoggingMsg.getLog("cnxLost"));
				}
			});
			connectionInitialized = (connection != null);
			if (connectionInitialized) logger.debug("cnxSuccess");
		} catch (Exception e) {
			logger.error(LoggingMsg.getLog("cnxUnable" + " - " + e.getMessage()));
		} finally{
			return connectionInitialized;
		}
	}
	
	@SuppressWarnings("finally")
	public boolean startConnection(){
		logger.debug(LoggingMsg.getLog("cnxStart"));
		connected = false;
		if (!connectionInitialized) return connected;
		String selectCondition = "(" + 
				MsgProperties.TYPE.toString() + " = '" + PQChannelMsg.READ_REQUEST.toString() + "' OR " + 
				MsgProperties.TYPE.toString() + " = '" + PQChannelMsg.WRITE_REQUEST.toString() + "' OR " +
				MsgProperties.TYPE.toString() + " = '" + TestChannelsMsg.TEST_REQUEST.toString() + "') AND " +
				"(" + MsgProperties.SENDERID.toString() + " <> '" + senderID + "')";
		logger.debug(LoggingMsg.getLog("listenSel") + selectCondition);
		try {
			connection.start();
			session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
			topic = session.createTopic(topicName);
			consumer = session.createConsumer(topic, selectCondition);
			consumer.setMessageListener(new ParamsListener());
			connected = true;
			logger.debug(LoggingMsg.getLog("cnxStartSucc"));
		} catch (JMSException e) {
			logger.error(LoggingMsg.getLog("cnxStartErr") + e.getMessage());
		} finally {
			return connected;
		}
		
	}
	
	public void stopConnection(){
		try{
			if ((connected) && (connection != null)) {
				logger.debug(LoggingMsg.getLog("cnxStop"));
				connection.stop();
				if (session != null) {
					session.close();
					session = null;
				}
				consumer = null;
				connected = false;
			}
		} catch (JMSException e) {
			logger.error("cnxStopErr" + e.getMessage());
		}
	}

	public void doSend(String message, String msgType){
		if ((connection == null) || (!connectionInitialized) || (session == null)) return;
		logger.debug(LoggingMsg.getLog("msgSend"));
		try{
			MessageProducer producer = session.createProducer(topic);
			TextMessage msg  = session.createTextMessage();
			msg.setStringProperty(MsgProperties.TYPE.toString(), msgType);
			msg.setStringProperty(MsgProperties.SENDERID.toString(), senderID);
			msg.setText(message);
			logger.info(LoggingMsg.getLog("sendMsg") + msgType + "] " + msg.getText());
			producer.send(msg);
			logger.debug(LoggingMsg.getLog("msgSent"));
		} catch (JMSException e){
			logger.error(e.getMessage());
		}
	}
	
	public boolean isConnected() {
		return connected;
	}

	public boolean isConnectionInitialized() {
		return connectionInitialized;
	}

}
