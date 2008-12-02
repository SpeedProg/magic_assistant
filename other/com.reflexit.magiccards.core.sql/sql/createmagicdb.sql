CREATE TABLE magiccards (
	id INT primary key,
	name VARCHAR(80),
	cost VARCHAR(20),
	power VARCHAR(7),
	toughness VARCHAR(7),
	type VARCHAR(80),
	class VARCHAR(20),
	setId INT,
	rarity VARCHAR(20),
	oracleText BLOB
);
CREATE TABLE sets (
	id INT primary key,
	abbr VARCHAR(5),
	name VARCHAR(80)
);

CREATE TABLE printings (
    cardId INT,
    setId INT
);