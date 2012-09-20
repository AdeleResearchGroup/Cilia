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

package fr.liglab.adele.cilia.ea;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractCollector;


/**
 * EventAdmin Collector. 
 * Collect Data objects using Event-based protocol (OSGi EventAdmin).
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */

public class EventAdminCollector extends AbstractCollector implements EventHandler {


    private EventHandler m_eventHandler;

    private static final Logger log = LoggerFactory.getLogger("cilia.component.eventadmin");
    /**
     * Configurable property that holds the Event Topics
     */
    private String m_topics;

    private String ldapfilter;

    /**
     * OSGi Bundle Context
     */
    private BundleContext m_bundleContext;
    /**
     * OSGi Service Registration
     */
    private ServiceRegistration m_serviceRegistration;
    /**
     * Name.
     */
    /**
     * Constructor
     * 
     * @param bc
     *            BundleContext
     */
    public EventAdminCollector(BundleContext bc) {
        this.m_bundleContext = bc;
    }




    /**
     * Called when the component starts
     */
    public void started() {
        register();
    }

    private void register() {
        if (m_topics != null && m_topics.length() > 0) {
            m_eventHandler = this;
            Dictionary dico = new Hashtable();
            String[] topics = getListStrings(m_topics);
            dico.put(EventConstants.EVENT_TOPIC, topics);
            if (ldapfilter != null) {
                try {
                    Filter filter = null;
                    filter = m_bundleContext.createFilter(ldapfilter);
                } catch (InvalidSyntaxException e) {
                    log.error("Error when converting filter "+ldapfilter+" in ldap syntax");
                    log.error(e.getStackTrace().toString());
                    ldapfilter = null;
                }
            }
            if (ldapfilter != null) {
                dico.put(EventConstants.EVENT_FILTER, ldapfilter);
            }
            if (m_serviceRegistration != null) {
                m_serviceRegistration.unregister();
            }
            m_serviceRegistration = m_bundleContext.registerService(
                    EventHandler.class.getName(), m_eventHandler, dico);
        }else {
        	log.error("Unable to register without topic");
        }
        
    }

    /**
     * Called when the component stops
     */
    public void stopped() {
        if (m_serviceRegistration != null) {
            m_serviceRegistration.unregister();
        }
    }

    /**
     * Get the Detailed description of this Collector
     * 
     * @return
     */
    public String getDescription() {
        return "EventAdminCollector \nConfiguration:\n- topics";
    }

    /**
     * Handle the OSGi Event
     */
    public void handleEvent(Event event) {
        Dictionary dico = new Hashtable();
        String[] keys = event.getPropertyNames();
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
               log.debug("received value:" + event.getProperty(keys[i]));
                if (!keys[i].equalsIgnoreCase("event.topics")) {
                    dico.put(keys[i], event.getProperty(keys[i]));
                }
            }
        }

        /*
         * Create new Data object from the received event.
         */
        Data data = new Data(dico
                .get(Data.DATA_CONTENT), String.valueOf(dico.get(Data.DATA_NAME)), dico);

        notifyDataArrival(data);

    }

    /**
     * Used to change the Topics that this collector listen to from the
     * EventAdmin
     */
    public void setTopics(String topics) {
        m_topics = topics;
        if (m_serviceRegistration != null) {
            m_serviceRegistration.unregister();			
        }
        Dictionary dico = new Hashtable();
        String[] topicss = getListStrings(m_topics);
        dico.put(EventConstants.EVENT_TOPIC, topicss);
        m_serviceRegistration = m_bundleContext.registerService(
                EventHandler.class.getName(), this, dico);

    }

    /**
     * Get a list of separate Strings from one String
     * 
     * @param text
     * @return
     */
    private String[] getListStrings(String text) {
        text = BlankRemover.trim(text);
        String[] topics = text.split(",");
        return topics;
    }

    /**
     * Remove blanks from a String
     */
    private static class BlankRemover {
        /* remove leading whitespace */
        private static String ltrim(String source) {
            return source.replaceAll("^\\s+", "");
        }

        /**
         *  remove trailing whitespace
         */
        private static String rtrim(String source) {
            return source.replaceAll("\\s+$", "");
        }

        /**
         *  replace multiple white spaces between words with single blank 
         */  
        private static String itrim(String source) {
            String str = source.replaceAll("\\b\\s{2,}\\b", " ");
            return str.replaceAll(" ", "");
        }

        /* remove all superfluous white spaces in source string */
        public static String trim(String source) {
            return itrim(ltrim(rtrim(source)));
        }
    }


}
