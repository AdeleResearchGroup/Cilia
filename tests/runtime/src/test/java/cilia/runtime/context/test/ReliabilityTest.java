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

import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

import java.net.URL;
import java.util.List;

import org.apache.felix.ipojo.test.helpers.IPOJOHelper;
import org.apache.felix.ipojo.test.helpers.OSGiHelper;
import org.junit.After;
import org.junit.Assert;
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
@RunWith(JUnit4TestRunner.class)

public class ReliabilityTest {

	@Inject
	private BundleContext context;

	private OSGiHelper osgi;
	
	private CiliaHelper cilia;
	
	private IPOJOHelper ipojo;

	@Before
	public void setUp() {
		osgi = new OSGiHelper(context);
		cilia = new CiliaHelper(context);
		ipojo = new IPOJOHelper(context);
	}

	@After
	public void tearDown() {
		osgi.dispose();
		cilia.dispose();
		ipojo.dispose();
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
						mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.eventadmin").version("1.2.14"),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-core").versionAsInProject(),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-runtime").versionAsInProject(),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-helper").versionAsInProject(),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("ea-adapter").versionAsInProject()
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
	
	@Test
	public void replaceWithoutLoose() {
		CiliaHelper.waitSomeTime(2000);
		createEnricherMediator();
		String chainID = "reliableTest";
		URL url = context.getBundle().getResource("reliableTest.dscilia");
		cilia.load(url);
		//wait to be added.
		System.out.println("will wait");
		boolean found = cilia.waitToChain(chainID,10000);
		MediatorTestHelper qd = cilia.instrumentChain(chainID, "firstMediator:unique", "lastMediator:unique");
		//chain must exist, and helper should be well constructed.
		Assert.assertNotNull(qd);
		qd.injectData(new Data ("data ONE", "dda"));
		CiliaHelper.waitSomeTime(100);
		Assert.assertEquals(0, qd.getAmountData());
		qd.injectData(new Data ("data TWO", "dda"));
		CiliaHelper.waitSomeTime(100);
		//wait some time to arrive message.
		
		//Now we replace component
		Builder b = cilia.getBuilder();
		Architecture arch = null;
		try {
			arch = b.get(chainID);
		} catch (BuilderException e) {
			Assert.fail("Unable to load chain" + chainID);
		}
		try {
			arch.replace().id("enricher1").to("enricher2");
		} catch (BuilderException e) {
			Assert.fail("Replace operation in builder fail" + chainID);
		}
		try {
			b.done();
		} catch (CiliaException e) {
			Assert.fail("Fail while done builder performer" + chainID);
		}
		//We inject the last data. Now processing must be performed on replaced mediator.
		Assert.assertEquals(0, qd.getAmountData());
		qd.injectData(new Data ("data THREE", "dda"));
		CiliaHelper.waitSomeTime(100);
		Assert.assertEquals(3, qd.getAmountData());
		
		Data lastData = qd.getLastData();
		Assert.assertEquals("data THREE", lastData.getContent());
		Assert.assertEquals("dda", lastData.getName());
	
		Assert.assertEquals("new enricher content", lastData.getProperty("enricher"));
		System.out.println("Received NEW processed data: " + lastData.getAllData());
	}
	
	@Test
	public void testCount() {
		CiliaHelper.waitSomeTime(2000);
		createEnricherMediator();
		URL url = context.getBundle().getResource("reliableTest.dscilia");
		cilia.load(url);
		//wait to be added.
		System.out.println("will wait");
		boolean found = cilia.waitToChain("reliableTest",10000);
		MediatorTestHelper qd = cilia.instrumentChain("reliableTest", "firstMediator:unique", "lastMediator:unique");
		//chain must exist, and helper should be well constructed.
		Assert.assertNotNull(qd);
		qd.injectData(new Data ("data ONE", "dda"));
		CiliaHelper.waitSomeTime(100);
		Assert.assertEquals(0, qd.getAmountData());
		qd.injectData(new Data ("data TWO", "dda"));
		CiliaHelper.waitSomeTime(100);
		Assert.assertEquals(0, qd.getAmountData());
		qd.injectData(new Data ("data THREE", "dda"));
		//wait some time to arrive message.
		CiliaHelper.waitSomeTime(100);
		Assert.assertEquals(3, qd.getAmountData());
		
		
		Data lastData = qd.getLastData();
		Assert.assertEquals("data THREE", lastData.getContent());
		Assert.assertEquals("dda", lastData.getName());
		System.out.println("Received data: " + lastData.getAllData());
	}
	
	
	private void createEnricherMediator(){
		MediatorRuntimeSpecification mrs = new MediatorRuntimeSpecification("ReliableTest", null, null, context);
		mrs.setDispatcher("multicast-dispatcher", "fr.liglab.adele.cilia");
		mrs.setScheduler("counter-scheduler", "fr.liglab.adele.cilia");
		mrs.setProcessor("SimpleEnricherProcessor", "fr.liglab.adele.cilia");
		mrs.setInPort("unique", "*");
		mrs.setOutPort("unique", "*");
		mrs.initializeSpecification();
	}
	
}
