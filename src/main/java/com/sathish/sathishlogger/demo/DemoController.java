package com.sathish.sathishlogger.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DemoController {

    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "world") String name) {
        log.info("Hello endpoint called with name={}", name);
        return demoService.greet(name);
    }

    @GetMapping("/compute/{n}")
    public long compute(@PathVariable int n) {
        return demoService.fib(n);
    }
}
