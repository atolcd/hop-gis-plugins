package com.atolcd.hop.pipeline.transforms.gisgroupby;

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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.atolcd.hop.core.row.value.GeometryInterface;
import com.atolcd.hop.gis.utils.GeometryUtils;
import com.atolcd.hop.pipeline.transforms.gisrelate.GisRelateData;
import com.atolcd.hop.pipeline.transforms.gisrelate.GisRelateMeta;
import org.locationtech.jts.dissolve.LineDissolver;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.util.GeometryCombiner;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopFileException;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.row.RowDataUtil;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.ValueDataUtil;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaNumber;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.ITransformData;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.ITransformMeta;

/**
 * Groups informations based on aggregation rules. (sum, count, ...)
 *
 * @author Matt
 * @since 2-jun-2003
 */
public class GisGroupBy extends BaseTransform<GisGroupByMeta,GisGroupByData> implements ITransform<GisGroupByMeta,GisGroupByData> {
    private static Class<?> PKG = GisGroupByMeta.class; // for i18n purposes,
                                                        // needed by
                                                        // Translator2!!

    // private boolean allNullsAreZero = false;
    private boolean minNullIsValued = false;
    private Integer geometrySRID = 0;

    public GisGroupBy(TransformMeta stepMeta, GisGroupByMeta meta, GisGroupByData data, int copyNr, PipelineMeta transMeta, Pipeline trans) {
        super(stepMeta, meta, data, copyNr, transMeta, trans);

    }

