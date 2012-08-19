package org.petrovic.qa.thindebugger;

import com.sun.jdi.*;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;

import java.util.List;

public class HandlerImpl implements Handler {
    public final String varName;
    public final BreakPointInfo breakPointInfo;

    public HandlerImpl(String className, int lineNumber) {
        breakPointInfo = new BreakPointInfo(className, lineNumber);
        this.varName = "msg";
    }

    @Override
    public void handle(Event evt) throws IncompatibleThreadStateException, AbsentInformationException {
        if (!(evt instanceof BreakpointEvent)) {
            return;
        }
        BreakpointEvent bpEvent = (BreakpointEvent) evt;
        if (bpEvent.location().lineNumber() == breakPointInfo.lineNumber) {
            System.out.print("Breakpoint at line " + breakPointInfo.lineNumber + ": ");
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

    @Override
    public BreakPointInfo breakPointInfo() {
        return breakPointInfo;
    }
}
