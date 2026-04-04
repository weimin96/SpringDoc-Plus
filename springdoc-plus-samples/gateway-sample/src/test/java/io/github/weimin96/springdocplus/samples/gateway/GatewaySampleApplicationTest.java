package io.github.weimin96.springdocplus.samples.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewaySampleApplicationTest {

    @LocalServerPort
    private int port;

    @Test
    void uiConfigEndpointIsAvailable() {
        WebTestClient webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        webTestClient.get()
                .uri("/springdoc-plus-gateway/ui-config")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.tagsSorter").exists()
                .jsonPath("$.operationsSorter").exists();
    }

    @Test
    void groupsEndpointIsAvailable() {
        WebTestClient webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        webTestClient.get()
                .uri("/springdoc-plus-gateway/openapi/groups")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.groups").isArray();
    }
}
