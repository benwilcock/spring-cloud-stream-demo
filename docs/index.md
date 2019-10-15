# Simple Event Driven Microservices with Spring Cloud Stream

Event driven architecture is great. But without a framework, writing the scaffolding required to work with popular event messaging platforms can be messy. In this post we'll take a look at how  [Spring Cloud Stream][project] can be used to simplify your code.

## The Problem

You just want to write logic for your event driven application, but the boilerplate messaging code can get in the way.  Connecting your apps to messaging services is tricky, and if you're an enterprise developer, you probably need to work with multiple messaging technologies (either on-premises or in the cloud).

## The Solution

Let a flexible messaging abstraction take care of the complex messaging platform integration so you can concentrate on writing simple clean business logic. [Spring Cloud Stream][docs] is a great candidate. It unifies lots of popular messaging platforms behind one easy to use API including RabbitMQ, Apache Kafka, Amazon Kinesis, Google PubSub, Solace PubSub+, Azure Event Hubs, and Apache RocketMQ. It even smoothes away any subtle differences in approach and features between these platforms (like partitioning or exchanges for example) leaving you free to create innovative event-driven solutions.

In the demo that follows, you'll see exactly how Spring Cloud Stream's clever abstractions help make event streaming code cleaner and easier to work with. You'll also see how trivial it is to switch between two different messaging platforms ([RabbitMQ][rabbit-project] or [Kafka][kafka-project]) using Spring Cloud Stream's `binding` libraries. 

## Before you start

These event driven microservices need the latest of these applications installed on your PC[^1]:

