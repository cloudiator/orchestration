FROM openjdk:8-jre-alpine

WORKDIR /data

ADD target/installer.jar .
ADD entry.sh .

ENTRYPOINT ["./entry.sh"]
