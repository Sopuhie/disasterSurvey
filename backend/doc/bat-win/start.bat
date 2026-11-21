@echo off

set javaw="%JAVA_HOME%\bin\javaw.exe"

ping -n 10 127.0.0.1>nul

echo ******start disaster-survey-admin.jar******
set disaster="%JAVA_HOME%\bin\disaster.exe"
if not exist  %disaster% (
  copy %javaw% %disaster%
)
start disaster -Xms256m -Xmx512m -Dfile.encoding=utf-8 -jar disaster-survey-admin.jar --spring.profiles.active=prod

exit
