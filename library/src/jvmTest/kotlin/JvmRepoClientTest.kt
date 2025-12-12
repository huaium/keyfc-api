package net.keyfc.api

import kotlin.test.Test
import kotlin.test.assertNotNull

class RepoClientJvmTest {

    @Test
    fun `create http client returns a configured client`() {
        val client = createHttpClient("keyfc-api/test")
        assertNotNull(client)
        client.close()
    }
}
