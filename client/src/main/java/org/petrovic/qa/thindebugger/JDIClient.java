package org.petrovic.qa.thindebugger;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

import java.util.List;
import java.util.Map;

/**
 * From http://wayne-adams.blogspot.com/2011/10/generating-minable-event-stream-with.html
 */
public class JDIClient {
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.out.println("Usage:  java JDIDemo debugPortNumber sourceLineNumber fqclassName variableName");
            System.exit(-1);
        }
        int debugPort = Integer.parseInt(args[0]);
        int lineNumber = Integer.parseInt(args[1]);
        String fullyQualifiedClassName = args[2];
        String varName = args[3];

        VirtualMachineManager vmMgr = Bootstrap.virtualMachineManager();
        AttachingConnector socketConnector = null;
        List<AttachingConnector> attachingConnectors = vmMgr.attachingConnectors();

        for (AttachingConnector ac : attachingConnectors) {
            if (ac.transport().name().equals("dt_socket")) {
                socketConnector = ac;
                break;
            }
        }

        if (socketConnector != null) {
            Map paramsMap = socketConnector.defaultArguments();
            Connector.StringArgument hostArg = (Connector.StringArgument) paramsMap.get("hostname");
            hostArg.setValue("localhost");
            Connector.IntegerArgument portArg = (Connector.IntegerArgument) paramsMap.get("port");
            portArg.setValue(debugPort);
            VirtualMachine vm = socketConnector.attach(paramsMap);
            System.out.println("Attached to process '" + vm.name() + "'");
            System.out.println("Attached to description '" + vm.description() + "'");
            System.out.println("Attached to version '" + vm.version() + "'");

            List<ReferenceType> refTypes = vm.allClasses();
            Location breakpointLocation = null;
            for (ReferenceType referenceType : refTypes) {
                if (referenceType.name().equals(fullyQualifiedClassName)) {
                    System.out.println(referenceType.name());
                    List<Location> locs = referenceType.allLineLocations();
                    for (Location loc : locs) {
                        System.out.println("allowed breakpoints: " + loc);
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
            System.out.println(breakpointLocation);

            if (breakpointLocation != null) {
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
                            EventRequest evtReq = evt.request();
                            if (evtReq instanceof BreakpointRequest) {
                                BreakpointRequest bpReq = (BreakpointRequest) evtReq;
                                if (bpReq.location().lineNumber() == lineNumber) {
                                    System.out.println("Breakpoint at line " + lineNumber + ": ");
                                    BreakpointEvent brEvt = (BreakpointEvent) evt;
                                    ThreadReference threadRef = brEvt.thread();
                                    StackFrame stackFrame = threadRef.frame(0);
                                    List<LocalVariable> visVars = stackFrame.visibleVariables();
                                    for (LocalVariable visibleVar : visVars) {
                                        if (visibleVar.name().equals(varName)) {
                                            Value val = stackFrame.getValue(visibleVar);
                                            if (val instanceof StringReference) {
                                                String varNameValue = ((StringReference) val).value();
                                                System.out.println(varName + " = '" + varNameValue + "'");
                                            }
                                        }
                                    }
                                }
                            }
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
        }
    }
}
