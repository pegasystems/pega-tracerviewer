/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTableTreeModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTree;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTreeCellRenderer;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTreeModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableCellEditor;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableColumn;
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
    protected void setTreeTableColumnModel() {

        TreeTableModelAdapter model = (TreeTableModelAdapter) getModel();
        TableColumnModel tableColumnModel = new DefaultTableColumnModel();

        for (int i = 0; i < model.getColumnCount(); i++) {

            TableColumn tableColumn = new TableColumn(i);

            TreeTableColumn treeTableColumn = model.getColumn(i);

            String text = treeTableColumn.getDisplayName();

            Class<?> columnClass = treeTableColumn.getColumnClass();

            int preferredWidth = treeTableColumn.getPrefColumnWidth();

            tableColumn.setHeaderValue(text);
            tableColumn.setPreferredWidth(preferredWidth);

            TableCellRenderer tcr = null;

            if (TreeTableColumn.TREE_COLUMN_CLASS.equals(columnClass)) {

                DefaultTreeTableTree treeTableTree = getTree();
                tcr = treeTableTree;
                tableColumn.setCellEditor(new TreeTableCellEditor(treeTableTree));
            } else {

                TraceTreeTableCombinedCellRenderer ltcr = new TraceTreeTableCombinedCellRenderer();
                ltcr.setBorder(new EmptyBorder(1, 3, 1, 1));
                ltcr.setHorizontalAlignment(treeTableColumn.getHorizontalAlignment());

                tableColumn.setCellEditor(new TreeTableCellEditor(ltcr));

                tcr = ltcr;
            }

            tableColumn.setCellRenderer(tcr);

            tableColumn.setResizable(true);

            tableColumnModel.addColumn(tableColumn);
        }

        setColumnModel(tableColumnModel);
    }

}
