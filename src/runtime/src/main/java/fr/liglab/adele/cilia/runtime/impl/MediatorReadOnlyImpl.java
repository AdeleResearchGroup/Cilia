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

import fr.liglab.adele.cilia.BindingReadOnly;
import fr.liglab.adele.cilia.ChainReadOnly;
import fr.liglab.adele.cilia.MediatorReadOnly;
import fr.liglab.adele.cilia.PortReadOnly;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.MediatorComponent;

public class MediatorReadOnlyImpl implements MediatorReadOnly {

	final MediatorComponent mediator;
	ChainReadOnly chainReadOnly=null ;
	
	public MediatorReadOnlyImpl(MediatorComponent m) {
		mediator = m;
	}

	public String getId() {
		return mediator.getId();
	}

	public String getType() {
		return mediator.getType();
	}

	public String getNamespace() {
		return mediator.getNamespace();
	}

	public ChainReadOnly getChain() {
		/* the chain will never change for a mediator */
		if (chainReadOnly==null) chainReadOnly= new ChainReadOnlyImpl(mediator.getChain());
		return chainReadOnly ;
	}

	public PortReadOnly getInPort(String name) {
		return new PortReadOnlyImpl(mediator.getInPort(name));
	}

	public PortReadOnly getOutPort(String name) {
		return new PortReadOnlyImpl(mediator.getOutPort(name));
	}

	public String getCategory() {
		return mediator.getCategory();
	}

	public BindingReadOnly[] getInBindings() {
		Binding[] in = mediator.getInBindings();
		BindingReadOnly[] ss = null;
		if (in.length > 0) {
			ss = new BindingReadOnly[in.length];
			for (int i = 0; i < in.length; i++) {
				ss[i] = new BindingReadOnlyImpl(in[i]);
			}
		}
		return ss;
	}

	public BindingReadOnly[] getOutBindings() {
		Binding[] in = mediator.getOutBindings();
		BindingReadOnly[] ss = null;
		if (in.length > 0) {
			ss = new BindingReadOnly[in.length];
			for (int i = 0; i < in.length; i++) {
				ss[i] = new BindingReadOnlyImpl(in[i]);
			}
		}
		return ss;
	}

	public String getQualifiedId() {
		return mediator.getQualifiedId();
	}

}
