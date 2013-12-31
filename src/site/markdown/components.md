A library with a set of components is ready to be used in the Cilia
framework. Here these components are described and its utilization
illustrated.

Schedulers
----------

### Immediate 

**Description :** The immediate-scheduler activates the mediator as soon
as one data is received, and triggers immediatly the processor.

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> namespace
</th>
<th> location
</th></tr>
<tr>
<td> immediate-scheduler
</td>
<td> fr.liglab.adele.cilia
</td>
<td> cilia-runtime
</td></tr></tbody></table>


**Example** of code declaring a mediator using the immediate-scheduler:



    <mediator-component name="MyMediator" namespace="fr.liglab.adele.cilia.test">
        <scheduler name="immediate-scheduler" namespace="fr.liglab.adele.cilia"/>
        <processor name="MyProcessor" namespace="fr.liglab.adele.cilia.test"/>
        <dispatcher name="MyDispatcher" namespace="fr.liglab.adele.cilia.test"/>
    </mediator-component>


### Periodic

**Description :** The periodic scheduler activates periodically the
mediator, with a period defined by the user in properties, and after a
first delay defined by the user in properties.

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> namespace
</th>
<th> location
</th></tr>
<tr>
<td> periodic-scheduler
</td>
<td> fr.liglab.adele.cilia
</td>
<td> cilia-runtime
</td></tr></tbody></table>

<center>Properties</center>


<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> delay
</td>
<td> 3000
</td>
<td> time to wait to trigger for the first time.
</td></tr>
<tr>
<td> period
</td>
<td> 3000
</td>
<td> time between processing tasks
</td></tr></tbody></table>


**Example** of code declaring a mediator using the periodic-scheduler:


    <mediator-component name="MyMediator" namespace="fr.liglab.adele.cilia.test">
     <ports>
       <in-port name="unique" type="XML" />
       <out-port name="unique" type="XML" />
     </ports>
     <scheduler name="periodic-scheduler" namespace="fr.liglab.adele.cilia"/>
     <processor name="MyProcessor" namespace="fr.liglab.adele.cilia.test"/>
     <dispatcher name="MyDispatcher" namespace="fr.liglab.adele.cilia.test"/>
    </mediator-component>


**Example** of code instantiating this mediator with a period and a
delay of 10 seconds :


      <mediator-instance id="mediatorId" type="MyMediator">
        <scheduler>           
           <property name="period" value="10000"/>
           <property name="delay" value="10000"/>
        </scheduler>
      </mediator-instance>




### Counter

**Description :** The counter-scheduler watches the data's content, and
decides whether this data counts or not. When the counter is valid under
a certain condition, the mediator is activated, and the processor
triggered.

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> namespace
</th>
<th> location
</th></tr>
<tr>
<td> counter-scheduler
</td>
<td> fr.liglab.adele.cilia
</td>
<td> cilia-runtime
</td></tr></tbody></table>

<center>Properties</center>


<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> count (Map)
</td>
<td> ∅
</td>
<td> A list of counting expressions to be applied to the collected data.
</td></tr>
<tr>
<td> correlation (String)
</td>
<td> (*)
</td>
<td> An LDAP based expression to identify correlated messages.
</td></tr>
<tr>
<td> condition (String)
</td>
<td> ∅
</td>
<td> LDAP expression to determine when to trigger processing, the condition will be applied to the resulting counts.
</td></tr></tbody></table>


**Example** of code declaring a mediator using the counter-scheduler:


    <mediator-component name="MyMediator" namespace="fr.liglab.adele.cilia.test">
        <ports>
          <in-port name="entry-port" />
          <out-port name="exit-port" />
        </ports>
       <scheduler name="counter-scheduler" namespace="fr.liglab.adele.cilia"/>
       <processor name="MyProcessor" namespace="fr.liglab.adele.cilia.test"/>
       <dispatcher name="MyDispatcher" namespace="fr.liglab.adele.cilia.test"/>
    </mediator-component>


**Example** of code instantiating this mediator. The processig will be
trigger when there exists one data which data.name is dog, and when two
data have their data.name as cat.


      <mediator-instance id="mediatorId" type="MyMediator">
        <scheduler>           
           <property name="correlation" value="(*)"/>
           <property name="count">
               <item key="dogs" value="(data.name=dog)"/>
               <item key="cats" value="(data.name=cat)" />
           </property>
           <property name="condition" value="(&(dogs=1)(cats=2))"/>
        </scheduler>
      </mediator-instance>


