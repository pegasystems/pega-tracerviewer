/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
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
    public void mouseClicked(MouseEvent mouseEvent) {

        if (SwingUtilities.isRightMouseButton(mouseEvent)) {

            final List<Integer> selectedRowList = new ArrayList<Integer>();

            final TraceTable source = (TraceTable) mouseEvent.getSource();

            if (isIntendedSource(source)) {

                int[] selectedRows = source.getSelectedRows();

                // in case the row was not selected when right clicking then
                // based on the point, select the row.
                Point point = mouseEvent.getPoint();

                if ((selectedRows != null) && (selectedRows.length <= 1)) {

                    int selectedRow = source.rowAtPoint(point);

                    if (selectedRow != -1) {
                        // select the row first
                        source.setRowSelectionInterval(selectedRow, selectedRow);
                        selectedRows = new int[] { selectedRow };
                    }
                }

                for (int selectedRow : selectedRows) {
                    selectedRowList.add(selectedRow);
                }

                int size = selectedRowList.size();

                if (size > 0) {

                    JPopupMenu popupMenu = new JPopupMenu();

                    // expected menus - so that they can be added as per order in the last
                    RightClickMenuItem copyEventXMLMenuItem = null;

                    copyEventXMLMenuItem = getCopyEventXMLRightClickMenuItem(popupMenu, selectedRowList);

                    popupMenu.add(copyEventXMLMenuItem);

                    popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
                }
            }

        } else if (mouseEvent.getClickCount() == 2) {

            TraceTable source = (TraceTable) mouseEvent.getSource();

            performDoubleClick(source);

        } else {
            super.mouseClicked(mouseEvent);
        }
    }

}
