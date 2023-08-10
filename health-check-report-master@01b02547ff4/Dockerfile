FROM openjdk:8-jdk-alpine

COPY ./target/polly-v1.0.jar /home/ec2-user/health-report/polly-v1.0.jar

ENTRYPOINT [ "java", "-jar", "/home/ec2-user/health-report/polly-v1.0.jar" ]

# Dockerfile로 image 생성
# docker build -t polly:1.0 .
  
# image로 container 실행
# docker run -d --name polly -p 8085:8080 polly:1.0

# container 접속
# docker exec -it polly bin/sh
# log 파일 경로 : /logs

