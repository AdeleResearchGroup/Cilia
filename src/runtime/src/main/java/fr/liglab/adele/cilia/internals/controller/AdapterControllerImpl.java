package fr.liglab.adele.cilia.internals.controller;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.Adapter;
import fr.liglab.adele.cilia.MediatorComponent;
import fr.liglab.adele.cilia.model.ConstModel;
import fr.liglab.adele.cilia.util.Uuid;

public class AdapterControllerImpl extends MediatorControllerImpl {

	public AdapterControllerImpl(BundleContext context, Adapter model, CreatorThread creat) {
		super(context, model, creat);
	}

	protected void updateProperties(){
		mediatorModel.setProperty(ConstModel.PROPERTY_COMPONENT_ID, mediatorModel.nodeId());
		mediatorModel.setProperty(ConstModel.PROPERTY_CHAIN_ID, mediatorModel.chainId());
		mediatorModel.setProperty(ConstModel.PROPERTY_UUID,mediatorModel.uuid());
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
