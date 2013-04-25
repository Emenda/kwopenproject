@echo off
set classDir=F:\eclipse\workspace\kwopenproject\src
set portNumber=1099
set hostName=localhost
javac KlocworkCaller.java IKlocworkWorker.java KlocworkWorker.java
java -classpath %classDir% -Djava.rmi.server.codebase=file:%classDir%/ emenda.kwopenproject.remote.KlocworkWorker %hostName% %portNumber%