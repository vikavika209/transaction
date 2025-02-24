CREATE TABLE exchange_rates (
                                id SERIAL PRIMARY KEY,
                                currency VARCHAR(255) NOT NULL,
                                rate NUMERIC(19,4) NOT NULL,
                                date DATE NOT NULL
);
