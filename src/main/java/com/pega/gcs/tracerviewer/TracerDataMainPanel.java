/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.pega.gcs.fringecommon.guiutilities.BaseFrame;
import com.pega.gcs.fringecommon.guiutilities.GUIUtilities;
import com.pega.gcs.fringecommon.guiutilities.GoToLineDialog;
import com.pega.gcs.fringecommon.guiutilities.Message;
import com.pega.gcs.fringecommon.guiutilities.Message.MessageType;
import com.pega.gcs.fringecommon.guiutilities.ModalProgressMonitor;
import com.pega.gcs.fringecommon.guiutilities.RecentFile;
import com.pega.gcs.fringecommon.guiutilities.RecentFileContainer;
import com.pega.gcs.fringecommon.guiutilities.search.SearchData;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.fringecommon.utilities.GeneralUtilities;
import com.pega.gcs.tracerviewer.model.TraceEventKey;
import com.pega.gcs.tracerviewer.report.TracerSimpleReportFrame;
import com.pega.gcs.tracerviewer.view.TracerDataCompareTableView;
import com.pega.gcs.tracerviewer.view.TracerDataSingleTableView;
import com.pega.gcs.tracerviewer.view.TracerDataTreeMergedTableView;
import com.pega.gcs.tracerviewer.view.TracerDataTreeTableView;
import com.pega.gcs.tracerviewer.view.TracerDataView;
import com.pega.gcs.tracerviewer.view.TracerDataViewMode;

public class TracerDataMainPanel extends JPanel {

    private static final long serialVersionUID = 5238167301628481200L;

    private static final Log4j2Helper LOG = new Log4j2Helper(TracerDataMainPanel.class);

    private RecentFileContainer recentFileContainer;

    private TraceTableModel traceTableModel;

    // required for supplementUtilityPanel to display as required by the View
    private HashMap<String, TracerDataView> tracerDataViewMap;

    private JComboBox<TracerDataViewMode> tracerDataViewModeComboBox;

    private JTextField incompleteTracerLabel;

    private TracerViewerSetting tracerViewerSetting;

    private JTextField charsetLabel;

    private JTextField fileSizeLabel;

    private JPanel tracerDataViewCardPanel;

    private JPanel supplementUtilityPanel;

    private JButton gotoLineButton;

    private JButton reloadButton;

    private JButton overviewButton;

    private TracerSimpleReportFrame tracerSimpleReportFrame;

    private TraceNavigationTableController traceNavigationTableController;

