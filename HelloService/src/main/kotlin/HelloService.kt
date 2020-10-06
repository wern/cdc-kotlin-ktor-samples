package de.mathema.cdc.kotlin.hello

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.util.concurrent.TimeUnit

fun main() {
    HelloService(8080).startService(true)
}

fun Application.helloModule() {
    routing {
        get("/") {
            call.respondText("""{ "response" : "Hello World" }""", ContentType.Application.Json)
        }
    }
}

class HelloService (val port:Int)  {
    private val server : ApplicationEngine =  embeddedServer(Netty, port = 8080, module = Application::helloModule)
    fun startService(block:Boolean) = server.start(wait = block)
    fun stopService() = server.stop(500, 1000, TimeUnit.MILLISECONDS)
}