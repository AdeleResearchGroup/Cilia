package fr.liglab.adele.cilia.core.tests;

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
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import fr.liglab.adele.cilia.Adapter;
import fr.liglab.adele.cilia.Binding;
import fr.liglab.adele.cilia.Chain;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Component;
import fr.liglab.adele.cilia.Mediator;
import fr.liglab.adele.cilia.core.tests.tools.CiliaTools;
import fr.liglab.adele.cilia.model.AdapterImpl;
import fr.liglab.adele.cilia.model.ChainImpl;
import fr.liglab.adele.cilia.model.MediatorImpl;


@RunWith(JUnit4TestRunner.class)
public class ChainModelTest {

	/**
	 * The Generic Discovery bundle, target of this test.
	 */
	private static final String TARGET_BUNDLE = "target"
		+ System.getProperty("file.separator")
		+ "cilia-core-tests-1.2.1-SNAPSHOT.jar";

	@Inject
	private BundleContext context;

	private CiliaContext ccontext;

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

	public void initializeServices() {
		ServiceReference sr[] = null;
		try {
			sr = context.getServiceReferences (CiliaContext.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		ccontext = (CiliaContext) context.getService(sr[0]);
	}


	public void chainTimeCreation() {
		//initializeServices();
		long initialmemory = 0;
		long finalmemory = 0;
		Component chain = null;
		initialmemory = CiliaTools.getMemory();
		chain = new ChainImpl("chainId", "type", "", null);
		((ChainImpl)chain).dispose();
		chain = null;
		finalmemory = CiliaTools.getMemory();
		System.out.println("initial " + CiliaTools.bytesToKilobytes(initialmemory) );
		System.out.println("final " + CiliaTools.bytesToKilobytes(finalmemory));
		Assert.assertTrue(CiliaTools.bytesToKilobytes(initialmemory) >= CiliaTools.bytesToKilobytes(finalmemory));
	}
	
	@Test
	public void chainCreation() {
		try {
            Thread.sleep(2000);//wait to be registered
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
		String chainId = "chainId";
		initializeServices();
		Chain chain = new ChainImpl(chainId, "type", "", null);

		ccontext.addChain(chain);
		ccontext.startChain(chain);
		
		Chain c = ccontext.getChain(chainId);
		
		Assert.assertEquals(c, chain);
	}

	@Test
	public void testComponents() {
        try {
            Thread.sleep(2000);//wait to be registered
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
		String chainId = "chainId";
		Chain chain = new ChainImpl(chainId, "type", "", null);
		
		Mediator m1 = new MediatorImpl("id1","type");
		Mediator m2 = new MediatorImpl("id2","type");
		
		Adapter a1 = new AdapterImpl("id1","type");
		Adapter a2 = new AdapterImpl("aid2","type");
		
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
        try {
            Thread.sleep(2000);//wait to be registered
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
