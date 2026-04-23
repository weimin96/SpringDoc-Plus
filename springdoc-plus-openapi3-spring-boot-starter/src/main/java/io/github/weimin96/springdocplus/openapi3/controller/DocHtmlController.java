package io.github.weimin96.springdocplus.openapi3.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档 HTML 控制器。
 * <p>
 * 提供 doc.html 入口页面，直接返回前端 UI 首页资源。
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
    public ResponseEntity<Resource> docHtml() {
        // 直接返回资源而不是使用内部转发，避免业务应用只放行 /doc.html 时，
        // 转发到 /springdoc-plus-ui/index.html 再次进入授权链并触发 403。
        Resource resource = resourceLoader.getResource("classpath:/META-INF/resources/springdoc-plus-ui/index.html");
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }
}
