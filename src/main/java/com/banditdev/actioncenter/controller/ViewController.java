package com.banditdev.actioncenter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/login", "/dashboard", "/bookings/new", "/schedule"})
    public String frontend() {
        return "forward:/index.html";
    }
}