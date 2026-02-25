package io.github.weimin96.springdocplus.gateway.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 保持 Knife4j 习惯：对外入口永远是 /doc.html
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
}
