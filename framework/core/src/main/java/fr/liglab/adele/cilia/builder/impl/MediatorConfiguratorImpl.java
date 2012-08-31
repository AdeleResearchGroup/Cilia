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
package fr.liglab.adele.cilia.builder.impl;

import java.util.Hashtable;

import fr.liglab.adele.cilia.builder.ConfiguratorValueSetter;
import fr.liglab.adele.cilia.builder.CustomBuilderConfigurator;
import fr.liglab.adele.cilia.builder.MediatorConfigurator;
import fr.liglab.adele.cilia.exceptions.BuilderException;

/**
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class MediatorConfiguratorImpl implements MediatorConfigurator {

	private String temporalKey;

	private int temporalSet ;

	protected Hashtable sharedprops;

	protected Hashtable schedulerprops;
	//protected Hashtable processorprops;
	protected Hashtable dispatcherprops;

	public MediatorConfiguratorImpl() {
		temporalSet = MediatorConfigurator.SHARED;
		sharedprops = new Hashtable();
		schedulerprops = new Hashtable();
		dispatcherprops = new Hashtable();
	}


	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#key(java.lang.String)
	 */
	public ConfiguratorValueSetter key(String name) {
		temporalKey = name;
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#value(java.lang.Object)
	 */
	public MediatorConfigurator value(Object value) throws BuilderException {
		if (temporalKey == null) {
			throw new BuilderException("Unable to assign a value to an empty key when configuring builder");
		}
		configureSet(temporalSet, temporalKey, value);
		temporalKey = null;
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#set(java.util.Dictionary)
	 */
	public MediatorConfigurator set(Hashtable props) {
		if(props != null){
			configureSet(temporalSet, props);
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#scheduler(fr.liglab.adele.cilia.builder.CustomBuilderConfigurator)
	 */
	public MediatorConfigurator scheduler(CustomBuilderConfigurator conf) {
		Hashtable nprops = conf.properties();
		if (nprops != null) {
			configureSet(SCHEDULER, nprops);
		}
		shared();
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#processor(fr.liglab.adele.cilia.builder.CustomBuilderConfigurator)
	 */
	public MediatorConfigurator processor(CustomBuilderConfigurator conf) {
		Hashtable nprops = conf.properties();
		if (nprops != null) {
			configureSet(PROCESSOR, nprops);
		}
		shared();
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#dispatcher(fr.liglab.adele.cilia.builder.CustomBuilderConfigurator)
	 */
	public MediatorConfigurator dispatcher(CustomBuilderConfigurator conf) {
		Hashtable nprops = conf.properties();
		if (nprops != null) {
			configureSet(DISPATCHER, nprops);
		}
		shared();
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#shared()
	 */
	public MediatorConfigurator shared() {
		temporalSet = SHARED;
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#scheduler()
	 */
	public MediatorConfigurator scheduler() {
		temporalSet = SCHEDULER;
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#processor()
	 */
	public MediatorConfigurator processor() {
		temporalSet = PROCESSOR;
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#dispatcher()
	 */
	public MediatorConfigurator dispatcher() {
		temporalSet = DISPATCHER;
		return this;
	}

	private void configureSet(int set, Hashtable props) {
		switch (set) {
		case SCHEDULER : 
			schedulerprops.putAll(props);
			break;
		case DISPATCHER :
			dispatcherprops.putAll(props);
			break;
		case PROCESSOR:
			sharedprops.putAll(props);
			break;
		default:
			sharedprops.putAll(props);
			break;
		}
	}
	
	private void configureSet(int set, String key, Object value) {
		switch (set) {
		case SCHEDULER : 
			schedulerprops.put(key, value);
			break;
		case DISPATCHER :
			dispatcherprops.put(key, value);
			break;
		case PROCESSOR:
			sharedprops.put(key, value);
			break;
		default:
			sharedprops.put(key, value);
			break;
		}
	}
	
	protected Hashtable getConfiguration() {
		Hashtable props = new Hashtable();
		props.putAll(sharedprops);
		props.putAll(dispatcherprops);
		props.putAll(schedulerprops);
		//props.put("scheduler.properties", schedulerprops);
		//props.put("dispatcher.properties", dispatcherprops);
		return props;
	}

}
