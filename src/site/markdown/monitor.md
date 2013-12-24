Monitoring at a glance
======================

The Monitoring can be controlled dynamically, and can be activated and
deactivated on user demand. The monitoring is based on two concepts
State variables , and knowledge of the mediation chain at run time.

State variable are part of the monitoring and collect specific numeric
informations on the dynamic of the mediation chain. The Monitoring
provides a configurable

knowledge is fully configurable. It fetch events from the cilia
framework and easy the access of mediation chain and constituants in
execution by topology access.

State Variables
---------------

The Monitoring relies on the notion of state variables (inspired from
the control theory). Their values are keepts in circular list, providing
records of past events.

A **state variable** are attached to the mediation chains and precisely
to mediators and adapters. Their values are called **measures** and are
timestamped at source level (when the measure is realized).

Alarm and warning can be defined on state variables. When an measure is
under *low* threshold or upper *high* threshold and warning is fired by
the Cilia Framework. When a measure is under *very low* and upper *very
high* threshold an alarm is fired.

List of **state variables** name defined by the Cilia Framework :

Phase Collect

-   *scheduler.count*: counts the number of incomming messages in the
    *scheduler*.
-   *scheduler.data*: stores the complete message 'Data' incomming in
    the *scheduler*.

Phase Process

-   *process.entry.count* : counts the number of incomming messages in
    the *processor*.
-   *process.entry.data* : stores the complete message 'Data' incomming
    in the *processor*.
-   *process.exit.count* : counts the number of outgoing messages the
    *processor*.
-   *process.exit.data* : stores the complete message 'Data' outcome
    from the *processor*.
-   *process.msg.treated*: counts the number of messages executed by the
    *processor*.
-   *processing.delay* : processing execution time.

Phase dispatch

-   *dispatch.count*: counts the number of outgoing messages the
    *dispacher*.
-   *dispatch.data* : stores the complete message 'Data' incomming in
    the *dispatcher*.
-   *dispatch.msg.treated* :counts the number of messages executed by
    the *dispatcher*.
-   message.history :

Link

-   *transmission.delay* : time between disptcher and scheduler (between
    mediators-adapters)

Events fired from POJOs - scheduler/processor/dispatcher

-   *fire.event* : stores events fired by the user code
    (scheduler/processor/dispatcher)
-   *fire.event.count* : counts the number of events fired by the
    mediator or adapter.

Events from the dependency manager

-   *service.arrival* : the mediator-adapter service dependency has
    injected a new service reference
-   *service.departure*: the mediator or adapter service dependency has
    removed the service reference used
-   *service.arrival.count*: counts the number of *service.arrival*
-   *service.departure.count*: counts the number of *service.departure*

Audits , variables accessed in read/write in POJOs
-scheduler/processor/dispatcher

-   field.set : variable Java accessed in write, the new value is
    stored.
-   field.set.count :counts the number of read access.
-   field.get :variable Java accessed in read, the value is stored.
-   field.get.count :counts the number of write access.

State variables compute a measure stored in a circular list. By adding a
condition storage, a data flow control is realized and configurable
either by XML or APIs. The data flow control , accelerates or reduces
the number of measures stored by unit of mediator/adaptor execution
time.

**Data flow control** is exprimed by using a LDAP filter and is applied
on the measure and/or the timestamp.

<table cellpadding="10" cellspacing="0" border="1">
<tbody><tr>
<th>KEYWORD
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>value.current
</td>
<td>current value of the measure
</td></tr>
<tr>
<td>value.previous
</td>
<td>previous value of the measure
</td></tr>
<tr>
<td>delta.absolute
</td>
<td> difference abolute between current and previous values.
</td></tr>
<tr>
<td>value.relative
</td>
<td> delta.absolute/value.current
</td></tr>
<tr>
<td>time.elapsed
</td>
<td>time elapsed (ms)  between value.previous and value.current
</td></tr>
<tr>
<td>time.current
</td>
<td>timestamp (ms)  of the the value.current
</td></tr>
<tr>
<td>time.previous
</td>
<td>timestamp (ms) of the value.previous
</td></tr></tbody></table>

