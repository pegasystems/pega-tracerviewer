/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.awt.Color;
import java.beans.PropertyChangeSupport;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.pega.gcs.fringecommon.guiutilities.CheckBoxMenuItemPopupEntry;
import com.pega.gcs.fringecommon.guiutilities.FilterColumn;
import com.pega.gcs.fringecommon.guiutilities.FilterTableModel;
import com.pega.gcs.fringecommon.guiutilities.FilterTableModelNavigation;
import com.pega.gcs.fringecommon.guiutilities.ModalProgressMonitor;
import com.pega.gcs.fringecommon.guiutilities.RecentFile;
import com.pega.gcs.fringecommon.guiutilities.SearchTableModelEvent;
import com.pega.gcs.fringecommon.guiutilities.search.SearchData;
import com.pega.gcs.fringecommon.guiutilities.search.SearchModel;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventAlert;
import com.pega.gcs.tracerviewer.model.TraceEventKey;
import com.pega.gcs.tracerviewer.model.TraceEventType;

public class TraceTableModel extends FilterTableModel<TraceEventKey> {

    private static final long serialVersionUID = -2061492402283117131L;

    private static final Log4j2Helper LOG = new Log4j2Helper(TraceTableModel.class);

    private TracerType tracerType;

    private List<TraceEventColumn> traceEventColumnList;

    // Main list. for reference purpose only, not working on this map.
    private TreeMap<TraceEventKey, TraceEvent> traceEventMap;

    private List<TraceEventKey> traceEventKeyList;

    // large files cause hanging during search, because of getIndex call, hence building a map to store these
    private HashMap<TraceEventKey, Integer> keyIndexMap;

    // search
    private SearchData<TraceEventKey> searchData;
    private SearchModel<TraceEventKey> searchModel;

    // sets for event filters
    private Map<TraceEventType, CheckBoxMenuItemPopupEntry<TraceEventKey>> traceEventTypeCheckBoxMenuItemMap;

    // tree related variables - trace event tree node map
    private TraceEventTreeNode rootTraceEventTreeNode;
    private TraceEventCombinedTreeNode rootTraceEventCombinedTreeNode;

    // DXAPI compatible stack
    private Map<String, LinkedList<TraceEvent>> dxApiTreeBuildTraceEventMap;

    private Map<TraceEventKey, TraceEventTreeNode> traceEventTreeNodeMap;
    private Map<TraceEventKey, TraceEventCombinedTreeNode> traceEventCombinedTreeNodeMap;

    // reporting
    private List<TraceEventKey> failedEventKeyList;
    private List<TraceEventKey> exceptionEventKeyList;
    private List<TraceEventKey> alertEventKeyList;
    private List<TraceEventKey> noStartEventKeyList;
    private List<TraceEventKey> noEndEventKeyList;
    private TreeMap<Double, List<TraceEventKey>> ownElapsedEventKeyMap;
    private TreeMap<TraceEventRuleset, TreeSet<TraceEventRule>> rulesInvokedMap;

    // lookup map for generated clipboard page name, for compare purpose
    private Map<String, String[]> stepPageHierarchyLookupMap;

    private Map<Integer, List<Pattern>> stepPagePatternMap;

    private Pattern parameterisedDatapagePattern = Pattern.compile("D_.*?\\[.*?\\]");

    public TraceTableModel(RecentFile recentFile, SearchData<TraceEventKey> searchData) {

        super(recentFile);
        this.searchData = searchData;

        resetModel();

    }

    public SearchData<TraceEventKey> getSearchData() {
        return searchData;
    }

    public TracerType getTracerType() {
        return tracerType;
    }

    public void setTracerType(TracerType tracerType) {
        this.tracerType = tracerType;

        populateTraceEventColumnList();
    }

    private List<TraceEventColumn> getTraceEventColumnList() {

        if (traceEventColumnList == null) {
            traceEventColumnList = new ArrayList<>();
        }

        return traceEventColumnList;
    }

    private void populateTraceEventColumnList() {

        TracerType tracerType = getTracerType();

        traceEventColumnList = TraceEventColumn.getTraceEventColumnList(tracerType, false);

        Map<FilterColumn, List<CheckBoxMenuItemPopupEntry<TraceEventKey>>> columnFilterMap;
        columnFilterMap = getColumnFilterMap();

        for (int columnIndex = 1; columnIndex < traceEventColumnList.size(); columnIndex++) {

            TraceEventColumn traceTableModelColumn = traceEventColumnList.get(columnIndex);

            // preventing unnecessary buildup of filter map
            if (traceTableModelColumn.isFilterable()) {

                FilterColumn filterColumn = new FilterColumn(columnIndex);

                filterColumn.setColumnFilterEnabled(false);

                columnFilterMap.put(filterColumn, null);
            }
        }

        PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
        propertyChangeSupport.firePropertyChange("traceTableModel", null, null);

    }

    protected TreeMap<TraceEventKey, TraceEvent> getTraceEventMap() {

        if (traceEventMap == null) {
            traceEventMap = new TreeMap<>();
        }

        return traceEventMap;
    }

    private Map<TraceEventKey, TraceEventTreeNode> getTraceEventTreeNodeMap() {

        if (traceEventTreeNodeMap == null) {
            traceEventTreeNodeMap = new HashMap<>();
        }

        return traceEventTreeNodeMap;
    }

    private Map<TraceEventKey, TraceEventCombinedTreeNode> getTraceEventCombinedTreeNodeMap() {

        if (traceEventCombinedTreeNodeMap == null) {
            traceEventCombinedTreeNodeMap = new HashMap<>();
        }

        return traceEventCombinedTreeNodeMap;
    }

