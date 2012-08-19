#!/bin/sh

set -ux

java -cp target/thin-debugger-lab-1.0-SNAPSHOT.jar org.petrovic.qa.thindebugger.JDIClient localhost:5050 org.petrovic.qa.tdt.App:15
