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

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.CiliaBindingService;
import fr.liglab.adele.cilia.framework.ICollector;
import fr.liglab.adele.cilia.framework.ISender;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.helper.CollectorHelper;
import fr.liglab.adele.cilia.helper.MediatorTestHelper;
import fr.liglab.adele.cilia.runtime.CiliaInstance;
import fr.liglab.adele.commons.distribution.test.AbstractDistributionBaseTest;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.test.helpers.IPOJOHelper;
import org.apache.felix.ipojo.test.helpers.OSGiHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import javax.inject.Inject;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class TestEA  extends AbstractDistributionBaseTest {


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

	

	public CollectorHelper createCollectorHelper(Hashtable<String, String> props){
		CiliaInstance ci = cilia.createInstance(COLLECTOR, props);
		ci.start();
		Assert.assertEquals(ComponentInstance.VALID,ci.getState());
		ICollector ic = (ICollector)ci.getObject();
		Assert.assertNotNull(ic);
		CollectorHelper ch = cilia.getCollectorHelper(ic);
		return ch;
	}

	public ISender createSender(Hashtable<String, String> props){
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
		CiliaHelper.waitSomeTime(500);
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
		boolean found = cilia.waitToChain("toto",6000);
		System.out.println("found chain "+ found);
		CiliaHelper.waitSomeTime(3000);
		MediatorTestHelper qd = cilia.instrumentChain("toto", "m11:unique", "m22:unique");
		//chain must exist, and helper should be well constructed.
		Assert.assertNotNull(qd);
		qd.injectData(new Data ("data", "dda"));
		//wait some time to arrive message.
		CiliaHelper.waitSomeTime(4000);
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
		boolean found = cilia.waitToChain("toto",6000);
		CiliaHelper.waitSomeTime(1000);
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
		CiliaHelper.waitSomeTime(6000);
		//See if all messages are received.
		Assert.assertEquals(10, ch.countReceived());
		Data data = ch.getLast();
		Assert.assertEquals("data", data.getName());
		Assert.assertEquals("Test number 9", data.getContent());
		//System.out.println("Last Data");
		System.out.println(data.getAllData());
	}


}
