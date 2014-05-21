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

import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import javax.jms.*;

/**
 * JMS/Joram helper class for new JMS 2.0 specification.
 * <p/>
 * This JMS helper works either in Point-To-Point (PTP) or Publish/Subscribe (P/S)
 * mode messages depending of the destination specified.
 * type (Queue or Topic) used to receive
 * messages. The destination type could be specified in the name of the destination, by
 * default it is a topic.
 *
 * @author ScalAgent Team
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class CiliaJoramTool {
    /**
     * Constant used to specified a queue
     */
    protected static final String QUEUE = "queue";
    /**
     * Constant used to specified a topic
     */
    protected static final String TOPIC = "topic";

    /**
     * Creates a JMS context allowing to send and receive messages.
     *
     * @param user     the user name.
     * @param pass     the password.
     * @param hostname the name or IP address of the Joram server's host.
     * @param port     the listening port of the Joram server's host.
     * @param mode     the mode of the created session.
     * @return the created JMS context.
     * @throws JMSException
     * @throws JMSRuntimeException
     */
    public static JMSContext createContext(String user, String pass,
                                           String hostname, int port,
                                           int mode) throws JMSException, JMSRuntimeException {
        ConnectionFactory joramCF = null;
        joramCF = TcpConnectionFactory.create(hostname, port);

        // resolving classloader issues
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(CiliaJoramTool.class.getClassLoader());
            // returns the a new JMS context
            return joramCF.createContext(user, pass, mode);
        } finally {
            Thread.currentThread().setContextClassLoader(classloader);
        }
    }

    /**
     * Returns the specified destination.
     * <p/>
     * The destination name is of the form "type:name" where type is the type of the
     * destination ('queue' or 'topic') and name its symbolic name. if the type is omitted
     * the destination is by default a topic.
     *
     * @param jctx  the JMS context to use.
     * @param dname the name of the destination.
     * @return the corresponding destination.
     */
    public static Destination createDestination(JMSContext jctx, String dname) {
        Destination dest = null;

        String type = TOPIC;
        int idx = dname.indexOf(':');
        if (idx >= 0) {
            type = dname.substring(0, idx);
            dname = dname.substring(idx + 1);
        }

        if (QUEUE.equals(type)) {
            dest = jctx.createQueue(dname);
        } else {
            dest = jctx.createTopic(dname);
        }

        return dest;
    }
}
