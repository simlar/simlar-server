CREATE TABLE IF NOT EXISTS simlar_sms_sent_log (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `telephoneNumber` varchar(64) NOT NULL,
    `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `dlrNumber` varchar(64) DEFAULT NULL,
    `dlrStatus` int(10) signed NOT NULL DEFAULT -1,
    `dlrTimestamp` TIMESTAMP DEFAULT 0,
    `smsTradeStatus` int(10) signed NOT NULL DEFAULT -1,
    `twilioStatus` varchar(64) DEFAULT NULL,
    `twilioError` varchar(64) DEFAULT NULL,
    `message` varchar(170) DEFAULT NULL,
    KEY `request_dlr_timestamp` (`dlrTimestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1"
