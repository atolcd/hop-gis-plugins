package com.atolcd.hop.pipeline.transforms.gisgeoprocessing;

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

import com.atolcd.hop.core.row.value.ValueMetaGeometry;
import com.atolcd.hop.gis.utils.GeometryUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import org.apache.commons.lang.ArrayUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.RowDataUtil;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.locationtech.jts.algorithm.MinimumBoundingCircle;
import org.locationtech.jts.densify.Densifier;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.Puntal;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.overlay.snap.GeometrySnapper;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.locationtech.jts.simplify.VWSimplifier;
import org.opensphere.geometry.algorithm.ConcaveHull;

public class GisGeoprocessing extends BaseTransform<GisGeoprocessingMeta, GisGeoprocessingData> {

  private static GeometryFactory geometryFactory = new GeometryFactory();

  private String operator;

  private Integer firstGeometryFieldIndex;
  private Integer secondGeometryFieldIndex;

  private Double distanceValue;
  private Integer distanceFieldIndex;

  private Integer bufferSegmentsCount;
  private String bufferCapStyle;
  private String bufferJoinStyle;
  private Boolean bufferSingleSide;

  private String returnType;
  private String extractType;
  private Integer outputFieldIndex;

  private boolean withSecondGeometry;
  private boolean withDistance;
  private boolean withExtractType;
  private boolean withExplode;

  private Geometry getGeoprocessingResult(Object[] row) throws HopException {

    Geometry firstGeometry = new ValueMetaGeometry().getGeometry(row[firstGeometryFieldIndex]);

    Double distance = null;
    if (withDistance) {

      if (distanceFieldIndex != null) {

        distance = getInputRowMeta().getNumber(row, distanceFieldIndex);

        if (distance == null) {
          throw new HopException("Distance, area, threshold or decimal count can not be null");
        }

      } else {
        distance = distanceValue;
      }
    }

    if (withSecondGeometry) {

      Geometry secondGeometry = new ValueMetaGeometry().getGeometry(row[secondGeometryFieldIndex]);
      return getTwoGeometriesGeoprocessing(operator, firstGeometry, secondGeometry, distance);

    } else {

      if (operator.equalsIgnoreCase("EXTENDED_BUFFER")) {

        Geometry outGeometry = null;

        if (!GeometryUtils.isNullOrEmptyGeometry(firstGeometry)) {

          if (distance != null) {

            if (bufferSegmentsCount == null) {
              bufferSegmentsCount = 8;
            }

            BufferParameters bufferParams = new BufferParameters();
            bufferParams.setQuadrantSegments(bufferSegmentsCount);
            bufferParams.setSingleSided(bufferSingleSide);

            if (bufferCapStyle.endsWith("FLAT")) {
              bufferParams.setEndCapStyle(BufferParameters.CAP_FLAT);

            } else if (bufferCapStyle.endsWith("ROUND")) {
              bufferParams.setEndCapStyle(BufferParameters.CAP_ROUND);

            } else if (bufferCapStyle.endsWith("SQUARE")) {
              bufferParams.setEndCapStyle(BufferParameters.CAP_SQUARE);
            }

            if (bufferJoinStyle.endsWith("BEVEL")) {
              bufferParams.setJoinStyle(BufferParameters.JOIN_BEVEL);

            } else if (bufferJoinStyle.endsWith("MITRE")) {
              bufferParams.setJoinStyle(BufferParameters.JOIN_MITRE);

            } else if (bufferJoinStyle.endsWith("ROUND")) {
              bufferParams.setJoinStyle(BufferParameters.JOIN_ROUND);
            }

            if (!bufferParams.isSingleSided()) {
              distance = Math.abs(distance);
            }

            outGeometry =
                GeometryUtils.getNonEmptyGeometry(
                    firstGeometry.getSRID(),
                    BufferOp.bufferOp(firstGeometry, distance, bufferParams));

          } else {

            throw new HopException("Distance can not be null");
          }
        }

        return outGeometry;

      } else {
        return getOneGeometryGeoprocessing(operator, firstGeometry, distance);
      }
    }
  }

