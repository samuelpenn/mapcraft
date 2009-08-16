
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
                        language varchar(16),
                        tech int default 0, law int default 0, population int default 0,
                        PRIMARY KEY (id), KEY(code), KEY(name));

INSERT INTO allegiance values(0, 'Un', 'Unaligned', '#000000', NULL, 0, 0);

#
# SECTOR
#
CREATE TABLE sector (id int auto_increment not null, name varchar(250) not null, 
					 x int not null, y int not null, codes varchar(32) default '', 
					 PRIMARY KEY(id)) ENGINE=INNODB;

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

CREATE TABLE facility (id int auto_increment not null, name varchar(64), type varchar(16), image varchar(32),
                       techLevel int default 0, capacity int default 0,
                       resource_id int default 0, inputs varchar(32) default '', outputs varchar(32) default '',
                       codes varchar(64) not null,
                       PRIMARY KEY(id), UNIQUE KEY(name)) ENGINE=INNODB;

CREATE TABLE facilities (facility_id int not null, planet_id int not null, size int not null,
                         UNIQUE KEY (facility_id, planet_id)) ENGINE=INNODB;

# Standard residential facilities
INSERT INTO facility VALUES(0, 'Primitive',        'Residential', 'res_primitive',  0,  1, 2, '', '', '');
INSERT INTO facility VALUES(0, 'Iron Age',         'Residential', 'res_ironage',    2,  1, 2, '', '', '');
INSERT INTO facility VALUES(0, 'Medieval',         'Residential', 'res_medieval',   3,  1, 2, '', '', '');
INSERT INTO facility VALUES(0, 'High Medieval',    'Residential', 'res_medieval',   4,  1, 2, '', '', '');
INSERT INTO facility VALUES(0, 'Early Industrial', 'Residential', 'res_industrial', 5,  1, 2, '', '', '');
INSERT INTO facility VALUES(0, 'Late Industrial',  'Residential', 'res_industrial', 6,  1, 2, '', '', '');
INSERT INTO facility VALUES(0, 'High Industrial',  'Residential', 'res_highind',    7,  1, 2, '', '', '');
INSERT INTO facility VALUES(0, 'Early Space Age',  'Residential', 'res_space',      8,  1, 2, '', '', '');
INSERT INTO facility VALUES(0, 'Early Imperium',   'Residential', 'res_early',      9,  1, 2, '', '', '');
INSERT INTO facility VALUES(0, 'Imperium',         'Residential', 'res_imperium',  10,  1, 2, '', '', '');
INSERT INTO facility VALUES(0, 'High Imperium',    'Residential', 'res_high',      11,  1, 2, '', '', '');
INSERT INTO facility VALUES(0, 'Advanced Imperium','Residential', 'res_advanced',  12,  1, 2, '', '', '');


# TL0 Cultures (Neolithic)
INSERT INTO facility VALUES(0, 'Neolithic settlements', 'Residential', 'res_primitive',  0,  1, 0, '', '', '');
INSERT INTO facility VALUES(0, 'Gatherers',         'Agriculture', 'agriculture',    0,  1, 200, '', '', '');
INSERT INTO facility VALUES(0, 'Hunter',            'Agriculture', 'agriculture',    0,  1, 203, '', '', '');

# TL1 Cultures (Bronze age)
INSERT INTO facility VALUES(0, 'Bronze age rural',  'Residential', 'res_primitive',  1,  1, 0, '', '', '');
INSERT INTO facility VALUES(0, 'Simple farming',    'Agriculture', 'agriculture',    1,  1, 200, '', '', '');
INSERT INTO facility VALUES(0, 'Simple livestock',  'Agriculture', 'agriculture',    1,  1, 203, '', '', '');

# TL2 Cultures (Iron age)
INSERT INTO facility VALUES(0, 'Iron age rural',    'Residential', 'res_primitive',  1,  1, 0, '', '', '');
INSERT INTO facility VALUES(0, 'Iron age cities',   'Residential', 'res_primitive',  1,  1, 0, '', '', '');
INSERT INTO facility VALUES(0, 'Primitive mining',  'Mining',      'mi_iron',        2,  1, 1, '', '', '');

