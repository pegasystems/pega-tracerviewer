/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.view;

import javax.swing.JPanel;

import com.pega.gcs.fringecommon.guiutilities.RecentFileContainer;
import com.pega.gcs.fringecommon.guiutilities.search.SearchData;
import com.pega.gcs.tracerviewer.SearchEventType;
import com.pega.gcs.tracerviewer.TraceNavigationTableController;
import com.pega.gcs.tracerviewer.TraceTable;
import com.pega.gcs.tracerviewer.TraceTableCompareModel;
import com.pega.gcs.tracerviewer.TraceTableModel;
import com.pega.gcs.tracerviewer.TracerViewerSetting;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TracerDataCompareTableView extends TracerDataCompareView {

    private static final long serialVersionUID = -6381481706035754170L;

    private TraceTable traceTableLeft;

    private TraceTable traceTableRight;

    public TracerDataCompareTableView(TraceTableModel traceTableModel, JPanel supplementUtilityJPanel,
            TraceNavigationTableController traceNavigationTableController, RecentFileContainer recentFileContainer,
            TracerViewerSetting tracerViewerSetting) {

        super(traceTableModel, supplementUtilityJPanel, traceNavigationTableController, recentFileContainer,
                tracerViewerSetting);
    }

    @Override
    protected TraceTable getTracerDataTableLeft() {

        if (traceTableLeft == null) {

            TraceTableModel traceTableModel = getTraceTableModel();

            traceTableLeft = new TraceTable(traceTableModel, false);
            traceTableLeft.setFillsViewportHeight(true);

            // mouse listener is setup in getCompareJSplitPane
        }

        return traceTableLeft;
    }

    @Override
    protected TraceTable getTracerDataTableRight() {

        if (traceTableRight == null) {

            SearchData<TraceEventKey> searchData = new SearchData<>(SearchEventType.values());

            TraceTableCompareModel traceTableCompareModel = new TraceTableCompareModel(null, searchData);

            traceTableRight = new TraceTable(traceTableCompareModel);
            traceTableRight.setFillsViewportHeight(true);

            // mouse listener is setup in getCompareJSplitPane
        }

        return traceTableRight;
    }

}
