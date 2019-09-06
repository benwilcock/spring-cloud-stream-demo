package io.pivotal.loancheck;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Component
public interface LoanApplicationsBinding {

  String APPLICATIONS_IN = "applications";
  String APPROVED_OUT = "approved";
  String DECLINED_OUT = "declined";

  @Input(APPLICATIONS_IN)
  MessageChannel sourceOfLoanApplications();

  @Output(APPROVED_OUT)
  MessageChannel approved();

  @Output(DECLINED_OUT)
  MessageChannel declined();

}
