package com.github.camelya58.kafka_logback_appender.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class LoggerController
 *
 * @author Kamila Meshcheryakova
 * created 29.10.2021
 */
@RestController
@RequestMapping("/info")
public class LoggerController {

    @PostMapping
    public String getInfo(@RequestParam("name") String name,
                          @RequestParam("age") int age,
                          @RequestParam("city") String city) {
        return "Entered info:\n" +
                "name - " + name +
                ",\nage - " + age +
                ",\ncity - " + city + ".";
    }
}
