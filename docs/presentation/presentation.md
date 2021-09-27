---
marp: true
title: Hop GIS Plugins
description: Hop GIS Plugins presentation
theme: default
paginate: true
_paginate: false
footer: '![width:30px](https://avatars.githubusercontent.com/u/1773101?s=30&v=4) [AtolCD - Hop GIS Plugins](https://github.com/atolcd/hop-gis-plugins)'



---
<style>
    :root {
    background-image: none;
    background-color: #fff;
    --color-background-paginate: #f6f8fa;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
    font-size: 40px;
  }
  section::after {
  content: '' attr(data-marpit-pagination) ' / ' attr(data-marpit-pagination-total);
}
  h2 {
     font-size: 1.6em;
  }
  p, ul {
    font-size: 30px;
    text-align: left;
  }
</style>

<!-- backgroundImage: "linear-gradient(to top right, #4350cf, #ca3f1d)" -->
![](#d1e9f9)

# <!--fit--> :globe_with_meridians: Hop GIS plugins :notes:

https://github.com/atolcd/hop-gis-plugins

<style scoped>a { color: #eee; }</style>




<!-- This is presenter note. You can write down notes through HTML comment. -->



---
<!-- backgroundImage: -->
## **[Apache Hop](https://hop.apache.org/)**

### Data integration platform

Visual design & metadata
Design once, run anywhere
Lifecycle Management

![bg right 60%](https://raw.githubusercontent.com/apache/incubator-hop/master/ui/src/main/resources/ui/images/logo_hop.svg)



---
## **[Hop GIS Plugins](https://github.com/atolcd/hop-gis-plugins)**

### Brings GIS to Hop

- new `Geometry` type
- new `GIS` transforms

![bg right 60%](https://icongr.am/octicons/mark-github.svg)



---
## **Transforms provided**
- GIS File input / output
- Coordinate system
- Geometry information
- Geometries operations

![bg right 100%](https://github.com/atolcd/hop-gis-plugins/raw/master/items-in-hop-gui.png)



---
## **GIS File input**

- ESRI Shapefile
- GPS eXchange Format
- GeoJSON
- GeoPackage
- MapInfo Interchange Format
- SpatiaLite / SQLite

![bg right 60%](https://raw.githubusercontent.com/atolcd/hop-gis-plugins/master/hop-gis-plugins/src/main/resources/GisFileInput.svg)



---
## **GIS File output**

- Drawing eXchange Format
- ESRI Shapefile
- GPS eXchange Format
- GeoJSON
- GeoPackage
- Keyhole Markup Language
- Scalable Vector Graphics

![bg right 60%](https://raw.githubusercontent.com/atolcd/hop-gis-plugins/master/hop-gis-plugins/src/main/resources/GisFileOutput.svg)



---
## **Coordinate transformation**

- Assign coordinate system
- Reproject

![bg right 60%](https://raw.githubusercontent.com/atolcd/hop-gis-plugins/master/hop-gis-plugins/src/main/resources/GisCoordinateTransformation.svg)



---
## **Geometry information**

- Get information such a WKT representation

```
LINESTRING (5.039015 [...])
```

![bg right 60%](https://raw.githubusercontent.com/atolcd/hop-gis-plugins/master/hop-gis-plugins/src/main/resources/GisGeometryInfo.svg)



---
## **Geoprocessing**

One geometry:
- Buffer
- Shorter line
- Remove holes
- ...

Or two:
- Difference
- Intersection
- ...

![bg right 60%](https://raw.githubusercontent.com/atolcd/hop-gis-plugins/master/hop-gis-plugins/src/main/resources/GisGeoprocessing.svg)



---
## **Geospatial Group by**

![bg right 60%](https://raw.githubusercontent.com/atolcd/hop-gis-plugins/master/hop-gis-plugins/src/main/resources/GisGroupBy.svg)



---
## **Spatial relationship**

- Contains
- Cover
- Touches
- ...

![bg right 60%](https://raw.githubusercontent.com/atolcd/hop-gis-plugins/master/hop-gis-plugins/src/main/resources/GisRelate.svg)



---
##  **A simple pipeline**

![width:1024px](https://raw.githubusercontent.com/atolcd/hop-gis-plugins/master/examples/pipelines-and-workflows/shp2geojson.png)

- reads a `shapefile` from disk
- loads `WKT` geometry information
- writes it to a `geojson` file



---
##  **Run a provided example**

```sh
cd examples

PIPELINE_TO_RUN="shp2geojson.hpl"
${HOP_HOME}/hop-run.sh \
  --file=pipelines-and-workflows/${PIPELINE_TO_RUN} \
  --project=hop-gis-plugins-examples \
  --runconfig=local \
  --level=Basic
```


velo_tour_2013.shp&nbsp;&nbsp;&nbsp;↝&nbsp;&nbsp;&nbsp;velo_tour_2013.geojson