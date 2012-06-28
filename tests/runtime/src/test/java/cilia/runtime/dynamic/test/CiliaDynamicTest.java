/*
 * Copyright  2002-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package cilia.runtime.dynamic.test;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

import java.util.Dictionary;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

import fr.liglab.adele.cilia.ApplicationRuntime;
import fr.liglab.adele.cilia.ApplicationSpecification;
import fr.liglab.adele.cilia.ChainCallback;
import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.NodeCallback;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.core.tests.tools.CiliaTools;
import fr.liglab.adele.cilia.exceptions.BuilderConfigurationException;
import fr.liglab.adele.cilia.exceptions.BuilderException;
import fr.liglab.adele.cilia.exceptions.BuilderPerformerException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.MediatorComponent;

@RunWith(JUnit4TestRunner.class)
public class CiliaDynamicTest {

	
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

	private void checkNode(Node[] nodes, int length, String prefix) {
		if (nodes.length != length) {
			Assert.fail("Expected length = " + length + " , current=" + nodes.length);
		}
		for (int i = 0; i < nodes.length; i++) {
			if (!(nodes[i] instanceof MediatorComponent)) {
				Assert.fail("Not instance of Mediator Component");
			}
			if (!nodes[i].qualifiedId().startsWith(prefix)) {
				Assert.fail("Wrong node retrieved ");
			}
		}

	}

	private void buildChain() {
		CiliaContext ciliaContext = getCiliaContextService();

		try {
			Builder builder = ciliaContext.getBuilder();
			Architecture chain = builder.create("Chain1");
			chain.create().adapter().type("gui-adapter").id("adapter_in");
			chain.create().adapter().type("console-adapter").id("adapter_out");
			chain.create().mediator().type("immediate-mediator").id("mediator_1");
			chain.create().mediator().type("immediate-mediator").id("mediator_2");
			chain.bind().from("adapter_in:out").to("mediator_1:in");
			chain.bind().from("mediator_1:out").to("mediator_2:in");
			chain.bind().from("mediator_2:out").to("adapter_out:in");
			builder.done();
		} catch (BuilderConfigurationException e) {
			Assert.fail(e.getMessage());
		} catch (BuilderException e) {
			Assert.fail(e.getMessage());
		} catch (BuilderPerformerException e) {
			Assert.fail(e.getMessage());
		}
	}

	private void api_findNodeByFilter(ApplicationRuntime runtime) {

		/* Invalid Syntax Exception */
		try {
			Node[] nodes = runtime
					.findNodeByFilter("&(chain=Chain1)(node=mediator_3))");
			Assert.fail("No Exception thrown : ldap syntax error");
		} catch (CiliaIllegalParameterException e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		} catch (CiliaInvalidSyntaxException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		/* Illegal Parameter Exception */
		try {
			Node[] nodes = runtime.findNodeByFilter("(toto=mediator_1)");
			Assert.fail("No Exception thrown : missing ldap keyword ");
		} catch (CiliaIllegalParameterException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (CiliaInvalidSyntaxException e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* API findNode Exception : Illegal Parameter */
		try {
			Node[] nodes = runtime.findNodeByFilter(null);
			Assert.fail("No Exception thrown :null filter");
		} catch (CiliaIllegalParameterException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (CiliaInvalidSyntaxException e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		/* Return 0 value , no exception */
		try {
			Node[] nodes = runtime
					.findNodeByFilter("(&(chain=Chain1)(node=mediator_3))");
			Assert.assertNotNull(nodes);
			Assert.assertArrayEquals(new Node[0], nodes);
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* Return 1 value , no exception */
		try {
			Node[] nodes = runtime
					.findNodeByFilter("(&(chain=Chain1)(node=mediator_1))");
			Assert.assertNotNull(nodes);
			checkNode(nodes, 1, "Chain1/mediator_");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* Return 2 values , no Exception */
		try {
			Node[] nodes = runtime
					.findNodeByFilter("(&(chain=Chain1)(node=mediator_*))");
			Assert.assertNotNull(nodes);
			checkNode(nodes, 2, "Chain1/mediator_");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* Return 4 value , no Exception */
		try {
			Node[] nodes = runtime.findNodeByFilter("(chain=Chain1)");
			Assert.assertNotNull(nodes);
			checkNode(nodes, 4, "Chain1/");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* Return 4 value , no Exception */
		try {
			Node[] nodes = runtime.findNodeByFilter("(node=*)");
			Assert.assertNotNull(nodes);
			checkNode(nodes, 4, "Chain1/");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
	}

	private void api_getChainId(ApplicationRuntime runtime) {
		/* Checks the API chainID */
		String[] ids = runtime.getChainId();
		Assert.assertNotNull("get chain return null", ids);
		if (ids.length != 1) {
			Assert.fail("Expected length =1 ,length =" + ids.length);
		}
		if (!"Chain1".equals(ids[0])) {
			Assert.fail("Expected chain Id = Chain1" + " read=" + ids[0]);
		}
	}

	private void api_getChainState(ApplicationRuntime runtime) {
		/* Exception : invalid parameter */
		try  { 
			int state = runtime.getChainState(null) ;
			Assert.fail("no exception thrown invalid parameter");
		} catch (CiliaIllegalParameterException e) {
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		/* Exception illegalStateException */
		try {
			int  state = runtime.getChainState("Chain100") ;
			Assert.fail("no exception thrown invalid parameter");
		} catch (CiliaIllegalStateException e) {
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* return the instance */
		try {
			int state = runtime.getChainState("Chain1");
			if (state !=ApplicationRuntime.IDLE) {
				Assert.fail("Illegate chain state exptected IDLE");
			}
			
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* test start  */
		try {
			runtime.start("Chain1") ;
			int state = runtime.getChainState("Chain1");
			if (state !=ApplicationRuntime.STARTED) {
				Assert.fail("Illegate chain state expected STARTED");
			}
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		
		try {
			runtime.stop("Chain1") ;
			int state = runtime.getChainState("Chain1");
			if (state !=ApplicationRuntime.STOPPED) {
				Assert.fail("Illegate chain state expected STOPPED");
			}
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		
	}



	private void api_endpointsIn(ApplicationSpecification application) {

		try {
			Node[] nodes = application.endpointIn(null);
			Assert.fail("Exception not thrown");
		} catch (CiliaIllegalParameterException e) {
			/* OK */
		} catch (CiliaInvalidSyntaxException e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			Node[] nodes = application.endpointIn("(chain=Chain))");
			Assert.fail("Exception not thrown");
		} catch (CiliaIllegalParameterException e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		} catch (CiliaInvalidSyntaxException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			Node[] nodes = application.endpointIn("(chain=Chain2)");
			Assert.assertNotNull(nodes);
			if (nodes.length != 0) {
				Assert.fail("Length expected null");
			}
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			Node[] nodes = application.endpointIn("(chain=Chain1)");
			Assert.assertNotNull(nodes);
			if (nodes.length != 2) {
				Assert.fail("Length expected 2 , received=" + nodes.length);
			}
			for (int i = 0; i < nodes.length; i++) {
				if ((!nodes[i].nodeId().startsWith("adapter")))
					Assert.fail("Adapter not retreived");
			}

		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

	}

	private void api_endpointsOut(ApplicationSpecification application) {

		try {
			Node[] nodes = application.endpointOut(null);
			Assert.fail("Exception not thrown");
		} catch (CiliaIllegalParameterException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (CiliaInvalidSyntaxException e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			Node[] nodes = application.endpointOut("(chain=Chain))");
			Assert.fail("Exception not thrown");
		} catch (CiliaIllegalParameterException e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		} catch (CiliaInvalidSyntaxException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			Node[] nodes = application.endpointOut("(chain=Chain2)");
			Assert.assertNotNull(nodes);
			if (nodes.length != 0) {
				Assert.fail("Length expected null");
			}
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			Node[] nodes = application.endpointOut("(chain=Chain1)");
			Assert.assertNotNull(nodes);
			if (nodes.length != 2) {
				Assert.fail("Length expected 2 , received=" + nodes.length);
			}
			for (int i = 0; i < nodes.length; i++) {
				if ((!nodes[i].nodeId().startsWith("adapter")))
					Assert.fail("Adapter not retreived");
			}

		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

	}

	private void api_connectedTo(ApplicationSpecification application) {
		/* connectedTo(ldap) */
		try {
			Node node = null;
			Node[] nodes = application.connectedTo(node);
			Assert.assertNotNull(nodes);
			if (nodes.length != 0) {
				Assert.fail("Length expected =0, recevied = " + nodes.length);
			}
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			Node[] nodes = application.connectedTo("chain=Chain)");
			Assert.fail("Exception not thrown");
		} catch (CiliaInvalidSyntaxException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			Node[] nodes = application.connectedTo("(&(chain=Chain1)(node=adapter_in))");
			Assert.assertNotNull(nodes);
			if (nodes.length != 1) {
				Assert.fail("Length expected =1, recevied = " + nodes.length);
			}
			if (!(nodes[0].nodeId().equals("mediator_1"))) {
				Assert.fail("Wong mediator retreived");
			}
		} catch (CiliaInvalidSyntaxException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			Node[] nodes = application.connectedTo("(&(chain=Chain1)(node=mediator_1))");
			Assert.assertNotNull(nodes);
			if (nodes.length != 1) {
				Assert.fail("Length expected =1, recevied = " + nodes.length);
			}
			if (!(nodes[0].nodeId().equals("mediator_2"))) {
				Assert.fail("Wong mediator retreived");
			}
		} catch (CiliaInvalidSyntaxException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			Node[] nodes = application.connectedTo("(&(chain=Chain1)(node=mediator_2))");
			Assert.assertNotNull(nodes);
			if (nodes.length != 1) {
				Assert.fail("Length expected =1, recevied = " + nodes.length);
			}
			if (!(nodes[0].nodeId().equals("adapter_out"))) {
				Assert.fail("Wong mediator retreived");
			}
		} catch (CiliaInvalidSyntaxException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			Node[] nodes = application.connectedTo("(&(chain=Chain1)(node=adapter_out))");
			Assert.assertNotNull(nodes);
			if (nodes.length != 0) {
				Assert.fail("Length expected =0, retreived = " + nodes.length);
			}

		} catch (CiliaInvalidSyntaxException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		/* connectedTo(node) */
		try {
			Node[] nodes = application.findNodeByFilter("(node=adapter_in)");
			nodes = application.connectedTo(nodes[0]);
			Assert.assertNotNull(nodes);
			if (nodes.length != 1) {
				Assert.fail("Length expected =0, retreived = " + nodes.length);
			}
			if (!(nodes[0].nodeId().equals("mediator_1"))) {
				Assert.fail("Wong mediator retreived");
			}

			nodes = application.findNodeByFilter("(node=mediator_1)");
			nodes = application.connectedTo(nodes[0]);
			Assert.assertNotNull(nodes);
			if (nodes.length != 1) {
				Assert.fail("Length expected =0, retreived = " + nodes.length);
			}
			if (!(nodes[0].nodeId().equals("mediator_2"))) {
				Assert.fail("Wong mediator retreived");
			}

			nodes = application.findNodeByFilter("(node=mediator_2)");
			nodes = application.connectedTo(nodes[0]);
			Assert.assertNotNull(nodes);
			if (nodes.length != 1) {
				Assert.fail("Length expected =0, retreived = " + nodes.length);
			}
			if (!(nodes[0].nodeId().equals("adapter_out"))) {
				Assert.fail("Wong mediator retreived");
			}

			nodes = application.findNodeByFilter("(node=adapter_out)");
			nodes = application.connectedTo(nodes[0]);
			Assert.assertNotNull(nodes);
			if (nodes.length != 0) {
				Assert.fail("Length expected =0, retreived = " + nodes.length);
			}

		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

	}

	private void api_registerListener(ApplicationSpecification application) {
		 CountDownLatch done = new CountDownLatch(1) ;
		 ChainCallbacks callback = new ChainCallbacks(done);
		/* checks illegal parameters */
		try {
			application.addListener(null, (ChainCallback)callback);
			Assert.fail("Exception not thrown");
		} catch (CiliaIllegalParameterException e) {
			/* OK */
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			application.addListener("(chain=",(ChainCallback) callback);
			Assert.fail("Exception not thrown");
		} catch (CiliaInvalidSyntaxException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		/* Check callback */
		try {
			callback.result=false ;
			application.addListener("(&(chain=*)(node=*))", (NodeCallback)callback);

			Builder builder = getCiliaContextService().getBuilder();
			builder.create("Chain2");
			builder.done();
			synchronized (done) {
				done.await(5000, TimeUnit.MILLISECONDS) ;
			}
			Assert.assertTrue("Callback never received " + callback.result, callback.result) ;
			//builder.remove("Chain2") ;
			//builder.done();
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

	}


	private void illegalStateException(ApplicationSpecification application) {
		Node[] nodes = null ;
		try {
			nodes = application.findNodeByFilter("(&(chain=Chain1)(node=adapter_in))");
			Assert.assertNotNull(nodes) ;
			Builder builder = getCiliaContextService().getBuilder();
			Architecture chain = builder.get("Chain1") ;
			chain.remove().adapter().id("adapter_in");
			builder.done();
			Node [] nodeTests = application.connectedTo(nodes[0]);
			Assert.fail("No Exception thrown ");
		}catch (CiliaIllegalStateException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		} 	
		try {
			MediatorComponent component = application.getModel(nodes[0]);
			Assert.fail("No Exception thrown ");		
		}
		catch (CiliaIllegalStateException e) {
			/* OK */
			assertNotNull(e.getMessage());
		}
		catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		try {
			Dictionary dico = application.getProperties(nodes[0]);
			Assert.fail("No Exception thrown ");		
		}
		catch (CiliaIllegalStateException e) {
			/* OK */
			assertNotNull(e.getMessage());
		}
		catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
	}

	private class ChainCallbacks implements ChainCallback, NodeCallback {
		public volatile boolean result;
		public CountDownLatch lock ;

		public ChainCallbacks(CountDownLatch done) {
			lock = done ;
		}

		
		private void stop() {
			 synchronized(lock) {
				 result=true;
				 lock.countDown();
			 }
		}

		public void onAdded(String chainId) {
			stop();
		}

		public void onRemoved(String chainId) {
			stop();	
		}

		public void onStarted(String chainId) {
			 stop();
	 }

		public void onStopped(String chainId) {
			 stop();
		}

		public void onArrival(Node node) {
			stop();	
	    }

		public void onDeparture(Node node) {
			stop();
	  }

		public void onModified(Node node) {
			stop() ;
		}


		public void onBind(Node from, Node to) {
			stop();
		}


		public void onUnBind(Node from, Node to) {
			stop() ;
		}


		public void onStateChange(Node node, boolean isValid) {
			// TODO Auto-generated method stub
			
		}

	}
	
	@Test
	public void testGetBuildChain() {
		CiliaTools.waitToInitialize();
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationSpecification application = ciliaContext.getApplicationSpecification();
		assertNotNull(application);
		ApplicationRuntime runtime = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
	}
	
	@Test
	public void testGetChainId() {
		CiliaTools.waitToInitialize();
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationSpecification application = ciliaContext.getApplicationSpecification();
		assertNotNull(application);
		ApplicationRuntime runtime = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_getChainId(runtime);
	}
	
	@Test
	public void testfindNodeByFilter() {
		CiliaTools.waitToInitialize();
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationSpecification application = ciliaContext.getApplicationSpecification();
		assertNotNull(application);
		ApplicationRuntime runtime = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_findNodeByFilter(runtime);
	}
	
	
	public void testGetChainState() {
		CiliaTools.waitToInitialize();
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationSpecification application = ciliaContext.getApplicationSpecification();
		assertNotNull(application);
		ApplicationRuntime runtime = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_getChainState(runtime);

		//api_endpointsIn(application);
		//api_endpointsOut(application);
		//api_connectedTo(application);
		//api_registerListener(application);
		//illegalStateException(application);
	}
	
	public void testEndpointsIn() {
		CiliaTools.waitToInitialize();
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationSpecification application = ciliaContext.getApplicationSpecification();
		assertNotNull(application);
		ApplicationRuntime runtime = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_getChainState(runtime);
		//api_endpointsIn(application);
		//api_endpointsOut(application);
		//api_connectedTo(application);
		//api_registerListener(application);
		//illegalStateException(application);
	}
	
	public void testEndpointsOut() {
		CiliaTools.waitToInitialize();
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationSpecification application = ciliaContext.getApplicationSpecification();
		assertNotNull(application);
		ApplicationRuntime runtime = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_getChainState(runtime);

		//api_endpointsIn(application);
		//api_endpointsOut(application);
		//api_connectedTo(application);
		//api_registerListener(application);
		//illegalStateException(application);
	}

}
