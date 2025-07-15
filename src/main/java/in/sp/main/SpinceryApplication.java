package in.sp.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class SpinceryApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpinceryApplication.class, args);
	}

	
}
