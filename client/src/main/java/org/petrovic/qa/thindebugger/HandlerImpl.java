package org.petrovic.qa.thindebugger;

import com.sun.jdi.*;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;

import java.util.List;

public class HandlerImpl implements Handler {
    private final int lineNumber;
    private final String varName;

    public HandlerImpl(int lineNumber, String varName) {
        this.lineNumber = lineNumber;
        this.varName = varName;
    }

    @Override
    public void handle(Event evt) throws IncompatibleThreadStateException, AbsentInformationException {
        if (!(evt instanceof BreakpointEvent)) {
            return;
        }
        BreakpointEvent bpEvent = (BreakpointEvent) evt;
        if (bpEvent.location().lineNumber() == lineNumber) {
            System.out.print("Breakpoint at line " + lineNumber + ": ");
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
}
