Application Configuration
=========================

Cilia proposes an extender language to configure existent mediation
chains.\
Its main purpose is to configure the running components.\
The file must be placed on the configured folder for Chameleon, by
default in the cilia distribution is the **applications**. directory\
The file extentions must be ***\*.cfgcilia*** instead of
***\*.dscilia*** used to create new mediation chains.

XML Description
---------------

The Cilia XML root element must be ***cilia***. And it could only have
as child ***chain*** elements.

<table cellpadding="10" cellspacing="0" border="1">
<tbody><tr>
<th>ELEMENT
</th>
<th>OPTIONAL
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>cilia
</td>
<td>No
</td>
<td> It's the root node.
</td></tr>
<tr>
<td>cilia/chain
</td>
<td>No
</td>
<td>Each chain is defined in this chain node.
</td></tr></tbody></table>

~~~~ {.xml}
<cilia>
   <chain ...>
     <!-- -->
   </chain>

   <chain ...>
     <!-- -->
   </chain>
</cilia>
~~~~

Each mediation chain (***chain*** XML Element) must have an attribute
called **extention** and value **true**.

<table cellpadding="10" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>id
</td>
<td>No
</td>
<td> Chain unique identification.
</td></tr>
<tr>
<td>extentions (value="true")
</td>
<td>No
</td>
<td>To identify the given configuration will impact an existent chain.
</td></tr></tbody></table>

~~~~ {.xml}
<cilia>
    <chain id="MyChainId" extension="true">
        <!-- -->
    </chain>
</cilia>
~~~~

It is possible to reconfigure the two type of mediator components,
mediators and adapters. And they must be in its corresponding section.
So there are two sections (mediators and adapters).

Mediator instances, adapter instances. There is an XML element for each
group:

<table cellpadding="10" cellspacing="0" border="1">
<tbody><tr>
<th>ELEMENT
</th>
<th>OPTIONAL
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>mediators
</td>
<td>Yes
</td>
<td>It contains all the mediator configurations used in each chain.
</td></tr>
<tr>
<td>adapters
</td>
<td>Yes
</td>
<td>It contains all the adapters configurations used in each chain.
</td></tr></tbody></table>

~~~~ {.xml}
<chain id="toto" extension="true">
   <mediators>..</mediators>
   <adapters>..</adapters>
</chain>
~~~~

Mediator configuration
----------------------

The configuration of mediators is defined in the ***mediators**'' XML
element. Each mediator (***mediator**'' tag) is followed by its mediator
id as an attribute.

~~~~ {.xml}
<chain id="toto" extension="true">
   <mediators>..
      <mediator id="existentMediator">
           <!--Configuration-->
      </mediator>
   </mediators>
</chain>
~~~~

### Mediator Properties

The mediator properties are all the properties declared for each of the
mediator constituent (scheduler, processor, dispatcher). So, in order to
differentiate them is used the XML Element ***scheduler***,
***processor*** or ***dispatcher***.

<table cellpadding="10" cellspacing="0" border="1">
<tbody><tr>
<th>ELEMENT
</th>
<th>OPTIONAL
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>processor
</td>
<td>Yes
</td>
<td>To (re)configure the processor properties.
</td></tr>
<tr>
<td>scheduler
</td>
<td>Yes
</td>
<td>To (re)configure the scheduler properties.
</td></tr>
<tr>
<td>dispatcher
</td>
<td>Yes
</td>
<td>To (re)configure the dispatcher properties.
</td></tr></tbody></table>

To declare the property (re)configuration is used the ***property***
XML element. The information need for each property is followed by the
next attributes:

<table cellpadding="10" cellspacing="0" border="1">
<tbody><tr>
<th>ATTRIBUTE
</th>
<th>OPTIONAL
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>name
</td>
<td>No
</td>
<td>The property name.
</td></tr>
<tr>
<td>value
</td>
<td>No
</td>
<td>The property value.
</td></tr></tbody></table>

### Mediator Map properties

Mediator properties could be in the form of primitives type, but also
they could be Maps (java.util.Map) Map properties values are declared as
follow:

~~~~ {.xml}
<chain id="chainId" type="chainType">
    <mediators>
      <mediator-instance id="mediatorId" type="MyMediatorType">
        <processor>
           <property name="MapProperty">
              <item key="keyValue" value="itemValue"/>
              <item key="otherKeyValue" value="otherItemValue"/>
           </property>
        </processor>
      </mediator-instance>
    </mediators>
</chain>
~~~~

These properties could be used by all the mediators constituents, such
as scheduler, processor and dispatcher.

### Mediator Example

~~~~ {.xml}
<chain id="chainId" extension="true">
    <mediators>
      <mediator id="mediatorId" >
        <processor>
           <property name="propertyName" value="propertyValue"/>
        </processor>
        <scheduler>
           <property name="schedPropertyName" value="propertyValue"/>
        </scheduler>
        <dispatcher>
           <property name="dispPropertyName" value="propertyValue"/>
        </dispatcher>
      </mediator>
    </mediators>
</chain>
~~~~

Adapter instances
-----------------

The configuration of adapters is defined in the ***adapters**'' XML
element. Each adapter (***adapter**'' tag) is followed by its adapter id
as an attribute.

Adapters are configured the same way as mediators, but without the
scheduler/processor/dispatcher tag. For example:

~~~~ {.xml}
<chain id="chainId" extension="true">
    <adapters>
      <adapter id="jms-adapter-01" >
           <property name="jms.topic" value="topic$test"/>
      </adapter>
    </adapters>
</chain>
~~~~
