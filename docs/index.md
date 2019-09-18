# Simple Event Driven Microservices

## The Problem

You just want to write logic for your event driven application, but the boilerplate messaging code is getting in the way and it's costing you time.  Connecting your apps to event messaging services is cumbersome, plus you need to work with multiple messaging technologies in your work (depending on the domain you're working in).

## The Solution

Use a flexible messaging abstraction like [Spring Cloud Stream][docs] to take care of the complicated boilerplate code, leaving you free to write clean code which anyone can maintain. Spring Cloud Stream can also seamlessly unify many different messaging protocols behind one easy to use API and it smoothes away the subtle differences in approach or features between technologies (like partitioning or exchanges for example). With this solution in place, you can concentrate on building innovative  event-driven solutions that "just work".

## How to do it

In this solution you'll see exactly how Spring Cloud Stream's clever abstractions help make your stream handling code cleaner, and easier to work with. You'll also see how easy it is to switch between different messaging technologies using Spring Cloud Stream's readily available `binding` libraries. This solution demo supports the **[Kafka][kafka]** and **[RabbitMQ][rabbit]** bindings but others are available including [Amazon Kinesis][amazon], [Azure Event Hub][azure], [Google PubSub][google], and more.

### Getting Started

The solution consists of two applications. The first is the source of our event stream messages. These messages take the form of bank loan applications. The second application is a processor which checks these loan applications and sorts them into "approved" or "declined". 

To run the solution, follow these simple steps...

#### Step 1: Start the Messaging Servers

To make this easy we've included a `docker-compose` configuration which will start both the Kafka and the RabbitMQ servers at the same time and leave them running in the background.

In a fresh terminal window, go to the root folder of this repository and issue the following command.

```bash
./start-servers.sh
```

This script will start Kafka and RabbitMQ and stream the log output from both until you exit with `Ctrl-C`. 

> Note: The servers won't stop (they're running in the background), only the log messages will.

#### Step 2: Choose Between Kafka or Rabbit Mode

In steps 3 & 4 which follow, where we issue the Maven commands to compile and run the applications, you must substitute the `-P<profile-choice>` with the name of the messaging mode which you'd like run.

* For **Kafka** mode, substitute: **`-Pkafka`**
* For **RabbitMQ** mode, substitute: **`-Prabbit`** 

> This demo is not designed to "bridge" messages between Kafka and Rabbit, so be sure to choose the same profile in each of the two applications when you compile and run them. If bridging is your goal [see the documentation here][multi-connect].

#### Step 3: Generate Some Loan Applications

In a new terminal window, make the `/loansource` directory the current directory, and then issue the following command substituting the `<profile-choice>` with the mode you'd like to run (either `kafka` or `rabbit` as discussed in step 2 above).

```bash
./mvnw package spring-boot:run -DskipTests=true -P<profile-choice>
```

Once the `loansource` application has started, in the terminal window, you should see a message every second telling you that a new Loan application has been created.

#### Step 4: Process the Loan Applications

In a new terminal window, make the `/loancheck` directory the current directory, and then issue the following command substituting the `<profile-choice>` with the mode you'd like to run (either `kafka` or `rabbit` as discussed in step 2 above).

```bash
./mvnw package spring-boot:run -DskipTests=true -P<profile-choice>
```

Once the `loancheck` application has started, in the terminal window, you should see a message every second telling you that a new Loan application has been either `APPROVED` or `DECLINED`.

#### Step 5: Stopping the Demo

Once you're done with the demo applications, in the terminal windows for the `/loansource` and the `/loancheck` applications press `Ctrl-C`. The application should come to a halt and processing will stop.

If you're switching modes between Kafks and Rabbit, simply go back to **Step 2** and repeat the process.

If you're completely done with the demo: to stop the Kafka and RabbitMQ servers, in a terminal window in the root folder of the project run the `./stop-servers.sh` script. This isn't necessary if you're just switching between modes.

## How it Works

Maven profiles control which of the Spring Cloud Stream bindings are added as dependencies when you compile the code. If you choose the **kafka** profile then the `[spring-cloud-stream-binder-kafka][kafka]` JAR is added to the project. If you choose **rabbit** profile then the `[spring-cloud-stream-binder-rabbit][rabbit]` JAR is added. Kafka is the default if no profile is specified.

```xml
<profiles>
    <profile>
        <id>kafka</id>
        <properties>
            <spring.profile.activated>kafka</spring.profile.activated>
        </properties>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-stream-binder-kafka</artifactId>
                <version>${spring-cloud-stream.version}</version>
            </dependency>
        </dependencies>
    </profile>
    ...
<profiles>
```

Your choice of Maven profile also influences the `spring.profiles.active` property in the `src/main/resources/application.properties` file which switches the banner you see boot time (just so it's clear which mode you're running in when you start up).

