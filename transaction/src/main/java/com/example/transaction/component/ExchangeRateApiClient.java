package com.example.transaction.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;

@Component
@Slf4j
public class ExchangeRateApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ExchangeRateApiProperties properties;

    @Autowired
    public ExchangeRateApiClient(RestTemplate restTemplate, ObjectMapper objectMapper, ExchangeRateApiProperties properties) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public BigDecimal getExchangeRate(String currency) {
        String url = properties.getUrl();

        log.info("Запрос курса валют относительно USD для: {}", currency);
        log.info(url);

        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                log.warn("Ответ API пустой.");
                return null;
            }

            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode ratesNode = rootNode.path("rates");

            JsonNode currencyNode = ratesNode.path(currency);
            if (currencyNode.isMissingNode()) {
                log.warn("Курс для {} отсутствует в ответе API", currency);
                return null;
            }

            BigDecimal rate = currencyNode.decimalValue();
            log.info("Получен курс: 1 USD = {} {}", rate, currency);
            return rate;

        } catch (Exception e) {
            log.error("Ошибка при запросе курса валют для {}: {}", currency, e.getMessage());
            return null;
        }
    }
}
