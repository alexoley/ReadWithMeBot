package org.stream.bot

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

/** Needs MongoDB, BOT_* env vars and network; excluded from `gradlew test` (tag "integration"). */
@Tag("integration")
@SpringBootTest
class ReadWithMeBotApplicationTests {

	@Test
	fun contextLoads() {
	}

}
