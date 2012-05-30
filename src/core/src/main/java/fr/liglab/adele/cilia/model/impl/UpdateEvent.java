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

import fr.liglab.adele.cilia.model.Component;


public class UpdateEvent {

	
	private int updateAction;
	private Component source;
	
	public UpdateEvent(int updateAction, Component source) {
		this.updateAction = updateAction;
		this.source = source;
	}
	
	public int getUpdateAction() {
		return this.updateAction;
	}
	
	public Component getSource() {
		return this.source;
	}
}