    @Override
    public void resetModel() {

        List<TraceEventKey> traceEventKeyList = getFtmEntryKeyList();
        traceEventKeyList.clear();

        HashMap<TraceEventKey, Integer> keyIndexMap = getKeyIndexMap();
        keyIndexMap.clear();

        Map<TraceEventKey, TraceEvent> traceEventMap = getTraceEventMap();
        traceEventMap.clear();

        Map<TraceEventKey, TraceEventTreeNode> traceEventTreeNodeMap = getTraceEventTreeNodeMap();
        traceEventTreeNodeMap.clear();

        Map<TraceEventKey, TraceEventCombinedTreeNode> traceEventCombinedTreeNodeMap = getTraceEventCombinedTreeNodeMap();
        traceEventCombinedTreeNodeMap.clear();

        // tree
        TraceEventTreeNode rootTraceEventTreeNode = getRootTraceEventTreeNode();
        rootTraceEventTreeNode.removeAllChildren();

        TraceEventCombinedTreeNode rootTraceEventCombinedTreeNode = getRootTraceEventCombinedTreeNode();
        rootTraceEventCombinedTreeNode.removeAllChildren();

        Map<String, LinkedList<TraceEvent>> dxApiTreeBuildTraceEventMap = getDxApiTreeBuildTraceEventMap();
        dxApiTreeBuildTraceEventMap.clear();

        Map<FilterColumn, List<CheckBoxMenuItemPopupEntry<TraceEventKey>>> columnFilterMap;
        columnFilterMap = getColumnFilterMap();

        columnFilterMap.clear();

        traceEventTypeCheckBoxMenuItemMap = new TreeMap<TraceEventType, CheckBoxMenuItemPopupEntry<TraceEventKey>>();

        TraceEventType[] values = TraceEventType.values();

        Arrays.sort(values, new Comparator<TraceEventType>() {

            @Override
            public int compare(TraceEventType o1, TraceEventType o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        for (TraceEventType traceEventType : values) {

            CheckBoxMenuItemPopupEntry<TraceEventKey> cbmipe = new CheckBoxMenuItemPopupEntry<TraceEventKey>(
                    traceEventType.toString());

            traceEventTypeCheckBoxMenuItemMap.put(traceEventType, cbmipe);
        }

        // reporting
        List<TraceEventKey> failedEventKeyList = getFailedEventKeyList();
        List<TraceEventKey> exceptionEventKeyList = getExceptionEventKeyList();
        List<TraceEventKey> alertEventKeyList = getAlertEventKeyList();
        List<TraceEventKey> noStartEventKeyList = getNoStartEventKeyList();
        List<TraceEventKey> noEndEventKeyList = getNoEndEventKeyList();

        Map<Double, List<TraceEventKey>> ownElapsedEventKeyMap = getOwnElapsedEventKeyMap();
        Map<TraceEventRuleset, TreeSet<TraceEventRule>> rulesInvokedMap = getRulesInvokedMap();

        failedEventKeyList.clear();
        exceptionEventKeyList.clear();
        alertEventKeyList.clear();
        noStartEventKeyList.clear();
        noEndEventKeyList.clear();

        ownElapsedEventKeyMap.clear();
        rulesInvokedMap.clear();

        Map<String, String[]> clipboardPageLookupMap = getStepPageHierarchyLookupMap();
        clipboardPageLookupMap.clear();

        clearSearchResults(true);

        fireTableDataChanged();
    }

    @Override
    public List<TraceEventKey> getFtmEntryKeyList() {

        if (traceEventKeyList == null) {
            traceEventKeyList = new ArrayList<TraceEventKey>();
        }

        return traceEventKeyList;
    }

    @Override
    protected HashMap<TraceEventKey, Integer> getKeyIndexMap() {

        if (keyIndexMap == null) {
            keyIndexMap = new HashMap<>();
        }

        return keyIndexMap;
    }

    // this is called from load task. hence the order is expected to be
    // sequential
    public void addTraceEventToMap(TraceEvent traceEvent) {
        addTraceEventToMap(traceEvent, false);
    }

    public void addTraceEventToMap(TraceEvent traceEvent, boolean isCompare) {

        TraceEventKey traceEventKey = traceEvent.getKey();

        Map<TraceEventKey, TraceEvent> traceEventMap = getTraceEventMap();
        traceEventMap.put(traceEventKey, traceEvent);

        List<TraceEventKey> traceEventKeyList = getFtmEntryKeyList();

        traceEventKeyList.add(traceEventKey);

        HashMap<TraceEventKey, Integer> keyIndexMap = getKeyIndexMap();
        keyIndexMap.put(traceEventKey, traceEventKeyList.size() - 1);

        // performing updateColumnFilterMap to avoid re-parsing the full map if
        // we used applyFilterEventSet(null).
        Map<FilterColumn, List<CheckBoxMenuItemPopupEntry<TraceEventKey>>> columnFilterMap = getColumnFilterMap();
        updateColumnFilterMap(traceEvent, columnFilterMap);

        TraceEventType tet = traceEvent.getTraceEventType();

        if (tet != null) {
            CheckBoxMenuItemPopupEntry<TraceEventKey> cbmipe;
            cbmipe = traceEventTypeCheckBoxMenuItemMap.get(tet);
            cbmipe.addRowIndex(traceEventKey);
        }

        // fix Issue #1 - Compare functionality not working to be overridden in TraceTableCompareModel to avoid building tree for compare
        // view
        if (!isCompare) {
            buildTree(traceEvent);
        }

        processEvent(traceEvent, isCompare);

    }

    protected void buildTree(TraceEvent currentTraceEvent) {

        String dxApiIntId = currentTraceEvent.getDxApiInteractionId();

        Map<String, LinkedList<TraceEvent>> dxApiTreeBuildTraceEventMap = getDxApiTreeBuildTraceEventMap();

        LinkedList<TraceEvent> treeBuildTraceEventList = dxApiTreeBuildTraceEventMap.get(dxApiIntId);

        if (treeBuildTraceEventList == null) {
            treeBuildTraceEventList = new LinkedList<>();
            dxApiTreeBuildTraceEventMap.put(dxApiIntId, treeBuildTraceEventList);
        }

        Map<TraceEventKey, TraceEventTreeNode> traceEventTreeNodeMap = getTraceEventTreeNodeMap();
        Map<TraceEventKey, TraceEventCombinedTreeNode> traceEventCombinedTreeNodeMap = getTraceEventCombinedTreeNodeMap();

        TraceEventTreeNode previousParentTraceEventTreeNode = null;
        TraceEventCombinedTreeNode previousParentTraceEventCombinedTreeNode = null;

        TraceEvent previousParentTraceEvent = getMatchingParentTraceEvent(currentTraceEvent, treeBuildTraceEventList);

        // if no parent found. ex if this is first entry. assign to root.
        if (previousParentTraceEvent == null) {
            previousParentTraceEventTreeNode = getRootTraceEventTreeNode();
            previousParentTraceEventCombinedTreeNode = getRootTraceEventCombinedTreeNode();
        } else {
            // get the tree nodes corresponding to key
            TraceEventKey traceEventKey = previousParentTraceEvent.getKey();

            previousParentTraceEventTreeNode = traceEventTreeNodeMap.get(traceEventKey);
            previousParentTraceEventCombinedTreeNode = traceEventCombinedTreeNodeMap.get(traceEventKey);
        }

        TraceEventColumn traceEventColumn = getColumn(0);
        String nodeName = currentTraceEvent.getColumnValueForTraceTableModelColumn(traceEventColumn);

        TraceEventTreeNode currentTraceEventTreeNode = new TraceEventTreeNode(currentTraceEvent, nodeName);
        TraceEventCombinedTreeNode currentTraceEventCombinedTreeNode = new TraceEventCombinedTreeNode(currentTraceEvent,
                nodeName);

        TraceEventKey currentTraceEventKey = currentTraceEvent.getKey();

        traceEventTreeNodeMap.put(currentTraceEventKey, currentTraceEventTreeNode);

        Boolean endEvent = currentTraceEvent.isEndEvent();

        if (endEvent != null) {
            // current event is a block event

            if (endEvent) {
                // find the correct 'begin' node. correct = same INT, RULE#,
                // EVENT_TYPE

                TraceEvent startTraceEvent = getMatchingStartTraceEvent(currentTraceEvent, treeBuildTraceEventList);

                if (startTraceEvent != null) {

                    TraceEventKey startTraceEventKey = startTraceEvent.getKey();

                    // single tree node
                    TraceEventTreeNode startTraceEventTreeNode = traceEventTreeNodeMap.get(startTraceEventKey);

                    TraceEventTreeNode parentTraceEventTreeNode = (TraceEventTreeNode) startTraceEventTreeNode
                            .getParent();
                    parentTraceEventTreeNode.add(currentTraceEventTreeNode);

                    // combined tree node
                    TraceEventCombinedTreeNode startTraceEventCombinedTreeNode = traceEventCombinedTreeNodeMap
                            .get(startTraceEventKey);

                    startTraceEventCombinedTreeNode.setEndEvent(currentTraceEvent);

                    traceEventCombinedTreeNodeMap.put(currentTraceEventKey, startTraceEventCombinedTreeNode);

                    processTraceEventElapsed(startTraceEventTreeNode, currentTraceEventTreeNode);

                } else {
                    LOG.info("Could'nt find a matching start for trace event: " + currentTraceEvent.toDebugString());

                    List<TraceEventKey> noStartEventKeyList = getNoStartEventKeyList();
                    noStartEventKeyList.add(currentTraceEventKey);

                    previousParentTraceEventTreeNode.add(currentTraceEventTreeNode);
                    previousParentTraceEventCombinedTreeNode.add(currentTraceEventCombinedTreeNode);

                    traceEventCombinedTreeNodeMap.put(currentTraceEventKey, currentTraceEventCombinedTreeNode);
                }
            } else {
                // if starting event, add to the stack
                treeBuildTraceEventList.add(currentTraceEvent);

                previousParentTraceEventTreeNode.add(currentTraceEventTreeNode);
                previousParentTraceEventCombinedTreeNode.add(currentTraceEventCombinedTreeNode);

                traceEventCombinedTreeNodeMap.put(currentTraceEventKey, currentTraceEventCombinedTreeNode);
            }
        } else {
            // current event is a singular event.
            previousParentTraceEventTreeNode.add(currentTraceEventTreeNode);
            previousParentTraceEventCombinedTreeNode.add(currentTraceEventCombinedTreeNode);

            traceEventCombinedTreeNodeMap.put(currentTraceEventKey, currentTraceEventCombinedTreeNode);

        }

    }

    private TraceEvent getMatchingParentTraceEvent(TraceEvent childTraceEvent,
            LinkedList<TraceEvent> treeBuildTraceEventList) {

        TraceEvent parentTraceEvent = null;

        Iterator<TraceEvent> descendingIterator = treeBuildTraceEventList.descendingIterator();

        while (descendingIterator.hasNext()) {

            TraceEvent traceEvent = descendingIterator.next();

            if (traceEvent.isMatchingParentTraceEvent(childTraceEvent)) {
                parentTraceEvent = traceEvent;
                break;
            }
        }

        // if no possible parent found, assign it to last start block
        if ((parentTraceEvent == null)) {
            parentTraceEvent = treeBuildTraceEventList.peekLast();
        }

        return parentTraceEvent;
    }

    private TraceEvent getMatchingStartTraceEvent(TraceEvent endTraceEvent,
            LinkedList<TraceEvent> treeBuildTraceEventList) {

        TraceEvent startTraceEvent = null;

        int loopCounter = 0;
        int treeBuildTraceEventListSize = treeBuildTraceEventList.size();

        ArrayList<TraceEvent> noEndTraceEventList = new ArrayList<>();

        Iterator<TraceEvent> descendingIterator = treeBuildTraceEventList.descendingIterator();

        while (descendingIterator.hasNext()) {

            TraceEvent traceEvent = descendingIterator.next();
            loopCounter++;

            if (traceEvent.isMatchingStartTraceEvent(endTraceEvent)) {
                startTraceEvent = traceEvent;
                break;
            } else {
                noEndTraceEventList.add(traceEvent);
            }
        }

        // remove the entry from stack
        if (startTraceEvent != null) {
            int removeIndex = treeBuildTraceEventListSize - loopCounter;
            treeBuildTraceEventList.remove(removeIndex);
        }

        if (noEndTraceEventList.size() > 0) {

            List<TraceEventKey> noEndEventKeyList = getNoEndEventKeyList();

            for (TraceEvent traceEvent : noEndTraceEventList) {

                LOG.info("Could'nt find a matching end for trace event: " + traceEvent.toDebugString());

                noEndEventKeyList.add(traceEvent.getKey());

                treeBuildTraceEventList.remove(traceEvent);

            }
        }

        return startTraceEvent;
    }

    @Override
    public int getColumnCount() {

        int columnCount = 0;

        if (traceEventColumnList != null) {
            columnCount = traceEventColumnList.size();
        }

        return columnCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        List<TraceEventKey> traceEventIndexList = getFtmEntryKeyList();

        // supply records in reverse order??.
        int reverseRowIndex = traceEventIndexList.size() - rowIndex - 1;
        TraceEventKey traceEventKey = traceEventIndexList.get(reverseRowIndex);

        // supply in right order
        // TraceEventKey traceEventKey = traceEventIndexList.get(rowIndex);

        TraceEvent traceEvent = getEventForKey(traceEventKey);

        return traceEvent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.pega.gcs.fringecommon.guiutilities.CustomJTableModel#getColumnValue(java. lang.Object, int)
     */
    @Override
    public String getColumnValue(Object valueAtObject, int columnIndex) {

        TraceEvent traceEvent = (TraceEvent) valueAtObject;

        String columnValue = null;

        if (traceEvent != null) {

            TraceEventColumn traceTableModelColumn = getColumn(columnIndex);

            columnValue = traceEvent.getColumnValueForTraceTableModelColumn(traceTableModelColumn);
        }

        return columnValue;
    }

    @Override
    public String getColumnName(int column) {

        TraceEventColumn traceTableModelColumn = getColumn(column);

        return traceTableModelColumn.getName();
    }

    @Override
    protected int getModelColumnIndex(int column) {
        return column;
    }

    public TraceEventColumn getColumn(int column) {

        List<TraceEventColumn> traceEventColumnList = getTraceEventColumnList();

        TraceEventColumn traceTableModelColumn = traceEventColumnList.get(column);

        return traceTableModelColumn;
    }

    public Set<TraceEventType> getTraceEventTypeList() {
        return traceEventTypeCheckBoxMenuItemMap.keySet();
    }

    public CheckBoxMenuItemPopupEntry<TraceEventKey> getCheckBoxMenuItem(TraceEventType traceEventType) {
        return traceEventTypeCheckBoxMenuItemMap.get(traceEventType);
    }

    // clearing the columnFilterMap will skip the below loop
    private void updateColumnFilterMap(TraceEvent traceEvent,
            Map<FilterColumn, List<CheckBoxMenuItemPopupEntry<TraceEventKey>>> columnFilterMap) {

        if (traceEvent != null) {

            Iterator<FilterColumn> fcIterator = columnFilterMap.keySet().iterator();

            while (fcIterator.hasNext()) {

                FilterColumn filterColumn = fcIterator.next();

                int columnIndex = filterColumn.getIndex();

                TraceEventColumn traceTableModelColumn = getColumn(columnIndex);

                List<CheckBoxMenuItemPopupEntry<TraceEventKey>> columnFilterEntryList;
                columnFilterEntryList = columnFilterMap.get(filterColumn);

                if (columnFilterEntryList == null) {
                    columnFilterEntryList = new ArrayList<CheckBoxMenuItemPopupEntry<TraceEventKey>>();
                    columnFilterMap.put(filterColumn, columnFilterEntryList);
                }

                String traceEventKeyStr = traceEvent.getColumnValueForTraceTableModelColumn(traceTableModelColumn);

                if (traceEventKeyStr == null) {
                    traceEventKeyStr = FilterTableModel.NULL_STR;
                } else if ("".equals(traceEventKeyStr)) {
                    traceEventKeyStr = FilterTableModel.EMPTY_STR;
                }

                CheckBoxMenuItemPopupEntry<TraceEventKey> columnFilterEntry;

                CheckBoxMenuItemPopupEntry<TraceEventKey> searchKey;
                searchKey = new CheckBoxMenuItemPopupEntry<TraceEventKey>(traceEventKeyStr);

                int index = columnFilterEntryList.indexOf(searchKey);

                if (index == -1) {
                    columnFilterEntry = new CheckBoxMenuItemPopupEntry<TraceEventKey>(traceEventKeyStr);
                    columnFilterEntryList.add(columnFilterEntry);
                } else {
                    columnFilterEntry = columnFilterEntryList.get(index);
                }

                TraceEventKey traceEventKey = traceEvent.getKey();

                columnFilterEntry.addRowIndex(traceEventKey);

                if (columnFilterEntryList.size() > 1) {
                    filterColumn.setColumnFilterEnabled(true);
                }

            }
        }
    }

    // performing one by one search because of showing progress in the monitor
    // also when cancelling the task we should keep the old search results
    // hence not search result is stored unless the task is completed
    @Override
    public boolean search(TraceEventKey key, Object searchStrObj) {

        TraceEvent traceEvent = getEventForKey(key);

        Charset charset = getCharset();

        boolean found = traceEvent.search(searchStrObj, charset);

        return found;
    }

    @Override
    protected FilterTableModelNavigation<TraceEventKey> getNavigationRowIndex(List<TraceEventKey> resultList,
            int selectedRowIndex, boolean forward, boolean first, boolean last, boolean wrap) {

        int currSelectedRowIndex = selectedRowIndex;
        // tracer viewer search results are NOT reversed
        TraceEventKey navigationKey = null;
        int navigationIndex = 0;
        int navigationRowIndex = 0;

        if ((resultList != null) && (resultList.size() > 0)) {

            int resultListSize = resultList.size();

            List<TraceEventKey> traceEventKeyList = getFtmEntryKeyList();

            int traceEventKeyListSize = traceEventKeyList.size();

            if (first) {

                int lastIndex = resultListSize - 1;
                navigationKey = resultList.get(lastIndex);
                navigationIndex = 1;

            } else if (last) {

                navigationKey = resultList.get(0);
                navigationIndex = resultListSize;

            } else if (forward) {
                // NEXT
                if (currSelectedRowIndex >= 0) {

                    if (currSelectedRowIndex < (traceEventKeyListSize - 1)) {
                        currSelectedRowIndex++;
                    } else {
                        if (wrap) {
                            currSelectedRowIndex = 0;
                        }
                    }
                } else {
                    currSelectedRowIndex = 0;
                }

                TraceEventKey currSelectedTraceEventKey = traceEventKeyList
                        .get((traceEventKeyListSize - 1) - currSelectedRowIndex);

                int searchIndex = Collections.binarySearch(resultList, currSelectedTraceEventKey);

                if (searchIndex >= 0) {
                    // exact search found
                    navigationKey = resultList.get(searchIndex);
                } else {

                    searchIndex = (searchIndex * -1) - 1;

                    if ((searchIndex == resultListSize) || (searchIndex == 0)) {

                        searchIndex = resultListSize - 1;
                    } else {
                        searchIndex--;
                    }

                    navigationKey = resultList.get(searchIndex);
                }

                navigationIndex = resultList.indexOf(navigationKey);
                navigationIndex = (resultListSize - 1) - navigationIndex + 1;

            } else {
                // PREVIOUS
                if (currSelectedRowIndex >= 0) {

                    if (currSelectedRowIndex > 0) {
                        currSelectedRowIndex--;
                    } else {
                        if (wrap) {
                            currSelectedRowIndex = traceEventKeyListSize - 1;
                        }
                    }
                } else {
                    currSelectedRowIndex = 0;
                }

                TraceEventKey currSelectedTraceEventKey = traceEventKeyList
                        .get((traceEventKeyListSize - 1) - currSelectedRowIndex);

                int searchIndex = Collections.binarySearch(resultList, currSelectedTraceEventKey);

                if (searchIndex >= 0) {
                    // exact search found
                    navigationKey = resultList.get(searchIndex);
                } else {

                    searchIndex = (searchIndex * -1) - 1;

                    if (searchIndex == resultListSize) {
                        searchIndex = 0;
                    }

                    navigationKey = resultList.get(searchIndex);
                }

                navigationIndex = resultList.indexOf(navigationKey);
                navigationIndex = (resultListSize - 1) - navigationIndex + 1;
            }

            if (navigationKey != null) {

                navigationRowIndex = getIndexOfKey(navigationKey);
                // navigationRowIndex =
                // traceEventKeyList.indexOf(navigationKey);
                //
                // navigationRowIndex = (traceEventKeyListSize - 1)
                // - navigationRowIndex;

            } else {
                navigationRowIndex = currSelectedRowIndex;
            }

        }

        FilterTableModelNavigation<TraceEventKey> ttmn = new FilterTableModelNavigation<TraceEventKey>();
        ttmn.setNavigationIndex(navigationIndex);
        ttmn.setNavigationRowIndex(navigationRowIndex);
        ttmn.setNavigationKey(navigationKey);

        return ttmn;
    }

    @Override
    /**
     * This uses treepmap's comparator which is based on traceeventkey's id
     */
    public TraceEvent getEventForKey(TraceEventKey traceEventKey) {

        TraceEvent traceEvent = null;

        if (traceEventKey != null) {
            Map<TraceEventKey, TraceEvent> traceEventMap = getTraceEventMap();
            traceEvent = traceEventMap.get(traceEventKey);
        }

        return traceEvent;
    }

    /**
     * Alternate implementation to getEventForKey to use traceeventkey's traceeventindex. used for reports where entries can change
     * because of compare view.
     */
    public TraceEvent getTraceEventForKey(TraceEventKey traceEventKey) {

        TraceEvent traceEvent = null;

        if (traceEventKey != null) {

            int traceEventIndex = traceEventKey.getTraceEventIndex();

            if (traceEventIndex != -1) {

                Map<TraceEventKey, TraceEvent> traceEventMap = getTraceEventMap();
                Set<Map.Entry<TraceEventKey, TraceEvent>> traceEventEntrySet = traceEventMap.entrySet();

                for (Map.Entry<TraceEventKey, TraceEvent> entry : traceEventEntrySet) {

                    TraceEventKey key = entry.getKey();
                    TraceEvent te = entry.getValue();

                    if (traceEventIndex == key.getTraceEventIndex()) {
                        traceEvent = te;
                        break;
                    }
                }
            } else {
                traceEvent = getEventForKey(traceEventKey);
            }

        }

        return traceEvent;
    }

    public void updateRecentFile(String charset) {

        RecentFile recentFile = getRecentFile();

        if (charset != null) {
            recentFile.setAttribute(RecentFile.KEY_CHARSET, charset);
            // change in character set will trigger reloading of file.
        }

        fireTableDataChanged();

    }

    @Override
    public void clearSearchResults(boolean clearResults) {

        getSearchModel().resetResults(clearResults);

        clearTraceEventSearchResults();
    }

    protected void clearTraceEventSearchResults() {

        Map<TraceEventKey, TraceEvent> traceEventMap = getTraceEventMap();

        if (traceEventMap != null) {

            for (Map.Entry<TraceEventKey, TraceEvent> entry : traceEventMap.entrySet()) {
                TraceEvent traceEvent = entry.getValue();
                traceEvent.setSearchFound(false);
            }
        }

        clearAbstractTraceEventTreeNode(rootTraceEventTreeNode);

        clearAbstractTraceEventTreeNode(rootTraceEventCombinedTreeNode);
    }

    private void clearAbstractTraceEventTreeNode(AbstractTraceEventTreeNode abstractTraceEventTreeNode) {

        for (Enumeration<?> e = abstractTraceEventTreeNode.children(); e.hasMoreElements();) {

            AbstractTraceEventTreeNode childNode = (AbstractTraceEventTreeNode) e.nextElement();

            clearAbstractTraceEventTreeNode(childNode);
        }

        abstractTraceEventTreeNode.setSearchFound(false);
    }

    @Override
    public int getIndexOfKey(TraceEventKey traceEventKey) {

        List<TraceEventKey> traceEventIndexList = getFtmEntryKeyList();

        int size = traceEventIndexList.size();

        int reverseIndex = -1;

        if (traceEventIndexList != null) {

            // int index = traceEventIndexList.indexOf(traceEventKey);
            int index = super.getIndexOfKey(traceEventKey);

            if (index != -1) {
                reverseIndex = size - index - 1;
            }
        }

        return reverseIndex;
    }

    @Override
    public TraceEventTreeNode getTreeNodeForKey(TraceEventKey key) {

        TraceEventTreeNode traceEventTreeNode = null;

        Map<TraceEventKey, TraceEventTreeNode> traceEventTreeNodeMap = getTraceEventTreeNodeMap();

        traceEventTreeNode = traceEventTreeNodeMap.get(key);

        return traceEventTreeNode;
    }

    public TraceEventCombinedTreeNode getTraceEventCombinedTreeNodeForKey(TraceEventKey key) {

        TraceEventCombinedTreeNode traceEventCombinedTreeNode = null;

        Map<TraceEventKey, TraceEventCombinedTreeNode> traceEventCombinedTreeNodeMap = getTraceEventCombinedTreeNodeMap();

        traceEventCombinedTreeNode = traceEventCombinedTreeNodeMap.get(key);

        return traceEventCombinedTreeNode;
    }

    private List<TraceEventKey> getFailedEventKeyList() {

        if (failedEventKeyList == null) {
            failedEventKeyList = new ArrayList<TraceEventKey>();
        }

        return failedEventKeyList;
    }

    private List<TraceEventKey> getExceptionEventKeyList() {

        if (exceptionEventKeyList == null) {
            exceptionEventKeyList = new ArrayList<TraceEventKey>();
        }

        return exceptionEventKeyList;
    }

    private List<TraceEventKey> getAlertEventKeyList() {

        if (alertEventKeyList == null) {
            alertEventKeyList = new ArrayList<TraceEventKey>();
        }

        return alertEventKeyList;
    }

    private List<TraceEventKey> getNoStartEventKeyList() {

        if (noStartEventKeyList == null) {
            noStartEventKeyList = new ArrayList<>();
        }

        return noStartEventKeyList;
    }

    private List<TraceEventKey> getNoEndEventKeyList() {

        if (noEndEventKeyList == null) {
            noEndEventKeyList = new ArrayList<>();
        }

        return noEndEventKeyList;
    }

    private TreeMap<Double, List<TraceEventKey>> getOwnElapsedEventKeyMap() {

        if (ownElapsedEventKeyMap == null) {
            ownElapsedEventKeyMap = new TreeMap<Double, List<TraceEventKey>>();
        }

        return ownElapsedEventKeyMap;
    }

    private TreeMap<TraceEventRuleset, TreeSet<TraceEventRule>> getRulesInvokedMap() {

        if (rulesInvokedMap == null) {
            rulesInvokedMap = new TreeMap<TraceEventRuleset, TreeSet<TraceEventRule>>();
        }

        return rulesInvokedMap;
    }

    private Map<String, String[]> getStepPageHierarchyLookupMap() {

        if (stepPageHierarchyLookupMap == null) {
            stepPageHierarchyLookupMap = new HashMap<>();
        }

        return stepPageHierarchyLookupMap;
    }

    private Map<Integer, List<Pattern>> getStepPagePatternMap() {

        if (stepPagePatternMap == null) {
            stepPagePatternMap = new HashMap<>();

            Integer key;
            List<Pattern> stepPagePatternList;
            Pattern pattern;

            // type 1:
            key = 1;
            stepPagePatternList = new ArrayList<>();

            // TempReportPage_947249767426383
            pattern = Pattern.compile("TempReportPage(?:_\\d+)");
            stepPagePatternList.add(pattern);

            // tempPage_EmbedReportBody_182384046637102
            pattern = Pattern.compile("tempPage_EmbedReportBody(?:_\\d+)");
            stepPagePatternList.add(pattern);

            // paramPagepyVirtualRecordEditorREResultPage133941
            pattern = Pattern.compile("paramPagepyVirtualRecordEditorREResultPage(?:\\d+)");
            stepPagePatternList.add(pattern);

            // pyVirtualRecordEditorREResultPage133941
            pattern = Pattern.compile("pyVirtualRecordEditorREResultPage(?:\\d+)");
            stepPagePatternList.add(pattern);

            // pyVirtualRecordEditorREResultPage133941METADATA
            pattern = Pattern.compile("pyVirtualRecordEditorREResultPage(?:\\d+)METADATA");
            stepPagePatternList.add(pattern);

            // D_pzRecordsEditor_pa176603094135431pz
            pattern = Pattern.compile(".*?_pa(?:\\d+)pz");
            stepPagePatternList.add(pattern);

            // pyDataSource1510679770820
            pattern = Pattern.compile("pyDataSource(?:\\d+)");
            stepPagePatternList.add(pattern);

            // RH_1
            pattern = Pattern.compile("RH_(?:\\d+)");
            stepPagePatternList.add(pattern);

            // RD_pzFilterRuleSpecificDirection_18764515930493081
            // RD_pzFilterRuleSpecificChannel_4193461107265123
            pattern = Pattern.compile("RD_pzFilterRuleSpecific[Direction|Channel](?:_\\d+)");
            stepPagePatternList.add(pattern);

            stepPagePatternMap.put(key, stepPagePatternList);

            // type 2:
            key = 2;
            stepPagePatternList = new ArrayList<>();

            // TempReportPage_D489CEC317D3C41C4377
            pattern = Pattern.compile("TempReportPage(?:_([0-9A-F]{2})+)");
            stepPagePatternList.add(pattern);

            stepPagePatternMap.put(key, stepPagePatternList);

            // type 3:
            key = 3;
            stepPagePatternList = new ArrayList<>();

            // D_BookingTypeSdList_pa01c8a18f41f24210a043ed76ff95e721pz
            pattern = Pattern.compile(".*?_pa(?:([0-9a-f]{2})+)pz");
            stepPagePatternList.add(pattern);

            stepPagePatternMap.put(key, stepPagePatternList);

        }

        return stepPagePatternMap;
    }

    private String[] getExtractSanitisedPageName(String[] stepPageArray) {

        int stepPageArrayLength = stepPageArray.length;

        String[] sanitisedStepPageArray = new String[stepPageArrayLength];

        Map<Integer, List<Pattern>> stepPagePatternMap = getStepPagePatternMap();

        for (int index = 0; index < stepPageArrayLength; index++) {

            String stepPage = stepPageArray[index];

            sanitisedStepPageArray[index] = stepPage;

            for (Integer key : stepPagePatternMap.keySet()) {

                boolean matched = false;

                List<Pattern> stepPagePatternList = stepPagePatternMap.get(key);

                switch (key) {

                // type 1 patterns where only integers are replaced
                case 1:

                    for (Pattern pattern : stepPagePatternList) {

                        Matcher matcher = pattern.matcher(stepPage);

                        // break out of pattern List loop
                        if (matcher.matches()) {
                            matched = true;
                            String sanitisedPageName = stepPage.replaceAll("\\d+", "XXXX");
                            sanitisedStepPageArray[index] = sanitisedPageName;
                            break;
                        }
                    }

                    break;

                // type 2 patterns where only Hexadecimal are replaced
                case 2:

                    for (Pattern pattern : stepPagePatternList) {

                        Matcher matcher = pattern.matcher(stepPage);

                        // break out of pattern List loop
                        if (matcher.matches()) {
                            matched = true;
                            String sanitisedPageName = stepPage.replaceAll("([0-9A-F]{2})+", "XXXX");
                            sanitisedStepPageArray[index] = sanitisedPageName;
                            break;
                        }
                    }

                    break;

                // type 3 patterns where only Hexadecimal inside pa - pz are replaced
                case 3:

                    for (Pattern pattern : stepPagePatternList) {

                        Matcher matcher = pattern.matcher(stepPage);

                        // break out of pattern List loop
                        if (matcher.matches()) {
                            matched = true;
                            String sanitisedPageName = stepPage.replaceAll("pa([0-9A-Fa-f]{2})+pz", "paXXXXpz");
                            sanitisedStepPageArray[index] = sanitisedPageName;
                            break;
                        }
                    }

                    break;

                default:

                    break;
                }

                // break out of pattern Map loop
                if (matched) {
                    break;
                }
            }

        }

        return sanitisedStepPageArray;
    }

    private void processTraceEventElapsed(TraceEventTreeNode beginNode, TraceEventTreeNode endNode) {

        TraceEvent endTE = (TraceEvent) endNode.getUserObject();

        if (endTE != null) {

            double mainElapsed = endTE.getElapsed();

            if (mainElapsed > 0) {

                double childrenElapsed = 0;

                for (Enumeration<?> e = beginNode.children(); e.hasMoreElements();) {

                    TraceEventTreeNode childNode = (TraceEventTreeNode) e.nextElement();

                    TraceEvent childTE = (TraceEvent) childNode.getUserObject();

                    double childElapsed = childTE.getElapsed();

                    if (childElapsed >= 0) {
                        childrenElapsed += childElapsed;
                    }
                }

                double ownElapsed = mainElapsed - childrenElapsed;

                endTE.setChildrenElapsed((beginNode.getChildCount() > 0) ? childrenElapsed : -1);
                endTE.setOwnElapsed(ownElapsed);

                // add ownElapsed to map for reporting
                TreeMap<Double, List<TraceEventKey>> ownElapsedEventKeyMap = getOwnElapsedEventKeyMap();

                List<TraceEventKey> traceEventKeyList = ownElapsedEventKeyMap.get(ownElapsed);

                if (traceEventKeyList == null) {
                    traceEventKeyList = new ArrayList<TraceEventKey>();
                    ownElapsedEventKeyMap.put(ownElapsed, traceEventKeyList);
                }

                traceEventKeyList.add(endTE.getKey());

            }
        }
    }

    private void processEvent(TraceEvent traceEvent, boolean isCompare) {

        TraceEventKey traceEventKey = traceEvent.getKey();

        if (traceEventKey.getTraceEventIndex() != -1) {

            List<TraceEventKey> failedEventKeyList = getFailedEventKeyList();
            List<TraceEventKey> exceptionEventKeyList = getExceptionEventKeyList();
            List<TraceEventKey> alertEventKeyList = getAlertEventKeyList();
            Map<TraceEventRuleset, TreeSet<TraceEventRule>> rulesInvokedMap = getRulesInvokedMap();
            Map<String, String[]> stepPageHierarchyLookupMap = getStepPageHierarchyLookupMap();

            // add failed events
            boolean stepStatusFail = traceEvent.isStepStatusFail();

            if (stepStatusFail) {
                failedEventKeyList.add(traceEventKey);
            }

            // add Exception events
            boolean stepStatusException = traceEvent.isStepStatusException();

            if (stepStatusException) {
                exceptionEventKeyList.add(traceEventKey);
            }

            boolean isAlertEvent = (traceEvent instanceof TraceEventAlert);

            if (isAlertEvent) {
                alertEventKeyList.add(traceEventKey);
            }

            String rulesetVersion = traceEvent.getRuleSet();

            if (rulesetVersion == null) {
                rulesetVersion = "<NULL>";
            }

            TraceEventRuleset traceEventRuleset = new TraceEventRuleset(rulesetVersion);

            TreeSet<TraceEventRule> traceEventRules = rulesInvokedMap.get(traceEventRuleset);

            if (traceEventRules == null) {
                traceEventRules = new TreeSet<TraceEventRule>();
                rulesInvokedMap.put(traceEventRuleset, traceEventRules);
            }

            // Don't need this map in compare left/right model
            if (!isCompare) {
                // extract sanitised page name
                String stepPage = traceEvent.getStepPage();

                if ((stepPage != null) && (!"".equals(stepPage))) {

                    if (!stepPageHierarchyLookupMap.containsKey(stepPage)) {

                        String[] stepPageArray = extractStepPageArray(stepPage);

                        if (stepPageArray != null) {

                            String[] sanitisedStepPageArray = getExtractSanitisedPageName(stepPageArray);

                            LOG.debug("Adding mapping for Step page: " + stepPage + " and page: "
                                    + Arrays.toString(sanitisedStepPageArray));

                            stepPageHierarchyLookupMap.put(stepPage, sanitisedStepPageArray);
                        }
                    }
                }
            }

            String insKey = traceEvent.getInsKey();

            if ((insKey != null) && (!"".equals(insKey))) {

                TraceEventType traceEventType = traceEvent.getTraceEventType();

                Color background = traceEvent.getBaseColumnBackground();

                TraceEventRule traceEventRule = new TraceEventRule(insKey, traceEventType, background);

                boolean success = traceEventRules.add(traceEventRule);

                Boolean isEndEvent = traceEvent.isEndEvent();

                // this is called after processTraceEventElapsed hence timing
                // info should be available
                if ((!success) && (isEndEvent != null) && (isEndEvent)) {

                    for (TraceEventRule ter : traceEventRules) {

                        if (ter.equals(traceEventRule)) {

                            // only increment if the entry is of same type.
                            // in case of activities, the child 'steps' and
                            // 'whens' have the activity's insKey set.
                            if (ter.getTraceEventType().equals(traceEventRule.getTraceEventType())) {
                                ter.incrementExecutionCount();
                            }

                            ter.processElapsed(traceEvent.getOwnElapsed());
                        }
                    }
                } else if (isEndEvent == null) {
                    traceEventRule.incrementExecutionCount();
                    traceEventRule.processElapsed(traceEvent.getOwnElapsed());
                }
            }
        }
    }

    public TraceEventTreeNode getRootTraceEventTreeNode() {

        if (rootTraceEventTreeNode == null) {
            rootTraceEventTreeNode = new TraceEventTreeNode(null, null);
        }

        return rootTraceEventTreeNode;
    }

    public TraceEventCombinedTreeNode getRootTraceEventCombinedTreeNode() {

        if (rootTraceEventCombinedTreeNode == null) {
            rootTraceEventCombinedTreeNode = new TraceEventCombinedTreeNode(null, null);
        }

        return rootTraceEventCombinedTreeNode;
    }

    private Map<String, LinkedList<TraceEvent>> getDxApiTreeBuildTraceEventMap() {

        if (dxApiTreeBuildTraceEventMap == null) {
            dxApiTreeBuildTraceEventMap = new HashMap<>();
        }

        return dxApiTreeBuildTraceEventMap;
    }

    @Override
    public SearchModel<TraceEventKey> getSearchModel() {

        if (searchModel == null) {

            searchModel = new SearchModel<TraceEventKey>(searchData) {

                @Override
                public void searchInEvents(final Object searchStrObj, final ModalProgressMonitor modalProgressMonitor) {

                    if ((searchStrObj != null) && (!((searchStrObj instanceof SearchEventType)
                            && searchStrObj.equals(SearchEventType.SEPERATOR))
                            || !("".equals(searchStrObj.toString())))) {

                        TraceTableSearchTask ttst = new TraceTableSearchTask(modalProgressMonitor, TraceTableModel.this,
                                searchStrObj) {

                            /*
                             * (non-Javadoc)
                             * 
                             * @see javax.swing.SwingWorker#done()
                             */
                            @Override
                            protected void done() {

                                try {
                                    List<TraceEventKey> searchResultList = get();

                                    if (searchResultList != null) {
                                        // LOG.info("TraceTableSearchTask
                                        // done "
                                        // + searchResultList.size()
                                        // + " entries found");
                                        setSearchResultList(searchStrObj, searchResultList);

                                    }
                                } catch (CancellationException ce) {
                                    LOG.info("TraceTableSearchTask cancelled: ");

                                } catch (Exception e) {
                                    LOG.error("Exception in TraceTableSearchTask", e);

                                } finally {

                                    // general fire will reload the tree,
                                    // collapsing the whole tree.
                                    // hence generating a special to identify
                                    // search action. used in
                                    // TraceTreeTableModelAdapter
                                    // fireTableDataChanged();
                                    fireTableChanged(new SearchTableModelEvent(TraceTableModel.this));

                                    modalProgressMonitor.close();
                                }
                            }
                        };

                        ttst.execute();

                    }
                }

                @Override
                public void resetResults(boolean clearResults) {

                    // clears search result on search model and reset the search
                    // panel
                    resetSearchResults(clearResults);

                    // clear search results from within trace events and tree
                    // nodes
                    clearTraceEventSearchResults();

                    // general fire will reload the tree,
                    // collapsing the whole tree.
                    // hence generating a special to identify
                    // search action. used in
                    // TraceTreeTableModelAdapter
                    // fireTableDataChanged();
                    fireTableChanged(new SearchTableModelEvent(TraceTableModel.this));
                }
            };
        }

        return searchModel;
    }

    public boolean isIncompletedTracerXML() {

        List<TraceEventKey> reportNoEndEventKeyList = getReportNoEndEventKeyList();

        int reportNoEndEventKeyListsize = reportNoEndEventKeyList.size();

        boolean incompleteTracerXML = reportNoEndEventKeyListsize > 0;

        if (incompleteTracerXML) {

            StringBuilder sb = new StringBuilder();

            for (TraceEventKey traceEventKey : reportNoEndEventKeyList) {

                if (sb.length() > 0) {
                    sb.append(", ");
                }

                sb.append(traceEventKey.toString());
            }

            String modelName = getModelName();

            LOG.info("Incomplete tracer xml: " + modelName + " - " + reportNoEndEventKeyListsize + " events - "
                    + sb.toString());
        }

        return incompleteTracerXML;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.pega.gcs.fringecommon.guiutilities.CustomJTableModel#getTableColumnModel( )
     */
    @Override
    public TableColumnModel getTableColumnModel() {

        TableColumnModel tableColumnModel = new DefaultTableColumnModel();

        for (int i = 0; i < getColumnCount(); i++) {

            TableColumn tableColumn = new TableColumn(i);

            TraceEventColumn traceTableModelColumn = getColumn(i);

            String text = traceTableModelColumn.getName();

            tableColumn.setHeaderValue(text);

            TraceTableCellRenderer ttcr = new TraceTableCellRenderer();
            ttcr.setBorder(new EmptyBorder(1, 3, 1, 1));
            ttcr.setHorizontalAlignment(traceTableModelColumn.getHorizontalAlignment());

            tableColumn.setCellRenderer(ttcr);

            int colWidth = traceTableModelColumn.getPrefColumnWidth();
            tableColumn.setPreferredWidth(colWidth);
            tableColumn.setResizable(true);

            tableColumnModel.addColumn(tableColumn);
        }

        return tableColumnModel;
    }

    public List<TraceEventKey> getReportFailedEventKeyList() {
        return Collections.unmodifiableList(getFailedEventKeyList());
    }

    public List<TraceEventKey> getReportExceptionEventKeyList() {
        return Collections.unmodifiableList(getExceptionEventKeyList());
    }

    public List<TraceEventKey> getReportAlertEventKeyList() {
        return Collections.unmodifiableList(getAlertEventKeyList());
    }

    public List<TraceEventKey> getReportNoStartEventKeyList() {
        return Collections.unmodifiableList(getNoStartEventKeyList());
    }

    public List<TraceEventKey> getReportNoEndEventKeyList() {

        List<TraceEventKey> reportNoEndEventKeyList = new ArrayList<TraceEventKey>();

        List<TraceEventKey> noEndEventKeyList = getNoEndEventKeyList();

        Map<String, LinkedList<TraceEvent>> dxApiTreeBuildTraceEventMap = getDxApiTreeBuildTraceEventMap();

        for (TraceEventKey traceEventKey : noEndEventKeyList) {
            reportNoEndEventKeyList.add(traceEventKey);
        }

        for (List<TraceEvent> treeBuildTraceEventList : dxApiTreeBuildTraceEventMap.values()) {
            for (TraceEvent traceEvent : treeBuildTraceEventList) {
                reportNoEndEventKeyList.add(traceEvent.getKey());
            }
        }

        Collections.sort(reportNoEndEventKeyList);

        return Collections.unmodifiableList(reportNoEndEventKeyList);
    }

    public List<TraceEventKey> getReportOwnElapsedEventKeyList() {

        List<TraceEventKey> reportElapsedTimeEventList = new ArrayList<TraceEventKey>();

        TreeMap<Double, List<TraceEventKey>> ownElapsedEventKeyMap = getOwnElapsedEventKeyMap();

        // select last 50 items
        int mapSize = ownElapsedEventKeyMap.size();
        int size = (mapSize > 50) ? 50 : mapSize;

        Iterator<Double> it = ownElapsedEventKeyMap.navigableKeySet().descendingIterator();

        while (it.hasNext() && size > 0) {

            Double key = it.next();
            List<TraceEventKey> traceEventKeyList = ownElapsedEventKeyMap.get(key);

            for (TraceEventKey traceEventKey : traceEventKeyList) {
                reportElapsedTimeEventList.add(traceEventKey);
            }

            size--;
        }

        return Collections.unmodifiableList(reportElapsedTimeEventList);

    }

    public Map<TraceEventRuleset, TreeSet<TraceEventRule>> getReportRulesInvokedMap() {
        return Collections.unmodifiableMap(getRulesInvokedMap());
    }

    public Map<String, String[]> getStepPageHierarchyMap() {
        return Collections.unmodifiableMap(getStepPageHierarchyLookupMap());
    }

    private String[] extractStepPageArray(String stepPage) {

        String[] stepPageArray = null;

        List<String> stepPageList = null;

        if (stepPage != null) {

            Matcher matcher = parameterisedDatapagePattern.matcher(stepPage);

            if (!matcher.matches()) {

                stepPageList = new ArrayList<>();

                String[] stepPageL1Array = stepPage.split("<--", 0);
                boolean first = true;

                for (String stepPageL1 : stepPageL1Array) {

                    if (!first) {
                        stepPageList.add("<--");
                    }

                    String[] stepPageL2Array = stepPageL1.split("\\.", 0);

                    for (String stepPageL2 : stepPageL2Array) {
                        stepPageList.add(stepPageL2);
                    }

                    first = false;
                }

            }
        }

        if (stepPageList != null) {
            stepPageArray = stepPageList.toArray(new String[stepPageList.size()]);
        }

        return stepPageArray;
    }

    public boolean isMultipleDxApi() {
        Map<String, LinkedList<TraceEvent>> dxApiTreeBuildTraceEventMap = getDxApiTreeBuildTraceEventMap();
        return dxApiTreeBuildTraceEventMap.size() > 1;

    }
}
