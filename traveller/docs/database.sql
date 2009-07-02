
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
					 uwp varchar(80), selection int default 0,
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
                   source varchar(4) not null, parent_id int not null,
                   cost int not null, dt int default 0,
                   production int not null, consumption int not null,
                   law int default 6, tech int default 0,
                   codes varchar(64) not null,
                   PRIMARY KEY(id), UNIQUE KEY(name))
                   ENGINE=INNODB;

CREATE TABLE facility (id int auto_increment not null, name varchar(64), type varchar(16), image varchar(16),
                       techLevel int default 0, capacity int default 0,
                       resource_id int default 0, inputs varchar(32) default '', outputs varchar(32) default '',
                       codes varchar(64) not null,
                       PRIMARY KEY(id), UNIQUE KEY(name)) ENGINE=INNODB;

CREATE TABLE facilities (facility_id int not null, planet_id int not null, size int not null,
                         UNIQUE KEY (facility_id, planet_id)) ENGINE=INNODB;

INSERT INTO facility VALUES(0, 'Colony',      'Residential', 'residential',  7,   100, 0, '', '', '');
INSERT INTO facility VALUES(0, 'Light rural', 'Residential', 'residential',  0,  1000, 0, '', '', '');
                       
INSERT INTO facility VALUES(0, "Camp",       "Residential", "residential",  0,   100, 0, '', '');
INSERT INTO facility VALUES(0, "Town",       "Residential", "residential",  2,   500, 0, '', '');
INSERT INTO facility VALUES(0, "City",       "Residential", "residential",  3,  2000, 0, '', '');
INSERT INTO facility VALUES(0, "Metropolis", "Residential", "residential",  6,  5000, 0, '', '');
INSERT INTO facility VALUES(0, "Arcology",   "Residential", "residential",  9, 20000, 0, '', '');

INSERT INTO facility VALUES(0, "Farm",       "Agriculture", "agriculture",  2,     0, 0, 'X,1', 'X,1');
INSERT INTO facility VALUES(0, "Mine",       "Mining", "mining",            3,     0, 0, 'X,1', 'X,1');
INSERT INTO facility VALUES(0, "Refinery",   "Industry", "industry",        4,     0, 1, '1,5', '5,3');
INSERT INTO facility VALUES(0, "Factory",    "Industry", "industry",        5,     0, 5, '5,5', '6,1');

INSERT INTO facility VALUES(0, 'Gaian Farm LT', 'Agriculture', 'agriculture',  1,    20, 10, '10,20', '203,3;202,4;201,6;207,7');
INSERT INTO facility VALUES(0, 'Gaian Farm MT', 'Agriculture', 'agriculture',  5,    40, 10, '10,20', '203,6;202,8;201,12;207,14');
INSERT INTO facility VALUES(0, 'Gaian Farm HT', 'Agriculture', 'agriculture',  7,    80, 10, '10,20', '203,10;202,15;201,20;207,35');
INSERT INTO facility VALUES(0, 'Gaian Farm UT', 'Agriculture', 'agriculture',  9,   120, 10, '10,20', '203,10;202,20;201,30;207,60');



# Basic goods
INSERT INTO commodity VALUES(1, 'Minerals', 'mineral',    'Mi', 0,  500, 1,  5, 5,  6, 3, 'Or');
INSERT INTO commodity VALUES(2, 'Food', 'agricultural',   'Ag', 0,  250, 1,  5, 5,  6, 1, 'Fo Vi Pe');
INSERT INTO commodity VALUES(3, 'Textiles', 'textiles',   'Ag', 0,  700, 1,  5, 5,  6, 1, 'Cl');
INSERT INTO commodity VALUES(4, 'Luxuries', 'luxuries',   'Ag', 0, 5000, 1,  5, 5,  6, 3, 'Lu');
INSERT INTO commodity VALUES(5, 'Alloys', 'alloys',       'In', 0, 1500, 1,  5, 5,  6, 5, 'Ma Tl In');
INSERT INTO commodity VALUES(6, 'Machinery', 'machinery', 'In', 0, 3000, 1,  5, 5,  6, 5, 'Ma Tl');

INSERT INTO commodity VALUES(10, 'Gaian Ecology', 'agricultural',     'Ag', 0,  0, 1,  5, 5, 6, 1, 'Fo');
INSERT INTO commodity VALUES(11, 'Jungle Ecology', 'agricultural',    'Ag', 0,  0, 1,  3, 5, 6, 1, 'Fo');
INSERT INTO commodity VALUES(12, 'Coastal Ecology', 'agricultural',   'Ag', 0,  0, 1,  4, 5, 6, 2, 'Fo');
INSERT INTO commodity VALUES(13, 'Oceanic Ecology', 'agricultural',   'Ag', 0,  0, 1,  3, 5, 6, 5, 'Fo');

