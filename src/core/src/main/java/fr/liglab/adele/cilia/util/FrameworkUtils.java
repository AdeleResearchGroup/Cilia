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

package fr.liglab.adele.cilia.util;

import java.util.regex.Pattern;

import fr.liglab.adele.cilia.Node;


/**
 * Set of usefull statics methods
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public final class FrameworkUtils {

	private static final String ID_STRING_PATTTERN = "(\\w-*\\.*:*)+";

	/**
	 * Ientifier composed by a-zA-Z_0-9 allowed characters are -_:.
	 * 
	 * @param id
	 */
	public static final void checkIdentifier(String id) {
		Pattern p = Pattern.compile(FrameworkUtils.ID_STRING_PATTTERN);
		if (!p.matcher(id).matches()) {
			throw new IllegalArgumentException(
					"id must be a word character + optionnal characters = {'_','.' ,'-' ,':' } id="
							+ id);
		}
	}

	/**
	 * @param chainId
	 * @param mediatorId
	 * @param uuid
	 * @return String chainId/mediatorId/uuid
	 */
	public static final String makeQualifiedId(String chainId, String mediatorId,
			String uuid) {
		StringBuffer sb = new StringBuffer(chainId);
		sb.append("/").append(mediatorId);
		if (uuid != null) {
			sb.append("/").append(uuid);
		}
		return sb.toString();
	}
	
	public static final String makeQualifiedId(Node node) {
		StringBuffer sb = new StringBuffer(node.chainId());
		sb.append("/").append(node.nodeId());
		sb.append("/").append(node.uuid());
		return sb.toString();
	}
	
	/* Data flow management ldap keys word */
	public static final String VALUE_CURRENT = "value.current";
	public static final String VALUE_PREVIOUS = "value.previous";
	public static final String DELTA_ABSOLUTE = "delta.absolute";
	public static final String DELTA_RELATIVE = "value.relative";
	public static final String TIME_ELAPSED = "time.elapsed";
	public static final String TIME_CURRENT = "time.current";
	public static final String TIME_PREVIOUS = "time.previous";
	
}