1. [Java 8][java]
2. [Docker][docker-for-mac] (where we'll run RabbitMQ and Kafka locally)
3. [Git][git-install] (optional)
4. Bash (assumed, although alternatives could work) 

## Running The Demo

First, clone the code repository from GitHub. To do this (if you have Git installed) open a new terminal window and issue the following command. If you don't have Git installed, download and extract [this zip file][zip].

```bash
git clone https://github.com/benwilcock/spring-cloud-stream-demo.git
```

Upon inspection of the code you'll notice that this repository consists of two microservices. 

1. The `Loansource` microservice (in the `/loansource` folder). This microservice acts as a source of event messages. These events are `Loan` applications similar to what you'd see in the world of banking and finance. Each loan has a "name", an "amount", and a "status" (which is set to `PENDING` at first).

2. The `Loancheck` microservice (in the `/loancheck` folder). This microservice acts as a `Loan` processor. It checks which loans are good ones to make and sorts them into `APPROVED` or `DECLINED` states.

To run the demo, follow the instructions below.

### Step 1: Start the Messaging Servers

In a fresh terminal window, go to the root folder of the project and issue the following command.

> You'll need ["Docker"][docker-for-mac] to be installed and running on your system for this script to work properly as it requires `docker-compose`.

```bash
./start-servers.sh
```

This script will start [Kafka][kafka-project] and [RabbitMQ][rabbit-project] and stream the log output from both to the terminal window (unless you exit with `Ctrl-C`). The servers do not stop when you press `Ctrl-C` - they'll keep running in the background. Once started these servers will all be available to applications running on your computer.

### Step 2: Choose Between Kafka or RabbitMQ Mode

In steps 3 & 4 which follow, we must substitute the **`-P<profile-choice>`** with the name of the messaging platform which we'd like to use.

* For **Kafka**, use: **`-Pkafka`**
* For **RabbitMQ**, use: **`-Prabbit`**

If you omit the `-P<profile-choice>` setting completely, then Kafka is used.

> Note: This demo is __not__ designed to "bridge" messages between Kafka and RabbitMQ, so be sure to choose the same profile name in each of the two applications when you compile and run them. If bridging messaging systems is your goal [see the documentation here][multi-connect].

### Step 3: Generate Some Loan Events

In a new terminal window, make the `/loansource` directory the current directory using `cd`, and then issue the following command substituting the `<profile-choice>` with the mode you'd like to run (either `kafka` or `rabbit` mode as discussed in step 2 above).

```bash
./mvnw clean package spring-boot:run -DskipTests=true -P<profile-choice>
```

Once the `loansource` application has started, in the terminal window, you should see a message every second telling you that a new Loan event has been posted to the messaging platform in the `PENDING` state. Leave this microservice running and move onto the next step.

### Step 4: Process The Loan Events

In another new terminal window, make the `/loancheck` directory your current directory, and then issue the following command, again substituting the `<profile-choice>` with the mode you'd like to run.

```bash
./mvnw clean package spring-boot:run -DskipTests=true -P<profile-choice>
```

Once the `loancheck` application has started, in the terminal window, you should see a message every second telling you that a new `PENDING` Loan application has been read from the messaging platform and either `APPROVED` or `DECLINED`. Skip ahead to "How It Works" if you'd like to understand how these applications were built.

### Step 5: Stop the Demo

Once you're done with the microservices, in each of the terminal windows for the `/loansource` and the `/loancheck` microservices press `Ctrl-C`. The application will come to a halt and the event processing will stop.

If you're switching modes between Kafka and Rabbit, simply go back to **Step 2** and repeat the process.

> If you're completely done with the demo and would also like to stop the Kafka and RabbitMQ servers, in a terminal window in the root folder of the project run the `./stop-servers.sh` script. This isn't necessary if you're just switching between modes.

## How it Works

Maven profiles (in each project's `pom.xml`) control which of the Spring Cloud Stream bindings are added as dependencies when you build. When you choose `-Pkafka` then the `[spring-cloud-stream-binder-kafka][kafka]` dependency is added to the project. When you choose `-Prabbit` then the `[spring-cloud-stream-binder-rabbit][rabbit]` dependency is added.

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

Your choice of Maven profile also influences the `spring.profiles.active` property in the `src/main/resources/application.properties` file which switches the banner you see boot time.

#### The Loansource Microservice

For the Loansource misroservice we're using a new feature from Spring Cloud Stream v2.1 - [Spring Cloud Function support][function-support]. With this new feature, all that's required to get the `LoansourceApplication` microservice to act as a source of `Loan` messages is to declare an `@Bean` method which generates and returns a `Supplier<>`. In this case it's a `Supplier` of type `Loan`. The function method code looks something like this...

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

`Supplier<>` is a Java function data type. Because there is only one `@Bean` method that returns this type, Spring Cloud Stream knows exactly what to do next. By default it will trigger this function once every second and send the result to the default `MessageChannel` named "output". What's nice about this function method is that it only contains business logic so you can test it using a regular unit test.

> We could use the `spring.cloud.function.definition` property in the `application.properties` file to explicitly declare which function bean we want to be bound to binding destinations - but for cases when you only have single `@Bean` defined, this is not necessary.

> If we wanted to use a different poller interval, we can use the `spring.integration.poller.fixed-delay` property in the `application.properties` file.

#### The Loancheck Microservice

The `loancheck` microservice requires a little bit more code, but not much. It's job is to sort the `Loan` events into separate channels. In order to do this, it is subscribing to the events coming from the source's `output` topic and then sending them into either the `approved` or `declined` topics based on the the value of the loan, similar to a fraud checking facility.

Beacuse we're using 3 messaging channels (one inbound and two outbound), a simple `LoanProcessor` interface is used to clarify the inputs and the outputs. Currently, it looks something like this:

```java
@Component
public interface LoanProcessor {

  String APPLICATIONS_IN = "output"; // Topic where the new loans appear
  String APPROVED_OUT = "approved"; // Topic where the approved loans are sent
  String DECLINED_OUT = "declined"; // Topic where the declined loans are sent

  @Input(APPLICATIONS_IN)
  SubscribableChannel sourceOfLoanApplications();

  @Output(APPROVED_OUT)
  MessageChannel approved();

  @Output(DECLINED_OUT)
  MessageChannel declined();
}
```

This `LoanProcessor` interface is first referenced in the `@SpringBootApplication` class (`LoanCheckApplication.java`) as a parameter of the `@EnableBinding()` annotation as you can see below.

```java
@SpringBootApplication
@EnableBinding(LoanProcessor.class)
public class LoanCheckApplication {

  public static void main(String[] args) {
    SpringApplication.run(LoanCheckApplication.class, args);
  }
}
```

In addition, a Spring `@Component` called the `LoanChecker.java` is constructed with this `LoanProcessor` at runtime. Furthermore, this component's `checkAndSortLoans(Loan)` method is called automatically whenever a new `Loan` event arrives because it's been annotated as a `@StreamListener()` for the `LoanProcessor.APPLICATIONS_IN` channel. You can see this annotation being used in the following code sample.

```java
  @StreamListener(LoanProcessor.APPLICATIONS_IN)
  public void checkAndSortLoans(Loan loan) {

    if (loan.getAmount() > MAX_AMOUNT) {
      loan.setStatus(Statuses.DECLINED.name());
      processor.declined().send(message(loan));
    } else {
      loan.setStatus(Statuses.APPROVED.name());
      processor.approved().send(message(loan));
    }
  }
```

This method then sorts the `Loan` objects using simple business logic. Depending on the outcome of the sort it sends them onwards to either the `processor.approved()` channel or the `processor.declined()` channel (after setting their Loan Status accordingly).

## Wrapping Up

As you can see, the separation of concerns that you get when using [Spring Cloud Streams][project] is very healthy indeed. There is absolutely zero Kafka or RabbitMQ specific code in either microservice. This allows us to focus on the business logic regardless of the messaging platform and you can easily swap messaging platforms simply by changing the "binder" dependencies in the project's `pom.xml`.

## There's More...

You can see the events flowing through the messaging platforms as follows:

* For **Kafka** the [KafDrop][kafdrop] tool on [`localhost:9000`][kafdrop-ui] may be used to observe the topics and the event messages. There is no login required.

* For **RabbitMQ** the Rabbit Management Console can be found on [`localhost:15672`][rabbit-ui] may be used to observe the exchanges and the event messages. To login the username is `guest` and the password is also `guest`. To observe the actual message contents, you may need to create a Queue manually and bind it to the desired topic using `#` as your `routing key`.

To keep up to date with the latest information on Spring Cloud Stream visit the projects dedicated [project page][project] on the Spring website.

To create your own Spring project from scratch, use the project configurator at [start.spring.io][initializr].

If you'd like to go deeper with Spring and pure Kafka check out these great blog posts:

1. [Gary Russell: Spring for Apache Kafka Deep Dive: Error Handling, Message Conversion and Transaction Support][blog1]

2. [Soby Chacko: Spring for Apache Kafka Deep Dive: Apache Kafka and Spring Cloud Stream][blog2]

---
### Footnotes 

[^1]: The microservice code in this repository is written and packaged using [Maven][maven], [Spring Boot][boot], and [Spring Cloud Stream][project]. At runtime the code relies on [Kafka][kafka-project], [Zookeeper][zookeeper-project], [RabbitMQ][rabbit-project], and [KafDrop][kafdrop] (a Docker image by by Obsidian Dynamics). Everything in this list has been provided for you - you don't need to install them.

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
[zookeeper-project]: https://zookeeper.apache.org/
[kafdrop]: https://hub.docker.com/r/obsidiandynamics/kafdrop
[kafdrop-ui]: http://localhost:9000
[rabbit-ui]: http://localhost:15672
[docker-for-mac]: https://docs.docker.com/docker-for-mac/install/

[zip]: https://github.com/benwilcock/spring-cloud-stream-demo/archive/master.zip
[blog1]: https://www.confluent.io/blog/spring-for-apache-kafka-deep-dive-part-1-error-handling-message-conversion-transaction-support
[blog2]: https://www.confluent.io/blog/spring-for-apache-kafka-deep-dive-part-2-apache-kafka-spring-cloud-stream

[boot]: https://spring.io/projects/spring-boot
[maven]: https://maven.apache.org/
[java]: https://adoptopenjdk.net/
[git-install]: https://git-scm.com/book/en/v2/Getting-Started-Installing-Git
[function-support]: https://cloud.spring.io/spring-cloud-stream/reference/html/spring-cloud-stream.html#spring_cloud_function
