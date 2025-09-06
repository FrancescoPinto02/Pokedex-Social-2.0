-- =====================================================
-- TABLE: Type
-- =====================================================
CREATE TABLE Type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- =====================================================
-- TABLE: Ability
-- =====================================================
CREATE TABLE Ability (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT NOT NULL
);


-- =====================================================
-- TABLE: Pokemon
-- =====================================================
CREATE TABLE Pokemon (
    id SERIAL PRIMARY KEY,
    ndex INT NOT NULL,
    species VARCHAR(100) NOT NULL,
    forme VARCHAR(100), -- es. "Alolan", "Mega", ecc
    dex1 TEXT NOT NULL,
    dex2 TEXT NOT NULL,

    type1_id INT NOT NULL REFERENCES Type(id) ON DELETE RESTRICT,
    type2_id INT REFERENCES Type(id) ON DELETE SET NULL,
    ability1_id INT NOT NULL REFERENCES Ability(id) ON DELETE RESTRICT,
    ability2_id INT REFERENCES Ability(id) ON DELETE SET NULL,
    hidden_ability_id INT REFERENCES Ability(id) ON DELETE SET NULL,

    hp INT NOT NULL,
    attack INT NOT NULL,
    defense INT NOT NULL,
    spattack INT NOT NULL,
    spdefense INT NOT NULL,
    speed INT NOT NULL,
    total INT NOT NULL,

    weight DECIMAL(6,2),
    height DECIMAL(6,2),

    class VARCHAR(100), -- es. "Seed Pokémon"
    percent_male NUMERIC(3,2) CHECK(percent_male >= 0 AND percent_male <= 1),
    percent_female NUMERIC(3,2) CHECK(percent_female >= 0 AND percent_female <= 1),
    

    egg_group1 VARCHAR(50),
    egg_group2 VARCHAR(50),

    image_url TEXT
);



