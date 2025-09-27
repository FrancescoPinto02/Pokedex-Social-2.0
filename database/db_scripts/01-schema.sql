-- =====================================================
-- TABLE: Type
-- =====================================================
CREATE TABLE Type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- =====================================================
-- TABLE: TypeEffectiveness
-- =====================================================
CREATE TABLE type_effectiveness (
    attacker_type_id INT NOT NULL REFERENCES type(id) ON DELETE CASCADE,
    defender_type_id INT NOT NULL REFERENCES type(id) ON DELETE CASCADE,
    multiplier NUMERIC(2,1) NOT NULL CHECK (multiplier IN (0, 0.5, 1, 2)),
    PRIMARY KEY (attacker_type_id, defender_type_id)
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

-- =====================================================
-- TABLE: User
-- =====================================================
CREATE TABLE app_user (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    pokecoin BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- =====================================================
-- TABLE: Team
-- =====================================================
CREATE TABLE team (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    visibility VARCHAR(20) NOT NULL CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- =====================================================
-- TABLE: TeamPokemon
-- =====================================================
CREATE TABLE team_pokemon (
    team_id INT NOT NULL REFERENCES team(id) ON DELETE CASCADE,
    pokemon_id INT NOT NULL REFERENCES pokemon(id) ON DELETE CASCADE,
    slot INT NOT NULL CHECK (slot BETWEEN 1 AND 6),
    PRIMARY KEY (team_id, slot)
);


-- =====================================================
-- Indexes
-- =====================================================
CREATE INDEX idx_user_email ON app_user(email);
CREATE INDEX idx_user_username ON app_user(username);


