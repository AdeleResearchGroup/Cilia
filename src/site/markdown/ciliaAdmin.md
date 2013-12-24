Cilia Admin Service
===================

Besides the usage of the builder pattern, it is possible to reconfigure
mediation chains using CiliaAdminService

Retrieving the CiliaAdminService
--------------------------------

### Using the BundleContext

The BundleContext in OSGi allow us to perform several functions in an
OSGi platform. In this case, it is used to retrieve a service.

~~~~ {.java}
import fr.liglab.adele.cilia.CiliaAdminService;
...
    //Retrieve it using FrameworkUtil, iPOJO or using the BundleActivator
    BundleContext bcontext;
...
    ServiceReference refs[] = bcontext.getServiceReference(CiliaAdminService.class.getName(), null);
    //Check if is not null
...
    CiliaAdminService admin = bcontext.getService(refs[0]); //There should be only one service CiliaAdminService. Not Checked
...
    //Use the service
...
    bcontext.ungetService(refs[0]);
~~~~

### Using iPOJO

Using iPOJO is easier to obtain the CiliaAdminService. Using annotations
we do:

~~~~ {.java}
//For the iPOJO annotations
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
//For the CiliaService
import fr.liglab.adele.cilia.CiliaAdminService;

@Component(name="myCiliaManager")
@Instantiate(name="my-cilia-manager-0")
@Provides(specifications={MyCiliaManager.class})
public class MyCiliaManager {

  @Requires
  CiliaAdminService cadmin;
...

}

...
~~~~

### Other options

You can also retrieve the CiliaAdminService using other mechanism, for
example using the
[ServiceTracker](http://www.osgi.org/javadoc/r4v42/org/osgi/util/tracker/ServiceTracker.html)
approach, [BluePrint](http://aries.apache.org/modules/blueprint.html) or
[DeclarativeService](http://wiki.osgi.org/wiki/Declarative_Services).

Manipulating mediation chains
-----------------------------

See [Admin Service] (apidocs//fr/liglab/adele/cilia/CiliaAdminService.html)