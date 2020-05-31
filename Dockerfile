FROM adoptopenjdk/openjdk11:alpine-jre
RUN apk add --no-cache tzdata
ENV TZ 'Africa/Addis_Ababa'
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]