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
import java.util.Hashtable;
import java.util.Map;

import fr.liglab.adele.cilia.builder.CustomBuilderConfigurator;
import fr.liglab.adele.cilia.model.MediatorComponentImpl;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
//@SuppressWarnings({"rawtypes","unchecked"})
public class ContentBasedRouting  implements CustomBuilderConfigurator{

	private final static String ROUTE = "conditions";

	private final static String LANGUAGE = "language";

	private MediatorComponentImpl mediator;

	private final Object lockObject = new Object();

	private RouteConfigurationImpl currentConfiguration;
	
	private Hashtable configurations;
	
	/**
	 * @param id
	 * @param type
	 * @param nspace
	 * @param catego
	 * @param properties
	 * @param chain
	 */
	public ContentBasedRouting(MediatorComponentImpl mediator) {
		this.mediator = mediator;
	}


	public ContentBasedRouting evaluator(String evaluator){
		mediator.setProperty(LANGUAGE, evaluator);
		return this;
	}

	public ContentBasedRouting condition(String condition) {
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
	public ContentBasedRouting to(String listPort){
		synchronized (lockObject) {
			if (currentConfiguration == null) {
				currentConfiguration = new RouteConfigurationImpl();
			}
			currentConfiguration.port(listPort);
		}
		return done();
	}

	private ContentBasedRouting done(){
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

	public ContentBasedRouting remove(String condition){
		Map configurations;
		synchronized (lockObject) {
			currentConfiguration = new RouteConfigurationImpl();
			currentConfiguration.condition(condition);
			configurations = configurations();
			if (configurations.containsKey(currentConfiguration)) {
				configurations.remove(currentConfiguration);
			}
			currentConfiguration = null;
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


	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.CustomBuilderConfigurator#getProperties()
	 */
	@Override
	public Hashtable getProperties() {
		// TODO Auto-generated method stub
		return null;
	}


	


}
