
CREATE TABLE IF NOT EXISTS table_name_a (
    `id` IDENTITY,
    aname VARCHAR(20),
    score INTEGER,
    born_date DATETIME
);

CREATE TABLE IF NOT EXISTS model_b (
    `id` IDENTITY,
    name VARCHAR(20),
    expired_date_on DATETIME,
    score BIGINT
);