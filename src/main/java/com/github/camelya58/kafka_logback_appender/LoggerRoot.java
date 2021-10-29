package com.github.camelya58.kafka_logback_appender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class LoggerRoot {
    private static final Logger logger = LoggerFactory.getLogger(LoggerRoot.class.getSimpleName());

    public static void main(String... args) {
        logger.info("Start");
        SpringApplication springApplication = new SpringApplication(LoggerRoot.class);
        Environment environment = springApplication.run(args).getEnvironment();

        logger.info("Completed");
        logger.info("\n------------------------------------------\n\t" +
                        "Application '{}' is running!\n\t" +
                        "Access URL: http://127.0.0.1:{}/swagger-ui.html\n",
                "LoggerRoot",
                environment.getProperty("server.port"));
    }
}
