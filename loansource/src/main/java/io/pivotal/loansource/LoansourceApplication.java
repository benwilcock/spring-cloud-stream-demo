package io.pivotal.loansource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

@SpringBootApplication
public class LoansourceApplication {

  private static final Logger log = LoggerFactory.getLogger(LoansourceApplication.class);
  private List<String> names = Arrays.asList("Donald", "Theresa", "Vladimir", "Angela", "Emmanuel", "Shinzō", "Jacinda", "Kim");
  private List<Long> amounts = Arrays.asList(10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 100000000L);

  public static void main(String[] args) {
    SpringApplication.run(LoansourceApplication.class, args);
    log.info("The Loansource Application has started...");
  }

  @Bean
  public Supplier<Loan> supplyLoan(){

    Supplier<Loan> loanSupplier = () -> {
      Loan loan = new Loan(UUID.randomUUID().toString(),
              names.get(new Random().nextInt(names.size())),
              amounts.get(new Random().nextInt(amounts.size())));
      log.info("{} {} for ${} for {}", loan.getStatus(), loan.getUuid(), loan.getAmount(), loan.getName());
      return loan;
    };

    return loanSupplier;
  }
}
