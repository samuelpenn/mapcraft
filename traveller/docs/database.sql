
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
					nextevent bigint default 0,
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

#
# TIME
#
CREATE TABLE numbers (property VARCHAR(32) NOT NULL, value BIGINT not null);
INSERT INTO numbers VALUES('time', 0);
INSERT INTO numbers VALUES('realtime', 0);
INSERT INTO numbers VALUES('timescale', 10);
INSERT INTO numbers VALUES('epocstart', 0);
INSERT INTO numbers VALUES('simstartyear', 4521);
INSERT INTO numbers VALUES('daysinyear', 365);
INSERT INTO numbers VALUES('secondsinday', 86400);

CREATE TABLE ship (id int auto_increment not null, name varchar(64) not null, type varchar(64) not null,
                   role varchar(32) not null, inservice bigint not null, displacement int not null,
                   system_id int not null, planet_id int not null, status varchar(12) default 'Docked',
                   nextevent bigint not null,
                   jump int not null,
                   accl int not null,
                   cargo int not null, cash int not null,
                   flag varchar(64) default 'Imperium',
                   PRIMARY KEY(id), 
                   FOREIGN KEY (system_id) REFERENCES system(id)
                   FOREIGN KEY (planet_id) REFERENCES planet(id))
                   ENGINE=INNODB;
                   
CREATE TABLE log (ship_id INT NOT NULL, system_id INT NOT NULL, planet_id INT NOT NULL, stamp BIGINT NOT NULL, type VARCHAR(16) NOT NULL, text VARCHAR(64) NOT NULL);

CREATE TABLE commodity (id int auto_increment not null,
                   name varchar(64) not null, image varchar(32) not null,
                   source varchar(4) not null,
                   cost int not null, dt int default 0,
                   production int not null, consumption int not null,
                   law int default 6, tech int default 0,
                   codes varchar(64) not null,
                   PRIMARY KEY(id), UNIQUE KEY(name))
                   ENGINE=INNODB;

# Basic goods
#INSERT INTO commodity VALUES(0, 'Food', 'Ag', 1000, 1, 8, 8, 6, 1, 'Vi Pe Tl');
#INSERT INTO commodity VALUES(0, 'Textiles', 'Ag', 3000, 1, 7, 6, 6, 1, 'Vi');
#INSERT INTO commodity VALUES(0, 'Minerals', 'Mi', 1000, 1, 6, 5, 6, 3, 'In');
#INSERT INTO commodity VALUES(0, 'Alloys', 'In', 1000, 1, 5, 4, 6, 5, 'In Tl');
#INSERT INTO commodity VALUES(0, 'Machinary', 'In', 1000, 1, 4, 3, 6, 6, 'Tl');

# Basic ores
INSERT INTO commodity VALUES(0, 'Silicate ore', 'silicate', 'Mi', 50, 1, 7, 4, 6, 3, 'Or');
INSERT INTO commodity VALUES(0, 'Carbonic ore', 'carbonic', 'Mi', 100, 1, 6, 4, 6, 3, 'Or Lt Mt Ht Ut');
INSERT INTO commodity VALUES(0, 'Ferric ore', 'ferric', 'Mi', 150, 1, 5, 3, 6, 2, 'Or');
INSERT INTO commodity VALUES(0, 'Aquam solution', 'aquam', 'Mi', 75, 1, 6, 3, 6, 6, 'Or In Mt Ht Ut');
INSERT INTO commodity VALUES(0, 'Auram gas', 'auram', 'Mi', 120, 1, 5, 3, 6, 6, 'Or In Ht Ut');

# Uncommon ores
INSERT INTO commodity VALUES(0, 'Krysite ore', 'silicate', 'Mi', 100, 1, 6, 2, 6, 8, 'Or In Mt Ht Ut');
INSERT INTO commodity VALUES(0, 'Heliacate ore', 'carbonic', 'Mi', 200, 1, 5, 2, 6, 9, 'Or In Ht Ut');
INSERT INTO commodity VALUES(0, 'Vardonnek ore', 'ferric', 'Mi', 300, 1, 4, 2, 6, 8, 'Or In Ht Ut');
INSERT INTO commodity VALUES(0, 'Doric crystals', 'aquam', 'Mi', 250, 1, 5, 2, 6, 9, 'Or In Hz Ut');
INSERT INTO commodity VALUES(0, 'Regiam gas', 'auram', 'Mi', 440, 1, 4, 2, 6, 7, 'Or In HZ Ht Ut');

