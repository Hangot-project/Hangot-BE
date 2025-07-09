FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} web.jar
EXPOSE 8080
ENV SPRING_PROFILE=prod
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=${SPRING_PROFILE}","web.jar"]
