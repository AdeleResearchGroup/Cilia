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

import fr.liglab.adele.cilia.exceptions.BuilderException;


/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 */
public interface Architecture {

    final static int MEDIATOR = 0;

    final static int ADAPTER = 1;

    final static int BINDING = 2;

    final static int CREATE = 0;

    final static int MODIFY = 1;

    final static int REMOVE = 2;

    final static int REPLACE = 3;

    Binder bind() throws BuilderException;

    Binder unbind() throws BuilderException;

    Creator create() throws BuilderException;

    Replacer replace() throws BuilderException;

    Replacer copy() throws BuilderException;

    Remover remove() throws BuilderException;

    Modifier configure() throws BuilderException;

    boolean toCreate();


}
