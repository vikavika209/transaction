package com.example.transaction;

import com.example.transaction.component.ExchangeRateApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class TransactionApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(TransactionApplication.class, args);
		ExchangeRateApiClient apiClient = context.getBean(ExchangeRateApiClient.class);

		System.out.println(apiClient.getExchangeRate("RUB"));
	}

}
