package com.atolcd.hop.gis.io.features;

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
import com.atolcd.hop.core.row.value.ValueMetaGeometry;
import com.atolcd.hop.gis.io.features.Field.FieldType;
import java.util.ArrayList;
import java.util.List;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaBoolean;
import org.apache.hop.core.row.value.ValueMetaDate;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaNumber;
import org.apache.hop.core.row.value.ValueMetaString;

public final class FeatureConverter {

  public static List<Field> getFields(IRowMeta rowMeta) {

    List<Field> fields = new ArrayList<Field>();

    for (String fieldName : rowMeta.getFieldNames()) {

      int fieldIndex = rowMeta.indexOfValue(fieldName);
      IValueMeta valueMetaInterface = rowMeta.getValueMeta(fieldIndex);
      Integer length = null;
      Integer precision = null;
      Field field = null;

      if (valueMetaInterface.getLength() != -1) {
        length = valueMetaInterface.getLength();
      }

      if (valueMetaInterface.getPrecision() != -1) {
        precision = valueMetaInterface.getPrecision();
      }

      switch (valueMetaInterface.getType()) {
        case IValueMeta.TYPE_BOOLEAN:
          field = new Field(fieldName, FieldType.BOOLEAN, length, precision);
          break;

        case IValueMeta.TYPE_INTEGER:
          field = new Field(fieldName, FieldType.LONG, length, precision);
          break;

        case IValueMeta.TYPE_NUMBER:
          field = new Field(fieldName, FieldType.DOUBLE, length, precision);
          break;

        case IValueMeta.TYPE_BIGNUMBER:
          field = new Field(fieldName, FieldType.DOUBLE, length, precision);
          break;

        case IValueMeta.TYPE_STRING:
          field = new Field(fieldName, FieldType.STRING, length, precision);
          break;

        case IValueMeta.TYPE_DATE:
          field = new Field(fieldName, FieldType.DATE, length, precision);
          break;

        case ValueMetaGeometry.TYPE_GEOMETRY:
          field = new Field(fieldName, FieldType.GEOMETRY, length, precision);
          break;

        default:
          field = new Field(fieldName, FieldType.STRING, length, precision);
          break;
      }

      fields.add(field);
    }

    return fields;
  }

  public static Feature getFeature(IRowMeta rowMeta, Object[] r) throws HopValueException {

    Feature feature = new Feature();

    for (Field field : getFields(rowMeta)) {

      int fieldIndex = rowMeta.indexOfValue(field.getName());

      if (rowMeta.isNull(r, fieldIndex)) {
        feature.addValue(field, null);
      } else {

        if (rowMeta.getValueMeta(fieldIndex).isBoolean()) {
          feature.addValue(field, rowMeta.getBoolean(r, fieldIndex));
        } else if (rowMeta.getValueMeta(fieldIndex).isInteger()) {
          feature.addValue(field, rowMeta.getInteger(r, fieldIndex));
        } else if (rowMeta.getValueMeta(fieldIndex).isNumber()) {
          feature.addValue(field, rowMeta.getNumber(r, fieldIndex));
        } else if (rowMeta.getValueMeta(fieldIndex).isBigNumber()) {
          feature.addValue(field, rowMeta.getNumber(r, fieldIndex).doubleValue());
        } else if (rowMeta.getValueMeta(fieldIndex).isString()) {
          feature.addValue(field, rowMeta.getString(r, fieldIndex));
        } else if (rowMeta.getValueMeta(fieldIndex).isDate()) {
          feature.addValue(field, (rowMeta.getDate(r, fieldIndex)));
        } else if (rowMeta.getValueMeta(fieldIndex).getType() == ValueMetaGeometry.TYPE_GEOMETRY) {
          feature.addValue(
              field,
              ((GeometryInterface) rowMeta.getValueMeta(fieldIndex)).getGeometry(r[fieldIndex]));
        } else {
          feature.addValue(field, rowMeta.getString(r, fieldIndex));
        }
      }
    }

    return feature;
  }

  public static IRowMeta getRowMeta(List<Field> fields, String origin) {

    RowMeta rowMeta = new RowMeta();

    for (Field field : fields) {

      IValueMeta valueMeta = null;

      if (field.getType().equals(FieldType.GEOMETRY)) {

        valueMeta = new ValueMetaGeometry(field.getName());

      } else if (field.getType().equals(FieldType.BOOLEAN)) {

        valueMeta = new ValueMetaBoolean(field.getName());

      } else if (field.getType().equals(FieldType.DATE)) {

        valueMeta = new ValueMetaDate(field.getName());

      } else if (field.getType().equals(FieldType.DOUBLE)) {

        valueMeta = new ValueMetaNumber(field.getName());

      } else if (field.getType().equals(FieldType.LONG)) {

        valueMeta = new ValueMetaInteger(field.getName());

      } else {

        valueMeta = new ValueMetaString(field.getName());
      }

      if (field.getLength() != null) {
        valueMeta.setLength(field.getLength());
      }
      if (field.getDecimalCount() != null) {
        valueMeta.setPrecision(field.getDecimalCount());
      }

      valueMeta.setOrigin(origin);
      rowMeta.addValueMeta(valueMeta);
    }

    return rowMeta;
  }

  public static Object[] getRow(IRowMeta rowMeta, Feature feature) throws HopValueException {

    Object[] row = new Object[rowMeta.size()];
    for (Field field : getFields(rowMeta)) {

      int fieldIndex = rowMeta.indexOfValue(field.getName());
      IValueMeta valueMeta = rowMeta.getValueMeta(fieldIndex);
      Object value = null;

      Object featureValue = feature.getValue(field);
      if (featureValue != null) {

        if (field.getType().equals(FieldType.GEOMETRY)) {

          value = featureValue;

        } else if (field.getType().equals(FieldType.BOOLEAN)) {

          value = valueMeta.getBoolean(featureValue);

        } else if (field.getType().equals(FieldType.DATE)) {

          value = valueMeta.getDate(featureValue);

        } else if (field.getType().equals(FieldType.DOUBLE)) {

          value = valueMeta.getNumber(Double.parseDouble(String.valueOf(featureValue)));

        } else if (field.getType().equals(FieldType.LONG)) {

          value = valueMeta.getInteger(Long.parseLong(String.valueOf(featureValue)));

        } else {
          value = valueMeta.getString(String.valueOf(featureValue));
        }
      }

      row[fieldIndex] = value;
    }

    return row;
  }
}
