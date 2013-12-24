Extend the DSCilia language
===========================

The DSCilia language is the one which permits to specify mediation
chains using XML. To see more in detail how this language allows to
build mediation chains see the section [Application Description
Language](Cilia/Cilia_DSL_types_Reference_mediator/Cilia_Application_Description "wikilink").

Cilia proposes a way to extends this language. Those extensions allows
to customize the configuration of mediators in a better and more human
readable way. So, this extensions, in resume, will and only will allow
to add properties to mediators or any of its constituents *i.e.* its
scheduler, its processor and/or its dispatcher.

Description of the constituent to configure
-------------------------------------------

We will create a parser for the periodic scheduler. This scheduler has
two properties, **period** and **delay**.
[Here](Cilia/Cilia_Components#Periodic "wikilink") you can find info
about this scheduler.

Using the basic language, you must configure a mediator as follow:

~~~~ {.xml}
<mediator-instance id="mediatorId" type="MyMediator">
  <scheduler>
     <property name="period" value="5000"/>
     <property name="delay" value="30000"/>
  </scheduler>
</mediator-instance>
~~~~

Project Creation
----------------

To create a project, we follow the instructions presented on the
[creating project
section](Cilia/Getting_Started_Tutorial/Project_Creation "wikilink").

The CiliaExtenderParser interface
---------------------------------

In order to create a new extender parser, we need to create an OSGi
services which provides the
*fr.liglab.adele.cilia.model.parser.CiliaExtenderParser* interface.

~~~~ {.java}
public interface CiliaExtenderParser {
  /**
   * See if the parser could handle the component description
   * @return
   */
  public boolean canHandle(Object mediatorDescription);
  /**
   * Creates or modifies a Component based on the component description.
   * @param componentDescription
   * @return
   */
  public IComponent getComponent(Object componentDescription, IComponent currentComponent) throws   CiliaParserException; 

}
~~~~

This interface is located on the **cilia-core** project.

### Methods

There are two methods to implement.

-   **canHandle (Object componentDescription)**: As we have only, at the
    moment, one parser based on DOM, the received object should be a
    Node object (org.w3c.dom.Node). This method will be called by the
    main dscilia parser to determine if a mediator instance need to be
    parsed by the new extender parser. If the return of calling this
    method is **true**, then the **getComponent** method will be called.

-   **getComponent(Object componentDescription, IComponent
    currentComponent)**: This method is called if the canHandle method
    return true. The **componentDescription** object, as well as in
    canHandle method, will be a Node object (org.w3c.dom.Node). The
    objective of this method is to inspect the receiving Node object in
    order to obtain the desired information. The current component is
    the actual information previously obtained by the main dscilia
    parser or by any other parser.

### The DomExtenderParser class

This class could be used to facilitate the work of parsing. It has two
protected fields:

-   NAME: The name of the tag we are interested on.
-   NAMESPACE: The namespace for the tag.

Also, it has two protected methods:

-   Node getNode(String parent, Object mediatorNode): It will locate the
    Node with a parent called *parent*. Also, this node should has a
    localName equals to NAME and a URI Namespace equals to NAMESPACE.

For example, if the XML file is:

~~~~ {.XML}
<mediator-instance ...>
 <scheduler>
    <periodic period="5000" delay="30000" xmlns="fr.imag.adele.cilia.periodic" />
 </scheduler>
</mediator-instance>
~~~~

Setting the NAMESPACE field as *fr.imag.adele.cilia.periodic* and the
NAME as *periodic*. Then, calling the **getNode("scheduler",
mediatorNode)** will return the **periodic** node:

~~~~ {.XML}
  <periodic period="5000" delay="30000" xmlns="fr.imag.adele.cilia.periodic" />
~~~~

-   String getAttributeValue(Node node, String attrName): this method
    allow us to easily obtain the value of any attribute in a given
    node.

Creating the extender parser
----------------------------

Now, we know the interface we need to implement and the class that can
help us to build the service easily. So, now we will create the service.
First, we create a class implementing the *CiliaExtenderParser*
interface. To facilitate the work, this class will extends also the
*DomExtenderParser* class.

~~~~ {.java}
public class PeriodicParser extends DomExtenderParser implements
      CiliaExtenderParser {
  /**
  * We set the NAME and NAMESPACE to use the DomExtenderParser methods.
  */
  public PeriodicParser(){
    NAME="periodic";
    NAMESPACE="fr.liglab.adele.cilia.scheduler";
  }
  
  /**
  * We see if the mediator description has the periodic node with 
  * fr.liglab.adele.cilia.scheduler as namespace. 
  * Also, it should be a child of scheduler.
  */
  public boolean canHandle(Object mediatorDescription) {
    Node disp = getNode("scheduler",mediatorDescription);
    if(disp == null) {
      return false;
    }
    return true;
  }
  

  public IComponent getComponent(Object componentDescription,
         IComponent currentComponent) throws CiliaParserException {
    /**We obtain the node. We are sure it exist as this method is called
    * only when the canHandle returns true.
    */
    Node periodicN = getNode("scheduler",componentDescription);

    /** We obtain the configuration from the Node.*/
    String period  = getAttributeValue(periodicN, "period");
    String delay  = getAttributeValue(periodicN, "delay");

    /** We add the acquired information to the currentComponent as properties*/
    if (period != null) {
       Long lperiod = Long.parseLong(period);
       currentComponent.setProperty("period", lperiod);
    }
    if (delay != null) {
      ldelay = Long.parseLong(delay);
      currentComponent.setProperty("delay", ldelay);
    }
    return currentComponent;
  }

}
~~~~

Now, we only need to add the service on the service registry. Using
iPOJO, this could be done as follows:

~~~~ {.XML}
<!--We create the component description-->
<component
   classname="fr.liglab.adele.cilia.compendium.extender.parsers.PeriodicParser"
   immediate="true">
      <provides />
</component>
<!--And we create the instance-->
<instance
   component="fr.liglab.adele.cilia.compendium.extender.parsers.PeriodicParser" />
~~~~
