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

package fr.liglab.adele.cilia.framework.monitor.statevariable;

public interface ComponentStateVarProperties {

	public static final String MONITOR_CHAIN_ID = "chain.id";
	public static final String MONITOR_NODE_ID = "node.id";	
	public static final String MONITOR_UUID ="uuid" ;
	
	public static final String SERVICE_TRACKED = "(mediator.service=monitor)";	
	public static final String TOPIC_HEADER = "cilia/runtime/statevariable/";

}
