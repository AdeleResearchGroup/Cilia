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

package fr.liglab.adele.cilia.admin.impl;


import fr.liglab.adele.cilia.*;
import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
import fr.liglab.adele.cilia.util.Const;
import fr.liglab.adele.cilia.util.FrameworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple gogo commands for debug purpose
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class GogoMonitoringCommands {

    private static final String HEADER = "+------------------------------------------------------"
            + "------------------------------------------------------";
    private CiliaContext ciliaContext;

    private ApplicationRuntime runtime;

    private CallbackEvent callbacks = new CallbackEvent();

    protected static Logger log = LoggerFactory.getLogger(Const.LOGGER_CORE);


    private void printSuccessor(Node[] nodes) throws CiliaIllegalStateException {
        if (nodes.length == 0) {
            System.out.println("| No node matching the filter");
        }
        for (int i = 0; i < nodes.length; i++) {
            System.out.println("| Successor ->"
                    + FrameworkUtils.makeQualifiedId(nodes[i]));
        }
    }

    public void start() {
        runtime = ciliaContext.getApplicationRuntime();
        app_callback_chain("(!(chain=admin-chain))");
        app_callback_node("(&(!(chain=admin-chain))(node=*))");
        app_callback_variable("(&(!(chain=admin-chain))(node=*))");
    }

    public void stop() {
    }

    //@Descriptor("Dump all successors to the node defined by ldapfiter")
    public void runtime_connected_to(String ldap) {
        try {
            System.out.println(HEADER);
            printSuccessor(runtime.connectedTo(ldap));
            System.out.println(HEADER);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //@Descriptor("Dump all adapters in to the node defined by ldapfiter")
    public void runtime_endpoints_in(String ldap) {
        try {
            System.out.println(HEADER);
            endpoints(runtime, ldap, true);
            System.out.println(HEADER);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //@Descriptor("Dump all adapters in to the node defined by ldapfiter")
    public void runtime_endpoints_out(String ldap) {
        try {
            System.out.println(HEADER);
            endpoints(runtime, ldap, false);
            System.out.println(HEADER);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //@Descriptor("Dump all adapters in to the node defined by ldapfiter")
    public void runtime_find_node(String ldapFilter) {
        try {
            Node[] nodes;
            System.out.println(HEADER);
            nodes = runtime.findNodeByFilter(ldapFilter);
            if (nodes.length == 0) {
                System.out.println("No node matching the filter " + ldapFilter);
            } else {
                for (int i = 0; i < nodes.length; i++) {
                    System.out.println("| Node ->"
                            + FrameworkUtils.makeQualifiedId(nodes[i]));
                }
            }
            System.out.println(HEADER);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //@Descriptor("Configure the setup of the node")
    public void node_setup(String nodeLdap, String variable, int queueSize, String flow,
                           boolean enable) {
        try {
            SetUp[] nodes = runtime.nodeSetup(nodeLdap);
            if (nodes.length == 0) {
                System.out.println("no node matching filter :" + nodeLdap);
                return;
            }
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].setMonitoring(variable, queueSize, flow, enable);
            }
            printSetupNode(nodes);

        } catch (Throwable e) {
            System.out
                    .println("Syntax \n\tnode_setup ldap variable queueSize flow enable");
            e.printStackTrace();
        }
    }

    //@Descriptor("Configure the setup of the node")
    public void node_rawdata(String nodeLdap, String variable) {
        try {
            RawData[] nodes = runtime.nodeRawData(nodeLdap);
            if (nodes.length != 0) {
                System.out.println(HEADER);
                for (int i = 0; i < nodes.length; i++) {
                    Measure[] measure = nodes[i].measures(variable);
                    System.out.println("| Node #" + FrameworkUtils.makeQualifiedId(nodes[i]));
                    System.out.println("| Variable '" + variable + "'");
                    for (int j = 0; j < measure.length; j++) {
                        System.out
                                .println("| Value #" + j + " =" + measure[j].toString());
                    }
                }
                System.out.println(HEADER);
            } else {
                System.out.println("No node matching filter :" + nodeLdap);
            }

        } catch (Throwable e) {
            System.out
                    .println("Syntax \n\tnode_setup ldap variable queueSize flow enable");
            e.printStackTrace();
        }
    }

    //@Descriptor("Configure all Threshold for node matching the filter")
    public void node_monitoring(String nodeLdap, String variable) {
        try {
            System.out.println("Not yet implemented");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //@Descriptor("Dump all nodes matching the filter")
    public void node_dump(String nodeLdap) {
        try {
            System.out.println("Not yet implemented");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //@Descriptor("Dump the state of the chain")
    public void app_chain_state(String chainId) {
        try {
            System.out.println(HEADER);
            int state = runtime.getChainState(chainId);
            switch (state) {
                case 0:
                    System.out.println(" Chain '" + chainId + "' , state IDLE, last command "
                            + runtime.lastCommand(chainId));
                    break;

                case 1:
                    System.out.println(" Chain '" + chainId
                            + "' , state RUNNING, last command "
                            + runtime.lastCommand(chainId));
                    break;
                case 2:
                    System.out.println(" Chain '" + chainId
                            + "' , state STOPPED, last command "
                            + runtime.lastCommand(chainId));
                    break;
                default:
                    System.out.println("Internal state error ");
            }
            System.out.println(HEADER);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //@Descriptor("Dump all chainID at runtime")
    public void runtime_chains() {
        String[] chains = runtime.getChainId();
        System.out.println(HEADER);
        for (int i = 0; i < chains.length; i++) {
            System.out.println("ChainID =" + chains[i]);
        }
        System.out.println(HEADER);
    }

    private void printSetupNode(Node[] nodes) throws CiliaIllegalStateException {
        String[] variables;
        System.out.println(HEADER);
        for (int i = 0; i < nodes.length; i++) {
            System.out.print("| Node #");
            System.out.println(FrameworkUtils.makeQualifiedId(nodes[i]));
            System.out.println("| Categories");
            System.out.println("|    SystemCall variables :");
            variables = ((SetUp) nodes[i]).getVariableNameByCategory("SystemCall");
            for (int j = 0; j < variables.length; j++) {
                System.out.println("|      " + variables[j]);

            }
            System.out.println("|    Dependency variables :");
            variables = ((SetUp) nodes[i]).getVariableNameByCategory("DependencyCall");
            for (int j = 0; j < variables.length; j++) {
                System.out.println("|      " + variables[j]);
            }
            System.out.println("|    Eventing variables :");
            variables = ((SetUp) nodes[i]).getVariableNameByCategory("EventingCall");
            for (int j = 0; j < variables.length; j++) {
                System.out.println("|      " + variables[j]);
            }
            System.out.println("|    Functionnal variables :");
            variables = ((SetUp) nodes[i]).getVariableNameByCategory("FunctionnalCall");
            for (int j = 0; j < variables.length; j++) {
                System.out.println("|      " + variables[j]);
            }
        }
        System.out.println(HEADER);
    }

    private void endpoints(Topology source, String ldapFilter, boolean depart) {
        try {
            Node[] array;
            String msg;
            if (depart) {
                array = source.endpointIn(ldapFilter);
                msg = "| Endpoint in #";
            } else {
                array = source.endpointOut(ldapFilter);
                msg = "| Endpoint out #";
            }
            System.out.println(HEADER);
            if (array.length != 0) {
                for (int i = 0; i < array.length; i++) {
                    System.out.println(msg + i + " : "
                            + FrameworkUtils.makeQualifiedId(array[i]));
                }
            } else {
                System.out.println("No endpoint found for the filter " + ldapFilter);
            }
            System.out.println(HEADER);
        } catch (Throwable e) {
            System.out.println("Internal Error !");
            e.printStackTrace();
        }
    }

    //@Descriptor("Dump all chainId")
    public void app_chains() {
        try {
            String[] chains = runtime.getChainId();
            System.out.println(HEADER);
            for (int i = 0; i < chains.length; i++) {
                System.out.println("ChainID =" + chains[i]);
            }
            System.out.println(HEADER);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //@Descriptor("List adapters In")
    public void app_endpoints_in(String ldapFilter) {
        endpoints(runtime, ldapFilter, true);
    }

    //@Descriptor("List adapters Out")
    public void app_endpoints_out(String ldapFilter) {
        endpoints(runtime, ldapFilter, false);
    }

    //@Descriptor("Dump all successors to the adapter/mediator defined by ldapfiter")
    public void app_connected_to(String ldap) {
        try {
            System.out.println(HEADER);
            printSuccessor(runtime.connectedTo(ldap));
            System.out.println(HEADER);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //@Descriptor("Dump all adapters in to the node defined by ldapfiter")
    public void app_find_node(String ldapFilter) {
        try {
            Node[] nodes;
            System.out.println(HEADER);
            nodes = runtime.findNodeByFilter(ldapFilter);
            if (nodes.length == 0) {
                System.out.println("No node matching the filter " + ldapFilter);
            } else {
                for (int i = 0; i < nodes.length; i++) {
                    System.out.println("| Node ->"
                            + FrameworkUtils.makeQualifiedId(nodes[i]));
                }
            }
            System.out.println(HEADER);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //@Descriptor("Regiter/Unregister chain callback")
    public void app_event_chain(String ldapFilter) {
        try {
            if ((ldapFilter != null) && (ldapFilter.length() > 0)) {
                System.out
                        .println("Registering events level chain, filter=" + ldapFilter);
                runtime.addListener(ldapFilter, (ChainCallback) callbacks);
            } else {
                System.out.println("UnRegistering events level chain");
                runtime.removeListener((ChainCallback) callbacks);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //@Descriptor("Regiter/Unregister chain callback")
    public void app_callback_chain(String ldapFilter) {
        try {
            if ((ldapFilter != null) && (ldapFilter.length() > 0)) {
                System.out.println("Registering events  chain, filter=" + ldapFilter);
                runtime.addListener(ldapFilter, (ChainCallback) callbacks);
            } else {
                System.out.println("UnRegistering events chain");
                runtime.removeListener((ChainCallback) callbacks);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //@Descriptor("Regiter/Unregister node callback")
    public void app_callback_node(String ldapFilter) {
        try {
            if ((ldapFilter != null) && (ldapFilter.length() > 0)) {
                System.out.println("Registering events  node, filter=" + ldapFilter);
                runtime.addListener(ldapFilter, (NodeCallback) callbacks);
            } else {
                System.out.println("UnRegistering events node");
                runtime.removeListener((NodeCallback) callbacks);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //@Descriptor("Regiter/Unregister measure callback")
    public void app_callback_variable(String ldapFilter) {
        try {
            if ((ldapFilter != null) && (ldapFilter.length() > 0)) {
                System.out.println("Registering events measure, filter=" + ldapFilter);
                runtime.addListener(ldapFilter, (VariableCallback) callbacks);
            } else {
                System.out.println("UnRegistering events measure");
                runtime.removeListener((VariableCallback) callbacks);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //@Descriptor("Entry for test")
    public void my_entry() {
        try {

            runtime.addListener("(node=mediator_1)", (VariableCallback) callbacks);

            SetUp[] rt = runtime.nodeSetup("(node=mediator_1)");

            if (rt.length != 0) {
                for (int i = 0; i < rt.length; i++)
                    rt[i].setMonitoring("process.entry.count", 10, "", true);
            }

            RawData[] data = runtime.nodeRawData("(node=mediator_1)");
            if (data.length != 0) {
                for (int i = 0; i < data.length; i++) {
                    Measure[] measures = data[i].measures("process.entry.count");
                    for (int j = 0; j < measures.length; j++) {
                        System.out.println("Measure #" + j + " :"
                                + measures[j].toString());
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private class CallbackEvent implements ChainCallback, NodeCallback,
            ThresholdsCallback, VariableCallback {

        public void onUpdate(Node node, String variable, Measure m) {

            log.info("GogoCommand--> onUpdate "
                    + FrameworkUtils.makeQualifiedId(node) + ", variable :" + variable
                    + ", value=" + m.toString());
        }

        public void onThreshold(Node node, String variable, Measure m, int thresholdType) {

            log.info("GogoCommand--> onThreshold "
                    + FrameworkUtils.makeQualifiedId(node) + ", variable :" + variable
                    + ", measure =" + m.toString());

        }

        public void onArrival(Node node) {
            log.info("GogoCommand--> onArrival "
                    + FrameworkUtils.makeQualifiedId(node));

        }

        public void onDeparture(Node node) {
            log.info("GogoCommand--> onDeparture "
                    + FrameworkUtils.makeQualifiedId(node));
        }

        public void onModified(Node node) {
            log.info("GogoCommand-->" + " onModified "
                    + FrameworkUtils.makeQualifiedId(node));
        }

        public void onBind(Node from, Node to) {

            log.info("GogoCommand-->" + " onBind from"
                    + FrameworkUtils.makeQualifiedId(from) + ", to"
                    + FrameworkUtils.makeQualifiedId(to));
        }

        public void onUnBind(Node from, Node to) {

            log.info("GogoCommand-->" + " onUnbind from"
                    + FrameworkUtils.makeQualifiedId(from) + ", to"
                    + FrameworkUtils.makeQualifiedId(to));

        }


        public void onAdded(String chainId) {
            log.info("GogoCommand-->" + " Chain Added " + chainId);
        }

        public void onRemoved(String chainId) {
            log.info("GogoCommand-->" + " Chain Removed " + chainId);
        }

        public void onStateChange(String chaindId, boolean type) {
            if (type == true)
                log.info("GogoCommand-->" + " Chain Started " + chaindId);
            else log.info("GogoCommand-->" + " Chain Stopped " + chaindId);

        }

        public void onStateChange(Node node, boolean isValid) {
            log.info("GogoCommand-->" + " Node state changed "
                    + FrameworkUtils.makeQualifiedId(node) + "valid=" + isValid);
        }

        public void onStateChange(Node node, String variable, boolean enable) {
            log.info("GogoCommand--> variable state changed "
                    + FrameworkUtils.makeQualifiedId(node) + ", variable :" + variable
                    + ", enable =" + enable);
        }
    }

}
