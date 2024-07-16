FROM openjdk:21-jdk-slim

WORKDIR /src
COPY . /src

RUN apt-get update
RUN apt-get install -y dos2unix
RUN dos2unix gradlew

WORKDIR /run
RUN cp /src/build/libs/*.jar /run/titossy.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "titossy.jar"]
