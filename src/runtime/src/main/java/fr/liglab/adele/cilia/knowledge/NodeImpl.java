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

package fr.liglab.adele.cilia.knowledge;

import fr.liglab.adele.cilia.Node;
import fr.liglab.adele.cilia.util.FrameworkUtils;
import fr.liglab.adele.cilia.util.Watch;

public class NodeImpl implements Node {
	protected String chainId, nodeId, uuid;
	protected long timestamp;

	public NodeImpl(Node node) {
		chainId = node.chainId();
		nodeId = node.nodeId();
		uuid = node.uuid();
		timestamp = node.timeStamp();
	}

	public String chainId() {
		return chainId;
	}

	public String nodeId() {
		return nodeId;
	}

	public String uuid() {
		return uuid;
	}

	public long timeStamp() {
		return timestamp;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(FrameworkUtils.makeQualifiedId(chainId,
				nodeId, uuid));
		sb.append(", creation date :" + Watch.formatDateIso8601(timestamp));
		return sb.toString();
	}

}