### Correlation

**Description :** This scheduler wait to some correlated correlated
messages arrive to be trigger the processing. Those messages must
contain a \$SPLIT.TOTAL variable in order to determine when the
correlated messages are complete.

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> namespace
</th>
<th> location
</th></tr>
<tr>
<td> correlation-scheduler
</td>
<td> fr.liglab.adele.cilia
</td>
<td> cilia-runtime
</td></tr></tbody></table>

<center>Properties</center>

<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> correlation
</td>
<td> "($SPLIT.ID)"
</td>
<td> an ldap based expression to identify correlated messages.
</td></tr></tbody></table>

**Example** of code declaring a mediator using the
correlation-scheduler:

    <mediator-component name="MyMediator" namespace="fr.liglab.adele.cilia.test">
        <ports>
          <in-port name="entry-port" />
          <out-port name="exit-port" />
        </ports>
       <scheduler name="correlation-scheduler" namespace="fr.liglab.adele.cilia"/>
       <processor name="MyProcessor" namespace="fr.liglab.adele.cilia.test"/>
       <dispatcher name="MyDispatcher" namespace="fr.liglab.adele.cilia.test"/>
    </mediator-component>

**Example** of code instantiating this mediator:

      <mediator-instance id="mediatorId" type="MyMediator">
        <scheduler>           
           <property name="correlation" value="value"/>
        </scheduler>
      </mediator-instance>
      
      
**NOTE: This schedulers works with an splitter who add the SPLIT.TOTAL variable to each message.**
      
      
      
      
      
Dispatchers
-----------

Dispatchers are the components which decide how to dispatch data from
their mediators.

### Multicast

**Description :** The multicast-dispatcher dispatches any data that it
receives to all the bound elements, mediators or adapters.

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> namespace
</th>
<th> location
</th></tr>
<tr>
<td> multicast-dispatcher
</td>
<td> fr.liglab.adele.cilia
</td>
<td> cilia-runtime
</td></tr></tbody></table>

**Example** of code declaring a mediator using the multicast-dispatcher:

    <mediator-component name="MyMediator" namespace="fr.liglab.adele.cilia.test">
        <ports>
          <in-port name="entry-port" />
          <out-port name="exit-port1" />
          <out-port name="exit-port2" />
        </ports>
       <scheduler name="MyScheduler" namespace="fr.liglab.adele.cilia.test"/>
       <processor name="MyProcessor" namespace="fr.liglab.adele.cilia.test"/>
       <dispatcher name="multicast-dispatcher" namespace="fr.liglab.adele.cilia"/>
    </mediator-component>
      
      
      
### Content-based

**Description :** The content-based-dispatcher dispatches the data in a
way depending on the data's content. The user defines conditions, and
ports associated to one condition, through a Map where keys are the
conditions, and where the values are the ports' identifiers. If the
data's content is valid for a condition, then the data is sent to the
port that is associated with this condition.

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> namespace
</th>
<th> location
</th></tr>
<tr>
<td> content-based-dispatcher
</td>
<td> fr.liglab.adele.cilia
</td>
<td> cilia-runtime
</td></tr></tbody></table>

<center>Properties</center>


<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> language (String)
</td>
<td> ldap
</td>
<td> Condition language to inspect the data content. Available options: LDAP, XPATH
</td></tr>
<tr>
<td> conditions (Map)
</td>
<td> ∅
</td>
<td>  Pair of key,value. Keys are the conditions, and the value is the port to send the data when the condition is valid.
</td></tr></tbody></table>

**Example** of code declaring a mediator using the
content-based-dispatcher:

    <mediator-component name="MyMediator" namespace="fr.liglab.adele.cilia.test">
        <ports>
          <in-port name="entry-port" />
          <out-port name="exit-port1" />
          <out-port name="exit-port2" />
        </ports>
       <scheduler name="MyScheduler" namespace="fr.liglab.adele.cilia.test"/>
       <processor name="MyProcessor" namespace="fr.liglab.adele.cilia.test"/>
       <dispatcher name="content-based-dispatcher" namespace="fr.liglab.adele.cilia"/>
     </mediator-component>

