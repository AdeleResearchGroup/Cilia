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

package fr.liglab.adele.modbus.proxy;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.ow2.chameleon.rose.AbstractImporterComponent;
import org.ow2.chameleon.rose.ImporterService;
import org.ow2.chameleon.rose.RoseMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.util.Const;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.apache.felix.ipojo.Factory.VALID;
import static org.osgi.framework.Constants.OBJECTCLASS;

/**
 * Service importer for Modbus/TCP protocol <br>
 * 
 * 
 * @author Denis Morand
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ModbusProxyImporter extends AbstractImporterComponent implements
		ImporterService {
	private static final Logger logger = LoggerFactory
			.getLogger(Const.LOGGER_APPLICATION);

	private static final String FACTORY_FILTER = "(" + OBJECTCLASS + "="
			+ Factory.class.getName() + ")(factory.state=" + VALID + ")";

	private RoseMachine roseMachine;
	private BundleContext m_bundleContext;
	private String m_factory;
	private String m_urldomain;
	private Properties domainProperies;

	public ModbusProxyImporter(BundleContext context)
			throws InvalidSyntaxException {
		super();
		logger.info("Proxy importer created");
		m_bundleContext = context;
		domainProperies = new Properties();
	}

	/*
	 * list of currently supported protocol
	 */
	public List getConfigPrefix() {
		List list = new ArrayList();
		list.add("Modbus/TCP");
		return list;
	}

	protected ServiceRegistration createProxy(EndpointDescription description,
			Map<String, Object> extraProperties) {

		Hashtable props = new Hashtable(description.getProperties());
		props.putAll(extraProperties);
		StringBuilder sb = new StringBuilder("(&");
		sb.append(FACTORY_FILTER);
		sb.append("(factory.name=").append(m_factory).append("))");

		try {
			/* Look for a ipojo Factory */
			ServiceReference[] refs = m_bundleContext.getAllServiceReferences(
					Factory.class.getName(), sb.toString());
			if (refs != null) {
				Factory factory = (Factory) m_bundleContext.getService(refs[0]);
				addProxyProperties(props, description);
				ComponentInstance instance = factory
						.createComponentInstance(props);
				if (instance != null) {
					ServiceRegistration sr = new DeviceService(instance);
					sr.setProperties(props);
					logger.debug("proxy stack instancied "
							+ instance.getInstanceName());
					return sr;
				} else {
					logger.error("Proxy creation error");
				}
			} else {
				logger.error("Factory not found");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private void addProxyProperties(Map props, EndpointDescription description) {
		props.put("managed.service.pid", description.getId());
		/* Compute the service.ranking propertie */
		String revision = (String) props.get("major.minor.revision");
		String value = "150"; // Faire la convertion en String */
		/* retreive the domain  */
		props.put("domain.id",
				getDomain((String) props.get("device.ip.address")));
		if (value != null) {
			try {
				Integer.parseInt(value);
				props.put(org.osgi.framework.Constants.SERVICE_RANKING, value);
			} catch (NumberFormatException e) {
				logger.error("Service ranking property must be an integer string format");
			}
		}
	}

	/* Retreive the property accordingthe ip address */
	private String getDomain(String hostAddr) {
		String domain = null;
		if ((!domainProperies.isEmpty()) && (hostAddr != null)) {
			/* Key = IP address, value = domain.id */
			domain = domainProperies.getProperty(hostAddr);
		}
		if (domain == null)
			domain = "none";
		return domain;
	}

	protected void destroyProxy(EndpointDescription description,
			ServiceRegistration registration) {
		logger.debug("Endoint destroyed ,ID=" + description.getId());
		registration.unregister();
	}

	protected LogService getLogService() {
		return null;
	}

	public RoseMachine getRoseMachine() {
		return roseMachine;
	}

	protected void start() {
		super.start();
		logger.debug("Proxy importer started");
		loadProperties();
	}

	protected void stop() {
		super.stop();
		logger.debug("Proxy importer stopped");
	}

	private void loadProperties() {
		/* set the property domain.id , if association file is existing */
		if (m_urldomain != null) {
			try {
				domainProperies.load(new URL(m_urldomain).openStream());
				logger.debug("Properties read {}", m_urldomain.toString());
			} catch (MalformedURLException e) {
				logger.error("Invalid URL");
			} catch (IOException e) {
				logger.error("file {} {}", m_urldomain, " not existing");
			}
		}

	}

	/**
	 * A wrapper for ipojo Component instances
	 */
	class DeviceService implements ServiceRegistration {

		ComponentInstance instance;

		public DeviceService(ComponentInstance instance) {
			super();
			this.instance = instance;
			logger.debug("Device Proxy Service create="
					+ instance.getInstanceName());
		}

		public ServiceReference getReference() {
			try {
				ServiceReference[] references = instance.getContext()
						.getServiceReferences(
								instance.getClass().getCanonicalName(),
								"(instance.name=" + instance.getInstanceName()
										+ ")");
				if (references != null) {
					logger.debug("Device Proxy Service , getServiceReferences[0]="
							+ references[0].getClass().getName());
					return references[0];
				} else
					logger.error("Device proxy service, get Service reference=null");
			} catch (InvalidSyntaxException e) {
				logger.error("Proxy instance error"
						+ e.getStackTrace().toString());
			}
			return null;
		}

		public void setProperties(Dictionary properties) {
			if (logger.isDebugEnabled()) {
				StringBuffer sb = new StringBuffer("Device Proxy [");
				sb.append(instance.getInstanceName()).append("] ");
				sb.append("properties = ").append(properties.toString());
				logger.debug(sb.toString());
			}
			instance.reconfigure(properties);
		}

		public void unregister() {
			logger.debug("Device Proxy Service unregister :"
					+ instance.getInstanceName());
			instance.dispose();
		}
	}

}
