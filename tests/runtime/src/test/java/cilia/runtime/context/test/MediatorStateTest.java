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
package cilia.runtime.context.test;

import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.runtime.MediatorRuntimeSpecification;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.ow2.chameleon.wisdom.test.WisdomRunner;

import javax.inject.Inject;
import java.net.URL;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@RunWith(WisdomRunner.class)
public class MediatorStateTest  {
	

	@Inject
	private BundleContext context;

	private OSGiHelper osgi;
	
	private CiliaHelper cilia;

	@Before
	public void setUp() {
		osgi = new OSGiHelper(context);
		cilia = new CiliaHelper(context);
	}

	@After
	public void tearDown() {
		cilia.dispose();
		osgi.dispose();
	}




	@Test
	public void mediatorInvalidState() {
		CiliaHelper.waitSomeTime(2000);
		URL url = context.getBundle().getResource("mediatorInvalidState.dscilia");
		cilia.load(url);
		System.out.println("will wait");
		boolean found = cilia.waitToChain("mediatorInvalidState",3000);
		System.out.println("found chain "+ found);
        if(!cilia.waitToComponent("mediatorInvalidState", "toto", 1000)){
            Assert.fail("Unable to locate mediator toto");
        }
        MediatorComponent toto = cilia.getMediatorModel("mediatorInvalidState", "toto");
		Assert.assertEquals(false,toto.isRunning());
        if(!cilia.checkComponentState("mediatorInvalidState","toto", MediatorComponent.State.INVALID, 8000)){
            Assert.fail("Failed to retrieve toto component, expected state invalid. Current state is:" + toto.getState());
        }
        cilia.dispose();
	}
	@Test
	public void mediatorDisposedState() {
		CiliaHelper.waitSomeTime(2000);
		URL url = context.getBundle().getResource("mediatorDisposedState.dscilia");
		cilia.load(url);
		System.out.println("will wait");
		boolean found = cilia.waitToChain("mediatorDisposedState",3000);
		System.out.println("found chain "+ found);
		try {
			cilia.getCiliaContext().getApplicationRuntime().stopChain("mediatorDisposedState");
		} catch (CiliaIllegalParameterException e) {
			e.printStackTrace();
		} catch (CiliaIllegalStateException e) {
			e.printStackTrace();
		}
		MediatorComponent toto = cilia.getMediatorModel("mediatorDisposedState", "toto");
        if(!cilia.checkComponentState("mediatorDisposedState","toto", MediatorComponent.State.DISPOSED, 8000)){
            Assert.fail("Failed to retrieve toto component, expected state disposed. Current state is:" + toto.getState());
        }
		Assert.assertEquals(false,toto.isRunning());
		Assert.assertEquals(MediatorComponent.State.DISPOSED, toto.getState());
        cilia.dispose();
	}
	@Test
	public void mediatorStopped(){
		CiliaHelper.waitSomeTime(2000);
		MediatorComponent titi = null;
		try {
			Builder b = cilia.getCiliaContext().getBuilder();
			Architecture toto = b.create("mediatorStopped");
			toto.create().mediator().type("titi").id("toto");
			b.done();
			titi = cilia.getMediatorModel("mediatorStopped", "toto");
		} catch (CiliaException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(titi);
		Assert.assertEquals(false,titi.isRunning());
		Assert.assertEquals(MediatorComponent.State.STOPPED, titi.getState());
        cilia.dispose();
	}

	@Test
	public void mediatorValid(){
		CiliaHelper.waitSomeTime(2000);
		URL url = context.getBundle().getResource("mediatorValid.dscilia");
		cilia.load(url);
		System.out.println("will wait");
		boolean found = cilia.waitToChain("mediatorValid",3000);
		System.out.println("found chain "+ found);
		MediatorComponent toto = cilia.getMediatorModel("mediatorValid", "validToto");

        if(!cilia.checkComponentState("mediatorValid","validToto", MediatorComponent.State.VALID, 8000)){
            Assert.fail("Failed to retrieve toto component, expected state valid. Current state is:" + toto.getState());
        }
		Assert.assertEquals(true,toto.isRunning());
		Assert.assertEquals(MediatorComponent.State.VALID, toto.getState());
        cilia.dispose();
	}
	@Test
	public void mediatorInvalidSAfterCreateSpecification() {
		CiliaHelper.waitSomeTime(2000);
		URL url = context.getBundle().getResource("mediatorInvalidAfterCreateSpecification.dscilia");
		cilia.load(url);
		System.out.println("will wait");
		boolean found = cilia.waitToChain("mediatorInvalidAfterCreateSpecification",3000);
		System.out.println("found chain "+ found);
		MediatorComponent toto = cilia.getMediatorModel("mediatorInvalidAfterCreateSpecification", "toto");
		System.out.println("Toto is running");
        if(!cilia.checkComponentState("mediatorInvalidAfterCreateSpecification","toto", MediatorComponent.State.INVALID, 8000)){
            Assert.fail("Failed to retrieve toto component, expected state invalid. Current state is:" + toto.getState());
        }
		Assert.assertEquals(false,toto.isRunning());
		Assert.assertEquals(MediatorComponent.State.INVALID, toto.getState());
		//Now we create the toto mediator.
		createTotoMediator("newMediator");
        if(!cilia.checkComponentState("mediatorInvalidAfterCreateSpecification","toto", MediatorComponent.State.VALID, 8000)){
            Assert.fail("Failed to retrieve toto component, expected state valid. Current state is:" + toto.getState());
        }
		Assert.assertEquals(true,toto.isRunning());
		Assert.assertEquals(MediatorComponent.State.VALID, toto.getState());
        cilia.dispose();
	}
	
	private void createTotoMediator(String name){
		MediatorRuntimeSpecification mrs = new MediatorRuntimeSpecification(name, null, null, context);
		mrs.setDispatcher("multicast-dispatcher", "fr.liglab.adele.cilia");
		mrs.setScheduler("immediate-scheduler", "fr.liglab.adele.cilia");
		mrs.setProcessor("simple-processor", "fr.liglab.adele.cilia");
		mrs.setInPort("unique", "*");
		mrs.setOutPort("unique", "*");
		mrs.initializeSpecification();
	}
}
