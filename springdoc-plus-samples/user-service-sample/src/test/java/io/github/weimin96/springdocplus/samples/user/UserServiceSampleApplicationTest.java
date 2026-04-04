package io.github.weimin96.springdocplus.samples.user;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceSampleApplicationTest {

    @LocalServerPort
    private int port;

    @Test
    void userEndpointsAreAvailable() throws Exception {
        HttpResponse<String> byId = get("/users/3");
        assertThat(byId.statusCode()).isEqualTo(200);
        assertThat(byId.body()).contains("\"id\":3");
        assertThat(byId.body()).contains("\"name\"");

        HttpResponse<String> byStatus = get("/users/by-status/ACTIVE");
        assertThat(byStatus.statusCode()).isEqualTo(200);
        assertThat(byStatus.body()).contains("\"status\":\"ACTIVE\"");
    }

    @Test
    void starterEndpointsAreExposed() throws Exception {
        HttpResponse<String> groups = get("/springdoc-plus-gateway/openapi/groups");
        assertThat(groups.statusCode()).isEqualTo(200);
        assertThat(groups.body()).contains("\"name\":\"default\"");

        HttpResponse<String> uiConfig = get("/springdoc-plus-gateway/ui-config");
        assertThat(uiConfig.statusCode()).isEqualTo(200);
        assertThat(uiConfig.body()).contains("\"tagsSorter\"");
        assertThat(uiConfig.body()).contains("\"operationsSorter\"");
    }

    @Test
    void mainDelegatesToSpringApplication() {
        try (MockedStatic<SpringApplication> application = Mockito.mockStatic(SpringApplication.class)) {
            UserServiceSampleApplication.main(new String[] {"--server.port=0"});

            application.verify(() -> SpringApplication.run(UserServiceSampleApplication.class, new String[] {"--server.port=0"}));
        }
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
