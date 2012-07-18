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

import java.util.Observable;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.MediatorComponent;
import fr.liglab.adele.cilia.runtime.CiliaInstance;
import fr.liglab.adele.cilia.runtime.CiliaInstanceWrapper;
import fr.liglab.adele.cilia.runtime.ISchedulerHandler;

/**
 *
 * Handle the scheduler relation between collectors and their life cycle.
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public abstract class ConstituentInstanceManager extends CiliaInstanceManagerSet {

	CiliaInstanceWrapper constituant;

	Component constituantInfo;

	protected volatile int elementCount = 0;

	protected volatile int elementValids = 0;

	protected volatile boolean validConstituant = true;
	


	BundleContext bcontext;

	public ConstituentInstanceManager(BundleContext context) {
		bcontext = context;
	}

	public void start (){
		constituant = new CiliaInstanceWrapper(bcontext, constituantInfo.getId(), createFilter(), constituantInfo.getProperties(), this);
		constituant.start();
	}


	protected abstract String createFilter() ;

	public void addComponent(String port, Component component) {
		CiliaInstanceWrapper collectorInstance = new CiliaInstanceWrapper(bcontext, component.getId(), createFilter(), component.getProperties(), this);
		collectorInstance.start();
		synchronized (lockObject) {
			elementCount++;
			super.addInstance(port, collectorInstance);
		}
	}

	public boolean removeComponent(String port, Component component){
		synchronized (lockObject) {
			return super.removeInstance(port, component.getId());
		}
	}

	public void update(Observable o, Object arg) {
		CiliaInstanceWrapper instance = (CiliaInstanceWrapper)o;
		Integer state = (Integer)arg;
		synchronized (lockObject) {
			if (instance.equals(constituant)) { // Its the scheduler
				if (state.equals(CiliaInstance.VALID)) {
					validConstituant = true;
				} else {
					validConstituant = false;
				}
			} else { // It is some of the collectors
				if (state == CiliaInstance.VALID){
					elementValids ++;
				} else {
					elementValids --;
				}
			}
		}
	}
	public int getState(){
		synchronized (lockObject) {
			if((elementCount == elementValids) && validConstituant) {
				return MediatorComponent.VALID;
			} else if (validConstituant && elementCount == elementValids) {
				return MediatorComponent.SEMIVALID;
			} else if (!validConstituant) {
				return MediatorComponent.INVALID;
			}
			return MediatorComponent.STOPPED;
		}
	}
}
