package fr.liglab.adele.cilia.jms.tests;

import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

import java.net.URL;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

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

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.CiliaBindingService;
import fr.liglab.adele.cilia.framework.ICollector;
import fr.liglab.adele.cilia.framework.ISender;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.helper.CollectorHelper;
import fr.liglab.adele.cilia.helper.MediatorTestHelper;
import fr.liglab.adele.cilia.jms.CiliaJoramTool;
import fr.liglab.adele.cilia.runtime.CiliaInstance;


@RunWith(JUnit4TestRunner.class)
public class CiliaJMSTest {


	private final static String CURRENT_VERSION="1.2.2-SNAPSHOT"; 

	@Inject
	private BundleContext context;

	private CiliaHelper cilia;

	private OSGiHelper osgi;

	private IPOJOHelper ipojo;

	@Before
	public void setUp() {
		osgi = new OSGiHelper(context);
		ipojo = new IPOJOHelper(context);
		cilia = new CiliaHelper(context);
	}

	@After
	public void tearDown() {
		cilia.dispose();
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
						mavenBundle().groupId("javax.jms").artifactId("com.springsource.javax.jms").version("1.1.0"),
						mavenBundle().groupId("org.objectweb.joram").artifactId("joram-client-jms").version("5.7.0"),
						mavenBundle().groupId("org.objectweb.joram").artifactId("a3-common").version("5.7.0"),
						mavenBundle().groupId("org.objectweb.joram").artifactId("jndi-client").version("5.7.0"),
						mavenBundle().groupId("org.objectweb.joram").artifactId("jndi-shared").version("5.7.0"),
						mavenBundle().groupId("org.objectweb.joram").artifactId("joram-shared").version("5.7.0"),
						mavenBundle().groupId("org.objectweb.joram").artifactId("jcup").version("5.3.1"),
						mavenBundle().groupId("org.ow2.jonas.osgi").artifactId("monolog").version("5.2.0"),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-core").versionAsInProject(),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-runtime").versionAsInProject(),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-helper").versionAsInProject(),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("jms-adapter").versionAsInProject()

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


	@Test
	public void validateServices() {
		waitSomeTime(2000);
		Factory col = ipojo.getFactory("jms-collector");
		Assert.assertNotNull(col);
		Assert.assertEquals(Factory.VALID, col.getState());
		Factory snd = ipojo.getFactory("jms-sender");
		Assert.assertNotNull(snd);
		Assert.assertEquals(Factory.VALID, snd.getState());
		CiliaBindingService gbs = (CiliaBindingService)osgi.getServiceObject(CiliaBindingService.class.getName(), "(cilia.binding.type=JMS-Joram)");
		Assert.assertNotNull(gbs);
	}

	@Test
	public void collectorTest() {
		String topic = "receivingTopic";
		waitSomeTime(2000);

		//initializesTopics(topic);
		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put("jms.topic", topic);

		CiliaInstance ci = cilia.createInstance("jms-collector", ht);
		ci.start();
		Assert.assertEquals(ComponentInstance.VALID,ci.getState());

		ICollector ic = (ICollector)ci.getObject();
		Assert.assertNotNull(ic);
		CollectorHelper ch = cilia.getCollectorHelper(ic);
		injectMessages(topic);
		//See if all messages are received.
		Assert.assertEquals(10, ch.countReceived());
		Data data = ch.getLast();
		Assert.assertEquals("jms-message", data.getName());
		System.out.println("Last Data");
		System.out.println(data.getAllData());
	}

