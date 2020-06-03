package de.unistuttgart.ipvs.as.mmp.search.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "de.unistuttgart.ipvs.as.mmp")
@ComponentScan(basePackages = "de.unistuttgart.ipvs.as.mmp")
@EnableJpaRepositories(basePackages = "de.unistuttgart.ipvs.as.mmp")
public class MmpJpaTestConfig {
}
