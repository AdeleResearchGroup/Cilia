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

package fr.liglab.adele.cilia.framework.monitor.statevariable;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.Measure;
import fr.liglab.adele.cilia.framework.monitor.AbstractMonitor;
import fr.liglab.adele.cilia.runtime.ConstRuntime;
import fr.liglab.adele.cilia.runtime.WorkQueue;
import fr.liglab.adele.cilia.util.Const;
import fr.liglab.adele.cilia.util.FrameworkUtils;
import fr.liglab.adele.cilia.util.Watch;
import fr.liglab.adele.cilia.util.concurrent.ConcurrentReaderHashMap;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class MonitorHandlerStateVar extends AbstractMonitor {
    private static Logger logger = LoggerFactory.getLogger(Const.LOGGER_RUNTIME);
    private BundleContext m_bundleContext;
    /* TAG for storing message history */
    private static final String PROPERTY_MSG_HISTORY = "cilia.message.history";
    private static final String PROPERTY_BINDING_TIME = "cilia.message.time.bind";
    /* This reference will be injected by iPOJO */
    private WorkQueue m_systemQueue;
    /* Internal variables */
    private long[] m_counters = new long[12];
    private LinkedList m_gatherMsgIn = new LinkedList();
    private LinkedList m_snapshootMsg = new LinkedList();
    private LinkedList m_historyList = new LinkedList();
    private Object _lock = new Object();
    private Watch processTime;
    private String chainId, componentId, uuid;
    private Map m_statevar = new ConcurrentReaderHashMap();
    private Set listStateVarEnabled = new HashSet();
    private Set previousStateVarEnabled = new HashSet();
    private String topic;

    /* Handler configuration */
    public void configure(Element metadata, Dictionary configuration)
            throws ConfigurationException {
        chainId = (String) configuration.get(Const.PROPERTY_CHAIN_ID);
        componentId = (String) configuration.get(Const.PROPERTY_COMPONENT_ID);
        uuid = (String) configuration.get(Const.PROPERTY_UUID);
        topic = ConstRuntime.TOPIC_HEADER + chainId;
        configureStateVar(configuration);
    }

    /* Configuration set by the mediator/adapter during initialisation */
    private void configureStateVar(Dictionary configuration) {
        Map configs = (Map) configuration.get(ConstRuntime.MONITORING_CONFIGURATION);
        /* Retreive all state var enabled */
        /* Set the data flow for all state Var */
        if (configs != null) {
            previousStateVarEnabled.clear();
            previousStateVarEnabled.addAll(listStateVarEnabled);
            Iterator it = configs.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                String key = (String) pairs.getKey();

                if (key.equalsIgnoreCase("enable")) {
                    listStateVarEnabled.clear();
                    /* Retreive all state enable/disable */
                    Set enabled = (Set) pairs.getValue();
                    Iterator iter = enabled.iterator();
                    while (iter.hasNext()) {
                        String stateVarId = (String) iter.next();
                        listStateVarEnabled.add(stateVarId);
                        stateVarConfiguration(stateVarId);
                    }
                } else {
					/* Store the dataflow control */
                    String ldapfilter = (String) pairs.getValue();
                    stateVarConfiguration(key, ldapfilter);
                }
            }
        }
    }

    public void validate() {
        retreiveEventAdmin();
        fireStatusChange();
    }

    public void unvalidate() {
    }

    public void start() {
        m_bundleContext = getFactory().getBundleContext();
    }

    public void stop() {
		/* Clear state var */
        listStateVarEnabled.clear();
    }

    /* retreive EventAdmin reference */
    private ServiceReference retreiveEventAdmin() {
        ServiceReference[] refs = null;
        ServiceReference refEventAdmin;
        try {
            refs = m_bundleContext.getServiceReferences(EventAdmin.class.getName(), null);
        } catch (InvalidSyntaxException e) {
            logger.error("Event Admin  service lookup unrecoverable error");
        }
        if (refs != null)
            refEventAdmin = refs[0];
        else
            refEventAdmin = null;
        return refEventAdmin;
    }

    /*
     * Configure the item (class) holding :
     * data flow control and the flag : enable/disable
     */
    private void stateVarConfiguration(String stateVarId) {

        StateVarItem item = (StateVarItem) m_statevar.get(stateVarId);
        if (item == null) {
            item = new StateVarItem();
        }
        m_statevar.put(stateVarId, item);
    }

    /*
     * For all state varaible publish the state
     */
    private void fireStatusChange() {
        Set union = new TreeSet(previousStateVarEnabled);
        union.addAll(listStateVarEnabled);

        Iterator it = union.iterator();
        String variableId;
        while (it.hasNext()) {
            variableId = (String) it.next();
            if ((previousStateVarEnabled.contains(variableId))
                    && (!listStateVarEnabled.contains(variableId)))
                firerVariableStatus(variableId, false);

            else {
                if ((!previousStateVarEnabled.contains(variableId))
                        && (listStateVarEnabled.contains(variableId)))
                    firerVariableStatus(variableId, true);
            }
        }
    }

    /* Store tjhe configuration
     * dataflow and flag enable/disable
     */
    private void stateVarConfiguration(String stateVarId, String ldapFilter) {
        Condition cond = null;
        if ((ldapFilter != null) && (ldapFilter.length() > 0)) {
            try {
                cond = new Condition(getInstanceManager().getContext(), ldapFilter);
            } catch (Exception ex) {
                logger.error("Invalid LDAP syntax '" + ldapFilter + "' ,state variable '"
                        + stateVarId + "'");
                cond = null;
            }
        }
        StateVarItem item = (StateVarItem) m_statevar.get(stateVarId);
        if (item == null) {
            item = new StateVarItem();
        }
        item.condition = cond;
        m_statevar.put(stateVarId, item);
    }

    private boolean isEnabled(String stateVarId) {
        return listStateVarEnabled.contains(stateVarId);
    }

    /* publish the measure to the base-level */
    private void publish(String stateVarId, Object data, long ticksCount) {
        long last_ticksCount;
        Condition cond;
        boolean fire;

        StateVarItem item = (StateVarItem) m_statevar.get(stateVarId);
        if (item == null) {
            fire = true;
        } else {
            cond = item.condition;
            if (cond != null) {
                last_ticksCount = item.lastpublish.longValue();
                fire = cond.match(
                        ticksCount,
                        Watch.fromTicksToMs(ticksCount)
                                - Watch.fromTicksToMs(last_ticksCount)
                );
            } else {
                fire = true;
                item.lastpublish = new Long(ticksCount);
            }
        }
        if (fire) {
            firer(stateVarId, data, ticksCount);
        }

    }

    private void firer(String stateVarId, Object value, long ticksCount) {
        EventAdmin m_eventAdmin;
        ServiceReference refEventAdmin = retreiveEventAdmin();
        if (refEventAdmin == null) {
            logger.error("Unable to retrieve Event Admin");
        } else {
			/* gather data to be published */
            Map data = new HashMap(5);
            data.put(ConstRuntime.EVENT_TYPE, ConstRuntime.TYPE_DATA);
            data.put(ConstRuntime.UUID, uuid);
            data.put(ConstRuntime.VARIABLE_ID, stateVarId);
            data.put(ConstRuntime.VALUE, value);
            data.put(ConstRuntime.TIMESTAMP, new Long(ticksCount));

            StateVarItem item = (StateVarItem) m_statevar.get(stateVarId);
            if (item != null)
                item.lastpublish = new Long(ticksCount);

            m_eventAdmin = (EventAdmin) m_bundleContext.getService(refEventAdmin);
            m_eventAdmin.postEvent(new Event(topic, data));
            m_bundleContext.ungetService(refEventAdmin);
            logger.debug("Node [{}] publish state variable  [{}]",
                    FrameworkUtils.makeQualifiedId(chainId, componentId, uuid) + ":"
                            + stateVarId, value
            );

        }
    }

    /*
     * Publish the state of a state-variable
     */
    private void firerVariableStatus(String stateVarId, boolean value) {
        EventAdmin m_eventAdmin;
        ServiceReference refEventAdmin = retreiveEventAdmin();
        if (refEventAdmin == null) {
            logger.error("Unable to retrieve Event Admin");
        } else {
			/* gather data to be published */
            Map data = new HashMap(4);
            data.put(ConstRuntime.EVENT_TYPE, ConstRuntime.TYPE_STATUS_VARIABLE);
            data.put(ConstRuntime.UUID, uuid);
            data.put(ConstRuntime.VARIABLE_ID, stateVarId);
            data.put(ConstRuntime.VALUE, new Boolean(value));

            m_eventAdmin = (EventAdmin) m_bundleContext.getService(refEventAdmin);
            m_eventAdmin.postEvent(new Event(topic, data));
            m_bundleContext.ungetService(refEventAdmin);
        }
    }

    private void gatherIncommingHistory(Data data) {
        if (data != null) {
            synchronized (_lock) {
				/* Gather all messages history */
                List list = (List) data.getProperty(PROPERTY_MSG_HISTORY);
				/* Watch == null means , first incoming message */
                if (list != null)
                    m_gatherMsgIn.addAll(list);
            }
        }
    }

    private void snapShotHistory() {
        synchronized (_lock) {
            if (!m_gatherMsgIn.isEmpty())
                m_snapshootMsg.addAll(m_gatherMsgIn);
            m_gatherMsgIn.clear();
        }
    }

    /*
     * Injects tag for - message history - compute binding time
     */
    private void injectTags(List listData) {
        Iterator it;
        Watch watch;
        if (listData != null) {
            synchronized (_lock) {
                if (!m_snapshootMsg.isEmpty()) {
                    m_historyList.addAll(m_snapshootMsg);
                    m_snapshootMsg.clear();
                }
                watch = new Watch(componentId);
                m_historyList.addLast(watch);
            }
            if (!m_historyList.isEmpty()) {
                it = listData.iterator();
                while (it.hasNext()) {
                    Data data = (Data) it.next();
                    data.setProperty(PROPERTY_MSG_HISTORY, new LinkedList(m_historyList));
                    data.setProperty(PROPERTY_BINDING_TIME, watch);
                }
                m_historyList.clear();
            }
        }
    }

    public void onCollect(Data data) {
        if (listStateVarEnabled.isEmpty())
            return;

        gatherIncommingHistory(data);

        if (isEnabled("scheduler.count")) {
            m_counters[0]++;
            m_systemQueue.execute(new AsynchronousExec("scheduler.count", new Long(
                    m_counters[0])));
        }
        if (isEnabled("scheduler.data")) {
            Data publishedData;
            if (data == null)
                publishedData = null;
            else
                publishedData = new Data(data);
            m_systemQueue.execute(new AsynchronousExec("scheduler.data", publishedData));

        }
		/* Computes the binding time */
        if ((isEnabled("transmission.delay") && data != null)) {
            synchronized (_lock) {
                Watch watch = (Watch) data.getProperty(PROPERTY_BINDING_TIME);
                data.removeProperty(PROPERTY_BINDING_TIME);
                if (watch != null) {
                    long elapsedTime = Watch.fromTicksToMs(watch.getElapsedTicks());
                    m_systemQueue.execute(new AsynchronousExec("transmission.delay",
                            new Long(elapsedTime)));
                }
            }
        }
		/* Publish data history */
        if (isEnabled("message.history")) {
			/* Retreive history */
            List list = (List) data.getProperty(PROPERTY_MSG_HISTORY);
            if ((list != null) && (!list.isEmpty())) {
                m_systemQueue.execute(new AsynchronousExec("message.history", new Data(
                        list)));
            }
        }
    }

    public void onProcessEntry(List data) {

        if (listStateVarEnabled.isEmpty())
            return;

        snapShotHistory();

        processTime = new Watch();
        if (isEnabled("process.entry.count")) {
            m_counters[1]++;
            m_systemQueue.execute(new AsynchronousExec("process.entry.count", new Long(
                    m_counters[1])));
        }
        if (isEnabled("process.entry.data")) {
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    m_systemQueue.execute(new AsynchronousExec("process.entry.data",
                            new Data(data.get(i))));
                }
            } else
                m_systemQueue.execute(new AsynchronousExec("scheduler.data", null));
        }
        if (isEnabled("process.msg.treated") && (data != null)) {
			/* # number of messages treated */
            m_counters[8] = data.size();
            m_systemQueue.execute(new AsynchronousExec("process.msg.treated", new Long(
                    m_counters[8])));

        }
    }

    public void onProcessExit(List data) {

        if (listStateVarEnabled.isEmpty())
            return;

        if (isEnabled("message.history") || isEnabled("transmission.delay")) {
            injectTags(data);
        }

        if (isEnabled("processing.delay")) {
            m_systemQueue.execute(new AsynchronousExec("processing.delay", new Long(Watch
                    .fromTicksToMs(processTime.getElapsedTicks()))));
        }
        if (isEnabled("process.exit.count")) {
            m_counters[2]++;
            m_systemQueue.execute(new AsynchronousExec("process.exit.count", new Long(
                    m_counters[2])));
        }
        if (isEnabled("process.exit.data")) {
            Data publishedData;
            if (data == null)
                publishedData = null;
            else
                publishedData = new Data(data);
            m_systemQueue
                    .execute(new AsynchronousExec("process.exit.data", publishedData));
        }

    }

    public void onDispatch(List data) {

        if (listStateVarEnabled.isEmpty())
            return;

        if (isEnabled("dispatch.count")) {
            m_counters[3]++;
            m_systemQueue.execute(new AsynchronousExec("dispatch.count", new Long(
                    m_counters[3])));
        }
        if (isEnabled("dispatch.data")) {
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    m_systemQueue.execute(new AsynchronousExec("dispatch.data", new Data(
                            data.get(i))));
                }
            } else
                m_systemQueue.execute(new AsynchronousExec("dispatch.data", null));
        }
        if (isEnabled("dispatch.msg.treated") && (data != null)) {
            m_counters[9] = data.size();
            m_systemQueue.execute(new AsynchronousExec("dispatch.msg.treated", new Long(
                    m_counters[9])));
        }

    }

    public void onProcessError(Data data, Exception ex) {

        if (listStateVarEnabled.isEmpty())
            return;

        m_counters[4]++;
        if (isEnabled("process.err.count")) {
            m_counters[4]++;
            m_systemQueue.execute(new AsynchronousExec("process.err.count", new Long(
                    m_counters[4])));
        }
        if (isEnabled("process.err.data")) {
            Data publishedData;
            if (data == null)
                publishedData = null;
            else
                publishedData = new Data(data);
            m_systemQueue
                    .execute(new AsynchronousExec("process.err.data", publishedData));

        }
    }

    /*
     * receive events from the framework
     */
    public void fireEvent(Map info) {

        if (listStateVarEnabled.isEmpty())
            return;

        if (isEnabled("fire.event")) {
            m_systemQueue.execute(new AsynchronousExec("fire.event", info));
        }
        if (isEnabled("fire.event.count")) {
            m_counters[5]++;
            m_systemQueue.execute(new AsynchronousExec("fire.event.count", new Long(
                    m_counters[5])));
        }
    }

    /*
     * receive event service from dependency handler
     */
    public void onServiceArrival(Map info) {

        if (listStateVarEnabled.isEmpty())
            return;

        if (isEnabled("service.arrival")) {
            m_systemQueue.execute(new AsynchronousExec("service.arrival", info));
        }
        if (isEnabled("service.arrival.count")) {
            m_counters[6]++;
            m_systemQueue.execute(new AsynchronousExec("service.arrival.count", new Long(
                    m_counters[6])));
        }
    }

    /*
     * receive event service form dependency handler
     */
    public void onServiceDeparture(Map info) {

        if (listStateVarEnabled.isEmpty())
            return;

        if (isEnabled("service.departure")) {
            m_systemQueue.execute(new AsynchronousExec("service.departure", info));
        }
        if (isEnabled("service.departure.count")) {
            m_counters[7]++;
            m_systemQueue.execute(new AsynchronousExec("service.departure.count",
                    new Long(m_counters[7])));
        }
    }

    public void onFieldGet(String field, Object o) {

        if (listStateVarEnabled.isEmpty())
            return;

        if (isEnabled("field.get")) {
            m_systemQueue.equals(new AsynchronousExec("field.get", Collections
                    .singletonMap(field, o)));
        }
        if (isEnabled("field.get.count")) {
            m_counters[10]++;
            m_systemQueue.execute(new AsynchronousExec("field.get.count", new Long(
                    m_counters[10])));
        }

    }

    public void onFieldSet(String field, Object o) {

        if (listStateVarEnabled.isEmpty())
            return;

        if (isEnabled("field.set")) {
            m_systemQueue.equals(new AsynchronousExec("field.set", Collections
                    .singletonMap(field, o)));
        }
        if (isEnabled("field.set.count")) {
            m_counters[11]++;
            m_systemQueue.execute(new AsynchronousExec("field.set.count", new Long(
                    m_counters[11])));
        }
    }

    /**
     * Asynchronous execution
     */
    private class AsynchronousExec implements Runnable {
        private final String stateVar;
        private final Object data;
        private final long tickCount = Watch.getCurrentTicks();

        AsynchronousExec(String stateVar, Object data) {
            this.stateVar = stateVar;
            if (data == null)
                this.data = Measure.NO_VALUE;
            else
                this.data = data;
        }

        public void run() {
            publish(stateVar, data, tickCount);
        }
    }

    /* Reconfigure */
    public void reconfigure(Dictionary configuration) {
        configureStateVar(configuration);
        fireStatusChange();
    }

    /* -- State var configuration */
    private final class StateVarItem {
        Condition condition;
        Long lastpublish;

        public StateVarItem() {
            lastpublish = new Long(0);
            condition = null;
        }
    }
}
