package com.fitombo.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(
    properties = {
      "spring.docker.compose.enabled=false",
      "app.cors.allowed-origins=",
      "app.security.jwt.secret=fitombo-integration-test-only-key-material"
    })
class FitomboApplicationTests {

  @Test
  void contextLoads() {}
}
