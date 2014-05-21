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
package fr.liglab.adele.cilia.model.impl;

import fr.liglab.adele.cilia.model.Adapter;
import fr.liglab.adele.cilia.model.Chain;
import fr.liglab.adele.cilia.util.FrameworkUtils;
import fr.liglab.adele.cilia.util.Watch;

import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represent the adapter in the model at execution.
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public class AdapterImpl extends MediatorComponentImpl implements Adapter {
    /**
     * The pattern this adapter has.
     */
    private PatternType adapterPattern = PatternType.UNASSIGNED;

    /**
     * @param adapterId
     * @param adapterType
     * @param adapterProperties
     * @param pattern
     */
    public AdapterImpl(String adapterId, String adapterType, String adapterNamespace, String version,
                       Dictionary adapterProperties, Chain chain, PatternType pattern) {
        super(adapterId, adapterType, adapterNamespace, null, version, adapterProperties, chain);
        adapterPattern = pattern;
        setChain(chain);
    }

    public AdapterImpl(String adapterId, String adapterType) {
        this(adapterId, adapterType, null, null, null, null, PatternType.UNASSIGNED);
    }

    /**
     * Get the pattern associated to the AdapterImpl.
     *
     * @return The pattern.
     */
    public PatternType getPattern() {
        return adapterPattern;
    }


    /**
     * Get the pattern associated to the AdapterImpl.
     *
     * @return The pattern.
     */
    public void setPattern(PatternType type) {
        adapterPattern = type;
    }


    /**
     * Set the chain representation model which will contain this mediator.
     *
     * @param chain chain which will contain this mediator.
     */
    public void setChain(Chain chain) {
        synchronized (lockObject) {
            this.chain = chain;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("{\n");
        sb.append(super.toString());
        sb.append("Pattern : ").append(getPattern().getName()).append(",\n");
        sb.append("\n}");
        return sb.toString();
    }

    public Map toMap() {
        Map result = new LinkedHashMap();
        result.put("UUID", FrameworkUtils.makeQualifiedId(chainId(), nodeId(), uuid()));
        result.put("Type", getType());
        result.put("Pattern", getPattern().getName());
        result.put("Namespace", getNamespace());
        result.put("ID", getId());
        result.put("State", getState());
        result.put("Version", getVersion());
        result.put("Creation date", Watch.formatDateIso8601(timeStamp()));
        result.put("Properties", super.getProperties());
        return result;
    }
}
