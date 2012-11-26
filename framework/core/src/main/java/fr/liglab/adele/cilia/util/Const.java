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

package fr.liglab.adele.cilia.util;


public class Const {


	public static final String CILIA_NAMESPACE = "fr.liglab.adele.cilia";

	public static final String PROPERTY_INSTANCE_TYPE = "cilia.component.type";
	public static final String PROPERTY_CHAIN_ID = "cilia.chain.name";
	public static final String PROPERTY_COMPONENT_ID = "cilia.component.id";
	public static final String PROPERTY_UUID ="cilia.component.uuid" ;
	
	public static final String PROPERTY_LOCK_UNLOCK = "cilia.command.lock.unlock" ;
	public static final String SET_LOCK ="lock" ;
	public static final String SET_UNLOCK = "unlock" ;
	
	/**
	 * The cilia.core is used to trace modification to models
	 */
	public final static String LOGGER_CORE = "cilia.core";
	/**
	 * The cilia.application is used to trace the behavior of mediation chains
	 */
	public final static String LOGGER_APPLICATION = "cilia.application";
	/**
	 * The cilia.runtime is used to trace component creatioin/execution
	 */
	public final static String LOGGER_RUNTIME = "cilia.runtime";

	
	/*
	 * Build a default Cilia Qualified Name String
	 */
	public static final String ciliaQualifiedName(String name) {
		StringBuffer sb = new StringBuffer().append(CILIA_NAMESPACE);
		sb.append(":").append(name);
		return sb.toString();
	}

}
