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
 *//*

package cilia.runtime.context.test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.representation.Form;
import fr.liglab.adele.cilia.helper.CiliaHelper;
import fr.liglab.adele.cilia.model.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.ow2.chameleon.json.JSONService;
import org.ow2.chameleon.rose.api.Machine;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.ow2.chameleon.wisdom.test.WisdomRunner;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

*/
/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *//*

@RunWith(WisdomRunner.class)
public class RuntimeRemoteTest {
	@Inject
	private BundleContext context;

	private OSGiHelper osgi;

	private CiliaHelper cilia;

	private static String HTTP_PORT = "8080";

	private static String ROOT_SITE = "http://localhost:"+HTTP_PORT+"/cilia/"; 


	@Before
	public void setUp() {
		CiliaHelper.waitSomeTime(2000);
		osgi = new OSGiHelper(context);
		cilia = new CiliaHelper(context);
	}

	@After
	public void tearDown() {
		cilia.dispose();
		osgi.dispose();
	}




	*/
/*****************************************//*

	*/
/**          GET METHODS                **//*

	*/
/*****************************************//*


	*/
/**
 * Test GET method in the URL http://localhost:9874/cilia
 *//*

	@Test
	public void testGetAllChains(){
		String chainName = "remote";
		Map reponse = null;
		createRemoteService();

		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		reponse = doRequest(ROOT_SITE, Method.GET);
		List chains = (List)reponse.get("chains");
		Assert.assertEquals(1, chains.size());
	}
	*/
/**
 * Test GET method in the URL http://localhost:9874/cilia/remote
 *//*

	@Test
	public void testGetSimpleChain(){
		String chainName = "remote";
		Map reponse = null;
		createRemoteService();

		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		reponse = doRequest(ROOT_SITE + chainName, Method.GET);
		Assert.assertEquals(chainName,reponse.get("ID"));
		List mediators = (List)reponse.get("Mediators");
		Assert.assertEquals(2, mediators.size());
		Map adapters = (Map)reponse.get("Adapters");
		Assert.assertEquals(1, ((List)adapters.get("out-only")).size()); //There is only one out adapter
		Assert.assertEquals(0, ((List)adapters.get("in-only")).size()); //zero adapters
		Assert.assertEquals(0, ((List)adapters.get("in-out")).size()); //zero adapters
		Assert.assertEquals(1, ((List)reponse.get("Bindings")).size()); //one binding
	}
	*/
/**
 * Test GET method in the URL http://localhost:9874/cilia/remote/components
 *//*

	@Test
	public void testGETAllComponents(){
		String chainName = "remote";
		Map reponse = null;
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		//Test if chain is present
		Assert.assertNotNull(cilia.getChain(chainName));
		reponse = doRequest(ROOT_SITE + chainName + "/components", Method.GET);
		List mediators = (List)reponse.get("Mediators");
		List adapters = (List)reponse.get("Adapters");
		Assert.assertEquals(2, mediators.size());
		Assert.assertEquals(1, adapters.size());
	}

	*/
/**
 * Test GET method in the URL http://localhost:9874/cilia/remote/mediators
 *//*

	@Test
	public void testGETAllMediators(){
		String chainName = "remote";
		Map reponse = null;
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		//Test if chain is present
		Assert.assertNotNull(cilia.getChain(chainName));
		reponse = doRequest(ROOT_SITE + chainName + "/mediators", Method.GET);
		List mediators = (List)reponse.get("Mediators");
		Assert.assertEquals(2, mediators.size());//This chain has only two mediators
	}



	*/
/**
 * Test GET method in the URL http://localhost:9874/cilia/remote/adapters
 *//*

	@Test
	public void testGETAlladapters(){
		String chainName = "remote";
		Map reponse = null;
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		//Test if chain is present
		Assert.assertNotNull(cilia.getChain(chainName));
		reponse = doRequest(ROOT_SITE + chainName + "/adapters", Method.GET);
		List adapters = (List)reponse.get("Adapters");
		Assert.assertEquals(1, adapters.size());//This chain does not have any adapter
	}



	*/
/**
 * Test GET method in the URL http://localhost:9874/cilia/remote/bindings
 *//*

	@Test
	public void testGETAllBindings(){
		String chainName = "remote";
		Map reponse = null;
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		//Test if chain is present
		Assert.assertNotNull(cilia.getChain(chainName));
		reponse = doRequest(ROOT_SITE + chainName + "/bindings", Method.GET);
		List bindings = (List)reponse.get("Bindings");
		Assert.assertEquals(1, bindings.size());//This chain has one adapter
	}

	*/
/**
 * Test GET method in the URL http://localhost:9874/cilia/remote/mediators/validToto
 * 						and in http://localhost:9874/cilia/remote/components/validToto
 * to retrieve the validToto mediator in the chain remote
 *//*

	@Test
	public void testGETOneMediator(){
		String chainName = "remote";
		String mediatorName = "validToto";
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		//Test if chain is present
		Assert.assertNotNull(cilia.getChain(chainName));
		//Test on the resource http://localhost:9874/cilia/remote/components/validToto
		Map reponse1 = doRequest(ROOT_SITE + chainName + "/components/" + mediatorName, Method.GET);
		Assert.assertNotNull(reponse1.get("ID"));//it is available
		Assert.assertEquals(mediatorName, reponse1.get("ID"));//compare mediator id
		//Test on the resource http://localhost:9874/cilia/remote/mediators/validToto 
		Map reponse2 = doRequest(ROOT_SITE + chainName + "/mediators/" + mediatorName, Method.GET);
		Assert.assertNotNull(reponse2.get("ID"));//it is available
		Assert.assertEquals(mediatorName, reponse2.get("ID"));//compare mediator name
	}

	*/
/**
 * Test GET method in the URL http://localhost:9874/cilia/remote/adapters/adapter1
 * to retrieve the adapter1 adapter in the chain remote
 *//*

	@Test
	public void testGETOneAdapter(){
		String chainName = "remote";
		String adapterName = "adapter1";
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		//Test if chain is present
		Assert.assertNotNull(cilia.getChain(chainName));
		//Test on the resource http://localhost:9874/cilia/remote/components/adapter1
		Map reponse1 = doRequest(ROOT_SITE + chainName + "/components/" + adapterName, Method.GET);
		Assert.assertNotNull(reponse1.get("ID"));//it is available
		Assert.assertEquals(adapterName, reponse1.get("ID"));//compare mediator name
		//Test on the resource http://localhost:9874/cilia/remote/adapters/adapter1
		Map reponse2 = doRequest(ROOT_SITE + chainName + "/adapters/" + adapterName, Method.GET);
		Assert.assertNotNull(reponse2.get("ID"));//it is available
		Assert.assertEquals(adapterName, reponse2.get("ID"));//compare mediator name
	}

	*/
/*****************************************//*

	*/
/**          PUT METHODS                **//*

	*/
/*****************************************//*


	*/
/**
 * Test PUT method in the URL http://localhost:9874/cilia/remote/components/validToto
 * with properties in json format -d properties={"delay":"10"}
 *//*

	@Test
	public void testPUTProperties(){
		String chainName = "remote";
		String mediatorName = "validToto";
		createRemoteService();
		//Build parameters
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		Mediator myMediator = cilia.getChain(chainName).getMediator(mediatorName);
		//Property does not exist
		Assert.assertNull(myMediator.getProperty("property1"));
		Form parameters = new Form();
		parameters.add("properties", "{property1:value1, property2:value2}");
		doRequest(ROOT_SITE + chainName+"/components/" + mediatorName, Method.PUT, parameters);
		//Property now exist
		Assert.assertEquals("value1", myMediator.getProperty("property1"));
		Assert.assertEquals("value2", myMediator.getProperty("property2"));
		//Bad request
		try{
			doRequest(ROOT_SITE + chainName+"/components/" + mediatorName, Method.PUT, new Form());
			Assert.fail("Must send a bad request"); //properties are null
		}catch(Exception ex){}
		//Bad request
		try{
			Form f = new Form();
			f.add("properties", "{A}}");
			doRequest(ROOT_SITE + chainName+"/components/" + mediatorName, Method.PUT, f);
			Assert.fail("Must send a bad request"); //properties are null
		}catch(Exception ex){}
	}

	*/
/**
 * Test PUT method in the URL http://localhost:9874/cilia/remote/components
 * with parameters :
 * 	command = replace
 * 	from = toto
 * 	to = validToto
 * to delete a chain.
 *//*

	@Test
	public void testReplaceComponent(){
		String chainName = "remote";
		Map reponse = null;
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		//Test if chain is present
		Assert.assertNotNull(cilia.getChain(chainName));
		//Build parameters
		Form parameters = new Form();
		parameters.add("command", "replace");
		parameters.add("from", "toto");
		parameters.add("to", "validToto");
		doRequest(ROOT_SITE + chainName + "/components", Method.PUT, parameters);
	}

	*/
/**
 * Test PUT method in the URL http://localhost:9874/cilia/remote/components
 * with parameters :
 * 	command = copy
 * 	from = toto
 * 	to = toto2
 * to delete a chain.
 *//*

	@Test
	public void testCopyComponent(){
		String chainName = "remote";
		String oldMediator = "toto";
		String newMediator = "toto2";
		Map reponse = null;
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		//Test if chain is present
		Assert.assertNotNull(cilia.getChain(chainName));
		//Test that toto2 does not exists
		Assert.assertNull(cilia.getMediatorModel(chainName, newMediator));
		//Build parameters
		Form parameters = new Form();
		parameters.add("command", "copy");
		parameters.add("from", oldMediator);
		parameters.add("to", newMediator);
		//send REST request
		doRequest(ROOT_SITE + chainName + "/components", Method.PUT, parameters);
		//Now test that the mediator toto2 exists
		Assert.assertNotNull(cilia.getMediatorModel(chainName, newMediator));
	}



	*/
/*****************************************//*

	*/
/**          POST METHODS               **//*

	*/
/*****************************************//*




	*/
/**
 * Test POST method in the URL http://localhost:9874/cilia/MyNewChain
 *//*

	@Test
	public void testCreationChain(){
		String chainName = "MyNewChain";
		createRemoteService();
		doRequest(ROOT_SITE + chainName, Method.POST);
		Chain myNewChain = cilia.getChain(chainName);
		Assert.assertNotNull(myNewChain);
	}
	*/
/**
 * Test POST method in the URL http://localhost:9874/cilia/remote/mediators/myMediator1 -d "type=Mock&properties={prop1:val1}"
 *//*

	@Test
	public void testCreationMediator(){
		String chainName = "remote";
		String mediatorName = "myMediator1";
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		Assert.assertNull(cilia.getChain(chainName).getMediator(mediatorName)); // It does not exist
		Form parameters = new Form();
		parameters.add("type", "Mock");
		parameters.add("properties", "{prop1:val1}");
		doRequest(ROOT_SITE + chainName + "/mediators/" + mediatorName, Method.POST, parameters);
		Mediator mymediator = cilia.getChain(chainName).getMediator(mediatorName);
		Assert.assertNotNull(mymediator);//Now it exist
		Assert.assertEquals(mymediator.getProperty("prop1"), "val1");
	}

	*/
/**
 * Test POST method in the URL http://localhost:9874/cilia/remote/adapters/myAdapter -d "type=console-adapter&properties={prop1:val1}"
 *//*

	@Test
	public void testCreationAdapter(){
		String chainName = "remote";
		String adapter = "myAdater";
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		Assert.assertNull(cilia.getChain(chainName).getMediator(adapter)); // It does not exist
		Form parameters = new Form();
		parameters.add("type", "console-adapter");
		parameters.add("properties", "{prop1:val1}");
		doRequest(ROOT_SITE + chainName + "/adapters/" + adapter, Method.POST, parameters);
		Adapter mymediator = cilia.getChain(chainName).getAdapter(adapter);
		Assert.assertNotNull(mymediator);//Now it exist
		Assert.assertEquals(mymediator.getProperty("prop1"), "val1");
		Assert.assertEquals(mymediator.getType(), "console-adapter");
	}

	*/
/**
 * Test POST method in the URL http://localhost:9874/cilia/remote/bindings/ -d "from=toto:unique&to=adapter1:unique"
 *//*

	@Test
	public void testCreationBinding(){
		String chainName = "remote";
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		MediatorComponent source  = cilia.getMediatorModel(chainName, "toto");
		MediatorComponent target = cilia.getAdapterModel(chainName, "adapter1");
		Binding bs[] = cilia.getChain(chainName).getBindings(source,target);
		Assert.assertEquals(bs.length, 0); // It does not exist any bindings
		Form parameters = new Form();
		parameters.add("from", "toto:unique");
		parameters.add("to", "adapter1:unique");
		doRequest(ROOT_SITE + chainName + "/bindings/", Method.POST, parameters);
		System.out.println("Binding request sent");
		bs = cilia.getChain(chainName).
				getBindings(source,target);
		Assert.assertEquals(1, bs.length); // Now the binding exists
	}


	*/
/*****************************************//*

	*/
/**          DELETE METHODS             **//*

	*/
/*****************************************//*



	*/
/**
 * Test DELETE method in the URL http://localhost:9874/cilia/remote
 * to delete a chain.
 *//*

	@Test
	public void testRemoveChain(){
		String chainName = "remote";
		Map reponse = null;
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		//Test if chain is present
		Assert.assertNotNull(cilia.getChain(chainName));
		//Delete the chain
		doRequest(ROOT_SITE + chainName, Method.DELETE);
		//Test there is any chain present
		Assert.assertNull(cilia.getChain(chainName));
	}

	*/
/**
 * Test DELETE method in the URL http://localhost:9874/cilia/remote/mediators/toto
 * to delete a mediator with ID toto.
 *//*

	@Test
	public void testRemoveMediator(){
		String chainName = "remote";
		String mediatorName = "toto";
		Map reponse = null;
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		//Test if mediator is present
		Assert.assertNotNull(cilia.getChain(chainName).getMediator(mediatorName));
		//Delete the mediator
		doRequest(ROOT_SITE + chainName+"/components/" + mediatorName, Method.DELETE);
		//Test there is not present
		Assert.assertNull(cilia.getChain(chainName).getMediator(mediatorName));

		//It will be added to remove it using mediators url
		Form parameters = new Form();
		parameters.add("type", "Mock");
		doRequest(ROOT_SITE + chainName + "/mediators/" + mediatorName, Method.POST, parameters);
		//It is available
		Assert.assertNotNull(cilia.getChain(chainName).getMediator(mediatorName));

		//Delete the mediator again
		doRequest(ROOT_SITE + chainName+"/mediators/" + mediatorName, Method.DELETE);
		//Test there is not present
		Assert.assertNull(cilia.getChain(chainName).getMediator(mediatorName));
		//test not found when removing
		try{
			doRequest(ROOT_SITE + chainName+"/mediators/inexistent" , Method.DELETE);
			Assert.fail("Trying to remove inexistant component");
		}catch(Exception ex){}
	}

	*/
/**
 * Test DELETE method in the URL http://localhost:9874/cilia/remote/adapters/toto
 * to delete a mediator with ID toto.
 *//*

	@Test
	public void testRemoveAdapter(){
		String chainName = "remote";
		String adapterName = "adapter1";
		Map reponse = null;
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		//Test if mediator is present
		Assert.assertNotNull(cilia.getChain(chainName).getAdapter(adapterName));
		//Delete the mediator
		doRequest(ROOT_SITE + chainName+"/components/" + adapterName, Method.DELETE);
		//Test there is not present
		Assert.assertNull(cilia.getChain(chainName).getAdapter(adapterName));

		//It will be added to remove it using mediators url
		Form parameters = new Form();
		parameters.add("type", "console-adapter");
		doRequest(ROOT_SITE + chainName + "/adapters/" + adapterName, Method.POST, parameters);
		//It is available
		Assert.assertNotNull(cilia.getChain(chainName).getAdapter(adapterName));

		//Delete the mediator again
		doRequest(ROOT_SITE + chainName+"/adapters/" + adapterName, Method.DELETE);
		//Test there is not present
		Assert.assertNull(cilia.getChain(chainName).getAdapter(adapterName));

		//test not found when removing
		try{
			doRequest(ROOT_SITE + chainName+"/adapters/inexistent" , Method.DELETE);
			Assert.fail("Trying to remove inexistant component");
		}catch(Exception ex){}

	}

	*/
/**
 * Test DELETE method in the URL http://localhost:9874/cilia/remote/bindings/ -d "validToto:unique&to=toto:unique"
 *//*

	@Test
	public void testRemoveBinding(){
		String chainName = "remote";
		createRemoteService();
		URL url = context.getBundle().getResource("remoteTest.dscilia");
		cilia.load(url);
		cilia.waitToChain(chainName, 3000);
		MediatorComponent source  = cilia.getMediatorModel(chainName, "validToto");
		MediatorComponent target = cilia.getMediatorModel(chainName, "toto");
		Binding bs[] = cilia.getChain(chainName).getBindings(source,target);
		Assert.assertEquals(bs.length, 1); // There is one binding
		
		String from= "validToto:unique";
		String to = "toto:unique";
		StringBuilder furl = new StringBuilder(ROOT_SITE);
		furl.append(chainName).append("/bindings/").append("?from=").append(from).append("&to=").append(to);
		doRequest(furl.toString(), Method.DELETE);
		System.out.println("Binding request sent");
		bs = cilia.getChain(chainName).
				getBindings(source,target);
		Assert.assertEquals(0, bs.length); // Now the binding does not exists
	}


	public Map doRequest(String URL, Method method){
		Client c = Client.create();
		Map reponse = null;
		WebResource r = c.resource(URL);
		r.accept(MediaType.APPLICATION_JSON);
		String result = null;
		switch(method) {
		case GET: result = r.get(String.class); break;
		case PUT: result = r.put(String.class); break;
		case DELETE: result = r.delete(String.class); break;
		case POST: result = r.post(String.class); break;
		}
		try {
			System.out.println("Result:" + result);
			reponse = json().fromJSON(result);
		} catch (ParseException e) {

			Assert.fail(e.getMessage());
		}
		return reponse;
	}

	public Map doRequest(String URL, Method method, Form properties){
		ClientConfig clientConfig = new DefaultClientConfig(); 
		Client c = Client.create(clientConfig); 
		//AsyncWebResource ar = c.asyncResource(URL); 
		Map reponse = null;
		WebResource r = c.resource(URL);
		//r.accept(MediaType.APPLICATION_JSON);
		String result = null;
		Future<String>  fresult;
		switch(method) {
		case PUT: result = r.put(String.class, properties); break;
		case POST: result = r.post(String.class, properties); break;
		}

		System.out.println("Result " + result);
		if (result != null){
			try {
				reponse = json().fromJSON(result);
			} catch (ParseException e) {
				Assert.fail(e.getMessage());
			}
		}
		System.out.println("Response " + reponse);
		return reponse;
	}
	

	
	public JSONService json(){
		return (JSONService) osgi.getServiceObject(JSONService.class.getName(), null);
	}

	public void createRemoteService(){
		try {
			Machine rose = Machine.MachineBuilder.machine(context, "test").create();
			rose.exporter("RoSe_exporter.jersey").withProperty("jersey.servlet.name", "/cilia").create();
			rose.out("(|(&(objectClass=fr.liglab.adele.cilia.remote.impl.AdminChainREST)(instance.name=remote-admin-chain-0))(&(objectClass=fr.liglab.adele.cilia.remote.impl.MonitorREST)(instance.name=remote-monitor-chain-0)))").add();
			rose.start();
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public enum Method  {
		GET,POST, DELETE, PUT
	}

}
*/
