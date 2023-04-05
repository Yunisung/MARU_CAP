#!/bin/sh
./stop.sh
echo "starting ...."


unset LANG

# LANG
##########################
LANG=ko_KR.utf8
export LANG

# SET LIBRARY
for i in ../lib/*.jar; do
    CP=$CP:$i
done
CP=`echo $CP | cut -c2-`

# JVM_ARGS for VM
##########################
JVM_ARGS="-DMARU_CAP -server -DCP_CONF=../conf -Dlogback.configurationFile=../conf/logback.xml -Dfile.encoding=UTF-8"
JVM_ARGS="$JVM_ARGS -Xss512k -Xms512m -Xmx1024m"
JVM_ARGS="$JVM_ARGS -cp $CP"


java $JVM_ARGS com.pgmate.cap.main.CaptureDaemon &
echo $!>daemon.pid
