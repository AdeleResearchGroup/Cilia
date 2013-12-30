Cilia DSL
---------

Cilia is a domain-specific component model, where the domain-specific is
mediation applications. Like almost all domain-specific component
models, there are special elements that must be specified. In this
section is described the language proposed by Cilia to define new
components types. The presented Cilia language uses an XML-based syntax

Mediator and its elements
-------------------------

Mediators are composed by three elements, scheduler, processor and
dispatcher.

### Scheduler

To create a new scheduler specification, is used the ***scheduler***
tag.

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>Each scheduler must have an associated name.
</td></tr>
<tr>
<td>classname
</td>
<td>No
</td>
<td>
</td>
<td>Defines the class name where the scheduler logic is located.
</td></tr>
<tr>
<td>namespace
</td>
<td>Yes
</td>
<td> fr.liglab.adele.cilia
</td>
<td>This attribute describes the scheduler namespace.
</td></tr></tbody></table>

#### Scheduler example

This scheduler triggers periodically the processing passing to the
processor all collected data. The Java class implementing the scheduler
logic is *org.my.first.scheduler.MyPeriodicScheduler*. The scheduler
name is MyPeriodicScheduler and it belonging to the
fr.liglab.adele.cilia.test namespace.

~~~~ {.xml}
   <scheduler name="MyPeriodicScheduler" namespace="fr.liglab.adele.cilia.test"
              classname="org.my.first.scheduler.MyPeriodicScheduler" />
~~~~



### Processor

To create a new processor specification, is used the ***processor*** tag

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>This attribute describes the processor name
</td></tr>
<tr>
<td>classname
</td>
<td>No
</td>
<td>
</td>
<td>This attribute describes the class name where the processor logic is located.
</td></tr>
<tr>
<td>namespace
</td>
<td>Yes
</td>
<td>fr.liglab.adele.cilia
</td>
<td>This attribute describes the processor namespace.
</td></tr></tbody></table>

#### Configuring processor method

To indicate the processor method, a tag called ***method*** must be
declared into the processor specification tag.

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>Yes
</td>
<td>process
</td>
<td>This attribute specifies the processor name.
</td></tr>
<tr>
<td>data.type
</td>
<td>Yes
</td>
<td>java.util.List
</td>
<td>This attribute specifies the parameter data type and the returning data type (both must be the same).
</td></tr></tbody></table>

#### Processor example

This processor filters data using a threshold value. The Java class
implementing the processor logic is
*org.my.first.processor.MyFilterProcessor*, this class must provide the
method *filter* that receives as parameter (and returns) an object of
type *fr.liglab.adele.cilia.Data*. The processor is called
*MyFilterProcessor* and belonging to the *fr.liglab.adele.cilia.test*
namespace.

~~~~ {.xml}
   <processor name="MyFilterProcessor" namespace="fr.liglab.adele.cilia.test" 
              classname="org.my.first.processor.MyFilterProcessor">
        <method name="filter" data.type="fr.liglab.adele.cilia.Data" />
   </processor>
~~~~

The Java class implementing this processor has a structure as follow:

~~~~ {.java}
package org.my.first.processor;

import fr.liglab.adele.cilia.Data;

public class MyFilterProcessor {

   /**
    * Method to be invoked by the Cilia framework
    * @param dataToProcess
    * @return
    */
   public Data filter(Data dataToProcess) {
      // Processor logig must be defined here 
      ....
   }
   
}
~~~~

### Dispatcher

To create a new dispatcher specification, is used the ***dispatcher***
tag.

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>Each dispatcher must have an associated name.
</td></tr>
<tr>
<td>classname
</td>
<td>No
</td>
<td>
</td>
<td>Defines the class name where the dispatcher logic is located.
</td></tr>
<tr>
<td>namespace
</td>
<td>Yes
</td>
<td> fr.liglab.adele.cilia
</td>
<td>This attribute describes the dispatcher namespace.
</td></tr></tbody></table>

#### Dispatcher example

This dispatcher will choice the destination based on the received
number, odd or even. The Java class implementing the dispatcher logic is
*org.my.first.dispatcher.MyOddEvenDispatcher*. The dispatcher name is
*MyOddEvenDispatcher* and it belonging to the
*fr.liglab.adele.cilia.test* namespace.

~~~~ {.xml}
   <dispatcher name="MyOddEvenDispatcher" namespace="fr.liglab.adele.cilia.test"
              classname="org.my.first.dispatcher.MyOddEvenDispatcher" />
~~~~

### Adding properties to the elements

