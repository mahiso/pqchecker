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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.meddeb.bee.common.MsgProperties;
import net.meddeb.japptools.Serverconf;
import net.meddeb.udir.common.PQChannelMessages;
import net.meddeb.udir.common.SendStatus;
import net.meddeb.udir.common.shared.PQParamsDto;

import org.apache.log4j.Logger;


public class Messenger {
	private final static String DEFAULT_HOST = "localhost";
	private final static String DEFAULT_PORT = "61616";
	private final static String DEFAULT_LOGIN = "tomee";
	private final static String DEFAULT_PASSWORD = "tomee";
	private String senderID = "";
	private Logger logger = Logger.getLogger(this.getClass());
	private String topicName = "java:pwdQualityParams";
	private InitialContext jndiContext = null;
	private Topic topic = null;
	private ConnectionFactory connectionFactory = null;	
	private Connection connection = null;
	private Properties props =  null;
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
					JNIGateway gateway = new JNIGateway();
					String params = "";
					switch (PQChannelMessages.fromName(msgType)){
						case TEST_REQUEST:
							doSend(SendStatus.SUCCESS.toString(), PQChannelMessages.TEST_RESPONSE.toString());
							break;
						case WRITE_REQUEST:
							params = msg.getText();
							if (gateway.setParams(params.trim(), PQParamsDto.FORMAT)){
								doSend(SendStatus.SUCCESS.toString(), PQChannelMessages.WRITE_RESPONSE.toString());
							} else doSend(SendStatus.FAIL.toString(), PQChannelMessages.WRITE_RESPONSE.toString());
							break;
						case READ_REQUEST:
							params = gateway.getParams(PQParamsDto.FORMAT);
							if (params == null){
								doSend(SendStatus.FAIL.toString(), PQChannelMessages.READ_RESPONSE.toString());
							} else doSend(params.trim(), PQChannelMessages.READ_RESPONSE.toString());
							break;
						default: //when response message, do nothing
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
	
	public Messenger(Serverconf serverConf) {
		senderID = getHostname();
		connected = false;
		props = new Properties();
		String tcpUrl = "tcp://" + serverConf.getHost() + ":" + serverConf.getPort();
		String asUrl = "http://" + serverConf.getHost() + ":8080/tomee/ejb";
		System.setProperty("udmbConnectionFactory", 
				"connectionfactory:org.apache.activemq.ActiveMQConnectionFactory:" + tcpUrl);
		System.setProperty("pwdQualityParams", "topic:org.apache.activemq.command.ActiveMQTopic:pwdQualityParams");
		props.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
		props.put(Context.PROVIDER_URL, asUrl);
		props.put(Context.SECURITY_PRINCIPAL, serverConf.getLogin()); 
		props.put(Context.SECURITY_CREDENTIALS, serverConf.getPassword()); 
	}
	
	public Messenger() {
		senderID = getHostname();
		connected = false;
		props = new Properties();
		String tcpUrl = "tcp://" + DEFAULT_HOST + ":" + DEFAULT_PORT;
		String asUrl = "http://" + DEFAULT_HOST + ":8080/tomee/ejb";
		System.setProperty("udmbConnectionFactory", 
				"connectionfactory:org.apache.activemq.ActiveMQConnectionFactory:" + tcpUrl);
		System.setProperty("pwdQualityParams", "topic:org.apache.activemq.command.ActiveMQTopic:pwdQualityParams");
		props.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
		props.put(Context.PROVIDER_URL, asUrl);
		props.put(Context.SECURITY_PRINCIPAL, DEFAULT_LOGIN); 
		props.put(Context.SECURITY_CREDENTIALS, DEFAULT_PASSWORD); 
	}
	
	@SuppressWarnings("finally")
	public boolean initConnection(){
		connectionInitialized = false;
		logger.debug(LoggingMsg.getLog("initCnx"));
		try {
			jndiContext = new InitialContext(props);
			connectionFactory = (ConnectionFactory)jndiContext.lookup("java:udmbConnectionFactory");
			logger.debug(LoggingMsg.getLog("factoryInit"));
			connection = connectionFactory.createConnection();
			logger.debug(LoggingMsg.getLog("cnxCreate") + connection.getClientID());
			connection.setExceptionListener(new ExceptionListener() {
				@Override
				public void onException(JMSException e) {
					connected = false;
					connectionInitialized = false;
					logger.error(LoggingMsg.getLog("cnxLost"));
				}
			});
			topic = (Topic)jndiContext.lookup(topicName);
			logger.debug(LoggingMsg.getLog("topicFound") + topic.getTopicName());
			connectionInitialized = (connection != null) && (topic != null);
			if (connectionInitialized) logger.debug("cnxSuccess" + jndiContext.getNameInNamespace());
		} catch (NamingException e) {
			logger.error(LoggingMsg.getLog("cnxUnable"));
		} catch (JMSException e) {
			logger.error(LoggingMsg.getLog("cnxUnable"));
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
				MsgProperties.TYPE.toString() + " = '" + PQChannelMessages.READ_REQUEST.toString() + "' OR " + 
				MsgProperties.TYPE.toString() + " = '" + PQChannelMessages.WRITE_REQUEST.toString() + "' OR " +
				MsgProperties.TYPE.toString() + " = '" + PQChannelMessages.TEST_REQUEST.toString() + "') AND " +
				"(" + MsgProperties.SENDERID.toString() + " <> '" + senderID + "')";
		logger.debug(LoggingMsg.getLog("listenSel") + selectCondition);
		try {
			connection.start();
			session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
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
