/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.report;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.pega.gcs.tracerviewer.TraceTableModel;
import com.pega.gcs.tracerviewer.TraceTableModelColumn;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TracerReportTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -1734090615883541481L;

    private TraceTableModelColumn[] tracerReportTableColumns;

    private List<TraceEventKey> traceEventKeyList;

    private TraceTableModel traceTableModel;

    public TracerReportTableModel(List<TraceEventKey> traceEventKeyList, TraceTableModel traceTableModel) {
        super();
        this.traceEventKeyList = traceEventKeyList;
        this.traceTableModel = traceTableModel;

        this.tracerReportTableColumns = TraceTableModelColumn.getReportTraceTableModelColumnArray();
    }

    @Override
    public int getRowCount() {
        return traceEventKeyList.size();
    }

    @Override
    public int getColumnCount() {
        return tracerReportTableColumns.length;
    }

    @Override
    public String getColumnName(int column) {
        return getColumn(column).getName();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        TraceEventKey traceEventKey = getTraceEventKey(rowIndex);

        TraceEvent traceEvent = traceTableModel.getTraceEventForKey(traceEventKey);

        return traceEvent;
    }

    public TraceTableModelColumn getColumn(int columnIndex) {
        return tracerReportTableColumns[columnIndex];
    }

    public String getColumnValue(TraceEvent traceEvent, int columnIndex) {

        String columnValue = null;

        TraceTableModelColumn traceTableModelColumn = getColumn(columnIndex);

        columnValue = traceEvent.getColumnValueForTraceTableModelColumn(traceTableModelColumn);

        return columnValue;
    }

    public TraceEventKey getTraceEventKey(int rowIndex) {
        TraceEventKey traceEventKey = traceEventKeyList.get(rowIndex);
        return traceEventKey;
    }
}
