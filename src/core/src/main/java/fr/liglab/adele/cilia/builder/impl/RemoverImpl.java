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
package fr.liglab.adele.cilia.builder.impl;

import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.InstanceRemover;
import fr.liglab.adele.cilia.builder.Remover;

/**
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class RemoverImpl implements Remover, InstanceRemover {
	
	int type = 0;
	
	String id;
	
	Architecture architecture;
	
	protected RemoverImpl(Architecture arch) {
		architecture = arch;
	}
	
	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Remover#mediator()
	 */
	@Override
	public InstanceRemover mediator() {
		type = Architecture.MEDIATOR;
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Remover#adapter()
	 */
	@Override
	public InstanceRemover adapter() {
		type = Architecture.ADAPTER;
		return this;
	}


	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.RemoverMediator#id(java.lang.String)
	 */
	@Override
	public Architecture id(String id) {
		this.id = id;
		return architecture;
	}

	/**
	 * @return the type
	 */
	protected int getType() {
		return type;
	}

	/**
	 * @return the id
	 */
	protected String getId() {
		return id;
	}
}
