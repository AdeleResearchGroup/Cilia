What to know
============

CiliaContext
------------

The **CiliaContext** is a service offered by the Cilia Framework, it is
the chain container and the main mechanism to perform modifications at
run-time. All the advanced features, such as the REST API uses the
CiliaContext and the builder pattern in background.

To use the CiliaContext we must first import the class

~~~~ {.java}
import fr.liglab.adele.cilia.CiliaContext;
...
~~~~

Next we need to obtain the service, we recommend using the dependency
handler of iPOJO:

~~~~ {.java}

import org.apache.felix.ipojo.annotations.Requires;
import fr.liglab.adele.cilia.CiliaContext;
...

@Requires
CiliaContext ccontext;
~~~~

But also, it is possible to obtain it using the BundleContext.

ApplicationRuntime
------------------

The ApplicationRuntime provides an API to introspect an executing
mediation chain, for example, to initialize it.

~~~~ {.java}
//To initialize a new mediation chain
ccontext.getApplicationRuntime().startChain("HelloWorld-2");
~~~~

Cilia Builder Pattern
=====================

Cilia, from the version 1.5.0 use the builder pattern to build and
reconfigure mediation chains. The use is in three steps, first, we
obtain the builder using the **CiliaContext** Service. Second, we call
the required operations to the builder, and finally we call the
**done()** method. This allow us to verify the given information before
commit the changes.

How-to start using the Cilia Builder
------------------------------------

The only way to obtain a builder is using the **CiliaContext** service.

~~~~ {.java}
//We must obtain the CiliaContext service available on the OSGi registry.
CiliaContext ccontext;
...
Builder builder = ccontext.getBuilder();
..
//Reconfigure a mediation chain
builder.done();
~~~~

How-to obtain and create a mediation chain
------------------------------------------

The builder allows to create a new mediation chain, and also to
configure an existent one.

~~~~ {.java}
...
Builder builder = ccontext.getBuilder();
//To get an existent chain
Architecture helloChain = builder.get("HelloWorld");
..
//To create a new chain
Architecture newChain = builder.create("HelloWorld-2");
..
//Reconfigure a mediation chain
builder.done();

//To initialize a new mediation chain
ccontext.getApplicationRuntime().startChain("HelloWorld-2");
~~~~

How-To Create components
------------------------

There are two types of components, mediators and adapters.\
To create a mediator we perform the following code:

~~~~ {.java}
Builder nbuilder = ccontext.getBuilder();
Architecture arch = nbuilder.create("toto");
arch.create().mediator().type("Mock").id("newMediator");
nbuilder.done();
~~~~

\
To create an adapter we perform the following code:

~~~~ {.java}
Builder nbuilder = ccontext.getBuilder();
Architecture arch = nbuilder.create("toto");
arch.create().adapter().type("MyAdapter").id("my-adapter");
nbuilder.done();
~~~~

\
Components and adapters could be configured at the same time we create
them. So, to configure them we add as follows:

~~~~ {.java}
Builder nbuilder = ccontext.getBuilder();
Architecture arch = nbuilder.create("toto");

//To add a simple property
arch.create().mediator().type("Mock").id("my-mediator").configure().key("myproperty").value("theValue");
arch.create().adapter().type("MyAdapter").id("my-adapter").configure().key("myAdapterProperty").value("theValue");


//It is possible to add several properties
arch.create().mediator()
     .type("Mock").id("my-mediator-2").configure()
   .key("p1").value("v1")
   .key("p2").value("v2")
   .key("p3").value("v3")
   .key("p4").value("v4");

//And finally, it is possible to add a Map.
Map properties = new Hashtable();
properties.put("key1", "v1");
properties.put("key3", "v3");
arch.create().mediator()
     .type("Mock").id("my-mediator-2").configure().set(properties);

//After modifications, we perform the done.
nbuilder.done();
~~~~

How-To Modify components
------------------------

Using the builder we can also modify existent components in mediation
chains.

~~~~ {.java}
//We obtain the builder
Builder nbuilder = ccontext.getBuilder();
//We get an existant mediation chain to modify.
Architecture arch = nbuilder.get("myExistentChain");

//We modify the mediator with ID med1 and add a key tata with value tataValue
arch.configure().mediator().id("med1").key("tata").value("tataValue");

//also, it is possible to add an Hashtable of properties
Map properties = new Hashtable();
properties.put("key1", "v1");
properties.put("key3", "v3");
arch.configure().mediator().id("med2").set(properties);

//Adapters are reconfigured using the same approach
arch.configure().adapter().id("myAdapter").set(properties);


//Finally, in order to take effect the changes, we must perform the done operation on the builder.
nbuilder.done();
~~~~

How-To create/remove bindings
-----------------------------

To create bindings, it is needed to know the following information:

-   The id of the sending component
-   The port name of the sending component
-   The id of the receiving component
-   The port name of receiving component

This information is also needed when removing bindings.

As an example, we create a binding from **med-1** using the port
**xml**, and the data will be received on the component **med-3** in the
port **unique**

~~~~ {.Java}
Builder builder = ccontext.getBuilder();
//We create a new mediation chain. But also, we can add bindings with an existent chain using the builder.get method
Architecture arch = b.create("helloWorld");
//we make a binding.
arch.bind().from("med-1:xml").to("med-3:unique"); // bind from med-1 to med-3

//We perform the modifications
builder.done();

//Now we initialize the chain
ccontext.getApplicationRuntime().startChain("HelloWorld");
~~~~

We perform the remove binding in an existent mediation chain. So, to
remove the previous binding, we perform the following code:

~~~~ {.Java}
Builder builder = ccontext.getBuilder();
//We obtain the previous mediation chain 

Architecture arch = b.get("helloWorld");
//we remove the binding.
arch.unbind().from("med-1:xml").to("med-3:unique"); // bind from med-1 to med-3

//We perform the modifications
builder.done();
~~~~
