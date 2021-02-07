/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import com.pega.gcs.fringecommon.guiutilities.CustomJTable;
import com.pega.gcs.fringecommon.guiutilities.NavigationTableController;
import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTable;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TraceNavigationTableController extends NavigationTableController<TraceEventKey> {

    public TraceNavigationTableController(TraceTableModel traceTableModel) {
        super(traceTableModel);

    }

    @Override
    public void scrollToKey(TraceEventKey key) {

        TraceTableModel traceTableModel = (TraceTableModel) getFilterTableModel();

        int rowNumber = traceTableModel.getIndexOfKey(key);

        for (CustomJTable customJTable : getCustomJTableList()) {

            if (customJTable instanceof TraceTreeCombinedTable) {
                TraceEventCombinedTreeNode traceEventCombinedTreeNode = traceTableModel
                        .getTraceEventCombinedTreeNodeForKey(key);

                if (traceEventCombinedTreeNode != null) {
                    ((AbstractTreeTable) customJTable).scrollNodeToVisible(traceEventCombinedTreeNode);
                }
            } else if (customJTable instanceof TraceTreeTable) {
                TraceEventTreeNode traceEventTreeNode = traceTableModel.getTreeNodeForKey(key);

                if (traceEventTreeNode != null) {
                    ((AbstractTreeTable) customJTable).scrollNodeToVisible(traceEventTreeNode);
                }

            } else {

                if (rowNumber != -1) {
                    customJTable.setRowSelectionInterval(rowNumber, rowNumber);
                    customJTable.scrollRowToVisible(rowNumber);
                }
            }
        }
    }

}
