FROM openjdk:17

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} gatewayservice.jar

ENTRYPOINT ["java","-jar","gatewayservice.jar"]

EXPOSE 8080