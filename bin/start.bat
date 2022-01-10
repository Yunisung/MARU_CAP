chcp 65001
d:
cd D:\Develop\workspace\MARU_CAP\bin

java -DCP_CONF=../conf -Dlogback.configurationFile=../conf/logback.xml -Dfile.encoding=UTF-8 -Xms128m -Xmx512m -Xss128k -XX:+AggressiveOpts -XX:+UseParallelGC -XX:+UseBiasedLocking -XX:NewSize=64m -cp ../lib/*;../classes com.pgmate.cap.main.CaptureDaemon
