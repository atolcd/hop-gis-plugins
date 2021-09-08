package com.atolcd.hop.pipeline.transforms.gisfileinput;

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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.Const;
import org.apache.hop.core.Counter;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopXmlException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.xml.XmlHandler;

import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformData;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.w3c.dom.Node;

import com.atolcd.hop.gis.io.AbstractFileReader;
import com.atolcd.hop.gis.io.DXFReader;
import com.atolcd.hop.gis.io.GPXReader;
import com.atolcd.hop.gis.io.GeoJSONReader;
import com.atolcd.hop.gis.io.GeoPackageReader;
import com.atolcd.hop.gis.io.MapInfoReader;
import com.atolcd.hop.gis.io.ShapefileReader;
import com.atolcd.hop.gis.io.SpatialiteReader;
import com.atolcd.hop.gis.io.features.FeatureConverter;
import com.atolcd.hop.pipeline.transforms.gisfileinput.GisFileInputDialog;

@Transform(	id = "GisFileInput",
			name = "i18n::GisFileInput.Shell.Name", 
			description = "i18n::GisFileInput.Shell.Description",
			image = "GisFileInput.png", 
			categoryDescription = "i18n::GisFileInput.Shell.CategoryDescription", 
			documentationUrl = ""
		)

public class GisFileInputMeta extends BaseTransformMeta implements ITransformMeta<GisFileInput,GisFileInputData> {

	private static Class<?> PKG = GisFileInputMeta.class;
	
    private HashMap<String, GisInputFormatDef> inputFormatDefs;

    private String inputFormat;
    private List<GisInputFormatParameter> inputFormatParameters;
    private String inputFileName;
    private String geometryFieldName;
    private String encoding;
    private Long rowLimit;

    public GisFileInputMeta() {
        super();
        
        this.inputFormatDefs = new HashMap<String, GisInputFormatDef>();
        this.inputFormatParameters = new ArrayList<GisInputFormatParameter>();

        // ESRI Shapefile
        GisInputFormatDef shpDef = new GisInputFormatDef("ESRI_SHP", new String[] { "*.shp;*.SHP" }, new String[] { "*.shp" });
        shpDef.addParameterDef("FORCE_TO_2D", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList(new String[] { "TRUE", "FALSE" }), "TRUE");
        shpDef.addParameterDef("FORCE_TO_MULTIGEOMETRY", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList(new String[] { "TRUE", "FALSE" }), "FALSE");
        this.inputFormatDefs.put("ESRI_SHP", shpDef);

        // GeoJSON
        GisInputFormatDef geojsonDef = new GisInputFormatDef("GEOJSON", new String[] { "*.geojson;*.GEOJSON", "*.json;*.JSON" }, new String[] { "*.geojson", "*.json" });
        geojsonDef.addParameterDef("FORCE_TO_MULTIGEOMETRY", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList(new String[] { "TRUE", "FALSE" }), "FALSE");
        this.inputFormatDefs.put("GEOJSON", geojsonDef);

        // Mapinfo MIF/MID
        GisInputFormatDef mapinfoDef = new GisInputFormatDef("MAPINFO_MIF", new String[] { "*.mif;*.MIF" }, new String[] { "*.mif" });
        mapinfoDef.addParameterDef("FORCE_TO_MULTIGEOMETRY", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList(new String[] { "TRUE", "FALSE" }), "FALSE");
        this.inputFormatDefs.put("MAPINFO_MIF", mapinfoDef);

        // SpatialLite
        GisInputFormatDef sqlLiteDef = new GisInputFormatDef("SPATIALITE", new String[] { "*.db;*.DB", "*.sqlite;*.SQLITE" }, new String[] { "*.db", "*.sqlite" });
        sqlLiteDef.addParameterDef("DB_TABLE_NAME", ValueMetaBase.TYPE_STRING, true);
        this.inputFormatDefs.put("SPATIALITE", sqlLiteDef);

        // DXF
        GisInputFormatDef dxfDef = new GisInputFormatDef("DXF", new String[] { "*.dxf;*.DXF" }, new String[] { "*.dxf" });
        dxfDef.addParameterDef("FORCE_TO_MULTIGEOMETRY", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList(new String[] { "TRUE", "FALSE" }), "FALSE");
        dxfDef.addParameterDef("READ_XDATA", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList(new String[] { "TRUE", "FALSE" }), "FALSE");
        dxfDef.addParameterDef("CIRCLE_AS_POLYGON", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList(new String[] { "TRUE", "FALSE" }), "FALSE");
        dxfDef.addParameterDef("ELLIPSE_AS_POLYGON", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList(new String[] { "TRUE", "FALSE" }), "FALSE");
        dxfDef.addParameterDef("LINE_AS_POLYGON", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList(new String[] { "TRUE", "FALSE" }), "FALSE");
        this.inputFormatDefs.put("DXF", dxfDef);

        // GPX
        GisInputFormatDef gpxDef = new GisInputFormatDef("GPX", new String[] { "*.gpx;*.GPX" }, new String[] { "*.gpx"});
        gpxDef.addParameterDef("FORCE_TO_2D", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList(new String[] {"TRUE", "FALSE"}), "TRUE");
        this.inputFormatDefs.put("GPX", gpxDef);
        
        // GeoPackage
        GisInputFormatDef gpkgDef = new GisInputFormatDef("GEOPACKAGE", new String[] { "*.gpkg;*.GPKG"}, new String[] {"*.gpkg"});
        gpkgDef.addParameterDef("DB_TABLE_NAME", ValueMetaBase.TYPE_STRING, true);
        gpkgDef.addParameterDef("FORCE_TO_2D", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList(new String[] {"TRUE", "FALSE"}), "TRUE");
        gpkgDef.addParameterDef("FORCE_TO_MULTIGEOMETRY", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList(new String[] {"TRUE", "FALSE"}), "FALSE");
        this.inputFormatDefs.put("GEOPACKAGE", gpkgDef);
    }

