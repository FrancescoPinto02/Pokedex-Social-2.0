COPY Pokemon(
    id,
    ndex,
    species,
    forme,
    dex1,
    dex2,
    type1_id,
    type2_id,
    ability1_id,
    ability2_id,
    hidden_ability_id,
    hp,
    attack,
    defense,
    spattack,
    spdefense,
    speed,
    total,
    weight,
    height,
    class,
    percent_male,
    percent_female,
    egg_group1,
    egg_group2,
    image_url
)
FROM '/docker-entrypoint-initdb.d/dataset/pokemon.csv'
DELIMITER ','
CSV HEADER;
