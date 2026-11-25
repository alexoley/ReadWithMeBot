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

The build uses Gradle 6.3 and Kotlin 1.3, which need **JDK 8–11** to run Gradle (JDK 17+ will not work).

```sh
export JAVA_HOME=/path/to/jdk-11
export BOT_TOKEN=... BOT_USERNAME=... BOT_CREATORID=...
export MONGO_URI='mongodb://user:password@localhost:27017/bot?authSource=admin'
export SPRING_PROFILES_ACTIVE=local
./gradlew bootRun
```

## Tests

```sh
JAVA_HOME=/path/to/jdk-11 ./gradlew test
```

Unit tests need neither MongoDB nor Telegram. `ReadWithMeBotApplicationTests` (full Spring context)
is tagged `integration` and is excluded from `gradlew test`, because it needs real MongoDB, bot credentials and network access.
