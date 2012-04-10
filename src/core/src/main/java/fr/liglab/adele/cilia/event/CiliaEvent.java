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

package fr.liglab.adele.cilia.event;

public interface CiliaEvent {

	/* List of supported events (support bitmask operation ) */
	static final int EVENT_CHAIN_ADDED = 1 << 0;
	static final int EVENT_CHAIN_REMOVED = 1 << 1;
	static final int EVENT_CHAIN_STARTED = 1 << 2;
	static final int EVENT_CHAIN_STOPPED = 1 << 3;
	static final int EVENT_MEDIATOR_ADDED = 1 << 4;
	static final int EVENT_MEDIATOR_REMOVED = 1 << 5;
	static final int EVENT_MEDIATOR_PROPERTIES_UPDATED = 1 << 6;
	static final int EVENT_ADAPTER_ADDED = 1 << 7;
	static final int EVENT_ADAPTER_REMOVED = 1 << 8;

	/* ALl Events published */
	static final int ALL_EVENTS = (EVENT_CHAIN_ADDED | EVENT_CHAIN_REMOVED
			| EVENT_CHAIN_STARTED | EVENT_CHAIN_STOPPED | EVENT_MEDIATOR_ADDED
			| EVENT_MEDIATOR_REMOVED | EVENT_MEDIATOR_PROPERTIES_UPDATED
			| EVENT_ADAPTER_ADDED | EVENT_ADAPTER_REMOVED);

	/* String representation */
	static final String STR_EVENT_CHAIN_ADDED = "chain.added";
	static final String STR_EVENT_CHAIN_REMOVED = "chain.removed";
	static final String STR_EVENT_CHAIN_STARTED = "chain.started";
	static final String STR_EVENT_CHAIN_STOPPED = "chain;stopped";
	static final String STR_EVENT_MEDIATOR_ADDED = "mediator.added";
	static final String STR_EVENT_MEDIATOR_REMOVED = "mediator.removed";
	static final String STR_EVENT_MEDIATOR_PROPERTIES_UPDATED = "mediator.adpater.updated.properties.updated";
	static final String STR_EVENT_ADAPTER_ADDED = "adapted.added";
	static final String STR_EVENT_ADAPTER_REMOVED = "adapter.removed";
	
}
