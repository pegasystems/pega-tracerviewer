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

import com.pega.gcs.fringecommon.guiutilities.MyColor;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventEmpty;

public class TraceTreeTableCombinedCellRenderer extends TraceTreeTableCellRenderer {

    private static final long serialVersionUID = -5521271063295036752L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        TraceEventCombinedTreeNode traceEventTreeNode = null;
        TraceEvent startEvent = null;
        TraceEvent endEvent = null;

        if (value instanceof TraceEventCombinedTreeNode) {
            traceEventTreeNode = (TraceEventCombinedTreeNode) value;
            startEvent = (TraceEvent) traceEventTreeNode.getUserObject();
        }

        if (startEvent != null) {

            String text = traceEventTreeNode.getNodeValue(column);

            boolean searchFound = false;
            boolean leafSearchFound = false;
            Color background = null;

            endEvent = traceEventTreeNode.getEndEvent();

            if ((endEvent != null) && (!(endEvent instanceof TraceEventEmpty))) {
                searchFound = traceEventTreeNode.isSearchFound();
                leafSearchFound = startEvent.isSearchFound() || endEvent.isSearchFound();
                background = endEvent.getColumnBackground(column);

            } else {
                searchFound = traceEventTreeNode.isSearchFound();
                leafSearchFound = startEvent.isSearchFound();
                background = startEvent.getColumnBackground(column);
            }

            super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);

            if (!table.isRowSelected(row)) {

                if (leafSearchFound) {
                    setBackground(MyColor.LIGHT_YELLOW);
                } else if (searchFound) {
                    setBackground(MyColor.LIGHTEST_YELLOW);
                } else {
                    setBackground(background);
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
