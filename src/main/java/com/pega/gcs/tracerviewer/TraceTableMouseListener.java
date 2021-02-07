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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.dom4j.Element;

import com.pega.gcs.fringecommon.guiutilities.BaseFrame;
import com.pega.gcs.fringecommon.guiutilities.CustomJTable;
import com.pega.gcs.fringecommon.guiutilities.RightClickMenuItem;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkAddDialog;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkDeleteDialog;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkModel;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkOpenDialog;
import com.pega.gcs.fringecommon.guiutilities.markerbar.Marker;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableColumn;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.fringecommon.utilities.GeneralUtilities;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TraceTableMouseListener extends MouseAdapter {

    private static final Log4j2Helper LOG = new Log4j2Helper(TraceTableMouseListener.class);

    private Component mainWindow;

    private List<TraceTable> traceTableList;

    private Map<String, TraceXMLTreeTableFrame> traceXMLTreeTableFrameMap;

    public TraceTableMouseListener(Component mainWindow) {

        this.mainWindow = mainWindow;

        traceTableList = new ArrayList<TraceTable>();
        traceXMLTreeTableFrameMap = new HashMap<String, TraceXMLTreeTableFrame>();
    }

    private List<TraceTable> getTraceTableList() {
        return traceTableList;
    }

    private Component getMainWindow() {
        return mainWindow;
    }

    private Map<String, TraceXMLTreeTableFrame> getTraceXMLTreeTableFrameMap() {
        return traceXMLTreeTableFrameMap;
    }

    public void addTraceTable(TraceTable traceTable) {
        traceTableList.add(traceTable);
    }

    protected boolean isIntendedSource(TraceTable traceTable) {

        boolean intendedSource = traceTableList.contains(traceTable);

        return intendedSource;

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

        if (SwingUtilities.isRightMouseButton(mouseEvent)) {

            final List<Integer> selectedRowList = new ArrayList<Integer>();

            final TraceTable traceTable = (TraceTable) mouseEvent.getSource();

            if (isIntendedSource(traceTable)) {

                int[] selectedRows = traceTable.getSelectedRows();

                // in case the row was not selected when right clicking then
                // based on the point, select the row.
                Point point = mouseEvent.getPoint();

                if ((selectedRows != null) && (selectedRows.length <= 1)) {

                    int selectedRow = traceTable.rowAtPoint(point);

                    if (selectedRow != -1) {
                        // select the row first
                        traceTable.setRowSelectionInterval(selectedRow, selectedRow);
                        selectedRows = new int[] { selectedRow };
                    }
                }

                for (int selectedRow : selectedRows) {
                    selectedRowList.add(selectedRow);
                }

                int size = selectedRowList.size();

                if (size > 0) {

                    TraceTableModel traceTableModel = (TraceTableModel) traceTable.getModel();

                    JPopupMenu popupMenu = new JPopupMenu();

                    // expected menus - so that they can be added as per order in the last
                    RightClickMenuItem copyEventXMLMenuItem = null;
                    RightClickMenuItem compareEventsMenuItem = null;
                    RightClickMenuItem addBookmarkMenuItem = null;
                    RightClickMenuItem openBookmarkMenuItem = null;
                    RightClickMenuItem deleteBookmarkMenuItem = null;
                    RightClickMenuItem beginEventMenuItem = null;
                    RightClickMenuItem endEventMenuItem = null;

                    copyEventXMLMenuItem = getCopyEventXMLRightClickMenuItem(popupMenu, selectedRowList);

                    // setup compare between 2 rows
                    if (size == 2) {

                        compareEventsMenuItem = getCompareRightClickMenuItem(selectedRowList, traceTableModel);
                    }

                    addBookmarkMenuItem = getAddBookmarkRightClickMenuItem(popupMenu, selectedRowList, traceTableModel);

                    // show open and delete
                    if (size == 1) {

                        Integer selectedRow = selectedRowList.get(0);
                        TraceEvent traceEvent = (TraceEvent) traceTableModel.getValueAt(selectedRow, 0);

                        if (traceEvent != null) {

                            TraceEventKey key = traceEvent.getKey();

                            BookmarkModel<TraceEventKey> bookmarkModel = traceTableModel.getBookmarkModel();

                            List<Marker<TraceEventKey>> bookmarkList = bookmarkModel.getMarkers(key);

                            if ((bookmarkList != null) && (bookmarkList.size() > 0)) {

                                openBookmarkMenuItem = getOpenBookmarkRightClickMenuItem(popupMenu, key, bookmarkModel);

                                deleteBookmarkMenuItem = getDeleteBookmarkRightClickMenuItem(popupMenu, key,
                                        bookmarkModel);
                            }

                            beginEventMenuItem = getBeginEventRightClickMenuItem(popupMenu, traceEvent, traceTable);

                            endEventMenuItem = getEndEventRightClickMenuItem(popupMenu, traceEvent, traceTable);

                        }

                    }

                    // expected order
                    if (copyEventXMLMenuItem != null) {
                        addPopupMenu(popupMenu, copyEventXMLMenuItem);
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

                    popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
                }
            }

        } else if (mouseEvent.getClickCount() == 2) {

            TraceTable source = (TraceTable) mouseEvent.getSource();

            performDoubleClick(source);

        } else {
            super.mouseClicked(mouseEvent);
        }
    }

    private void addPopupMenu(JPopupMenu popupMenu, RightClickMenuItem rightClickMenuItem) {

        if (rightClickMenuItem != null) {
            popupMenu.add(rightClickMenuItem);
        }
    }

    protected RightClickMenuItem getCopyEventXMLRightClickMenuItem(JPopupMenu popupMenu,
            List<Integer> selectedRowList) {

        RightClickMenuItem copyEventXML = new RightClickMenuItem("Copy Event XML");

        copyEventXML.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                try {

                    List<TraceTable> traceTableList = getTraceTableList();

                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

                    String data = "";
                    boolean compareMode = traceTableList.size() > 1;
                    int counter = 1;

                    for (CustomJTable traceTable : traceTableList) {

                        TraceTableModel traceTableModel = (TraceTableModel) traceTable.getModel();

                        Charset charset = traceTableModel.getCharset();

                        List<Element> elementList = new ArrayList<Element>();

                        for (int selectedRow : selectedRowList) {

                            TraceEvent traceEvent = (TraceEvent) traceTableModel.getValueAt(selectedRow, 0);

                            if ((traceEvent != null) && (traceEvent.getTraceEventRootElement(charset) != null)) {
                                elementList.add(traceEvent.getTraceEventRootElement(charset));
                            }
                        }

                        String elementXML = GeneralUtilities.getElementsAsXML(elementList);

                        if (compareMode) {
                            data = data + "<!--- Table " + counter + "--->";
                        }

                        data = data + elementXML;

                        if (compareMode) {
                            data = data + "<!--- Table " + counter + "--->\n";
                        }

                        counter++;
                    }

                    clipboard.setContents(new StringSelection(data), copyEventXML);

                } catch (Exception ex) {
                    LOG.error("Error in Copy Event XML", ex);
                } finally {
                    popupMenu.setVisible(false);
                }
            }
        });

        return copyEventXML;
    }

    private RightClickMenuItem getCompareRightClickMenuItem(List<Integer> selectedRowList,
            TraceTableModel traceTableModel) {

        RightClickMenuItem compareMenuItem = new RightClickMenuItem("Compare Events");

        compareMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

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

                    Charset charset = traceTableModel.getCharset();

                    StringBuilder seqSB = new StringBuilder();

                    Element[] traceEventPropertyElementArray = new Element[size];
                    String[] searchStrArray = new String[size];

                    TreeTableColumn[] treeTableColumnArray = new TreeTableColumn[size + 1];
                    treeTableColumnArray[0] = TreeTableColumn.NAME_COLUMN;

                    for (int i = 0; i < size; i++) {

                        int row = selectedRowList.get(i);
                        rowStrSB.append(row);
                        rowStrSB.append("-");

                        TraceEvent traceEvent = (TraceEvent) traceTableModel.getValueAt(row, 0);

                        Element traceEventPropertyElement = null;
                        String searchStr = null;

                        if (traceEvent != null) {

                            String traceLine = traceEvent.getLine();

                            seqSB.append(traceLine);

                            treeTableColumnArray[i + 1] = new TreeTableColumn(traceLine, String.class);

                            if (seqSB.length() > 0) {
                                seqSB.append(", ");
                            }

                            Object searchStrObj = null;

                            boolean searchFound = traceEvent.isSearchFound();

                            if (searchFound) {
                                searchStrObj = traceTableModel.getSearchModel().getSearchStrObj();

                                if (!(searchStrObj instanceof SearchEventType)) {
                                    searchStr = searchStrObj.toString();
                                }
                            }

                            traceEventPropertyElement = traceEvent.getTraceEventRootElement(charset);

                        }

                        traceEventPropertyElementArray[i] = traceEventPropertyElement;
                        searchStrArray[i] = searchStr;

                    }

                    String modelName = traceTableModel.getModelName();

                    StringBuilder titleSB = new StringBuilder();

                    titleSB.append(modelName);
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
            TraceTableModel traceTableModel) {

        RightClickMenuItem addBookmarkMenuItem = new RightClickMenuItem("Add Bookmark");

        addBookmarkMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                BookmarkModel<TraceEventKey> bookmarkModel = traceTableModel.getBookmarkModel();

                // add bookmark
                Map<TraceEventKey, TraceEvent> teKeyMap = new HashMap<>();

                for (int selectedRow : selectedRowList) {

                    TraceEvent traceEvent = (TraceEvent) traceTableModel.getValueAt(selectedRow, 0);

                    if (traceEvent != null) {

                        teKeyMap.put(traceEvent.getKey(), traceEvent);
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

    private RightClickMenuItem getBeginEventRightClickMenuItem(JPopupMenu popupMenu, TraceEvent traceEvent,
            TraceTable traceTable) {

        RightClickMenuItem beginEvent = null;

        Boolean isEndEvent = traceEvent.isEndEvent();

        // end event is true
        if ((isEndEvent != null) && isEndEvent) {

            beginEvent = new RightClickMenuItem("Select Begin Event");

            beginEvent.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {

                    TraceEventKey key = traceEvent.getKey();

                    TraceTableModel traceTableModel = (TraceTableModel) traceTable.getModel();

                    TraceEventTreeNode traceEventTreeNode = traceTableModel.getTreeNodeForKey(key);

                    TraceEventTreeNode beginTraceEventTreeNode = TracerViewerUtil
                            .getBeginTraceEventTreeNode(traceEventTreeNode);

                    if (beginTraceEventTreeNode != null) {

                        TraceEvent beginTraceEvent = (TraceEvent) beginTraceEventTreeNode.getUserObject();

                        TraceEventKey beginKey = beginTraceEvent.getKey();

                        int rowNumber = traceTableModel.getIndexOfKey(beginKey);

                        if (rowNumber != -1) {
                            traceTable.setRowSelectionInterval(rowNumber, rowNumber);
                            traceTable.scrollRowToVisible(rowNumber);
                        }
                    }

                    popupMenu.setVisible(false);
                }
            });
        }

        return beginEvent;
    }

    private RightClickMenuItem getEndEventRightClickMenuItem(JPopupMenu popupMenu, TraceEvent traceEvent,
            TraceTable traceTable) {

        RightClickMenuItem endEvent = null;

        Boolean isEndEvent = traceEvent.isEndEvent();

        if ((isEndEvent != null) && (!isEndEvent)) {

            endEvent = new RightClickMenuItem("Select End Event");
            endEvent.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {

                    TraceEventKey key = traceEvent.getKey();

                    TraceTableModel traceTableModel = (TraceTableModel) traceTable.getModel();

                    TraceEventTreeNode traceEventTreeNode = traceTableModel.getTreeNodeForKey(key);

                    TraceEventTreeNode endTraceEventTreeNode = TracerViewerUtil
                            .getEndTraceEventTreeNode(traceEventTreeNode);

                    if (endTraceEventTreeNode != null) {

                        TraceEvent endTraceEvent = (TraceEvent) endTraceEventTreeNode.getUserObject();

                        TraceEventKey endKey = endTraceEvent.getKey();

                        int rowNumber = traceTableModel.getIndexOfKey(endKey);

                        if (rowNumber != -1) {
                            traceTable.setRowSelectionInterval(rowNumber, rowNumber);
                            traceTable.scrollRowToVisible(rowNumber);
                        }
                    }

                    popupMenu.setVisible(false);
                }
            });
        }

        return endEvent;

    }

    protected void performDoubleClick(TraceTable source) {

        if (isIntendedSource(source)) {

            int row = source.getSelectedRow();
            final String rowStr = String.valueOf(row);

            Charset charset = ((TraceTableModel) source.getModel()).getCharset();

            Map<String, TraceXMLTreeTableFrame> traceXMLTreeTableFrameMap = getTraceXMLTreeTableFrameMap();

            TraceXMLTreeTableFrame traceXMLTreeTableFrame = traceXMLTreeTableFrameMap.get(rowStr);

            if (traceXMLTreeTableFrame == null) {

                List<TraceTable> traceTableList = getTraceTableList();

                int size = traceTableList.size();
                Element[] traceEventPropertyElementArray = new Element[size];
                String[] searchStrArray = new String[size];

                int counter = 0;

                TreeTableColumn[] treeTableColumnArray = new TreeTableColumn[size + 1];
                treeTableColumnArray[0] = TreeTableColumn.NAME_COLUMN;

                StringBuilder seqSB = new StringBuilder();
                StringBuilder modelNameSB = new StringBuilder();

                for (CustomJTable traceTable : traceTableList) {

                    Element traceEventPropertyElement = null;
                    String searchStr = null;

                    TraceTableModel traceTableModel = (TraceTableModel) traceTable.getModel();

                    String modelName = traceTableModel.getModelName();

                    if (modelNameSB.length() > 0) {
                        modelNameSB.append(", ");
                    }

                    modelNameSB.append(modelName);

                    TraceEvent traceEvent = (TraceEvent) traceTableModel.getValueAt(row, 0);

                    if (traceEvent != null) {

                        String traceLine = traceEvent.getLine();

                        String columnName = traceEvent.getEventName();
                        columnName = columnName + " [" + traceLine + "]";

                        if (seqSB.length() > 0) {
                            seqSB.append(", ");
                        }

                        seqSB.append(traceLine);

                        treeTableColumnArray[counter + 1] = new TreeTableColumn(columnName, String.class);

                        Object searchStrObj = null;

                        boolean searchFound = traceEvent.isSearchFound();

                        if (searchFound) {
                            searchStrObj = traceTableModel.getSearchModel().getSearchStrObj();

                            if (!(searchStrObj instanceof SearchEventType)) {
                                searchStr = searchStrObj.toString();
                            }
                        }

                        traceEventPropertyElement = traceEvent.getTraceEventRootElement(charset);
                    }

                    traceEventPropertyElementArray[counter] = traceEventPropertyElement;
                    searchStrArray[counter] = searchStr;
                    counter++;
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

        Map<String, TraceXMLTreeTableFrame> traceXMLTreeTableFrameMap = getTraceXMLTreeTableFrameMap();

        Collection<TraceXMLTreeTableFrame> traceXMLTreeTableFrameList = traceXMLTreeTableFrameMap.values();

        for (TraceXMLTreeTableFrame traceXMLTreeTableFrame : traceXMLTreeTableFrameList) {
            traceXMLTreeTableFrame.dispose();
        }

        traceXMLTreeTableFrameList.clear();

    }

}
