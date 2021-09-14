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

import com.atolcd.hop.core.row.value.ValueMetaGeometry;
import java.util.List;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.Const;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopXmlException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.w3c.dom.Node;

@Transform(
    id = "GisGroupBy",
    name = "i18n::GisGroupBy.Shell.Name",
    description = "i18n::GisGroupBy.Shell.Description",
    image = "GisGroupBy.png",
    categoryDescription = "i18n::GisGroupBy.Shell.CategoryDescription",
    documentationUrl = "")
public class GisGroupByMeta extends BaseTransformMeta
    implements ITransformMeta<GisGroupBy, GisGroupByData> {

  private static Class<?> PKG = GisGroupByMeta.class;

  public static final int TYPE_GROUP_NONE = 0;

  public static final int TYPE_GROUP_SUM = 1;

  public static final int TYPE_GROUP_AVERAGE = 2;

  public static final int TYPE_GROUP_MEDIAN = 3;

  public static final int TYPE_GROUP_PERCENTILE = 4;

  public static final int TYPE_GROUP_MIN = 5;

  public static final int TYPE_GROUP_MAX = 6;

  public static final int TYPE_GROUP_COUNT_ALL = 7;

  public static final int TYPE_GROUP_CONCAT_COMMA = 8;

  public static final int TYPE_GROUP_FIRST = 9;

  public static final int TYPE_GROUP_LAST = 10;

  public static final int TYPE_GROUP_FIRST_INCL_NULL = 11;

  public static final int TYPE_GROUP_LAST_INCL_NULL = 12;

  public static final int TYPE_GROUP_CUMULATIVE_SUM = 13;

  public static final int TYPE_GROUP_CUMULATIVE_AVERAGE = 14;

  public static final int TYPE_GROUP_STANDARD_DEVIATION = 15;

  public static final int TYPE_GROUP_CONCAT_STRING = 16;

  public static final int TYPE_GROUP_COUNT_DISTINCT = 17;

  public static final int TYPE_GROUP_COUNT_ANY = 18;

  // GIS : Ajout d'opérateurs d'aggrégation
  public static final int TYPE_GROUP_GEOMETRY_UNION = 19;
  public static final int TYPE_GROUP_GEOMETRY_EXTENT = 20;
  public static final int TYPE_GROUP_GEOMETRY_AGG = 21;
  public static final int TYPE_GROUP_GEOMETRY_DISSOLVE = 22;

  // GIS : Ajout d'opérateurs d'aggrégation
  public static final String[] typeGroupCode = /*
                                                  * WARNING: DO NOT TRANSLATE
                                                  * THIS. WE ARE SERIOUS, DON'T
                                                  * TRANSLATE!
                                                  */ {
    "-",
    "SUM",
    "AVERAGE",
    "MEDIAN",
    "PERCENTILE",
    "MIN",
    "MAX",
    "COUNT_ALL",
    "CONCAT_COMMA",
    "FIRST",
    "LAST",
    "FIRST_INCL_NULL",
    "LAST_INCL_NULL",
    "CUM_SUM",
    "CUM_AVG",
    "STD_DEV",
    "CONCAT_STRING",
    "COUNT_DISTINCT",
    "COUNT_ANY",
    "GEOMETRY_UNION",
    "GEOMETRY_EXTENT",
    "GEOMETRY_AGG",
    "GEOMETRY_DISSOLVE"
  };

  public static final String[] typeGroupLongDesc = {
    "-",
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.SUM"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.AVERAGE"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.MEDIAN"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.PERCENTILE"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.MIN"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.MAX"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.CONCAT_ALL"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.CONCAT_COMMA"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.FIRST"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.LAST"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.FIRST_INCL_NULL"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.LAST_INCL_NULL"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.CUMUMALTIVE_SUM"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.CUMUMALTIVE_AVERAGE"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.STANDARD_DEVIATION"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.CONCAT_STRING"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.COUNT_DISTINCT"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.COUNT_ANY"),
    // GIS : Ajout d'opérateurs d'aggrégation
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.GEOMETRY_UNION"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.GEOMETRY_EXTENT"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.GEOMETRY_AGG"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.GEOMETRY_DISSOLVE")
  };

  /** All rows need to pass, adding an extra row at the end of each group/block. */
  private boolean passAllRows;

  /** Directory to store the temp files */
  private String directory;

  /** Temp files prefix... */
  private String prefix;

  /** Indicate that some rows don't need to be considered : TODO: make work in GUI & worker */
  private boolean aggregateIgnored;

  /**
   * name of the boolean field that indicates we need to ignore the row : TODO: make work in GUI &
   * worker
   */
  private String aggregateIgnoredField;

  /** Fields to group over */
  private String[] groupField;

  /** Name of aggregate field */
  private String[] aggregateField;

  /** Field name to group over */
  private String[] subjectField;

  /** Type of aggregate */
  private int[] aggregateType;

  /** Value to use as separator for ex */
  private String[] valueField;

  /** Add a linenr in the group, resetting to 0 in a new group. */
  private boolean addingLineNrInGroup;

  /** The fieldname that will contain the added integer field */
  private String lineNrInGroupField;

  /**
   * Flag to indicate that we always give back one row. Defaults to true for existing
   * transformations.
   */
  private boolean alwaysGivingBackOneRow;

  public GisGroupByMeta() {
    super(); // allocate BaseStepMeta
  }

  /** @return Returns the aggregateField. */
  public String[] getAggregateField() {
    return aggregateField;
  }

  /** @param aggregateField The aggregateField to set. */
  public void setAggregateField(String[] aggregateField) {
    this.aggregateField = aggregateField;
  }

  /** @return Returns the aggregateIgnored. */
  public boolean isAggregateIgnored() {
    return aggregateIgnored;
  }

  /** @param aggregateIgnored The aggregateIgnored to set. */
  public void setAggregateIgnored(boolean aggregateIgnored) {
    this.aggregateIgnored = aggregateIgnored;
  }

  /** @return Returns the aggregateIgnoredField. */
  public String getAggregateIgnoredField() {
    return aggregateIgnoredField;
  }

  /** @param aggregateIgnoredField The aggregateIgnoredField to set. */
  public void setAggregateIgnoredField(String aggregateIgnoredField) {
    this.aggregateIgnoredField = aggregateIgnoredField;
  }

  /** @return Returns the aggregateType. */
  public int[] getAggregateType() {
    return aggregateType;
  }

  /** @param aggregateType The aggregateType to set. */
  public void setAggregateType(int[] aggregateType) {
    this.aggregateType = aggregateType;
  }

  /** @return Returns the groupField. */
  public String[] getGroupField() {
    return groupField;
  }

  /** @param groupField The groupField to set. */
  public void setGroupField(String[] groupField) {
    this.groupField = groupField;
  }

  /** @return Returns the passAllRows. */
  public boolean passAllRows() {
    return passAllRows;
  }

  /** @param passAllRows The passAllRows to set. */
  public void setPassAllRows(boolean passAllRows) {
    this.passAllRows = passAllRows;
  }

  /** @return Returns the subjectField. */
  public String[] getSubjectField() {
    return subjectField;
  }

  /** @param subjectField The subjectField to set. */
  public void setSubjectField(String[] subjectField) {
    this.subjectField = subjectField;
  }

  /** @return Returns the valueField. */
  public String[] getValueField() {
    return valueField;
  }

  /** @param separatorField The valueField to set. */
  public void setValueField(String[] valueField) {
    this.valueField = valueField;
  }

  @Override
  public void loadXml(Node stepnode, IHopMetadataProvider metaStore) throws HopXmlException {
    readData(stepnode);
  }

  public void allocate(int sizegroup, int nrfields) {
    groupField = new String[sizegroup];
    aggregateField = new String[nrfields];
    subjectField = new String[nrfields];
    aggregateType = new int[nrfields];
    valueField = new String[nrfields];
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  private void readData(Node stepnode) throws HopXmlException {
    try {
      passAllRows = "Y".equalsIgnoreCase(XmlHandler.getTagValue(stepnode, "all_rows"));
      aggregateIgnored = "Y".equalsIgnoreCase(XmlHandler.getTagValue(stepnode, "ignore_aggregate"));
      aggregateIgnoredField = XmlHandler.getTagValue(stepnode, "field_ignore");

      directory = XmlHandler.getTagValue(stepnode, "directory");
      prefix = XmlHandler.getTagValue(stepnode, "prefix");

      addingLineNrInGroup = "Y".equalsIgnoreCase(XmlHandler.getTagValue(stepnode, "add_linenr"));
      lineNrInGroupField = XmlHandler.getTagValue(stepnode, "linenr_fieldname");

      Node groupn = XmlHandler.getSubNode(stepnode, "group");
      Node fields = XmlHandler.getSubNode(stepnode, "fields");

      int sizegroup = XmlHandler.countNodes(groupn, "field");
      int nrfields = XmlHandler.countNodes(fields, "field");

      allocate(sizegroup, nrfields);

      for (int i = 0; i < sizegroup; i++) {
        Node fnode = XmlHandler.getSubNodeByNr(groupn, "field", i);
        groupField[i] = XmlHandler.getTagValue(fnode, "name");
      }

      boolean hasNumberOfValues = false;
      for (int i = 0; i < nrfields; i++) {
        Node fnode = XmlHandler.getSubNodeByNr(fields, "field", i);
        aggregateField[i] = XmlHandler.getTagValue(fnode, "aggregate");
        subjectField[i] = XmlHandler.getTagValue(fnode, "subject");
        aggregateType[i] = getType(XmlHandler.getTagValue(fnode, "type"));

        if (aggregateType[i] == TYPE_GROUP_COUNT_ALL
            || aggregateType[i] == TYPE_GROUP_COUNT_DISTINCT
            || aggregateType[i] == TYPE_GROUP_COUNT_ANY) {
          hasNumberOfValues = true;
        }

        valueField[i] = XmlHandler.getTagValue(fnode, "valuefield");
      }

      String giveBackRow = XmlHandler.getTagValue(stepnode, "give_back_row");
      if (giveBackRow.isEmpty()) {
        alwaysGivingBackOneRow = hasNumberOfValues;
      } else {
        alwaysGivingBackOneRow = "Y".equalsIgnoreCase(giveBackRow);
      }
    } catch (Exception e) {
      throw new HopXmlException(
          BaseMessages.getString(PKG, "GroupByMeta.Exception.UnableToLoadStepInfoFromXML"), e);
    }
  }

  public static final int getType(String desc) {
    for (int i = 0; i < typeGroupCode.length; i++) {
      if (typeGroupCode[i].equalsIgnoreCase(desc)) {
        return i;
      }
    }
    for (int i = 0; i < typeGroupLongDesc.length; i++) {
      if (typeGroupLongDesc[i].equalsIgnoreCase(desc)) {
        return i;
      }
    }
    return 0;
  }

  public static final String getTypeDesc(int i) {
    if (i < 0 || i >= typeGroupCode.length) {
      return null;
    }
    return typeGroupCode[i];
  }

  public static final String getTypeDescLong(int i) {
    if (i < 0 || i >= typeGroupLongDesc.length) {
      return null;
    }
    return typeGroupLongDesc[i];
  }

  public void setDefault() {
    directory = "%%java.io.tmpdir%%";
    prefix = "grp";

    passAllRows = false;
    aggregateIgnored = false;
    aggregateIgnoredField = null;

    int sizegroup = 0;
    int nrfields = 0;

    allocate(sizegroup, nrfields);
  }

  @Override
  public void getFields(
      IRowMeta r,
      String origin,
      IRowMeta[] info,
      TransformMeta nextStep,
      IVariables space,
      IHopMetadataProvider metaStore) {
    // re-assemble a new row of metadata
    //
    IRowMeta fields = new RowMeta();

    if (!passAllRows) {
      // Add the grouping fields in the correct order...
      //
      for (int i = 0; i < groupField.length; i++) {
        IValueMeta valueMeta = r.searchValueMeta(groupField[i]);
        if (valueMeta != null) {
          fields.addValueMeta(valueMeta);
        }
      }
    } else {
      // Add all the original fields from the incoming row meta
      //
      fields.addRowMeta(r);
    }

    // Re-add aggregates
    //
    for (int i = 0; i < subjectField.length; i++) {
      IValueMeta subj = r.searchValueMeta(subjectField[i]);
      if (subj != null || aggregateType[i] == TYPE_GROUP_COUNT_ANY) {
        String value_name = aggregateField[i];
        int value_type = IValueMeta.TYPE_NONE;
        int length = -1;
        int precision = -1;

        switch (aggregateType[i]) {
          case TYPE_GROUP_SUM:
          case TYPE_GROUP_AVERAGE:
          case TYPE_GROUP_CUMULATIVE_SUM:
          case TYPE_GROUP_CUMULATIVE_AVERAGE:
          case TYPE_GROUP_FIRST:
          case TYPE_GROUP_LAST:
          case TYPE_GROUP_FIRST_INCL_NULL:
          case TYPE_GROUP_LAST_INCL_NULL:
          case TYPE_GROUP_MIN:
          case TYPE_GROUP_MAX:
            value_type = subj.getType();
            break;
          case TYPE_GROUP_COUNT_DISTINCT:
          case TYPE_GROUP_COUNT_ANY:
          case TYPE_GROUP_COUNT_ALL:
            value_type = IValueMeta.TYPE_INTEGER;
            break;
          case TYPE_GROUP_CONCAT_COMMA:
            value_type = IValueMeta.TYPE_STRING;
            break;
          case TYPE_GROUP_STANDARD_DEVIATION:
          case TYPE_GROUP_MEDIAN:
            value_type = IValueMeta.TYPE_NUMBER;
            break;
          case TYPE_GROUP_CONCAT_STRING:
            value_type = IValueMeta.TYPE_STRING;
            break;

            // GIS : Ajout d'opérateurs d'aggrégation
          case TYPE_GROUP_GEOMETRY_UNION:
          case TYPE_GROUP_GEOMETRY_EXTENT:
          case TYPE_GROUP_GEOMETRY_AGG:
          case TYPE_GROUP_GEOMETRY_DISSOLVE:
            value_type = ValueMetaGeometry.TYPE_GEOMETRY;
            break;

          default:
            break;
        }

        // Change type from integer to number in case off averages for
        // cumulative average
        //
        if (aggregateType[i] == TYPE_GROUP_CUMULATIVE_AVERAGE
            && value_type == IValueMeta.TYPE_INTEGER) {
          value_type = IValueMeta.TYPE_NUMBER;
          precision = -1;
          length = -1;
        } else if (aggregateType[i] == TYPE_GROUP_COUNT_ALL
            || aggregateType[i] == TYPE_GROUP_COUNT_DISTINCT
            || aggregateType[i] == TYPE_GROUP_COUNT_ANY) {
          length = IValueMeta.DEFAULT_INTEGER_LENGTH;
          precision = 0;
        } else if (aggregateType[i] == TYPE_GROUP_SUM
            && value_type != IValueMeta.TYPE_INTEGER
            && value_type != IValueMeta.TYPE_NUMBER
            && value_type != IValueMeta.TYPE_BIGNUMBER) {
          // If it ain't numeric, we change it to Number
          //
          value_type = IValueMeta.TYPE_NUMBER;
          precision = -1;
          length = -1;
        }

        if (value_type != IValueMeta.TYPE_NONE) {
          IValueMeta v = new ValueMetaBase(value_name, value_type);
          v.setOrigin(origin);
          v.setLength(length, precision);
          fields.addValueMeta(v);
        }
      }
    }

    if (passAllRows) {
      // If we pass all rows, we can add a line nr in the group...
      if (addingLineNrInGroup && !lineNrInGroupField.isEmpty()) {
        IValueMeta lineNr = new ValueMetaBase(lineNrInGroupField, IValueMeta.TYPE_INTEGER);
        lineNr.setLength(IValueMeta.DEFAULT_INTEGER_LENGTH, 0);
        lineNr.setOrigin(origin);
        fields.addValueMeta(lineNr);
      }
    }

    // Now that we have all the fields we want, we should clear the original
    // row and replace the values...
    //
    r.clear();
    r.addRowMeta(fields);
  }

  @Override
  public String getXml() {
    StringBuffer retval = new StringBuffer(500);

    retval.append("      ").append(XmlHandler.addTagValue("all_rows", passAllRows));
    retval.append("      ").append(XmlHandler.addTagValue("ignore_aggregate", aggregateIgnored));
    retval.append("      ").append(XmlHandler.addTagValue("field_ignore", aggregateIgnoredField));
    retval.append("      ").append(XmlHandler.addTagValue("directory", directory));
    retval.append("      ").append(XmlHandler.addTagValue("prefix", prefix));
    retval.append("      ").append(XmlHandler.addTagValue("add_linenr", addingLineNrInGroup));
    retval.append("      ").append(XmlHandler.addTagValue("linenr_fieldname", lineNrInGroupField));
    retval.append("      ").append(XmlHandler.addTagValue("give_back_row", alwaysGivingBackOneRow));

    retval.append("      <group>").append(Const.CR);
    for (int i = 0; i < groupField.length; i++) {
      retval.append("        <field>").append(Const.CR);
      retval.append("          ").append(XmlHandler.addTagValue("name", groupField[i]));
      retval.append("        </field>").append(Const.CR);
    }
    retval.append("      </group>").append(Const.CR);

    retval.append("      <fields>").append(Const.CR);
    for (int i = 0; i < subjectField.length; i++) {
      retval.append("        <field>").append(Const.CR);
      retval.append("          ").append(XmlHandler.addTagValue("aggregate", aggregateField[i]));
      retval.append("          ").append(XmlHandler.addTagValue("subject", subjectField[i]));
      retval
          .append("          ")
          .append(XmlHandler.addTagValue("type", getTypeDesc(aggregateType[i])));
      retval.append("          ").append(XmlHandler.addTagValue("valuefield", valueField[i]));
      retval.append("        </field>").append(Const.CR);
    }
    retval.append("      </fields>").append(Const.CR);

    return retval.toString();
  }

  public void check(
      List<ICheckResult> remarks,
      PipelineMeta transMeta,
      TransformMeta stepMeta,
      IRowMeta prev,
      String[] input,
      String[] output,
      IRowMeta info,
      IVariables space,
      IHopMetadataProvider metaStore) {
    CheckResult cr;

    if (input.length > 0) {
      cr =
          new CheckResult(
              ICheckResult.TYPE_RESULT_OK,
              BaseMessages.getString(PKG, "GroupByMeta.CheckResult.ReceivingInfoOK"),
              stepMeta);
      remarks.add(cr);
    } else {
      cr =
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR,
              BaseMessages.getString(PKG, "GroupByMeta.CheckResult.NoInputError"),
              stepMeta);
      remarks.add(cr);
    }
  }

  /** @return Returns the directory. */
  public String getDirectory() {
    return directory;
  }

  /** @param directory The directory to set. */
  public void setDirectory(String directory) {
    this.directory = directory;
  }

  /** @return Returns the prefix. */
  public String getPrefix() {
    return prefix;
  }

  /** @param prefix The prefix to set. */
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  /** @return the addingLineNrInGroup */
  public boolean isAddingLineNrInGroup() {
    return addingLineNrInGroup;
  }

  /** @param addingLineNrInGroup the addingLineNrInGroup to set */
  public void setAddingLineNrInGroup(boolean addingLineNrInGroup) {
    this.addingLineNrInGroup = addingLineNrInGroup;
  }

  /** @return the lineNrInGroupField */
  public String getLineNrInGroupField() {
    return lineNrInGroupField;
  }

  /** @param lineNrInGroupField the lineNrInGroupField to set */
  public void setLineNrInGroupField(String lineNrInGroupField) {
    this.lineNrInGroupField = lineNrInGroupField;
  }

  /** @return the alwaysGivingBackOneRow */
  public boolean isAlwaysGivingBackOneRow() {
    return alwaysGivingBackOneRow;
  }

  /** @param alwaysGivingBackOneRow the alwaysGivingBackOneRow to set */
  public void setAlwaysGivingBackOneRow(boolean alwaysGivingBackOneRow) {
    this.alwaysGivingBackOneRow = alwaysGivingBackOneRow;
  }

  @Override
  public ITransform createTransform(
      TransformMeta transformMeta,
      GisGroupByData data,
      int cnr,
      PipelineMeta pipelineMeta,
      Pipeline pipeline) {
    return new GisGroupBy(transformMeta, this, data, cnr, pipelineMeta, pipeline);
  }

  @Override
  public GisGroupByData getTransformData() {
    return new GisGroupByData();
  }
}
