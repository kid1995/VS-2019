# VS-2019
VS 2019 Project

##Install

Open Command Line (Terminal) on current folder.

check if javac was included in System Variable Path: 

	javac -version

if it was not in System Variable Path
use instruction in this [link](https://javatutorial.net/set-java-home-windows-10)
----------------------------------------
if it was there, then run these commands:

	javac *.java

##Run

Start Java RMI App at port 1099:

	rmiregistry 1099

Start Server Side:

	java Server

!!! If Server is not ready and throw error,
    just ignore it, and start again,
    this bug will fix in the future.

Start Client Side:

	java Client



!!! Using another Terminal for Client, and turn off all App before reopen, else a error wil happen

#References Link 

[how to get host IP](https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java)

[java RMI for remote IP](https://stackoverflow.com/questions/35403765/java-rmi-for-remote-ip-host)

Javac not found:

set "path=%path%;C:\Program Files\Java\jdk1.8.0_181\bin"