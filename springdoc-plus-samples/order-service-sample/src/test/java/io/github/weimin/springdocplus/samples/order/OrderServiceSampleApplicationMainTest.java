package io.github.weimin.springdocplus.samples.order;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class OrderServiceSampleApplicationMainTest {

    @Test
    void mainDelegatesToSpringApplication() {
        try (MockedStatic<SpringApplication> application = Mockito.mockStatic(SpringApplication.class)) {
            OrderServiceSampleApplication.main(new String[] {"--server.port=0"});

            application.verify(() -> SpringApplication.run(OrderServiceSampleApplication.class, new String[] {"--server.port=0"}));
        }
    }
}
