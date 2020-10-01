# PersonsBaseline - Performance Testing Solution with Gatling

## 1. Introduction

The presented solution uses Gatling - a very popular Scala based performance testing tool in order to emulate User Behavior.

## 2. Tooling

### 2.1. Gatling

**Gatling is a powerful open-source load testing solution.** Gatling is designed for **continuous load testing** and integrates with your development pipeline. Gatling includes a **web recorder** and **colorful reports.**

[Read about it on galting.io](https://gatling.io/open-source/)

### 2.2. Apache Maven 3

Apache Maven is a software project management and comprehension tool. Based on the concept of a project object model (POM), Maven can manage a project's build, reporting and documentation from a central piece of information.

[Read about it on maven.apache.org](https://maven.apache.org/)

### 2.3. Java JDK 8

[Read about it on oracle.com](https://www.oracle.com/ro/java/technologies/javase/javase-jdk8-downloads.html)

### 2.4. Scala SDK 2.12.12

[Read about it on scala-lang.org](https://www.scala-lang.org/download/2.12.12.html)

### 2.4. Extra Libs

- [com.github.stevenchen3 > scala-faker_2.12 > 0.1.1](https://github.com/stevenchen3/scala-faker)
- [com.jayway.jsonpath > json-path > 2.4.0](https://mvnrepository.com/artifact/com.jayway.jsonpath/json-path)

### 2.5. Fiddler

[Read about it on telerik.com](https://www.telerik.com/fiddler)

## 3. Codebase structure

The solution is based on the standard Maven archetype

[gatling-highcharts-maven-archetype 3.3.1](https://mvnrepository.com/artifact/io.gatling.highcharts/gatling-highcharts-maven-archetype/3.3.1)

The main elements used / customized are

- src/test/scala/simulations/* - housing the PersonsBaseline.scala script
- src/test/scala/Engine.scala - is used to run the simulations on-demand for debugging purposes
- src/test/resources/bodies/* - contains the .json models used in the feeders that provide test data to the requests
- target/* - excluded from the codebase and in target/gatling contains the .html reports for each run - this gets deleted with ```mvn clean```

## 4. How to run

### 4.1. From IntelliJ

Customize the runtime variables in ```src/test/scala/simulations/PersonsBaseline.scala``` to your liking and save

```scala
/*************************** VARIABLES ***********************/
  // runtime variables - MVN CLI
  def userCount: Int =  getProperty("USERS", "15").toInt
  def rampDuration: Int = getProperty("RAMP_DURATION", "300").toInt
  def testDuration: Int = getProperty("DURATION", "600").toInt
  def thinktime_min: Int = getProperty("THINKTIME_MIN", "1").toInt
  def thinktime_max: Int = getProperty("THINKTIME_MAX", "10").toInt
  def baseUrl: String = getProperty("BASEURL", "164.90.242.25")
  def protocol: String = getProperty("PROTOCOL", "http")
/*************************** VARIABLES ***********************/
```

**Right click** on ```src/test/scala/Engine.scala``` and press Run to start the interactive menu that allows you to choose run scenario

In order to better **debug**, just install Fiddler, start it on port 8888 and 

```scala
/*uncomment the bellow lines in src/test/scala/simulations/PersonsBaseline.scala*/

//    .proxy(Proxy("localhost", 8888)) // run through Fiddler to debug - DISABLE for load tests!
---------------------------------------------
// use this for debugging
//  setUp(
//    scn.inject(atOnceUsers(1))
//  ).protocols(httpConf)

/*comment the bellow lines src/test/scala/simulations/PersonsBaseline.scala*/
 setUp(
    scn.inject(
      nothingFor(5 seconds),
      rampUsers(userCount) during (rampDuration seconds)),
  ).protocols(httpConf)
    .maxDuration(testDuration seconds)
```



### 4.2. From Command Line

Maven is used here to run the test, and the way to do it is:

```bash
# all simulations with default values
mvn gatling:test

# specific simulation with default values for CLI parameters
mvn gatling:test -D"gatling.simulationClass"="simulations.PersonsBaseline"

# specific simulation with custom values for some/all CLI parameters
mvn gatling:test -D"gatling.simulationClass"="simulations.PersonsBaseline" -DUSERS=200 -DRAMP_DURATION=30 -DBASEURL=localhost
```

**Note:** Maven3 (might work with 2 as well, but not tested) should be installed and placed in the PATH



## 5. Reporting

Default HTML Reports are generated automatically by Gatling at the end of the run and they are stored in 

```target/gatling/personsbaseline-***```

**Note:** Running ```mvn clean``` will delete the ```target/*``` folder along with its contents 

## 6. Future script improvements

- create distinct Helper Classes
- improve feeder performance and resilience (especially jsonPersonArrayFeeder)
- segregate HTTP calls into distinct classes
- misc



