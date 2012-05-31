package cilia.runtime.specification.test;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

import java.util.Dictionary;

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

import fr.liglab.adele.cilia.ApplicationSpecification;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.core.tests.tools.CiliaTools;
import fr.liglab.adele.cilia.exceptions.BuilderConfigurationException;
import fr.liglab.adele.cilia.exceptions.BuilderException;
import fr.liglab.adele.cilia.exceptions.BuilderPerformerException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.CiliaContainer;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.model.impl.AdapterImpl;
import fr.liglab.adele.cilia.model.impl.ChainImpl;
import fr.liglab.adele.cilia.model.impl.MediatorImpl;

@RunWith(JUnit4TestRunner.class)
public class CiliaSpecificationTester {

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

		Option[] bundles = options(provision(
				mavenBundle().groupId("org.apache.felix")
						.artifactId("org.apache.felix.ipojo").versionAsInProject(),
				mavenBundle().groupId("org.apache.felix")
						.artifactId("org.apache.felix.ipojo.test.helpers")
						.versionAsInProject(), mavenBundle().groupId("org.osgi")
						.artifactId("org.osgi.compendium").versionAsInProject(),
				mavenBundle().groupId("org.slf4j").artifactId("slf4j-api")
						.versionAsInProject(), mavenBundle().groupId("org.slf4j")
						.artifactId("slf4j-simple").version("1.6.1"), mavenBundle()
						.groupId("fr.liglab.adele.cilia").artifactId("cilia-core")
						.versionAsInProject(),
				mavenBundle().groupId("fr.liglab.adele.cilia")
						.artifactId("cilia-runtime").versionAsInProject()));
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

	// Verify the service is present
	@Test
	public void validateService() {
		CiliaTools.waitToInitialize();
		ServiceReference sr[] = null;
		sr = osgi.getServiceReferences(CiliaContext.class.getName(), null);
		assertNotNull(sr[0]);
		CiliaContext ccontext = (CiliaContext) context.getService(sr[0]);
		assertNotNull(ccontext);
	}

	public CiliaContext getCiliaContextService() {
		ServiceReference sr[] = null;
		sr = osgi.getServiceReferences(CiliaContext.class.getName(), null);
		assertNotNull(sr[0]);
		CiliaContext ccontext = (CiliaContext) context.getService(sr[0]);
		assertNotNull(ccontext);
		return ccontext;
	}

	public CiliaContainer getCiliaContainerService() {
		ServiceReference sr[] = null;
		sr = osgi.getServiceReferences(CiliaContainer.class.getName(), null);
		assertNotNull(sr[0]);
		CiliaContainer ccontext = (CiliaContainer) context.getService(sr[0]);
		assertNotNull(ccontext);
		return ccontext;
	}

	private void checkNode(Node[] nodes, int length, String prefix) {
		if (nodes.length != length) {
			Assert.fail("Expected length = " + length + " , current=" + nodes.length);
		}
		for (int i = 0; i < nodes.length; i++) {
			if (!(nodes[i] instanceof MediatorComponent)) {
				Assert.fail("Not instance of Mediator Component");
			}
			if (!nodes[i].getQualifiedId().startsWith(prefix)) {
				Assert.fail("Wrong node retrieved ");
			}
		}

	}

	private void buildChain() {
		CiliaContext ciliaContext = getCiliaContextService();

		try {
			Builder builder = ciliaContext.getBuilder();
			Architecture chain = builder.create("Chain1");
			chain.create().adapter().type("type").namespace("sample").id("entryAdapter");
			chain.create().adapter().type("type").namespace("sample").id("exitAdapter");
			chain.create().mediator().type("type").namespace("sample").id("mediator_1");
			chain.create().mediator().type("type").namespace("sample").id("mediator_2");
			chain.bind().from("entryAdapter:out").to("mediator_1:in");
			chain.bind().from("mediator_1:out").to("mediator_2:in");
			chain.bind().from("mediator_2:out").to("exitAdapter:in");
			builder.done();
		} catch (BuilderConfigurationException e) {
			Assert.fail(e.getMessage());
		} catch (BuilderException e) {
			Assert.fail(e.getMessage());
		} catch (BuilderPerformerException e) {
			Assert.fail(e.getMessage());
		}
	}

