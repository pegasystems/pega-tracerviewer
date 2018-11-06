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
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CancellationException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import com.pega.gcs.fringecommon.guiutilities.Message;
import com.pega.gcs.fringecommon.guiutilities.ModalProgressMonitor;
import com.pega.gcs.fringecommon.guiutilities.MyColor;
import com.pega.gcs.fringecommon.guiutilities.NavigationPanel;
import com.pega.gcs.fringecommon.guiutilities.NavigationPanelController;
import com.pega.gcs.fringecommon.guiutilities.NavigationTableController;
import com.pega.gcs.fringecommon.guiutilities.RecentFile;
import com.pega.gcs.fringecommon.guiutilities.RecentFileContainer;
import com.pega.gcs.fringecommon.guiutilities.Searchable.SelectedRowPosition;
import com.pega.gcs.fringecommon.guiutilities.TableCompareEntry;
import com.pega.gcs.fringecommon.guiutilities.TableWidthColumnModelListener;
import com.pega.gcs.fringecommon.guiutilities.markerbar.MarkerBar;
import com.pega.gcs.fringecommon.guiutilities.markerbar.MarkerModel;
import com.pega.gcs.fringecommon.guiutilities.search.SearchPanel;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.fringecommon.utilities.FileUtilities;
import com.pega.gcs.tracerviewer.CompareMarkerModel;
import com.pega.gcs.tracerviewer.TraceNavigationTableController;
import com.pega.gcs.tracerviewer.TraceTable;
import com.pega.gcs.tracerviewer.TraceTableCompareModel;
import com.pega.gcs.tracerviewer.TraceTableCompareMouseListener;
import com.pega.gcs.tracerviewer.TraceTableCompareTask;
import com.pega.gcs.tracerviewer.TraceTableModel;
import com.pega.gcs.tracerviewer.TracerViewer;
import com.pega.gcs.tracerviewer.TracerViewerSetting;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public abstract class TracerDataCompareView extends TracerDataView {

    private static final long serialVersionUID = -454998733253496709L;

    private static final Log4j2Helper LOG = new Log4j2Helper(TracerDataCompareView.class);

    // search panel for left table
    private SearchPanel<TraceEventKey> searchPanel;

    private RecentFileContainer recentFileContainer;

    private TracerViewerSetting tracerViewerSetting;

    private JButton fileOpenJButton;

    private MarkerBar<TraceEventKey> compareMarkerBar;

    private NavigationPanel<TraceEventKey> navigationPanel;

    private NavigationPanelController<TraceEventKey> navigationPanelController;

    protected abstract TraceTable getTracerDataTableLeft();

    protected abstract TraceTable getTracerDataTableRight();

    private JScrollPane jscrollPaneLeft;

    private JScrollPane jscrollPaneRight;

    public TracerDataCompareView(TraceTableModel traceTableModel, JPanel supplementUtilityJPanel,
            TraceNavigationTableController traceNavigationTableController, RecentFileContainer recentFileContainer,
            TracerViewerSetting tracerViewerSetting) {

        super(traceTableModel, traceNavigationTableController, supplementUtilityJPanel);

        this.recentFileContainer = recentFileContainer;
        this.tracerViewerSetting = tracerViewerSetting;

        TraceTable traceTableLeft = getTracerDataTableLeft();
        traceNavigationTableController.addCustomJTable(traceTableLeft);

        searchPanel = new SearchPanel<TraceEventKey>(traceNavigationTableController, traceTableModel.getSearchModel());

        // SearchModel<TraceEventKey> searchModel =
        // traceTableModel.getSearchModel();
        // searchModel.addSearchModelListener(searchPanel);

        setLayout(new BorderLayout());

        JSplitPane traceCompareJSplitPane = getCompareJSplitPane();
        JPanel compareMarkerJPanel = getCompareMarkerJPanel();

        add(traceCompareJSplitPane, BorderLayout.CENTER);
        add(compareMarkerJPanel, BorderLayout.EAST);

        updateSupplementUtilityJPanel();
    }

    protected RecentFileContainer getRecentFileContainer() {
        return recentFileContainer;
    }

    protected TracerViewerSetting getTracerViewerSetting() {
        return tracerViewerSetting;
    }

    @Override
    protected void updateSupplementUtilityJPanel() {

        JPanel supplementUtilityJPanel = getSupplementUtilityJPanel();

        supplementUtilityJPanel.removeAll();

        LayoutManager layout = new BoxLayout(supplementUtilityJPanel, BoxLayout.LINE_AXIS);
        supplementUtilityJPanel.setLayout(layout);

        Dimension spacer = new Dimension(5, 10);
        Dimension endspacer = new Dimension(15, 10);

        JButton compareFileOpenJButton = getFileOpenJButton();

        JPanel compareFileOpenPanel = new JPanel();
        layout = new BoxLayout(compareFileOpenPanel, BoxLayout.LINE_AXIS);

        compareFileOpenPanel.add(Box.createRigidArea(spacer));
        compareFileOpenPanel.add(compareFileOpenJButton);
        compareFileOpenPanel.add(Box.createRigidArea(spacer));
        // compareFileOpenPanel.setBorder(BorderFactory.createLineBorder(MyColor.LIGHT_GRAY,
        // 1));

        JPanel navigationPanel = getNavigationPanel();

        supplementUtilityJPanel.add(Box.createHorizontalGlue());
        supplementUtilityJPanel.add(Box.createRigidArea(endspacer));
        supplementUtilityJPanel.add(compareFileOpenPanel);
        supplementUtilityJPanel.add(navigationPanel);
        supplementUtilityJPanel.add(Box.createRigidArea(endspacer));

        supplementUtilityJPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        supplementUtilityJPanel.revalidate();
        supplementUtilityJPanel.repaint();
    }

    private JScrollPane getjscrollPaneLeft() {

        if (jscrollPaneLeft == null) {

            TraceTable traceTableLeft = getTracerDataTableLeft();

            jscrollPaneLeft = getJScrollPane(traceTableLeft);
        }

        return jscrollPaneLeft;
    }

    private JScrollPane getjscrollPaneRight() {

        if (jscrollPaneRight == null) {

            TraceTable traceTableRight = getTracerDataTableRight();

            jscrollPaneRight = getJScrollPane(traceTableRight);
        }

        return jscrollPaneRight;
    }

    private JSplitPane getCompareJSplitPane() {

        TraceTable traceTableLeft = getTracerDataTableLeft();
        TraceTable traceTableRight = getTracerDataTableRight();

        // set selection model
        traceTableRight.setSelectionModel(traceTableLeft.getSelectionModel());

        TraceTableCompareMouseListener traceTableCompareMouseListener = new TraceTableCompareMouseListener(this);

        // add combined mouse listener
        traceTableCompareMouseListener.addTraceTable(traceTableLeft);
        traceTableCompareMouseListener.addTraceTable(traceTableRight);

        traceTableLeft.addMouseListener(traceTableCompareMouseListener);
        traceTableRight.addMouseListener(traceTableCompareMouseListener);

        // setup column model listener
        TableWidthColumnModelListener tableWidthColumnModelListener;
        tableWidthColumnModelListener = new TableWidthColumnModelListener();
        tableWidthColumnModelListener.addTable(traceTableLeft);
        tableWidthColumnModelListener.addTable(traceTableRight);

        traceTableLeft.getColumnModel().addColumnModelListener(tableWidthColumnModelListener);
        traceTableRight.getColumnModel().addColumnModelListener(tableWidthColumnModelListener);

        // setup JScrollBar
        JScrollPane jscrollPaneLeft = getjscrollPaneLeft();
        JScrollPane jscrollPaneRight = getjscrollPaneRight();

        JPanel traceTablePanelLeft = getSingleTableJPanel(jscrollPaneLeft, traceTableLeft, false);
        JPanel traceTablePanelRight = getSingleTableJPanel(jscrollPaneRight, traceTableRight, true);

        JSplitPane traceCompareJSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, traceTablePanelLeft,
                traceTablePanelRight);

        traceCompareJSplitPane.setContinuousLayout(true);
        // traceCompareJComponent.setDividerLocation(260);
        traceCompareJSplitPane.setResizeWeight(0.5);

        // not movable divider
        // traceCompareJSplitPane.setEnabled(false);

        return traceCompareJSplitPane;
    }

    private JPanel getCompareMarkerJPanel() {

        JPanel compareMarkerJPanel = new JPanel();
        compareMarkerJPanel.setLayout(new BorderLayout());

        Dimension topDimension = new Dimension(16, 60);

        JLabel topSpacer = new JLabel();
        topSpacer.setPreferredSize(topDimension);
        topSpacer.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        Dimension bottomDimension = new Dimension(16, 35);

        JLabel bottomSpacer = new JLabel();
        bottomSpacer.setPreferredSize(bottomDimension);
        bottomSpacer.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        MarkerBar<TraceEventKey> compareMarkerBar = getCompareMarkerBar();

        compareMarkerJPanel.add(topSpacer, BorderLayout.NORTH);
        compareMarkerJPanel.add(compareMarkerBar, BorderLayout.CENTER);
        compareMarkerJPanel.add(bottomSpacer, BorderLayout.SOUTH);

        return compareMarkerJPanel;

    }

    private JScrollPane getJScrollPane(TraceTable traceTable) {

        JScrollPane jscrollpane = new JScrollPane(traceTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        traceTable.setPreferredScrollableViewportSize(jscrollpane.getPreferredSize());

        return jscrollpane;
    }

    private JPanel getSingleTableJPanel(JScrollPane traceTableScrollpane, TraceTable traceTable, boolean isRightSide) {

        JPanel singleTableJPanel = new JPanel();

        singleTableJPanel.setLayout(new BorderLayout());

        final JTextField statusBar = getStatusBar();

        SearchPanel<TraceEventKey> searchPanel;

        if (isRightSide) {
            searchPanel = getSearchPanel(traceTable);
        } else {
            searchPanel = this.searchPanel;
        }

        TraceTableModel traceTableModel = (TraceTableModel) traceTable.getModel();

        JPanel tracerDataJPanel = getTracerDataJPanel(traceTableModel, traceTableScrollpane);
        JPanel statusBarJPanel = getStatusBarJPanel(statusBar);

        singleTableJPanel.add(searchPanel, BorderLayout.NORTH);
        singleTableJPanel.add(tracerDataJPanel, BorderLayout.CENTER);
        singleTableJPanel.add(statusBarJPanel, BorderLayout.SOUTH);

        traceTableModel.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                String propertyName = evt.getPropertyName();

                if ("message".equals(propertyName)) {
                    Message message = (Message) evt.getNewValue();
                    setMessage(statusBar, message);
                }

            }
        });

        return singleTableJPanel;
    }

    private SearchPanel<TraceEventKey> getSearchPanel(TraceTable traceTable) {

        final TraceTableModel traceTableModel = (TraceTableModel) traceTable.getModel();

        NavigationTableController<TraceEventKey> navigationTableController;
        navigationTableController = new NavigationTableController<TraceEventKey>(traceTableModel);

        navigationTableController.addCustomJTable(traceTable);

        SearchPanel<TraceEventKey> searchPanel;
        searchPanel = new SearchPanel<TraceEventKey>(navigationTableController, traceTableModel.getSearchModel());

        return searchPanel;
    }

    private JPanel getStatusBarJPanel(JTextField statusBar) {

        JPanel statusBarJPanel = new JPanel();

        LayoutManager layout = new BoxLayout(statusBarJPanel, BoxLayout.LINE_AXIS);
        statusBarJPanel.setLayout(layout);

        Dimension spacer = new Dimension(5, 16);

        statusBarJPanel.add(Box.createRigidArea(spacer));
        statusBarJPanel.add(statusBar);
        statusBarJPanel.add(Box.createRigidArea(spacer));

        statusBarJPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        return statusBarJPanel;

    }

    protected JPanel getTracerDataJPanel(TraceTableModel traceTableModel, JScrollPane traceTableScrollpane) {

        JPanel traceTableJPanel = new JPanel();
        traceTableJPanel.setLayout(new BorderLayout());

        JPanel markerBarPanel = getMarkerBarPanel(traceTableModel);

        traceTableJPanel.add(traceTableScrollpane, BorderLayout.CENTER);
        traceTableJPanel.add(markerBarPanel, BorderLayout.EAST);

        return traceTableJPanel;
    }

    private JTextField getStatusBar() {

        JTextField statusBar = new JTextField();
        statusBar.setEditable(false);
        statusBar.setBackground(null);
        statusBar.setBorder(null);

        return statusBar;
    }

    protected JButton getFileOpenJButton() {

        if (fileOpenJButton == null) {

            fileOpenJButton = new JButton("Open tracer file for compare");

            ImageIcon ii = FileUtilities.getImageIcon(this.getClass(), "open.png");

            fileOpenJButton.setIcon(ii);

            Dimension size = new Dimension(250, 20);
            fileOpenJButton.setPreferredSize(size);
            // compareJButton.setMinimumSize(size);
            fileOpenJButton.setMaximumSize(size);
            fileOpenJButton.setHorizontalTextPosition(SwingConstants.LEADING);

            fileOpenJButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {

                    File fileChooserBase = null;

                    TraceTableModel traceTableModel = getTraceTableModel();

                    RecentFile recentFile = traceTableModel.getRecentFile();

                    // check for previous comparison folder
                    String leftPrevComparisionFilePath = (String) recentFile
                            .getAttribute(TracerViewer.RECENT_FILE_PREV_COMPARE_FILE);

                    if ((leftPrevComparisionFilePath != null) && (!"".equals(leftPrevComparisionFilePath))) {

                        File leftPrevComparisionFile = new File(leftPrevComparisionFilePath);

                        if (leftPrevComparisionFile.exists()) {
                            fileChooserBase = leftPrevComparisionFile;
                        }
                    }

                    if (fileChooserBase == null) {
                        // file open on the same folder as left file.
                        String leftFilePath = traceTableModel.getFilePath();

                        if ((leftFilePath != null) && (!"".equals(leftFilePath))) {
                            fileChooserBase = new File(leftFilePath);
                        }
                    }

                    FileFilter fileFilter = TracerViewer.getDefaultFileFilter(TracerViewer.FILE_CHOOSER_FILTER_DESC,
                            Arrays.asList(TracerViewer.FILE_CHOOSER_FILTER_EXT));

                    File file = TracerViewer.openFileChooser(getFileOpenJButton(), TracerViewer.class,
                            TracerViewer.FILE_CHOOSER_DIALOG_TITLE, fileFilter, fileChooserBase);

                    if (file != null) {

                        // for compare tree, the compare tree view should be
                        // inherited from compare table view, so that the same
                        // right hand data get passed over.
                        TraceTable tracerDataTableRight = getTracerDataTableRight();
                        TraceTableCompareModel traceTableCompareModel = (TraceTableCompareModel) tracerDataTableRight
                                .getModel();

                        RecentFileContainer recentFileContainer = getRecentFileContainer();
                        String charset = getTracerViewerSetting().getCharset();

                        RecentFile compareRecentFile;
                        compareRecentFile = recentFileContainer.getRecentFile(file, charset);

                        // also reset the model and clears old stuff
                        traceTableCompareModel.setRecentFile(compareRecentFile);

                        // save the compare file path to main file
                        recentFile.setAttribute(TracerViewer.RECENT_FILE_PREV_COMPARE_FILE, file.getAbsolutePath());

                        TraceTable tracerDataTableLeft = getTracerDataTableLeft();

                        UIManager.put("ModalProgressMonitor.progressText", "Loading Tracer XML file");

                        final ModalProgressMonitor progressMonitor = new ModalProgressMonitor(
                                TracerDataCompareView.this, "", "Loaded 0 trace events (0%)", 0, 100);

                        progressMonitor.setMillisToDecideToPopup(0);
                        progressMonitor.setMillisToPopup(0);

                        TraceTableCompareTask traceTableCompareTask = new TraceTableCompareTask(traceTableModel,
                                tracerDataTableLeft, tracerDataTableRight, progressMonitor,
                                TracerDataCompareView.this) {

                            @Override
                            protected void done() {

                                try {

                                    get();

                                    if (!isCancelled()) {

                                        TraceTable tracerDataTableLeft = getTracerDataTableLeft();
                                        TraceTable tracerDataTableRight = getTracerDataTableRight();

                                        TraceTableCompareModel traceTableCompareModelLeft;
                                        traceTableCompareModelLeft = (TraceTableCompareModel) tracerDataTableLeft
                                                .getModel();

                                        TraceTableCompareModel traceTableCompareModelRight;
                                        traceTableCompareModelRight = (TraceTableCompareModel) tracerDataTableRight
                                                .getModel();

                                        getNavigationPanel().setEnabled(true);

                                        MarkerModel<TraceEventKey> thisMarkerModel;
                                        thisMarkerModel = new CompareMarkerModel(MyColor.LIGHT_GREEN,
                                                traceTableCompareModelLeft);

                                        MarkerModel<TraceEventKey> otherMarkerModel;
                                        otherMarkerModel = new CompareMarkerModel(Color.LIGHT_GRAY,
                                                traceTableCompareModelRight);

                                        MarkerBar<TraceEventKey> markerBar = getCompareMarkerBar();
                                        markerBar.addMarkerModel(thisMarkerModel);
                                        markerBar.addMarkerModel(otherMarkerModel);

                                        syncScrollBars();
                                        getNavigationPanelController().updateState();
                                    }
                                } catch (CancellationException ce) {
                                    LOG.info("TraceTableCompareTask cancelled: ");

                                } catch (Exception e) {
                                    JOptionPane.showMessageDialog(TracerDataCompareView.this,
                                            "Error in comparing the tracer xmls.", "Compare Tracer XML Error",
                                            JOptionPane.ERROR_MESSAGE);
                                    LOG.error("Exception in TraceTableCompareTask.", e);

                                } finally {
                                    progressMonitor.close();
                                }
                            }
                        };

                        traceTableCompareTask.execute();

                        // TracerDataMainPanel.loadFile(traceTableCompareModel,
                        // TracerDataCompareView.this, true);
                        //
                        // applyTraceModelCompare();
                    }
                }
            });
        }

        return fileOpenJButton;
    }

    protected MarkerBar<TraceEventKey> getCompareMarkerBar() {

        if (compareMarkerBar == null) {
            NavigationPanelController<TraceEventKey> navigationPanelController = getNavigationPanelController();
            compareMarkerBar = new MarkerBar<TraceEventKey>(navigationPanelController, null);
        }

        return compareMarkerBar;
    }

    protected NavigationPanel<TraceEventKey> getNavigationPanel() {

        if (navigationPanel == null) {

            JLabel label = new JLabel("Compare:");

            NavigationPanelController<TraceEventKey> compareNavigationPanelController = getNavigationPanelController();

            navigationPanel = new NavigationPanel<>(label, compareNavigationPanelController);
            navigationPanel.setEnabled(false);

        }

        return navigationPanel;
    }

    protected NavigationPanelController<TraceEventKey> getNavigationPanelController() {

        if (navigationPanelController == null) {

            navigationPanelController = new NavigationPanelController<TraceEventKey>() {

                @Override
                public void navigateToRow(int startRowIndex, int endRowIndex) {

                    TraceTable traceTableLeft = getTracerDataTableLeft();
                    TraceTable traceTableRight = getTracerDataTableRight();

                    traceTableLeft.setRowSelectionInterval(startRowIndex, endRowIndex);
                    traceTableLeft.scrollRowToVisible(startRowIndex);

                    traceTableRight.setRowSelectionInterval(startRowIndex, endRowIndex);
                    traceTableRight.scrollRowToVisible(startRowIndex);

                    traceTableLeft.updateUI();
                    traceTableRight.updateUI();
                }

                @Override
                public void updateState() {

                    NavigationPanel<TraceEventKey> navigationPanel = getNavigationPanel();

                    JLabel dataJLabel = navigationPanel.getDataJLabel();
                    JButton firstJButton = navigationPanel.getFirstJButton();
                    JButton prevJButton = navigationPanel.getPrevJButton();
                    JButton nextJButton = navigationPanel.getNextJButton();
                    JButton lastJButton = navigationPanel.getLastJButton();

                    TraceTable traceTableLeft = getTracerDataTableLeft();
                    int[] selectedrows = traceTableLeft.getSelectedRows();

                    int selectedRow = -1;

                    if (selectedrows.length > 0) {
                        selectedRow = selectedrows[selectedrows.length - 1];
                    }

                    TraceTableCompareModel traceTableCompareModel = (TraceTableCompareModel) traceTableLeft.getModel();

                    int compareCount = traceTableCompareModel.getCompareCount();
                    int compareNavIndex = traceTableCompareModel.getCompareNavIndex();

                    SelectedRowPosition selectedRowPosition = SelectedRowPosition.NONE;

                    if (compareCount == 0) {
                        selectedRowPosition = SelectedRowPosition.NONE;
                    } else if (traceTableCompareModel.isCompareResultsWrap()) {
                        selectedRowPosition = SelectedRowPosition.BETWEEN;
                    } else {

                        selectedRow = (selectedRow >= 0) ? (selectedRow) : 0;

                        selectedRowPosition = traceTableCompareModel.getCompareSelectedRowPosition(selectedRow);
                    }

                    switch (selectedRowPosition) {

                    case FIRST:
                        firstJButton.setEnabled(false);
                        prevJButton.setEnabled(false);
                        nextJButton.setEnabled(true);
                        lastJButton.setEnabled(true);
                        break;

                    case LAST:
                        firstJButton.setEnabled(true);
                        prevJButton.setEnabled(true);
                        nextJButton.setEnabled(false);
                        lastJButton.setEnabled(false);
                        break;

                    case BETWEEN:
                        firstJButton.setEnabled(true);
                        prevJButton.setEnabled(true);
                        nextJButton.setEnabled(true);
                        lastJButton.setEnabled(true);
                        break;

                    case NONE:
                        firstJButton.setEnabled(false);
                        prevJButton.setEnabled(false);
                        nextJButton.setEnabled(false);
                        lastJButton.setEnabled(false);
                        break;
                    default:
                        break;
                    }

                    String text = String.format(SearchPanel.PAGES_FORMAT_STR, compareNavIndex, compareCount);

                    dataJLabel.setText(text);

                }

                @Override
                public void previous() {

                    TraceTable traceTableLeft = getTracerDataTableLeft();
                    int[] selectedrows = traceTableLeft.getSelectedRows();

                    int selectedRow = -1;

                    if (selectedrows.length > 0) {
                        selectedRow = selectedrows[selectedrows.length - 1];
                    }

                    TraceTableCompareModel traceTableCompareModel = (TraceTableCompareModel) traceTableLeft.getModel();

                    TableCompareEntry tableCompareEntry;
                    tableCompareEntry = traceTableCompareModel.comparePrevious(selectedRow);

                    int startEntry = tableCompareEntry.getStartEntry();
                    int endEntry = tableCompareEntry.getEndEntry();

                    navigateToRow(startEntry, endEntry);

                    updateState();

                }

                @Override
                public void next() {

                    TraceTable traceTableLeft = getTracerDataTableLeft();
                    int[] selectedrows = traceTableLeft.getSelectedRows();

                    int selectedRow = -1;

                    if (selectedrows.length > 0) {
                        selectedRow = selectedrows[selectedrows.length - 1];
                    }

                    TraceTableCompareModel traceTableCompareModel = (TraceTableCompareModel) traceTableLeft.getModel();

                    TableCompareEntry tableCompareEntry;
                    tableCompareEntry = traceTableCompareModel.compareNext(selectedRow);

                    int startEntry = tableCompareEntry.getStartEntry();
                    int endEntry = tableCompareEntry.getEndEntry();

                    navigateToRow(startEntry, endEntry);

                    updateState();

                }

                @Override
                public void last() {

                    TraceTable traceTableLeft = getTracerDataTableLeft();

                    TraceTableCompareModel traceTableCompareModel = (TraceTableCompareModel) traceTableLeft.getModel();

                    TableCompareEntry tableCompareEntry;
                    tableCompareEntry = traceTableCompareModel.compareLast();

                    int startEntry = tableCompareEntry.getStartEntry();
                    int endEntry = tableCompareEntry.getEndEntry();

                    navigateToRow(startEntry, endEntry);

                    updateState();

                }

                @Override
                public void first() {

                    TraceTable traceTableLeft = getTracerDataTableLeft();

                    TraceTableCompareModel traceTableCompareModel = (TraceTableCompareModel) traceTableLeft.getModel();

                    TableCompareEntry tableCompareEntry;
                    tableCompareEntry = traceTableCompareModel.compareFirst();

                    int startEntry = tableCompareEntry.getStartEntry();
                    int endEntry = tableCompareEntry.getEndEntry();

                    navigateToRow(startEntry, endEntry);

                    updateState();

                }

                @Override
                public void scrollToKey(TraceEventKey traceEventKey) {
                    // TODO Auto-generated method stub

                }
            };
        }

        return navigationPanelController;
    }

    protected void syncScrollBars() {

        JScrollPane jscrollPaneLeft = getjscrollPaneLeft();
        JScrollPane jscrollPaneRight = getjscrollPaneRight();

        JScrollBar jscrollBarLeftH = jscrollPaneLeft.getHorizontalScrollBar();
        JScrollBar jscrollBarLeftV = jscrollPaneLeft.getVerticalScrollBar();
        JScrollBar jscrollBarRightH = jscrollPaneRight.getHorizontalScrollBar();
        JScrollBar jscrollBarRightV = jscrollPaneRight.getVerticalScrollBar();

        jscrollBarRightH.setModel(jscrollBarLeftH.getModel());
        jscrollBarRightV.setModel(jscrollBarLeftV.getModel());
    }

}
