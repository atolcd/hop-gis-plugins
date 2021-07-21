package org.atolcd.hop.pipeline.transforms.giscoordinatetransformation;

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

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.Counter;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopXmlException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.metadata.api.IHopMetadataProvider;

import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformData;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.w3c.dom.Node;

import org.atolcd.hop.core.row.value.ValueMetaGeometry;
import org.atolcd.hop.pipeline.transforms.giscoordinatetransformation.GisCoordinateTransformationDialog;

@Transform(	id = "GisCoordinateTransformation",
			name = "i18n::GisCoordinateTransformation.Shell.Name",
			description = "i18n::GisCoordinateTransformation.Shell.Description",
			image = "GisCoordinateTransformation.svg",
			categoryDescription = "i18n::GisCoordinateTransformation.Shell.CategoryDescription",
			documentationUrl = ""
			)

public class GisCoordinateTransformationMeta extends BaseTransformMeta 
implements ITransformMeta<GisCoordinateTransformation,GisCoordinateTransformationData> {

	private static final Class<?> PKG = GisCoordinateTransformationMeta.class; // Needed by Translator
	
    private String geometryFieldName; // Colonne contenant la géométrie
    private String outputGeometryFieldName; // Nom de la colonne contenant la
                                            // géométrie après opération
    private String inputCRSAuthority; // Autorité du CRS d'entrée
    private String inputCRSCode; // Code du CRS d'entrée
    private String outputCRSAuthority; // Autorité du CRS de sortie
    private String outputCRSCode; // Code du CRS de sortie
    private boolean crsFromGeometry; // Utiliser le système de projection
                                     // associé à la géométrie pour la
                                     // reprojection
    private String crsOperation; // Opération à réaliser : Assignation de SRID
                                 // ou reprojection

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
    public String getXml() {

        StringBuffer retval = new StringBuffer();
        retval.append("    " + XmlHandler.addTagValue("crsOperation", crsOperation));
        retval.append("    " + XmlHandler.addTagValue("geometryFieldName", geometryFieldName));
        retval.append("    " + XmlHandler.addTagValue("outputGeometryFieldName", outputGeometryFieldName));
        retval.append("    " + XmlHandler.addTagValue("inputCRSAuthority", inputCRSAuthority));
        retval.append("    " + XmlHandler.addTagValue("inputCRSCode", inputCRSCode));
        retval.append("    " + XmlHandler.addTagValue("outputCRSAuthority", outputCRSAuthority));
        retval.append("    " + XmlHandler.addTagValue("outputCRSCode", outputCRSCode));
        retval.append("    " + XmlHandler.addTagValue("crsFromGeometry", crsFromGeometry));
        return retval.toString();

    }

    @Override
    public void getFields(IRowMeta r, String origin, IRowMeta[] info, TransformMeta nextStep, IVariables space,
    		IHopMetadataProvider metadataProvider) 
    {

        IValueMeta valueMeta = new ValueMetaGeometry(outputGeometryFieldName);
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

            crsOperation = XmlHandler.getTagValue(stepnode, "crsOperation");
            geometryFieldName = XmlHandler.getTagValue(stepnode, "geometryFieldName");
            outputGeometryFieldName = XmlHandler.getTagValue(stepnode, "outputGeometryFieldName");
            inputCRSAuthority = XmlHandler.getTagValue(stepnode, "inputCRSAuthority");
            inputCRSCode = XmlHandler.getTagValue(stepnode, "inputCRSCode");
            outputCRSAuthority = XmlHandler.getTagValue(stepnode, "outputCRSAuthority");
            outputCRSCode = XmlHandler.getTagValue(stepnode, "outputCRSCode");
            crsFromGeometry = "Y".equalsIgnoreCase(XmlHandler.getTagValue(stepnode, "crsFromGeometry"));

        } catch (Exception e) {
            throw new HopXmlException("Unable to read step info from XML node", e);
        }

    }

    public void setDefault() {
        this.crsOperation = "ASSIGN";
        this.crsFromGeometry = false;

    }

    @Override
    public void check(List<ICheckResult> remarks, PipelineMeta transmeta, TransformMeta stepMeta, IRowMeta prev, String input[], String output[],
    		IRowMeta info, IVariables variables, IHopMetadataProvider metadataProvider) {

        CheckResult cr;

        if (input.length > 0) {

            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta);
            remarks.add(cr);

        } else {

            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps.", stepMeta);
            remarks.add(cr);

        }

    }

    public ITransformDialog getDialog(Shell shell,IVariables variables, ITransformMeta meta, PipelineMeta transMeta, String name) {
        return new GisCoordinateTransformationDialog(shell, variables, meta, transMeta, name);
    }


	@Override
	public ITransform createTransform(TransformMeta transformMeta, GisCoordinateTransformationData data, int cnr,
			PipelineMeta pipelineMeta, Pipeline pipeline) {
		return new GisCoordinateTransformation(transformMeta, this, data, cnr, pipelineMeta, pipeline);
	}

	@Override
	public GisCoordinateTransformationData getTransformData() {
		return new GisCoordinateTransformationData();
	}
}
