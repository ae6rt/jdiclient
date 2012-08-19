Simple JDI client
=================

Inspired by [Wayne Adams blog post](http://wayne-adams.blogspot.com/2011/10/generating-minable-event-stream-with.html).

This lab project consists of two parts:  a target app running in its VM with debugging enabled and a client debugger app that connects to the target app VM.

Start the target app
--------------------

In one window, start the target app:

`java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5050  -cp target/thin-debugger-target-lab-1.0-SNAPSHOT.jar org.petrovic.qa.tdt.App`

Start the client debugger
-----------------------------------------------------

In a second terminal window, start the debugger:

`java -cp target/thin-debugger-lab-1.0-SNAPSHOT.jar org.petrovic.qa.thindebugger.JDIClient 5050 15 org.petrovic.qa.tdt.App msg`

You should see the target app print its random numbers to the console, and the client debugger print similar output.

Both target app and client debugger are assumed to be running on localhost.

Sample output
-------------

For example, the target app prints this

    random: 4890883
    random: -2082873808
    random: 1684298449
    random: -1263057291
    random: -3311150
    random: 350047496

while the debugger client prints this

    Breakpoint at line 15: msg = 'random: 4890883'
    Breakpoint at line 15: msg = 'random: -2082873808'
    Breakpoint at line 15: msg = 'random: 1684298449'
    Breakpoint at line 15: msg = 'random: -1263057291'
    Breakpoint at line 15: msg = 'random: -3311150'
    Breakpoint at line 15: msg = 'random: 350047496'