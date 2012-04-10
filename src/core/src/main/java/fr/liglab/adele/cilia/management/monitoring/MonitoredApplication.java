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

package fr.liglab.adele.cilia.management.monitoring;

import fr.liglab.adele.cilia.CiliaContextReadOnly;
import fr.liglab.adele.cilia.event.ChangeStateListener;
import fr.liglab.adele.cilia.event.CiliaFrameworkListener;

/**
 * Monitored application <br>
 * RuntimePerformance, gives runtime time data level framework <br>
 * ChangeSet , logger contextual change and global change <br>
 * ChangeSetProperties , some customization for the logger the poller (state
 * variable level framework requires a poller) <br>
 * 
 * @author denismorand
 * 
 */
public interface MonitoredApplication extends RuntimePerformance, ChangeSet,
		ChangeSetProperties, PollerProperties {

	/* List of chainId , providing runtime information */
	String[] getMonitoredChainId();

	MonitoredChain getMonitoredChain(String chainId);

	/* List of Monitored Component id per chain */
	String[] getMonitoredComponentId(String chain);

	/* Monitored component */
	MonitoredComponent getMonitoredComponent(String chainId, String componentId);

	/* Current Topology */
	CiliaContextReadOnly getCiliaContextRO();

	/* Events listener on topology update */
	CiliaFrameworkListener getFrameworkListener();
}
