/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

import javax.swing.JTable;

import com.pega.gcs.fringecommon.guiutilities.FilterTable;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TraceTable extends FilterTable<TraceEventKey> {

    private static final long serialVersionUID = -3752172566327398734L;

    public TraceTable(TraceTableModel traceTableModel) {
        
        this(traceTableModel, true);
    }
    
    public TraceTable(TraceTableModel traceTableModel, boolean filterColumns) {

        super(traceTableModel, filterColumns);

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // enabling this disables the drag drop of files functionality when
        // hover over table.
        // setTransferHandler(new TransferHandler() {
        //
        // private static final long serialVersionUID = -985293381481032831L;
        //
        // @Override
        // protected Transferable createTransferable(JComponent c) {
        //
        // int[] selectedRows = getSelectedRows();
        //
        // StringBuffer dataSB = new StringBuffer();
        //
        // if (selectedRows != null) {
        //
        // for (int selectedRow : selectedRows) {
        //
        // TraceEvent traceEvent = (TraceEvent) getValueAt(selectedRow, 0);
        //
        // dataSB.append(traceEvent.getTraceEventColumnString());
        // dataSB.append(System.getProperty("line.separator"));
        // }
        //
        // }
        // return new StringSelection(dataSB.toString());
        // }
        //
        // @Override
        // public int getSourceActions(JComponent c) {
        // return TransferHandler.COPY;
        // }
        //
        // });
    }

}
