COPY type_effectiveness (
    attacker_type_id,
    defender_type_id,
    multiplier
)
FROM '/docker-entrypoint-initdb.d/dataset/type_effectiveness.csv'
DELIMITER ','
CSV HEADER;