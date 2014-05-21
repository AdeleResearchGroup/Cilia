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

public interface UpdateActions {
    final static int UPDATE_ID = 1;
    final static int UPDATE_TYPE = 2;
    final static int UPDATE_CLASSNAME = 3;
    final static int UPDATE_PROPERTIES = 4;
    final static int UPDATE_SCHEDULER = 5;
    final static int UPDATE_DISPATCHER = 6;
    final static int ADD_COLLECTOR = 7;
    final static int ADD_SENDER = 8;
    final static int REMOVE_COLLECTOR = 9;
    final static int REMOVE_SENDER = 10;
    final static int ADD_MEDIATOR = 11;
    final static int REMOVE_MEDIATOR = 12;
    final static int ADD_BINDING = 13;
    final static int REMOVE_BINDING = 14;
    final static int ADD_ADAPTER = 15;
    final static int REMOVE_ADAPTER = 16;
}	
