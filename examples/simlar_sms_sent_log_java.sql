CREATE TABLE IF NOT EXISTS simlar_sms_sent_log_java (
	`id` int(11) NOT NULL AUTO_INCREMENT,
	`type` varchar(64) DEFAULT NULL,
	`telephoneNumber` varchar(64) NOT NULL,
	`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`dlrNumber` varchar(64) DEFAULT NULL,
	`dlrTimestamp` timestamp NULL DEFAULT NULL,
	`twilioStatus` varchar(64) DEFAULT NULL,
	`twilioError` varchar(64) DEFAULT NULL,
	`message` varchar(170) DEFAULT NULL,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
