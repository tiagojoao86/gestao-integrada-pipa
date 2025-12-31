package br.com.grupopipa.gestaointegrada.config;

import jakarta.annotation.PostConstruct;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Configuration;

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
