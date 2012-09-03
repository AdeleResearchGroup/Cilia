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
package fr.liglab.adele.cilia.ea.test;

import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

import java.net.URL;
import java.util.Hashtable;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
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
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.CiliaBindingService;
import fr.liglab.adele.cilia.framework.ICollector;
import fr.liglab.adele.cilia.framework.ISender;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.helper.CollectorHelper;
import fr.liglab.adele.cilia.helper.MediatorTestHelper;
import fr.liglab.adele.cilia.runtime.CiliaInstance;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
@RunWith(JUnit4TestRunner.class)
public class TestEA  {

	private final static String CURRENT_VERSION="1.2.2-SNAPSHOT"; 

	private final static String COLLECTOR = "ea-collector";

	private final static String SENDER = "ea-sender";

	private final static String LINKER ="event-admin";

	@Inject
	protected BundleContext context;

	protected CiliaHelper cilia;

	protected OSGiHelper osgi;

	protected IPOJOHelper ipojo;

	@Before
	public void setUp() {
		osgi = new OSGiHelper(context);
		ipojo = new IPOJOHelper(context);
		cilia = new CiliaHelper(context);
	}

	@After
	public void tearDown() {
		cilia.dispose();
		osgi.dispose();
		ipojo.dispose();
	}



	@Configuration
	public static Option[] configure() {
		Option[] platform = options(felix());

		Option[] bundles = options(
				provision(
						mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo").version("1.8.0"),
						mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo.test.helpers").version("1.5.0-SNAPSHOT"),
						mavenBundle().groupId("org.osgi").artifactId("org.osgi.compendium").version("4.2.0"),
						mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.gogo.runtime").version("0.6.1"),
						mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.fileinstall").version("3.2.0"),
						mavenBundle().groupId("org.slf4j").artifactId("slf4j-api").version("1.6.1"),
						mavenBundle().groupId("org.slf4j").artifactId("slf4j-simple").version("1.6.1"),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-core").version(CURRENT_VERSION),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-runtime").version(CURRENT_VERSION),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-helper").version(CURRENT_VERSION),
						mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.eventadmin").version("1.2.14"),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("ea-adapter").version("1.2.2-SNAPSHOT")
						)); // The target
		Option[] r = OptionUtils.combine(platform, bundles);
		return r;
	}

	/**
	 * Mockito bundle
	 * @return
	 */
	@Configuration
	public static Option[] mockitoBundle() {
		return options(JUnitOptions.mockitoBundles());
	}

	

	public CollectorHelper createCollectorHelper(Hashtable props){
		CiliaInstance ci = cilia.createInstance(COLLECTOR, props);
		ci.start();
		Assert.assertEquals(ComponentInstance.VALID,ci.getState());
		ICollector ic = (ICollector)ci.getObject();
		Assert.assertNotNull(ic);
		CollectorHelper ch = cilia.getCollectorHelper(ic);
		return ch;
	}

	public ISender createSender(Hashtable props){
		CiliaInstance si = cilia.createInstance(SENDER, props);
		si.start();
		Assert.assertEquals(ComponentInstance.VALID,si.getState());
		ISender is = (ISender)si.getObject();
		Assert.assertNotNull(is);
		return is;
	}

	@Test
	public void validateServices() {
		CiliaHelper.waitSomeTime(2000);
		Factory col = ipojo.getFactory(COLLECTOR);
		Assert.assertNotNull(col);
		Assert.assertEquals(Factory.VALID, col.getState());
		Factory snd = ipojo.getFactory(SENDER);
		Assert.assertNotNull(snd);
		Assert.assertEquals(Factory.VALID, snd.getState());
		CiliaBindingService gbs = (CiliaBindingService)osgi.getServiceObject(CiliaBindingService.class.getName(), "(cilia.binding.type=" +  LINKER +")");
		Assert.assertNotNull(gbs);
	}

	@Test
	public void collectorTest() {
		String topic = "eatopic";
		CiliaHelper.waitSomeTime(2000);
		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put("topic", topic);
		CollectorHelper ch = createCollectorHelper(ht);
		injectMessages(topic);
		//wait to receive
		CiliaHelper.waitSomeTime(100);
		//See if all messages are received.
		Assert.assertEquals(10, ch.countReceived());
		Data data = ch.getLast();

		System.out.println("Last Data");
		System.out.println(data.getAllData());
	}

	@Test
	public void senderTest() {
		String topic = "receivingTopic";
		CiliaHelper.waitSomeTime(2000);

		//initializesTopics(topic);
		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put("topic", topic);

		//initializesTopics(topic);
		Hashtable<String, String> ht2 = new Hashtable<String, String>();
		ht2.put("topic", topic);

		//Get collector Helper
		CollectorHelper ch = createCollectorHelper(ht);
		//Get Sender
		ISender is = createSender(ht2);

		int i;
		for (i = 0; i < 10; i++) {
			Data ndata = new Data("Test number " + i, "data");
			is.send(ndata);
		}

		CiliaHelper.waitSomeTime(1000);
		//See if all messages are received.
		Assert.assertEquals(10, ch.countReceived());
		Data data = ch.getLast();
		Assert.assertEquals("data", data.getName());
		Assert.assertEquals("Test number 9", data.getContent());
		//System.out.println("Last Data");
		System.out.println(data.getAllData());
	}

	private void injectMessages(String stopic){
		EventAdmin m_eventAdmin = getService();

		int i;
		for (i = 0; i < 10; i++) {
			System.out.println("Sending Message: " + i);
			Data data = new Data("Test number " + i, "Test number " + i);
			m_eventAdmin.postEvent(new Event(stopic, data.getAllData()));
		}

	}
	private EventAdmin getService(){
		return (EventAdmin) osgi.getServiceObject(EventAdmin.class.getName(), null);
	}
	
	@Test
	public void testBinding() {
		CiliaHelper.waitSomeTime(2000);
		URL url = context.getBundle().getResource("test.dscilia");
		cilia.load(url);
		//wait to be added.
		System.out.println("will wait");
		boolean found = cilia.waitToChain("toto",10000);
		System.out.println("found chain "+ found);
		MediatorTestHelper qd = cilia.instrumentChain("toto", "m11:unique", "m22:unique");
		//chain must exist, and helper should be well constructed.
		Assert.assertNotNull(qd);
		qd.injectData(new Data ("data", "dda"));
		//wait some time to arrive message.
		CiliaHelper.waitSomeTime(1000);
		Assert.assertEquals(1, qd.getAmountData());
		Data lastData = qd.getLastData();
		Assert.assertEquals("data", lastData.getContent());
		Assert.assertEquals("dda", lastData.getName());
		System.out.println("Received data: " + lastData.getAllData());
	}

	@Test
	public void testAdapters() {
		CiliaHelper.waitSomeTime(2000);
		URL url = context.getBundle().getResource("testAdapter.dscilia");
		cilia.load(url);
		System.out.println("will wait");
		boolean found = cilia.waitToChain("toto",10000);
		System.out.println("found chain "+ found);
		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put("topic", "in_adapter_topic");

		Hashtable<String, String> ht2 = new Hashtable<String, String>();
		ht2.put("topic", "out_adapter_topic");
		//create a collector to test sending.
		CollectorHelper ch = createCollectorHelper(ht2); 
				
		//create a sender to test sending.
		ISender is = createSender(ht);

		int i;
		for (i = 0; i < 10; i++) {
			Data ndata = new Data("Test number " + i, "data");
			is.send(ndata);
		}
		CiliaHelper.waitSomeTime(1000);
		//See if all messages are received.
		Assert.assertEquals(10, ch.countReceived());
		Data data = ch.getLast();
		Assert.assertEquals("data", data.getName());
		Assert.assertEquals("Test number 9", data.getContent());
		//System.out.println("Last Data");
		System.out.println(data.getAllData());
	}


}