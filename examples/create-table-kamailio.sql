CREATE DATABASE kamailio;

CREATE USER 'simlar'@'localhost' IDENTIFIED BY 'changeMe123';
GRANT ALL PRIVILEGES ON kamailio.* TO 'simlar'@'localhost';

use kamailio;
CREATE TABLE `subscriber` (
    `id` int(10) NOT NULL AUTO_INCREMENT,
    `username` varchar(64) NOT NULL,
    `domain` varchar(64) NOT NULL,
    `password` varchar(64) NOT NULL DEFAULT '',
    `ha1` varchar(128) NOT NULL DEFAULT '',
    `ha1b` varchar(128) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `account_idx` (`username`,`domain`),
    KEY `username_idx` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=latin1;
