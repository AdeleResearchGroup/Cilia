/*
 * Copyright Adele Team LIG
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.liglab.adele.cilia.core.tests;

import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

import java.util.Hashtable;

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

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.BuilderConfigurationException;
import fr.liglab.adele.cilia.exceptions.BuilderException;
import fr.liglab.adele.cilia.exceptions.BuilderPerformerException;
import fr.liglab.adele.cilia.exceptions.CiliaException;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@RunWith(JUnit4TestRunner.class)
public class BuilderTest {

	@Inject
	private BundleContext context;

	//private OSGiHelper osgi;

	@Before
	public void setUp() {
		//osgi = new OSGiHelper(context);
	}

	@After
	public void tearDown() {
		//osgi.dispose();
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
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-runtime").versionAsInProject(),
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-core").versionAsInProject()
				)); // The target
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
	/**
	 * Test that we can reach the builder and is not null. 
	 */
	@Test
	public void testBuilder() {
		waitToInitialize();
		Builder builder = getBuilder();
		//Builder builder = null;
		Assert.assertNotNull(builder);
	}

	/**
	 * Test that we can create a mediation chain.
	 */
	@Test
	public void createChain(){
		waitToInitialize();
		try {
			createNewChain("chain-id");
		} catch (CiliaException e) {
			Assert.fail("Unable to create Chain");
		} 
	}
	/**
	 * Test that we can create at least two different mediation chains.
	 */
	@Test
	public void createTwoChains(){
		waitToInitialize();
		try {
			createNewChain("chain-id");
			createNewChain("chain-id2");
		} catch (CiliaException e) {
			Assert.fail("Error when creating multiple chains");
		} 
	}
	/**
	 * Test that we can not create two mediation chain with the same id.
	 * @throws BuilderPerformerException 
	 * @throws BuilderException 
	 */
	@Test
	public void createExistantChain(){
		waitToInitialize();
		try {
			String id = "myId";
			createNewChain(id);
			createNewChain(id);
			Assert.fail("Must throw an exception");
		} catch (CiliaException e) {}

	}

	/**
	 * Test That we can't retrieve a builder for an inexistent chain.
	 * @throws BuilderPerformerException 
	 * @throws BuilderException 
	 */
	public @Test
	void createInvalidChain() {
		waitToInitialize();
		Builder builder = getBuilder();
		try {
			Architecture arch = builder.get("toto");
			Assert.fail("We can't retrieve an inexistant chain");
		} catch (BuilderException e) {
		}
	}

	/**
	 * Test that we can't use a builder for two chains.
	 */
	@Test 
	public void getAnInExistanBuilderChain(){
		waitToInitialize();
		try {
			Builder builder = getBuilder();
			Architecture arch = builder.create("firstChain");
			builder.get("secondChain");
			Assert.fail("We can't use a builder for two different chains");
		} catch (CiliaException e) {}	
	}
	/**
	 * Test that we can't use an invalid builder.
	 * @throws BuilderPerformerException 
	 * @throws BuilderException 
	 */
	@Test
	public void testBuilderInvalidity() {
		waitToInitialize();
		Builder builder = getBuilder();
		Architecture arch = null;
		try {
			builder.done();
			Assert.fail("It muist throw an exception. Invalid architecture");
		}catch(CiliaException ex){}
		try{
			arch = builder.create("chain-1");
			arch.create().mediator().type("toto").id("titi");
			builder.done();
			arch.configure();
			Assert.fail("Must throw an BuilderException because an invalid builder");
		}catch(CiliaException ex) {}
		try {
			arch.bind();
			Assert.fail("Must throw an BuilderException because an invalid builder");
		} catch (BuilderException e) {}
		try {
			arch.create();
			Assert.fail("Must throw an BuilderException because an invalid builder");
		} catch (BuilderException e) {}
		
		try {
			arch.remove();
			Assert.fail("Must throw an BuilderException because an invalid builder");
		} catch (BuilderException e) {}
		try {
			arch.unbind();
			Assert.fail("Must throw an BuilderException because an invalid builder");
		} catch (BuilderException e) {}
	}

	
	public void createNewChain(String id)  throws BuilderException, BuilderPerformerException{
		Builder builder = getBuilder();
		builder.create(id);
		builder.done();
	}

	//@Test
	public void test() {
		Builder builder = getBuilder();
		try {
			Architecture arch = builder.create("MyChain");
			arch.create().mediator().type("toto").namespace("nspace")
			.id("tata");
			arch.create().mediator().type("toto").id("tata").configure()
			.key("toto").value("rr").key("").value("sq");
			arch.create().mediator().type("toto").id("dsds").configure();
			arch.create().mediator().type("rere").id("dsds");
			arch.create().mediator().type("dsds").namespace("dsds").id("dsds");
			arch.bind().using("ea").from("mediator1:titi").to("mediator2:tito");
			arch.bind().from("mediator:toto").to("mediator2:end");
			arch.configure().mediator().id("toto").key("tata").value("value")
			.key("isi").value("rere").key("tata").value("value")
			.key("isi").value("rere").key("tata").value("value")
			.key("isi").value("rere").key("tata").value("value")
			.key("isi").value("rere").key("tata").value("value")
			.key("isi").value("rere").set(new Hashtable());

			// ContentBasedRouting cb = new ContentBasedRouting();
			// cb.evaluator("ldap").condition("(toto)").to("portX");
			// arch.create().mediator().type("b").id("i").configure().dispatcher(cbd);

			builder.done();
		} catch (BuilderException be) {

		} catch (BuilderConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BuilderPerformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Builder getBuilder() {
		ServiceReference sr[] = null;
		try {
			sr = context.getServiceReferences (CiliaContext.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(sr);
		CiliaContext ccontext = (CiliaContext) context.getService(sr[0]);
		
		return ccontext.getBuilder();
	}

	public void waitToInitialize() {
		try {
			Thread.sleep(2500);// wait to be registered
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