# TL3 Cultures (Medieval)
INSERT INTO facility VALUES(0, 'Medieval colony',      'Residential', 'res_medieval',      3,  0, 2, '', '', '');
INSERT INTO facility VALUES(0, 'Medieval settlements', 'Residential', 'res_medieval',      3,  3, 2, '1501;3100', '', '');
INSERT INTO facility VALUES(0, 'Medieval cities',      'Residential', 'res_medieval_city', 3,  7, 2, '1000;1501;3100', '', '');

INSERT INTO facility VALUES(0, 'Medieval farm steads', 'Agriculture', 'ag_medieval',       3,  0, 2130, '', '', 'FC');
INSERT INTO facility VALUES(0, 'Medieval farming',     'Agriculture', 'ag_medieval',       3,  3, 2130, '2140,X', '', 'FC');
INSERT INTO facility VALUES(0, 'Medieval husbandry',   'Agriculture', 'ag_medieval',       3,  3, 2400, '', '', 'FA');

# Mining
INSERT INTO facility VALUES(0, 'LoTech Mines',     'Mining', 'mining',  4,  2, 1, '206,X', '', '');
INSERT INTO facility VALUES(0, 'Industrial Mines', 'Mining', 'mining',  6,  4, 1, '6,X', '', '');
INSERT INTO facility VALUES(0, 'HiTech Mines',     'Mining', 'mining',  8,  8, 1, '6,X;7,X', '', '');
INSERT INTO facility VALUES(0, 'UltraTech Mines',  'Mining', 'mining', 10, 16, 1, '6,X;7,X', '', '');

# Agriculture
INSERT INTO facility VALUES(0, 'Simple farming',   'Agriculture', 'agriculture',  1,   2, 200, '', '', '');
INSERT INTO facility VALUES(0, 'Agriculture',      'Agriculture', 'agriculture',  3,   3, 200, '', '', '');
INSERT INTO facility VALUES(0, 'Agriculture 5',    'Agriculture', 'agriculture',  5,   5, 200, '', '', '');
INSERT INTO facility VALUES(0, 'Agriculture 7',    'Agriculture', 'agriculture',  7,  10, 200, '', '', '');
INSERT INTO facility VALUES(0, 'Agriculture 9',    'Agriculture', 'agriculture',  9,  25, 200, '', '', '');
INSERT INTO facility VALUES(0, 'Agriculture 11',   'Agriculture', 'agriculture', 11, 100, 200, '', '', '');

INSERT INTO facility VALUES(0, 'Fishing 4',        'Agriculture', 'agriculture',  4,   2, 204, '', '', '');
INSERT INTO facility VALUES(0, 'Fishing 6',        'Agriculture', 'agriculture',  6,   4, 204, '', '', '');
INSERT INTO facility VALUES(0, 'Fishing 8',        'Agriculture', 'agriculture',  8,  10, 204, '', '', '');
INSERT INTO facility VALUES(0, 'Fishing 10',       'Agriculture', 'agriculture', 10,  25, 204, '', '', '');

INSERT INTO facility VALUES(0, "Farm",       "Agriculture", "agriculture",  2,     0, 0, 'X,1', 'X,1');
INSERT INTO facility VALUES(0, "Mine",       "Mining", "mining",            3,     0, 0, 'X,1', 'X,1');
INSERT INTO facility VALUES(0, "Refinery",   "Industry", "industry",        4,     0, 1, '1,5', '5,3');
INSERT INTO facility VALUES(0, "Factory",    "Industry", "industry",        5,     0, 5, '5,5', '6,1');



