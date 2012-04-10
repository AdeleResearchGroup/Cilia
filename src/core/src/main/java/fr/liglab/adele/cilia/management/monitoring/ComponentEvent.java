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

public interface ComponentEvent {
	/* Event number */
	static final int STATEVAR_CONDITION_UPDATED = 1 << 0;
	static final int STATEVAR_ADDED = 1 << 1;
	static final int STATEVAR_REMOVED = 1 << 2;
	static final int STATEVAR_DISABLED = 1 << 3;
	static final int STATEVAR_ENABLED = 1 << 4;
	static final int STATEVAR_MESURE_ADDED  = 1<<5 ;
	static final int STATEVAR_MESURE_CLEARED  = 1<<6 ;
	static final int STATEVAR_WINDOW_UPDATED = 1<<7 ;
	static final int STATEVAR_LOW = 1<<8 ;
	static final int STATEVAR_VERY_LOW = 1<<9 ;
	static final int STATEVAR_HIGH = 1<<10 ;
	static final int STATEVAR_VERY_HIGH = 1<<11 ;	
	
	/* Event string */
	static final String STR_STATEVAR_CONDITION_UPDATED = "state.var.condition.updated";
	static final String STR_STATEVAR_ADDED = "state.var.added";
	static final String STR_STATEVAR_REMOVED = "state.var.disabled";
	static final String STR_STATEVAR_ENABLED = "state.var.enabled";
	static final String STR_STATEVAR_DISABLED = "state.var.disabled";
	static final String STR_STATEVAR_MESURE_ADDED = "state.var.mesure.added";
	static final String STR_STATEVAR_MESURE_CLEARED = "state.var.mesure.cleared";
	static final String STR_STATEVAR_WINDOW_UPDATED = "state.var.window.updated";
	static final String STR_STATEVAR_LOW = "state.var.low" ;
	static final String STR_STATEVAR_VERY_LOW = "state.var.very.low" ;
	static final String STR_STATEVAR_HIGH = "state.var.high" ;
	static final String STR_STATEVAR_VERY_HIGH ="state.var.very.high" ;
		
}