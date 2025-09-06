COPY Ability(id, name, description)
FROM '/docker-entrypoint-initdb.d/dataset/abilities.csv'
DELIMITER ','
CSV HEADER;
