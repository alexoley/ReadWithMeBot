# ---------- build stage ----------
FROM eclipse-temurin:11-jdk AS build
WORKDIR /workspace

# Gradle wrapper + build scripts first, so dependencies are cached between builds
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

# ---------- runtime stage ----------
# JRE 17: транзитивные зависимости (eclipse-collections для mapdb) собраны под Java 17
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar /app/app.jar
RUN mkdir -p /app/bookshelf
# Старые библиотеки (guice/cglib в telegrambots, mapdb) требуют доступа к внутренностям JDK на Java 17
ENV JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED \
 --add-opens java.base/java.util=ALL-UNNAMED \
 --add-opens java.base/java.nio=ALL-UNNAMED \
 --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
 --add-opens java.base/java.io=ALL-UNNAMED"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
