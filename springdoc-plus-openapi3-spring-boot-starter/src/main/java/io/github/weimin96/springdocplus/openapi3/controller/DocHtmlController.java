package io.github.weimin96.springdocplus.openapi3.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author pwm
 */
@Controller
public class DocHtmlController {

    @GetMapping(value = "/doc.html", produces = MediaType.TEXT_HTML_VALUE)
    public String docHtml() {
        return "forward:/springdoc-plus-ui/index.html";
    }
}
