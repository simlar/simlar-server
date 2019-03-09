### 0.6.0
The property name ```accountCreation``` has changed to ```create.account```. You need to update your config files ```/etc/simlar-server/config.properties``` or ```src/main/resources/application-default.properties```.
```
sed -i "s/accountCreation/create.account/" /etc/simlar-server/config.properties
```
