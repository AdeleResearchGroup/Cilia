/* Copyright Adele Team LIG
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

package fr.liglab.adele.cilia.runtime.knowledge;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Mediator;

/**
 * Build a Weak Reference proxy
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MediatorModelProxy {

	private static MediatorModelProxy instance = null;

	public static MediatorModelProxy getInstance() {
		if (null == instance) {
			instance = new MediatorModelProxy();
		}
		return instance;
	}

	private MediatorModelProxy() {
	}

	public Object make(Object object) {
		Object proxy;
		if (object instanceof Adapter)
			proxy = makeAdapter(object);
		else
			proxy = makeMediator(object);
		return proxy;
	}

	private Object makeAdapter(Object object) {
		Handler handler = new Handler(object);
		Object proxy = Proxy.newProxyInstance(Adapter.class.getClassLoader(),
				new Class[] { Adapter.class }, handler);
		return Adapter.class.cast(proxy);
	}

	private Object makeMediator(Object object) {
		Handler handler = new Handler(object);
		Object proxy = Proxy.newProxyInstance(Mediator.class.getClassLoader(),
				new Class[] { Mediator.class }, handler);
		return Mediator.class.cast(proxy);
	}

	public Object makeNode(Object object) {
		Handler handler = new Handler(object);
		Object proxy = Proxy.newProxyInstance(Node.class.getClassLoader(),
				new Class[] { Node.class }, handler);
		return Node.class.cast(proxy);

	}

	private class Handler implements InvocationHandler {
		private final WeakReference resourceRef;

		public Handler(Object resource) {
			this.resourceRef = new WeakReference(resource);
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Object resource = resourceRef.get();
			if ((resource == null)) {
				throw new CiliaIllegalStateException();
			} else {
				try {
					return method.invoke(resource, args);
				} catch (InvocationTargetException e) {
					throw e.getTargetException();
				} catch (Throwable e) {
					if (e instanceof NullPointerException) {
						throw new CiliaIllegalStateException();
					} else
						throw new RuntimeException(e.getMessage());
				}
			}
		}
	}
}