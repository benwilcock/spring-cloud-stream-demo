# Recipe: Using Kafka via Spring Cloud Stream 

## Problem

Event driven architecture often uses messaging but connecting and using messaging servers can be cumbersome.

## Solution

Spring Cloud Stream can do the heavy lifting, abstracting away the complicated setup and integration plumbing behind one, easy to use messaging API. Spring Cloud Stream is designed to connect to multiple messaging platforms and any subtle differences in approach or features (like partitioning for example) are handled by the Spring Cloud Stream framework. 

## How to run the demo

1. Start two terminal windows. In the fist, start your local Kafka server on Docker. You can use the `run-kafka.sh` script in this folder to get your single node cluster running.

2. Next, `cd` to the `loansink` folder and then type `./mvnw package spring-boot:run`

3. Now, in the second terminal window, `cd` to the `loancheck` folder and run `./mvnw package spring-boot:run`.

## How it works

The `loansink` application in the first terminal window is generating `LoanApplication` objects and serialising them as messages onto a Kafka topic called "applications".

The `loancheck` application in the second terminal window is subscribing to the messages coming from the `applications` topic and then sending them into `approved` or `declined` based on the message's content (specifically the name of the loan applicant, similar to a fraud checking facility).

The code for this is quite simple. The `loansink` is using the spring Scheduling feature alongside Spring Cloud Stream messaging to generate application messages and put them on a Kafka topic. You'll notice there is zero Kafka specific code here.

Similarly, the `loancheck` application is using Spring Cloud Stream to subscribe to the Kafka topic, and it too features zero Kafka specific code.

Both apps are configured using a combination of Spring Boot autoconfiguration (based on dependencies) and regular `application.properties` entries (of which there are very few). 

This lack of Kafka specific code allows you to use the same solution on different messaging platforms easily. You could swap Kafka for RabbitMQ etc. simply by changing the runtime dependencies in the POMs.

## Theres more

Keep up to date with the latest information on Spring Cloud Stream on the projects dedicated [website][1]. At the time of writing, the messaging platforms supported by Spring Cloud Stream include Kafka, RabbitMQ, Amazon Kinesis, Google PubSub, and Azure Event Hub.

To create your own messaging project from scratch, use the project configurator at [start.spring.io][2]

Want to go deeper with Spring and pure Kafka? Check out these great blog posts:

1. [Gary Russell: Spring for Apache Kafka Deep Dive: Error Handling, Message Conversion and Transaction Support][3]

2. [Soby Chacko: Spring for Apache Kafka Deep Dive: Apache Kafka and Spring Cloud Stream][4]




[1]: https://spring.io/projects/spring-cloud-stream
[2]: https://start.spring.io
[3]: https://www.confluent.io/blog/spring-for-apache-kafka-deep-dive-part-1-error-handling-message-conversion-transaction-support
[4]: https://www.confluent.io/blog/spring-for-apache-kafka-deep-dive-part-2-apache-kafka-spring-cloud-stream