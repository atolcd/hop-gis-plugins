Apache Hop GIS Plugins
================================

This project allows you to manage GIS data in Apache Hop.

Works with Apache Hop 0.70.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [![Blog article](https://blog.atolcd.com/wp-content/uploads/sites/2/2015/06/pdi_gis_00.png)](https://blog.atolcd.com/une-extension-gis-dans-pentaho-data-integration-5/)


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


The built package is assemblies/assemblies-gis-plugins/target/assemblies-gis-plugins-0.70.zip (version can differ)



Installing/upgrading the module
---------------------

***Method 1 : Hop's Marketplace installation***

--TO DO


***Method 2 : Manual installation***

Extract the content of pentaho-gis-plugins-VERSION-bin.zip in ${PENTAHO_HOME}/plugins.
Example of extraction from the root directory of the project :

        wget https://github.com/atolcd/hop-gis-plugins/releases/download/v?.?.?/pentaho-gis-plugins-?.?.?-bin-5.zip
        unzip assemblies-gis-plugins-0.70.zip -d ${PROJECT_HOME}/plugins/transforms

To upgrade the plugin, delete files you added before and start a fresh installation.


***Oracle JDBC usage***

If you plan to connect to an Oracle database, add needed jars in lib folder of PDI :

 - PENTAHO_HOME/lib/ojdbc6.jar
 - PENTAHO_HOME/lib/orai18n.jar

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

Some information is available [here](https://blog.atolcd.com/une-extension-gis-dans-pentaho-data-integration-5/) in french.

Provided steps presentation :

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [![Provided steps presentation](https://lh3.googleusercontent.com/proxy/xG_Nit5UEhPvdHnrMbYiLLJhbX0Di6qeDMDgBiDQt6mCblRvfbDi8UGQyvmzTi33Xdt0-oAPIa2hVxPUYVpf=w506-h285-n)](https://www.youtube.com/watch?v=gotnjNSVcaE)

Usage example :

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; [![Usage example](https://lh3.googleusercontent.com/proxy/RwdveW5Zd1gPHjK0-imga_xMHp2Vgn7Roww1i1S7qlz0BA-do8CT8FLcIMg13kZ9vvurLmSZcRsH4OpXWaIq=w506-h285-n)](https://www.youtube.com/watch?v=IO0Chh0XjgY)


Contributing
---------------------
***Reporting bugs***

1. First check if the version you used is the last one
2. Next check if the issue has not ever been described in the [issues tracker](https://github.com/atolcd/pentaho-gis-plugins/issues)
3. You can [create the issue](https://github.com/atolcd/hop-gis-plugins/issues/new)

***Submitting a Pull Request***

1. Fork the repository on GitHub
2. Clone your repository (`git clone https://github.com/XXX/pentaho-gis-plugins.git && cd pentaho-gis-plugins`)
3. Create a local branch that will support your dev (`git checkout -b a-new-dev`)
4. Commit changes to your local branch branch (`git commit -am "Add a new dev"`)
5. Push the branch to the central repository (`git push origin a-new-dev`)
6. Open a [Pull Request](https://github.com/atolcd/hop-gis-plugins/pulls)
7. Wait for the PR to be supported


LICENSE
---------------------
This extension is licensed under `GNU Library or "Lesser" General Public License (LGPL)`.

Developed by [Cédric Darbon](https://twitter.com/cedricdarbon) and packaged by [Charles-Henry Vagner](https://github.com/cvagner)


Our company
---------------------
[Atol Conseils et Développements](http://www.atolcd.com)
Follow us on twitter [@atolcd](https://twitter.com/atolcd)
