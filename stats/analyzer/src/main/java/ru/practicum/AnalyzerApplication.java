package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.practicum.service.AnalyzerKafkaProperties;

@EnableConfigurationProperties(AnalyzerKafkaProperties.class)
@SpringBootApplication
public class AnalyzerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalyzerApplication.class, args);
    }
}


