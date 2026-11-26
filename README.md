# ReadWithMeBot

Telegram bot that sends you a small part of your book every day (PDF, EPUB, TXT).

## Run with Docker (recommended)

```sh
cp .env.example .env   # fill in BOT_TOKEN, BOT_USERNAME, BOT_CREATORID
docker compose up -d --build
```

`docker-compose.yml` starts MongoDB and the bot. With the default `SPRING_PROFILES_ACTIVE=local`,
books are stored in the `books-data` volume. To use Google Drive instead, leave
`SPRING_PROFILES_ACTIVE` empty and set `GOOGLE_SERVICE_ACCOUNT_CREDENTIALS` and `FILE_PARENT_FOLDER_ID`.

## Run locally

Stack: Java 25, Kotlin 2.4, Spring Boot 4, telegrambots 10, Gradle 9.
Gradle needs JDK 17+ to start; the project itself is compiled and tested with a **Java 25 toolchain**,
which Gradle downloads automatically if it is not installed.

```sh
export BOT_TOKEN=... BOT_USERNAME=... BOT_CREATORID=...
export MONGO_URI='mongodb://user:password@localhost:27017/bot?authSource=admin'
export SPRING_PROFILES_ACTIVE=local
./gradlew bootRun
```

## Tests

```sh
./gradlew test
```

Unit tests need neither MongoDB nor Telegram. `ReadWithMeBotApplicationTests` (full Spring context)
is tagged `integration` and is excluded from `gradlew test`, because it needs real MongoDB, bot credentials and network access.
