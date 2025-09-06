COPY Type(id, name)
FROM '/docker-entrypoint-initdb.d/dataset/types.csv'
DELIMITER ','
CSV HEADER;
