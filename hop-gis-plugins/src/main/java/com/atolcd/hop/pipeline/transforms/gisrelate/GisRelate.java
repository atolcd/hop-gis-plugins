package com.atolcd.hop.pipeline.transforms.gisrelate;

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

import com.atolcd.hop.core.row.value.GeometryInterface;
import com.atolcd.hop.gis.utils.GeometryUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.RowDataUtil;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class GisRelate extends BaseTransform<GisRelateMeta, GisRelateData> {

  private static final Class<?> PKG = GisRelate.class; // Needed by Translator

  private static GeometryFactory geometryFactory = new GeometryFactory();

  private String operator;

  private Integer firstGeometryFieldIndex;
  private Integer secondGeometryFieldIndex;
  private Integer distanceFieldIndex;
  private Double distanceValue;

  private String returnType;
  private Integer outputFieldIndex;

  private boolean withDistance;
  private Class<?> resultType;
  private GeometryInterface firstGeometryInterface;
  private GeometryInterface secondGeometryInterface;

  private Object getRelateResult(Object[] row) throws HopException {

    Geometry firstGeometry =
        ((GeometryInterface) firstGeometryInterface).getGeometry(row[firstGeometryFieldIndex]);
    Geometry secondGeometry =
        ((GeometryInterface) secondGeometryInterface).getGeometry(row[secondGeometryFieldIndex]);

    if (!GeometryUtils.isNullOrEmptyGeometry(firstGeometry)
        && !GeometryUtils.isNullOrEmptyGeometry(secondGeometry)) {

      Double distance = null;

      if (withDistance) {

        if (distanceFieldIndex != null) {

          distance = getInputRowMeta().getNumber(row, distanceFieldIndex);

          if (distance == null) {
            throw new HopException("Distance can not be null");
          }

        } else {
          distance = distanceValue;
        }
      }

      return process(operator, firstGeometry, secondGeometry, distance);
    }

    return null;
  }

  public GisRelate(
      TransformMeta s,
      GisRelateMeta meta,
      GisRelateData data,
      int c,
      PipelineMeta t,
      Pipeline dis) {
    super(s, meta, data, c, t, dis);
  }

  @Override
  public boolean processRow() throws HopException {

    Object result;

    Object[] r = getRow();

    if (r == null) {

      setOutputDone();
      return false;
    }

    if (first) {

      first = false;
      data.outputRowMeta = (IRowMeta) getInputRowMeta().clone();
      meta.getFields(data.outputRowMeta, getTransformName(), null, null, this, metadataProvider);

      operator = meta.getOperator();
      returnType = meta.getReturnType();

      // Récupération des indexes des colonnes contenant les géomrtries
      // d'entrée
      firstGeometryFieldIndex = getInputRowMeta().indexOfValue(meta.getFirstGeometryFieldName());
      secondGeometryFieldIndex = getInputRowMeta().indexOfValue(meta.getSecondGeometryFieldName());
      firstGeometryInterface =
          (GeometryInterface) getInputRowMeta().getValueMeta(firstGeometryFieldIndex);
      secondGeometryInterface =
          (GeometryInterface) getInputRowMeta().getValueMeta(secondGeometryFieldIndex);
      // Besoin de distance
      if (ArrayUtils.contains(meta.getWithDistanceOperators(), operator)) {

        withDistance = true;
        if (meta.isDynamicDistance()) {
          distanceFieldIndex = getInputRowMeta().indexOfValue(meta.getDistanceFieldName());
        } else {

          try {
            distanceValue = Double.parseDouble(resolve(meta.getDistanceValue()));
          } catch (Exception e) {
            throw new HopException("Distance is not valid");
          }
        }

      } else {
        withDistance = false;
      }

      // En fonction du type de résultat
      if (ArrayUtils.contains(meta.getBoolResultOperators(), operator)) {
        resultType = Boolean.class;
      } else if (ArrayUtils.contains(meta.getNumericResultOperators(), operator)) {
        resultType = Double.class;
      }

      // Récupération de l'index de la colonne contenant le résultat
      outputFieldIndex = data.outputRowMeta.indexOfValue(meta.getOutputFieldName());

      logBasic("Initialized successfully");
    }

    Object[] outputRow = null;
    result = getRelateResult(r);

    if (resultType.equals(Boolean.class)) {

      if (returnType.equalsIgnoreCase("ALL")) {

        outputRow = RowDataUtil.resizeArray(r, r.length + 1);
        outputRow[outputFieldIndex] = (Boolean) result;
        putRow(data.outputRowMeta, outputRow);

      } else {

        if (String.valueOf((Boolean) result).equalsIgnoreCase(returnType)) {
          putRow(data.outputRowMeta, r);
        }
      }

    } else if (resultType.equals(Double.class)) {

      outputRow = RowDataUtil.resizeArray(r, r.length + 1);
      outputRow[outputFieldIndex] = (Double) result;
      putRow(data.outputRowMeta, outputRow);
    }

    incrementLinesInput();

    if (checkFeedback(getLinesRead())) {
      logBasic("Linenr " + getLinesRead());
    }

    return true;
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

  private Object process(
      String operator, Geometry inGeometryA, Geometry inGeometryB, Double distance)
      throws HopException {

    Object result = false;

    if (GeometryUtils.getSrid(inGeometryA).compareTo(GeometryUtils.getSrid(inGeometryB)) == 0) {

      if (operator.equalsIgnoreCase("CONTAINS")) {
        result = inGeometryA.contains(inGeometryB);

      } else if (operator.equalsIgnoreCase("COVERED_BY")) {
        result = inGeometryA.coveredBy(inGeometryB);

      } else if (operator.equalsIgnoreCase("COVERS")) {
        result = inGeometryA.covers(inGeometryB);

      } else if (operator.equalsIgnoreCase("CROSSES")) {
        result = inGeometryA.crosses(inGeometryB);

      } else if (operator.equalsIgnoreCase("DISJOINT")) {
        result = inGeometryA.disjoint(inGeometryB);

      } else if (operator.equalsIgnoreCase("EQUALS")) {
        result = inGeometryA.equals(inGeometryB);

      } else if (operator.equalsIgnoreCase("EQUALS_EXACT")) {
        result = inGeometryA.equalsExact(inGeometryB);

      } else if (operator.equalsIgnoreCase("INTERSECTS")) {
        result = inGeometryA.intersects(inGeometryB);

      } else if (operator.equalsIgnoreCase("WITHIN")) {
        result = inGeometryA.within(inGeometryB);

      } else if (operator.equalsIgnoreCase("IS_WITHIN_DISTANCE")) {

        distance = Math.abs(distance);
        result = inGeometryA.isWithinDistance(inGeometryB, distance);

      } else if (operator.equalsIgnoreCase("IS_NOT_WITHIN_DISTANCE")) {

        distance = Math.abs(distance);
        result = !inGeometryA.isWithinDistance(inGeometryB, distance);

      } else if (operator.equalsIgnoreCase("OVERLAPS")) {
        result = inGeometryA.overlaps(inGeometryB);

      } else if (operator.equalsIgnoreCase("TOUCHES")) {
        result = inGeometryA.touches(inGeometryB);

      } else if (operator.equalsIgnoreCase("DISTANCE_MIN")) {
        result = inGeometryA.distance(inGeometryB);

      } else if (operator.equalsIgnoreCase("DISTANCE_MAX")) {

        Double maxDistance = inGeometryA.distance(inGeometryB);
        Geometry geometry =
            geometryFactory.createGeometryCollection(new Geometry[] {inGeometryA, inGeometryB});
        Coordinate[] coords = geometry.convexHull().getCoordinates();

        for (Coordinate aCoordinate : coords) {

          for (Coordinate bCoordinate : coords) {

            double currenDistance =
                geometryFactory
                    .createPoint(aCoordinate)
                    .distance(geometryFactory.createPoint(bCoordinate));
            if (currenDistance > maxDistance) {
              maxDistance = currenDistance;
            }
          }
        }

        result = maxDistance;

      } else {
        throw new IllegalArgumentException("Function \"" + operator + "\" is not allowed");
      }

    } else {
      throw new HopException(
          "Unauthorized mixed srids : "
              + GeometryUtils.getSrid(inGeometryA)
              + " with "
              + GeometryUtils.getSrid(inGeometryB));
    }

    return result;
  }
}
