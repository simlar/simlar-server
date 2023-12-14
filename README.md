simlar-server
==============

[![Build Status](https://github.com/simlar/simlar-server/workflows/simlar-server-ci/badge.svg?branch=master)](https://github.com/simlar/simlar-server/actions)


**This project is work in progress and not complete, yet.**
But after all this code runs in the simlar production environment.


[Simlar](https://www.simlar.org) is a cross-platform VoIP App aiming to make ZRTP encrypted calls easy.

<!--suppress HtmlUnknownAttribute, HtmlDeprecatedAttribute -->
<div id="screenshots" align="center">
<img src="https://www.simlar.org/press/screenshots/Android/en/talking-to-so.png" alt="Screenshot call" text-align="center" width="200" margin="15">
<img src="https://www.simlar.org/press/screenshots/iOS/ongoing_call.png" alt="Screenshot call" text-align="center" width="200">
</div>

You may start the simlar-server standalone, e.g., for development.
For a useful setup you will at least need the following servers.
Maybe some alternatives will work, too:
* [Apache Tomcat](https://tomcat.apache.org/)
* [Apache Web Server](https://httpd.apache.org/)
* [Kamailio SIP Server](https://www.kamailio.org/)


### Build dependencies ###
Java Development Kit 11

### Compile (Console) ###
```
./gradlew build
```

### Run ###
As the simlar-server is a spring-boot application you may start it with an embedded tomcat server and an in-memory database.
```
./gradlew bootRun
```

### Build war ###
```
./gradlew bootWar
```

### Check dependencies ###
The simlar-server uses the [owasp-dependency-checker](https://www.owasp.org/index.php/OWASP_Dependency_Check). Execute it with:
```
./gradlew dependencyCheckAnalyze
```
The simlar-server uses the [gradle versions plugin](https://github.com/ben-manes/gradle-versions-plugin). Run it with:
```
./gradlew dependencyUpdates
```
Run both:
```
./gradlew dependencyChecks
```

## IntelliJ IDEA CE ##
We use the [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/) for development.
To generate some files for this ide run:
```
./gradlew idea
```
Then simply open (not import) the directory in IntelliJ.

### Lombok
Because the simlar-server uses the [Project Lombok](https://projectlombok.org/), IntelliJ requires the [Lombok Plugin](https://plugins.jetbrains.com/plugin/6317-lombok-plugin) to compile it.
After installing the plugin it is required to enable annotation processing in Settings/Build, Execution,Deployment/Compiler/Annotation Processors.

### Dictionary ###
In order to quiet IntelliJ's inspection warnings import the dictionary.
In Settings/Editor/Spelling choose the tab Dictionaries and add ```ides/intellij/dictionaries/simlar.dic``` to the list of Custom Dictionaries.

## Configuration
A production environment needs a configuration file ```/etc/simlar-server/config.properties```.
E.g., to set the domain and the database.
Have a look at the [example](examples/config.properties).

For development, you may place your configurations in ```src/main/resources/application-default.properties```.
The [example](examples/application-default.properties) configures the database and sets a log pattern with filenames and line numbers.
If you do not want to set up a database for development you may change the dependency type of the h2 database to ```providedRuntime```.

## Build with docker
A docker file provides a defined build environment.
You may create a simlar-server build container like this.
```
docker build --no-cache -t simlar-server-builder docker-files/
```
You may use the container to build the war file.
```
docker run --rm -v $(pwd):/pwd simlar-server-builder:latest bash -c "cd /pwd && ./gradlew --no-daemon --warning-mode all clean build dependencyUpdates dependencyCheckAnalyze"
```
However, caching gradle downloads speeds up the build, and some security options do not hurt.
```
docker run --cap-drop all --security-opt=no-new-privileges --rm -v $(pwd)-docker-gradle-cache:/home/builder/.gradle -v $(pwd):/pwd -e SIMLAR_NVD_API_KEY simlar-server-builder:latest bash -c "cd /pwd && ./gradlew --no-daemon --warning-mode all clean build dependencyUpdates dependencyCheckAnalyze"
```