    @Override
    public boolean processRow() throws HopException {

        Object[] r = getRow(); // get row!

        if (first) {

            /*
             * String val = getVariable(
             * Const.KETTLE_AGGREGATION_ALL_NULLS_ARE_ZERO, "N" );
             * allNullsAreZero = ValueMetaBase.convertStringToBoolean( val );
             * val = getVariable( Const.KETTLE_AGGREGATION_MIN_NULL_IS_VALUED,
             * "N" ); minNullIsValued = ValueMetaBase.convertStringToBoolean(
             * val );
             */

            // What is the output looking like?
            //
            data.inputRowMeta = getInputRowMeta();

            // In case we have 0 input rows, we still want to send out a single
            // row aggregate
            // However... the problem then is that we don't know the layout from
            // receiving it from the previous step over the
            // row set.
            // So we need to calculated based on the metadata...
            //
            if (data.inputRowMeta == null) {
                data.inputRowMeta = getPipelineMeta().getPrevTransformFields(this, getTransformMeta());
            }

            data.outputRowMeta = data.inputRowMeta.clone();
            meta.getFields(data.outputRowMeta, getTransformName(), null, null, this, metadataProvider);

            // Do all the work we can beforehand
            // Calculate indexes, loop up fields, etc.
            //
            data.counts = new long[meta.getSubjectField().length];
            data.subjectnrs = new int[meta.getSubjectField().length];

            data.cumulativeSumSourceIndexes = new ArrayList<Integer>();
            data.cumulativeSumTargetIndexes = new ArrayList<Integer>();

            data.cumulativeAvgSourceIndexes = new ArrayList<Integer>();
            data.cumulativeAvgTargetIndexes = new ArrayList<Integer>();

            for (int i = 0; i < meta.getSubjectField().length; i++) {
                if (meta.getAggregateType()[i] == GisGroupByMeta.TYPE_GROUP_COUNT_ANY) {
                    data.subjectnrs[i] = 0;
                } else {
                    data.subjectnrs[i] = data.inputRowMeta.indexOfValue(meta.getSubjectField()[i]);
                }
                if ((r != null) && (data.subjectnrs[i] < 0)) {
                    logError(BaseMessages.getString(PKG, "GroupBy.Log.AggregateSubjectFieldCouldNotFound", meta.getSubjectField()[i]));
                    setErrors(1);
                    stopAll();
                    return false;
                }

                if (meta.getAggregateType()[i] == GisGroupByMeta.TYPE_GROUP_CUMULATIVE_SUM) {
                    data.cumulativeSumSourceIndexes.add(data.subjectnrs[i]);

                    // The position of the target in the output row is the input
                    // row size + i
                    //
                    data.cumulativeSumTargetIndexes.add(data.inputRowMeta.size() + i);
                }
                if (meta.getAggregateType()[i] == GisGroupByMeta.TYPE_GROUP_CUMULATIVE_AVERAGE) {
                    data.cumulativeAvgSourceIndexes.add(data.subjectnrs[i]);

                    // The position of the target in the output row is the input
                    // row size + i
                    //
                    data.cumulativeAvgTargetIndexes.add(data.inputRowMeta.size() + i);
                }

            }

            data.previousSums = new Object[data.cumulativeSumTargetIndexes.size()];

            data.previousAvgSum = new Object[data.cumulativeAvgTargetIndexes.size()];
            data.previousAvgCount = new long[data.cumulativeAvgTargetIndexes.size()];

            data.groupnrs = new int[meta.getGroupField().length];
            for (int i = 0; i < meta.getGroupField().length; i++) {
                data.groupnrs[i] = data.inputRowMeta.indexOfValue(meta.getGroupField()[i]);
                if ((r != null) && (data.groupnrs[i] < 0)) {
                    logError(BaseMessages.getString(PKG, "GroupBy.Log.GroupFieldCouldNotFound", meta.getGroupField()[i]));
                    setErrors(1);
                    stopAll();
                    return false;
                }
            }

            // Create a metadata value for the counter Integers
            //
            data.valueMetaInteger = new ValueMetaInteger("count");
            data.valueMetaNumber = new ValueMetaNumber("sum");

            // Initialize the group metadata
            //
            initGroupMeta(data.inputRowMeta);
        }

        if (first || data.newBatch) {
            // Create a new group aggregate (init)
            //
            newAggregate(r);
        }

        if (first) {
            // for speed: groupMeta+aggMeta
            //
            data.groupAggMeta = new RowMeta();
            data.groupAggMeta.addRowMeta(data.groupMeta);
            data.groupAggMeta.addRowMeta(data.aggMeta);
        }

        if (r == null) // no more input to be expected... (or none received in
                       // the first place)
        {
            handleLastOfGroup();
            setOutputDone();
            return false;
        }

        if (first || data.newBatch) {
            first = false;
            data.newBatch = false;

            data.previous = data.inputRowMeta.cloneRow(r); // copy the row to
                                                           // previous
        } else {
            calcAggregate(data.previous);
            // System.out.println("After calc, agg="+agg);

            if (meta.passAllRows()) {
                addToBuffer(data.previous);
            }
        }

        // System.out.println("Check for same group...");

        if (!sameGroup(data.previous, r)) {
            // System.out.println("Different group!");

            if (meta.passAllRows()) {
                // System.out.println("Close output...");

                // Not the same group: close output (if any)
                closeOutput();

                // System.out.println("getAggregateResult()");

                // Get all rows from the buffer!
                data.groupResult = getAggregateResult();

                // System.out.println("dump rows from the buffer");

                Object[] row = getRowFromBuffer();

                long lineNr = 0;
                while (row != null) {
                    int size = data.inputRowMeta.size();

                    row = RowDataUtil.addRowData(row, size, data.groupResult);
                    size += data.groupResult.length;

                    lineNr++;

                    if (meta.isAddingLineNrInGroup() && !meta.getLineNrInGroupField().isEmpty()) {
                        Object lineNrValue = new Long(lineNr);
                        // IValueMeta lineNrValueMeta = new
                        // ValueMeta(meta.getLineNrInGroupField(),
                        // IValueMeta.TYPE_INTEGER);
                        // lineNrValueMeta.setLength(9);
                        row = RowDataUtil.addValueData(row, size, lineNrValue);
                        size++;
                    }

                    addCumulativeSums(row);
                    addCumulativeAverages(row);

                    putRow(data.outputRowMeta, row);
                    row = getRowFromBuffer();
                }
                closeInput();
            } else {
                Object[] result = buildResult(data.previous);
                if (result != null) {
                    putRow(data.groupAggMeta, result); // copy row to possible
                                                       // alternate rowset(s).
                }
            }
            newAggregate(r); // Create a new group aggregate (init)
        }

        data.previous = data.inputRowMeta.cloneRow(r);

        if (checkFeedback(getLinesRead())) {
            if (log.isBasic()) {
                logBasic(BaseMessages.getString(PKG, "GroupBy.LineNumber") + getLinesRead());
            }
        }

        return true;
    }

