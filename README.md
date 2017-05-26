simlar-server
==============

[![Build Status](https://travis-ci.org/simlar/simlar-server.svg?branch=master)](https://travis-ci.org/simlar/simlar-server)


**This project is work in progress, incomplete and not ready for production, yet.**


[Simlar](https://www.simlar.org) is a cross platform VoIP App aiming to make ZRTP encrypted calls easy.

<div id="screenshots" align="center">
<img src="https://www.simlar.org/press/screenshots/Android/en/talking-to-so.png" alt="Screenshot call" text-align="center" width="200" margin="15">
<img src="https://www.simlar.org/press/screenshots/iOS/ongoing_call.png" alt="Screenshot call" text-align="center" width="200">
</div>

You may start the simlar-server standalone e.g. for development.
But for a useful setup you will at least need the following servers.
Maybe some alternatives will work, too:
* [Apache Tomcat](https://tomcat.apache.org/)
* [Apache Web Server](https://httpd.apache.org/)
* [Kamailio SIP Server](https://www.kamailio.org/)


### Build dependencies ###
Java Development Kit 1.8

### Compile (Console) ###
```
./gradlew build
```

### Run ###
As the simlar-server is a spring-boot application you may start it with an embed tomcat server and an in-memory database.
```
./gradlew bootRun
```

### Build war ###
It is recommended to run clean before building the war file. This may lead to smaller wars.
```
./gradlew clean war
```
### Check dependencies ###
The simlar-server uses the [gradle versions plugin](https://github.com/ben-manes/gradle-versions-plugin). You may check for dependency updates like:
```
./gradlew dependencyUpdates -Drevision=release
```

## IntelliJ IDEA CE ##
We use the [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/) for development.

### Inspection ###
Initially importing simlar-server removes the inspection settings. That's why we recommend to run the following command once after importing:
```
git checkout .idea/
```

### Dictionary ###
In Settings/Editor/Spelling choose the tab Dictionaries and add ```ides/intellij/dictionaries/``` to list of Custom Dictionary Folders.

### Workaround server start ###
Unfortunately IntelliJ has a bug, if you start the server within it.
To workaround it, you may comment out a line your ```build.gradle``` like this:
```
runtime("mysql:mysql-connector-java:5.1.38")
//providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
```
Now update IntelliJ by opening the gradle tool window and clicking the refresh button.
After that, you may revert your change.
