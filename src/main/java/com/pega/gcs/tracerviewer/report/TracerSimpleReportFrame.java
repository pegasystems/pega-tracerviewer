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

    private JButton refreshJButton;

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

        setContentPane(getMainJPanel());

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

    private JButton getRefreshJButton() {

        if (refreshJButton == null) {
            refreshJButton = new JButton("Refresh Overview");
            refreshJButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    rebuildOverview();
                }
            });
        }

        return refreshJButton;
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

    private JPanel getMainJPanel() {

        JPanel mainJPanel = new JPanel();
        mainJPanel.setLayout(new BorderLayout());

        JPanel refreshButtonJPanel = getRefreshButtonJPanel();
        JTabbedPane tracerReportTabbedPane = getTracerReportTabbedPane();

        mainJPanel.add(refreshButtonJPanel, BorderLayout.NORTH);
        mainJPanel.add(tracerReportTabbedPane, BorderLayout.CENTER);

        buildTabs();

        return mainJPanel;
    }

    private JPanel getRefreshButtonJPanel() {

        JPanel refreshButtonJPanel = new JPanel();

        LayoutManager layout = new BoxLayout(refreshButtonJPanel, BoxLayout.LINE_AXIS);
        refreshButtonJPanel.setLayout(layout);

        Dimension spacer = new Dimension(5, 35);

        JButton refreshJButton = getRefreshJButton();

        refreshButtonJPanel.add(Box.createHorizontalGlue());
        refreshButtonJPanel.add(Box.createRigidArea(spacer));
        refreshButtonJPanel.add(refreshJButton);
        refreshButtonJPanel.add(Box.createRigidArea(spacer));
        refreshButtonJPanel.add(Box.createHorizontalGlue());

        return refreshButtonJPanel;

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

        Object searchStrObj = traceTableModel.getSearchModel().getSearchStrObj();

        boolean containsBookmark = bookmarkModel.getMarkerCount() > 0;

        boolean containsFailure = (reportFailedEventList.size() > 0) ? true : false;

        boolean containsException = (reportExceptionEventList.size() > 0) ? true : false;

        boolean containsAlerts = (reportAlertEventList.size() > 0) ? true : false;

        boolean containsNoStartEvent = (reportNoStartEventList.size() > 0) ? true : false;

        boolean containsNoEndEvent = (reportNoEndEventList.size() > 0) ? true : false;

        boolean containsOwnElapsed = (reportOwnElapsedEventList.size() > 0) ? true : false;

        boolean containsSearch = (searchStrObj != null) ? true : false;

        if (containsFailure) {
            JPanel failedEventJPanel = getFailedEventJPanel();
            String tabText = "Failed Events";
            addTab(tabText, failedEventJPanel, tabIndex);
            tabIndex++;
        }

        if (containsException) {
            JPanel exceptionEventJPanel = getExceptionEventJPanel();
            String tabText = "Exception Events";
            addTab(tabText, exceptionEventJPanel, tabIndex);
            tabIndex++;
        }

        if (containsAlerts) {
            JPanel alertEventJPanel = getAlertEventJPanel();
            String tabText = "Alert Events";
            addTab(tabText, alertEventJPanel, tabIndex);
            tabIndex++;
        }

        if (containsNoStartEvent) {
            JPanel noStartEventJPanel = getNoStartEventJPanel();
            String tabText = "No Start Events";
            addTab(tabText, noStartEventJPanel, tabIndex);
            tabIndex++;
        }

        if (containsNoEndEvent) {
            JPanel noEndEventJPanel = getNoEndEventJPanel();
            String tabText = "No End Events";
            addTab(tabText, noEndEventJPanel, tabIndex);
            tabIndex++;
        }

        if (containsOwnElapsed) {
            JPanel ownElapsedEventJPanel = getOwnElapsedEventJPanel();
            String tabText = "Own Elapsed Time";
            addTab(tabText, ownElapsedEventJPanel, tabIndex);
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

    private JPanel getFailedEventJPanel() {

        String description = "List of innermost trace events that has failed status. Select an entry to select the record on the main table.";

        List<TraceEventKey> reportFailedEventList = traceTableModel.getReportFailedEventKeyList();

        JPanel failedEventJPanel = getTraceReportJPanel(description, reportFailedEventList);

        return failedEventJPanel;
    }

    private JPanel getExceptionEventJPanel() {

        String description = "List of innermost trace events that has failed with an exception. Select an entry to "
                + "select the record on the main table.";

        List<TraceEventKey> reportExceptionEventList = traceTableModel.getReportExceptionEventKeyList();

        JPanel exceptionEventJPanel = getTraceReportJPanel(description, reportExceptionEventList);

        return exceptionEventJPanel;
    }

    private JPanel getAlertEventJPanel() {

        String description = "List of Alert trace events. Select an entry to select the record on the main table.";

        List<TraceEventKey> reportAlertEventList = traceTableModel.getReportAlertEventKeyList();

        JPanel alertEventJPanel = getTraceReportJPanel(description, reportAlertEventList);

        return alertEventJPanel;
    }

    private JPanel getNoStartEventJPanel() {

        String description = "List of trace events which doesnt have corresponding 'Begin' event. Select an entry to "
                + "select the record on the main table.";
        List<TraceEventKey> reportNoStartEventList = traceTableModel.getReportNoStartEventKeyList();

        JPanel noStartEventJPanel = getTraceReportJPanel(description, reportNoStartEventList);

        return noStartEventJPanel;
    }

    private JPanel getNoEndEventJPanel() {

        String description = "List of trace events which doesnt have corresponding 'End' event. Select an entry to "
                + "select the record on the main table.";
        List<TraceEventKey> reportNoEndEventList = traceTableModel.getReportNoEndEventKeyList();

        JPanel noEndEventJPanel = getTraceReportJPanel(description, reportNoEndEventList);

        return noEndEventJPanel;
    }

    private JPanel getOwnElapsedEventJPanel() {

        String description = "List of innermost trace events sorted by 'own elapsed time' in decending order. Select "
                + "an entry to select the record on the main table.";
        List<TraceEventKey> reportOwnElapsedEventList = traceTableModel.getReportOwnElapsedEventKeyList();

        JPanel ownElapsedEventJPanel = getTraceReportJPanel(description, reportOwnElapsedEventList);

        return ownElapsedEventJPanel;
    }

    private JPanel getSearchEventJPanel() {

        String description = "List of current search results. Select an entry to select the record on the main table.";

        SearchModel<TraceEventKey> searchModel = traceTableModel.getSearchModel();
        Object searchStrObj = searchModel.getSearchStrObj();
        List<TraceEventKey> searchEventList = searchModel.getSearchResultList(searchStrObj);

        JPanel searchEventJPanel = getTraceReportJPanel(description, searchEventList);

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

    private JPanel getTraceReportJPanel(String description, List<TraceEventKey> traceEventKeyList) {

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
