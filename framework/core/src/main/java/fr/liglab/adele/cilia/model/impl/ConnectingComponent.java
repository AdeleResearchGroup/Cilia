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
/**
 *
 */
package fr.liglab.adele.cilia.model.impl;

import java.util.Dictionary;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class ConnectingComponent extends InternalComponent {

    private final String portname;

    /**
     * @return the portname
     */
    public String getPortname() {
        return portname;
    }

    /**
     * @param id
     * @param type
     * @param nspace
     * @param properties
     */
    public ConnectingComponent(String id, String type, String nspace, String portname,
                               Dictionary properties) {
        super(id, type, nspace, properties);
        this.portname = portname;
    }

}
