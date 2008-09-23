set appHome=..
set javaHome=C:/WINDOWS/system32/

set cp=%appHome%
set cp=%cp%;%appHome%/classes
set cp=%cp%;%appHome%/resources
set cp=%cp%;%appHome%/lib/liquidlnf.jar
set cp=%cp%;%appHome%/lib/API-9.4.jar
set cp=%cp%;%appHome%/lib/mail.jar
set cp=%cp%;%appHome%/lib/activation.jar
set cp=%cp%;%appHome%/lib/jcommon-1.0.13.jar
set cp=%cp%;%appHome%/lib/jfreechart-1.0.10.jar
set cp=%cp%;%appHome%/lib/jetty-6.1.11.jar
set cp=%cp%;%appHome%/lib/jetty-util-6.1.11.jar
set cp=%cp%;%appHome%/lib/servlet-api-2.5-6.1.11.jar


set javaOptions=-XX:+AggressiveHeap
set mainClass=com.jbooktrader.platform.startup.JBookTrader

%javaHome%java.exe -cp "%cp%" %javaOptions% %mainClass% "%appHome%"

