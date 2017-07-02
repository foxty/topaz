
CREATE TABLE IF NOT EXISTS model_a (
    `id` IDENTITY,
    name VARCHAR(20),
    score INTEGER,
    born_date DATETIME
);
INSERT INTO model_a VALUES (1, 'test_data_1', 0, now());
INSERT INTO model_a VALUES (2, 'test_data_2', 10, now());
INSERT INTO model_a VALUES (3, 'test_data_3', 20, now());
INSERT INTO model_a VALUES (4, 'test_data_4', 30, now());

CREATE TABLE IF NOT EXISTS model_b (
    `id` IDENTITY,
    name VARCHAR(20),
    expired_date_on DATETIME,
    model_a_id INTEGER
);
INSERT INTO model_b VALUES (1, 'modelb_1', now(), 1);
INSERT INTO model_b VALUES (2, 'modelb_2', now(), 1);
INSERT INTO model_b VALUES (3, 'modelb_3', now(), 2);
INSERT INTO model_b VALUES (4, 'modelb_4', now(), 3);

CREATE TABLE IF NOT EXISTS model_c (
    `id` IDENTITY,
    name VARCHAR(20),
    created_at DATETIME,
    model_a_id INTEGER
);
INSERT INTO model_c VALUES (1, 'modelc_1', now(), 1);
INSERT INTO model_c VALUES (2, 'modelc_2', now(), 1);
INSERT INTO model_c VALUES (3, 'modelc_3', now(), 2);
INSERT INTO model_c VALUES (4, 'modelc_4', now(), 2);
