CREATE DATABASE kamailio;

CREATE USER 'simlar'@'localhost' IDENTIFIED BY 'changeMe123';
GRANT ALL PRIVILEGES ON kamailio.* TO 'simlar'@'localhost';

CREATE TABLE `subscriber` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `username` varchar(64) NOT NULL DEFAULT '',
    `domain` varchar(64) NOT NULL DEFAULT '',
    `password` varchar(25) NOT NULL DEFAULT '',
    `email_address` varchar(64) NOT NULL DEFAULT '',
    `ha1` varchar(64) NOT NULL DEFAULT '',
    `ha1b` varchar(64) NOT NULL DEFAULT '',
    `rpid` varchar(64) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `account_idx` (`username`,`domain`),
    KEY `username_idx` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
