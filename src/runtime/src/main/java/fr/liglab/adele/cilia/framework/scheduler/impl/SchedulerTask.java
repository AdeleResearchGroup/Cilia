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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.felix.ipojo.util.Callback;


public class SchedulerTask implements Runnable{

    private final List dataList; 

    private Callback callback;

    private boolean isList = false;

    public SchedulerTask (List list, Callback call) {
        dataList = list;
        callback = call;
    }

    public SchedulerTask (List list) {
        this(list, null);
    }

    public void setCallback( Callback call) {
        callback = call;
    }

    public void isList(boolean islist) {
        isList = islist;
    }
    public void run() {
        if (callback == null) {
            throw new RuntimeException("SchedulerTask.run(): There is any callback to call");
        }
        Object args[] = new Object[1];//it support only one parametter.
        
        try {
            if (isList) {
                args[0] = dataList;
                callback.call(args);
            } else { //iterate in all the elements.
                for (int i = 0 ; i < dataList.size(); i++) {
                    args[0] = dataList.get(i);
                    callback.call(args);
                }
            }
            
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
