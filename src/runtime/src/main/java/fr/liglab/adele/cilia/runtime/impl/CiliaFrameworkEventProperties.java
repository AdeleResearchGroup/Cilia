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

public interface CiliaFrameworkEventProperties {
	/* 
	 * a topic defined as follow : ROOT_TOPIC/chainID/mediatorID 
	 * chainID is optional 
	 * mediatorID is optional
	 */
	public String ROOT_TOPIC = "cilia/framework/events/";


	/* List of properties defining event published by the cilia framework  */
	
	/* chain Id */
	public String PROPERTY_CHAIN = "chain.name";
	/* mediator id ( optional if event fired by a level chain ) */
	public String PROPERTY_MEDIATOR = "mediator.name";
	/* Mediator properties updated */
	public String PROPERTY_MEDIATOR_PROPS = "mediator.properties";

	/* operations */
	public String EVENT_ID = "event.id";

}
