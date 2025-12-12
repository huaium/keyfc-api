package net.keyfc.api

import kotlin.test.Test
import kotlin.test.assertNotNull

class RepoClientIosTest {

    @Test
    fun `ios http client creation succeeds`() {
        val client = createHttpClient("keyfc-api/ios-test")
        assertNotNull(client)
        client.close()
    }
}