# Basic goods
INSERT INTO commodity VALUES(1, 'Minerals', 'mineral',    'Mi', 0,  500, 1,  5, 5,  6, 3, 'Or');
INSERT INTO commodity VALUES(2, 'Food', 'agricultural',   'Ag', 0,  250, 1,  5, 5,  6, 1, 'Fo Vi Pe');
INSERT INTO commodity VALUES(3, 'Textiles', 'textiles',   'Ag', 0,  700, 1,  5, 5,  6, 1, 'Cl');
INSERT INTO commodity VALUES(4, 'Luxuries', 'luxuries',   'Ag', 0, 5000, 1,  5, 5,  6, 3, 'Lu');
INSERT INTO commodity VALUES(5, 'Alloys', 'alloys',       'In', 0, 1500, 1,  5, 5,  6, 5, 'Ma Tl In');
INSERT INTO commodity VALUES(6, 'Machinery', 'machinery', 'In', 0, 3000, 1,  5, 5,  6, 5, 'Ma Tl');
INSERT INTO commodity VALUES(7, 'Computers', 'computers', 'In', 0, 5000, 1,  5, 5,  6, 7, 'El TL');
INSERT INTO commodity VALUES(8, 'Consumer goods', 'consumer', 'In', 0, 1000, 1, 5, 5, 6, 7, '');



# Silicate ores
INSERT INTO commodity VALUES(1000, 'Silicate ore',  'silicate', 'Mi',    1,  50, 1, 7, 4, 6, 3, 'Or');
INSERT INTO commodity VALUES(1001, 'Krysite ore',   'silicate', 'Mi', 1000, 100, 1, 6, 2, 6, 8, 'Or In Mt Ht Ut');
INSERT INTO commodity VALUES(1002, 'Magnesite ore', 'silicate', 'Mi', 1000, 300,  1, 5, 2, 6, 8, 'Or In Hz Ht Ut');
INSERT INTO commodity VALUES(1003, 'Ericate ore',   'silicate', 'Mi', 1000, 500, 1, 4, 0, 6,9, 'Or In Hz');

# Carbonic ores
INSERT INTO commodity VALUES(1100, 'Carbonic ore',  'carbonic', 'Mi',    1, 100, 1, 6, 4, 6, 3, 'Or Lt Mt Ht Ut');
INSERT INTO commodity VALUES(1101, 'Heliacate ore', 'carbonic', 'Mi', 1100, 200, 1, 5, 2, 6, 9, 'Or In Ht Ut');
INSERT INTO commodity VALUES(1102, 'Acenite ore',   'carbonic', 'Mi', 1100, 500, 1, 4, 1, 6, 9, 'Or In Hz Ht Ut Sp');
INSERT INTO commodity VALUES(1103, 'Pardenic ore',  'carbonic', 'Mi', 1100, 800, 1, 3, 0, 6, 10, 'Or In');

# Ferric ores
INSERT INTO commodity VALUES(1200, 'Ferric ore',    'ferric', 'Mi',    1,  150, 1, 5, 3, 6, 2, 'Or');
INSERT INTO commodity VALUES(1201, 'Vardonnek ore', 'ferric', 'Mi', 1200,  300, 1, 4, 2, 6, 8, 'Or In Ht Ut');
INSERT INTO commodity VALUES(1202, 'Larathic ore',  'ferric', 'Mi', 1200,  600, 1, 3, 1, 6, 9, 'Or In Ut');
INSERT INTO commodity VALUES(1203, 'Xithantite ore','ferric', 'Mi', 1200, 1200, 1, 2, 0, 6, 10, 'Or In');

# Aquam ores
INSERT INTO commodity VALUES(1300, 'Water',          'aquam', 'Mi',    1,  75, 1, 12, 3, 6, 6, 'Or In Mt Ht Ut');
INSERT INTO commodity VALUES(1301, 'Doric crystals', 'aquam', 'Mi', 1300, 250, 1,  5, 2, 6, 9, 'Or In Hz Ut');
INSERT INTO commodity VALUES(1302, 'Iskine crystals','aquam', 'Mi', 1300, 300, 1,  4, 1, 6, 8, 'Or In Ag Ht Ut');
INSERT INTO commodity VALUES(1303, 'Oorcine ices',   'aquam', 'Mi', 1300, 600, 1,  3, 0, 6, 9, 'Or In Fr');

# Auram ores
INSERT INTO commodity VALUES(1400, 'Air',            'auram', 'Mi',    1,  120, 1, 11, 3, 6, 6, 'Or In Ht Ut');
INSERT INTO commodity VALUES(1401, 'Regiam gas',     'auram', 'Mi', 1400,  440, 1,  5, 2, 6, 7, 'Or In HZ Ht Ut');
INSERT INTO commodity VALUES(1402, 'Tritanium gas',  'auram', 'Mi', 1400,  480, 1,  3, 1, 6, 9, 'Or In Ht Ut');
INSERT INTO commodity VALUES(1403, 'Synthosium gas', 'auram', 'Mi', 1400, 1060, 1,  2, 0, 3, 10, 'Or Hz');