**Example** of code instantiating this mediator. When the data has a
property value of data.name as cat, it send the data throw port
exit-port1. When the value is dog, it send it throw port exit-port2.

      <mediator-instance id="mediatorId" type="MyMediator">

        <dispatcher>           
           <property name="language" value="ldap"/>
           <property name="conditions">
              <item key="(data.name=cat)" value="exit-port1" />
              <item key="(data.name=dog)" value="exit-port2" />
           </property>
        </dispatcher>
      </mediator-instance>


Processors
----------

Processors are mediator elements in charge of performing modifications
or treatment to the received data.

### XSLT Transformer

**Description :** This processor take the incoming data as a String in
an XML format and perform some transformation based on a XSLT file.

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> namespace
</th>
<th> location
</th></tr>
<tr>
<td> XsltTransformerProcessor
</td>
<td> fr.liglab.adele.cilia
</td>
<td> cilia-runtime
</td></tr></tbody></table>

<center>Properties</center>


<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> xslt-file
</td>
<td> ∅
</td>
<td> absolute path where the xslt file is located.
</td></tr></tbody></table>

**Example** of code declaring a mediator using the
XsltTransformerProcessor:

    <mediator-component name="MyMediator" namespace="fr.liglab.adele.cilia.test">
        <ports>
          <in-port name="entry-port" />
          <out-port name="exit-port" />
        </ports>
       <scheduler name="MyScheduler" namespace="fr.liglab.adele.cilia.test"/>
       <processor name="XsltTransformerProcessor" namespace="fr.liglab.adele.cilia"/>
       <dispatcher name="MyDispatcher" namespace="fr.liglab.adele.cilia.test"/>
     </mediator-component>


**Example** of code instantiating this mediator:

      <mediator-instance id="mediatorId" type="MyMediator">
        <processor>
           <property name="xslt-file" value="/home/torito/files/transformator.xslt"/>
        </processor>
      </mediator-instance>


### XML Splitter

**Description :** An XML splitter processor. This processor take the
incoming data as a String in an XML format and perform an split
operation using an XPATH pattern.

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> namespace
</th>
<th> location
</th></tr>
<tr>
<td> XmlSplitterProcessor
</td>
<td> fr.liglab.adele.cilia
</td>
<td> cilia-runtime
</td></tr></tbody></table>

<center>Properties</center>


<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> separator
</td>
<td> ∅
</td>
<td> An XPATH expression used to split xml.
</td></tr></tbody></table>

**Example** of code declaring a mediator using the XmlSplitterProcessor:

    <mediator-component name="MyMediator" namespace="fr.liglab.adele.cilia.test">
        <ports>
          <in-port name="entry-port" />
          <out-port name="exit-port" />
        </ports>
       <scheduler name="MyScheduler" namespace="fr.liglab.adele.cilia.test"/>
       <processor name="XmlSplitterProcessor" namespace="fr.liglab.adele.cilia"/>
       <dispatcher name="MyDispatcher" namespace="fr.liglab.adele.cilia.test"/>
    </mediator-component>


**Example** of code instantiating this mediator:

      <mediator-instance id="mediatorId" type="MyMediator">
        <processor>
           <property name="separator" value="//suiviconso-requete"/>
        </processor>
      </mediator-instance>



### String Splitter

**Description :** An splitter processor. This processor take the content
of each data as a String and perform a split operation to it. The
resulting is a set of Data, each of one containing each chunk of data.

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> namespace
</th>
<th> location
</th></tr>
<tr>
<td> StringSplitterProcessor
</td>
<td> fr.liglab.adele.cilia
</td>
<td> cilia-runtime
</td></tr></tbody></table>

<center>Properties</center>


<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> separator
</td>
<td> ∅
</td>
<td> A regex expression used to split string located in the data content.
</td></tr></tbody></table>

**Example** of code declaring a mediator using the
StringSplitterProcessor:

    <mediator-component name="MyMediator" namespace="fr.liglab.adele.cilia.test">
        <ports>
          <in-port name="entry-port" />
          <out-port name="exit-port" />
        </ports>
       <scheduler name="MyScheduler" namespace="fr.liglab.adele.cilia.test"/>
       <processor name="StringSplitterProcessor" namespace="fr.liglab.adele.cilia"/>
       <dispatcher name="MyDispatcher" namespace="fr.liglab.adele.cilia.test"/>
    </mediator-component>


