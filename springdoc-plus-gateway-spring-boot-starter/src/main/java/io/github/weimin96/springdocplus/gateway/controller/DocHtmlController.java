package io.github.weimin96.springdocplus.gateway.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 保持 Knife4j 习惯：对外入口永远是 /doc.html
 *
 * @author pwm
 */
@RestController
public class DocHtmlController {

    private final ResourceLoader resourceLoader;

    /**
     * 构造器
     *
     * @param resourceLoader 资源加载器
     */
    public DocHtmlController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 获取文档 HTML 页面
     *
     * @return HTML 页面响应
     */
    @GetMapping(value = "/doc.html", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<ResponseEntity<Resource>> docHtml() {
        return Mono.fromCallable(() -> resourceLoader.getResource("classpath:/META-INF/resources/springdoc-plus-ui/index.html"))
                .map(resource -> ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                        .body(resource));
    }
}
