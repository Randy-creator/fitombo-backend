package com.fitombo.app;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(
    properties = {
      "spring.docker.compose.enabled=false",
      "app.security.jwt.secret=fitombo-integration-test-only-key-material"
    })
@Transactional
public abstract class ApiIntegrationTest {

  @Autowired private WebApplicationContext context;

  protected MockMvc mvc;

  @BeforeEach
  void configurerMvc() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  protected RequestPostProcessor utilisateur(int numero) {
    return jwt().jwt(token -> token.subject("10000000-0000-4000-8000-00000000000" + numero));
  }
}
