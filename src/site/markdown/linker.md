Linker Creation
===============

Linkers are components (and OSGi srvices) in charge of performing the
binding operation between two mediators using a defined protocol.

In that sense, bindings are a logical relation between a source mediator
and a target mediator. And the protocol is the way mediators communicate
in order to share or send information while running.

So, the linker service is the one who configure and add *sender* and
*collector* elements to both source and target mediators.

In this section we will learn how to, first create the collector and
sender in charge of communication between components, then how to create
a linker component which will plug those elements to the desired
mediator components.

This tutorial is based on the *Event Admin* service provided by the
Apache Felix project. [EA][] is a publish/subscriber service based on
topics.

Create the project
------------------

The project is created using Maven, and in order to create an OSGi
bundle using iPOJO it is needed to use Maven Bundle Plugin and Maven
iPOJO plugin as follows:

    <plugin>
       <groupId>org.apache.felix</groupId>
       <artifactId>maven-bundle-plugin</artifactId>
       <extensions>true</extensions>
       <configuration>
          <instructions>
             <Bundle-SymbolicName>${pom.artifactId}</Bundle-SymbolicName>
             <Private-Package></Private-Package>
          </instructions>
       </configuration>
    </plugin>
    <plugin>
       <groupId>org.apache.felix</groupId>
       <artifactId>maven-ipojo-plugin</artifactId>
       <executions>
          <execution>
             <goals>
                <goal>ipojo-bundle</goal>
             </goals>
          </execution>
       </executions>
    </plugin>


The needed dependencies for this project are two: The cilia run-time and
the event admin implementation provided by Apache Felix.


    <dependencies>
       <dependency>
          <groupId>fr.liglab.adele.cilia</groupId>
          <artifactId>cilia-core</artifactId>
       </dependency>
       <dependency>
          <groupId>org.apache.felix</groupId>
          <artifactId>org.apache.felix.eventadmin</artifactId>
       </dependency>
    </dependencies>


On the root of the project, create a file named *metadata.xml*. This
file will contain the specifications of the linker and the constituents
created for this project. At this moment, we leave this file empty.

In order to create the linker service, first it is needed to create the
elements in charge of performing the communication operation for the
given protocol as presented on the following section.

Creating the Event Admin Sender
-------------------------------

First we need to create a class called EventAdminSender. We place this
class on the package *fr.liglab.adele.cilia.components.sender.ea*. This
class must implement the *fr.liglab.adele.cilia.framework.ISender*
interface and implement the *send* method.

    package fr.liglab.adele.cilia.components.sender.ea;
    /* Packages for Cilia*/
    import fr.liglab.adele.cilia.framework.ISender;
    import fr.liglab.adele.cilia.Data;
    
    public class EventAdminSender implements ISender {
    
      public boolean send(Data data) {
          /*Here the sending code*/
      }
    }


In order to use Event Admin to publish events, we need to use the Event
Admin Service. We complete the code using Event Admin.

    package fr.liglab.adele.cilia.components.sender.ea;
    /*Imported classes to use Event Admin*/
    import org.osgi.service.event.Event;
    import org.osgi.service.event.EventAdmin;
    /* Imported classes to implement the Sender Cilia*/
    import fr.liglab.adele.cilia.framework.ISender;
    import fr.liglab.adele.cilia.Data;
    
    public class EventAdminSender implements ISender {
    /**
    * Event Admin. Will be injected by iPOJO.
    */
      private EventAdmin eventAdmin;
    /**
    * Topic to use when sending each data. Also injected by the framework.
    */
      private String topic;
      public boolean send(Data data) {
         /*It will post the contained data on the given topic */
         eventAdmin.postEvent(new Event(topic, data.getAllData()));
         return true;  
      }
    }


In the *metadata.xml* file we need to add the description of this
sender.

    <cilia>
      <sender classname="fr.liglab.adele.cilia.components.sender.ea.EventAdminSender"  name="ea-sender" >
          <requires field="eventAdmin" /> <!--The required Event Admin Service-->
          <properties>
             <property name="topic" field="topic" /> <!--The injected topic-->
          </properties>
       </sender>
    </cilia>


Creating the Event Admin Collector
----------------------------------

Now, we need to create a collector which will complete the new protocol
communication using Event Admin between two mediator components. So
create a class named EventAdminCollector. Using eclipse or netbeans we
create the new class on the package
*fr.liglab.adele.cilia.components.collector.ea*. To create a collector
we need to extend the abstract class
**fr.liglab.adele.cilia.framework.AbstractCollector**. And, to create
this specific EventAdmin collector we also implement the
**org.osgi.service.event.EventHandler** interface.

    package fr.liglab.adele.cilia.components.collector.ea;
    /* Packages for Cilia*/
    import fr.liglab.adele.cilia.framework.AbstractCollector;
    import fr.liglab.adele.cilia.Data;
    /*Empty collector*/
    public class EventAdminCollector extends AbstractCollector {
    
    
    }


