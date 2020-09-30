package simulations

import java.time.Instant
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.util.Random
import io.alphash.faker._
import scala.concurrent.duration.DurationInt

/**
  * This class contains the code necessary to run the Gatling prsn solution
  *
  */
class PersonsBaseline extends Simulation {

  val httpConf = http.baseUrl(s"${protocol}://${baseUrl}/")
    .header("Accept", "application/json")
//    .proxy(Proxy("localhost", 8888)) // run through Fiddler to debug - DISABLE for load tests!

/*************************** CUSTOM FEEDERS ***************************/
  val jsonPersonFeeder = Iterator.continually(Map(
    "name" -> Person().firstNameMale,
    "age" -> (new Random()).nextInt(100),
    "created" -> Instant.now.getEpochSecond
  ))

  //  TODO - refactor as it looks quite sloppy
  val jsonPersonArrayFeeder = Iterator.continually(
        Map(
      "name0" -> Person().firstNameMale, "age0" -> (new Random()).nextInt(100), "created0" -> Instant.now.getEpochSecond,
      "name1" -> Person().firstNameMale, "age1" -> (new Random()).nextInt(100), "created1" -> Instant.now.getEpochSecond,
      "name2" -> Person().firstNameMale, "age2" -> (new Random()).nextInt(100), "created2" -> Instant.now.getEpochSecond,
      "name3" -> Person().firstNameMale, "age3" -> (new Random()).nextInt(100), "created3" -> Instant.now.getEpochSecond,
      "name4" -> Person().firstNameMale, "age4" -> (new Random()).nextInt(100), "created4" -> Instant.now.getEpochSecond,
      "name5" -> Person().firstNameMale, "age5" -> (new Random()).nextInt(100), "created5" -> Instant.now.getEpochSecond,
      "name6" -> Person().firstNameMale, "age6" -> (new Random()).nextInt(100), "created6" -> Instant.now.getEpochSecond,
      "name7" -> Person().firstNameMale, "age7" -> (new Random()).nextInt(100), "created7" -> Instant.now.getEpochSecond,
      "name8" -> Person().firstNameMale, "age8" -> (new Random()).nextInt(100), "created8" -> Instant.now.getEpochSecond,
      "name9" -> Person().firstNameMale, "age9" -> (new Random()).nextInt(100), "created9" -> Instant.now.getEpochSecond
    )
  )
/*************************** CUSTOM FEEDERS ***************************/

/***************************  HTTP CALLS ***************************/
  def addOnePerson() = {
      exec(http("[POST] -> /persons")
        .post("persons")
        .body(ElFileBody("bodies/PersonModel.json")).asJson
        .check(status.is(201)))
  }
  def addSomePersonsInBulk() = {
      exec(http("[POST] -> /persons/bulk")
        .post("persons/bulk")
        .body(ElFileBody("bodies/PersonBulkModel.json")).asJson
        .check(status.is(201)))
  }

  def listPageOfAllPersonsOrderedByCreationDate() = {
      exec(http("[GET] -> /persons/_pageNo_")
        .get("persons/${pageNo}")
        .check(status.is(200)))
  }
/***************************  HTTP CALLS ***************************/

/*************************** SCENARIO DESIGN ***********************/
  val scn = scenario("Person Baseline Scenario")
    .forever() {
      // user creates 10 new persons one by one from JSON
      repeat(10){
        feed(jsonPersonFeeder)
        .exec(addOnePerson())
          .pause(thinktime_min,thinktime_max)
      }
      // 10 bulks of 10 persons in each bulk from JSON.
      .repeat(10) {
        feed(jsonPersonArrayFeeder)
        .exec(addSomePersonsInBulk())
          .pause(thinktime_min,thinktime_max)
      }
      // user browses through 5 first pages of all persons list
      .repeat(5, "repeatIndex") {
        exec(session => {
          session.set("pageNo", session.loopCounterValue("repeatIndex") + 1 )
        })
        .exec(listPageOfAllPersonsOrderedByCreationDate())
          .pause(thinktime_min,thinktime_max)
      }
      // user performs 10 subsequent searches for persons by the names taken from the first page of all persons list.
      .exec( http("[GET] -> Scrape 1st Person page")
        .get("persons/1")
        .check(jsonPath("$[0:9].name")
          .findAll
          .saveAs("names")
        ))
        .pause(thinktime_min,thinktime_max)
      .foreach("${names}", "name"){
        exec( http("[GET] -> /persons/name/_name_")
          .get("persons/name/${name}")
          .check(status.is(200)))
      }
        .pause(thinktime_min,thinktime_max)
    }
/*************************** SCENARIO DESIGN ***********************/

/*************************** HELPER METHODS ***********************/
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }
/*************************** HELPER METHODS ***********************/

/*************************** VARIABLES ***********************/
  // runtime variables - MVN CLI
  def userCount: Int =  getProperty("USERS", "30").toInt
  def rampDuration: Int = getProperty("RAMP_DURATION", "600").toInt
  def testDuration: Int = getProperty("DURATION", "600").toInt
  def thinktime_min: Int = getProperty("THINKTIME_MIN", "1").toInt
  def thinktime_max: Int = getProperty("THINKTIME_MAX", "10").toInt
  def baseUrl: String = getProperty("BASEURL", "164.90.242.25")
  def protocol: String = getProperty("PROTOCOL", "http")
/*************************** VARIABLES ***********************/

/********************* BEFORE / AFTER HOOKS *******************/
  before {
    println(s"Target environment: ${protocol}://${baseUrl}")
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
    println(s"Thinktime ranges between ${thinktime_min} and ${thinktime_max} seconds" )
    println("\n\n************* Load Test STARTED ***************\n\n")
  }

  after {
    println("\n\n************* Load Test COMPLETE ***************\n\n")
  }
/********************* BEFORE / AFTER HOOKS *******************/

/********************* SETUP LOAD SIMULATION ******************/
  setUp(
    scn.inject(
      nothingFor(5 seconds),
      rampUsers(userCount) during (rampDuration seconds)),
  ).protocols(httpConf)
    .maxDuration(testDuration seconds)

// use this for debugging
//  setUp(
//    scn.inject(atOnceUsers(1))
//  ).protocols(httpConf)

/********************* SETUP LOAD SIMULATION ******************/

}
