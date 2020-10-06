import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.Pact
import au.com.dius.pact.consumer.PactFolder
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.model.RequestResponsePact
import org.apache.http.HttpResponse
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = "GreetingProvider")
@PactFolder("pacts")
class GreetingConsumerTest {
    @Pact(provider = "GreetingProvider", consumer = "GreetingConsumer")
    fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given("Es gibt keine Gruesse")
            .uponReceiving("Abfrage aller Gruesse ohne vorhandene Daten")
                .path("/greetings")
                .method("GET")
            .willRespondWith()
                .status(200)
                .body(PactDslJsonBody().array("greetings"))
            .given("Es gibt zwei Gruesse")
            .uponReceiving("Abfrage aller Gruesse mit vorhandenen Daten")
                .path("/greetings")
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(
                    PactDslJsonBody()
                        .array("greetings")
                        .`object`()
                        .stringValue("type", "formal")
                        .stringValue("phrase", "Good morning")
                        .closeObject()
                        .`object`()
                        .stringValue("type", "casual")
                        .stringValue("phrase", "Hey")
                        .closeObject()
                )
            .given("Es gibt einen casual Gruss")
            .uponReceiving("Abfrage eines existierenden Grusses")
                .path("/greetings/casual")
                .method("GET")
            .willRespondWith()
                .status(200)
                .body(
                    PactDslJsonBody()
                        .`object`("greeting")
                        .stringValue("type", "casual")
                        .stringValue("phrase", "Hey")
                        .closeObject()
                )
            .given("Es gibt keinen formal Gruss")
            .uponReceiving("Abfrage eines nicht existierenden Grusses")
                .path("/greetings/formal")
                .method("GET")
            .willRespondWith()
                .status(404)
            .uponReceiving("Speichern eines neuen Grusses")
                .path("/greetings/formal")
                .method("PUT")
                .headers("Content-type", ContentType.APPLICATION_JSON.toString())
                .body(
                    PactDslJsonBody()
                        .`object`("greeting")
                        .stringValue("type", "formal")
                        .stringValue("phrase", "Good morning")
                        .closeObject()
                )
            .willRespondWith()
                .status(200)
            .given("Es gibt einen formal Gruss")
            .uponReceiving("Loeschen eines Grusses")
                .path("/greetings/formal")
                .method("DELETE")
                .willRespondWith()
                .status(200)
            .toPact()
    }

    @Test
    @Throws(Exception::class)
    fun testAbfrageAllerGruesse(mockServer: MockServer) {
        var response: HttpResponse =
            Request.Get("${mockServer.getUrl()}/greetings").execute().returnResponse()
        Assertions.assertEquals(200, response.statusLine.statusCode)
        response = Request.Get("${mockServer.getUrl()}/greetings/casual").execute().returnResponse()
        Assertions.assertEquals(200, response.statusLine.statusCode)
        response = Request.Get("${mockServer.getUrl()}/greetings/formal").execute().returnResponse()
        Assertions.assertEquals(404, response.statusLine.statusCode)
        response = Request.Put("${mockServer.getUrl()}/greetings/formal").bodyString(
                """{"greeting":{"type":"formal","phrase":"Good morning"}}""",
            ContentType.APPLICATION_JSON
        ).execute().returnResponse()
        Assertions.assertEquals(200, response.statusLine.statusCode)
        response = Request.Delete("${mockServer.getUrl()}/greetings/formal").execute().returnResponse()
        Assertions.assertEquals(200, response.statusLine.statusCode)
    }
}