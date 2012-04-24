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

import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.MediatorConfigurator;
import fr.liglab.adele.cilia.builder.ConfiguratorReturner;
import fr.liglab.adele.cilia.builder.Creator;
import fr.liglab.adele.cilia.builder.InstanceCreator;
import fr.liglab.adele.cilia.builder.InstanceCreatorConfiguration;

/**
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class CreatorImpl extends MediatorConfiguratorImpl implements Creator, InstanceCreator, InstanceCreatorConfiguration, ConfiguratorReturner {
	
	private String type = null;
	
	private String category = null;
	
	private String namespace = null;
	
	private String id = null;
	
	private int instanceType ;

	private String version;
	

	/**
	 * @see fr.liglab.adele.cilia.builder.Creator#mediator()
	 */
	@Override
	public InstanceCreator mediator() {
		setInstanceType(Architecture.MEDIATOR);
		return this;
	}

	/** 
	 * @see fr.liglab.adele.cilia.builder.Creator#adapter()
	 */
	@Override
	public InstanceCreator adapter() {
		setInstanceType(Architecture.ADAPTER);
		return this;
	}

	/**
	 * @see fr.liglab.adele.cilia.builder.InstanceCreator#type(java.lang.String)
	 */
	@Override
	public InstanceCreatorConfiguration type(String type) {
		this.setType(type);
		return this;
	}
	/** 
	 * @see fr.liglab.adele.cilia.builder.InstanceCreatorConfiguration#namespace(java.lang.String)
	 */
	@Override
	public InstanceCreatorConfiguration namespace(String nspace) {
		this.setNamespace(nspace);
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
	@Override
	public ConfiguratorReturner id(String id) {
		this.setId(id);
		return this;
	}

	
	/** 
	 * @see fr.liglab.adele.cilia.builder.ConfiguratorReturner#configure()
	 */
	@Override
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
	 * @param instanceType the instanceType to set
	 */
	protected void setInstanceType(int instanceType) {
		this.instanceType = instanceType;
	}

	/**
	 * @return the id
	 */
	protected String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	protected void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the namespace
	 */
	protected String getNamespace() {
		return namespace;
	}

	/**
	 * @param namespace the namespace to set
	 */
	protected void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * @return the type
	 */
	protected String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	protected void setType(String type) {
		this.type = type;
	}

	/**
	 * @return
	 */
	protected Hashtable getProperties() {
		Hashtable prps = new Hashtable();
		prps.putAll(sharedprops);
		prps.put("scheduler.properties", schedulerprops);
		prps.put("processor.properties", processorprops);
		prps.put("dispatcher.properties", dispatcherprops);
		return prps;
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

}
