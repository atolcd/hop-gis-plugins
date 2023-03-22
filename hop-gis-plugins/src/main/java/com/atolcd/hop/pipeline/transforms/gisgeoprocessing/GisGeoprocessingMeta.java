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
import com.atolcd.hop.pipeline.transforms.giscoordinatetransformation.GisCoordinateTransformationDialog;
import java.util.List;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopXmlException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

@Transform(
    id = "GisGeoprocessing",
    name = "i18n::GisGeoprocessing.Shell.Name",
    description = "i18n::GisGeoprocessing.Shell.Description",
    image = "GisGeoprocessing.png",
    categoryDescription = "i18n::GisGeoprocessing.Shell.CategoryDescription",
    documentationUrl = "",
    keywords = "i18n::GisGeoprocessing.keywords")
public class GisGeoprocessingMeta extends BaseTransformMeta<GisGeoprocessing, GisGeoprocessingData> {

  private String operator;

  // Pour opérateurs avec une seule géométrie
  private static String[] oneGeometryOperators =
      new String[] {
        "BOUNDARY",
        "INTERIOR_POINT",
        "CONVEX_HULL",
        "CONCAVE_HULL",
        "BUFFER",
        "EXTENDED_BUFFER",
        "EXPLODE",
        "REVERSE",
        "DENSIFY",
        "SIMPLIFY",
        "SIMPLIFY_VW",
        "TO_2D_GEOMETRY",
        "TO_MULTI_GEOMETRY",
        "EXTRACT_COORDINATES",
        "EXTRACT_FIRST_COORDINATE",
        "EXTRACT_LAST_COORDINATE",
        "MBR",
        "MBC",
        "CENTROID",
        "LESS_PRECISION",
        "POLYGONIZE",
        "LINEMERGE",
        "REMOVE_HOLES",
        "LARGEST_POLYGON",
        "SMALLEST_POLYGON",
        "LONGEST_LINESTRING",
        "SHORTEST_LINESTRING",
        "LINEAR_REFERENCING"
      };
  private String firstGeometryFieldName;

  // Pour opérateurs avec deux géométries
  private static String[] twoGeometriesOperators =
      new String[] {
        "UNION",
        "DIFFERENCE",
        "INTERSECTION",
        "SYM_DIFFERENCE",
        "SNAP_TO_GEOMETRY",
        "SIMPLIFY_POLYGON",
        "SPLIT"
      };
  private String secondGeometryFieldName;

  // Pour opérateurs avec possibilités de filtrage de géométries hétérogènes
  private static String[] withExtractTypeOperators =
      new String[] {"UNION", "DIFFERENCE", "INTERSECTION", "SYM_DIFFERENCE"};
  private static String[] extractTypes =
      new String[] {"ALL", "PUNTAL_ONLY", "LINEAL_ONLY", "POLYGONAL_ONLY"};
  private String extractType;

  // Filtrage de lignes
  private static String[] returnTypes = new String[] {"ALL", "NOT_NULL"};
  private String returnType;

  // Pour opérateurs avec besoin de distance
  private static String[] withDistanceOperators =
      new String[] {
        "CONCAVE_HULL",
        "BUFFER",
        "EXTENDED_BUFFER",
        "DENSIFY",
        "SIMPLIFY",
        "SIMPLIFY_VW",
        "CONCAVE_HULL",
        "SNAP_TO_GEOMETRY",
        "SIMPLIFY_POLYGON",
        "LESS_PRECISION",
        "REMOVE_HOLES",
        "LINEAR_REFERENCING"
      };
  private boolean dynamicDistance;
  private String distanceFieldName;
  private String distanceValue;

  // Pour EXTENDED_BUFFER
  private static String[] bufferJoinStyles = new String[] {"BEVEL", "MITRE", "ROUND"};
  private static String[] bufferCapStyles = new String[] {"FLAT", "ROUND", "SQUARE"};

  private Integer bufferSegmentsCount;
  private Boolean bufferSingleSide;
  private String bufferCapStyle;
  private String bufferJoinStyle;

  // Géométrie de sortie
  private String outputFieldName;

  public String[] getBufferJoinStyles() {
    return bufferJoinStyles;
  }

  public String[] getBufferCapStyles() {
    return bufferCapStyles;
  }

  public String[] getWithExtractTypeOperators() {
    return withExtractTypeOperators;
  }

  public Integer getBufferSegmentsCount() {
    return bufferSegmentsCount;
  }

  public void setBufferSegmentsCount(Integer bufferSegmentsCount) {
    this.bufferSegmentsCount = bufferSegmentsCount;
  }

  public Boolean getBufferSingleSide() {
    return bufferSingleSide;
  }

  public void setBufferSingleSide(Boolean bufferSingleSide) {
    this.bufferSingleSide = bufferSingleSide;
  }

  public String getBufferCapStyle() {
    return bufferCapStyle;
  }

  public void setBufferCapStyle(String bufferCapStyle) {
    this.bufferCapStyle = bufferCapStyle;
  }

  public String getBufferJoinStyle() {
    return bufferJoinStyle;
  }

  public void setBufferJoinStyle(String bufferJoinStyle) {
    this.bufferJoinStyle = bufferJoinStyle;
  }

  public String[] getOneGeometryOperators() {
    return oneGeometryOperators;
  }

  public String[] getTwoGeometriesOperators() {
    return twoGeometriesOperators;
  }

