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

### 0.9.0
In order to activate push notifications add the following files
```
/etc/simlar-server/simlar-ios-voip.p12
/etc/simlar-server/simlar-org-firebase-credentials.json
```
Furthermore, add these properties to ```/etc/simlar-server/config.properties```
```
## push notifications
push.apiKey=

push.apple.voipCertificatePath=
push.apple.voipCertificatePassword=
push.apple.sslProtocol=TLSv1.3
## expires on 2022/05/20 15:42:02 GMT
push.apple.voipCertificatePinning=sha256/tc+C1H75gj+ap48SMYbFLoh56oSw+CLJHYPgQnm3j9U=

push.google.credentialsJsonPath=
push.google.projectId=
## expires on 2012/12/15 00:00:42 GMT
push.google.firebaseCertificatePinning=sha256/YZPgTZ+woNCCCIW3LH2CxQeLzB/1m42QcCTBSdgayjs=
```


### 0.12.7
Update apple push server certificate pinning in ```/etc/simlar-server/config.properties```
```
## first expires on 2022/05/20 15:42:02 UTC
## second expires on 2028/12/06 23:59:59 UTC
push.apple.voipCertificatePinning=sha256/tc+C1H75gj+ap48SMYbFLoh56oSw+CLJHYPgQnm3j9U=, sha256/1CC6SL5QjEUUEr5JiV4Zw8QxiSkGVmp2CRJ4mm1IhKU=
```
