package io.github.weimin96.springdocplus.openapi3.security;

import io.github.weimin96.springdocplus.openapi3.properties.SpringdocPlusOpenApi3Properties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 单服务模式下的可选 HTTP Basic 保护（Servlet 版）。
 *
 * <p>对齐 Gateway 模块的 {@code BasicAuthWebFilter}（WebFlux 版），
 * 保护 {@code /doc.html} 和 {@code /springdoc-plus-ui/**} 路径。
 *
 * <p>启用方式：在 {@code application.yml} 中配置：
 * <pre>{@code
 * springdoc-plus.openapi3:
 *   basic:
 *     enabled: true
 *     username: admin
 *     password: secret
 * }</pre>
 *
 * @author pwm
 */
public class BasicAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(BasicAuthFilter.class);

    private final SpringdocPlusOpenApi3Properties props;

    /**
     * 构造器
     *
     * @param props 单服务配置属性
     */
    public BasicAuthFilter(SpringdocPlusOpenApi3Properties props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (props.getBasic() == null || !props.getBasic().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (!needProtect(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = props.getBasic().getUsername();
        String password = props.getBasic().getPassword();
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            log.warn("Basic Auth 已启用，但用户名或密码为空，拒绝访问受保护资源");
            sendUnauthorized(response);
            return;
        }

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(auth) || !auth.startsWith("Basic ")) {
            sendUnauthorized(response);
            return;
        }

        String token = auth.substring("Basic ".length());
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
        } catch (Exception e) {
            sendUnauthorized(response);
            return;
        }

        String expected = username + ":" + password;

        // 恒定时间比对，防止时序攻击
        boolean matches = MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                decoded.getBytes(StandardCharsets.UTF_8));

        if (!matches) {
            log.info("Basic Auth 鉴权失败，请求路径: {}", path);
            sendUnauthorized(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean needProtect(String path) {
        return "/doc.html".equals(path)
                || path.startsWith("/springdoc-plus-ui");
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"springdoc-plus\"");
        response.getWriter().write("Unauthorized");
    }
}
