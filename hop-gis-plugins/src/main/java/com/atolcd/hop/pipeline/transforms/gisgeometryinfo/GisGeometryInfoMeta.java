package com.atolcd.hop.pipeline.transforms.gisgeometryinfo;

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
import com.atolcd.hop.pipeline.transforms.gisfileinput.GisFileInputDialog;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.Const;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopXmlException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

@Transform(
    id = "GisGeometryInfo",
    name = "i18n::GisGeometryInfo.Shell.Name",
    description = "i18n::GisGeometryInfo.Shell.Description",
    image = "GisGeometryInfo.png",
    categoryDescription = "i18n::GisGeometryInfo.Shell.CategoryDescription",
    documentationUrl = "",
    keywords = "i18n::GisGeometryInfo.keywords")
public class GisGeometryInfoMeta extends BaseTransformMeta<GisGeometryInfo, GisGeometryInfoData> {

  private HashMap<String, Integer> infosTypes;
  private String geometryFieldName;
  private LinkedHashMap<String, String> outputFields;

  public GisGeometryInfoMeta() {

    super();
    this.infosTypes = new HashMap<String, Integer>();
    this.outputFields = new LinkedHashMap<String, String>();

    this.infosTypes.put("NULL_OR_EMPTY", ValueMetaBase.TYPE_BOOLEAN);
    this.infosTypes.put("AREA", ValueMetaBase.TYPE_NUMBER);
    this.infosTypes.put("LENGTH", ValueMetaBase.TYPE_NUMBER);
    this.infosTypes.put("DIMENSION", ValueMetaBase.TYPE_INTEGER);
    this.infosTypes.put("SRID", ValueMetaBase.TYPE_INTEGER);
    this.infosTypes.put("GEOMETRY_TYPE", ValueMetaBase.TYPE_STRING);
    this.infosTypes.put("GEOMETRY_COUNT", ValueMetaBase.TYPE_INTEGER);
    this.infosTypes.put("GEOMETRY_VERTEX_COUNT", ValueMetaBase.TYPE_INTEGER);
    this.infosTypes.put("X_MIN", ValueMetaBase.TYPE_NUMBER);
    this.infosTypes.put("Y_MIN", ValueMetaBase.TYPE_NUMBER);
    this.infosTypes.put("Z_MIN", ValueMetaBase.TYPE_NUMBER);
    this.infosTypes.put("X_MAX", ValueMetaBase.TYPE_NUMBER);
    this.infosTypes.put("Y_MAX", ValueMetaBase.TYPE_NUMBER);
    this.infosTypes.put("Z_MAX", ValueMetaBase.TYPE_NUMBER);
  }

  public HashMap<String, Integer> getInfosTypes() {
    return infosTypes;
  }

  public void setInfosTypes(HashMap<String, Integer> infosTypes) {
    this.infosTypes = infosTypes;
  }

  public String getGeometryFieldName() {
    return geometryFieldName;
  }

  public void setGeometryFieldName(String geometryFieldName) {
    this.geometryFieldName = geometryFieldName;
  }

  public LinkedHashMap<String, String> getOutputFields() {
    return outputFields;
  }

  public void setOutputFields(LinkedHashMap<String, String> outputFields) {
    this.outputFields = outputFields;
  }

  @Override
  public String getXml() {

    StringBuffer retval = new StringBuffer();

    retval.append("    " + XmlHandler.addTagValue("geometryFieldName", geometryFieldName));

    retval.append("\t<outputs>").append(Const.CR);
    for (Entry<String, String> output : outputFields.entrySet()) {

      String key = output.getKey();
      String value = output.getValue();

      retval.append("\t\t<output>").append(Const.CR);
      retval.append("\t\t\t").append(XmlHandler.addTagValue("infoKey", key));
      retval.append("\t\t\t").append(XmlHandler.addTagValue("infoFieldname", value));
      retval.append("\t\t</output>").append(Const.CR);
    }

    retval.append("\t</outputs>").append(Const.CR);

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

    for (Entry<String, String> output : outputFields.entrySet()) {

      String fieldName = output.getValue();
      int valueMetaType = infosTypes.get(output.getKey());

      IValueMeta valueMeta = null;

      if (valueMetaType == ValueMetaGeometry.TYPE_GEOMETRY) {
        valueMeta = new ValueMetaGeometry(fieldName);
      } else {
        valueMeta = new ValueMetaBase(fieldName, valueMetaType);
      }

      valueMeta.setOrigin(origin);
      r.addValueMeta(valueMeta);
    }
  }

  public Object clone() {

    Object retval = super.clone();
    return retval;
  }

  @Override
  public void loadXml(Node stepnode, IHopMetadataProvider metadataProvider) throws HopXmlException {

    try {

      geometryFieldName = XmlHandler.getTagValue(stepnode, "geometryFieldName");
      Node outputsNode = XmlHandler.getSubNode(stepnode, "outputs");
      for (int i = 0; i < XmlHandler.countNodes(outputsNode, "output"); i++) {

        Node outputNode = XmlHandler.getSubNodeByNr(outputsNode, "output", i);
        String key = XmlHandler.getTagValue(outputNode, "infoKey");
        String value = XmlHandler.getTagValue(outputNode, "infoFieldname");

        outputFields.put(key, value);
      }

    } catch (Exception e) {
      throw new HopXmlException("Unable to read step info from XML node", e);
    }
  }

  public void setDefault() {}

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
    return new GisFileInputDialog(shell, variables, meta, transMeta, name);
  }

}
