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

package fr.liglab.adele.cilia.internals.controller;

import fr.liglab.adele.cilia.exceptions.CiliaRuntimeException;
import fr.liglab.adele.cilia.internals.factories.MediatorComponentManager;
import fr.liglab.adele.cilia.knowledge.MediatorMonitoring;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.model.impl.*;
import fr.liglab.adele.cilia.runtime.CiliaInstanceWrapper;
import fr.liglab.adele.cilia.runtime.FirerEvents;
import fr.liglab.adele.cilia.runtime.MediatorDescriptionEntry;
import fr.liglab.adele.cilia.util.Const;
import org.apache.felix.ipojo.ComponentInstance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;

/**
 * This class will observe the mediator model and will act as an itermediator
 * between the mediator model and the mediator instance.
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class MediatorControllerImpl implements Observer {

    /**
     * Mediator representation model that will be observed.
     */
    protected MediatorComponentImpl mediatorModel;
    /**
     * OSGi Bundle Context
     */
    protected BundleContext bcontext;
    /**
     * iPOJO Cilia instance wrapper, to wrap the mediator iPOJO instance.
     */
    protected CiliaInstanceWrapper mediatorInstance;

    protected Hashtable addedCollectors = new Hashtable();

    protected Hashtable addedSenders = new Hashtable();

    private static Logger logger = LoggerFactory
            .getLogger(Const.LOGGER_RUNTIME);

    protected CreatorThread creator;

    protected final Object lockObject = new Object();

    protected final String filter;

    private FirerEvents eventFirer;
    private ServiceRegistration entryRegistry;

    /**
     * Create a mediator model controller.
     *
     * @param model Mediator model to handle.
     */
    public MediatorControllerImpl(BundleContext context,
                                  MediatorComponent model, CreatorThread creat, FirerEvents notifier) {
        bcontext = context;
        creator = creat;
        mediatorModel = (MediatorComponentImpl) model;
        filter = createComponentFilter(mediatorModel);
        updateProperties();
        mediatorModel.addObserver(this);
        /* add extended Model : "monitoring" */
        MediatorMonitoring monitoring = (MediatorMonitoring) mediatorModel
                .getModel(MediatorMonitoring.NAME);
        if (monitoring == null) {
            monitoring = new MediatorMonitoring();
            mediatorModel.addModel(MediatorMonitoring.NAME, monitoring);
            monitoring.setModel(mediatorModel);
        }
        monitoring.setFirerEvent(notifier);
        eventFirer = notifier;
    }

    protected void updateProperties() {
        mediatorModel.setProperty(Const.PROPERTY_INSTANCE_TYPE,
                mediatorModel.getType());
        mediatorModel.setProperty(Const.PROPERTY_COMPONENT_ID,
                mediatorModel.nodeId());
        mediatorModel.setProperty(Const.PROPERTY_CHAIN_ID,
                mediatorModel.chainId());
        mediatorModel.setProperty(Const.PROPERTY_UUID, mediatorModel.uuid());
    }

    /**
     * Create the mediator instance when the mediator Controller is started.
     */
    private void createMediatorInstance() {
        // if there is an instance defined, destroy it and create a new one with
        // the given model.
        synchronized (lockObject) {
            if (mediatorInstance != null) {
                mediatorInstance.stop();
                mediatorInstance = null;
            }
            mediatorInstance = new CiliaInstanceWrapper(bcontext,
                    mediatorModel.getId(), filter,
                    mediatorModel.getProperties(), this);
        }
        logger.debug("[{}] instance will be created",
                mediatorModel.getId());
        mediatorInstance.start();
    }

    /**
     * Create the filter to locate the mediator factory.
     *
     * @param mediator
     * @return
     */
    protected String createComponentFilter(MediatorComponent mediator) {
        StringBuffer filter = new StringBuffer();
        filter.append("(&");
        filter.append("(");
        filter.append("mediator.name=");
        filter.append(mediator.getType());
        filter.append(")");
        if (mediator.getNamespace() != null) {
            filter.append("(");
            filter.append("mediator.namespace=");
            filter.append(mediator.getNamespace());
            filter.append(")");
        }
        if (mediator.getCategory() != null) {
            filter.append("(");
            filter.append("mediator.category=");
            filter.append(mediator.getCategory());
            filter.append(")");
        }
        filter.append("(factory.state=1)");
        filter.append(")");
        return filter.toString();
    }

    /**
     * Update the mediator instance using model information.
     */
    protected void updateMediationComponentInstance() {
        if (mediatorInstance.getState() == ComponentInstance.VALID) {
            createCollectorInstances();
            createSenderInstances();
            //update the mediator version in model.
            MediatorComponentManager mcm = (MediatorComponentManager) (mediatorInstance
                    .getInstanceManager());
            mediatorModel.setVersion(mcm.getVersion());
        }
    }

    /**
     * Create collector instances from the model.
     */
    private void createCollectorInstances() {
        BindingImpl[] bs = (BindingImpl[]) mediatorModel.getInBindings();
        for (int i = 0; i < bs.length; i++) {
            CollectorImpl collector = bs[i].getCollector();
            if (collector != null) {
                createCollector(collector);
            }
        }
    }

    /**
     * Create sender instances from the model.
     */
    private void createSenderInstances() {
        BindingImpl[] bs = (BindingImpl[]) mediatorModel.getOutBindings();
        for (int i = 0; i < bs.length; i++) {
            SenderImpl sender = bs[i].getSender();
            if (sender != null) {
                createSender(sender);
            }
        }

    }

    /**
     * update mediator properties.
     */
    public void updateInstanceProperties(Dictionary properties) {
        if (mediatorInstance == null
                || mediatorInstance.getState() != ComponentInstance.VALID) {
            logger.warn(
                    "[{}] Component instance is not valid when updating model properties",
                    this.mediatorModel.getId());
            return;
        }
        mediatorInstance.updateInstanceProperties(properties);
        eventFirer.fireEventNode(FirerEvents.EVT_MODIFIED, mediatorModel);
    }

    /**
     * Stop the mediator instance.
     */
    public void stop() {
        synchronized (lockObject) {
            mediatorModel.deleteObserver(this);
            if (mediatorInstance != null) {
                mediatorInstance.deleteObserver(this);
                MediatorComponentManager mcm = (MediatorComponentManager) (mediatorInstance
                        .getInstanceManager());
                if (mcm != null) {
                    try {
                        mcm.waitToProcessing(5000);
                    } catch (CiliaRuntimeException e) {
                        e.printStackTrace();
                    }
                }
                // We stop the mediator even if
                mediatorInstance.stop();
                logger.info("[{}] stopped", mediatorModel.getId());
                mediatorModel.dispose();
            }
        }
    }

    /**
     * Start the mediator instance.
     */
    public void start() {
        mediatorModel.setRunningState(MediatorComponent.State.INVALID);
        createMediatorInstance();

    }

    /**
     * Add collector instance to the mediator.
     *
     * @param collector collector model to add to the mediator.
     */
    public void createCollector(CollectorImpl collector) {
        boolean toAdd = true;
        synchronized (lockObject) {
            if (!addedCollectors.contains(collector)) {
                addedCollectors.put(collector.getId(), collector);
                toAdd = true;
            } else {
                // logger.warn(" (addCollector) Object instance in " +
                // mediatorModel
                // + " already exist " + collector.getId());
                toAdd = false;
            }
            if (toAdd) { // create
                getMediatorManager().addCollector(collector.getPortname(),
                        collector);
                // scheduler.addCollector(collectorType, portName, properties);
            }
        }
    }

    /**
     * Create the Sender instance from mthe given model.
     *
     * @param senderm the sender model added to the mediator model.
     */
    public void createSender(SenderImpl senderm) {
        boolean toAdd = true;
        synchronized (lockObject) {

            if (!addedSenders.contains(senderm)) {
                addedSenders.put(senderm.getId(), senderm);
                toAdd = true;
            } else {
                // logger.warn(" (addSender) Object instance in " +
                // mediatorModel
                // + "already exist" + senderm.getId());
                toAdd = false;
            }
            if (toAdd) { // create
                getMediatorManager().addSender(senderm.getPortname(), senderm);
            }
        }
    }

    /**
     * Remove the given collector to the mediator instance.
     *
     * @param collector Collector to remove.
     */
    public void removeCollector(CollectorImpl collector) {
        synchronized (lockObject) {
            if (collector != null) {

                addedCollectors.remove(collector);
                MediatorComponentManager mm = getMediatorManager();
                if (mm != null) {
                    mm.removeCollector(collector.getPortname(), collector);
                }
            }
        }
    }

    /**
     * Remove the given sender to the mediator instance.
     *
     * @param sender Sender to remove.
     */
    public void removeSender(SenderImpl sender) {
        synchronized (lockObject) {
            if (sender != null) {

                addedSenders.remove(sender);
                MediatorComponentManager mm = getMediatorManager();

                if (mm != null) {
                    mm.removeSender(sender.getPortname(), sender);
                }
            }
        }
    }

    /**
     * get the mediator instance state.
     */
    public MediatorComponent.State getState() {
        MediatorComponent.State returningState = MediatorComponent.State.INVALID;
        if (mediatorInstance != null) {
            MediatorComponentManager mm = (MediatorComponentManager) mediatorInstance
                    .getInstanceManager();
            if (mm != null) {
                int ipojoState = mm.getState();
                returningState = mapState(ipojoState);
            }
        }
        return returningState;
    }

    private MediatorComponent.State mapState(int ipojoState) {
        MediatorComponent.State returningState = MediatorComponent.State.INVALID;
        switch (ipojoState) {
            case ComponentInstance.DISPOSED:
                returningState = MediatorComponent.State.DISPOSED;
                break;
            case ComponentInstance.INVALID:
                returningState = MediatorComponent.State.INVALID;
                break;
            case ComponentInstance.STOPPED:
                returningState = MediatorComponent.State.STOPPED;
                break;
            case ComponentInstance.VALID:
                returningState = MediatorComponent.State.VALID;
                break;
        }
        return returningState;
    }

    /**
     * Method called when some event happend in the mediator model.
     *
     * @param mediator Mediator model observed.
     * @param arg      Observer method parapeters.
     */
    public void update(Observable mediator, Object arg) {
        if (mediator instanceof MediatorComponent) {
            MediatorComponent md = ((MediatorComponent) mediator);
            UpdateEvent event = (UpdateEvent) arg;
            if (event != null) {
                int action = event.getUpdateAction();
                switch (action) {
                    case UpdateActions.UPDATE_PROPERTIES: {
                        logger.debug("[{}] updating Properties: \n\t {}", md.getId(), md.getProperties());
                        updateInstanceProperties(md.getProperties());
                    }
                }
            }
        } else if (mediator instanceof CiliaInstanceWrapper) {
            MediatorComponent.State state = getState();
            mediatorModel.setRunningState(state);
            switch (state) {
                case VALID: {
                    updateMediationComponentInstance();
                    registerServiceDescription();
                }
                break;
                case DISPOSED:
                case STOPPED:
                case INVALID: {
                    cleanInstances();
                    unregisterServiceDescription();
                }
                break;
            }
            if (mediatorModel.isRunning()) {
                eventFirer.fireEventNode(FirerEvents.EVT_VALID, mediatorModel);
            } else {
                eventFirer
                        .fireEventNode(FirerEvents.EVT_INVALID, mediatorModel);
            }
        }
        synchronized (creator) {
            creator.notifyAll();
        }

    }

    private void registerServiceDescription() {
        Hashtable props = new Hashtable();
        props.put(Const.PROPERTY_INSTANCE_TYPE, mediatorModel.getType());
        props.put(Const.PROPERTY_CHAIN_ID, mediatorModel.chainId());
        props.put(Const.PROPERTY_COMPONENT_ID, mediatorModel.getId());
        MediatorDescriptionEntry mde = new MediatorDescriptionEntry() {
        };
        entryRegistry = bcontext.registerService(MediatorDescriptionEntry.class.getName(), mde, props);
    }

    private void unregisterServiceDescription() {
        if (entryRegistry != null) {
            entryRegistry.unregister();
            entryRegistry = null;
        }
    }

    private void cleanInstances() {
        synchronized (lockObject) {
            addedCollectors.clear();
            addedSenders.clear();
        }
    }

    private MediatorComponentManager getMediatorManager() {
        if (mediatorInstance == null) {
            return null;
        }
        ComponentInstance im = mediatorInstance.getInstanceManager();
        if (im instanceof MediatorComponentManager) {
            MediatorComponentManager mm = (MediatorComponentManager) im;
            return mm;
        }
        return null;
    }

    /**
     * Get the In port description of the executing component.
     *
     * @param name Name of the port.
     * @return the port.
     */
    public Port getInPort(String name) {
        MediatorComponentManager mm = getMediatorManager();
        if (mm != null) {
            return mm.getInPort(name);
        }
        logger.error("Unable to retrieve In-port '{}' on '{}'", name,
                this.mediatorModel.getId());
        logger.error("Instance Manager {} ", getMediatorManager());
        return null;
    }

    /**
     * Get the Out port description of the executing component.
     *
     * @param name Name of the port.
     * @return the port.
     */
    public Port getOutPort(String name) {
        MediatorComponentManager mm = getMediatorManager();
        if (mm != null) {
            return mm.getOutPort(name);
        }
        logger.error("Unable to retrieve Out-port '{}' on '{}' ", name,
                this.mediatorModel.getId());
        logger.error("Instance Manager {} ", getMediatorManager());
        return null;
    }
}
