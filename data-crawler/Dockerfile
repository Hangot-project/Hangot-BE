FROM amazoncorretto:21
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} datacrawler.jar
EXPOSE 8082
ENV SPRING_PROFILE=prod
ENTRYPOINT ["java","-jar","datacrawler.jar"]