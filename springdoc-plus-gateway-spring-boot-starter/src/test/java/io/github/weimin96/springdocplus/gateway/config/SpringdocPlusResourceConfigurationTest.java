package io.github.weimin96.springdocplus.gateway.config;

import io.github.weimin96.springdocplus.gateway.security.SpringdocPlusSecurityHeadersWebFilter;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.WebFilter;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(SpringdocPlusResourceConfigurationTest.TestConfig.class)
class SpringdocPlusResourceConfigurationTest {

    @Test
    void servesIndexWithNoCache(ApplicationContext context) {
        WebTestClient client = WebTestClient.bindToApplicationContext(context).build();

        client.get()
                .uri("/springdoc-plus-ui/index.html")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Cache-Control", "no-cache")
                .expectHeader().contentType("text/html");
    }

    @Test
    void servesNestedAssetsWithLongCacheAndContentType(ApplicationContext context) {
        WebTestClient client = WebTestClient.bindToApplicationContext(context).build();
        String javascriptAsset = firstAssetName("classpath:/META-INF/resources/springdoc-plus-ui/assets/*.js");

        client.get()
                .uri("/springdoc-plus-ui/assets/" + javascriptAsset)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().value("Cache-Control", value -> {
                    assertThat(value).contains("max-age=31536000");
                    assertThat(value).contains("immutable");
                })
                .expectHeader().contentType("application/javascript");

        client.get()
                .uri("/springdoc-plus-ui/docs/{filename}", "模板.docx")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    private String firstAssetName(String locationPattern) {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(locationPattern);
            assertThat(resources).isNotEmpty();
            return resources[0].getFilename();
        } catch (IOException e) {
            throw new IllegalStateException("读取前端资源失败", e);
        }
    }

    @Configuration
    @EnableWebFlux
    static class TestConfig {

        @Bean
        WebFluxConfigurer springdocPlusResourceConfigurer() {
            return new SpringdocPlusResourceConfiguration();
        }

        @Bean
        WebFilter springdocPlusSecurityHeadersWebFilter() {
            return new SpringdocPlusSecurityHeadersWebFilter();
        }
    }
}
