package io.pivotal.loansink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@SpringBootApplication
@EnableBinding(ApplicationsBinding.class)
@EnableScheduling
public class LoansinkApplication {

	private static final Logger LOG = LoggerFactory.getLogger(LoansinkApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(LoansinkApplication.class, args);
	}

	@Component("TimerSource")
	public static class TimerSource {

		private final MessageChannel applicationsOut;

		private List<String> names = Arrays.asList("Donald", "Theresa", "Vladimir", "Angela", "Emmanuel", "Shinzō", "Jacinda", "Kim");
		private List<Long> amounts = Arrays.asList(1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 100000000L);

		public TimerSource(ApplicationsBinding binding) {
			this.applicationsOut = binding.sourceOfLoanApplications();
		}

		@Scheduled(fixedRate = 1000L)
		public void timerMessageSource() {
			String rName = names.get(new Random().nextInt(names.size()));
			Long rAmount = amounts.get(new Random().nextInt(amounts.size()));
			LoanApplication application = new LoanApplication(UUID.randomUUID().toString(), rName, rAmount);
			LOG.info("Created: {}", application);
			applicationsOut.send(new GenericMessage<LoanApplication>(application));
		}
	}

}
