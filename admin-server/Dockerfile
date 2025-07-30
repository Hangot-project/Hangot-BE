FROM amazoncorretto:21
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} admin.jar
EXPOSE 8000
ENV SPRING_PROFILE=prod
ENTRYPOINT ["java","-jar","admin.jar"]