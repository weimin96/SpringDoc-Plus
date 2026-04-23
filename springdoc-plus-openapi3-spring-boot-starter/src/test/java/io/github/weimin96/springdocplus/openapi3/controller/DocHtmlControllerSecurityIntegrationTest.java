package io.github.weimin96.springdocplus.openapi3.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = DocHtmlControllerSecurityIntegrationTest.TestApplication.class,
        properties = {
                "springdoc-plus.openapi3.enabled=true",
                "springdoc.api-docs.enabled=true"
        })
class DocHtmlControllerSecurityIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void docHtmlRemainsAccessibleWhenOnlyEntryPathIsPermitted() throws Exception {
        HttpResponse<String> response = get("/doc.html");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("springdoc-plus-ui");
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    @SpringBootApplication
    static class TestApplication {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers("/doc.html", "/springdoc-plus-gateway/**").permitAll()
                            .anyRequest().denyAll())
                    .build();
        }
    }
}
