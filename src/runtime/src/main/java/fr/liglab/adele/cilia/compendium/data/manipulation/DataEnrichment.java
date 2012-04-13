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

package fr.liglab.adele.cilia.compendium.data.manipulation;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;

import fr.liglab.adele.cilia.Data;


public class DataEnrichment {

    /**
     * Add split info to be used in agregator.
     * @param data splited data to add info.
     * @param size pieces of splited data.
     * @param number the number of this splited data.
     * @param id id of the data splited.
     * @return Data with the split info 
     */
    public static Data addSplitInfo(final Data data, final int size,
            final int number, final long id) {
        data.setProperty("SPLITED", "true");
        data.setProperty("SPLIT.TOTAL", new Integer(size));
        data.setProperty("SPLIT.NUMBER", new Integer(number));
        data.setProperty("SPLIT.ID", new Long(id));
        return data;
    }
    /**
     * When Data is enrichment with SplittInfo, this method
     * will return the amount of pices the Data was splitted . 
     * @param data Splitted Data.
     * @return the amount of all splitted Data.
     */
    public static int getSplitTotal(final Data data) {
        int total = 0;
        Integer num = (Integer) data.getProperty("SPLIT.TOTAL");
        if (num != null) {
            total = num.intValue();
        }
        return total;
    }
    /**
     * Add Metadata information to the given Data.
     * @param data Data to enrichment with new meta-information.
     * @param newmetadata Dictionary wich contains new meta-information.
     * @return The enrichemented Data.
     */
    public static Data addMetadataInfo(final Data data, Dictionary newmetadata ) {
        Enumeration enume = newmetadata.keys();
        while (enume.hasMoreElements())  {
            String key = enume.nextElement().toString();
            data.setProperty(key, newmetadata.get(key));
        }
        return data;
    }
    /**
     * Add Metadata information in all Data in the given DataSet.
     * @param dataset DataSet to enrichment with new meta-information.
     * @param newmetadata Dictionary wich contains new meta-information.
     * @return The enrichemented DataSet.
     */
    public static List addMetadataInfo(final List /*<Data>*/ dataset, Dictionary newmetadata ) {
        int datasize = dataset.size();
        for (int i = 0; i < datasize; i++) {
            Data data = (Data)dataset.get(i);
            data = addMetadataInfo(data, newmetadata);
        }
        return dataset;
    }
}
