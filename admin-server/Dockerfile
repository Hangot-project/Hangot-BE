FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} admin.jar
EXPOSE 8000
ENV SPRING_PROFILE=prod
ENTRYPOINT ["java","-XX:+UseContainerSupport","-Djdk.internal.platform.cgroupv1=true","-jar","admin.jar"]