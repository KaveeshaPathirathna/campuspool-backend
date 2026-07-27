package backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class BackendApplication {

	// FIX: Force Java JVM to use Sri Lanka Timezone (Asia/Colombo)
	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Colombo"));
	}

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}