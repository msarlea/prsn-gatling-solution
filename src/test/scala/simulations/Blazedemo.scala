package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt
import io.alphash.faker._

class Blazedemo extends Simulation {
  val number = Phone().phoneNumber()
  println(s"$number")

  val httpConf = http.baseUrl("http://blazedemo.com")
  //    .header("Accept", "application/json")

  val scn = scenario("Baseline")

    .exec(http("Homepage")
      .get("/")
      .check(status.is(200)))
    .pause(5)

    .exec(http("login")
      .get("/login")
      .check(status.in(200 to 210)))
    .pause(1, 20)

    .exec(http("register")
      .get("/register")
      .check(status.not(404), status.not(500)))
    .pause(3000.milliseconds)

  setUp(
    scn.inject(
      nothingFor(5 seconds),
      //   constantUsersPerSec(10) during (10 seconds)
      rampUsersPerSec(1) to (5) during (20 seconds)
    ).protocols(httpConf.inferHtmlResources())
  )
}
