CREATE TABLE IF NOT EXISTS simlar_account_creation_request (
    `simlarId` varchar(64) NOT NULL,
    `password` varchar(64) NOT NULL,
    `registrationCode` varchar(64) NOT NULL,
    `requestTries` int(10) unsigned NOT NULL DEFAULT 1,
    `confirmTries` int(10) unsigned NOT NULL DEFAULT 0,
    `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ip` varchar(64) NOT NULL,
    PRIMARY KEY (`simlarId`),
    KEY `request_timestamp` (`timestamp`),
    KEY `request_ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1
