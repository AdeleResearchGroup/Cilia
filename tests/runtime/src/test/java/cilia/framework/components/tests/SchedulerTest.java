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

import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

import java.util.Hashtable;
import java.util.List;

import junit.framework.Assert;

import org.apache.felix.ipojo.test.helpers.OSGiHelper;
import org.junit.After;
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

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.helper.MediatorTestHelper;

/**
 *This class will test the behaviour of processors.
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
@RunWith(JUnit4TestRunner.class)
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
		osgi.dispose();
		cilia.dispose();
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
						mavenBundle().groupId("fr.liglab.adele.cilia").artifactId("cilia-helper").versionAsInProject(),
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
	 * Test the periodic-scheduler behavior
	 */
	@Test
	public void testPeriodicScheduler(){
		CiliaHelper.waitSomeTime(2000);
		Hashtable<String, String> properties = new Hashtable<String,String>();
		properties.put("period", "1000");
		properties.put("delay", "5000");
		MediatorTestHelper helper = cilia.getSchedulerHelper("periodic-scheduler", "fr.liglab.adele.cilia", properties);
		CiliaHelper.waitSomeTime(1000);//initial delay by default
		helper.injectData(new Data("Data one","data one"));
		helper.injectData(new Data("Data two","data two"));
		CiliaHelper.waitSomeTime(5000);
		Assert.assertEquals(2, helper.getAmountData());
		helper.getData();//to erase processed data.
		//We inject another set of data
		helper.injectData(new Data("Data three","data three"));
		helper.injectData(new Data("Data four","data four"));
		helper.injectData(new Data("Data five","data five"));
		//It must not retrieve anything, it must wait some time (periodically)
		Assert.assertEquals(0, helper.getAmountData());
		CiliaHelper.waitSomeTime(3000);//We wait some time
		//We must now have the three messages
		Assert.assertEquals(3, helper.getAmountData());
	}
}
