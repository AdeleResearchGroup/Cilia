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

import fr.liglab.adele.cilia.AdapterReadOnly;
import fr.liglab.adele.cilia.BindingReadOnly;
import fr.liglab.adele.cilia.PortReadOnly;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.PatternType;

public class AdapterReadOnlyImpl implements AdapterReadOnly {

	final Adapter adapter;

	public AdapterReadOnlyImpl(Adapter adapter) {
		this.adapter = adapter;
	}

	public String getId() {
		return adapter.getId();
	}

	public String getType() {
		return adapter.getType();
	}

	public String getNamespace() {
		return adapter.getNamespace();
	}

	public String getCategory() {
		return adapter.getCategory();
	}

	public BindingReadOnly[] getInBindings() {
		Binding[] b = adapter.getInBindings();
		BindingReadOnly[] bRO = null;
		if (b.length > 0) {
			bRO = new BindingReadOnly[b.length];
			for (int i = 0; i < b.length; i++) {
				bRO[i] = new BindingReadOnlyImpl(b[i]);
			}
		}
		return bRO;
	}

	public BindingReadOnly[] getOutBindings() {
		Binding[] b = adapter.getOutBindings();
		BindingReadOnly[] bRO = null;
		if (b.length > 0) {
			bRO = new BindingReadOnly[b.length];
			for (int i = 0; i < b.length; i++) {
				bRO[i] = new BindingReadOnlyImpl(b[i]);
			}
		}
		return bRO;
	}


	public PortReadOnly getInPort(String name) {
		return new PortReadOnlyImpl(adapter.getInPort(name));
	}

	public PortReadOnly getOutPort(String name) {
		return new PortReadOnlyImpl(adapter.getOutPort(name));
	}

	public PatternType getPattern() {
		return adapter.getPattern();
	}

	public String getQualifiedId() {
		return adapter.getQualifiedId();
	}

}
