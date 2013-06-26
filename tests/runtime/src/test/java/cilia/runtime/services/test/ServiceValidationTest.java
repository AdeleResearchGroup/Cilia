package cilia.runtime.services.test;

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.util.ChainParser;
import fr.liglab.adele.cilia.util.CiliaFileManager;
import fr.liglab.adele.commons.distribution.test.AbstractDistributionBaseTest;
import org.apache.felix.ipojo.test.helpers.OSGiHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class ServiceValidationTest extends AbstractDistributionBaseTest {

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
	
	@Test
	public void CiliaContextvalidation() {
		CiliaHelper.waitSomeTime(2000);
		ServiceReference sr[] = null;
		sr = osgi.getServiceReferences(CiliaContext.class.getName(), null);
		assertNotNull(sr[0]);
		CiliaContext ccontext = (CiliaContext) context.getService(sr[0]);
		assertNotNull(ccontext);
	}
	
	@Test
	public void CiliaFileManagervalidation() {
		CiliaHelper.waitSomeTime(2000);
		ServiceReference sr[] = null;
		sr = osgi.getServiceReferences (CiliaFileManager.class.getName(), null);
		assertNotNull(sr[0]);
		CiliaFileManager parser = (CiliaFileManager) context.getService(sr[0]);
		assertNotNull(parser);
	}
	
	@Test
	public void ChainParservalidation() {
		CiliaHelper.waitSomeTime(2000);
		ServiceReference sr[] = null;
		sr = osgi.getServiceReferences (ChainParser.class.getName(), null);
		assertNotNull(sr[0]);
		ChainParser parser = (ChainParser) context.getService(sr[0]);
		assertNotNull(parser);
	}
}