To add properties to each of the mediators constituents, a
***Properties*** tag, followed by the needed ***property*** tag is
needed.

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>Property name.
</td></tr>
<tr>
<td>field
</td>
<td>No
</td>
<td>
</td>
<td>Specifies which field in the class will be configured by the property value.
</td></tr>
<tr>
<td>value
</td>
<td>Yes
</td>
<td>
</td>
<td>To specify the default value.
</td></tr></tbody></table>

#### Example : Scheduler with properties

~~~~ {.xml}
   <scheduler name="MyPeriodicScheduler" namespace="fr.liglab.adele.cilia.test"
              classname="org.my.first.scheduler.MyPeriodicScheduler" >
      <properties>
         <property name="period" field="m_period" value="10000" />
      </properties>
   </scheduler>
   
~~~~

~~~~ {.java}
package org.my.first.scheduler;
...
public class MyPeriodicScheduler extends CiliaScheduler implements Runnable {
  /**
  *Field injected by the framework.
  */
  private long m_period;
  ...
  /**
  *Method called by the thread.
  */
  public void run() {
     List dataToProcess = new ArrayList(dataList);
     dataList.clear();
     process(dataToProcess);
  }
}
~~~~

### Mediator type

In Cilia, mediators are formed by the composition of the previous
elements (scheduler, processor, dispatcher). To define a new mediator is
used the ***mediator-component*** tag.

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>Defines the mediator name.
</td></tr>
<tr>
<td>namespace
</td>
<td>Yes
</td>
<td>fr.liglab.adele.cilia
</td>
<td>Describes the mediator namespace.
</td></tr>
<tr>
<td>category
</td>
<td>Yes
</td>
<td>generic
</td>
<td>Each mediator could have a defined category (<i>e.g.</i> splitter, translator).
</td></tr></tbody></table>

#### Specifying the mediator’s scheduler

Each mediator must have a scheduler. To define the used scheduler, a
***scheduler*** tag is used. The needed attributes when defying the
scheduler are:

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>To specify which scheduler to use.
</td></tr>
<tr>
<td>namespace
</td>
<td>Yes
</td>
<td>
</td>
<td>To specify the scheduler namespace to use.
</td></tr></tbody></table>

#### Specifying the mediator’s processor

Each mediator must have a processor. To define the used processor, a
***processor*** tag is used. The needed attributes when defying the
processor are:

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>To specify which processor to use.
</td></tr>
<tr>
<td>namespace
</td>
<td>Yes
</td>
<td>
</td>
<td>To specify the processor namespace to use.
</td></tr></tbody></table>

#### Specifying the mediator’s dispatcher

Each mediator must have a dispatcher. To define the used dispatcher, a
***dispatcher*** tag is used. The needed attributes when defying the
dispatcher are:

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>To specify which dispatcher to use.
</td></tr>
<tr>
<td>namespace
</td>
<td>Yes
</td>
<td>
</td>
<td>To specify the dispatcher namespace to use.
</td></tr></tbody></table>

#### Specifying the mediator’s port

Each mediator must have a ports for to receive data and ports to deliver
the processed data. To define the ports a ***ports*** tag is used. And
it contains the ***in-port*** and ***out-port***. Each in or out port
containt the following attributes.

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>The name of the port.
</td></tr>
<tr>
<td>type
</td>
<td>No
</td>
<td>
</td>
<td>The data type accepted for that port.
</td></tr></tbody></table>

#### Mediator example

~~~~ {.xml}
   <mediator-component name="MyFilterMediator" namespace="fr.liglab.adele.cilia.test"
              category="Filter" >
       <scheduler name="MyPeriodicScheduler" namespace="fr.liglab.adele.cilia.test"/>
       <processor name="MyFilterProcessor" namespace="fr.liglab.adele.cilia.test"/>
       <dispatcher name="MyOddEvenDispatcher" namespace="fr.liglab.adele.cilia.test"/>
       <ports>
          <in-port name="default" type="string" />
          <out-port name="default" type="string" />
       </ports>
   </mediator-component>
~~~~

#### Mediator specification at run-time

Mediator components could be specified at run-time. First it is
necessary to obtain the *fr.liglab.adele.cilia.CiliaContext* service,
there are two ways to obtain it.

##### Obtaining CiliaContext by using the BundleContext

