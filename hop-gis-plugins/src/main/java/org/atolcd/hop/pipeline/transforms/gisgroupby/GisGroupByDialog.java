package org.atolcd.hop.pipeline.transforms.gisgroupby;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.core.gui.SwingGUIResource;
import org.apache.hop.ui.core.widget.ColumnInfo;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;

import org.atolcd.hop.pipeline.transforms.gisgroupby.GisGroupByMeta;

public class GisGroupByDialog extends BaseTransformDialog implements ITransformDialog {
    private static Class<?> PKG = GisGroupByMeta.class;

    public static final String STRING_SORT_WARNING_PARAMETER = "GroupSortWarning";

    private Label wlGroup;

    private TableView wGroup;

    private FormData fdlGroup, fdGroup;

    private Label wlAgg;

    private TableView wAgg;

    private FormData fdlAgg, fdAgg;

    private Label wlAllRows;

    private Button wAllRows;

    private FormData fdlAllRows, fdAllRows;

    private Label wlSortDir;

    private Button wbSortDir;

    private TextVar wSortDir;

    private FormData fdlSortDir, fdbSortDir, fdSortDir;

    private Label wlPrefix;

    private Text wPrefix;

    private FormData fdlPrefix, fdPrefix;

    private Label wlAddLineNr;

    private Button wAddLineNr;

    private FormData fdlAddLineNr, fdAddLineNr;

    private Label wlLineNrField;

    private Text wLineNrField;

    private FormData fdlLineNrField, fdLineNrField;

    private Label wlAlwaysAddResult;

    private Button wAlwaysAddResult;

    private FormData fdlAlwaysAddResult, fdAlwaysAddResult;

    private Button wGet, wGetAgg;

    private FormData fdGet, fdGetAgg;

    private Listener lsGet, lsGetAgg;

    private GisGroupByMeta input;

    private boolean backupAllRows;

    private ColumnInfo[] ciKey;

    private ColumnInfo[] ciReturn;

    private Map<String, Integer> inputFields;

