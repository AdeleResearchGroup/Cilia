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
/**
 *
 */
package fr.liglab.adele.cilia.tcp.test;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.ICollector;
import fr.liglab.adele.cilia.framework.ISender;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.helper.CollectorHelper;
import fr.liglab.adele.cilia.runtime.CiliaInstance;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.runner.test.ChameleonRunner;
import org.ow2.chameleon.testing.helpers.IPOJOHelper;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

import javax.inject.Inject;
import java.net.URL;
import java.util.Hashtable;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
@RunWith(ChameleonRunner.class)
public class TestTCPAdapters {


    private final static String COLLECTOR = "tcp-collector";

    private final static String SENDER = "tcp-sender";

    private final static int PORT = 9999;

    private final static String HOSTNAME = "localhost";


    @Inject
    protected BundleContext context;

    protected CiliaHelper cilia;

    protected OSGiHelper osgi;

    protected IPOJOHelper ipojo;

    @Before
    public void setUp() {
        osgi = new OSGiHelper(context);
        ipojo = new IPOJOHelper(context);
        cilia = new CiliaHelper(context);
    }

    @After
    public void tearDown() {
        cilia.dispose();
        osgi.dispose();
        ipojo.dispose();
    }


    public CollectorHelper createCollectorHelper(Hashtable<String, Object> props) {
        CiliaInstance ci = cilia.createInstance(COLLECTOR, props);
        ci.start();
        Assert.assertEquals(ComponentInstance.VALID, ci.getState());
        ICollector ic = (ICollector) ci.getObject();
        Assert.assertNotNull(ic);
        CollectorHelper ch = cilia.getCollectorHelper(ic);
        return ch;
    }

    public ISender createSender(Hashtable<String, Object> props) {
        CiliaInstance si = cilia.createInstance(SENDER, props);
        si.start();
        Assert.assertEquals(ComponentInstance.VALID, si.getState());
        ISender is = (ISender) si.getObject();
        Assert.assertNotNull(is);
        return is;
    }

    @Test
    public void validateServices() {
        CiliaHelper.waitSomeTime(5000);
        Factory col = ipojo.getFactory(COLLECTOR);
        Assert.assertNotNull(col);
        Assert.assertEquals(Factory.VALID, col.getState());
        Factory snd = ipojo.getFactory(SENDER);
        Assert.assertNotNull(snd);
        Assert.assertEquals(Factory.VALID, snd.getState());
    }

    @Test
    public void collectorTest() {
        CiliaHelper.waitSomeTime(5000);

        CollectorHelper ch = createCollectorHelper(getTestProperties(9876));

        injectMessages(9876);
        CiliaHelper.checkReceived(ch, 10, 10000);

        Assert.assertEquals(10, ch.countReceived());
        Data data = ch.getLast();

        System.out.println("Last Data");
        System.out.println(data.getAllData());
        cilia.dispose();
    }

    @Test
    public void senderTest() {
        CiliaHelper.waitSomeTime(2000);

        //Get collector Helper
        CollectorHelper ch = createCollectorHelper(getTestProperties(9875));
        //Get Sender
        ISender is = createSender(getTestProperties(9875));

        int i;
        for (i = 0; i < 10; i++) {
            Data ndata = new Data("Test number " + i, "data");
            is.send(ndata);
        }

        CiliaHelper.checkReceived(ch, 10, 10000);
        //See if all messages are received.
        Assert.assertEquals(10, ch.countReceived());
        Data data = ch.getLast();
        Assert.assertEquals("Test number 9", data.getContent());
        //System.out.println("Last Data");
        System.out.println(data.getAllData());
        cilia.dispose();
    }

    private void injectMessages(int port) {

        ISender sender = createSender(getTestProperties(port));
        int i;
        for (i = 0; i < 10; i++) {
            System.out.println("Sending Message: " + i);
            Data data = new Data("Test number " + i, "Test number " + i);
            sender.send(data);
        }

    }


    @Test
    public void testAdapters() {
        CiliaHelper.waitSomeTime(2000);
        URL url = context.getBundle().getResource("testAdapter.dscilia");
        cilia.load(url);
        System.out.println("will wait");
        boolean found = cilia.waitToChain("toto", 6000);
        System.out.println("found chain " + found);
        //create a collector to test sending.
        CollectorHelper ch = createCollectorHelper(getTestProperties(9999));

        //create a sender to test sending.
        ISender is = createSender(getTestProperties(8888));
        if (!cilia.checkValidState("toto", "a1", 5000) || !cilia.checkValidState("toto", "a2", 5000)) {
            Assert.fail("Unable to have a valid state for components in chain toto");
        }
        int i;
        for (i = 0; i < 10; i++) {
            Data ndata = new Data("Test number " + i, "data");
            is.send(ndata);
        }
        CiliaHelper.checkReceived(ch, 10, 10000);
        //See if all messages are received.
        Assert.assertEquals(10, ch.countReceived());
        Data data = ch.getLast();
        Assert.assertEquals("Test number 9", data.getContent());
        //System.out.println("Last Data");
        System.out.println(data.getAllData());
        cilia.dispose();
    }

    private Hashtable<String, Object> getTestProperties(int port) {
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put("port", port);
        props.put("hostname", HOSTNAME);
        return props;
    }

}
