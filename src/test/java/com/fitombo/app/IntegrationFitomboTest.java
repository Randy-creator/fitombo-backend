package com.fitombo.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(TestcontainersConfiguration.class)
@SpringBootTest(
    properties = {
      "spring.docker.compose.enabled=false",
      "app.cors.allowed-origins=",
      "app.security.jwt.secret=fitombo-integration-test-only-key-material"
    })
@interface IntegrationFitomboTest {}
