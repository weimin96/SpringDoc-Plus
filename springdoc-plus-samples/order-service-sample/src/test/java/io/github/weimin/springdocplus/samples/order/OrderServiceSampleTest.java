package io.github.weimin.springdocplus.samples.order;

import io.github.weimin.springdocplus.samples.order.controller.OrderController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceSampleTest {

    @Autowired
    private OrderController controller;

    @LocalServerPort
    private int port;

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
    }

    @Test
    void orderEndpointReturnsExpectedPayload() throws Exception {
        HttpResponse<String> response = get("/orders/7");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"id\":7");
        assertThat(response.body()).contains("\"status\":\"PAID\"");
    }

    @Test
    void starterEndpointsAreExposed() throws Exception {
        HttpResponse<String> groups = get("/springdoc-plus-gateway/openapi/groups");
        assertThat(groups.statusCode()).isEqualTo(200);
        assertThat(groups.body()).contains("\"name\":\"default\"");

        HttpResponse<String> uiConfig = get("/springdoc-plus-gateway/ui-config");
        assertThat(uiConfig.statusCode()).isEqualTo(200);
        assertThat(uiConfig.body()).contains("\"tagsSorter\"");
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