  public GisGeoprocessing(
      TransformMeta s,
      GisGeoprocessingMeta meta,
      GisGeoprocessingData data,
      int c,
      PipelineMeta t,
      Pipeline dis) {
    super(s, meta, data, c, t, dis);
  }

  @Override
  public boolean processRow() throws HopException {
    Geometry geoprocessingResult;

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

      // Besoin de 2 géométries
      if (ArrayUtils.contains(meta.getTwoGeometriesOperators(), operator)) {
        withSecondGeometry = true;
        secondGeometryFieldIndex =
            getInputRowMeta().indexOfValue(meta.getSecondGeometryFieldName());
      } else {
        withSecondGeometry = false;
      }

      // Posibilités d'extraction de géométries
      if (ArrayUtils.contains(meta.getWithExtractTypeOperators(), operator)) {

        withExtractType = true;
        extractType = meta.getExtractType();

      } else {
        withExtractType = false;
      }

      // Besoin de distance
      if (ArrayUtils.contains(meta.getWithDistanceOperators(), operator)) {

        withDistance = true;
        if (meta.isDynamicDistance()) {
          distanceFieldIndex = getInputRowMeta().indexOfValue(meta.getDistanceFieldName());
        } else {

          try {
            distanceValue = Double.parseDouble(resolve(meta.getDistanceValue()));
          } catch (Exception e) {
            throw new HopException("Distance, area, threshold or decimal count is not valid");
          }
        }

      } else {
        withDistance = false;
      }

      // Récupération de l'index de la colonne contenant le résultat
      outputFieldIndex = data.outputRowMeta.indexOfValue(meta.getOutputFieldName());

      // Exploser les géométries;
      if (operator.equalsIgnoreCase("EXPLODE")) {
        withExplode = true;
      } else {
        withExplode = false;
      }

      // Si buffer étendu
      if (operator.equalsIgnoreCase("EXTENDED_BUFFER")) {
        bufferSegmentsCount = meta.getBufferSegmentsCount();
        bufferCapStyle = meta.getBufferCapStyle();
        bufferJoinStyle = meta.getBufferJoinStyle();
        bufferSingleSide = meta.getBufferSingleSide();

      } else {
        bufferSegmentsCount = null;
        bufferCapStyle = null;
        bufferJoinStyle = null;
        bufferSingleSide = null;
      }

      logBasic("Initialized successfully");
    }

    Object[] currenRow = RowDataUtil.resizeArray(r, r.length + 1);
    geoprocessingResult = getGeoprocessingResult(r);
    Geometry[] resultGeometries = null;

    if (withExplode && geoprocessingResult != null) {

      int numGeometries = geoprocessingResult.getNumGeometries();
      resultGeometries = new Geometry[numGeometries];
      for (int i = 0; i < numGeometries; i++) {
        Geometry subGeometry =
            GeometryUtils.getNonEmptyGeometry(
                geoprocessingResult.getSRID(), geoprocessingResult.getGeometryN(i));
        if (subGeometry instanceof LinearRing) {
          int srid = subGeometry.getSRID();
          subGeometry = geometryFactory.createLineString(subGeometry.getCoordinates());
          subGeometry.setSRID(srid);
        }
        resultGeometries[i] = subGeometry;
      }

    } else {
      resultGeometries = new Geometry[] {geoprocessingResult};
    }