State variables have two **states** *enable* and *disable* and can be
set individually either by APIs or XML. State *enable* , measures are
recorded in the circular list; state *disable*, measures are not
computed.

Knowledge of mediation chains @runtime
--------------------------------------

TO DO : events configuration TO DO : node description , topology access
and node retreival TO DO : measures stored by state variable retreival

State variables configuration
=============================

State variables are attached to mediators and adapters. All state
variables can be configured by the Cilia DSL, API and REST API ( see
REST API )

Cilia DSL
---------

Tag **state-variable**

<table cellpadding="10" cellspacing="0" border="1">
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
<td>id
</td>
<td>No
</td>
<td>
</td>
<td>The state variable name (see section State Variable)
</td></tr>
<tr>
<td>enable
</td>
<td>Yes
</td>
<td>false
</td>
<td>true for enable, false for disable
</td></tr></tbody></table>

Tag **state-variable/setup**

<table cellpadding="10" cellspacing="0" border="1">
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
<td>queue
</td>
<td>Yes
</td>
<td>1
</td>
<td>Number of records (stored in circular list)
</td></tr>
<tr>
<td>flow-control
</td>
<td>Yes
</td>
<td>null
</td>
<td>LDAP condition to store a record (see data flow control), null means no flow control
</td></tr></tbody></table>

Extract of the Cilia DSL :

    <chain id="chainId" type="chainType" xmlns:ns="http://adele.imag.fr/cilia/compendium/schema" xmlns="http://adele.imag.fr/cilia/core/schema">
        <mediators>
          <mediator-instance id="mediatorId" type="MyMediatorType">
             <monitoring>
             <!--  The state variable process.entry.count is enabled -->
             <ns:state-variable enable="true" id="process.entry.count">
               <!-- 
                    process.entry.count configuration :
                    20 records max in the list, 
                    the time between two store in the list must be greater than 1000 ms and
                    the difference between two measures must be greater than 2 
                -->                 
               <ns:setup queue="20" control-flow="(&amp;((time.elapsed &gt; 1000)(delta.absolute &gt; 2)))" />
        </ns:state-variable>
             </monitoring>
          </mediator-instance>
        </mediators>
    </chain>


API : ApplicationRuntime
------------------------

The ApplicationRuntime provides API to access to the Knowledge and State
Variables (configurations and measures). ApplicationRuntime is
accessible by the OSGi service CiliaContext.

The CiliaContext is a service offered by the Cilia Framework, it is the
chain container and the main mechanism to perform modifications at
run-time. All the advanced features, such as the REST API uses the
CiliaContext and the builder pattern in background. To use the
CiliaContext we must first import the class

    import fr.liglab.adele.cilia.CiliaContext;
    ...


Next we need to obtain the service, we recommend using the dependency
handler of iPOJO:


    import org.apache.felix.ipojo.annotations.Requires;
    import fr.liglab.adele.cilia.CiliaContext;
    ...
    
    @Requires
    CiliaContext ccontext;


But also, it is possible to obtain it using the BundleContext.

    //To retreive the ApplicationRuntime 
    ApplicationRuntime applicationruntime = ccontext.getApplicationRuntime();


The object ApplicationRuntime retreived it is now possible to configure
and enable/disable state variables per adapters/mediators.

    //To retreive the ApplicationRuntime 
    ApplicationRuntime applicationruntime = ccontext.getApplicationRuntime();
    
REST : API
----------

