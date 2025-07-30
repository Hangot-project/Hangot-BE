FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} web.jar
EXPOSE 8080
ENTRYPOINT ["java","-XX:+UseContainerSupport","-Djdk.internal.platform.cgroupv1=true","-jar","web.jar"]
