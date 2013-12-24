Creating Adapters
=================

As we have presented in the Cilia Overview adapters are in charge of
connect a Cilia chain with the external world. In-adapters are in charge
of collect data and then bring them to the Cilia chain, on the other
hand out-adapters send the data manipulated by the chain to the external
world.

In this section we will present how to define new adapters in the Cilia
framework.


A Sensor In-Adapter
-------------------

This example presents an in-adapter that collects data from a presence
detector sensor.

### Presence Sensor Interface

The sensor software is encapsulated into an OSGi service, the interface
of this service is as follows.

    package fr.liglab.adele.cilia.sample.sensor;
    
    public interface PresenceDetector {
          
       /**
        * Returns the Presence Detector ID
        * @return
        */
       public String getId();
       
       /**
        * Returns the Presence Detector Location
        * @return
        */
       public String getLocalisation();
       
       /**
        * return if a presence has been detected
        * @return
        */
       public boolean getSensedPresence();
    }


### Collector Implementation

In order to create a new in-adapter a **Collector** component must be
implemented, the collector is in charge of get data from the Sensor
service. To implement a Collector in Cilia developers must to extend the
abstract class *fr.liglab.adele.cilia.framework.AbstractCollector* and
invoke its method *notifyDataArrival* to indicate that data to be
processed is ready.

Collectors components are passive, a mechanism to trigger the data
collection must be established, in this example we will implement the
method (*start*) to trigger the collector when the component is ready to
execute.

In our implementation when the start method is invoked by the Cilia
framework, the method uses the PresenceDetector service to get the
required data, then it encapsulates the data into the right format, and
finally it invokes the notifyDataArrival method.

    package fr.liglab.adele.cilia.sample.collector;
    
    import fr.liglab.adele.cilia.Data;
    import fr.liglab.adele.cilia.framework.AbstractCollector;
    import fr.liglab.adele.cilia.sample.sensor.PresenceDetector;
    
    
    public class SensorCollector extends AbstractCollector {
    
       /** Sensor field  */
       PresenceDetector sensor;
       
       /**
        * Method to trigger the collection of data
        */
       public void start() {
          String sensorData = sensor.getId() + sensor.getLocalisation() + sensor.getSensedPresence();
          Data data = new Data(sensorData);  
          
          //This method must be invoked
          notifyDataArrival(data);
       }
    }


The collector will use a service of type sensor (*sensor* field), in the
DSL definition this requirement must be specified (*requires* tag). In
addition, the callback method (*start*) has been defined, the Cilia
framework will invoke this method when the collector is ready to
execute. This definition must be included into the *metadata.xml* file.
The definition of Collector in Cilia DSL will be:

      <collector classname="fr.liglab.adele.cilia.sample.collector.SensorCollector" name="SensorCollector">
        <requires field="sensor" />
        <callback transition="validate" method="start" />
      </collector>

### Adapter Definition

Once the collector has been defined we will define the in-adapter using
the Cilia DSL language into the *metadata.xml* file.

      <adapter name="PresenceDetectorAdapter" pattern="in-only">
        <collector type="SensorCollector" />
      </adapter>

### Maven Dependencies

In order to build this example, the Maven POM file must defined all
dependencies. In our case three dependencies must be specified, to the
Cilia core, Cilia Compendium and finally to the artifact including the
interface of presence sensor. Dependencies section of POM file should
look like this:

      <dependencies>
        <dependency>
          <groupId>fr.liglab.adele.cilia</groupId>
          <artifactId>cilia-core</artifactId>
          <version>${project.version}</version>
        </dependency>
        <dependency>
          <groupId>fr.liglab.adele.cilia</groupId>
          <artifactId>cilia-runtime</artifactId>
          <version>${project.version}</version>
        </dependency>
        <dependency>
          <groupId>fr.liglab.adele.cilia</groupId>
          <artifactId>presence-detector</artifactId>
          <version>0.0.1-SNAPSHOT</version>
        </dependency>
      </dependencies>


### Sample Application

This application will use the PresenceDetector adapter, we reuse the
HelloMediator mediator presented in the first lesson of getting started
tutorial and also the console adapter. To execute the application you
have to deploy the bundle containing the PresenceDetector interface and
one bundle containing an implementation of the service (one default
implementation is provided). To see the example running stop the service
implementation (stop bundle) and then restart it, a message like this is
show in the console.

![ center ](Execute-in-adapter-1.png  " center ")

The sample application code is:

    <cilia>
      <chain id="TestAdapters-Chain1">
     
        <!-- Adapters instances definition -->
        <adapters>
          <adapter-instance type="PresenceDetectorAdapter" id="presenceDectector" />
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
     
        <!-- Bindings definition -->
        <bindings>
          <binding from="presenceDectector" to="hello:in" />
          <binding from="hello:out" to="exitAdapter" />
        </bindings>
     
      </chain>
    </cilia>


A Periodic Sensor In-Adapter
----------------------------

In the second example we will implement an in-adapter that collects data
in a periodical way from the presence detector sensor.

### Collector Implementation

In this example we will extend the class
*fr.liglab.adele.cilia.components.collectors.AbstractPullCollector* that
it-self extends the base class (AbstractCollector) and implements a
periodic behavior. The method pullData is invoked periodically.

