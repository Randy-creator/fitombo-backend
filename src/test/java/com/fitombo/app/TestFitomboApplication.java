package com.fitombo.app;

import org.springframework.boot.SpringApplication;

public class TestFitomboApplication {

  public static void main(String[] args) {
    SpringApplication.from(FitomboApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
