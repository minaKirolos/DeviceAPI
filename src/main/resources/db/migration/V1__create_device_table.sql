CREATE TABLE device(
    id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    state VARCHAR(20) NOT NULL,
    creation_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE= InnoDB;
CREATE INDEX idx_device_brand ON device(brand);
CREATE INDEX idx_device_state ON device(state);
