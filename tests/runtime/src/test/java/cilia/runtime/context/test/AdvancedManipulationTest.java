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
package cilia.runtime.context.test;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.helper.MediatorTestHelper;
import fr.liglab.adele.cilia.runtime.MediatorRuntimeSpecification;
import fr.liglab.adele.commons.distribution.test.AbstractDistributionBaseTest;
import junit.framework.Assert;
import org.apache.felix.ipojo.test.helpers.OSGiHelper;
import org.junit.After;
import org.junit.Before;
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
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class AdvancedManipulationTest  extends AbstractDistributionBaseTest {

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
	public void replaceTest(){
		CiliaHelper.waitSomeTime(2000);
		createEnricherMediator();
		CiliaHelper.waitSomeTime(1000);
		URL url = context.getBundle().getResource("ReplacerTest.dscilia");
		cilia.load(url);
		System.out.println("will wait");
		cilia.waitToChain("replacerExample",6000);
		//get the helper to inject and retrieve result.
		MediatorTestHelper tester = cilia.instrumentChain("replacerExample","firstMediator:unique", "lastMediator:unique");
		Assert.assertNotNull(tester);
		Assert.assertEquals(true, tester.injectData(new Data("data content", "data name")));
		//get the resulting data.
        CiliaHelper.checkReceived(tester, 1, 2000);
		Data data = tester.getData().get(0);
		Assert.assertNotNull(data);
		String enricherValue = (String)data.getProperty("enricher");
		//test the value based on the replacerTest.dscilia file
		/*<item key="enricher" value="enricher1"/>*/
		Assert.assertEquals("enricher1", enricherValue);
		tester.getAmountData();
		//Now do the replace of enricher1 to enricher2, the enricher2 is already instantiated.
		System.out.println("To start replacment");
		CiliaHelper.waitSomeTime(3000);
		Builder b = cilia.getBuilder();
		try {
			b.get("replacerExample").replace().id("enricher1").to("enricher2");
			b.done();
		} catch (CiliaException e) {
			Assert.fail("Exception when replacing");
		}
		System.out.println("Finish replacment");
		//We inject a new Data.
		CiliaHelper.waitSomeTime(5000);
		System.out.println("Inject second data");
		Assert.assertEquals(true, tester.injectData(new Data("data content", "data name")));
        CiliaHelper.checkReceived(tester, 1, 2000);
        System.out.println("Verify second data");
		Assert.assertEquals(1, tester.getAmountData());
		Data data2 = tester.getData().get(0);
		Assert.assertNotNull(data2);
		String enricherValue2 = (String)data2.getProperty("enricher");
		//test the value based on the replacerTest.dscilia file
		/*<item key="enricher" value="enricher1"/>*/
		Assert.assertEquals("enricher2", enricherValue2);
		CiliaHelper.waitSomeTime(1000);
		cilia.dispose();
	}
	
	@Test
	public void copyMediatorTest(){
		CiliaHelper.waitSomeTime(2000);
		createEnricherMediator();
		CiliaHelper.waitSomeTime(1000);
		URL url = context.getBundle().getResource("CopyTest.dscilia");
		cilia.load(url);
		System.out.println("will wait");
		Assert.assertTrue(cilia.waitToChain("copyExample",6000));
		
		//get the helper to inject and retrieve result.
		MediatorTestHelper tester = cilia.instrumentChain("copyExample","firstMediator:unique", "lastMediator:unique");
		Assert.assertNotNull(tester);
		Assert.assertEquals(true, tester.injectData(new Data("data content", "data name")));
		//get the resulting data.
        CiliaHelper.checkReceived(tester, 1, 2000);
        List<Data> data = tester.getData();
		Assert.assertEquals(0, data.size());

		Assert.assertEquals(0,tester.getAmountData());
		//Now do the copy from enricher1 to enricher2, the enricher2 does not exist.
		System.out.println("To start copy");
		CiliaHelper.waitSomeTime(3000);
		Builder b = cilia.getBuilder();
		try {
			Architecture arch = b.get("copyExample");
			arch.copy().id("enricher1").to("enricher2");
			//the copy will create a new mediator with id=enricher2
			//we make a binding.
			arch.bind().from("firstMediator:unique").to("enricher2:unique"); // bind to the copied mediator.
			arch.bind().from("enricher2:unique").to("lastMediator:unique"); // bind to the copied mediator.
			b.done();
		} catch (CiliaException e) {
			Assert.fail("Exception when copying " + e.getMessage());
		}
		System.out.println("Finish to copy");
		//We inject a new Data.
		CiliaHelper.waitSomeTime(5000);
		System.out.println("Inject second data");
		Assert.assertEquals(true, tester.injectData(new Data("data content", "data name")));
		System.out.println("Verify second data");
        CiliaHelper.checkReceived(tester, 1, 2000);
        Assert.assertEquals(1, tester.getAmountData());
		Data data2 = tester.getData().get(0);
		Assert.assertNotNull(data2);
		String enricherValue2 = (String)data2.getProperty("enricher");
		//test the value based on the replacerTest.dscilia file
		/*<item key="enricher" value="enricher1"/>*/
		Assert.assertEquals("enricher1", enricherValue2);
		CiliaHelper.waitSomeTime(1000);
		cilia.dispose();
	}
	
	
	private void createEnricherMediator(){
		MediatorRuntimeSpecification mrs = new MediatorRuntimeSpecification("EnricherTest", null, null, context);
		mrs.setDispatcher("multicast-dispatcher", "fr.liglab.adele.cilia");
		mrs.setScheduler("immediate-scheduler", "fr.liglab.adele.cilia");
		mrs.setProcessor("SimpleEnricherProcessor", "fr.liglab.adele.cilia");
		mrs.setInPort("unique", "*");
		mrs.setOutPort("unique", "*");
		mrs.initializeSpecification();
	}
}
