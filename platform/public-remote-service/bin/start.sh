#!/bin/bash

base="/data/web/public-remote-server"
pfile=$base/server.pid
log=$base/logs/stdout

project="public-remote-server"
workspace=$base/$project

mainClass="com.rr.publik.bootstrap.DeamonRunner"
libs="$workspace/target/lib/*:$workspace/target/classes/"

if [ ! -d $workspace ]
then
    echo "$workspace not exists!"
    exit 1
fi

if [ -f $pfile ]
then
    echo "`cat $pfile` is running!"
    exit 1
fi

# jdk1.8 需要换成元空间
JAVA_OPTS="-Xms400m -Xmx400m -Xss256k -XX:PermSize=112m -XX:MaxPermSize=112m";
JAVA_OPTS="$JAVA_OPTS -verbose:gc"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCTimeStamps"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails"
JAVA_OPTS="$JAVA_OPTS -Xloggc:/data/log/gclogs/gc.log"

vm_args="-server -verbose:gc -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:-CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=7 -XX:GCTimeRatio=19 -XX:CMSInitiatingOccupancyFraction=70 -XX:CMSFullGCsBeforeCompaction=0 -XX:+CMSClassUnloadingEnabled -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=51639  $JAVA_OPTS ";

cd $base

#nohup mvn exec:java -Dexec.mainClass="$mainClass" -Dexec.args="$configFile $beanName" >>$log 2>&1 &
nohup java $vm_args -cp $libs $mainClass >>$log 2>&1 &

pid=$!

echo $pid >$pfile

echo "start server!"


