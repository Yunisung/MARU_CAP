#!/bin/sh

#아래 모듈은 MARU_CAP 이 데몬이기때문에 언제든지 죽을 수 있는 위험이 있다.
#그리하여 10분에 한번씩 PID 가 살아 있는지 확인 후 없을 경우 실행할 수 있도록 되어 있다.

cd /home/bkwinners/MARU/MARU_CAP/bin

pid_file="daemon.pid"

now=$(date +"%Y-%m-%d:%H:%M:%S");

value=`cat $pid_file`

if [ -f "$pid_file" ] 
then

	if ps -p $value > /dev/null
		then
   		echo "$now : $pid_file $value is running"
	else
   		echo "$now : $pid_file $value was stop "
		./start.sh
	fi
else
	echo "$now : $pid_file is not exist"
	./start.sh
fi
