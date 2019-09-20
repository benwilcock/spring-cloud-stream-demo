package io.pivotal.loancheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class LoanChecker {

  public static final Logger log = LoggerFactory.getLogger(LoanChecker.class);
  private static final Long MAX_AMOUNT = 10000L;
  private LoanProcessor processor;

  @Autowired
  public LoanChecker(LoanProcessor processor) {
    this.processor = processor;
  }

  @StreamListener(LoanProcessor.APPLICATIONS_IN)
  public void checkAndSortLoans(Loan loan) {
    log.info("{} {} for ${} for {}", loan.getStatus(), loan.getUuid(), loan.getAmount(), loan.getName());

    if (loan.getAmount() > MAX_AMOUNT) {
      loan.setStatus(Statuses.DECLINED.name());
      processor.declined().send(message(loan));
    } else {
      loan.setStatus(Statuses.APPROVED.name());
      processor.approved().send(message(loan));
    }

    log.info("{} {} for ${} for {}", loan.getStatus(), loan.getUuid(), loan.getAmount(), loan.getName());

  }

  private static final <T> Message<T> message(T val) {
    return MessageBuilder.withPayload(val).build();
  }
}
