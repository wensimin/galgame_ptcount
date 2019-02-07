package shali.tech.ptcount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import shali.tech.ptcount.config.SynonymConfig;

@SpringBootApplication
@EnableConfigurationProperties(SynonymConfig.class)
public class GalgamePtcountApplication {

	public static void main(String[] args) {
		SpringApplication.run(GalgamePtcountApplication.class, args);
	}

}

