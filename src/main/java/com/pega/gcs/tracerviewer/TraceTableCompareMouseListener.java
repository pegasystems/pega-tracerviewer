/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.pega.gcs.fringecommon.guiutilities.RightClickMenuItem;

public class TraceTableCompareMouseListener extends TraceTableMouseListener {

    public TraceTableCompareMouseListener(Component mainWindow) {
        super(mainWindow);
    }

    @Override
    public void mouseClicked(MouseEvent event) {

        if (SwingUtilities.isRightMouseButton(event)) {

            final List<Integer> selectedRowList = new ArrayList<Integer>();

            final TraceTable source = (TraceTable) event.getSource();

            if (isIntendedSource(source)) {

                int[] selectedRows = source.getSelectedRows();

                // in case the row was not selected when right clicking then
                // based on the point, select the row.
                Point point = event.getPoint();

                if ((selectedRows != null) && (selectedRows.length <= 1)) {

                    int selectedRow = source.rowAtPoint(point);

                    if (selectedRow != -1) {
                        // select the row first
                        source.setRowSelectionInterval(selectedRow, selectedRow);
                        selectedRows = new int[] {selectedRow};
                    }
                }

                for (int selectedRow : selectedRows) {
                    selectedRowList.add(selectedRow);
                }

                int size = selectedRowList.size();

                if (size > 0) {

                    JPopupMenu popupMenu = new JPopupMenu();

                    RightClickMenuItem copyAsXML = getCopyAsXMLRightClickMenuItem(popupMenu, selectedRowList);

                    popupMenu.add(copyAsXML);

                    popupMenu.show(event.getComponent(), event.getX(), event.getY());
                }
            }

        } else if (event.getClickCount() == 2) {

            TraceTable source = (TraceTable) event.getSource();

            performDoubleClick(source);

        } else {
            super.mouseClicked(event);
        }
    }

}
