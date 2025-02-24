CREATE TABLE limits (
                        id SERIAL PRIMARY KEY,
                        account VARCHAR(10) NOT NULL,
                        limit_category VARCHAR(255) NOT NULL,
                        limit_sum NUMERIC(19,2) NOT NULL,
                        limit_datetime TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                        limit_currency_shortname VARCHAR(255) NOT NULL,
                        CONSTRAINT chk_account CHECK (account ~ '^\d{10}$')
    );

CREATE TABLE transactions (
                              id SERIAL PRIMARY KEY,
                              account_from VARCHAR(10) NOT NULL,
                              account_to VARCHAR(10) NOT NULL,
                              currency_shortname VARCHAR(255) NOT NULL,
                              sum NUMERIC(19,2) NOT NULL,
                              expense_category VARCHAR(255),
                              transaction_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                              limit_exceeded BOOLEAN NOT NULL,
                              CONSTRAINT chk_account_from CHECK (account_from ~ '^\d{10}$'),
    CONSTRAINT chk_account_to CHECK (account_to ~ '^\d{10}$'),
    CONSTRAINT fk_limit FOREIGN KEY (account_from, expense_category)
        REFERENCES limits(account, limit_category) ON DELETE SET NULL
);
