Apache Hop GIS Plugins - Examples
================================


Running the examples
-------------------
Before running the examples, you must:
* install Apache Hop Installation
* set the variable `HOP_HOME` to the installation folder
* deploy plugins: see "Installing/upgrading the module" of main [README.md](../README.md)


```sh
cd examples

PIPELINE_TO_RUN="shp2geojson.hpl"
${HOP_HOME}/hop-run.sh \
  --file=pipelines-and-workflows/${PIPELINE_TO_RUN} \
  --project=hop-gis-plugins-examples \
  --runconfig=local \
  --level=Basic
```

A new file has been created : `output/velo_tour_2013.geojson`


Our company
---------------------
[Atol Conseils et Développements](http://www.atolcd.com)
Follow us on twitter [@atolcd](https://twitter.com/atolcd)
