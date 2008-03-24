
DROP TABLE IF EXISTS planet;
DROP TABLE IF EXISTS star;
DROP TABLE IF EXISTS system;
DROP TABLE IF EXISTS sector;
DROP TABLE IF EXISTS subsector;
DROP TABLE IF EXISTS allegiance;
DROP TABLE IF EXISTS glossary;
DROP TABLE IF EXISTS map;
DROP TABLE IF EXISTS globe;
DROP TABLE IF EXISTS note;

#
# ALLEGIANCE
#
CREATE TABLE allegiance(id int not null auto_increment, code varchar(4) not null,
						name varchar(240) not null,
                        colour varchar(12) default '#000000',
                        PRIMARY KEY (id), KEY(code), KEY(name));

INSERT INTO allegiance values(0, 'Un', 'Unaligned', '#000000');

#
# SECTOR
#
CREATE TABLE sector (id int auto_increment not null, name varchar(250) not null, 
					 x int not null, y int not null, PRIMARY KEY(id))
					 ENGINE=INNODB;

#
# SYSTEM
#
CREATE TABLE system (id int auto_increment not null, sector_id int not null, 
					 x int not null, y int not null, name varchar(250) not null, 
					 allegiance varchar(4) default 'Un',
					 zone varchar(16) default 'Green', base varchar(8), 
					 uwp varchar(80),
					 PRIMARY KEY(id), KEY (sector_id), KEY (sector_id, x, y),
					 FOREIGN KEY (sector_id) REFERENCES sector(id))
					 ENGINE=INNODB;
					 
#
# STAR
#
CREATE TABLE star (id int auto_increment not null, name varchar(250), 
				   system_id int not null, parent_id int not null, 
				   distance int not null, form varchar(16), 
				   class varchar(16), type varchar(16),
                   PRIMARY KEY(id), KEY(system_id), KEY(parent_id),
                   FOREIGN KEY (system_id) REFERENCES system(id))
                   ENGINE=INNODB;

#
# PLANET
#
CREATE TABLE planet (id int auto_increment not null, system_id int not null, 
					parent_id int not null, moon boolean default false,
					name varchar(250), distance int not null, radius int not null, 
					type varchar(32) not null, starport varchar(8) DEFAULT 'X', 
					atmosphere varchar(32) DEFAULT 'Vacuum', 
					pressure varchar(32) DEFAULT 'None', 
					hydrographics int DEFAULT 0,
					population bigint default 0,
					government varchar(32),
					law int DEFAULT 0,
					tech int DEFAULT 0,
					temperature varchar(32), 
					description TEXT,
					life varchar(32) DEFAULT 'None',
					base varchar(8), trade varchar(64), day int default 86400,
					features varchar(64),
					PRIMARY KEY(id), KEY(system_id), KEY(parent_id),
					FOREIGN KEY (system_id) REFERENCES system(id))
					ENGINE=INNODB;



create table glossary (id int auto_increment not null, uri varchar(64) not null, title varchar(128) not null,
					   message text, primary key(id));
create index glossary1_idx on glossary (uri);


create table subsector(id int not null auto_increment, sector_id int not null,
                       idx int not null, name varchar(64), primary key(id));

create table map (planet_id int not null, image longblob not null, primary key(planet_id));
create table globe (planet_id int not null, image longblob not null, primary key(planet_id));

create table note (id int auto_increment not null, planet_id int not null, property varchar(16), message text, primary key(id));

#
# Radical stuff
#

create table ship (id int auto_increment not null, name varchar(64) not null, type varchar(64) not null,
                   system_id int not null,
                   planet_id int not null,
                   nextevent bigint not null,
                   status varchar(8) default 'Docked',
				   displacement int not null,
				   jump int not null,
				   accl int not null,
				   cargo int not null,
				   flag varchar(64) default 'Imperium',
				   PRIMARY KEY(id), 
				   FOREIGN KEY (system_id) REFERENCES system(id)
				   FOREIGN KEY (planet_id) REFERENCES planet(id));

CREATE TABLE commodity (id int auto_increment not null,
                   name varchar(64) not null,
                   source varchar(4) not null,
                   cost int not null, dt int default 0,
                   production int not null, consumption int not null,
                   law int default 6, tech int default 0,
                   codes varchar(64) not null,
                   PRIMARY KEY(id), UNIQUE KEY(name))
                   ENGINE=INNODB;

