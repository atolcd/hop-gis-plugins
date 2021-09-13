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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.widget.ColumnInfo;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;

import com.atolcd.hop.core.row.value.ValueMetaGeometry;
import com.atolcd.hop.pipeline.transforms.gisgeometryinfo.GisGeometryInfoMeta;

public class GisGeometryInfoDialog extends BaseTransformDialog implements ITransformDialog {

    private static Class<?> PKG = GisGeometryInfoMeta.class;

    private Label wlGeometryField;
    private CCombo wGeometryField;
    private FormData fdlGeometryField, fdGeometryField;

    // Sorties
    private Label wlOutputs;
    private TableView wOutputs;
    private FormData fdlOutputs, fdOutputs;

    private ColumnInfo[] outputsColumnInfo;

    private GisGeometryInfoMeta input;

    public GisGeometryInfoDialog(Shell parent, IVariables variables, Object in, PipelineMeta tr, String sname) {

        super(parent, variables, (BaseTransformMeta) in, tr, sname);
        input = (GisGeometryInfoMeta) in;

    }

    public String open() {

        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);
        setShellImage(shell, input);

        ModifyListener lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                input.setChanged();
            }
        };
        backupChanged = input.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "GisGeometryInfo.Shell.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Nom du step
        wlTransformName = new Label(shell, SWT.RIGHT);
        wlTransformName.setText(BaseMessages.getString(PKG, "GisGeometryInfo.TransformName.Label"));
        props.setLook(wlTransformName);
        fdlTransformName = new FormData();
        fdlTransformName.left = new FormAttachment(0, 0);
        fdlTransformName.right = new FormAttachment(middle, -margin);
        fdlTransformName.top = new FormAttachment(0, margin);
        wlTransformName.setLayoutData(fdlTransformName);
        wTransformName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wTransformName.setText(transformName);
        props.setLook(wTransformName);
        wTransformName.addModifyListener(lsMod);
        fdTransformName = new FormData();
        fdTransformName.left = new FormAttachment(middle, 0);
        fdTransformName.top = new FormAttachment(0, margin);
        fdTransformName.right = new FormAttachment(100, 0);
        wTransformName.setLayoutData(fdTransformName);

        // Géométrie
        wlGeometryField = new Label(shell, SWT.RIGHT);
        wlGeometryField.setText(BaseMessages.getString(PKG, "GisGeometryInfo.GeometryFieldName.Label"));
        props.setLook(wlGeometryField);
        fdlGeometryField = new FormData();
        fdlGeometryField.left = new FormAttachment(0, 0);
        fdlGeometryField.top = new FormAttachment(wTransformName, margin);
        fdlGeometryField.right = new FormAttachment(middle, -margin);
        wlGeometryField.setLayoutData(fdlGeometryField);

        wGeometryField = new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
        wGeometryField.setToolTipText(BaseMessages.getString(PKG, "GisGeometryInfo.GeometryFieldName.ToolTip"));
        wGeometryField.setEditable(false);
        props.setLook(wGeometryField);
        wGeometryField.addModifyListener(lsMod);
        fdGeometryField = new FormData();
        fdGeometryField.left = new FormAttachment(middle, 0);
        fdGeometryField.right = new FormAttachment(100, 0);
        fdGeometryField.top = new FormAttachment(wTransformName, margin);
        wGeometryField.setLayoutData(fdGeometryField);

        // Boutons Ok et Annuler
        wOk = new Button(shell, SWT.PUSH);
        wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        // Sorties
        wlOutputs = new Label(shell, SWT.NONE);
        wlOutputs.setText(BaseMessages.getString(PKG, "GisGeometryInfo.Outputs.Label"));
        props.setLook(wlOutputs);
        fdlOutputs = new FormData();
        fdlOutputs.left = new FormAttachment(0, 0);
        fdlOutputs.top = new FormAttachment(wGeometryField, margin);
        wlOutputs.setLayoutData(fdlOutputs);

        outputsColumnInfo = getOutputsColumnInfo();
        wOutputs = new TableView(variables, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, outputsColumnInfo, 0, lsMod, props);
        wOutputs.optWidth(true);
        wOutputs.setReadonly(false);
        fdOutputs = new FormData();
        fdOutputs.left = new FormAttachment(0, 0);
        fdOutputs.top = new FormAttachment(wlOutputs, margin);
        fdOutputs.right = new FormAttachment(100, 0);
        fdOutputs.bottom = new FormAttachment(wOk, -margin);
        wOutputs.setLayoutData(fdOutputs);

        setButtonPositions(new Button[] { wOk, wCancel }, margin, null);
        Listener lsCancel = new Listener() {
            public void handleEvent(Event e) {
                cancel();
            }
        };
        Listener lsOk = new Listener() {
            public void handleEvent(Event e) {
                ok();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOk.addListener(SWT.Selection, lsOk);
        SelectionListener lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };
        wTransformName.addSelectionListener(lsDef);

        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        wGeometryField.setItems(getFieldsFromType(ValueMetaBase.getTypeDesc(ValueMetaGeometry.TYPE_GEOMETRY)));
        loadData();
        input.setChanged(changed);
        setSize();

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        return transformName;
    }

    // Charge les données dans le formulaire
    public void loadData() {

        // Liste des types d'infos
        List<String> infosTypes = new ArrayList<String>();
        for (String infoType : input.getInfosTypes().keySet()) {
            infosTypes.add(getInfoLabel(infoType));
        }
        Collections.sort(infosTypes);
        outputsColumnInfo[0].setComboValues(infosTypes.toArray(new String[infosTypes.size()]));

        if (input.getGeometryFieldName() != null) {
            wGeometryField.setText(input.getGeometryFieldName());
        }

        // Tableau de valeur des paramètres
        if (!input.getOutputFields().isEmpty()) {

            Table outputsTable = wOutputs.table;
            outputsTable.removeAll();

            int i = 0;
            for (Entry<String, String> output : input.getOutputFields().entrySet()) {

                TableItem tableItem = new TableItem(outputsTable, SWT.NONE);
                tableItem.setText(0, String.valueOf(i));
                tableItem.setText(1, getInfoLabel(output.getKey()));
                tableItem.setText(2, output.getValue());
                i++;

            }
        }

        wOutputs.setRowNums();

        wTransformName.selectAll();
    }

    private void cancel() {
    	transformName = null;
        input.setChanged(backupChanged);
        dispose();
    }

    private void ok() {

    	transformName = wTransformName.getText();
        input.setGeometryFieldName(wGeometryField.getText());
        LinkedHashMap<String, String> outputFields = new LinkedHashMap<String, String>();
        for (int i = 0; i < wOutputs.nrNonEmpty(); i++) {

            TableItem tableItem = wOutputs.getNonEmpty(i);
            outputFields.put(getInfoKey(tableItem.getText(1)), tableItem.getText(2));
        }
        input.setOutputFields(outputFields);

        dispose();
    }

    // Libellé de l'info en fonction du code
    private String getInfoLabel(String key) {

        return BaseMessages.getString(PKG, "GisGeometryInfo.Info." + key + ".Label");

    }

    // Code de l'info en fonction du libellé
    private String getInfoKey(String label) {

        for (String key : input.getInfosTypes().keySet()) {

            if (label.equalsIgnoreCase(BaseMessages.getString(PKG, "GisGeometryInfo.Info." + key + ".Label"))) {
                return key;
            }
        }
        return null;

    }

    // Liste les colonnes d'un certain type
    private String[] getFieldsFromType(String type) {

        String fieldNamesFromType[] = null;
        
        TransformMeta transformMeta = pipelineMeta.findTransform(transformName);
        if(transformMeta != null) {
	        try {
	
	            // Récupération des colonnes de l'étape précédente
	            IRowMeta r = pipelineMeta.getPrevTransformFields(variables, transformMeta);
	            if (r != null) {
	
	                // Filtrage par type de colonne texte
	                TreeSet<String> fieldsTree = new TreeSet<String>();
	                String[] fieldNames = r.getFieldNames();
	                String[] fieldNamesAndTypes = r.getFieldNamesAndTypes(0);
	
	                for (int i = 0; i < fieldNames.length; i++) {
	                    if (fieldNamesAndTypes[i].toLowerCase().contains(type.toLowerCase())) {
	                        if (fieldNames[i] != null && !fieldNames[i].isEmpty()) {
	                            fieldsTree.add(fieldNames[i]);
	                        }
	                    }
	                }
	
	                fieldNamesFromType = fieldsTree.toArray(new String[] {});
	
	            }
	
	        } catch (HopException ke) {
	            new ErrorDialog(shell, BaseMessages.getString(PKG, "ChangeFileEncodingDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG,
	                    "ChangeFileEncodingDialog.FailedToGetFields.DialogMessage"), ke);
	        }
        }

        return fieldNamesFromType;

    }

    private ColumnInfo[] getOutputsColumnInfo() {

        // Type d'info
        ColumnInfo infoTypeColumnInfo = new ColumnInfo(BaseMessages.getString(PKG, "GisFileInput.Outputs.Columns.OUTPUT_KEY.Label"), ColumnInfo.COLUMN_TYPE_CCOMBO);
        infoTypeColumnInfo.setReadOnly(true);

        // Nom du champ
        ColumnInfo infoFieldNameColumnInfo = new ColumnInfo(BaseMessages.getString(PKG, "GisFileInput.Outputs.Columns.OUTPUT_FIELDNAME.Label"), ColumnInfo.COLUMN_TYPE_TEXT);
        infoFieldNameColumnInfo.setReadOnly(false);
        return new ColumnInfo[] { infoTypeColumnInfo, infoFieldNameColumnInfo };
    }

}
