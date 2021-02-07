/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import javax.swing.table.DefaultTableCellRenderer;

import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTableTreeModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTree;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTreeCellRenderer;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTreeModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableModelAdapter;

public class TraceTreeCombinedTable extends TraceTreeTable {

    private static final long serialVersionUID = -5697655931346278629L;

    public TraceTreeCombinedTable(DefaultTreeTableTreeModel traceTreeTableModel, TraceTableModel traceTableModel) {
        super(traceTreeTableModel, traceTableModel);
    }

    @Override
    protected DefaultTreeTableTree constructTree(AbstractTreeTableTreeModel abstractTreeTableModel) {

        DefaultTreeTableTreeCellRenderer defaultTreeTableTreeCellRenderer;

        defaultTreeTableTreeCellRenderer = new DefaultTreeTableTreeCellRenderer(this);
        defaultTreeTableTreeCellRenderer.setOpenIcon(null);
        defaultTreeTableTreeCellRenderer.setClosedIcon(null);
        defaultTreeTableTreeCellRenderer.setLeafIcon(null);

        TraceTreeTableTree defaultTreeTableTree = new TraceTreeTableCombinedTree(this, abstractTreeTableModel,
                defaultTreeTableTreeCellRenderer);

        defaultTreeTableTree.setRootVisible(false);
        defaultTreeTableTree.setShowsRootHandles(true);

        return defaultTreeTableTree;
    }

    @Override
    protected TreeTableModelAdapter getTreeTableModelAdapter(DefaultTreeTableTree tree) {

        TraceTreeTableModelAdapter traceTreeTableModelAdapter;
        traceTreeTableModelAdapter = new TraceTreeTableModelAdapter(tree) {

            private static final long serialVersionUID = -603548020819744671L;

            @Override
            public DefaultTableCellRenderer getTreeTableCellRenderer() {
                return new TraceTreeTableCombinedCellRenderer();
            }

        };

        return traceTreeTableModelAdapter;
    }

}
