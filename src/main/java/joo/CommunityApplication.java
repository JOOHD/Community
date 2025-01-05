package joo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class CommunityApplication {

	private static final Logger logger = LoggerFactory.getLogger(CommunityApplication.class);

	public static void main(String[] args) {
		logger.info("Starting application...");
		SpringApplication.run(CommunityApplication.class, args);
	}
}