    private void handleLastOfGroup() throws HopException {
        if (meta.passAllRows()) {
            // ALL ROWS

            if (data.previous != null) {
                calcAggregate(data.previous);
                addToBuffer(data.previous);
            }
            data.groupResult = getAggregateResult();

            Object[] row = getRowFromBuffer();

            long lineNr = 0;
            while (row != null) {
                int size = data.inputRowMeta.size();
                row = RowDataUtil.addRowData(row, size, data.groupResult);
                size += data.groupResult.length;
                lineNr++;

                if (meta.isAddingLineNrInGroup() && !meta.getLineNrInGroupField().isEmpty()) {
                    Object lineNrValue = new Long(lineNr);
                    // IValueMeta lineNrValueMeta = new
                    // ValueMeta(meta.getLineNrInGroupField(),
                    // IValueMeta.TYPE_INTEGER);
                    // lineNrValueMeta.setLength(9);
                    row = RowDataUtil.addValueData(row, size, lineNrValue);
                    size++;
                }

                addCumulativeSums(row);
                addCumulativeAverages(row);

                putRow(data.outputRowMeta, row);
                row = getRowFromBuffer();
            }
            closeInput();
        } else {
            // JUST THE GROUP + AGGREGATE

            // Don't forget the last set of rows...
            if (data.previous != null) {
                calcAggregate(data.previous);
            }
            Object[] result = buildResult(data.previous);
            if (result != null) {
                putRow(data.groupAggMeta, result);
            }
        }
    }

    private void addCumulativeSums(Object[] row) throws HopValueException {

        // We need to adjust this row with cumulative averages?
        //
        for (int i = 0; i < data.cumulativeSumSourceIndexes.size(); i++) {
            int sourceIndex = data.cumulativeSumSourceIndexes.get(i);
            Object previousTarget = data.previousSums[i];
            Object sourceValue = row[sourceIndex];

            int targetIndex = data.cumulativeSumTargetIndexes.get(i);

            IValueMeta sourceMeta = data.inputRowMeta.getValueMeta(sourceIndex);
            IValueMeta targetMeta = data.outputRowMeta.getValueMeta(targetIndex);

            // If the first values where null, or this is the first time around,
            // just take the source value...
            //
            if (targetMeta.isNull(previousTarget)) {
                row[targetIndex] = sourceMeta.convertToNormalStorageType(sourceValue);
            } else {
                // If the source value is null, just take the previous target
                // value
                //
                if (sourceMeta.isNull(sourceValue)) {
                    row[targetIndex] = previousTarget;
                } else {
                    row[targetIndex] = ValueDataUtil.plus(targetMeta, data.previousSums[i], sourceMeta, row[sourceIndex]);
                }
            }
            data.previousSums[i] = row[targetIndex];
        }

    }

    private void addCumulativeAverages(Object[] row) throws HopValueException {

        // We need to adjust this row with cumulative sums
        //
        for (int i = 0; i < data.cumulativeAvgSourceIndexes.size(); i++) {
            int sourceIndex = data.cumulativeAvgSourceIndexes.get(i);
            Object previousTarget = data.previousAvgSum[i];
            Object sourceValue = row[sourceIndex];

            int targetIndex = data.cumulativeAvgTargetIndexes.get(i);

            IValueMeta sourceMeta = data.inputRowMeta.getValueMeta(sourceIndex);
            IValueMeta targetMeta = data.outputRowMeta.getValueMeta(targetIndex);

            // If the first values where null, or this is the first time around,
            // just take the source value...
            //
            Object sum = null;

            if (targetMeta.isNull(previousTarget)) {
                sum = sourceMeta.convertToNormalStorageType(sourceValue);
            } else {
                // If the source value is null, just take the previous target
                // value
                //
                if (sourceMeta.isNull(sourceValue)) {
                    sum = previousTarget;
                } else {
                    if (sourceMeta.isInteger()) {
                        sum = ValueDataUtil.plus(data.valueMetaInteger, data.previousAvgSum[i], sourceMeta, row[sourceIndex]);
                    } else {
                        sum = ValueDataUtil.plus(targetMeta, data.previousAvgSum[i], sourceMeta, row[sourceIndex]);
                    }
                }
            }
            data.previousAvgSum[i] = sum;

            if (!sourceMeta.isNull(sourceValue)) {
                data.previousAvgCount[i]++;
            }

            if (sourceMeta.isInteger()) {
                // Change to number as the exception
                //
                if (sum == null) {
                    row[targetIndex] = null;
                } else {
                    row[targetIndex] = new Double(((Long) sum).doubleValue() / data.previousAvgCount[i]);
                }
            } else {
                row[targetIndex] = ValueDataUtil.divide(targetMeta, sum, data.valueMetaInteger, data.previousAvgCount[i]);
            }
        }

    }

