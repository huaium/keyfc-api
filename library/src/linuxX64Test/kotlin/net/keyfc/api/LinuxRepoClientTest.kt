package net.keyfc.api

import kotlin.test.Test
import kotlin.test.assertNotNull

class RepoClientLinuxTest {

    @Test
    fun `linux http client creation succeeds`() {
        val client = createHttpClient("keyfc-api/linux-test")
        assertNotNull(client)
        client.close()
    }
}
