package com.sapirgo.campusserver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HelloWorldController {
        @GetMapping("/")
            public List<String> sayHello() {
            return List.of("Hello", "World");
        }


}
