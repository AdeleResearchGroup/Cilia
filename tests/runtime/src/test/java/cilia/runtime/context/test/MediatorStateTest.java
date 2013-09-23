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
import fr.liglab.adele.commons.distribution.test.AbstractDistributionBaseTest;
import junit.framework.Assert;
import org.apache.felix.ipojo.test.helpers.OSGiHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@Ignore
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class MediatorStateTest  extends AbstractDistributionBaseTest {
	

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


    public static Option helpBundles() {

        return new DefaultCompositeOption(
                mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo.test.helpers").versionAsInProject(),
                mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-helper").versionAsInProject()
        );
    }

    @org.ops4j.pax.exam.Configuration
    public Option[] configuration() {

        List<Option> lst = super.config();
        lst.add(helpBundles());
        Option conf[] = lst.toArray(new Option[0]);
        return conf;
    }



	@Test
	public void mediatorInvalidState() {
		CiliaHelper.waitSomeTime(2000);
		URL url = context.getBundle().getResource("test1.dscilia");
		cilia.load(url);
		System.out.println("will wait");
		boolean found = cilia.waitToChain("toto",3000);
		System.out.println("found chain "+ found);
		MediatorComponent toto = cilia.getMediatorModel("toto", "toto");
		
		Assert.assertEquals(false,toto.isRunning());
		Assert.assertEquals(MediatorComponent.State.INVALID, toto.getState());
	}
	@Test
	public void mediatorDisposedState() {
		CiliaHelper.waitSomeTime(2000);
		URL url = context.getBundle().getResource("test1.dscilia");
		cilia.load(url);
		System.out.println("will wait");
		boolean found = cilia.waitToChain("toto",3000);
		System.out.println("found chain "+ found);
		try {
			cilia.getCiliaContext().getApplicationRuntime().stopChain("toto");
		} catch (CiliaIllegalParameterException e) {
			e.printStackTrace();
		} catch (CiliaIllegalStateException e) {
			e.printStackTrace();
		}
		MediatorComponent toto = cilia.getMediatorModel("toto", "toto");

		Assert.assertEquals(false,toto.isRunning());
		Assert.assertEquals(MediatorComponent.State.DISPOSED, toto.getState());
	}
	@Test
	public void mediatorStopped(){
		CiliaHelper.waitSomeTime(2000);
		MediatorComponent titi = null;
		try {
			Builder b = cilia.getCiliaContext().getBuilder();
			Architecture toto = b.create("toto");
			toto.create().mediator().type("titi").id("toto");
			b.done();
			titi = cilia.getMediatorModel("toto", "toto");
		} catch (CiliaException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(titi);
		Assert.assertEquals(false,titi.isRunning());
		Assert.assertEquals(MediatorComponent.State.STOPPED, titi.getState());
	}

	@Test
	public void mediatorValid(){
		CiliaHelper.waitSomeTime(2000);
		URL url = context.getBundle().getResource("test1.dscilia");
		cilia.load(url);
		System.out.println("will wait");
		boolean found = cilia.waitToChain("toto",3000);
		System.out.println("found chain "+ found);
		MediatorComponent toto = cilia.getMediatorModel("toto", "validToto");
		Assert.assertEquals(true,toto.isRunning());
		Assert.assertEquals(MediatorComponent.State.VALID, toto.getState());
	}
	@Test
	public void mediatorInvalidSAfterCreateSpecification() {
		CiliaHelper.waitSomeTime(2000);
		URL url = context.getBundle().getResource("test1.dscilia");
		cilia.load(url);
		System.out.println("will wait");
		boolean found = cilia.waitToChain("toto",3000);
		System.out.println("found chain "+ found);
		MediatorComponent toto = cilia.getMediatorModel("toto", "toto");
		System.out.println("Toto is running");
		Assert.assertEquals(false,toto.isRunning());
		Assert.assertEquals(MediatorComponent.State.INVALID, toto.getState());
		//Now we create the toto mediator.
		createTotoMediator();
		CiliaHelper.waitSomeTime(3000);
		Assert.assertEquals(true,toto.isRunning());
		Assert.assertEquals(MediatorComponent.State.VALID, toto.getState());
	}
	
	private void createTotoMediator(){
		MediatorRuntimeSpecification mrs = new MediatorRuntimeSpecification("toto", null, null, context);
		mrs.setDispatcher("multicast-dispatcher", "fr.liglab.adele.cilia");
		mrs.setScheduler("immediate-scheduler", "fr.liglab.adele.cilia");
		mrs.setProcessor("simple-processor", "fr.liglab.adele.cilia");
		mrs.setInPort("unique", "*");
		mrs.setOutPort("unique", "*");
		mrs.initializeSpecification();
	}
}
