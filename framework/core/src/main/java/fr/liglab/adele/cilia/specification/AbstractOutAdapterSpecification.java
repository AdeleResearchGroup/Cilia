package fr.liglab.adele.cilia.specification;


import fr.liglab.adele.cilia.model.impl.*;
import fr.liglab.adele.cilia.util.Const;

import java.util.HashMap;

public abstract class AbstractOutAdapterSpecification implements OutAdapterSpecification {

    private AdapterImpl adapterSpec;

    protected HashMap inPorts = new HashMap();

    private ComponentImpl senderDef;

    private ComponentImpl schedulerDef;

    private static final String DEFAULT_CAT = "generic";

    public AbstractOutAdapterSpecification(String name, String namespace, String category) {
        adapterSpec = new AdapterImpl(name, name, namespace, category, null, null, null);
    }

    /**
     * Set the adapter category.
     *
     * @param category the given category.
     */
    public OutAdapterSpecification setCategory(String category) {
        adapterSpec.setCategory(category);
        return this;
    }

    /**
     * Get the adapter category.
     *
     * @return the adapter category.
     */
    public String getCategory() {
        if (adapterSpec.getCategory() == null) {
            return DEFAULT_CAT;
        }
        return adapterSpec.getCategory();
    }

    /**
     * Set the adapter namespace.
     *
     * @param namespace the adapter namespace.
     */
    public OutAdapterSpecification setNamespace(String namespace) {
        adapterSpec.setNamespace(namespace);
        return this;
    }

    /**
     * Get the adapter namespace.
     *
     * @return the adapter namespace.
     */
    public String getNamespace() {
        return adapterSpec.getNamespace();
    }


    /**
     * Retrieve the adapter specification name.
     *
     * @return
     */
    public String getName() {
        return adapterSpec.getType();
    }

    /**
     * Assign the scheduler info to the adapter.
     *
     * @param schedulerName
     * @param schedulerNamespace
     */
    public OutAdapterSpecification setScheduler(String schedulerName, String schedulerNamespace) {
        schedulerDef = new ComponentImpl(schedulerName, schedulerName, schedulerNamespace, null);
        return this;
    }

    /**
     * Retrieve the scheduler assigned name.
     *
     * @return the scheduler name.
     */
    public String getSchedulerName() {
        if (schedulerDef == null) {
            return null;
        }
        return schedulerDef.getType();
    }

    /**
     * Retrieve the chosen scheduler namespace.
     *
     * @return the scheduler namespace. NULL when there is not scheduler assigned.
     */
    public String getSchedulerNamespace() {
        if (schedulerDef == null) {
            return null;
        }
        if (schedulerDef.getNamespace() == null) {
            return Const.CILIA_NAMESPACE;
        }
        return schedulerDef.getNamespace();
    }

    /**
     * Set the chosen sender info.
     *
     * @param senderName      the sender name.
     * @param senderNamespace the sender namespace.
     */
    public OutAdapterSpecification setSender(String senderName, String senderNamespace) {
        senderDef = new ComponentImpl(senderName, senderName, senderNamespace, null);
        return this;
    }

    /**
     * Retrieve the chosen sender name.
     *
     * @return the sender name.
     */
    public String getSenderName() {
        if (senderDef == null) {
            return null;
        }
        return senderDef.getType();
    }

    /**
     * Retrieve the sender namespace.
     *
     * @return the sender namespace.
     */
    public String getSenderNamespace() {
        if (senderDef == null) {
            return null;
        }
        if (senderDef.getNamespace() == null) {
            return Const.CILIA_NAMESPACE;
        }
        return senderDef.getNamespace();
    }

    public void setInPort(String name, String type) {
        inPorts.put(name, new PortImpl(name, type, PortType.INPUT, null));
    }

    /**
     * Initialize the adapter specification type.
     */
    public abstract OutAdapterSpecification initializeSpecification();

    /**
     * Stop the adapter specification type.
     */
    public abstract OutAdapterSpecification stopSpecification();


}
