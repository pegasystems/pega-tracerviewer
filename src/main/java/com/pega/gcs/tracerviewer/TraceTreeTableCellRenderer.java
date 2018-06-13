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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.pega.gcs.fringecommon.guiutilities.MyColor;
import com.pega.gcs.tracerviewer.model.TraceEvent;

public class TraceTreeTableCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = -5768343434033636406L;

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent
     * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        TraceEventTreeNode traceEventTreeNode = null;
        TraceEvent te = null;

        if (value instanceof TraceEventTreeNode) {
            traceEventTreeNode = (TraceEventTreeNode) value;
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

            setBorder(new EmptyBorder(1, 3, 1, 1));

            setToolTipText(text);

        } else {
            setBackground(Color.WHITE);
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        return this;
    }

}
