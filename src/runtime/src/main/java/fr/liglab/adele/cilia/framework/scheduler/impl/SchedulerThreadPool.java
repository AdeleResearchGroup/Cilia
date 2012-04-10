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

package fr.liglab.adele.cilia.framework.scheduler.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.felix.ipojo.util.Callback;


public class SchedulerThreadPool {

    private ExecutorService threadPool;
    
    private Callback callback;
    
    private boolean isList = true;
    
    public SchedulerThreadPool(int poolSize, Callback call, boolean list) {
        threadPool = Executors.newFixedThreadPool(poolSize);
        callback = call;
        isList = list;
    }
    
    public void addTask(SchedulerTask task) {
        task.setCallback(callback);
        task.isList(isList);
        threadPool.execute(task);
    }
    
}
