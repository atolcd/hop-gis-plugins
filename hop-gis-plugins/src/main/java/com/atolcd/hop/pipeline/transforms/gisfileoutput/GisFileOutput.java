package com.atolcd.hop.pipeline.transforms.gisfileoutput;

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

import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.ITransformData;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.ITransformMeta;

import com.atolcd.hop.gis.io.AbstractFileWriter;
import com.atolcd.hop.gis.io.DXFWriter;
import com.atolcd.hop.gis.io.GPXWriter;
import com.atolcd.hop.gis.io.GeoJSONWriter;
import com.atolcd.hop.gis.io.GeoPackageWriter;
import com.atolcd.hop.gis.io.KMLWriter;
import com.atolcd.hop.gis.io.SVGWriter;
import com.atolcd.hop.gis.io.ShapefileWriter;
import com.atolcd.hop.gis.io.features.Feature;
import com.atolcd.hop.gis.io.features.FeatureConverter;
import com.atolcd.hop.pipeline.transforms.gisrelate.GisRelateData;
import com.atolcd.hop.pipeline.transforms.gisrelate.GisRelateMeta;

public class GisFileOutput extends BaseTransform<GisFileOutputMeta,GisFileOutputData> implements ITransform<GisFileOutputMeta,GisFileOutputData> {

    private List<Feature> gisFeatures = new ArrayList<Feature>();

    public GisFileOutput(TransformMeta s, GisFileOutputMeta meta, GisFileOutputData data, int c, PipelineMeta t, Pipeline dis) {
        super(s, meta, data, c, t, dis);
    }

