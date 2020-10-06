package de.mathema.cdc.kotlin.hello

import com.fasterxml.jackson.core.util.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty
import io.ktor.util.*
import java.util.concurrent.TimeUnit

@KtorExperimentalAPI
fun main() {
    GreetingService(8080).startService(true)
}

@KtorExperimentalAPI
fun Application.greetingModule() {
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                indentObjectsWith(DefaultIndenter("  ", "\n"))
            })
            registerModule(JavaTimeModule())
        }
    }

    routing {
        get("/greetings") {
            call.respond(GreetingsModel(greetings.values))
        }
        get("/greetings/{type}") {
           greetings[call.parameters["type"]]?.
                let {
                    call.respond(GreetingModel(it))
                }?:kotlin.run {
                   throw NotFoundException()
                }
        }
        put("/greetings/{type}") {
            greetings[call.parameters["type"]!!] = call.receive<GreetingModel>().greeting
            call.respond(HttpStatusCode.OK)
        }
        delete("/greetings/{type}") {
            greetings[call.parameters["type"]]?.
                let {
                    greetings.remove(call.parameters["type"])
                    call.respond(HttpStatusCode.OK)
                }?:kotlin.run {
                    throw NotFoundException()
                }
        }
    }
}

data class GreetingsModel(val greetings: Collection<Greeting>)
data class GreetingModel(val greeting: Greeting)
data class Greeting(val type: String, val phrase: String)
val greetings = mutableMapOf<String, Greeting>()

@KtorExperimentalAPI
class GreetingService (val port:Int)  {
    private val server : ApplicationEngine =  embeddedServer(Netty, port = 8080, module = Application::greetingModule)
    fun startService(block:Boolean) = server.start(wait = block)
    fun stopService() = server.stop(500, 1000, TimeUnit.MILLISECONDS)
    fun saveGreeting (greeting:Greeting) {
        greetings[greeting.type] = greeting
    }
    fun clearGreetings () {
        greetings.clear()
    }
}