# Other resources
INSERT INTO commodity VALUES(1500, 'Petroleum',      'oil',   'Mi',    1,  200, 1, 6, 5, 6, 6, 'Or In Mt Ht Ut');
INSERT INTO commodity VALUES(1501, 'Wood',           'wood',  'Ag',    1,  100, 1, 7, 6, 6, 1, 'In Ag Nt Pt Lt Mt Ht');

# Basic agricultural resources
INSERT INTO commodity VALUES(2000, 'Marine food',     'seafood',         'Ag', 2,    100, 1, 10,  7, 6, 1, 'Fo Vi Pe Tl');
INSERT INTO commodity VALUES(2001, 'Primitive food',  'metazoa',         'Ag', 2000, 100, 1, 10,  7, 6, 1, 'Fo Vi Pe Tl');
INSERT INTO commodity VALUES(2002, 'Base organics',   'base_organics',   'Ag', 2001,   5, 1, 4,  12, 6, 1, 'Fo Pe FO FM Lq');
INSERT INTO commodity VALUES(2003, 'Simple organics', 'simple_organics', 'Ag', 2001,  10, 1, 4,  11, 6, 1, 'Fo Pe FO FM Lq');
INSERT INTO commodity VALUES(2004, 'Metazoa',         'metazoa',         'Ag', 2001,   8, 1, 4,  11, 6, 1, 'Fo Pe FO FM Lq');

INSERT INTO commodity VALUES(2010, 'Simple marine',   'simple_marine',   'Ag', 2000,  35, 1, 5,   6, 6, 1, 'Fo Pe Lq Tl');
INSERT INTO commodity VALUES(2011, 'Crustacean',      'shellfish',       'Ag', 2010,  35, 1, 5,   6, 6, 1, 'Fo Pe Lq Tl');
INSERT INTO commodity VALUES(2012, 'Jellyfish',       'jellyfish',       'Ag', 2010,  40, 1, 6,   7, 6, 1, 'Fo Pe Lq Tl');
INSERT INTO commodity VALUES(2013, 'Sponges',         'sponges',         'Ag', 2010,  20, 1, 5,   9, 6, 1, 'Fo Pe Lq Tl');

INSERT INTO commodity VALUES(2020, 'Plankton',        'plankton',        'Ag', 2000,   20, 1, 5,  10, 6, 1, 'Fo Pe Lq Tl');

INSERT INTO commodity VALUES(2030, 'Marine plants',   'seaweed',         'Ag', 2000,   20, 1, 5,  10, 6, 1, 'Fo Pe Lq Tl');
INSERT INTO commodity VALUES(2031, 'Algae',           'algae',           'Ag', 2030,   50, 1, 7,  10, 6, 1, 'Fo Lq Pe Tl');
INSERT INTO commodity VALUES(2032, 'Seaweed',         'seaweed',         'Ag', 2030,   65, 1, 6,   8, 6, 1, 'Fo Pe Lq Tl');

INSERT INTO commodity VALUES(2040, 'Fish',            'fish',            'Ag', 2000,  150, 1, 7,   5, 6, 1, 'Fo Pe Vi Tl');
INSERT INTO commodity VALUES(2041, 'Small fish',      'fish',            'Ag', 2040,  150, 1, 7,   5, 6, 1, 'Fo Pe Vi Tl');
INSERT INTO commodity VALUES(2042, 'Medium fish',     'fish',            'Ag', 2040,  150, 1, 7,   5, 6, 1, 'Fo Pe Vi Tl');
INSERT INTO commodity VALUES(2043, 'Large fish',      'fish',            'Ag', 2040,  150, 1, 7,   5, 6, 1, 'Fo Pe Vi Tl');

