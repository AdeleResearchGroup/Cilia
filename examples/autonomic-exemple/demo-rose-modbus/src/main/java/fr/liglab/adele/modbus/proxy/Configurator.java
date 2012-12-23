package fr.liglab.adele.modbus.proxy;

import static org.ow2.chameleon.rose.api.Machine.MachineBuilder.machine;

import java.util.Arrays;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.ow2.chameleon.rose.api.Machine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.liglab.adele.cilia.util.Const;

public class Configurator {
	private static final Logger logger = LoggerFactory
			.getLogger(Const.LOGGER_APPLICATION);
	private final Machine rose;

	public Configurator(BundleContext context) throws InvalidSyntaxException {
		rose = machine(context, "rose-modbus").create();
		// Discovery 
		rose.instance("Modbus_TCP.discovery").create();

		// Importer
		rose.importer("Modbus/TCP.importer").create();

		// Connections
		rose.in("(service.imported=true)").protocol(Arrays.asList("Modbus/TCP")).add();
	}

	protected void start() {
		logger.debug("Rose Machine started");
		rose.start();	
	}

	protected void stop() {
		logger.debug("Rose Machine stopped");
		rose.stop();
	}

}
