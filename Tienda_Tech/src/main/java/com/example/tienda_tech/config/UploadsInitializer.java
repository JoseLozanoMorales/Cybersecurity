package com.example.tienda_tech.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

import java.nio.file.*;

@Configuration
public class UploadsInitializer {
  @Bean
  CommandLineRunner ensureUploadsRoot(@Value("${app.upload-root}") String root) {
    return args -> {
      Path p = Paths.get(root);
      Files.createDirectories(p);
      System.out.println("[uploads] Root: " + p.toAbsolutePath());
    };
  }
}
