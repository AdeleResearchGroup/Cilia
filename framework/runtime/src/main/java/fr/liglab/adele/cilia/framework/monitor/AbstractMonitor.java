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
package fr.liglab.adele.cilia.framework.monitor;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.runtime.MediatorHandler;

import java.util.List;
import java.util.Map;

public abstract class AbstractMonitor extends MediatorHandler implements IMonitor {

    public void onCollect(Data data) {
    }

    public void onProcessEntry(List data) {
    }

    public void onProcessExit(List data) {
    }

    public void onDispatchEntry(List data) {
    }

    public void onDispatch(List data) {
    }

    public void onProcessError(List data, Exception ex) {
    }

    public void fireEvent(Map info) {
    }

    public void onServiceArrival(Map info) {
    }

    public void onServiceDeparture(Map info) {
    }

    public void onFieldGet(String field, Object o) {
    }

    public void onFieldSet(String field, Object o) {
    }

}
