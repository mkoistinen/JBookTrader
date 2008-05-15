set ibHome=C:\Jts
set javaHome=C:/WINDOWS/system32/

cd %ibHome%

set cp=
set cp=%cp%;jts.jar
set cp=%cp%;pluginsupport.jar
set cp=%cp%;jcommon-1.0.0.jar
set cp=%cp%;jfreechart-1.0.0.jar
set cp=%cp%;jhall.jar
set cp=%cp%;other.jar
set cp=%cp%;riskfeed.jar
set cp=%cp%;rss.jar

set javaOptions=-Dsun.java2d.noddraw=true -Xms256M -Xmx256M
set mainClass=jclient/LoginFrame

%javaHome%javaw.exe -cp "%cp%" %javaOptions% %mainClass% "%ibHome%"


