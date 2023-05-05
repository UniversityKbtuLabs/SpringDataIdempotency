import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class MySimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .authorizationHeader("Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJLYWlseW4uTWFnZ2lvQGdtYWlsLmNvbSIsImlhdCI6MTY4Mjc0MzUxMywiZXhwIjoxNjgyNzQ3MTEzfQ.ZLKnS4cSVXAptS4CPz0RG2xrq3FgkcSCQGjkDAKZ3XE")

  val paymentBodyFeeder = Iterator.continually(
    Map(
      "idempotencyId" -> java.util.UUID.randomUUID.toString,
      "amount" -> util.Random.nextInt(100),
      "currency" -> "KZT",
      "description" -> java.util.UUID.randomUUID.toString
    )
  )

  val scn = scenario("MyScenario")
    .feed(paymentBodyFeeder)
    .exec(http("request1")
      .post("/api/payments")
      .header("Content-Type", "application/json")
      .body(StringBody("""{
        "idempotencyId": "${idempotencyId}",
        "amount": ${amount},
        "currency": "${currency}",
        "description": "${description}"
      }""")))

  setUp(
    scn.inject(
      rampUsersPerSec(1) to 10 during (10 seconds)
    )
  ).protocols(httpProtocol)
}
