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
package fr.liglab.adele.cilia.builder;

import java.util.Map;

/**
 * Configures a mediator/adapter instance.
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
// @SuppressWarnings("rawtypes")
public interface MediatorConfigurator extends ConfiguratorValueSetter {

    final static int SHARED = 0;

    final static int PROCESSOR = 1;

    final static int SCHEDULER = 2;

    final static int DISPATCHER = 3;

    /**
     * Specifies that the following properties will be shared with
     * scheduler/processor/dispatcher.
     *
     * @return The current @link{Configurator} instance.
     */
    MediatorConfigurator shared();

    /**
     * Specifies that the following properties will be setted only to the
     * scheduler.
     *
     * @return The current @link{Configurator} instance.
     */
    MediatorConfigurator scheduler();

    /**
     * Specifies that the following properties will be setted only to the
     * processor.
     *
     * @return The current @link{Configurator} instance.
     */
    MediatorConfigurator processor();

    /**
     * Specifies that the following properties will be setted only to the
     * dispatcher.
     *
     * @return The current @link{Configurator} instance.
     */
    MediatorConfigurator dispatcher();

    /**
     * Assign a new key property to be added. It will assign it to the specified
     * set defined by the :
     * {@link fr.liglab.adele.cilia.builder.Configurator#shared(),
     * fr.liglab.adele.cilia.builder.Configurator#scheduler(),
     * fr.liglab.adele.cilia.builder.Configurator#processor(),
     * fr.liglab.adele.cilia.builder.Configurator#dispatcher()} methods.
     *
     * @param name The key name
     * @return a {@link ConfiguratorValueSetter} to set the value associated
     * with the added key.
     */
    ConfiguratorValueSetter key(String name);

    /**
     * To add a properties set. It will internally make a
     * {@link java.util.Hashtable#putAll(java.util.Map)} on the current
     * properties.
     *
     * @param props The new properties to be added to the current set.
     * @return This object to continue configuring.
     */
    MediatorConfigurator set(Map props);

    /**
     * Set scheduler properties located by the @link {@link CustomBuilderConfigurator}
     * object.
     * The current set will be automatically change to shared.
     *
     * @param conf The {@link CustomBuilderConfigurator} object.
     * @return This object to continue configuring.
     */
    MediatorConfigurator scheduler(CustomBuilderConfigurator conf);

    /**
     * Set processor properties located by the @link {@link CustomBuilderConfigurator}
     * object.
     * The current set will be automatically change to shared.
     *
     * @param conf The {@link CustomBuilderConfigurator} object.
     * @return This object to continue configuring.
     */
    MediatorConfigurator processor(CustomBuilderConfigurator conf);

    /**
     * Set dispatcher properties located by the @link {@link CustomBuilderConfigurator}
     * object.
     * The current set will be automatically change to shared.
     *
     * @param conf The {@link CustomBuilderConfigurator} object.
     * @return This object to continue configuring.
     */
    MediatorConfigurator dispatcher(CustomBuilderConfigurator conf);

}
