CREATE TABLE IF NOT EXISTS currencies (
    code VARCHAR(3) PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS exchange_rates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    base_currency VARCHAR(3) NOT NULL,
    quote_currency VARCHAR(3) NOT NULL,
    rate DOUBLE NOT NULL,
    update_time TIMESTAMP NOT NULL,
    CONSTRAINT unique_currency_pair UNIQUE (base_currency, quote_currency)
);

-- Initial currency data
INSERT INTO currencies (code, name) VALUES 
('USD', 'US Dollar'),
('EUR', 'Euro'),
('JPY', 'Japanese Yen'),
('GBP', 'British Pound'),
('AUD', 'Australian Dollar'),
('CAD', 'Canadian Dollar'),
('CHF', 'Swiss Franc'),
('CNY', 'Chinese Yuan');
