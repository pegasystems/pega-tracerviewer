/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.view;

import javax.swing.JPanel;

import com.pega.gcs.fringecommon.guiutilities.CustomJTable;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTreeModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableColumn;
import com.pega.gcs.tracerviewer.TraceEventCombinedTreeNode;
import com.pega.gcs.tracerviewer.TraceNavigationTableController;
import com.pega.gcs.tracerviewer.TraceTableModel;
import com.pega.gcs.tracerviewer.TraceTreeCombinedTable;
import com.pega.gcs.tracerviewer.TraceTreeTableMouseListener;

public class TracerDataTreeMergedTableView extends TracerDataTreeTableView {

    private static final long serialVersionUID = -8850804441797020315L;

    private TraceTreeCombinedTable traceTreeCombinedTable;

    public TracerDataTreeMergedTableView(TraceTableModel traceTableModel, JPanel supplementUtilityJPanel,
            TraceNavigationTableController traceNavigationTableController) {
        super(traceTableModel, supplementUtilityJPanel, traceNavigationTableController);
    }

    @Override
    protected CustomJTable getTracerDataTable() {

        if (traceTreeCombinedTable == null) {

            TraceTableModel traceTableModel = getTraceTableModel();

            TraceEventCombinedTreeNode root = traceTableModel.getRootTraceEventCombinedTreeNode();

            TreeTableColumn[] columns = getTreeTableColumnArray(true);

            DefaultTreeTableTreeModel dtttm = new DefaultTreeTableTreeModel(root, columns);

            traceTreeCombinedTable = new TraceTreeCombinedTable(dtttm, traceTableModel);

            TraceTreeTableMouseListener traceTreeTableMouseListener;
            traceTreeTableMouseListener = new TraceTreeTableMouseListener(traceTreeCombinedTable, this);

            traceTreeCombinedTable.addMouseListener(traceTreeTableMouseListener);
        }

        return traceTreeCombinedTable;
    }
}