    // Is the row r of the same group as previous?
    private boolean sameGroup(Object[] previous, Object[] r) throws HopValueException {
        return data.inputRowMeta.compare(previous, r, data.groupnrs) == 0;
    }

    /**
     * used for junits in GroupByAggregationNullsTest
     * 
     * @param r
     * @throws HopValueException
     */
    @SuppressWarnings("unchecked")
    void calcAggregate(Object[] r) throws HopValueException {
        for (int i = 0; i < data.subjectnrs.length; i++) {
            Object subj = r[data.subjectnrs[i]];
            IValueMeta subjMeta = data.inputRowMeta.getValueMeta(data.subjectnrs[i]);
            Object value = data.agg[i];
            IValueMeta valueMeta = data.aggMeta.getValueMeta(i);

            switch (meta.getAggregateType()[i]) {
            case GisGroupByMeta.TYPE_GROUP_SUM:
                data.agg[i] = ValueDataUtil.sum(valueMeta, value, subjMeta, subj);
                break;
            case GisGroupByMeta.TYPE_GROUP_AVERAGE:
                if (!subjMeta.isNull(subj)) {
                    data.agg[i] = ValueDataUtil.sum(valueMeta, value, subjMeta, subj);
                    data.counts[i]++;
                }
                break;
            case GisGroupByMeta.TYPE_GROUP_MEDIAN:
            case GisGroupByMeta.TYPE_GROUP_PERCENTILE:
                if (!subjMeta.isNull(subj)) {
                    ((List<Double>) data.agg[i]).add(subjMeta.getNumber(subj));
                }
                break;
            case GisGroupByMeta.TYPE_GROUP_STANDARD_DEVIATION:
                if (!subjMeta.isNull(subj)) {
                    data.counts[i]++;
                    double n = data.counts[i];
                    double x = subjMeta.getNumber(subj);
                    // for standard deviation null is exact 0
                    double sum = value == null ? new Double(0) : (Double) value;
                    double mean = data.mean[i];

                    double delta = x - mean;
                    mean = mean + (delta / n);
                    sum = sum + delta * (x - mean);

                    data.mean[i] = mean;
                    data.agg[i] = sum;
                }
                break;
            case GisGroupByMeta.TYPE_GROUP_COUNT_DISTINCT:
                if (!subjMeta.isNull(subj)) {
                    if (data.distinctObjs == null) {
                        data.distinctObjs = new Set[meta.getSubjectField().length];
                    }
                    if (data.distinctObjs[i] == null) {
                        data.distinctObjs[i] = new TreeSet<Object>();
                    }
                    Object obj = subjMeta.convertToNormalStorageType(subj);
                    if (!data.distinctObjs[i].contains(obj)) {
                        data.distinctObjs[i].add(obj);
                        // null is exact 0, or we will not be able to ++.
                        value = value == null ? new Long(0) : value;
                        data.agg[i] = (Long) value + 1;
                    }
                }
                break;
            case GisGroupByMeta.TYPE_GROUP_COUNT_ALL:
                if (!subjMeta.isNull(subj)) {
                    data.counts[i]++;
                }
                break;
            case GisGroupByMeta.TYPE_GROUP_COUNT_ANY:
                data.counts[i]++;
                break;
            case GisGroupByMeta.TYPE_GROUP_MIN: {
                if (subj == null && !minNullIsValued) {
                    // PDI-10250 do not compare null
                    break;
                }
                if (subjMeta.isSortedDescending()) {
                    // Account for negation in ValueMeta.compare() - See
                    // PDI-2302
                    if (subjMeta.compare(value, valueMeta, subj) < 0) {
                        data.agg[i] = subj;
                    }
                } else {
                    if (subjMeta.compare(subj, valueMeta, value) < 0) {
                        data.agg[i] = subj;
                    }
                }
                break;
            }
            case GisGroupByMeta.TYPE_GROUP_MAX:
                if (subjMeta.isSortedDescending()) {
                    // Account for negation in ValueMeta.compare() - See
                    // PDI-2302
                    if (subjMeta.compare(value, valueMeta, subj) > 0) {
                        data.agg[i] = subj;
                    }
                } else {
                    if (subjMeta.compare(subj, valueMeta, value) > 0) {
                        data.agg[i] = subj;
                    }
                }
                break;
            case GisGroupByMeta.TYPE_GROUP_FIRST:
                if (!(subj == null) && value == null) {
                    data.agg[i] = subj;
                }
                break;
            case GisGroupByMeta.TYPE_GROUP_LAST:
                if (!(subj == null)) {
                    data.agg[i] = subj;
                }
                break;
            case GisGroupByMeta.TYPE_GROUP_FIRST_INCL_NULL:
                // This is on purpose. The calculation of the
                // first field is done when setting up a new group
                // This is just the field of the first row
                // if (linesWritten==0) value.setValue(subj);
                break;
            case GisGroupByMeta.TYPE_GROUP_LAST_INCL_NULL:
                data.agg[i] = subj;
                break;
            case GisGroupByMeta.TYPE_GROUP_CONCAT_COMMA:
                if (!(subj == null)) {
                    StringBuilder sb = (StringBuilder) value;
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(subjMeta.getString(subj));
                }
                break;
            case GisGroupByMeta.TYPE_GROUP_CONCAT_STRING:
                if (!(subj == null)) {
                    String separator = "";
                    if (!meta.getValueField()[i].isEmpty()) {
                        separator = resolve(meta.getValueField()[i]);
                    }

                    StringBuilder sb = (StringBuilder) value;
                    if (sb.length() > 0) {
                        sb.append(separator);
                    }
                    sb.append(subjMeta.getString(subj));
                }

                break;

            // GIS : Union des géométries
            case GisGroupByMeta.TYPE_GROUP_GEOMETRY_UNION:
                if (subj != null) {
                    Geometry geometryForUnion = GeometryCombiner.combine(((GeometryInterface) valueMeta).getGeometry(value), ((GeometryInterface) subjMeta).getGeometry(subj));
                    geometrySRID = ((GeometryInterface) subjMeta).getGeometry(subj).getSRID();
                    data.agg[i] = geometryForUnion;

                }
                break;

            // GIS : Union des étendues
            case GisGroupByMeta.TYPE_GROUP_GEOMETRY_EXTENT:
                if (subj != null) {
                    Geometry geometryForExtent = GeometryCombiner.combine(((GeometryInterface) valueMeta).getGeometry(value).getEnvelope(), ((GeometryInterface) subjMeta)
                            .getGeometry(subj).getEnvelope());
                    geometrySRID = ((GeometryInterface) subjMeta).getGeometry(subj).getSRID();
                    data.agg[i] = geometryForExtent;
                }
                break;

            // GIS : Union des étendues
            case GisGroupByMeta.TYPE_GROUP_GEOMETRY_AGG:
                if (subj != null) {
                    Geometry geometryForAgg = GeometryCombiner.combine(((GeometryInterface) valueMeta).getGeometry(value), ((GeometryInterface) subjMeta).getGeometry(subj));
                    geometrySRID = ((GeometryInterface) subjMeta).getGeometry(subj).getSRID();
                    data.agg[i] = geometryForAgg;
                }
                break;

            // GIS : Dissolution des géométries
            case GisGroupByMeta.TYPE_GROUP_GEOMETRY_DISSOLVE:
                if (subj != null) {
                    Geometry geometryForDissolve = GeometryCombiner.combine(((GeometryInterface) valueMeta).getGeometry(value), ((GeometryInterface) subjMeta).getGeometry(subj));
                    geometrySRID = ((GeometryInterface) subjMeta).getGeometry(subj).getSRID();
                    data.agg[i] = geometryForDissolve;
                }
                break;

            default:
                break;
            }
        }
    }

