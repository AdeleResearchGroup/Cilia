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

package fr.liglab.adele.cilia.knowledge.impl.specification;

import fr.liglab.adele.cilia.knowledge.Node;

/**
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class NodeImpl implements Node {

	private final String chain;
	private final String node;

	public NodeImpl(String chain, String node) {
		this.chain = chain;
		this.node = node;
	}

	public String chainId() {
		return chain;
	}

	public String nodeId() {
		return node;
	}

	public String uuid() {
		throw new UnsupportedOperationException(
				"uuid is not relevant for the specification");
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("qualified name [").append(chainId()).append("/").append(nodeId())
				.append("]");
		return sb.toString();
	}

}
