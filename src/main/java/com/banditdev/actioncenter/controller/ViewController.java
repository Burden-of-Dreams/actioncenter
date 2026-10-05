package com.banditdev.actioncenter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/login", "/dashboard"})
    public String frontend() {
        return "forward:/index.html";
    }
}