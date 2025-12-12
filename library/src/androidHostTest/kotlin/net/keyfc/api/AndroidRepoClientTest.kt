package net.keyfc.api

import kotlin.test.Test
import kotlin.test.assertNotNull

class RepoClientAndroidTest {

    @Test
    fun `android http client creation succeeds`() {
        val client = createHttpClient("keyfc-api/android-test")
        assertNotNull(client)
        client.close()
    }
}
