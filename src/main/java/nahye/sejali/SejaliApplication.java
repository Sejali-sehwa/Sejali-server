package nahye.sejali;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "nahye.sejali.repository")
@EntityScan(basePackages = "nahye.sejali.entity")
public class SejaliApplication {

	public static void main(String[] args) {
		SpringApplication.run(SejaliApplication.class, args);
	}

}
