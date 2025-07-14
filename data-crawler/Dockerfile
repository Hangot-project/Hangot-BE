FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} datacrawler.jar
EXPOSE 8082
ENV SPRING_PROFILE=prod
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=${SPRING_PROFILE}","datacrawler.jar"]