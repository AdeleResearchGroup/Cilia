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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.MediatorConfigurator;
import fr.liglab.adele.cilia.builder.ConfiguratorReturner;
import fr.liglab.adele.cilia.builder.Creator;
import fr.liglab.adele.cilia.builder.InstanceCreator;
import fr.liglab.adele.cilia.builder.InstanceCreatorConfiguration;
import fr.liglab.adele.cilia.exceptions.BuilderConfigurationException;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public class CreatorImpl extends MediatorConfiguratorImpl implements Creator,
		InstanceCreator, InstanceCreatorConfiguration, ConfiguratorReturner {

	private String type = null;

	private String category = null;

	private String namespace = null;

	private String id = null;

	private int instanceType;

	private String version;

	ArchitectureImpl architecture;

	protected CreatorImpl(ArchitectureImpl arch) {
		architecture = arch;
	}

	/**
	 * @see fr.liglab.adele.cilia.builder.Creator#mediator()
	 */
	public InstanceCreator mediator() {
		instanceType = Architecture.MEDIATOR;
		return this;
	}

	/**
	 * @see fr.liglab.adele.cilia.builder.Creator#adapter()
	 */
	public InstanceCreator adapter() {
		instanceType = Architecture.ADAPTER;
		return this;
	}

	/**
	 * @see fr.liglab.adele.cilia.builder.InstanceCreator#type(java.lang.String)
	 */
	public InstanceCreatorConfiguration type(String type) {
		this.type = type;
		return this;
	}

	/**
	 * @see fr.liglab.adele.cilia.builder.InstanceCreatorConfiguration#namespace(java.lang.String)
	 */
	public InstanceCreatorConfiguration namespace(String nspace) {
		this.namespace = nspace;
		return this;
	}

	/**
	 * @see fr.liglab.adele.cilia.builder.InstanceCreatorConfiguration#category(java.lang.String)
	 */
	public InstanceCreatorConfiguration category(String cate) {
		this.category = cate;
		return this;
	}

	/**
	 * @see fr.liglab.adele.cilia.builder.InstanceCreatorConfiguration#version(java.lang.String)
	 */
	public InstanceCreatorConfiguration version(String ver) {
		this.version = ver;
		return this;
	}

	/**
	 * @see fr.liglab.adele.cilia.builder.InstanceCreatorConfiguration#id(java.lang.String)
	 */
	public ConfiguratorReturner id(String id)
			throws BuilderConfigurationException {
		if (alreadyInList(id)) {
			throw new BuilderConfigurationException(
					"There exist the same ID in the Builder Configuration");
		}
		this.id = id;
		return this;
	}

	/**
	 * @see fr.liglab.adele.cilia.builder.ConfiguratorReturner#configure()
	 */
	public MediatorConfigurator configure() {
		return this;
	}

	/**
	 * @return the instanceType
	 */
	protected int getInstanceType() {
		return instanceType;
	}

	/**
	 * @return the id
	 */
	protected String getId() {
		return id;
	}

	/**
	 * @return the namespace
	 */
	protected String getNamespace() {
		return namespace;
	}

	/**
	 * @return the type
	 */
	protected String getType() {
		return type;
	}


	/**
	 * @return the category
	 */
	protected String getCategory() {
		return category;
	}

	/**
	 * @return the version
	 */
	protected String getVersion() {
		return version;
	}

	private boolean alreadyInList(String id) {
		Iterator it = architecture.getCreated().iterator();
		while (it.hasNext()) {
			CreatorImpl toCreate = (CreatorImpl) it.next();
			String mid = toCreate.getId();
			if (id.equalsIgnoreCase(mid) && !toCreate.equals(this)) {
				return true;
			}
		}
		return false;
	}

}
