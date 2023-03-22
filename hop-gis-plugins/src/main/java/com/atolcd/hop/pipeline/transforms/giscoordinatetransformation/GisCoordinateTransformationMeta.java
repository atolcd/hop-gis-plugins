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

import com.atolcd.hop.core.row.value.ValueMetaGeometry;
import java.util.List;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.eclipse.swt.widgets.Shell;

@Transform(
    id = "GisCoordinateTransformation",
    name = "i18n::GisCoordinateTransformation.Shell.Name",
    description = "i18n::GisCoordinateTransformation.Shell.Description",
    image = "GisCoordinateTransformation.svg",
    categoryDescription = "i18n::GisCoordinateTransformation.Shell.CategoryDescription",
    documentationUrl = "",
    keywords = "i18n::GisCoordinateTransformation.keywords")
public class GisCoordinateTransformationMeta
    extends BaseTransformMeta<GisCoordinateTransformation, GisCoordinateTransformationData> {

  private static final Class<?> PKG = GisCoordinateTransformationMeta.class; // Needed by Translator

  /** Colonne contenant la géométrie */
  @HopMetadataProperty private String geometryFieldName;

  /** Nom de la colonne contenant la géométrie après opération */
  @HopMetadataProperty private String outputGeometryFieldName;

  /** Autorité du CRS d'entrée */
  @HopMetadataProperty private String inputCRSAuthority;

  /** Code du CRS d'entrée */
  @HopMetadataProperty private String inputCRSCode;

  /** Autorité du CRS de sortie */
  @HopMetadataProperty private String outputCRSAuthority;

  /** Code du CRS de sortie */
  @HopMetadataProperty private String outputCRSCode;

  /** Utiliser le système de projection associé à la géométrie pour la reprojection */
  @HopMetadataProperty private boolean crsFromGeometry; //

  /** Opération à réaliser : Assignation de SRID ou reprojection */
  @HopMetadataProperty private String crsOperation;

  public GisCoordinateTransformationMeta() {
    this.crsOperation = "ASSIGN";
    this.crsFromGeometry = false;
  }

  public String getGeometryFieldName() {
    return geometryFieldName;
  }

  public void setGeometryFieldName(String geometryFieldName) {
    this.geometryFieldName = geometryFieldName;
  }

  public String getInputCRSCode() {
    return inputCRSCode;
  }

  public void setInputCRSCode(String inputCRSCode) {
    this.inputCRSCode = inputCRSCode;
  }

  public String getOutputCRSCode() {
    return outputCRSCode;
  }

  public void setOutputCRSCode(String outputCRSCode) {
    this.outputCRSCode = outputCRSCode;
  }

  public String getInputCRSAuthority() {
    return inputCRSAuthority;
  }

  public void setInputCRSAuthority(String inputCRSAuthority) {
    this.inputCRSAuthority = inputCRSAuthority;
  }

  public String getOutputCRSAuthority() {
    return outputCRSAuthority;
  }

  public void setOutputCRSAuthority(String outputCRSAuthority) {
    this.outputCRSAuthority = outputCRSAuthority;
  }

  public boolean isCrsFromGeometry() {
    return crsFromGeometry;
  }

  public void setCrsFromGeometry(boolean crsFromGeometry) {
    this.crsFromGeometry = crsFromGeometry;
  }

  public String getCrsOperation() {
    return crsOperation;
  }

  public void setCrsOperation(String crsOperation) {
    this.crsOperation = crsOperation;
  }

  public String getOutputGeometryFieldName() {
    return outputGeometryFieldName;
  }

  public void setOutputGeometryFieldName(String outputGeometryFieldName) {
    this.outputGeometryFieldName = outputGeometryFieldName;
  }

  @Override
  public void getFields(
      IRowMeta r,
      String origin,
      IRowMeta[] info,
      TransformMeta nextStep,
      IVariables space,
      IHopMetadataProvider metadataProvider) {

    IValueMeta valueMeta = new ValueMetaGeometry(outputGeometryFieldName);
    valueMeta.setOrigin(origin);
    r.addValueMeta(valueMeta);
  }

  public Object clone() {

    Object retval = super.clone();
    return retval;
  }

  @Override
  public void check(
      List<ICheckResult> remarks,
      PipelineMeta transmeta,
      TransformMeta stepMeta,
      IRowMeta prev,
      String input[],
      String output[],
      IRowMeta info,
      IVariables variables,
      IHopMetadataProvider metadataProvider) {

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
