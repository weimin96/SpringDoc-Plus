package io.github.weimin96.springdocplus.gateway.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 保持 Knife4j 习惯：对外入口永远是 /doc.html
 * @author pwm
 */
@Controller
public class DocHtmlController {

    @GetMapping(value = "/doc.html", produces = MediaType.TEXT_HTML_VALUE)
    public String docHtml() {
        return "forward:/springdoc-plus-ui/index.html";
    }
}
