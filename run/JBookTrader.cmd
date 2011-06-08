echo off
set appHome=..

set cp=%appHome%
set cp=%cp%;%appHome%/classes
set cp=%cp%;%appHome%/resources


SETLOCAL
FOR %%f IN (%appHome%/lib\*.jar) DO call :append_classpath %%f
GOTO :end
:append_classpath
set cp=%cp%;%1
GOTO :eof
:end


set javaOptions=-XX:+AggressiveHeap
set mainClass=com.jbooktrader.platform.startup.JBookTrader

echo on
java.exe -cp "%cp%" %javaOptions% %mainClass% "%appHome%"

