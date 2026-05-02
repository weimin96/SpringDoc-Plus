package io.github.weimin96.springdocplus.openapi3.security;

import io.github.weimin96.springdocplus.openapi3.properties.SpringdocPlusOpenApi3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BasicAuthFilter 单元测试。
 * <p>
 * 这里覆盖鉴权成功、失败与放行分支，确保单服务模式下的文档保护逻辑
 * 与网关模式保持一致，避免后续演进时出现静默回归。
 */
class BasicAuthFilterTest {

    private SpringdocPlusOpenApi3Properties properties;
    private BasicAuthFilter filter;

    @BeforeEach
    void setUp() {
        properties = new SpringdocPlusOpenApi3Properties();
        properties.getBasic().setEnabled(true);
        properties.getBasic().setUsername("admin");
        properties.getBasic().setPassword("123456");
        filter = new BasicAuthFilter(properties);
    }

    @Test
    void disabledBasicAuthPassesThroughProtectedPath() throws Exception {
        properties.getBasic().setEnabled(false);

        MockHttpServletRequest request = createRequest("/doc.html", null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_OK);
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void unprotectedPathPassesThrough() throws Exception {
        MockHttpServletRequest request = createRequest("/api/users", null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_OK);
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void emptyCredentialsReturnUnauthorized() throws Exception {
        properties.getBasic().setUsername("");

        MockHttpServletRequest request = createRequest("/doc.html", basicHeader("admin:123456"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertUnauthorized(response);
    }

    @Test
    void missingAuthorizationHeaderReturnsUnauthorized() throws Exception {
        MockHttpServletRequest request = createRequest("/doc.html", null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertUnauthorized(response);
    }

    @Test
    void invalidAuthorizationSchemeReturnsUnauthorized() throws Exception {
        MockHttpServletRequest request = createRequest("/doc.html", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertUnauthorized(response);
    }

    @Test
    void invalidBase64TokenReturnsUnauthorized() throws Exception {
        MockHttpServletRequest request = createRequest("/doc.html", "Basic not-base64");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertUnauthorized(response);
    }

    @Test
    void wrongCredentialsReturnUnauthorized() throws Exception {
        MockHttpServletRequest request = createRequest("/doc.html", basicHeader("wrong:credentials"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertUnauthorized(response);
    }

    @Test
    void correctCredentialsPassThroughUiAssetPath() throws Exception {
        MockHttpServletRequest request = createRequest("/springdoc-plus-ui/index.html", basicHeader("admin:123456"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_OK);
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void bcryptPasswordPassesThrough() throws Exception {
        properties.getBasic().setPassword("{bcrypt}" + new BCryptPasswordEncoder().encode("123456"));
        MockHttpServletRequest request = createRequest("/doc.html", basicHeader("admin:123456"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_OK);
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void gatewayContractPathRequiresAuthorization() throws Exception {
        MockHttpServletRequest request = createRequest("/springdoc-plus-gateway/openapi/groups", null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertUnauthorized(response);
    }

    @Test
    void correctCredentialsPassThroughGatewayContractPath() throws Exception {
        MockHttpServletRequest request = createRequest(
                "/springdoc-plus-gateway/openapi/groups",
                basicHeader("admin:123456")
        );
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_OK);
        assertThat(chain.getRequest()).isSameAs(request);
    }

    private MockHttpServletRequest createRequest(String path, String authorization) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setRequestURI(path);
        if (authorization != null) {
            request.addHeader(HttpHeaders.AUTHORIZATION, authorization);
        }
        return request;
    }

    private String basicHeader(String credentials) {
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private void assertUnauthorized(MockHttpServletResponse response) throws Exception {
        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getHeader(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Basic realm=\"springdoc-plus\"");
        assertThat(response.getContentAsString()).isEqualTo("Unauthorized");
    }
}
