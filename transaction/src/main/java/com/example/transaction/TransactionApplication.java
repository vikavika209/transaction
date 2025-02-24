package com.example.transaction;

import com.example.transaction.component.ExchangeRateApiClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransactionApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionApplication.class, args);

		ExchangeRateApiClient apiClient = new ExchangeRateApiClient();
		System.out.println(apiClient.getExchangeRate("RUB"));
	}

}
