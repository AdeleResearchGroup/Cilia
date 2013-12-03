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

import fr.liglab.adele.cilia.*;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.*;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.util.FrameworkUtils;
import org.apache.felix.ipojo.util.TrackerCustomizer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.runner.test.ChameleonRunner;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

import javax.inject.Inject;
import java.util.Dictionary;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

@RunWith(ChameleonRunner.class)
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
			Assert.fail("Expected length = " + length + " , current="
					+ nodes.length);
		}
		for (int i = 0; i < nodes.length; i++) {
			if (!(nodes[i] instanceof MediatorComponent)) {
				Assert.fail("Not instance of Mediator Component");
			}
			String qualifiedId = FrameworkUtils.makeQualifiedId(nodes[i]);
			if (!qualifiedId.startsWith(prefix)) {
				Assert.fail("Wrong node retrieved ");
			}
		}

	}

	private void buildChain(String chainId) {
		CiliaContext ciliaContext = getCiliaContextService();

		try {
			Builder builder = ciliaContext.getBuilder();
			Architecture chain = builder.create(chainId);
			chain.create().adapter().type("gui-adapter").id("adapter_in");
			chain.create().adapter().type("console-adapter").id("adapter_out");
			chain.create().mediator().type("immediate-mediator")
			.id("mediator_1");
			chain.create().mediator().type("immediate-mediator")
			.id("mediator_2");
			chain.bind().from("adapter_in:out").to("mediator_1:in");
			chain.bind().from("mediator_1:out").to("mediator_2:in");
			chain.bind().from("mediator_2:out").to("adapter_out:in");
			builder.done();
		} catch (BuilderConfigurationException e) {
			//Assert.fail(e.getMessage());
		} catch (BuilderException e) {
			//Assert.fail(e.getMessage());
		} catch (BuilderPerformerException e) {
			//Assert.fail(e.getMessage());
		}
	}

	private void api_findNodeByFilter(String chainId, ApplicationRuntime runtime) {

		/* Invalid Syntax Exception */
		try {
			Node[] nodes = runtime
					.findNodeByFilter("&(chain="+chainId+")(node=mediator_3))");
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
					.findNodeByFilter("(&(chain="+chainId+")(node=mediator_3))");
			Assert.assertNotNull(nodes);
			Assert.assertArrayEquals(new Node[0], nodes);
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* Return 1 value , no exception */
		try {
			Node[] nodes = runtime
					.findNodeByFilter("(&(chain="+chainId+")(node=mediator_1))");
			Assert.assertNotNull(nodes);
			checkNode(nodes, 1, chainId+"/mediator_");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* Return 2 values , no Exception */
		try {
			Node[] nodes = runtime
					.findNodeByFilter("(&(chain="+chainId+")(node=mediator_*))");
			Assert.assertNotNull(nodes);
			checkNode(nodes, 2, chainId+"/mediator_");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* Return 4 value , no Exception */
		try {
			Node[] nodes = runtime.findNodeByFilter("(chain="+chainId+")");
			Assert.assertNotNull(nodes);
			checkNode(nodes, 4, chainId+"/");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* Return 4 value , no Exception */
		try {
			Node[] nodes = runtime.findNodeByFilter("(node=*)");
			Assert.assertNotNull(nodes);
			checkNode(nodes, 4, chainId+"/");
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
	}

	private void api_getChainId(String chainId, ApplicationRuntime runtime) {
		/* Checks the API chainID */
		String[] ids = runtime.getChainId();
		Assert.assertNotNull("get chain return null", ids);
		if (ids.length < 1) {
			Assert.fail("Expected length =1 ,length =" + ids.length);
		}
		if (!chainId.equals(ids[0])) {
			Assert.fail("Expected chain Id = " +chainId+ " read=" + ids[0]);
		}
	}

	private void api_getChainState(String chainId, ApplicationRuntime runtime) {
		/* Exception : invalid parameter */
		try {
			int state = runtime.getChainState(null);
			Assert.fail("no exception thrown invalid parameter");
		} catch (CiliaIllegalParameterException e) {
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		/* Exception illegalStateException */
		try {
			int state = runtime.getChainState("Chain100");
			Assert.fail("no exception thrown invalid parameter");
		} catch (CiliaIllegalStateException e) {
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* return the instance */
		try {
			int state = runtime.getChainState(chainId);
			if (state != ApplicationRuntime.CHAIN_STATE_IDLE) {
				Assert.fail("Illegate chain state exptected IDLE");
			}

		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		/* test start */
		try {
			runtime.startChain(chainId);
			int state = runtime.getChainState(chainId);
			if (state != ApplicationRuntime.CHAIN_STATE_STARTED) {
				Assert.fail("Illegate chain state expected STARTED");
			}
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			runtime.stopChain(chainId);
			int state = runtime.getChainState(chainId);
			if (state != ApplicationRuntime.CHAIN_STATE_STOPPED) {
				Assert.fail("Illegate chain state expected STOPPED");
			}
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
			Node[] nodes = application
					.connectedTo("(&(chain=Chain1)(node=adapter_in))");
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
			Node[] nodes = application
					.connectedTo("(&(chain=Chain1)(node=mediator_1))");
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
			Node[] nodes = application
					.connectedTo("(&(chain=Chain1)(node=mediator_2))");
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
			Node[] nodes = application
					.connectedTo("(&(chain=Chain1)(node=adapter_out))");
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
		CountDownLatch done = new CountDownLatch(1);
		ChainCallbacks callback = new ChainCallbacks(done);
		/* checks illegal parameters */
		try {
			application.addListener(null, (ChainCallback) callback);
			Assert.fail("Exception not thrown");
		} catch (CiliaIllegalParameterException e) {
			/* OK */
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			application.addListener("(chain=", (ChainCallback) callback);
			Assert.fail("Exception not thrown");
		} catch (CiliaInvalidSyntaxException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		/* Check callback */
		try {
			callback.result = false;
			application.addListener("(&(chain=*)(node=*))",
					(NodeCallback) callback);

			Builder builder = getCiliaContextService().getBuilder();
			builder.create("Chain2");
			builder.done();
			synchronized (done) {
				done.await(5000, TimeUnit.MILLISECONDS);
			}
			Assert.assertTrue("Callback never received " + callback.result,
					callback.result);
			// builder.remove("Chain2") ;
			// builder.done();
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

	}

	private void illegalStateException(ApplicationRuntime application) {
		Node[] nodes = null;
		try {
			nodes = application
					.findNodeByFilter("(&(chain=Chain1)(node=adapter_in))");
			Assert.assertNotNull(nodes);
			Builder builder = getCiliaContextService().getBuilder();
			Architecture chain = builder.get("Chain1");
			chain.remove().adapter().id("adapter_in");
			builder.done();
			Node[] nodeTests = application.connectedTo(nodes[0]);
			Assert.fail("No Exception thrown ");
		} catch (CiliaIllegalStateException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		try {
			MediatorComponent component = application.getModel(nodes[0]);
			Assert.fail("No Exception thrown ");
		} catch (CiliaIllegalStateException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
		try {
			Dictionary dico = application.getProperties(nodes[0]);
			Assert.fail("No Exception thrown ");
		} catch (CiliaIllegalStateException e) {
			/* OK */
			assertNotNull(e.getMessage());
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}
	}

	private class ChainCallbacks implements ChainCallback, NodeCallback {
		public volatile boolean result;
		public CountDownLatch lock;

		public ChainCallbacks(CountDownLatch done) {
			lock = done;
		}

		private void stop() {
			synchronized (lock) {
				result = true;
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
			stop();
		}

		public void onBind(Node from, Node to) {
			stop();
		}

		public void onUnBind(Node from, Node to) {
			stop();
		}

		public void onStateChange(Node node, boolean isValid) {
			stop();
		}

		public void onStateChange(String chainId, boolean event) {
			stop();
		}

	}

    public void removeChain(String chainId){
        CiliaContext ciliaContext = getCiliaContextService();
        try {
            ciliaContext.getBuilder().remove(chainId).done();
        } catch (BuilderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (BuilderPerformerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

	@Test
	public void testGetBuildChain() {
		CiliaHelper.waitSomeTime(2000);
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		ApplicationRuntime runtime = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain("chain1");
	}

	@Test
	public void testGetChainId() {
		CiliaHelper.waitSomeTime(2000);
        String chainId = "chain2";
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		ApplicationRuntime runtime = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain(chainId);
		api_getChainId(chainId, runtime);
	}

	@Test
	public void testfindNodeByFilter() {
		CiliaHelper.waitSomeTime(2000);
        String chainId = "chain3";
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		ApplicationRuntime runtime = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain(chainId);
		api_findNodeByFilter(chainId, runtime);
	}


	public void testGetChainState() {
		CiliaHelper.waitSomeTime(2000);
        String chainId = "Chain4";
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		ApplicationRuntime runtime = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain(chainId);
		api_getChainState(chainId, runtime);
		// api_endpointsIn(application);
		// api_endpointsOut(application);
		// api_connectedTo(application);
		// api_registerListener(application);
		// illegalStateException(application);
	}

	public void testEndpointsIn() {
		CiliaHelper.waitSomeTime(2000);
        String chainId = "Chain5";
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		ApplicationRuntime runtime = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain(chainId);
		api_getChainState(chainId, runtime);
		// api_endpointsIn(application);
		// api_endpointsOut(application);
		// api_connectedTo(application);
		// api_registerListener(application);
		// illegalStateException(application);
	}

	public void testEndpointsOut() {
		CiliaHelper.waitSomeTime(2000);
        String chainId = "Chain6";
		CiliaContext ciliaContext = getCiliaContextService();
		ApplicationRuntime application = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		ApplicationRuntime runtime = ciliaContext.getApplicationRuntime();
		assertNotNull(application);
		buildChain(chainId);
		//api_getChainState(chainId, runtime);

		// api_endpointsIn(application);
		// api_endpointsOut(application);
		// api_connectedTo(application);
		// api_registerListener(application);
		// illegalStateException(application);
	}

	private class CustomTracker implements TrackerCustomizer {

		/* (non-Javadoc)
		 * @see org.apache.felix.ipojo.util.TrackerCustomizer#addedService(org.osgi.framework.ServiceReference)
		 */
		public void addedService(ServiceReference arg0) {
			System.out.println("Added Service with:\n" + arg0.getPropertyKeys());
			// TODO Auto-generated method stub
		}

		/* (non-Javadoc)
		 * @see org.apache.felix.ipojo.util.TrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
		 */
		public boolean addingService(ServiceReference arg0) {
			System.out.println("To Add Service with:\n" + arg0.getPropertyKeys());
			return false;
		}

		/* (non-Javadoc)
		 * @see org.apache.felix.ipojo.util.TrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void modifiedService(ServiceReference arg0, Object arg1) {
		}

		/* (non-Javadoc)
		 * @see org.apache.felix.ipojo.util.TrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void removedService(ServiceReference arg0, Object arg1) {
		}
		
	}
	
}