    /**
     * used for junits in GroupByAggregationNullsTest
     * 
     * @param r
     */
    void newAggregate(Object[] r) {
        // Put all the counters at 0
        for (int i = 0; i < data.counts.length; i++) {
            data.counts[i] = 0;
        }
        data.distinctObjs = null;
        data.agg = new Object[data.subjectnrs.length];
        data.mean = new double[data.subjectnrs.length]; // sets all doubles to
                                                        // 0.0
        data.aggMeta = new RowMeta();

        for (int i = 0; i < data.subjectnrs.length; i++) {
            IValueMeta subjMeta = data.inputRowMeta.getValueMeta(data.subjectnrs[i]);
            Object v = null;
            IValueMeta vMeta = null;
            int aggType = meta.getAggregateType()[i];
            switch (aggType) {
            case GisGroupByMeta.TYPE_GROUP_SUM:
            case GisGroupByMeta.TYPE_GROUP_AVERAGE:
            case GisGroupByMeta.TYPE_GROUP_CUMULATIVE_SUM:
            case GisGroupByMeta.TYPE_GROUP_CUMULATIVE_AVERAGE:
                vMeta = new ValueMetaBase(meta.getAggregateField()[i], subjMeta.isNumeric() ? subjMeta.getType() : IValueMeta.TYPE_NUMBER);
                break;
            case GisGroupByMeta.TYPE_GROUP_MEDIAN:
            case GisGroupByMeta.TYPE_GROUP_PERCENTILE:
                vMeta = new ValueMetaBase(meta.getAggregateField()[i], IValueMeta.TYPE_NUMBER);
                v = new ArrayList<Double>();
                break;
            case GisGroupByMeta.TYPE_GROUP_STANDARD_DEVIATION:
                vMeta = new ValueMetaBase(meta.getAggregateField()[i], IValueMeta.TYPE_NUMBER);
                break;
            case GisGroupByMeta.TYPE_GROUP_COUNT_DISTINCT:
            case GisGroupByMeta.TYPE_GROUP_COUNT_ANY:
            case GisGroupByMeta.TYPE_GROUP_COUNT_ALL:
                vMeta = new ValueMetaBase(meta.getAggregateField()[i], IValueMeta.TYPE_INTEGER);
                break;
            case GisGroupByMeta.TYPE_GROUP_FIRST:
            case GisGroupByMeta.TYPE_GROUP_LAST:
            case GisGroupByMeta.TYPE_GROUP_FIRST_INCL_NULL:
            case GisGroupByMeta.TYPE_GROUP_LAST_INCL_NULL:
            case GisGroupByMeta.TYPE_GROUP_MIN:
            case GisGroupByMeta.TYPE_GROUP_MAX:
                vMeta = subjMeta.clone();
                vMeta.setName(meta.getAggregateField()[i]);
                v = r == null ? null : r[data.subjectnrs[i]];
                break;
            case GisGroupByMeta.TYPE_GROUP_CONCAT_COMMA:
                vMeta = new ValueMetaBase(meta.getAggregateField()[i], IValueMeta.TYPE_STRING);
                v = new StringBuilder();
                break;
            case GisGroupByMeta.TYPE_GROUP_CONCAT_STRING:
                vMeta = new ValueMetaBase(meta.getAggregateField()[i], IValueMeta.TYPE_STRING);
                v = new StringBuilder();
                break;

            // GIS : Ajout des géométries ou étendues à la collection
            case GisGroupByMeta.TYPE_GROUP_GEOMETRY_UNION:
            case GisGroupByMeta.TYPE_GROUP_GEOMETRY_EXTENT:
            case GisGroupByMeta.TYPE_GROUP_GEOMETRY_AGG:
            case GisGroupByMeta.TYPE_GROUP_GEOMETRY_DISSOLVE:
                vMeta = subjMeta.clone();
                vMeta.setName(meta.getAggregateField()[i]);
                v = new GeometryFactory().createGeometryCollection(null);
                break;

            default:

                break;
            }

            if ((subjMeta != null)
                    && (aggType != GisGroupByMeta.TYPE_GROUP_COUNT_ALL && aggType != GisGroupByMeta.TYPE_GROUP_COUNT_DISTINCT && aggType != GisGroupByMeta.TYPE_GROUP_COUNT_ANY)) {
                vMeta.setLength(subjMeta.getLength(), subjMeta.getPrecision());
            }
            data.agg[i] = v;
            data.aggMeta.addValueMeta(vMeta);
        }

        // Also clear the cumulative data...
        //
        for (int i = 0; i < data.previousSums.length; i++) {
            data.previousSums[i] = null;
        }
        for (int i = 0; i < data.previousAvgCount.length; i++) {
            data.previousAvgCount[i] = 0L;
            data.previousAvgSum[i] = null;
        }
    }

