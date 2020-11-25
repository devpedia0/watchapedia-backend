FROM openjdk:11

LABEL maintainer="wert1229@gmail.com"

VOLUME /tmp

EXPOSE 8080

ARG JAR_FILE=build/libs/watchapedia-0.0.1-SNAPSHOT.jar

ADD ${JAR_FILE} watchapedia.jar

ENTRYPOINT ["java", "-jar", "/watchapedia.jar"]