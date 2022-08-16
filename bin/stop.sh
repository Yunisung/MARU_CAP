#!/bin/sh 
kill -9 `cat < daemon.pid`
rm daemon.pid