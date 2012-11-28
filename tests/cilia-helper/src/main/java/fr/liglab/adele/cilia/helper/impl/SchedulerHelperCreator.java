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
import fr.liglab.adele.cilia.exceptions.BuilderException;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.helper.MediatorTestHelper;
import fr.liglab.adele.cilia.runtime.MediatorRuntimeSpecification;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class SchedulerHelperCreator {

	CiliaHelper cilia;

	String mediatorType;

	String schedulername;

	String schedulernamespace;

	Hashtable properties;

	private volatile static int idnumber = 0;

	private final static String PROCESSORNAME="simple-processor";
	private final static String DISPATCHERNAME="multicast-dispatcher";
	private final static String TESTNAMESPACE="fr.liglab.adele.cilia.test";
	private final static String CILIANAMESPACE="fr.liglab.adele.cilia";

	/**
	 * @param ciliaHelper
	 * @param schedulername
	 * @param schedulernamespace
	 * @param properties
	 */
	public SchedulerHelperCreator(CiliaHelper ciliaHelper, String schedulername,
			String schedulernamespace, Hashtable properties) {
		this.schedulername= schedulername;
		this.schedulernamespace = schedulernamespace;
		this.properties = properties;
		mediatorType=schedulername+"_"+schedulernamespace+"_"+idnumber;
		idnumber ++;
		cilia = ciliaHelper;
		createHelperMediator();	
	}

	public void start() {
		try {
			startChain();
		} catch (CiliaException e) {
			e.printStackTrace();
			Assert.fail("Unable to create helper to scheduler");
		}
	}


	/**
	 * @throws BuilderException 
	 * 
	 */
	private void startChain() throws CiliaException {
		String id = "chain_"+mediatorType;
		Builder b = cilia.getBuilder();
		Architecture chain = b.create(id);
		//We create the new mediator to inspect scheduler behavior 
		chain.create().mediator().type(mediatorType).namespace(TESTNAMESPACE).id(mediatorType);
		if (properties != null) {
			chain.configure().mediator().id(mediatorType).set(new Hashtable(properties));
		}
		chain.create().adapter().type("cilia-adapter-helper")
		.namespace(TESTNAMESPACE).id(id).configure().key("identifier").value(mediatorType);
		chain.bind().from(id+":unique").to(mediatorType+":unique");
		chain.bind().from(mediatorType+":unique").to(id+":unique");
		b.done();
		cilia.startChain(id);
	}

	public String getId() {
		return mediatorType;
	}

	private void createHelperMediator(){
		MediatorRuntimeSpecification mrs = new MediatorRuntimeSpecification(mediatorType, TESTNAMESPACE, null, cilia.getBundleContext());
		mrs.setDispatcher(DISPATCHERNAME, CILIANAMESPACE);
		mrs.setScheduler(schedulername, schedulernamespace);
		mrs.setProcessor(PROCESSORNAME, CILIANAMESPACE);
		mrs.setInPort("unique", "*");
		mrs.setOutPort("unique", "*");
		mrs.initializeSpecification();
		testFactories();
	}


	private void testFactories(){
		//CiliaHelper.waitSomeTime(1000);
		OSGiHelper osgi = cilia.getOSGIHelper();
		//Scheduler Factory
		osgi.waitForService(Factory.class.getName(), "(&(scheduler.name="+schedulername+")(scheduler.namespace="+schedulernamespace+"))",4000);
		osgi.waitForService(Factory.class.getName(), "(&(scheduler.name="+schedulername+")(factory.state=1))",4000);
		//Mediator Factory
		osgi.waitForService(Factory.class.getName(), "(&(mediator.name="+mediatorType+")(mediator.namespace="+TESTNAMESPACE+"))",4000);
		osgi.waitForService(Factory.class.getName(), "(&(mediator.name="+mediatorType+")(factory.state=1))",4000);
	}

}