**Example** of code instantiating this mediator. This instance will
split each message using ":" as a separator token.

      <mediator-instance id="mediatorId" type="MyMediator">
        <processor>
           <property name="separator" value=":"/>
        </processor>
      </mediator-instance>


### Simple Aggregator

**Description :** This processor will receive a set of Data, and will
construct a new Data containing as content a List (java.util.List) of
the received set of data.

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> namespace
</th>
<th> location
</th></tr>
<tr>
<td> AggregatorProcessor
</td>
<td> fr.liglab.adele.cilia</td>
<td>cilia-runtime</td>
</tr></tbody></table>

Basically, this processor will perform the following operation.

    return new Data(list, "aggregated-data");

Where the object list, is an instance of java.util.List containing a set
of data objects. The string "aggregated-data" is the name of the data.

**Example** of code declaring a mediator using the AggregatorProcessor:

    <mediator-component name="MyMediator" namespace="fr.liglab.adele.cilia.test">
        <scheduler name="MyScheduler" namespace="fr.liglab.adele.cilia.test"/>
        <processor name="AggregatorProcessor" namespace="fr.liglab.adele.cilia"/>
        <dispatcher name="MyDispatcher" namespace="fr.liglab.adele.cilia.test"/>
    </mediator-component>


### Semantic Translator

**Description :** This processor replace words on the data content based
on a given dictionary.
<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> namespace
</th>
<th> location
</th></tr>
<tr>
<td> SemanticTranslatorProcessor
</td>
<td> fr.liglab.adele.cilia
</td>
<td> cilia-runtime
</td></tr></tbody></table>

<center>Properties</center>

<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> dictionary (Map)
</td>
<td> ∅
</td>
<td> list of words to be translated, the key is the word to translate, and the value is the replacing word.
</td></tr></tbody></table>

**Example** of code declaring a mediator using the
SemanticTranslatorProcessor:

    <mediator-component name="MyMediator" namespace="fr.liglab.adele.cilia.test">
        <ports>
            <in-port name="entry-port" />
            <out-port name="exit-port" />
        </ports>
       <scheduler name="MyScheduler" namespace="fr.liglab.adele.cilia.test"/>
       <processor name="SemanticTranslatorProcessor" namespace="fr.liglab.adele.cithlia"/>
       <dispatcher name="MyDispatcher" namespace="fr.liglab.adele.cilia.test"/>
    </mediator-component>


**Example** of code instantiating this mediator. This processor will
replace the word *dog* for *Canidae* and the word *cat* for *Felidae*.

      <mediator-instance id="mediatorId" type="MyMediator">
        <processor>
           <property name="dictionary">
              <item key="dog" value="Canidae"/>
              <item key="cat" value="Felidae"/>
           </property>
        </processor>
      </mediator-instance>


Linkers
-------

Linkers are in charge of binding two mediation components.

### Direct

**Description :** The **direct-binding** is the default when binding two
mediator components.

**Example** of code declaring a binding using a direct binding:

    <bindings>
        <binding from="source-meduatir:exit" to="target-mediator:entry" />
    </bindings>


### Event Admin

The [Event Admin][] Specification, part of the OSGi Compendium
specification, defines a general inter-bundle communication mechanism.
The communication conforms to the popular publish/subscribe paradigm.

**Description :** The **event-admin** linker is in charge of binding two
mediator components using the Event Admin Service.

**Example** of code declaring a binding using the event admin binding:

    <bindings>
        <binding from="source-meduatir:exit" to="target-mediator:entry" linker="event-admin"/>
    </bindings>


**Example** of code using custom properties:

    <bindings>
        <binding from="source-mediator:exit" to="target-mediator:entry" linker="event-admin">
            <property name="topic" value="fromSource/to/targetMediator"/>
        </binding>
    </bindings>


Adapters
--------

Adapters are the mediation components in charge of the communication
with external entities (applications, services, communication protocols,
middlewares,...). Most of the linkers had also an equivalent as adapter,
but is not always applied in the inverse. Here are some of the available
adapters for the current cilia version.

