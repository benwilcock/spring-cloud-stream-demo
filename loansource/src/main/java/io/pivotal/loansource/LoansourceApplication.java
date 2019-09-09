package io.pivotal.loansource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The @EnableBinding annotation specifies that the `LoansourceApplication` is acting as a `Source` of messages.
 * By declaring our class as a `Source` will are asking Spring Cloud Stream to create a
 * `MessageChannel` (called "output" by default). Any messages we create will be placed on this channel. Spring Cloud
 * Stream will take care of creating this channel using our underlying messaging technology (defined
 * by the Binding JAR on our classpath (either Rabbit or Kafka in this demo, depending on the Maven
 * profile chosen).
 */

@SpringBootApplication
//@EnableBinding(Source.class)
public class LoansourceApplication {

  private static final Logger LOG = LoggerFactory.getLogger(LoansourceApplication.class);
  private List<String> names = Arrays.asList("Donald", "Theresa", "Vladimir", "Angela", "Emmanuel", "Shinzō", "Jacinda", "Kim");
  private List<Long> amounts = Arrays.asList(1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 100000000L);

  public static void main(String[] args) {
    SpringApplication.run(LoansourceApplication.class, args);
    LOG.info("The Loansource Application has started...");
  }

  /**
   * This @Bean is returning a `Supplier` (function). The function simply generates `Loan` objects.
   * Because this function's method name is declared in the `spring.cloud.stream.function.definition` property,
   * these Loan objects are then automatically sent as messages on our default message channel (called "output"
   * by default) by Spring Cloud Stream (via Spring Cloud Function).
   */
  @Bean
  public Supplier<Loan> supplyLoan() {
    return () -> {
      String rName = names.get(new Random().nextInt(names.size()));
      Long rAmount = amounts.get(new Random().nextInt(amounts.size()));
      Loan loan = new Loan(UUID.randomUUID().toString(), rName, rAmount);
      LOG.info("Created: {}", loan);
      return loan;
    };
  }
}
