CILIA API REST
==============

Prerequisites
-------------

The Cilia API REST is based on [Jersey](http://jersey.java.net/). And
uses [RoSe](https://github.com/barjo/arvensis) to eases the usage. In
order to export the API Rest it is needed to have the following bundles.

To easies the configuration and the dependencies, please download the cilia distribution containing the dependencies and the configuration files to have the remote service ready to use :

[snapshot distribution](http://repository-cilia.forge.cloudbees.com/snapshot/fr/liglab/adele/cilia/cilia-remote-distribution/)
or 
[release distribution](http://repository-cilia.forge.cloudbees.com/release/fr/liglab/adele/cilia/cilia-remote-distribution/)

Manipulating a Chain
--------------------
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
<td>GET
</td>
<td> &lt;HTTP_SERVER&gt;/cilia
</td>
<td> NONE
</td>
<td> Get the list of chain ids.
</td>
<td> curl -X GET http://localhost:8080/cilia
</td></tr>
<tr>
<td>GET
</td>
<td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;
</td>
<td> NONE
</td>
<td> Get the information of a mediation chain.
</td>
<td> curl -X GET http://localhost:8080/cilia/HelloWorld
</td></tr>
<tr>
<td>POST
</td>
<td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;
</td>
<td> NONE
</td>
<td> Creates an empty chain.
</td>
<td> curl -X POST http://localhost:8080/cilia/HelloWorld
</td></tr>
<tr>
<td>PUT
</td>
<td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;/components
</td>
<td>
<ul><li> command=copy 
</li><li> from=&lt;componentID&gt;
</li><li> to=&lt;componentID&gt;
</li></ul>
</td>
<td> Copy the information of an existent component to another.
</td>
<td> curl -X PUT -d "command=copy&amp;from=m1&amp;to=m2" http://localhost:8080/cilia/HelloWorld/components
</td></tr>
<tr>
<td>PUT
</td>
<td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;/components
</td>
<td>
<ul><li> command=replace
</li><li> from=&lt;componentID&gt;
</li><li> to=&lt;componentID&gt;
</li></ul>
</td>
<td> Replace one component for another and copy his data.
</td>
<td> curl -X PUT -d "command=replace&amp;from=m1&amp;to=m2" http://localhost:8080/cilia/HelloWorld/components
</td></tr>
<tr>
<td>DELETE
</td>
<td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;
</td>
<td> NONE
</td>
<td> Delete a mediation chain
</td>
<td> curl -X DELETE http://localhost:8080/cilia/HelloWorld
</td></tr></tbody></table>

Manipulating Components
-----------------------

<table cellpadding="10" cellspacing="0" border="1">
<tbody>
<tr>
   <th>Method
   </th>
   <th>PATH
   </th>
   <th>PARAMETERS
   </th>
   <th>DESCRIPTION
   </th>
   <th>EXAMPLE (Using command line tool <a rel="nofollow" class="external text" href="http://curl.haxx.se/">cURL</a>)
   </th>
</tr>
<tr>
   <td>GET
   </td>
   <td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;/components/&lt;ID&gt;
   </td>
   <td> NONE
   </td>
   <td> Get the information of a component (mediator or adapter)
   </td>
   <td> curl -X GET http://localhost:8080/cilia/HelloWorld/components/hello-mediator-1
   </td>
</tr>
<tr>
   <td>GET
   </td>
   <td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;/mediators/&lt;ID&gt;
   </td>
   <td> NONE
   </td>
   <td> Get the information of a mediator
   </td>
   <td> curl -X GET http://localhost:8080/cilia/HelloWorld/mediators/hello-mediator-1
   </td>
</tr>
<tr>
<td>GET
</td>
<td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;/adapters/&lt;ID&gt;
</td>
<td> NONE
</td>
<td> Get the information of an adapter
</td>
<td> curl -X GET http://localhost:8080/cilia/HelloWorld/adapters/adapter1</td>
</tr>
<tr>
<td>POST
</td>
<td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;/mediator/&lt;TYPE&gt;/&lt;ID&gt;
</td>
<td> properties=&lt;Properties in JSON&gt;
</td>
<td> Creates a mediator component
</td>
<td> curl -X POST -d "properties={name=titi}" http://localhost:8080/cilia/HelloWorld/mediator/HelloMediator/hello-mediator-1</td>
</tr>
<tr>
<td>POST
</td>
<td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;/adapter/&lt;TYPE&gt;/&lt;ID&gt;
</td>
<td> properties=&lt;Properties in JSON Format&gt;
</td>
<td> Creates an adapter component
</td>
<td> curl -X POST -d "properties={namespace=titi}" http://localhost:8080/cilia/HelloWorld/adapter/ConsoleAdapter/console-adapter-1
</td></tr>
<tr>
<td>PUT
</td>
<td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;/components/&lt;ID&gt;
</td>
<td> properties=&lt;Properties in JSON Format&gt;
</td>
<td> Modify the component properties
</td>
<td> curl -X PUT -d "properties={prefix=Hi}" http://localhost:8080/cilia/HelloWorld/components/hello-mediator-1
</td></tr>
<tr>
<td>DELETE
</td>
<td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;/components/&lt;ID&gt;
</td>
<td> NONE
</td>
<td> Remove a component in the given chain.
</td>
<td> curl -X DELETE http://localhost:8080/cilia/HelloWorld/components/hello-mediator-1
</td></tr>
<tr>
<td>DELETE
</td>
<td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;/mediators/&lt;ID&gt;
</td>
<td> NONE
</td>
<td> Remove a mediator in the given chain.
</td>
<td> curl -X DELETE http://localhost:8080/cilia/HelloWorld/mediators/hello-mediator-1
</td></tr>
<tr>
   <td>DELETE
   </td>
   <td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;/adapters/&lt;ID&gt;
   </td>
   <td> NONE
   </td>
   <td> Remove an adapter in the given chain.
   </td>
   <td> curl -X DELETE http://localhost:8080/cilia/HelloWorld/adapters/console-adapter-1</td>
</tr>
</tbody></table>

Manipulating Bindings
---------------------

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
<td>POST
</td>
<td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;/bindings
</td>
<td>
<ul><li> from=&lt;ID&gt;:&lt;PORT&gt;
</li><li> to=&lt;ID&gt;:&lt;PORT&gt;
</li><li> linker=&lt;PROTOCOL&gt;
</li><li> properties=&lt;Properties in JSON format&gt;
</li></ul>
</td>
<td> Perform a binding between two components.
</td>
<td> curl -d "from=hello-mediator-1:unique&amp;to=console-adapter-1:unique&amp;linker=JMS_Joram&amp;properties={jms.topic=wiki}" -X POST http://localhost:8080/cilia/HelloWorld/bindings
</td></tr>
<tr>
<td>DELETE
</td>
<td> &lt;HTTP_SERVER&gt;/cilia/&lt;CHAIN_ID&gt;/bindings?from=&lt;id:port&gt;&amp;to=&lt;id:port&gt;
</td>
<td> NONE
</td>
<td> Remove a binding between two components.
</td>
<td> curl -X DELETE http://localhost:8080/cilia/HelloWorld/bindings?from=hello-mediator-1:unique&amp;to=console-adapter-1:unique
</td></tr></tbody></table>