CREATE TABLE IF NOT EXISTS simlar_account_creation_request (
    `simlarId` varchar(64) NOT NULL,
    `type` varchar(64) DEFAULT NULL,
    `password` varchar(64) NOT NULL,
    `registrationCode` varchar(64) NOT NULL,
    `registrationCodeTimestamp` timestamp NOT NULL DEFAULT current_timestamp(),
    `requestTries` int(10) unsigned NOT NULL DEFAULT 1,
    `calls` int(10) unsigned NOT NULL DEFAULT 0,
    `confirmTries` int(10) unsigned NOT NULL DEFAULT 0,
    `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ip` varchar(64) NOT NULL,
    `callTimestamp` timestamp NULL DEFAULT NULL,
    PRIMARY KEY (`simlarId`),
    KEY `request_timestamp` (`timestamp`),
    KEY `request_ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
