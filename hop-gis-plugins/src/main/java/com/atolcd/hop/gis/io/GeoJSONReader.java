package com.atolcd.hop.gis.io;

/*
 * #%L
 * Apache Hop GIS Plugin
 * %%
 * Copyright (C) 2021 Atol CD
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.atolcd.hop.gis.io.features.Feature;
import com.atolcd.hop.gis.io.features.Field;
import com.atolcd.hop.gis.io.features.Field.FieldType;
import com.atolcd.hop.gis.utils.GeometryUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.hop.core.exception.HopException;
import org.locationtech.jts.geom.Geometry;
import org.wololo.geojson.Crs;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;

public class GeoJSONReader extends AbstractFileReader {

  private String geoJsonFileName;
  private boolean geoJsonFileExist;
  private GeoJSON json;

  private FieldType inferFieldType(Object value) {
    if (value instanceof String) return FieldType.STRING;
    if (value instanceof Integer) return FieldType.LONG;
    if (value instanceof Boolean) return FieldType.BOOLEAN;
    if (value instanceof Double) return FieldType.DOUBLE;
    if (value instanceof Date) return FieldType.DATE;
    return null;
  }

  private FieldType mergeFieldTypes(FieldType existingType, FieldType newType) {
    if (existingType == null) return newType;

    if (existingType == FieldType.STRING || newType == FieldType.STRING) return FieldType.STRING;
    if (existingType == FieldType.BOOLEAN && newType == FieldType.BOOLEAN) return FieldType.BOOLEAN;
    if (existingType == FieldType.DATE && newType == FieldType.DATE) return FieldType.DATE;
    if (existingType == FieldType.LONG && newType == FieldType.LONG) return FieldType.LONG;
    // Deux flottants ou bien un flottant et un entier
    if ((existingType == FieldType.LONG || existingType == FieldType.DOUBLE)
        && (newType == FieldType.LONG || newType == FieldType.DOUBLE)) return FieldType.DOUBLE;

    return null;
  }

  public GeoJSONReader(String fileName, String geometryFieldName, String charsetName)
      throws HopException {

    super(null, geometryFieldName, charsetName);

    this.geoJsonFileExist = new File(checkFilename(fileName).getFile()).exists();

    if (!this.geoJsonFileExist) {
      throw new HopException("Missing " + fileName + " file");
    } else {
      this.geoJsonFileName = checkFilename(fileName).getFile();
    }

    this.fields.add(new Field(geometryFieldName, FieldType.GEOMETRY, null, null));
    File file = new File(this.geoJsonFileName);
    this.json = GeoJSONFactory.create(file);

    if (this.json instanceof FeatureCollection) {

      Map<String, FieldType> fieldTypes = new HashMap<>();

      for (org.wololo.geojson.Feature feature : ((FeatureCollection) json).getFeatures()) {
        for (Map.Entry<String, Object> entry : feature.getProperties().entrySet()) {
          String fieldName = entry.getKey();
          Object value = entry.getValue();
          FieldType newType = inferFieldType(value);

          fieldTypes.put(fieldName, mergeFieldTypes(fieldTypes.get(fieldName), newType));
        }
      }

      // Création des champs avec les types consolidés
      for (Map.Entry<String, FieldType> entry : fieldTypes.entrySet()) {
        // FieldType.STRING par défaut
        this.fields.add(
            new Field(
                entry.getKey(),
                entry.getValue() == null ? FieldType.STRING : entry.getValue(),
                null,
                null));
      }

    } else {

      throw new HopException("Error initialize reader : only FeatureCollection is supported");
    }
  }

  public List<Feature> getFeatures() {

    List<Feature> features = new ArrayList<Feature>();
    org.wololo.jts2geojson.GeoJSONReader geoJSONReader = new org.wololo.jts2geojson.GeoJSONReader();
    FeatureCollection featureCollection = (FeatureCollection) json;

    Crs crs = featureCollection.getCrs();
    int srid = 0;

    if (crs != null) {

      if (crs.getType().equalsIgnoreCase("name") && crs.getProperties().containsKey("name")) {

        try {

          String csrName = (String) crs.getProperties().get("name");
          int sridIndex = csrName.lastIndexOf(':');
          srid = Integer.valueOf(csrName.substring(sridIndex + 1, csrName.length()));

        } catch (Exception e) {
          srid = 0;
        }
      }
    }

    // Traitement des features
    org.wololo.geojson.Feature geoJsonfeatures[] = featureCollection.getFeatures();
    if (this.limit == 0 || this.limit > geoJsonfeatures.length || this.limit < 0) {
      this.limit = geoJsonfeatures.length;
    }

    for (int i = 0; i < this.limit; i++) {

      org.wololo.geojson.Feature geoJsonfeature = geoJsonfeatures[i];
      Feature feature = new Feature();
      for (Field field : fields) {

        if (field.getType().equals(FieldType.GEOMETRY)) {

          Geometry geometry = geoJSONReader.read(geoJsonfeature.getGeometry());

          if (geometry != null) {
            if (this.forceTo2DGeometry) {
              geometry = GeometryUtils.get2DGeometry(geometry);
            }

            if (this.forceToMultiGeometry) {
              geometry = GeometryUtils.getMultiGeometry(geometry);
            }

            geometry.setSRID(srid);
          }

          feature.addValue(field, geometry);

        } else {
          feature.addValue(field, geoJsonfeature.getProperties().get(field.getName()));
        }
      }

      features.add(feature);
    }

    return features;
  }
}
