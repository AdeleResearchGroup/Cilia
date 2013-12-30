Cilia Application DSL
---------------------

The Cilia DSL allows to define a mediation chain, its mediator/adapter
instances and the bindings between them.

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

Each mediation chain (***chain*** XML Element) could have a chain type
and an unique identifier.

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
<td>type
</td>
<td>No
</td>
<td>Chain type.
</td></tr></tbody></table>

~~~~ {.xml}
<cilia>
    <chain id="MyChainId" type="MyChainType">
        <!-- -->
    </chain>
</cilia>
~~~~

Mediator instances, adapter instances, and bindings are declared
separately in three different groups. There is an XML element for each
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
<td>It contains all the mediator instances configurations used in each chain.
</td></tr>
<tr>
<td>adapters
</td>
<td>Yes
</td>
<td>It contains all the adapters instances configurations used in each chain.
</td></tr>
<tr>
<td>bindings
</td>
<td>Yes
</td>
<td>It contains all the bindings in the chain.
</td></tr></tbody></table>

~~~~ {.xml}
<chain>
   <mediators>..</mediators>
   <adapters>..</adapters>
   <bindings>...</bindings>
</chain>
~~~~

Mediators instances
-------------------

Mediator instances are defined in the ***mediators**'' XML element. Each
mediator instance (***mediator-instance**'') is followed by its mediator
type and an instance identifier.


### Mediator Properties

When declaring mediator instances, it is possible to (re)configure some
mediator properties. We say, mediator properties are all the properties
declared for each of the mediator constituent (scheduler, processor,
dispatcher). So, in order to differentiate them is used the XML Element
***scheduler***, ***processor*** or ***dispatcher***.

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

### Mediator Instance Example

~~~~ {.xml}
<chain id="chainId" type="chainType">
    <mediators>
      <mediator-instance id="mediatorId" type="MyMediatorType">
        <ports>
          <in-port name="entry-port" />
          <out-port name="exit-port" />
        </ports>
        <processor>
           <property name="propertyName" value="propertyValue"/>
        </processor>
        <scheduler>
           <property name="schedPropertyName" value="propertyValue"/>
        </scheduler>
        <dispatcher>
           <property name="dispPropertyName" value="propertyValue"/>
        </dispatcher>
      </mediator-instance>
    </mediators>
</chain>
~~~~

Adapter instances
-----------------

Adapter instances are defined in the ***adapters**'' XML element. Each
adapter instance(***adapter-instance**'') is followed by its adapter
type and an instance identifier.

Similar to the mediator instances, adapters could have properties. These
properties are handled as the mediator properties, since adapters are
also a special mediator type.

Bindings
--------

Each binding in a mediation chain is declared in the ***bindings***
section.

~~~~ {.xml}
<chain id="chainId" type="chainType">
    <mediators>
      <!-- mediator instances -->
    </mediators>
    <adapters>
      <!-- mediator instances -->
    </adapters>
    <bindings>
       <binding from="mediatorId1:exitPoint1" to="mediatorId2:inPoint1" />
    </bindings>
</chain>
~~~~

<table cellpadding="10" cellspacing="0" border="1">
<tbody><tr>
<th>NAME
</th>
<th>OPTIONAL
</th>
<th>DESCRIPTION
</th></tr>
<tr>
<td>binding
</td>
<td>No
</td>
<td> Binding declaration
</td></tr>
<tr>
<td>binding from
</td>
<td>Yes
</td>
<td>Describes which mediator is the one who sends the data, and which port is used by this binding.
</td></tr>
<tr>
<td>binding to
</td>
<td>Yes
</td>
<td>Describes which mediator is the one who receives the data, and which port is used by this binding.
</td></tr>
<tr>
<td>binding type
</td>
<td>Yes
</td>
<td>Describes the type of the binding between mediators, event-admin-binding is the default one.
</td></tr></tbody></table>

### Bindings and linkers

Bindings use predefined linkers, but there is possible to change the
linker to use by adding a **linker** attribute to the binding
declaration.

~~~~ {.xml}
    <bindings>
       <binding from="mediatorId1:std" to="mediatorId2:std" linker="ea-binding"/>
    </bindings>
~~~~

Mediation chain example
-----------------------

~~~~ {.xml}
<cilia>
    <chain id="MyChainId" type="MyChainType">
       <mediators>
          <mediator-instance type="myMediator" id="mediatorId1"/>
          <mediator-instance type="myMediator2" id="mediatorId2"/>
       </mediators>
       <bindings>
          <binding from="mediator1:std" to="mediatorId2:std" >
       </bindings>
    </chain>
</cilia>
~~~~

Mediation chain - hot deployment
--------------------------------

To deploy a mediation chain at runtime using the dscilia language, the
bundle cilia-deployer must be installed and started on the OSGi
platform.

Mediation chains must be in a file with the file extension **.dscilia**.
This file must be placed based on the chameleon configuration to load on-the-fly new artifacts or configuration files.
In cilia-distributions, the directory is ***applications***