In order to use Event Admin to publish events, we need to use the Event
Admin Service. We complete the code using Event Admin.

    package fr.liglab.adele.cilia.components.collector.ea;
    /*Imported classes to use Event Admin*/
    import org.osgi.service.event.Event;
    import org.osgi.service.event.EventHandler;
    /* Imported classes to implement the Sender Cilia*/
    import fr.liglab.adele.cilia.Data;
    import fr.liglab.adele.cilia.framework.AbstractCollector;
    
    public class EventAdminCollector extends AbstractCollector implements EventHandler {
      /*Method called when receiving an event*/  
      public void handleEvent(Event event) {
        Dictionary dico = new Hashtable(); //to store the incoming data.
        String[] keys = event.getPropertyNames();
        if (keys != null) {
          for (int i = 0; i < keys.length; i++) {
            dico.put(keys[i], event.getProperty(keys[i]));//add the incoming data.
          }
        }
        /*
        * Create new Data object from the received event.
        */
        Data data = new Data(dico.get(Data.DATA_CONTENT),
                              String.valueOf(dico.get(Data.DATA_NAME)), 
                              dico);
        notifyDataArrival(data); //Notify to scheduler that a new Data has arrived.
      }
    }

Now, we need to register the Event Handler in order to be notified when
someone wants to send data. We create a register and unregister methods.

    /*Topic to listen events, this field is injected by iPOJO*/
    private String topic;
    private BundleContext context;
    private ServiceRegistration serviceRegistration; //to register the event handler.
    private void register() {
      Dictionary dico = new Hashtable();
      String[] topics = {topic}; //We'll listen only one topic.
      dico.put(EventConstants.EVENT_TOPIC, topics);
      if (serviceRegistration != null) {
        serviceRegistration.unregister();
      }
      serviceRegistration = context.registerService(
      EventHandler.class.getName(), this, dico); //register the event listener
    }

    /**
     * Called when the component stops
     */
    public void unregister() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister(); //unregister the event listener.
        }
    }


In the *metadata.xml* file we need to add the description of this
collector.

    <cilia>
     <collector classname="fr.liglab.adele.cilia.components.collector.ea.EventAdminCollector"   name="ea-collector" >
       <properties>
          <property name="topic" field="topic" />
       </properties>
       <callback transition="validate" method="register" />
       <callback transition="invalidate" method="unregister" />
     </collector>
    </cilia>


Create the Event Admin Linker Service
-------------------------------------

Now we have created the elements that allow to communicate mediator
instances in a given protocol. In this example we use Event Admin as a
communication protocol. Now we need to create a linker service which is
in charge to connect two mediator components at run-time. This linker
service will interact at a model level at run-time, so it will not
interact with the executing instances. Instead it will add the correct
information to connect them. We need to create another class, in this
case it is called EALinkerService. It extends
*fr.liglab.adele.cilia.runtime.CiliaBindingServiceImpl* and implements
*fr.liglab.adele.cilia.runtime.CiliaBindingService*. We place this class
on the package *fr.liglab.adele.cilia.components.binding.ea*. And the
only method we need to implement is called *getProperties*

    package fr.liglab.adele.cilia.components.binding.ea;
    
    import java.util.Dictionary;
    import java.util.Hashtable;
    
    import fr.liglab.adele.cilia.model.Binding;
    import fr.liglab.adele.cilia.runtime.CiliaBindingService;
    import fr.liglab.adele.cilia.runtime.CiliaBindingServiceImpl;
    
    public class EALinkerService extends CiliaBindingServiceImpl implements
    CiliaBindingService {
      public Dictionary getProperties(Dictionary collectorProperties,
        Dictionary senderProperties, Binding bindingInfo) {
        Dictionary properties = new Hashtable();
        
        //with the information on bindingInfo we generate a topic property.
        String topic = generateTopic(bindingInfo); 
        
        /*Set the properties that will be injected on the sender/collector respectively*/
        collectorProperties.put("topic", topic);
        senderProperties.put("topic", topic);
        
        /*We add the corresponding properties to a global properties container*/
        properties.put(CILIA_COLLECTOR_PROPERTIES, collectorProperties);
        properties.put(CILIA_SENDER_PROPERTIES, senderProperties);
        return properties;
      }
    }


The topic generation could be as follows:

      private String generateTopic(Binding b) {
        StringBuffer topic = new StringBuffer();
        /**
        *The topic is as follows chainId/mediatorSourceId/mediatorTargetId
        */
        topic.append(b.getChain().getId());
        topic.append("/");
        topic.append(b.getSourceMediator().getId());
        topic.append("/");
        topic.append(b.getTargetMediator().getId());
        return topic.toString();
      }


The framework will create the sender and collector instance with the
given information.

Now, we need to specify the new linker on the *metadata.xml*

    <linker classname="fr.liglab.adele.cilia.components.binding.ea.EALinkerService" name="event-admin">
      <collector name="ea-collector" />
      <sender name="ea-sender"  />
    </linker>


Sample Application
------------------

And that's all. Now we can use the new linker on any mediation chain.
For example, to use this linker on the Hello World, we modify the
DSCilia file as follows:

    <cilia>
      <chain id="HelloWorldChain">
     
        <!-- Adapters instances definition -->
        <adapters>
          <adapter-instance type="gui-adapter" id="entryAdapter" />
          <adapter-instance type="console-adapter" id="exitAdapter" />
        </adapters>
     
        <!-- Mediators instances definition -->
        <mediators>
          <mediator-instance type="HelloMediator" id="hello">
            <ports>
              <in-port name="in" />
              <out-port name="out" />
            </ports>
          </mediator-instance>
        </mediators>
     
        <!-- Bindings definition With Event Admin -->
        <bindings>
          <binding from="entryAdapter" to="hello:in" linker="event-admin" />
          <binding from="hello:out" to="exitAdapter" linker="event-admin" />
        </bindings>
     
      </chain>
    </cilia>



  [EA]: http://felix.apache.org/site/apache-felix-event-admin.html