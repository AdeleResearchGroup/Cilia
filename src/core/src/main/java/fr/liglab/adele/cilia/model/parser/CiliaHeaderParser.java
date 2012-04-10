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

package fr.liglab.adele.cilia.model.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import fr.liglab.adele.cilia.CiliaException;
import fr.liglab.adele.cilia.model.Chain;

/**
 * This class will read the header to get the cilia chain instances.
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class CiliaHeaderParser {

    private ChainParser xmlParser;

    private static final Logger logger = LoggerFactory.getLogger("cilia.core");

    public CiliaHeaderParser() {
        xmlParser = new XmlChainParser(); 
    }
    /**
     * This method will obtain chain models
     * from the bundle header.
     * @param headers
     * @return
     */
    public  Chain[] getChainsFromHeader(Dictionary headers, Bundle bundle) throws CiliaException{
        Chain [] chains = null;
        //File xmlFile=null;
        String ciliaFile = (String) headers.get("cilia-file");
        if (ciliaFile == null) {
            return null;
        }
        try {
            URL ciliaFileUrl = bundle.getResource(ciliaFile);
            chains = getChainFromFile(ciliaFileUrl);
        } catch (FileNotFoundException e) {
            throw new CiliaException(ciliaFile + " Root element must be <cilia>", e);
        }
        return chains;
    }

    private  Chain[] getChainFromFile(URL xmlFile) throws CiliaException, FileNotFoundException{
        List listChains = new ArrayList();
        InputStream fis;
        try {
            fis = xmlFile.openStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw new CiliaException("Error when trying to open " + xmlFile.getPath() + " File.", e);
        }
        //First child is the root node.
        Node node = XmlTools.streamToNode(fis).getFirstChild();
        String rootName = node.getNodeName();
        if (rootName.compareTo("cilia") != 0) {
            throw new CiliaException(xmlFile.getPath() + " Root element must be <cilia> and is <" + rootName + ">");
        }
        logger.debug("Found cilia tag");
        Node possibleChain = node.getFirstChild();
        while (possibleChain!= null) {
            String nodeName = possibleChain.getNodeName(); 
            if (nodeName.compareTo("chain") == 0) {
                logger.debug("Found chain in file: ");
                Chain newChain = xmlParser.parseChain(possibleChain);
                if (newChain == null) {
                    logger.warn("Found chain in file but is null: ");
                } else {
                    listChains.add(newChain);
                }
            }

            possibleChain = possibleChain.getNextSibling();
        }
        if (listChains.isEmpty()) {
            return null;
        }
        Chain[] newArray = new Chain[listChains.size()];
        for (int i = 0; i < listChains.size(); i++)
            newArray[i] = (Chain)listChains.get(i);
        return newArray;
    }




}