To obtain the CiliaContext it is need to obtain first the BundleContext
In this
[link](http://felix.apache.org/site/apache-felix-tutorial-example-1.html)
is presented how obtain the BundleContext using the BundleActivator.

~~~~ {.java}
...
   ServiceReference[] serv = bundleContext.getAllServiceReferences(CiliaContext.class.getName(), (*));
   if (serv != null && serv.length != 0) {
       ciliaContext = (CiliaContext) bundleContext.getService(serv[0]);
   }
...
~~~~

##### Obtaining CiliaContext by using the iPOJO or other OSGi service injector

Using [iPOJO](http://felix.apache.org/site/apache-felix-ipojo.html) it
is possible to obtain the CiliaContext service.

~~~~ {.java}
...
@Requires
private CiliaContext ciliaContext;
...
~~~~

Using [Spring
DM](http://static.springsource.org/osgi/docs/current/reference/html/)

~~~~ {.java}
...
@ServiceReference
public void setCiliaContext(CiliaContext ccontext) { ... }
...
~~~~

##### Creating a Mediator Specification

As say previously, mediators are composed by three elements, scheduler,
dispatcher and processor. Thus to define new Mediators at run-time there
is need of such information.

~~~~ {.java}
...
/* The mediator information*/
String mediatorName = "MyNewMediatorStringSplitter";
String defaultNamespace = "fr.liglab.adele.cilia";
String mediatorCategory = "Splitter";
/*Creating the empty mediator specification*/
MediatorSpecification mtype = ciliaContext.createMediatorSpecification(
                      mediatorName,mediatorNamespace, mediatorCategory);

/* Setting up mediator information*/
mtype.setScheduler("immediate-scheduler", defaultNamespace);
mtype.setProcessor("string-splitter", defaultNamespace);
mtype.setDispatcher("content-based-dispatcher", defaultNamespace);
/*Initializing mediator specification*/
mtype.initializeSpecification();
...
~~~~

It is important to notify the framework when we do not need any more the
created mediator specification, to do so, it is need to stop it. After
stop the specification, there is not possible to create a mediator
instance of it.

~~~~ {.java}
...
mtype.stopSpecification();
...
~~~~

Connectors
----------

A Cilia mediation chain is composed by mediation components, and their
connections. There are three communication styles in Cilia: inter
mediation communication, communication from the chain boundaries, and
communication with external services.

### Inter Mediation Communication

In order to communicate two mediation components, Cilia uses the
*linker* pattern. There are some linker components predefined for Cilia
(*direct, event-admin*), but there is possible to extend and build newer
ones.

The linker is the special component which is in charge to logically bind
two mediator instances. At runtime, this binding is performed by other
communication components, the one who is in charge to send the message,
and the one who is in charge to receive it.

So the linker is defined by these two communication components, a
*collector*, and a *sender*

The [Advanced developer
guide](http://wikiadele.imag.fr/index.php/Cilia/Developers_Guide/) shows
how to create new Linkers.

### Adapters

Adapters are a special type of mediators. They scheduler, processor and
dispatcher are defined *a priori*. And there is not possible to change
them. Instead, adapters are defined by its communication component:
collector and sender. Or by an specific service specification.

There are three adapter patterns:

-   ***In Adapter***: To receive messages from the chain boundaries
    (External Applications).
-   ***Out Adapter***: To send messages to the chain boundaries
    (External Applications).
-   ***In-Out Adapter***: To send messages to the chain and wait for the
    result.

**Ports in Adapters** are used to bind adapter to another mediation
components. So an **In Adapter** must have an **out-port** and an **Out
Adapter** must have an **in-port**. **In-Out Adapters** must have both.

#### In Adapter

The ***In Adapters*** is defined by its collector component.

Collectors are the communication components which collect or receives
the data. They could receive the data from an external application or
from a communication protocol.

To create a new collector specification, is used the ***collector***
tag. The attributes associated to collectors are:

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>Each collector must have an associated name.
</td></tr>
<tr>
<td>classname
</td>
<td>No
</td>
<td>
</td>
<td>Defines the class name where the collector logic is located.
</td></tr></tbody></table>

To create a new adapter specification, is used the ***adapter*** tag.
The attributes associated to adapters are:

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>Each adapter must have an associated name.
</td></tr>
<tr>
<td>namespace
</td>
<td>Yes
</td>
<td>fr.liglab.adele.cilia
</td>
<td>Defines the adapter namespace.
</td></tr>
<tr>
<td>pattern
</td>
<td>No
</td>
<td>
</td>
<td>Defines adapter pattern, This adapter must have 'in-only' as pattern.
</td></tr></tbody></table>

##### In Adapter example

This adapter polls the content of the desired file to process it
periodically.

~~~~ {.xml}
   <collector name="MyLogFileCollector" 
              classname="org.my.first.file.collector.MyLogFileCollector" >
      <properties>
         <property name="filename" field="m_filename" />
         <property name="pool.period" field="m_period" />
      </properties>
   </collector>
   <adapter name="file-adapter" pattern="in-only">
      <collector type="MyLogFileCollector"  />
      <ports>
         <out-port name="default" type="string" />
      </ports>
   </adapter>  
~~~~

~~~~ {.java}
package org.my.first.file.collector;
...
public class MyLogFileCollector extends AbstractCollector implements Runnable {
   /**
    * Field injected by the framework, it refers to the ''filename'' property.
    */
   private String m_filename;
   /**
    * Field injected by the framework, it refers to the ''pool.period'' property.
    */
   private String m_period;
   /**
    * Method to be invoked by the thread when collecting data.
    */
   ...
   public void run () {
       Data data = createDataFromFile();
       super.notifyDataArrival(data);
   }
}
~~~~

#### Out Adapter

The ***Out Adapters*** is defined by its sender component. Senders are
the communication components which deliver the processed data. They
could send the data to an external application or through a
communication protocol.

To create a new sender specification, is used the ***sender*** tag. The
attributes associated to senders are:

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>Each sender must have an associated name.
</td></tr>
<tr>
<td>classname
</td>
<td>No
</td>
<td>
</td>
<td>Defines the class name where the sender logic is located.
</td></tr></tbody></table>

To create a new adapter specification, is used the adapter tag.

The attributes associated to adapters are:

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>Each adapter must have an associated name.
</td></tr>
<tr>
<td>namespace
</td>
<td>Yes
</td>
<td>fr.liglab.adele.cilia
</td>
<td>Defines the adapter namespace.
</td></tr>
<tr>
<td>pattern
</td>
<td>No
</td>
<td>
</td>
<td>Defines adapter pattern, This adapter must have 'out-only' as pattern.
</td></tr></tbody></table>

##### Out Adapter example

This sender stock the processed data to a file.

~~~~ {.xml}
   <sender name="MyLogFileSender" 
              classname="org.my.first.file.sender.MyLogFileSender" >
      <properties>
         <property name="filename" field="m_filename" />
      </properties>
   </sender>
   <adapter name="file-out-adapter" pattern="out-only">
      <sender type="MyLogFileSender" />
      <ports>
         <in-port name="default" type="string" />
      </ports>
    </adapter>
~~~~

~~~~ {.java}
package org.my.first.file.sender;
...
public class MyLogFileSender implements ISender  {
   /**
    * Field injected by the framework, it refers to the ''filename'' property.
    */
   private String m_filename;
   /**
    * Method to be invoked by the Cilia framework.
    * @param data the processed data.
    * @return True if the data has been sent, false if not.
    */
   public boolean send (Data data) {
   //Stock the received data to the file specified in the m_filename field.
   }
}
~~~~

#### In-Out Adapter
There is only one synchronous implementation of an IO-Adapter, it will block until the reponse is achieved or a time-out is triggered. See https://github.com/AdeleResearchGroup/Cilia/blob/master/examples/hello-world-example-annotations/hello-mediator/src/main/java/hello/world/example/adapters/GuiAdapter.java for an example.

To create a new IO-Adapter specification, is used the ***io-adapter***
tag.

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>Each io-adapter must have an associated name.
</td></tr>
<tr>
<td>classname
</td>
<td>No
</td>
<td>
</td>
<td>Defines the class name where the adapter logic is located.
</td></tr>
<tr>
<td>namespace
</td>
<td>Yes
</td>
<td> fr.liglab.adele.cilia
</td>
<td>This attribute describes the adapter namespace.
</td></tr></tbody></table>

#### Specifying the io-adapter’s port

Each io-adapter must have at least one port to receive data and one port to deliver
the data. To define the ports a ***ports*** tag is used. And
it contains the ***in-port*** and ***out-port***. Each in or out port
containt the following attributes.

<table cellpadding="4" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DEFAULT
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>
</td>
<td>The name of the port.
</td></tr>
<tr>
<td>type
</td>
<td>No
</td>
<td>
</td>
<td>The data type accepted for that port.
</td></tr></tbody></table>

#### IO-Adapter example

~~~~ {.xml}
   <io-adapter classname="org.my.first.io.adapter.GuiAdapter"
                name="gui-adapter">
                <ports>
                        <in-port name="in" type="*" />
                        <out-port name="out" type="*" />
                </ports>
        </io-adapter>
~~~~

To create the only (blocking-)implementation of the io-adapter the class must inherit the abstract class  **fr.liglab.adele.cilia.framework.AbstractIOAdapter** as follows:

~~~~ {.java}
package org.my.first.io.adapter;
...
public class GuiAdapter extends AbstractIOAdapter {

        public void adaptionCode(){
            ...
            Data result = null;
            try {
                result = invokeChain(data); //wait some time for the result.
            } catch (TimeoutException e1) {
                e1.printStackTrace();
            }
            ...
        }

        @Override
        public void receiveData(Data data){
                super.receiveData(data);
        }
        @Override
        public Data dispatchData(Data data) {
                return super.dispatchData(data);
        }
}
~~~~

One restriction due to the inheritance problems of iPOJO, is that the new adapter class must override the receiveData and dispatchData as presented in the example.