# Basic goods
INSERT INTO commodity VALUES(0, 'Food', 'Ag', 1000, 1, 8, 8, 6, 1, 'Vi Pe Tl');
INSERT INTO commodity VALUES(0, 'Textiles', 'Ag', 3000, 1, 7, 6, 6, 1, 'Vi');
INSERT INTO commodity VALUES(0, 'Minerals', 'Mi', 1000, 1, 6, 5, 6, 3, 'In');
INSERT INTO commodity VALUES(0, 'Alloys', 'In', 1000, 1, 5, 4, 6, 5, 'In Tl');
INSERT INTO commodity VALUES(0, 'Machinary', 'In', 1000, 1, 4, 3, 6, 6, 'Tl');

# Basic ores
INSERT INTO commodity VALUES(0, 'Ferric ore', 'Mi', 200, 1, 5, 0, 6, 2, 'In');
INSERT INTO commodity VALUES(0, 'Carbonic ore', 'Mi', 100, 1, 6, 0, 6, 3, 'In');
INSERT INTO commodity VALUES(0, 'Silicate ore', 'Mi', 50, 1, 6, 0, 6, 3, 'In');
INSERT INTO commodity VALUES(0, 'Aquean ore', 'Mi', 75, 1, 5, 0, 6, 6, 'In');

# Rare ores
INSERT INTO commodity VALUES(0, 'VanAzek ore', 'Mi', 500, 1, 4, 0, 6, 8, 'In');
INSERT INTO commodity VALUES(0, 'Pentric ore', 'Mi', 700, 1, 3, 0, 6, 8, 'In');
INSERT INTO commodity VALUES(0, 'Krysite ore', 'Mi', 600, 1, 4, 0, 6, 8, 'In');
INSERT INTO commodity VALUES(0, 'Ishik ore', 'Mi', 600, 1, 3, 0, 6, 8, 'In');

# Very rare ores
INSERT INTO commodity VALUES(0, 'Xithricate ore', 'Mi', 1500, 1, 2, 0, 6, 9, 'In');
INSERT INTO commodity VALUES(0, 'Lanthanic ore', 'Mi', 2000, 1, 1, 0, 6, 10, 'In Hz');
INSERT INTO commodity VALUES(0, 'Heliocene ore', 'Mi', 2500, 1, 1, 0, 6, 9, 'In');
INSERT INTO commodity VALUES(0, 'Apicene ore', 'Mi', 1200, 1, 1, 0, 6, 9, 'In Hz');
INSERT INTO commodity VALUES(0, 'Magnesite ore', 'Mi', 1700, 1, 2, 0, 6, 8, 'In');
INSERT INTO commodity VALUES(0, 'Pyronic ore', 'Mi', 1500, 1, 2, 0, 6, 9, 'In Hz');
INSERT INTO commodity VALUES(0, 'Denic ore', 'Mi', 900, 1, 1, 0, 6, 8, 'In Hz');
INSERT INTO commodity VALUES(0, 'Oorcine ore', 'Mi', 1000, 1, 2, 0, 6, 10, 'In Fr');

# Basic agricultural resources
INSERT INTO commodity VALUES(0, 'Vegetables', 'Ag', 100, 1, 8, 8, 6, 1, 'Vi Pe Tl');
INSERT INTO commodity VALUES(0, 'Fruits', 'Ag', 150, 1, 8, 8, 6, 1, 'Vi Pe Tl');
INSERT INTO commodity VALUES(0, 'Meat', 'Ag', 350, 1, 8, 8, 6, 1, 'Vi Pe Tl');
INSERT INTO commodity VALUES(0, 'Seafood', 'Ag', 250, 1, 8, 8, 6, 1, 'Vi Pe Tl');


CREATE TABLE resources (planet_id INT NOT NULL, commodity_id INT NOT NULL, density INT NOT NULL,
                        UNIQUE KEY(planet_id, commodity_id)) ENGINE=INNODB;


CREATE TABLE trade (id INT AUTO_INCREMENT NOT NULL, planet_id INT NOT NULL,
                    commodity_id int not null, amount int not null,
                    PRIMARY KEY(id),
                    FOREIGN KEY(planet_id) REFERENCES planet(id),
                    FOREIGN KEY(commodity_id) REFERENCES commodity(id))
                    ENGINE=INNODB;
					