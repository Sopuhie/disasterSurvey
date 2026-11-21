#!/bin/bash
#name: startAll.sh
#desc: 启动项目微服务：由于微服务的启动又先后顺序之别，当所有微服务初次发版时，需要按照如下顺序进行发版：
#先启动eureka，待启动完毕后再启动config和zuul，以上启动完毕后，对其他业务模块进行启动。
#先启动eureka，如果该JAR存在，就使用nohup命令启动，休眠5秒，然后获取jar包运行的pid判断是否启动成功。
#如果eureka启动成功，则启动zuul，步骤与eureka相同。
#如果zuul启动成功，则可启动其他运维服务以及业务服务，启动后休眠10秒，然后通过获取jar包运行的pid判断是否启动成功。
#dete: 2025/7
#author: liu

export JAVA_HOME=/opt/java/jdk1.8.0_311
export JRE_HOME=/opt/java/jdk1.8.0_311/jre
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib:$CLASSPATH
export PATH=$JAVA_HOME/bin:$PATH

#当前脚本所在目录的路径
current_path="$( cd "$( dirname "$0"  )" && pwd  )"
#日志文件
logFile=$current_path"/log/runStartAllStatus.log"
#JAR包所在目录
jarPath="/data/program/disaster_program/"
#脚本名称
name=$0
#各个微服务JAR名称
disasterJar="disaster-survey-admin.jar"


#各个微服务启动后的进程号
disasterPid=''

#获取程序运行状态方法
function startAllProject(){
	cd /data/program/disaster_program/
	echo $(date "+%Y-%m-%d %H:%M:%S")" [INFO ] 启动服务开始..." > $logFile
	if [ -f $jarPath$disasterJar ];then
		echo $(date "+%Y-%m-%d %H:%M:%S")" [INFO ] disaster服务包存在，开始启动..." >> $logFile
		nohup java -Xms512m -Xmx1024m -jar $jarPath$disasterJar > $jarPath"log/temp.log" 2>&1&
		sleep 60
		disasterPid=`ps aux|grep ${disasterJar}|grep -v grep|grep -v ${name}|awk '{print $2}'` #获取jar包运行的pid
		if [ ! -z $disasterPid ] ;then  #如果pid的长度为空，说明没有在运行
			echo $(date "+%Y-%m-%d %H:%M:%S")" [INFO ] 启动disaster服务成功" >> $logFile
		else
				echo $(date "+%Y-%m-%d %H:%M:%S")" [INFO ] 启动disaster服务失败" >> $logFile
		fi
	else
		echo $(date "+%Y-%m-%d %H:%M:%S")" [INFO ] disaster服务包不存在" >> $logFile
	fi
}

################程序入口#################
startAllProject
exit 0