    for (Geometry resultGeometry : resultGeometries) {

      Object[] outputRow = currenRow.clone();

      if (withExtractType && !GeometryUtils.isNullOrEmptyGeometry(resultGeometry)) {

        if (extractType.equalsIgnoreCase("PUNTAL_ONLY")) {

          resultGeometry = GeometryUtils.getGeometryFromType(resultGeometry, Puntal.class);

        } else if (extractType.equalsIgnoreCase("LINEAL_ONLY")) {

          resultGeometry = GeometryUtils.getGeometryFromType(resultGeometry, Lineal.class);

        } else if (extractType.equalsIgnoreCase("POLYGONAL_ONLY")) {

          resultGeometry = GeometryUtils.getGeometryFromType(resultGeometry, Polygonal.class);
        }
      }

      if (returnType.equalsIgnoreCase("ALL")) {

        outputRow[outputFieldIndex] = resultGeometry;
        putRow(data.outputRowMeta, outputRow);

      } else if (returnType.equalsIgnoreCase("NOT_NULL")) {

        if (!GeometryUtils.isNullOrEmptyGeometry(resultGeometry)) {

          outputRow[outputFieldIndex] = resultGeometry;
          putRow(data.outputRowMeta, outputRow);
        }
      }
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

  private Geometry getOneGeometryGeoprocessing(
      String operator, Geometry inGeometry, Double distance) throws HopException {

    Geometry outGeometry = null;

    if (!GeometryUtils.isNullOrEmptyGeometry(inGeometry)) {

      if (operator.equalsIgnoreCase("BOUNDARY")) {

        outGeometry = inGeometry.getBoundary();
        if (outGeometry instanceof LinearRing) {
          outGeometry = geometryFactory.createLineString(outGeometry.getCoordinates());
        }

      } else if (operator.equalsIgnoreCase("INTERIOR_POINT")) {

        outGeometry = inGeometry.getInteriorPoint();

      } else if (operator.equalsIgnoreCase("CONVEX_HULL")) {

        outGeometry = inGeometry.convexHull();

      } else if (operator.equalsIgnoreCase("CONCAVE_HULL")) {

        if (distance != null) {

          distance = Math.abs(distance);
          ConcaveHull concaveHull = new ConcaveHull(inGeometry, distance);
          outGeometry = concaveHull.getConcaveHull();

        } else {
          throw new HopException("Threshold can not be null");
        }

      } else if (operator.equalsIgnoreCase("BUFFER")) {

        if (distance != null) {

          distance = Math.abs(distance);
          outGeometry = inGeometry.buffer(distance);

        } else {
          throw new HopException("Distance can not be null");
        }

      } else if (operator.equalsIgnoreCase("EXTENTED_BUFFER")) {

        // Non géré ici

      } else if (operator.equalsIgnoreCase("EXPLODE")) {

        // Non géré ici mais avec paramètre withExplode"
        outGeometry = inGeometry;

      } else if (operator.equalsIgnoreCase("REVERSE")) {

        outGeometry = inGeometry.reverse();

      } else if (operator.equalsIgnoreCase("DENSIFY")) {

        if (distance != null) {

          distance = Math.abs(distance);
          Densifier densifier = new Densifier(inGeometry);
          densifier.setDistanceTolerance(distance);
          outGeometry = densifier.getResultGeometry();

        } else {
          throw new HopException("Distance can not be null");
        }

      } else if (operator.equalsIgnoreCase("SIMPLIFY")) {

        if (distance != null) {

          distance = Math.abs(distance);
          DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(inGeometry);
          simplifier.setDistanceTolerance(distance);
          outGeometry = simplifier.getResultGeometry();

        } else {
          throw new HopException("Distance can not be null");
        }

      } else if (operator.equalsIgnoreCase("SIMPLIFY_VW")) {

        if (distance != null) {

          distance = Math.abs(distance);
          VWSimplifier simplifier = new VWSimplifier(inGeometry);
          simplifier.setDistanceTolerance(distance);
          outGeometry = simplifier.getResultGeometry();

        } else {
          throw new HopException("Distance can not be null");
        }

      } else if (operator.equalsIgnoreCase("LESS_PRECISION")) {

        if (distance != null) {
          outGeometry = GeometryUtils.getLessPrecisionGeometry(inGeometry, distance.intValue());

        } else {
          throw new HopException("Decimal count can not be null");
        }
      } else if (operator.equalsIgnoreCase("POLYGONIZE")) {

        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(inGeometry);
        outGeometry = geometryFactory.buildGeometry(polygonizer.getPolygons());

      } else if (operator.equalsIgnoreCase("LINEMERGE")) {

        outGeometry = GeometryUtils.getMergedGeometry(inGeometry);

      } else if (operator.equalsIgnoreCase("REMOVE_HOLES")) {

        if (distance != null) {

          if (inGeometry instanceof Polygon) {

            Polygon polygon = (Polygon) inGeometry;
            List<LinearRing> rings = new ArrayList<LinearRing>();
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
              LinearRing currentRing = (LinearRing) polygon.getInteriorRingN(i);

              if (geometryFactory.createPolygon(currentRing).getArea() > distance) {
                rings.add(currentRing);
              }
            }

            outGeometry =
                geometryFactory.createPolygon(
                    (LinearRing) polygon.getExteriorRing(),
                    rings.toArray(new LinearRing[rings.size()]));

          } else if (inGeometry instanceof MultiPolygon) {

            Polygon[] polygons = new Polygon[inGeometry.getNumGeometries()];
            for (int i = 0; i < polygons.length; i++) {
              polygons[i] =
                  (Polygon)
                      getOneGeometryGeoprocessing(
                          "REMOVE_HOLES", inGeometry.getGeometryN(i), distance);
            }
            outGeometry = geometryFactory.createMultiPolygon(polygons);
          }

        } else {
          throw new HopException("Area can not be null");
        }

      } else if (operator.equalsIgnoreCase("LARGEST_POLYGON")) {

        if (inGeometry instanceof Polygon) {
          outGeometry = (Geometry) inGeometry.clone();

        } else if (inGeometry instanceof MultiPolygon) {
          Polygon largestPolygon = (Polygon) inGeometry.getGeometryN(0);
          for (int i = 0; i < inGeometry.getNumGeometries(); i++) {

            Polygon currentPolygon = (Polygon) inGeometry.getGeometryN(i);
            if (currentPolygon.getArea() > largestPolygon.getArea()) {
              largestPolygon = currentPolygon;
            }
          }
          outGeometry = largestPolygon;
        }

      } else if (operator.equalsIgnoreCase("SMALLEST_POLYGON")) {

        if (inGeometry instanceof Polygon) {
          outGeometry = (Geometry) inGeometry.clone();

        } else if (inGeometry instanceof MultiPolygon) {
          Polygon smallestPolygon = (Polygon) inGeometry.getGeometryN(0);
          for (int i = 0; i < inGeometry.getNumGeometries(); i++) {

            Polygon currentPolygon = (Polygon) inGeometry.getGeometryN(i);
            if (currentPolygon.getArea() < smallestPolygon.getArea()) {
              smallestPolygon = currentPolygon;
            }
          }
          outGeometry = smallestPolygon;
        }

      } else if (operator.equalsIgnoreCase("LONGEST_LINESTRING")) {

        if (inGeometry instanceof LineString) {
          outGeometry = (Geometry) inGeometry.clone();

        } else if (inGeometry instanceof MultiLineString) {
          LineString longestLineString = (LineString) inGeometry.getGeometryN(0);
          for (int i = 0; i < inGeometry.getNumGeometries(); i++) {

            LineString currentLineString = (LineString) inGeometry.getGeometryN(i);
            if (currentLineString.getLength() > longestLineString.getLength()) {
              longestLineString = currentLineString;
            }
          }
          outGeometry = longestLineString;
        }
      } else if (operator.equalsIgnoreCase("SHORTEST_LINESTRING")) {

        if (inGeometry instanceof LineString) {
          outGeometry = (Geometry) inGeometry.clone();

        } else if (inGeometry instanceof MultiLineString) {
          LineString shortestLineString = (LineString) inGeometry.getGeometryN(0);
          for (int i = 0; i < inGeometry.getNumGeometries(); i++) {

            LineString currentLineString = (LineString) inGeometry.getGeometryN(i);
            if (currentLineString.getLength() < shortestLineString.getLength()) {
              shortestLineString = currentLineString;
            }
          }
          outGeometry = shortestLineString;
        }

      } else if (operator.equalsIgnoreCase("LINEAR_REFERENCING")) {

        if (inGeometry instanceof LineString) {

          if (distance != null) {

            outGeometry =
                geometryFactory.createPoint(
                    new LengthIndexedLine((LineString) inGeometry).extractPoint(distance));

          } else {

            throw new HopException("Distance can not be null");
          }
        }

      } else if (operator.equalsIgnoreCase("TO_2D_GEOMETRY")) {

        outGeometry = GeometryUtils.get2DGeometry(inGeometry);

      } else if (operator.equalsIgnoreCase("TO_MULTI_GEOMETRY")) {

        outGeometry = GeometryUtils.getMultiGeometry(inGeometry);

      } else if (operator.equalsIgnoreCase("EXTRACT_COORDINATES")) {

        outGeometry = geometryFactory.createMultiPoint(inGeometry.getCoordinates());

      } else if (operator.equalsIgnoreCase("EXTRACT_FIRST_COORDINATE")) {

        outGeometry = geometryFactory.createPoint(inGeometry.getCoordinates()[0]);

      } else if (operator.equalsIgnoreCase("EXTRACT_LAST_COORDINATE")) {

        outGeometry =
            geometryFactory.createPoint(
                inGeometry.getCoordinates()[inGeometry.getCoordinates().length - 1]);

      } else if (operator.equalsIgnoreCase("MBR")) {

        outGeometry = inGeometry.getEnvelope();

      } else if (operator.equalsIgnoreCase("MBC")) {

        outGeometry = new MinimumBoundingCircle(inGeometry).getCircle();

      } else if (operator.equalsIgnoreCase("CENTROID")) {

        outGeometry = inGeometry.getCentroid();
      }
    }

    return GeometryUtils.getNonEmptyGeometry(inGeometry.getSRID(), outGeometry);
  }

