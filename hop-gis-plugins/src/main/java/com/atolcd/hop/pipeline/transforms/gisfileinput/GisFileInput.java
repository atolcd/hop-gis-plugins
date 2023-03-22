package com.atolcd.hop.pipeline.transforms.gisfileinput;

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

import com.atolcd.hop.gis.io.AbstractFileReader;
import com.atolcd.hop.gis.io.DXFReader;
import com.atolcd.hop.gis.io.GPXReader;
import com.atolcd.hop.gis.io.GeoJSONReader;
import com.atolcd.hop.gis.io.GeoPackageReader;
import com.atolcd.hop.gis.io.MapInfoReader;
import com.atolcd.hop.gis.io.ShapefileReader;
import com.atolcd.hop.gis.io.SpatialiteReader;
import com.atolcd.hop.gis.io.features.Feature;
import com.atolcd.hop.gis.io.features.FeatureConverter;
import java.util.Iterator;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.TransformMeta;

public class GisFileInput extends BaseTransform<GisFileInputMeta, GisFileInputData> {
  private static Class<?> PKG = GisFileInput.class;

  private AbstractFileReader fileReader;

  public GisFileInput(
      TransformMeta s,
      GisFileInputMeta meta,
      GisFileInputData data,
      int c,
      PipelineMeta t,
      Pipeline dis) {
    super(s, meta, data, c, t, dis);
  }

  @Override
  public boolean processRow() throws HopException {

    if (first) {

      first = false;
      data.outputRowMeta = new RowMeta();
      meta.getFields(data.outputRowMeta, getTransformName(), null, null, this, metadataProvider);

      if (meta.getInputFormat().equalsIgnoreCase("ESRI_SHP")) {
        fileReader =
            new ShapefileReader(
                resolve(meta.getInputFileName()),
                resolve(meta.getGeometryFieldName()),
                meta.getEncoding());
      } else if (meta.getInputFormat().equalsIgnoreCase("GEOJSON")) {
        fileReader =
            new GeoJSONReader(
                resolve(meta.getInputFileName()),
                resolve(meta.getGeometryFieldName()),
                meta.getEncoding());
      } else if (meta.getInputFormat().equalsIgnoreCase("MAPINFO_MIF")) {
        fileReader =
            new MapInfoReader(
                resolve(meta.getInputFileName()),
                resolve(meta.getGeometryFieldName()),
                meta.getEncoding());
      } else if (meta.getInputFormat().equalsIgnoreCase("SPATIALITE")) {

        String tableName = resolve((String) meta.getInputParameterValue("DB_TABLE_NAME"));
        fileReader =
            new SpatialiteReader(resolve(meta.getInputFileName()), tableName, meta.getEncoding());

      } else if (meta.getInputFormat().equalsIgnoreCase("DXF")) {
        String readXData = resolve((String) meta.getInputParameterValue("READ_XDATA"));
        String circleAsPolygon = resolve((String) meta.getInputParameterValue("CIRCLE_AS_POLYGON"));
        String ellipseAsPolygon =
            resolve((String) meta.getInputParameterValue("ELLIPSE_AS_POLYGON"));
        String lineAsPolygon = resolve((String) meta.getInputParameterValue("LINE_AS_POLYGON"));

        fileReader =
            new DXFReader(
                resolve(meta.getInputFileName()),
                resolve(meta.getGeometryFieldName()),
                meta.getEncoding(),
                Boolean.parseBoolean(circleAsPolygon),
                Boolean.parseBoolean(ellipseAsPolygon),
                Boolean.parseBoolean(lineAsPolygon),
                Boolean.parseBoolean(readXData));
      } else if (meta.getInputFormat().equalsIgnoreCase("GPX")) {
        fileReader =
            new GPXReader(
                resolve(meta.getInputFileName()),
                resolve(meta.getGeometryFieldName()),
                meta.getEncoding());

      } else if (meta.getInputFormat().equalsIgnoreCase("GEOPACKAGE")) {

        fileReader =
            new GeoPackageReader(
                resolve(meta.getInputFileName()),
                resolve((String) meta.getInputParameterValue("DB_TABLE_NAME")),
                resolve(meta.getGeometryFieldName()),
                meta.getEncoding());
      }

      String forceToMultigeometry =
          resolve((String) meta.getInputParameterValue("FORCE_TO_MULTIGEOMETRY"));
      if (forceToMultigeometry != null) {
        fileReader.setForceToMultiGeometry(Boolean.parseBoolean(forceToMultigeometry));
      }

      String forceTo2D = resolve((String) meta.getInputParameterValue("FORCE_TO_2D"));
      if (forceTo2D != null) {
        fileReader.setForceTo2DGeometry(Boolean.parseBoolean(forceTo2D));
      }

      fileReader.setLimit(meta.getRowLimit());
      incrementLinesInput();
      logBasic("Initialized successfully");
    }

    Iterator<Feature> featureIt = fileReader.getFeatures().iterator();
    while (featureIt.hasNext()) {

      putRow(data.outputRowMeta, FeatureConverter.getRow(data.outputRowMeta, featureIt.next()));
      incrementLinesOutput();
    }

    setOutputDone();
    return false;
  }

  @Override
  public boolean init() {
    return super.init();
  }

  @Override
  public void dispose() {
    super.dispose();
  }

  public void run() {
    logBasic("Starting to run...");
    try {
      while (processRow() && !isStopped())
        ;
    } catch (Exception e) {
      logError("Unexpected error : " + e.toString());
      logError(Const.getStackTracker(e));
      setErrors(1);
      stopAll();
    } finally {
      dispose();
      logBasic("Finished, processing " + getLinesRead() + " rows");
      markStop();
    }
  }
}
