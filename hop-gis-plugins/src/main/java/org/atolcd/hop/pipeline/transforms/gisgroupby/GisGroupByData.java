package org.atolcd.hop.pipeline.transforms.gisgroupby;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class GisGroupByData extends BaseTransformData {
    public Object[] previous;

    /**
     * target value meta for aggregation fields
     */
    public IRowMeta aggMeta;
    public Object[] agg;
    public IRowMeta groupMeta;
    public IRowMeta groupAggMeta; // for speed: groupMeta+aggMeta
    public int[] groupnrs;
    /**
     * array, length is equal to aggMeta value meta list size and metadata
     * subject fields length. Values corresponds to input values used to
     * calculate target results.
     */
    public int[] subjectnrs;
    public long[] counts;

    public Set<Object>[] distinctObjs;

    public ArrayList<Object[]> bufferList;

    public File tempFile;

    public FileOutputStream fos;

    public DataOutputStream dos;

    public int rowsOnFile;

    public boolean firstRead;

    public FileInputStream fis;
    public DataInputStream dis;

    public Object[] groupResult;

    public boolean hasOutput;

    public IRowMeta inputRowMeta;
    public IRowMeta outputRowMeta;

    public List<Integer> cumulativeSumSourceIndexes;
    public List<Integer> cumulativeSumTargetIndexes;

    public List<Integer> cumulativeAvgSourceIndexes;
    public List<Integer> cumulativeAvgTargetIndexes;

    public Object[] previousSums;

    public Object[] previousAvgSum;

    public long[] previousAvgCount;

    public IValueMeta valueMetaInteger;
    public IValueMeta valueMetaNumber;

    public double[] mean;

    public boolean newBatch;

    /**
   *
   */
    public GisGroupByData() {
        super();

        previous = null;
    }

}
