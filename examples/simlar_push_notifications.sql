CREATE TABLE IF NOT EXISTS simlar_push_notifications (
    `simlarId` varchar(64) NOT NULL,
    `deviceType` int(10) unsigned NOT NULL,
    `pushId` text NOT NULL,
    PRIMARY KEY (`simlarId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
