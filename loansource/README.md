# Loansink

This application generates loan applications every few seconds and places them as messages onto the
queue configured in `application.properties`.

The target messaging system is determined by Spring Cloud Stream based
on the Autoconfiguration mechanism for Spring Boot.

Therefore, if `spring-kafka is on the classpath, Kafka messaging is used.

```xml
<dependency>
	<groupId>org.springframework.kafka</groupId>
	<artifactId>spring-kafka</artifactId>
</dependency>
``` 