set appHome=..
set javaHome=C:/WINDOWS/system32/

set cp=%appHome%
set cp=%cp%;%appHome%/classes
set cp=%cp%;%appHome%/resources
set cp=%cp%;%appHome%/lib/activation.jar
set cp=%cp%;%appHome%/lib/ibapi-9.51.jar
set cp=%cp%;%appHome%/lib/jcommon-1.0.14.jar
set cp=%cp%;%appHome%/lib/jfreechart-1.0.11.jar
set cp=%cp%;%appHome%/lib/junit-4.5.jar
set cp=%cp%;%appHome%/lib/liquidlnf.jar
set cp=%cp%;%appHome%/lib/mail.jar

set javaOptions=-XX:+AggressiveHeap
set mainClass=com.jbooktrader.platform.startup.JBookTrader

%javaHome%java.exe -cp "%cp%" %javaOptions% %mainClass% "%appHome%"

