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
import java.util.Enumeration;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractCollector;

/**
 * A JMS sender publishing messages to a given topic. 
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class JMSCollector extends AbstractCollector implements MessageListener {

	private String stopic;

	private String hostname;

	private int port;

	private String login;

	private String password;

	private static TopicConnectionFactory joramTopicConnectionFactory = null;
	private TopicConnection cnx = null;
	private TopicSubscriber subscriber = null;


	private void start() {
		
		try {
			cnx = CiliaJoramTool.createTopicConnection(login, password, hostname, port);
			TopicSession tss = cnx.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);
			// creates a topic object
			Topic topic = tss.createTopic(stopic);
			// creates a topic subscriber

			subscriber = tss.createSubscriber(topic);
			subscriber.setMessageListener(this);
			cnx.start();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void stop() {
		try {
			cnx.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}




	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message msg) {
		
		Enumeration enume = null;
		try {
			enume = msg.getPropertyNames();
			Object content = null;
			Hashtable dico = new Hashtable();
			String name = "jms-message";
			if (msg instanceof TextMessage) {
				content = ((TextMessage) msg).getText();
			} else if (msg instanceof ObjectMessage) {
				content = ((ObjectMessage) msg).getObject();
			} else if (msg instanceof MapMessage) {
				MapMessage message = (MapMessage)msg;
				enume = message.getMapNames();
				if (message.getObject(Data.DATA_CONTENT) != null) {
					content = message.getObject(Data.DATA_CONTENT);
				}
				if (message.getString(Data.DATA_NAME) != null){
					name = message.getString(Data.DATA_NAME);
				}
				while (enume.hasMoreElements()){
					String propname = (String)enume.nextElement();
					Object ob = ((MapMessage) msg).getObject(propname);
					dico.put(propname, ob);
				}
			}
			Data ndata = new Data(content, name, dico);
			notifyDataArrival(ndata);
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
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
