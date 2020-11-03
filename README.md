Dockerfile of application

```
FROM adoptopenjdk/openjdk11:alpine-jre
RUN apk add --no-cache tzdata
ENV BOT_TOKEN ''
ENV BOT_USERNAME ''
ENV BOT_CREATORID ''
#'local' profile if you dont want setup Google Drive and want to save all file localy inside docker container
#ENV SPRING_PROFILES_ACTIVE 'local'
ENV MONGO_URI 'mongodb://user:password@mongo:27017/bot?authSource=admin&authMechanism=SCRAM-SHA-1'

### Envs below optionaly, only for work with Google Drive
ENV GOOGLE_SERVICE_ACCOUNT_CREDENTIALS ''
#Google Drive not allowed more than one parent
ENV FILE_PARENT_FOLDER_ID ''
### Envs above optionaly, only for work with Google Drive

ENV TZ 'Africa/Addis_Ababa'
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Script for running Dockerfile

```sh
#!/bin/bash
./gradlew bootJar && \
docker stop readwithmebot
docker rm readwithmebot
docker rmi readwithmebot-local
docker build -t readwithmebot-local -f {nameOfYourDockerfile} . && \
docker run -d --name readwithmebot -p 8000:8000 --net mongodb_default readwithmebot-local
```

Docker-compose for mongodb(put in the folder "mongodb")
```
version: '3'
services:
  mongo:
    image: mongo:latest
    container_name: mongo
    environment:
      MONGO_INITDB_ROOT_USERNAME: user
      MONGO_INITDB_ROOT_PASSWORD: password
      MONGO_INITDB_DATABASE: admin
    ports:
      - "27017:27017"
    volumes:
     - my-mongo:/data/db
# Names our volume
volumes:
  my-mongo:
```
