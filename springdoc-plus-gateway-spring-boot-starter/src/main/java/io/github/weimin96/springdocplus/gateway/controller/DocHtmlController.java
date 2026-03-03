package io.github.weimin96.springdocplus.gateway.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 保持 Knife4j 习惯：对外入口永远是 /doc.html
 * 同时提供 /springdoc-plus-ui/ 静态资源（JS/CSS/图片）
 * @author pwm
 */
@RestController
public class DocHtmlController {

    private final ResourceLoader resourceLoader;

    public DocHtmlController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping(value = "/doc.html", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<ResponseEntity<Resource>> docHtml() {
        // WebFlux 环境不支持 forward: 视图，直接加载资源
        return Mono.fromCallable(() -> resourceLoader.getResource("classpath:/META-INF/resources/springdoc-plus-ui/index.html"))
                .map(resource -> ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(resource));
    }

    /**
     * 提供 /springdoc-plus-ui/assets/** 资源（JS/CSS）
     */
    @GetMapping("/springdoc-plus-ui/assets/{filename}")
    public Mono<ResponseEntity<Resource>> uiAsset(@PathVariable String filename) {
        return Mono.fromCallable(() -> {
                    Resource resource = resourceLoader.getResource("classpath:/META-INF/resources/springdoc-plus-ui/assets/" + filename);
                    String contentType = getContentType(filename);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                            .body(resource);
                });
    }

    /**
     * 提供 /springdoc-plus-ui/** 根目录资源（如 favicon.svg）
     */
    @GetMapping("/springdoc-plus-ui/{filename}")
    public Mono<ResponseEntity<Resource>> uiRootAsset(@PathVariable String filename) {
        return Mono.fromCallable(() -> {
                    Resource resource = resourceLoader.getResource("classpath:/META-INF/resources/springdoc-plus-ui/" + filename);
                    String contentType = getContentType(filename);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                            .body(resource);
                });
    }

    /**
     * 根据文件扩展名推断 Content-Type
     */
    private String getContentType(String filename) {
        if (filename.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        } else if (filename.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        } else if (filename.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".ico")) {
            return "image/x-icon";
        }
        return "application/octet-stream";
    }
}