# Basic ores
INSERT INTO commodity VALUES(20, 'Silicate ore', 'silicate', 'Mi', 1,  50, 1, 7, 4, 6, 3, 'Or');
INSERT INTO commodity VALUES(21, 'Carbonic ore', 'carbonic', 'Mi', 1, 100, 1, 6, 4, 6, 3, 'Or Lt Mt Ht Ut');
INSERT INTO commodity VALUES(22, 'Ferric ore', 'ferric',     'Mi', 1, 150, 1, 5, 3, 6, 2, 'Or');
INSERT INTO commodity VALUES(23, 'Aquam', 'aquam',           'Mi', 1,  75, 1, 6, 3, 6, 6, 'Or In Mt Ht Ut');
INSERT INTO commodity VALUES(24, 'Auram', 'auram',           'Mi', 1, 120, 1, 5, 3, 6, 6, 'Or In Ht Ut');

# Uncommon ores
INSERT INTO commodity VALUES(101, 'Krysite ore', 'silicate',   'Mi', 20, 100, 1, 6, 2, 6, 8, 'Or In Mt Ht Ut');
INSERT INTO commodity VALUES(102, 'Heliacate ore', 'carbonic', 'Mi', 21, 200, 1, 5, 2, 6, 9, 'Or In Ht Ut');
INSERT INTO commodity VALUES(103, 'Vardonnek ore', 'ferric',   'Mi', 22, 300, 1, 4, 2, 6, 8, 'Or In Ht Ut');
INSERT INTO commodity VALUES(104, 'Doric crystals', 'aquam',   'Mi', 23, 250, 1, 5, 2, 6, 9, 'Or In Hz Ut');
INSERT INTO commodity VALUES(105, 'Regiam gas', 'auram',       'Mi', 24, 440, 1, 4, 2, 6, 7, 'Or In HZ Ht Ut');

# Rare ores
INSERT INTO commodity VALUES(106, 'Magnesite ore', 'silicate', 'Mi', 20, 300,  1, 5, 2, 6, 8, 'Or In Hz Ht Ut');
INSERT INTO commodity VALUES(107, 'Acenite ore', 'carbonic', 'Mi', 21, 500, 1, 4, 1, 6, 9, 'Or In Hz Ht Ut Sp');
INSERT INTO commodity VALUES(108, 'Larathic ore', 'ferric', 'Mi', 22, 600, 1, 3, 1, 6, 9, 'Or In Ut');
INSERT INTO commodity VALUES(109, 'Iskine crystals', 'aquam', 'Mi', 23, 300, 1, 4, 1, 6, 8, 'Or In Ag Ht Ut');
INSERT INTO commodity VALUES(110, 'Tritanium gas', 'auram', 'Mi', 24, 480, 1, 3, 1, 6, 9, 'Or In Ht Ut');

# Very rare ores
INSERT INTO commodity VALUES(111, 'Ericate ore', 'silicate', 'Mi', 20, 500, 1, 4, 0, 6,9, 'Or In Hz');
INSERT INTO commodity VALUES(112, 'Pardenic ore', 'carbonic', 'Mi', 21, 800, 1, 3, 0, 6, 10, 'Or In');
INSERT INTO commodity VALUES(113, 'Xithantite ore', 'ferric', 'Mi', 22, 1200, 1, 2, 0, 6, 10, 'Or In');
INSERT INTO commodity VALUES(114, 'Oorcine ices', 'aquam', 'Mi', 23, 600, 1, 3, 0, 6, 9, 'Or In Fr');
INSERT INTO commodity VALUES(115, 'Synthosium gas', 'auram', 'Mi', 24, 1060, 1, 2, 0, 3, 10, 'Or Hz');

# Basic agricultural resources
INSERT INTO commodity VALUES(201, 'Vegetables', 'vegetables', 'Ag', 2, 100, 1, 10, 10, 6, 1, 'Fo Vi Pe Tl');
INSERT INTO commodity VALUES(202, 'Fruits',     'fruits', 'Ag', 2,     150, 1, 9,  10, 6, 1, 'Fo Vi Pe Tl');
INSERT INTO commodity VALUES(203, 'Meat',       'meat', 'Ag', 2,       350, 1, 7,   9, 6, 1, 'Fo Lu Pe Tl');
INSERT INTO commodity VALUES(204, 'Seafood',    'seafood', 'Ag', 2,    150, 1, 7,  10, 6, 1, 'Fo Pe Tl');
INSERT INTO commodity VALUES(205, 'Algae',      'algae', 'Ag', 2,       50, 1, 7,  11, 6, 1, 'Fo Lq Pe Tl');
INSERT INTO commodity VALUES(206, 'Wood',       'wood', 'Ag', 0,       100, 1, 6,   7, 6, 1, 'In Ag Pt Lt Mt Ht');
INSERT INTO commodity VALUES(207, 'Grain',      'grain', 'Ag', 2,       75, 1, 10, 11, 6, 2, 'Fo Vi Pe Tl');
INSERT INTO commodity VALUES(208, 'Fish',       'seafood', 'Ag', 204,  150, 1, 7,  10, 6, 1, 'Fo Pe Vi Tl');
INSERT INTO commodity VALUES(209, 'Shellfish',  'seafood', 'Ag', 204,  125, 1, 7,  10, 6, 1, 'Fo Pe Tl');
INSERT INTO commodity VALUES(210, 'Seaweed',    'seafood', 'Ag', 204,   65, 1, 7,  12, 6, 1, 'Fo Pe Lq Tl');
INSERT INTO commodity VALUES(211, 'Rice',       'rice', 'Ag', 2,        55, 1, 7,  12, 6, 1, 'Fo Pe Lq Tl');

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
