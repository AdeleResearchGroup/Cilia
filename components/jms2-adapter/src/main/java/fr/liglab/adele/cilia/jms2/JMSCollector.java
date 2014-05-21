/*
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
package fr.liglab.adele.cilia.jms2;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractCollector;
import fr.liglab.adele.cilia.runtime.SerializedData;
import fr.liglab.adele.cilia.util.Const;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * JMS/Joram in adapter based on new JMS 2.0 specification.
 * <p/>
 * This JMS collector works either in Point-To-Point (PTP) or Publish/Subscribe (P/S)
 * mode messages depending of the destination type (Queue or Topic) used to receive
 * messages. The destination type could be specified in the name of the destination, by
 * default it is a topic.
 *
 * @author ScalAgent Team
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class JMSCollector extends AbstractCollector implements MessageListener {
    /**
     * Destination name and type used to send messages.
     */
    private String dname;
    /**
     * Joram user's name.
     */
    private String login;
    /**
     * Password of Joram user.
     */
    private String password;
    /**
     * Name or IP address of the Joram server's host.
     */
    private String hostname;
    /**
     * Joram server's listening port.
     */
    private int port;

    /**
     * JMS context needed to control messages sending.
     */
    private JMSContext jctx = null;
    /**
     * Message consumer used to receive messages.
     */
    private JMSConsumer consumer = null;

    private BundleContext bcontext;

    private static final Logger log = LoggerFactory.getLogger(Const.LOGGER_APPLICATION);

    public JMSCollector(BundleContext context) {
        bcontext = context;
    }

    private void start() {
        try {
            // creates the connection
            jctx = CiliaJoramTool.createContext(login, password, hostname, port, Session.AUTO_ACKNOWLEDGE);
            jctx.setAutoStart(false);
            // creates the destination object
            Destination dest = CiliaJoramTool.createDestination(jctx, dname);
            // creates the message consumer and set the message listener
            consumer = jctx.createConsumer(dest);
            consumer.setMessageListener(this);
            // starts the connection
            jctx.start();

            log.debug("[JMSCollector] started");
        } catch (JMSRuntimeException e) {
            log.error("[JMSCollector] unable to start", e);
            e.printStackTrace();
        } catch (JMSException e) {
            log.error("[JMSCollector] unable to start", e);
            e.printStackTrace();
        }
    }

    private void stop() {
        try {
            // closes the connection
            jctx.close();
        } catch (JMSRuntimeException e) {
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
                if (content instanceof Data) {
                    hasCiliaData = true;
                }
            } else if (msg instanceof MapMessage) {
                MapMessage message = (MapMessage) msg;
                enume = message.getMapNames();
                if (message.getObject(Data.DATA_CONTENT) != null) {
                    content = message.getObject(Data.DATA_CONTENT);
                }
                if (message.getString(Data.DATA_NAME) != null) {
                    name = message.getString(Data.DATA_NAME);
                }
                while (enume.hasMoreElements()) {
                    String propname = (String) enume.nextElement();
                    Object ob = ((MapMessage) msg).getObject(propname);
                    dico.put(propname, ob);
                }
            }
            if (hasCiliaData) {
                if (content instanceof SerializedData) {
                    SerializedData cdata = (SerializedData) content;
                    ndata = cdata.deserializeContent(bcontext);
                } else {
                    ndata = new Data((Data) content);
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
     * @param name the destination to set
     */
    public void setDestination(String name) {
        this.dname = name;
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

    private void switchClassLoader() {
    }
}