    public TracerDataMainPanel(File selectedFile, RecentFileContainer recentFileContainer,
            TracerViewerSetting tracerViewerSetting) {

        super();

        this.recentFileContainer = recentFileContainer;
        this.tracerViewerSetting = tracerViewerSetting;

        String charset = tracerViewerSetting.getCharset();

        RecentFile recentFile = recentFileContainer.getRecentFile(selectedFile, charset);

        SearchData<TraceEventKey> searchData = new SearchData<>(SearchEventType.values());

        this.traceTableModel = new TraceTableModel(recentFile, searchData);

        // moving bookmark loading to end of file load, so that bookmarked key are avilable in model.

        // BookmarkContainer<TraceEventKey> bookmarkContainer;
        // bookmarkContainer = (BookmarkContainer<TraceEventKey>) recentFile.getAttribute(RecentFile.KEY_BOOKMARK);
        //
        // if (bookmarkContainer == null) {
        //
        // bookmarkContainer = new BookmarkContainer<TraceEventKey>();
        //
        // recentFile.setAttribute(RecentFile.KEY_BOOKMARK, bookmarkContainer);
        // }
        //
        // bookmarkContainer = (BookmarkContainer<TraceEventKey>) recentFile.getAttribute(RecentFile.KEY_BOOKMARK);
        //
        // BookmarkModel<TraceEventKey> bookmarkModel = new BookmarkModel<TraceEventKey>(bookmarkContainer,
        // traceTableModel);
        //
        // traceTableModel.setBookmarkModel(bookmarkModel);

        traceNavigationTableController = new TraceNavigationTableController(traceTableModel);

        tracerDataViewMap = new HashMap<String, TracerDataView>();

        traceTableModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent tableModelEvent) {
                updateDisplayPanel();
                updateTracerDataViewModeComboBox();
            }
        });

        setLayout(new GridBagLayout());

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 1.0D;
        gbc1.weighty = 0.0D;
        gbc1.fill = GridBagConstraints.BOTH;
        gbc1.anchor = GridBagConstraints.NORTHWEST;
        gbc1.insets = new Insets(0, 0, 0, 0);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 1;
        gbc2.weightx = 1.0D;
        gbc2.weighty = 1.0D;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.anchor = GridBagConstraints.NORTHWEST;
        gbc2.insets = new Insets(0, 0, 0, 0);

        JPanel utilityCompositePanel = getUtilityCompositePanel();
        JPanel tracerDataViewCardJPanel = getTracerDataViewCardPanel();

        add(utilityCompositePanel, gbc1);
        add(tracerDataViewCardJPanel, gbc2);

        // set default view
        JComboBox<TracerDataViewMode> tracerDataViewModeJComboBox = getTracerDataViewModeComboBox();

        // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4699927
        // tree need to be built once the root node has some child nodes. hence
        // setting the default to single table
        tracerDataViewModeJComboBox.setSelectedItem(TracerDataViewMode.SINGLE_TABLE);

        loadFile(traceTableModel, this, false);

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#removeNotify()
     */
    @Override
    public void removeNotify() {
        super.removeNotify();

        clearJDialogList();
    }

    protected TraceTableModel getTraceTableModel() {
        return traceTableModel;
    }

    protected TraceNavigationTableController getTraceNavigationTableController() {
        return traceNavigationTableController;
    }

    private JPanel getUtilityCompositePanel() {

        JPanel utilityCompositePanel = new JPanel();

        utilityCompositePanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 0.0D;
        gbc1.weighty = 1.0D;
        gbc1.fill = GridBagConstraints.BOTH;
        gbc1.anchor = GridBagConstraints.NORTHWEST;
        gbc1.insets = new Insets(0, 0, 0, 0);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 1;
        gbc2.gridy = 0;
        gbc2.weightx = 1.0D;
        gbc2.weighty = 1.0D;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.anchor = GridBagConstraints.NORTHWEST;
        gbc2.insets = new Insets(0, 0, 0, 0);

        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.gridx = 2;
        gbc3.gridy = 0;
        gbc3.weightx = 0.5D;
        gbc3.weighty = 1.0D;
        gbc3.fill = GridBagConstraints.BOTH;
        gbc3.anchor = GridBagConstraints.NORTHWEST;
        gbc3.insets = new Insets(0, 0, 0, 0);

        JPanel utilityPanel = getUtilityPanel();
        JPanel supplementUtilityPanel = getSupplementUtilityPanel();
        JPanel infoPanel = getInfoPanel();

        utilityCompositePanel.add(utilityPanel, gbc1);
        utilityCompositePanel.add(supplementUtilityPanel, gbc2);
        utilityCompositePanel.add(infoPanel, gbc3);

        return utilityCompositePanel;
    }

    private JPanel getUtilityPanel() {

        JPanel utilityPanel = new JPanel();

        LayoutManager layout = new BoxLayout(utilityPanel, BoxLayout.LINE_AXIS);
        utilityPanel.setLayout(layout);

        Dimension spacer = new Dimension(10, 40);

        JLabel tracerDataViewModeLabel = new JLabel("Select view: ");

        JComboBox<TracerDataViewMode> tracerDataViewModeComboBox = getTracerDataViewModeComboBox();
        JButton gotoLineButton = getGotoLineButton();
        JButton overviewButton = getOverviewButton();
        JButton reloadButton = getReloadButton();

        utilityPanel.add(Box.createRigidArea(spacer));
        utilityPanel.add(tracerDataViewModeLabel);
        utilityPanel.add(Box.createRigidArea(spacer));
        utilityPanel.add(tracerDataViewModeComboBox);
        utilityPanel.add(Box.createRigidArea(spacer));
        utilityPanel.add(gotoLineButton);
        utilityPanel.add(Box.createRigidArea(spacer));
        utilityPanel.add(overviewButton);
        utilityPanel.add(Box.createRigidArea(spacer));
        utilityPanel.add(reloadButton);
        utilityPanel.add(Box.createRigidArea(spacer));

        utilityPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        return utilityPanel;
    }

    private JPanel getInfoPanel() {

        JPanel infoPanel = new JPanel();

        LayoutManager layout = new BoxLayout(infoPanel, BoxLayout.X_AXIS);
        infoPanel.setLayout(layout);

        JPanel incompleteTracerPanel = getIncompleteTracerPanel();
        JPanel charsetPanel = getCharsetPanel();
        JPanel sizePanel = getFileSizePanel();

        infoPanel.add(incompleteTracerPanel);
        infoPanel.add(charsetPanel);
        infoPanel.add(sizePanel);

        return infoPanel;
    }

    private JPanel getIncompleteTracerPanel() {

        JTextField incompleteTracerLabel = getIncompleteTracerLabel();
        JPanel incompleteTracerJPanel = getMetadataPanel(incompleteTracerLabel);

        return incompleteTracerJPanel;
    }

    private JPanel getCharsetPanel() {

        JTextField charsetLabel = getCharsetLabel();
        JPanel charsetPanel = getMetadataPanel(charsetLabel);

        return charsetPanel;
    }

    private JPanel getFileSizePanel() {

        JTextField fileSizeLabel = getFileSizeLabel();
        JPanel fileSizePanel = getMetadataPanel(fileSizeLabel);

        return fileSizePanel;
    }

    private JPanel getMetadataPanel(Component metadataLabel) {

        JPanel metadataPanel = new JPanel();

        LayoutManager layout = new BoxLayout(metadataPanel, BoxLayout.X_AXIS);
        metadataPanel.setLayout(layout);

        Dimension dim = new Dimension(10, 40);

        metadataPanel.add(Box.createHorizontalGlue());
        metadataPanel.add(Box.createRigidArea(dim));
        metadataPanel.add(metadataLabel);
        metadataPanel.add(Box.createRigidArea(dim));
        metadataPanel.add(Box.createHorizontalGlue());

        metadataPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        return metadataPanel;
    }

    protected JPanel getSupplementUtilityPanel() {

        if (supplementUtilityPanel == null) {

            supplementUtilityPanel = new JPanel();
            LayoutManager layout = new BoxLayout(supplementUtilityPanel, BoxLayout.LINE_AXIS);
            supplementUtilityPanel.setLayout(layout);
        }

        return supplementUtilityPanel;
    }

    private JPanel getTracerDataViewCardPanel() {

        if (tracerDataViewCardPanel == null) {

            TraceTableModel traceTableModel = getTraceTableModel();

            TraceNavigationTableController traceNavigationTableController = getTraceNavigationTableController();

            JPanel supplementUtilityPanel = getSupplementUtilityPanel();

            tracerDataViewCardPanel = new JPanel(new CardLayout());

            for (TracerDataViewMode tracerDataViewMode : TracerDataViewMode.values()) {

                TracerDataView tracerDataView;

                switch (tracerDataViewMode) {

                case SINGLE_TABLE:
                    tracerDataView = new TracerDataSingleTableView(traceTableModel, supplementUtilityPanel,
                            traceNavigationTableController);
                    break;

                case SINGLE_TREE:
                    tracerDataView = new TracerDataTreeTableView(traceTableModel, supplementUtilityPanel,
                            traceNavigationTableController);
                    break;

                case SINGLE_TREE_MERGED:
                    tracerDataView = new TracerDataTreeMergedTableView(traceTableModel, supplementUtilityPanel,
                            traceNavigationTableController);
                    break;

                case COMPARE_TABLE:
                    tracerDataView = new TracerDataCompareTableView(traceTableModel, supplementUtilityPanel,
                            traceNavigationTableController, recentFileContainer, tracerViewerSetting);
                    break;

                default:
                    tracerDataView = new TracerDataSingleTableView(traceTableModel, supplementUtilityPanel,
                            traceNavigationTableController);
                    break;
                }

                String tracerDataViewModeName = tracerDataViewMode.name();

                tracerDataViewMap.put(tracerDataViewModeName, tracerDataView);

                tracerDataViewCardPanel.add(tracerDataView, tracerDataViewModeName);
            }
        }

        return tracerDataViewCardPanel;
    }

    private JComboBox<TracerDataViewMode> getTracerDataViewModeComboBox() {

        if (tracerDataViewModeComboBox == null) {

            tracerDataViewModeComboBox = new JComboBox<>();

            Dimension size = new Dimension(170, 26);

            tracerDataViewModeComboBox.setPreferredSize(size);
            tracerDataViewModeComboBox.setMinimumSize(size);
            tracerDataViewModeComboBox.setMaximumSize(size);

            tracerDataViewModeComboBox.addActionListener(new ActionListener() {

                @SuppressWarnings("unchecked")
                @Override
                public void actionPerformed(ActionEvent actionEvent) {

                    JComboBox<TracerDataViewMode> tracerDataViewModeJComboBox;
                    tracerDataViewModeJComboBox = (JComboBox<TracerDataViewMode>) actionEvent.getSource();

                    TracerDataViewMode tracerDataViewMode;
                    tracerDataViewMode = (TracerDataViewMode) tracerDataViewModeJComboBox.getSelectedItem();

                    switchTracerDataViewMode(tracerDataViewMode);
                }
            });
        }

        return tracerDataViewModeComboBox;
    }

    private JTextField getIncompleteTracerLabel() {

        if (incompleteTracerLabel == null) {
            incompleteTracerLabel = GUIUtilities.getNonEditableTextField();

            Dimension size = new Dimension(170, 26);

            incompleteTracerLabel.setPreferredSize(size);
            incompleteTracerLabel.setMinimumSize(size);
            incompleteTracerLabel.setMaximumSize(size);

            incompleteTracerLabel.setForeground(Color.RED);
        }

        return incompleteTracerLabel;
    }

    private JTextField getCharsetLabel() {

        if (charsetLabel == null) {
            charsetLabel = GUIUtilities.getNonEditableTextField();
        }

        return charsetLabel;
    }

    private JTextField getFileSizeLabel() {

        if (fileSizeLabel == null) {
            fileSizeLabel = GUIUtilities.getNonEditableTextField();
        }

        return fileSizeLabel;
    }

    protected void switchTracerDataViewMode(TracerDataViewMode tracerDataViewMode) {

        if (tracerDataViewMode != null) {

            String tracerDataViewModeName = tracerDataViewMode.name();

            TracerDataView tracerDataView = tracerDataViewMap.get(tracerDataViewModeName);

            if (tracerDataView != null) {

                tracerDataView.switchToFront();

                JPanel tracerDataViewCardJPanel = getTracerDataViewCardPanel();
                CardLayout cardLayout = (CardLayout) (tracerDataViewCardJPanel.getLayout());

                cardLayout.show(tracerDataViewCardJPanel, tracerDataViewModeName);

            }
        }
    }

    protected void updateDisplayPanel() {

        LOG.info("updateDisplayPanel");
        populateDisplayPanel();
    }

    protected void populateDisplayPanel() {

        TraceTableModel traceTableModel = getTraceTableModel();

        JTextField incompleteTracerJLabel = getIncompleteTracerLabel();
        JTextField charsetLabel = getCharsetLabel();
        JTextField sizeLabel = getFileSizeLabel();

        boolean incompleteTracerXML = traceTableModel.isIncompletedTracerXML();
        String incompleteTracerStr = (incompleteTracerXML ? "Incomplete Tracer XML" : "");
        Charset charset = traceTableModel.getCharset();
        Long fileSize = traceTableModel.getFileSize();

        String fileSizeStr = null;

        if (fileSize != null) {
            fileSizeStr = GeneralUtilities.humanReadableSize(fileSize.longValue(), false);
        }

        incompleteTracerJLabel.setText(incompleteTracerStr);
        charsetLabel.setText(charset.name());
        sizeLabel.setText(fileSizeStr);
    }

    private void updateTracerDataViewModeComboBox() {

        TraceTableModel traceTableModel = getTraceTableModel();

        JComboBox<TracerDataViewMode> tracerDataViewModeComboBox = getTracerDataViewModeComboBox();

        DefaultComboBoxModel<TracerDataViewMode> defaultComboBoxModel;
        defaultComboBoxModel = (DefaultComboBoxModel<TracerDataViewMode>) tracerDataViewModeComboBox.getModel();

        defaultComboBoxModel.removeAllElements();

        boolean isMultipleDxApi = traceTableModel.isMultipleDxApi();

        TracerDataViewMode[] tracerDataViewModes = TracerDataViewMode.getTracerDataViewModeList(isMultipleDxApi);

        for (TracerDataViewMode tracerDataViewMode : tracerDataViewModes) {
            defaultComboBoxModel.addElement(tracerDataViewMode);
        }

    }

    public static void loadFile(TraceTableModel traceTableModel, Component parent, final boolean wait) {

        UIManager.put("ModalProgressMonitor.progressText", "Loading Tracer XML file");

        final ModalProgressMonitor progressMonitor = new ModalProgressMonitor(parent, "",
                "Loaded 0 trace events (0%)                                                      ", 0, 100);

        progressMonitor.setMillisToDecideToPopup(0);
        progressMonitor.setMillisToPopup(0);

        loadFile(traceTableModel, progressMonitor, wait, parent);
    }

    public static void loadFile(TraceTableModel traceTableModel, ModalProgressMonitor progressMonitor, boolean wait,
            Component parent) {

        RecentFile recentFile = traceTableModel.getRecentFile();

        if (recentFile != null) {

            TracerFileLoadTask tflt = new TracerFileLoadTask(progressMonitor, traceTableModel, wait, parent);

            tflt.execute();
            tflt.completeTask();

        } else {
            traceTableModel.setMessage(new Message(MessageType.ERROR, "No file selected for model"));
        }
    }

    private JButton getGotoLineButton() {

        if (gotoLineButton == null) {

            gotoLineButton = new JButton("Go to line");

            Dimension size = new Dimension(90, 20);
            gotoLineButton.setPreferredSize(size);

            gotoLineButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {

                    List<TraceEventKey> traceEventKeyList;
                    traceEventKeyList = getTraceTableModel().getFtmEntryKeyList();

                    // increment by 1 to get the display sequence no.
                    int startIndex = traceEventKeyList.get(0).getId() + 1;
                    int endIndex = traceEventKeyList.get(traceEventKeyList.size() - 1).getId() + 1;

                    GoToLineDialog goToLineDialog = new GoToLineDialog(startIndex, endIndex, BaseFrame.getAppIcon(),
                            TracerDataMainPanel.this);
                    goToLineDialog.setVisible(true);

                    Integer selectedIndex = goToLineDialog.getSelectedInteger();

                    if (selectedIndex != null) {

                        TraceEventKey selectedTraceEventKey = null;

                        for (TraceEventKey traceEventKey : traceEventKeyList) {

                            int traceEventKeyId = traceEventKey.getId();

                            // decrement by 1 to get actual id.
                            if ((selectedIndex.intValue() - 1) == traceEventKeyId) {
                                selectedTraceEventKey = traceEventKey;
                                break;
                            }
                        }

                        if (selectedTraceEventKey != null) {
                            getTraceNavigationTableController().scrollToKey(selectedTraceEventKey);
                            // traverseToKey(selectedTraceEventKey);
                        }
                    }
                }
            });

        }

        return gotoLineButton;
    }

    private JButton getOverviewButton() {

        if (overviewButton == null) {

            overviewButton = new JButton("Overview");
            Dimension size = new Dimension(90, 26);
            overviewButton.setPreferredSize(size);

            overviewButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {

                    JFrame tracerSimpleReportFrame = getTracerOverviewFrame();

                    tracerSimpleReportFrame.toFront();
                }
            });
        }

        return overviewButton;

    }

    private JButton getReloadButton() {

        if (reloadButton == null) {

            reloadButton = new JButton("Reload file");

            Dimension size = new Dimension(100, 26);
            reloadButton.setPreferredSize(size);

            reloadButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {

                    TraceTableModel traceTableModel = getTraceTableModel();

                    String origCharsetName = traceTableModel.getCharset().name();
                    TraceTablePanelSettingDialog traceTablePanelSettingDialog;

                    traceTablePanelSettingDialog = new TraceTablePanelSettingDialog(origCharsetName,
                            BaseFrame.getAppIcon(), TracerDataMainPanel.this);

                    boolean settingUpdated = traceTablePanelSettingDialog.isSettingUpdated();

                    if (settingUpdated) {

                        String selectedCharsetName = traceTablePanelSettingDialog.getSelectedCharsetName();

                        traceTableModel.updateRecentFile(selectedCharsetName);

                        if (origCharsetName.equals(selectedCharsetName)) {

                            populateDisplayPanel();

                        } else {
                            // charset changed, read/parse the file again

                            // clear and reset the model.
                            traceTableModel.resetModel();

                            // charset changed, read/parse the file again
                            loadFile(traceTableModel, TracerDataMainPanel.this, false);
                        }
                    }
                }
            });
        }

        return reloadButton;
    }

    protected TracerSimpleReportFrame getTracerOverviewFrame() {

        if (tracerSimpleReportFrame == null) {

            TraceTableModel traceTableModel = getTraceTableModel();
            TraceNavigationTableController traceNavigationTableController = getTraceNavigationTableController();

            tracerSimpleReportFrame = new TracerSimpleReportFrame(traceTableModel, traceNavigationTableController,
                    BaseFrame.getAppIcon(), this);

            tracerSimpleReportFrame.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosed(WindowEvent windowEvent) {
                    TracerSimpleReportFrame tracerSimpleReportFrame;
                    tracerSimpleReportFrame = getTracerOverviewFrame();

                    tracerSimpleReportFrame.destroyFrame();
                    setTracerSimpleReportFrame(null);
                }

            });
        }

        return tracerSimpleReportFrame;
    }

    protected void setTracerSimpleReportFrame(TracerSimpleReportFrame tracerSimpleReportFrame) {
        this.tracerSimpleReportFrame = tracerSimpleReportFrame;
    }

    private void clearJDialogList() {

        if (tracerSimpleReportFrame != null) {
            tracerSimpleReportFrame.dispose();
            tracerSimpleReportFrame = null;
        }
    }
}
