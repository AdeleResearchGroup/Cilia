///*
// * Copyright Adele Team LIG
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package fr.liglab.adele.cilia.framework;
//
//import java.util.Dictionary;
//import java.util.Map;
//import java.util.Properties;
//
//import org.apache.felix.ipojo.ComponentInstance;
//import org.apache.felix.ipojo.ConfigurationException;
//import org.apache.felix.ipojo.Handler;
//import org.apache.felix.ipojo.PrimitiveHandler;
//import org.apache.felix.ipojo.metadata.Element;
//
//import fr.liglab.adele.cilia.runtime.CiliaMediatorAdmin;
//
///**
// * Handler used to configure/reconfigure in runtime a mediator.
// * @author torito
// *
// */
//public class CiliaAdminHandler extends PrimitiveHandler
//    implements CiliaMediatorAdmin {
//    /**
//     * 
//     */
//    private static final String NAMESPACE = "fr.liglab.adele.cilia.handlers";
//    /**
//     * 
//     */
//    private static final String SCHEDULER = "scheduler";
//    /**
//     * 
//     */
//    private static final String DISPATCHER = "dispatcher";
//    /**
//     * Mediator Properties.
//     */
//    private Properties m_mediatorProperties = new Properties();
//    /**
//     * True if the Mediator use SchedulerHandler.
//     */
//    private boolean usingScheduler = false;
//    /**
//     * True if the Mediator use DispatcherHandler.
//     */
//    private boolean usingDispatcher = false;
//    
//    /**
//     * Configuring this handler.
//     * @param element Element metadata.
//     * @param dictionary POJO properties
//     * @throws ConfigurationException never.
//     */
//    public final void configure(final Element element,
//            final Dictionary dictionary)
//            throws ConfigurationException {
//        m_mediatorProperties.putAll((Map) dictionary);
//        m_mediatorProperties.remove("component");
//        m_mediatorProperties.remove("instance.name");
//        if (element.containsElement(DISPATCHER, NAMESPACE)) {
//            usingDispatcher = true;
//        }
//        if (element.containsElement(SCHEDULER, NAMESPACE)) {
//            usingScheduler = true;
//        }
//        
//    }
//    /**
//     * Starting Handler.
//     */
//    public void start() {
//
//    }
//    /**
//     * Stoping Handler.
//     */
//    public void stop() {
//
//    }
//    /**
//     * Get the Mediator Instance Name.
//     * @return Mediator InstanceName.
//     */
//    public final String getMediatorName() {
//        return getInstanceManager().getInstanceName();
//    }
//    /**
//     * Get Mediator Properties.
//     * @return m_mediatorProperties.
//     */
//    public final Properties getMediatorProperties() {
//        return m_mediatorProperties;
//    }
//    /**
//     * Set New Mediator Properties.
//     * @param properties New Properties to be setted.
//     */
//    public final void setMediatorProperties(final Properties properties) {
//        ComponentInstance instance = getInstanceManager();
//        instance.reconfigure(properties);
//        Handler [] handlers = getInstanceManager().getRegistredHandlers();
//        for (int i = 0; i < handlers.length; i++) {
//        	handlers[i].reconfigure(properties);
//        	System.out.println("RECONFIGUTING  "  + handlers[i].getDescription().getHandlerName());
//        }
//        
//    }
//    /**
//     * Calling when Mediator is reconfigured.
//     * @param dictionary new properties.
//     */
//    public final void reconfigure(final Dictionary dictionary) {
//        m_mediatorProperties.putAll((Map) dictionary);
//    }
//    /**
//     * Start Mediator.
//     */
//    public final void startMediator() {
//        
//    }
//    /**
//     * Stop Mediator.
//     */
//    public final void stopMediator() {
//
//    }
//    /**
//     * Return true is this mediator is using dispatcher.
//     * @return usingDispatcher.
//     */
//    public final boolean usingDispatcher() {
//        return usingDispatcher;
//    }
//    /**
//     * Return true is this mediator is using scheduler.
//     * @return usingScheduler.
//     */
//    public final boolean usingScheduler() {
//        return usingScheduler;
//    }
//   
//}