    public List<GisInputFormatParameter> getInputFormatParameters() {
        return inputFormatParameters;
    }

    public void setInputFormatParameters(List<GisInputFormatParameter> inputFormatParameters) {
        this.inputFormatParameters = inputFormatParameters;
    }

    public HashMap<String, GisInputFormatDef> getInputFormatDefs() {
        return inputFormatDefs;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public String getGeometryFieldName() {
        return geometryFieldName;
    }

    public void setGeometryFieldName(String geometryFieldName) {
        this.geometryFieldName = geometryFieldName;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Long getRowLimit() {
        return rowLimit;
    }

    public void setRowLimit(Long rowLimit) {
        this.rowLimit = rowLimit;
    }

    @Override
    public String getXml() {

        StringBuffer retval = new StringBuffer();
        retval.append("\t" + XmlHandler.addTagValue("inputFormat", inputFormat));

        // Paramètres
        retval.append("\t<params>").append(Const.CR);
        for (GisInputFormatParameter parameter : inputFormatParameters) {

            String key = parameter.getKey();
            String value = (String) parameter.getValue();

            retval.append("\t\t<param>").append(Const.CR);
            retval.append("\t\t\t").append(XmlHandler.addTagValue("key", key));
            retval.append("\t\t\t").append(XmlHandler.addTagValue("value", value));
            retval.append("\t\t</param>").append(Const.CR);

        }

        retval.append("\t</params>").append(Const.CR);

        retval.append("    " + XmlHandler.addTagValue("inputFileName", inputFileName));
        retval.append("    " + XmlHandler.addTagValue("geometryFieldName", geometryFieldName));
        retval.append("    " + XmlHandler.addTagValue("encoding", encoding));
        retval.append("    " + XmlHandler.addTagValue("rowLimit", rowLimit));

        return retval.toString();

    }

    @Override
    public void getFields(IRowMeta r, String origin, IRowMeta[] info, TransformMeta nextStep,
    		IVariables space, IHopMetadataProvider metadataProvider) {

        Charset charset;
        try {
            charset = Charset.forName(encoding);
        } catch (Exception e) {
            charset = Charset.defaultCharset();
        }

        try {

            AbstractFileReader fileReader = null;
            
            if (inputFormat.equalsIgnoreCase("ESRI_SHP")) {
                fileReader = new ShapefileReader(space.resolve(inputFileName), space.resolve(geometryFieldName), charset.displayName());
            } else if (inputFormat.equalsIgnoreCase("GEOJSON")) {
                fileReader = new GeoJSONReader(space.resolve(inputFileName), space.resolve(geometryFieldName), charset.displayName());
            } else if (inputFormat.equalsIgnoreCase("MAPINFO_MIF")) {
                fileReader = new MapInfoReader(space.resolve(inputFileName), space.resolve(geometryFieldName), charset.displayName());
            } else if (inputFormat.equalsIgnoreCase("SPATIALITE")) {
                String tableName = space.resolve((String) getInputParameterValue("DB_TABLE_NAME"));
                fileReader = new SpatialiteReader(space.resolve(inputFileName), tableName, charset.displayName());
            } else if (inputFormat.equalsIgnoreCase("DXF")) {
                fileReader = new DXFReader(
                    space.resolve(inputFileName),
                    space.resolve(geometryFieldName),
                    charset.displayName(),
                    false,
                    false,
                    false,
                    Boolean.valueOf(space.resolve((String) getInputParameterValue("READ_XDATA"))));
            }else if (inputFormat.equalsIgnoreCase("GPX")) {
	            fileReader = new GPXReader(space.resolve(inputFileName), space.resolve(geometryFieldName), charset.displayName());
	        } else if (inputFormat.equalsIgnoreCase("GEOPACKAGE")) {

	            fileReader = new GeoPackageReader(
	            	space.resolve(inputFileName),
	            	space.resolve((String) getInputParameterValue("DB_TABLE_NAME")),
	            	space.resolve(geometryFieldName),
	            	charset.displayName()
	            );
	        }
            r.addRowMeta(FeatureConverter.getRowMeta(fileReader.getFields(), origin));
        } catch (HopException e) {
            e.printStackTrace();
        }
    }

    public List<String> getParameterPredefinedValues(String formatKey, String parameterKey) {
        List<String> predefinedValues = inputFormatDefs.get(formatKey).getParameterDef(parameterKey).getPredefinedValues();
        Collections.sort(predefinedValues);
        return predefinedValues;
    }

    public int getParameterValueMetaType(String formatKey, String parameterKey) {

        return inputFormatDefs.get(formatKey).getParameterDef(parameterKey).getValueMetaType();
    }

    public Object getInputParameterValue(String parameterKey) {

        for (GisInputFormatParameter parameter : inputFormatParameters) {

            if (parameter.getKey().equalsIgnoreCase(parameterKey)) {
                return parameter.getValue();
            }

        }

        return null;
    }

    public Object clone() {

        Object retval = super.clone();
        return retval;

    }

    @Override
    public void loadXml(Node stepnode, IHopMetadataProvider metadataProvider) throws HopXmlException {

        try {

            inputFormat = XmlHandler.getTagValue(stepnode, "inputFormat");
            Node paramsNode = XmlHandler.getSubNode(stepnode, "params");
            for (int i = 0; i < XmlHandler.countNodes(paramsNode, "param"); i++) {

                Node paramNode = XmlHandler.getSubNodeByNr(paramsNode, "param", i);
                String key = XmlHandler.getTagValue(paramNode, "key");
                String value = XmlHandler.getTagValue(paramNode, "value");

                inputFormatParameters.add(new GisInputFormatParameter(key, value));

            }

            inputFileName = XmlHandler.getTagValue(stepnode, "inputFileName");
            geometryFieldName = XmlHandler.getTagValue(stepnode, "geometryFieldName");
            encoding = XmlHandler.getTagValue(stepnode, "encoding");
            rowLimit = Long.valueOf(XmlHandler.getTagValue(stepnode, "rowLimit"));

        } catch (Exception e) {
            throw new HopXmlException("Unable to read step info from XML node", e);
        }

    }

    public void setDefault() {

        inputFormat = "ESRI_SHP";
        rowLimit = (long) 0;
    }

    @Override
    public void check(List<ICheckResult> remarks, PipelineMeta transmeta, TransformMeta stepMeta, IRowMeta prev,
    		String input[], String output[], IRowMeta info, IVariables variables,
            IHopMetadataProvider metadataProvider) {

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
        return new GisFileInputDialog(shell, variables, meta, transMeta, name);
    }
    

	@Override
	public ITransform createTransform(TransformMeta stepMeta,  GisFileInputData data, int cnr, PipelineMeta pipelineMeta,
			Pipeline pipeline) {
		return new GisFileInput(stepMeta, this, data, cnr, pipelineMeta, pipeline);
	}

	@Override
	public GisFileInputData getTransformData() {
		return new GisFileInputData();
	}
}
