package com.example

import io.ktor.server.testing.*
import org.junit.Test

class ApplicationTest {
    @Test
    fun testCustomer() = testApplication {
        application {
            module()
        }
    }
}
