package cilia.runtime.parser.test;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

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
import org.osgi.framework.ServiceReference;

import fr.liglab.adele.cilia.ChainParser;
import fr.liglab.adele.cilia.CiliaFileManager;
import fr.liglab.adele.cilia.core.tests.tools.CiliaTools;

@RunWith(JUnit4TestRunner.class)
public class CiliaFileManagerTester {

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
		sr = osgi.getServiceReferences (CiliaFileManager.class.getName(), null);
		assertNotNull(sr[0]);
		CiliaFileManager parser = (CiliaFileManager) context.getService(sr[0]);
		assertNotNull(parser);
	}
}
