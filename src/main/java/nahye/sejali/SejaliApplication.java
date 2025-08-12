package nahye.sejali;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("nahye.sejali.entity")
@EnableScheduling
public class SejaliApplication {
	public static void main(String[] args) {
		SpringApplication.run(SejaliApplication.class, args);
	}
}