### Connecting Adapters

Adapters in this section is presented the current available adapters and
its properties to be used in mediation chains. One important issue to
know about these adapters is that they had only one *cilia port* to
communicate with other mediation components. This port is named
***unique*** and it receives any kind of Data. So, in order to bind an
adapter, we must do as follow:

    <adapters>
        <adapter-instance type="event-admin-in-adapter" id="ea-in-adapter">
            <property name="topic" value="topic/to/listen"/>
        </adapter-instance>
    <adapters>
        <adapter-instance type="console-adapter" id="console-adapter" />
    </adapters>
   
    <bindings>
        <binding from="ea-in-adapter:unique" to"console-adapter:unique" />
    </bindings>


### Console Adapter

The console adapter allows print the received Data to the shell. It does
a simple System.out.println call.

#### OUTPUT Adapter

**Description :** Print the Data to the console shell

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> location
</th></tr>
<tr>
<td> console-adapter
</td>
<td> cilia-runtime
</td></tr></tbody></table>

<center>Properties</center>

<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> console.detail
</td>
<td> false
</td>
<td> If it will print all the data or only its content.
</td></tr>
<tr>
<td> console.header
</td>
<td> ∅
</td>
<td> A specific header to print before the Data.
</td></tr></tbody></table>

**Example** of code declaring a **INPUT Event Admin adapter**:

    <adapters>
       <adapter-instance type="console-adapter" id="console-adapter">
         <property name="console.detail" value="true"/>
         <property name="console.header" value="[Chain-1]:"/>
       </adapter-instance>
    </adapters>


### Event Admin Adapter

Event Admin could be used to link two mediation components but also to
communicate a mediation chain with an external application using Event
Admin. This allows, for example, to have a loosely coupled architecture.

#### INPUT Adapter

**Description :** The **input event-admin** adapter is in charge of
receiving information of an external service/component running in the
same gateway, using the Event Admin Service.

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> location
</th></tr>
<tr>
<td> event-admin
</td>
<td> <a rel="nofollow" class="external text" href="http://repository-cilia.forge.cloudbees.com/snapshot/fr/liglab/adele/cilia/ea-adapter/">ea-adapter</a>
</td></tr></tbody></table>

<center>Properties</center>

<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> topic
</td>
<td> ∅
</td>
<td> topic
</td></tr></tbody></table>

**Example** of code declaring a **INPUT Event Admin adapter**:

    <adapters>
       <adapter-instance type="event-admin-in-adapter" id="ea-in-adapter">
         <property name="topic" value="topic/to/listen"/>
       </adapter-instance>
    </adapters>


#### OUTPUT Adapter

**Description :** The **output event-admin** adapter is in charge of
sending information to an external service/component running in the same
gateway, using the Event Admin Service.

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> location
</th></tr>
<tr>
<td> event-admin
</td>
<td> <a rel="nofollow" class="external text" href="http://repository-cilia.forge.cloudbees.com/release/fr/liglab/adele/cilia/ea-adapter/">ea-adapter</a>
</td></tr></tbody></table>

<center>Properties</center>


<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> topic
</td>
<td> ∅
</td>
<td> topic
</td></tr></tbody></table>


**Example** of code declaring a **OUTPUT Event Admin adapter**:

    <adapters>
       <adapter-instance type="event-admin-out-adapter" id="ea-out-adapter">
         <property name="topic" value="topic/to/listen"/>
       </adapter-instance>
    </adapters>
 

### JMS/Joram (JMS 1.1) Adapter

Joram is an Open source implementation of JMS (Java Message System) 1.1
Cilia provides INPUT and OUTPUT adapters to communicate with
applications or systems using Joram as a Message Oriented Middleware
(MOM). Also, there is possible to communicate two distributed mediation
chains using the JMS adapters.


#### INPUT Adapter

**Description :** The **input JMS** adapter is in charge of receiving
information of the JMS server on an specified topic

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> location
</th></tr>
<tr>
<td> JMS-in-adapter
</td>
<td> <a rel="nofollow" class="external text" href="http://repository-cilia.forge.cloudbees.com/release/fr/liglab/adele/cilia/jms-adapter/">jms-adapter</a>
</td></tr></tbody></table>