    @Override
    public boolean processRow() throws HopException {

        Object[] r = getRow();

        // Ecriture des fichiers à partir de la collection de features
        if (r == null) {

            AbstractFileWriter fileWriter = null;

            // ESRI_SHP
            if (meta.getOutputFormat().equalsIgnoreCase("ESRI_SHP")) {

                fileWriter = new ShapefileWriter(resolve(meta.getOutputFileName()), meta.getGeometryFieldName(), meta.getEncoding());

                // Forcer en 2D
                String forceTo2D = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "FORCE_TO_2D"));
                if (forceTo2D != null) {
                    ((ShapefileWriter) fileWriter).setForceTo2DGeometry(Boolean.parseBoolean(forceTo2D));
                }

                // PRJ
                String withPRJ = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "ESRI_SHP_CREATE_PRJ"));
                if (withPRJ != null) {
                    ((ShapefileWriter) fileWriter).setCreatePrjFile(Boolean.parseBoolean(withPRJ));
                }

                // GEOJSON -- TODO SERVLET
            } else if (meta.getOutputFormat().equalsIgnoreCase("GEOJSON")) {

                if (meta.isDataToServlet()) {
                    //Writer writer = getTrans().getServletPrintWriter();
                    //fileWriter = new GeoJSONWriter(writer, meta.getGeometryFieldName(), meta.getEncoding());
                } else {
                    fileWriter = new GeoJSONWriter(resolve(meta.getOutputFileName()), meta.getGeometryFieldName(), meta.getEncoding());
                }

                // Exporter id
                String featureIdField = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "GEOJSON_FEATURE_ID"));
                if (featureIdField != null && !featureIdField.isEmpty()) {
                    ((GeoJSONWriter) fileWriter).setFeatureIdField(featureIdField);
                }

                // KML -- TODO SERVLET
            } else if (meta.getOutputFormat().equalsIgnoreCase("KML")) {

                if (meta.isDataToServlet()) {
                    //Writer writer = getTrans().getServletPrintWriter();
                    //fileWriter = new KMLWriter(writer, meta.getGeometryFieldName(), meta.getEncoding());

                } else {
                    fileWriter = new KMLWriter(resolve(meta.getOutputFileName()), meta.getGeometryFieldName(), meta.getEncoding());
                }

                // Forcer en 2D
                String forceTo2D = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "FORCE_TO_2D"));
                if (forceTo2D != null) {
                    ((KMLWriter) fileWriter).setForceTo2DGeometry(Boolean.parseBoolean(forceTo2D));
                }

                // Exporter les attributs
                String withAttributs = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "KML_EXPORT_ATTRIBUTS"));
                if (withAttributs != null) {
                    ((KMLWriter) fileWriter).setExportWithAttributs(Boolean.parseBoolean(withAttributs));
                }

                // Exporter nom <Document>
                String docName = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "KML_DOC_NAME"));
                if (docName != null) {
                    ((KMLWriter) fileWriter).setDocumentName(docName);
                }

                // Exporter description <Document>
                String docDescription = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "KML_DOC_DESCRIPTION"));
                if (docDescription != null) {
                    ((KMLWriter) fileWriter).setDocumentDescription(docDescription);
                }

                // Exporter nom <Placemark>
                String featureNameField = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "KML_PLACEMARK_NAME"));
                if (featureNameField != null && !featureNameField.isEmpty()) {
                    ((KMLWriter) fileWriter).setFeatureNameField(featureNameField);
                }

                // Exporter description <Placemark>
                String featureDescriptionField = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "KML_PLACEMARK_DESCRIPTION"));
                if (featureDescriptionField != null && !featureNameField.isEmpty()) {
                    ((KMLWriter) fileWriter).setFeatureDescriptionField(featureDescriptionField);
                }

                // DXF
            } else if (meta.getOutputFormat().equalsIgnoreCase("DXF")) {

                String layerName = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "DXF_LAYER_NAME"));
                fileWriter = new DXFWriter(resolve(meta.getOutputFileName()), layerName, meta.getGeometryFieldName(), meta.getEncoding());

                String layerNameFieldName = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "DXF_FEATURE_LAYER_NAME"));
                if (layerNameFieldName != null && !layerNameFieldName.isEmpty()) {
                    ((DXFWriter) fileWriter).setLayerNameFieldName(layerNameFieldName);
                }

                String precision = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "DXF_COORD_PRECISION"));
                if (precision != null) {
                    ((DXFWriter) fileWriter).setPrecision(Integer.parseInt(precision));
                }

                // Forcer en 2D
                String forceTo2D = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "FORCE_TO_2D"));
                if (forceTo2D != null) {
                    ((DXFWriter) fileWriter).setForceTo2DGeometry(Boolean.parseBoolean(forceTo2D));
                }

                // Write xData
                String exportXdataD = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "DXF_WRITE_XDATA"));
                if (exportXdataD != null) {
                    ((DXFWriter) fileWriter).setExportWithAttributs(Boolean.parseBoolean(exportXdataD));
                }
            //GPX -- TODO SERVLET
            } else if (meta.getOutputFormat().equalsIgnoreCase("GPX")) {
            
              if (meta.isDataToServlet()) {
                  //Writer writer = getTrans().getServletPrintWriter();
                  //fileWriter = new GPXWriter(writer, meta.getGeometryFieldName(), meta.getEncoding());

              } else {
                  fileWriter = new GPXWriter(resolve(meta.getOutputFileName()), meta.getGeometryFieldName(), meta.getEncoding());
              }

              //Version
              String version = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "GPX_VERSION"));
              if (version != null) {
                  ((GPXWriter) fileWriter).setVersion(version);
              }

              // Exporter nom <metadata>
              String metaName = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "GPX_META_NAME"));
              if (metaName != null) {
                  ((GPXWriter) fileWriter).setDocumentName(metaName);
              }

              // Exporter description <metadata>
              String metaDescription = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "GPX_META_DESCRIPTION"));
              if (metaDescription != null) {
                  ((GPXWriter) fileWriter).setDocumentDescription(metaDescription);
              }
              
              //Nom Auteur
              String metaAuthorName = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "GPX_META_AUTHOR_NAME"));
              if (metaAuthorName != null) {
                  ((GPXWriter) fileWriter).setAuthorName(metaAuthorName);
              }
              
              //Email Auteur
              String metaAuthorEmail = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "GPX_META_AUTHOR_EMAIL"));
              if (metaAuthorEmail != null) {
                  ((GPXWriter) fileWriter).setAuthorEmail(metaAuthorEmail);
              }
              
              //Mots clefs
              String metaKeyWords = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "GPX_META_KEYWORDS"));
              if (metaKeyWords != null) {
                  ((GPXWriter) fileWriter).setKeywords(metaKeyWords);
              }
              
                 //Date et heure
              String dateTime = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "GPX_META_DATETIME"));
              if (dateTime != null) {
           
                    try {
                        ((GPXWriter) fileWriter).setDatetime(toCalendar(dateTime));
                    } catch (Exception e) {

                        throw new HopException(e.getMessage());
                    }

              }

              // Exporter nom <wpt>,<rte> ou <trk>
              String featureNameField = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "GPX_FEATURE_NAME"));
              if (featureNameField != null && !featureNameField.isEmpty()) {
                  ((GPXWriter) fileWriter).setFeatureNameField(featureNameField);
              }

              // Exporter description <wpt>,<rte> ou <trk>
              String featureDescriptionField = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "GPX_FEATURE_DESCRIPTION"));
              if (featureDescriptionField != null && !featureDescriptionField.isEmpty()) {
                  ((GPXWriter) fileWriter).setFeatureDescriptionField(featureDescriptionField);
              }
              
        //GEOPACKAGE
        } else if (meta.getOutputFormat().equalsIgnoreCase("GEOPACKAGE")) {
            

            fileWriter = new GeoPackageWriter(
                resolve(meta.getOutputFileName()),
                resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "DB_TABLE_NAME")),
                resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "DB_TABLE_PK_FIELD")),
                resolve(meta.getGeometryFieldName()),
                meta.getEncoding()
            );
            
            //Commit limit
            String commitLimit = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "DB_TABLE_COMMIT_LIMIT"));
            if (commitLimit != null) {
                ((GeoPackageWriter) fileWriter).setCommitLimit(Long.parseLong(commitLimit));
            }
        
            // Remplacer fichier
            String replaceFile = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "REPLACE_FILE"));
            if (replaceFile != null) {
                ((GeoPackageWriter) fileWriter).setReplaceFile(Boolean.parseBoolean(replaceFile));
            }
            
            // Remplacer table
            String replaceTable = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "REPLACE_TABLE"));
            if (replaceTable != null) {
                ((GeoPackageWriter) fileWriter).setReplaceTable(Boolean.parseBoolean(replaceTable));
            }
              
            // Forcer en 2D
            String forceTo2D = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "FORCE_TO_2D"));
            if (forceTo2D != null) {
                ((GeoPackageWriter) fileWriter).setForceTo2DGeometry(Boolean.parseBoolean(forceTo2D));
            }
            
            //SRID
            String geometrySrid = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "GPKG_GEOMETRY_SRID"));
            if (geometrySrid != null) {
                if(geometrySrid.isEmpty()){
                    ((GeoPackageWriter) fileWriter).setAssignedSrid(-1);
                }else{
                    ((GeoPackageWriter) fileWriter).setAssignedSrid(Long.parseLong(geometrySrid));
                }
            }
            
            //Type de géométrie
            String geometryType = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "GPKG_GEOMETRY_GEOMETRYTYPE"));
            if (geometryType != null) {
                ((GeoPackageWriter) fileWriter).setAssignedGeometryType(geometryType);
            }
            
            //Identifier
            String contentsIdentifier = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "GPKG_CONTENTS_IDENTIFIER"));
            if (contentsIdentifier != null) {
                ((GeoPackageWriter) fileWriter).setContentsIdentifier(contentsIdentifier);
            }else{
                resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "DB_TABLE_NAME"));
            }
            
            //Description
            String contentsDescription = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "GPKG_CONTENTS_DESCRIPTION"));
            if (contentsDescription != null) {
                ((GeoPackageWriter) fileWriter).setContentsDescription(contentsDescription);
            }
              
              //SVG -- TODO SERVLET
        } else if (meta.getOutputFormat().equalsIgnoreCase("SVG")) {
            
            if (meta.isDataToServlet()) {
               // Writer writer = getTrans().getServletPrintWriter();
               // fileWriter = new SVGWriter(writer, meta.getGeometryFieldName(), meta.getEncoding());

            } else {
                fileWriter = new SVGWriter(resolve(meta.getOutputFileName()), meta.getGeometryFieldName(), meta.getEncoding());
            }

            // Largeur
            int svgWidth = Integer.parseInt(resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "SVG_DOC_WIDTH")));
            ((SVGWriter) fileWriter).setWidth(svgWidth);
          
            // Hauteur
            int svgHeight = Integer.parseInt(resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "SVG_DOC_HEIGHT")));
            ((SVGWriter) fileWriter).setHeight(svgHeight);
            
            //Precision
            String precision = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "SVG_DOC_COORD_PRECISION"));
            if (precision != null) {
                ((SVGWriter) fileWriter).setPrecision(Integer.parseInt(precision));
            }
            
            // Exporter title  du doc
            String title = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "SVG_DOC_TITLE"));
            if (title != null) {
                ((SVGWriter) fileWriter).setDocumentTitle(title);
            }

            // Exporter description  du doc
            String description = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "SVG_DOC_DESCRIPTION"));
            if (description != null) {
                ((SVGWriter) fileWriter).setDocumentDescription(description);
            }
            
            // Feuille de style
            String styleSheet = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "SVG_DOC_STYLESHEET"));
            if (styleSheet != null) {
                ((SVGWriter) fileWriter).setStyleSheet(styleSheet);
            }
            
            // Usage de la feuille de style
            String styleSheetMode = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "SVG_DOC_STYLESHEET_MODE"));
            if (styleSheetMode != null) {
                ((SVGWriter) fileWriter).setStyleSheetMode(styleSheetMode);
            }
            
            // Usage des symboles
            String symbolMode = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIXED, "SVG_DOC_SYMBOL_MODE"));
            if (symbolMode != null) {
                ((SVGWriter) fileWriter).setSymbolMode(symbolMode);
            }

            // Exporter id 
            String featureIdField = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "SVG_FEATURE_ID"));
            if (featureIdField != null && !featureIdField.isEmpty()) {
                ((SVGWriter) fileWriter).setFeatureIdField(featureIdField);
            }

            // Exporter title 
            String featureTitleField = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "SVG_FEATURE_TITLE"));
            if (featureTitleField != null && !featureTitleField.isEmpty()) {
                ((SVGWriter) fileWriter).setFeatureTitleField(featureTitleField);
            }


            // Exporter description
            String featureDescriptionField = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "SVG_FEATURE_DESCRIPTION"));
            if (featureDescriptionField != null && !featureDescriptionField.isEmpty()) {
                ((SVGWriter) fileWriter).setFeatureDescriptionField(featureDescriptionField);
            }
            
            //Style svg
            String featureStyleField = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "SVG_FEATURE_STYLE"));
            if (featureStyleField != null && !featureStyleField.isEmpty()) {
                ((SVGWriter) fileWriter).setFeatureSvgStyleField(featureStyleField);
            }
            
            //Class CSS
            String featureCssClassField = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "SVG_FEATURE_CLASS"));
            if (featureCssClassField != null && !featureCssClassField.isEmpty()) {
                ((SVGWriter) fileWriter).setFeatureCssClassField(featureCssClassField);
            }
            
            //Symbole
            String featureSymbolField = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "SVG_FEATURE_SYMBOL"));
            if (featureSymbolField != null && !featureSymbolField.isEmpty()) {
                ((SVGWriter) fileWriter).setFeatureSymbolField(featureSymbolField);
            }
            
            //Symbole
            String featureLabelField = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "SVG_FEATURE_LABEL"));
            if (featureLabelField != null && !featureLabelField.isEmpty()) {
                ((SVGWriter) fileWriter).setFeatureLabelField(featureLabelField);
            }
            
            //Groupe
            String featureGroupField = resolve((String) meta.getInputParameterValue(GisOutputFormatParameterDef.TYPE_FIELD, "SVG_FEATURE_GROUP"));
            if (featureGroupField != null && !featureGroupField.isEmpty()) {
                ((SVGWriter) fileWriter).setFeatureGroupField(featureGroupField);
            }

      }

            fileWriter.setFields(FeatureConverter.getFields(getPipelineMeta().getPrevTransformFields(variables, getTransformMeta())));

            // Création contextuelle du fichier
            if ((gisFeatures.size() == 0 && !meta.isCreateFileAtEnd()) || gisFeatures.size() > 0) {

                logBasic("Write file - Start");
                fileWriter.writeFeatures(gisFeatures);
                logBasic("Write file - End");

            }

            setOutputDone();
            return false;
        }

        if (first) {

            first = false;
            data.outputRowMeta = (IRowMeta) getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getTransformName(), null, null, this, metadataProvider);
            logBasic("Initialized successfully");

        }

        // Nouvelle feature pour chaque ligne
        gisFeatures.add(FeatureConverter.getFeature(data.outputRowMeta, r));
        putRow(data.outputRowMeta, r);

        if (checkFeedback(getLinesRead())) {
            logBasic("Linenr " + getLinesRead());
        }

        return true;
    }

    @Override
    public boolean init() {
        return super.init();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public void run() {
        logBasic("Starting to run...");
        try {
            while (processRow() && !isStopped())
                ;
        } catch (Exception e) {
            logError("Unexpected error : " + e.toString());
            logError(Const.getStackTracker(e));
            setErrors(1);
            stopAll();
        } finally {
            dispose();
            logBasic("Finished, processing " + getLinesRead() + " rows");
            markStop();
        }
    }
    
    private static GregorianCalendar toCalendar(String iso8601string) throws Exception{

    	try{
    		
    		java.util.Date date = Date.from(OffsetDateTime.parse(iso8601string).toInstant());
    	    GregorianCalendar calendar = new GregorianCalendar();
    	    calendar.setTime(date);
    	    return calendar;
    	
    	}catch(Exception e){
    		throw new Exception(e.getMessage());
    	}

    }

}
