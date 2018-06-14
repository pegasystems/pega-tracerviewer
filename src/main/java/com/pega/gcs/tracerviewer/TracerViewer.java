/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import com.pega.gcs.fringecommon.guiutilities.BaseFrame;
import com.pega.gcs.fringecommon.guiutilities.RecentFile;
import com.pega.gcs.fringecommon.guiutilities.RecentFileContainer;
import com.pega.gcs.fringecommon.guiutilities.RecentFileJMenu;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.fringecommon.utilities.FileUtilities;
import com.pega.gcs.fringecommon.utilities.GeneralUtilities;
import com.pega.gcs.fringecommon.utilities.kyro.KryoSerializer;

import gnu.getopt.Getopt;

public class TracerViewer extends BaseFrame {

    private static final long serialVersionUID = -90660332725468972L;

    private static final Log4j2Helper LOG = new Log4j2Helper(TracerViewer.class);

    public static final String FILE_CHOOSER_FILTER_DESC = "Tracer XML Files";

    public static final String[] FILE_CHOOSER_FILTER_EXT = { "xml" };

    public static final String FILE_CHOOSER_DIALOG_TITLE = "Select Tracer File";

    public static final String RECENT_FILE_PREV_COMPARE_FILE = "prevCompareFile";

    private String appName;

    private RecentFileJMenu recentFileJMenu;

    private RecentFileContainer recentFileContainer;

    private TraceTabbedPane traceTabbedPane;

    // storing selected file so that open dialog known the last opened folder
    private File selectedFile;

    private TracerViewerSetting tracerViewerSetting;

    private ArrayList<String> openFileList;

    protected TracerViewer() throws Exception {

        super();

        // setPreferredSize(new Dimension(1200, 700));

        setFocusTraversalKeysEnabled(false);

        pack();

        // setLocationRelativeTo(null);
        setExtendedState(Frame.MAXIMIZED_BOTH);

        setVisible(true);

        // openFileList is loaded in initialize method
        if ((openFileList != null) && (openFileList.size() > 0)) {
            loadFile(openFileList);
        }

        LOG.info("TracerViewer - Started");
    }

    protected File getSelectedFile() {
        return selectedFile;
    }

    protected TracerViewerSetting getTracerViewerSetting() {
        return tracerViewerSetting;
    }

