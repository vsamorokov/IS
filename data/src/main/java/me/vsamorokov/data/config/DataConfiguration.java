package me.vsamorokov.data.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("me.vsamorokov.data.repository")
@EntityScan("me.vsamorokov.data.entity")
public class DataConfiguration {
}
