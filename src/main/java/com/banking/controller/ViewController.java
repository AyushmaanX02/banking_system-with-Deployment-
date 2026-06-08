package com.banking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    /** Root redirect → React dashboard */
    @GetMapping("/")
    public String home() {
        return "redirect:/react/";
    }

    /** Bare /react → /react/ (avoid 404 on missing trailing slash) */
    @GetMapping("/react")
    public String reactNoSlash() {
        return "redirect:/react/";
    }

    /**
     * SPA catch-all: any /react/<route> that isn't a static asset
     * gets forwarded to React's index.html so React Router can handle it.
     */
    @GetMapping("/react/{path:[^\\.]*}")
    public String reactDeepLink() {
        return "forward:/react/index.html";
    }

    @GetMapping("/react/{path:[^\\.]*}/**")
    public String reactDeepLinkNested() {
        return "forward:/react/index.html";
    }
}

