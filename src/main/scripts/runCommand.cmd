@echo off
setlocal
set JAR_EXEC=%~dp0${bin.name}
set JAVA_OPTS=-Xms${java.heap.min} -Xmx${java.heap.max} -XX:+UseParNewGC -XX:+UseConcMarkSweepGC
start /B /MIN java %JAVA_OPTS% -jar "%JAR_EXEC%" %*
endlocal