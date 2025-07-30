FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} datacrawler.jar
EXPOSE 8082
ENV SPRING_PROFILE=prod
ENTRYPOINT ["java","-XX:+UseContainerSupport","-Djdk.internal.platform.cgroupv1=true","-jar","datacrawler.jar"]