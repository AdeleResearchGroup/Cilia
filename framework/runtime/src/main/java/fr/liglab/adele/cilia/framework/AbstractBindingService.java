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

import java.util.Dictionary;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.exceptions.CiliaException;
import fr.liglab.adele.cilia.model.Binding;
import fr.liglab.adele.cilia.model.Component;
import fr.liglab.adele.cilia.model.impl.CollectorImpl;
import fr.liglab.adele.cilia.model.impl.SenderImpl;
import fr.liglab.adele.cilia.util.Const;

/**
 * 
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public abstract class AbstractBindingService implements CiliaBindingService {

    String collectorType ;
    
    String senderType ;
    
    String senderNS = Const.CILIA_NAMESPACE;
    
    String collectorNS = Const.CILIA_NAMESPACE;

    private short nature = NATURE_UNASSIGNED;
    
    private volatile static int identificator = 0;
    
    private static final Logger loggerRt = LoggerFactory.getLogger(Const.LOGGER_RUNTIME);
    
    public abstract Dictionary getProperties(Dictionary collectorProperties,
            Dictionary senderProperties, Binding b) throws CiliaException;

    public Component getCollectorModel(Dictionary props) {
        
        if (collectorType == null || collectorType.compareTo("") == 0) {
            if ((nature & NATURE_IN) > 0) {
                loggerRt.error("Linker Service is not well configured, collector.type not set");
            }
            return null;
        }
        if (props == null) {
            props = new Properties();
        }
        Component collector = new CollectorImpl(collectorType+identificator++, collectorType,collectorNS,null, props);
        return collector;
    }

    public Component getSenderModel(Dictionary props) {
        if (senderType == null || senderType.compareTo("") == 0) {
            if ((nature & NATURE_OUT) > 0) {
                loggerRt.error("Linker service is not well configured, sender.type not set");
            }
            return null;
        }
        if (props == null) {
            props = new Properties();
        }
        Component sender = new SenderImpl(senderType+identificator++, senderType, senderNS,null,  props);
        return sender;
    }
    
    public void setSenderType(String st){
        this.senderType = st;
    }
    
    public void setCollectorType(String ct) {
        this.collectorType = ct;
    }
    
    public void setSenderNS(String senderNS){
        this.senderNS = senderNS;
    }
    
    public void setCollectorNS(String collectorNS) {
        this.collectorNS = collectorNS;
    }
    
    public void setBindingNature(String nat) {
        if ("SEND-RECEIVE".compareToIgnoreCase(nat) == 0) {
            nature = NATURE_INOUT;
        }
        if ("SEND-ONLY".compareToIgnoreCase(nat) == 0) {
            nature = NATURE_OUT;
        }
        if ("RECEIVE-ONLY".compareToIgnoreCase(nat) == 0) {
            nature = NATURE_IN;
        }
    }

    public short getBindingNature() {
        return nature;
    }

}
