package io.pivotal.loansource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

@SpringBootApplication
@EnableBinding(Source.class)
public class LoansourceApplication {

  private static final Logger LOG = LoggerFactory.getLogger(LoansourceApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(LoansourceApplication.class, args);
    LOG.info("Loansource Application has started...");
  }

  private List<String> names = Arrays.asList("Donald", "Theresa", "Vladimir", "Angela", "Emmanuel", "Shinzō", "Jacinda", "Kim");
  private List<Long> amounts = Arrays.asList(1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 100000000L);

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
