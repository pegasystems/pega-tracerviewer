/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.report;

import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.pega.gcs.fringecommon.guiutilities.CustomJTableModel;
import com.pega.gcs.tracerviewer.TraceTableModel;
import com.pega.gcs.tracerviewer.TraceEventColumn;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TracerReportTableModel extends CustomJTableModel {

    private static final long serialVersionUID = -1734090615883541481L;

    private TraceEventColumn[] tracerReportTableColumns;

    private List<TraceEventKey> traceEventKeyList;

    private TraceTableModel traceTableModel;

    public TracerReportTableModel(List<TraceEventKey> traceEventKeyList, TraceTableModel traceTableModel) {
        super();
        this.traceEventKeyList = traceEventKeyList;
        this.traceTableModel = traceTableModel;

        this.tracerReportTableColumns = TraceEventColumn.getReportTraceTableModelColumnArray();
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

    public TraceEventColumn getColumn(int columnIndex) {
        return tracerReportTableColumns[columnIndex];
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.pega.gcs.fringecommon.guiutilities.CustomJTableModel#getColumnValue(java. lang.Object, int)
     */
    @Override
    public String getColumnValue(Object valueAtObject, int columnIndex) {

        TraceEvent traceEvent = (TraceEvent) valueAtObject;

        String columnValue = null;

        TraceEventColumn traceTableModelColumn = getColumn(columnIndex);

        columnValue = traceEvent.getColumnValueForTraceTableModelColumn(traceTableModelColumn);

        return columnValue;
    }

    public TraceEventKey getTraceEventKey(int rowIndex) {
        TraceEventKey traceEventKey = traceEventKeyList.get(rowIndex);
        return traceEventKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.pega.gcs.fringecommon.guiutilities.CustomJTableModel#getTableColumnModel( )
     */
    @Override
    public TableColumnModel getTableColumnModel() {

        TableColumnModel tableColumnModel = new DefaultTableColumnModel();

        for (int i = 0; i < getColumnCount(); i++) {

            TableColumn tableColumn = new TableColumn(i);

            String text = getColumnName(i);

            tableColumn.setHeaderValue(text);

            DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer() {

                private static final long serialVersionUID = -1226451806128342507L;

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {

                    String text = null;

                    if ((value != null) && (value instanceof TraceEvent)) {

                        TraceEvent traceEvent = (TraceEvent) value;

                        text = getColumnValue(traceEvent, column);

                        if (!table.isRowSelected(row)) {

                            TraceEventColumn traceTableModelColumn = getColumn(column);

                            setBackground(traceEvent.getColumnBackground(traceTableModelColumn));
                        }

                        setHorizontalAlignment(CENTER);
                    }

                    super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);

                    return this;
                }

            };

            dtcr.setBorder(new EmptyBorder(1, 3, 1, 1));

            tableColumn.setCellRenderer(dtcr);

            TraceEventColumn trtc = getColumn(i);

            int colWidth = trtc.getPrefColumnWidth();
            tableColumn.setPreferredWidth(colWidth);
            tableColumn.setMinWidth(colWidth);
            tableColumn.setWidth(colWidth);
            tableColumn.setResizable(true);

            tableColumnModel.addColumn(tableColumn);
        }

        return tableColumnModel;
    }
}
