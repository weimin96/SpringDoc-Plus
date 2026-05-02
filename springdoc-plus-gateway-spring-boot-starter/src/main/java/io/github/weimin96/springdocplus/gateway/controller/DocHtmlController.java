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
        // WebFlux 环境不支持 forward: 视图，直接加载资源
        return Mono.fromCallable(() -> resourceLoader.getResource("classpath:/META-INF/resources/springdoc-plus-ui/index.html"))
                .map(resource -> ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(resource));
    }

    /**
     * 提供 /springdoc-plus-ui/assets/** 资源（JS/CSS）
     *
     * @param filename 资源文件名
     * @return 资源响应
     */
    @GetMapping("/springdoc-plus-ui/assets/{filename}")
    public Mono<ResponseEntity<Resource>> uiAsset(@PathVariable String filename) {
        return loadUiResource("classpath:/META-INF/resources/springdoc-plus-ui/assets/", filename);
    }

    /**
     * 提供 /springdoc-plus-ui/docs/** 文档资源（如 DOCX 导出模板）
     *
     * @param filename 资源文件名
     * @return 资源响应
     */
    @GetMapping("/springdoc-plus-ui/docs/{filename}")
    public Mono<ResponseEntity<Resource>> uiDocAsset(@PathVariable String filename) {
        return loadUiResource("classpath:/META-INF/resources/springdoc-plus-ui/docs/", filename);
    }

    /**
     * 提供 /springdoc-plus-ui/** 根目录资源（如 favicon.svg）
     *
     * @param filename 资源文件名
     * @return 资源响应
     */
    @GetMapping("/springdoc-plus-ui/{filename}")
    public Mono<ResponseEntity<Resource>> uiRootAsset(@PathVariable String filename) {
        return loadUiResource("classpath:/META-INF/resources/springdoc-plus-ui/", filename);
    }

    /**
     * 统一加载受控 UI 资源目录下的单文件。
     * <p>
     * 只接受不含路径分隔符的文件名，原因是这些资源由前端构建产物提供，
     * 后端只需要开放固定目录下的静态文件，不应允许调用方拼接任意 classpath 路径。
     *
     * @param basePath 受控资源目录
     * @param filename 资源文件名
     * @return 资源响应
     */
    private Mono<ResponseEntity<Resource>> loadUiResource(String basePath, String filename) {
        // 路径遍历防护：白名单校验
        if (!isValidFilename(filename)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return Mono.fromCallable(() -> {
                    Resource resource = resourceLoader.getResource(basePath + filename);
                    if (!resource.exists()) {
                        return ResponseEntity.notFound().build();
                    }
                    String contentType = getContentType(filename);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                            .body(resource);
                });
    }

    /**
     * 校验文件名是否安全，防止路径遍历攻击
     *
     * @param filename 文件名
     * @return 是否安全
     */
    private boolean isValidFilename(String filename) {
        return filename != null
                && !filename.contains("..")
                && !filename.toLowerCase().contains("%2e")
                && !filename.toLowerCase().contains("%2f")
                && !filename.toLowerCase().contains("%5c")
                && !filename.contains("/")
                && !filename.contains("\\");
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
        } else if (filename.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return "application/octet-stream";
    }
}
