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

import fr.liglab.adele.cilia.management.Configurable;

/**
 * Some properties configurable for the ChangeSet class
 * 
 * @author denismorand
 * 
 */
public interface ChangeSetProperties extends Configurable {
	/* Capacity max of events stored String format */
	static final String DEFAULT_SIZE = "500";
	/*
	 * Default value is 'DEFAULT_SIZE' items stored Value = must be string
	 * integer example : setPropery(PROPERTY_NUMBER_EVENT_STORED,"20")
	 */
	String PROPERTY_NUMBER_EVENT_STORED = "window.size";

	/*
	 * run / stop  the ChangeSet service
	 * setProperty(PROPERTY_ENABLE,ENABLED) or
	 * setProperty(PROPERTY_ENABLE,DISABLED)
	 */
	static final String PROPERTY_ENABLE = "state.enabled";
	static final String ENABLED = "true";
	static final String DISABLED = "false";

}
