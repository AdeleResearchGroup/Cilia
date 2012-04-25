package cilia.runtime.context.test;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

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
import org.osgi.framework.ServiceReference;

import fr.liglab.adele.cilia.Adapter;
import fr.liglab.adele.cilia.Binding;
import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Mediator;
import fr.liglab.adele.cilia.core.tests.tools.CiliaTools;
import fr.liglab.adele.cilia.model.AdapterImpl;
import fr.liglab.adele.cilia.model.ChainImpl;
import fr.liglab.adele.cilia.model.ComponentImpl;
import fr.liglab.adele.cilia.model.MediatorImpl;


@RunWith(JUnit4TestRunner.class)
public class CiliaContextTester {


	@Inject
	private BundleContext context;

	private OSGiHelper osgi;

	@Before
	public void setUp() {
		osgi = new OSGiHelper(context);
	}

	@After
	public void tearDown() {
		osgi.dispose();
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
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-core").versionAsInProject(),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-runtime").versionAsInProject()
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
	public void validateService() {
		CiliaTools.waitToInitialize();
		ServiceReference sr[] = null;
		sr = osgi.getServiceReferences (CiliaContext.class.getName(), null);
		assertNotNull(sr[0]);
		CiliaContext ccontext = (CiliaContext) context.getService(sr[0]);
		assertNotNull(ccontext);
	}

	public CiliaContext getCiliaContextService() {
		ServiceReference sr[] = null;
		sr = osgi.getServiceReferences (CiliaContext.class.getName(), null);
		assertNotNull(sr[0]);
		CiliaContext ccontext = (CiliaContext) context.getService(sr[0]);
		assertNotNull(ccontext);
		return ccontext;
	}


	public void chainTimeCreation() {
		//initializeServices();
		long initialmemory = 0;
		long finalmemory = 0;
		ComponentImpl chain = null;
		initialmemory = CiliaTools.getMemory();
		chain = new ChainImpl("chainId", "type", "", null);
		chain.dispose();
		chain = null;
		finalmemory = CiliaTools.getMemory();
		System.out.println("initial " + CiliaTools.bytesToKilobytes(initialmemory) );
		System.out.println("final " + CiliaTools.bytesToKilobytes(finalmemory));
		Assert.assertTrue(CiliaTools.bytesToKilobytes(initialmemory) >= CiliaTools.bytesToKilobytes(finalmemory));
	}

	@Test
	public void chainCreation() {
		CiliaTools.waitToInitialize();
		String chainId = "chainId";
		CiliaContext ccontext = getCiliaContextService();
		Chain chain = new ChainImpl(chainId, "type", "", null);

		ccontext.addChain(chain);
		ccontext.startChain(chain);

		Chain c = ccontext.getChain(chainId);

		Assert.assertEquals(c, chain);
	}



	public void testComponents() {
		CiliaTools.waitToInitialize();

		String chainId = "chainId";
		ChainImpl chain = new ChainImpl(chainId, "type", "", null);

		MediatorImpl m1 = new MediatorImpl("id1","type");
		MediatorImpl m2 = new MediatorImpl("id2","type");

		AdapterImpl a1 = new AdapterImpl("id1","type");
		AdapterImpl a2 = new AdapterImpl("aid2","type");

		chain.add(m1);
		chain.add(m2);

		try {
			chain.add(a1);
			Assert.fail("It must throw : Id already exists");
		}catch (Exception ex) {	}

		a1 = new AdapterImpl("aid1","type");
		chain.add(a1);
		chain.add(a2);

		//check ports in mediators
		Assert.assertNotNull(m1.getInPort("toto"));
		Assert.assertNotNull(m1.getOutPort("toto"));

		//check ports in adapters
		Assert.assertNotNull(a1.getInPort("toto"));
		Assert.assertNotNull(a1.getOutPort("toto"));

	}
	@Test
	public void testBindings(){
		CiliaTools.waitToInitialize();

		String chainId = "chainId";
		Chain chain = new ChainImpl(chainId, "type", "", null);

		Mediator m1 = new MediatorImpl("id1","type");
		Mediator m2 = new MediatorImpl("id2","type");

		Adapter a1 = new AdapterImpl("aid1","type");
		Adapter a2 = new AdapterImpl("aid2","type");
		chain.add(m1);
		chain.add(m2);
		chain.add(a1);
		chain.add(a2);

		Assert.assertNotNull(chain.bind(m1.getOutPort("op"), m2.getInPort("in"))); //mediator to mediator

		Assert.assertNotNull(chain.bind(a1.getOutPort("op"), a2.getInPort("in"))); //adapter to adapter

		Assert.assertNotNull(chain.bind(m1.getOutPort("op"), a1.getInPort("in"))); //mediator to adapter
		Assert.assertNotNull(chain.bind(a1.getOutPort("op"), m2.getInPort("in"))); //adapter to mediator

		//Not correct
		try{
			Binding n = chain.bind(a1.getInPort("opG"), m2.getInPort("iJn")); //adapter to mediator
			Assert.fail("It must throw an incompatible port exception");
		}catch(RuntimeException ex) {
		}
	}

}