For the REST methods to manipulate CILIA architectures see [REST
API](rest-api.html)
To access the following methods go to root path:
HTTP_SERVER:PORT/cilia/runtime/ and the needed command PATH. In the
following table, the root path will be replaced by @
<table cellpadding="10" cellspacing="0" border="1">
<tbody><tr>
<th>Method
</th>
<th>PATH
</th>
<th>PARAMETERS
</th>
<th>DESCRIPTION
</th>
<th>EXAMPLE (Using command line tool <a rel="nofollow" class="external text" href="http://curl.haxx.se/">cURL</a>)
</th></tr>
<tr>
<td>PUT
</td>
<td> @/&lt;CHAIN_ID&gt;/start
</td>
<td> NONE
</td>
<td> Initialize a mediation chain.
</td>
<td> curl -X PUT http://localhost:8080/cilia/runtime/HelloWorld/start
</td></tr>
<tr>
<td>PUT
</td>
<td> @/&lt;CHAIN_ID&gt;/stop
</td>
<td> NONE
</td>
<td> Stop a mediation chain.
</td>
<td> curl -X PUT http://localhost:8080/cilia/runtime/HelloWorld/stop
</td></tr>
<tr>
<td>GET
</td>
<td> @/&lt;CHAIN_ID&gt;
</td>
<td> NONE
</td>
<td> Retrieve a mediation chain state.
</td>
<td> curl -X GET http://localhost:8080/cilia/runtime/HelloWorld/
</td></tr>
<tr>
<td>GET
</td>
<td> @/&lt;CHAIN_ID&gt;/components/&lt;COMPONENT_ID&gt;/setup
</td>
<td> NONE
</td>
<td> Get the monitor setup of a mediation component.
</td>
<td> curl -X GET http://localhost:8080/cilia/runtime/HelloWorld/components/hello_component/setup
</td></tr>
<tr>
<td>GET
</td>
<td> @/&lt;CHAIN_ID&gt;/components/&lt;COMPONENT_ID&gt;/setup/&lt;VARIABLE&gt;
</td>
<td> NONE
</td>
<td> Get the setup of a specific variable in the given mediation component.
</td>
<td> curl -X GET http://localhost:8080/cilia/runtime/HelloWorld/components/hello_component/setup/scheduler.count
</td></tr>
<tr>
<td>PUT
</td>
<td> @/&lt;CHAIN_ID&gt;/components/&lt;COMPONENT_ID&gt;/setup/&lt;VARIABLE&gt;/&lt;CONCEPT&gt;
</td>
<td>
<ul><li> value: The new concept value of the specific variable.
</li></ul>
</td>
<td> Modify a concept in a specific variable. The available concepts are:
<ul><li> queue: The queue size of the variable buffer.
</li><li> control-flow: an LDAP filter to limit the flow of events.
</li><li> enable: True or false, to enable/disable the variable.
</li></ul>
</td>
<td> curl -X PUT http://localhost:8080/cilia/runtime/HelloWorld/components/hello_component/setup/scheduler.count/control-flow -d "value=(time.elapsed &gt; 1000)"
</td></tr>
<tr>
<td>PUT
</td>
<td> @/&lt;CHAIN_ID&gt;/components/&lt;COMPONENT_ID&gt;/threshold/&lt;VARIABLE&gt;/&lt;CONCEPT&gt;
</td>
<td>
<ul><li> value: The new threshold value of the specific variable.
</li></ul>
</td>
<td> Modify the threshold in a specific variable. The available concepts are:
<ul><li> low
</li><li> very-low
</li><li> high
</li><li> very-high
</li></ul>
</td>
<td> curl -X PUT http://localhost:8080/cilia/runtime/HelloWorld/components/hello_component/threshold/scheduler.count/low -d "value=100"
</td></tr>
<tr>
<td>GET
</td>
<td> @/&lt;CHAIN_ID&gt;/components/&lt;COMPONENT_ID&gt;/rawdata
</td>
<td> NONE
</td>
<td> Get the buffered raw data obtained by the monitor.
</td>
<td> curl -X GET http://localhost:8080/cilia/runtime/HelloWorld/components/hello_component/rawdata
</td></tr>
<tr>
<td>GET
</td>
<td> @/&lt;CHAIN_ID&gt;/components/&lt;COMPONENT_ID&gt;/rawdata/&lt;VARIABLE&gt;
</td>
<td> NONE
</td>
<td> Get the buffered raw data obtained for a specific variable.
</td>
<td> curl -X GEThttp://localhost:8080/cilia/runtime/HelloWorld/components/hello_component/rawdata/scheduler.count
</td></tr></tbody></table>

Knowledge @runtime
==================

to explain properties provided by services and meaning.
