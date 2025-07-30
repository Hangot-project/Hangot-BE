FROM amazoncorretto:21
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} web.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","web.jar"]
