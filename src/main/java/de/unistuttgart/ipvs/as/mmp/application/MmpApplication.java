package de.unistuttgart.ipvs.as.mmp.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "de.unistuttgart.ipvs.as.mmp")
@ComponentScan(basePackages = {"de.unistuttgart.ipvs.as.mmp"})
@EntityScan(basePackages = "de.unistuttgart.ipvs.as.mmp")
@EnableJpaRepositories(basePackages = "de.unistuttgart.ipvs.as.mmp")
@EnableConfigurationProperties
public class MmpApplication {

    public static void main(String[] args) {
        SpringApplication.run(MmpApplication.class, args);
    }

}
