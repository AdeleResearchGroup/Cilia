package fr.liglab.adele.cilia.internals.controller;

import org.apache.felix.ipojo.ComponentInstance;
import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.internals.factories.AdapterFactoryI;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.impl.AdapterImpl;
import fr.liglab.adele.cilia.runtime.FirerEvents;

public class AdapterControllerImpl extends MediatorControllerImpl {

	AdapterImpl adapterModel; 
	
	public AdapterControllerImpl(BundleContext context, Adapter model, CreatorThread creat,FirerEvents notifier) {
		super(context, model, creat,notifier);
		adapterModel = (AdapterImpl)model;
	}

	protected String createComponentFilter (MediatorComponent mediator) {
        StringBuffer filter = new StringBuffer();
        filter.append("(&");
        filter.append("(");
        filter.append("adapter.name=");
        filter.append(mediator.getType());
        filter.append(")");
        if (mediator.getNamespace() != null) {
            filter.append("(");
            filter.append("adapter.namespace=");
            filter.append(mediator.getNamespace());
            filter.append(")");
        }
        if (mediator.getCategory() != null) {
            filter.append("(");
            filter.append("adapter.category=");
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
		super.updateMediationComponentInstance();
		setUpAdapterPattern();
	}
	
	private void setUpAdapterPattern()  {
		if (mediatorInstance.getState() == ComponentInstance.VALID) {
			AdapterFactoryI factory = (AdapterFactoryI)mediatorInstance.getInstanceManager().getFactory();
			adapterModel.setPattern(factory.getPattern());
		}
	}
	
}
