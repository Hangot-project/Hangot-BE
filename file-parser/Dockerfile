FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} datastore.jar
EXPOSE 8081
ENV SPRING_PROFILE=prod
ENTRYPOINT ["java","-XX:+UseContainerSupport","-Djdk.internal.platform.cgroupv1=true","-jar","datastore.jar"]
