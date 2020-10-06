package de.mathema.cdc.kotlin.hello

import au.com.dius.pact.provider.junit.Provider
import au.com.dius.pact.provider.junit.loader.PactUrl
import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URL

@PactUrl(urls = ["pacts/HelloWorldConsumer-HelloWorldProvider.json"])
@Provider("HelloWorldProvider")
class HelloProviderVerification {
    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    @BeforeEach
    @Throws(Exception::class)
    fun before(context: PactVerificationContext) {
        context.target = HttpTestTarget.fromUrl(URL("http", "localhost", service.port, ""))
    }

    companion object {
        var service = HelloService(8080)
        @BeforeAll
        @JvmStatic
        fun startServer() {
            service.startService(false)
        }

        @AfterAll
        @JvmStatic
        fun stopServer() {
            service.stopService()
        }
    }
}