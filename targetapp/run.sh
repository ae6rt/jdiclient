#!/bin/sh

set -u 
set -x 

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5050 -cp target/thin-debugger-target-lab-1.0-SNAPSHOT.jar org.petrovic.qa.tdt.App