  public String[] getWithDistanceOperators() {
    return withDistanceOperators;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public String getFirstGeometryFieldName() {
    return firstGeometryFieldName;
  }

  public void setFirstGeometryFieldName(String firstGeometryFieldName) {
    this.firstGeometryFieldName = firstGeometryFieldName;
  }

  public String getSecondGeometryFieldName() {
    return secondGeometryFieldName;
  }

  public void setSecondGeometryFieldName(String secondGeometryFieldName) {
    this.secondGeometryFieldName = secondGeometryFieldName;
  }

  public String getOutputFieldName() {
    return outputFieldName;
  }

  public void setOutputFieldName(String outputFieldName) {
    this.outputFieldName = outputFieldName;
  }

  public String getReturnType() {
    return returnType;
  }

  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }

  public String[] getReturnTypes() {
    return returnTypes;
  }

  public String getDistanceFieldName() {
    return distanceFieldName;
  }

  public void setDistanceFieldName(String distanceFieldName) {
    this.distanceFieldName = distanceFieldName;
  }

  public boolean isDynamicDistance() {
    return dynamicDistance;
  }

  public void setDynamicDistance(boolean dynamicDistance) {
    this.dynamicDistance = dynamicDistance;
  }

  public String getDistanceValue() {
    return distanceValue;
  }

  public void setDistanceValue(String distanceValue) {
    this.distanceValue = distanceValue;
  }

  public String[] getExtractTypes() {
    return extractTypes;
  }

  public String getExtractType() {
    return extractType;
  }

  public void setExtractType(String extractType) {
    this.extractType = extractType;
  }

  @Override
  public String getXml() {

    StringBuffer retval = new StringBuffer();
    retval.append("    " + XmlHandler.addTagValue("operator", operator));
    retval.append("    " + XmlHandler.addTagValue("returnType", returnType));
    retval.append("    " + XmlHandler.addTagValue("extractType", extractType));
    retval.append(
        "    " + XmlHandler.addTagValue("firstGeometryFieldName", firstGeometryFieldName));
    retval.append(
        "    " + XmlHandler.addTagValue("secondGeometryFieldName", secondGeometryFieldName));
    retval.append("    " + XmlHandler.addTagValue("dynamicDistance", dynamicDistance));
    retval.append("    " + XmlHandler.addTagValue("distanceFieldName", distanceFieldName));
    retval.append("    " + XmlHandler.addTagValue("distanceValue", distanceValue));
    retval.append("    " + XmlHandler.addTagValue("outputFieldName", outputFieldName));

    retval.append("    " + XmlHandler.addTagValue("bufferSegmentsCount", bufferSegmentsCount));
    retval.append("    " + XmlHandler.addTagValue("bufferSingleSide", bufferSingleSide));
    retval.append("    " + XmlHandler.addTagValue("bufferCapStyle", bufferCapStyle));
    retval.append("    " + XmlHandler.addTagValue("bufferJoinStyle", bufferJoinStyle));

    return retval.toString();
  }

  @Override
  public void getFields(
      IRowMeta r,
      String origin,
      IRowMeta[] info,
      TransformMeta nextStep,
      IVariables space,
      IHopMetadataProvider metadataProvider) {

    IValueMeta valueMeta = new ValueMetaGeometry(outputFieldName);
    valueMeta.setOrigin(origin);
    r.addValueMeta(valueMeta);
  }

  public Object clone() {

    Object retval = super.clone();
    return retval;
  }

  @Override
  public void loadXml(Node stepnode, IHopMetadataProvider metadataProvider) throws HopXmlException {

    try {

      operator = XmlHandler.getTagValue(stepnode, "operator");
      returnType = XmlHandler.getTagValue(stepnode, "returnType");
      extractType = XmlHandler.getTagValue(stepnode, "extractType");
      firstGeometryFieldName = XmlHandler.getTagValue(stepnode, "firstGeometryFieldName");
      secondGeometryFieldName = XmlHandler.getTagValue(stepnode, "secondGeometryFieldName");
      dynamicDistance = "Y".equalsIgnoreCase(XmlHandler.getTagValue(stepnode, "dynamicDistance"));
      distanceFieldName = XmlHandler.getTagValue(stepnode, "distanceFieldName");
      distanceValue = XmlHandler.getTagValue(stepnode, "distanceValue");
      outputFieldName = XmlHandler.getTagValue(stepnode, "outputFieldName");

      bufferSegmentsCount =
          Integer.parseInt(XmlHandler.getTagValue(stepnode, "bufferSegmentsCount"));
      bufferSingleSide = "Y".equalsIgnoreCase(XmlHandler.getTagValue(stepnode, "bufferSingleSide"));
      bufferCapStyle = XmlHandler.getTagValue(stepnode, "bufferCapStyle");
      bufferJoinStyle = XmlHandler.getTagValue(stepnode, "bufferJoinStyle");

    } catch (Exception e) {
      throw new HopXmlException("Unable to read step info from XML node", e);
    }
  }

  public void setDefault() {
    operator = "CENTROID";
    returnType = "ALL";
    extractType = "ALL";
  }

  public void check(
      List<ICheckResult> remarks,
      PipelineMeta transmeta,
      TransformMeta stepMeta,
      IRowMeta prev,
      String input[],
      String output[],
      IRowMeta info) {

    CheckResult cr;

    if (input.length > 0) {

      cr =
          new CheckResult(
              CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta);
      remarks.add(cr);

    } else {

      cr =
          new CheckResult(
              CheckResult.TYPE_RESULT_ERROR, "No input received from other steps.", stepMeta);
      remarks.add(cr);
    }
  }

  public ITransformDialog getDialog(
      Shell shell, IVariables variables, ITransformMeta meta, PipelineMeta transMeta, String name) {
    return new GisCoordinateTransformationDialog(shell, variables, meta, transMeta, name);
  }

}
