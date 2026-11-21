#!/bin/bash
#name: stopAll.sh
#desc: 停止项目微服务：首先通过获取jar包运行的pid判断该服务是否已启动，如果启动则杀死该进程。批量停止服务
#date: 2025/7/8

#当前脚本所在目录的路径
current_path="$( cd "$( dirname "$0"  )" && pwd  )"
#日志文件
logFile=$current_path"/log/runStopAllStatus.log"
#脚本名称
name=$0
#各个微服务JAR名称
disasterJar="disaster-survey-admin.jar"


#各个微服务启动后的进程号
disasterPid=''


#停止jar包
function stopAllProject(){
	cd /data/program/disaster_program/
	#停止运行disaster服务
	echo $(date "+%Y-%m-%d %H:%M:%S")" [INFO ] 停止服务开始..." > $logFile
	#先判断jar是否在运行
	disasterPid=`ps aux|grep ${disasterJar}|grep -v grep|grep -v ${name}|awk '{print $2}'` #获取jar包运行的pid
	if [ -z $disasterPid ] ;then  #如果pid的长度为空，说明没有在运行
		echo $(date "+%Y-%m-%d %H:%M:%S")" [INFO ] disaster服务包没有运行" >> $logFile
	else
		echo $(date "+%Y-%m-%d %H:%M:%S")" [INFO ] disaster服务包开始停止...">> $logFile
		kill -9 $disasterPid  #jar在运行，使用kill命令停止
		disasterPid=`ps aux|grep ${disasterJar}|grep -v grep|grep -v ${name}|awk '{print $2}'` #获取jar包运行的pid
		if [ -z $disasterPid ] ;then  #如果pid的长度为空，说明没有在运行
			echo $(date "+%Y-%m-%d %H:%M:%S")" [INFO ] disaster服务包停止运行成功">> $logFile
		else
			echo $(date "+%Y-%m-%d %H:%M:%S")" [INFO ] disaster服务包停止运行失败">> $logFile
		fi
	fi
}

################程序入口#################
stopAllProject
