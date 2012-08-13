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
/**
 * 
 */
package fr.liglab.adele.cilia.helper.impl;

import java.util.Hashtable;
import java.util.List;

import junit.framework.Assert;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.test.helpers.OSGiHelper;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.BuilderConfigurationException;
import fr.liglab.adele.cilia.exceptions.BuilderException;
import fr.liglab.adele.cilia.exceptions.BuilderPerformerException;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.helper.DispatcherProcessorHelper;
import fr.liglab.adele.cilia.helper.ProcessorHelper;
import fr.liglab.adele.cilia.helper.SchedulerProcessorHelper;
import fr.liglab.adele.cilia.runtime.MediatorRuntimeSpecification;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class ProcessorHelperImpl implements ProcessorHelper {

	CiliaHelper cilia;
	
	String mediatorType;
	
	String processorName;
	
	String processorNamespace;
	
	Hashtable properties;
	
	private volatile static int id = 0;
	
	private final static String SCHEDULERNAME="scheduler-helper";
	private final static String DISPATCHERNAME="dispatcher-helper";
	private final static String TESTNAMESPACE="fr.liglab.adele.cilia.test";
	
	SchedulerProcessorHelper scheduler;
	
	DispatcherProcessorHelper dispatcher;
	
	public ProcessorHelperImpl(CiliaHelper helper, String processorname, String processornamespace, Hashtable properties){
		this.processorName= processorname;
		this.processorNamespace = processornamespace;
		this.properties = properties;
		mediatorType=processorname+"_"+processornamespace+":"+id;
		id ++;
		cilia = helper;
		createHelperMediator();
	}
	
	public void start() {
		try {
			startChain();
		} catch (CiliaException e) {
			e.printStackTrace();
			Assert.fail("Unable to create processor helper" + e.getMessage());
		}
		locateServices();
	}
	
	private void createHelperMediator(){
		MediatorRuntimeSpecification mrs = new MediatorRuntimeSpecification(mediatorType, TESTNAMESPACE, null, cilia.getBundleContext());
		mrs.setDispatcher(DISPATCHERNAME, TESTNAMESPACE);
		mrs.setScheduler(SCHEDULERNAME, TESTNAMESPACE);
		mrs.setProcessor(processorName, processorNamespace);
		mrs.setInPort("unique", "*");
		mrs.setOutPort("unique", "*");
		mrs.initializeSpecification();
		testFactories();
	}
	
	private void testFactories(){
		CiliaHelper.waitSomeTime(1000);
		OSGiHelper osgi = cilia.getOSGIHelper();
		//Processor Factory
		osgi.waitForService(Factory.class.getName(), "(&(processor.name="+processorName+")(processor.namespace="+processorNamespace+"))",4000);
		osgi.waitForService(Factory.class.getName(), "(&(processor.name="+processorName+")(factory.state=1))",4000);
		//Scheduler Factory
		osgi.waitForService(Factory.class.getName(), "(&(scheduler.name="+SCHEDULERNAME+")(scheduler.namespace="+TESTNAMESPACE+"))",4000);
		osgi.waitForService(Factory.class.getName(), "(&(scheduler.name="+SCHEDULERNAME+")(factory.state=1))",4000);
		//Dispatcher Factory
		osgi.waitForService(Factory.class.getName(), "(&(dispatcher.name="+DISPATCHERNAME+")(dispatcher.namespace="+TESTNAMESPACE+"))",4000);
		osgi.waitForService(Factory.class.getName(), "(&(dispatcher.name="+DISPATCHERNAME+")(factory.state=1))",4000);
		//Mediator Factory
		osgi.waitForService(Factory.class.getName(), "(&(mediator.name="+mediatorType+")(mediator.namespace="+TESTNAMESPACE+"))",4000);
		osgi.waitForService(Factory.class.getName(), "(&(mediator.name="+mediatorType+")(factory.state=1))",4000);
	}
	
	private void startChain() throws BuilderException, BuilderConfigurationException, BuilderPerformerException{
		String id = "chain_"+mediatorType;
		Builder b = cilia.getBuilder();
		Architecture chain = b.create(id);
		chain.create().mediator().type(mediatorType).id(mediatorType).configure().key("identifier").value(mediatorType);
		if (properties != null) {
			chain.configure().mediator().id(mediatorType).set(new Hashtable(properties));
		}
		b.done();
		cilia.startChain(id);
	}
	
	private void locateServices(){
		OSGiHelper osgi = cilia.getOSGIHelper();
		osgi.waitForService(DispatcherProcessorHelper.class.getName(), "(identifier="+mediatorType+")",4000);
		dispatcher = (DispatcherProcessorHelper) osgi.getServiceObject(DispatcherProcessorHelper.class.getName(), "(identifier="+mediatorType+")");
		osgi.waitForService(SchedulerProcessorHelper.class.getName(), "(identifier="+mediatorType+")",4000);
		scheduler = (SchedulerProcessorHelper) osgi.getServiceObject(SchedulerProcessorHelper.class.getName(), "(identifier="+mediatorType+")");
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.helper.DispatcherHelper#clear()
	 */
	public void clear() {
		dispatcher.clear();
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.helper.DispatcherHelper#getData()
	 */
	public List getData() {
		return dispatcher.getData();
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.helper.DispatcherHelper#getAmountData()
	 */
	public int getAmountData() {
		return dispatcher.getAmountData();
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.helper.DispatcherHelper#getLastData()
	 */
	public Data getLastData() {
		return dispatcher.getLastData();
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.helper.SchedulerHelper#trigger()
	 */
	public void trigger() {
		scheduler.trigger();
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.helper.SchedulerHelper#notifyData(fr.liglab.adele.cilia.Data)
	 */
	public void notifyData(Data data) {
		scheduler.notifyData(data);
	}
	
	public void notifyData(Data [] data){
		for (int i = 0; i < data.length; i ++){
			notifyData(data[i]);
		}
	}


	
}