#### The Loansource Microservice

All that's required to get the `LoansourceApplication` microservice to act as a source of `Loan` messages is to declare a `@Bean` method which generates and returns a `Supplier<>`, in this case it's of type `Loan`.

```java
@Bean
  public Supplier<Loan> supplyLoan() {
    return () -> {
      Loan loan = new Loan(UUID.randomUUID().toString(), "Ben", 10000L);
      LOG.info("{} {} for ${} for {}", loan.getStatus(), loan.getUuid(), loan.getAmount(), loan.getName());
      return loan;
    };
  }
```

`Supplier<>` is a Java function data type. Because there is only one `@Bean` of this type declared in this application, Spring Cloud Stream knows exactly what to do next. By default it will trigger this function once every second and send the result to the default output `MessageChannel`. The generated Loan objects are then automatically sent as messages on our default message channel (called `output`). 

> What's nice about this is that you can test the function using a regular unit test.

#### The Loancheck Microservice

The `loancheck` microservice is a bit more complex as it sorts messages, but not overly so.

The `loancheck` microservice is subscribing to the messages coming from the `output` topic and then sending them into either the `approved` or `declined` topics based on the message's content (specifically the amount of the loan, similar to a fraud checking facility).

Beacuse 3 message channels are involved, a simple `LoanProcessor` component is used to specify the input and output `MessageChannel`'s which looks something like this:

```java
@Component
public interface LoanProcessor {

  String APPLICATIONS_IN = "output"; // Topic where new loans appear
  String APPROVED_OUT = "approved"; // Topic where approved loans go
  String DECLINED_OUT = "declined"; // Topic where declined loans go

  @Input(APPLICATIONS_IN)
  SubscribableChannel sourceOfLoanApplications();

  @Output(APPROVED_OUT)
  MessageChannel approved();

  @Output(DECLINED_OUT)
  MessageChannel declined();
}
```

A `@Component` called the `LoanChecker` receives this `LoanProcessor` in it's constructor. Then, it's method `checkAndSortLoans(Loan)` is called automatically by the Spring Cloud Stream whenever a new `Loan` message arrives. This is because the method is annotated with the `@StreamListener(LoanProcessor.APPLICATIONS_IN)` annotation.

The `Loan` objects are then sorted using simple business logic, and depending on the outcome, sent to either the `processor.approved()` channel or the `processor.declined()` channel (after having their status set accordingly).

## Final Thoughts

As you can see, the separation of concerns introduced between the messaging logic and the messaging infrastructure is very healthy indeed. There is absolutely zero Kafka or RabbitMQ specific code in either application. This allows developers to focus on the business logic regardless of the messaging platform. There is very little messaging boilerplate and you can easily swap messaging solutions simply by changing the "binder" dependencies in the application POM.

## There's More...

Keep up to date with the latest information on Spring Cloud Stream visit the projects dedicated [website][project]. At the time of writing, the messaging platforms supported by bindings for Spring Cloud Stream include [Kafka][kafka], [RabbitMQ][rabbit], [Amazon Kinesis][amazon], [Google PubSub][google], and [Azure Event Hub][azure].

To create your own Spring project from scratch, use the project configurator at [start.spring.io][initializr].

If you'd like to go deeper with Spring and pure Kafka? Check out these great blog posts:

1. [Gary Russell: Spring for Apache Kafka Deep Dive: Error Handling, Message Conversion and Transaction Support][blog1]

2. [Soby Chacko: Spring for Apache Kafka Deep Dive: Apache Kafka and Spring Cloud Stream][blog2]

[project]: https://spring.io/projects/spring-cloud-stream
[docs]: https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/
[initializr]: https://start.spring.io
[multi-connect]:https://cloud.spring.io/spring-cloud-static/spring-cloud-stream/2.2.1.RELEASE/spring-cloud-stream.html#multiple-systems

[rabbit]: https://github.com/spring-cloud/spring-cloud-stream-binder-rabbit
[kafka]: https://github.com/spring-cloud/spring-cloud-stream-binder-kafka
[amazon]: https://github.com/spring-cloud/spring-cloud-stream-binder-aws-kinesis
[azure]: https://github.com/microsoft/spring-cloud-azure/tree/master/spring-cloud-azure-stream-binder/spring-cloud-azure-eventhubs-stream-binder
[google]: https://github.com/spring-cloud/spring-cloud-gcp/tree/master/spring-cloud-gcp-pubsub-stream-binder

[blog1]: https://www.confluent.io/blog/spring-for-apache-kafka-deep-dive-part-1-error-handling-message-conversion-transaction-support
[blog2]: https://www.confluent.io/blog/spring-for-apache-kafka-deep-dive-part-2-apache-kafka-spring-cloud-stream
