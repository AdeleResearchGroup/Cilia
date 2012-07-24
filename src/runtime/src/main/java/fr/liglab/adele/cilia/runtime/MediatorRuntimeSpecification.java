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
package fr.liglab.adele.cilia.runtime;

import java.util.Iterator;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.internals.factories.MediatorFactory;
import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.specification.AbstractMediatorSpecification;
import fr.liglab.adele.cilia.specification.MediatorSpecification;
/**
 * 
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class MediatorRuntimeSpecification extends AbstractMediatorSpecification {

	BundleContext context;
	
	private MediatorFactory factory;
	
	public MediatorRuntimeSpecification(String name, String namespace,
			String category) {
		super(name, namespace, category);
	}

	public MediatorRuntimeSpecification(String name, String namespace,
			String category, BundleContext c) {
		this(name, namespace, category);
		setContext(c);
	}
	
	protected void setContext(BundleContext bc) {
		context = bc;
	}
	
	public MediatorSpecification initializeSpecification()  {
		Element meta = generateMetadata();
		try {
			factory = new MediatorFactory(context, meta);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		System.out.println("Creating factory with: " + meta );
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
		
		Element ports = new Element("ports", "");
		Iterator it = inports.keySet().iterator();
		while(it.hasNext()){
			Port inport = (Port)inports.get((String)it.next());
			Element nport = new Element("in-port", "");
			nport.addAttribute(new Attribute("name", inport.getName()));
			nport.addAttribute(new Attribute("type", inport.getDataType()));
			ports.addElement(nport);
		}
		Iterator it3 = outports.keySet().iterator();
		while(it3.hasNext()){
			Port outport = (Port)outports.get((String)it3.next());
			Element nport = new Element("out-port", "");
			nport.addAttribute(new Attribute("name", outport.getName()));
			nport.addAttribute(new Attribute("type", outport.getDataType()));
			ports.addElement(nport);
		}
		
		
		mediatorMetadata.addElement(scheduler);
		mediatorMetadata.addElement(processor);
		mediatorMetadata.addElement(dispatcher);
		mediatorMetadata.addElement(ports);
		return mediatorMetadata;
	}

}
