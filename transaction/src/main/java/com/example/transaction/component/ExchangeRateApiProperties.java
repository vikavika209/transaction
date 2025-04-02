package com.example.transaction.component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.exchange.rate.api")
@Getter
@Setter
public class ExchangeRateApiProperties {
    private String url;
}
