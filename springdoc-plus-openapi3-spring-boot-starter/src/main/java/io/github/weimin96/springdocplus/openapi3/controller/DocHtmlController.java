package io.github.weimin96.springdocplus.openapi3.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 文档 HTML 控制器。
 * <p>
 * 提供 doc.html 入口页面，将请求转发到前端 UI 页面。
 *
 * @author pwm
 */
@Controller
public class DocHtmlController {

    /**
     * 获取文档 HTML 页面
     *
     * @return 转发到前端 UI 页面
     */
    @GetMapping(value = "/doc.html", produces = MediaType.TEXT_HTML_VALUE)
    public String docHtml() {
        return "forward:/springdoc-plus-ui/index.html";
    }

    /**
     * 无参构造器
     */
    public DocHtmlController() {
    }
}
