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
package fr.liglab.adele.cilia.ext;

import java.util.HashMap;
import java.util.Map;

import fr.liglab.adele.cilia.Mediator;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class PropertyBasedRouting {
	private final static String ROUTE = "conditions";

	private final static String PROPERTY = "property";

	private Mediator mediator;

	private final Object lockObject = new Object();

	private RouteConfigurationImpl currentConfiguration;

	public PropertyBasedRouting(Mediator mediator) {
		this.mediator = mediator;
	}


	public PropertyBasedRouting property_name(String propertyName){
		mediator.setProperty(PROPERTY, propertyName);
		return this;
	}

	public PropertyBasedRouting when_property_value_is(String condition) {
		synchronized (lockObject) {
			if (currentConfiguration == null) {
				currentConfiguration = new RouteConfigurationImpl();
			}
			currentConfiguration.condition(condition);
		}
		return done();
	}
	/**
	 * 
	 * @param listPort coma separated ports.
	 * @return
	 */
	public PropertyBasedRouting send_to(String listPort){
		synchronized (lockObject) {
			if (currentConfiguration == null) {
				currentConfiguration = new RouteConfigurationImpl();
			}
			currentConfiguration.port(listPort);
		}
		return done();
	}

	private PropertyBasedRouting done(){
		Map configurations = null;
		boolean modified = false;
		synchronized (lockObject) {
			configurations = configurations();
			if (currentConfiguration != null && currentConfiguration.getCondition() != null && currentConfiguration.getPort() != null) {
				configurations.put(currentConfiguration.getCondition(),currentConfiguration.getPort());
				modified = true;
			}
			if (modified) {
				currentConfiguration = null;
			}
		}
		if (modified) { // it is tested outside the sync block.
			mediator.setProperty(ROUTE, new HashMap(configurations));
		}
		return this;
	}

	public PropertyBasedRouting remove(String condition){
		Map configurations;
		synchronized (lockObject) {
			configurations = configurations();
			if (configurations.containsKey(condition)) {
				configurations.remove(condition);
			}
			done();
		}
		return this;
	}

	public Map configurations(){
		Map configurations = null;
		synchronized (lockObject) {
			Object prop = mediator.getProperty(ROUTE);
			if (prop != null && prop instanceof Map) {
				configurations = (Map)prop;
			} else {
				configurations = new HashMap();
			}
		}
		return configurations;
	}


	private class RouteConfigurationImpl {

		private String condition;

		private String ports;

		/* (non-Javadoc)
		 * @see fr.liglab.adele.cilia.component.dispatcher.evaluator.RouteConfiguration#condition(java.lang.String)
		 */
		public void condition(String cond) {
			condition = cond;
		}

		/* (non-Javadoc)
		 * @see fr.liglab.adele.cilia.component.dispatcher.evaluator.RouteConfiguration#port(java.lang.String[])
		 */
		public void port(String port) {
			ports = port;
		}

		/* (non-Javadoc)
		 * @see fr.liglab.adele.cilia.component.dispatcher.evaluator.RouteConfiguration#getCondition()
		 */
		public String getCondition() {
			return condition;
		}

		/* (non-Javadoc)
		 * @see fr.liglab.adele.cilia.component.dispatcher.evaluator.RouteConfiguration#getPort()
		 */
		public String getPort() {
			return ports;
		}
	}


}
