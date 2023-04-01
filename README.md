Apache Hop GIS Plugins
================================

This project allows you to manage GIS data in Apache Hop, [Hop Orchestration Platform](https://hop.apache.org/). It is a counterpart of [Pentaho Data Integrator GIS Plugins](https://github.com/atolcd/pentaho-gis-plugins).

Tested with Apache Hop `2.5.0-SNAPSHOT`.


Building the plugins
-------------------
Check out the project if you have not already done so (with `git` installed):

```sh
git clone git://github.com/atolcd/hop-gis-plugins.git
cd hop-gis-plugins
```

Run the following commands from the base project directory (with `java 11` and `maven` installed):
```sh
mvn clean package
```

Or with Docker (you need `docker`) :
```sh
docker run --network="host" --rm \
  --name hop-gis-plugins-builder \
  -u $(id -u):$(id -g) \
  -v "$(pwd)":/app -w /app \
  -v ~/.m2:/var/maven/.m2 -v "${HOME}":/var/maven \
  -e HOME=/var/maven -e MAVEN_CONFIG=/var/maven/.m2 -e MAVEN_OPTS="-Duser.home=/var/maven" \
  \
  maven:3-jdk-11 \
  \
  mvn clean package
```

The built package is `assemblies/target/gis-plugin-assemblies-X.X.X.zip` (version can differ).


Installing/upgrading the module
---------------------

***Method 1 : Manual installation***

Extract the content of `gis-plugin-assemblies-X.X.X.zip` in ${HOP_HOME}/plugins.
Examples of extraction from the root directory of the project :

```sh
GIS_PLUGINS_VERSION="1.1.0"

# Use compiled version...
GIS_PLUGINS_ASSEMBLY="assemblies/target/gis-plugin-assemblies-${GIS_PLUGINS_VERSION}.zip"

# ... Or download a prepared one (must exist :)
wget https://github.com/atolcd/hop-gis-plugins/releases/download/v${GIS_PLUGINS_VERSION}/gis-plugin-assemblies-${GIS_PLUGINS_VERSION}.zip
GIS_PLUGINS_ASSEMBLY="gis-plugin-assemblies-${GIS_PLUGINS_VERSION}.zip"

# Unzip it ate the right place !
unzip ${GIS_PLUGINS_ASSEMBLY} -d ${HOP_HOME}/plugins/
```

To upgrade the plugins, delete files you added before and start a fresh installation.


***Oracle JDBC usage***

If you plan to connect to an Oracle database, add needed jars in lib folder of PDI :

 - ${HOP_HOME}/lib/ojdbc11.jar
 - ${HOP_HOME}/lib/orai18n.jar

You can get them [here](http://www.oracle.com/technetwork/apps-tech/jdbc-112010-090769.html)


Using the plugins
---------------------

See [dedicated page](docs/README.md)

You will find new elements in "Geospatial"'s directory :

 - Geospatial Group by
 - GIS File output
 - GIS File input
 - Geoprocessing
 - Geometry information
 - Coordinate system operation
 - Spatial relationship and proximity

With a french locale :

![](items-in-hop-gui.png)


Code formatting
---------------------

The java code for this project conforms to [Google's code styleguide](https://google.github.io/styleguide/javaguide.html).
The `spotless` maven plugin deals with this aspect:

```sh
# Formatting check
mvn spotless:check

# Formatting (to be done before any commit)
mvn spotless:apply
```


Debugging
---------------------

You can debug plugins remotely in Hop GUI:
```sh
# Set debugging options (or uncomment appropriate line in hop-gui.sh to keep it active)
export HOP_OPTIONS="-Xmx2048m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# Launch Hop Gui as usual
./hop-gui.sh
```
You will see a message like `Listening for transport dt_socket at address: 5005` in your terminal.

Next, attach the debugger in your favorite IDE. Example `launch.json` in Visual Studio Code with [Debugger for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-debug) extension:
```sh
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Attach java debugger",
            "projectName": "hop-gis-plugins",
            "request": "attach",
            "hostName": "localhost",
            "port": 5005
        }
    ]
}
```

You are now able to inspect variables values at breakpoints you have set, analyse call stack and so.


Contributing
---------------------

See [dedicated page](CONTRIBUTING.md)


LICENSE
---------------------
This extension is licensed under `GNU Library or "Lesser" General Public License (LGPL)`.

Contributors :
* [Marc Lherbette](https://github.com/scali)
* [Matt Casters](https://github.com/mattcasters)
* [Jérémy Tridard](https://github.com/jtridard)
* [Charles-Henry Vagner](https://github.com/cvagner)
* [Cédric Darbon](https://twitter.com/cedricdarbon)


Our company
---------------------
[Atol Conseils et Développements](http://www.atolcd.com)
Follow us on twitter [@atolcd](https://twitter.com/atolcd)