<center>Properties</center>

<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> jms.topic
</td>
<td> ∅
</td>
<td> The JMS topic to receive event messages.
</td></tr>
<tr>
<td> jms.host
</td>
<td> localhost
</td>
<td> The hostname where the JMS server is located.
</td></tr>
<tr>
<td> jms.port
</td>
<td> 16010
</td>
<td> The port number to connect to the JMS server.
</td></tr>
<tr>
<td> jms.login
</td>
<td> root
</td>
<td> The user name to connect to the JMS server.
</td></tr>
<tr>
<td> jms.password
</td>
<td> root
</td>
<td> The password to connect to the JMS server.
</td></tr></tbody></table>

**Example** of code declaring a **INPUT JMS adapter**:

    <adapters>
       <adapter-instance type="JMS-in-adapter" id="jms-in-adapter-1">
         <property name="jms.topic" value="topic$to$listen"/>
         <property name="jms.host" value="129.88.51.194"/>
         <property name="jms.login" value="garciai"/>
         <property name="jms.password" value="12375"/>
       </adapter-instance>
    </adapters>


#### OUTPUT Adapter

**Description :** The **output JMS** adapter is in charge of sending
information to the JMS server using an specified topic

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> location
</th></tr>
<tr>
<td> JMS-out-adapter
</td>
<td> <a rel="nofollow" class="external text" href="http://repository-cilia.forge.cloudbees.com/release/fr/liglab/adele/cilia/jms-adapter/">jms-adapter</a>
</td></tr></tbody></table>

<center>Properties</center>

<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> jms.topic
</td>
<td> ∅
</td>
<td> The JMS topic to sent event messages.
</td></tr>
<tr>
<td> jms.host
</td>
<td> localhost
</td>
<td> The hostname where the JMS server is located.
</td></tr>
<tr>
<td> jms.port
</td>
<td> 16010
</td>
<td> The port number to connect to the JMS server.
</td></tr>
<tr>
<td> jms.login
</td>
<td> root
</td>
<td> The user name to connect to the JMS server.
</td></tr>
<tr>
<td> jms.password
</td>
<td> root
</td>
<td> The password to connect to the JMS server.
</td></tr></tbody></table>

**Example** of code declaring a **OUTPUT JMS adapter**:


    <adapters>
       <adapter-instance type="JMS-out-adapter" id="jms-out-adapter-1">
         <property name="jms.topic" value="topic$to$listen"/>
         <property name="jms.host" value="129.88.51.194"/>
         <property name="jms.login" value="garciai"/>
         <property name="jms.password" value="12375"/>
       </adapter-instance>
    </adapters>

    
### JMS/Joram (JMS 2.0) Adapter

Joram is an Open source implementation of JMS (Java Message System) 2.0
Cilia provides INPUT and OUTPUT adapters to communicate with
applications or systems using Joram as a Message Oriented Middleware
(MOM). Also, there is possible to communicate two distributed mediation
chains using the JMS adapters.


#### INPUT Adapter

**Description :** The **input JMS** adapter is in charge of receiving
information of the JMS server on an specified topic

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> location
</th></tr>
<tr>
<td> JMS2-in-adapter
</td>
<td> <a rel="nofollow" class="external text" href="http://repository-cilia.forge.cloudbees.com/release/fr/liglab/adele/cilia/jms2-adapter/">jms2-adapter</a>
</td></tr></tbody></table>

<center>Properties</center>

<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> jms.topic
</td>
<td> ∅
</td>
<td> The JMS topic to receive event messages.
</td></tr>
<tr>
<td> jms.host
</td>
<td> localhost
</td>
<td> The hostname where the JMS server is located.
</td></tr>
<tr>
<td> jms.port
</td>
<td> 16010
</td>
<td> The port number to connect to the JMS server.
</td></tr>
<tr>
<td> jms.login
</td>
<td> root
</td>
<td> The user name to connect to the JMS server.
</td></tr>
<tr>
<td> jms.password
</td>
<td> root
</td>
<td> The password to connect to the JMS server.
</td></tr></tbody></table>

