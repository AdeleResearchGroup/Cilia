package fr.liglab.adele.cilia.specification;


import fr.liglab.adele.cilia.model.impl.ComponentImpl;
import fr.liglab.adele.cilia.model.impl.MediatorImpl;
import fr.liglab.adele.cilia.model.impl.PortImpl;
import fr.liglab.adele.cilia.model.impl.PortType;
import fr.liglab.adele.cilia.util.Const;

import java.util.HashMap;

public abstract class AbstractInAdapterSpecification implements InAdapterSpecification {

    private MediatorImpl mediatorSpec;

    protected HashMap outPorts = new HashMap();

    private ComponentImpl dispatcherDef;

    private ComponentImpl collectorDef;

    private static final String DEFAULT_CAT = "generic";


    public AbstractInAdapterSpecification(String name, String namespace, String category) {
        mediatorSpec = new MediatorImpl(name, name, namespace, category, null, null, null);
    }

    /**
     * Set the mediator category.
     *
     * @param category the given category.
     */
    public InAdapterSpecification setCategory(String category) {
        mediatorSpec.setCategory(category);
        return this;
    }

    /**
     * Get the mediator category.
     *
     * @return the mediator category.
     */
    public String getCategory() {
        if (mediatorSpec.getCategory() == null) {
            return DEFAULT_CAT;
        }
        return mediatorSpec.getCategory();
    }

    /**
     * Set the mediator namespace.
     *
     * @param namespace the mediator namespace.
     */
    public InAdapterSpecification setNamespace(String namespace) {
        mediatorSpec.setNamespace(namespace);
        return this;
    }

    /**
     * Get the mediator namespace.
     *
     * @return the mediator namespace.
     */
    public String getNamespace() {
        return mediatorSpec.getNamespace();
    }


    /**
     * Retrieve the mediator specification name.
     *
     * @return
     */
    public String getName() {
        return mediatorSpec.getType();
    }

    /**
     * Set the chosen collector info.
     *
     * @param collectorName      the collector name.
     * @param collectorNamespace the collector namesâce.
     */
    public InAdapterSpecification setCollector(String collectorName, String collectorNamespace) {
        collectorDef = new ComponentImpl(collectorName, collectorName, collectorNamespace, null);
        return this;
    }

    /**
     * Retrieve the chosen collector name.
     *
     * @return the collector name.
     */
    public String getCollectorName() {
        if (collectorDef == null) {
            return null;
        }
        return collectorDef.getType();
    }

    /**
     * Retrieve the collector namespace.
     *
     * @return the collector namespace.
     */
    public String getCollectorNamespace() {
        if (collectorDef == null) {
            return null;
        }
        if (collectorDef.getNamespace() == null) {
            return Const.CILIA_NAMESPACE;
        }
        return collectorDef.getNamespace();
    }

    /**
     * Assign the chosen dispatcher name.
     *
     * @param dispatcherName      the chosen dispatcher name.
     * @param dispatcherNamespace the chosen dispatcher namespace.
     */
    public InAdapterSpecification setDispatcher(String dispatcherName, String dispatcherNamespace) {
        dispatcherDef = new ComponentImpl(dispatcherName, dispatcherName, dispatcherNamespace, null);
        return this;
    }

    /**
     * Retrieve the chosen dispatcher name.
     *
     * @return the dispatcher name.
     */
    public String getDispatcherName() {
        if (dispatcherDef == null) {
            return null;
        }
        return dispatcherDef.getType();
    }

    /**
     * Retrieve the dispatcher namespace.
     *
     * @return the dispatcher namespace.
     */
    public String getDispatcherNamespace() {
        if (dispatcherDef == null) {
            return null;
        }
        if (dispatcherDef.getNamespace() == null) {
            return Const.CILIA_NAMESPACE;
        }
        return dispatcherDef.getNamespace();
    }

    public void setOutPort(String name, String type) {
        outPorts.put(name, new PortImpl(name, type, PortType.OUTPUT, null));
    }

    /**
     * Initialize the mediator specification type.
     */
    public abstract InAdapterSpecification initializeSpecification();

    /**
     * Stop the mediator specification type.
     */
    public abstract InAdapterSpecification stopSpecification();


}
