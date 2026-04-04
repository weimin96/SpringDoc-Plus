package io.github.weimin96.springdocplus.samples.gateway;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class GatewaySampleApplicationMainTest {

    @Test
    void mainDelegatesToSpringApplication() {
        try (MockedStatic<SpringApplication> application = Mockito.mockStatic(SpringApplication.class)) {
            GatewaySampleApplication.main(new String[] {"--server.port=0"});

            application.verify(() -> SpringApplication.run(GatewaySampleApplication.class, new String[] {"--server.port=0"}));
        }
    }
}
