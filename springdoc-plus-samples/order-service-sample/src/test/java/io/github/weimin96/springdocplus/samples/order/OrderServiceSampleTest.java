package io.github.weimin96.springdocplus.samples.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.weimin96.springdocplus.samples.order.controller.OrderController;
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
    void documentViewEndpointReturnsWrappedDto() throws Exception {
        HttpResponse<String> response = get("/orders/7/document-view");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"code\":200");
        assertThat(response.body()).contains("\"orderNo\":\"SO-7\"");
        assertThat(response.body()).contains("\"customerName\":\"华东采购中心\"");
    }

    @Test
    void fulfillmentNodesEndpointReturnsWrappedList() throws Exception {
        HttpResponse<String> response = get("/orders/7/fulfillment-nodes");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"nodeCode\":\"PICKING\"");
        assertThat(response.body()).contains("\"nodeCode\":\"SHIPPING\"");
        assertThat(response.body()).contains("\"traceId\":\"trace-order-fulfillment-7\"");
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

    @Test
    void openApiSpecContainsWrappedReferenceSchemas() throws Exception {
        HttpResponse<String> response = get("/v3/api-docs");
        assertThat(response.statusCode()).isEqualTo(200);

        JsonNode spec = OBJECT_MAPPER.readTree(response.body());
        JsonNode documentWrapperSchema = spec.path("components").path("schemas").path("CommonResultOrderDocumentDto");
        JsonNode fulfillmentWrapperSchema = spec.path("components").path("schemas").path("CommonResultListOrderFulfillmentNodeDto");

        assertThat(spec.path("paths").has("/orders/{id}/document-view")).isTrue();
        assertThat(spec.path("paths").has("/orders/{id}/fulfillment-nodes")).isTrue();
        assertThat(documentWrapperSchema.isMissingNode()).isFalse();
        assertThat(documentWrapperSchema.path("properties").path("data").path("$ref").asText())
                .isEqualTo("#/components/schemas/OrderDocumentDto");
        assertThat(spec.path("components").path("schemas").path("OrderDocumentDto").path("properties").has("customerName"))
                .isTrue();
        assertThat(fulfillmentWrapperSchema.isMissingNode()).isFalse();
        assertThat(fulfillmentWrapperSchema.path("properties").path("data").path("type").asText())
                .isEqualTo("array");
        assertThat(fulfillmentWrapperSchema.path("properties").path("data").path("items").path("$ref").asText())
                .isEqualTo("#/components/schemas/OrderFulfillmentNodeDto");
        assertThat(spec.path("components").path("schemas").path("OrderFulfillmentNodeDto").path("properties").has("nodeName"))
                .isTrue();
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
