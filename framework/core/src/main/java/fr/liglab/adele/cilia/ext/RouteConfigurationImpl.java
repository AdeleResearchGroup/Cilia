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
package fr.liglab.adele.cilia.ext;


/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class RouteConfigurationImpl {

    private String condition;

    private String ports;

    public RouteConfigurationImpl() {
    }

    ;

    /* (non-Javadoc)
     * @see fr.liglab.adele.cilia.component.dispatcher.evaluator.RouteConfiguration#condition(java.lang.String)
     */
    public void condition(String cond) {
        condition = cond;
    }

    /* (non-Javadoc)
     * @see fr.liglab.adele.cilia.component.dispatcher.evaluator.RouteConfiguration#port(java.lang.String[])
     */
    public void port(String port) {
        ports = port;
    }

    /* (non-Javadoc)
     * @see fr.liglab.adele.cilia.component.dispatcher.evaluator.RouteConfiguration#getCondition()
     */
    public String getCondition() {
        return condition;
    }

    /* (non-Javadoc)
     * @see fr.liglab.adele.cilia.component.dispatcher.evaluator.RouteConfiguration#getPort()
     */
    public String getPort() {
        return ports;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RouteConfigurationImpl)) return false;
        RouteConfigurationImpl e = (RouteConfigurationImpl) obj;
        if (condition == null && e.getCondition() == null) {
            return true;
        }
        if (condition != null && e.getCondition() == null) {
            return false;
        }
        return (condition.compareTo(e.getCondition()) == 0);
    }

    public int hashcode() {
        if (condition == null) {
            return super.hashCode();
        }
        return condition.hashCode();
    }

}
