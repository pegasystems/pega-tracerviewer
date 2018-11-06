/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;

import com.pega.gcs.fringecommon.guiutilities.MyColor;
import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTable;
import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTableTreeModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTreeCellRenderer;
import com.pega.gcs.tracerviewer.model.TraceEvent;

public class TraceTreeTableCombinedTree extends TraceTreeTableTree {

    private static final long serialVersionUID = -3982884844488034849L;

    public TraceTreeTableCombinedTree(AbstractTreeTable treeTable, AbstractTreeTableTreeModel model,
            DefaultTreeTableTreeCellRenderer defaultTreeTableTreeCellRenderer) {
        super(treeTable, model, defaultTreeTableTreeCellRenderer);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        TraceEventCombinedTreeNode traceEventTreeNode = null;
        TraceEvent te = null;

        if (value instanceof TraceEventCombinedTreeNode) {
            traceEventTreeNode = (TraceEventCombinedTreeNode) value;
            te = (TraceEvent) traceEventTreeNode.getUserObject();
        }

        if (te != null) {

            String text = traceEventTreeNode.getNodeValue(column);

            super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);

            if (!table.isRowSelected(row)) {

                boolean searchFound = traceEventTreeNode.isSearchFound();
                boolean leafSearchFound = te.isSearchFound();

                if (leafSearchFound) {
                    setBackground(MyColor.LIGHT_YELLOW);
                } else if (searchFound) {
                    setBackground(MyColor.LIGHTEST_YELLOW);
                } else {
                    setBackground(te.getColumnBackground(column));
                }
            }

            // setBorder(new EmptyBorder(1, 3, 1, 1));

            // setToolTipText(text);

        } else {
            setBackground(Color.WHITE);
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        return this;
    }

}
