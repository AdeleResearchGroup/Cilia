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


/**
 * ChangeSet , logger contextual change and global change <br>
 * Interface States , gives the state <br>
 * 
 */
public interface MonitoredChain extends ChangeSet {

	/* Return the chain ID */
	String getId();

	/* List of component ID providing Component informations at runtime */
	String[] getMonitoredComponentId();

	/* Runtime informations for the given component Id */
	MonitoredComponent getMonitoredComponent(String componentId);

}
