/*
 * Copyright Adele Team LIG
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.liglab.adele.cilia.runtime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.Dictionary;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

import fr.liglab.adele.cilia.Data;



/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */

public class SerializedData extends Data {

	private byte[] content = null;
	
	private boolean isSerialized = false;
	
	public static final String CONTENT_CLASSNAME = "content.classname";
	
	

	
	public SerializedData(Data odata) throws IOException{
		super(odata.getContent(), odata.getName(), odata.getAllData());
	}

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6737018877566979219L;


	public void serializeContent() throws IOException {
		Object object = getContent();
	  	if (object == null)
	  		return ;
	  	ByteArrayOutputStream baos = null;
	  	ObjectOutputStream oos = null;
	  	try {
	  		baos = new ByteArrayOutputStream();
	  		oos = new ObjectOutputStream(baos);
	  		oos.writeObject(object);
	  		oos.flush();
	  		content = baos.toByteArray();
	  	} finally {
	  		if (oos != null)
	  			oos.close();
	  		if (baos != null)
	  			baos.close();
	  	}
	  	setContent("");
	  	isSerialized = true;
	  }
	
	public Data deserializeContent(BundleContext bcontext) throws IOException {
		Object object = null;
		if(isSerialized == false) {
			return  new Data(data.get(DATA_CONTENT), (String)data.get(DATA_NAME), data); // is not serialized
		}
		ByteArrayInputStream in = new ByteArrayInputStream(content);
	    ObjectInputStream is = new ObjectInputStream(in);
	    try {
			object =  is.readObject();
		} catch (ClassNotFoundException e) {
			
			in = new ByteArrayInputStream(content);
  			is = new OSGiedObjectInputStream(in, bcontext);
  			try {
				object = is.readObject();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
				throw new IOException("Unable to read object");
			}
		}
	    System.out.println("serialized Object " + object);
	    data.put(DATA_CONTENT, object);
	    isSerialized = false;
	    content = null;
	    return new Data(object, (String)data.get(DATA_NAME), data);
	}

	private class OSGiedObjectInputStream extends ObjectInputStream {
			BundleContext bcontext = null;
			OSGiedObjectInputStream(InputStream is, BundleContext context) throws IOException {
				super(is);
				bcontext = context;
			}
			
			protected Class resolveClass(ObjectStreamClass osc) throws IOException, ClassNotFoundException {
				String classname = osc.getName();
				return loadClass(bcontext, classname);//Class.forName(classname, false, Thread.currentThread().getContextClassLoader());
			}
			
			//TODO: Change to 4.3 OSGi version
			private Class<?> loadClass(BundleContext context,String klassname) throws ClassNotFoundException {
		        ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
		        PackageAdmin padmin = (PackageAdmin) context.getService(sref);
		        String pname = klassname.substring(0, klassname.lastIndexOf(".")); // extract package name
		        ExportedPackage pkg = padmin.getExportedPackage(pname);

		        context.ungetService(sref);
		        return pkg.getExportingBundle().loadClass(klassname);
		    }
		}
	
	
}
