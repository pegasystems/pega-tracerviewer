/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.view;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.pega.gcs.fringecommon.guiutilities.CustomJTable;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTree;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTreeModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableColumn;
import com.pega.gcs.tracerviewer.TraceEventColumn;
import com.pega.gcs.tracerviewer.TraceEventTreeNode;
import com.pega.gcs.tracerviewer.TraceNavigationTableController;
import com.pega.gcs.tracerviewer.TraceTableModel;
import com.pega.gcs.tracerviewer.TraceTreeTable;
import com.pega.gcs.tracerviewer.TraceTreeTableMouseListener;
import com.pega.gcs.tracerviewer.TracerType;

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

            TreeTableColumn[] columns = getTreeTableColumnArray(false);

            DefaultTreeTableTreeModel dtttm = new DefaultTreeTableTreeModel(root, columns);

            traceTreeTable = new TraceTreeTable(dtttm, traceTableModel);

            TraceTreeTableMouseListener traceTreeTableMouseListener;
            traceTreeTableMouseListener = new TraceTreeTableMouseListener(traceTreeTable, this);

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

    @Override
    protected void updateTreeTableColumnModel() {

        TraceTreeTable traceTreeTable = (TraceTreeTable) getTracerDataTable();

        DefaultTreeTableTree defaultTreeTableTree = traceTreeTable.getTree();

        DefaultTreeTableTreeModel defaultTreeTableTreeModel = (DefaultTreeTableTreeModel) defaultTreeTableTree
                .getModel();

        TreeTableColumn[] columns = getTreeTableColumnArray(false);

        defaultTreeTableTreeModel.setColumns(columns);

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
                public void actionPerformed(ActionEvent actionEvent) {

                    JButton expandAllJButton = getExpandAllJButton();

                    TraceTreeTable traceTreeTable = (TraceTreeTable) getTracerDataTable();

                    if (EXPAND_ALL_ACTION.equals(actionEvent.getActionCommand())) {

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

    protected TreeTableColumn[] getTreeTableColumnArray(boolean combined) {

        int columnIndex = 0;
        String columnName;
        int prefColumnWidth;
        int alignment;
        Class<?> columnClass;

        TraceTableModel traceTableModel = getTraceTableModel();

        TracerType tracerType = traceTableModel.getTracerType();

        List<TraceEventColumn> traceEventColumnList = TraceEventColumn.getTraceEventColumnList(tracerType, combined);

        int size = traceEventColumnList.size();

        TreeTableColumn[] treeTableColumnArray = new TreeTableColumn[size];

        for (TraceEventColumn traceEventColumn : traceEventColumnList) {

            columnName = traceEventColumn.getName();
            prefColumnWidth = traceEventColumn.getPrefColumnWidth();
            alignment = traceEventColumn.getHorizontalAlignment();
            columnClass = traceEventColumn.getColumnClass();

            TreeTableColumn treeTableColumn = new TreeTableColumn(columnName, prefColumnWidth, alignment, columnClass);
            treeTableColumnArray[columnIndex] = treeTableColumn;
            columnIndex++;
        }

        return treeTableColumnArray;
    }
}
