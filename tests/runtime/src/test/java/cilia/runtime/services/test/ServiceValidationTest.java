package cilia.runtime.services.test;

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.util.ChainParser;
import fr.liglab.adele.cilia.util.CiliaFileManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.runner.test.ChameleonRunner;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(ChameleonRunner.class)
public class ServiceValidationTest {

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
        sr = osgi.getServiceReferences(CiliaFileManager.class.getName(), null);
        assertNotNull(sr[0]);
        CiliaFileManager parser = (CiliaFileManager) context.getService(sr[0]);
        assertNotNull(parser);
    }

    @Test
    public void ChainParservalidation() {
        CiliaHelper.waitSomeTime(2000);
        ServiceReference sr[] = null;
        sr = osgi.getServiceReferences(ChainParser.class.getName(), null);
        assertNotNull(sr[0]);
        ChainParser parser = (ChainParser) context.getService(sr[0]);
        assertNotNull(parser);
    }
}
