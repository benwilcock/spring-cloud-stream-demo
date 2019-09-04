package io.pivotal.loancheck;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableBinding(ApplicationsBinding.class)
public class LoancheckApplication {

	public static final Logger LOG = LoggerFactory.getLogger(LoancheckApplication.class);
	public static final List<LoanApplication> applications = new ArrayList<>();

	public static void main(String[] args) {
		SpringApplication.run(LoancheckApplication.class, args);
	}


	@Component("LoanChecker")
	public static class LoanChecker{

		ApprovedSender approver;
		DeclinedSender decliner;
		Long MAX_AMOUNT = 1000L;

		@Autowired
		public LoanChecker(ApprovedSender approver, DeclinedSender decliner) {
			this.approver = approver;
			this.decliner = decliner;
		}

		@StreamListener(ApplicationsBinding.APPLICATIONS_IN)
		public void process(LoanApplication application) {
			LOG.info("Application {} for ${} for {}", application.getUuid(), application.getAmount(), application.getName());

			if(application.getAmount() > MAX_AMOUNT){
				decliner.decline(application);
			} else {
				approver.approve(application);
			}
		}
	}

	@Component("ApprovedSender")
	public static class ApprovedSender{

		@SendTo(ApplicationsBinding.APPROVED_OUT)
		public LoanApplication approve(LoanApplication application){
			application.setStatus(Statuses.APPROVED.name());
			LOG.info("{} {} for ${} for {}", application.getStatus(), application.getUuid(), application.getAmount(), application.getName());
			return application;
		}
	}

	@Component("DeclinedSender")
	public static class DeclinedSender{

		@SendTo(ApplicationsBinding.DECLINED_OUT)
		public LoanApplication decline(LoanApplication application){
			application.setStatus(Statuses.DECLINED.name());
			LOG.info("{} {} for ${} for {}", application.getStatus(), application.getUuid(), application.getAmount(), application.getName());
			return application;
		}
	}
}