  private Geometry getTwoGeometriesGeoprocessing(
      String operator, Geometry inGeometryA, Geometry inGeometryB, Double distance)
      throws HopException {

    Geometry outGeometry = null;

    if (!GeometryUtils.isNullOrEmptyGeometry(inGeometryA)
        && !GeometryUtils.isNullOrEmptyGeometry(inGeometryB)) {

      if (GeometryUtils.getSrid(inGeometryA).compareTo(GeometryUtils.getSrid(inGeometryB)) == 0) {

        if (operator.equalsIgnoreCase("UNION")) {
          outGeometry = inGeometryA.union(inGeometryB);

        } else if (operator.equalsIgnoreCase("DIFFERENCE")) {
          outGeometry = inGeometryA.difference(inGeometryB);

        } else if (operator.equalsIgnoreCase("INTERSECTION")) {
          outGeometry = inGeometryA.intersection(inGeometryB);

        } else if (operator.equalsIgnoreCase("SYM_DIFFERENCE")) {
          outGeometry = inGeometryA.symDifference(inGeometryB);

        } else if (operator.equalsIgnoreCase("SNAP_TO_GEOMETRY")) {

          if (distance != null) {

            distance = Math.abs(distance);
            outGeometry = snapToGeometry(inGeometryA, inGeometryB, distance);

          } else {
            throw new HopException("Snap distance can not be null");
          }

        } else if (operator.equalsIgnoreCase("SIMPLIFY_POLYGON")) {

          if (!(inGeometryA instanceof Polygonal)) {
            throw new HopException("The first geometry is not a POLYGON or a MULTIPOLYGON");
          }

          if (!(inGeometryB instanceof Polygonal)) {
            throw new HopException("The second geometry is not a POLYGON or a MULTIPOLYGON");
          }

          if (distance != null) {

            distance = Math.abs(distance);
            outGeometry = simplifyPolygon(inGeometryA, inGeometryB, distance);

          } else {
            throw new HopException("Distance can not be null");
          }

        } else if (operator.equalsIgnoreCase("SPLIT")) {

          // Géométrie à découper ne doit pas être nulle
          if (!GeometryUtils.isNullOrEmptyGeometry(inGeometryA)) {

            // Geometrie de découpe ne doit pas être nulle
            if (GeometryUtils.isNullOrEmptyGeometry(inGeometryB)) {

              outGeometry = (Geometry) inGeometryA.clone();

            } else {

              // Découpe de surface par un linéaire
              if (inGeometryA instanceof Polygonal && inGeometryB instanceof Lineal) {

                Polygonizer polygonizer = new Polygonizer();
                List<Geometry> boundaries = new ArrayList<Geometry>();
                boundaries.add(inGeometryA.getBoundary());
                boundaries.add(inGeometryB);
                UnaryUnionOp uOp = new UnaryUnionOp(geometryFactory.buildGeometry(boundaries));
                polygonizer.add(GeometryUtils.getMergedGeometry(uOp.union()));

                List<Polygon> realPolygons = new ArrayList<Polygon>();
                for (Polygon polygon : GeometryFactory.toPolygonArray(polygonizer.getPolygons())) {

                  if (inGeometryA.contains(polygon.getInteriorPoint())) {
                    realPolygons.add(polygon);
                  }
                }

                outGeometry = geometryFactory.buildGeometry(realPolygons);

                // Découpe de linéaire par un linéaire
              } else if (inGeometryA instanceof Lineal
                  && inGeometryB instanceof Lineal
                  && !GeometryUtils.isNullOrEmptyGeometry(inGeometryA)
                  && !GeometryUtils.isNullOrEmptyGeometry(inGeometryB)) {

                outGeometry = inGeometryA.difference(inGeometryB);

                // Découpe de linéaire par un ponctuel
              } else if (inGeometryA instanceof Lineal && inGeometryB instanceof Puntal) {

                List<LineString> linestrings = new ArrayList<LineString>();
                if (inGeometryA instanceof LineString) {

                  linestrings.addAll(
                      Arrays.asList(
                          splitLineStringAtCoordinates(
                              inGeometryB.getCoordinates(), (LineString) inGeometryA, 10E-6)));

                } else {

                  for (int i = 0; i < inGeometryA.getNumGeometries(); i++) {
                    linestrings.addAll(
                        Arrays.asList(
                            splitLineStringAtCoordinates(
                                inGeometryB.getCoordinates(),
                                (LineString) inGeometryA.getGeometryN(i),
                                10E-6)));
                  }
                }

                outGeometry = geometryFactory.buildGeometry(linestrings);

                // Autres cas de découpe non pris en charge
              } else {
                throw new HopException(
                    "Split (MULTI)POLYGON by a (MULTI)LINESTRING, (MULTI)LINESTRING by (MULTI)LINESTRING or (MULTI)LINESTRING by (MULTI)POINT");
              }
            }
          }

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

    } else {

      // Simplification de polygones sans voisins
      if (!GeometryUtils.isNullOrEmptyGeometry(inGeometryA)
          && GeometryUtils.isNullOrEmptyGeometry(inGeometryB)) {

        if (operator.equalsIgnoreCase("SIMPLIFY_POLYGON")) {

          outGeometry = getOneGeometryGeoprocessing("SIMPLIFY", inGeometryA, distance);
        }
      }
    }

    return GeometryUtils.getNonEmptyGeometry(inGeometryA.getSRID(), outGeometry);
  }

  @SuppressWarnings("unchecked")
  private Geometry simplifyPolygon(Geometry inGeometryA, Geometry inGeometryB, double distance) {

    distance = Math.abs(distance);

    List<Geometry> allBoundaries = new ArrayList<Geometry>();
    Geometry inputBoundary = inGeometryA.getBoundary();

    allBoundaries.add(inputBoundary);
    for (int i = 0; i < inGeometryB.getNumGeometries(); i++) {

      allBoundaries.add(inGeometryB.getGeometryN(i).getBoundary());
    }

    LineMerger lineMerger = new LineMerger();
    lineMerger.add(geometryFactory.buildGeometry(allBoundaries).union());
    Polygonizer polygonizer = new Polygonizer();
    for (LineString lineString : (List<LineString>) lineMerger.getMergedLineStrings()) {
      if (lineString.coveredBy(inputBoundary)) {

        DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(lineString);
        simplifier.setDistanceTolerance(distance);
        polygonizer.add(simplifier.getResultGeometry());
      }
    }

    return geometryFactory.buildGeometry(polygonizer.getPolygons());
  }

  @SuppressWarnings("static-access")
  private Geometry snapToGeometry(Geometry inGeometryA, Geometry inGeometryB, double distance) {

    Geometry outGeometry = null;

    // Si geometrie B est de type ponctuel -> accrochage des sommets aux
    // coordonnées
    if (inGeometryB instanceof Puntal) {

      GeometrySnapper geometrySnapper = new GeometrySnapper(inGeometryA);
      // Si point bug : ajout d'autres coordonnée non accrochables
      if (inGeometryB instanceof Point) {
        CoordinateList coordinateList = new CoordinateList();
        coordinateList.add(inGeometryB.getCoordinate(), true);
        Envelope enveloppe = inGeometryA.getEnvelopeInternal();
        enveloppe.expandBy(distance * 2);
        coordinateList.add(geometryFactory.toGeometry(enveloppe).getCoordinates(), true);
        inGeometryB = geometryFactory.createMultiPoint(coordinateList.toCoordinateArray()).union();
      }

      outGeometry = geometrySnapper.snapTo(inGeometryB, distance);

      // Sinon, accrochage des sommets aux éléments linéraires
    } else {

      Geometry linearGeometry =
          geometryFactory.buildGeometry(LinearComponentExtracter.getLines(inGeometryB, true));

      // Réalise une accroche sur les coordonnées
      // GeometrySnapper geometrySnapper = new
      // GeometrySnapper(inGeometryA);
      // outGeometry =
      // GeometryUtils.getNonEmptyGeometry(inGeometryA.getSRID(),geometrySnapper.snapTo(inGeometryB,
      // distance));
      outGeometry = inGeometryA;

      // Puis sur les éléments linéaires
      if (outGeometry instanceof Point) {

        outGeometry =
            geometryFactory.createPoint(snapToLineString(outGeometry, linearGeometry, distance)[0]);

      } else if (outGeometry instanceof MultiPoint) {

        outGeometry =
            geometryFactory.createMultiPoint(
                snapToLineString(outGeometry, linearGeometry, distance));

      } else if (outGeometry instanceof LineString) {

        outGeometry =
            geometryFactory.createLineString(
                snapToLineString(outGeometry, linearGeometry, distance));

      } else if (outGeometry instanceof MultiLineString) {

        List<LineString> lineStrings = new ArrayList<LineString>();
        for (int iGeom = 0; iGeom < outGeometry.getNumGeometries(); iGeom++) {

          lineStrings.add(
              geometryFactory.createLineString(
                  snapToLineString(outGeometry.getGeometryN(iGeom), linearGeometry, distance)));
        }

        outGeometry =
            geometryFactory.createMultiLineString(geometryFactory.toLineStringArray(lineStrings));

      } else if (outGeometry instanceof Polygon) {

        Polygon polygon = (Polygon) outGeometry;
        LinearRing exteriorRing =
            geometryFactory.createLinearRing(
                snapToLineString(polygon.getExteriorRing(), linearGeometry, distance));
        List<LinearRing> interiorRings = new ArrayList<LinearRing>();

        for (int iRing = 0; iRing < polygon.getNumInteriorRing(); iRing++) {

          interiorRings.add(
              geometryFactory.createLinearRing(
                  snapToLineString(polygon.getInteriorRingN(iRing), linearGeometry, distance)));
        }

        outGeometry =
            geometryFactory.createPolygon(
                exteriorRing, geometryFactory.toLinearRingArray(interiorRings));

      } else if (outGeometry instanceof MultiPolygon) {

        MultiPolygon multiPolygon = (MultiPolygon) outGeometry;
        List<Polygon> polygons = new ArrayList<Polygon>();

        for (int iGeom = 0; iGeom < outGeometry.getNumGeometries(); iGeom++) {

          Polygon polygon = (Polygon) multiPolygon.getGeometryN(iGeom);
          LinearRing exteriorRing =
              geometryFactory.createLinearRing(
                  snapToLineString(polygon.getExteriorRing(), linearGeometry, distance));
          List<LinearRing> interiorRings = new ArrayList<LinearRing>();

          for (int iRing = 0; iRing < polygon.getNumInteriorRing(); iRing++) {

            interiorRings.add(
                geometryFactory.createLinearRing(
                    snapToLineString(polygon.getInteriorRingN(iRing), linearGeometry, distance)));
          }

          polygons.add(
              geometryFactory.createPolygon(
                  exteriorRing, geometryFactory.toLinearRingArray(interiorRings)));
        }

        outGeometry = geometryFactory.createMultiPolygon(geometryFactory.toPolygonArray(polygons));

      } else {
        throw new IllegalArgumentException("Unauthorized geometry type");
      }
    }

    return GeometryUtils.getNonEmptyGeometry(inGeometryA.getSRID(), outGeometry);
  }

  @SuppressWarnings("unchecked")
  private Coordinate[] snapToLineString(
      Geometry geometry, Geometry linearGeometry, double distance) {

    MultiPoint linearCoordinates =
        geometryFactory.createMultiPoint(linearGeometry.getCoordinates());

    if (geometry instanceof LineString || geometry instanceof LinearRing) {

      LineMerger merger = new LineMerger();
      merger.add(
          geometryFactory.createMultiLineString(
              splitLineStringAtCoordinates(
                  linearCoordinates.getCoordinates(),
                  geometryFactory.createLineString(geometry.getCoordinates()),
                  distance)));
      geometry = (LineString) merger.getMergedLineStrings().iterator().next();
    }

    CoordinateList newCoordinates = new CoordinateList();
    LengthIndexedLine linearIndexedLine = new LengthIndexedLine(linearGeometry);

    for (Coordinate coordinate : geometry.getCoordinates()) {

      Point inputPoint = geometryFactory.createPoint(coordinate);

      // Si coordonnée n'est pas déjà présente sur la géométrie à
      // accrocher
      if (!(CoordinateArrays.indexOf(inputPoint.getCoordinate(), linearGeometry.getCoordinates())
          > 0)) {

        // double minDistanceCoords =
        // inputPoint.distance(linearCoordinates);
        double minDistanceCoords = inputPoint.distance(linearCoordinates);

        // Tentative de snapping si distance < au seuil
        if (minDistanceCoords <= distance) {
          GeometrySnapper geometrySnapper = new GeometrySnapper(inputPoint);
          inputPoint = (Point) geometrySnapper.snapTo(linearGeometry, minDistanceCoords + 1);
        }

        // Si coordonnée snappée n'est pas déjà présente sur la
        // géométrie à accrocher
        if (!(CoordinateArrays.indexOf(inputPoint.getCoordinate(), linearGeometry.getCoordinates())
            > 0)) {

          // Tentative d'accrochage au segment si distance < au seuil
          if (inputPoint.distance(linearGeometry) <= distance) {

            newCoordinates.add(
                linearIndexedLine.extractPoint(
                    linearIndexedLine.project(inputPoint.getCoordinate())));

          } else {

            newCoordinates.add(inputPoint.getCoordinate(), true);
          }

        } else {

          newCoordinates.add(inputPoint.getCoordinate());
        }

      } else {
        newCoordinates.add(coordinate);
      }
    }

    return CoordinateArrays.removeRepeatedPoints(newCoordinates.toCoordinateArray());
  }

  private static LineString[] splitLineStringAtCoordinates(
      Coordinate[] splitCoordinates, LineString inputLineString, double distance) {

    List<LineString> lineStrings = new ArrayList<LineString>();
    LengthIndexedLine lengthIndexedLine = new LengthIndexedLine(inputLineString);

    TreeSet<Double> indexesTree = new TreeSet<Double>();
    indexesTree.add(lengthIndexedLine.getStartIndex());
    indexesTree.add(lengthIndexedLine.getEndIndex());

    for (Coordinate splitCoordinate : splitCoordinates) {

      if (geometryFactory.createPoint(splitCoordinate).distance(inputLineString) <= distance) {
        indexesTree.add(lengthIndexedLine.project(splitCoordinate));
      }
    }

    Double[] indexes = indexesTree.toArray(new Double[indexesTree.size()]);
    for (int i = 0; i < indexes.length - 1; i++) {

      LineString splitedLineString =
          (LineString) lengthIndexedLine.extractLine(indexes[i], indexes[i + 1]);
      if (splitedLineString != null && !splitedLineString.isEmpty()) {
        lineStrings.add(splitedLineString);
      }
    }

    return lineStrings.toArray(new LineString[lineStrings.size()]);
  }
}
