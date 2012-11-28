/*
 * Copyright Adele Team LIG
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.liglab.adele.cilia.jms;

import java.util.Date;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.ISender;
import fr.liglab.adele.cilia.util.Const;


public class JMSSender implements ISender{

	private String stopic;

	private String login;

	private String password;

	private String hostname;

	private int port;

	private TopicConnection cnx;

	private TopicSession session;

	private TopicPublisher publisher;
	
	private Topic topic = null;
	
	private static final Logger log = LoggerFactory.getLogger(Const.LOGGER_APPLICATION);
	
	public void start() {
		
		try {
			// creates the connection
			cnx = CiliaJoramTool.createTopicConnection(login, password, hostname, port);
			// creates a session object
			session = cnx.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			// creates a topic object
			topic = session.createTopic(stopic);
			publisher = session.createPublisher(topic);
			cnx.start();
			log.debug("[JMSSender] started with topic:{}", stopic);
		} catch (JMSException e) {
			log.error("[JMSSender] Unable to start", e);
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			cnx.close();
		} catch (JMSException e) {
			log.error("[JMSSender] Unable to stop", e);
			e.printStackTrace();
		}

	}
	
	public boolean send(Data data) {
		MapMessage message;
		try {
			message = session.createMapMessage();
			Enumeration<String> keys = data.getAllData().keys();
			
			while (keys.hasMoreElements()) {
				String key = keys.nextElement().toString();
				Object object = data.getProperty(key);
				if (object instanceof String || object instanceof Number || object instanceof Boolean){
					message.setObject(key, object);
				} else if (object instanceof Date){
					Date dobj = (Date)object;
					message.setObject(key, dobj.getTime());
				}
			}
			log.trace("[JMSSender] Sending message {}", data);
			publisher.publish(message);
		} catch (JMSException e) {
			log.error("[JMSSender] unable to send message {}", data);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	
	/**
	 * @param stopic the stopic to set
	 */
	public void setTopic(String stopic) {
		this.stopic = stopic;
	}
	
	/**
	 * @param login the login to set
	 */
	public void setLogin(String login) {
		this.login = login;
	}
	
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
}
