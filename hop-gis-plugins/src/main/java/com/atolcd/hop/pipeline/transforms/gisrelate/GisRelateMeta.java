package com.atolcd.hop.pipeline.transforms.gisrelate;

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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.widgets.Shell;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.Counter;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.exception.HopXmlException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.DatabaseImpact;

import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformData;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.errorhandling.IStream;
import org.apache.hop.resource.IResourceNaming;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.w3c.dom.Node;

import com.atolcd.hop.pipeline.transforms.gisrelate.GisRelateDialog;
import com.atolcd.hop.pipeline.transforms.gisrelate.GisRelate;

@Transform(	id = "GisRelate",
			name = "i18n::GisRelate.Shell.Name",
			description = "i18n::GisRelate.Shell.Description",
			image = "GisRelate.svg",
			categoryDescription = "i18n::GisRelate.Shell.CategoryDescription",
			documentationUrl = ""
			)

public class GisRelateMeta extends BaseTransformMeta implements ITransformMeta<GisRelate, GisRelateData> {

	private static final Class<?> PKG = GisRelateMeta.class; // Needed by Translator
	
    private String operator;

    // Opérateurs avec résultat de type boolean
    private static String[] boolResultOperators = new String[] { "CONTAINS", "COVERED_BY", "COVERS", "CROSSES", "DISJOINT", "EQUALS", "EQUALS_EXACT", "INTERSECTS", "WITHIN",
            "OVERLAPS", "TOUCHES", "IS_WITHIN_DISTANCE", "IS_NOT_WITHIN_DISTANCE" };

    // Opérateurs avec résultat de type numérique
    private static String[] numericResultOperators = new String[] { "DISTANCE_MIN", "DISTANCE_MAX" };

    private String firstGeometryFieldName;
    private String secondGeometryFieldName;

    // Filtrage de lignes
    private static String[] returnTypes = new String[] { "ALL", "FALSE", "TRUE" };
    private String returnType;

    // Pour opérateurs avec besoin de distance
    private static String[] withDistanceOperators = new String[] { "IS_WITHIN_DISTANCE", "IS_NOT_WITHIN_DISTANCE" };
    private boolean dynamicDistance;
    private String distanceFieldName;
    private String distanceValue;

    // Colonne de sortie
    private String outputFieldName;

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getFirstGeometryFieldName() {
        return firstGeometryFieldName;
    }

    public String[] getBoolResultOperators() {
        return boolResultOperators;
    }

    public String[] getNumericResultOperators() {
        return numericResultOperators;
    }

    public String[] getWithDistanceOperators() {
        return withDistanceOperators;
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

    public boolean isDynamicDistance() {
        return dynamicDistance;
    }

    public void setDynamicDistance(boolean dynamicDistance) {
        this.dynamicDistance = dynamicDistance;
    }

    public String getDistanceFieldName() {
        return distanceFieldName;
    }

    public void setDistanceFieldName(String distanceFieldName) {
        this.distanceFieldName = distanceFieldName;
    }

    public String getDistanceValue() {
        return distanceValue;
    }

    public void setDistanceValue(String distanceValue) {
        this.distanceValue = distanceValue;
    }

    @Override
    public String getXml() {

        StringBuffer retval = new StringBuffer();
        retval.append("    " + XmlHandler.addTagValue("operator", operator));
        retval.append("    " + XmlHandler.addTagValue("returnType", returnType));
        retval.append("    " + XmlHandler.addTagValue("firstGeometryFieldName", firstGeometryFieldName));
        retval.append("    " + XmlHandler.addTagValue("secondGeometryFieldName", secondGeometryFieldName));
        retval.append("    " + XmlHandler.addTagValue("dynamicDistance", dynamicDistance));
        retval.append("    " + XmlHandler.addTagValue("distanceFieldName", distanceFieldName));
        retval.append("    " + XmlHandler.addTagValue("distanceValue", distanceValue));
        retval.append("    " + XmlHandler.addTagValue("outputFieldName", outputFieldName));
        return retval.toString();

    }

    @Override
    public void getFields(IRowMeta r, String origin, IRowMeta[] info, TransformMeta nextStep, IVariables space
    		,  IHopMetadataProvider metadataProvider) {

        if (ArrayUtils.contains(numericResultOperators, operator)) {
            IValueMeta valueMeta = (IValueMeta) new ValueMetaBase(outputFieldName, ValueMetaBase.TYPE_NUMBER);
            valueMeta.setOrigin(origin);
            r.addValueMeta(valueMeta);
        }

        if (ArrayUtils.contains(boolResultOperators, operator)) {

            if (returnType.equalsIgnoreCase("ALL")) {
                IValueMeta valueMeta = new ValueMetaBase(outputFieldName, ValueMetaBase.TYPE_BOOLEAN);
                valueMeta.setOrigin(origin);
                r.addValueMeta(valueMeta);
            }
        }

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
            firstGeometryFieldName = XmlHandler.getTagValue(stepnode, "firstGeometryFieldName");
            secondGeometryFieldName = XmlHandler.getTagValue(stepnode, "secondGeometryFieldName");
            dynamicDistance = "Y".equalsIgnoreCase(XmlHandler.getTagValue(stepnode, "dynamicDistance"));
            distanceFieldName = XmlHandler.getTagValue(stepnode, "distanceFieldName");
            distanceValue = XmlHandler.getTagValue(stepnode, "distanceValue");
            outputFieldName = XmlHandler.getTagValue(stepnode, "outputFieldName");

        } catch (Exception e) {
            throw new HopXmlException("Unable to read step info from XML node", e);
        }

    }

    public void setDefault() {
        operator = "CONTAINS";
        returnType = "ALL";
    }


    public ITransformDialog getDialog(Shell shell,IVariables variables, ITransformMeta meta, PipelineMeta transMeta, String name) {
        return new GisRelateDialog(shell, variables, meta, transMeta, name);
    }

	@Override
	public void check(List remarks, PipelineMeta pipelineMeta, TransformMeta transformMeta, IRowMeta prev,
			String[] input, String[] output, IRowMeta info, IVariables variables,
			IHopMetadataProvider metadataProvider) {
		// TODO Auto-generated method stub
        CheckResult cr;

        if (input.length > 0) {

            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", transformMeta);
            remarks.add(cr);

        } else {

            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps.", transformMeta);
            remarks.add(cr);

        }
	}


	@Override
	public GisRelateData getTransformData() {
		return new GisRelateData();
	}

	@Override
	public void analyseImpact(IVariables variables, List impact, PipelineMeta pipelineMeta, TransformMeta transformMeta,
			IRowMeta prev, String[] input, String[] output, IRowMeta info, IHopMetadataProvider metadataProvider)
			throws HopTransformException {
		// TODO Auto-generated method stub

	}

	@Override
	public String exportResources(IVariables variables, Map definitions, IResourceNaming iResourceNaming,
			IHopMetadataProvider metadataProvider) throws HopException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void searchInfoAndTargetTransforms(List transforms) {
		// TODO Verifier si on en a besoin
		List<IStream> infoStreams = getTransformIOMeta().getInfoStreams();
	    for (IStream stream : infoStreams) {
	      stream.setTransformMeta(
	          TransformMeta.findTransform(transforms, (String) stream.getSubject()));
	    }
		
	}

	@Override
	public ITransform createTransform(TransformMeta transformMeta, GisRelateData data, int cnr, PipelineMeta pipelineMeta,
			Pipeline pipeline) {
		return new GisRelate(transformMeta, this, data, cnr, pipelineMeta, pipeline);
	}
}
