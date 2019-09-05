# Recipe: Using Kafka OR Rabbit with Spring 

## Problem

Event driven architecture often uses messaging but connecting and using messaging servers can be cumbersome. Furthermore, enterprises often need to use several different messaging technologies within one domain. Code can get so complex and so proprietary that flexibility is reduced.

## Solution

Spring Cloud Stream can abstract away the complicated setup and integration plumbing leaving you with nice clean code which anyone can maintain.  Spring Cloud Stream unifies many disparate messaging protocols behind one, easy to use API. Spring Cloud Stream is designed to smooth away any subtle differences in approach or features (like partitioning for example) so that you can concentrate on building the solution rather than compensating for shortfalls or incompatibilities between messaging implementations. 

## How to run the demo

This demo has two modes (implemented as Maven profiles) - Kafka mode or RabbitMQ mode. Which you choose is entirely up to you. 

#### Step 1: Start the messaging servers

To make this simple, we'll use `docker-compose` and we'll start both the Kafka and Rabbit servers at the same time and leave them running in the background.

To start them up, in a fresh terminal window, go to the root folder of this repository and issue the following command.

```bash
./docker-compose up
```

#### Step 2: Decide which mode you want to try, Kafka or Rabbit

##### For Kafka

If you want to run the code against Kafka, in the following Maven commands, where it says `-P<profile-choice>` substitute **`-Pkafka`**. 

##### For RabbitMQ

If you want to run the code against RabbitMQ, in the following Maven commands, where it says `-P<profile-choice>` substitute the profile **`-Prabbit`**. 

#### Step 3: Generate some messages

In a fresh terminal window, make the `/loansource` directory the current directory, and then issue the following command (substituting the profile name as discussed in step 2 above).

```bash
./mvnw package spring-boot:run -DskipTests=true -P<profile-choice>
```

Once started, in the log output in the terminal window, you should see a message every second telling you that a new Loan application has been created and sent for processing.

#### Step 4: Process some messages

In a fresh terminal window, make the `/loancheck` directory the current directory, and then issue the following command (substituting the profile name as discussed in step 2 above).

```bash
./mvnw package spring-boot:run -DskipTests=true -P<profile-choice>
```

Once started, in the log output in the terminal window, you should see a message every second telling you that a new Loan application has been processed and either accepted or rejected.

#### Step 5: Tidy Up

Once you're done, press `ctrl-c` in each terminal window to stop the microservices and stop the messaging servers.

## How it works

Both apps are configured using a combination of Spring Boot autoconfiguration (based on dependencies) and regular `application.properties` entries (of which there are very few). 

Your chosen Maven profile controls which Spring Cloud Stream bindings are added as dependencies. If you choose `-Pkafka` then the `spring-cloud-stream-binder-kafka` JAR is added. If you choose `-Prabbit` then the `spring-cloud-stream-binder-rabbit` JAR is added. Your choice also influences the `spring.profiles.active` property and the banner used at boot time (just so it's clear which mode you're running with in the console when you start).

The `loansink` application you started is generating `Loan` objects and serialising them as messages onto a Spring Cloud Stream topic called "applications". Under the hood, Spring Cloud Stream is working with either Kafka or RabbitMQ to establish the necessary messaging topic. The `loansink` is using the Spring scheduling feature alongside Spring Cloud Stream messaging to generate application messages and put them on the messaging topic.

Similarly, the `loancheck` application you started is subscribing to the messages coming from the `applications` topic and then sending them into either the `approved` or `declined` topics based on the message's content (specifically the amount of the loan, similar to a fraud checking facility).

You'll notice there is zero Kafka or RabbitMQ specific code in this sample. This means the solution can focus almost exclusively on the business logic, bar a few configuration items.

This allows you to focus on the business logic and use exactly the same solution on different messaging platforms. You could swap out Kafka or RabbitMQ for any of the other supported messaging solutions simply by changing the "binder" dependencies in the application POMs.

## Theres more

Keep up to date with the latest information on Spring Cloud Stream visit the projects dedicated [website][1]. At the time of writing, the messaging platforms supported by Spring Cloud Stream include Kafka, RabbitMQ, Amazon Kinesis, Google PubSub, and Azure Event Hub.

To create your own messaging project from scratch, use the project configurator at [start.spring.io][2]

Want to go deeper with Spring and pure Kafka? Check out these great blog posts:

1. [Gary Russell: Spring for Apache Kafka Deep Dive: Error Handling, Message Conversion and Transaction Support][3]

2. [Soby Chacko: Spring for Apache Kafka Deep Dive: Apache Kafka and Spring Cloud Stream][4]

[1]: https://spring.io/projects/spring-cloud-stream
[2]: https://start.spring.io
[3]: https://www.confluent.io/blog/spring-for-apache-kafka-deep-dive-part-1-error-handling-message-conversion-transaction-support
[4]: https://www.confluent.io/blog/spring-for-apache-kafka-deep-dive-part-2-apache-kafka-spring-cloud-stream
