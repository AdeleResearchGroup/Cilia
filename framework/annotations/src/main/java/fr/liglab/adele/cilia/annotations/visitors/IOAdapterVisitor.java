package fr.liglab.adele.cilia.annotations.visitors;

import org.apache.felix.ipojo.manipulator.metadata.annotation.ComponentWorkbench;
import org.apache.felix.ipojo.manipulator.spi.BindingContext;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * User: garciai@imag.fr
 * Date: 9/24/13
 * Time: 4:50 PM
 */
public class IOAdapterVisitor extends EmptyVisitor implements AnnotationVisitor {

    private String NAMESPACE = "fr.liglab.adele.cilia";

    private Element component;

    private ComponentWorkbench workbench;

    public IOAdapterVisitor(BindingContext context) {
        this.component = new Element("IO-Adapter", null);
        this.workbench = context.getWorkbench();
    }

    /**
     * Visit @Processor annotation attributes.
     */
    public void visit(String name, Object value) {

        if (name.equals("name")) {
            component.addAttribute(new Attribute("name", value.toString()));
            return;
        } else if (name.equals("namespace")) {
            component.addAttribute(new Attribute("namespace", value.toString()));
            return;
        }
    }

    public AnnotationVisitor visitAnnotation(String name, String annotation) {

        if (annotation.compareTo("Lfr/liglab/adele/cilia/annotations/Port;") == 0) {
            return new PortInfoVisitor(name);
        }

        return this;

    }

    /**
     * Append to the "component" element computed attribute.
     */
    public void visitEnd() {
        component.addAttribute(new Attribute("classname", workbench.getClassNode().name.replace("/", ".")));
        workbench.setRoot(component);
    }

    /**
     * To add port definition.
     */
    private class PortInfoVisitor extends EmptyVisitor implements AnnotationVisitor {

        private String name;

        private String portName;

        private String dataType = "*";

        public PortInfoVisitor(String name) {
            this.name = name;
        }

        /**
         * Visit @Processor annotation attributes.
         */
        public void visit(String name, Object value) {

            if (name.equals("name")) {
                portName = String.valueOf(value);
            } else if (name.equals("dataType")) {
                //slashed classname: change it from L/package/name/ClassName; to package.name.ClassName
                //remove "L" and ";", and replace "/" for "."
                String classname = String.valueOf(value);
                dataType = String.valueOf(classname.substring(1, classname.length() - 1).replace("/", "."));//slashed classname
            } else if (name.equals("semanticType")) {
                dataType = String.valueOf(value);
            }
        }

        public void visitEnd() {
            Element portElement = new Element(name.replace('_', '-'), null);//in-port or out-port, instead of in_port/out_port
            Element[] ports = component.getElements("ports");
            Element portsElement = null;
            if (ports == null || ports.length < 1) {
                portsElement = new Element("ports", null);
                component.addElement(portsElement);
            } else {
                portsElement = ports[0];//there is only one ports element
            }
            portElement.addAttribute(new Attribute("name", portName));
            portElement.addAttribute(new Attribute("type", dataType));
            portsElement.addElement(portElement);
        }
    }

}
