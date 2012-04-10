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

package fr.liglab.adele.cilia.framework.utils;

/**
 * String handler
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 *
 */
public class StringHandler {

	/**
	 * Split a String
	 * @param text
	 * @return
	 */
	public static String[] getListStrings(String text, String splitter) {
		text = BlankRemover.trim(text);
		String[] topics = text.split(splitter);
		return topics;
	}

	private static class BlankRemover {

		/* remove line return */
		private static String returntrim(String source) {
			return source.replaceAll("\n", " ");
		}
		
		/* remove leading whitespace */
		private static String ltrim(String source) {
			return source.replaceAll("^\\s+", "");
		}

		/* remove trailing whitespace */
		private static String rtrim(String source) {
			return source.replaceAll("\\s+$", "");
		}

		/* replace multiple white spaces between words with single blank */
		private static String itrim(String source) {
			String str = source.replaceAll("\\b\\s{2,}\\b", " ");
			return str.replaceAll(" ", "");
		}

		/* remove all superfluous white spaces in source string */
		public static String trim(String source) {
			return itrim(ltrim(rtrim(source)));
		}

	}

}
