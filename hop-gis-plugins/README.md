Apache Hop GIS Plugins
================================

This project allows you to manage GIS data in Apache Hop.

Works with Apache Hop 1.0-SNAPSHOT.


Building the plugins
-------------------
Check out the project if you have not already done so :

        git clone git://github.com/atolcd/hop-gis-plugins.git
        cd hop-gis-plugins

Install Java 8+, Maven and PDI.

To package the plugins, run the following commands from the base project directory :

        # Install dependencies if needed
        mvn install -pl '!hop-gis-plugins'
        # Create the package
        mvn clean package


The built package is assemblies/assemblies-gis-plugins/target/assemblies-gis-plugins-1.0-SNAPSHOT.zip (version can differ)



Installing/upgrading the module
---------------------

***Method 1 : Hop's Marketplace installation***

--TO DO


***Method 2 : Manual installation***

Extract the content of assemblies-gis-plugins-1.0-SNAPSHOT.zip in ${HOP_HOME}/plugins/transforms.
Example of extraction from the root directory of the project :

        wget https://github.com/atolcd/hop-gis-plugins/releases/download/v1.0-SNAPSHOT/assemblies-gis-plugins-1.0-SNAPSHOT.zip
        unzip assemblies-gis-plugins-1.0-SNAPSHOT.zip -d ${HOP_HOME}/plugins/transforms

To upgrade the plugin, delete files you added before and start a fresh installation.


***Oracle JDBC usage***

If you plan to connect to an Oracle database, add needed jars in lib folder of PDI :

 - ${HOP_HOME}/lib/ojdbc6.jar
 - ${HOP_HOME}/lib/orai18n.jar

You can get them [here](http://www.oracle.com/technetwork/apps-tech/jdbc-112010-090769.html)


Using the plugins
---------------------
You will find new elements in "Geospatial"'s directory :

 - Geospatial Group by
 - GIS File output
 - GIS File input
 - Geoprocessing
 - Geometry information
 - Coordinate system operation
 - Spatial relationship and proximity


Contributing
---------------------
***Reporting bugs***

1. First check if the version you used is the last one
2. Next check if the issue has not ever been described in the [issues tracker](https://github.com/atolcd/hop-gis-plugins/issues)
3. You can [create the issue](https://github.com/atolcd/hop-gis-plugins/issues/new)

***Submitting a Pull Request***

1. Fork the repository on GitHub
2. Clone your repository (`git clone https://github.com/XXX/hop-gis-plugins.git && cd hop-gis-plugins`)
3. Create a local branch that will support your dev (`git checkout -b a-new-dev`)
4. Commit changes to your local branch branch (`git commit -am "Add a new dev"`)
5. Push the branch to the central repository (`git push origin a-new-dev`)
6. Open a [Pull Request](https://github.com/atolcd/hop-gis-plugins/pulls)
7. Wait for the PR to be supported


LICENSE
---------------------
This extension is licensed under `GNU Library or "Lesser" General Public License (LGPL)`.

Developed by :
* [Jérémy Tridard](https://fr.linkedin.com/in/jeremwy) - Adaptation for HOP
* [Cédric Darbon](https://twitter.com/cedricdarbon) - Original author
* [Charles-Henry Vagner](https://github.com/cvagner) - Help (review)


Our company
---------------------
[Atol Conseils et Développements](http://www.atolcd.com)
Follow us on twitter [@atolcd](https://twitter.com/atolcd)