	@Test
	public void senderTest() {
		String topic = "receivingTopic";
		waitSomeTime(2000);

		//initializesTopics(topic);
		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put("jms.topic", topic);

		//initializesTopics(topic);
		Hashtable<String, String> ht2 = new Hashtable<String, String>();
		ht2.put("jms.topic", topic);
		//create a collector to test sending.
		CiliaInstance ci = cilia.createInstance("jms-collector", ht);
		ci.start();
		ICollector ic = (ICollector)ci.getObject();
		CollectorHelper ch = cilia.getCollectorHelper(ic);

		//create a collector to test sending.
		CiliaInstance si = cilia.createInstance("jms-sender", ht2);
		si.start();

		//check sender validity
		Assert.assertEquals(ComponentInstance.VALID,si.getState());

		//See if sender is not null
		ISender is = (ISender)si.getObject();
		Assert.assertNotNull(is);

		int i;
		for (i = 0; i < 10; i++) {
			Data ndata = new Data("Test number " + i, "data");
			is.send(ndata);
		}

		waitSomeTime(1000);
		//See if all messages are received.
		Assert.assertEquals(10, ch.countReceived());
		Data data = ch.getLast();
		Assert.assertEquals("data", data.getName());
		Assert.assertEquals("Test number 9", data.getContent());
		//System.out.println("Last Data");
		System.out.println(data.getAllData());
	}

	private void injectMessages(String stopic){
		// creates the connection
		try {
			TopicConnection cnx = CiliaJoramTool.createTopicConnection("root", "root","localhost",16010 );
			// creates a session object
			TopicSession session = cnx.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			// creates a topic object
			Topic topic = session.createTopic(stopic);
			TopicPublisher publisher = session.createPublisher(topic);
			cnx.start();


			int i;
			for (i = 0; i < 10; i++) {
				TextMessage msg = session.createTextMessage();
				msg.setText("Test number " + i);
				publisher.publish(msg);
			}

			cnx.close();

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testBinding() {
		waitSomeTime(2000);
		URL url = context.getBundle().getResource("test.dscilia");
		cilia.load(url);
		//wait to be added.
		System.out.println("will wait");
		boolean found = cilia.waitToChain("toto",6000);
		System.out.println("found chain "+ found);
		MediatorTestHelper qd = cilia.instrumentChain("toto", "m11:unique", "m22:unique");
		//chain must exist, and helper should be well constructed.
		Assert.assertNotNull(qd);
		qd.injectData(new Data ("data", "dda"));
		//wait some time to arrive message.
		waitSomeTime(1000);
		Assert.assertEquals(1, qd.getAmountData());
		Data lastData = qd.getLastData();
		Assert.assertEquals("data", lastData.getContent());
		Assert.assertEquals("dda", lastData.getName());
		System.out.println("Received data: " + lastData.getAllData());
	}

	@Test
	public void testAdapters() {
		waitSomeTime(2000);
		URL url = context.getBundle().getResource("testAdapter.dscilia");
		cilia.load(url);
		System.out.println("will wait");
		boolean found = cilia.waitToChain("toto",10000);
		System.out.println("found chain "+ found);
		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put("jms.topic", "in_adapter_topic");

		Hashtable<String, String> ht2 = new Hashtable<String, String>();
		ht2.put("jms.topic", "out_adapter_topic");
		//create a collector to test sending.
		CiliaInstance ci = cilia.createInstance("jms-collector", ht2);
		ci.start();
		ICollector ic = (ICollector)ci.getObject();
		CollectorHelper ch = cilia.getCollectorHelper(ic);

		//create a sender to test sending.
		CiliaInstance si = cilia.createInstance("jms-sender", ht);
		si.start();

		//check sender validity
		Assert.assertEquals(ComponentInstance.VALID,si.getState());

		//See if sender is not null
		ISender is = (ISender)si.getObject();
		Assert.assertNotNull(is);

		int i;
		for (i = 0; i < 10; i++) {
			Data ndata = new Data("Test number " + i, "data");
			is.send(ndata);
		}
		waitSomeTime(1000);
		//See if all messages are received.
		Assert.assertEquals(10, ch.countReceived());
		Data data = ch.getLast();
		Assert.assertEquals("data", data.getName());
		Assert.assertEquals("Test number 9", data.getContent());
		//System.out.println("Last Data");
		System.out.println(data.getAllData());
	}

	private void  initializesTopics(String topics){
		System.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
	}

	public void waitSomeTime(int l) {
		try {
			Thread.sleep(l);//wait to be registered
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


}