The collector class looks like:

    package fr.liglab.adele.cilia.sample.collector;
    
    import java.io.IOException;
    import java.util.Collections;
    import java.util.List;
    
    import fr.liglab.adele.cilia.Data;
    import fr.liglab.adele.cilia.components.collectors.AbstractPullCollector;
    import fr.liglab.adele.cilia.sample.sensor.PresenceDetector;
    
    
    public class PeriodicSensorCollector extends AbstractPullCollector {
    
       /** Sensor field  */
       PresenceDetector sensor;
       
       /* (non-Javadoc)
        * @see fr.liglab.adele.cilia.components.collectors.AbstractPullCollector#pullData()
        */
       protected List pullData() throws IOException {
          String sensorData = sensor.getId() + "-" + sensor.getLocalisation() + "-" + sensor.getSensedPresence();
          Data data = new Data(sensorData);      
          return Collections.singletonList(data);
       }
       
       public void delay(long iDelay) {
          super.delay(iDelay);
       }
       
       public void period(long lperiod) {
          super.period(lperiod);
       }
       
       public void start() {
          super.start();
       }
       
       public void stop() {
          super.stop();
       }
    }


The definition of this collector includes two properties:

-   delay : time to start data collecting (ms),
-   period: period of data collecting (ms).

The definition of Collector in Cilia DSL will be:

      <collector classname="fr.liglab.adele.cilia.sample.collector.PeriodicSensorCollector" name="PeriodicCollector">
        <requires field="sensor" />
        <properties>
          <property name="delay" method="delay" />
          <property name="period" method="period" />
        </properties>
        <callback transition="validate" method="start" />
        <callback transition="invalidate" method="stop" />
      </collector>

### Adapter Definition

Once the collector has been defined we will define the in-adapter using
the Cilia DSL language into the metadata.xml.

      <adapter name="PeriodicPresenceDetectorAdapter" pattern="in-only">
        <collector type="PeriodicCollector" />
      </adapter>


### Sample Application

We have modified the sample application to use the
PeriodicPresenceDetectorAdapter, the definiton is as follows:

    <cilia>
      <chain id="TestAdapters-Chain2">
     
        <!-- Adapters instances definition -->
        <adapters>
          <adapter-instance type="PeriodicPresenceDetectorAdapter" id="presenceDectector">
          <property name="delay" value="500" />
          <property name="period" value="1000" />         
         </adapter-instance>
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
     
        <!-- Bindings definition -->
        <bindings>
          <binding from="presenceDectector" to="hello:in" />
          <binding from="hello:out" to="exitAdapter" />
        </bindings>
     
      </chain>
    </cilia>


An Out Adapter
--------------

We will present how create an out-adapter, more precisely the
console-adapter that has been used in many examples. This adapter prints
in console a message with the received data, a header can be optionally
used to print the message.

### Sender Implementation

Every out-adapter in Cilia uses a Sender Component. Each Sender
component has to implement the interface
fr.liglab.adele.cilia.framework.ISender. The implementation code of our
ConsoleSender is:

    package fr.liglab.adele.cilia.components.senders;
    
    import org.osgi.framework.BundleContext;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    
    import fr.liglab.adele.cilia.Data;
    import fr.liglab.adele.cilia.framework.ISender;
    
    
    public class ConsoleSender implements ISender {
    
        boolean detail;
    
        String header ;
       
         private static Logger log =LoggerFactory.getLogger("cilia.ipojo.compendium");
          
         public boolean send(Data data) {
       if (data != null) {
          if (detail == true) {
          System.out.println(header + "\n" + data.toString());
          } else {
                    System.out.println(header + "\n" + String.valueOf(data.getContent()));
          }
       } else  {
          log.warn(header + "\n" + "[INFO] ConsoleSender : data=null");
       }
       return false;
        }
    }


The definition of this sender into the metadata.xml is:

      <sender classname="fr.liglab.adele.cilia.components.senders.ConsoleSender"  name="console-sender" architecture="false">
        <properties>
          <property name="console.detail" field="detail" value="false" />
          <property name="console.header" field="header" value="" />
        </properties>
      </sender>

### Adapter Definition

Once the sender has been defined we will define the out-adapter using
the Cilia DSL language into the metadata.xml.

       <adapter name="console-adapter" pattern="out-only">
         <sender type="console-sender" />
       </adapter>
    
### Sample Application

We have modified the sample application to use the
PeriodicPresenceDetectorAdapter, the definiton is as follows:

    <cilia>
      <chain id="TestAdapters-Chain3">
     
        <!-- Adapters instances definition -->
        <adapters>
          <adapter-instance type="PeriodicPresenceDetectorAdapter" id="presenceDectector">
          <property name="delay" value="500" />
          <property name="period" value="1000" />         
         </adapter-instance>
          <adapter-instance type="console-adapter" id="exitAdapter" >
             <property name="console.detail" value="true" />
             <property name="console.header" value="My-Header" />
          </adapter-instance>  
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
     
        <!-- Bindings definition -->
        <bindings>
          <binding from="presenceDectector" to="hello:in" />
          <binding from="hello:out" to="exitAdapter" />
        </bindings>
     
      </chain>
    </cilia>

