# Simple Event Driven Microservices

## The Problem

You just want to write logic for your event driven application, but the boilerplate messaging code is getting in the way and it's costing you time.  Connecting your apps to event messaging services is cumbersome, plus you need to work with multiple messaging technologies in your work (depending on the domain you're working in).

## The Solution

Use a flexible messaging abstraction like [Spring Cloud Stream][docs] to take care of the complex messaging platform integration. With this solution in place, you can concentrate on writing simple clean buiness logic which anyone can maintain. Spring Cloud Stream unifies lots of messaging platforms behind one simple API. It smoothes away the subtle differences in approach or features between platforms (like partitioning or exchanges for example) leaving you free to create innovative event-driven solutions.

## How to do it

When you run this demo you'll see exactly how Spring Cloud Stream's clever abstractions can help to make your stream handling code cleaner and easier to work with. You'll also see how trivial it is to switch between different messaging technologies using readily available `binding` libraries. 

> This demo supports the **[Kafka][kafka]** and **[RabbitMQ][rabbit]** bindings but others are available including [Amazon Kinesis][amazon], [Azure Event Hub][azure], [Google PubSub][google], and more.

### Getting Started

Clone the code repository from GitHub. To do this, in a new terminal window issue the following command.

```bash
git clone https://github.com/benwilcock/spring-cloud-stream-demo.git
```

Upon inspection you'll notice that this repository consists of two microservices. The first microservice (`loansource`) acts as the source of  event messages. These events are Bank Loan applications. Each loan application has a "name", an "amount", and a "status". The second microservice (`loancheck`) is an event processor which checks these loan requests and sorts them into "approved" or "declined".

To run the demo, follow the 5 steps below.

#### Step 1: Start the Messaging Servers

To make this easy we've included a `docker-compose` configuration which will start both the [Kafka][kafka-project] and the [RabbitMQ][rabbit-project] servers at the same time and then leave them running in the background.

In a fresh terminal window, go to the root folder of this repository and issue the following command.

```bash
./start-servers.sh
```

> You'll need ["Docker for Mac"][docker-for-mac] to be installed and running on your system for this script to work properly.

This will start Kafka and RabbitMQ and stream the log output from both (unless you exit the log tail with `Ctrl-C`). Once started these servers will all be available to applications running on your computer. 

> Note: The servers do not stop when you press `Ctrl-C` - they'll keep running in the background.

#### Step 2: Choose Between Kafka or RabbitMQ Modes

In steps 3 & 4 which follow, where we issue our Maven commands to compile and run the microservices, we must substitute the **`-P<profile-choice>`** with the name of the messaging mode in which we'd like run.

* For **Kafka** mode, substitute: **`-Pkafka`**
* For **RabbitMQ** mode, substitute: **`-Prabbit`** 

> Note: This demo is __not__ designed to "bridge" messages between Kafka and Rabbit, so be sure to choose the same profile name in each of the two applications when you compile and run them. If bridging messaging systems is your goal [see the documentation here][multi-connect].

#### Step 3: Generate the Loan Events

In a new terminal window, make the `/loansource` directory the current directory, and then issue the following command substituting the `<profile-choice>` with the mode you'd like to run with (either `kafka` or `rabbit` mode as discussed in step 2 above).

```bash
./mvnw clean package spring-boot:run -DskipTests=true -P<profile-choice>
```

Once the `loansource` application has started, in the terminal window, you should see a message every second telling you that a new Loan event has been created and it's in the `PENDING` state. Leave this mocroservice running in the terminal and move onto the next step.

#### Step 4: Process the Loan Events

In another new terminal window, make the `/loancheck` directory your current directory, and then issue the following command, again substituting the `<profile-choice>` with the mode you'd like to run with (either `kafka` or `rabbit` mode as discussed in step 2 above).

```bash
./mvnw clean package spring-boot:run -DskipTests=true -P<profile-choice>
```

Once the `loancheck` application has started, in the terminal window, you should see a message every second telling you that a new Loan application has been either `APPROVED` or `DECLINED`.

#### Step 5: Stopping the Demo

Once you're done with the demo applications, in each of the terminal windows for the `/loansource` and the `/loancheck` microservices press `Ctrl-C`. The application will come to a halt and message processing will stop.

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

All that's required to get the `LoansourceApplication` microservice to act as a source of `Loan` messages is to declare an `@Bean` method which generates and returns a `Supplier<>`, in this case it's a `Supplier` of type `Loan`.

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

`Supplier<>` is a Java function data type. Because there is only one `@Bean` that returns this type in this application, Spring Cloud Stream knows exactly what to do next. By default it will trigger this function once every second and send the result to the default "output" `MessageChannel`.

> What's nice about this is that you can test the function using a regular unit test.

#### The Loancheck Microservice

The `loancheck` microservice is a bit more complex, but not much. It's job is to sort the `Loan` events into channels. In order to do this, it is subscribing to the events coming from the source's `output` topic and then sending them into either the `approved` or `declined` topics based on the message's content (specifically the amount of the loan, similar to a fraud checking facility).

Beacuse 3 message channels are involved, a simple `LoanProcessor` interface is used to clarify the input and output `MessageChannel`'s. It looks something like this:

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

A `@Component` in the code called the `LoanChecker` is constructed with this `LoanProcessor`. Then, it's `checkAndSortLoans(Loan)` method is called automatically by Spring Cloud Stream whenever a new `Loan` event arrives. This is because the method is annotated with the `@StreamListener(LoanProcessor.APPLICATIONS_IN)` annotation.

The `Loan` objects are then sorted using simple business logic, and depending on the outcome, sent to either the `processor.approved()` channel or the `processor.declined()` channel (after having their status set accordingly).

#### Observing the Event Messages

You can see the events flowing through the messaging platforms as follows.

* For **Kafka** the [KafDrop][kafdrop] tool on [`localhost:9000`][kafdrop-ui] may be used to observe the topics and the event messages. There is no login required.

* For **RabbitMQ** the Rabbit Management Console can be found on [`localhost:15672`][rabbit-ui] may be used to observe the exchanges and the event messages. To login the username is `guest` and the password is also `guest`. To observe actual message content, you may need to create a queue manually and bind it to the topic using `#` as your routing key.

## Final Thoughts

As you can see, the separation of concerns you get with [Spring Cloud Streams][project] between the messaging logic and the messaging platform is very healthy indeed. There is absolutely zero Kafka or RabbitMQ specific code in either microservice. This allows us to focus more time on the business logic regardless of the messaging platform. There is very little messaging code required and you can easily swap messaging solutions simply by changing the "binder" dependencies in the application POM.

## There's More...

To keep up to date with the latest information on Spring Cloud Stream visit the projects dedicated [project page][project] on the Spring website. At the time of writing, the messaging platforms supported as bindings for Spring Cloud Stream include [Kafka][kafka], [RabbitMQ][rabbit], [Amazon Kinesis][amazon], [Google PubSub][google], and [Azure Event Hub][azure].

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

[kafka-project]: https://kafka.apache.org/
[rabbit-project]: https://www.rabbitmq.com/
[kafdrop]: https://hub.docker.com/r/obsidiandynamics/kafdrop
[kafdrop-ui]: http://localhost:9000
[rabbit-ui]: http://localhost:15672
[docker-for-mac]: https://docs.docker.com/docker-for-mac/install/

[blog1]: https://www.confluent.io/blog/spring-for-apache-kafka-deep-dive-part-1-error-handling-message-conversion-transaction-support
[blog2]: https://www.confluent.io/blog/spring-for-apache-kafka-deep-dive-part-2-apache-kafka-spring-cloud-stream
