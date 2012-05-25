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

/**
 * Set of usefull statics methods
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public final class FrameworkUtils {

	private static final String ID_STRING_PATTTERN ="(\\w-*\\.*:*)+" ;
	
	/**
	 * Ientifier composed by  a-zA-Z_0-9 allowed characters are -_:. 
	 * @param id
	 */
	public static final void checkIdentifier(String id)  {
		Pattern p = Pattern.compile(FrameworkUtils.ID_STRING_PATTTERN);
		if (!p.matcher(id).matches()) {
			throw new IllegalArgumentException(
					"id must be a word character + optionnal characters = {'_','.' ,'-' ,':' } id=" + id);
		}
	}
	
	/**
	 * @param chainId
	 * @param mediatorId
	 * @param uuid
	 * @return String urn:chainId/mediatorId:uuid
	 */
	public static final String makeQualifiedId(String chainId,String mediatorId, String uuid)  {
		//if ((chainId==null) || (mediatorId==null) ) {
		//	return "";
		//}
		StringBuffer sb = new StringBuffer(chainId);
		sb.append("/").append(mediatorId);
		if ((uuid != null) && (uuid.length() > 0)) {
			sb.append("/").append(uuid);
		}
		return sb.toString();
	}
}

