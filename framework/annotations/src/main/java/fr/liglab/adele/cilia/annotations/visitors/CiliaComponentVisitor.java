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
package fr.liglab.adele.cilia.annotations.visitors;

import org.apache.felix.ipojo.manipulator.metadata.annotation.ComponentWorkbench;
import org.apache.felix.ipojo.manipulator.spi.BindingContext;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.commons.EmptyVisitor;


/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class CiliaComponentVisitor extends EmptyVisitor implements AnnotationVisitor {
	
	private String NAMESPACE = "fr.liglab.adele.cilia";
	
	private Element component;
	
	private ComponentWorkbench workbench;

	 public CiliaComponentVisitor(String component, BindingContext context) {
         this.component = new Element(component, null);
	     this.workbench = context.getWorkbench();
	    }

	    /**
	     * Visit @Processor annotation attributes.
	     */
	    public void visit(String name, Object value) {
	        if (name.equals("name")) {
	        	component.addAttribute(new Attribute("name", value.toString()));
	            return;
	        }
            else if (name.equals("namespace")) {
                component.addAttribute(new Attribute("namespace", value.toString()));
                return;
            }
	    }


    /**
	     * Append to the "component" element computed attribute.
	     */
	    public void visitEnd() {
            component.addAttribute(new Attribute("classname", workbench.getClassNode().name.replace("/", ".")));
            workbench.setRoot(component);
	    }
}
