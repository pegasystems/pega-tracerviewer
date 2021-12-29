/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableColumnModel;

import com.pega.gcs.fringecommon.guiutilities.CustomJTable;
import com.pega.gcs.fringecommon.guiutilities.CustomJTableModel;
import com.pega.gcs.fringecommon.guiutilities.GUIUtilities;
import com.pega.gcs.fringecommon.guiutilities.Message;
import com.pega.gcs.fringecommon.guiutilities.MyColor;
import com.pega.gcs.fringecommon.guiutilities.search.SearchPanel;
import com.pega.gcs.tracerviewer.TraceNavigationTableController;
import com.pega.gcs.tracerviewer.TraceTableModel;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public abstract class TracerDataSingleView extends TracerDataView {

    private static final long serialVersionUID = -1109125493476264152L;

    private SearchPanel<TraceEventKey> searchPanel;

    private JTextField statusBar;

    // not using TraceTable to accommodate tree view as well.
    protected abstract CustomJTable getTracerDataTable();

    protected abstract JPanel getAdditionalUtilityPanel();

    protected abstract void updateTreeTableColumnModel();

    public TracerDataSingleView(TraceTableModel traceTableModel, JPanel supplementUtilityJPanel,
            TraceNavigationTableController traceNavigationTableController) {

        super(traceTableModel, traceNavigationTableController, supplementUtilityJPanel);

        CustomJTable traceTable = getTracerDataTable();
        traceNavigationTableController.addCustomJTable(traceTable);

        searchPanel = new SearchPanel<TraceEventKey>(traceNavigationTableController, traceTableModel.getSearchModel());
        //
        // SearchModel<TraceEventKey> searchModel =
        // traceTableModel.getSearchModel();
        // searchModel.addSearchModelListener(searchPanel);

        setLayout(new BorderLayout());

        JPanel utilityJPanel = getUtilityJPanel();
        JPanel tracerDataJPanel = getTracerDataJPanel();
        JPanel statusBarJPanel = getStatusBarJPanel();

        add(utilityJPanel, BorderLayout.NORTH);
        add(tracerDataJPanel, BorderLayout.CENTER);
        add(statusBarJPanel, BorderLayout.SOUTH);

        traceTableModel.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                String propertyName = evt.getPropertyName();

                if ("message".equals(propertyName)) {

                    JTextField statusBar = getStatusBar();
                    Message message = (Message) evt.getNewValue();
                    setMessage(statusBar, message);
                } else if ("traceTableModel".equals(propertyName)) {
                    // 'traceTableModel' fired by TraceTableModel as the type
                    // of tracer file (dxApi or not) is known after parsing the file

                    // in case of treetable , update the tree model first then table column model
                    updateTreeTableColumnModel();

                    CustomJTableModel tableModel = (CustomJTableModel) traceTable.getModel();
                    traceTable.setColumnModel(tableModel.getTableColumnModel());
                }
            }
        });

    }

    @Override
    protected void updateSupplementUtilityPanel() {

        JPanel supplementUtilityPanel = getSupplementUtilityPanel();

        supplementUtilityPanel.removeAll();

        Dimension spacer = new Dimension(15, 40);

        supplementUtilityPanel.add(Box.createHorizontalGlue());
        supplementUtilityPanel.add(Box.createRigidArea(spacer));
        supplementUtilityPanel.add(Box.createHorizontalGlue());

        supplementUtilityPanel.setBorder(BorderFactory.createLineBorder(MyColor.LIGHT_GRAY, 1));

        supplementUtilityPanel.revalidate();
        supplementUtilityPanel.repaint();
    }

    @Override
    protected void performComponentResized(Rectangle oldBounds, Rectangle newBounds) {

        CustomJTable customJTable = getTracerDataTable();

        TableColumnModel tableColumnModel = customJTable.getColumnModel();

        GUIUtilities.applyTableColumnResize(tableColumnModel, oldBounds, newBounds);
    }

    private JPanel getUtilityJPanel() {

        JPanel utilityJPanel = new JPanel();

        utilityJPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 0.0D;
        gbc1.weighty = 1.0D;
        gbc1.fill = GridBagConstraints.BOTH;
        gbc1.anchor = GridBagConstraints.NORTHWEST;
        gbc1.insets = new Insets(0, 0, 0, 0);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 1;
        gbc2.gridy = 0;
        gbc2.weightx = 1.0D;
        gbc2.weighty = 1.0D;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.anchor = GridBagConstraints.NORTHWEST;
        gbc2.insets = new Insets(0, 0, 0, 0);

        JPanel tracerUtilsJPanel = getTracerUtilsJPanel();

        utilityJPanel.add(searchPanel, gbc1);
        utilityJPanel.add(tracerUtilsJPanel, gbc2);

        return utilityJPanel;
    }

    private JPanel getTracerDataJPanel() {

        // table can be tree or table
        CustomJTable tracerDataTable = getTracerDataTable();

        JPanel traceTableJPanel = new JPanel();
        traceTableJPanel.setLayout(new BorderLayout());

        JScrollPane traceTableScrollpane = new JScrollPane(tracerDataTable,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        tracerDataTable.setPreferredScrollableViewportSize(traceTableScrollpane.getPreferredSize());

        // use the stored traceTableModel, as table can be tree or table
        TraceTableModel traceTableModel = getTraceTableModel();

        JPanel markerBarPanel = getMarkerBarPanel(traceTableModel);

        traceTableJPanel.add(traceTableScrollpane, BorderLayout.CENTER);
        traceTableJPanel.add(markerBarPanel, BorderLayout.EAST);

        return traceTableJPanel;
    }

    private JPanel getStatusBarJPanel() {

        JPanel statusBarJPanel = new JPanel();

        LayoutManager layout = new BoxLayout(statusBarJPanel, BoxLayout.LINE_AXIS);
        statusBarJPanel.setLayout(layout);

        Dimension spacer = new Dimension(5, 16);

        JTextField statusBar = getStatusBar();

        statusBarJPanel.add(Box.createRigidArea(spacer));
        statusBarJPanel.add(statusBar);
        statusBarJPanel.add(Box.createRigidArea(spacer));

        statusBarJPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        return statusBarJPanel;

    }

    private JPanel getTracerUtilsJPanel() {

        JPanel tracerReportJPanel = new JPanel();

        LayoutManager layout = new BoxLayout(tracerReportJPanel, BoxLayout.LINE_AXIS);
        tracerReportJPanel.setLayout(layout);

        JPanel additionalUtilityPanel = getAdditionalUtilityPanel();

        Dimension dim = new Dimension(5, 40);

        tracerReportJPanel.add(Box.createHorizontalGlue());
        tracerReportJPanel.add(Box.createRigidArea(dim));

        if (additionalUtilityPanel != null) {
            tracerReportJPanel.add(additionalUtilityPanel);
            tracerReportJPanel.add(Box.createRigidArea(dim));
        }

        tracerReportJPanel.add(Box.createHorizontalGlue());

        tracerReportJPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        return tracerReportJPanel;
    }

    protected JTextField getStatusBar() {

        if (statusBar == null) {
            statusBar = new JTextField();
            statusBar.setEditable(false);
            statusBar.setBackground(null);
            statusBar.setBorder(null);
            statusBar.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        }

        return statusBar;
    }
}
