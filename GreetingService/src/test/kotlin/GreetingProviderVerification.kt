package de.mathema.cdc.kotlin.hello

import au.com.dius.pact.provider.junit.Provider
import au.com.dius.pact.provider.junit.State
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

@PactUrl(urls = ["pacts/GreetingConsumer-GreetingProvider.json"])
@Provider("GreetingProvider")
class GreetingProviderVerification {
    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    @BeforeEach
    @Throws(Exception::class)
    fun before(context: PactVerificationContext) {
        service.clearGreetings()
        context.target = HttpTestTarget.fromUrl(URL("http", "localhost", service.port, ""))
    }

    @State("Es gibt einen casual Gruss")
    fun setUpEinenCasualGruss() {
        service.saveGreeting(Greeting("casual", "Hey"))
    }

    @State("Es gibt einen formal Gruss")
    fun setUpEinenFormalGruss() {
        service.saveGreeting( Greeting("formal", "How do you do"))
    }

    @State("Es gibt keinen formal Gruss", "Es gibt keine Gruesse")
    fun nichtsZuTun() {
        // state already reseted
    }

    @State("Es gibt zwei Gruesse")
    fun setupZweiGruesse() {
        service.saveGreeting(Greeting("formal", "Good morning"))
        service.saveGreeting(Greeting("casual", "Hey"))
    }

    companion object {
        var service = GreetingService(8080)
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