package org.petrovic.qa.thindebugger;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.event.Event;

/**
 * @author petrovic -- 8/19/12 4:01 PM
 */
public interface Handler {
    public void handle(Event evt) throws IncompatibleThreadStateException, AbsentInformationException;

    public BreakPointInfo breakPointInfo();
}