# Rare ores
INSERT INTO commodity VALUES(0, 'Magnesite ore', 'silicate', 'Mi', 300,  1, 5, 2, 6, 8, 'Or In Hz Ht Ut');
INSERT INTO commodity VALUES(0, 'Acenite ore', 'carbonic', 'Mi', 500, 1, 4, 1, 6, 9, 'Or In Hz Ht Ut Sp');
INSERT INTO commodity VALUES(0, 'Larathic ore', 'ferric', 'Mi', 600, 1, 3, 1, 6, 9, 'Or In Ut');
INSERT INTO commodity VALUES(0, 'Iskine crystals', 'aquam', 'Mi', 300, 1, 4, 1, 6, 8, 'Or In Ag Ht Ut');
INSERT INTO commodity VALUES(0, 'Tritanium gas', 'auram', 'Mi', 480, 1, 3, 1, 6, 9, 'Or In Ht Ut');

# Very rare ores
INSERT INTO commodity VALUES(0, 'Ericate ore', 'silicate', 'Mi', 500, 1, 4, 0, 6,9, 'Or In Hz');
INSERT INTO commodity VALUES(0, 'Pardenic ore', 'carbonic', 'Mi', 800, 1, 3, 0, 6, 10, 'Or In');
INSERT INTO commodity VALUES(0, 'Xithantite ore', 'ferric', 'Mi', 1200, 1, 2, 0, 6, 10, 'Or In');
INSERT INTO commodity VALUES(0, 'Oorcine ices', 'aquam', 'Mi', 600, 1, 3, 0, 6, 9, 'Or In Fr');
INSERT INTO commodity VALUES(0, 'Synthosium gas', 'auram', 'Mi', 1060, 1, 2, 0, 3, 10, 'Or Hz');

# Basic agricultural resources
INSERT INTO commodity VALUES(0, 'Vegetables', 'vegetables', 'Ag', 100, 1, 10, 8, 6, 1, 'Fo Vi Pe Tl');
INSERT INTO commodity VALUES(0, 'Fruits', 'fruits', 'Ag', 150, 1, 9, 8, 6, 1, 'Fo Vi Pe Tl');
INSERT INTO commodity VALUES(0, 'Meat', 'meat', 'Ag', 350, 1, 7, 8, 6, 1, 'Fo Vi Pe Tl');
INSERT INTO commodity VALUES(0, 'Seafood', 'seafood', 'Ag', 250, 1, 7, 8, 6, 1, 'Fo Vi Pe Tl');
INSERT INTO commodity VALUES(0, 'Algae', 'algae', 'Ag', 50, 1, 7, 8, 6, 1, 'Fo Vi Pe Tl');
INSERT INTO commodity VALUES(0, 'Wood', 'wood', 'Ag', 100, 1, 6, 5, 6, 1, 'In Ag Pt Lt Mt Ht');

# Requirements
CREATE TABLE requirements (commodity_id INT NOT NULL, requires_id INT NOT NULL, number INT DEFAULT 1);

# Measure of what a planet produces naturally. Mostly ores and agricultural produce.
CREATE TABLE resources (planet_id INT NOT NULL, commodity_id INT NOT NULL, density INT NOT NULL,
                        UNIQUE KEY(planet_id, commodity_id)) ENGINE=INNODB;


CREATE TABLE trade (id INT AUTO_INCREMENT NOT NULL, planet_id INT NOT NULL,
                    commodity_id int not null, amount int not null, price int not null,
                    PRIMARY KEY(id),
                    FOREIGN KEY(planet_id) REFERENCES planet(id),
                    FOREIGN KEY(commodity_id) REFERENCES commodity(id))
                    ENGINE=INNODB;
                    
                    
                    
#
# SHIP TEST DATA
#
insert into ship values(0, "Child of Adkynson", "Adder", 14132, 174453, 0, "Docked", 100, 1, 30, 30, "Imperium", 0);
insert into ship values(0, "Child of Free Enterprise", "Adder", 14132, 174453, 0, "Docked", 100, 1, 30, 30, "Imperium", 0);
insert into ship values(0, "Child of Boccob", "Adder", 14132, 174453, 0, "Docked", 100, 1, 30, 30, "Imperium", 0);
insert into ship values(0, "Child of Tax Havens", "Adder", 14132, 174453, 0, "Docked", 100, 1, 30, 30, "Imperium", 0);
insert into ship values(0, "Child of Greed", "Adder", 14132, 174453, 0, "Docked", 100, 1, 30, 30, "Imperium", 0);
insert into ship values(0, "Child of Liberty", "Adder", 14132, 174453, 0, "Docked", 100, 1, 30, 30, "Imperium", 0);
insert into ship values(0, "Child of Mathematics", "Adder", 14132, 174453, 0, "Docked", 100, 1, 30, 30, "Imperium", 0);
insert into ship values(0, "Child of Vacuum", "Adder", 14132, 174453, 0, "Docked", 100, 1, 30, 30, "Imperium", 0);
insert into ship values(0, "Child of the Stars", "Adder", 14132, 174453, 0, "Docked", 100, 1, 30, 30, "Imperium", 0);
insert into ship values(0, "Child of Serendipity", "Adder", 14132, 174453, 0, "Docked", 100, 1, 30, 30, "Imperium", 0);
