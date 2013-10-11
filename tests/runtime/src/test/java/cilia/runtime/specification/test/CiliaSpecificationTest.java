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
package cilia.runtime.specification.test;

import fr.liglab.adele.cilia.*;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.*;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.util.FrameworkUtils;
import fr.liglab.adele.commons.distribution.test.AbstractDistributionBaseTest;
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
import org.osgi.framework.ServiceReference;

import javax.inject.Inject;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class CiliaSpecificationTest  extends AbstractDistributionBaseTest {

	
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


	public CiliaContext getCiliaContextService() {
		ServiceReference sr[] = null;
        osgi.waitForService(CiliaContext.class.getName(), null,2000);
		sr = osgi.getServiceReferences(CiliaContext.class.getName(), null);
        assertNotNull(sr);
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
			String qualifiedId = FrameworkUtils.makeQualifiedId(nodes[i]) ;
			if (!qualifiedId.startsWith(prefix)) {
				Assert.fail("Wrong node retrieved ");
			}
		}

	}

	private void buildChain() {
		CiliaContext ciliaContext = getCiliaContextService();

		try {
			Builder builder = ciliaContext.getBuilder();
			Architecture chain = builder.create("Chain1");
			chain.create().adapter().type("type").namespace("sample").id("adapter_in");
			chain.create().adapter().type("type").namespace("sample").id("adapter_out");
			chain.create().mediator().type("type").namespace("sample").id("mediator_1");
			chain.create().mediator().type("type").namespace("sample").id("mediator_2");
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

	private void api_findNodeByFilter(ApplicationRuntime application) {

		/* Invalid Syntax Exception */
		try {
			Node[] nodes = application
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
			Node[] nodes = application.findNodeByFilter("(toto=mediator_1)");
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
			Node[] nodes = application.findNodeByFilter(null);
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

	private void api_getChainId(ApplicationRuntime application) {
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

	private void api_getChain(ApplicationRuntime application) {
		/* Exception : invalid parameter */
		try {
			Chain chain = application.getChain(null);
			Assert.fail("no exception thrown invalid parameter");
		} catch (CiliaIllegalParameterException e) {
			/* OK */
			assertNotNull(e.getMessage());
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

	public void api_getModel(ApplicationRuntime application) {

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
			String qualifiedId = FrameworkUtils.makeQualifiedId(mediatorModel) ;
			if (!qualifiedId.startsWith("Chain1/mediator_1")) {
				Assert.fail("Wrong node");
			}
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			Node[] node = application.findNodeByFilter("(node=mediator_1)");
			Assert.assertNotNull(node);
			MediatorComponent mediatorModel = application.getModel(node[0]);
			String qualifiedId = FrameworkUtils.makeQualifiedId(mediatorModel) ;
			if (!qualifiedId.startsWith("Chain1/mediator_1")) {
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
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

	}

	private void api_getPropertie(ApplicationRuntime application) {
		Node[] node = null;
		try {
			node = application.findNodeByFilter("(node=mediator_1)");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());

		}

		try {
			Dictionary dico = application.getProperties(null);
		} catch (CiliaIllegalParameterException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		Dictionary dico = null;
		try {
			dico = application.getProperties(node[0]);
			Assert.assertNotNull(dico);
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			dico.put("key", "testValue");
		} catch (UnsupportedOperationException e) {
			/* OK */
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
	}

	private void api_endpointsIn(ApplicationRuntime application) {

		try {
			Node[] nodes = application.endpointIn(null);
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
			Node[] nodes = application.endpointIn("(chain=Chain))");
			Assert.fail("Exception not thrown");
		} catch (CiliaIllegalParameterException e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		} catch (CiliaInvalidSyntaxException e) {
			/* OK */
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

	private void api_endpointsOut(ApplicationRuntime application) {

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

	private void api_connectedTo(ApplicationRuntime application) {
		/* connectedTo(ldap) */
		try {
			Node node = null;
			try {
				Node[] nodes = application.connectedTo(node);
				Assert.fail("Exception not thrown");
			} catch (CiliaIllegalStateException e) {
				Assert.fail("Invalid exception thrown " + e.getMessage());
			}
		} catch (CiliaIllegalParameterException e) {
			/* OK */
			assertNotNull(e.getMessage());
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

	private void api_registerListener(ApplicationRuntime application) {
		 CountDownLatch done = new CountDownLatch(1) ;
		 ChainCallbacks callback = new ChainCallbacks(done);
		/* checks illegal parameters */
		try {
			application.addListener(null, (ChainCallback)callback);
			Assert.fail("Exception not thrown");
		} catch (CiliaIllegalParameterException e) {
			/* OK */
			assertNotNull(e.getMessage());
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


	private void illegalStateException(ApplicationRuntime application) {
		Node[] nodes = null ;
		try {
			nodes = application.findNodeByFilter("(&(chain=Chain1)(node=adapter_in))");
			Assert.assertNotNull(nodes) ;
			MediatorComponent mediatorModel = application.getModel(nodes[0]);
			String qualifiedId = FrameworkUtils.makeQualifiedId(mediatorModel) ;
			if (!qualifiedId.startsWith("Chain1/adapter_in")) {
				Assert.fail("Wrong node");
			}
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


		public void onStateChange(String chainId, boolean event) {
			stop();
		}

	}

	@Test
	public void testBuildChain() {
		CiliaHelper.waitSomeTime(2000);		
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
	}
	@Test
	public void testGetChainId() {
		CiliaHelper.waitSomeTime(2000);		
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_getChainId(application);
	}
	
	
	@Test
	public void testFindNodeByFilter() {
		CiliaHelper.waitSomeTime(2000);
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_findNodeByFilter(application);
	}
	
	@Test
	public void testGetChain() {
		CiliaHelper.waitSomeTime(2000);
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_getChain(application);
	}
	
	@Test
	public void testGetModel() {
		CiliaHelper.waitSomeTime(2000);
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_getModel(application);
	}
	
	@Test
	public void testGetPropertie() {
		CiliaHelper.waitSomeTime(2000);
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_getPropertie(application);
	}
	
	@Test
	public void testEndpointsIn() {
		CiliaHelper.waitSomeTime(2000);
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_endpointsIn(application);
	}
	
	@Test
	public void testEndpointsOut() {
		CiliaHelper.waitSomeTime(2000);
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_endpointsOut(application);
	}
	
	@Test
	public void testConnectedTo() {
		CiliaHelper.waitSomeTime(2000);
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_connectedTo(application);

	}
	
	@Test
	public void testIllegalStateException() {
		CiliaHelper.waitSomeTime(2000);
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		illegalStateException(application);
	}
	
	//@Test
	public void testRegisterListener() {
		CiliaHelper.waitSomeTime(2000);
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_registerListener(application);
	}
	
	
	public void api_all() {
		CiliaHelper.waitSomeTime(2000);
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain();
		api_getChainId(application);
		api_findNodeByFilter(application);
		api_getChain(application);
		api_getModel(application);
		api_getPropertie(application);
		api_endpointsIn(application);
		api_endpointsOut(application);
		api_connectedTo(application);
		api_registerListener(application);
		illegalStateException(application);
	}

}
