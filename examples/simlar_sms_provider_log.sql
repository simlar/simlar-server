CREATE TABLE IF NOT EXISTS simlar_sms_provider_log (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `type` varchar(64) DEFAULT NULL,
    `telephoneNumber` varchar(64) NOT NULL,
    `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `sessionId` varchar(64) DEFAULT NULL,
    `callbackTimestamp` timestamp NULL DEFAULT NULL,
    `status` varchar(64) DEFAULT NULL,
    `error` varchar(64) DEFAULT NULL,
    `message` varchar(170) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
