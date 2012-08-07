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
package cilia.framework.processors.tests;

import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

import java.net.URL;
import java.util.List;

import junit.framework.Assert;

import org.apache.felix.ipojo.test.helpers.OSGiHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.junit.JUnitOptions;
import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.helper.MediatorTestHelper;
import fr.liglab.adele.cilia.helper.ProcessorHelper;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.runtime.MediatorRuntimeSpecification;

/**
 *This class will test the behaviour of processors.
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
@RunWith(JUnit4TestRunner.class)
public class ProcessorsTest {

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
		osgi.dispose();
		cilia.dispose();
	}

	@Configuration
	public static Option[] configure() {
		Option[] platform = options(felix());

		Option[] bundles = options(
				provision(mavenBundle().groupId(
						"org.apache.felix").artifactId("org.apache.felix.ipojo").versionAsInProject(), 
						mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo.test.helpers").versionAsInProject(),
						mavenBundle().groupId("org.osgi").artifactId("org.osgi.compendium").versionAsInProject(),
						mavenBundle().groupId("org.slf4j").artifactId("slf4j-api").versionAsInProject(),
						mavenBundle().groupId("org.slf4j").artifactId("slf4j-simple").version("1.6.1"),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-runtime").versionAsInProject(),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-helper").versionAsInProject(),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-core").versionAsInProject()
						)); // The target
		Option[] r = OptionUtils.combine(platform, bundles);
		return r;
	}

	/**
	 * Mockito bundle
	 * 
	 * @return
	 */
	@Configuration
	public static Option[] mockitoBundle() {
		return options(JUnitOptions.mockitoBundles());
	}
	
	/**
	 * Test the SimpleEnricherProcessor
	 */
	@Test
	public void enricherTest() {
		CiliaHelper.waitSomeTime(2000);
		createEnricherMediator();
		CiliaHelper.waitSomeTime(1000);
		URL url = context.getBundle().getResource("EnricherTest.dscilia");
		cilia.load(url);
		System.out.println("will wait");
		boolean found = cilia.waitToChain("toto",3000);
		//first enricher value
		MediatorComponent enricher1 = cilia.getMediatorModel("toto", "enricher1");
		Assert.assertEquals(true, enricher1.isRunning());
		Assert.assertEquals(MediatorComponent.VALID, enricher1.getState());
		MediatorTestHelper tester = cilia.instrumentChain("toto","enricher1:unique", "enricher1:unique");
		
		Assert.assertNotNull(tester);
		Assert.assertEquals(true, tester.injectData(new Data("data content", "data name")));
		
		Data data = tester.getLastData();
		Assert.assertNotNull(data);
		String enricherValue = (String)data.getProperty("enricher");
		Assert.assertEquals("enricher1", enricherValue);
		
		MediatorTestHelper tester2 = cilia.instrumentChain("toto","enricher2:unique", "enricher2:unique");
		Assert.assertNotNull(tester2);
		Assert.assertEquals(true, tester2.injectData(new Data("data content", "data name")));
		Data data2 = tester2.getLastData();
		Assert.assertNotNull(data2);
		String enricherValue2 = (String)data2.getProperty("enricher");
		Assert.assertEquals("enricher2", enricherValue2);

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
	/**
	 * Test the aggregator processor
	 */
	@Test
	public void testAggregatorProcessor(){
		CiliaHelper.waitSomeTime(2000);
		ProcessorHelper helper = cilia.getProcessorHelper("AggregatorProcessor", "fr.liglab.adele.cilia");
		helper.notifyData(new Data("Data One",""));
		helper.notifyData(new Data("Data Two",""));
		helper.notifyData(new Data("Data three",""));
		helper.trigger();
		Assert.assertEquals(1, helper.getAmountData());
		//It must retrieve the List of data
		Data data = helper.getLastData();
		Assert.assertNotNull(data);
		List<Data> list = (List<Data>)data.getContent();
		//There must be three messages
		Assert.assertEquals(3, list.size());
		Data thirdData = list.get(list.size()-1);
		Assert.assertEquals("Data three", thirdData.getContent());
	}
	

}
