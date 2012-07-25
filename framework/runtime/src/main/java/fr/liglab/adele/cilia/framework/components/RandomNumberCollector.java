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


package fr.liglab.adele.cilia.framework.components;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Random;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.framework.AbstractCollector;

public class RandomNumberCollector extends AbstractCollector implements Runnable {


    private long m_interval=6000;	
    private Thread m_thread;  

    public void started() {
        m_thread = new Thread(this);					
        m_thread.start();
    }

    public void stopped() {
        
        m_thread.interrupt();
        m_thread = null;

    }



 
    public void run() {			
        while (true){          
            try {
                Thread.sleep(m_interval);
                Random rn = new Random();
                long number = rn.nextInt() % 100;
                String source = "";

                Dictionary metadata = new Hashtable();
                metadata.put(Data.DATA_TYPE, Data.TEXT_DATA);
                metadata.put(Data.DATA_SOURCE, source);
                Data data = new Data(new Long(number),"random_number", metadata);


                notifyDataArrival(data);					              
                // wait t time			
                
            } catch (InterruptedException e) {	
            }

        }
    }	

}
