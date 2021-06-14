package com.sentinel.hrms.controller;

import com.sentinel.hrms.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @GetMapping("/home")
    public String showUser(){
        return "Application Works";
    }

}
