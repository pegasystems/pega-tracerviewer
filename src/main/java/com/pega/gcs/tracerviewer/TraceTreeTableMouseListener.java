/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;

import org.dom4j.Element;

import com.pega.gcs.fringecommon.guiutilities.BaseFrame;
import com.pega.gcs.fringecommon.guiutilities.ModalProgressMonitor;
import com.pega.gcs.fringecommon.guiutilities.RightClickMenuItem;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkAddDialog;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkDeleteDialog;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkModel;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkOpenDialog;
import com.pega.gcs.fringecommon.guiutilities.markerbar.Marker;
import com.pega.gcs.fringecommon.guiutilities.search.SearchModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTableNode;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableColumn;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.fringecommon.utilities.FileUtilities;
import com.pega.gcs.fringecommon.utilities.GeneralUtilities;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TraceTreeTableMouseListener extends MouseAdapter {

    private static final Log4j2Helper LOG = new Log4j2Helper(TraceTreeTableMouseListener.class);

    private Component mainWindow;

    private TraceTreeTable traceTreeTable;

    private Map<String, TraceXMLTreeTableFrame> traceXMLTreeTableFrameMap;

    public TraceTreeTableMouseListener(TraceTreeTable traceTreeTable, Component mainWindow) {

        this.mainWindow = mainWindow;
        this.traceTreeTable = traceTreeTable;

        traceXMLTreeTableFrameMap = new HashMap<String, TraceXMLTreeTableFrame>();
    }

    private Component getMainWindow() {
        return mainWindow;
    }

    private Map<String, TraceXMLTreeTableFrame> getTraceXMLTreeTableFrameMap() {
        return traceXMLTreeTableFrameMap;
    }

    private boolean isIntendedSource(TraceTreeTable traceTable) {

        boolean intendedSource = traceTreeTable.equals(traceTable);

        return intendedSource;

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

        if (SwingUtilities.isRightMouseButton(mouseEvent)) {

            final List<Integer> selectedRowList = new ArrayList<Integer>();

            TraceTreeTable traceTreeTable = (TraceTreeTable) mouseEvent.getSource();

            if (isIntendedSource(traceTreeTable)) {

                int[] selectedRows = traceTreeTable.getSelectedRows();

                // in case the row was not selected when right clicking then
                // based on the point, select the row.
                Point point = mouseEvent.getPoint();

                if ((selectedRows != null) && (selectedRows.length <= 1)) {

                    int selectedRow = traceTreeTable.rowAtPoint(point);

                    if (selectedRow != -1) {
                        // select the row first
                        traceTreeTable.setRowSelectionInterval(selectedRow, selectedRow);
                        selectedRows = new int[] { selectedRow };
                    }
                }

                for (int selectedRow : selectedRows) {
                    selectedRowList.add(selectedRow);
                }

                final int size = selectedRowList.size();

                if (size > 0) {

                    TraceTreeTableModelAdapter traceTreeTableModelAdapter = (TraceTreeTableModelAdapter) traceTreeTable
                            .getModel();

                    final JPopupMenu popupMenu = new JPopupMenu();

                    // expected menus - so that they can be added as per order in the last
                    RightClickMenuItem copyEventXMLMenuItem = null;
                    RightClickMenuItem exportTreeXMLMenuItem = null;
                    RightClickMenuItem copyNodeHierarchyMenuItem = null;
                    RightClickMenuItem compareEventsMenuItem = null;
                    RightClickMenuItem addBookmarkMenuItem = null;
                    RightClickMenuItem openBookmarkMenuItem = null;
                    RightClickMenuItem deleteBookmarkMenuItem = null;
                    RightClickMenuItem beginEventMenuItem = null;
                    RightClickMenuItem endEventMenuItem = null;
                    RightClickMenuItem parentEventMenuItem = null;
                    RightClickMenuItem expandNodeMenuItem = null;
                    RightClickMenuItem expandAllChildrenMenuItem = null;
                    RightClickMenuItem collapseNodeMenuItem = null;
                    RightClickMenuItem collapseAllChildrenMenuItem = null;

                    copyEventXMLMenuItem = getCopyEventXMLRightClickMenuItem(popupMenu, traceTreeTable,
                            selectedRowList);

                    exportTreeXMLMenuItem = getExportTreeXMLRightClickMenuItem(popupMenu, traceTreeTable,
                            selectedRowList);

                    addBookmarkMenuItem = getAddBookmarkRightClickMenuItem(popupMenu, selectedRowList,
                            traceTreeTableModelAdapter);

                    // setup compare between 2 rows
                    if (size == 2) {
                        compareEventsMenuItem = getCompareRightClickMenuItem(selectedRowList,
                                traceTreeTableModelAdapter);
                    }

                    if (size == 1) {

                        Integer selectedRow = selectedRowList.get(0);

                        // get node stack hierarchy
                        copyNodeHierarchyMenuItem = getNodeHierarchyRightClickMenuItem(popupMenu, selectedRow,
                                traceTreeTableModelAdapter);

                        AbstractTraceEventTreeNode abstractTraceEventTreeNode = (AbstractTraceEventTreeNode) traceTreeTableModelAdapter
                                .getValueAt(selectedRow, 0);

                        List<TraceEvent> traceEvents = abstractTraceEventTreeNode.getTraceEvents();

                        // taking the start event as key
                        TraceEvent traceEvent = (traceEvents.size() > 0) ? traceEvents.get(0) : null;

                        if (traceEvent != null) {

                            TraceEventKey key = traceEvent.getKey();

                            BookmarkModel<TraceEventKey> bookmarkModel = traceTreeTableModelAdapter.getBookmarkModel();

                            List<Marker<TraceEventKey>> bookmarkList = bookmarkModel.getMarkers(key);

                            if ((bookmarkList != null) && (bookmarkList.size() > 0)) {

                                openBookmarkMenuItem = getOpenBookmarkRightClickMenuItem(popupMenu, key, bookmarkModel);

                                deleteBookmarkMenuItem = getDeleteBookmarkRightClickMenuItem(popupMenu, key,
                                        bookmarkModel);
                            }
                        }

                        final JTree tree = traceTreeTable.getTree();

                        final TreePath treePath = tree.getPathForRow(selectedRows[0]);

                        // menus only for tree mode (not merged mode)
                        if (abstractTraceEventTreeNode instanceof TraceEventTreeNode) {

                            TraceEventTreeNode traceEventTreeNode = (TraceEventTreeNode) abstractTraceEventTreeNode;

                            beginEventMenuItem = getBeginEventRightClickMenuItem(popupMenu, traceTreeTable,
                                    traceEventTreeNode);
                            endEventMenuItem = getEndEventRightClickMenuItem(popupMenu, traceTreeTable,
                                    traceEventTreeNode);
                        }

                        parentEventMenuItem = getParentEventRightClickMenuItem(popupMenu, traceTreeTable,
                                abstractTraceEventTreeNode);

                        boolean expanded = tree.isExpanded(treePath);

                        if (!abstractTraceEventTreeNode.isLeaf() && !expanded) {
                            expandNodeMenuItem = getExpandNodeRightClickMenuItem(popupMenu, selectedRow, tree,
                                    treePath);
                        } else if (!abstractTraceEventTreeNode.isLeaf() && expanded) {
                            collapseNodeMenuItem = getCollapseNodeRightClickMenuItem(popupMenu, tree, treePath);
                        }

                        // adding menu option for child nodes
                        expandAllChildrenMenuItem = getExpandAllChildrenRightClickMenuItem(popupMenu, traceTreeTable,
                                treePath);
                        collapseAllChildrenMenuItem = getCollapseAllChildrenRightClickMenuItem(popupMenu,
                                traceTreeTable, treePath);
                    }

                    // expected order
                    if (copyEventXMLMenuItem != null) {
                        addPopupMenu(popupMenu, copyEventXMLMenuItem);
                    }

                    if (exportTreeXMLMenuItem != null) {
                        addPopupMenu(popupMenu, exportTreeXMLMenuItem);
                    }

                    if (copyNodeHierarchyMenuItem != null) {
                        addPopupMenu(popupMenu, copyNodeHierarchyMenuItem);
                    }

                    if (compareEventsMenuItem != null) {
                        addPopupMenu(popupMenu, compareEventsMenuItem);
                    }

                    if (addBookmarkMenuItem != null) {
                        addPopupMenu(popupMenu, addBookmarkMenuItem);
                    }

                    if (openBookmarkMenuItem != null) {
                        addPopupMenu(popupMenu, openBookmarkMenuItem);
                    }

                    if (deleteBookmarkMenuItem != null) {
                        addPopupMenu(popupMenu, deleteBookmarkMenuItem);
                    }

                    if (beginEventMenuItem != null) {
                        addPopupMenu(popupMenu, beginEventMenuItem);
                    }

                    if (endEventMenuItem != null) {
                        addPopupMenu(popupMenu, endEventMenuItem);
                    }

                    if (parentEventMenuItem != null) {
                        addPopupMenu(popupMenu, parentEventMenuItem);
                    }

                    if (expandNodeMenuItem != null) {
                        addPopupMenu(popupMenu, expandNodeMenuItem);
                    }

                    if (expandAllChildrenMenuItem != null) {
                        addPopupMenu(popupMenu, expandAllChildrenMenuItem);
                    }

                    if (collapseNodeMenuItem != null) {
                        addPopupMenu(popupMenu, collapseNodeMenuItem);
                    }

                    if (collapseAllChildrenMenuItem != null) {
                        addPopupMenu(popupMenu, collapseAllChildrenMenuItem);
                    }

                    popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
                }
            }
        } else if (mouseEvent.getClickCount() == 2) {

            TraceTreeTable traceTreeTable = (TraceTreeTable) mouseEvent.getSource();

            performDoubleClick(traceTreeTable);

        } else {
            super.mouseClicked(mouseEvent);
        }
    }

    private void addPopupMenu(JPopupMenu popupMenu, RightClickMenuItem rightClickMenuItem) {

        if (rightClickMenuItem != null) {
            popupMenu.add(rightClickMenuItem);
        }
    }

    private RightClickMenuItem getCopyEventXMLRightClickMenuItem(JPopupMenu popupMenu, TraceTreeTable traceTreeTable,
            List<Integer> selectedRowList) {

        RightClickMenuItem copyEventXML = new RightClickMenuItem("Copy Event XML");

        copyEventXML.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                try {

                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

                    TraceTreeTableModelAdapter traceTreeTableModelAdapter = (TraceTreeTableModelAdapter) traceTreeTable
                            .getModel();

                    Charset charset = traceTreeTableModelAdapter.getCharset();

                    List<Element> elementList = new ArrayList<Element>();

                    for (int selectedRow : selectedRowList) {

                        AbstractTraceEventTreeNode traceEventTreeNode = (AbstractTraceEventTreeNode) traceTreeTableModelAdapter
                                .getValueAt(selectedRow, 0);

                        List<TraceEvent> traceEventList = traceEventTreeNode.getTraceEvents();

                        for (TraceEvent traceEvent : traceEventList) {
                            elementList.add(traceEvent.getTraceEventRootElement(charset));
                        }
                    }

                    String elementXML = GeneralUtilities.getElementsAsXML(elementList);

                    clipboard.setContents(new StringSelection(elementXML), copyEventXML);

                } catch (Exception ex) {
                    LOG.error("Error in Copy Event XML", ex);
                } finally {
                    popupMenu.setVisible(false);
                }
            }
        });

        return copyEventXML;
    }

    private RightClickMenuItem getExportTreeXMLRightClickMenuItem(JPopupMenu popupMenu, TraceTreeTable traceTreeTable,
            List<Integer> selectedRowList) {

        RightClickMenuItem exportTreeXML = new RightClickMenuItem("Export Tree XML");

        exportTreeXML.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                try {

                    TraceTreeTableModelAdapter traceTreeTableModelAdapter = (TraceTreeTableModelAdapter) traceTreeTable
                            .getModel();

                    AbstractTraceEventTreeNode abstractTraceEventTreeNode = (AbstractTraceEventTreeNode) traceTreeTableModelAdapter
                            .getValueAt(selectedRowList.get(0), 0);

                    List<TraceEvent> traceEventList = abstractTraceEventTreeNode.getTraceEvents();

                    TraceEvent traceEvent = traceEventList.get(0);
                    String postfix = traceEvent.getLine();

                    String filePath = traceTreeTableModelAdapter.getFilePath();
                    File tracerFile = new File(filePath);

                    String fileName = FileUtilities.getNameWithoutExtension(tracerFile);
                    fileName = fileName + "-" + postfix + ".xml";
                    File currentDirectory = tracerFile.getParentFile();

                    File proposedFile = new File(currentDirectory, fileName);

                    JFileChooser fileChooser = new JFileChooser(currentDirectory);

                    fileChooser.setDialogTitle("Save XML(.xml) File");
                    fileChooser.setSelectedFile(proposedFile);
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                    FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Format", "xml");

                    fileChooser.setFileFilter(filter);

                    Component mainWindow = getMainWindow();

                    int returnValue = fileChooser.showSaveDialog(mainWindow);

                    if (returnValue == JFileChooser.APPROVE_OPTION) {

                        File exportFile = fileChooser.getSelectedFile();

                        returnValue = JOptionPane.YES_OPTION;

                        if (exportFile.exists()) {

                            returnValue = JOptionPane.showConfirmDialog(mainWindow,
                                    "Replace existing file '" + exportFile.getAbsolutePath() + "' ?", "File Exists",
                                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        }

                        if (returnValue == JOptionPane.YES_OPTION) {

                            UIManager.put("ModalProgressMonitor.progressText", "Exporting tree node xml");

                            ModalProgressMonitor modalProgressMonitor = new ModalProgressMonitor(mainWindow, "",
                                    "Exporting tree node xml (0%)                                          ", 0, 100);
                            modalProgressMonitor.setMillisToDecideToPopup(0);
                            modalProgressMonitor.setMillisToPopup(0);
                            modalProgressMonitor.setIndeterminate(true);

                            TraceEventTreeNodeXMLExportTask traceEventTreeNodeXMLExportTask;

                            traceEventTreeNodeXMLExportTask = new TraceEventTreeNodeXMLExportTask(traceTreeTable,
                                    selectedRowList, exportFile, modalProgressMonitor) {

                                @Override
                                protected void done() {

                                    Boolean success = Boolean.FALSE;
                                    try {

                                        success = get();

                                    } catch (Exception e) {
                                        LOG.error("TraceEventTreeNodeXMLExportTask erorr: ", e);
                                    } finally {

                                        modalProgressMonitor.close();
                                        System.gc();

                                        if ((success != null) && (success)) {

                                            String message = "Exported tree xml to '" + exportFile.getAbsolutePath()
                                                    + "'.\nOpen in new tab?";

                                            int nextAction = JOptionPane.showConfirmDialog(mainWindow, message,
                                                    "Open Exported xml?", JOptionPane.YES_NO_OPTION,
                                                    JOptionPane.INFORMATION_MESSAGE);

                                            if (nextAction == JOptionPane.YES_OPTION) {
                                                TracerViewer tracerViewer = TracerViewer.getInstance();
                                                tracerViewer.loadFile(exportFile);
                                            }

                                        }
                                    }
                                }

                            };

                            traceEventTreeNodeXMLExportTask.execute();

                            modalProgressMonitor.show();
                        }
                    }
                } catch (Exception ex) {
                    LOG.error("Error in Export Tree XML", ex);
                } finally {
                    popupMenu.setVisible(false);
                }

            }
        });

        return exportTreeXML;
    }

    private RightClickMenuItem getNodeHierarchyRightClickMenuItem(JPopupMenu popupMenu, Integer selectedRow,
            TraceTreeTableModelAdapter traceTreeTableModelAdapter) {

        RightClickMenuItem copyNodeHierarchy = new RightClickMenuItem("Copy Node Hierarchy");

        copyNodeHierarchy.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                try {

                    StringBuilder dataSB = new StringBuilder();
                    ArrayDeque<AbstractTraceEventTreeNode> nodeStack = new ArrayDeque<>();

                    AbstractTraceEventTreeNode abstractTraceEventTreeNode = (AbstractTraceEventTreeNode) traceTreeTableModelAdapter
                            .getValueAt(selectedRow, 0);

                    while (abstractTraceEventTreeNode != null) {

                        nodeStack.push(abstractTraceEventTreeNode);

                        abstractTraceEventTreeNode = (AbstractTraceEventTreeNode) abstractTraceEventTreeNode
                                .getParent();
                    }

                    for (AbstractTraceEventTreeNode node : nodeStack) {

                        Object userObject = node.getUserObject();

                        if ((userObject != null) && (userObject instanceof TraceEvent)) {

                            TraceEvent te = (TraceEvent) userObject;

                            if (dataSB.length() != 0) {
                                dataSB.append(" --> ");
                            }

                            if (te != null) {
                                String name = te.getName();
                                String step = te.getStep();

                                dataSB.append(name);

                                if ((step != null) && (!"".equals(step))) {
                                    dataSB.append("[Step-");
                                    dataSB.append(step);
                                    dataSB.append("]");
                                }
                            }
                        }
                    }

                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

                    clipboard.setContents(new StringSelection(dataSB.toString()), copyNodeHierarchy);

                } catch (Exception ex) {
                    LOG.error("Error in Copy Node Hierarchy", ex);
                } finally {
                    popupMenu.setVisible(false);
                }

            }
        });

        return copyNodeHierarchy;
    }

    private RightClickMenuItem getCompareRightClickMenuItem(List<Integer> selectedRowList,
            TraceTreeTableModelAdapter traceTreeTableModelAdapter) {

        RightClickMenuItem compareMenuItem = new RightClickMenuItem("Compare Events");

        compareMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                Charset charset = traceTreeTableModelAdapter.getCharset();

                StringBuilder rowStrSB = new StringBuilder();
                int size = 2; // comparing 2 rows only

                Map<String, TraceXMLTreeTableFrame> traceXMLTreeTableFrameMap;
                traceXMLTreeTableFrameMap = getTraceXMLTreeTableFrameMap();

                for (int i = 0; i < size; i++) {

                    int row = selectedRowList.get(i);
                    rowStrSB.append(row);
                    rowStrSB.append("-");
                }

                final String rowStr = rowStrSB.toString();

                TraceXMLTreeTableFrame traceXMLTreeTableFrame = traceXMLTreeTableFrameMap.get(rowStr);

                if (traceXMLTreeTableFrame == null) {

                    StringBuilder seqSB = new StringBuilder();

                    List<Element> traceEventPropertyElementList = new ArrayList<>();

                    List<String> searchStrList = new ArrayList<>();

                    List<TreeTableColumn> treeTableColumnList = new ArrayList<>();
                    treeTableColumnList.add(TreeTableColumn.NAME_COLUMN);

                    for (int i = 0; i < size; i++) {

                        int row = selectedRowList.get(i);
                        rowStrSB.append(row);
                        rowStrSB.append("-");

                        AbstractTraceEventTreeNode traceEventTreeNode = (AbstractTraceEventTreeNode) traceTreeTableModelAdapter
                                .getValueAt(row, 0);

                        List<TraceEvent> traceEventList = traceEventTreeNode.getTraceEvents();

                        for (TraceEvent traceEvent : traceEventList) {

                            Element traceEventPropertyElement = null;
                            String searchStr = null;

                            if (traceEvent != null) {

                                String traceLine = traceEvent.getLine();

                                seqSB.append(traceLine);

                                TreeTableColumn treeTableColumn = new TreeTableColumn(traceLine, String.class);
                                treeTableColumnList.add(treeTableColumn);

                                if (seqSB.length() > 0) {
                                    seqSB.append(", ");
                                }

                                Object searchStrObj = null;

                                boolean searchFound = traceEvent.isSearchFound();

                                if (searchFound) {

                                    SearchModel<TraceEventKey> searchModel = traceTreeTableModelAdapter
                                            .getSearchModel();
                                    searchStrObj = searchModel.getSearchStrObj();

                                    if (!(searchStrObj instanceof SearchEventType)) {
                                        searchStr = searchStrObj.toString();
                                    }
                                }

                                traceEventPropertyElement = traceEvent.getTraceEventRootElement(charset);

                            }

                            traceEventPropertyElementList.add(traceEventPropertyElement);
                            searchStrList.add(searchStr);

                        }
                    }

                    String modelName = traceTreeTableModelAdapter.getModelName();

                    StringBuilder titleSB = new StringBuilder();

                    titleSB.append(modelName);
                    titleSB.append(" - Properties on Page - Trace Event [");
                    titleSB.append(seqSB);
                    titleSB.append("]");

                    String title = titleSB.toString();

                    Element[] traceEventPropertyElementArray = traceEventPropertyElementList
                            .toArray(new Element[traceEventPropertyElementList.size()]);

                    String[] searchStrArray = searchStrList.toArray(new String[searchStrList.size()]);

                    TreeTableColumn[] treeTableColumnArray = treeTableColumnList
                            .toArray(new TreeTableColumn[treeTableColumnList.size()]);

                    Component mainWindow = getMainWindow();

                    traceXMLTreeTableFrame = new TraceXMLTreeTableFrame(title, traceEventPropertyElementArray,
                            searchStrArray, treeTableColumnArray, TraceEventFactory.xmlElementTableTypeMap, charset,
                            mainWindow);

                    traceXMLTreeTableFrame.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosing(WindowEvent windowEvent) {
                            super.windowClosing(windowEvent);

                            traceXMLTreeTableFrameMap.remove(rowStr);
                        }

                    });

                    traceXMLTreeTableFrameMap.put(rowStr, traceXMLTreeTableFrame);

                } else {
                    traceXMLTreeTableFrame.toFront();
                }
            }
        });

        return compareMenuItem;
    }

    private RightClickMenuItem getAddBookmarkRightClickMenuItem(JPopupMenu popupMenu, List<Integer> selectedRowList,
            TraceTreeTableModelAdapter traceTreeTableModelAdapter) {

        RightClickMenuItem addBookmarkMenuItem = new RightClickMenuItem("Add Bookmark");

        addBookmarkMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                BookmarkModel<TraceEventKey> bookmarkModel = traceTreeTableModelAdapter.getBookmarkModel();

                // add bookmark
                Map<TraceEventKey, TraceEvent> teKeyMap = new HashMap<>();

                for (int selectedRow : selectedRowList) {

                    AbstractTraceEventTreeNode traceEventTreeNode = (AbstractTraceEventTreeNode) traceTreeTableModelAdapter
                            .getValueAt(selectedRow, 0);

                    List<TraceEvent> traceEvents = traceEventTreeNode.getTraceEvents();

                    if (traceEvents != null) {

                        for (TraceEvent traceEvent : traceEvents) {
                            teKeyMap.put(traceEvent.getKey(), traceEvent);
                        }
                    }
                }

                ImageIcon appIcon = BaseFrame.getAppIcon();
                Component mainWindow = getMainWindow();

                BookmarkAddDialog<TraceEventKey> bookmarkAddDialog;
                bookmarkAddDialog = new BookmarkAddDialog<TraceEventKey>(null, bookmarkModel,
                        new ArrayList<TraceEventKey>(teKeyMap.keySet()), appIcon, mainWindow) {

                    private static final long serialVersionUID = -2139967893143937083L;

                    @Override
                    public List<Marker<TraceEventKey>> getMarkerList(List<TraceEventKey> keyList, String text) {

                        List<Marker<TraceEventKey>> markerList = new ArrayList<>();

                        for (TraceEventKey key : keyList) {

                            TraceEvent te = teKeyMap.get(key);

                            String line = te.getLine();

                            TraceEventMarker traceEventMarker;

                            traceEventMarker = new TraceEventMarker(key, text, line);

                            markerList.add(traceEventMarker);
                        }

                        return markerList;
                    }
                };

                bookmarkAddDialog.setVisible(true);

                popupMenu.setVisible(false);

            }
        });

        return addBookmarkMenuItem;
    }

    private RightClickMenuItem getOpenBookmarkRightClickMenuItem(JPopupMenu popupMenu, TraceEventKey key,
            BookmarkModel<TraceEventKey> bookmarkModel) {

        RightClickMenuItem openBookmark = new RightClickMenuItem("Open Bookmark");

        openBookmark.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                ImageIcon appIcon = BaseFrame.getAppIcon();
                Component mainWindow = getMainWindow();

                BookmarkOpenDialog<TraceEventKey> bookmarkOpenDialog;
                bookmarkOpenDialog = new BookmarkOpenDialog<>(bookmarkModel, key, appIcon, mainWindow);

                bookmarkOpenDialog.setVisible(true);

                popupMenu.setVisible(false);

            }
        });

        return openBookmark;
    }

    private RightClickMenuItem getDeleteBookmarkRightClickMenuItem(JPopupMenu popupMenu, TraceEventKey key,
            BookmarkModel<TraceEventKey> bookmarkModel) {

        RightClickMenuItem deleteBookmark = new RightClickMenuItem("Delete Bookmark");

        deleteBookmark.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                ImageIcon appIcon = BaseFrame.getAppIcon();
                Component mainWindow = getMainWindow();

                BookmarkDeleteDialog<TraceEventKey> bookmarkDeleteDialog;

                bookmarkDeleteDialog = new BookmarkDeleteDialog<>(bookmarkModel, key, appIcon, mainWindow);

                bookmarkDeleteDialog.setVisible(true);

                popupMenu.setVisible(false);

            }
        });

        return deleteBookmark;
    }

    private RightClickMenuItem getBeginEventRightClickMenuItem(JPopupMenu popupMenu, TraceTreeTable source,
            TraceEventTreeNode traceEventTreeNode) {

        RightClickMenuItem beginEvent = null;

        TraceEvent traceEvent = (TraceEvent) traceEventTreeNode.getUserObject();

        if (traceEvent != null) {

            Boolean isEndEvent = traceEvent.isEndEvent();

            // end event is true
            if ((isEndEvent != null) && isEndEvent) {

                beginEvent = new RightClickMenuItem("Select Begin Event");

                beginEvent.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {

                        TraceEventTreeNode beginTraceEventTreeNode = TracerViewerUtil
                                .getBeginTraceEventTreeNode(traceEventTreeNode);

                        if (beginTraceEventTreeNode != null) {
                            source.scrollNodeToVisible(beginTraceEventTreeNode);
                        }

                        popupMenu.setVisible(false);
                    }
                });
            }
        }

        return beginEvent;
    }

    private RightClickMenuItem getEndEventRightClickMenuItem(JPopupMenu popupMenu, TraceTreeTable source,
            TraceEventTreeNode traceEventTreeNode) {

        RightClickMenuItem endEvent = null;

        TraceEvent traceEvent = (TraceEvent) traceEventTreeNode.getUserObject();

        if (traceEvent != null) {

            Boolean isEndEvent = traceEvent.isEndEvent();

            if ((isEndEvent != null) && (!isEndEvent)) {

                endEvent = new RightClickMenuItem("Select End Event");
                endEvent.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {

                        TraceEventTreeNode endTraceEventTreeNode = TracerViewerUtil
                                .getEndTraceEventTreeNode(traceEventTreeNode);

                        if (endTraceEventTreeNode != null) {
                            source.scrollNodeToVisible(endTraceEventTreeNode);
                        }

                        popupMenu.setVisible(false);
                    }
                });
            }
        }

        return endEvent;

    }

    private RightClickMenuItem getParentEventRightClickMenuItem(JPopupMenu popupMenu, TraceTreeTable source,
            AbstractTreeTableNode abstractTreeTableNode) {

        RightClickMenuItem parentEvent = null;

        // Select Parent node
        AbstractTreeTableNode parentTreeNode = (AbstractTreeTableNode) abstractTreeTableNode.getParent();

        Object parentUserobject = parentTreeNode.getUserObject();

        if (parentUserobject != null) {

            parentEvent = new RightClickMenuItem("Select Parent Event");

            parentEvent.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {

                    source.scrollNodeToVisible(parentTreeNode);

                    popupMenu.setVisible(false);
                }
            });

            popupMenu.add(parentEvent);

        }

        return parentEvent;

    }

    private RightClickMenuItem getExpandNodeRightClickMenuItem(JPopupMenu popupMenu, Integer selectedRow, JTree tree,
            TreePath treePath) {

        RightClickMenuItem expandNode = null;

        expandNode = new RightClickMenuItem("Expand Node");

        expandNode.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                tree.expandPath(treePath);

                popupMenu.setVisible(false);

                tree.setSelectionRow(selectedRow);

            }
        });

        return expandNode;

    }

    private RightClickMenuItem getExpandAllChildrenRightClickMenuItem(JPopupMenu popupMenu, TraceTreeTable source,
            TreePath treePath) {

        RightClickMenuItem expandAllChildren = new RightClickMenuItem("Expand All Children");

        expandAllChildren.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                source.expandAll(treePath, true, 1);
                popupMenu.setVisible(false);

            }
        });

        return expandAllChildren;

    }

    private RightClickMenuItem getCollapseNodeRightClickMenuItem(JPopupMenu popupMenu, JTree tree, TreePath treePath) {

        RightClickMenuItem collapseNode = new RightClickMenuItem("Collapse Node");

        collapseNode.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                tree.collapsePath(treePath);

                popupMenu.setVisible(false);

            }
        });

        return collapseNode;
    }

    private RightClickMenuItem getCollapseAllChildrenRightClickMenuItem(JPopupMenu popupMenu, TraceTreeTable source,
            TreePath treePath) {

        RightClickMenuItem collapseAllChildren = new RightClickMenuItem("Collapse All Children");

        collapseAllChildren.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                // collapse only children
                source.expandAll(treePath, false, 0);
                popupMenu.setVisible(false);

            }
        });

        return collapseAllChildren;
    }

    private void performDoubleClick(TraceTreeTable traceTreeTable) {

        if (isIntendedSource(traceTreeTable)) {

            int row = traceTreeTable.getSelectedRow();
            final String rowStr = String.valueOf(row);

            Charset charset = ((TraceTreeTableModelAdapter) traceTreeTable.getModel()).getCharset();

            Map<String, TraceXMLTreeTableFrame> traceXMLTreeTableFrameMap = getTraceXMLTreeTableFrameMap();

            TraceXMLTreeTableFrame traceXMLTreeTableFrame = traceXMLTreeTableFrameMap.get(rowStr);

            if (traceXMLTreeTableFrame == null) {

                List<TraceEvent> traceEventList = new ArrayList<TraceEvent>();
                List<String> searchStrList = new ArrayList<String>();
                StringBuilder seqSB = new StringBuilder();
                StringBuilder modelNameSB = new StringBuilder();

                TraceTreeTableModelAdapter traceTreeTableModelAdapter;

                traceTreeTableModelAdapter = (TraceTreeTableModelAdapter) traceTreeTable.getModel();

                String modelName = traceTreeTableModelAdapter.getModelName();

                if (modelNameSB.length() > 0) {
                    modelNameSB.append(", ");
                }

                modelNameSB.append(modelName);

                SearchModel<TraceEventKey> searchModel = traceTreeTableModelAdapter.getSearchModel();
                Object searchStrObj = searchModel.getSearchStrObj();

                String searchStr = null;

                if (searchStrObj != null) {
                    if (!(searchStrObj instanceof SearchEventType)) {
                        searchStr = searchStrObj.toString();
                    }
                }

                AbstractTraceEventTreeNode traceEventTreeNode = (AbstractTraceEventTreeNode) traceTreeTableModelAdapter
                        .getValueAt(row, 0);

                List<TraceEvent> traceEvents = traceEventTreeNode.getTraceEvents();

                for (TraceEvent traceEvent : traceEvents) {
                    traceEventList.add(traceEvent);
                    searchStrList.add(searchStr);
                }

                int size = traceEventList.size();
                Element[] traceEventPropertyElementArray = new Element[size];
                String[] searchStrArray = new String[size];

                TreeTableColumn[] treeTableColumnArray = new TreeTableColumn[size + 1];
                treeTableColumnArray[0] = TreeTableColumn.NAME_COLUMN;

                for (int i = 0; i < size; i++) {

                    TraceEvent traceEvent = traceEventList.get(i);

                    String traceLine = traceEvent.getLine();

                    String columnName = traceEvent.getEventName();
                    columnName = columnName + " [" + traceLine + "]";

                    if (seqSB.length() > 0) {
                        seqSB.append(", ");
                    }

                    seqSB.append(traceLine);

                    treeTableColumnArray[i + 1] = new TreeTableColumn(columnName, String.class);

                    searchStr = searchStrList.get(i);

                    Element traceEventPropertyElement;
                    traceEventPropertyElement = traceEvent.getTraceEventRootElement(charset);

                    traceEventPropertyElementArray[i] = traceEventPropertyElement;
                    searchStrArray[i] = searchStr;
                }

                StringBuilder titleSB = new StringBuilder();

                titleSB.append(modelNameSB);
                titleSB.append(" - Properties on Page - Trace Event [");
                titleSB.append(seqSB);
                titleSB.append("]");

                String title = titleSB.toString();

                Component mainWindow = getMainWindow();

                traceXMLTreeTableFrame = new TraceXMLTreeTableFrame(title, traceEventPropertyElementArray,
                        searchStrArray, treeTableColumnArray, TraceEventFactory.xmlElementTableTypeMap, charset,
                        mainWindow);

                traceXMLTreeTableFrame.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent windowEvent) {
                        super.windowClosing(windowEvent);

                        Map<String, TraceXMLTreeTableFrame> traceXMLTreeTableFrameMap;
                        traceXMLTreeTableFrameMap = getTraceXMLTreeTableFrameMap();

                        traceXMLTreeTableFrameMap.remove(rowStr);
                    }

                });

                traceXMLTreeTableFrameMap.put(rowStr, traceXMLTreeTableFrame);
            } else {
                // bring back to focus
                traceXMLTreeTableFrame.toFront();
            }
        }
    }

    public void clearTraceXMLTreeTableFrameList() {

        Collection<TraceXMLTreeTableFrame> traceXMLTreeTableFrameList = traceXMLTreeTableFrameMap.values();

        for (TraceXMLTreeTableFrame traceXMLTreeTableFrame : traceXMLTreeTableFrameList) {
            traceXMLTreeTableFrame.dispose();
        }

        traceXMLTreeTableFrameList.clear();

    }

}
