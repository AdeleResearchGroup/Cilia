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

package fr.liglab.adele.cilia.controller;

import java.util.Dictionary;

import fr.liglab.adele.cilia.model.Collector;
import fr.liglab.adele.cilia.model.Dispatcher;
import fr.liglab.adele.cilia.model.Mediator;
import fr.liglab.adele.cilia.model.Scheduler;
import fr.liglab.adele.cilia.model.Sender;

public interface MediatorController {
    
    
    void updateInstanceProperties(Dictionary properties);
    
    void createCollector(Collector collector);
    
    void removeCollector(Collector collector);
    
    void createSender(Sender sender);
    
    void removeSender(Sender sender);
    
    int getState();
    
    void start();
    
    void stop();
    
}
