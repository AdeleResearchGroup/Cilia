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

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.ISender;
import fr.liglab.adele.cilia.runtime.SerializedData;
import fr.liglab.adele.cilia.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;


public class JMSSender implements ISender {

    private String stopic;

    private String login;

    private String password;

    private String hostname;

    private int port;

    private Option option;

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
        boolean returnValue = true;
        Message message = null;
        /**TODO:
         * Send the Data with the content as an byte array.
         * Send the also the classname
         * In the collector, locate the classloader containing the classname, and recreate it with the byte array
         * The classloader will be localizated using BundleCapabilities. The attribute is the package
         * Attribute name osgi.wiring.package
         */
        switch (option) {
            case map_elements:
                message = prepareMapElements(data);
                break;

            case only_content:
                message = prepareOnlyContent(data);
                break;

            case serialized_data:
                message = prepareSerializedData(data);
                break;
        }
        log.trace("[JMSSender] Sending message {}", data);
        try {
            if (message == null) {
                log.error("[JMSSender] unable to message, somme error happend when creating JMS message with " + data);
            } else {
                publisher.publish(message);
            }
        } catch (JMSException e) {
            log.error("[JMSSender] unable to send message {}", data);
            e.printStackTrace();
            returnValue = false;
        }
        return returnValue;
    }

    private Message prepareMapElements(Data data) {
        MapMessage message;
        try {
            message = session.createMapMessage();
            Enumeration<String> keys = data.getAllData().keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                Object object = data.getProperty(key);
                if (object instanceof String || object instanceof Number || object instanceof Boolean) {
                    message.setObject(key, object);
                } else if (object instanceof Date) {
                    Date dobj = (Date) object;
                    message.setObject(key, dobj.getTime());
                }
            }
            return message;

        } catch (JMSException e) {
            log.error("[JMSSender] unable to prepare map message {}", data);
            e.printStackTrace();
            return null;
        }
    }

    private Message prepareOnlyContent(Data data) {
        ObjectMessage message = null;
        try {
            message = session.createObjectMessage();
            Object object = data.getContent();
            if (object instanceof Serializable) {
                Serializable sobject = (Serializable) object;
                message.setObject(sobject);
            } else {
                log.error("[JMSSender] unable to serialize content data {}", data.getContent());
                return null;
            }
            return message;

        } catch (JMSException e) {
            log.error("[JMSSender] unable to prepare data content{}", data);
            e.printStackTrace();
            return null;
        }
    }

    private Message prepareSerializedData(Data data) {
        ObjectMessage message = null;

        try {
            SerializedData ndata = new SerializedData(data);
            ndata.serializeContent();
            message = session.createObjectMessage();
            message.setObject(ndata);
            return message;
        } catch (Exception e) {
            log.error("[JMSSender] unable to prepare data {}", data);
            e.printStackTrace();
            return null;
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

    public void setMessageOption(String message) {
        if (message.compareToIgnoreCase(Option.map_elements.name()) == 0) {
            option = Option.map_elements;
        } else if (message.compareToIgnoreCase(Option.only_content.name()) == 0) {
            option = Option.only_content;
        } else {
            option = Option.serialized_data;
        }
    }

}