    public GisGroupByDialog(Shell parent, IVariables variables, Object in, PipelineMeta transMeta, String sname) {
        super(parent, variables, (BaseTransformMeta) in, transMeta, sname);
        input = (GisGroupByMeta) in;
        inputFields = new HashMap<String, Integer>();
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
        backupAllRows = input.passAllRows();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "GroupByDialog.Shell.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // transformName line
        wlTransformName = new Label(shell, SWT.RIGHT);
        wlTransformName.setText(BaseMessages.getString(PKG, "GisGroupBy.TransformName.Label"));
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

        // Include all rows?
        wlAllRows = new Label(shell, SWT.RIGHT);
        wlAllRows.setText(BaseMessages.getString(PKG, "GroupByDialog.AllRows.Label"));
        props.setLook(wlAllRows);
        fdlAllRows = new FormData();
        fdlAllRows.left = new FormAttachment(0, 0);
        fdlAllRows.top = new FormAttachment(wTransformName, margin);
        fdlAllRows.right = new FormAttachment(middle, -margin);
        wlAllRows.setLayoutData(fdlAllRows);
        wAllRows = new Button(shell, SWT.CHECK);
        props.setLook(wAllRows);
        fdAllRows = new FormData();
        fdAllRows.left = new FormAttachment(middle, 0);
        fdAllRows.top = new FormAttachment(wTransformName, margin);
        fdAllRows.right = new FormAttachment(100, 0);
        wAllRows.setLayoutData(fdAllRows);
        wAllRows.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                input.setPassAllRows(!input.passAllRows());
                input.setChanged();
                setFlags();
            }
        });

        wlSortDir = new Label(shell, SWT.RIGHT);
        wlSortDir.setText(BaseMessages.getString(PKG, "GroupByDialog.TempDir.Label"));
        props.setLook(wlSortDir);
        fdlSortDir = new FormData();
        fdlSortDir.left = new FormAttachment(0, 0);
        fdlSortDir.right = new FormAttachment(middle, -margin);
        fdlSortDir.top = new FormAttachment(wAllRows, margin);
        wlSortDir.setLayoutData(fdlSortDir);

        wbSortDir = new Button(shell, SWT.PUSH | SWT.CENTER);
        props.setLook(wbSortDir);
        wbSortDir.setText(BaseMessages.getString(PKG, "GroupByDialog.Browse.Button"));
        fdbSortDir = new FormData();
        fdbSortDir.right = new FormAttachment(100, 0);
        fdbSortDir.top = new FormAttachment(wAllRows, margin);
        wbSortDir.setLayoutData(fdbSortDir);

        wSortDir = new TextVar(variables, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSortDir);
        wSortDir.addModifyListener(lsMod);
        fdSortDir = new FormData();
        fdSortDir.left = new FormAttachment(middle, 0);
        fdSortDir.top = new FormAttachment(wAllRows, margin);
        fdSortDir.right = new FormAttachment(wbSortDir, -margin);
        wSortDir.setLayoutData(fdSortDir);

        wbSortDir.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                DirectoryDialog dd = new DirectoryDialog(shell, SWT.NONE);
                dd.setFilterPath(wSortDir.getText());
                String dir = dd.open();
                if (dir != null) {
                    wSortDir.setText(dir);
                }
            }
        });

        // Whenever something changes, set the tooltip to the expanded version:
        wSortDir.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                wSortDir.setToolTipText(variables.resolve(wSortDir.getText()));
            }
        });

        // Prefix line...
        wlPrefix = new Label(shell, SWT.RIGHT);
        wlPrefix.setText(BaseMessages.getString(PKG, "GroupByDialog.FilePrefix.Label"));
        props.setLook(wlPrefix);
        fdlPrefix = new FormData();
        fdlPrefix.left = new FormAttachment(0, 0);
        fdlPrefix.right = new FormAttachment(middle, -margin);
        fdlPrefix.top = new FormAttachment(wbSortDir, margin * 2);
        wlPrefix.setLayoutData(fdlPrefix);
        wPrefix = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wPrefix);
        wPrefix.addModifyListener(lsMod);
        fdPrefix = new FormData();
        fdPrefix.left = new FormAttachment(middle, 0);
        fdPrefix.top = new FormAttachment(wbSortDir, margin * 2);
        fdPrefix.right = new FormAttachment(100, 0);
        wPrefix.setLayoutData(fdPrefix);

        // Include all rows?
        wlAddLineNr = new Label(shell, SWT.RIGHT);
        wlAddLineNr.setText(BaseMessages.getString(PKG, "GroupByDialog.AddLineNr.Label"));
        props.setLook(wlAddLineNr);
        fdlAddLineNr = new FormData();
        fdlAddLineNr.left = new FormAttachment(0, 0);
        fdlAddLineNr.top = new FormAttachment(wPrefix, margin);
        fdlAddLineNr.right = new FormAttachment(middle, -margin);
        wlAddLineNr.setLayoutData(fdlAddLineNr);
        wAddLineNr = new Button(shell, SWT.CHECK);
        props.setLook(wAddLineNr);
        fdAddLineNr = new FormData();
        fdAddLineNr.left = new FormAttachment(middle, 0);
        fdAddLineNr.top = new FormAttachment(wPrefix, margin);
        fdAddLineNr.right = new FormAttachment(100, 0);
        wAddLineNr.setLayoutData(fdAddLineNr);
        wAddLineNr.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                input.setAddingLineNrInGroup(!input.isAddingLineNrInGroup());
                input.setChanged();
                setFlags();
            }
        });

        // LineNrField line...
        wlLineNrField = new Label(shell, SWT.RIGHT);
        wlLineNrField.setText(BaseMessages.getString(PKG, "GroupByDialog.LineNrField.Label"));
        props.setLook(wlLineNrField);
        fdlLineNrField = new FormData();
        fdlLineNrField.left = new FormAttachment(0, 0);
        fdlLineNrField.right = new FormAttachment(middle, -margin);
        fdlLineNrField.top = new FormAttachment(wAddLineNr, margin);
        wlLineNrField.setLayoutData(fdlLineNrField);
        wLineNrField = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wLineNrField);
        wLineNrField.addModifyListener(lsMod);
        fdLineNrField = new FormData();
        fdLineNrField.left = new FormAttachment(middle, 0);
        fdLineNrField.top = new FormAttachment(wAddLineNr, margin);
        fdLineNrField.right = new FormAttachment(100, 0);
        wLineNrField.setLayoutData(fdLineNrField);

        // Always pass a result rows as output
        //
        wlAlwaysAddResult = new Label(shell, SWT.RIGHT);
        wlAlwaysAddResult.setText(BaseMessages.getString(PKG, "GroupByDialog.AlwaysAddResult.Label"));
        wlAlwaysAddResult.setToolTipText(BaseMessages.getString(PKG, "GroupByDialog.AlwaysAddResult.ToolTip"));
        props.setLook(wlAlwaysAddResult);
        fdlAlwaysAddResult = new FormData();
        fdlAlwaysAddResult.left = new FormAttachment(0, 0);
        fdlAlwaysAddResult.top = new FormAttachment(wLineNrField, margin);
        fdlAlwaysAddResult.right = new FormAttachment(middle, -margin);
        wlAlwaysAddResult.setLayoutData(fdlAlwaysAddResult);
        wAlwaysAddResult = new Button(shell, SWT.CHECK);
        wAlwaysAddResult.setToolTipText(BaseMessages.getString(PKG, "GroupByDialog.AlwaysAddResult.ToolTip"));
        props.setLook(wAlwaysAddResult);
        fdAlwaysAddResult = new FormData();
        fdAlwaysAddResult.left = new FormAttachment(middle, 0);
        fdAlwaysAddResult.top = new FormAttachment(wLineNrField, margin);
        fdAlwaysAddResult.right = new FormAttachment(100, 0);
        wAlwaysAddResult.setLayoutData(fdAlwaysAddResult);

        wlGroup = new Label(shell, SWT.NONE);
        wlGroup.setText(BaseMessages.getString(PKG, "GroupByDialog.Group.Label"));
        props.setLook(wlGroup);
        fdlGroup = new FormData();
        fdlGroup.left = new FormAttachment(0, 0);
        fdlGroup.top = new FormAttachment(wAlwaysAddResult, margin);
        wlGroup.setLayoutData(fdlGroup);

        int nrKeyCols = 1;
        int nrKeyRows = (input.getGroupField() != null ? input.getGroupField().length : 1);

        ciKey = new ColumnInfo[nrKeyCols];
        ciKey[0] = new ColumnInfo(BaseMessages.getString(PKG, "GroupByDialog.ColumnInfo.GroupField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false);

        wGroup = new TableView(variables, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciKey, nrKeyRows, lsMod, props);

        wGet = new Button(shell, SWT.PUSH);
        wGet.setText(BaseMessages.getString(PKG, "GroupByDialog.GetFields.Button"));
        fdGet = new FormData();
        fdGet.top = new FormAttachment(wlGroup, margin);
        fdGet.right = new FormAttachment(100, 0);
        wGet.setLayoutData(fdGet);

        fdGroup = new FormData();
        fdGroup.left = new FormAttachment(0, 0);
        fdGroup.top = new FormAttachment(wlGroup, margin);
        fdGroup.right = new FormAttachment(wGet, -margin);
        fdGroup.bottom = new FormAttachment(45, 0);
        wGroup.setLayoutData(fdGroup);

        // THE Aggregate fields
        wlAgg = new Label(shell, SWT.NONE);
        wlAgg.setText(BaseMessages.getString(PKG, "GroupByDialog.Aggregates.Label"));
        props.setLook(wlAgg);
        fdlAgg = new FormData();
        fdlAgg.left = new FormAttachment(0, 0);
        fdlAgg.top = new FormAttachment(wGroup, margin);
        wlAgg.setLayoutData(fdlAgg);

        int UpInsCols = 4;
        int UpInsRows = (input.getAggregateField() != null ? input.getAggregateField().length : 1);

        ciReturn = new ColumnInfo[UpInsCols];
        ciReturn[0] = new ColumnInfo(BaseMessages.getString(PKG, "GroupByDialog.ColumnInfo.Name"), ColumnInfo.COLUMN_TYPE_TEXT, false);
        ciReturn[1] = new ColumnInfo(BaseMessages.getString(PKG, "GroupByDialog.ColumnInfo.Subject"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false);
        ciReturn[2] = new ColumnInfo(BaseMessages.getString(PKG, "GroupByDialog.ColumnInfo.Type"), ColumnInfo.COLUMN_TYPE_CCOMBO, GisGroupByMeta.typeGroupLongDesc);
        ciReturn[3] = new ColumnInfo(BaseMessages.getString(PKG, "GroupByDialog.ColumnInfo.Value"), ColumnInfo.COLUMN_TYPE_TEXT, false);
        ciReturn[3].setToolTip(BaseMessages.getString(PKG, "GroupByDialog.ColumnInfo.Value.Tooltip"));
        ciReturn[3].setUsingVariables(true);

        wAgg = new TableView(variables, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciReturn, UpInsRows, lsMod, props);

        wGetAgg = new Button(shell, SWT.PUSH);
        wGetAgg.setText(BaseMessages.getString(PKG, "GroupByDialog.GetLookupFields.Button"));
        fdGetAgg = new FormData();
        fdGetAgg.top = new FormAttachment(wlAgg, margin);
        fdGetAgg.right = new FormAttachment(100, 0);
        wGetAgg.setLayoutData(fdGetAgg);

        //
        // Search the fields in the background

        final Runnable runnable = new Runnable() {
            public void run() {
                TransformMeta stepMeta = pipelineMeta.findTransform(transformName);
                if (stepMeta != null) {
                    try {
                        IRowMeta row = pipelineMeta.getPrevTransformFields(variables, stepMeta);

                        // Remember these fields...
                        for (int i = 0; i < row.size(); i++) {
                            inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                        }
                        setComboBoxes();
                    } catch (HopException e) {
                        logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();

        // THE BUTTONS
        wOk = new Button(shell, SWT.PUSH);
        wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        setButtonPositions(new Button[] { wOk, wCancel }, margin, null);

        fdAgg = new FormData();
        fdAgg.left = new FormAttachment(0, 0);
        fdAgg.top = new FormAttachment(wlAgg, margin);
        fdAgg.right = new FormAttachment(wGetAgg, -margin);
        fdAgg.bottom = new FormAttachment(wOk, -margin);
        wAgg.setLayoutData(fdAgg);

        // Add listeners
        lsOk = new Listener() {
            public void handleEvent(Event e) {
                ok();
            }
        };
        lsGet = new Listener() {
            public void handleEvent(Event e) {
                get();
            }
        };
        lsGetAgg = new Listener() {
            public void handleEvent(Event e) {
                getAgg();
            }
        };
        lsCancel = new Listener() {
            public void handleEvent(Event e) {
                cancel();
            }
        };

        wOk.addListener(SWT.Selection, lsOk);
        wGet.addListener(SWT.Selection, lsGet);
        wGetAgg.addListener(SWT.Selection, lsGetAgg);
        wCancel.addListener(SWT.Selection, lsCancel);

        lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };

        wTransformName.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        // Set the shell size, based upon previous time...
        setSize();

        getData();
        input.setChanged(backupChanged);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return transformName;
    }

    protected void setComboBoxes() {
        // Something was changed in the row.
        //
        final Map<String, Integer> fields = new HashMap<String, Integer>();

        // Add the currentMeta fields...
        fields.putAll(inputFields);

        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);

        String[] fieldNames = entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
        ciKey[0].setComboValues(fieldNames);
        ciReturn[1].setComboValues(fieldNames);
    }

    public void setFlags() {
        wlSortDir.setEnabled(wAllRows.getSelection());
        wbSortDir.setEnabled(wAllRows.getSelection());
        wSortDir.setEnabled(wAllRows.getSelection());
        wlPrefix.setEnabled(wAllRows.getSelection());
        wPrefix.setEnabled(wAllRows.getSelection());
        wlAddLineNr.setEnabled(wAllRows.getSelection());
        wAddLineNr.setEnabled(wAllRows.getSelection());

        wlLineNrField.setEnabled(wAllRows.getSelection() && wAddLineNr.getSelection());
        wLineNrField.setEnabled(wAllRows.getSelection() && wAddLineNr.getSelection());
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void getData() {
        logDebug(BaseMessages.getString(PKG, "GroupByDialog.Log.GettingKeyInfo"));

        wAllRows.setSelection(input.passAllRows());

        if (input.getPrefix() != null) {
            wPrefix.setText(input.getPrefix());
        }
        if (input.getDirectory() != null) {
            wSortDir.setText(input.getDirectory());
        }
        wAddLineNr.setSelection(input.isAddingLineNrInGroup());
        if (input.getLineNrInGroupField() != null) {
            wLineNrField.setText(input.getLineNrInGroupField());
        }
        wAlwaysAddResult.setSelection(input.isAlwaysGivingBackOneRow());

        if (input.getGroupField() != null) {
            for (int i = 0; i < input.getGroupField().length; i++) {
                TableItem item = wGroup.table.getItem(i);
                if (input.getGroupField()[i] != null) {
                    item.setText(1, input.getGroupField()[i]);
                }
            }
        }

        if (input.getAggregateField() != null) {
            for (int i = 0; i < input.getAggregateField().length; i++) {
                TableItem item = wAgg.table.getItem(i);
                if (input.getAggregateField()[i] != null) {
                    item.setText(1, input.getAggregateField()[i]);
                }
                if (input.getSubjectField()[i] != null) {
                    item.setText(2, input.getSubjectField()[i]);
                }
                item.setText(3, GisGroupByMeta.getTypeDescLong(input.getAggregateType()[i]));
                if (input.getValueField()[i] != null) {
                    item.setText(4, input.getValueField()[i]);
                }
            }
        }

        wGroup.setRowNums();
        wGroup.optWidth(true);
        wAgg.setRowNums();
        wAgg.optWidth(true);

        setFlags();

        wTransformName.selectAll();
        wTransformName.setFocus();
    }

    private void cancel() {
        transformName = null;
        input.setChanged(backupChanged);
        input.setPassAllRows(backupAllRows);
        dispose();
    }

    private void ok() {
        if (wTransformName.getText().isEmpty()) {
            return;
        }

        int sizegroup = wGroup.nrNonEmpty();
        int nrfields = wAgg.nrNonEmpty();
        input.setPrefix(wPrefix.getText());
        input.setDirectory(wSortDir.getText());

        input.setLineNrInGroupField(wLineNrField.getText());
        input.setAlwaysGivingBackOneRow(wAlwaysAddResult.getSelection());

        input.allocate(sizegroup, nrfields);

        // CHECKSTYLE:Indentation:OFF
        for (int i = 0; i < sizegroup; i++) {
            TableItem item = wGroup.getNonEmpty(i);
            input.getGroupField()[i] = item.getText(1);
        }

        // CHECKSTYLE:Indentation:OFF
        for (int i = 0; i < nrfields; i++) {
            TableItem item = wAgg.getNonEmpty(i);
            input.getAggregateField()[i] = item.getText(1);
            input.getSubjectField()[i] = item.getText(2);
            input.getAggregateType()[i] = GisGroupByMeta.getType(item.getText(3));
            input.getValueField()[i] = item.getText(4);
        }

        transformName = wTransformName.getText();

        if (sizegroup > 0 && "Y".equalsIgnoreCase(props.getCustomParameter(STRING_SORT_WARNING_PARAMETER, "Y"))) {
            MessageDialogWithToggle md = new MessageDialogWithToggle(shell, BaseMessages.getString(PKG, "GroupByDialog.GroupByWarningDialog.DialogTitle"), null,
                    BaseMessages.getString(PKG, "GroupByDialog.GroupByWarningDialog.DialogMessage", Const.CR) + Const.CR, MessageDialog.WARNING,
                    new String[] { BaseMessages.getString(PKG, "GroupByDialog.GroupByWarningDialog.Option1") }, 0, BaseMessages.getString(PKG,
                            "GroupByDialog.GroupByWarningDialog.Option2"), "N".equalsIgnoreCase(props.getCustomParameter(STRING_SORT_WARNING_PARAMETER, "Y")));
            //MessageDialogWithToggle.setDefaultImage(SwingGUIResource.getInstance().getImageSpoon());
            md.open();
            props.setCustomParameter(STRING_SORT_WARNING_PARAMETER, md.getToggleState() ? "N" : "Y");
            //props.saveProps();
        }

        dispose();
    }

    private void get() {
    	TransformMeta transformMeta = pipelineMeta.findTransform(transformName);
        if(transformMeta != null) {
	        try {
	            IRowMeta r = pipelineMeta.getPrevTransformFields(variables, transformMeta);
	            if (r != null && !r.isEmpty()) {
	                BaseTransformDialog.getFieldsFromPrevious(r, wGroup, 1, new int[] { 1 }, new int[] {}, -1, -1, null);
	            }
	        } catch (HopException ke) {
	            new ErrorDialog(shell, BaseMessages.getString(PKG, "GroupByDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG,
	                    "GroupByDialog.FailedToGetFields.DialogMessage"), ke);
	        }
        }
    }

    private void getAgg() {
    	TransformMeta transformMeta = pipelineMeta.findTransform(transformName);
        if(transformMeta != null) {
	        try {
	        	IRowMeta r = pipelineMeta.getPrevTransformFields(variables, transformMeta);
	            if (r != null && !r.isEmpty()) {
	                BaseTransformDialog.getFieldsFromPrevious(r, wAgg, 1, new int[] { 1, 2 }, new int[] {}, -1, -1, null);
	            }
	        } catch (HopException ke) {
	            new ErrorDialog(shell, BaseMessages.getString(PKG, "GroupByDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG,
	                    "GroupByDialog.FailedToGetFields.DialogMessage"), ke);
	        }
        }
    }
}
