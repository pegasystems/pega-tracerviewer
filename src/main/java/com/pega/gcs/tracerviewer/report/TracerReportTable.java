/*******************************************************************************
 *  Copyright (c) 2021 Pegasystems Inc. All rights reserved.
 *
 *  Contributors:
 *      Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.report;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.pega.gcs.fringecommon.guiutilities.CustomJTable;
import com.pega.gcs.fringecommon.guiutilities.NavigationTableController;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TracerReportTable extends CustomJTable {

    private static final long serialVersionUID = -2065906088624043840L;

    public TracerReportTable(TracerReportTableModel tracerReportTableModel,
            NavigationTableController<TraceEventKey> navigationTableController) {

        super(tracerReportTableModel);

        setAutoCreateColumnsFromModel(false);

        setRowHeight(20);

        setRowSelectionAllowed(true);

        setFillsViewportHeight(true);

        TableColumnModel columnModel = tracerReportTableModel.getTableColumnModel();

        setColumnModel(columnModel);

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader tableHeader = getTableHeader();

        tableHeader.setReorderingAllowed(false);

        // bold the header
        Font existingFont = tableHeader.getFont();
        String existingFontName = existingFont.getName();
        int existFontSize = existingFont.getSize();
        Font newFont = new Font(existingFontName, Font.BOLD, existFontSize);
        tableHeader.setFont(newFont);

        final TableCellRenderer origTableCellRenderer = tableHeader.getDefaultRenderer();

        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer() {

            private static final long serialVersionUID = 2523481693501568166L;

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                JLabel origComponent = (JLabel) origTableCellRenderer.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                origComponent.setHorizontalAlignment(CENTER);

                // set header height
                Dimension dim = origComponent.getPreferredSize();
                dim.setSize(dim.getWidth(), 30);
                origComponent.setPreferredSize(dim);

                return origComponent;
            }

        };

        tableHeader.setDefaultRenderer(dtcr);

        ListSelectionModel listSelectionModel = getSelectionModel();
        listSelectionModel.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {

                if (!listSelectionEvent.getValueIsAdjusting()) {

                    int row = getSelectedRow();

                    TracerReportTableModel tracerReportTableModel;
                    tracerReportTableModel = (TracerReportTableModel) getModel();

                    TraceEventKey traceEventKey = tracerReportTableModel.getTraceEventKey(row);

                    navigationTableController.scrollToKey(traceEventKey);
                }
            }
        });
    }
}
