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
package fr.liglab.adele.cilia.administration.command;

import org.apache.felix.service.command.Descriptor;

import fr.liglab.adele.cilia.Data;
import fr.liglab.adele.cilia.administration.util.CiliaInstructionConverter;
import fr.liglab.adele.cilia.framework.AbstractCollector;

/**
 * Felix Gogo Shell to Cilia-Admin AdapterImpl.
 * 
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class CiliaAdminGogoCommandAdapter extends AbstractCollector {

    private final CiliaInstructionConverter mConverter = new CiliaInstructionConverter();

    @Descriptor("Create a chain, a mediator, an adapter or a binding")
    public void create(@Descriptor("Parameters") String... params) {
        executeCommand("create", params);
    }

    @Descriptor("Modify a chain, a mediator, an adapter or a binding")
    public void modify(@Descriptor("Parameters") String... params) {
        executeCommand("modify", params);
    }

    @Descriptor("Remove a chain, a mediator, an adapter or a binding")
    public void remove(@Descriptor("Parameters") String... params) {
        executeCommand("remove", params);
    }
    
    @Descriptor("Start a chain")
    public void start(@Descriptor("Parameters") String... params) {
        executeCommand("start", params);
    }
    
    @Descriptor("Stop a chain")
    public void stop(@Descriptor("Parameters") String... params) {
        executeCommand("stop", params);
    }

    @Descriptor("Show the specified chain, mediator or binding")
    public void show(@Descriptor("Parameters") String... params) {
        executeCommand("show", params);
    }

    @Descriptor("Load a Cilia chain description file")
    public void load(@Descriptor("Parameters") String... params) {
        executeCommand("load", params);
    }

    private void executeCommand(String command, String... params) {
        StringBuilder builder = new StringBuilder(command);
        for (String param : params) {
            builder.append(" ");
            builder.append(param);
        }
        String line = builder.toString();
        Data result = mConverter.getDataFromInstruction(line);
        if (result != null) {
            super.notifyDataArrival(result);
        } else {
            System.out.println("Cannot execute command : " + line);
            System.out.println(help());
        }
    }

    /**
     * Get the command usage.
     * 
     * @return the command usage.
     */
    @Descriptor("Shows the Cilia command help")
    public String help() {
        return "cilia\n"
                + "		create \n"
                + "			chain id=chainid\n"
                + "			mediator chain=chainId type=namespace:mediatorType id=mediatorId\n"
                + "			adapter  chain=chainId type=namespace:adapterType id=adapterId\n"
                + "			binding  chain=chainId from=mediator:port to=mediator:port\n"
                + "		modify \n"
                + "			chain id=chainid property=propertyName value=propertyvalue type=[primitive|Array|Map]\n"
                + "			mediator chain=chainId id=mediatorId property=propertyName value=propertyvalue\n"
                + "			adapter  chain=chainId id=adapterId property=propertyName value=propertyvalue\n"
                + "			binding  chain=chainId from=mediator:port to=mediator:port property=propertyName value=propertyvalue\n"
                + "		remove \n"
                + "			chain id=chainid\n"
                + "			mediator chain=chainId  id=mediatorId\n"
                + "			adapter  chain=chainId  id=adapterId\n"
                + "			binding  chain=chainId from=mediator:port to=mediator:port\n"
                + "		start\n"
                + "			chain id=chain_id\n"
                + "		stop\n"
                + "			chain id=chain_id\n"
                + "		show\n"
                + "			chain id=chain_id\n"
                + "			mediator id=mediator_id chain=chainId\n"
                + "			adapter id=adapter_id chain=chainId\n"
                + "		load\n"
                + "			file=url\n" + "\n";
    }

}
