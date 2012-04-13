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
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//
//import fr.liglab.adele.cilia.Adapter;
//import fr.liglab.adele.cilia.Binding;
//import fr.liglab.adele.cilia.Chain;
//import fr.liglab.adele.cilia.Mediator;
//
//public class ChainReadOnlyImpl implements Chain {
//
//	final Chain chain;
//
//	public ChainReadOnlyImpl(Chain chain) {
//		this.chain = chain;
//	}
//
//	public Mediator getMediator(String mediatorId) {
//		Mediator m = chain.getMediator(mediatorId);
//		return new MediatorReadOnlyImpl(m);
//	}
//
//	public Set getMediators() {
//
//		Set setReadOnly = null;
//		Set set = chain.getMediators();
//		if (set != null) {
//			setReadOnly = new HashSet(set.size());
//			Iterator it = set.iterator();
//			while (it.hasNext()) {
//				Mediator m = (Mediator) it.next();
//				setReadOnly.add(getMediator(m.getId()));
//			}
//		}
//		return setReadOnly;
//	}
//
//	public Adapter getAdapter(String adapterId) {
//		return new AdapterReadOnlyImpl(chain.getAdapter(adapterId));
//	}
//
//	public Set getAdapters() {
//		Set setReadOnly = null;
//		Set set = chain.getAdapters();
//		if (set != null) {
//			setReadOnly = new HashSet(set.size());
//			Iterator it = set.iterator();
//			while (it.hasNext()) {
//				Adapter a = (Adapter) it.next();
//				setReadOnly.add(getAdapter(a.getId()));
//			}
//		}
//		return setReadOnly;
//	}
//
//	public Set getBindings() {
//		Set set = chain.getBindings();
//		Set setReadOnly = new HashSet(set.size());
//		Iterator it = set.iterator();
//
//		while (it.hasNext()) {
//			setReadOnly.add(new BindingReadOnlyImpl((Binding) it.next()));
//		}
//		return setReadOnly;
//	}
//
//	public Binding[] getBindings(Mediator source, Mediator target) {
//		List result = new ArrayList();
//		List bindings = new ArrayList();
//		Binding[] sourceB = null;
//		Binding[] targetB = null;
//		Binding[] allBindings = null;
//		if (source != null) {
//			sourceB = source.getOutBindings();
//		}
//		if (target != null) {
//			targetB = target.getInBindings();
//		}
//
//		if (sourceB != null && targetB != null) {
//			bindings = Arrays.asList(sourceB);
//			bindings.addAll(Arrays.asList(targetB));
//			if (bindings != null) {
//				Iterator it = bindings.iterator();
//				while (it.hasNext()) {
//					Binding c = (Binding) it.next();
//					if ((c.getSourceMediator().getId().compareTo(source.getId()) == 0)
//							&& (c.getTargetMediator().getId().compareTo(target.getId()) == 0)) {
//						result.add(c);
//					}
//				}
//			}
//			allBindings = (Binding[]) result.toArray(new BindingReadOnly[result
//					.size()]);
//		}
//		return allBindings;
//	}
//
//	public String getId() {
//		return chain.getId();
//	}
//
//	public String getType() {
//		return chain.getType();
//	}
//
//	public String getNamespace() {
//		return chain.getNamespace();
//	}
//
//}
