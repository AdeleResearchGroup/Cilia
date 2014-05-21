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

import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.framework.CiliaBindingService;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.model.impl.BindingImpl;
import fr.liglab.adele.cilia.model.impl.CollectorImpl;
import fr.liglab.adele.cilia.model.impl.SenderImpl;
import fr.liglab.adele.cilia.util.Const;
import org.apache.felix.ipojo.util.Tracker;
import org.apache.felix.ipojo.util.TrackerCustomizer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class BindingControllerImpl implements TrackerCustomizer {
    /**
     * Binding model to handle.
     */
    private BindingImpl modelBinding;

    private MediatorControllerImpl sourceController;

    private MediatorControllerImpl targetController;

    private final Object lockObject = new Object();

    private CiliaBindingService bindingService = null;

    private volatile byte allServices = 0x0;

    private final static byte ALL_SERVICES = 0x7;
    private final static byte BINDING_SERVICE = 0x1;
    private final static byte RECEIVING_SERVICE = 0x2;
    private final static byte SENDING_SERVICE = 0x4;
    private final ChainControllerImpl chainController;


    public void setSourceController(MediatorControllerImpl sourceController) {
        this.sourceController = sourceController;
    }

    public void setTargetController(MediatorControllerImpl targetController) {
        this.targetController = targetController;
    }

    /**
     * Reference to the chain controller.
     */
    private BundleContext bcontext;

    private static Logger log = LoggerFactory.getLogger(Const.LOGGER_RUNTIME);
    private Tracker bindingTracker;
    /**
     * Create a new binding controller.
     *
     * @param binding
     * to handle.
     */
    private static String DEFAULT_TYPE = "direct";

    public BindingControllerImpl(BundleContext context, Binding binding, ChainControllerImpl chainController) {
        modelBinding = (BindingImpl) binding;
        this.chainController = chainController;
        bcontext = context;
        settingUp();
        // start();
    }

    /**
     * Starting observing model.
     */
    public void start() {
        // stop();
        registerTracker();
    }

    /**
     * @throws CiliaException
     */
    private void createModels(CiliaBindingService cbs) throws CiliaException {

        CollectorImpl collector = null;
        SenderImpl sender = null;
        String bindingId = null;

        log.debug("Adding internal models for binding from {} to {}", modelBinding.getSourceMediator().getId(), modelBinding.getTargetMediator().getId());
        Component collectorModel = cbs.getCollectorModel(modelBinding
                .getProperties());
        Component senderModel = cbs
                .getSenderModel(modelBinding.getProperties());
        Dictionary colProps = null;
        Dictionary senProps = null;
        if (collectorModel != null) {
            colProps = collectorModel.getProperties();
        }
        if (senderModel != null) {
            senProps = senderModel.getProperties();
        }
        Dictionary componentProperties = cbs.getProperties(colProps, senProps,
                modelBinding);

        colProps = (Dictionary) componentProperties
                .get("cilia.collector.properties");
        senProps = (Dictionary) componentProperties
                .get("cilia.sender.properties");

        Port port1 = modelBinding.getSourcePort();
        Port port2 = modelBinding.getTargetPort();
        if (port1 == null) {
            log.error("Source Port in model binding is null"
                    + port1.getPortType() + " " + port1.getName());
            senderModel = null;
        }
        if (port2 == null) {
            log.error("Target Port in model binding is null "
                    + port2.getPortType() + " " + port2.getName());
            collectorModel = null;
        }

        bindingId = modelBinding.getSourceMediator().getId() + ":"
                + modelBinding.getSourcePort().getName() + ":"
                + modelBinding.getTargetMediator().getId() + ":"
                + modelBinding.getTargetPort().getName();

        if (collectorModel != null) {
            colProps.put("cilia.collector.identifier", bindingId);
            colProps.put("cilia.collector.port", modelBinding.getTargetPort()
                    .getName());
            collector = new CollectorImpl(bindingId, collectorModel.getType(),
                    null, modelBinding.getTargetPort().getName(), colProps);
        }
        if (senderModel != null) {
            senProps.put("cilia.sender.identifier", bindingId);
            senProps.put("cilia.sender.port", modelBinding.getSourcePort()
                    .getName());
            sender = new SenderImpl(bindingId, senderModel.getType(), null,
                    modelBinding.getSourcePort().getName(), senProps);
        }
        addModels(sender, collector);
    }

    /**
     * Add sender and collector models to the mediators.
     *
     * @param senderm    the sender model.
     * @param collectorm the collector model.
     */
    private void addModels(SenderImpl senderm, CollectorImpl collectorm) {
        MediatorComponent sm = modelBinding.getSourceMediator();
        MediatorComponent tm = modelBinding.getTargetMediator();

        if (sm != null && senderm != null) {
            synchronized (lockObject) {
                modelBinding.addSender(senderm);
                sourceController.createSender(senderm);
            }
        }

        if (tm != null && collectorm != null) {
            synchronized (lockObject) {
                modelBinding.addCollector(collectorm);
                targetController.createCollector(collectorm);
            }
        }
    }

    /**
     * Get the binding type.
     *
     * @return the binding type.
     */
    private String getBindingType() {
        String bindingType = modelBinding.getType();
        if (bindingType == null) {
            bindingType = DEFAULT_TYPE;
        }
        return bindingType;
    }

    /**
     * Stop observing model.
     */
    public void stop() {
        unregisterTracker();
        removeModels();
    }

    private void removeModels() {
        if (sourceController != null) {
            SenderImpl s = modelBinding.getSender();
            modelBinding.addSender(null);
            sourceController.removeSender(s);
        }
        if (targetController != null) {
            CollectorImpl c = modelBinding.getCollector();
            modelBinding.addCollector(null);
            targetController.removeCollector(c);
        }
    }

    /**
     * Update binding properties.
     *
     * @param binding binding to modify.
     */
    public void updateInstanceProperties(Binding binding) {
        Dictionary properties = binding.getProperties();
        if (sourceController != null) {
            sourceController.updateInstanceProperties(properties);
        }
        if (targetController != null) {
            targetController.updateInstanceProperties(properties);
        }
    }

    protected void registerTracker() {
        String filter = createFilter();
        log.warn("Services to bind {} to {} are not immediately available. It will wait...", modelBinding.getSourceMediator().getId(), modelBinding.getTargetMediator().getId());
        if (bindingTracker == null) {
            try {
                bindingTracker = new Tracker(bcontext,
                        bcontext.createFilter(filter), this);
                bindingTracker.open();
            } catch (InvalidSyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    protected void unregisterTracker() {
        if (bindingTracker != null) {
            bindingTracker.close();
        }
    }

    private String createFilter() {
        String filter = "(|(cilia.binding.protocol=" + getBindingType() + ")"
                + "(cilia.binding.type=" + getBindingType() + ")" + "(&("
                + Const.PROPERTY_CHAIN_ID + "="
                + modelBinding.getChain().getId() + ")("
                + Const.PROPERTY_COMPONENT_ID + "="
                + modelBinding.getSourceMediator().getId() + "))" + "(&("
                + Const.PROPERTY_CHAIN_ID + "="
                + modelBinding.getChain().getId() + ")("
                + Const.PROPERTY_COMPONENT_ID + "="
                + modelBinding.getTargetMediator().getId() + "))" + ")";
        return filter;
    }

    public void settingUp() {
        String defaultProtocol = null;
        if (bcontext != null) {
            defaultProtocol = bcontext.getProperty("cilia.default.protocol");
        }
        if (defaultProtocol != null) {
            DEFAULT_TYPE = defaultProtocol;
        }
    }

    /**
     * Methods from TrackerCustomizer.
     */
    public void addedService(ServiceReference reference) {
        if (allServices == ALL_SERVICES) {
            setupControllers();//all is ready, get controllers to be sure binding will be done.
            try {
                if (validatePort(modelBinding.getSourcePort(),
                        modelBinding.getTargetPort())) {
                    createModels(bindingService);
                }
            } catch (CiliaException e) {
                e.printStackTrace();
            }

        }
    }

    private void setupControllers() {
        setSourceController(chainController.getComponentcontroller(modelBinding.getSourceMediator().getId()));
        setTargetController(chainController.getComponentcontroller(modelBinding.getTargetMediator().getId()));
    }

    public boolean addingService(ServiceReference reference) {
        Object cbs = null;
        boolean toAdd = false;
        if (allServices == ALL_SERVICES) {
            return false;
        }
        cbs = bcontext.getService(reference);
        if (cbs instanceof CiliaBindingService) {
            allServices = (byte) (allServices | BINDING_SERVICE);
            bindingService = (CiliaBindingService) cbs;
            toAdd = true;
        } else if (String.valueOf(reference.getProperty(Const.PROPERTY_COMPONENT_ID))
                .compareToIgnoreCase(modelBinding.getSourceMediator().getId()) == 0) {
            allServices = (byte) (allServices | SENDING_SERVICE);
            toAdd = true;
        } else if (String.valueOf(reference.getProperty(Const.PROPERTY_COMPONENT_ID))
                .compareToIgnoreCase(modelBinding.getTargetMediator().getId()) == 0) {
            allServices = (byte) (allServices | RECEIVING_SERVICE);
            toAdd = true;
        }
        return toAdd;
    }

    public void modifiedService(ServiceReference reference, Object service) {
    }

    public void removedService(ServiceReference reference, Object service) {
        try {
            if (service instanceof CiliaBindingService) {
                allServices = (byte) (allServices ^ BINDING_SERVICE);
            } else if (String.valueOf(reference.getProperty(Const.PROPERTY_COMPONENT_ID))
                    .compareToIgnoreCase(
                            modelBinding.getSourceMediator().getId()) == 0) {
                allServices = (byte) (allServices ^ SENDING_SERVICE);
            } else if (String.valueOf(reference.getProperty(Const.PROPERTY_COMPONENT_ID))
                    .compareToIgnoreCase(
                            modelBinding.getTargetMediator().getId()) == 0) {
                allServices = (byte) (allServices ^ RECEIVING_SERVICE);
            }
        } catch (Exception e) {
        }
        if (sourceController != null) {
            sourceController.removeSender(modelBinding.getSender());
        }
        if (targetController != null) {
            targetController.removeCollector(modelBinding.getCollector());
        }
        removeModels();
    }

    private boolean validatePort(Port exitPort, Port entryPort) {
        boolean forceBinding;
        boolean valid = true;
        try {
            String fb = bcontext.getProperty("cilia.forcebindings");
            forceBinding = Boolean.parseBoolean(fb);
        } catch (Exception ex) {
            forceBinding = false;
        }

        Port componentExitPort = sourceController
                .getOutPort(exitPort.getName());
        Port componentEntryPort = targetController.getInPort(entryPort
                .getName());
        if (componentExitPort == null) {
            log.error("Mediator {} does not have a port named {}", exitPort
                    .getMediator().getId(), exitPort.getName());
            valid = false;
        }
        if (valid && componentExitPort.getDataType() == null) {
            log.error(
                    "Mediator port {} in {} does not have a well defined data type",
                    exitPort.getName(), exitPort.getMediator().getId());
            valid = false;
        }
        if (valid && componentEntryPort == null) {
            log.error("Mediator {} does not have a port named {}", entryPort
                    .getMediator().getId(), entryPort.getName());
            valid = false;
        }
        if (valid && componentEntryPort.getDataType() == null) {
            log.error(
                    "Mediator port {} in {} does not have a well defined data type",
                    entryPort.getName(), entryPort.getMediator().getId());
            valid = false;
        }
        if (valid
                && ((componentEntryPort.getDataType().compareToIgnoreCase(
                componentExitPort.getDataType()) != 0) && (componentEntryPort
                .getDataType().compareTo("*") != 0 && (componentExitPort
                .getDataType().compareTo("*") != 0)))) {
            log.error("Trying to bind incompatible ports: ExitPort["
                    + componentExitPort.getName() + " = "
                    + componentExitPort.getDataType() + "] & EntryPort["
                    + componentEntryPort.getName() + " = "
                    + componentEntryPort.getDataType() + "]");
            valid = false;
        }
        return valid || forceBinding;
    }

}