    private Object[] buildResult(Object[] r) throws HopValueException {
        Object[] result = null;
        if (r != null || meta.isAlwaysGivingBackOneRow()) {
            result = RowDataUtil.allocateRowData(data.groupnrs.length);
            if (r != null) {
                for (int i = 0; i < data.groupnrs.length; i++) {
                    result[i] = r[data.groupnrs[i]];
                }
            }

            result = RowDataUtil.addRowData(result, data.groupnrs.length, getAggregateResult());
        }

        return result;
    }

    private void initGroupMeta(IRowMeta previousRowMeta) throws HopValueException {
        data.groupMeta = new RowMeta();
        for (int i = 0; i < data.groupnrs.length; i++) {
            data.groupMeta.addValueMeta(previousRowMeta.getValueMeta(data.groupnrs[i]));
        }

        return;
    }

    /**
     * Used for junits in GroupByAggregationNullsTest
     * 
     * @return
     * @throws HopValueException
     */
    Object[] getAggregateResult() throws HopValueException {
        Object[] result = new Object[data.subjectnrs.length];

        if (data.subjectnrs != null) {
            for (int i = 0; i < data.subjectnrs.length; i++) {
                Object ag = data.agg[i];
                switch (meta.getAggregateType()[i]) {
                case GisGroupByMeta.TYPE_GROUP_SUM:
                    break;
                case GisGroupByMeta.TYPE_GROUP_AVERAGE:
                    ag = ValueDataUtil.divide(data.aggMeta.getValueMeta(i), ag, new ValueMetaBase("c", IValueMeta.TYPE_INTEGER), new Long(data.counts[i]));
                    break;
                case GisGroupByMeta.TYPE_GROUP_MEDIAN:
                case GisGroupByMeta.TYPE_GROUP_PERCENTILE:
                    double percentile = 50.0;
                    if (meta.getAggregateType()[i] == GisGroupByMeta.TYPE_GROUP_PERCENTILE) {
                        percentile = Double.parseDouble(meta.getValueField()[i]);
                    }
                    @SuppressWarnings("unchecked")
                    List<Double> valuesList = (List<Double>) data.agg[i];
                    double[] values = new double[valuesList.size()];
                    for (int v = 0; v < values.length; v++) {
                        values[v] = valuesList.get(v);
                    }
                    ag = new Percentile().evaluate(values, percentile);
                    break;
                case GisGroupByMeta.TYPE_GROUP_COUNT_ANY:
                case GisGroupByMeta.TYPE_GROUP_COUNT_ALL:
                    ag = new Long(data.counts[i]);
                    break;
                case GisGroupByMeta.TYPE_GROUP_COUNT_DISTINCT:
                    break;
                case GisGroupByMeta.TYPE_GROUP_MIN:
                    break;
                case GisGroupByMeta.TYPE_GROUP_MAX:
                    break;
                case GisGroupByMeta.TYPE_GROUP_STANDARD_DEVIATION:
                    double sum = (Double) ag / data.counts[i];
                    ag = Double.valueOf(Math.sqrt(sum));
                    break;
                case GisGroupByMeta.TYPE_GROUP_CONCAT_COMMA:
                case GisGroupByMeta.TYPE_GROUP_CONCAT_STRING:
                    ag = ((StringBuilder) ag).toString();
                    break;

                // GIS : Union des géométries
                case GisGroupByMeta.TYPE_GROUP_GEOMETRY_UNION:

                    Geometry geomUnionGroup = ((GeometryInterface) data.aggMeta.getValueMeta(i)).getGeometry(ag);
                    UnaryUnionOp unionOperateor = new UnaryUnionOp(geomUnionGroup);
                    Geometry geometryUnion = unionOperateor.union();
                    geometryUnion = GeometryUtils.getMergedGeometry(geometryUnion);
                    geometryUnion.setSRID(geometrySRID);
                    ag = geometryUnion;
                    break;

                // GIS : Extent des géométries
                case GisGroupByMeta.TYPE_GROUP_GEOMETRY_EXTENT:

                    Geometry geomExtentGroup = ((GeometryInterface) data.aggMeta.getValueMeta(i)).getGeometry(ag);
                    Geometry geomtryExtent = geomExtentGroup.getEnvelope();
                    geomtryExtent.setSRID(geometrySRID);
                    ag = geomtryExtent;
                    break;

                // GIS : Aggrégation des géométries
                case GisGroupByMeta.TYPE_GROUP_GEOMETRY_AGG:

                    Geometry geomAggGroup = ((GeometryInterface) data.aggMeta.getValueMeta(i)).getGeometry(ag);
                    Geometry geomtryAgg = geomAggGroup;
                    geomtryAgg.setSRID(geometrySRID);
                    ag = geomtryAgg;
                    break;

                // GIS : Dissolution des géométries
                case GisGroupByMeta.TYPE_GROUP_GEOMETRY_DISSOLVE:

                    Geometry geomDissolveGroup = ((GeometryInterface) data.aggMeta.getValueMeta(i)).getGeometry(ag);
                    Geometry geometryDissolve = LineDissolver.dissolve(geomDissolveGroup);
                    geometryDissolve.setSRID(geometrySRID);
                    ag = geometryDissolve;
                    break;

                default:
                    break;
                }
                /*
                 * if ( ag == null && allNullsAreZero ) { // PDI-10250, 6960
                 * seems all rows for min function was nulls... // get output
                 * subject meta based on original subject meta calculation
                 * IValueMeta vm = data.aggMeta.getValueMeta( i );
                 * 
                 * ag = ValueDataUtil.getZeroForValueMetaType( vm ); }
                 */
                result[i] = ag;
            }
        }
        return result;

    }

