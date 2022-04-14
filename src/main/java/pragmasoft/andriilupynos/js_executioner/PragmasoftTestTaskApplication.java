package pragmasoft.andriilupynos.js_executioner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PragmasoftTestTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(PragmasoftTestTaskApplication.class, args);
    }

}
