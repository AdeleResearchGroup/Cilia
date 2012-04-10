package fr.liglab.adele.cilia.controller.impl;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.controller.AdapterController;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.ConstModel;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.MediatorComponent;

public class AdapterControllerImpl extends MediatorControllerImpl implements AdapterController{

	public AdapterControllerImpl(BundleContext context, Adapter model, CreatorThread creat) {
		super(context, model, creat);
	}

	protected void updateProperties(){
		mediatorModel.setProperty(ConstModel.PROPERTY_COMPONENT_ID, mediatorModel.getId());
		mediatorModel.setProperty(ConstModel.PROPERTY_CHAIN_ID, mediatorModel.getChain().getId());
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
	
}
