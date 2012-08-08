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
import java.util.Hashtable;
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
	public void enricherProcessorTest() {
		CiliaHelper.waitSomeTime(2000);
		
		Hashtable enricher = new Hashtable();
		enricher.put("enricher", "enricher1");
		Hashtable properties = new Hashtable();
		properties.put("enricher.content", enricher);
		
		ProcessorHelper helper = cilia.getProcessorHelper("SimpleEnricherProcessor", "fr.liglab.adele.cilia", properties);
		
		Assert.assertNotNull(helper);
		
		helper.notifyData(new Data("data content", "data name"));
		helper.trigger();
		
		Data data = helper.getLastData();
		Assert.assertNotNull(data);
		String enricherValue = (String)data.getProperty("enricher");
		Assert.assertEquals("enricher1", enricherValue);
		
	}
	
	/**
	 * Test the AggregatorProcessor
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
	
	/**
	 * Test the behavior of SemanticTranslatorProcessor
	 */
	@Test
	public void testLocalSemanticProcessor () {
		
		Hashtable<String, String> dictionary = new Hashtable<String, String>();
		dictionary.put("casa", "house");
		dictionary.put("gato", "cat");
		dictionary.put("perro", "dog");
		dictionary.put("verde", "green");
		Hashtable<String, Hashtable<String, String>> properties = new Hashtable<String, Hashtable<String, String>>();
		properties.put("dictionary", dictionary);
		
		CiliaHelper.waitSomeTime(2000);
		ProcessorHelper helper = cilia.getProcessorHelper("SemanticTranslatorProcessor", "fr.liglab.adele.cilia", properties);
		helper.notifyData(new Data("La casa es verde","contenido"));
		helper.trigger();
		Assert.assertEquals(1, helper.getAmountData());
		//It must retrieve the List of data
		Data data = helper.getLastData();
		Assert.assertEquals("La house es green", data.getContent());
	}
	
	/**
	 * Test the behavior of SemanticTranslatorProcessor
	 */
	@Test
	public void testPrefixEnricher () {
		Hashtable<String, String> properties = new Hashtable<String,String>();
		properties.put("enricher.prefix", "This is before");
		CiliaHelper.waitSomeTime(2000);
		ProcessorHelper helper = cilia.getProcessorHelper("PrefixEnricherProcessor", "fr.liglab.adele.cilia", properties);
		helper.notifyData(new Data(" the following message","contenido"));
		helper.trigger();
		Assert.assertEquals(1, helper.getAmountData());
		Data data = helper.getLastData();
		Assert.assertEquals("This is before the following message", data.getContent());
		System.out.println(data.getContent());
	}
	
	/**
	 * Test the behavior of SufixEnricher
	 */
	@Test
	public void testSufixEnricher () {
		Hashtable<String, String> properties = new Hashtable<String,String>();
		properties.put("enricher.sufix", " is after");
		CiliaHelper.waitSomeTime(2000);
		ProcessorHelper helper = cilia.getProcessorHelper("SufixEnricherProcessor", "fr.liglab.adele.cilia", properties);
		helper.notifyData(new Data("The following message","contenido"));
		helper.trigger();
		Assert.assertEquals(1, helper.getAmountData());
		Data data = helper.getLastData();
		Assert.assertEquals("The following message is after", data.getContent());
		System.out.println(data.getContent());
	}

	/**
	 * Test the behavior of SufixEnricher
	 */
	@Test
	public void testSufixEnricher2 () {
		Hashtable<String, String> properties = new Hashtable<String,String>();
		properties.put("sufix", " is after");
		CiliaHelper.waitSomeTime(2000);
		ProcessorHelper helper = cilia.getProcessorHelper("SufixEnricherProcessor", "fr.liglab.adele.cilia", properties);
		helper.notifyData(new Data("The following message","contenido"));
		helper.trigger();
		Assert.assertEquals(1, helper.getAmountData());
		Data data = helper.getLastData();
		Assert.assertEquals("The following message is after", data.getContent());
		System.out.println(data.getContent());
	}
	
	/**
	 * Test the behavior of StringSplitterProcessor
	 */
	@Test
	public void testSplitterProcessor() {
		Hashtable<String, String> properties = new Hashtable<String,String>();
		properties.put("separator", "/");
		CiliaHelper.waitSomeTime(2000);
		ProcessorHelper helper = cilia.getProcessorHelper("StringSplitterProcessor", "fr.liglab.adele.cilia", properties);
		helper.notifyData(new Data("The/following/message/will/be/splitted","contenido"));
		helper.trigger();
		//It process one message and returns 6
		Assert.assertEquals(6, helper.getAmountData());
		Data data = helper.getLastData();
		Assert.assertEquals("splitted", data.getContent());
		List<Data> alldata = helper.getData();
		//check the order.
		Assert.assertEquals("The",alldata.get(0).getContent());
		Assert.assertEquals("following",alldata.get(1).getContent());
		Assert.assertEquals("message",alldata.get(2).getContent());
		Assert.assertEquals("will",alldata.get(3).getContent());
		Assert.assertEquals("be",alldata.get(4).getContent());
		Assert.assertEquals("splitted",alldata.get(5).getContent());
		System.out.println(data.getContent());
	}
	
}