    private void addToBuffer(Object[] row) throws HopFileException {
        data.bufferList.add(row);
        if (data.bufferList.size() > 5000) {
            if (data.rowsOnFile == 0) {
                try {
                    data.tempFile = File.createTempFile(meta.getPrefix(), ".tmp", new File(resolve(meta.getDirectory())));
                    data.fos = new FileOutputStream(data.tempFile);
                    data.dos = new DataOutputStream(data.fos);
                    data.firstRead = true;
                } catch (IOException e) {
                    throw new HopFileException(BaseMessages.getString(PKG, "GroupBy.Exception.UnableToCreateTemporaryFile"), e);
                }
            }
            // OK, save the oldest rows to disk!
            Object[] oldest = data.bufferList.get(0);
            data.inputRowMeta.writeData(data.dos, oldest);
            data.bufferList.remove(0);
            data.rowsOnFile++;
        }
    }

    private Object[] getRowFromBuffer() throws HopFileException {
        if (data.rowsOnFile > 0) {
            if (data.firstRead) {
                // Open the inputstream first...
                try {
                    data.fis = new FileInputStream(data.tempFile);
                    data.dis = new DataInputStream(data.fis);
                    data.firstRead = false;
                } catch (IOException e) {
                    throw new HopFileException(BaseMessages.getString(PKG, "GroupBy.Exception.UnableToReadBackRowFromTemporaryFile"), e);
                }
            }

            // Read one row from the file!
            Object[] row;
            try {
                row = data.inputRowMeta.readData(data.dis);
            } catch (SocketTimeoutException e) {
                throw new HopFileException(e); // Shouldn't happen on files
            }
            data.rowsOnFile--;

            return row;
        } else {
            if (data.bufferList.size() > 0) {
                Object[] row = data.bufferList.get(0);
                data.bufferList.remove(0);
                return row;
            } else {
                return null; // Nothing left!
            }
        }
    }

