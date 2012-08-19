package org.petrovic.qa.thindebugger;

/**
 * @author petrovic -- 8/19/12 4:35 PM
 */
public class BreakPointInfo {
    public final String className;
    public final Integer lineNumber;

    public BreakPointInfo(String className, Integer lineNumber) {
        this.className = className;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return "BreakPointInfo{" +
                "className='" + className + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }
}
