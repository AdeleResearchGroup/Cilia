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
//package fr.liglab.adele.cilia.runtime.impl;
//
//import java.util.Dictionary;
//
//import fr.liglab.adele.cilia.Binding;
//import fr.liglab.adele.cilia.Chain;
//import fr.liglab.adele.cilia.Mediator;
//import fr.liglab.adele.cilia.MediatorComponent;
//import fr.liglab.adele.cilia.Port;
//
//public class MediatorReadOnlyImpl implements Mediator {
//
//	final MediatorComponent mediator;
//	Chain chainReadOnly=null ;
//	
//	public MediatorReadOnlyImpl(MediatorComponent m) {
//		mediator = m;
//	}
//
//	public String getId() {
//		return mediator.getId();
//	}
//
//	public String getType() {
//		return mediator.getType();
//	}
//
//	public String getNamespace() {
//		return mediator.getNamespace();
//	}
//
//	public Chain getChain() {
//		/* the chain will never change for a mediator */
//		if (chainReadOnly==null) chainReadOnly= new ChainReadOnlyImpl(mediator.getChain());
//		return chainReadOnly ;
//	}
//
//	public Port getInPort(String name) {
//		return new PortReadOnlyImpl(mediator.getInPort(name));
//	}
//
//	public Port getOutPort(String name) {
//		return new PortReadOnlyImpl(mediator.getOutPort(name));
//	}
//
//	public String getCategory() {
//		return mediator.getCategory();
//	}
//
//	public Binding[] getInBindings() {
//		Binding[] in = mediator.getInBindings();
//		Binding[] ss = null;
//		if (in.length > 0) {
//			ss = new BindingReadOnly[in.length];
//			for (int i = 0; i < in.length; i++) {
//				ss[i] = new BindingReadOnlyImpl(in[i]);
//			}
//		}
//		return ss;
//	}
//
//	public Binding[] getOutBindings() {
//		Binding[] in = mediator.getOutBindings();
//		Binding[] ss = null;
//		if (in.length > 0) {
//			ss = new BindingReadOnly[in.length];
//			for (int i = 0; i < in.length; i++) {
//				ss[i] = new BindingReadOnlyImpl(in[i]);
//			}
//		}
//		return ss;
//	}
//
//	public String getQualifiedId() {
//		return mediator.getQualifiedId();
//	}
//
//	/* (non-Javadoc)
//	 * @see fr.liglab.adele.cilia.MediatorComponent#addOutBinding(fr.liglab.adele.cilia.Binding)
//	 */
//	public void addOutBinding(Binding binding) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see fr.liglab.adele.cilia.MediatorComponent#addInBinding(fr.liglab.adele.cilia.Binding)
//	 */
//	public void addInBinding(Binding bindingImpl) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see fr.liglab.adele.cilia.MediatorComponent#setChain(fr.liglab.adele.cilia.Chain)
//	 */
//	public void setChain(Chain chain) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see fr.liglab.adele.cilia.MediatorComponent#removeInBinding(fr.liglab.adele.cilia.Binding)
//	 */
//	public boolean removeInBinding(Binding binding) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see fr.liglab.adele.cilia.MediatorComponent#removeOutBinding(fr.liglab.adele.cilia.Binding)
//	 */
//	public boolean removeOutBinding(Binding binding) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see fr.liglab.adele.cilia.Component#setProperties(java.lang.String)
//	 */
//	public void setProperties(String propertiesAsString) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see fr.liglab.adele.cilia.Component#getProperties()
//	 */
//	public Dictionary getProperties() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	/* (non-Javadoc)
//	 * @see fr.liglab.adele.cilia.Component#getProperty(java.lang.Object)
//	 */
//	public Object getProperty(Object key) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	/* (non-Javadoc)
//	 * @see fr.liglab.adele.cilia.Component#setProperties(java.util.Dictionary)
//	 */
//	public void setProperties(Dictionary newProps) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see fr.liglab.adele.cilia.Component#setProperty(java.lang.Object, java.lang.Object)
//	 */
//	public void setProperty(Object key, Object value) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see fr.liglab.adele.cilia.Component#setNamespace(java.lang.String)
//	 */
//	public void setNamespace(String namespace) {
//		// TODO Auto-generated method stub
//		
//	}
//
//}
