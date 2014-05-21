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
package cilia.framework.components.tests;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.data.DataEnrichment;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.helper.MediatorTestHelper;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.runner.test.ChameleonRunner;
import org.ow2.chameleon.testing.helpers.OSGiHelper;

import javax.inject.Inject;
import java.util.Hashtable;

/**
 * This class will test the behaviour of processors.
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
@RunWith(ChameleonRunner.class)
public class SchedulerTest {

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
     * Test the periodic-scheduler behavior
     */
    @Test
    public void testPeriodicScheduler() {
        CiliaHelper.waitSomeTime(2000);
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("period", "500");
        properties.put("delay", "2000");
        MediatorTestHelper helper = cilia.getSchedulerHelper("periodic-scheduler", "fr.liglab.adele.cilia", properties);
        CiliaHelper.waitSomeTime(3000);//initial delay by default
        helper.injectData(new Data("Data one", "data one"));
        helper.injectData(new Data("Data two", "data two"));
        CiliaHelper.checkReceived(helper, 2, 20000);

        Assert.assertEquals(2, helper.getAmountData());
        helper.getData();//to erase processed data.
        //We inject another set of data
        helper.injectData(new Data("Data three", "data three"));
        helper.injectData(new Data("Data four", "data four"));
        helper.injectData(new Data("Data five", "data five"));
        //It must not retrieve anything, it must wait some time (periodically)
        Assert.assertEquals(0, helper.getAmountData());
        CiliaHelper.checkReceived(helper, 3, 10000);
        //We must now have the three messages
        Assert.assertEquals(3, helper.getAmountData());
        cilia.dispose();
    }

    /**
     * Test the correlation-scheduler behavior
     */
    @Test
    public void testCorrelationScheduler() {
        CiliaHelper.waitSomeTime(2000);
        Hashtable<String, String> properties = new Hashtable<String, String>();
        MediatorTestHelper helper = cilia.getSchedulerHelper("correlation-scheduler", "fr.liglab.adele.cilia", properties);

        helper.injectData(createCorrelatedData("Animal", 0, 3, "This contain a Dog"));
        helper.injectData(createCorrelatedData("Animal", 1, 3, "This contain a Cat"));
        helper.injectData(createCorrelatedData("Transport", 0, 2, "This contain a Boat"));
        //Not ready
        Assert.assertEquals(0, helper.getAmountData());
        //Inject the last waited animal
        helper.injectData(createCorrelatedData("Animal", 2, 3, "This contain a Rat"));
        CiliaHelper.checkReceived(helper, 3, 2000);
        Assert.assertEquals(3, helper.getAmountData()); //We collect the waited correlated
        helper.getData();//To erase.
        //We inject the missing data
        helper.injectData(createCorrelatedData("Transport", 1, 2, "This contain a Train"));
        CiliaHelper.checkReceived(helper, 2, 2000);
        Assert.assertEquals(2, helper.getAmountData()); //We collect the waited correlated
        cilia.dispose();
    }

    /**
     * Test counter-scheduler
     */
    @Test
    public void testCounterSchedulerWithoutCorrelation() {
        CiliaHelper.waitSomeTime(2000);
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        Hashtable<String, String> count = new Hashtable<String, String>();
        count.put("A", "(Test.Category=Animal)");
        count.put("B", "(Test.Category=Transport)");

        //A is Animal, B is transport
        properties.put("condition", "(&(A=3)(B=2))");
        properties.put("count", count);
        MediatorTestHelper helper = cilia.getSchedulerHelper("counter-scheduler", "fr.liglab.adele.cilia", properties);

        helper.injectData(createCorrelatedData("Animal", 0, 3, "This contain a Dog"));
        CiliaHelper.waitSomeTime(100);//wait to be received
        Assert.assertEquals(0, helper.getAmountData());
        helper.injectData(createCorrelatedData("Animal", 1, 3, "This contain a Cat"));
        Assert.assertEquals(0, helper.getAmountData());
        helper.injectData(createCorrelatedData("Transport", 0, 2, "This contain a Boat"));
        Assert.assertEquals(0, helper.getAmountData());
        helper.injectData(createCorrelatedData("Animal", 2, 3, "This contain a Rat"));
        Assert.assertEquals(0, helper.getAmountData()); //We collect the waited correlated
        //We inject the missing data
        helper.injectData(createCorrelatedData("Transport", 1, 2, "This contain a Train"));
        CiliaHelper.checkReceived(helper, 5, 5000);
        Assert.assertEquals(5, helper.getAmountData()); //We collect the waited correlated
        cilia.dispose();

    }

    private Data createCorrelatedData(String id, int number, int size, String content) {
        Data data = new Data(content, "data");
        data.setProperty("Test.Category", id);
        data = DataEnrichment.addCorrelationInfo(data, size, number, id);
        return data;
    }
}
