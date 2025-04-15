package com.sapirgo.campusserver;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WalkController {

    @PostMapping("/walk")
    public String greetUser(@RequestBody WalkRequest request) {
        return "Hello, nice walk to " + request.getDestination() + "!";
    }

    // Inner class to represent the request body
    public static class WalkRequest {
        private String destination;

        // Getter and Setter for 'destination'
        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }
    }
}
