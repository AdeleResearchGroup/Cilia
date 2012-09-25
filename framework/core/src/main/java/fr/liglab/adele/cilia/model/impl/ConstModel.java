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

package fr.liglab.adele.cilia.model.impl;

public interface ConstModel {

	public static final String INSTANCE_TYPE_COLLECTOR = "collector";
	public static final String INSTANCE_TYPE_SENDER = "sender";
	public static final String INSTANCE_TYPE_SCHEDULER = "scheduler";
	public static final String INSTANCE_TYPE_DISPATCHER = "dispatcher";
	public static final String INSTANCE_TYPE_PROCESSOR = "processor";
	public static final String CILIA_NAMESPACE = "fr.liglab.adele.cilia";

	public static final String PROPERTY_INSTANCE_TYPE = "cilia.component.type";
	public static final String PROPERTY_CHAIN_ID = "cilia.chain.name";
	public static final String PROPERTY_COMPONENT_ID = "cilia.component.id";
	public static final String PROPERTY_UUID ="cilia.component.uuid" ;
	
	public static final String PROPERTY_LOCK_UNLOCK = "cilia.command.lock.unlock" ;
	public static final String SET_LOCK ="lock" ;
	public static final String SET_UNLOCK = "unlock" ;
	

}
