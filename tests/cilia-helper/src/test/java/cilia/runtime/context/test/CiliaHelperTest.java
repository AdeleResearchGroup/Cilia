package cilia.runtime.context.test;

import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import junit.framework.Assert;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.test.helpers.IPOJOHelper;
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

import fr.liglab.adele.cilia.helper.CiliaHelper;


@RunWith(JUnit4TestRunner.class)
public class CiliaHelperTest {


	@Inject
	private BundleContext context;

	private CiliaHelper cilia;

	private OSGiHelper osgi;
	
	

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

	@Configuration
	public static Option[] configure() {
		Option[] platform = options(felix());

		Option[] bundles = options(
				provision(mavenBundle().groupId(
						"org.apache.felix").artifactId("org.apache.felix.ipojo").versionAsInProject(), 
						mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo.test.helpers").versionAsInProject(),
						mavenBundle().groupId("org.osgi").artifactId("org.osgi.compendium").versionAsInProject(),
						mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.gogo.runtime").version("0.6.1"),
						mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.fileinstall").version("3.2.0"),
						mavenBundle().groupId("org.slf4j").artifactId("slf4j-api").versionAsInProject(),
						mavenBundle().groupId("org.slf4j").artifactId("slf4j-simple").version("1.6.1"),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-core").versionAsInProject(),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-runtime").versionAsInProject(),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-helper").version("1.5.2-SNAPSHOT")
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
	public void validateFactories() {
		waitSomeTime(5000);
		Factory adapter = (Factory)osgi.getServiceObject(Factory.class.getName(), "(factory.name=cilia-adapter-helper)");
		Assert.assertNotNull(adapter);
		Assert.assertEquals(Factory.VALID, adapter.getState());

		Factory mediator = (Factory)osgi.getServiceObject(Factory.class.getName(), "(factory.name=Mock)");
		Assert.assertNotNull(mediator);
		Assert.assertEquals(Factory.VALID, mediator.getState());
	}
	
	public void waitSomeTime(int l) {
		try {
			Thread.sleep(l);//wait to be registered
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	
	





}
