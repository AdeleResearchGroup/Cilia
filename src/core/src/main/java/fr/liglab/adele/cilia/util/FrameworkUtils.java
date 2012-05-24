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
	

	private static final Pattern IDENTIFIER = Pattern.compile(
			"^urn:"+ID_STRING_PATTTERN+":"+ID_STRING_PATTTERN,
			Pattern.CASE_INSENSITIVE);

	/**
	 * Ientifier composed by  a-zA-Z_0-9 allowed characters are -_:. 
	 * @param id
	 */
	public static final void checkIdentifier(String id)  {
		Pattern p = Pattern.compile(FrameworkUtils.ID_STRING_PATTTERN);
		if (!p.matcher(id).matches()) {
			throw new IllegalArgumentException(
					"id must be a word character + optionnal characters = {'.' ,'-' ,':' } id=" + id);
		}
	}
	
	/**
	 * @param aUrn
	 *            URN <URN> ::= "urn:" <NID> ":" <NSS>
	 * @return true if aUrn is an URN
	 */
	public static final boolean isUrn(String aUrn) {
		if (aUrn == null)
			throw new NullPointerException("urn is null !");
		return IDENTIFIER.matcher(aUrn).matches();
	}

	/**
	 * @param aUrn
	 *            an URN <URN> ::= "urn:" <NID> ":" <NSS>
	 * @return Suffixe part <NID> or aURN if it is not an URN
	 */
	public static final String getURNNameSpace(String aUrn) {
		String uuid;
		if ((aUrn == null) || (aUrn.length() == 0)) {
			return aUrn;
		}
		if (!isUrn(aUrn))
			return aUrn;
		String[] substring = aUrn.split(":");
		return substring[1];
	}

	/**
	 * @param aUrn
	 *            an URN <URN> ::= "urn:" <NID> ":" <NSS>
	 * @return Suffixe part <NSS> or aURN if it is not an URN
	 */
	public static final String getURNSuffixe(String aUrn) {
		String suffixe;
		if ((aUrn == null) || (aUrn.length() == 0)) {
			return aUrn;
		}
		if (!isUrn(aUrn))
			return aUrn;
		String[] substring = aUrn.split(":");
		if (substring.length > 2)
			suffixe = substring[substring.length-1];
		else
			suffixe = "";
		return suffixe;
	}

	/**
	 * 
	 * @param chainId
	 * @param mediatorId
	 * @param uuid
	 * @return String urn:chainId/mediatorId:uuid
	 */
	public static final String makeURN(String chainId,String mediatorId, String uuid)  {
		StringBuffer sb = new StringBuffer("urn:").append(chainId);
		sb.append("/").append(mediatorId);
		if ((uuid != null) && (uuid.length() > 0)) {
			sb.append(":").append(uuid);
		}
		return sb.toString();
	}

}
