/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.view;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.pega.gcs.fringecommon.guiutilities.CustomJTable;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTreeModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableColumn;
import com.pega.gcs.tracerviewer.TraceEventTreeNode;
import com.pega.gcs.tracerviewer.TraceNavigationTableController;
import com.pega.gcs.tracerviewer.TraceTableModel;
import com.pega.gcs.tracerviewer.TraceTableModelColumn;
import com.pega.gcs.tracerviewer.TraceTreeTable;
import com.pega.gcs.tracerviewer.TraceTreeTableMouseListener;

public class TracerDataTreeTableView extends TracerDataSingleView {

    private static final long serialVersionUID = -2065567512444191531L;

    private static final String EXPAND_ALL_ACTION = "Expand all nodes";

    private static final String COLLAPSE_ALL_ACTION = "Collapse all nodes";

    private TraceTreeTable traceTreeTable;

    private JButton expandAllJButton;

    public TracerDataTreeTableView(TraceTableModel traceTableModel, JPanel supplementUtilityJPanel,
            TraceNavigationTableController traceNavigationTableController) {
        super(traceTableModel, supplementUtilityJPanel, traceNavigationTableController);
    }

    @Override
    protected CustomJTable getTracerDataTable() {

        if (traceTreeTable == null) {

            TraceTableModel traceTableModel = getTraceTableModel();

            TraceEventTreeNode root = traceTableModel.getRootTraceEventTreeNode();

            TraceTableModelColumn[] traceTreeTableModelColumnArray;
            traceTreeTableModelColumnArray = TraceTableModelColumn.getTraceTreeTableModelColumnArray();

            TreeTableColumn[] columns = getTreeTableColumnArray(traceTreeTableModelColumnArray);

            DefaultTreeTableTreeModel dtttm = new DefaultTreeTableTreeModel(root, columns);

            traceTreeTable = new TraceTreeTable(dtttm, traceTableModel);

            TraceTreeTableMouseListener traceTreeTableMouseListener = new TraceTreeTableMouseListener(this);

            traceTreeTableMouseListener.addTraceTreeTable(traceTreeTable);

            traceTreeTable.addMouseListener(traceTreeTableMouseListener);
        }

        return traceTreeTable;
    }

    @Override
    protected JPanel getAdditionalUtilityPanel() {

        JPanel additionalUtilityPanel = new JPanel();

        LayoutManager layout = new BoxLayout(additionalUtilityPanel, BoxLayout.LINE_AXIS);

        additionalUtilityPanel.setLayout(layout);

        JButton expandAllJButton = getExpandAllJButton();

        additionalUtilityPanel.add(expandAllJButton);

        return additionalUtilityPanel;

    }

    protected JButton getExpandAllJButton() {

        if (expandAllJButton == null) {

            expandAllJButton = new JButton(EXPAND_ALL_ACTION);
            expandAllJButton.setActionCommand(EXPAND_ALL_ACTION);

            Dimension dim = new Dimension(150, 20);
            expandAllJButton.setPreferredSize(dim);
            expandAllJButton.setMaximumSize(dim);

            expandAllJButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    JButton expandAllJButton = getExpandAllJButton();

                    TraceTreeTable traceTreeTable = (TraceTreeTable) getTracerDataTable();

                    if (EXPAND_ALL_ACTION.equals(e.getActionCommand())) {

                        if (traceTreeTable != null) {
                            traceTreeTable.expandAll(true);
                        }

                        expandAllJButton.setText(COLLAPSE_ALL_ACTION);
                        expandAllJButton.setActionCommand(COLLAPSE_ALL_ACTION);

                    } else {

                        if (traceTreeTable != null) {
                            traceTreeTable.expandAll(false);
                        }

                        expandAllJButton.setText(EXPAND_ALL_ACTION);
                        expandAllJButton.setActionCommand(EXPAND_ALL_ACTION);

                    }

                }
            });
        }

        return expandAllJButton;
    }

    protected TreeTableColumn[] getTreeTableColumnArray(TraceTableModelColumn[] traceTableModelColumnArray) {

        TreeTableColumn[] columns = null;
        int columnIndex = 0;
        String columnName;
        int prefColumnWidth;
        int alignment;
        Class<?> columnClass;

        int size = traceTableModelColumnArray.length;
        columns = new TreeTableColumn[size];

        for (TraceTableModelColumn traceTableModelColumn : traceTableModelColumnArray) {

            columnName = traceTableModelColumn.getName();
            prefColumnWidth = traceTableModelColumn.getPrefColumnWidth();
            alignment = traceTableModelColumn.getHorizontalAlignment();
            columnClass = traceTableModelColumn.getColumnClass();

            TreeTableColumn column = new TreeTableColumn(columnName, prefColumnWidth, alignment, columnClass);
            columns[columnIndex] = column;
            columnIndex++;
        }

        return columns;
    }
}
