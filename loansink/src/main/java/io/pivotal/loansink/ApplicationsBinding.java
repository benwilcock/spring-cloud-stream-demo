package io.pivotal.loansink;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Component
public interface ApplicationsBinding {

  String APPLICATIONS_SINK = "applications";

  @Output(APPLICATIONS_SINK)
  MessageChannel sourceOfLoanApplications();

}