    protected RecentFileContainer getRecentFileContainer() {
        return recentFileContainer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.pega.fringe.common.gui.BaseFrame#initialize()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void initialize() throws Exception {

        byte[] byteArray;

        // get TracerViewerSetting
        byteArray = GeneralUtilities.getPreferenceByteArray(TracerViewer.class, PREF_SETTINGS);

        if (byteArray != null) {
            try {
                tracerViewerSetting = KryoSerializer.decompress(byteArray, TracerViewerSetting.class);
            } catch (Exception exception) {
                LOG.error("Error in decompressing tracerviewersetting", exception);
            }
        }

        if (tracerViewerSetting == null) {
            tracerViewerSetting = new TracerViewerSetting();
            byteArray = KryoSerializer.compress(tracerViewerSetting);
            GeneralUtilities.setPreferenceByteArray(TracerViewer.class, PREF_SETTINGS, byteArray);
        }

        // get OpenFileList
        byteArray = GeneralUtilities.getPreferenceByteArray(TracerViewer.class, PREF_OPEN_FILE_LIST);

        if (byteArray != null) {
            try {
                openFileList = KryoSerializer.decompress(byteArray, ArrayList.class);
            } catch (Exception exception) {
                LOG.error("Error decompressing open file list.", exception);
            }
        }

        if (openFileList == null) {
            openFileList = new ArrayList<>();
            byteArray = KryoSerializer.compress(openFileList);
            GeneralUtilities.setPreferenceByteArray(TracerViewer.class, PREF_OPEN_FILE_LIST, byteArray);
        }

        int capacity = tracerViewerSetting.getRecentItemsCount();

        recentFileContainer = new RecentFileContainer(getClass(), capacity);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.pega.fringe.common.gui.BaseFrame#getMainJPanel()
     */
    @Override
    protected JComponent getMainJPanel() {

        JPanel mainJPanel = new JPanel();

        TraceTabbedPane traceTabbedPane = getTraceTabbedPane();

        mainJPanel.setLayout(new BorderLayout());

        mainJPanel.add(traceTabbedPane, BorderLayout.CENTER);

        return mainJPanel;

    }

    private TraceTabbedPane getTraceTabbedPane() {

        if (traceTabbedPane == null) {

            traceTabbedPane = new TraceTabbedPane(tracerViewerSetting, recentFileContainer);
            traceTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

            // logTabbedPane.setBorder(BorderFactory.createLineBorder(
            // MyColor.GRAY, 1));
        }
        return traceTabbedPane;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.pega.fringe.common.gui.BaseFrame#getAppName()
     */
    @Override
    protected String getAppName() {

        if (appName == null) {

            Package apackage = TracerViewer.class.getPackage();

            StringBuffer sb = new StringBuffer();
            sb.append(apackage.getImplementationTitle());
            sb.append(" ");
            sb.append(apackage.getImplementationVersion());

            appName = sb.toString();
        }

        return appName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.pega.fringe.common.gui.BaseFrame#release()
     */
    @Override
    protected void release() {
        savePreferences();
    }

    protected void savePreferences() {

        recentFileContainer.saveRecentFilesPreferrence();

        // save TracerViewerSetting
        try {
            byte[] byteArray = KryoSerializer.compress(tracerViewerSetting);

            LOG.info("TracerViewerSetting ByteSize: " + byteArray.length);

            GeneralUtilities.setPreferenceByteArray(this.getClass(), PREF_SETTINGS, byteArray);
        } catch (Exception exception) {
            LOG.error("Error saving preferences.", exception);
        }

        // save openFileList
        saveOpenFileList();

    }

    private void saveOpenFileList() {

        try {

            boolean reloadPreviousFiles = tracerViewerSetting.isReloadPreviousFiles();

            if (reloadPreviousFiles) {
                TraceTabbedPane traceTabbedPane = getTraceTabbedPane();
                openFileList = traceTabbedPane.getOpenFileList();
            } else {
                openFileList = new ArrayList<>();
            }

            byte[] byteArray = KryoSerializer.compress(openFileList);

            LOG.info("Open File List ByteSize: " + byteArray.length);

            GeneralUtilities.setPreferenceByteArray(this.getClass(), PREF_OPEN_FILE_LIST, byteArray);

        } catch (Exception exception) {
            LOG.error("Error compressing open file list.", exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.pega.fringe.common.gui.BaseFrame#getMenuJMenuBar()
     */
    @Override
    protected JMenuBar getMenuJMenuBar() {

        // ---- FILE ----
        JMenu fileJMenu = new JMenu("   File   ");

        fileJMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem fileOpenJMenuItem = new JMenuItem("Load Pega Tracer File");

        fileOpenJMenuItem.setMnemonic(KeyEvent.VK_L);
        fileOpenJMenuItem.setToolTipText("Load Pega Tracer XML File");

        ImageIcon ii = FileUtilities.getImageIcon(this.getClass(), "open.png");

        fileOpenJMenuItem.setIcon(ii);

        fileOpenJMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                File selectedFile = getSelectedFile();

                FileFilter fileFilter = TracerViewer.getDefaultFileFilter(FILE_CHOOSER_FILTER_DESC,
                        Arrays.asList(FILE_CHOOSER_FILTER_EXT));

                File file = openFileChooser(TracerViewer.this, TracerViewer.class, FILE_CHOOSER_DIALOG_TITLE,
                        fileFilter, selectedFile);

                if (file != null) {
                    loadFile(file);
                }
            }
        });

        RecentFileJMenu recentFileJMenu = getRecentFileJMenu();

        JMenuItem clearRecentJMenuItem = new JMenuItem("Clear Recent");
        clearRecentJMenuItem.setMnemonic(KeyEvent.VK_C);
        clearRecentJMenuItem.setToolTipText("Clear Recent");

        clearRecentJMenuItem.setIcon(ii);

        clearRecentJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearRecentPreferences();
                savePreferences();
            }
        });

        JMenuItem exitJMenuItem = new JMenuItem("Exit");
        exitJMenuItem.setMnemonic(KeyEvent.VK_X);
        exitJMenuItem.setToolTipText("Exit application");

        ii = FileUtilities.getImageIcon(this.getClass(), "exit.png");

        exitJMenuItem.setIcon(ii);

        exitJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                exit(0);
            }
        });

        // fileJMenu.addSeparator();
        fileJMenu.add(fileOpenJMenuItem);
        fileJMenu.add(recentFileJMenu);
        fileJMenu.add(clearRecentJMenuItem);
        fileJMenu.add(exitJMenuItem);

        // ---- EDIT ----
        JMenu editJMenu = new JMenu("   Edit   ");
        editJMenu.setMnemonic(KeyEvent.VK_E);

        JMenuItem settingsJMenuItem = new JMenuItem("Settings");
        settingsJMenuItem.setToolTipText("Settings");

        ii = FileUtilities.getImageIcon(this.getClass(), "settings.png");
        settingsJMenuItem.setIcon(ii);

        settingsJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                TracerViewerSettingsDialog tracerViewerSettingsDialog;
                tracerViewerSettingsDialog = new TracerViewerSettingsDialog(getTracerViewerSetting(), getAppIcon(),
                        TracerViewer.this);

                if (tracerViewerSettingsDialog.isSettingUpdated()) {

                    int recentItemsCount = tracerViewerSettingsDialog.getRecentItemsCount();
                    String charset = tracerViewerSettingsDialog.getSelectedCharset();
                    boolean reloadPreviousFiles = tracerViewerSettingsDialog.isReloadPreviousFiles();

                    TracerViewerSetting tracerViewerSetting = getTracerViewerSetting();

                    tracerViewerSetting.setRecentItemsCount(recentItemsCount);
                    tracerViewerSetting.setCharset(charset);
                    tracerViewerSetting.setReloadPreviousFiles(reloadPreviousFiles);
                }
            }
        });

        editJMenu.add(settingsJMenuItem);

        // ---- HELP ----
        JMenu helpJMenu = getHelpAboutJMenu();

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileJMenu);
        menuBar.add(editJMenu);
        menuBar.add(helpJMenu);

        return menuBar;

    }

    public RecentFileJMenu getRecentFileJMenu() {

        if (recentFileJMenu == null) {

            recentFileJMenu = new RecentFileJMenu(recentFileContainer) {

                private static final long serialVersionUID = 5024129406924781237L;

                @Override
                public void onSelect(RecentFile recentFile) {

                    String fileName = (String) recentFile.getAttribute(RecentFile.KEY_FILE);

                    File file = new File(fileName);

                    if (file.exists() && file.isFile() && file.canRead()) {
                        loadFile(file);
                    } else {

                        JOptionPane.showMessageDialog(this, "File: " + file + " cannot be read.", "File not found",
                                JOptionPane.ERROR_MESSAGE);

                        getRecentFileContainer().deleteRecentFile(recentFile);
                    }

                }
            };
        }

        return recentFileJMenu;
    }

    protected void clearRecentPreferences() {
        recentFileContainer.clearRecentFilesPreferrence();
        tracerViewerSetting = new TracerViewerSetting();
        openFileList = new ArrayList<>();
    }

    protected void loadFile(List<String> fileNameList) {

        for (String filename : fileNameList) {

            LOG.info("Processing file: " + filename);

            File file = new File(filename);

            if (file.exists() && file.isFile() && file.canRead()) {
                loadFile(file);
            } else {
                LOG.info("\"" + filename + "\" is not a file.");
            }
        }
    }

    protected void loadFile(File file) {

        this.selectedFile = file;

        TraceTabbedPane traceTabbedPane = getTraceTabbedPane();

        try {

            traceTabbedPane.loadFile(selectedFile);

            saveOpenFileList();

        } catch (Exception e) {
            LOG.error("Error loading file - " + selectedFile.toString(), e);

            JOptionPane.showMessageDialog(this, (e.getMessage() + " " + selectedFile), "Error loading file: ",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void generateReport(List<String> fileNameList) {

        if (fileNameList.size() == 0) {
            LOG.info("no files to process");
        }

        for (String filename : fileNameList) {

            LOG.info("Processing file: " + filename);

            File file = new File(filename);

            if (file.exists() && file.isFile() && file.canRead()) {
                generateReport(file);
            } else {
                LOG.info("\"" + filename + "\" is not a file.");
            }
        }
    }

    private static void generateReport(File tracerFile) {
        // TODO - generate report
    }

    public static void main(final String[] args) {

        LOG.info("Default Locale: " + Locale.getDefault() + " args length: " + args.length);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {

                    if (args.length > 0) {

                        List<String> fileNameList = new ArrayList<String>();

                        boolean isReport = false;

                        Getopt getopt = new Getopt("TracerViewer", args, "f:r:h:?;");

                        int charAt;
                        String arg;

                        while ((charAt = getopt.getopt()) != -1) {

                            switch (charAt) {

                            case 'f':

                                int index = getopt.getOptind() - 1;
                                // LOG.info("index -> " + index);

                                while (index < args.length) {

                                    String next = args[index];
                                    index++;

                                    if (next.startsWith("-")) {
                                        break;
                                    } else {
                                        fileNameList.add(next);
                                    }
                                }

                                getopt.setOptind(index - 1);

                                break;

                            case 'r':
                                arg = getopt.getOptarg();
                                isReport = Boolean.parseBoolean(arg);

                                break;

                            case 'h':
                                printUsageAndExit();
                                break;

                            case '?':
                                printUsageAndExit();
                                break;

                            default:
                                LOG.info("getopt() returned " + charAt);
                                break;
                            }
                        }

                        // handle non option arguments - for ex starting using open-with menu command on
                        // windows. assume them as file names
                        for (int i = getopt.getOptind(); i < args.length; i++) {
                            String filename = args[i];
                            LOG.info("Non option arg element: " + filename + "\n");
                            fileNameList.add(filename);
                        }

                        if (isReport) {
                            generateReport(fileNameList);
                        } else {
                            TracerViewer tracerViewer = new TracerViewer();
                            tracerViewer.loadFile(fileNameList);
                        }
                    } else {
                        TracerViewer tracerViewer = new TracerViewer();
                        tracerViewer.setVisible(true);
                    }

                } catch (Exception e) {
                    LOG.error("TracerViewer error reading command line arguments.", e);
                }
            }
        });
    }

    protected static void printUsageAndExit() {
        String usageStr = "\t-f <space seperated list of file path> \n\t-r <true|false generate report, no UI \n\t-h <print command usage>";
        LOG.info("Usage Arguments: \n" + usageStr);
        System.exit(0);
    }
}
