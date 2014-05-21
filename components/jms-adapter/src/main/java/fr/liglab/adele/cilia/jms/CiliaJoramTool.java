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

import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class CiliaJoramTool {
    public static TopicConnection createTopicConnection(String user, String pass, String hostname, int port)
            throws JMSException {

        TopicConnectionFactory joramTopicConnectionFactory = null;
        try {
            joramTopicConnectionFactory = TcpConnectionFactory.create(hostname, port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // resolving classloader issues
        ClassLoader classloader = Thread.currentThread()
                .getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(
                    CiliaJoramTool.class.getClassLoader());
            // returns the a new topic connection
            return joramTopicConnectionFactory
                    .createTopicConnection(user, pass);
        } finally {
            Thread.currentThread().setContextClassLoader(classloader);
        }
    }
}
