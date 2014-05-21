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
package cilia.runtime.context.test;

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.BuilderConfigurationException;
import fr.liglab.adele.cilia.exceptions.BuilderException;
import fr.liglab.adele.cilia.exceptions.BuilderPerformerException;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.model.CiliaContainer;
import fr.liglab.adele.cilia.model.MediatorComponent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.runner.test.ChameleonRunner;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

import javax.inject.Inject;
import java.util.Hashtable;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
@RunWith(ChameleonRunner.class)
public class BuilderTest {

    @Inject
    private BundleContext context;

    private OSGiHelper osgi;

    private CiliaHelper cilia;

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

    /**
     * Test that we can reach the builder and is not null.
     */
    @Test
    public void testBuilder() {
        CiliaHelper.waitSomeTime(2000);
        Builder builder = getBuilder();
        //Builder builder = null;
        Assert.assertNotNull(builder);
    }

    /**
     * Test that we can create a mediation chain.
     */
    @Test
    public void createChain() {
        CiliaHelper.waitSomeTime(2000);
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
    public void createTwoChains() {
        CiliaHelper.waitSomeTime(2000);
        try {
            createNewChain("chain-id");
            createNewChain("chain-id2");
        } catch (CiliaException e) {
            Assert.fail("Error when creating multiple chains");
        }
    }

    /**
     * Test that we can not create two mediation chain with the same id.
     *
     * @throws BuilderPerformerException
     * @throws BuilderException
     */
    @Test
    public void createExistantChain() {
        CiliaHelper.waitSomeTime(2000);
        try {
            String id = "myId";
            createNewChain(id);
            createNewChain(id);
            Assert.fail("Must throw an exception");
        } catch (CiliaException e) {
        }

    }

    /**
     * Test That we can modify an inexistent chain, and wait until it is ready.
     *
     * @throws BuilderPerformerException
     * @throws BuilderException
     */
    @Test
    public void modifyInexistantChain() {
        CiliaHelper.waitSomeTime(2000);
        try {
            Builder builder = getBuilder();
            Architecture arch = builder.get("toto");
            arch.configure().mediator().id("modifiedMediator").key("modifiedValue").value("newValue");
            builder.done();
        } catch (CiliaException e) {
            Assert.fail("Must pass OK when modifying inexistant chain, it must create a chain listener: " + e.getMessage());
        }
        //Now we create the chain and the mediator.

        try {
            Builder nbuilder = getBuilder();
            Architecture arch = nbuilder.create("toto");
            arch.create().mediator().type("TOTO").id("modifiedMediator").configure().key("modifiedValue").value("oldValue");
            nbuilder.done();
        } catch (CiliaException e) {
            Assert.fail("Must pass OK when creating a chain, it must trigger the chain listener");
        }
        CiliaHelper.waitSomeTime(500);//Wait until event has changed to modify mediator.
        MediatorComponent mc = cilia.getMediatorModel("toto", "modifiedMediator");
        Assert.assertNotNull(mc);
        Assert.assertEquals("newValue", mc.getProperty("modifiedValue"));
    }

    /**
     * Test That we can modify an inexistent chain, and wait until it is ready.
     *
     * @throws BuilderPerformerException
     * @throws BuilderException
     */
    @Test
    public void modifyInexistantComponent() {
        CiliaHelper.waitSomeTime(2000);
        try {
            Builder builder = getBuilder();
            Architecture arch = builder.create("toto");
            arch.configure().mediator().id("modifiedMediator").key("modifiedValue").value("newValue");
            builder.done();
        } catch (CiliaException e) {
            Assert.fail("Must pass OK when modifying inexistant chain, it must create a chain listener: " + e.getMessage());
        }
        //Now we create the chain and the mediator.

        try {
            Builder nbuilder = getBuilder();
            Architecture arch = nbuilder.get("toto");
            arch.create().mediator().type("TOTO").id("modifiedMediator").configure().key("modifiedValue").value("oldValue");
            nbuilder.done();
        } catch (CiliaException e) {
            Assert.fail("Must pass OK when creating a chain, it must trigger the chain listener");
        }
        CiliaHelper.waitSomeTime(10000);//Wait until event has changed to modify mediator.
        MediatorComponent mc = cilia.getMediatorModel("toto", "modifiedMediator");
        Assert.assertNotNull(mc);
        Assert.assertEquals("newValue", mc.getProperty("modifiedValue"));
    }

    /**
     * Test that we can't use a builder for two chains.
     */
    @Test
    public void getAnInExistanBuilderChain() {
        CiliaHelper.waitSomeTime(2000);
        try {
            Builder builder = getBuilder();
            builder.create("firstChain");
            builder.get("secondChain");
            Assert.fail("We can't use a builder for two different chains");
        } catch (CiliaException e) {
        }
    }

    /**
     * Test that we can't use an invalid builder.
     *
     * @throws BuilderPerformerException
     * @throws BuilderException
     */
    @Test
    public void testBuilderInvalidity() {
        CiliaHelper.waitSomeTime(2000);
        Builder builder = getBuilder();
        Architecture arch = null;
        try {
            builder.done();
            Assert.fail("It muist throw an exception. Invalid architecture");
        } catch (CiliaException ex) {
        }
        try {
            arch = builder.create("chain-1");
            arch.create().mediator().type("toto").id("titi");
            builder.done();
            arch.configure();
            Assert.fail("Must throw an BuilderException because an invalid builder");
        } catch (CiliaException ex) {
        }
        try {
            arch.bind();
            Assert.fail("Must throw an BuilderException because an invalid builder");
        } catch (BuilderException e) {
        }
        try {
            arch.create();
            Assert.fail("Must throw an BuilderException because an invalid builder");
        } catch (BuilderException e) {
        }

        try {
            arch.remove();
            Assert.fail("Must throw an BuilderException because an invalid builder");
        } catch (BuilderException e) {
        }
        try {
            arch.unbind();
            Assert.fail("Must throw an BuilderException because an invalid builder");
        } catch (BuilderException e) {
        }
    }

    @Test
    public void cannotCreateMediatorWithoutId() {
        CiliaHelper.waitSomeTime(2000);
        Builder builder = getBuilder();

        Architecture arch;
        try {
            arch = builder.create("chain-1");
            arch.create().mediator().type("toto");
            builder.done();
            Assert.fail("Must throw BuilderPerformerException");
        } catch (CiliaException e) {
        }
    }

    @Test
    public void cannotCreateComponentWithExistantId() {
        CiliaHelper.waitSomeTime(2000);
        Builder builder = getBuilder();
        try {
            Architecture arch = builder.create("chain-1");
            arch.create().mediator().type("toto").id("id1");
            arch.create().mediator().type("toto").id("id1");
            builder.done();
            Assert.fail("Must throw BuilderPerformerException");
        } catch (Exception ex) {
        }
    }

    @Test
    public void cannotCreateComponentWithExistantId2() {
        CiliaHelper.waitSomeTime(2000);
        Builder builder = getBuilder();
        try {
            Architecture arch = builder.create("chain-1");
            arch.create().mediator().type("toto").id("id1");
            arch.create().adapter().type("toto").id("id1");
            builder.done();
            Assert.fail("Must throw BuilderPerformerException");
        } catch (Exception ex) {
        }
    }

    @Test
    public void cannotCreateComponentWithExistantId3() {
        CiliaHelper.waitSomeTime(2000);
        Builder builder = getBuilder();
        try {
            Architecture arch = builder.create("chain-1");
            arch.create().adapter().type("toto").id("id1");
            arch.create().adapter().type("toto").id("id1");
            builder.done();
            Assert.fail("Must throw BuilderPerformerException");
        } catch (CiliaException ex) {
        }
    }

    @Test
    public void cannotCreateComponentWithExistantId4() {
        CiliaHelper.waitSomeTime(2000);
        Builder builder = getBuilder();
        try {
            Architecture arch = builder.create("chain-1");
            arch.create().adapter().type("toto").id("id1");
            arch.create().mediator().type("toto").id("id1");
            builder.done();
            Assert.fail("Must throw BuilderPerformerException");
        } catch (CiliaException ex) {
        }
    }

    @Test
    public void cannotBindInexistantComponents() {
        CiliaHelper.waitSomeTime(2000);
        Builder builder = getBuilder();
        try {
            Architecture arch = builder.create("chain-1");
            arch.create().adapter().type("toto").id("id1");
            arch.create().mediator().type("toto").id("id1");
            //arch.bind().
            builder.done();
            Assert.fail("Must throw BuilderPerformerException");
        } catch (CiliaException ex) {
        }
    }


    public void createNewChain(String id) throws BuilderException, BuilderPerformerException {
        Builder builder = getBuilder();
        builder.create(id);
        builder.done();
    }

    //@Test
    public void test() {
        Builder builder = getBuilder();
        try {
            Architecture arch = builder.create("MyChain");

            arch.create().adapter().type("fd").id("").configure().key("toto").value("tata");

            arch.create().mediator().type("toto").namespace("nspace")
                    .id("tata");
            arch.create().mediator().type("toto").id("tata").configure()
                    .key("toto").value("rr").key("").value("sq");
            arch.create().mediator().type("toto").id("dsds").configure();
            arch.create().mediator().type("rere").id("dsds");
            arch.create().mediator().type("dsds").namespace("dsds").id("dsds");

            arch.bind().using("ea").from("mediator1:titi").to("mediator2:tito").configure(new Hashtable<String, String>());

            arch.bind().from("mediator:toto").to("mediator2:end");
            arch.configure().mediator().id("toto").key("tata").value("value")
                    .key("isi").value("rere").key("tata").value("value")
                    .key("isi").value("rere").key("tata").value("value")
                    .key("isi").value("rere").key("tata").value("value")
                    .key("isi").value("rere").key("tata").value("value")
                    .key("isi").value("rere").set(new Hashtable<String, String>());

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
        osgi.getServiceObject(CiliaContainer.class.getName(), null);
        CiliaContext ccontext = (CiliaContext) osgi.getServiceObject(CiliaContext.class.getName(), null);
        return ccontext.getBuilder();
    }
}
