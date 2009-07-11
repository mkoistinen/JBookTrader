set appHome=..
set javaHome=C:/WINDOWS/system32/

set cp=%appHome%
set cp=%cp%;%appHome%/classes
set cp=%cp%;%appHome%/resources
set cp=%cp%;%appHome%/lib/ibapi-9.62.jar
set cp=%cp%;%appHome%/lib/jcommon-1.0.16.jar
set cp=%cp%;%appHome%/lib/jfreechart-1.0.13.jar
set cp=%cp%;%appHome%/lib/junit-4.5.jar
set cp=%cp%;%appHome%/lib/substance-5.2.jar

set javaOptions=-XX:+AggressiveHeap
set mainClass=com.jbooktrader.platform.startup.JBookTrader

%javaHome%java.exe -cp "%cp%" %javaOptions% %mainClass% "%appHome%"

