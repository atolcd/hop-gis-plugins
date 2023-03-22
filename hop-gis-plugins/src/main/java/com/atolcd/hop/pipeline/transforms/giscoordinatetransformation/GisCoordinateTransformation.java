package com.atolcd.hop.pipeline.transforms.giscoordinatetransformation;

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
import com.atolcd.hop.gis.utils.CoordinateTransformer;
import com.atolcd.hop.gis.utils.GeometryUtils;
import java.util.List;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowDataUtil;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.cts.CRSFactory;
import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.crs.GeodeticCRS;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationFactory;
import org.cts.registry.EPSGRegistry;
import org.cts.registry.ESRIRegistry;
import org.cts.registry.IGNFRegistry;
import org.cts.registry.RegistryManager;
import org.locationtech.jts.geom.Geometry;

public class GisCoordinateTransformation
    extends BaseTransform<GisCoordinateTransformationMeta, GisCoordinateTransformationData> {

  private static final Class<?> PKG = GisCoordinateTransformation.class;

  Integer geometryFieldIndex;
  Integer outputFieldIndex;
  String crsOperationType = null;
  protected IValueMeta geometryValueMeta;

  CRSFactory cRSFactory;
  RegistryManager registryManager;

  CoordinateOperation transformation = null;

  public GisCoordinateTransformation(
      TransformMeta s,
      GisCoordinateTransformationMeta meta,
      GisCoordinateTransformationData data,
      int c,
      PipelineMeta t,
      Pipeline dis) {
    super(s, meta, data, c, t, dis);

    cRSFactory = new CRSFactory();
    registryManager = cRSFactory.getRegistryManager();
    registryManager.addRegistry(new IGNFRegistry());
    registryManager.addRegistry(new EPSGRegistry());
    registryManager.addRegistry(new ESRIRegistry());
  }

  @Override
  public boolean processRow() throws HopException {

    Object[] r = getRow();

    if (r == null) {

      setOutputDone();
      return false;
    }

    if (first) {

      first = false;
      data.outputRowMeta = (IRowMeta) getInputRowMeta().clone();
      meta.getFields(data.outputRowMeta, getTransformName(), null, null, this, metadataProvider);

      // Récupération de l'index de la colonne contenant la geométrie
      IRowMeta inputRowMeta = getInputRowMeta();
      geometryFieldIndex =
          getInputRowMeta().indexOfValue(meta.getGeometryFieldName()); // Récupération
      // de
      // l'index
      // de
      // la
      // colonne
      // contenant
      // la
      // geométrie
      geometryValueMeta = inputRowMeta.getValueMeta(geometryFieldIndex);

      // Récupération de l'index de la colonne contenant le résultat
      outputFieldIndex = data.outputRowMeta.indexOfValue(meta.getOutputGeometryFieldName());

      crsOperationType = meta.getCrsOperation();

      if (crsOperationType.equalsIgnoreCase("REPROJECT")) {

        if (!meta.isCrsFromGeometry()) {
          transformation =
              getTransformation(
                  meta.getInputCRSAuthority() + ":" + resolve(meta.getInputCRSCode()),
                  meta.getOutputCRSAuthority() + ":" + resolve(meta.getOutputCRSCode()));
        }
      }

      logBasic("Initialized successfully");
    }

    Object[] outputRow = RowDataUtil.resizeArray(r, r.length + 1);
    Geometry inGeometry =
        ((GeometryInterface) geometryValueMeta).getGeometry(r[geometryFieldIndex]);

    if (crsOperationType.equalsIgnoreCase("ASSIGN")) {

      Geometry outGeometry = (Geometry) inGeometry.clone();

      if (!GeometryUtils.isNullOrEmptyGeometry(inGeometry)) {
        outGeometry.setSRID(Integer.valueOf(resolve(meta.getInputCRSCode())));
      }

      outputRow[outputFieldIndex] = outGeometry;

    } else {

      if (meta.isCrsFromGeometry()) {

        if (!GeometryUtils.isNullOrEmptyGeometry(inGeometry)) {

          if (inGeometry.getSRID() > 0) {

            transformation =
                getTransformation(
                    "EPSG:" + inGeometry.getSRID(),
                    meta.getOutputCRSAuthority() + ":" + resolve(meta.getOutputCRSCode()));

          } else {
            throw new HopException(
                "Transformation error : Unknown SRID for geometry " + inGeometry.toString());
          }
        }
      }

      Geometry outGeometry = null;
      if (transformation != null) {
        outGeometry = CoordinateTransformer.transform(inGeometry, transformation);
      }

      // Assignation SRID si EPSG
      if (meta.getOutputCRSAuthority().equalsIgnoreCase("EPSG")
          && !GeometryUtils.isNullOrEmptyGeometry(outGeometry)) {
        outGeometry.setSRID(Integer.valueOf(resolve(meta.getOutputCRSCode())));
      }

      outputRow[outputFieldIndex] = outGeometry;
    }

    putRow(data.outputRowMeta, outputRow);

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

  private CoordinateOperation getTransformation(String inputCRSCode, String outputCRSCode) {

    CoordinateOperation transformation = null;

    // Création de la transformation à partir des CRS entrées et sorties
    try {

      CoordinateReferenceSystem inputCRS = cRSFactory.getCRS(inputCRSCode);
      CoordinateReferenceSystem outputCRS = cRSFactory.getCRS(outputCRSCode);
      List<CoordinateOperation> transformations =
          CoordinateOperationFactory.createCoordinateOperations(
              (GeodeticCRS) inputCRS, (GeodeticCRS) outputCRS);

      if (!transformations.isEmpty()) {
        transformation = transformations.get(0);
      } else {
        new HopException("No transformation available");
      }

    } catch (CRSException e) {

      new HopException(e);
    }
    return transformation;
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
