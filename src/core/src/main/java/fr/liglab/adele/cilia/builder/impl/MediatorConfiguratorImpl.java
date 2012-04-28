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
	protected Hashtable processorprops;
	protected Hashtable dispatcherprops;

	public MediatorConfiguratorImpl() {
		temporalSet = MediatorConfigurator.SHARED;
		sharedprops = new Hashtable();
		schedulerprops = new Hashtable();
		processorprops = new Hashtable();
		dispatcherprops = new Hashtable();
	}


	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#key(java.lang.String)
	 */
	@Override
	public ConfiguratorValueSetter key(String name) {
		temporalKey = name;
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#value(java.lang.Object)
	 */
	@Override
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
	@Override
	public MediatorConfigurator set(Hashtable props) {
		configureSet(temporalSet, props);
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#scheduler(fr.liglab.adele.cilia.builder.CustomBuilderConfigurator)
	 */
	@Override
	public MediatorConfigurator scheduler(CustomBuilderConfigurator conf) {
		Hashtable nprops = conf.getProperties();
		if (nprops != null) {
			configureSet(SCHEDULER, nprops);
		}
		shared();
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#processor(fr.liglab.adele.cilia.builder.CustomBuilderConfigurator)
	 */
	@Override
	public MediatorConfigurator processor(CustomBuilderConfigurator conf) {
		Hashtable nprops = conf.getProperties();
		if (nprops != null) {
			configureSet(PROCESSOR, nprops);
		}
		shared();
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#dispatcher(fr.liglab.adele.cilia.builder.CustomBuilderConfigurator)
	 */
	@Override
	public MediatorConfigurator dispatcher(CustomBuilderConfigurator conf) {
		Hashtable nprops = conf.getProperties();
		if (nprops != null) {
			configureSet(DISPATCHER, nprops);
		}
		shared();
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#shared()
	 */
	@Override
	public MediatorConfigurator shared() {
		temporalSet = SHARED;
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#scheduler()
	 */
	@Override
	public MediatorConfigurator scheduler() {
		temporalSet = SCHEDULER;
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#processor()
	 */
	@Override
	public MediatorConfigurator processor() {
		temporalSet = PROCESSOR;
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Configurator#dispatcher()
	 */
	@Override
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
			processorprops.putAll(props);
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
			processorprops.put(key, value);
			break;
		default:
			sharedprops.put(key, value);
			break;
		}
	}
	
	protected Hashtable getConfiguration() {
		Hashtable props = new Hashtable();
		props.putAll(sharedprops);
		props.put("scheduler.properties", schedulerprops);
		props.put("dispatcher.properties", dispatcherprops);
		props.put("processor.properties", processorprops);
		return props;
	}

}
