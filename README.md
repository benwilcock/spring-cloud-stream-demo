## Problem

You just want to write logic for your event driven application, but the boilerplate messaging code keeps getting in the way and it's costing you time.  Connecting your apps to messaging servers is cumbersome and you need to work with multiple messaging technologies in your organisation depending on which team you're working in.

## Solution

Spring Cloud Stream takes care of the complicated boilerplate code for you, leaving you free to create nice clean business logic which anyone can maintain.  Spring Cloud Stream seamlessly unifies many different messaging protocols behind one easy to use API and it smoothes away any subtle differences in approach or features (like partitioning or exchanges for example) so that you can concentrate on building event-driven solutions that "just work".

For the rest of this recipe [see the website here][recipe].

## Pre-requisites

These event driven applications are built on: [Spring Boot][boot], [Spring Cloud Stream][stream], [Maven][maven], and [Java 8][java].

This server-side runs on [Docker for Mac][docker] and includes: [Kafka][kafka], [Zookeeper][zookeeper], [RabbitMQ][rabbit], and [KafDrop][kafdrop] (image by by Obsidian Dynamics).

[recipe]: https://benwilcock.github.io/spring-cloud-stream-demo/
[stream-docs]: https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/
[boot]: https://spring.io/projects/spring-boot
[stream]: https://spring.io/projects/spring-cloud-stream
[maven]: https://maven.apache.org/
[java]: https://adoptopenjdk.net/
[docker]: https://www.docker.com/
[kafka]: https://kafka.apache.org/
[zookeeper]: https://zookeeper.apache.org/
[rabbit]: https://www.rabbitmq.com/
[kafdrop]: https://hub.docker.com/r/obsidiandynamics/kafdrop