    private void closeOutput() throws HopFileException {
        try {
            if (data.dos != null) {
                data.dos.close();
                data.dos = null;
            }
            if (data.fos != null) {
                data.fos.close();
                data.fos = null;
            }
            data.firstRead = true;
        } catch (IOException e) {
            throw new HopFileException(BaseMessages.getString(PKG, "GroupBy.Exception.UnableToCloseInputStream"), e);
        }
    }

    private void closeInput() throws HopFileException {
        try {
            if (data.fis != null) {
                data.fis.close();
                data.fis = null;
            }
            if (data.dis != null) {
                data.dis.close();
                data.dis = null;
            }
        } catch (IOException e) {
            throw new HopFileException(BaseMessages.getString(PKG, "GroupBy.Exception.UnableToCloseInputStream"), e);
        }
    }

    @Override
    public boolean init() {

        if (super.init()) {
            data.bufferList = new ArrayList<Object[]>();

            data.rowsOnFile = 0;

            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        if (data.tempFile != null) {
            data.tempFile.delete();
        }

        super.dispose();
    }

    public void batchComplete() throws HopException {
        handleLastOfGroup();
        data.newBatch = true;
    }

    /**
     * Used for junits in GroupByAggregationNullsTest
     * 
     * @param allNullsAreZero
     *            the allNullsAreZero to set
     */
    /*
     * void setAllNullsAreZero( boolean allNullsAreZero ) { this.allNullsAreZero
     * = allNullsAreZero; }
     */

    /**
     * Used for junits in GroupByAggregationNullsTest
     * 
     * @param minNullIsValued
     *            the minNullIsValued to set
     */
    void setMinNullIsValued(boolean minNullIsValued) {
        this.minNullIsValued = minNullIsValued;
    }
}
