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

package fr.liglab.adele.cilia.knowledge.impl.runtime;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.knowledge.registry.RuntimeRegistry;

/**
 * Build a Weak Reference proxy
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class NodeProxy {

	public Object make(RuntimeRegistry r, String uuid, Object resource,
			Class interfaceClass) {
		Handler handler = new Handler(r, uuid, resource);
		Object proxy = Proxy.newProxyInstance(interfaceClass.getClassLoader(),
				new Class[] { interfaceClass }, handler);
		return interfaceClass.cast(proxy);
	}

	private class Handler implements InvocationHandler {
		private final WeakReference resourceRef;
		private final RuntimeRegistry registry;
		private final String uuid;

		public Handler(RuntimeRegistry r, String uuid, Object resource) {
			this.registry = r;
			this.uuid = uuid;
			this.resourceRef = new WeakReference(resource);
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			Object resource = resourceRef.get();
			if ((resource == null) || (registry.findByUuid(uuid) == null)) {
				/* disappears */
				throw new CiliaIllegalStateException(uuid +" no longer exists");
			} else {
				try { 
					return method.invoke(resource, args);
				} catch (InvocationTargetException e) {
					throw e.getTargetException(); 
				} catch (Exception e) {	
					throw new RuntimeException(e.getMessage());
				}
			}
		}
	}

}