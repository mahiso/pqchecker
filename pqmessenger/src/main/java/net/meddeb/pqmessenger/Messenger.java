package net.meddeb.pqmessenger;
/*--------------------------------------------------------------------
ppMessenger, password policy messenger for ppInspect component
Messaging middleware between ppInspect OpenLDAP plugin and messaging 
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

import net.meddeb.japptools.Serverconf;
import net.meddeb.pqmsgshared.MsgProperties;
import net.meddeb.pqmsgshared.MsgStatus;
import net.meddeb.pqmsgshared.PQCRequest;
import net.meddeb.pqmsgshared.PQCResponse;
import net.meddeb.pqmsgshared.PQParams;

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
			logger.debug("Receives a message.");
			TextMessage msg = null;
			try {
				if (message instanceof TextMessage) {
					msg = (TextMessage) message;
					String msgType = msg.getStringProperty(MsgProperties.TYPE.toString());
					if ((msgType == null) || msgType.isEmpty()){
						logger.warn("Message not operable, No message type defined.");
						return;
					}
					logger.info("Message received: [" + msgType + "] " + msg.getText());
					JNIGateway gateway = new JNIGateway();
					String params = "";
					switch (PQCRequest.fromName(msgType)){
						case TEST:
							doSend(MsgStatus.SUCCESS.toString(), PQCResponse.TEST.toString());
							break;
						case WRITE:
							params = msg.getText();
							if (gateway.setParams(params.trim(), PQParams.FORMAT)){
								doSend(MsgStatus.SUCCESS.toString(), PQCResponse.WRITE.toString());
							} else doSend(MsgStatus.FAIL.toString(), PQCResponse.WRITE.toString());
							break;
						case READ:
							params = gateway.getParams(PQParams.FORMAT);
							if (params == null){
								doSend(MsgStatus.FAIL.toString(), PQCResponse.READ.toString());
							} else doSend(params.trim(), PQCResponse.READ.toString());
							break;
					}
				} else {
					logger.warn("The message type is not supported: " + message.getClass().getName());
				}
			} catch (JMSException e) {
				logger.error("JMS Error: " + e.toString());
			} catch (Exception e) {
				logger.error("Error: " + e.toString());
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
		logger.debug("Initializing connection..");
		try {
			jndiContext = new InitialContext(props);
			connectionFactory = (ConnectionFactory)jndiContext.lookup("java:udmbConnectionFactory");
			logger.debug("ConnectionFactory initialized.");
			connection = connectionFactory.createConnection();
			logger.debug("connection created: " + connection.getClientID());
			connection.setExceptionListener(new ExceptionListener() {
				@Override
				public void onException(JMSException e) {
					connected = false;
					connectionInitialized = false;
					logger.error("Connection lost.");
				}
			});
			topic = (Topic)jndiContext.lookup(topicName);
			logger.debug("Topic found: " + topic.getTopicName());
			connectionInitialized = (connection != null) && (topic != null);
			if (connectionInitialized) logger.debug("Connection successfully initialized: " + jndiContext.getNameInNamespace());
		} catch (NamingException e) {
			logger.error("Unable to initialize connection.");
		} catch (JMSException e) {
			logger.error("Unable to initialize connection.");
		} finally{
			return connectionInitialized;
		}
	}
	
	@SuppressWarnings("finally")
	public boolean startConnection(){
		logger.debug("Starting connection..");
		connected = false;
		if (!connectionInitialized) return connected;
		String selectCondition = "(" + 
				MsgProperties.TYPE.toString() + " = '" + PQCRequest.READ.toString() + "' OR " + 
				MsgProperties.TYPE.toString() + " = '" + PQCRequest.WRITE.toString() + "' OR " +
				MsgProperties.TYPE.toString() + " = '" + PQCRequest.TEST.toString() + "') AND " +
				"(" + MsgProperties.SENDERID.toString() + " <> '" + senderID + "')";
		logger.debug("Listen selector: " + selectCondition);
		try {
			connection.start();
			session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
			consumer = session.createConsumer(topic, selectCondition);
			consumer.setMessageListener(new ParamsListener());
			connected = true;
			logger.debug("Connection successfully started.");
		} catch (JMSException e) {
			logger.error("Error when starts connection: " + e.getMessage());
		} finally {
			return connected;
		}
		
	}
	
	public void stopConnection(){
		try{
			if ((connected) && (connection != null)) {
				logger.debug("Stopping connection..");
				connection.stop();
				if (session != null) {
					session.close();
					session = null;
				}
				consumer = null;
				connected = false;
			}
		} catch (JMSException e) {
			logger.error("Error when stops connection: " + e.getMessage());
		}
	}

	public void doSend(String message, String msgType){
		if ((connection == null) || (!connectionInitialized) || (session == null)) return;
		logger.debug("Sending a message.");
		try{
			MessageProducer producer = session.createProducer(topic);
			TextMessage msg  = session.createTextMessage();
			msg.setStringProperty(MsgProperties.TYPE.toString(), msgType);
			msg.setStringProperty(MsgProperties.SENDERID.toString(), senderID);
			msg.setText(message);
			logger.info("Sends message: [" + msgType + "] " + msg.getText());
			producer.send(msg);
			logger.debug("Message sent.");
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
