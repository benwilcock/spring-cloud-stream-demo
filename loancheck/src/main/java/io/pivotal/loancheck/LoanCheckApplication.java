package io.pivotal.loancheck;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;

@SpringBootApplication
@EnableBinding(LoanProcessor.class)
public class LoanCheckApplication {

  public static final Logger LOG = LoggerFactory.getLogger(LoanCheckApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(LoanCheckApplication.class, args);
    LOG.info("The Loancheck Application has started...");
  }
}
