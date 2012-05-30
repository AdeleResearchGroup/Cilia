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
package fr.liglab.adele.cilia.internals;

import org.w3c.dom.Node;

import fr.liglab.adele.cilia.exceptions.CiliaParserException;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.impl.ComponentImpl;
import fr.liglab.adele.cilia.util.CiliaExtenderParser;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class PeriodicParser extends DomExtenderParser implements
CiliaExtenderParser {

	public PeriodicParser(){
		NAME="periodic";
		NAMESPACE="fr.liglab.adele.cilia.scheduler";
	}
	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.model.impl.parser.CiliaExtenderParser#getComponent(java.lang.Object, fr.liglab.adele.cilia.model.impl.IComponent)
	 */
	public Component getComponent(Object componentDescription,
			Component component) throws CiliaParserException {
		String period = null;
		String delay = null;
		ComponentImpl currentComponent = (ComponentImpl)component;
		Node periodicN = getNode("scheduler",componentDescription);

		period  = getAttributeValue(periodicN, "period");
		delay  = getAttributeValue(periodicN, "delay");

		if (period != null) {
			Long lperiod = null;
			try {
				lperiod = Long.parseLong(period);
			}catch (Exception ex) {
				throw new CiliaParserException("The period information is not in the correct format " + period);
			}
			currentComponent.setProperty("period", lperiod);
		}
		if (delay != null) {
			Long ldelay = null;
			try {
				ldelay = Long.parseLong(delay);
			} catch (Exception ex) {
				throw new CiliaParserException("The delay information is not in the correct format " + delay);
			}
			currentComponent.setProperty("delay", ldelay);
		}
		return currentComponent;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.model.impl.parser.CiliaExtenderParser#canHandle(java.lang.Object)
	 */
	public boolean canHandle(Object mediatorDescription) {
		Node disp = getNode("scheduler",mediatorDescription);
		if(disp == null) {
			return false;
		}
		return true;
	}

}
