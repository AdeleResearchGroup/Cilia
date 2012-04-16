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

package fr.liglab.adele.cilia.knowledge;

import java.util.regex.Pattern;

/**
 * Class providing static methods for URN
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public final class UniformResourceName {

	private static final Pattern URN_PATTERN = Pattern.compile(
			"^urn:[a-z0-9][a-z0-9-]{0,31}:([a-z0-9()+,\\-.:=@;$_!*']|%[0-9a-f]{2})+$",
			Pattern.CASE_INSENSITIVE);

	/**
	 * @param aUrn
	 *            URN <URN> ::= "urn:" <NID> ":" <NSS>
	 * @return true if aUrn is an URN
	 */
	public static final boolean isUrn(String aUrn) {
		if (aUrn == null)
			throw new NullPointerException("urn is null !");
		return URN_PATTERN.matcher(aUrn).matches();
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
			suffixe = substring[2];
		else
			suffixe = "";
		return suffixe;
	}

	/**
	 * Construct an URN
	 * 
	 * @param namespace
	 *            an URN <URN> ::= "urn:" <NID> ":" <NSS>
	 * @param suffixe
	 *            null if no extension
	 * @return urn:namespace or urn:namespace:suffixe
	 */
	public static final String makeURN(String namespace, String suffixe) {
		if (namespace == null)
			throw new NullPointerException("Namespace is null !");
		StringBuffer sb = new StringBuffer("urn:").append(namespace);
		if ((suffixe != null) && (suffixe.length() > 0)) {
			sb.append(":").append(suffixe);
		}
		return sb.toString();
	}

}
