package com.example.transaction.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleYamlParser {

    private final Map<String, String> config = parseYaml();
    private final String URL = config.get("url_for_API");

    private static final Logger logger = LoggerFactory.getLogger(SimpleYamlParser.class);

    private Map<String, String> parseYaml() {
        Map<String, String> map = new HashMap<>();
        try (InputStream inputStream = SimpleYamlParser.class.getClassLoader().getResourceAsStream("application.yml")) {

            if (inputStream == null) {
                logger.error("Файл {} не найден!", "application.yml");
                return map;
            }

            List<String> lines = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().toList();

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(":", 2);
                if (parts.length < 2) continue;

                String key = parts[0].trim();
                String value = parts[1].trim();

                if (!value.isEmpty()) {
                    map.put(key, value);
                    logger.info("Пара ключ: {}, значение: {} успешно получены", key, value);
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка при получении данных для API из yml файла");
        }
        return map;
    }

    public SimpleYamlParser() {
    }

    public String getURL() {
        return URL;
    }
}
