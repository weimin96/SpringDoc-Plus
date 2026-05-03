package io.github.weimin96.springdocplus.openapi3.controller;

import io.github.weimin96.springdocplus.core.enums.GroupOrderStrategy;
import io.github.weimin96.springdocplus.openapi3.properties.SpringdocPlusOpenApi3Properties;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SingleOpenApiControllersTest {

    @Test
    void groupsReturnsConfiguredEntries() {
        SpringdocPlusOpenApi3Properties props = new SpringdocPlusOpenApi3Properties();
        SpringdocPlusOpenApi3Properties.Group group = new SpringdocPlusOpenApi3Properties.Group();
        group.setName("admin");
        group.setUrl("/v3/api-docs?group=admin");
        group.setContextPath("/api");
        group.setOrder(2);
        props.setGroups(List.of(group));

        SingleOpenApiGroupsController controller = new SingleOpenApiGroupsController(props);
        Map<String, Object> result = controller.groups();

        assertThat(result).containsKey("groups");
        List<?> groups = (List<?>) result.get("groups");
        assertThat(groups).hasSize(1);
        Map<?, ?> first = (Map<?, ?>) groups.get(0);
        assertThat(first.get("name")).isEqualTo("admin");
        assertThat(first.get("url")).isEqualTo("/v3/api-docs?group=admin");
        assertThat(first.get("contextPath")).isEqualTo("/api");
        assertThat(first.get("order")).isEqualTo(2);
    }

    @Test
    void groupsFallsBackToDefaultEntry() {
        SingleOpenApiGroupsController controller = new SingleOpenApiGroupsController(new SpringdocPlusOpenApi3Properties());
        Map<String, Object> result = controller.groups();

        List<?> groups = (List<?>) result.get("groups");
        Map<?, ?> first = (Map<?, ?>) groups.get(0);
        assertThat(first.get("name")).isEqualTo("default");
        assertThat(first.get("url")).isEqualTo("/v3/api-docs");
        assertThat(first.get("contextPath")).isEqualTo("/");
    }

    @Test
    void uiConfigReturnsConfiguredSortingAndAuth() {
        SpringdocPlusOpenApi3Properties props = new SpringdocPlusOpenApi3Properties();
        props.setTagsSorter(GroupOrderStrategy.order);
        props.setOperationsSorter(GroupOrderStrategy.order);
        props.getAuth().setEnabled(false);
        props.getAuth().setHeaderName("X-Auth");
        props.getAuth().setDefaultPrefix("Bearer");
        props.getAuth().setPersist(false);
        props.getBasic().setEnabled(true);

        SingleOpenApiUiConfigController controller = new SingleOpenApiUiConfigController(props);
        var result = controller.config();

        assertThat(result.getTagsSorter()).isEqualTo(GroupOrderStrategy.order);
        assertThat(result.getOperationsSorter()).isEqualTo(GroupOrderStrategy.order);
        assertThat(result.isAuthEnabled()).isFalse();
        assertThat(result.getAuthHeaderName()).isEqualTo("X-Auth");
        assertThat(result.getAuthDefaultPrefix()).isEqualTo("Bearer");
        assertThat(result.isAuthPersist()).isFalse();
        assertThat(result.isGatewayBasicEnabled()).isTrue();
    }

    @Test
    void docHtmlReturnsUiIndexResource() {
        DocHtmlController controller = new DocHtmlController(new DefaultResourceLoader());
        var response = controller.docHtml();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_HTML);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().exists()).isTrue();
        assertThat(response.getBody().getFilename()).isEqualTo("index.html");
    }
}