INSERT INTO commodity VALUES(2100, 'Land food',       'food',            'Ag', 2,     100, 1, 10,  7, 6, 1, 'Fo');
INSERT INTO commodity VALUES(2101, 'Land plants',     'food',            'Ag', 2100,  100, 1, 10,  7, 6, 1, 'Fo');
INSERT INTO commodity VALUES(2104, 'Land animals',    'meat',            'Ag', 2100,  350, 1, 7,   5, 6, 1, 'Fo');
INSERT INTO commodity VALUES(2110, 'Moss',            'moss',            'Ag', 2100,   20, 1, 4,   9, 6, 1, 'Fo Pe Tl FV Lq');
INSERT INTO commodity VALUES(2120, 'Fungi',           'fungi',           'Ag', 2100,   50, 1, 5,   7, 6, 1, 'Fo Pe Tl FV FC');
INSERT INTO commodity VALUES(2130, 'Crops',           'food',            'Ag', 2100,  100, 1, 10,  7, 6, 1, 'Fo');
INSERT INTO commodity VALUES(2131, 'Ferns',           'ferns',           'Ag', 2130,   30, 1, 6,   8, 6, 1, 'Fo Pe Tl FV FC Lq');
INSERT INTO commodity VALUES(2132, 'Vegetables',      'vegetables',      'Ag', 2130,  100, 1, 10,  7, 6, 1, 'Fo Pe Tl FV FC Vi');
INSERT INTO commodity VALUES(2133, 'Fruits',          'fruits',          'Ag', 2130,  150, 1, 9,   7, 6, 1, 'Fo Pe Tl FV FC');
INSERT INTO commodity VALUES(2134, 'Grain',           'grain',           'Ag', 2130,   75, 1, 10,  6, 6, 2, 'Fo Pe Tl FV FC Vi');
INSERT INTO commodity VALUES(2135, 'Rice',            'rice',            'Ag', 2130,   55, 1, 7,   8, 6, 1, 'Fo Pe Tl FV FC');


INSERT INTO commodity VALUES(2200, 'Insects',         'insects',         'Ag', 2100,   50, 1, 6,   6, 6, 1, 'Fo Pe FA Lq');
INSERT INTO commodity VALUES(2300, 'Amphibians',      'amphibians',      'Ag', 2100,  150, 1, 6,   6, 6, 1, 'Fo Pe FA');
INSERT INTO commodity VALUES(2400, 'Animals',         'medium_animals',  'Ag', 2100,  150, 1, 6,   6, 6, 1, 'Fo Pe FA');
INSERT INTO commodity VALUES(2410, 'Tiny animals',    'tiny_animals',    'Ag', 2400,  175, 1, 6,   5, 6, 1, 'Fo Pe FA Lq');
INSERT INTO commodity VALUES(2420, 'Small animals',   'small_animals',   'Ag', 2400,  2, 1, 7,   5, 6, 1, 'Fo Pe FA');
INSERT INTO commodity VALUES(2430, 'Medium animals',  'medium_animals',  'Ag', 2400,  250, 1, 8,   5, 6, 1, 'Fo Pe FA Hq');
INSERT INTO commodity VALUES(2440, 'Large animals',   'large_animals',   'Ag', 2400,  350, 1, 7,   4, 6, 1, 'Fo Pe FA Hq Lu');
INSERT INTO commodity VALUES(2450, 'Huge animals',    'huge_animals',    'Ag', 2400,  450, 1, 7,   4, 6, 1, 'Fo Pe FA Hq Lu');

INSERT INTO commodity VALUES(3000, 'Wool',                'textiles',    'In',    3, 100, 1, 7, 4, 6, 1, 'In');
INSERT INTO commodity VALUES(3001, 'Silk',                'textiles',    'In',    3, 100, 1, 7, 4, 6, 3, 'In');
INSERT INTO commodity VALUES(3002, 'Simple textiles',     'textiles',    'In',    3, 100, 1, 7, 4, 6, 1, 'In');

INSERT INTO commodity VALUES(3100, 'Primitive clothing',  'textiles',    'In',    3, 100, 1, 7, 4, 6, 0, 'In Cl Nt Pt Lt');
INSERT INTO commodity VALUES(3101, 'Furs',                'textiles',    'In', 3100, 100, 1, 7, 4, 6, 0, 'In Cl Nt Pt');
INSERT INTO commodity VALUES(3102, 'Animal skins',        'textiles',    'In', 3100, 100, 1, 7, 4, 6, 0, 'In Cl Nt');
INSERT INTO commodity VALUES(3103, 'Simple leathers',     'textiles',    'In', 3100, 100, 1, 7, 4, 6, 2, 'In Cl Pt Lt');