**Example** of code declaring a **INPUT JMS2 adapter**:

    <adapters>
       <adapter-instance type="JMS2-in-adapter" id="jms2-in-adapter-1">
         <property name="jms.topic" value="topic$to$listen"/>
         <property name="jms.host" value="129.88.51.194"/>
         <property name="jms.login" value="garciai"/>
         <property name="jms.password" value="12375"/>
       </adapter-instance>
    </adapters>


#### OUTPUT Adapter

**Description :** The **output JMS2** adapter is in charge of sending
information to the JMS server using an specified topic

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> location
</th></tr>
<tr>
<td> JMS2-out-adapter
</td>
<td> <a rel="nofollow" class="external text" href="http://repository-cilia.forge.cloudbees.com/release/fr/liglab/adele/cilia/jms2-adapter/">jms2-adapter</a>
</td></tr></tbody></table>

<center>Properties</center>

<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> jms.topic
</td>
<td> ∅
</td>
<td> The JMS topic to sent event messages.
</td></tr>
<tr>
<td> jms.host
</td>
<td> localhost
</td>
<td> The hostname where the JMS server is located.
</td></tr>
<tr>
<td> jms.port
</td>
<td> 16010
</td>
<td> The port number to connect to the JMS server.
</td></tr>
<tr>
<td> jms.login
</td>
<td> root
</td>
<td> The user name to connect to the JMS server.
</td></tr>
<tr>
<td> jms.password
</td>
<td> root
</td>
<td> The password to connect to the JMS server.
</td></tr></tbody></table>

**Example** of code declaring a **OUTPUT JMS adapter**:


    <adapters>
       <adapter-instance type="JMS2-out-adapter" id="jms2-out-adapter-1">
         <property name="jms.topic" value="topic$to$listen"/>
         <property name="jms.host" value="129.88.51.194"/>
         <property name="jms.login" value="garciai"/>
         <property name="jms.password" value="12375"/>
       </adapter-instance>
    </adapters>
    


### Simple TCP Adapter

TCP Adapters can be used to communicate in a point-to-point way with
external application or systems. Also, could be used to communicate
distributed mediation chains.

#### INPUT Adapter

**Description :** The **input tcp** adapter is in charge of receiving
information of an external application, using an specified port number.
The adapter will open the port, so it must be had the rights to do it.

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> location
</th></tr>
<tr>
<td> tcp-in-adapter
</td>
<td> <a rel="nofollow" class="external text" href="http://repository-cilia.forge.cloudbees.com/snapshot/fr/liglab/adele/cilia/tcp-adapter/">tcp-adapter</a>
</td></tr></tbody></table>

<center>Properties</center>


<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> port
</td>
<td> 9999
</td>
<td> The port where the adapter will listen to new connections.
</td></tr></tbody></table>

**Example** of code declaring a **INPUT TCP adapter**:

    <adapters>
       <adapter-instance type="tcp-in-adapter" id="tcp-in-adapter-1">
         <property name="port" value="9878" />
       </adapter-instance>
    </adapters>


#### OUTPUT Adapter

**Description :** The **output TCP** adapter is in charge of sending
information to an external application listening on the specified port.

<table class="wikitable gauche" style="text-align:center; width:90%; background-color:#cecece">
<tbody><tr>
<th> name
</th>
<th> location
</th></tr>
<tr>
<td> tcp-out-adapter
</td>
<td> <a rel="nofollow" class="external text" href="http://repository-cilia.forge.cloudbees.com/release/fr/liglab/adele/cilia/tcp-adapter/">tcp-adapter</a>
</td></tr></tbody></table>

<center>Properties</center>
<table style="width: 90%; text-align:center; background-color:#cecece;" border="3">
<tbody>
<tr>
<th> name
</th>
<th> default value
</th>
<th> description
</th></tr>
<tr>
<td> hostname
</td>
<td> localhost
</td>
<td> The server name to send the data.
</td></tr>
<tr>
<td> port
</td>
<td> 9999
</td>
<td> The server port to send the data.
</td></tr></tbody></table>


**Example** of code declaring a **OUTPUT TCP adapter**:

    <adapters>
       <adapter-instance type="tcp-out-adapter" id="tcp-out-adapter">
         <property name="port" value="9878"/>
         <property name="hostname" value="129.88.51.194"/>
       </adapter-instance>
    </adapters>


[Event Admin]: http://felix.apache.org/site/apache-felix-event-admin.html
  
