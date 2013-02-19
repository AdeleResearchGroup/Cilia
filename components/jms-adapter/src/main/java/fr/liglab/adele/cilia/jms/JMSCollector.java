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

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractCollector;
import fr.liglab.adele.cilia.runtime.SerializedData;
import fr.liglab.adele.cilia.util.Const;

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
	
	private BundleContext bcontext;

	private static final Logger log = LoggerFactory.getLogger(Const.LOGGER_APPLICATION);

	public JMSCollector(BundleContext context){
		bcontext = context;
	}
	
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
			log.debug("[JMSCollector] started");
		} catch (JMSException e) {
			log.error("[JMSCollector] unable to start", e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void stop() {
		try {
			cnx.close();
		} catch (JMSException e) {
			log.error("[JMSCollector] Unable to stop", e);
			e.printStackTrace();
		}
	}




	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message msg) {
		log.trace("[JMSCollector] message arrive");
		Data ndata = null;
		Enumeration enume = null;
		boolean hasCiliaData = false;
		try {
			enume = msg.getPropertyNames();
			Object content = null;
			Hashtable dico = new Hashtable();
			String name = "jms-message";
			if (msg instanceof TextMessage) {
				content = ((TextMessage) msg).getText();
			} else if (msg instanceof ObjectMessage) {
				//Swap class loader
				ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
				ClassLoader thCL = this.getClass().getClassLoader();
				Thread.currentThread().setContextClassLoader(thCL);
				try {
					content = ((ObjectMessage) msg).getObject();
				} finally {
					Thread.currentThread().setContextClassLoader(oldCL);
				}
				if (content instanceof Data){
					hasCiliaData = true;
				}
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
			if (hasCiliaData){
				if (content instanceof SerializedData){
					SerializedData cdata = (SerializedData)content;
					ndata = cdata.deserializeContent(bcontext);
				} else {
					ndata = new Data((Data)content);
				}
			} else {
				ndata = new Data(content, name, dico);
			}

			log.trace("[JMSCollector] message content", ndata);
			notifyDataArrival(ndata);

		} catch (Exception e) {
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
	
	private void switchClassLoader(){
		
		
	}
}
