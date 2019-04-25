### 0.6.0
The property name ```accountCreation``` has changed to ```create.account```. You need to update your config files ```/etc/simlar-server/config.properties``` or ```src/main/resources/application-default.properties```.
```
sed -i "s/accountCreation/create.account/" /etc/simlar-server/config.properties
```

### 0.7.0
The table ```simlar_account_creation_request``` gets a new field ```registrationCodeTimestamp```:
```
ALTER TABLE `simlar_account_creation_request` ADD COLUMN `registrationCodeTimestamp` timestamp NOT NULL DEFAULT current_timestamp() AFTER `registrationCode`;
UPDATE `simlar_account_creation_request` SET `registrationCodeTimestamp` = `timestamp`;
```
