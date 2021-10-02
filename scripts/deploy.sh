#!/bin/bash
REPOSITORY=/home/ec2-user/app/step2
PROJECT_NAME=project1

echo "> Build 파일 복사"
cp $REPOSITORY/zip/*.jar $REPOSITORY/

echo "> 현재 구동중인 어플리케이션 pid 확인"

CURRENT_PID=$(lsof -i :8080 | grep LISTEN | awk '{print $2}')

# pgrep -fl project1 | grep jar | awk '{print $1}' 가 먹히질 않음 그래서 변경
# lsof -i :8080 | grep LISTEN | awk '{print $2}' 로 변경

# lsof -i :8080 | grep LISTEN 가 8080 port의 정보를 찾음

echo " 현재 구동중인 어플리케이션pid : $CURRENT_PID"

lsof -P | grep ':8080' | awk '{print $2}' | xargs kill -15
sleep 5

#if [ -z "$CURRENT_PID"]; then
#        echo "> 현재 구동중인 어플리케이션이 없으므로 종료하지 않습니다."
#else
#        echo"> kill -15 $CURRNET_PID"
#        kill -15 $CURRENT_PID
#        sleep 5
#fi

echo "> 새 어플리케이션 배포"
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

echo "> JAR Name : $JAR_NAME"

echo "> $JAR_NAME 에 실행권한 추가"
chmod +x $JAR_NAME

echo "> $JAR_NAME 실행"

nohup java -jar \
        -Dspring.config.location=classpath:/application.properties,classpath:/application-errors.properties,/home/ec2-user/app/application-oauth.properties,classpath:/application-real.properties \
        -Dspring.profiles.active=real \
        $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &