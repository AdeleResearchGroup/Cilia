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
package fr.liglab.adele.cilia.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Dictionary;

import org.apache.felix.ipojo.test.helpers.OSGiHelper;
import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.CiliaContext;
import fr.liglab.adele.cilia.administration.CiliaAdminService;
import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Builder;
import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
import fr.liglab.adele.cilia.framework.ICollector;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.runtime.CiliaInstance;
import fr.liglab.adele.cilia.runtime.CiliaInstanceWrapper;
import fr.liglab.adele.cilia.runtime.Const;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class CiliaHelper {


	private OSGiHelper ohelper;

	public CiliaHelper(BundleContext bc) {
		ohelper = new OSGiHelper(bc);
	}
	/**
	 * Get the mediator model.
	 * @param chain The chain where the mediator is.
	 * @param mm The mediator id.
	 * @return The mediator model object.
	 */
	public Mediator getMediatorModel(String chain, String mm) {
		CiliaContext context = getCiliaContext();
		try {
			return context.getApplicationRuntime().getChain(chain).getMediator(mm);
		} catch (CiliaIllegalParameterException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * @param chain
	 * @param am
	 * @return
	 */
	public Adapter getAdapterModel(String chain, String am) {
		CiliaContext context = getCiliaContext();
		try {
			return context.getApplicationRuntime().getChain(chain).getAdapter(am);
		} catch (CiliaIllegalParameterException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getMediatorState(String mm) {
		CiliaContext context = getCiliaContext();
		return 0;
	}

	public int getAdapterState(String mm) {
		return 0;
	}

	public boolean isBindingOk(String from, String to) {
		return false;
	}


	public MediatorTestHelper instrumentMediatorInstance(String chainId,
			String mediator, String inputports[], String exitports[]) {
		if ((exitports == null) || (exitports.length < 1)
				|| (inputports == null) || (inputports.length < 1)) {
			System.err.println("Ilegal port parameters");
			return null;
		}
		String id = chainId+"/"+mediator;
		CiliaContext ccontext = (CiliaContext) ohelper.getServiceObject(
				CiliaContext.class.getName(), null);
		Builder builder = ccontext.getBuilder();
		Architecture arch = null;
		try {
			arch = builder.get(chainId);
			arch.create().adapter().type("cilia-adapter-helper")
			.namespace(Const.CILIA_NAMESPACE).id(id).configure().key("identifier").value(id);
			for (int i = 0; i < inputports.length; i++) {
				arch.bind().from("helper:unique")
				.to(mediator + ":" + inputports[i]);
			}
			for (int i = 0; i < exitports.length; i++) {
				arch.bind().from(mediator + ":" + exitports[i])
				.to("helper:unique");
			}
			builder.done();
		} catch (CiliaException e) {
			e.printStackTrace();
			return null;
		}
		ohelper.waitForService(MediatorTestHelper.class.getName(), "(identifier="+id+")", 1000);
		MediatorTestHelper helper = (MediatorTestHelper)ohelper.getServiceObject(MediatorTestHelper.class.getName(), "(identifier="+id+")");
		return helper;
	}

	public CiliaInstance createInstance(String factory){
		return new CiliaInstanceWrapper(ohelper.getContext(), "0", "(factory.name="+factory+")", null, null);
	}

	public CiliaInstance createInstance(String factory, Dictionary props){
		return new CiliaInstanceWrapper(ohelper.getContext(), "0", "(factory.name="+factory+")", props, null);
	}

	public MediatorTestHelper instrumentChain(String chainId,
			String firstMediatorWithPort, String lastMediatorWithPort) {
		CiliaContext ccontext = (CiliaContext) ohelper.getServiceObject(
				CiliaContext.class.getName(), null);
		String id = chainId;
		Builder builder = ccontext.getBuilder();
		Architecture arch = null;
		try {
			arch = builder.get(chainId);
			arch.create().adapter().type("cilia-adapter-helper")
			.namespace(Const.CILIA_NAMESPACE).id("helper").configure().key("identifier").value(id);
			arch.bind().from("helper:unique").to(firstMediatorWithPort);
			arch.bind().from(lastMediatorWithPort).to("helper:unique");
			builder.done();
		} catch (CiliaException e) {
			e.printStackTrace();
			return null;
		}
		ohelper.waitForService(MediatorTestHelper.class.getName(), "(identifier="+id+")", 4000);
		MediatorTestHelper helper = (MediatorTestHelper)ohelper.getServiceObject(MediatorTestHelper.class.getName(), "(identifier="+id+")");
		return helper;
	}

	public void dispose() {
		ohelper.dispose();
	}

	public CiliaContext getCiliaContext() {
		ohelper.waitForService(CiliaContext.class.getName(), null, 1000);
		CiliaContext context = (CiliaContext)ohelper.getServiceObject(CiliaContext.class.getName(), null);
		return context;
	}

	public Chain getChain(String chain){
		CiliaContext cc = getCiliaContext();
		try {
			return cc.getApplicationRuntime().getChain(chain);
		} catch (CiliaIllegalParameterException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public CollectorHelper getCollectorHelper(ICollector ic){
		return new CollectorHelper(ic);
	}

	public void load(URL url){
		InputStream fis = null;
		try {
			fis = url.openStream();
		} catch (IOException e) {
			e.printStackTrace();

		}
		File file = null;
		try {
			
			file = File.createTempFile("test", ".dscilia");
			OutputStream out=new FileOutputStream(file);
			byte buf[]=new byte[1024];
			int len;
			while((len=fis.read(buf))>0)
				out.write(buf,0,len);
			out.close();
			fis.close();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("chain to be loaded...");
		ohelper.waitForService(CiliaAdminService.class.getName(), null, 1000);
		CiliaAdminService service = (CiliaAdminService)ohelper.getServiceObject(CiliaAdminService.class.getName(), null);
		service.loadChain(file.getAbsolutePath());
		System.out.println("chain loaded");
	}

	public void unload(String url){

	}

	public boolean waitToChain(String chainId, long time) {
		boolean found = false;
		long current = System.currentTimeMillis();
		long finalTime = current + time;
		while(!found && current <= finalTime) {
			waitSomeTime(500);
			Chain ch = null;
			try {
				ch = getCiliaContext().getApplicationRuntime().getChain(chainId);
			} catch (CiliaIllegalParameterException e) {
				e.printStackTrace();
			}
			if (ch != null){
				found = true;
			}
			current = System.currentTimeMillis();
		}
		return found;
	}
	
	public static void waitSomeTime(int l) {
		try {
			Thread.sleep(l);//wait to be registered
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
