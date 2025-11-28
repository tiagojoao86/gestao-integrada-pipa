package br.com.grupopipa.gestaointegrada.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class FlywayIniatilizer {

    private Flyway flyway;

    public FlywayIniatilizer(Flyway flyway) {
        this.flyway = flyway;
    }

    @PostConstruct
    public void iniatilizer() {
        this.flyway.migrate();
    }
}
