package org.petrovic.qa.thindebugger;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * From http://wayne-adams.blogspot.com/2011/10/generating-minable-event-stream-with.html
 */
public class JDIClient {
    public static void main(String[] args) throws Exception {
        JDIClient jdiClient = new JDIClient();
        jdiClient.run(args);
    }

    private void run(String[] args) throws InterruptedException, AbsentInformationException, IOException, IllegalConnectorArgumentsException {
        if (args.length != 2) {
            System.out.println("Usage:  java JDIDemo debugHost:debugPortNumber className:sourceLineNumber");
            System.exit(-1);
        }
        String[] address = args[0].split(":");
        String hostName = address[0];
        int debugPort = Integer.parseInt(address[1]);

        String[] codeLocus = args[1].split(":");
        String fullyQualifiedClassName = codeLocus[0];
        int lineNumber = Integer.parseInt(codeLocus[1]);

        Bag bag = getLocation(hostName, debugPort, lineNumber, fullyQualifiedClassName);
        waitForBreakpoint(bag.virtualMachine, bag.location, new HandlerImpl(lineNumber, "msg"));
    }

    private Bag getLocation(String hostName, int debugPort, int lineNumber, String fullyQualifiedClassName) throws IllegalConnectorArgumentsException, IOException, AbsentInformationException {
        VirtualMachineManager vmMgr = Bootstrap.virtualMachineManager();
        AttachingConnector socketConnector = null;
        List<AttachingConnector> attachingConnectors = vmMgr.attachingConnectors();

        Location breakpointLocation = null;
        VirtualMachine vm = null;

        for (AttachingConnector ac : attachingConnectors) {
            if (ac.transport().name().equals("dt_socket")) {
                socketConnector = ac;
                break;
            }
        }

        if (socketConnector != null) {
            Map paramsMap = socketConnector.defaultArguments();
            Connector.StringArgument hostArg = (Connector.StringArgument) paramsMap.get("hostname");
            hostArg.setValue(hostName);
            Connector.IntegerArgument portArg = (Connector.IntegerArgument) paramsMap.get("port");
            portArg.setValue(debugPort);
            vm = socketConnector.attach(paramsMap);
            System.out.print("Attached to process='" + vm.name() + "'");
            System.out.print(", description='" + vm.description() + "'");
            System.out.println(", JVM version='" + vm.version() + "'\n");

            List<ReferenceType> refTypes = vm.allClasses();

            for (ReferenceType referenceType : refTypes) {
                if (referenceType.name().equals(fullyQualifiedClassName)) {
                    List<Location> locs = referenceType.allLineLocations();
                    for (Location loc : locs) {
                        if (loc.lineNumber() == lineNumber) {
                            breakpointLocation = loc;
                            break;
                        }
                    }
                }
            }
            if (breakpointLocation == null) {
                System.out.printf("No breakpoint allowed at %s:%s\n", fullyQualifiedClassName, lineNumber);
                System.exit(0);
            }
        }
        return new Bag(breakpointLocation, vm);
    }

    private void waitForBreakpoint(VirtualMachine vm, Location breakpointLocation, Handler handler) throws InterruptedException {
        EventRequestManager evtReqMgr = vm.eventRequestManager();
        BreakpointRequest bReq = evtReqMgr.createBreakpointRequest(breakpointLocation);
        bReq.setSuspendPolicy(BreakpointRequest.SUSPEND_ALL);
        bReq.enable();
        EventQueue evtQueue = vm.eventQueue();
        while (true) {
            EventSet evtSet = evtQueue.remove();
            EventIterator evtIter = evtSet.eventIterator();
            while (evtIter.hasNext()) {
                try {
                    Event evt = evtIter.next();
                    handler.handle(evt);
                } catch (AbsentInformationException aie) {
                    System.out.println("AbsentInformationException: did you compile your target application with -g option?");
                } catch (Exception exc) {
                    System.out.println(exc.getClass().getName() + ": " + exc.getMessage());
                } finally {
                    evtSet.resume();
                }
            }
        }
    }

    private class Bag {
        public final Location location;
        public final VirtualMachine virtualMachine;

        private Bag(Location location, VirtualMachine virtualMachine) {
            this.location = location;
            this.virtualMachine = virtualMachine;
        }
    }
}
