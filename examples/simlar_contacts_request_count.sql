CREATE TABLE IF NOT EXISTS simlar_contacts_request_count (
    `simlarId` varchar(64) NOT NULL,
    `count` int(10) unsigned NOT NULL,
    `hash` varchar(64) NOT NULL,
    `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`simlarId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
