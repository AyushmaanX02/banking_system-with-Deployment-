package com.banking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebConfig {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}