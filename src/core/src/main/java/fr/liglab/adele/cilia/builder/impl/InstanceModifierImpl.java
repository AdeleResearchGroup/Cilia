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
import fr.liglab.adele.cilia.builder.MediatorConfigurator;
import fr.liglab.adele.cilia.builder.InstanceModifier;
import fr.liglab.adele.cilia.builder.Modifier;

/**
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class InstanceModifierImpl extends MediatorConfiguratorImpl implements InstanceModifier, Modifier {
	String id;
	int type;
	public InstanceModifierImpl() {
	}
	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.InstanceModifier#id(java.lang.String)
	 */
	@Override
	public MediatorConfigurator id(String id) {
		this.id = id;
		return this;
	}
	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.InstanceModifier#adapter()
	 */
	@Override
	public InstanceModifier adapter() {
		type = Architecture.ADAPTER;
		return this;
	}
	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.InstanceModifier#mediator()
	 */
	@Override
	public InstanceModifier mediator() {
		type = Architecture.MEDIATOR;
		return this;
	}
	/**
	 * @return the id
	 */
	protected String getId() {
		return id;
	}
	/**
	 * @return the type
	 */
	protected int getType() {
		return type;
	}

}
