///*
// * Copyright Adele Team LIG
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package fr.liglab.adele.cilia.runtime;
//
//import java.util.Dictionary;
//import java.util.Properties;
//import java.util.Set;
//
//import org.osgi.framework.BundleContext;
//
///**
// * CiliaAdmin: the Control interface of all the mediators within the Gateway
// * 
// * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
// *         Team</a>
// */
//public interface CiliaAdmin {
//
//	public static final String CILIA_VERSION = "0.9.0";
//
//	public BundleContext getBundleContext();
//			
//	/**
//	 * Add a new mediator instance
//	 * @param name
//	 * @param factory
//	 * @param properties
//	 * @deprecated
//	 */
//	//public MediatorContext addMediator(String name, String factory, Dictionary config);
//	
//	/* FACTORIES */	
//	public String[] getAllMediatorsFactories();
//	public Dictionary getMediatorFactoryProperties(String factory);
//	public Dictionary getCiliaComponentProperties(String factory);
//	
//
//	/*Instances*/
//	public int countInstances();
//	
//	public Set getInstancesKeySet();
//	
//	public String getMediatorinstanceId(String key);
//
//	public String getMediatorStateAsString(String key);
//
//	public String getSymbolicName(String key);
//	
//	public Properties getMediatorInstanceProperties(String key);
//	
//	/*Creation*/
//	long newMediatorInstance(String name, String factoryName, Dictionary properties);
//	
//	
//	/**
//	 * 
//	 * @return true if success, false if Error 
//	 */
//	public boolean updateInstance(String instanceName, Properties properties);
//	
//	public String getCiliaVersion();
//
//
//}