	private void api_findNodeByFilter(ApplicationSpecification application) {

		/* Invalid Syntax Exception */
		try {
			Node[] nodes = application
					.findNodeByFilter("&(chain=Chain1)(node=mediator_3))");
			Assert.fail("No Exception thrown : ldap syntax error");
		} catch (CiliaIllegalParameterException e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		} catch (CiliaInvalidSyntaxException e) {
			/* OK */
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		/* Illegal Parameter Exception */
		try {
			Node[] nodes = application.findNodeByFilter("(toto=mediator_1)");
			Assert.fail("No Exception thrown : missing ldap keyword ");
		} catch (CiliaIllegalParameterException e) {
			/* OK */
		} catch (CiliaInvalidSyntaxException e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* API findNode Exception : Illegal Parameter */
		try {
			Node[] nodes = application.findNodeByFilter(null);
			Assert.fail("No Exception thrown :null filter");
		} catch (CiliaIllegalParameterException e) {
			/* OK */
		} catch (CiliaInvalidSyntaxException e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		/* Return 0 value , no exception */
		try {
			Node[] nodes = application
					.findNodeByFilter("(&(chain=Chain1)(node=mediator_3))");
			Assert.assertNotNull(nodes);
			Assert.assertArrayEquals(new Node[0], nodes);
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* Return 1 value , no exception */
		try {
			Node[] nodes = application
					.findNodeByFilter("(&(chain=Chain1)(node=mediator_1))");
			Assert.assertNotNull(nodes);
			checkNode(nodes, 1, "Chain1/mediator_");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* Return 2 values , no Exception */
		try {
			Node[] nodes = application
					.findNodeByFilter("(&(chain=Chain1)(node=mediator_*))");
			Assert.assertNotNull(nodes);
			checkNode(nodes, 2, "Chain1/mediator_");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* Return 4 value , no Exception */
		try {
			Node[] nodes = application.findNodeByFilter("(chain=Chain1)");
			Assert.assertNotNull(nodes);
			checkNode(nodes, 4, "Chain1/");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* Return 4 value , no Exception */
		try {
			Node[] nodes = application.findNodeByFilter("(node=*)");
			Assert.assertNotNull(nodes);
			checkNode(nodes, 4, "Chain1/");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
	}

	private void api_getChainId(ApplicationSpecification application) {
		/* Checks the API chainID */
		String[] ids = application.getChainId();
		Assert.assertNotNull("get chain return null", ids);
		if (ids.length != 1) {
			Assert.fail("Expected length =1 ,length =" + ids.length);
		}
		if (!"Chain1".equals(ids[0])) {
			Assert.fail("Expected chain Id = Chain1" + " read=" + ids[0]);
		}
	}

	private void api_getChain(ApplicationSpecification application) {
		/* Exception : invalid parameter */
		try {
			Chain chain = application.getChain(null);
			Assert.fail("no exception thrown invalid parameter");
		} catch (CiliaIllegalParameterException e) {
			/* OK */
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* return null */
		try {
			Chain chain = application.getChain("Chain2");
			Assert.assertNull(chain);
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* return the instance */
		try {
			Chain chain = application.getChain("Chain1");
			Assert.assertNotNull(chain);
			if (!chain.getId().equals("Chain1")) {
				Assert.fail("invalid chain retreived");
			}
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
	}

	public void api_getModel(ApplicationSpecification application) {

		Chain chain = null;
		try {
			chain = application.getChain("Chain1");
			Assert.assertNotNull(chain);
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			Node node = chain.getMediator("mediator_1");
			Assert.assertNotNull(node);
			MediatorComponent mediatorModel = application.getModel(node);
			if (!mediatorModel.getQualifiedId().startsWith("Chain1/mediator_1")) {
				Assert.fail("Wrong node");
			}
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			Node[] node = application.findNodeByFilter("(node=mediator_1)");
			Assert.assertNotNull(node);
			MediatorComponent mediatorModel = application.getModel(node[0]);
			if (!mediatorModel.getQualifiedId().startsWith("Chain1/mediator_1")) {
				Assert.fail("Wrong node");
			}
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		/* IllegalParameterException */
		try {
			MediatorComponent mediatorModel = application.getModel(null);
			Assert.fail("Exception not thrown");
		} catch (CiliaIllegalParameterException e) {
			/* OK */
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

	}

	private void api_getPropertie(ApplicationSpecification application) {
		Node[] node = null;
		try {
			node = application.findNodeByFilter("(node=mediator_1)");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());

		}

		try {
			Dictionary dico = application.properties(null);
		} catch (CiliaIllegalParameterException e) {
			/* OK */
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		Dictionary dico=null ;
		try {
			 dico = application.properties(node[0]);
			Assert.assertNotNull(dico) ;
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		
		try {
			dico.put("key", "testValue") ;
		} catch (UnsupportedOperationException e) {
			/* OK */
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());			
		}
	}
	
	private void api_endpointsIn(ApplicationSpecification application) {
		try {
			Node[] nodes = application.endpointIn("(chain=Chain1)");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());	
		} 
	}
	private void api_endpointsOut(ApplicationSpecification application) {
		
	}
	private void api_connectedTo(ApplicationSpecification application) {
		
	}
	
	private void illegalStateException(ApplicationSpecification application) {
	}
	

	@Test
	public void all_apis() {
		CiliaTools.waitToInitialize();
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationSpecification application = ciliaContext.getApplicationSpecification();
		assertNotNull(application);
		buildChain();
		api_getChainId(application);
		api_findNodeByFilter(application);
		api_getChain(application);
		api_getModel(application);
		api_getPropertie(application);
		api_endpointsIn(application);
		api_endpointsOut(application);
		api_connectedTo(application) ;
		/* Tester l'exception IllegalStateException */
	}

}
