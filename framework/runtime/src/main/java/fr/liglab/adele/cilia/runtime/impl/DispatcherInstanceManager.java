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
package fr.liglab.adele.cilia.runtime.impl;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.exceptions.CiliaRuntimeException;
import fr.liglab.adele.cilia.framework.AbstractDispatcher;
import fr.liglab.adele.cilia.framework.IDispatcher;
import fr.liglab.adele.cilia.internals.factories.MediatorComponentManager;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.runtime.CiliaInstanceWrapper;

/**
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class DispatcherInstanceManager extends ConstituentInstanceManager {

	DispatcherHandler handler;
	/**
	 * @param context
	 * @param schedulerInfo
	 */
	public DispatcherInstanceManager(BundleContext context,DispatcherHandler sched,
			Component schedulerInfo, MediatorComponentManager manager) {
		super(context, schedulerInfo, manager);
		handler = sched;
		handler.setDispatcherManager(this);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.runtime.impl.ConstituentInstanceManager#createFilter()
	 */
	@Override
	protected String createFilter() {
		StringBuffer filter = new StringBuffer("(&(factory.state=1)");
		filter.append("(dispatcher.name=" + constituantInfo.getType() + ")");
		if (constituantInfo.getNamespace() != null) {
			filter.append("(dispatcher.namespace=" + constituantInfo.getNamespace()
					+ ")");
		}
		filter.append(")");
		return filter.toString();
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.runtime.impl.ConstituentInstanceManager#createConstituantFilter(fr.liglab.adele.cilia.model.Component)
	 */
	@Override
	protected String createConstituantFilter(Component component) {
		StringBuffer filter = new StringBuffer();
		filter.append("(&");
		filter.append("(");
		filter.append("sender.name=");
		filter.append(component.getType());
		filter.append(")");
		filter.append("(factory.state=1)");
		filter.append(")");
		return filter.toString();
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.runtime.impl.ConstituentInstanceManager#organizeReferences(fr.liglab.adele.cilia.runtime.CiliaInstanceWrapper)
	 */
	@Override
	protected void organizeReferences(CiliaInstanceWrapper instance) {
		AbstractDispatcher disp = (AbstractDispatcher )getDispatcher();//All dispatchers must hinerit for this class
		if (disp != null) {
			disp.setDispatcher(handler);
		}
		return;
	}

	public IDispatcher getDispatcher(){
		return (IDispatcher)this.constituant.getObject();
	}
	
	public CiliaInstanceWrapper addSender(String port, Component component, boolean start) {
		return super.addComponent(port, component, start);
	}
	
	public void removeSender(String port, Component component){
		try {
			mediatorInstance.waitToProcessing(5000);
		} catch (CiliaRuntimeException e) {
			e.printStackTrace();
		}
		
		removeComponent(port, component);//we can only remove a component where there is any processing.
		//Also, the mediator MUST be blocked when removing, so it must be impossible to trigger a processing while removing a sender.
	}
	
}
