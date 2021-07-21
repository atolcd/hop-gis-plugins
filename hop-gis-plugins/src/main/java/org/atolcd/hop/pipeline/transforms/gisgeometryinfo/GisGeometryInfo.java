package org.atolcd.hop.pipeline.transforms.gisgeometryinfo;

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

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.atolcd.hop.core.row.value.ValueMetaGeometry;
import org.atolcd.hop.gis.utils.GeometryUtils;
import org.atolcd.hop.pipeline.transforms.gisrelate.GisRelateData;
import org.atolcd.hop.pipeline.transforms.gisrelate.GisRelateMeta;
import org.locationtech.jts.geom.Geometry;

import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.RowDataUtil;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.ITransformData;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.ITransformMeta;

public class GisGeometryInfo extends BaseTransform<GisGeometryInfoMeta, GisGeometryInfoData> implements ITransform<GisGeometryInfoMeta, GisGeometryInfoData> {

    private Integer geometryFieldIndex;
    private LinkedHashMap<String, Integer> outputMap = new LinkedHashMap<String, Integer>();

    public GisGeometryInfo(TransformMeta s, GisGeometryInfoMeta meta, GisGeometryInfoData data, int c, PipelineMeta t, Pipeline dis) {
        super(s, meta, data, c, t, dis);
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

            // Récupération de l'index de la colonne contenant la géométrie
            // d'entrée
            geometryFieldIndex = getInputRowMeta().indexOfValue(meta.getGeometryFieldName());

            // Récupération des infos demandées et des index des colonnes
            // resultats
            for (Entry<String, String> output : meta.getOutputFields().entrySet()) {
                outputMap.put(output.getKey(), data.outputRowMeta.indexOfValue(output.getValue()));
            }

            logBasic("Initialized successfully");

        }

        putRow(data.outputRowMeta, getOutputInfoRow(r));

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

    private Object[] getOutputInfoRow(Object[] row) throws HopException {

        Object[] newRow = RowDataUtil.resizeArray(row, row.length + outputMap.size());

        Geometry geometry = new ValueMetaGeometry().getGeometry(row[geometryFieldIndex]);
        Object value = null;

        for (Entry<String, Integer> output : outputMap.entrySet()) {

            String infoKey = output.getKey();
            Integer fieldIndex = output.getValue();

            if (infoKey.equalsIgnoreCase("NULL_OR_EMPTY")) {
                value = GeometryUtils.isNullOrEmptyGeometry(geometry);

            } else if (infoKey.equalsIgnoreCase("AREA")) {
                value = GeometryUtils.getArea(geometry);

            } else if (infoKey.equalsIgnoreCase("LENGTH")) {
                value = GeometryUtils.getLength(geometry);

            } else if (infoKey.equalsIgnoreCase("DIMENSION")) {
                value = getIntegerAsLong(GeometryUtils.getCoordinateDimension(geometry));

            } else if (infoKey.equalsIgnoreCase("SRID")) {
                value = getIntegerAsLong(GeometryUtils.getSrid(geometry));

            } else if (infoKey.equalsIgnoreCase("GEOMETRY_TYPE")) {
                value = GeometryUtils.getGeometryType(geometry);

            } else if (infoKey.equalsIgnoreCase("GEOMETRY_COUNT")) {
                value = getIntegerAsLong(GeometryUtils.getGeometriesCount(geometry));

            } else if (infoKey.equalsIgnoreCase("GEOMETRY_VERTEX_COUNT")) {
                value = getIntegerAsLong(GeometryUtils.getCoordinatesCount(geometry));

            } else if (infoKey.equalsIgnoreCase("X_MIN")) {
                value = GeometryUtils.getMinX(geometry);

            } else if (infoKey.equalsIgnoreCase("X_MAX")) {
                value = GeometryUtils.getMaxX(geometry);

            } else if (infoKey.equalsIgnoreCase("Y_MIN")) {
                value = GeometryUtils.getMinY(geometry);

            } else if (infoKey.equalsIgnoreCase("Y_MAX")) {
                value = GeometryUtils.getMaxY(geometry);

            } else if (infoKey.equalsIgnoreCase("Z_MIN")) {
                value = GeometryUtils.getMinZ(geometry);

            } else if (infoKey.equalsIgnoreCase("Z_MAX")) {
                value = GeometryUtils.getMaxZ(geometry);

            }

            newRow[fieldIndex] = value;
        }

        return newRow;

    }

    private static Long getIntegerAsLong(Integer value) {

        if (value != null) {
            return new Long(value);
        }
        return null;
    }

}
