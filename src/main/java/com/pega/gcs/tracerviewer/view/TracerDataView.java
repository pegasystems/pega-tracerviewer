/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

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

    protected abstract void updateSupplementUtilityPanel();

    protected abstract void performComponentResized(Rectangle oldBounds, Rectangle newBounds);

    private TraceNavigationTableController traceNavigationTableController;

    private TraceTableModel traceTableModel;

    private JPanel supplementUtilityPanel;

    private Rectangle oldBounds;

    public TracerDataView(TraceTableModel traceTableModel,
            TraceNavigationTableController traceNavigationTableController, JPanel supplementUtilityJPanel) {

        super();

        this.traceTableModel = traceTableModel;
        this.traceTableModel.addTableModelListener(this);

        this.traceNavigationTableController = traceNavigationTableController;
        this.supplementUtilityPanel = supplementUtilityJPanel;

        oldBounds = new Rectangle(1915, 941);

        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent componentEvent) {

                Rectangle newBounds = componentEvent.getComponent().getBounds();

                if (!oldBounds.equals(newBounds)) {
                    try {
                        performComponentResized(oldBounds, newBounds);
                    } finally {
                        oldBounds = newBounds;
                    }
                }
            }
        });
    }

    @Override
    public void tableChanged(TableModelEvent tableModelEvent) {

        if (tableModelEvent.getType() == TableModelEvent.UPDATE) {
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

    protected JPanel getSupplementUtilityPanel() {
        return supplementUtilityPanel;
    }

    public void switchToFront() {
        updateSupplementUtilityPanel();
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
