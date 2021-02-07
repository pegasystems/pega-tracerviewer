/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTable;
import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTableTreeModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTree;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTreeCellRenderer;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTreeModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableModelAdapter;

public class TraceTreeTable extends AbstractTreeTable {

    private static final long serialVersionUID = -7613230824104399834L;

    public TraceTreeTable(DefaultTreeTableTreeModel traceTreeTableModel, TraceTableModel traceTableModel) {

        super(traceTreeTableModel, 20, 30);

        TraceTreeTableModelAdapter traceTreeTableModelAdapter;

        traceTreeTableModelAdapter = (TraceTreeTableModelAdapter) getModel();
        traceTreeTableModelAdapter.setTraceTableModel(traceTableModel);

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    }

    @Override
    protected DefaultTreeTableTree constructTree(AbstractTreeTableTreeModel abstractTreeTableModel) {

        DefaultTreeTableTreeCellRenderer defaultTreeTableTreeCellRenderer;

        defaultTreeTableTreeCellRenderer = new DefaultTreeTableTreeCellRenderer(this);
        defaultTreeTableTreeCellRenderer.setOpenIcon(null);
        defaultTreeTableTreeCellRenderer.setClosedIcon(null);
        defaultTreeTableTreeCellRenderer.setLeafIcon(null);

        TraceTreeTableTree defaultTreeTableTree = new TraceTreeTableTree(this, abstractTreeTableModel,
                defaultTreeTableTreeCellRenderer);

        defaultTreeTableTree.setRootVisible(false);
        defaultTreeTableTree.setShowsRootHandles(true);

        return defaultTreeTableTree;
    }

    @Override
    protected TreeTableModelAdapter getTreeTableModelAdapter(DefaultTreeTableTree tree) {

        TraceTreeTableModelAdapter traceTreeTableModelAdapter;
        traceTreeTableModelAdapter = new TraceTreeTableModelAdapter(tree) {

            private static final long serialVersionUID = -3360882227416906092L;

            @Override
            public DefaultTableCellRenderer getTreeTableCellRenderer() {
                return new TraceTreeTableCellRenderer();
            }

        };

        return traceTreeTableModelAdapter;
    }
}
