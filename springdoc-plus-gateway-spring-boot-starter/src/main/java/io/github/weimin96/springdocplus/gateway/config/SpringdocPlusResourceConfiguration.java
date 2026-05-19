package io.github.weimin96.springdocplus.gateway.config;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.ResourceHandlerRegistration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.time.Duration;
import java.util.Map;

/**
 * SpringDoc Plus UI 静态资源配置。
 *
 * @author pwm
 */
public class SpringdocPlusResourceConfiguration implements WebFluxConfigurer {

    private static final String UI_LOCATION = "classpath:/META-INF/resources/springdoc-plus-ui/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        ResourceHandlerRegistration assets = registry.addResourceHandler("/springdoc-plus-ui/**")
                .addResourceLocations(UI_LOCATION)
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());
        assets.setMediaTypes(mediaTypes());
    }

    private Map<String, MediaType> mediaTypes() {
        return Map.ofEntries(
                Map.entry("js", MediaType.parseMediaType("application/javascript")),
                Map.entry("mjs", MediaType.parseMediaType("application/javascript")),
                Map.entry("css", MediaType.parseMediaType("text/css")),
                Map.entry("map", MediaType.APPLICATION_JSON),
                Map.entry("json", MediaType.APPLICATION_JSON),
                Map.entry("svg", MediaType.parseMediaType("image/svg+xml")),
                Map.entry("png", MediaType.IMAGE_PNG),
                Map.entry("jpg", MediaType.IMAGE_JPEG),
                Map.entry("jpeg", MediaType.IMAGE_JPEG),
                Map.entry("gif", MediaType.IMAGE_GIF),
                Map.entry("webp", MediaType.parseMediaType("image/webp")),
                Map.entry("ico", MediaType.parseMediaType("image/x-icon")),
                Map.entry("woff", MediaType.parseMediaType("font/woff")),
                Map.entry("woff2", MediaType.parseMediaType("font/woff2")),
                Map.entry("ttf", MediaType.parseMediaType("font/ttf")),
                Map.entry("otf", MediaType.parseMediaType("font/otf")),
                Map.entry("wasm", MediaType.parseMediaType("application/wasm")),
                Map.entry("docx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        );
    }
}
