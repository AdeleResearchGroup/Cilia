package fr.liglab.adele.cilia.runtime.impl;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.factories.MediatorComponentFactory;
import fr.liglab.adele.cilia.specification.AbstractMediatorSpecification;
import fr.liglab.adele.cilia.specification.MediatorSpecification;

public class MediatorRuntimeSpecification extends AbstractMediatorSpecification {

	BundleContext context;
	
	private MediatorComponentFactory factory;
	
	public MediatorRuntimeSpecification(String name, String namespace,
			String category) {
		super(name, namespace, category);
	}

	protected void setContext(BundleContext bc) {
		context = bc;
	}
	
	public MediatorSpecification initializeSpecification()  {
		Element meta = generateMetadata();
		try {
			factory = new MediatorComponentFactory(context, meta);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		factory.start();
		return this;
	}

	public MediatorSpecification stopSpecification() {
		factory.stop();
		return null;
	}
	
	private Element generateMetadata(){
		Element mediatorMetadata = new Element("mediator","");
		mediatorMetadata.addAttribute(new Attribute("name", getName()));
		mediatorMetadata.addAttribute(new Attribute("category", getCategory()));
		mediatorMetadata.addAttribute(new Attribute("namespace", getNamespace()));
		
		Element scheduler = new Element("scheduler", "");
		scheduler.addAttribute(new Attribute("name", getSchedulerName()));
		scheduler.addAttribute(new Attribute("namespace", getSchedulerNamespace()));
		
		Element processor = new Element("processor", "");
		processor.addAttribute(new Attribute("name", getProcessorName()));
		processor.addAttribute(new Attribute("namespace", getProcessorNamespace()));
		
		Element dispatcher = new Element("dispatcher", "");
		dispatcher.addAttribute(new Attribute("name", getDispatcherName()));
		dispatcher.addAttribute(new Attribute("namespace", getDispatcherNamespace()));
		
		mediatorMetadata.addElement(scheduler);
		mediatorMetadata.addElement(processor);
		mediatorMetadata.addElement(dispatcher);
		return mediatorMetadata;
	}

}
