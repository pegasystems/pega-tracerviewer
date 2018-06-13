/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.pega.gcs.fringecommon.guiutilities.Message;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkModel;
import com.pega.gcs.fringecommon.guiutilities.markerbar.MarkerBar;
import com.pega.gcs.fringecommon.guiutilities.search.SearchMarkerModel;
import com.pega.gcs.tracerviewer.TraceNavigationTableController;
import com.pega.gcs.tracerviewer.TraceTableModel;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public abstract class TracerDataView extends JPanel implements TableModelListener {

    private static final long serialVersionUID = 3180116914194377351L;

    protected abstract void updateSupplementUtilityJPanel();

    private TraceNavigationTableController traceNavigationTableController;

    private TraceTableModel traceTableModel;

    private JPanel supplementUtilityJPanel;

    public TracerDataView(TraceTableModel traceTableModel,
            TraceNavigationTableController traceNavigationTableController, JPanel supplementUtilityJPanel) {

        super();

        this.traceTableModel = traceTableModel;
        this.traceTableModel.addTableModelListener(this);

        this.traceNavigationTableController = traceNavigationTableController;
        this.supplementUtilityJPanel = supplementUtilityJPanel;

    }

    @Override
    public void tableChanged(TableModelEvent e) {

        if (e.getType() == TableModelEvent.UPDATE) {
            revalidate();
            repaint();
        }
    }

    protected TraceTableModel getTraceTableModel() {
        return traceTableModel;
    }

    protected TraceNavigationTableController getTraceNavigationTableController() {
        return traceNavigationTableController;
    }

    protected JPanel getSupplementUtilityJPanel() {
        return supplementUtilityJPanel;
    }

    public void switchToFront() {
        updateSupplementUtilityJPanel();
    }

    protected void setMessage(JTextField statusBar, Message message) {

        if (message != null) {

            Color color = Color.BLUE;

            if (message.getMessageType().equals(Message.MessageType.ERROR)) {
                color = Color.RED;
            }

            String text = message.getText();

            statusBar.setForeground(color);
            statusBar.setText(text);
        }
    }

    protected JPanel getMarkerBarPanel(TraceTableModel traceTableModel) {

        JPanel markerBarPanel = new JPanel();
        markerBarPanel.setLayout(new BorderLayout());

        Dimension topDimension = new Dimension(16, 28);

        JLabel topSpacer = new JLabel();
        topSpacer.setPreferredSize(topDimension);
        topSpacer.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        Dimension bottomDimension = new Dimension(16, 17);

        JLabel bottomSpacer = new JLabel();
        bottomSpacer.setPreferredSize(bottomDimension);
        bottomSpacer.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        MarkerBar<TraceEventKey> markerBar = getMarkerBar(traceTableModel);

        markerBarPanel.add(topSpacer, BorderLayout.NORTH);
        markerBarPanel.add(markerBar, BorderLayout.CENTER);
        markerBarPanel.add(bottomSpacer, BorderLayout.SOUTH);

        return markerBarPanel;
    }

    private MarkerBar<TraceEventKey> getMarkerBar(TraceTableModel traceTableModel) {

        TraceNavigationTableController traceNavigationTableController = getTraceNavigationTableController();

        SearchMarkerModel<TraceEventKey> searchMarkerModel = new SearchMarkerModel<TraceEventKey>(traceTableModel);

        MarkerBar<TraceEventKey> markerBar = new MarkerBar<TraceEventKey>(traceNavigationTableController,
                searchMarkerModel);

        BookmarkModel<TraceEventKey> bookmarkModel;
        bookmarkModel = traceTableModel.getBookmarkModel();

        markerBar.addMarkerModel(bookmarkModel);

        return markerBar;
    }
}