INSERT INTO commodity VALUES(3200, 'Civilian clothing',   'textiles',    'In',    3, 100, 1, 7, 4, 6, 1, 'In Cl');
INSERT INTO commodity VALUES(3300, 'Industrial clothing', 'textiles',    'In',    3, 100, 1, 7, 4, 6, 1, 'In Cl');
INSERT INTO commodity VALUES(3400, 'Military clothing',   'textiles',    'In',    3, 100, 1, 7, 4, 6, 1, 'In Cl');


INSERT INTO commodity VALUES(5000, 'Metals',          'alloys', 'In', 5,  900, 1, 5, 5, 6, 1, 'In');
INSERT INTO commodity VALUES(5001, 'Refined metals',  'alloys', 'In', 5, 1600, 1, 5, 5, 6, 4, 'In');

INSERT INTO commodity VALUES(6000, 'Agricultural machinary',  'machinery', 'In',    6, 2500, 1, 3, 3, 6, 5, 'Ag');
INSERT INTO commodity VALUES(6100, 'Stone farm tools',        'tools',     'In', 6000,  150, 1, 4, 4, 6, 0, 'Ag');
INSERT INTO commodity VALUES(6101, 'Bronze farm tools',       'tools',     'In', 6000,  200, 1, 4, 4, 6, 1, 'Ag');
INSERT INTO commodity VALUES(6102, 'Iron farm tools',         'tools',     'In', 6000,  250, 1, 4, 4, 6, 2, 'Ag');
INSERT INTO commodity VALUES(6103, 'Medieval farm tools',     'tools',     'In', 6000,  300, 1, 4, 4, 6, 3, 'Ag');

INSERT INTO commodity VALUES(500, 'Farm implements',        'machinery', 'In', 6, 1000, 1, 4, 7, 6,  3, 'Ag Ma Lo');
INSERT INTO commodity VALUES(501, 'Farm machinery',         'machinery', 'In', 6, 1000, 1, 4, 7, 6,  5, 'Ag Ma Lo');
INSERT INTO commodity VALUES(502, 'Agricultural machinery', 'machinery', 'In', 6, 1000, 1, 4, 7, 6,  7, 'Ag Ma Lo');
INSERT INTO commodity VALUES(503, 'Agricultural robotics',  'machinery', 'In', 6, 1000, 1, 4, 7, 6,  9, 'Ag Ma Lo');
INSERT INTO commodity VALUES(504, 'Advanced agribots',      'machinery', 'In', 6, 1000, 1, 4, 7, 6, 11, 'Ag Ma Lo');

INSERT INTO commodity VALUES(600, 'Medieval goods',         'machinery', 'Ag',   6,  250, 1, 4, 7, 6, 3, '');
INSERT INTO commodity VALUES(601, 'Medieval farm tools',    'machinery', 'Ag', 600,  400, 1, 4, 5, 6, 3, 'Ma Ag Lo');
INSERT INTO commodity VALUES(602, 'Medieval hand tools',    'machinery', 'In', 600,  250, 1, 5, 6, 6, 3, 'Ma Lo');
INSERT INTO commodity VALUES(602, 'Medieval textiles',      'textiles',  'In', 600,  250, 1, 5, 6, 6, 3, 'Ma Lo');

# Requirements
CREATE TABLE requirements (commodity_id INT NOT NULL, requires_id INT NOT NULL, number INT DEFAULT 1);

# Measure of what a planet produces naturally. Mostly ores and agricultural produce.
CREATE TABLE resources (planet_id INT NOT NULL, commodity_id INT NOT NULL, density INT NOT NULL,
                        UNIQUE KEY(planet_id, commodity_id)) ENGINE=INNODB;


CREATE TABLE trade (id INT AUTO_INCREMENT NOT NULL, planet_id INT NOT NULL,
                    commodity_id int not null, amount bigint default 0, consumed bigint default 0, price int not null,
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
