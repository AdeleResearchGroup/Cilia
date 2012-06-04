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
import fr.liglab.adele.cilia.exceptions.CiliaInvalidSyntaxException;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.CiliaContainer;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.util.concurrent.Mutex;

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

		Dictionary dico = null;
		try {
			dico = application.properties(node[0]);
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
		/* Chain Registration */
		ChainCb cb = new ChainCb();
		try {
			application.addListener(null, cb);
			Assert.fail("Exception not thrown");
		} catch (CiliaIllegalParameterException e) {
			/* OK */
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			application.addListener("(chain=", cb);
			Assert.fail("Exception not thrown");
		} catch (CiliaInvalidSyntaxException e) {
			/* OK */
		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

		try {
			application.addListener("(chain=*)", cb);
			Builder builder = getCiliaContextService().getBuilder();
			cb.start(0) ;
			Architecture chain = builder.create("Chain2");
			builder.done();
			Thread.currentThread().sleep(2000);
			//synchronized (cb.synchro) {
			//	cb.synchro.wait(2000);
			//}

			Assert.fail("error cb=" + cb.result) ;
				
			//Assert.assertTrue("Callback no recevied", cb.result);

		} catch (Exception e) {
			Assert.fail("Invalid exception thrown " + e.getMessage());
		}

	}

	private void api_unregisterListener(ApplicationSpecification application) {

	}

	private void callback(ApplicationSpecification application) {

	}

	private void illegalStateException(ApplicationSpecification application) {
	}

	private class ChainCb implements ChainCallback {

		public Object synchro = new Object();
		public boolean result=false;
		public int evt ;
		private Mutex mutex ;
		public ChainCb() {

		}

		public void start(int evt) {
			result=false ;
			this.evt =evt ;
			mutex =new Mutex();
		}
		public void stop() {
			result = true;
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
			// TODO Auto-generated method stub		
		}

		public void onDeparture(Node node) {
			// TODO Auto-generated method stub
			
		}

		public void onModified(Node node) {
			// TODO Auto-generated method stub
			
		}



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
		api_connectedTo(application);
		api_registerListener(application);
		api_unregisterListener(application);
		callback(application);
		illegalStateException(application);
	}

}
