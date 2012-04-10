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
package fr.liglab.adele.cilia.framework;

public class CiliaComponentDescription {
	private String name;

	private String namespace;

	private String category;
	
	private String type;

	public CiliaComponentDescription(String ntype, String nname, String nspace,
	        String ccategory) {
	    this.type = ntype;
		this.name = nname;
		this.namespace = nspace;
		this.category = ccategory;
	}

	public String getCategory() {
		return category;
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
     * @return the name
     */
    public String getType() {
        return type;
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Type:");
        buffer.append(type);
        buffer.append(", Name: ");
        buffer.append(name);
        if (namespace != null) {
            buffer.append(", Namespace: ");
            buffer.append(namespace);
        }
        if (category != null) {
            buffer.append(", Category: ");
            buffer.append(category);
        }
        return buffer.toString();
    }
}
