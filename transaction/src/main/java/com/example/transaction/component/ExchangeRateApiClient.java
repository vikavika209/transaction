package com.example.transaction.component;

import com.example.transaction.utils.SimpleYamlParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;

@Component
public class ExchangeRateApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateApiClient.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SimpleYamlParser simpleYamlParser = new SimpleYamlParser();

    public ExchangeRateApiClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    private final String API_KEY = simpleYamlParser.getAPI_KEY();
    private final String URL = simpleYamlParser.getURL() + API_KEY;

    public BigDecimal getExchangeRate(String currency) {
        logger.info("Запрос курса валют относительно USD для: {}", currency);
        logger.info(URL);

        try {
            String response = restTemplate.getForObject(URL, String.class);
            if (response == null) {
                logger.warn("Ответ API пустой.");
                return null;
            }

            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode ratesNode = rootNode.path("rates");

            JsonNode currencyNode = ratesNode.path(currency);
            if (currencyNode.isMissingNode()) {
                logger.warn("Курс для {} отсутствует в ответе API", currency);
                return null;
            }

            BigDecimal rate = currencyNode.decimalValue();
            logger.info("Получен курс: 1 USD = {} {}", rate, currency);
            return rate;

        } catch (Exception e) {
            logger.error("Ошибка при запросе курса валют для {}: {}", currency, e.getMessage());
            return null;
        }
    }
}
