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
package fr.liglab.adele.cilia.internals.factories;

import java.util.Hashtable;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.model.Port;
import fr.liglab.adele.cilia.model.impl.PortImpl;
import fr.liglab.adele.cilia.model.impl.PortType;

/**
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public abstract class MediatorComponentFactory extends CiliaComponentFactory {

	private Hashtable inPorts;
	private Hashtable outPorts;

	/**
	 * @param context
	 * @param element
	 * @throws ConfigurationException
	 */
	public MediatorComponentFactory(BundleContext context, Element element)
			throws ConfigurationException {
		super(context, element);
	}
	
	protected void computePorts() {
		inPorts = new Hashtable();
		outPorts = new Hashtable();
		Element ports[] = m_componentMetadata.getElements("ports",
				DEFAULT_NAMESPACE); // cilia namespace
		if (ports == null) { //try to use ipojo namespace
			ports = m_componentMetadata.getElements("ports");
		}
		for (int i = 0; ports != null && i < ports.length; i++ ) {
			Element allports[] = ports[i].getElements();
			computePorts(allports);
		}
	}
	
	protected void computePorts(Element[] ports) {
		for (int i = 0; ports != null && i < ports.length; i++ ) {
			String name = ports[i].getAttribute("name");
			String type = ports[i].getAttribute("type");
			if (ports[i].getName().compareTo("in-port") == 0 && name != null) {
				Port pt = new PortImpl(name, type, PortType.INPUT, null); 
				inPorts.put(name, pt);
			}
			else if (ports[i].getName().compareTo("out-port") == 0 && name != null) {
				Port pt = new PortImpl(name, type, PortType.OUTPUT, null); 
				outPorts.put(name, pt);
			}
		}
	}
	
	public Port getInPort(String name) {
		return (Port)inPorts.get(name);
	}
	
	public Port getOutPort(String name) {
		return (Port)outPorts.get(name);
	}

}
