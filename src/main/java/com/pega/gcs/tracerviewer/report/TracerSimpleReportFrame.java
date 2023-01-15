/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.report;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.pega.gcs.fringecommon.guiutilities.FilterTableModel;
import com.pega.gcs.fringecommon.guiutilities.NavigationTableController;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkContainerPanel;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkModel;
import com.pega.gcs.fringecommon.guiutilities.search.SearchModel;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.tracerviewer.TraceEventRule;
import com.pega.gcs.tracerviewer.TraceEventRuleset;
import com.pega.gcs.tracerviewer.TraceTableModel;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TracerSimpleReportFrame extends JFrame implements TableModelListener {

    private static final long serialVersionUID = -3013669038509576223L;

    private static final Log4j2Helper LOG = new Log4j2Helper(TracerSimpleReportFrame.class);

    private TraceTableModel traceTableModel;

    private NavigationTableController<TraceEventKey> navigationTableController;

    private JButton refreshButton;

    private JTabbedPane tracerReportTabbedPane;

    private AtomicInteger selectedTab;

    private boolean removeAction;

    public TracerSimpleReportFrame(TraceTableModel traceTableModel,
            NavigationTableController<TraceEventKey> navigationTableController, ImageIcon appIcon, Component parent) {

        super();

        this.traceTableModel = traceTableModel;
        this.navigationTableController = navigationTableController;
        this.selectedTab = new AtomicInteger(0);
        this.removeAction = false;

        traceTableModel.addTableModelListener(this);

        setTitle(traceTableModel.getModelName());

        setIconImage(appIcon.getImage());

        setPreferredSize(new Dimension(1150, 600));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        setContentPane(getMainPanel());

        pack();

        setLocationRelativeTo(parent);

        // visible should be the last step
        setVisible(true);

    }

    protected TraceTableModel getTraceTableModel() {
        return traceTableModel;
    }

    protected NavigationTableController<TraceEventKey> getNavigationTableController() {
        return navigationTableController;
    }

    @Override
    public void tableChanged(TableModelEvent tableModelEvent) {

        if (tableModelEvent.getType() == TableModelEvent.UPDATE) {
            rebuildOverview();
        }
    }

    private void rebuildOverview() {
        LOG.info("rebuildOverview()");

        JTabbedPane tracerReportTabbedPane = getTracerReportTabbedPane();

        removeAction = true;
        tracerReportTabbedPane.removeAll();
        buildTabs();
        removeAction = false;

        tracerReportTabbedPane.setSelectedIndex(selectedTab.get());

        validate();
        repaint();
    }

    public void destroyFrame() {
        traceTableModel.removeTableModelListener(this);
        setVisible(false);
    }

    private JButton getRefreshButton() {

        if (refreshButton == null) {
            refreshButton = new JButton("Refresh Overview");
            refreshButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    rebuildOverview();
                }
            });
        }

        return refreshButton;
    }

    private JTabbedPane getTracerReportTabbedPane() {

        if (tracerReportTabbedPane == null) {
            tracerReportTabbedPane = new JTabbedPane();

            tracerReportTabbedPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

            ChangeListener tabChangeListener = new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent changeEvent) {

                    if (!removeAction) {
                        JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
                        int index = sourceTabbedPane.getSelectedIndex();
                        selectedTab.set(index);
                    }
                }
            };

            tracerReportTabbedPane.addChangeListener(tabChangeListener);
        }

        return tracerReportTabbedPane;
    }

    private JPanel getMainPanel() {

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel refreshButtonPanel = getRefreshButtonPanel();
        JTabbedPane tracerReportTabbedPane = getTracerReportTabbedPane();

        mainPanel.add(refreshButtonPanel, BorderLayout.NORTH);
        mainPanel.add(tracerReportTabbedPane, BorderLayout.CENTER);

        buildTabs();

        return mainPanel;
    }

    private JPanel getRefreshButtonPanel() {

        JPanel refreshButtonPanel = new JPanel();

        LayoutManager layout = new BoxLayout(refreshButtonPanel, BoxLayout.LINE_AXIS);
        refreshButtonPanel.setLayout(layout);

        Dimension spacer = new Dimension(5, 35);

        JButton refreshButton = getRefreshButton();

        refreshButtonPanel.add(Box.createHorizontalGlue());
        refreshButtonPanel.add(Box.createRigidArea(spacer));
        refreshButtonPanel.add(refreshButton);
        refreshButtonPanel.add(Box.createRigidArea(spacer));
        refreshButtonPanel.add(Box.createHorizontalGlue());

        return refreshButtonPanel;

    }

    private void buildTabs() {
        int tabIndex = 0;

        BookmarkModel<TraceEventKey> bookmarkModel = traceTableModel.getBookmarkModel();

        List<TraceEventKey> reportFailedEventList = traceTableModel.getReportFailedEventKeyList();
        List<TraceEventKey> reportExceptionEventList = traceTableModel.getReportExceptionEventKeyList();
        List<TraceEventKey> reportAlertEventList = traceTableModel.getReportAlertEventKeyList();
        List<TraceEventKey> reportNoStartEventList = traceTableModel.getReportNoStartEventKeyList();
        List<TraceEventKey> reportNoEndEventList = traceTableModel.getReportNoEndEventKeyList();
        List<TraceEventKey> reportOwnElapsedEventList = traceTableModel.getReportOwnElapsedEventKeyList();
        List<TraceEventKey> reportBytesLengthEventList = traceTableModel.getReportBytesLengthEventKeyList();

        Object searchStrObj = traceTableModel.getSearchModel().getSearchStrObj();

        boolean containsBookmark = bookmarkModel.getMarkerCount() > 0;

        boolean containsFailure = (reportFailedEventList.size() > 0) ? true : false;

        boolean containsException = (reportExceptionEventList.size() > 0) ? true : false;

        boolean containsAlerts = (reportAlertEventList.size() > 0) ? true : false;

        boolean containsNoStartEvent = (reportNoStartEventList.size() > 0) ? true : false;

        boolean containsNoEndEvent = (reportNoEndEventList.size() > 0) ? true : false;

        boolean containsOwnElapsed = (reportOwnElapsedEventList.size() > 0) ? true : false;

        boolean containsBytesLength = (reportBytesLengthEventList.size() > 0) ? true : false;

        boolean containsSearch = (searchStrObj != null) ? true : false;

        if (containsFailure) {
            JPanel failedEventPanel = getFailedEventPanel();
            String tabText = "Failed Events";
            addTab(tabText, failedEventPanel, tabIndex);
            tabIndex++;
        }

        if (containsException) {
            JPanel exceptionEventPanel = getExceptionEventPanel();
            String tabText = "Exception Events";
            addTab(tabText, exceptionEventPanel, tabIndex);
            tabIndex++;
        }

        if (containsAlerts) {
            JPanel alertEventPanel = getAlertEventPanel();
            String tabText = "Alert Events";
            addTab(tabText, alertEventPanel, tabIndex);
            tabIndex++;
        }

        if (containsNoStartEvent) {
            JPanel noStartEventPanel = getNoStartEventPanel();
            String tabText = "No Start Events";
            addTab(tabText, noStartEventPanel, tabIndex);
            tabIndex++;
        }

        if (containsNoEndEvent) {
            JPanel noEndEventPanel = getNoEndEventPanel();
            String tabText = "No End Events";
            addTab(tabText, noEndEventPanel, tabIndex);
            tabIndex++;
        }

        if (containsOwnElapsed) {
            JPanel ownElapsedEventPanel = getOwnElapsedEventPanel();
            String tabText = "Own Elapsed Time";
            addTab(tabText, ownElapsedEventPanel, tabIndex);
            tabIndex++;
        }

        if (containsBytesLength) {
            JPanel bytesLengthEventPanel = getBytesLengthEventPanel();
            String tabText = "Trace Event Length";
            addTab(tabText, bytesLengthEventPanel, tabIndex);
            tabIndex++;
        }

        if (containsSearch) {
            JPanel searchEventJPanel = getSearchEventJPanel();
            String tabText = "Search Results";
            addTab(tabText, searchEventJPanel, tabIndex);
            tabIndex++;
        }

        if (containsBookmark) {

            JPanel bookmarkContainerPanel = getBookmarkContainerPanel();

            String tabText = "Bookmarks";
            addTab(tabText, bookmarkContainerPanel, tabIndex);
            tabIndex++;

        }

        JPanel rulesInvokedJPanel = getRulesInvokedJPanel();
        String tabText = "Rules invoked";
        addTab(tabText, rulesInvokedJPanel, tabIndex);
        tabIndex++;

    }

    private void addTab(String tabText, JPanel panel, int tabIndex) {

        JTabbedPane tracerReportTabbedPane = getTracerReportTabbedPane();

        JLabel tabLabel = new JLabel(tabText);
        Font labelFont = tabLabel.getFont();
        Font tabFont = labelFont.deriveFont(Font.BOLD, 12);
        Dimension dim = new Dimension(140, 26);
        tabLabel.setFont(tabFont);
        tabLabel.setSize(dim);
        tabLabel.setPreferredSize(dim);
        tabLabel.setHorizontalAlignment(SwingConstants.CENTER);

        tracerReportTabbedPane.addTab(tabText, panel);
        tracerReportTabbedPane.setTabComponentAt(tabIndex, tabLabel);
    }

    private BookmarkContainerPanel<TraceEventKey> getBookmarkContainerPanel() {

        BookmarkModel<TraceEventKey> bookmarkModel = traceTableModel.getBookmarkModel();

        BookmarkContainerPanel<TraceEventKey> bookmarkContainerPanel;

        bookmarkContainerPanel = new BookmarkContainerPanel<TraceEventKey>(bookmarkModel, navigationTableController) {

            private static final long serialVersionUID = 5672957295689747776L;

            @Override
            public FilterTableModel<TraceEventKey> getFilterTableModel() {
                return getTraceTableModel();
            }

        };

        return bookmarkContainerPanel;
    }

    private JPanel getFailedEventPanel() {

        String description = "List of innermost trace events that has failed status. Select an entry to select the record on the main table.";

        List<TraceEventKey> reportFailedEventList = traceTableModel.getReportFailedEventKeyList();

        JPanel failedEventPanel = getTraceReportPanel(description, reportFailedEventList);

        return failedEventPanel;
    }

    private JPanel getExceptionEventPanel() {

        String description = "List of innermost trace events that has failed with an exception. Select an entry to "
                + "select the record on the main table.";

        List<TraceEventKey> reportExceptionEventList = traceTableModel.getReportExceptionEventKeyList();

        JPanel exceptionEventPanel = getTraceReportPanel(description, reportExceptionEventList);

        return exceptionEventPanel;
    }

    private JPanel getAlertEventPanel() {

        String description = "List of Alert trace events. Select an entry to select the record on the main table.";

        List<TraceEventKey> reportAlertEventList = traceTableModel.getReportAlertEventKeyList();

        JPanel alertEventPanel = getTraceReportPanel(description, reportAlertEventList);

        return alertEventPanel;
    }

    private JPanel getNoStartEventPanel() {

        String description = "List of trace events which doesnt have corresponding 'Begin' event. Select an entry to "
                + "select the record on the main table.";
        List<TraceEventKey> reportNoStartEventList = traceTableModel.getReportNoStartEventKeyList();

        JPanel noStartEventPanel = getTraceReportPanel(description, reportNoStartEventList);

        return noStartEventPanel;
    }

    private JPanel getNoEndEventPanel() {

        String description = "List of trace events which doesnt have corresponding 'End' event. Select an entry to "
                + "select the record on the main table.";
        List<TraceEventKey> reportNoEndEventList = traceTableModel.getReportNoEndEventKeyList();

        JPanel noEndEventPanel = getTraceReportPanel(description, reportNoEndEventList);

        return noEndEventPanel;
    }

    private JPanel getOwnElapsedEventPanel() {

        String description = "List of innermost trace events sorted by 'own elapsed time' in decending order. Select "
                + "an entry to select the record on the main table.";
        List<TraceEventKey> reportOwnElapsedEventList = traceTableModel.getReportOwnElapsedEventKeyList();

        JPanel ownElapsedEventPanel = getTraceReportPanel(description, reportOwnElapsedEventList);

        return ownElapsedEventPanel;
    }

    private JPanel getBytesLengthEventPanel() {

        String description = "List of trace event lengths sorted by 'length' in decending order. Select an entry to select the record on th"
                + "e main table.";
        List<TraceEventKey> reportBytesLengthEventList = traceTableModel.getReportBytesLengthEventKeyList();

        JPanel bytesLengthEventPanel = getTraceReportPanel(description, reportBytesLengthEventList);

        return bytesLengthEventPanel;
    }

    private JPanel getSearchEventJPanel() {

        String description = "List of current search results. Select an entry to select the record on the main table.";

        SearchModel<TraceEventKey> searchModel = traceTableModel.getSearchModel();
        Object searchStrObj = searchModel.getSearchStrObj();
        List<TraceEventKey> searchEventList = searchModel.getSearchResultList(searchStrObj);

        JPanel searchEventJPanel = getTraceReportPanel(description, searchEventList);

        return searchEventJPanel;
    }

    private JPanel getRulesInvokedJPanel() {

        JPanel rulesInvokedJPanel = new JPanel(new BorderLayout());

        String text = "List all the rules invoked during the trace capture, sorted on total own elapsed time.";
        JPanel labelJPanel = getLabelJPanel(text);

        JTable rulesInvokedJTable = getRulesInvokedJTable();

        JScrollPane ownElapsedEventJTableScrollPane = new JScrollPane(rulesInvokedJTable);

        rulesInvokedJPanel.add(labelJPanel, BorderLayout.NORTH);
        rulesInvokedJPanel.add(ownElapsedEventJTableScrollPane, BorderLayout.CENTER);

        return rulesInvokedJPanel;
    }

    private TracerReportRulesTable getRulesInvokedJTable() {

        Map<TraceEventRuleset, TreeSet<TraceEventRule>> reportRulesInvokedMap = traceTableModel
                .getReportRulesInvokedMap();

        TracerReportRulesTableModel tracerReportRulesTableModel = new TracerReportRulesTableModel(
                reportRulesInvokedMap);

        TracerReportRulesTable tracerReportRulesTable;
        tracerReportRulesTable = new TracerReportRulesTable(tracerReportRulesTableModel);

        return tracerReportRulesTable;

    }

    private JPanel getTraceReportPanel(String description, List<TraceEventKey> traceEventKeyList) {

        JPanel traceReportJPanel = new JPanel(new BorderLayout());

        JPanel labelJPanel = getLabelJPanel(description);

        TraceTableModel traceTableModel = getTraceTableModel();

        NavigationTableController<TraceEventKey> navigationTableController = getNavigationTableController();

        TracerReportTableModel tracerReportTableModel = new TracerReportTableModel(traceEventKeyList, traceTableModel);

        TracerReportTable tracerReportTable = new TracerReportTable(tracerReportTableModel, navigationTableController);

        JScrollPane traceReportJTableScrollPane = new JScrollPane(tracerReportTable);

        traceReportJPanel.add(labelJPanel, BorderLayout.NORTH);
        traceReportJPanel.add(traceReportJTableScrollPane, BorderLayout.CENTER);

        return traceReportJPanel;
    }

    private JPanel getLabelJPanel(String text) {

        JPanel labelJPanel = new JPanel();

        LayoutManager layout = new BoxLayout(labelJPanel, BoxLayout.LINE_AXIS);
        labelJPanel.setLayout(layout);

        JLabel label = new JLabel(text);

        int height = 30;

        Dimension spacer = new Dimension(10, height);
        labelJPanel.add(Box.createRigidArea(spacer));
        labelJPanel.add(label);
        labelJPanel.add(Box.createHorizontalGlue());

        labelJPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return labelJPanel;

    }

}
