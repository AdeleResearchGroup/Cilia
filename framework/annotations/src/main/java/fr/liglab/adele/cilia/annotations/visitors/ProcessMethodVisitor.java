package fr.liglab.adele.cilia.annotations.visitors;


import org.apache.felix.ipojo.manipulator.spi.BindingContext;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.MethodNode;

/**
 * User: torito
 * Date: 7/1/13
 * Time: 3:37 PM
 */
public class ProcessMethodVisitor  extends EmptyVisitor implements AnnotationVisitor {

    private String NAMESPACE = "fr.liglab.adele.cilia";

    private Element method = new Element("method", NAMESPACE);

    private BindingContext context;

    public ProcessMethodVisitor(BindingContext context) {
        this.context = context;
        MethodNode node = (MethodNode) context.getNode();
        System.out.println("********** INIT ProcessMethodVisitor: " + node.name + " " + node.desc);
        Type[] parameters = Type.getArgumentTypes(node.desc);
        Type returnType =  Type.getReturnType(node.desc);
        method.addAttribute(new Attribute("name", node.name));
        if (parameters.length != 1){
            throw new RuntimeException("Unable to handle more than one parameter in Processor" + node.name);
        }
        method.addAttribute(new Attribute("in.data.type", parameters[0].getClassName()));
        method.addAttribute(new Attribute("out.data.type", returnType.getClassName()));

    }



    /**
     * Append to the "component" element computed attribute.
     */
    public void visitEnd() {
        context.getWorkbench().getRoot().addElement(method);
    }
}
